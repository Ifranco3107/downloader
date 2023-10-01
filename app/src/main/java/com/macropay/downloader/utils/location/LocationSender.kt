package com.macropay.downloader.utils.location

import android.content.Context
import android.location.Location
import com.macropay.data.usecases.SendLocationDevice
import com.macropay.downloader.data.preferences.TipoBloqueo
import com.macropay.utils.preferences.Cons
import com.macropay.downloader.data.preferences.TipoParametro
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.Utils
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class LocationSender
@Inject constructor(
        @ApplicationContext var context:Context,
        private val sendLocationDevice: SendLocationDevice) {

    private  val TAG = "LocationSender"
    private var lastLocation: Location? = null //

    fun sendPos(location: Location?, context: Context?,source:String) {
        //Sino esta habilitado el Tracking GPS, se sale...
        val enableTracking = Settings.getSetting(TipoBloqueo.disable_tracking_GPS, false)
      //  Log.msg(TAG, "[sendPos] enableTracking: $enableTracking")
        if (!enableTracking) return

        var ln = 0
        try {
            val envioForced = Settings.getSetting(TipoParametro.limiteSinEnvioGPS, 30)
            val rangoMismoLugar = Settings.getSetting(TipoParametro.rangoMismoLugarGPS, 50)
            if (location == null) return

            var distancia = rangoMismoLugar + 1 //Para que entre en caso que sea la primera vez.
            ln = 1
            if (lastLocation != null){
                distancia = location.distanceTo(lastLocation).toInt()
            }else{
                Log.msg(TAG,"[sendPos] lastLocation == null")
            }

            ln = 2
            Log.msg(TAG, "[sendPos] Lat:" + location.latitude + " , " + location.longitude + " [" + distancia + " metros.] [$source]")
            if(!vencioFrecuenciaEnvio()){
                Log.msg(TAG,"[sendPos] Aun no cumple la frecuencia de envio...")
                return
            }

            ln = 1
            if (distancia >= rangoMismoLugar
                || vencioLimiteSinEnvio(envioForced)) {
                ln = 3
                lastLocation = location
                ln = 4
                Settings.setSetting(Cons.KEY_LAST_GPS_SENT, LocalDateTime.now())
                Settings.setSetting(Cons.KEY_LAST_LAT_SENT, lastLocation!!.latitude.toString())
                Settings.setSetting(Cons.KEY_LAST_LON_SENT, lastLocation!!.longitude.toString())
                Log.msg(TAG, "[sendPos] Envio: [" + location.latitude + "," + location.longitude + "] $distancia")
                ln = 5
                sendLocationDevice.send(lastLocation!!.latitude, lastLocation!!.longitude,distancia)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendPos($ln)", ex.message)
        }
    }

    fun vencioLimiteSinEnvio(minutos_limite: Int): Boolean {
        var minutos = minutos_limite + 1
        try {
            //val UltimoEnvio = SettingsApp.ultimoEnvioGPS() //// LocalDateTime.now();
           val  DefaultUltimoEnvio = LocalDateTime.now().minusMinutes(minutos.toLong())
            val UltimoEnvio =  Settings.getSetting(Cons.KEY_LAST_GPS_SENT, DefaultUltimoEnvio)
            Log.msg(TAG,"[vencioLimiteSinEnvio] UltimoEnvio: $UltimoEnvio")
            if (UltimoEnvio != null)
                minutos = Math.toIntExact(java.lang.Long.valueOf(Utils.tiempoTranscurrido(UltimoEnvio, ChronoUnit.MINUTES)))
            Log.msg(TAG,"[vencioLimiteSinEnvio] minutos: $minutos")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "vencioLimiteSinEnvio", ex.message)
        }
        return minutos > minutos_limite
    }
    fun vencioFrecuenciaEnvio():Boolean{
        val fecEnvioGPS = Settings.getSetting(TipoParametro.frecuenciaCapturaGPS, 10)
        var bVencio = true
        if (!vencioLimiteSinEnvio(fecEnvioGPS)) {
            Log.msg(TAG, "Tiene menos de $fecEnvioGPS minutos de haber requerido GPS, SE SALE... ")
            bVencio = false
        }
        return bVencio
    }
}