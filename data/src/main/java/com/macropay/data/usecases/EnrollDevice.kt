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
//@HiltViewModel
class EnrollDevice
@Inject constructor(
    @ApplicationContext val context: Context) : ViewModel(){
    private val TAG = "EnrollDevice"

    @Inject
    lateinit var cabeceras: UserSessionCredentials
    @Inject
    lateinit var enrollRepository: EnrollRepository
    val enrollSuccessModel = MutableLiveData<StatusRestrictions>()
    val enrollErrorModel = MutableLiveData<JSONObject>()

/*
    suspend fun sendTest(): Deferred<Int> = coroutineScope {
       // GlobalScope.launch {
            Log.msg(TAG,"[sendTest] -1-")
            var result = async{suma(10,4)}
            result.await()
            Log.msg(TAG,"[sendTest] -3-"+result)
            result
       // }
    }

    suspend fun suma(num1:Int,num2:Int):Int{
        delay(3000)
        return num1+num2
    }*/

    suspend fun send(): Deferred<StatusRestrictions> = coroutineScope {

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
            imei = DeviceCfg.getImei(context),
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
            ui_version = "0",
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

        //val credenciales = session.getCredentials()
        var result = async{enrollRepository.execute(enrolldto,cabeceras)}

        result.await()

        result
    }


    fun sendVM( )  {
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
            imei = DeviceCfg.getImei(context),
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
            ui_version = "0",
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
                }else {
                    Log.msg(TAG,"Error:"+result.code()+ " - "+ result.errorBody().toString())
                    val jsonError = JSONObject()
                    jsonError.put("code",result.code())
                    jsonError.put("data", result.message())
                    enrollErrorModel.value = (jsonError)
                    ErrorMgr.guardar(TAG, "send","["+result.code() +"]" +result.message(),json)
                    Sender.sendEnrollResult(false,result.code(),"error")
                }
            }catch (ex:Exception){
                ErrorMgr.guardar(TAG, "send", ex.message,json)
                Sender.sendEnrollResult(false,902,"error")
            }

        }
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

class StatusRestrictions(val isSucces: Boolean,val code:Int ,val body:String)