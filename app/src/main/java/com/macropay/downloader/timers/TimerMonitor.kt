package com.macropay.downloader.timers

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.macropay.data.logs.ErrorMgr.guardar
import com.macropay.data.logs.Log
import com.macropay.data.logs.Log.msg
import com.macropay.utils.preferences.Kiosko
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import javax.inject.Inject

// class TimerMonitor(context: Context) {
//@Singleton
class TimerMonitor
    @Inject constructor(
    @ApplicationContext val context:Context
    ) {

    val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
/*    @Inject
    lateinit var monKiosk: MonKiosk*/

    @Inject
    lateinit var executorKiosk: ExecutorKiosk

   /* @Inject
   ateinit var monSinConexion: MonSinConexion
    */


    var starttime: Long = 0
    val minuto = 60 * 1000

    //Timers
    var tmrSinConn // = new Timer();
            : Timer? = null
    var tmrMTTQ // = new Timer();
            : Timer? = null
    var tmrBloqueo: Timer? = null
    var tmrKiosk: Timer? = null
   // private val context: Context
    private var activity: Activity? = null
    private var enabledSinConexion = false
    private var enabledMTTQ = false
    private var enabledBlqueo = false
    private val enabledKiosk = false
    val TAG = "TimerMonitor"



    //  ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    init {
        msg(TAG, "[init] -<>-<>-<>-<>- Constructor - TimerMonitor")
       // this.context = context
    }

    //Getters/Setters
    fun setActivity(activity: Activity?) {
        this.activity = activity
       // val component = AppModule.WarComponent..create()
       // val war = component.provideWarDagger()
    }

    /*fun enabledSinConexion(enabledSinConexion: Boolean) {
        try {
            this.enabledSinConexion = enabledSinConexion
            msg(TAG, "[enabledSinConexion] - enabledSinConexion: [$enabledSinConexion]")
            if (!enabledSinConexion) {
                if (tmrSinConn != null) {
                    tmrSinConn!!.cancel()
                    tmrSinConn!!.purge()
                }
            } else {
                //Parametros del monitor de Sin conexcion
                val frecSinCon = (5 * minuto).toLong()
                val delaySinCon = (2 * minuto).toLong()
                tmrSinConn = Timer()

                //Parametros para Monitor de MQTT
                msg(TAG, "[enabledSinConexion] - MonSinConexion [$frecSinCon ms, delay: $delaySinCon ms")
                // scheduleAtFixedRate
              //  tmrSinConn!!.schedule(MonSinConexion(context), delaySinCon, frecSinCon)
                var  monSinConexion =com.macropay.downloader.di.Inject.inject().getMonSinConn()
                msg(TAG, "[enabledSinConexion] - MonSinConexion creo instancia...")
                tmrSinConn!!.schedule(monSinConexion, delaySinCon, frecSinCon)
                msg(TAG, "[enabledSinConexion] - Se ejecuto el Scheduler..")
            }
        } catch (ex: Exception) {
            guardar(TAG, "enabledSinConexion", ex.message)
        }
    }*/

    fun enabledMTTQ(enabledMTTQ: Boolean) {
        try {
            Log.msg(TAG,"[enabledMTTQ] 1.- enabledMTTQ= "+enabledMTTQ);
            this.enabledMTTQ = enabledMTTQ

            if (!enabledMTTQ) {
                // Log.msg(TAG,"[enabledMTTQ] 2.- cancelar");
                if (tmrMTTQ != null) {
                    //   Log.msg(TAG,"[enabledMTTQ] 2.- cancelar -1-");
                    tmrMTTQ!!.cancel()
                    //Log.msg(TAG,"[enabledMTTQ] 2.- cancelar -2-");
                    tmrMTTQ!!.purge()
                    //Log.msg(TAG,"[enabledMTTQ] 2.- cancelar -3-");
                }
            } else {
                msg(TAG, "[enabledMTTQ] 3.- iniciar -1-")
                tmrMTTQ = Timer()
                msg(TAG, "[enabledMTTQ] 4.- iniciar -2-")
                val frecMonMQTT = (2 * minuto).toLong()
                val delayMonMQTT = (3 * minuto).toLong()
                msg(TAG, "[enabledMTTQ] - MonMttq [$frecMonMQTT ms, delay: $delayMonMQTT ms")
                //Revisq conexion, cada 10 minutos.
                tmrMTTQ!!.schedule(MonMttq(context), delayMonMQTT, frecMonMQTT)
            }
        } catch (ex: Exception) {
            guardar(TAG, "enabledMTTQ", ex.message)
        }
    }

    //--
    fun enabledKiosk(enabledKiosk: Boolean, activity: Class<*>?, intent: Intent?) {

        Log.msg(TAG,"[enabledKiosk] ")
        try{
            var clase = if(activity == null) "" else activity!!.name
            Log.msg(TAG,"[enabledKiosk] enabledKiosk: [$enabledKiosk] activity: [${clase}" )
        }catch (ex:Exception){
            guardar(TAG, "enabledKiosk**", ex.message,false)
        }
        var ln = 0
        Kiosko.kioskRequired = enabledKiosk

        ln = 1
        enabledBlqueo = enabledKiosk
        ln = 2
        try {
            if (!enabledBlqueo) {
                ln = 3
                Log.msg(TAG,"[enabledKiosk] CANCELAR ...")
                executorKiosk.resetExecutor()
            } else {
                ln = 4
                msg(TAG, "[enabledKiosk] INICIAR - executor")
                executorKiosk.show(activity!!, intent!!)
            }
        } catch (ex: Exception) {
            guardar(TAG, "enabledKiosk [$ln]", ex.message)
        }
    }
}