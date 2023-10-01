package com.macropay.downloader.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import com.macropay.data.repositories.TrxOffline.enviaTxtPendientes
//import com.macropay.dpcmacro.data.awsiot.mqtt.isConnected
import com.macropay.utils.broadcast.Sender.sendNetworkStatus
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.NetworkInfo
import android.os.Looper
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.telephony.TelephonyManager
import kotlin.Throws
import android.os.Handler
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.network.Red
import com.macropay.utils.phone.DeviceCfg
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.InvocationTargetException
import kotlin.Exception

//import com.macropay.dpcmacro.data.remote.requests.PostMonitor;
//class NetworkReceiver : BroadcastReceiver() {
@AndroidEntryPoint
class NetworkReceiver : HiltBroadcasterReceiver() {

    var TAG = "NetworkReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION != intent.action)
            return

        val pendingResult: PendingResult = goAsync()
        val networkEvent = NetworkEvent(pendingResult, intent,context)
        networkEvent.execute()

        /**/
        //Log.msg(TAG, "[onReceive]  getAction: " + intent.getAction());
       /* try {
            val netInfo = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO) ?: return
            when (netInfo.state) {
                NetworkInfo.State.CONNECTED -> {
                    Log.msg(TAG, "[onReceive] getState: CONNECTED")
                    val handlerService = Handler(Looper.getMainLooper())

                    //Notifica para que se retire la ventana de Kiosko.
                    handlerService.postDelayed(
                        {
                            //Para asegurar que realmente se conecto, en ocasiones el usuario conecta y desconecta en el mismo moemnto.
                            if(Red.isOnline){
                                Log.d(TAG, "[onReceive] sendNetworkStatus")
                                //
                                sendNetworkStatus(true,context.applicationContext)
                            }
                            else
                                Log.msg(TAG,"Se volvio a DESCONECTAR...")
                        }, 2_000
                    )

                    //Envia las transacciones pendientes...
                    handlerService.postDelayed(
                        {
                            //Para asegurar que realmente se conecto, en ocasiones el usuario conecta y desconecta en el mismo moemnto.
                            if(Red.isOnline){
                                Log.d(TAG, "[onReceive] enviaTxtPendientes")
                                //Verifica si hay transacciones pendientes de enviar...
                                enviaTxtPendientes()
                            }
                            else
                                Log.msg(TAG,"Se volvio a DESCONECTAR...")
                        }, 15_000
                    )
                }
                NetworkInfo.State.DISCONNECTED -> {
                    Log.d(TAG, "[onReceive] getState: DISCONNECTED")
                   // sendLockStatus("StatusRed,Disconnected,")
                    sendNetworkStatus(false,context.applicationContext)
                }
                else -> {}
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "onReceive", ex.message)
        }*/
    }


    private fun notifyCentral(context: Context) {
        Log.msg(TAG, "[notifyCentral] - enable")
        try {
/*            boolean bIsOnLine = ConnectionStatus.isOnline();
            Log.msg(TAG,"SettingsApp.Nivel(): "+SettingsApp.Nivel() + " bIsOnLine: " + bIsOnLine);
            Log.msg(TAG, "Se Avisa a central, para que se desbloque. - bIsOnLine: " + bIsOnLine);
            */
            sendNetworkStatus(true, context)
            //  }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "notifyCentral", e.message)
        }
    }

    @SuppressLint("MissingPermission")
    fun ConnectionQuality(context: Context): String {
        val info = getInfo(context)
        if (info == null || !info.isConnected) {
            return "UNKNOWN"
        }
        return if (info.type == ConnectivityManager.TYPE_WIFI) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val numberOfLevels = 5
            val wifiInfo = wifiManager.connectionInfo
            val level = WifiManager.calculateSignalLevel(wifiInfo.rssi, numberOfLevels)
            if (level == 2) "POOR" else if (level == 3) "MODERATE" else if (level == 4) "GOOD" else if (level == 5) "EXCELLENT" else "UNKNOWN"
        } else if (info.type == ConnectivityManager.TYPE_MOBILE) {
           // val networkClass = getNetworkClass(getNetworkType(context))
            val networkClass: Int =   (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkType
            if (networkClass == 1) "POOR" else if (networkClass == 2) "GOOD" else if (networkClass == 3) "EXCELLENT" else "UNKNOWN"
        } else "UNKNOWN"
    }

    fun getInfo(context: Context): NetworkInfo? {
        return (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
    }

    fun getNetworkClass(networkType: Int): Int {
        try {
            return getNetworkClassReflect(networkType)
        } catch (ignored: Exception) {
        }
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, 16,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN -> 1

            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_HSPAP, 17 -> 2
            TelephonyManager.NETWORK_TYPE_LTE, 18 -> 3
            else -> 0
        }
    }

    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
    private fun getNetworkClassReflect(networkType: Int): Int {
        val getNetworkClass = TelephonyManager::class.java.getDeclaredMethod("getNetworkClass", Int::class.javaPrimitiveType)
        if (!getNetworkClass.isAccessible) {
            getNetworkClass.isAccessible = true
        }
        return getNetworkClass.invoke(null, networkType) as Int
    }
}


private class NetworkEvent(
    private val pendingResult: BroadcastReceiver.PendingResult,
    private val intent: Intent,
    private val  context: Context
) : AsyncTask<String, Int, String>() {
    val TAG = "NetworkEvent"
    override fun doInBackground(vararg params: String?): String {
        //Log.msg(TAG,"[doInBackground]")
        var msg = ""
        try {
            processEvent(intent,context)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "doInBackground", ex.message);
        }
        return msg
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // Must call finish() so the BroadcastReceiver can be recycled.
        pendingResult.finish()
    }

    private fun processEvent(intent: Intent,context: Context){
        try {
            val netInfo = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO) ?: return
            connectedProcess(context,netInfo)
            when (netInfo.state) {
                NetworkInfo.State.CONNECTED -> {
                    Log.msg(TAG, "[processEvent] getState: CONNECTED")
                    val handlerService = Handler(Looper.getMainLooper())


                    //Notifica para que se retire la ventana de Kiosko.
                    handlerService.postDelayed(
                        {
                            //Para asegurar que realmente se conecto, en ocasiones el usuario conecta y desconecta en el mismo moemnto.
                            if(Red.isOnline){
                                Log.d(TAG, "[processEvent] sendNetworkStatus")
                                //
                                sendNetworkStatus(true,context.applicationContext)
                            }
                            else
                                Log.msg(TAG,"Se volvio a DESCONECTAR...")
                        }, 2_000
                    )

                    //Envia las transacciones pendientes...
                    handlerService.postDelayed(
                        {
                            //Para asegurar que realmente se conecto, en ocasiones el usuario conecta y desconecta en el mismo moemnto.
                            if(Red.isOnline){
                                Log.d(TAG, "[processEvent] enviaTxtPendientes")
                                //Verifica si hay transacciones pendientes de enviar...
                                enviaTxtPendientes()
                            }
                            else
                                Log.msg(TAG,"Se volvio a DESCONECTAR...")
                        }, 15_000
                    )
                }
                NetworkInfo.State.DISCONNECTED -> {
                    Log.d(TAG, "[processEvent] getState: DISCONNECTED")
                    // sendLockStatus("StatusRed,Disconnected,")
                    sendNetworkStatus(false,context.applicationContext)
                }
                else -> {}
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "processEvent", ex.message)
        }

    }


    private fun connectedProcess(context: Context, netInfo: NetworkInfo) {
        var typeTransport = ""
        try {
            Log.msg(TAG, "[connectedProcess] -1-")
            //Tipo Conexion
            typeTransport = if (ConnectivityManager.TYPE_WIFI == netInfo.type) "Wifi" else "Celular"
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wifiManager.connectionInfo
            var strTransport = ""
            strTransport = if (typeTransport == "Wifi") {
                val ssidName = info.ssid
                if (!ssidName.contains("unknown")) "Wifi: $ssidName" else "Conexion Wifi"
            } else
                "Datos: "+DeviceCfg.getDisplayText(context,0)


            //Envia mensaje de status de red, para actualizar el Activity.
            //sendLockStatus("StatusRed,Connected,$strTransport")
            Log.msg(TAG, "[connectedProcess] transporte: $typeTransport: $strTransport")

            //Desbloquea...
            sendNetworkStatus(true, context)

            //IFA :15FEB
            //Conecta el MQTT,debedo a que ya no es automatico..
          //  Log.msg(TAG, "[connectedProcess] ConnectMQTT.isConnected(): " + mqttAWS!!.isConnected)

        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "connectedProcess", e.message)
        }
    }
    private fun transport(context:Context, netInfo: NetworkInfo):String{
        //Tipo Conexion
        var typeTransport = ""
        var strTransport = "Wifi"
        try{
            typeTransport = if (ConnectivityManager.TYPE_WIFI == netInfo.type) "Wifi" else "Celular"
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wifiManager.connectionInfo
            var strTransport = ""
            strTransport = if (typeTransport == "Wifi") {
                val ssidName = info.ssid
                if (!ssidName.contains("unknown")) "Wifi: $ssidName" else "Conexion Wifi"
            } else
                "Datos: "+DeviceCfg.getDisplayText(context,0)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"transport",ex.message)
        }

        return strTransport
    }
}