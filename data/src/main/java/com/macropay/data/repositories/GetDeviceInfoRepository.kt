package com.macropay.data.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.request.EnrollDto
import com.macropay.data.dto.response.CertsEncResponse
import com.macropay.data.dto.response.EnrollInfoResponse
import com.macropay.data.dto.response.enroll.EnrollResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.macropay.data.server.DeviceAPI
import com.macropay.data.logs.Log
import com.macropay.data.preferences.Defaults

import com.macropay.utils.AES
import com.macropay.utils.Settings
import com.macropay.utils.preferences.Cons
import okhttp3.ResponseBody
import retrofit2.Response


class GetEnrollInfoRepository
@Inject constructor(
    @ApplicationContext context: Context,
    private val api: DeviceAPI
) : DataResponse() {
    val ctx: Context
    val TAG = "GetEnrollInfoRepository"

    init {
        this.ctx = context
    }

    suspend fun executeVM  ( userSessionCredentials: UserSessionCredentials): Response<EnrollInfoResponse> {

        val url = UrlServer.getHttp() + BuildConfig.ei19

        val response = api.getEnrollInfo(url, userSessionCredentials.getHeadersWithoutCognito())
        if (response.isSuccessful) {
            Log.msg(TAG,"[execute] code: "+response.code())
            Log.msg(TAG,"[execute] body: "+response.body())
            onSuccess(response.code(), response.message())
        } else {
            Log.msg(TAG, "[execute] FAILURE  1.- error: \n" + response.errorBody()!!.string())
            onError(response.code(), response.errorBody()!!.string(), "GEI", url)

        }
        //Log.msg(TAG, "[execute] -termino-")
        return response
    }
}
