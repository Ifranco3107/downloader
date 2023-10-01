package com.macropay.data.repositories
import android.content.Context
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

import com.macropay.data.dto.request.SIMDto
import com.macropay.data.dto.response.SIMResponse
import com.macropay.data.preferences.Defaults
import com.macropay.data.server.DeviceAPI
import com.macropay.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons

class SimRepository
@Inject constructor(@ApplicationContext context: Context,
                    private val api: DeviceAPI
): DataResponse(){

    val TAG = "SimRepository"

    //
    suspend fun execute(SimDto: SIMDto,userSessionCredentials: UserSessionCredentials): Response<SIMResponse> {
        var endPoint = BuildConfig.cc09
        if(BuildConfig.isFase7 =="true")
            endPoint = BuildConfig.cc09v3
        Log.msg(TAG,"[execute]  imei:"+ SimDto.imei)
        val url = UrlServer.getHttp() + endPoint // "/api/locks/mobile/confirmarcodigo"
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
       Log.msg(TAG,"url: "+url)
        // Log.msg(TAG,"apiKeyMobile: "+apiKeyMobile)

        val response = api.sendSIM(url,SimDto, userSessionCredentials.getHeadersWithoutCognito())


        Log.msg(TAG,"[execute]  --------------------------------------------------")

        if(response.isSuccessful){
            // if(response.code() == 200)
            // Log.msg(TAG,"[execute] isSuccessful: "+ response.code())
            // Log.msg(TAG,"[execute] response: \n"+response.body())
            onSuccess(response.code(),response.message())
        }else{
            Log.msg(TAG,"[execute] isFailure: "+ response.code())
            onError(response.code(),response.errorBody()!!.string(),"SIM",SimDto)
        }
        Log.msg(TAG,"[execute] -termino-")
        return response
    }
}

