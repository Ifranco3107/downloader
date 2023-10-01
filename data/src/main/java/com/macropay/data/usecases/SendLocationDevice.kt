package com.macropay.data.usecases

import android.content.Context
import com.google.gson.Gson
import com.macropay.data.di.Session


import com.macropay.data.di.UserSessionCredentials

import com.macropay.data.dto.ResponseData
import com.macropay.data.dto.request.LocationDto
import com.macropay.data.preferences.Values
import com.macropay.data.repositories.LocationRepository
import com.macropay.data.repositories.TrxOffline
import com.macropay.utils.Fechas
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.FileMgr
import com.macropay.utils.network.Red
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.phone.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class SendLocationDevice
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val TAG = "SendLocationDevice"

    @Inject
    lateinit var locationRepository: LocationRepository
    //@Inject
    //lateinit var session: Session

    @Inject
    lateinit var cabeceras: UserSessionCredentials

    fun send(latitud: Double, longitud: Double, metros: Int) {

        val locationDto = LocationDto(
            imei = DeviceInfo.getDeviceID(),
            latitud = latitud.toString(),
            longitud = longitud.toString(),
            metros = metros,
            fec_gps = Fechas.getToday()
        )
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {

                    val response = locationRepository.execute(locationDto, cabeceras)

                } catch (ex: Exception) {
                    val json = Gson().toJson(locationDto)
                    TrxOffline.guardarDtoTrx("GPS", locationDto)
                    if(Red.isOnline)
                        ErrorMgr.guardar(TAG, "send", ex.message, json)
                }
            }

        }

    }

    fun sendObject(locationDto: LocationDto,file: File) {

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = locationRepository.execute(locationDto, cabeceras)
                    if(response.isSuccessful){
                        FileMgr.eliminarArchivo(file)
                    }
                } catch (ex: Exception) {
                    val json = Gson().toJson(locationDto)
                    ErrorMgr.guardar(TAG, "send", ex.message, json)
                }
            }
        }

    }
}