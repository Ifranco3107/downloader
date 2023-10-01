package com.macropay.downloader.data.awsiot
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.macropay.data.dto.request.EventMQTT
//import com.macropay.dpcmacro.data.mqtt.messages.*
import com.macropay.downloader.data.preferences.Status
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.Utils
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.data.mqtt.messages.EventoFactory
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.utils.activities.Dialogs
import com.macropay.downloader.utils.policies.Restrictions
import com.macropay.utils.FileMgr
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplyMsgMqtt
@Inject constructor (@ApplicationContext  val context: Context) {
    private val TAG = "ApplyMsgMqtt"

    @Inject
    lateinit var eventoFactory: EventoFactory

    var messages: MutableList<EventMQTT> = ArrayList()
    private val myListMutex = Mutex()

    var bProcessing = false
    var ultimaExecucion =  LocalDateTime.now()
    val SECONDS_DELAY = 7_000L
    var handlerMsgs = Handler(Looper.getMainLooper())
    var countGral =0


    @Inject
    lateinit var restrinctions: Restrictions
    var requiereSaving= false
    fun addMessage(msg: String?) {
        try {
            val jsonMessage = JSONObject(msg)
            val action = jsonMessage.getString("action")
            var orden  = jsonMessage.getLong("orden")

            Log.msg(TAG, "[addMessage] -------------[ $action $orden -- $jsonMessage - processing; $bProcessing - ${messages.size} eventos ]-------------------")
            var existsId = messages.find { it.orden ==orden  }

            //<==========
            //Agrega al lista de mensajes.
            if(existsId == null){
               // Log.msg(TAG, "[addMessage] agrego orden $orden")
                messages.add(EventMQTT(action, jsonMessage, false))
                // Guarda el mensaje, solo si llega un mensaje de Actualizacion de Macrolock,
                // para que continue procesando despues de que se inicie la app.
                if(!requiereSaving) requiereSaving = (action.equals("1004"))
                if(requiereSaving)  save(msg!!)
            }else
                Log.msg(TAG, "[addMessage] ya existe orden $orden")

            //Si aun no termina de instalarse, ignora los mensajes.
            if(Status.currentStatus.ordinal < Status.eStatus.TerminoEnrolamiento.ordinal){
                Log.msg(TAG,"[addMessage] Aun no termina el enrolamiento.- currentStatus: "+Status.currentStatus)
                if(msg!!.contains("ForceMQTT")) {
                    Dialogs.playSound(context)
                    Log.msg(TAG,"[addMessage] ***** ForceMQTT *****")
                    Status.currentStatus= Status.eStatus.TerminoEnrolamiento
                }
                return
            }
            var segs = Utils.tiempoTranscurrido(ultimaExecucion, ChronoUnit.SECONDS).toInt()

            if(segs > SECONDS_DELAY)  bProcessing = false

            //Espera 5 segundos, para recibir todos los mensajes encolados.
            if (!bProcessing) {
                bProcessing = true
                ultimaExecucion =  LocalDateTime.now()

                handlerMsgs.postDelayed({
                    Log.msg(TAG,"[addMessage] ************** va iniciar a procesar **********")
                    procesaMsgs2(messages)
                }, SECONDS_DELAY)

                //addFakes()  //Solo para pruebas...
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "addMessage", ex.message)
        }
    }
    suspend fun modifyMyList(block: MutableList<EventMQTT>.() -> Unit) {
        try{
            myListMutex.withLock { messages.block() }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"modifyMyList",ex.message)
        }
        catch (ex:ConcurrentModificationException){
            ErrorMgr.guardar(TAG,"modifyMyList[concurrent]",ex.message)
        }
    }

    private fun procesaMsgs2(eventos: MutableList<EventMQTT>) {
        Log.msg(TAG, "[procesaMsgs] " + messages.size + " msgs")
        var count = 0
        if(!Settings.initialized()) Settings.init(context)
        //if(mqtt.mqttManager!! != null)
        //  mqtt.mqttManager!!.disconnect()

        dpcValues.mqttAWS!!.disconnect()
        try{
            //ORdena los mensajes en orden cronologico
            eventos.sortWith(Comparator.comparingLong( { obj: EventMQTT -> obj.orden } ))
            Log.msg(TAG, "[procesaMsgs] -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+--+-+-+")
            Log.msg(TAG, "[procesaMsgs] Inicia --  mensajes: "+ messages.size + " msgs")
            CoroutineScope(Dispatchers.Main)
                .launch {
                    var actionAnterior = "1005"
                    messages.forEach {
                            count++

                            Log.msg(TAG, "[procesaMsgs] Evento No. $count.- [${it.orden}] action: ${it.action} anterior: [$actionAnterior]")
                            if( "1001,1002,1003".contains(actionAnterior) && count >1)
                                esperar(it.orden)
                            actionAnterior = it.action

                            //Aplica la accion del evento.
                            applyEvento(it)

                            //Marca como procesado
                            it.isProcesado = true
                            delete(it.orden)

                            suspend {
                                delay(1_000)
                            }
                    }
                    /// [termino el foreacj]++++++++++++++++++++
                    Log.msg(TAG,"[procesaMsgs] <========[ Termino forEach ]========> eventos procesados: ${ eventos.size} messages: ${messages.size}")
                    bProcessing = false
                    requiereSaving= false
                   //messages.clear()
                    modifyMyList {  messages.removeIf(){
                        it.isProcesado == true} }


                    //TODO: 23May2023 - se reconecta para volver a recibir mensajes de MQTT
                    // Reconecta mqtt. --
                    dpcValues.mqttAWS!!.connect(TAG)
/*                  messages.removeIf(){
                        it.isProcesado == true}*/
                    Log.msg(TAG,"[procesaMsgs] <========[ Termino de limpiar]]========>")
            } //launch

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"procesaMsgs2",ex.message)
        }
        catch (ex:ConcurrentModificationException){
            ErrorMgr.guardar(TAG,"procesaMsgs[concurrent]",ex.message)
        }

        //   Log.msg(TAG,"[procesaMsgs] <========[ Termino for - 3]========>")
    }

    fun  applyEvento(evento:EventMQTT){
        try{
            val lastMsgProcessed:Long = Settings.getSetting(Cons.KEY_LAST_MSG_PROCESSED,0L)
            if(evento.orden<=lastMsgProcessed) {
                Log.msg(TAG,"[applyEvento] PROCESADO ANTERIORMENTE. "+evento.orden)
                return
            }
            Log.msg(TAG, "[applyEvento] - 1 - Inicio - ${evento.action} orden: " + evento.orden + "--->>>>")
            val event = eventoFactory.getEvento(evento.action, context)
            Log.msg(TAG, "[applyEvento] - 2 - Inicio event.execute " + evento.orden)

            //Procesa el evento...
            event.execute(evento)
            Log.msg(TAG, "[applyEvento] - 3 - ${evento.action} TERMINO <<<<----- " + evento.orden)
            Settings.setSetting(Cons.KEY_LAST_MSG_PROCESSED,evento.orden)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"applyEvento",ex.message)
        }
    }




    private suspend fun esperar(orden: Long,){
        try{
            //Espera para dar espacio a que termine el evento anterior y no se encimen los procesos.
            for (i in 1..5){
               // Log.msg(TAG,"[esperar] DELAY - Espera que termine evento anterior $i segs. ["+orden +"]")
                delay(1_000)
            }
            //Limpia variables, para que inicie correctamente este nuevo evento.
            dpcValues.timerMonitor!!.enabledKiosk(false, null, null)
            Kiosko.kioskRequired =false


            for (i in 1..3){
                //Log.msg(TAG,"[esperar] DELAY - Espera que termine timer.., $i segs. ["+orden +"]")
                delay(1_000)
            }

        Log.msg(TAG,"[esperar] DELAY - Termino espera ["+orden +"]" )
        }catch (ex:Exception){

            ErrorMgr.guardar(TAG,"esperar",ex.message)
        }
    }

    private fun save(msg:String) {

        try {
            val jsonMessage = JSONObject(msg)
            Log.msg(TAG,"[save] jsonMessage: $jsonMessage")
            val msgId = jsonMessage.getString("orden")
            Log.msg(TAG,"[save] msgId: "+msgId)

            FileMgr.saveFile("msg_" + msgId, msg, context)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"save",ex.message)
        }
    }
    private fun delete(msgId:Long){
        Log.msg(TAG,"[delete] msgId: "+msgId)
        try {
            FileMgr.eliminar("msg_" + msgId, context)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"delete",ex.message)
        }
    }




    //-----------------------------------------------------------------
    //fun procesar():Int= runBlocking{
    private fun apply(evento:EventMQTT){
        val lastMsgProcessed:Long = Settings.getSetting(Cons.KEY_LAST_MSG_PROCESSED,0L)
        //Log.msg(TAG,"[apply] evento.orden: "+evento.orden +" last_msg $lastMsgProcessed")
        if(evento.orden<=lastMsgProcessed) {
            Log.msg(TAG,"[apply] PROCESADO ANTERIORMENTE.")
            return
        }
        try{
            // launch {
            //val success = async(Dispatchers.Main) {
            //   val success = withContext(Dispatchers.Main)     {

            Log.msg(TAG, "[apply] - 1 - Inicio - ${evento.action} orden: ultimo: $lastMsgProcessed actual" + evento.orden + "--->>>>")
            val event = eventoFactory.getEvento(evento.action, context)
            Log.msg(TAG, "[apply] - 2 - Inicio event.execute " + evento.orden)
            //Procesa el evento...
            event.execute(evento)
            Log.msg(TAG, "[apply] - 3 - termino event.execute " + evento.orden)

            // } //withContext
            //success.await()
            //Termino de aplicar el evento...
            Log.msg(TAG, "[apply] - 4 - ${evento.action} TERMINO <<<<----- " + evento.orden)

            Settings.setSetting(Cons.KEY_LAST_MSG_PROCESSED,evento.orden)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"apply",ex.message)
        }
    }
    private fun clean(evento:EventMQTT):Int{
        var msgsCount=0
        var ln  =0
        try{
            //Limpia el evento.
            msgsCount = messages.size
            Log.msg(TAG,"[clean] - 5 - Limpia evento ${evento.action} - $msgsCount msgs ")
            ln = 1
/*            if (messages.size > 0)
                messages.remove(evento) //.remove(0);*/
            //
            ln = 2
            //TODO: 28Abr23 - No se necesita borrar..  delete(evento.orden)
            ln = 3
            msgsCount = messages.size
            ln = 4
            Log.msg(TAG,"[clean] - 6 - FALTAN: [ $msgsCount mensajes]")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"clean $ln",ex.message)
        }

        return msgsCount
    }
    private fun addFakes(){
        //TODO
        /* if(countGral==0)
         {
             countGral++

             handlerMsgs.postDelayed({
                 //Log.msg(TAG,"[addMessage] ************** va iniciar a procesar **********")
                 //TDDO:Temporal para pruebas...
                 addTemp(orden+1,1001)
             }, SECONDS_DELAY+500)


             handlerMsgs.postDelayed({
                 //Log.msg(TAG,"[addMessage] ************** va iniciar a procesar **********")
                 //TDDO:Temporal para pruebas...
                 addTemp(orden+2,1002)
             }, SECONDS_DELAY+800)
         }*/
    }
    private fun addTemp(orden:Long,accion:Int){
        var newOrden = orden+1
        val strnsg = "{\n" +
                "  \"action\":" +accion+",\n" +
                "  \"orden\": " + newOrden+",\n" +
                "  \"trans_id\": 6260,\n" +
                "  \"user_id\": 10,\n" +
                "  \"lock_id\": 2,\n" +
                "  \"restrictions\": [\n" +
                "    {\n" +
                "      \"name\": \"show_kiosk\",\n" +
                "      \"params\": [\n" +
                "        {\n" +
                "          \"name\": \"tipo_bloqueo\",\n" +
                "          \"type\": \"String\",\n" +
                "          \"value\": \"PorCredito\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"current_balance\",\n" +
                "          \"type\": \"int\",\n" +
                "          \"value\": 0\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"loan_message\",\n" +
                "          \"type\": \"String\",\n" +
                "          \"value\": \"Hola *@nombreCliente**, detectamos que tu equipo fue bloqueado por falta de pago.\\n¡No pierdas la comunicación!, te invitamos a realizar tu pago desde la app, en nuestras tiendas Macropay o en la tienda de tu preferencia para poder seguir disfrutando de tu equipo.\\nRecuerda que si ya realizaste tu pago, debes de estar conectado a Wifi o contar con datos móviles.\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"enabled\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"disable_screen_capture\",\n" +
                "      \"params\": [\n" +
                "        \n" +
                "      ],\n" +
                "      \"enabled\": true\n" +
                "    }\n" +
                "  ],\n" +
                "  \"imei\": \"352355710819717\",\n" +
                "  \"id_dispositivo_estatus_pasos\": 0\n" +
                "}"

        //  handlerMsgs.postDelayed({
        Log.msg(TAG,"[addMessage] ************** va agregar Mensaje FAKE: $newOrden")
        addMessage(strnsg )
        //}, 1_000)

    }
}



/*
    private fun procesaMsgs() {
        Log.msg(TAG, "[procesaMsgs] " + messages.size + " msgs")
        var count = 0
        if(!Settings.initialized()) Settings.init(context)

        //ORdena los mensajes en orden cronologico
        messages.sortWith(Comparator.comparingLong( { obj: EventMQTT -> obj.orden } ))
        Log.msg(TAG, "[procesaMsgs] -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+--+-+-+")
        Log.msg(TAG, "[procesaMsgs] Inicia --  mensajes: "+ messages.size + " msgs")
        // Log.msg(TAG, "[procesaMsgs] -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+--+-+-+")
        //val myJob = Job()
        // GlobalScope.launch {

        runBlocking {
              withContext(Dispatchers.Main) {
            val myContext = Job() + Dispatchers.Default
            for (evento in messages) {
                try {


                    //Aplica el evento...
                    //  var result = async(myContext) {
                    launch(myContext) {
                        count++
                        val action = evento.action
                        Log.msg(TAG,"[procesaMsgs] ======================================")
                        Log.msg(TAG,"[procesaMsgs] PROCESANDO: mensaje: " +count  +" action: "+ action +" ["+evento.orden +"]")

                        if( "1001,1002,1003".contains(action) && count >1){
                            Log.msg(TAG,"[procesaMsgs] DELAY - Espera 10 segs-> a-a-> ["+evento.orden +"]")
                            //Limpia variables, para que inicie correctamente este nuevo evento.
                            dpcValues.timerMonitor!!.enabledKiosk(false, null, null)
                            Kiosko.kioskRequired =false

                            //Espera para dar espacio a que termine el evento anterior y no se encimen los procesos.
                            delay(7_000)
                            Log.msg(TAG,"[procesaMsgs] DELAY - Termino espera ["+evento.orden +"]" )


                        }
                        Log.msg(TAG,"[procesaMsgs] apply - inicio ["+evento.orden +"]" )
                        apply(evento)
                        Log.msg(TAG,"[procesaMsgs] apply - Termino ["+evento.orden +"]" )
                        //Elimina el evento de la lista
                        val msgsCount=   clean(evento)
                        Log.msg(TAG,"[procesaMsgs] delete - Termino ["+evento.orden +"]" )

                    }

                    //  result.await()/**/
                    Log.msg(TAG,"[procesaMsgs] Termino {launch} ["+evento.orden +"]")
                } catch (e: Exception) {
                    ErrorMgr.guardar(TAG, "procesaMsgs", e.message)
                }

            } //For

            Log.msg(TAG,"[procesaMsgs] <========[ Termino for - 1 ]========>")
              } //Withcontext
            Log.msg(TAG,"[procesaMsgs] <========[ Termino de aplicar mensajes - [for]  ]========>")
            bProcessing = false
            requiereSaving= false
        } //launch
        //   Log.msg(TAG,"[procesaMsgs] <========[ Termino for - 3]========>")
    }
*/