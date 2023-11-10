package com.macropay.data.usecases

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.response.EnrollInfoResponse
import com.macropay.data.dto.response.enroll.EnrollResponse
import com.macropay.data.logs.ErrorMgr

import com.macropay.data.logs.Log

import com.macropay.data.repositories.GetEnrollInfoRepository
import com.macropay.utils.Settings
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.lifecycle.HiltViewModel

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class GetEnrollInfo
@Inject constructor(

    @ApplicationContext val context: Context) : ViewModel() {
    private val TAG = "GetEnrollInfo"

    @Inject
    lateinit var cabeceras: UserSessionCredentials

    @Inject
    lateinit var getEnrollInfoRepository: GetEnrollInfoRepository

    @Inject
    lateinit var enrollFailed: EnrollFailed
    val enrollSuccessModel = MutableLiveData<StatusRestrictions>()
    val enrollErrorModel = MutableLiveData<JSONObject>()
     fun get() {

        Log.msg(TAG, "[get] -inicio-")
        viewModelScope.launch {
            try {
                val result: Response<EnrollInfoResponse> = withContext(Dispatchers.Main) {
                    getEnrollInfoRepository.executeVM(cabeceras)
                }
                if (result.isSuccessful) {
                    val responseBody: String = result.body().toString()
                    Log.msg(TAG, "[get] isSuccessful  ------------------------------")
                    Log.msg(TAG, "[get] isSuccessful  responseBody: " + responseBody)
                    Log.msg(TAG, "[get] isSuccessful  ------------------------------")
                    var body = result.body()!!.data
                    Log.msg(TAG, "[get] isSuccessful  body: " + body)
                    val gson = Gson()
                    val dataEnroll = gson.toJson(body)
                    val statusRestrictions = StatusRestrictions(true, result.code(), dataEnroll)
                    enrollSuccessModel.value = (statusRestrictions)
                } else {
                    Log.msg(TAG, "Error:" + result.code() + " - " + result.errorBody().toString())
                    sendError(result.code(), result.message())
                    ErrorMgr.guardar(TAG, "send", "[" + result.code() + "]" + result.message())
                    enrollFailed.send("")
                }
            } catch (ex: Exception) {
                sendError(902, ex.message!!)
                enrollFailed.send("enrolldto.imei")
                ErrorMgr.guardar(TAG, "send", ex.message)
            }

        }


    }

    fun sendError(code: Int, message: String) {
        val jsonError = JSONObject()
        jsonError.put("code", code)
        jsonError.put("data", message)
        enrollErrorModel.value = (jsonError)
    }
}
//class DeviceInformation(val isSucces: Boolean,val code:Int ,val body:String)