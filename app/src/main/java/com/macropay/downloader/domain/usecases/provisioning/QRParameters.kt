package com.macropay.downloader.domain.usecases.provisioning

import android.content.Context
import android.os.PersistableBundle
import com.macropay.data.preferences.Defaults
import com.macropay.data.logs.ErrorMgr
import com.macropay.downloader.utils.SettingsApp

import com.macropay.downloader.data.preferences.*
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons
import java.lang.Exception
import java.time.temporal.ChronoUnit


public class QRParameters {
    var TAG = "QRParameters"
    public fun leeQRSettings(bundle: PersistableBundle?, context: Context) {
        Log.msg(TAG, "[leeQRSettings] ======================================================")

        //Define defaults
        var httpServer = Defaults.SERVIDOR_HTTP;
        var mqttServer = Defaults.SERVIDOR_MQTT
        var pkgServer = Defaults.SERVIDOR_HTTP_PKG
        var rptServer = Defaults.SERVIDOR_HTTP_RPT
        var appkeymobile = Defaults.API_KEY
        var applicative = "";
        var subsidiary = "";
        var employee = "";
        var packname = Defaults.DPC_PACKAGENAME
        var location = Defaults.DPC_LOCATION

        var  isLegacy = ""
        try {
            //  Log.msg(TAG, "[leeQRSettings] - bundle: \n"+bundle.toString());
            if (bundle != null ) {
                if (bundle.containsKey("server")) httpServer = bundle.getString("server", Defaults.SERVIDOR_HTTP)

                if (bundle.containsKey("applicative")) applicative = bundle.getString("applicative", "")
                if (bundle.containsKey("subsidiary")) subsidiary = bundle.getString("subsidiary", "")
                if (bundle.containsKey("employee")) employee = bundle.getString("employee", "")

                if (bundle.containsKey("dpc_package")) packname = bundle.getString("dpc_package", Defaults.DPC_PACKAGENAME)
                if (bundle.containsKey("dpc_location")) location = bundle.getString("dpc_location", Defaults.DPC_LOCATION)

                if (bundle.containsKey("checksum")) appkeymobile = bundle.getString("checksum", Defaults.API_KEY)
                if (bundle.containsKey("port2"))  isLegacy = bundle.getString("port2", "")
                //campos modulares
                if (bundle.containsKey("server_pkg"))  pkgServer = bundle.getString("server_pkg", Defaults.SERVIDOR_HTTP_PKG)
                if (bundle.containsKey("server_rpt"))  rptServer = bundle.getString("server_rpt", Defaults.SERVIDOR_HTTP_RPT)
                //Temporal, solo para poder seguir probando con el QR de la version vieja.
                //Solo para pruebas de desarrollo.
                if(!isLegacy.isEmpty())
                {
                    Log.msg(TAG,"isLegacy: ["+isLegacy +"]")
                    httpServer = Defaults.SERVIDOR_HTTP
                }

                Log.msg(TAG,"<-------------------------------> ")
                Log.msg(TAG,"<----  QRSettings [Guardar] ----> ")
                Log.msg(TAG,"server: "+ httpServer)
                Log.msg(TAG,"appkeymobile: "+ appkeymobile)

                //Guarda los parametros de QR
                SettingsApp.init(context)
                Settings.setSetting(Cons.KEY_HTTP_SERVER,httpServer)
                Settings.setSetting(Cons.KEY_HTTP_SERVER_PKG,pkgServer)
                Settings.setSetting(Cons.KEY_HTTP_SERVER_RPT,rptServer)
                Settings.setSetting(Cons.KEY_APIKEYMOBILE,appkeymobile)

                Settings.setSetting(Cons.KEY_APPLICATIVE,applicative)
                Settings.setSetting(Cons.KEY_SUBSIDIARY,subsidiary)
                Settings.setSetting(Cons.KEY_EMPLOYEE,employee)

                Settings.setSetting(Cons.KEY_PACKAGENAME_DPC,packname)
                Settings.setSetting(Cons.KEY_LOCATION_DPC,location)
                // Settings.getSetting(Cons.,"com.macropay.dpcmacro")
// Settings.getSetting(Cons.,"https://amacrolockbucketdev.lockmacropay.mx/api/devices/mobile/apk/download?appkeymobile=CMaz3r2r23r23r23r23ssd11SWSZXWEFWWFd99s")


                //Parametros default..
                Settings.setSetting(TipoParametro.medidaTiempo, ChronoUnit.HOURS.name)
                Settings.setSetting(TipoParametro.frecNotificaStatus, 1) //Cada Hora
                Settings.setSetting(TipoParametro.limiteSinConexion, 120) // 120 Horas = 5 Dias


                //Guarda los datos del servidor...
                Log.msg(TAG, "[leeQRSettings] httpServer: $httpServer")
                Log.msg(TAG, "[leeQRSettings] mqttServer: $mqttServer")
                Log.msg(TAG, "[leeQRSettings] applicative: $applicative")
                Log.msg(TAG, "[leeQRSettings] subsidiary: $subsidiary")
                Log.msg(TAG, "[leeQRSettings] employee: $employee")

            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "leeQRSettings", ex.message)
          //  if (SettingsApp.initialized()) SettingsApp.init(context)
        }

    }

/*
    private fun addAppsEmpresa(apps: JSONArray) {
        var apps = apps
        try {
            if (apps == null) {
                Log.msg(TAG, "apps == null")
                apps = appsDefault() //new JSONArray(appsDefault());
                //        Log.msg(TAG,"-1-");
            }
            if (apps.length() == 0) {
                Log.msg(TAG, "apps.length() == 0")
                apps = appsDefault() // new JSONArray(appsDefault());
            }
            //
            val enterpriseApps : Array<String?> = arrayOfNulls<String>(apps.length())
            Log.msg(TAG, "[addLockApps] apps; $apps")
            for (i in enterpriseApps.indices) {
                enterpriseApps[i] = apps.optString(i)
                enterpriseApps[i] = enterpriseApps[i]?.replace("\\", "")
                Log.msg(TAG, "[addLockApps] enterpriseApps[i]; " + enterpriseApps[i])
            }
            //Log.msg(TAG,"enterpriseApps: " +enterpriseApps.toString());
            Settings.setSetting("enterpriseApps", enterpriseApps)
            // Instala las apps...
            val macroPolicies = MacroPolicies(MainApp.getMainCtx(), null)
            macroPolicies.instalarApp()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "addAppsEmpresa", ex.message)
        }
    }*/

  /*  private fun appsDefault(): JSONArray {
        Log.msg(TAG, "---> appsDefault <---")
        val apps = JSONArray()
        val postData = JSONObject()
        val packageName = "com.grupomacro.macropay"
        //String location = "/lock/uploads/grupomacroapp.apk";
        //https://lockmacropay.mx/lock/api/download/MacroLock-release.apk
        //https://lockmacropay.mx/lock/api/download/grupomacroapp.apk
        //
        val server = SettingsApp.getServerHttp()
        Log.msg(TAG, "server; [$server]")
        val location = "/lock/api/download/grupomacroapp.apk"
        try {
            postData.put("packageName", packageName)
            postData.put("location", location)
            apps.put(postData)
        } catch (ex: JSONException) {
            ErrorMgr.guardar(TAG, "appsDefault", ex.message)
        }
        Log.msg(TAG, "[appsDefault] apps: $apps")
        return apps
    }*/
}