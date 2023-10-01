package com.macropay.data.repositories

import android.content.Context
import android.os.Build
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

import com.macropay.data.dto.request.EnrollDto
import com.macropay.data.dto.request.LocationDto
import com.macropay.data.dto.response.UnrollResponse
import com.macropay.data.dto.response.enroll.EnrollResponse
import com.macropay.data.dto.response.enroll.Restriction
import com.macropay.data.preferences.Consts
import com.macropay.data.preferences.Defaults
import com.macropay.data.preferences.Values
import com.macropay.data.server.DeviceAPI
import com.macropay.data.usecases.Sender
import com.macropay.data.usecases.StatusRestrictions
import com.macropay.utils.Fechas
import com.macropay.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons
import okhttp3.ResponseBody
import retrofit2.Response

class EnrollRepository
@Inject constructor(
    @ApplicationContext context: Context,
    private val api: DeviceAPI
) : DataResponse() {
    val ctx: Context
    val TAG = "EnrollRepository"

    init {
        // Log.msg(TAG,"* init *")
        this.ctx = context
    }

    suspend fun execute(enrollDto: EnrollDto, userSessionCredentials: UserSessionCredentials?): StatusRestrictions {
        var statusRestrictions = StatusRestrictions(false, 500, "")

        Log.msg(TAG, "[execute]  imei:" + enrollDto.imei)
        //val url = UrlServer.getHttp() +"/api/devices/enroll/Device"
        val url = UrlServer.getHttp() + BuildConfig.ed01
        // Log.msg(TAG,"[execute] url2: "+url2)
        // val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE,Defaults.API_KEY)
        // Log.msg(TAG,"[execute] url: "+url)
        // Log.msg(TAG,"[execute] apiKeyMobile: "+apiKeyMobile)
        val response = api.enroll(url, enrollDto, userSessionCredentials?.getHeadersWithoutCognito())

        //Log.msg(TAG, "[execute]  --------------------------------------------------")

        if (response.isSuccessful) {
            onSuccess(response.code(), response.message())
            var body = response.body()!!.data
            // Log.msg(TAG, "[execute] isSuccessful  1.- body: \n" + body)
            val gson = Gson()
            val dataEnroll = gson.toJson(body)
            // Log.msg(TAG, "[execute] isSuccessful  2.- data:\n\n" + dataEnroll)
            statusRestrictions = StatusRestrictions(true, response.code(), dataEnroll)
        } else {
            //   Log.msg(TAG,"[execute] isFailure: "+ response.code())
            Log.msg(TAG, "[execute] FAILURE  1.- error: \n" + response.errorBody()!!.string())
            onError(response.code(), response.errorBody()!!.string(), "ENR", enrollDto)
            statusRestrictions = StatusRestrictions(false, response.code(), response.errorBody()!!.string())
            //  com.macropay.utils.broadcast.Sender.sendHttpError(response.code(),response.message(),url,ctx)
        }
        Log.msg(TAG, "[execute] -termino-")
        return statusRestrictions
    }



    //----------------------------
    suspend fun executeVM(enrollDto: EnrollDto, userSessionCredentials: UserSessionCredentials?): Response<EnrollResponse> {

        val url = UrlServer.getHttp() + BuildConfig.ed01
        val response = api.enroll(url, enrollDto, userSessionCredentials?.getHeadersWithoutCognito())

        Log.msg(TAG,"[execute]  --------------------------------------------------")
        if (response.isSuccessful) {
            onSuccess(response.code(),response.message())
        } else {
            //   Log.msg(TAG,"[execute] isFailure: "+ response.code())
            Log.msg(TAG, "[execute] FAILURE  1.- error: \n" + response.errorBody()!!.string())
            onError(response.code(), response.errorBody()!!.string(), "ENR", enrollDto)
        }
        Log.msg(TAG,"[execute] -termino-")
        return response
    }
}

/*


    val TAG = "EnrollRepository"

    var result : StatusRestrictions?  = null
    get() {
        return field
    }

    //
    suspend fun execute(enroll: EnrollDto): Response<EnrollResponse> {
        println("[EnrollRepository] postEnroll - 1 - v2")
        Log.init("postEnroll",Values.context!!)
        val gson = Gson()
        Log.msg(TAG,"[execute]  imei:"+ enroll.toString())
*/
/*        Log.msg(TAG,"[execute]  --------------------------------------------------")

        val strEnroll= gson.toJson(enroll)
        Log.msg(TAG,"[execute]  json:"+ strEnroll)*//*

        var bResult = false
        var statusRestrictions:StatusRestrictions = StatusRestrictions(false,500,"")

      //  try{
            Log.msg(TAG,"[execute] << v3 >>>-------------------------------------------------->>>>")
            val response = api.enroll(enroll, Consts.API_KEY)
            Log.msg(TAG,"[execute] -2-")
            if(response.isSuccessful){
                Log.msg(TAG,"[execute] isSuccessful: "+ response.code())
                Log.msg(TAG,"[execute] response: \n"+response.body()!!)
              //  var data: List<Restriction> = emptyList()
              //  data= response.body()!!.data.get(0).restrictions
                var body = response.body()!!.data.get(0)
                Log.msg(TAG,"[execute] 1.- body: \n"+body)

              //  val jsonElements :JsonArray = Gson().toJsonTree(body.restrictions) as JsonArray
              //  Log.msg(TAG,"jsonElements: "+jsonElements)

                val strEnroll= gson.toJson(body)
                Log.msg(TAG,"[execute] 2.- data:\n\n"+ strEnroll)
                onSuccess(response.code(),response.message())
                statusRestrictions= StatusRestrictions(true,response.code(),strEnroll)

                bResult = true
            }else{
                Log.msg(TAG,"[execute] -3-")
                Log.msg(TAG,"[execute] isFailure: "+ response.code())
                onError(response.code(),response.errorBody()!!.string(),"Enroll",enroll)
                statusRestrictions= StatusRestrictions(false,response.code(),response.message())
            }
*/
/*        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"execute",ex.message)
        }*//*

        Log.msg(TAG,"[execute] -termino- " +bResult)
        return response
    //  return statusRestrictions
    }
}


class StatusRestrictions(val isSucces: Boolean,val code:Int ,val body:String)

*/
