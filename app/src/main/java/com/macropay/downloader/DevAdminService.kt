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
            Log.msg(TAG,"[onCreate] status: ${Status.currentStatus} isProvisioning: ${dpcValues.isProvisioning}")
            Settings.setSetting(Cons.KEY_IS_SERVICE_RUNNING,true)
            if(BuildConfig.isTestTCL.equals("true")){
              //  if(dpcValues.isProvisioning)
                if(Status.currentStatus == Status.eStatus.TerminoEnrolamiento)
                    ToastDPC.showPolicyRestriction(this.applicationContext,"BackgroundService","Si esta Funcionando el Servicio...")
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
