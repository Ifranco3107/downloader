package com.macropay.data.usecases

import android.content.Context
import com.google.gson.Gson
import com.macropay.data.di.Session

import com.macropay.data.di.UserSessionCredentials

import com.macropay.data.dto.request.ErrorDto
import com.macropay.data.dto.request.LocationDto
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.repositories.ErrorRepository
import com.macropay.data.repositories.TrxOffline
import com.macropay.utils.Fechas
import com.macropay.utils.FileMgr
import com.macropay.utils.network.Red
import com.macropay.utils.phone.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class SendError
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val TAG = "SendError"


    @Inject
    lateinit var errorRepository: ErrorRepository

    //@Inject
    //lateinit var session: Session
    @Inject
    lateinit var cabeceras:UserSessionCredentials

    fun send(
        app: String,
        clase: String,
        funcion: String,
        error: String,
        datos: String
    ) {


        //Crea el dto.
        val errorDto = ErrorDto(
            imei = DeviceInfo.getDeviceID(),
            fecha_alta = Fechas.getToday(),
            app = app,
            clase = clase,
            funcion = funcion,
            error = error,
            datos = datos
        )

        val gson = Gson()
        val json = gson.toJson(errorDto)
       // Log.msg(TAG, "errorDto: \n" + json)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                if(Red.isOnline)
                    errorRepository.execute(errorDto, cabeceras)
                else
                    TrxOffline.guardarTrx("ERR",errorDto.toString())

                } catch (ex: Exception) {
                    // ErrorMgr.guardar(TAG,"send",ex.message)
                    //val gson = Gson()
                    //val json = gson.toJson(errorDto)
                    //      Log.msg(TAG,"phoneNumberDto:"+json)
                }
            }
        }

    }
    fun sendObject(errorDto: ErrorDto, file: File) {

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = errorRepository.execute(errorDto, cabeceras)
                    if(response.isSuccessful){
                        FileMgr.eliminarArchivo(file)
                    }
                } catch (ex: Exception) {
                    val json = Gson().toJson(errorDto)
                    ErrorMgr.guardar(TAG, "send", ex.message, json)
                }
            }
        }

    }
}