package com.macropay.data.usecases

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.macropay.data.BuildConfig
import com.macropay.data.di.Session
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.request.EnrollDto
import com.macropay.data.dto.response.enroll.EnrollResponse
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.preferences.Values
import com.macropay.data.repositories.EnrollRepository
import com.macropay.utils.Fechas
import com.macropay.utils.Settings.getSetting
import com.macropay.data.logs.Log
import com.macropay.data.repositories.EnrollFailedRepository
import com.macropay.utils.Settings
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
@HiltViewModel
class EnrollDeviceVM
@Inject constructor(
    @ApplicationContext val context: Context) : ViewModel(){
    private val TAG = "EnrollDeviceVM"

    @Inject
    lateinit var cabeceras: UserSessionCredentials
    @Inject
    lateinit var enrollRepository: EnrollRepository
    @Inject
    lateinit var enrollFailed: EnrollFailed
    val enrollSuccessModel = MutableLiveData<StatusRestrictions>()
    val enrollErrorModel = MutableLiveData<JSONObject>()



    fun send(iuVesion:String )  {
        Log.msg(TAG, "[send] -inicio-")
        var applicative =   getSetting(Cons.KEY_APPLICATIVE,"")
        var subsidiary =   getSetting(Cons.KEY_SUBSIDIARY,"")
        var employee =   getSetting(Cons.KEY_EMPLOYEE,"")
        var enrollId = DeviceInfo.getDeviceID()
        Log.msg(TAG,"[send] enrollId: "+ enrollId)
        Log.msg(TAG,"[send] SIMs:"+DeviceCfg.getCountSIM(context))
        if(DeviceCfg.getCountSIM(context) >1){
            Log.msg(TAG,"[send] SIM 1:"+DeviceCfg.getImeiBySlot(context,0))
            Log.msg(TAG,"[send] SIM 2"+DeviceCfg.getImeiBySlot(context,1))
        }

        val enrolldto = EnrollDto(
          //  imei = DeviceCfg.getImei(context),
            imei = DeviceInfo.getDeviceID(),
            hasImei = DeviceCfg.hasIMEI(context).toString(),
            id_usuario = "1234", //TODO - no se usa
            username = "Dispositivo",
            id_bloqueo = "1",//TODO - no se usa
            id_enrolado = "1",//TODO - no se usa
            serie = Build.getSerial(),
            no_telefono = "",
            marca = Build.MANUFACTURER,
            modelo = Build.MODEL,
            sistema_operativo = "android",
            os_version = Build.VERSION.RELEASE,
            ui_version =iuVesion,
            dcp_version = getVersion(Values.context!!).toString(),
            dcp_version_name = getVersionName(Values.context!!).toString(),
            ult_fec_act = Fechas.getToday(),
            ult_fec_syncmovil = Fechas.getToday(),
            fec_enroll = Fechas.getToday(),
            applicative= applicative,
            subsidiary=subsidiary,
            employee=employee
        )

        val gson = Gson()
        val json = gson.toJson(enrolldto)
        Log.msg(TAG,"enrolldto: \n"+json)

        viewModelScope.launch {
            try{
                val result : Response<EnrollResponse> =  withContext(Dispatchers.Main) {
                    enrollRepository.executeVM(enrolldto,cabeceras)
                }
                if (result.isSuccessful) {
                    val responseBody: String = result.body().toString()
                    Log.msg(TAG,"[2] isSuccessful  responseBody: "+responseBody)

                    var body = result.body()!!.data
                    val gson = Gson()
                    val dataEnroll = gson.toJson(body)
                    val statusRestrictions = StatusRestrictions(true, result.code(), dataEnroll)
                    enrollSuccessModel.value = (statusRestrictions)
                    Log.msg(TAG,"[4] isSuccessful ->send --->>>>>")
                    if(BuildConfig.isFase7 =="true"){
                        var idEnroll = result.body()!!.id_registro
                        Settings.setSetting(Cons.KEY_ID_ENROLLMENT,idEnroll)
                        Log.msg(TAG,"[5] ->idEnroll: $idEnroll")
                    }
                }else {
                    Log.msg(TAG,"Error:"+result.code()+ " - "+ result.errorBody().toString())
                    sendError(result.code(),result.message())
                    ErrorMgr.guardar(TAG, "send","["+result.code() +"]" +result.message(),json)
                    enrollFailed.send(enrolldto.imei)
                }
            }catch (ex:Exception){
                sendError(902 ,ex.message!!)
                enrollFailed.send(enrolldto.imei)
                ErrorMgr.guardar(TAG, "send", ex.message,json)
            }

        }
    }

    fun sendError(code:Int,message:String){
        val jsonError = JSONObject()
        jsonError.put("code",code)
        jsonError.put("data", message)
        enrollErrorModel.value = (jsonError)
    }
    fun getVersion(context: Context): Long? {
        var version = 0L
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = packageInfo.longVersionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return version
    }

    fun getVersionName(context: Context): String? {
        var versionName = ""
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionName
    }


}

//class StatusRestrictions(val isSucces: Boolean,val code:Int ,val body:String)