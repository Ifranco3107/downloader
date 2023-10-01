package com.macropay.data.repositories
import android.content.Context
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.request.ErrorDto
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject
import com.macropay.data.preferences.Defaults
import com.macropay.data.server.DeviceAPI
import com.macropay.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons
import okhttp3.ResponseBody

class ErrorRepository
@Inject constructor(@ApplicationContext context: Context,
                    private val api: DeviceAPI
): DataResponse(){

    val TAG = "ErrorRepository"
    val ctx :Context
    init {
      //  Log.msg(TAG,"init..")
        this.ctx = context
    }
    //
    suspend fun execute(errorDto: ErrorDto,userSessionCredentials: UserSessionCredentials): Response<ResponseBody> {
        // Log.msg(TAG,"[execute]  imei:"+ phoneNumberDto.imei)
        val url = UrlServer.getHttpRpt() +BuildConfig.er12 //"/api/locks/confirm/new/number"

        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
       // Log.msg(TAG,"url: "+url)
        // Log.msg(TAG,"apiKeyMobile: "+apiKeyMobile)
        val response =   api.sendError(url,errorDto, userSessionCredentials.getHeadersWithoutCognito())

        //Log.msg(TAG,"[execute]  --------------------------------------------------")

        if(response.isSuccessful){
           // if(response.code() == 200)
            // Log.msg(TAG,"[execute] isSuccessful: "+ response.code())
            // Log.msg(TAG,"[execute] response: \n"+response.body())
            onSuccess(response.code(),response.message())
        }else{
           // Log.msg(TAG,"[execute] isFailure: "+ response.code())
            onError(response.code(),response.errorBody()!!.string(),"ERR",errorDto)
        }
        //Log.msg(TAG,"[execute] -termino-")
        return response
    }
}

