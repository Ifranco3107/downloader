package com.macropay.data.repositories
import android.content.Context
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.response.CertsEncResponse
import com.macropay.data.logs.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

import com.macropay.data.preferences.Defaults
import com.macropay.data.server.DeviceAPI
import com.macropay.utils.Settings

import com.macropay.utils.preferences.Cons

class CertsRepository
@Inject constructor(@ApplicationContext context: Context,
                    private val api: DeviceAPI
): DataResponse(){

    val TAG = "CertsRepository"
    val ctx :Context
    init {
      //  Log.msg(TAG,"init..")
        this.ctx = context
    }
    //

    suspend fun execute(userSessionCredentials: UserSessionCredentials): Response<CertsEncResponse> {

        val url = UrlServer.getHttp() +BuildConfig.ic03 // "/api/mobile/iot-certificates"
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
        val response =   api.getCerts(url, userSessionCredentials.getHeadersWithoutCognito())


        Log.msg(TAG,"[execute]  --------------------------------------------------")

        if(response.isSuccessful){
           // if(response.code() == 200)
        //    Log.msg(TAG,"[execute] isSuccessful: "+ response.code())
        //    Log.msg(TAG,"[execute] response: \n"+response.body())
        //    onSuccess(response.code(),response.message())
        }else{
            Log.msg(TAG,"[execute] isFailure: "+ response.code())
            onError(response.code(),response.errorBody()!!.string(),"GCT","")
          //  com.macropay.utils.broadcast.Sender.sendHttpError(response.code(),response.message(),url,ctx)
        }
        Log.msg(TAG,"[execute] -termino-")
        return response
    }
}

