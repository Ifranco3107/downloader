package com.macropay.data.usecases

import android.content.Context
import com.google.gson.Gson
import com.macropay.data.di.Session
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.ResponseData
import com.macropay.data.dto.request.ReportStatusDto
import com.macropay.data.dto.response.SyncReportResponse
import com.macropay.data.repositories.ReportStatusRepository
import com.macropay.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject

class SendLockStatus
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val TAG = "SendLockStatus"

    @Inject
    lateinit var reportStatusRepository: ReportStatusRepository
    //@Inject
    //lateinit var session: Session

    @Inject
    lateinit var cabeceras: UserSessionCredentials


    //Este endPoint se usa solo para reportar cada x tiempo, el status del bloqueo.
    //Usado en el MonSinConexion
    fun send(lockId: String, locked: Boolean, kioskActived: String) {


        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val reportStatusDto = getDto(lockId, locked, kioskActived, true)
                try {
                    Log.msg(TAG, "[send]")
                    val gson = Gson()
                    val json = gson.toJson(reportStatusDto)
                    //   Log.msg(TAG,"reportStatusDto:"+json)
                    val response = reportStatusRepository.executeMqtt(reportStatusDto, cabeceras)
                } catch (ex: Exception) {
                    val json = Gson().toJson(reportStatusDto)
                    ErrorMgr.guardar(TAG, "send", ex.message, json)
                }
            }
        }
    }

    // suspend fun send(): Deferred<StatusRestrictions> = coroutineScope {
    // suspend fun send(): Deferred<StatusRestrictions> = coroutineScope {
    suspend fun sendHttp(lockId: String, locked: Boolean, kioskActived: String): Deferred<SyncReportResponse> = coroutineScope {

        Log.msg(TAG, "[sendHttp]")
        val reportStatusDto = getDto(lockId, locked, kioskActived, false)
        val gson = Gson()
        var dataBody = gson.toJson(reportStatusDto)
        Log.msg(TAG, "[sendHttp] dataBody: $dataBody")
        //val credenciales = session.getCredentials()
        var result = async {
            reportStatusRepository.executeHttp(reportStatusDto, cabeceras)
        }
        result.await()
        result
    }


    fun getDto(lockId: String, locked: Boolean, kioskActived: String, reponseMqtt: Boolean): ReportStatusDto {
        val lockActived = if (locked) "1" else "0"
        val typeResponse = if (reponseMqtt) 1 else 0
        var lastMsgProcessed: Long = Settings.getSetting(Cons.KEY_LAST_MSG_PROCESSED, 0L)
        var imei = DeviceInfo.getDeviceID() //  DeviceCfg.getImei(context)

        val reportStatusDto = ReportStatusDto(
            imei,
            lockId,
            lockActived,
            kioskActived,
            lastMsgProcessed,
            typeResponse
        )
        var responseData: ResponseData? = null

        val gson = Gson()
        val json = gson.toJson(reportStatusDto)
        Log.msg(TAG, "[getDto] reportStatusDto: " + json)
        return reportStatusDto
    }

    suspend fun sendHttpUrl(lockId: String, locked: Boolean, kioskActived: String,newUrl:String?): Deferred<Boolean> = coroutineScope {


        Log.msg(TAG, "[sendHttp]")
        val reportStatusDto = getDto(lockId, locked, kioskActived, false)
        //val credenciales = session.getCredentials()
        var result = async {
            reportStatusRepository.executeHttpUrl(reportStatusDto, cabeceras,newUrl!!)
        }
        result.await()
        result

    }
}