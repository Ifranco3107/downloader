package com.macropay.data.repositories

import com.google.gson.Gson
import com.macropay.data.preferences.Values
import com.macropay.utils.Settings
import com.macropay.utils.broadcast.Sender
import com.macropay.utils.broadcast.Sender.sendStatus
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.network.Red
import com.macropay.utils.preferences.Kiosko
import java.time.LocalDateTime

open class DataResponse {
private val TAG = "DataResponse"
     val TEXT_HTTP_ERROR = "HTTP ERROR"

    fun onSuccess(code:Int,result:Any){
        try{
            Log.msg(TAG,"[onSuccess]: $code ")
            Log.msg(TAG,"[onSuccess]: ${result.toString()} ")

            //Desbloquea por falta de conexion...
            desbloquear()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onSuccess",ex.message)
        }
    }

    fun onError(error:Int,msg:String,transId:String,body :Any ){
        val gson = Gson()
        var dataBody = gson.toJson(body)
        Log.msg(TAG,"Error: $error trxID:["+transId +
                        "\nMsg: "+msg +
                        "\n"+dataBody)

        //Tempralmente para evitar que mande el mensaje. esta en revision el problema en el EndPonit.
       // if(!transId.contains("STS"))
       //     sendStatus(TEXT_HTTP_ERROR + ": " + transId +"-"+ error + "\n" + msg)

        //Guarda la transaccion, para intentar enviarla posteriormente, reenviarla.
      //  if(transId.equals("GPS"))


        //Envia error al Central...
        //Si el error no es el envio del error, para que no se cicle.
        if(!transId.equals("ERR")){
            TrxOffline.guardarTrx(transId, dataBody)

            //Solo reporta, si el error no es por conexion.
            if(Red.isOnline && error != 990)
                ErrorMgr.guardar(TAG, transId,"["+error +"] " +msg,dataBody)
        }

    }

    private  fun desbloquear() {

        val KEY_CURRENT_KIOSKO = "kiosko_actual"
        try {
            val KEY_STATUS = "statusEnrolamiento"
            val statusName = Settings.getSetting(KEY_STATUS,"")
            Log.msg(TAG,"statusName: [$statusName]")
            if(statusName.equals("TerminoEnrolamiento")){
                //Actualiza de ultimo conexion a central
                Log.msg(TAG,"GUARDO LA FECHA DE ULTIMO CONEXION...")
                Settings.setSetting("ultimaNotificacion",LocalDateTime.now())
                Log.msg(TAG,"currentKiosko: ["+Kiosko.currentKiosko+"]")
                if(Kiosko.currentKiosko ==  Kiosko.eTipo.PorNoConexion) {
                    Log.msg(TAG, "ENTRA EN DESBLOQUEO AUTOMATICO")
                    Sender.sendBloqueo(false, Values.context!!, Kiosko.eTipo.PorNoConexion)
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "desbloquear", ex.message)
        }
    }
}