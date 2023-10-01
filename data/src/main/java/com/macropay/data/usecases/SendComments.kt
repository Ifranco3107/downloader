package com.macropay.data.usecases
import android.content.Context

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject

import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.repositories.CommentsRepository

import com.macropay.utils.phone.DeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class SendComments
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val TAG = "SendComments"
    @Inject
    lateinit var commentsRepository: CommentsRepository
    @Inject
    lateinit var cabeceras:UserSessionCredentials

    val phoneModel = MutableLiveData<JSONObject>()
    fun send( tipo_estatus:String,aplicacion:String,comentarios:String )  {

        val commentsDto = JsonObject()
        commentsDto.addProperty("imei", DeviceInfo.getDeviceID())
        commentsDto.addProperty("tipo_estatus",tipo_estatus)
        commentsDto.addProperty("aplicacion",aplicacion)
        commentsDto.addProperty("comentarios",comentarios)

        val gson = Gson()
        val json = gson.toJson(commentsDto)
        Log.msg(TAG,"phoneNumberDto: \n"+json)
        CoroutineScope(Dispatchers.Main).
            launch {
            try{
                val result : Response<ResponseBody> = withContext(Dispatchers.Main) {
                                                        commentsRepository.execute(commentsDto,cabeceras) }
                if (result.isSuccessful) {
                    val responseBody: String = result.body()!!.string()
                    Log.msg(TAG,"[2] isSuccessful  responseBody: "+responseBody)
                    val json = JSONObject(responseBody)
                }else
                    Log.msg(TAG,"Error: "+ result.errorBody().toString())
            }catch (ex:Exception){
                ErrorMgr.guardar(TAG, "send", ex.message,json)
            }
        }
    }
}