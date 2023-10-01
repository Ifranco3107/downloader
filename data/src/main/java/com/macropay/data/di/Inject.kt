package com.macropay.data.di

import com.macropay.data.preferences.Values
import com.macropay.data.usecases.*
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

//Pare forzar la inyeccion de dependecias, cuando la no soporta nativamente la DI
object Inject {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DataClassInterface {
        fun getSendLocationDevice(): SendLocationDevice
        fun getSendUpdateStatus(): SendUpdateStatus
        fun getGetCerts() :GetCerts
        fun getSendError() :SendError
        fun getSendComments() :SendComments
        fun getUpdateAppStatus(): UpdateAppsStatus
    }


    //Inicializa objetos inyectables
    fun  inject(): DataClassInterface {
        return     EntryPoints.get(Values.context, DataClassInterface::class.java)
    }
}