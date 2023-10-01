package com.macropay.data.repositories
import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject

import com.macropay.data.dto.request.ReportStatusDto
import com.macropay.data.dto.response.ReportStatusResponse
import com.macropay.data.dto.response.SyncReportResponse
import com.macropay.data.dto.response.SyncReportResponseMqtt
import com.macropay.data.preferences.Consts
import com.macropay.data.preferences.Defaults
import com.macropay.data.preferences.Values
import com.macropay.data.server.DeviceAPI
import com.macropay.data.usecases.StatusRestrictions
import com.macropay.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons
import org.json.JSONObject
import java.lang.reflect.Type

class ReportStatusRepository
@Inject constructor(@ApplicationContext context: Context,
                    private val api: DeviceAPI
): DataResponse(){

    val TAG = "ReportStatusRepository"
    //SyncReportResponseMqtt
    suspend fun executeMqtt(reportStatusDto: ReportStatusDto,userSessionCredentials: UserSessionCredentials ): Response<ReportStatusResponse> {
        //Log.msg(TAG,"[executeMqtt]  imei:"+ reportStatusDto.imei)
        val url=  UrlServer.getHttp() + BuildConfig.rs02
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
      //  Log.msg(TAG,"[executeMqtt] url: "+url)
     //   Log.msg(TAG,"[executeMqtt] apiKeyMobile: "+apiKeyMobile)

        val response = api.reportStatus(url,reportStatusDto, userSessionCredentials.getHeadersWithoutCognito())

        Log.msg(TAG,"[executeMqtt]  --------------------------------------------------")

        if(response.isSuccessful){
          //  Log.msg(TAG,"[executeMqtt] isSuccessful: "+ response.code())
          //  Log.msg(TAG,"[executeMqtt] response: \n"+response.body())
            onSuccess(response.code(),response.message())
            //---TODO
            //---Esto sirve solo para los telefonos que instalaron antes del cambio de IMEI como id.
            //---Quitar cuando ya todos los telefonos tengan la ultima version.
            checkDeviceID(reportStatusDto.imei)
        }else{
            Log.msg(TAG,"[executeMqtt] isFailure: "+ response.code())
            onError(response.code(),response.errorBody()!!.string(),"RPT",reportStatusDto)
        }
        Log.msg(TAG,"[executeMqtt] -termino-")
        return response
    }

    fun checkDeviceID(currentID:String): String {
        var deviceID = "Inst"
        val valDefault = "000000"
        try {
            deviceID = Settings.getSetting(Cons.KEY_ID_DEVICE, valDefault)
            if(deviceID.equals(valDefault)) {
                Log.msg(TAG,"[checkDeviceID] NO TENIA KEY_DEVICE: [$currentID]")
                Settings.setSetting(Cons.KEY_ID_DEVICE, currentID)
                Settings.setSetting(Cons.KEY_UPDATED_ID_DEVICE, true)
            }
        } catch (ex: java.lang.Exception) {
             ErrorMgr.guardar(TAG, "checkDeviceID", ex.message)
        }
        return deviceID
    }
   /* suspend fun executeMqtt(reportStatusDto: ReportStatusDto ) {
    //    suspend fun execute(reportStatusDto: ReportStatusDto ): Response<ReportStatusResponse> {
        Log.msg(TAG,"[execute]  imei:"+ reportStatusDto.imei)
        val url=  UrlServer.getHttp() + BuildConfig.rs02
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
        Log.msg(TAG,"[execute] url: "+url)
        Log.msg(TAG,"[execute] apiKeyMobile: "+apiKeyMobile)

        return try{
            val response  = api.reportStatus(url,reportStatusDto, apiKeyMobile)
            if(response.isSuccessful){
                Log.msg(TAG,"[execute] isSuccessful: "+ response.code())
                Log.msg(TAG,"[execute] response: \n"+response.body())
                onSuccess(response.code(),response.message())
            }else{
                Log.msg(TAG,"[execute] isFailure: "+ response.code())
                onError(response.code(),response.errorBody()!!.string(),"RPT",reportStatusDto)
            }
            Log.msg(TAG,"[execute] -termino-")
            Log.msg(TAG,"[execute]  --------------------------------------------------")
            response
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"execute",ex.message)
            val response : Response<SyncReportResponse>? = null
            response!!
        }
    }*/
    suspend fun executeHttp(reportStatusDto: ReportStatusDto, credentials: UserSessionCredentials? ): SyncReportResponse {
        //    suspend fun execute(reportStatusDto: ReportStatusDto ): Response<ReportStatusResponse> {
        Log.msg(TAG,"[executeHttp]  imei:"+ reportStatusDto.imei)
        val url=  UrlServer.getHttp() + BuildConfig.rs02
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
        Log.msg(TAG,"[executeHttp] url: "+url)
      //  Log.msg(TAG,"[executeHttp] apiKeyMobile: "+apiKeyMobile)
       val gson = Gson()
       var dataBody = gson.toJson(reportStatusDto)

        var syncReportResponse : SyncReportResponse? = null
        try{

            val response  = api.reportStatusHttp(url,reportStatusDto, credentials?.getHeadersWithoutCognito())
            if(response.isSuccessful){
                onSuccess(response.code(),response.message())
                Log.msg(TAG,"[executeHttp] success ->code:  "+response.code())
                syncReportResponse =  response.body()!!
            }else{
                Log.msg(TAG,"[executeHttp] isFailure: "+ response.code())
                onError(response.code(),response.errorBody()!!.string(),"RPT",reportStatusDto)
            }
            Log.msg(TAG,"[executeHttp] -termino-")
            Log.msg(TAG,"[executeHttp]  --------------------------------------------------")

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"executeHttp",ex.message,dataBody)
        }

       return syncReportResponse!!
    }
    //

    suspend fun executeHttpUrl(reportStatusDto: ReportStatusDto, credentials: UserSessionCredentials?, newUrl:String): Boolean {
        //    suspend fun execute(reportStatusDto: ReportStatusDto ): Response<ReportStatusResponse> {
        Log.msg(TAG,"[executeHttpUrl]  imei:"+ reportStatusDto.imei)
        val url=   newUrl + BuildConfig.rs02
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
        Log.msg(TAG,"[executeHttpUrl] url: "+url)
        //  Log.msg(TAG,"[executeHttp] apiKeyMobile: "+apiKeyMobile)
        var syncReportResponse : SyncReportResponse? = null
        try{

            val response  = api.reportStatusHttp(url,reportStatusDto, credentials?.getHeadersWithoutCognito())
            if(response.isSuccessful){
                onSuccess(response.code(),response.message())
                Log.msg(TAG,"[executeHttpUrl] success ->code:  "+response.code())
                syncReportResponse =  response.body()!!
                return true
            }else{
                Log.msg(TAG,"[executeHttpUrl] isFailure: "+ response.code())
                onError(response.code(),response.errorBody()!!.string(),"RPT",reportStatusDto)
                return false

            }
            Log.msg(TAG,"[executeHttpUrl] -termino-")
            Log.msg(TAG,"[executeHttpUrl]  --------------------------------------------------")

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"executeHttp",ex.message)
            return false
        }


    }

}

