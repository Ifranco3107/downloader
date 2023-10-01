package com.macropay.downloader.receivers

import com.macropay.downloader.utils.Settings.initialized
import com.macropay.downloader.utils.Settings.init
import com.macropay.downloader.utils.Settings.getSetting
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.macropay.downloader.utils.location.LocationDevice
import com.macropay.downloader.data.preferences.TipoBloqueo
import com.google.android.gms.location.LocationResult
import com.macropay.downloader.utils.location.LocationSender
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.lang.Exception

class LocationReceiver : BroadcastReceiver() {
    private val TAG = "LocationReceiver"
    private val lastSent: LocationDevice? = null
    //Inyeccion de dependencias...
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MyClassInterface {
        fun getLocationSender(): LocationSender

    }
    //Inicializa objetos inyectables
    fun  inject(context: Context): MyClassInterface {
        return     EntryPoints.get(context.applicationContext, MyClassInterface::class.java)
    }


    override fun onReceive(context: Context, intent: Intent) {
        if (intent == null) {
            return
        }
        if (!initialized()) init(context)

        //Sino esta habilitado el Tracking GPS, se sale...
        val enableTracking = getSetting(TipoBloqueo.disable_tracking_GPS, false)
      //  Log.msg(TAG, "[onReceive] enableTracking: $enableTracking")
        if (!enableTracking) return

        // Log.msg(TAG,"[onReceive] trackingEnabled: "+trackingEnabled);
        val action = intent.action
        if (ACTION_PROCESS_UPDATES != action) return
        try {
            val result = LocationResult.extractResult(intent) ?: return

            //Obtiene ubicaion...
            val locations = result.locations
            if (locations.size == 0) return



            //Envia las ubicaciones recibidas...
            for (loc in locations) {
                Log.msg(TAG, "[onReceive] location: " + loc.latitude + " ," + loc.longitude)
              //  inject(context).getLocationSender().sendPos(loc, context,TAG) //onReceive
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "onReceive", ex.message)
        }
    }

    companion object {
        const val ACTION_PROCESS_UPDATES = "com.macropay.downloader.LocationReceiver.ACTION_PROCESS_UPDATES"
    }
}