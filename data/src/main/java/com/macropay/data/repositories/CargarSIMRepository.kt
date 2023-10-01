package com.macropay.data.repositories
import android.content.Context
import com.google.gson.JsonObject
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.request.CargarSimDto
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

import com.macropay.data.preferences.Defaults
import com.macropay.data.server.DeviceAPI
import com.macropay.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject

class CargarSIMRepository
@Inject constructor(@ApplicationContext context: Context,
                    private val api: DeviceAPI
): DataResponse(){

    val TAG = "CargarSIMRepository"
    val ctx :Context
    init {

        this.ctx = context
    }
    //

    suspend fun execute(cargarSimDto: CargarSimDto, userSessionCredentials: UserSessionCredentials): Response<ResponseBody> {
        val url = UrlServer.getHttp() +BuildConfig.ns14 //"/api/locks/confirm/new/number"
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)

        val response =   api.sendCargarSim(url,cargarSimDto, userSessionCredentials.getHeadersWithoutCognito())

        Log.msg(TAG,"[execute]  --------------------------------------------------")
        if(response.isSuccessful){
           // if(response.code() == 200)


             Log.msg(TAG,"[execute] isSuccessful: "+ response.code())
             Log.msg(TAG,"[execute] response: \n"+response.body())
            onSuccess(response.code(),response.message())
        }else{
            Log.msg(TAG,"[execute] isFailure: "+ response.code())
            onError(response.code(),response.errorBody()!!.string(),"CSI",cargarSimDto)
           // com.macropay.utils.broadcast.Sender.sendHttpError(response.code(),response.message(),url,ctx)
        }
        Log.msg(TAG,"[execute] -termino-")
        return response
    }
}

