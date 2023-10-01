/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.macropay.downloader.utils

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.ErrorMgr.guardar
import com.macropay.data.logs.Log
import com.macropay.data.logs.Log.msg
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.ui.provisioning.EnrollActivity
import com.macropay.downloader.utils.policies.Restrictions
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

class Utils(mContext: Context) {
    var mContext :Context
    init {
        this.mContext = mContext
    }

    companion object {
        const val TAG = "Utils"

        private const val DEFAULT_BUFFER_SIZE = 4096

        // TODO: Update to S when VERSION_CODES.R becomes available.
        const val R_VERSION_CODE = 30
        private val IS_RUNNING_R = Build.VERSION.CODENAME.length == 1 && Build.VERSION.CODENAME[0] == 'R'
        const val Q_VERSION_CODE = 29
        @JvmField
        val SDK_INT = if (IS_RUNNING_R) Build.VERSION_CODES.CUR_DEVELOPMENT else Build.VERSION.SDK_INT
        const val TEXT_GPS_ACCEPTED = "permisos_gps_accepted"
        private const val permisosGranted = false
        var lastPackage = ""
        fun isDeviceOwner(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return dpm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && dpm.isDeviceOwnerApp(context.packageName)
        }
        fun isAdmin(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            var mAdminComponent= ComponentName(context, DeviceAdminReceiver::class.java)
            return   dpm.isAdminActive(mAdminComponent)
        //    return dpm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && dpm.isDeviceOwnerApp(context.packageName)
        }
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, DeviceAdminReceiver::class.java)
        }

        @JvmStatic
        fun tiempoTranscurrido(dteFechaAnterior: LocalDateTime?, escala: ChronoUnit?): Long {
            var tiempo = 0L
            try {
                val dteHoy = LocalDateTime.now()
                val tempDateTime = LocalDateTime.from(dteFechaAnterior)
                tiempo = tempDateTime.until(dteHoy, escala)
            } catch (ex: Exception) {
                guardar(TAG, "tiempoTranscurrido: ", ex.message)
            }
            return tiempo
        }

        //Se paso a policies/KioskScreen
        val isLockedShowed: Boolean
            get() {
                var bIsVisible = false
                try {
/*                    if (LockedActivity.fa != null && LockedActivity.fa!!.isDestroyed) {
                        msg(TAG, "LockedActivity isDestroyed")
                        LockedActivity.fa = null
                    }*/
                } catch (ex: Exception) {
                    guardar(TAG, "isLockedShowed", ex.message)
                }
            //    bIsVisible = LockedActivity.fa != null
                return bIsVisible
            }

        //Se paso a policies/KioskScreen
        fun isLockTaskEnabled(context: Context?): Boolean {
            var context = context
            if (context == null) {
                guardar(TAG, "constructor", "context = null")
                context = dpcValues.mContext
            }
            var bResult = false
            try {
                val activityManager: ActivityManager
                activityManager = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                bResult = activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
                //  Log.msg(TAG,"[isLockTaskEnabled] bResult: "+bResult);
            } catch (ex: Exception) {
                guardar(TAG, "isLockTaskEnabled", ex.message)
            }
            return bResult
        }

        //Se paso a Dialogs
        fun isRunning(context: Context, activityClass: Class<*>): Boolean {
            try {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val tasks = activityManager.getRunningTasks(Int.MAX_VALUE)
                for (task in tasks) {
                    if (activityClass.canonicalName.equals(task.baseActivity!!.className, ignoreCase = true)) return true
                }
            } catch (ex: Exception) {
                guardar(TAG, "isRunning", ex.message)
            }
            return false
        }

        //Se paso a Dialogs
        fun showAppIcon(context: Context, bShow: Boolean) {
            msg(TAG, "showAppIcon: [$bShow]-")
            try {
                val p = context.packageManager
                //ComponentName adminComponentName = DeviceAdminReceiver.getComponentName(context);
                val componentName = ComponentName(context, EnrollActivity::class.java)
                if (bShow) p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP) else p.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                guardar(TAG, "showAppIcon", e.message)
            }
        }

        //Se paso a Dialogs
        fun activarActivity(context: Context, activityClass: Class<*>) {
            val bDelay = Build.MODEL.uppercase(Locale.getDefault()).contains("REDMI") || Build.MODEL.uppercase(Locale.getDefault()).contains("M2102J20SG")
            msg(TAG, "activarActivity - bDelay: $bDelay")
            try {
                val handler = Handler(Looper.getMainLooper())
                if (bDelay) handler.postDelayed({ showActivity(context, activityClass) }, 1000) else handler.post { showActivity(context, activityClass) }
            } catch (ex: Exception) {
                guardar(TAG, "activarActivity", ex.message)
            }
        }

        //Se paso a Dialogs
        fun showActivity(context: Context, activityClass: Class<*>) {
            try {
                val intentMain = Intent(context, activityClass)
                msg(TAG, "showActivity - activityClass: [" + activityClass.name + "]")
                intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intentMain)
            } catch (ex: Exception) {
                guardar(TAG, "showActivity", ex.message)
            }
        }

        @JvmStatic
        fun getAppValue(context: Context?): String {
            if (context == null) msg(TAG, "context == null")
            var sValue = ""
            try {
                val ai = context!!.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                val value = ai.metaData["supportAndroid12"] //support12
                if (value != null) sValue = value.toString()
            } catch (ex: Exception) {
                //  ErrorMgr.guardar(TAG,"getAppValue",ex.getMessage());
            }
            return sValue
        }

        fun reboot(context: Context?) {
            try {
                msg(TAG, "reboot---xxx")
                msg(TAG, "\n\n\n\n\n\n")
                val restrinctions = Restrictions(context!!)
                restrinctions.Reboot()
                /*           Handler handler = new Handler(Looper.getMainLooper());
            Looper.prepare();
            handler.postDelayed(() ->

                    restrinctions.Reboot()
                    ,2000 ) ;
            Looper.loop();
*/

/*            Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(new Runnable() {
                public void run() {
                    restrinctions.Reboot();
                }
            },2000);*/
            } catch (ex: Exception) {
                guardar(TAG, "reboot.1", ex.message)
            }
        }
        fun timeRebooted():Long{
            var ms = 1_000_000L //Inicializa, valor default
            try{
                ms = SystemClock.elapsedRealtime()
                Log.msg(TAG,"[isRebooted] ms: $ms")
            }catch (ex:Exception){
                ErrorMgr.guardar(TAG,"isRebooted",ex.message)
            }

            return  (ms )
        }
    }
    fun isRebooted():Boolean{
        val ms_limite = 80_000L
        var ms = ms_limite+1_000 //Se inicializa mayor al limite, para que si hay error, devuelva false
        try{
            ms = SystemClock.elapsedRealtime()
            Log.msg(TAG,"[isRebooted] ms: $ms")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"isRebooted",ex.message)
        }

        return  (ms<=ms_limite)
    }
}