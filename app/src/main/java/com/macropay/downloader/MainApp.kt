package com.macropay.downloader

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.data.preferences.Status
import com.macropay.downloader.data.preferences.TipoBloqueo
import com.macropay.downloader.domain.usecases.main.DPCAplication
import com.macropay.downloader.domain.usecases.main.StartDPC
import com.macropay.downloader.domain.usecases.provisioning.ProvisioningManual
import com.macropay.downloader.ui.manual.AdminActivity
import com.macropay.downloader.ui.provisioning.ResetCveActivity
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.activities.Dialogs
import com.macropay.downloader.utils.device.DeviceService
import com.macropay.downloader.utils.logs.LogInfoDevice
import com.macropay.downloader.utils.policies.Restrictions
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltAndroidApp
class MainApp: Application(){
    val TAG = "MainApp"
    @Inject
    lateinit var   dpcAplication : DPCAplication
    @Inject
    lateinit var   startDPC: StartDPC


    @Inject
    lateinit var   restrinctions: Restrictions
    override fun getApplicationContext(): Context {
        return super.getApplicationContext()
    }

    companion object {
        private var mContext: Context? = null
        var instance: MainApp? = null
    }
    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
        instance = this
        try{
           // System.out.println("MainApp, inicio....******************************")
            Auxiliares.init(applicationContext)

            Log.init("downloader",this)
            Settings.init(this)

            SettingsApp.init(this)
           // Log.msg(TAG,"\n\n\n")
            Log.msg(TAG,"")
            Log.msg(TAG,"")
            Log.msg(TAG,"")
            Log.msg(TAG,"")
            Log.msg(TAG,"")
            Log.msg(TAG,"[onCreate] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
            Log.msg(TAG,"[onCreate] +++            < Inicializa la Aplicacion DOWNLOAD >            +++")
            Log.msg(TAG,"[onCreate] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
            Settings.setSetting(Cons.KEY_IS_SERVICE_RUNNING,false)
            //Loguea informacion del dispositivo y de Status de la App.
            LogInfoDevice.deviceInfo(applicationContext)
            LogInfoDevice.statusApp(applicationContext)
            avoidSystemError(true)
            //Para atrapar errores que no esten manejados por un try/catch
            Thread.setDefaultUncaughtExceptionHandler( DefaultExceptionHandler(applicationContext))

            //Asegura que se levante el Servicio de DeviceAdminService
          //Todo:14Nov2023 verificar si se usa  ensureAdminService()

            if(Status.currentStatus ==Status.eStatus.TerminoEnrolamiento){
                Log.msg(TAG,"[onCreate] va iniciar - AdminActivity")
                showActivity(applicationContext)
            }
            else {
                //Log.msg(TAG,"[onCreate] va iniciar - iniciarAlarm")
                //Status.currentStatus = Status.eStatus.TerminoEnrolamiento

            }
            Log.msg(TAG,"[onCreate] ++++++++++++++++++++< Termino > ++++++++++++++++++++++++++++++++++++++")
        }catch (ex:Exception){
            System.out.println(TAG +"[onCreate], ERROR: "+ex.message)
//            Log.msg(TAG,"onCreate- Errror:\n"+ex.message)
        }
    }
    fun showActivity(context:Context){
        Log.msg(TAG,"[showActivity] va iniciar - AdminActivity")
        try{
            val handlerService = Handler(Looper.getMainLooper())
            handlerService.postDelayed({
            val intentMain =  Intent(context, AdminActivity::class.java)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentMain)
            },3_000)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "showActivity", ex.message)
        }
    }

    fun  ensureAdminService(){
        Settings.setSetting(Cons.KEY_DEVICE_ADMIN_ENABLED,false)
        // Log.msg(TAG,"[onCreate] postdelyed, startService..")
        val handlerService = Handler(Looper.getMainLooper())
        handlerService.postDelayed({
            val adminServiceEnabled = Settings.getSetting(Cons.KEY_DEVICE_ADMIN_ENABLED,false)
            Log.msg(TAG,"[onCreate] isServiceEnabled:  ${DeviceService.isServiceEnabled(applicationContext)} adminServiceEnabled: $adminServiceEnabled")
            if(!adminServiceEnabled){
                Log.msg(TAG,"[onCreate] va iniciar el servicio -startDPC.start() [Forced]")

                var bIsHidden = Dialogs.isIconHidden(applicationContext)
                Log.msg(TAG,"[onCreate]: esta oculto: $bIsHidden")
                //--> Si esta oculto, lo muestra,
                //  startDPC.start
                Settings.setSetting(Cons.PROBLEM_ADMINSERVICE,false)
                Dialogs.showAppIcon(applicationContext,!bIsHidden)
                var bHideRequired = Settings.getSetting(TipoBloqueo.hide_icon_dpc,false)
                Log.msg(TAG,"[onCreate] debe estar oculto: $bHideRequired")
                if(bHideRequired == bIsHidden){
                    Log.msg(TAG,"[onCreate] Actualizar estado ")
                    Dialogs.showAppIcon(applicationContext,bHideRequired)
                }
            }else {
                // Settings.setSetting(Cons.PROBLEM_ADMINSERVICE,false)
                Log.msg(TAG,"[onCreate] startDPC.start() iniciado en DevAdminService [normal]")
            }
        },3_000)


    }
    fun avoidSystemError(bEnabled:Boolean=true) {
        com.macropay.utils.logs.Log.msg(TAG,"[avoidSystemError] va a asignar: bEnabled: $bEnabled")
        try{
            restrinctions.setRestriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, bEnabled)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"avoidSystemError*",ex.message)
        }
    }
    private fun restart(){
        Log.msg(TAG, "[restart]")
        try{
            System.exit(1)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "restart", ex.message)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        try{
            Log.msg(TAG,"[onTerminate] ++++++++++++++++++++< Termino Application > ++++++++++++++++++++++++++++++++++++++")
        }catch (ex:Exception){

        }
    }
    private fun checkIconDbg(context: Context){
        try {
            if(Log.isDbgIconEnabled()) {
                Log.msg(TAG,"[start] mostrar icon")
                Settings.setSetting(TipoBloqueo.hide_icon_dpc,false)
                Dialogs.showAppIcon(context,false)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "checkIconDbg", ex.message)
        }
    }




   fun startProcess()= runBlocking{
       Log.msg(TAG,"[startProcess] <--------------[ inicio ]------------->")
        try{
            //Si se reinicio, y debe continuar con el proceso de instalacion...
            if (Settings.getSetting("restartInstall", false)) {
                val provisioningManual = ProvisioningManual(applicationContext)
                launch {
                    provisioningManual.continuaInstall()
                }
                return@runBlocking
            }

            //!!NO QUITAR!!
            //Esta validacion es para evitar que inicie la instalacion,
            //Antes de que termine la configuracion de Android.
            //Durante el provisiong, no deberia funcionar el DeviceService.
            if( ! Settings.getSetting(Cons.KEY_FIRST_REBOOT,false)){
                Log.msg(TAG,"[startProcess] <--------------------------->")
                Log.msg(TAG,"[startProcess] <---- Se sale, aun no entra el receiver. {FIRST_REBOOT} ---->")
                Log.msg(TAG,"[startProcess] <--------------------------->")
                return@runBlocking
            }
            Log.msg(TAG,"[startProcess] <----------- dpcAplication ---------->")
          //  return@runBlocking

            //*********************
            //Inicia la aplicacion
          launch {
                val success = withContext(Dispatchers.IO){
                    // delay(3_000)
                    Log.msg(TAG,"[startProcess] <----------- start() ---------->")
                    dpcAplication.start()
                }
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"startProcess",ex.message)
        }
    }

}

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
        instance = this
    }

    override fun getApplicationContext(): Context {
        return super.getApplicationContext()
    }

    companion object {
        private var mContext: Context? = null
        var instance: ApplicationClass? = null
    }
}