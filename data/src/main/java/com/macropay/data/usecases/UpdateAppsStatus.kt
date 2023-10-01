package com.macropay.data.usecases

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.request.UpdateLockStatusDto
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log

import com.macropay.data.repositories.UpdateAppStatusRepository
import com.macropay.utils.FileMgr
import com.macropay.utils.phone.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class UpdateAppsStatus
@Inject constructor(
    @ApplicationContext val context: Context
): ViewModel() {
    private val TAG = "UpdateAppsStatus"
    @Inject
    lateinit var updateAppStatusRepository: UpdateAppStatusRepository
    @Inject
    lateinit var cabeceras: UserSessionCredentials

    val phoneModel = MutableLiveData<JSONObject>()
    fun send( packageName: String,statusKey:String)  {
        val packageInfo = JsonObject()
        packageInfo.addProperty("imei", DeviceInfo.getDeviceID())
        packageInfo.addProperty("package",packageName)
        packageInfo.addProperty("key",statusKey)


      //  val gson = Gson()
       // val json = gson.toJson(packageInfo)
        Log.msg(TAG,"packageInfo: \n"+packageInfo)
        viewModelScope.launch {
            try{
                val result : Response<ResponseBody> =  withContext(Dispatchers.Main) {
                    updateAppStatusRepository.execute(packageInfo,cabeceras)
                }
                if (result.isSuccessful) {
                    val responseBody: String = result.body()!!.string()
                    Log.msg(TAG,"[2] isSuccessful  responseBody: "+responseBody)
                    val json = JSONObject(responseBody)
                    phoneModel.value = (json)
                    Log.msg(TAG,"[4] isSuccessful ->send --->>>>> json: "+packageInfo.toString())
                }else {
                    val jsonError = JSONObject()
                    jsonError.put("code",500)
                    jsonError.put("data", result.message())
                    phoneModel.value = (jsonError)
                    ErrorMgr.guardar(TAG, "send","["+result.code() +"]" +result.message(),packageInfo.toString())
                }
            }catch (ex:Exception){
                ErrorMgr.guardar(TAG, "send", ex.message,packageInfo.toString())
            }

        }
    }

    fun sendObject(updateLockStatusDto  : JsonObject, file: File) {
        Log.msg(TAG,"[sendObject]")

        GlobalScope.launch {
            withContext(Dispatchers.IO){
                try{
                    val response = updateAppStatusRepository.execute(updateLockStatusDto,cabeceras)
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