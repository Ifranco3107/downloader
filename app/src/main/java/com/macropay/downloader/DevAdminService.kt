package com.macropay.downloader


import android.app.admin.DeviceAdminService
import android.content.Intent
import com.macropay.data.BuildConfig
import com.macropay.utils.preferences.Cons
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log

import com.macropay.downloader.data.preferences.Status
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.domain.usecases.main.StartDPC
import com.macropay.downloader.ui.common.mensajes.ToastDPC
import com.macropay.utils.broadcast.Sender

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class DevAdminService
@Inject constructor()
    : DeviceAdminService() {
    private val TAG = "DevAdminService"
    @Inject
    lateinit var   startDPC: StartDPC
    override fun onCreate() {
        super.onCreate()
        try{

            Log.msg(TAG, "[onCreate] *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*")
            Log.msg(TAG, "[onCreate] *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*")
            Log.msg(TAG, "[onCreate] status: ${Status.currentStatus} isProvisioning: ${dpcValues.isProvisioning}")
            Log.msg(TAG, "[onCreate] *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*")
            Log.msg(TAG, "[onCreate] *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*")
            Settings.setSetting(Cons.KEY_IS_SERVICE_RUNNING,true)
            if(BuildConfig.isTestTCL.equals("true")){
                if(Status.currentStatus == Status.eStatus.TerminoEnrolamiento){
                    val tipoTest = Settings.getSetting(Cons.KEY_TYPE_TEST,"SERVICE")
                    Log.msg(TAG,"[onCreate] tipoTest: $tipoTest")
                    val msg = if(tipoTest.equals("REBOOT"))  "SERVICIO" else "CONTROL ERRORES"
                    ToastDPC.showPolicyRestriction(this.applicationContext,"Verificación del $msg","Si esta Funcionando correctamente...")
                   // startDPC.dpcApp().iniciarAlarm(applicationContext)
                   Sender.sendStatus("Inicio_servicio")
                }
            }else{
                Log.msg(TAG,"[onCreate]  startDPC.start() ")
                Settings.setSetting(Cons.KEY_DEVICE_ADMIN_ENABLED,true)
                startDPC.start()
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onCreate",ex.message)
        }
    }

    override fun onStart(intent: Intent?, startId: Int) {
        Log.msg(TAG,"[onStart]")
        super.onStart(intent, startId)
    }

    override fun onDestroy() {
        Log.msg(TAG,"[onStart]")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.msg(TAG,"[onStartCommand]")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.msg(TAG,"[onUnbind]")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        Log.msg(TAG,"[onRebind]")
        super.onRebind(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.msg(TAG,"[onTaskRemoved]")
        super.onTaskRemoved(rootIntent)
    }
}
