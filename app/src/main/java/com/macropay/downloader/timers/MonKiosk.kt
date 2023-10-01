package com.macropay.downloader.timers

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.data.preferences.dpcValues.mAdminComponent
import com.macropay.downloader.data.preferences.dpcValues.mDpm
import com.macropay.downloader.data.preferences.dpcValues.timerMonitor
import com.macropay.downloader.utils.Settings.getSetting
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.Utils
import com.macropay.downloader.utils.activities.Dialogs.isRunning
import com.macropay.downloader.utils.activities.Dialogs.lockScreen
import com.macropay.downloader.utils.samsung.KnoxConfig
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


//---------------------------------------------------------------
// Monitoreo del status del Bloque...
//---------------------------------------------------------------
@Singleton
class MonKiosk
@Inject constructor(
    @ApplicationContext val context:Context) : TimerTask(){


  //  private val context: Context
   // private val activity: Activity
    private lateinit var className: Class<*>
    private var intent: Intent? = null
    val TAG = "MonKiosk"
    var bProcesando = false
    var intervalo = 0
    var intento = 0
    var listener: KioskStatus? = null
    var lastExecution = LocalDateTime.now()
    fun setOnShowedStatus(listener: KioskStatus) {
        this.listener = listener
    }

    fun setParams( className: Class<*>, intent: Intent) {
        this.className = className
        this.intent = intent
    }

    //Constructor
    init {
        Log.msg(TAG, "[MonKiosk] create")
        //  this.context = context
        //    activity = activity
        //  this.className = className
        //  this.intent = intent
        intervalo = 0
        intento = 0
        Log.msg(TAG, "[MonKiosk] create termino")
    }

/*    override fun scheduledExecutionTime(): Long {
        Log.msg(TAG,"[scheduledExecutionTime]")
        return super.scheduledExecutionTime()
    }*/

    override fun run() {
      //  val timing = System.currentTimeMillis() - scheduledExecutionTime()
        val segs  = Utils.tiempoTranscurrido(lastExecution,ChronoUnit.SECONDS).toInt()
        Log.msg(TAG,"[run] ***************************************************")
        Log.msg(TAG,"[run] intento: $intento segs:$segs" )
        Log.msg(TAG,"[run] ***************************************************")
        if(segs<3 ){ //&& intento > 0
            Log.msg(TAG,"Se salio, aun no se cumple el tiempo...")
            return
        }
        //if (System.currentTimeMillis() - scheduledExecutionTime() >=           MAX_TARDINESS)               return;  // Too late; skip this execution.
        lastExecution = LocalDateTime.now()
        if (SettingsApp.isUpdating()) return
        //val bIsVisible = Utils.isLockedShowed()
        val bisRunning = isRunning(context, className)
        var bKioskRequired = Kiosko.kioskRequired
        val bKioskRequired2 = getSetting("kioskoRequired", false)
        val bIsShowed = getSetting(Cons.KEY_IS_KIOSK_SHOWED, false)
        var bIsLockTask = Utils.isLockTaskEnabled(context)
        val bIslockTaskEvent = getSetting("kioskoShowed", false)
        try {
            Log.msg(TAG, "[run] =================================================")
            Log.msg(TAG, "[run] isrunning:  "+ bisRunning );
            Log.msg(TAG, "[run] bKioskRequired:  $bKioskRequired")
            Log.msg(TAG, "[run] bIsShowed:  $bIsShowed")
            Log.msg(TAG, "[run] bIsLockTask:  $bIsLockTask bIslockTaskEvent:  $bIslockTaskEvent ")

            Log.msg(TAG, "[run] bIsLockTask:  $bIsLockTask  or  isrunning:  "+ bisRunning +" and bKioskRequired:  $bKioskRequired [$bKioskRequired2]" );
            // if (bNeedBloqueo && (  !bIsVisible || !Utils.isLockTaskEnabled(context) )) {       //val bisRunning = isRunning(context, className)
            // if(!bisRunning || !bIsShowed)
            //
            val handlerLock = Handler(Looper.getMainLooper())
            if ((!bIsLockTask || !bisRunning ) && bKioskRequired)   {
                intento++

                Log.msg(TAG, "[run] +++++++++++++++++++++++++++++++++++++++++++")
                Log.msg(TAG, "[run] intento: $intento - VA MOSTRAR LA ACTIVITY.")
                //Si la pantalla es apagada, se sale...
                if(intento >2 && !isScreenOn(context) ){
                    Log.msg(TAG, "[run] isScreenOn: ---> ${isScreenOn(context)} ")
                    bIsLockTask = true
                }
                try {
/*                    val thread: Thread = object : Thread() {
                        override fun run() {
                            handlerLock.post {*/

                    CoroutineScope(Dispatchers.Main).
                    launch {
                        Log.msg(TAG, "[run] [showActivity] ******----- intento: "+intento +" bIsShowed: "+bIsShowed)
                        if (intento == 3) {
                            Log.msg(TAG, "[run] VA A BLOQUEAR LA PANTALLA. con lockScreen()")
                            lockScreen()
                        }

                        Log.msg(TAG, "[run] va mostrar la activity ")
                        showActivity(context, className)

                        Log.msg(TAG, "[run] bIsLockTask:  $bIsLockTask  or  isrunning:  "+ bisRunning +" and bKioskRequired:  $bKioskRequired" );
                        if (intento > 50 || bIsLockTask) {
                            Log.msg(TAG, "[run] [2] TERMINO TIMER - MONITOREO DE KIOSKO")
                            timerMonitor!!.enabledKiosk(false, null, null)
                        }
                    }
/*                              }
                      }
                    }
                    thread.start()*/
                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "thread: ", ex.message)
                }
            } else {
                intervalo++
                Log.msg(TAG, "[run] intervalo:  $intervalo bIsLockTask: $bIsLockTask cls: [${this.className.name}]")
                if (intervalo > 15 || bIsLockTask) {
                    Log.msg(TAG, "[run] TERMINO TIMER - MONITOREO DE KIOSKO --- 13Mar23")
                    timerMonitor!!.enabledKiosk(false, null, null)

                    //Verifica si esta pendiente la activacion de knox, Esto es porque en ocasiones se va para atras el dialogo de aceptacion de licencia.
                    if(this.className.name.contains("EnrollActivity")) {
                        handlerLock.postDelayed ({
                            val bKnoxLicenseActived = SettingsApp.getSetting(Cons.KEY_ES_LICENCIA_REQUERIDA, false)
                            Log.msg(TAG, "[run] bKnoxLicenseActived = $bKnoxLicenseActived ")
                            if (bKnoxLicenseActived) {
                                KnoxConfig.activateLicense()
                            }
                        },1_000)
                    }
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "run: ", ex.message)
        }
    }
    private fun isScreenOn(context: Context):Boolean{
        var isScreenOn = true
        try{
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
            isScreenOn = pm!!.isInteractive
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "isScreenOn", ex.message)
        }
        return isScreenOn
    }
    private fun showActivity(context: Context, activityClass: Class<*>) {
        try {
            Log.msg(TAG, "[showActivity] -1- [ " + activityClass.name + " ]")
            val intentMain: Intent
            if (intent == null) {
                intentMain = Intent(context, activityClass)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            } else intentMain = this.intent!!
            context.startActivity(intentMain)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "showActivity", ex.message)
        }
    }

    private fun hideStatusBar(disallow: Boolean) {
        try {
            mDpm!!.setStatusBarDisabled(mAdminComponent!!, disallow)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "hideStatusBar", ex.message)
        }
    }

/*    @Throws(Throwable::class)
    protected fun finalize() {
      //  super.finalize()
        try {
            Log.msg(TAG, "finalize")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "finalize*", ex.message)
        }
    }*/
}


interface KioskStatus {
    fun onShowed(success: Boolean)
}
