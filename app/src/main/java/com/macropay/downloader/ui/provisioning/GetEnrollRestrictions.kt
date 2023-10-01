package com.macropay.downloader.ui.provisioning

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.preferences.Defaults
import com.macropay.data.usecases.EnrollDeviceVM
import com.macropay.data.usecases.EnrollFailed
import com.macropay.data.usecases.StatusRestrictions
import com.macropay.downloader.data.preferences.Status
import com.macropay.downloader.domain.usecases.provisioning.Provisioning
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.policies.KioskScreen
import com.macropay.downloader.utils.samsung.KnoxConfig
import com.macropay.utils.broadcast.Sender
import com.macropay.utils.network.Red
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
//Es usada en FinalizeActiviy y en EnrollActivity
open class GetEnrollRestrictions :  KioskScreen() {
    private val TAG = "GetEnrollRestrictions"
    private lateinit var enrollDevice: EnrollDeviceVM
    private  var listener: QueryEnrollStatus? = null
    @Inject
    lateinit var provisioning : Provisioning

    @Inject
    lateinit var enrollFailed: EnrollFailed


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.msg(TAG,"[onCreate]")
        initViewModel(this)
    }

    fun queryRestrictions(source:String){
        Log.msg(TAG,"[queryRestrictions] source: $source")
        if(!Red.isOnline){
            Log.msg(TAG,"[queryRestrictions] no hay red...")
            Sender.sendHttpError(500,"No hay conexion de red","",0,this )
            return
        }

        try{
            Log.msg(TAG,"[queryRestrictions] 1.- Requiere permisos")
            provisioning.appyPermissions.autoGrant(this)
           // Settings.setSetting(Cons.KEY_ENROLL_SOURCE,"manual")
            DeviceInfo.setDeviceID(this)
            Log.msg(TAG,"[queryRestrictions] 2.- Hace la consulta a central...")
            //Valida que sea un dispositivo registrado...
            var result = enrollDevice.send(getUIVersion())

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"queryRestrictions",ex.message)
        }

    }
    private fun getUIVersion():String {
        var uiVersion = "0"
        try{
            if (Build.MANUFACTURER.uppercase(Locale.getDefault()).contains("SAMSUNG")) {
                uiVersion = KnoxConfig.knoxVersion
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"getUIVersion",ex.message)
        }
        return uiVersion
    }

    fun setOnDownloadStatus(listener: QueryEnrollStatus) {
        Log.msg(TAG,"[setOnDownloadStatus]")
        try{
            this.listener = listener
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"setOnDownloadStatus",ex.message)
        }
    }
   private fun initViewModel(context: Context) {
        Log.msg(TAG,"initViewModel")
        try{
            enrollDevice = ViewModelProvider(this)[EnrollDeviceVM::class.java]
            enrollDevice.enrollSuccessModel.observe(this, ::onEnrollSuccess)
            enrollDevice.enrollErrorModel.observe(this, ::onEnrollFail)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"initViewModel",ex.message)
        }
    }


    private fun onEnrollSuccess(enrollRestrictions: StatusRestrictions) {
        var ln = 0
        try{
            //Registra status...
            Status.currentStatus = Status.eStatus.RegistroEnServer
            Settings.setSetting(Cons.KEY_ENROLL_STARTED,false)
ln = 1
            Log.msg(TAG, "[onEnrollSuccess] ")
            var jsonObject  =  JSONObject(enrollRestrictions.body)
            ln = 2

            val eventMQTT = EventMQTT("bloqueo", jsonObject, false)
            ln = 3

            this.listener!!.onSuccess(eventMQTT)
            Log.msg(TAG, "[onEnrollSuccess] va iniciar a aplicar las restricciones...")

            Log.msg(TAG,"[onEnrollSuccess]_________________[ inicia descarga de Macrolock ]____________________________________")
           Settings.setSetting(Cons.KEY_PACKAGENAME_DPC, Defaults.DPC_PACKAGENAME )
           Settings.setSetting(Cons.KEY_LOCATION_DPC, Defaults.DPC_LOCATION)


            /*
                    Settings.setSetting(Cons.KEY_APPLICATIVE,"downloader")
        Settings.setSetting(Cons.KEY_SUBSIDIARY,"macropay")
        Settings.setSetting(Cons.KEY_EMPLOYEE,"134567")
        Settings.setSetting(Cons.KEY_ENROLL_SOURCE,"manual")
        Settings.setSetting(Cons.KEY_HTTP_SERVER, Defaults.SERVIDOR_HTTP2_DEV)
             */
            //provisioning.leeQRSettings(this,intent)
            provisioning.downloadDPC(eventMQTT,TAG+".onEnrollSuccess")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onEnrollSuccess($ln)",ex.message,enrollRestrictions.body)
            this.listener!!.onError(999,"error en onEnrollSuccess")
        }
    }

    private fun onEnrollFail(errorResponse: JSONObject) {
        try{
            Log.msg(TAG, "[onEnrollFail] json: " + errorResponse.toString())
            var code = if (errorResponse.has("code")) errorResponse.getInt("code") else 0
            var data = if (errorResponse.has("data")) errorResponse.getString("data") else "nada"
            Log.msg(TAG, "[onEnrollFail] code:  " + code)
            this.listener!!.onError(code,data)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onEnrollFail",ex.message)
        }
    }

}
interface QueryEnrollStatus {
    fun onSuccess(body:EventMQTT)
    fun onError(code: Int, error: String?)
}