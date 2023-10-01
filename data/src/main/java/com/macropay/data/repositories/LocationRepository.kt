package com.macropay.data.repositories
import android.content.Context
import com.google.gson.Gson
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

import com.macropay.data.dto.request.LocationDto
import com.macropay.data.dto.response.LocationResponse
import com.macropay.data.dto.response.UnrollResponse
import com.macropay.data.preferences.Consts
import com.macropay.data.preferences.Defaults
import com.macropay.data.preferences.Values
import com.macropay.data.server.DeviceAPI
import com.macropay.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons

class LocationRepository
@Inject constructor(@ApplicationContext context: Context,
                    private val api: DeviceAPI
): DataResponse(){

    val TAG = "LocationRepository"

    //
    suspend fun execute(locationDto: LocationDto,userSessionCredentials: UserSessionCredentials): Response<LocationResponse> {
        Log.msg(TAG,"[execute]  imei:"+ locationDto.imei)
        val url = UrlServer.getHttpRpt() + BuildConfig.gp05 // "/api/devices/send/DeviceLocation"
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
       // Log.msg(TAG,"url: "+url)
       // Log.msg(TAG,"apiKeyMobile: "+apiKeyMobile)
        val response = api.locationDevice(url,locationDto, userSessionCredentials.getHeadersWithoutCognito())


        Log.msg(TAG,"[execute]  --------------------------------------------------")

        if(response.isSuccessful){
           // if(response.code() == 200)
           // Log.msg(TAG,"[execute] isSuccessful: "+ response.code())
           // Log.msg(TAG,"[execute] response: \n"+response.body())
            onSuccess(response.code(),response.message())
        }else{
            Log.msg(TAG,"[execute] isFailure: "+ response.code())
            onError(response.code(),response.errorBody()!!.string(),"GPS",locationDto)
        }
        Log.msg(TAG,"[execute] -termino-")
        return response
    }
}

