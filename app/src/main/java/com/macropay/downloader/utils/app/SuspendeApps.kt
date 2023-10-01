package com.macropay.downloader.utils.app

import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.ErrorMgr.guardar
import com.macropay.utils.FileMgr.loadFile
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.utils.Utils
import org.json.JSONArray
import org.json.JSONException
import java.lang.Exception
import java.util.ArrayList

class SuspendeApps(context: Context?) : Activity() {
    private var mPackages: MutableList<String> = ArrayList()
    private val mEnabled = false
    private val TAG = "SuspendeApps"

    init {
        mPackages = ArrayList()
        try {
            attachBaseContext(context)
        } catch (e: Exception) {
            msg("SuspendApps.Constructor", "Error:" + e.message)
        }
    }

    fun addPackage(packageName: String) {
        mPackages.add(packageName)
    }

    fun suspendeApps(mEnabled: Boolean?) {
        //  Log.msg("SuspendApps", "-- Inicia --- mEnabled; "+mEnabled);
        val mDevicePolicyManager: DevicePolicyManager
        val mAdminComponentName: ComponentName
        try {
            mDevicePolicyManager = this.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            mAdminComponentName = DeviceAdminReceiver.getComponentName(this)
            val arrPackages = mPackages.toTypedArray()
            mDevicePolicyManager.setPackagesSuspended(mAdminComponentName, arrPackages, mEnabled!!)
            //    Log.msg("setPackagesSuspended","Termimo Correctamente ");
        } catch (ex: Exception) {
            guardar(TAG, "suspendeApps", ex.message)
        }
    }

    //  @RequiresApi(api = Build.VERSION_CODES.Q)
    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun appsSuspendidas(): List<String> {
        val pm = this.packageManager
        val packages = pm.getInstalledPackages(0)
        val appSuspendidas: MutableList<String> = ArrayList()
        try {
            for (packageInfo in packages) {
                try {
                    if (pm.isPackageSuspended(packageInfo.packageName)) {
                        appSuspendidas.add(packageInfo.packageName)
                    }
                    msg("$TAG.Instalada: ", packageInfo.packageName)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
        } catch (ex: Exception) {
            guardar(TAG, "appsSuspendidas", ex.message)
        }
        return appSuspendidas
    }

    fun liberaSuspendidas(): Int {
        mPackages = ArrayList()
        try {
            if (Utils.SDK_INT >= Build.VERSION_CODES.Q) {
                msg(TAG, "getAppsSuspended_Q SDK: " + Utils.SDK_INT)
                appsSuspended_Q
            } else {
                msg(TAG, "getAppsSuspended_P  SDK: " + Utils.SDK_INT)
                appsSuspended_P
            }
            msg(TAG, "Libero " + mPackages.size + " apps suspendidas.")
            if (mPackages.size > 0) suspendeApps(false)
        } catch (ex: Exception) {
            guardar(TAG, "liberaSuspendidas", ex.message)
        }
        return mPackages.size
    }

    @get:RequiresApi(api = Build.VERSION_CODES.Q)
    val appsSuspended_Q: Int
        get() {
            val pm = this.packageManager
            val packages = pm.getInstalledPackages(0)
            mPackages = ArrayList()
            try {
                for (packageInfo in packages) {
                    try {
                        if (pm.isPackageSuspended(packageInfo.packageName)) {
                            mPackages.add(packageInfo.packageName)
                            msg(TAG, "Suspendida: " + packageInfo.packageName)
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        guardar(TAG, "getAppsSuspended_Q", e.message)
                    }
                }
            } catch (ex: Exception) {
                guardar(TAG, "getAppsSuspended_Q", ex.message)
            }
            return mPackages.size
        }
    private val appsSuspended_P: Int
        private get() {
            try {
                val apps = loadApps()
                for (i in 0 until apps!!.length()) {
                    mPackages.add(apps.getString(i))
                }
            } catch (ex: JSONException) {
                guardar(TAG, "getAppsSuspended_P", ex.message)
            }
            return mPackages.size
        }

    fun loadApps(): JSONArray? {
        var jsonApps = loadFile("apps.json", this)
        if (jsonApps == "") jsonApps = appsDefault()
        var jsonMessage: JSONArray? = null
        try {
            jsonMessage = JSONArray(jsonApps)
        } catch (e: JSONException) {
            guardar(TAG, "loadApps[1]", e.message)
        }
        return jsonMessage

        /* JSONArray apps = null;
        try {
            apps = jsonMessage.getJSONArray("apps");
        } catch (JSONException e) {
            ErrorMgr.guardar(TAG,"loadApps[2]",e.getMessage());
        }*/
    }

    private fun appsDefault(): String {
        return "[\"com.whatsapp\",\"com.facebook.katana\",\"com.facebook.lite\",\"com.facebook.orca\",\"com.facebook.mlite\",\"com.whatsapp.w4b\",\"org.telegram.messenger\",\"com.google.android.apps.maps\",\"com.waze\",\"com.ubercab\",\"com.didiglobal.passenger\",\"com.spotify.music\",\"com.google.android.youtube\",\"com.android.vending\",\"com.snapchat.android\",\"com.google.android.gm\",\"com.instagram.android\",\"mx.com.bancoazteca.bazdigitalmovil\",\"com.google.android.googlequicksearchbox\",\"com.bancomer.mbanking\",\"com.citibanamex.banamexmobile\",\"com.android.chrome\",\"org.mozilla.firefox\",\"us.zoom.videomeetings\",\"com.google.android.apps.meetings\",\"com.zhiliaoapp.musically\",\"com.payclip.clip\",\"mx.hsbc.hsbcmexico\",\"com.linkedin.android\",\"mx.com.miapp\",\"com.motorola.camera2\",\"com.didiglobal.driver\",\"com.google.android.apps.tachyon\",\"com.google.android.apps.messaging\",\"com.netflix.mediaclient\",\"com.ubercab.driver\",\"com.ubercab.eats\",\"com.google.android.apps.youtube.music\",\"com.google.android.apps.subscriptions.red\",\"com.microsoft.office.outlook\",\"com.android.camera2\",\"com.ume.browser.cust\"]\n"
    }
}