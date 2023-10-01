package com.macropay.data.usecases
import com.macropay.data.di.UserSessionCredentials
import android.content.Context
import androidx.core.text.isDigitsOnly
import com.google.gson.Gson
import com.macropay.data.BuildConfig
import com.macropay.data.dto.request.MsgMQTT
import com.macropay.data.dto.request.UpdateLockStatusDto
import com.macropay.data.repositories.UpdateLockStatusRepository
import com.macropay.utils.Fechas
import com.macropay.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.repositories.TrxOffline
import com.macropay.utils.FileMgr
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
class SendUpdateStatus
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val TAG = "SendUpdateStatus"
    @Inject
    lateinit var updateLockStatusRepository: UpdateLockStatusRepository

    @Inject
    lateinit var cabeceras:UserSessionCredentials

    fun test(txt:String){
        Log.msg(TAG,"........")
        Log.msg(TAG,"test...."+txt)
        Log.msg(TAG,"........")
    }

    fun send(userId:String, transID:String, lockId:String, lockActived:String, kioskActived:String, data: JSONObject) = runBlocking() {
        //Verifica si hay status pendientes por enviar...
        TrxOffline.enviaStatusPendientes()
        delay(2_000)
        //
        val bDpc_updated = Settings.getSetting(Cons.KEY_DPC_UPDATED, false)
        val  updateLockStatusDto = getBody(userId, transID, lockId,lockActived,kioskActived,data)
        val json = Gson().toJson(updateLockStatusDto)
        Log.msg(TAG,"[send] json: "+json )
        launch {
            withContext(Dispatchers.IO){
                try{
                    val response = updateLockStatusRepository.execute(updateLockStatusDto,cabeceras)
                }catch (ex:Exception){
                    val json = Gson().toJson(updateLockStatusDto)
                    ErrorMgr.guardar(TAG, "send", ex.message,json)
                }
            }
        }
    }
    /*   fun send(userId:String, transID:String, lockId:String, lockActived:String, kioskActived:String, data: JSONObject) = runBlocking() {
           val bDpc_updated = Settings.getSetting(Cons.KEY_DPC_UPDATED, false)
           Log.msg(TAG,"[send] -inicio- " +userId + " bDpc_updated: "+bDpc_updated)
           val  updateLockStatusDto = getBody(userId, transID, lockId,lockActived,kioskActived,data)
           launch {
                withContext(Dispatchers.IO){
                    try{
                        val response = updateLockStatusRepository.execute(updateLockStatusDto)
                        Log.msg(TAG,"regreso del updateLockStatusRepository ")
                        if(response.isSuccessful)
                            Log.msg(TAG,"isSuccessful")
                        else
                            Log.msg(TAG,"isFAILURE: ${response.code()} - "+response.message())
                    }catch (ex:Exception){
                        ErrorMgr.guardar(TAG,"send",ex.message)
                        val gson = Gson()
                        val json = gson.toJson(updateLockStatusDto)
                        Log.msg(TAG,"updateLockStatusDto:"+json)
                    }
                }
            } //GlobalScope
        }*/
    fun getBody(userId:String, transID:String, lockId:String, lockActived:String, kioskActived:String, data: JSONObject) :UpdateLockStatusDto {
        var updateLockStatusDto : UpdateLockStatusDto? = null
        var curLockId = lockId
        var curLockActived = lockActived
        try {
            Log.msg(TAG,"[getBody] data: "+data.toString())
            Log.msg(TAG,"[getBody] kioskActived: $kioskActived")
            Log.msg(TAG,"[getBody] lockId: $lockId")
            var msgMqtt: MsgMQTT?  = null

            if (data.has("lock_id")) {
                Log.msg(TAG, "[getBody] si trae Lock_id")
                msgMqtt = Gson().fromJson(data.toString(), MsgMQTT::class.java)
            } else {
                Log.msg(TAG, "[getBody] NO TRAE lock_id")
            }

            if( (   kioskActived.equals(Kiosko.eTipo.PorCambioSIM.name) || kioskActived.equals(Kiosko.eTipo.PorNoConexion.name))
                    && lockId == "2" ){
                Log.msg(TAG,"[getBody] Este bloqueo no debe enviar nada en la propidadad DATA.")
                curLockId = "2"
               curLockActived = "1"
                if(BuildConfig.isFase7 =="false"){
                    if (!data.has("orden")) {
                        data.put("orden",0)
                        msgMqtt = Gson().fromJson(data.toString(), MsgMQTT::class.java)
                    }
                }else
                    msgMqtt  = null
            }
            Log.msg(TAG,"[getBody] msgMqtt: " +msgMqtt.toString())
            updateLockStatusDto = UpdateLockStatusDto(
                DeviceInfo.getDeviceID(),
                curLockId,
                curLockActived,
                kioskActived,
                transID,
                Fechas.getFormatDateTimeUTC(),
                Fechas.getFormatDateTimeUTC(),
                userId
                ,msgMqtt
            )
            val gson = Gson()
            val json = gson.toJson(updateLockStatusDto)
            Log.msg(TAG, "[getBody] updateLockStatusDto:\n" + json)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"getBody",ex.message)
        }
        return updateLockStatusDto!!
    }
    fun sendObject( updateLockStatusDto  : UpdateLockStatusDto,file: File) {
        Log.msg(TAG,"[sendObject]")

        GlobalScope.launch {
            withContext(Dispatchers.IO){
                try{
                    val response = updateLockStatusRepository.execute(updateLockStatusDto,cabeceras)
                    if(response.isSuccessful){
                        FileMgr.eliminarArchivo(file)
                    }
                }catch (ex:Exception){
                    ErrorMgr.guardar(TAG,"sendObject",ex.message)
                }
            }
        }
    }
}