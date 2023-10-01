package com.macropay.downloader.data.mqtt.messages

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.usecases.SendLogs
import com.macropay.downloader.data.preferences.Status
import com.macropay.downloader.data.preferences.TipoBloqueo
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.ui.common.mensajes.ToastDPC
import com.macropay.downloader.ui.provisioning.ResetCveActivity
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.logs.Tracker
import com.macropay.downloader.utils.activities.Dialogs
import com.macropay.downloader.utils.policies.Restrictions
import com.macropay.utils.broadcast.Sender
import com.macropay.utils.network.Red
import com.macropay.utils.preferences.Kiosko
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class Commands
@Inject constructor(@ApplicationContext val  context: Context,
                    val sendLogs: SendLogs,
                    val restrictions: Restrictions
) : IEvento  {
//I{
//  "action": 9001,
//  "orden": 1674258804937,
//  "command_id": "1",
//  "params": {
//    "name": "hola",
//    "value": "100",
//    "type": "string"
//  }
//}
    // ,    val sendLockStatus: SendLockStatus
    //
/*   @Inject
    lateinit var restoreRestrictions: RestoreRestrictions*/

    var TAG = "Commands"
    override  fun execute(msg: EventMQTT):  Boolean  {
        Log.msg(TAG, "commands: $msg")
        var evento = ""
        var supportAction = 0
        var data: JsonObject? = null
        try {
            val jsonParser = JsonParser()
            val gsonObject = jsonParser.parse(msg.message.toString()) as JsonObject
            data = gsonObject
            Log.msg(TAG, "data: \n$data")
            var commandId     = if (msg.message.has("command_id"))  msg.message.getString("command_id") else "0"
            var supportParams : JSONObject? = if (msg.message.has("params")) (msg.message.getJSONObject("params") ) else null
            Log.msg(TAG, "supportParams: \n$supportParams")
            Log.msg(TAG,"supportActiond: "+commandId)

            ErrorMgr.guardar(TAG,"Recibio mensaje: ","supportParams: $supportParams commandId: $commandId")
            supportAction = commandId.toInt()

            when (supportAction) {
                0 -> { showToast("test de conexion...[$supportAction]") }
                1 -> { sendLogs(supportParams) }
                2 -> { sendStatus() }
                3 -> { restart(supportParams) }
                4 -> { setStatus() }
                5 -> { startMQTT() }
                6 -> { applySetting(supportParams) }
                7 -> { getSetting(supportParams) }
                8 -> { showIcon() }
                9 -> { lastLocation()}
                10-> { ResetClave() }
                    else -> return false
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "commands: [supportAction: $supportAction]", ex.message)
        }
        return true
    }

    fun getMsgEvent(idComand:String, name:String, value:String, tipo:String):EventMQTT{
        Log.msg(TAG, "[getMsgEvent]")
        var eventMQTT: EventMQTT? = null
        try{
            var  evento = JSONObject()
            evento.put("command_id",idComand)
            evento.put("params", getMsgParams(name,value,tipo))
            eventMQTT =  EventMQTT("",evento,false)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "getMsgEvent", ex.message)
        }
        return eventMQTT!!
    }

    fun getMsgParams( name:String, value:String, tipo:String):JSONObject{
        Log.msg(TAG, "[getMsgParams]")
        var  params = JSONObject()
        try{
            params.put("name",name)
            params.put("value",value)
            params.put("type",tipo)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "getMsgParams", ex.message)
        }
        return params
    }
    private fun showToast(msg:String){
        try{
            Log.msg(TAG, "[showToast][0] msg: "+msg)
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                ToastDPC.showToast(context, msg)
                Dialogs.playSound(context)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "commands: [showToast]", ex.message)
        }
    }

    fun sendLogs(supportParams: JSONObject?) {
        Log.msg(TAG, "[sendLogs][1] Enviar Logs")
        if(!Red.isOnline) return
        try{
            var parametro = "0"
            if(supportParams!!.has("name")) parametro = supportParams!!.getString("name")
            var fileLog = Log.fileName()
            if (!parametro.isEmpty() && isNumeric( parametro))
                fileLog = Log.getFilenameLog(Integer.valueOf( parametro))

            Log.msg(TAG,"[sendLogs] fileLog: "+fileLog)
            if (!fileLog.isEmpty()){
               val fileToSend = copyFile(fileLog)

                //Espera a que copie..
                var handlerLock = Handler(Looper.getMainLooper())
                handlerLock.postDelayed({
                    Log.msg(TAG,"[sendLogs] Inicio- Ejecutar sendLogs.send()..." +fileToSend.absolutePath)
                    sendLogs.send(fileToSend)
                    Log.msg(TAG,"[sendLogs] termino...")
                    Tracker.status("support","LogFile","envio log: $ fileLog")
                },4_000);


            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "commands: sendLogs", ex.message)
        }
    }
    private fun copyFile(fileName:String):File{
        val file = File(fileName)
        val tempNane = fileName.replace(".log","_tmp.log")
        Log.msg(TAG,"[copyFile] tempNane: "+tempNane)
        val fileCopied = File(tempNane)
        try{
            Log.msg(TAG,"[copyFile] va borrar el archivo..."+fileCopied.absolutePath)
            fileCopied.delete()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"copyFile[1]",ex.message)
        }

        //Copia el archivo, para que no tenga conflicto de uso.
        file.copyTo(fileCopied,true)
        return fileCopied
    }
    fun sendStatus(){
        Log.msg(TAG, "[sendStatus][2] Envia status del Telefono a central")
        try{
            Log.msg(TAG,"[sendStatus]")
            Sender.sendRemoteCommand(context, 3, "", "")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "commands: sendStatus", ex.message)
        }
    }
    private fun startMQTT(){
        Log.msg(TAG, "[startMQTT][5] inicia MQTT")
        try{
            Log.msg(TAG, "Reintenta conectar el MQTT")
            dpcValues.mqttAWS!!.connect(TAG)
            Tracker.status("support","startMQTT","reinicio mqtt" )
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "commands: startMQTT", ex.message)
        }
    }


    fun restart(supportParams: JSONObject?){
        Log.msg(TAG, "[restart][3] Realizar Reboot ")
        try{
            var supportAction:Int = 1
            var parametro = "restart"
            if(supportParams!!.has("name")) parametro = supportParams!!.getString("name")
            when(parametro){
                "restart" -> supportAction = 1
                "reboot" -> supportAction = 2
                else -> supportAction = 1
            }
            Log.msg(TAG,"[restart] parametro: "+parametro)
            Tracker.status("support","restart","accion: $parametro" )
            Sender.sendRemoteCommand(context, supportAction, "", "")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "commands: restart", ex.message)
        }
    }
    private fun setStatus(){
        try{
            Log.msg(TAG, "[setStatus][4] forzar status TerminoEnrolamiento")
            Status.currentStatus = Status.eStatus.TerminoEnrolamiento
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "commands: setStatus", ex.message)
        }
    }

    fun applySetting(supportParams: JSONObject?): Boolean {
        Log.msg(TAG, "[applySetting][6] Cambia Settings.")
        try {
            val parametro = supportParams!!.getString("name")
            val valor = supportParams!!.getString("value")
            val tipoValor = supportParams!!.getString("type")
            Log.msg(TAG,"parametro: "+parametro)
            Log.msg(TAG,"valor: "+valor)
            Log.msg(TAG,"tipoValor: "+tipoValor)
            when (tipoValor) {
                "string" ->
                    Settings.setSetting(parametro, valor)
                "int", "integer" ->
                    Settings.setSetting(parametro, valor.toInt())
                "long" ->
                    Settings.setSetting(parametro, valor.toLong())
                "bool", "boolean" ->
                    Settings.setSetting(parametro, java.lang.Boolean.parseBoolean(valor))
                else -> {
                    Settings.setSetting(parametro, valor)
                    return true
                }
            }
            Tracker.status("support","setting","parametro: $parametro  $valor $tipoValor" )
        } catch (ex: Exception) {
        ErrorMgr.guardar(TAG, "settings:  supportAction:", ex.message)
        }
        return true
    }
    fun getSetting(supportParams: JSONObject?): Boolean {
        Log.msg(TAG, "[getSetting][7] get Settings.")
        try {
            val parametro = supportParams!!.getString("name")
            val valor = supportParams!!.getString("value")
            val tipoValor = supportParams!!.getString("type")


            Log.msg(TAG,"parametro: "+parametro)
            Log.msg(TAG,"valor: "+valor)
            Log.msg(TAG,"tipoValor: "+tipoValor)
            var msg = "parametro: $parametro="
            when (tipoValor) {
                "string" ->{
                    val curValue:String =   Settings.getSetting(parametro, valor)
                    msg += "["+curValue +"]"
                }
                "int", "integer" ->{
                    val curValue : Int = Settings.getSetting(parametro, valor.toInt())
                    msg += "["+curValue +"]"
                }
                "long" ->{
                    val  curValue :Long = Settings.getSetting(parametro, valor.toLong())
                    msg += "["+curValue +"]"
                }

                "bool", "boolean" ->{
                    val curValue:Boolean =  Settings.getSetting(parametro, java.lang.Boolean.parseBoolean(valor))
                    msg += "["+curValue +"]"
                }

                else -> {
                    val curValue:String =   Settings.getSetting(parametro, valor)
                    msg += "["+curValue +"]"
                    return true
                }
            }

            Log.msg(TAG,"[getSetting] $msg" )
            showToast(msg)

            //usa el ErrorMgr, para Enviar a central....
            msg =  msg +" - currentKiosko: "+Kiosko.currentKiosko + " currentStatus: "+Status.currentStatus
            Tracker.status("support","query",msg)
           // ErrorMgr.guardar(TAG, "settings:",  msg)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "settings:  supportAction:", ex.message)
        }
        return true
    }
    fun showIcon(){
        try{
            Log.msg(TAG, "[getSetting][8] showIcon")
            Settings.setSetting(TipoBloqueo.hide_icon_dpc,false)
            Dialogs.showAppIcon(context,false)
            Tracker.status("support","showIcon","mostro el icon" )
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "showIcon", ex.message)
        }
    }
    private fun ResetClave(){
        try{
            Log.msg(TAG, "[getSetting][9] ResetClave")
           // restrictions.resetPassword(true)
/*            val resetPwd= ResetPwd()
            resetPwd.changePwd(context)*/
            val intentMain =  Intent(context, ResetCveActivity::class.java)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
       //     intentMain.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
      //      intentMain.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
      //      intentMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

            context.startActivity(intentMain)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "ResetClave", ex.message)
        }
    }

    fun isNumeric(toCheck: String): Boolean {
        val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
        return toCheck.matches(regex)
    }
    fun lastLocation() {
        Log.msg(TAG, "[lastLocation] Obtiene la posicion GPS - lastLocation")
        try {
            com.macropay.downloader.di.Inject.inject( context!!).getLocationDevice().currentPos
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "lastLocation", ex.message)
        }
    }


}