package com.macropay.downloader.domain.usecases.main

import android.content.Context
import android.content.Intent
import android.os.Build
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.data.mqtt.messages.Commands
import com.macropay.downloader.data.preferences.Status
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.domain.usecases.provisioning.ProvisioningManual
import com.macropay.downloader.ui.provisioning.EnrollActivity
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.activities.Dialogs
import com.macropay.downloader.utils.samsung.KnoxConfig
import com.macropay.downloader.utils.xiaomi.MIUI
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject



class StartDPC
@Inject constructor(@ApplicationContext val context: Context) {

    //val provisioningManual: ProvisioningManual
    private val TAG = "StartDPC"

    @Inject
    lateinit var   dpcAplication : DPCAplication



    @Inject
    lateinit var  commands: Commands

    val scope = CoroutineScope(Job() + Dispatchers.Main)

    fun start() {

        try{
            //Inicializa, Log,ErrMsg, SettingsAPP,etc.
            //  Auxiliares.init(applicationContext)
            Settings.setSetting(Cons.KEY_DEVICE_ADMIN_ENABLED,true)

            Log.msg(TAG,"----------------------- INICIA SERVICIO -------------")
            //Log.msg(TAG,"\n\n\n\n");
            Log.msg(TAG,"[start] -----------------------------------------------------------------------------------");
            Log.msg(TAG,"[start] -----------------------------------------------------------------------------------");
            Log.msg(TAG,"[start] currentStatus:[" + Status.currentStatus + "] restartInstall: " + Settings.getSetting("restartInstall", false))
            Log.msg(TAG,"[start] KEY_FIRST_REBOOT: "+ Settings.getSetting(Cons.KEY_FIRST_REBOOT,false))
            Log.msg(TAG,"[start] dpcValues.isProvisioning: "+ dpcValues.isProvisioning + " restartInstall: " + Settings.getSetting("restartInstall", false))
            Log.msg(TAG,"[start] ")
            //Loguea informacion del dispositivo y de Status de la App.
            logInfoDevice()

          //  sendLogs()
            Log.msg(TAG,"[start] -----------------------------------------------------------------------------------");
            Log.msg(TAG,"[start] -----------------------------------------------------------------------------------");

            //Si se reinicio, y debe continuar con el proceso de instalacion...
            if (Settings.getSetting("restartInstall", false)) {
                val provisioningManual = ProvisioningManual(context)
                GlobalScope.launch {
                    provisioningManual.continuaInstall()
                }
                return
            }

            //!!NO QUITAR!!
            //Esta validacion es para evitar que inicie la instalacion,
            //Antes de que termine la configuracion de Android.
            //Durante el provisiong, no deberia funcionar el DeviceService.
            if( ! Settings.getSetting(Cons.KEY_FIRST_REBOOT,false)){
                Log.msg(TAG,"[onCreate] <--------------------------->")
                Log.msg(TAG,"[onCreate] <---- Se sale, aun no entra el receiver. {FIRST_REBOOT} ---->")
                Log.msg(TAG,"[onCreate] <--------------------------->")

                //Revisa si ya termino el enrolamiento...
                restartEnroll()
                return
            }

            //*********************
            //Inicia la aplicacion
            scope.  launch {
                val success = withContext(Dispatchers.IO){
                    // delay(3_000)
                    dpcAplication.start()

                }
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onCreate",ex.message)
        }
    }

    private fun logInfoDevice() {
        try {
            var version = 0L
            var versionName = ""
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = packageInfo.longVersionCode
            versionName = packageInfo.versionName
            val numTel = Settings.getSetting(Cons.KEY_CURRENT_PHONE_NUMBER, "")
            Log.msg(TAG, "[onCreate] ")
            Log.msg(TAG, "[onCreate] ")
            Log.msg(TAG, "[onCreate] ")
            Log.msg(TAG, "[onCreate] ====================[ Prendio el Telefono ]===================================")
            Log.msg(TAG, "[onCreate] Equipo: " + Build.MANUFACTURER + " - " + Build.MODEL + " - [" + Build.PRODUCT + "]")
            Log.msg(TAG, "[onCreate] imei: " + DeviceInfo.getDeviceID() + " id: "+ DeviceInfo.getAndroidId(context) )
            Log.msg(TAG, "[onCreate] Android " + Build.VERSION.RELEASE + "  [ " + Build.VERSION.SDK_INT + " ]")
            Log.msg(TAG, "[onCreate] Status.currentStatus: " + Status.currentStatus)
            Log.msg(TAG, "[onCreate] Kiosko:  ${Kiosko.enabled} ["+ Kiosko.currentKiosko +"]")
            Log.msg(TAG, "[onCreate] Versión:  $versionName ($version)")

            if (!numTel!!.isEmpty()) Log.msg(TAG, "numTel: [$numTel]")
            infoKnox()
            infoXiaomi()
            Log.msg(TAG, "[onCreate] ==============================================================================")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "logInfoDevice", ex.message,false)
        }
    }

    private fun infoKnox() {
        Log.msg(TAG, "[onCreate] marca: " + Build.MANUFACTURER.uppercase(Locale.getDefault()) )
        if (!Build.MANUFACTURER.uppercase(Locale.getDefault()).contains("SAMSUNG")) {
            Log.msg(TAG, "[onCreate] No es telefono Samsung ...")
            return
        }
        try {
            //  KnoxConfig knoxConfig = new KnoxConfig(getApplicationContext());
            Log.msg(TAG, "[onCreate] knox: API: " + KnoxConfig.aPILevel + " [" + KnoxConfig.knoxVersion + "]")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "infoKnox", ex.message)
        }
    }

    private fun infoXiaomi() {
        if (!Build.MANUFACTURER.uppercase(Locale.getDefault()).contains("XIAOMI")) return
        Log.msg(TAG, "infoXiaomi: " + MIUI.version + " [" + MIUI.versionName + "] ")
    }

    fun restartEnroll(){

        try {
            val isEnrollStared = Settings.getSetting(Cons.KEY_ENROLL_STARTED,false)
            Log.msg(TAG, "[restartEnroll] currentStatus ${Status.currentStatus} isEnrollStared: $isEnrollStared")
            //Es necesario para que no entre la p
/*            if (Status.currentStatus != Status.eStatus.SinInstalar)
                return*/

            if (Status.currentStatus == Status.eStatus.RegistroEnServer ||
                Status.currentStatus == Status.eStatus.AplicoRestricciones
                || isEnrollStared){
                //Es posible que ocurrio un error o se apago el telefono en proceso de enrolamiento.
                Log.msg(TAG, "[restartEnroll] <* * * * * * * * * * * * * * * * * * * >")
                Log.msg(TAG, "[restartEnroll] <* [  REINCIAR EL PROCESO DE ENROLAMIENTO] ] * >")
                Log.msg(TAG, "[restartEnroll] <* * * * * * * * * * * * * * * * * * * >")
                reviewEnrollStatus(context)
            }

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"restartEnroll",ex.message)
        }
    }

    private fun reviewEnrollStatus(context:Context){
        try{
            //Muestra la ventana de enrolamiento...
            val intentMain = Intent(context, EnrollActivity::class.java)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            intentMain.putExtra("auto_retry",true)
            Dialogs.activarTmrActivity(context, EnrollActivity::class.java,intentMain)
            sendLogs()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"reviewEnrollStatus",ex.message)
        }
    }
    private fun sendLogs(){
        try{
            commands.sendLogs(commands.getMsgParams("0","n","n"))
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"sendLogs",ex.message)
        }
    }
}