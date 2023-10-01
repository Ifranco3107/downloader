package com.macropay.data.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.macropay.data.server.DeviceAPI
import com.macropay.data.logs.Log
import com.macropay.data.usecases.DeviceInformation


class GetDeviceInfoRepository
@Inject constructor(
    @ApplicationContext context: Context,
    private val api: DeviceAPI
) : DataResponse() {
    val ctx: Context
    val TAG = "GetSIMSAutorizadosRepository"

    init {
        this.ctx = context
    }

    suspend fun execute(deviceId: JsonObject, userSessionCredentials: UserSessionCredentials?): DeviceInformation {
        var deviceInformation = DeviceInformation(false, 500, "")

        val url = UrlServer.getHttp() + BuildConfig.di15
        val response = api.getDeviceInfo(url, deviceId, userSessionCredentials?.getHeadersWithoutCognito())
        if (response.isSuccessful) {
            Log.msg(TAG,"[execute] code: "+response.code())
            onSuccess(response.code(), response.message())
            var body = response.body()!!.data
            // Log.msg(TAG, "[execute] isSuccessful  1.- body: \n" + body)
            val gson = Gson()
            val dataEnroll = gson.toJson(body)
            deviceInformation = DeviceInformation(true, response.code(), dataEnroll)
        } else {
            Log.msg(TAG, "[execute] FAILURE  1.- error: \n" + response.errorBody()!!.string())
            onError(response.code(), response.errorBody()!!.string(), "SAU", deviceId)
            deviceInformation = DeviceInformation(false, response.code(), response.errorBody()!!.string())
        }
        Log.msg(TAG, "[execute] -termino-")
        return deviceInformation
    }
}
