package com.macropay.downloader.di

import android.content.Context
import com.macropay.data.usecases.*
import com.macropay.downloader.data.awsiot.Mqtt
import com.macropay.downloader.data.mqtt.messages.*
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.domain.usecases.provisioning.Provisioning
import com.macropay.downloader.utils.location.LocationDevice
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

//Pare forzar la inyeccion de dependecias, cuando la no soporta nativamente la DI
//Por ejemplo los receivers, aun no soporta el AndroidEntryPoint.

object Inject {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DPCClassInterface {
        fun getLocationDevice(): LocationDevice
        fun getSendPackageVersion() : SendPackageVersion
        fun getMqtt(): Mqtt
        fun getProvision():Provisioning
    }

    //Inicializa objetos inyectables
    fun  inject(): DPCClassInterface {
        return     EntryPoints.get(dpcValues.mContext!!, DPCClassInterface::class.java)
    }
    fun  inject(context: Context): DPCClassInterface {
        return     EntryPoints.get(context, DPCClassInterface::class.java)
    }
}