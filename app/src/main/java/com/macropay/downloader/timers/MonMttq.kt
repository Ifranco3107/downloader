package com.macropay.downloader.timers

import com.macropay.downloader.data.preferences.dpcValues.mqttAWS

import com.macropay.utils.network.Red.isOnline
import com.macropay.utils.network.Red.enableWifi

import com.macropay.utils.phone.DeviceCfg.getImei
import android.content.Context
import com.macropay.downloader.utils.SettingsApp
import android.os.Build
import com.macropay.downloader.utils.Utils
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import java.lang.Exception
import java.time.temporal.ChronoUnit.*
import java.util.*

//---------------------------------------------------------------
// Monitoreo de Dias sin Conexion a
//---------------------------------------------------------------
class MonMttq(private val context: Context) : TimerTask() {
   // private val activity: Activity
    val TAG = "MonMqtt"
    var bProcesando = false

    //Constructor
    init {
        Log.msg(TAG,"-init-")
     //   activity = activity
    }

    override fun run() {
        try {
            val inicioUpdater = SettingsApp.inicioUpdater()
            if (inicioUpdater != null) {
                val minsUpdater = Utils.tiempoTranscurrido(inicioUpdater, MINUTES)
                if (minsUpdater < 10) {
                    Log.msg(TAG, "[run] Updater en proceso...[Se sale del monitor...] mins: $minsUpdater")
                    return
                } else {
                    Log.msg(TAG, "[run] Da por terminado el Updater, y reinicia el MQTT $minsUpdater")
                    //ConnectMQTT.connect(context,"MonMttq.endUpdater");
                    mqttAWS!!.connect(TAG)
                }
            }
            initLog(context)
            //Si es cambio de dia, pone el cabecero con los datos del equipo.
            var hasNetwork = isOnline
            if (!hasNetwork) {
                Log.msg(TAG, "[run] hasNetwork: $hasNetwork")
                enableWifi()
            }
            hasNetwork = isOnline
            val isMQTTConnected = mqttAWS!!.isConnected
            Log.msg(TAG, "[run] - - - - - - - <  Monitor de MonMttq  > - - - - - - - - - - - - - - - -isMQTTConnected: [$isMQTTConnected] hasNetwork: $hasNetwork")
            /*            Log.msg(TAG, "ServiceConnected: "+ ConnectMQTT.isConnected() +" hasNetwork: "+hasNetwork);
            if(!ConnectMQTT.isConnected()) {
                Log.msg(TAG,"---> Va reconectar... ConnectMQTT.connect();");
                ConnectMQTT.connect(context,"MonMttq");
            }*/

            if (!isMQTTConnected) {
                Log.msg(TAG, "[run] Reintenta conectar el MQTT -- commented")
                //TODO; Se comento, porque marcaba error, debido a que MQTT tiene el autoconnect
               // mqttAWS!!.connect(TAG)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "run: ", ex.message)
        }
    }

    private fun initLog(context: Context) {
        try {
            val curName = Log.fileName()
            //Se inicializa aqui, para que cambie de Log, en el cambio de dia.
            Log.init("downloader", context)
            if (curName == Log.fileName()) return
            Log.msg(TAG, "Equipo: " + Build.MODEL + " - " + Build.PRODUCT)
            Log.msg(TAG, "imei: " + getImei(context))
            Log.msg(TAG, "Android " + Build.VERSION.RELEASE + "  [ " + Build.VERSION.SDK_INT + " ]")
/*            Log.msg(TAG, "statusEnroll(): " + SettingsApp.statusEnroll())
            Log.msg(TAG, "isKiosko():" + SettingsApp.isKiosko())
            Log.msg(TAG, "Nivel():" + SettingsApp.Nivel())*/
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = packageInfo.longVersionCode
            val versionName = packageInfo.versionName
            Log.msg(TAG, "Versión: $versionName ($version)")
            Log.msg(TAG, "=================================")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "initLog", ex.message)
        }
    }
}