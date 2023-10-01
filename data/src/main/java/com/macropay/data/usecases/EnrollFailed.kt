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
import com.macropay.data.dto.request.EnrollFailedDto
import com.macropay.data.dto.request.SIMDto
import com.macropay.data.dto.response.enroll.EnrollResponse
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.preferences.Values
import com.macropay.data.repositories.EnrollRepository
import com.macropay.utils.Fechas
import com.macropay.utils.Settings.getSetting
import com.macropay.data.logs.Log
import com.macropay.data.repositories.EnrollFailedRepository
import com.macropay.data.repositories.SimRepository
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
class EnrollFailed
@Inject constructor(
    @ApplicationContext val context: Context) : ViewModel(){
    private val TAG = "EnrollFailed"

    @Inject
    lateinit var enrollFailedRepository: EnrollFailedRepository



    @Inject
    lateinit var cabeceras: UserSessionCredentials


    fun send(
             imei:String

    ) {
        Log.msg(TAG,"[send] imei: "+imei)
        val enrollFailedDto=EnrollFailedDto(
            imei= imei,
            fecha = Fechas.getFormatDateTimeUTC()
        )

        val gson = Gson()
        val json = gson.toJson(enrollFailedDto)
        Log.msg(TAG,"enrollFailedDto: "+json)
        GlobalScope.launch {
            withContext(Dispatchers.IO){
                try{
                    val response = enrollFailedRepository.execute(enrollFailedDto,cabeceras)
                    if(response.isSuccessful){
                        Log.msg(TAG, "[send]" +response.code() + " "+response.message())
                    }else
                    {

                        ErrorMgr.guardar(TAG, "send", response.message().toString(),json)
                    }
                }catch (ex:Exception){

                    ErrorMgr.guardar(TAG, "send", ex.message,json)
                }
            }

        }
    }
}