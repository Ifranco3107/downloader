package com.macropay.downloader.utils.policies


import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.data.preferences.*
import com.macropay.downloader.ui.backdoor.ValidarFragment
import com.macropay.downloader.ui.common.mensajes.ToastDPC
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.Utils
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.ui.provisioning.EnrollActivity
import com.macropay.utils.network.Red
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject


open class KioskScreen : AppCompatActivity(),ValidarFragment.EditNameDialogListener {
    private val TAG = "KioskScreen"

    private var mAdminComponentName: ComponentName? = null
    private var mDevicePolicyManager: DevicePolicyManager? = null
    private var mPackageManager: PackageManager? = null
    lateinit var  activity:AppCompatActivity

    var bShowMenu = false   //Usado para el EditNameDialogListener
    var bPhase1 = false     //Usado para el EditNameDialogListener
    private var flagExit = 0
    var countTaps=0;
    var startLocked: LocalDateTime? = null

    @Inject
    lateinit var restrinctions: Restrictions

    var isShowed = false
        set(value) {
            Settings.setSetting(Cons.KEY_IS_KIOSK_SHOWED,value)
            field = value
        }
        get() {
            return field
        }

    var isLocked = false
        set(value) {
            Settings.setSetting(Cons.KEY_IS_LOCKED_ENABLED,value)
            field = value
        }
        get() {
            val status =  Settings.getSetting(Cons.KEY_IS_LOCKED_ENABLED,false)
            return status
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.msg(TAG, "[onCreate] INICIO "+super.getLocalClassName())
            mAdminComponentName = DeviceAdminReceiver.getComponentName(this)
            mDevicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            mPackageManager = packageManager
            isShowed = false
            Log.msg(TAG, "[onCreate] TERMINO")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "onCreate", ex.message)
        }
    }
    override fun onBackPressed() {
        // Log.msg(TAG, "onBackPressed - Status: [" + Status.currentStatus+"]")
        // Log.msg(TAG, "onBackPressed - bloqueado :"+ Kiosko.enabled)
        if(!Kiosko.enabled)
            super.onBackPressed();
        return
    }

    fun enabledKiosk(enabled: Boolean): Boolean {
        Log.msg(TAG, "[enabledKiosk] mEnabled: $enabled isLocked: $isLocked")
        var ln = 0
        countTaps=0
        try {
            Kiosko.kioskRequired = enabled
            if (enabled) {
                ln = 1
                disableStatusBar(enabled)

                enableStayOnWhilePluggedIn(enabled)

               // setUpdatePolicy(enabled)
                ln = 2
                //Comento IFA 07 Oct - setAsHomeApp( getPackageName(), "LockedActivity",true);
                setKeyGuardEnabled(enabled)
                ln = 3
                //setLockTask(mEnabled); onWindowFocusChanged
                hideSystemUI(window)
                ln = 4
                setShowWhenLocked(true)
                // TODO:  setLockTask(enabled) //Se hace en: onWindowFocusChanged()
                ln = 5
            } else {
                setShowWhenLocked(false)
                ln = 7
                disableStatusBar(enabled)
                ln = 8
                enableStayOnWhilePluggedIn(enabled)
                setKeyGuardEnabled(enabled)

                showSystemUI(window)

                ln = 9
               setLockTask(enabled)
 /*                setDefaultActiviy(packageName, "EnrollActivity")
                */
                ln = 10
            }

            //Guarda el Status
            Kiosko.enabled = enabled
            ln = 11
            //
            SettingsApp.setKiosko(enabled)
            ln = 12
            Log.msg(TAG, "[enabledKiosk] Termino....Kiosko.enabled: "+Kiosko.enabled )
        } catch (ex: Exception) {
           // Kiosko.enabled = enabled
            ErrorMgr.guardar(TAG, "enabledKiosk [$ln] enabled: [$enabled] ", ex.message)
        }
        return true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        try {
            var bKioskRequired = Kiosko.kioskRequired
            val bKioskRequired2 = Settings.getSetting("kioskoRequired", false)
            val bIsShowed = Settings.getSetting(Cons.KEY_IS_KIOSK_SHOWED, false)
            val bIsLockTask = Utils.isLockTaskEnabled(this)
            //
            val isEnabled = Kiosko.enabled
            Log.msg(TAG, "[onWindowFocusChanged] hasFocus: $hasFocus isEnabled: $isEnabled   bKioskRequired: $bKioskRequired bIsShowed: $bIsShowed bIsLockTask: $bIsLockTask")
            if (hasFocus && isEnabled && !bIsLockTask) {
                setLockTask(true)
            }

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "[onWindowFocusChanged]", ex.message)
        }
    }
    /*
    *           Eventos del Activity
    */


    fun setDefaultActiviy(packageName: String?, className: String?) {
        try {
            mDevicePolicyManager!!.clearPackagePersistentPreferredActivities(mAdminComponentName!!, getPackageName())
            mPackageManager!!.setComponentEnabledSetting(
                ComponentName(getPackageName(), EnrollActivity::class.java.name),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP
            )
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "setDefaultActiviy", ex.message)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun setLockTask(start: Boolean) {
        var ln = 0
        try {
            ln= 1

            // set lock task packages
            if (start) {
                ln= 2
                lockApps()
                ln= 3
                lockFeatures()

            } else {
                ln= 20
                mDevicePolicyManager!!.setLockTaskPackages(mAdminComponentName!!, arrayOf())
                stopLockTask()
            }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "setLockTask [$ln]", e.message)
        }
    }
    private fun lockFeatures(){
        var ln = 0
        val mDPM :DevicePolicyManager = this.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val mComponent :ComponentName = DeviceAdminReceiver.getComponentName(this)
        try {
            var flagsBefore:Int = mDevicePolicyManager!!.getLockTaskFeatures(mAdminComponentName!!)
            Log.msg(TAG,"[lockFeatures] 1.- flagsBefore: "+flagsBefore)
            ln= 4
            flagsBefore = flagsBefore and DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS.inv()
            ln= 5
            mDevicePolicyManager!!.setLockTaskFeatures(mAdminComponentName!!, flagsBefore)

            ln= 6
            startLockTask()
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "lockFeatures [$ln]", e.message)
        }
    }
    private fun lockApps(){
        var ln = 0
        try{
/*            val mDPM :DevicePolicyManager = this.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val mComponent :ComponentName = DeviceAdminReceiver.getComponentName(this)*/
            ln= 2

            val APP_PACKAGES = MainApp.getAppPermitted(this)
            ln= 4
            mDevicePolicyManager!!.setLockTaskPackages(mAdminComponentName!!, APP_PACKAGES)
            ln= 5
            Log.msg(TAG,"[lockApps] ok---")
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "lockApps [$ln]", e.message)
        }

    }
    fun disableStatusBar(disallow: Boolean?) {
        try{
            mDevicePolicyManager!!.setStatusBarDisabled(mAdminComponentName!!, disallow!!)
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "disableStatusBar", e.message)
        }
    }

    private fun setUserRestriction(restriction: String, disallow: Boolean) {
        try{
            if (disallow) {
                mDevicePolicyManager!!.addUserRestriction(mAdminComponentName!!, restriction)
            } else {
                mDevicePolicyManager!!.clearUserRestriction(mAdminComponentName!!, restriction)
            }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "setUserRestriction", e.message)
        }
    }

    fun setAsHomeApp(packageName: String, className: String, enable: Boolean) {
        Log.msg(TAG, "setAsHomeApp: $packageName.$className ,$enable")
        var ln = 1
        try {
            if (enable) {
                ln = 1
                val intentFilter = IntentFilter(Intent.ACTION_MAIN)
                intentFilter.addCategory(Intent.CATEGORY_HOME)
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
                ln = 2
                mDevicePolicyManager!!.addPersistentPreferredActivity(
                    mAdminComponentName!!, intentFilter, ComponentName(packageName, className)
                )
            } else {
                mDevicePolicyManager!!.clearPackagePersistentPreferredActivities(mAdminComponentName!!, getPackageName())
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "setAsHomeApp ($ln)", ex.message)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        //Solo para la pantalla de bloqueo...
        if(currentClass().contains("LockedActivity"))
        {
            countTaps++
            var segs = 100
            try{
                if(Status.currentStatus == Status.eStatus.TerminoEnrolamiento){
                    if (startLocked != null)
                        segs= Utils.tiempoTranscurrido(startLocked, ChronoUnit.SECONDS).toInt()

               //  Log.msg(TAG,"[dispatchTouchEvent] count: [$countTaps]  segs: [$segs] isLocked: [$isLocked]")

                    if(countTaps==10 && segs <10){
                        Log.msg(TAG,"[dispatchTouchEvent] $countTaps --- lockNow()")
                        mDevicePolicyManager!!.lockNow()
                    }
                }
            }catch (ex:Exception){
                ErrorMgr.guardar(TAG,"dispatchTouchEvent",ex.message)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun currentClass():String{
        var className = "LockedActivity"
        try{
            className = super.getLocalClassName()
           // Log.msg(TAG,"[currentClass] : getLocalClassName: " + className)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"currentClass",ex.message)
        }
        return className
    }

    fun enableStayOnWhilePluggedIn(active: Boolean) {
      //  Log.msg(TAG, "[enableStayOnWhilePluggedIn]  active $active")
        try {
            if (active) {
                mDevicePolicyManager!!.setGlobalSetting(
                    mAdminComponentName!!,
                    android.provider.Settings.Global.STAY_ON_WHILE_PLUGGED_IN, (BatteryManager.BATTERY_PLUGGED_AC
                            or BatteryManager.BATTERY_PLUGGED_USB
                            or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
                )
            } else {
                mDevicePolicyManager!!.setGlobalSetting(mAdminComponentName!!, android.provider.Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "0")
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "enableStayOnWhilePluggedIn", ex.message)
        }
    }
    fun turnScreenOnAndKeyguard() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                this.window.addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                )
            }
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyguardManager.requestDismissKeyguard(this, null)
            }
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "turnScreenOnAndKeyguardOff", ex.message)
        }
    }


    private val isForeground: Boolean
        private get() {
            var bResult = false
            try {
                //   Log.msg(TAG, "[isForeground] CurrentState: " + this.lifecycle.currentState)
                bResult = this.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "isForeground", ex.message)
            }
            //      Log.msg(TAG, "[isForeground] CurrentState: " +bResult)
            return bResult
        }

    fun setKeyGuardEnabled(enable: Boolean) {
        //   Log.msg(TAG, "setKeyGuardEnabled: enable $enable")
        mDevicePolicyManager!!.setKeyguardDisabled(mAdminComponentName!!, !enable)
    }

    fun hideSystemUI(window: Window) {
        //    Log.msg(TAG, "hideSystemUI")
        try {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.decorView.systemUiVisibility = flags
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "hideSystemUI", ex.message,false)
        }
    }

    fun hidSystemUI() {
        Log.msg(TAG, "")
        hideSystemUI(window)
    }
    fun showSystemUI(window: Window) {
        val isEnabled = Kiosko.enabled
      //  Log.msg(TAG,"[showSystemUI] isEnabled: ${isEnabled}")
        if(!isEnabled) return

        try {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.decorView.systemUiVisibility = flags
        } catch (ex: Exception) {
           // ErrorMgr.guardar(TAG, "showSystemUI*", ex.message)
        }
    }

    val isEnabled: Boolean
        get() = SettingsApp.isKiosko()

    val isLockedShowed: Boolean
        get() {
            var bIsVisible = false
           /* try {
                if (LockedActivity.fa != null && LockedActivity.fa!!.isDestroyed) {
                    //Log.msg(TAG, "LockedActivity isDestroyed")
                    LockedActivity.fa = null
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "isLockedShowed", ex.message)
            }
            bIsVisible = LockedActivity.fa != null*/
            return bIsVisible
        }

    fun isLockTaskEnabled(context: Context?): Boolean {
        var context = context
        if (context == null) {
            ErrorMgr.guardar(TAG, "constructor", "context = null")
            context = MainApp.getMainCtx()
        }
        var bResult = false
        try {
            val activityManager: ActivityManager
            activityManager = context!!.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            bResult = activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "isLockTaskEnabled", ex.message)
        }
        return bResult
    }

    override fun startLockTask() {
        super.startLockTask()
        isLocked = true
        startLocked =LocalDateTime.now();
    }

    override fun stopLockTask() {
        super.stopLockTask()
        isLocked = false
    }

    //

    public fun showEditDialog(flagEmergency: Int) {
        val hasNetwork = Red.isOnline
        flagExit = flagEmergency
        if (!hasNetwork) {
            ToastDPC.showToast(this, "No tienes red...")
         //   return
        }
        val fm = supportFragmentManager
        val editNameDialogFragment = ValidarFragment.newInstance("Introduce el Código:")
        editNameDialogFragment.show(fm, "fragment_validar")
    }

    override fun onFinishEditDialog(inputText: String?) {
        try {
            Log.msg(TAG, "[onFinishEditDialog]\n\n\n\n")
            Log.msg(TAG, "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
            Log.msg(TAG, "%%%%%%%%[ SALIDA DE EMERGENCIA - flagExit<$flagExit> ] %%%%%%%%%%%%%%%%")
            Log.msg(TAG, "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
            //val sCve = "Adonai_1705"
            Log.msg(TAG, "sCve: [$Cons.TEXT_VOID]")
            if (inputText == Cons.TEXT_VOID) {
                if (flagExit == 1 || flagExit == 2) {
                    this.enabledKiosk(false)
                    dpcValues.mqttAWS!!.connect(TAG)
                    closeKiosk()

                }
                if (flagExit == 3) {
                    bPhase1 = true
                }
                if (flagExit == 4) {
                    bShowMenu = true
                }
            } else {
                Log.msg(TAG, "Reboot...")
                reboot();
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onFinishEditDialog",ex.message)
        }
    }
    fun reboot() {
        Log.msg(TAG, "reboot")
        try {
          //  val restrinctions = Restrictions(this)
            restrinctions.Reboot()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "restart", ex.message)
        }
    }

    fun closeKiosk(){
        Log.msg(TAG, "[closeKiosko] -1-")
        try{
            this.enabledKiosk(false)
            Log.msg(TAG, "[closeKiosko] -2-")
            stopMonitor()
            Log.msg(TAG, "[closeKiosko] -3-")
            this.finish()
            Log.msg(TAG, "[closeKiosko] -4-")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"closeKiosko",ex.message)
        }
    }
    fun stopMonitor() {
        try {
            if(dpcValues.timerMonitor != null){
                Log.msg(TAG, "[stopMonitor] TERMINO TIMER - MONITOREO DE KIOSKO")
                dpcValues.timerMonitor!!.enabledKiosk(false, null, null)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "stopMonitor", ex.message)
        }
    }


    override fun onStart() {
        super.onStart()
        this.isShowed = true

        // Log.msg(TAG,"onStart: "+this.isShowed )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.msg(TAG,"onDestroy: "+this.isShowed )
    }

    override fun onStop() {
        super.onStop()
        //   this.isVisibleActivity = false
     //   Log.msg(TAG,"onStop: "+this.isShowed )
    }

    override fun onPause() {
        super.onPause()
        //  this.isVisibleActivity = false
        // Log.msg(TAG,"onPause: "+this.isShowed )
        this.setVisible(true)

    }

    override fun onResume() {
        super.onResume()
        this.isShowed = true
        //Log.msg(TAG,"onResume: "+this.isShowed )
    }

}