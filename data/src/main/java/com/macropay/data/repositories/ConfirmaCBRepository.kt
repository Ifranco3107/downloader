package com.macropay.data.repositories
import android.content.Context
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.request.ConfirmaCBDto
import com.macropay.data.dto.response.ConfirmaCBResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

import com.macropay.data.preferences.Defaults
import com.macropay.data.server.DeviceAPI
import com.macropay.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons

class ConfirmaCBRepository
@Inject constructor(@ApplicationContext context: Context,
                    private val api: DeviceAPI
): DataResponse(){

    val TAG = "ConfirmaCBRepository"
    val ctx :Context
    init {
  //      Log.msg(TAG,"init..")
        this.ctx = context
    }
    //
    suspend fun execute(confirmaCBDto: ConfirmaCBDto,userSessionCredentials: UserSessionCredentials): Response<ConfirmaCBResponse> {
        Log.msg(TAG,"[execute]  imei:"+ confirmaCBDto.imei)
        val url = UrlServer.getHttp() + BuildConfig.cb04 //"/api/devices/update/CodeBarStatus"
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
        //Log.msg(TAG,"url: "+url)
        //Log.msg(TAG,"apiKeyMobile: "+apiKeyMobile)
        val response =   api.sendConfirmacionCB(url,confirmaCBDto, userSessionCredentials.getHeadersWithoutCognito())


        Log.msg(TAG,"[execute]  --------------------------------------------------")

        if(response.isSuccessful){
           // if(response.code() == 200)
            Log.msg(TAG,"[execute] isSuccessful: "+ response.code())
            Log.msg(TAG,"[execute] response: \n"+response.body())
            onSuccess(response.code(),response.message())
        }else{
            Log.msg(TAG,"[execute] isFailure: "+ response.code())
            onError(response.code(),response.errorBody()!!.string(),"CCB",confirmaCBDto)
            //com.macropay.utils.broadcast.Sender.sendHttpError(response.code(),response.message(),url,0,ctx)
        }
        Log.msg(TAG,"[execute] -termino-")
        return response
    }
}

