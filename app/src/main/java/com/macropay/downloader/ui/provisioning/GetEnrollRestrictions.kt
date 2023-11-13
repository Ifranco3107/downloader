package com.macropay.downloader.ui.provisioning

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.macropay.data.BuildConfig
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.preferences.Defaults
import com.macropay.data.usecases.EnrollDeviceVM
import com.macropay.data.usecases.EnrollFailed
import com.macropay.data.usecases.GetEnrollInfo
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
    private lateinit var getEnrollInfo: GetEnrollInfo
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
            var result = getEnrollInfo.get( )

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
            getEnrollInfo = ViewModelProvider(this)[GetEnrollInfo::class.java]
            getEnrollInfo.enrollSuccessModel.observe(this, ::onEnrollSuccess)
            getEnrollInfo.enrollErrorModel.observe(this, ::onEnrollFail)
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
            saveSettings(jsonObject)

           //IFA-10Nov23  this.listener!!.onSuccess(eventMQTT)
            if(!BuildConfig.isTestTCL.equals("true")){
                Log.msg(TAG,"[onEnrollSuccess]_________________[ inicia descarga de Macrolock ]____________________________________")
                provisioning.downloadDPC(TAG+".onEnrollSuccess")
            }

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onEnrollSuccess($ln)",ex.message,enrollRestrictions.body)
            this.listener!!.onError(999,"error en onEnrollSuccess")
        }
    }
    private fun saveSettings(json:JSONObject){


        var httpServer =  json.getString("server")
        var pkgeServer =  json.getString("server_pkg")
        var reptServer =  json.getString("server_rpt")
        var mqttServer =  json.getString("mqtt")
        var locationDPC = json.getString("location_dpc")
        var appkeymobile = json.getString("app_key_mobile")
        if(locationDPC.isNullOrEmpty() ){
            locationDPC = Defaults.DPC_LOCATION

        }
        if(appkeymobile.isNullOrEmpty() ){
            appkeymobile = Defaults.API_KEY
        }

        Log.msg(TAG,"[saveSettings] --- va guardar --")
        Log.msg(TAG,"[saveSettings] httpServer:  " + httpServer)
        Log.msg(TAG,"[saveSettings] pkgeServer: "+pkgeServer)
        Log.msg(TAG,"[saveSettings] reptServer:  "+reptServer)
        Log.msg(TAG,"[saveSettings] mqttServer:  "+mqttServer)
        Log.msg(TAG,"[saveSettings] locationDPC:  "+locationDPC)
        Log.msg(TAG,"[saveSettings] appkeymobile:  "+appkeymobile)
        //Guarda los parametros de QR
        //   Settings.init(context)
        Settings.setSetting(Cons.KEY_HTTP_SERVER,httpServer)
        Settings.setSetting(Cons.KEY_HTTP_SERVER_PKG,pkgeServer)
        Settings.setSetting(Cons.KEY_HTTP_SERVER_RPT,reptServer)
        Settings.setSetting(Cons.KEY_MQTT_SERVER,mqttServer)
        Settings.setSetting(Cons.KEY_APIKEYMOBILE,appkeymobile)

        Settings.setSetting(Cons.KEY_LOCATION_DPC,locationDPC)
        Settings.setSetting(Cons.KEY_PACKAGENAME_DPC, Defaults.DPC_PACKAGENAME )
        /*
            Settings.setSetting(Cons.KEY_APPLICATIVE,applicative)
            Settings.setSetting(Cons.KEY_SUBSIDIARY,subsidiary)
            Settings.setSetting(Cons.KEY_EMPLOYEE,employee)
       */
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