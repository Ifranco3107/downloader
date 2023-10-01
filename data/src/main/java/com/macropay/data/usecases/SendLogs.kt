package com.macropay.data.usecases

import android.content.Context
import com.google.gson.Gson
import com.macropay.data.di.Session
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.repositories.LogsRepository
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.phone.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

class SendLogs
@Inject constructor(
    @ApplicationContext val context: Context
) {

    private val TAG = "SendLogs"

    @Inject
    lateinit var logsRepository: LogsRepository

    //@Inject
    //lateinit var session: Session

    @Inject
    lateinit var cabeceras: UserSessionCredentials

    fun send(file: File) {

        val imei: String = DeviceInfo.getDeviceID()

        Log.msg(TAG,"[send] file: "+file.absolutePath)
        Log.msg(TAG,"[send] imei: "+imei)

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = logsRepository.execute(imei, file)
                    if(response.isSuccessful){
                        Log.msg(TAG,"[send] isSuccessful")
                        try{
                            Log.msg(TAG,"[sendLogs] va borrar el archivo...")
                            file.delete()
                        }catch (ex:Exception){
                            ErrorMgr.guardar(TAG,"send[1]",ex.message)
                        }
                    }else{
                        Log.msg(TAG,"[send] failure, intento 2")
                        delay(3_000)
                        val response = logsRepository.execute(imei, file)
                    }

                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "send", ex.message, file.absolutePath)
                }
            }

        }

    }

}