package com.macropay.data.repositories
import android.content.Context
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

import com.macropay.data.dto.request.UnrollDto
import com.macropay.data.dto.response.UnrollResponse
import com.macropay.data.preferences.Consts
import com.macropay.data.preferences.Defaults
import com.macropay.data.preferences.Values
import com.macropay.data.server.DeviceAPI
import com.macropay.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons

class UnrollRepository
@Inject constructor(@ApplicationContext context: Context,
                    private val api: DeviceAPI
): DataResponse(){

    val TAG = "UnrollRepository"

    //
    suspend fun execute(unrollDto: UnrollDto,userSessionCredentials: UserSessionCredentials): Response<UnrollResponse> {
        Log.msg(TAG,"[execute]  imei:"+ unrollDto.imei)
        val url = UrlServer.getHttp() + BuildConfig.ud10 //"/api/device/mobile/unrollDevice"
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)

        // Log.msg(TAG,"[execute] url: "+url)
        // Log.msg(TAG,"[execute] apiKeyMobile: "+apiKeyMobile)
        //val response = api.unroll(url,unrollDto, apiKeyMobile)

        //Log.msg(TAG,"url: "+url)
        //Log.msg(TAG,"apiKeyMobile: "+apiKeyMobile)

        val response = api.unroll(url,unrollDto, userSessionCredentials.getHeadersWithoutCognito())

        Log.msg(TAG,"[execute]  --------------------------------------------------")

        if(response.isSuccessful){
           // if(response.code() == 200)
            Log.msg(TAG,"[execute] isSuccessful: "+ response.code())
            Log.msg(TAG,"[execute] response: \n"+response.body())
            onSuccess(response.code(),response.message())
        }else{
            Log.msg(TAG,"[execute] isFailure: "+ response.code())
            onError(response.code(),response.errorBody()!!.string(),"UNR",unrollDto)
        }
        Log.msg(TAG,"[execute] -termino-")
        return response
    }
}

