package com.macropay.downloader.utils.policies

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.*
import android.os.Build
import android.os.UserManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.data.preferences.MainApp
import com.macropay.downloader.utils.Utils
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


//: Activity()
class Restrictions
@Inject constructor(
    @ApplicationContext val context:Context)
     {
    private lateinit var mDevicePolicyManager: DevicePolicyManager
    private lateinit var mUserManager: UserManager
    private lateinit var mAdminComponentName: ComponentName

    var TAG = "Restrictions"
    var mContext: Context? = null

    init {
        var ln = 1
        try {
         //   Log.msg(TAG,"[constructor] -1-");
            ln = 2
         //   attachBaseContext(context)
            mContext = context
            mDevicePolicyManager = context.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            ln = 3
            mUserManager = context.getSystemService(USER_SERVICE) as UserManager
            ln = 4
            mAdminComponentName = DeviceAdminReceiver.getComponentName(context) as ComponentName
            ln = 5
           // Log.msg(TAG,"[constructor] -2-");
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "constructor[$ln]", ex.message)
        }
    }

    fun setRestriction(restriction: String, newValue: Boolean): Boolean {
        return try {

            if (newValue) {
                //Log.msg(TAG, "[setRestriction] restriction: [$restriction] true")
                mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction)
            } else {
                //Log.msg(TAG, "[setRestriction] restriction: [$restriction] false")
                mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction)
            }
          //  Log.msg(TAG, "[setRestriction] restriction: [$restriction] - ok")
            true
        } catch (e: Exception) {
            //Log.msg(TAG, "[setRestriction] restriction: [$restriction] - ERROR ***")
            ErrorMgr.guardar(TAG, "setRestriction [$restriction]", e.message)
            false
        }
    }

    fun setUsbMassStorage(bEnabled: Boolean) {
        val value = if (bEnabled) "1" else "0"
        mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.USB_MASS_STORAGE_ENABLED, value)
    }

    fun liberaRestricciones() {
        val userRestrictions = mDevicePolicyManager.getUserRestrictions(mAdminComponentName)
    }

    fun Reboot() {
        try {
            mDevicePolicyManager.reboot(mAdminComponentName)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "Reboot", ex.message)
        }
    }

    fun disableScrenCapture(disabled: Boolean) {
        // Log.msg("Restrictions","disableScrenCapture: " +disabled);
        mDevicePolicyManager!!.setScreenCaptureDisabled(mAdminComponentName!!, disabled)
    }

    @RequiresApi(api = Build.VERSION_CODES.R) //Android 11 - SDK 30
    fun disableLocation(disabled: Boolean) {
        mDevicePolicyManager!!.setLocationEnabled(mAdminComponentName!!, disabled)
    }

    //  @RequiresApi(api = Build.VERSION_CODES.R)
    fun disableAutoTime(disabled: Boolean) {
        if (Utils.SDK_INT >= Build.VERSION_CODES.R) {
            mDevicePolicyManager!!.setAutoTimeEnabled(mAdminComponentName!!, disabled)
        } else {
            mDevicePolicyManager!!.setAutoTimeRequired(mAdminComponentName!!, disabled)
        }
    }


    fun disableCall(bActiva: Boolean) {
        var ln = 0
        try {
            ln = 5
            if (bActiva) {
                ln = 6
                mDevicePolicyManager!!.addUserRestriction(mAdminComponentName!!, UserManager.DISALLOW_OUTGOING_CALLS)
            } else {
                ln = 7
                mDevicePolicyManager!!.clearUserRestriction(mAdminComponentName!!, UserManager.DISALLOW_OUTGOING_CALLS)
            }
            ln = 8
        } catch (ex: Exception) {
            Log.msg("[disableCall]", "ERROR[" + ln + "]" + ex.message)
        }
    }

    fun enableAccessibility(enable: Boolean) {
        var ln = 1
        mDevicePolicyManager = mContext!!.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mAdminComponentName  = DeviceAdminReceiver.getComponentName(mContext)
        try {
            var curPermitted = mDevicePolicyManager!!.getPermittedAccessibilityServices(mAdminComponentName!!)
            if (curPermitted == null) curPermitted = ArrayList()
            ln = 2
            val packageName = mContext!!.packageName
            //    Log.msg(TAG,"packageName: "+packageName);
            if (enable) {
                if (!isAccessibilityEnabled) curPermitted.add(packageName)
            } else curPermitted.remove(packageName)
            ln = 3
            Log.msg(TAG, "apps con permisos de Accessibility:")
            for (componenet in curPermitted) {
                Log.msg(TAG, componenet)
            }
            val result = mDevicePolicyManager!!.setPermittedAccessibilityServices(mAdminComponentName!!, curPermitted)
            if (result) {
                Log.msg(TAG, "OK")
                curPermitted = mDevicePolicyManager!!.getPermittedAccessibilityServices(mAdminComponentName!!)
                if (curPermitted == null) curPermitted = ArrayList()
                for (componenet in curPermitted) {
                    Log.msg(TAG, "+ $componenet")
                }
            } else Log.msg(TAG, "No pudo asingar permisos")
            ln = 4
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "enabledAccesiblity($enable) - $ln", ex.message)
        }
    }

    val isAccessibilityEnabled: Boolean
        get() {
            var ln = 1
            var isEnabled = false
            try {
                val curPermitted = mDevicePolicyManager!!.getPermittedAccessibilityServices(mAdminComponentName!!)
                ln = 2
                if (curPermitted != null) {
                    val packageName = mContext!!.packageName
                    ln = 3
                    isEnabled = curPermitted.contains(packageName)
                    ln = 4
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "isAccessibilityEnabled-$ln", ex.message)
            }
            return isEnabled
        }

    fun isStatusRestriction(restriction: String): Boolean {
        var bStatus = false
        try {
            val restrictions = mDevicePolicyManager!!.getUserRestrictions(mAdminComponentName!!)
            bStatus = restrictions.getBoolean(restriction)
            //   Log.msg(TAG,"bStatus: "+bStatus);
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "getStatusRestriction ($restriction)", ex.message)
        }
        return bStatus
    }

    val isAdmin: Boolean
        get() {
            var adminActive = false
            try {
                adminActive = mDevicePolicyManager!!.isAdminActive(mAdminComponentName!!)
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "isAdmin", ex.message)
            }
            return adminActive
        }

    fun setAdmin() {
        Log.msg(TAG, "*** setAdmin ***")
        try {
            //  UserHandle userHandle = mDevicePolicyManager.createAndManageUser(DevicePolicyManager.MAKE_USER_EPHEMERAL);
            val flags = DevicePolicyManager.SKIP_SETUP_WIZARD or DevicePolicyManager.MAKE_USER_EPHEMERAL or DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED
            val userHandle = mDevicePolicyManager!!.createAndManageUser(mAdminComponentName!!, "downloader", mAdminComponentName!!, null, flags)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "setAdmin", ex.message)
        }
        // mDevicePolicyManager.setct(mAdminComponentName, true /*refreshing*/, mUserId);
    }

    fun lockApps(){
        var ln = 0
        try{
         ln= 1
         val APP_PACKAGES = MainApp.getAppPermitted(context)
         ln= 2
         mDevicePolicyManager!!.setLockTaskPackages(mAdminComponentName!!, APP_PACKAGES)
         ln= 3
         Log.msg(TAG,"[lockApps] ok---")
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "lockApps [$ln]", e.message)
        }
    }


         fun lockFeatures(){
             var ln = 0


             try {
                 var flagsBefore:Int = mDevicePolicyManager!!.getLockTaskFeatures(mAdminComponentName!!)
                 Log.msg(TAG,"[lockFeatures] 1.- flagsBefore: "+flagsBefore)
                 ln= 4
                 flagsBefore = flagsBefore and DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS.inv()
                 // flagsBefore = DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS
                 Log.msg(TAG,"[lockFeatures] 2.- flagsBefore: "+flagsBefore)
                 if(flagsBefore== null){
                     Log.msg(TAG,"[lockFeatures]  flagsBefore = null ")
                 }

                 if(mAdminComponentName== null){
                     Log.msg(TAG,"[lockFeatures]  mAdminComponentName = null ")
                 }
                 if(mDevicePolicyManager== null){
                     Log.msg(TAG,"[lockFeatures]  mDevicePolicyManager = null ")
                 }
                 ln= 5
                 mAdminComponentName = ComponentName(context, DeviceAdminReceiver::class.java)
                 ln =51
                 //     mDevicePolicyManager!!.lockNow()
                 ln =52
                 Log.msg(TAG,"[lockFeatures] className: " +mAdminComponentName!!.className)
                 ln =53
                 apply{
                     mDevicePolicyManager!!.setLockTaskFeatures(mAdminComponentName!!, flagsBefore)
                 }

                 ln= 6
                // startLockTask()
             } catch (e: Exception) {
                 ErrorMgr.guardar(TAG, "lockFeatures [$ln]", e.message)
             }
         }
}