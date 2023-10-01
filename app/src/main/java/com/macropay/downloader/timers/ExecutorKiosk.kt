package com.macropay.downloader.timers

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.Utils
import com.macropay.downloader.utils.activities.Dialogs
import com.macropay.downloader.utils.samsung.KnoxConfig
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/*
* So what are the main differences between the Timer and the ExecutorService solution:

Timer can be sensitive to changes in the system clock; ScheduledThreadPoolExecutor isn't.
Timer has only one execution thread; ScheduledThreadPoolExecutor can be configured with any number of threads.
Runtime Exceptions thrown inside the TimerTask kill the thread, so the following scheduled tasks won't run further;
* with ScheduledThreadExecutor, the current task will be cancelled, but the rest will continue to run.
* */
@Singleton
class ExecutorKiosk
@Inject constructor(
    @ApplicationContext val context:Context)
{
    var TAG = "ExecutorKiosk"
  //  private var executor = Executors.newFixedThreadPool(1)
    var executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
//
    private lateinit var className: Class<*>
   // private lateinit var currentClassName: Class<*>
    private var intent: Intent? = null
    private var bProcesando = false
    private var intervalo = 0
    private var intento = 0
    private var listener: KioskStatus? = null
    private var lastExecution = LocalDateTime.now()
    var isActivityShowed = false

    set(value) {
        Log.msg(TAG,"[set] isActivityShowed: $value")
        field = value}
    get() = field


    fun resetExecutor() {
        Log.msg(TAG, "[resetExecutor]")
        try {
            isActivityShowed = true
            Log.msg(TAG, "[resetExecutor] isShutdown ${executor.isShutdown}")
            Log.msg(TAG, "[resetExecutor] isTerminated ${executor.isTerminated}")

            if (!executor.isShutdown || !executor.isTerminated) {
                Log.msg(TAG, "[resetExecutor] RESET el - Executor")

                executor.shutdown()
                executor.awaitTermination(2, TimeUnit.SECONDS)
                executor = Executors.newSingleThreadScheduledExecutor()

            }else {
                Log.msg(TAG,"[resetExecutor] No hizo el reset...** ")
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "resetExecutor", ex.message)
        }
    }


     fun show( className: Class<*>, intent: Intent) {
        var ln = 0
/*        try{
            if(this.className != null)
                currentClassName = this.className
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "show **", ex.message,false)
        }*/
        this.className = className
        this.intent = intent
        intervalo = 0
        intento = 0
        try {
        Log.msg(TAG, "[show] inicia [${className.name}] ")

            ln = 1
            resetExecutor()
            ln = 3
            // Run a task in a background thread
            executor.scheduleAtFixedRate(Runnable { activeActivity() },2_000,3_000,TimeUnit.MILLISECONDS)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "show ln=$ln", ex.message)
        }
    }

    fun isElapsedTime():Boolean{
        var isElapsed = true
        try {
            val segs = Utils.tiempoTranscurrido(lastExecution, ChronoUnit.SECONDS).toInt()
            val showed = Settings.getSetting("kioskoShowed", false)
            var bIsLockTask = Utils.isLockTaskEnabled(context)
            // bIsLockTask es usada, por Si ya esta bloqueado, lo deja pasar para que termine el Executor...
            if (segs < 3 && intento > 0 && !bIsLockTask) { //
                Log.msg(TAG, "[activeActivity] Se salio, aun no se cumple el tiempo...")
                isElapsed = false
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "isElapsedTime", ex.message)
        }
        return isElapsed
    }
    fun activeActivity():Boolean {
        var isShowed = false

        try {
            if(!isElapsedTime()) return isShowed

            lastExecution = LocalDateTime.now()
            if (SettingsApp.isUpdating()) return isShowed

            val bisRunning = Dialogs.isRunning(context, className)
            var bKioskRequired = Kiosko.kioskRequired
            val bKioskRequired2 = Settings.getSetting("kioskoRequired", false)
            val bIsShowed = Settings.getSetting(Cons.KEY_IS_KIOSK_SHOWED, false)
            var bIsLockTask = Utils.isLockTaskEnabled(context)
            val bIslockTaskEvent = Settings.getSetting("kioskoShowed", false)

            Log.msg(TAG, "[activeActivity] =================================================")
            Log.msg(TAG, "[activeActivity] isrunning: [$bisRunning] KioskRequired: [$bKioskRequired] IsShowed:  [$bIsShowed] bIsLockTask: [$bIsLockTask] bIslockTaskEvent: [$bIslockTaskEvent]" );

            Log.msg(TAG, "[activeActivity] if(bIsLockTask:  $bIsLockTask  or  isrunning:  "+ bisRunning +" and bKioskRequired:  $bKioskRequired [$bKioskRequired2]  isScreenOn: ---> ${isScreenOn(context)})" );
            // if (bNeedBloqueo && (  !bIsVisible || !Utils.isLockTaskEnabled(context) )) {       //val bisRunning = isRunning(context, className)
            // if(!bisRunning || !bIsShowed)
            //
            val handlerLock = Handler(Looper.getMainLooper())
            if ((!bIsLockTask || !bisRunning ) && bKioskRequired)   {
                intento++
                Log.msg(TAG, "[activeActivity] ++++++++++++++++<  VA MOSTRAR LA ACTIVITY [intento: $intento]>+++++++++++++++++++++++++++")
                //Si la pantalla es apagada, se sale...
                if(intento >2 && !isScreenOn(context) ){
                    Log.msg(TAG, "[activeActivity] isScreenOn: ---> ${isScreenOn(context)} ")
                    bIsLockTask = true
                }
                try {
/*                    val thread: Thread = object : Thread() {
                        override fun run() {
                            handlerLock.post {*/
                    CoroutineScope(Dispatchers.Main).
                    launch {
                        Log.msg(TAG, "[activeActivity] [showActivity] ******----- intento: "+intento +" bIsShowed: "+bIsShowed)
                        if (intento == 3) {
                            Log.msg(TAG, "[activeActivity] VA A BLOQUEAR LA PANTALLA. con lockScreen()")
                            Dialogs.lockScreen()
                        }

                        Log.msg(TAG, "[activeActivity] va mostrar la activity ")
                        showActivity(context, className)

                        Log.msg(TAG, "[activeActivity] bIsLockTask:  $bIsLockTask  or  isrunning:  "+ bisRunning +" and bKioskRequired:  $bKioskRequired" );
                        if (intento > 50 || bIsLockTask) {
                            Log.msg(TAG, "[activeActivity] [2] TERMINO TIMER - MONITOREO DE KIOSKO")
                           // dpcValues.timerMonitor!!.enabledKiosk(false, null, null)
                            //isShowed = true
                            resetExecutor()
                        }
                    }
/*                              }
                      }
                    }
                    thread.start()*/
                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "activeActivity: ", ex.message)
                }
            } else {
                intervalo++
                Log.msg(TAG, "[activeActivity] intervalo:  $intervalo bIsLockTask: $bIsLockTask cls: [${this.className.name}]")
                Log.msg(TAG, "[activeActivity] ++++++++++++++++<  VA MOSTRAR LA ACTIVITY [intento: $intento]>+++++++++++++++++++++++++++")
                if (intervalo > 15 || bIsLockTask) {
                    Log.msg(TAG, "[activeActivity] TERMINO TIMER - MONITOREO DE KIOSKO ")
                    //isShowed = true
                    //dpcValues.timerMonitor!!.enabledKiosk(false, null, null)
                    resetExecutor()
                    //Verifica si esta pendiente la activacion de knox, Esto es porque en ocasiones se va para atras el dialogo de aceptacion de licencia.
                    if(this.className.name.contains("EnrollActivity")) {
                        handlerLock.postDelayed ({
                            val bKnoxLicenseActived = SettingsApp.getSetting(Cons.KEY_ES_LICENCIA_REQUERIDA, false)
                            Log.msg(TAG, "[activeActivity] bKnoxLicenseActived = $bKnoxLicenseActived ")
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
        return isShowed
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
            dpcValues.mDpm!!.setStatusBarDisabled(dpcValues.mAdminComponent!!, disallow)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "hideStatusBar", ex.message)
        }
    }
}