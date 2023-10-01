package com.macropay.data.repositories
import android.content.Context
import com.google.gson.Gson
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

import com.macropay.data.dto.request.UpdateLockStatusDto
import com.macropay.data.dto.response.MessageResponse
import com.macropay.data.preferences.Consts
import com.macropay.data.preferences.Defaults
import com.macropay.data.preferences.Values
import com.macropay.data.server.DeviceAPI
import com.macropay.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons

class UpdateLockStatusRepository
@Inject constructor(@ApplicationContext context: Context,
                    private val api: DeviceAPI
): DataResponse(){

    val TAG = "UpdateLockStatusRepository"

    //
    suspend fun execute(updateLockStatusDto: UpdateLockStatusDto,userSessionCredentials: UserSessionCredentials?): Response<MessageResponse> {
       // Log.msg(TAG,"[execute]  imei:"+ updateLockStatusDto.imei)
        val url = UrlServer.getHttp() + BuildConfig.ls11 // "/api/devices/update/LockStatus"
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE,Defaults.API_KEY)

        // Log.msg(TAG,"[execute] url: "+url)
        //  Log.msg(TAG,"apiKeyMobile: "+apiKeyMobile)
        var response : Response<MessageResponse>? = null
        try {

              response = api.updateLockStatus(url, updateLockStatusDto, userSessionCredentials!!.getHeadersWithoutCognito())
           // Log.msg(TAG, "[execute]  --------------------------------------------------")
            val gson = Gson()
            val json = gson.toJson(updateLockStatusDto)
            Log.msg(TAG,"[execute] updateLockStatusDto: "+json)

            if (response.isSuccessful) {
                // if(response.code() == 200)
                Log.msg(TAG, "[execute] isSuccessful: " + response.code())
                Log.msg(TAG, "[execute] response: \n" + response.body()!!)
                onSuccess(response.code(), response.message())
            } else {
                Log.msg(TAG, "[execute] isFailure: " + response.code())
                onError(response.code(), response.errorBody()!!.string(), "STS", updateLockStatusDto)
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"execute",ex.message)
            onError(400, ex.message!!, "STS", updateLockStatusDto)
        }
        Log.msg(TAG,"[execute] -termino-")
        return response!!
    }
}

