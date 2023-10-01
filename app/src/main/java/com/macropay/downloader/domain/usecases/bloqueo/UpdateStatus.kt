package com.macropay.downloader.domain.usecases.bloqueo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.repositories.TrxOffline
import com.macropay.data.usecases.SendUpdateStatus
import com.macropay.downloader.data.preferences.Status
import com.macropay.downloader.data.preferences.TipoBloqueo
import com.macropay.downloader.data.testers.ConfigInicial
import com.macropay.downloader.utils.Settings
import com.macropay.utils.network.Red
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


class UpdateStatus
@Inject constructor(
    @ApplicationContext val context: Context,
    val sendUpdateStatus:SendUpdateStatus){

    val TAG = "UpdateStatus"

    private var lockId = ""
    private var transId = ""
    private var userId = ""
    private var orden: Long =0L
    private var curEvent: EventMQTT? = null
    private var data: JsonObject? = null

    suspend fun sendStatus(curLockId: String): Unit = coroutineScope {

        leerSettings()


        //Avisa a Central.

        var ln = 1
        try {
            CoroutineScope(Dispatchers.IO).launch {
                ln = 2
                Log.msg(TAG, "[sendStatus] curEvent!!.message:  "+curEvent!!.message)
                val lockedActive = getLockedID()
                ln=3
                //Actualiza Status en Central
                if(Red.isOnline) {
                    sendUpdateStatus.send(userId, transId, curLockId, lockedActive, Kiosko.currentKiosko.name,curEvent!!.message)
                }else {
                    //Se genera la transaccion offline, para que se envie cuando regrese la conexion de red.
                    val body  = sendUpdateStatus.getBody(userId, transId, curLockId, lockedActive, Kiosko.currentKiosko.name, curEvent!!.message)
                    var dataBody = Gson().toJson(body)
                    TrxOffline.guardarTrx("STS", dataBody)
                }

                //Avisa que Termino de procesar
                Log.msg(TAG, "[sendStatus] ....Va revisar si ya terminaron todos los procesos del Enrolamiento.")
                //TODO: terminar()
            }

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "updateStatus " + ln, ex.message)
        }


    }

    fun leerSettings(){
        try{
            lockId   = Settings.getSetting(Cons.KEY_CURRENT_LOCK_ID, lockId)
            transId  = Settings.getSetting(Cons.KEY_CURRENT_TRANSAC, transId)
            userId   = Settings.getSetting(Cons.KEY_CURRENT_USER_ID, userId)
            orden   = Settings.getSetting(Cons.KEY_CURRENT_ORDEN, orden)
            curEvent = load(lockId,userId,transId)

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "leerSettings ", ex.message)
        }
    }

    fun getLockedID():String{
        /*Locked
        0 = Activo
        1 = Bloqueado
        2 = Enrolado
        3 = Configurado Activo
        4 = Configurado desbloqueado
        */

        var lockedActive = "0"
        try{
/*            when (currentProcess) {*/
                /*LockMgr.eProcess.unlock ->
                    lockedActive = "0"
                LockMgr.eProcess.lock -> {
                    var kioskStatus = Settings.getSetting(TipoBloqueo.show_kiosko,false)
                    if(!kioskStatus){
                        kioskStatus = Kiosko.currentKiosko == Kiosko.eTipo.PorNoConexion ||
                                Kiosko.currentKiosko == Kiosko.eTipo.PorCambioSIM
                    }
                    if(lockId.isEmpty()) lockId= "1"
                    Log.msg(TAG, "[getLockedID] kioskStatus: " +kioskStatus  +" lockId: "+lockId)
                    if( lockId.toInt()<1000)
                        lockedActive = if (kioskStatus) "1" else "0"
                    else
                        lockedActive = if (kioskStatus) "4" else "3"
                    Log.msg(TAG, "[getLockedID] ....-3- kioskStatus: $kioskStatus lockedActive: "+lockedActive)
                }
                LockMgr.eProcess.enroll ->*/
                    lockedActive = "2"
/*                else -> {}
            }*/
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"getLockedID",ex.message,false)
        }
        return lockedActive
    }

    //Usado por el unLock
    private fun load(lockId:String,userId: String,transId: String): EventMQTT? {
        var bloqueo = JSONObject()
/*        val lockFile = "lock"+lockId*/
        var eventMQTT : EventMQTT? = null
        var restricciones: JSONArray? = null

        try{
          /*  var body = FileMgr.loadFile(lockFile,context)
            Log.msg(TAG,"[load] body: "+body!!.length)
            Log.msg(TAG,"[load] body:\n"+body+"")*/

            restricciones = JSONArray()
/*            if (!body!!.isEmpty()){
                restricciones = JSONArray(body)
            }else {*/
             //   Log.msg(TAG,"[load] body = null")
               var body =Settings.getSetting(Cons.KEY_LOCK_RESTRICTIONS, "")
                if(!body.isEmpty()){
                    restricciones = JSONArray(body)
                }else
                    restricciones.put(ConfigInicial.getShowBienvenida(true))
           //}

            ///Agrega los parametros del Evento original.:
            bloqueo.put("trans_id",transId)
            bloqueo.put("user_id",userId)
            bloqueo.put("lock_id",lockId)
            bloqueo.put("orden",this.orden)
            bloqueo.put("restrictions", restricciones)
            //Genera el objeto Event con lo cargado del archivo...
            eventMQTT = EventMQTT("1002", bloqueo, false)
  /*          Log.msg(TAG,"[load] ================================")
            Log.msg(TAG,"[load] transId: "+transId)
            Log.msg(TAG,"[load] user_id: "+userId)
            Log.msg(TAG,"[load] orden: "+orden)
            Log.msg(TAG,"[load] eventMQTT.message"+ eventMQTT.toString())*/

        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "load", e.message)
        }
        return eventMQTT
    }
}