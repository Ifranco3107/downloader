package com.macropay.downloader.utils.logs

import android.content.Context
import android.os.Build
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.logs.Tracker
import com.macropay.downloader.data.preferences.Status
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.samsung.KnoxConfig
import com.macropay.downloader.utils.xiaomi.MIUI
import com.macropay.utils.network.Red
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko
import java.util.*

object LogInfoDevice {
    private val TAG = "MainApp"
    fun deviceInfo(context:Context) {
        try {

            var version = 0L
            var versionName = ""
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = packageInfo.longVersionCode
            versionName = packageInfo.versionName
            val numTel = Settings.getSetting(Cons.KEY_CURRENT_PHONE_NUMBER, "")
            Log.msg(TAG, "==============================================================================")
            Log.msg(TAG, "Equipo: " + Build.MANUFACTURER + " - " + Build.MODEL + " - [" + Build.PRODUCT + "]")
            Log.msg(TAG, "imei: " + DeviceCfg.getImei(context))
            Log.msg(TAG, "Android " + Build.VERSION.RELEASE + "  [ " + Build.VERSION.SDK_INT + " ]")
            Log.msg(TAG, "Versión:  $versionName ($version)")
            if (!numTel!!.isEmpty()) Log.msg(TAG, "numTel: [$numTel]")
            infoKnox()
            infoXiaomi()
            Log.msg(TAG, "==============================================================================")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "logInfoDevice", ex.message)
        }
    }

    fun statusApp(context:Context){
        try{
            Log.msg(TAG,"-----------------------------------------------------------------");
            Log.msg(TAG,"restartInstall: " + Settings.getSetting("restartInstall", false))
            Log.msg(TAG,"currentStatus:[" + Status.currentStatus + "]" )
            Log.msg(TAG,"KEY_FIRST_REBOOT: "+ Settings.getSetting(Cons.KEY_FIRST_REBOOT,false))
            Log.msg(TAG,"EsKiosko: ${SettingsApp.isKiosko()}   Kiosko.enabled: ${Kiosko.enabled} ${Kiosko.currentKiosko}")
            Log.msg(TAG,"")
            Log.msg(TAG,"dpcValues.isProvisioning: "+ dpcValues.isProvisioning)
            Log.msg(TAG,"")
            Log.msg(TAG,"")
            Log.msg(TAG,"-----------------------------------------------------------------");
            val lastLat=Settings.getSetting(Cons.KEY_LAST_LAT_SENT, "0")
            val lastLen=Settings.getSetting(Cons.KEY_LAST_LON_SENT, "0")
            var redInfo = "Type: ${Red.isWiFi} ${Red.getSSID(context)}"
            Tracker.status(TAG,"reinicio","currentStatus: ${Status.currentStatus} EsKiosko:${SettingsApp.isKiosko()} -Kiosko.enabled: ${Kiosko.enabled} currentKioko: ${Kiosko.currentKiosko} $lastLat,$lastLen $redInfo")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "statusApp", ex.message)
        }
    }
    private fun infoKnox() {
        Log.msg(TAG, "marca: " + Build.MANUFACTURER.uppercase(Locale.getDefault()) )
        if (!Build.MANUFACTURER.uppercase(Locale.getDefault()).contains("SAMSUNG")) {
            Log.msg(TAG, "No es telefono Samsung ...")
            return
        }
        try {
            //  KnoxConfig knoxConfig = new KnoxConfig(getApplicationContext());
            Log.msg(TAG, "knox: API: " + KnoxConfig.aPILevel + " [" + KnoxConfig.knoxVersion + "]")
            Tracker.status(TAG,"infoKnox","knox: API: " + KnoxConfig.aPILevel + " [" + KnoxConfig.knoxVersion + "]")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "", ex.message)
        }
    }

    private fun infoXiaomi() {
        if (!Build.MANUFACTURER.uppercase(Locale.getDefault()).contains("XIAOMI")) return
        Log.msg(TAG, "infoXiaomi: " + MIUI.version + " [" + MIUI.versionName + "] ")
    }
}