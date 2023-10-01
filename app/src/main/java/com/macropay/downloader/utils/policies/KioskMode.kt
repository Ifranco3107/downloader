package com.macropay.downloader.utils.policies

import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.ErrorMgr.guardar
import android.app.Activity
import android.content.ComponentName
import android.app.admin.DevicePolicyManager
import android.content.pm.PackageManager
import com.macropay.downloader.utils.SettingsApp
import android.content.IntentFilter
import android.os.BatteryManager
import android.app.admin.SystemUpdatePolicy
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.data.preferences.MainApp
import com.macropay.downloader.ui.provisioning.EnrollActivity
import java.lang.Exception
import java.util.ArrayList

//import com.macropay.dpcmacro.LockActivity;
/*
    NO SE USA... SE UTILIZA KioskScreen
*/
class KioskMode(context: Context?) : Activity() {
    private val mKioskPackages = ArrayList<String>()
    private val mAdminComponentName: ComponentName
    private val mDevicePolicyManager: DevicePolicyManager
    private val mPackageManager: PackageManager
    var TAG = "KioskMode"
    var context: Context? = null

    init {
        //Log.msg(TAG,"--- Constructor");
        attachBaseContext(context)
        this.context = context
        mAdminComponentName = DeviceAdminReceiver.getComponentName(context)
        //mAdminComponentName = DeviceAdminReceiver.getComponentName(this)
        //  mAdminComponentName = new ComponentName(context, DeviceAdminReceiver.class);
        //mAdminComponentName = DeviceAdminReceiver.getComponentName(this);
        mDevicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mPackageManager = packageManager
    }

    fun addPackage(packageName: String) {
        msg(TAG, "addPackage: $packageName")
        mKioskPackages.add(packageName)
        msg(TAG, "Packages: " + mKioskPackages.size)
    }

    fun enabled(mEnabled: Boolean): Boolean {
        // Log.msg(TAG,"Enabled: "+mEnabled);
        try {
            // ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            // ActivityManager.getLockTaskModeState api is not available in pre-M.
            if (mEnabled) {
                setRestrictions(mEnabled)
                enableStayOnWhilePluggedIn(mEnabled)
                setUpdatePolicy(mEnabled)
                //Comento IFA 07 Oct - setAsHomeApp( getPackageName(), "LockedActivity",true);
                setKeyGuardEnabled(mEnabled)
                //setLockTask(mEnabled);
                hideSystemUI(window)
            } else {
                mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, mEnabled)
                setRestrictions(mEnabled)
                enableStayOnWhilePluggedIn(mEnabled)
                setUpdatePolicy(mEnabled)
                setKeyGuardEnabled(mEnabled)
                //Comento IFA 07 Oct -  setAsHomeApp( getPackageName(), "EnrollActivity", false);
                //  setLockTask(mEnabled);
                setDefaultActiviy(packageName, "EnrollActivity")
            }

            //Guarda el Status
            SettingsApp.setKiosko(mEnabled)
            // Log.msg(TAG,"enabled: Termino....");
        } catch (ex: Exception) {
            guardar(TAG, "enabled", ex.message)
        }
        return true
    }

    fun setDefaultActiviy(packageName: String?, className: String?) {
        try {
            //  Log.msg(TAG,"---------------------> setDefaultActiviy "+packageName + " - "+className);
            //stopLockTask();
            //setDefaultKioskPolicies(false);
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, getPackageName())
            mPackageManager.setComponentEnabledSetting(
                ComponentName(getPackageName(), EnrollActivity::class.java.name),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP
            )
            //finish();
            //  startActivity(new Intent(this, EnrollActivity.class));
        } catch (ex: Exception) {
            guardar(TAG, "setDefaultActiviy", ex.message)
        }
    }
    /*
    public void setDefaultActiviy(String packageName,String className) {
        stopLockTask();
        //setDefaultKioskPolicies(false);
        mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, getPackageName());
        mPackageManager.setComponentEnabledSetting(new ComponentName(packageName,className),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP);
        finish();
        startActivity(new Intent(this, EnrollActivity.class));
    }*/
    /*    public void setLockTask(Boolean start) {
        Log.msg(TAG,"setLockTask: "+start);
        // set lock task packages
        if (start) {
            //Si viene vacio, agrega este paquete.
            if(mKioskPackages.size() ==0)
                addPackage(getPackageName());

            String[] packages = mKioskPackages.toArray(new String[0]);
            for (String packageName : packages
            ) {
                Log.msg("setLockTask package: ",packageName);
            }
            mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, new String[]{
                    getPackageName(), */
    /** PUT OTHER PACKAGE NAMES HERE!  */ /*
            });
            //  mDevicePolicyManager.setLockTaskPackages(mAdminComponentName,packages);
            startLockTask();
        }
        else {
            mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, new String[]{});
            stopLockTask();
        }
    }*/
    fun setLockTask(start: Boolean) {
        val mDevicePolicyManager = this.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val mAdminComponentName = DeviceAdminReceiver.getComponentName(this)
        msg(TAG, "[setLockTask] start: $start")
        // set lock task packages
        if (start) {
            val APP_PACKAGES = MainApp.getAppPermitted(this)
            try {
                //Define las Aplicaciones permitidas.
                mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, APP_PACKAGES)
                var flagsBefore = mDevicePolicyManager.getLockTaskFeatures(mAdminComponentName)
                flagsBefore = flagsBefore and DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS.inv()
                mDevicePolicyManager.setLockTaskFeatures(mAdminComponentName, flagsBefore)
                startLockTask()
            } catch (e: Exception) {
                guardar(TAG, "setLockTask", e.message)
            }
        } else {
            mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, arrayOf())
            stopLockTask()
        }
    }

    fun setRestrictions(disallow: Boolean?) {
        //   Log.msg(TAG,"setRestrictions: disallow "+disallow);
        /* setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, disallow);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, disallow);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, disallow);*/
        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, disallow!!)
    }

    private fun setUserRestriction(restriction: String, disallow: Boolean) {
        // Log.msg(TAG,"setUserRestriction: restriction "+disallow);
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction)
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction)
        }
    }

    fun setAsHomeApp(packageName: String, className: String, enable: Boolean) {
        msg(TAG, "setAsHomeApp: $packageName.$className ,$enable")
        var ln = 1
        try {
            if (enable) {
                ln = 1
                val intentFilter = IntentFilter(Intent.ACTION_MAIN)
                intentFilter.addCategory(Intent.CATEGORY_HOME)
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
                ln = 2
                mDevicePolicyManager.addPersistentPreferredActivity(
                    mAdminComponentName, intentFilter, ComponentName(packageName, className)
                )
            } else {
                mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, getPackageName())
            }
        } catch (ex: Exception) {
            guardar(TAG, "setAsHomeApp ($ln)", ex.message)
        }
    }

    fun enableStayOnWhilePluggedIn(active: Boolean) {
        //  Log.msg(TAG,"enableStayOnWhilePluggedIn: active "+active);
        if (active) {
            mDevicePolicyManager.setGlobalSetting(
                mAdminComponentName,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN, (BatteryManager.BATTERY_PLUGGED_AC
                        or BatteryManager.BATTERY_PLUGGED_USB
                        or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
            )
        } else {
            mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "0")
        }
    }

    fun setUpdatePolicy(enable: Boolean) {
        // Log.msg(TAG,"setUpdatePolicy: enable "+enable);
        if (enable) {
            mDevicePolicyManager.setSystemUpdatePolicy(
                mAdminComponentName,
                SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
            )
        } else {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null)
        }
    }

    fun setKeyGuardEnabled(enable: Boolean?) {
        //    Log.msg(TAG,"setKeyGuardEnabled: enable "+enable);
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, !enable!!)
    }

    fun hideSystemUI(window: Window) {
        // Log.msg(TAG, "hideSystemUI");
        try {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.decorView.systemUiVisibility = flags
            //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } catch (ex: Exception) {
            guardar(TAG, "hideSystemUI", ex.message)
        }
        /*  getWindow().getDecorView().setSystemUiVisibility(
                |
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
      //  getWindow().setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS, WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);*/
    }

    fun showSystemUI(window: Window) {
        //  Log.msg(TAG,"showSystemUI");
        try {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.decorView.systemUiVisibility = flags
        } catch (ex: Exception) {
            guardar(TAG, "showSystemUI", ex.message)
        }
    }

    val isEnabled: Boolean
        get() = SettingsApp.isKiosko()
}