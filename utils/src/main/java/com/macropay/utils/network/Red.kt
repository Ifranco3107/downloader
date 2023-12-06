package com.macropay.utils.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.logs.Log
import com.macropay.utils.phone.DeviceCfg
import java.lang.reflect.InvocationTargetException

@SuppressLint("StaticFieldLeak")
object Red {
    var wifiInfo: NetworkInfo? = null
    var mobileInfo: NetworkInfo? = null
    var connected = false

   // companion object {
       // private val instance = ConnectionStatus()
        var ctxStatus: Context? = null
           get() = field
           set(value) {field= value}

        var connectivityManager: ConnectivityManager? = null


        val isInitialized: Boolean
            get() = ctxStatus != null

        var TAG = "Red"

        val isOnline: Boolean
            get() {
                if (ctxStatus == null) {
                    Log.msg(TAG, "isOnline - ctx== null")
                    return false
                }
                var connected = false
                try {
                    connectivityManager = ctxStatus!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    if (connectivityManager == null) {
                        Log.msg(TAG, "-1- connectivityManager == null")
                        return false
                    }
                    val curNetwork = connectivityManager!!.activeNetwork
                    if (curNetwork != null) {
                        val capabilities = connectivityManager!!.getNetworkCapabilities(curNetwork)
                        if (capabilities != null) {
                            // Log.msg(TAG,"-3- NET_CAPABILITY_INTERNET: "+capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
                            // Log.msg(TAG,"-3- NET_CAPABILITY_VALIDATED: "+capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
                            // If we check only for "NET_CAPABILITY_INTERNET", we get "true" if we are connected to a wifi
                            // which has no access to the internet. "NET_CAPABILITY_VALIDATED" also verifies that we
                            // are online
                            connected = (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))

                            //   Log.msg(TAG,"isOnline.capabilities: "+connected);
                        }
                    }

                } catch (e: Exception) {
                   ErrorMgr.guardar(TAG, "isOnline", e.message)
                }
                return connected
            }

        val isWiFi: Boolean
            get() {
                if (ctxStatus == null) {
                     Log.msg(TAG, "isWiFi -ctxStatus == null")
                    return false
                }
                var isMetered = false
                try {
                    val cm = ctxStatus!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    isMetered = cm.isActiveNetworkMetered
                } catch (e: Exception) {
                    ErrorMgr.guardar(TAG, "isWiFi", e.message)
                }
                return !isMetered
            }


        fun enableWifi() {
            if (ctxStatus == null) {
                Log.msg(TAG, "enableWifi - ctxStatus == null")
                return
            }
            try {
                val wifi = ctxStatus!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (!wifi.isWifiEnabled) {
                    Log.msg(TAG, "SE PRENDIO EL WIFI...")
                    wifi.isWifiEnabled = true // true or false to activate/deactivate wifi
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "enableWifi", ex.message)
            }
        }
    fun enableWifi(enable :Boolean) {
        if (ctxStatus == null) {
           Log.msg(TAG, "enableWifi - ctxStatus == null")
            return
        }
        try {
            val wifi = ctxStatus!!.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (!wifi.isWifiEnabled && enable ) {
                Log.msg(TAG, "SE PRENDIO EL WIFI...")
            }
            if (wifi.isWifiEnabled && !enable ) {
                Log.msg(TAG, "apago PRENDIO EL WIFI...")
            }
            wifi.isWifiEnabled = enable // true or false to activate/deactivate wifi
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "enableWifi", ex.message)
        }
    }

    @SuppressLint("MissingPermission")
    fun ConnectionQuality(context: Context): String? {
        val info: NetworkInfo? = getInfo(context)
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
            val networkClass: Int =   (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkType
          //  val networkClass: Int = getNetworkClass(NetworkReceiver.getNetworkType(context))
            if (networkClass == 1) "POOR" else if (networkClass == 2) "GOOD" else if (networkClass == 3) "EXCELLENT" else "UNKNOWN"
        } else "UNKNOWN"
    }

    fun getInfo(context: Context): NetworkInfo? {
        return (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
    }

    fun getNetworkClass(networkType: Int): Int {
        try {
            return getNetworkClassReflect(networkType)
        } catch (ignored: java.lang.Exception) {

        }
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, 16, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> 1
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, 17 -> 2
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
    fun getSSID(context: Context):String{
        var typeTransport = ""
        var strTransport = ""
        var ln = 0
        try {
         //   Log.msg(TAG, "[getSSID] isOnline: $isOnline isWifi: $isWiFi" )
            if(!isOnline)
                return "Sin red"
ln =1
            //Tipo Conexion
            typeTransport = if (ConnectivityManager.TYPE_WIFI == getInfo(context)!!.getType()) "Wifi" else "Celular"
           // Log.msg(TAG, "[getSSID] typeTransport: $typeTransport" )
     ln=2
            var wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            var info = wifiManager.connectionInfo
ln = 3
       //    Log.msg(TAG, "[getSSID] typeTransport: " + typeTransport)
            if ((typeTransport.equals( "Wifi"))) {
      ln=4
                val ssidName = info.ssid
                strTransport = if (!ssidName.contains("unknown")) ssidName else "Conexión Wifi"
                //strTransport =ssidName
          ln=5
            } else
                strTransport = DeviceCfg.getCarrierName(context, 0)!!

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "getSSID[$ln]", ex.message)
        }
        return  strTransport
    }

    /*public static boolean hasNetwork() {
        boolean connected = false;
        try {

            connectivityManager = (ConnectivityManager) ctxStatus
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            //  connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            connected= networkInfo != null && networkInfo.isConnectedOrConnecting();

            System.out.println("[hasNetwork] connected : " + connected);


        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }*/
        /*
    public static boolean isInternetConnected(Context getApplicationContext) {
        boolean status = false;

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (cm.getActiveNetwork() != null && cm.getNetworkCapabilities(cm.getActiveNetwork()) != null) {
                    // connected to the internet
                    status = true;
                }

            } else {
                if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                    // connected to the internet
                    status = true;
                }
            }
        }

        return status;
    }*/
    }
