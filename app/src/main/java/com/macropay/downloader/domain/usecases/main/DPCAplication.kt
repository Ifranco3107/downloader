package com.macropay.downloader.domain.usecases.main


import android.app.AlarmManager
import android.app.PendingIntent
import com.macropay.downloader.utils.location.LocationMgr

import com.macropay.downloader.utils.SettingsApp

import com.macropay.data.logs.ErrorMgr

import com.macropay.downloader.timers.TimerMonitor
import com.macropay.downloader.utils.app.PackageService
import android.net.wifi.WifiManager

import com.macropay.downloader.utils.app.InstallManager
import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.*
import com.macropay.data.di.RequestValidateServers
import com.macropay.data.dto.request.PackageFile
import com.macropay.data.preferences.Defaults
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.data.awsiot.MqttSettings
import com.macropay.downloader.data.preferences.*
import com.macropay.downloader.domain.usecases.provisioning.Provisioning
import com.macropay.downloader.receivers.*

import com.macropay.downloader.utils.Settings

import com.macropay.data.logs.Log
import com.macropay.data.logs.Tracker
import com.macropay.data.usecases.UpdateAppsStatus
import com.macropay.downloader.R

import com.macropay.downloader.utils.app.InstallStatus
import com.macropay.downloader.receivers.AlarmReceiver
import com.macropay.downloader.receivers.NetworkReceiver
import com.macropay.downloader.receivers.PackageReceiver

import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import javax.inject.Inject


class DPCAplication
@Inject constructor(
    @ApplicationContext val context: Context,
                    val packageService : PackageService,
                    val timerMonitor: TimerMonitor
) {
    var TAG = "DPCAplication"


    @Inject
    lateinit var installManager : InstallManager

    @Inject
    lateinit var requestValidateServers: RequestValidateServers
    @Inject
    lateinit var provisioning : Provisioning


    @Inject
    lateinit var updateAppStatus: UpdateAppsStatus
    //Receivers...
    private var mPackageChangedReceiver: BroadcastReceiver? = null
    private var mIdleReceiver: BroadcastReceiver? = null
    private var mNetworkReceiver: BroadcastReceiver? = null
    public var mLockReceiver: BroadcastReceiver? = null

    private var mSMSReceiver: BroadcastReceiver? = null
    //
    private var handlerService: Handler? = null
    private var locationMgr: LocationMgr? = null

    init {
        Log.msg(TAG,"[init]")
        try {
            globaltimerMonitor = timerMonitor
            dpcValues.timerMonitor = timerMonitor

        } catch (ex:Exception){
                ErrorMgr.guardar(TAG,"start 1",ex.message)
            }

    }
    suspend fun start() {
        Log.msg(TAG,"[start] -1-")
        inicializaGlobales(context)

        //
        handlerService = Handler(Looper.getMainLooper())


        //Si AUN NO termina el Enrolamiento,
        Log.msg(TAG,"[start] 1.- Verifica si no hay pendientes del enrolamiento, currentStatus: "+Status.currentStatus)
        if(!provisioning.isEnrollmentFinished()){
            Log.msg(TAG,"[start] Aun NO termina el Enrolamiento")
            return
        }

        Log.msg(TAG,"[start] -----------------------------------------------------------");
        Log.msg(TAG,"[start] 2- currentStatus: " +Status.currentStatus)
        Log.msg(TAG,"[start] -----------------------------------------------------------");
        Tracker.status(TAG,"start","currentStatus: ${Status.currentStatus}")

        //Agregado- 14Enero22 -testing
        withContext(Dispatchers.Main) {
            try{
                Log.msg(TAG,"[start] 2.5 Notifica termino de Enrolamiento. ")
                provisioning.notifyEndEnrollement()

                //ok  provisioning.revisarBussinesApps()
                //ok         provisioning.verificarKnox()
                starServices()
                //uninstallAppManual(context)
                //TODO: ver si se puedo pasar al AlarmReceiver, para optimizar el proceso de Enrolamiento...
                //enviaInventario()
            }

            //
            catch (ex:Exception){
                ErrorMgr.guardar(TAG,"start 1",ex.message)
            }

            Settings.setSetting(Cons.KEY_DPC_UPDATED,false)
        }
        Log.msg(TAG,"[start] termino proceso...")
    }



   /* private fun enviaInventario() {
        Log.msg(TAG, "[enviaInventario]")
        if(Settings.getSetting(Cons.KEY_ENVIO_INVENTARIO,false)) return
        try {
            Log.msg(TAG, "[enviaInventario]--- INVENTARIO DE APPs INSTALADAS---")
            packageService.appsInstaladas()

            Settings.setSetting(Cons.KEY_ENVIO_INVENTARIO, true)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "enviaInventario", ex.message)
        }
    }*/

    private fun starServices() {

        Log.msg(TAG, "[starServices] 1 -  >--------------------------------")
        try {

            //Si esta en modo Kiosko, retrasa el inicio de MQTT, para darle tiempo a que se reestableca el kiosko.
            Log.msg(TAG, "[starServices] 2 - Inicio Normal MQTT >--------------------------------- ")
           handlerService!!.postDelayed( {
                    initMQTT(context, TAG)
                    },5_000)

            Log.msg(TAG, "[starServices] 3 - Revisa Actualizacion del DPC -------------------------------- ")
            //Si se actualizo la App del DPC, avisa a central.
            handlerService!!.post {
                revisarUpdateUPD()
            }

            Log.msg(TAG, "[starServices] 4 - Inicializa Timers de Monitoreo -------------------------------- ")
            //Inicia Timers
/*
//Se puso con Delay, para que no detenga el proceso de enrolamiento.
 handlerService!!.post {
                        iniciarMonitores()
                        }*/

            handlerService!!.postDelayed({
                iniciarMonitores() },
                45_000)
            Log.msg(TAG, "[starServices] 5 - Registra Receivers >-[Deleyed]-------------------------------- ")
            handlerService!!.postDelayed({
                        initReceivers() },
                    10_000)
            //handlerService!!.post({ initReceivers() })
            //Log.msg(TAG, "[starServices] 6 - Location >-[Deleyed]-------------------------------- ")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "starServices", ex.message)
        }
    }

    private fun startLocation() {
        var ln = 0
        Log.msg(TAG, "[startLocation] Inicio")
        try {
            ln = 1
            locationMgr = LocationMgr(context)
            ln = 2
            locationMgr!!.requestLocationUpdates()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "startLocation [$ln]", ex.message)
        }
    }

    private fun iniciarMonitores() {
        Log.msg(TAG, "[iniciarMonitores] ---- Activa Monitores ---- currentStatus: " + Status.currentStatus)
        try {
            //Inicializa bandera de Inicio de Updater.
            SettingsApp.setinicioUpdater(null)

            Log.msg(TAG, "[iniciarMonitores] ---- habilitar  Monitor - MQTT ")
            timerMonitor.enabledMTTQ(true)
                // timerMonitor.enabledBloqueo(SettingsApp.isKiosko()); //Solo cuando este bloqueado
                //Si ya esta liberado, ya no monitorea inactividad


        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "iniciarMonitores", ex.message)
        }
    }

    private fun initReceivers() {
        Log.msg(TAG, "[5initReceivers] ---- 1 ---- ")
        try {
            registerPackageChangesReceiver()

            registerNetworkReceiver()
           //IFA- se paso al la funcion - start()
            // registerLockReceiver()

          //  registerSMS();
            //TODO
            startLocation()
            iniciarAlarm(context)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "initReceivers", ex.message)
        }
    }

    //Verifica si se actualizo el Apk de MacroLock
    private fun revisarUpdateUPD() {
        Log.msg(TAG, "[revisarUpdateUPD] Actualiza en central, la informacion de la  version DPC " )
        try {
            val packageName: String = context.getPackageName()
            val versionCode :Long = packageService.versionCode(packageName)
            val versionName = packageService.versionName(packageName)
            val appName = packageService.applicationName(packageName)
            val tipoApp = packageService.tipoApp(packageName)
            SettingsApp.appsUpdated.add(PackageFile(packageName, appName, versionCode, versionName, tipoApp, 1))

            Settings.setSetting(Cons.KEY_DPC_UPDATED,false)
            Log.msg(TAG, "[revisarUpdateUPD] va actualizar...")

            //Envia Aplicaciones de actualizados o instaladas...
            packageService.enviaApps()

            //Envia estatus de installacion
            updateStatusInstallation(packageName,versionCode)

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "revisarUpdateUPD", ex.message)
        }
    }
    private fun updateStatusInstallation(packageName: String,versionCode:Long){
        try{
            var status = InstallStatus.eEstado.Actualizada.key

            // val versionInstalled = Settings.getSetting(Cons.DPC_INSTALLED,versionCode)
            val versionInstalled = Settings.getSetting(Cons.DPC_INSTALLED,0L)
            if(versionInstalled == versionCode) {
                Log.msg(TAG, "[revisarUpdateUPD] recien installado...")
                // Settings.getSetting(Cons.DPC_INSTALLED,false)
                status = InstallStatus.eEstado.Instalada.key
            }
            //Actualiza el status de instalacion...
            updateAppStatus.send(packageName,status)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "updateStatusInstallation", ex.message)
        }

    }
    private fun registerPackageChangesReceiver() {
        Log.msg(TAG,"registerPackageChangesReceiver")
        try {
            unregisterPackageChangesReceiver()
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
            intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
            intentFilter.addDataScheme("package")
            mPackageChangedReceiver = PackageReceiver()
            context.registerReceiver(mPackageChangedReceiver, intentFilter, R.string.name_permissions_packages.toString(),null)


        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "registerPackageChangesReceiver", ex.message)
        }
    }

    private fun unregisterPackageChangesReceiver() {
        try {
            if (mPackageChangedReceiver != null) {
                context.unregisterReceiver(mPackageChangedReceiver)
                mPackageChangedReceiver = null
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "unregisterPackageChangesReceiver", ex.message)
        }
    }


    private fun registerNetworkReceiver() {
        Log.msg(TAG,"registerNetworkReceiver")
        try {
            unregisterNetworkReceiver()
            val filter = IntentFilter()
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            mNetworkReceiver = NetworkReceiver()
            context.registerReceiver(mNetworkReceiver, filter, R.string.name_permissions_network.toString(),null)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "registerNetworkReceiver", ex.message)
        }
    }

    private fun unregisterNetworkReceiver() {
        try {
            if (mNetworkReceiver != null) {
                context.unregisterReceiver(mNetworkReceiver)
                mNetworkReceiver = null
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "unregisterNetworkReceiver", ex.message)
        }
    }

    fun iniciarAlarm(context: Context) {
        Log.msg(TAG, "[iniciarAlarm] Inicializa el TimerManager,.")
        try {
            // Get AlarmManager instance
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val minuto = 60_000L
            // Intent part
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.action = "GPS_ACTION"
            intent.putExtra("KEY_TEST_STRING", "Dato pasado al onReceive()")
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            // Alarm time - Tiempo de intervalo de ejecucion.
            val timeInterval = 5 * minuto
            val alarmTime = System.currentTimeMillis() + 5_000L
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, timeInterval, pendingIntent)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"iniciarAlarm",ex.message)
        }
    }

    fun cleanup() {
        //
        Log.msg(TAG, "[cleanup] -1-")
        try {
            unregisterPackageChangesReceiver()
            Log.msg(TAG, "[cleanup] -2-")
            unregisterNetworkReceiver()
            Log.msg(TAG, "[cleanup] -3-")

            //if(dpcValues.timerMonitor != null) {
                timerMonitor.enabledKiosk(false,null,null)
                Log.msg(TAG, "[cleanup] -5-")
                timerMonitor.enabledMTTQ(false)

                Log.msg(TAG, "[cleanup] -7-")
            //}
            Log.msg(TAG, "[cleanup] - end -")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "cleanup", ex.message)
        }
    }

    private fun uninstallAppManual(context: Context) {
        Log.msg(TAG, "[uninstallAppManual]")
        try {
            if(packageService.isInstalled(Defaults.APP_MANUAL_PACKAGE) )
            {
                Log.msg(TAG,"Desinstalo: "+ Defaults.APP_MANUAL_PACKAGE)
                installManager.unInstall(Defaults.APP_MANUAL_PACKAGE)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "uninstallAppManual", ex.message)
        }
    }

    fun initMQTT(context: Context?, source: String) {
        val TAG = "DPCAplication"
        Log.msg(TAG, "[initMQTT] source:[$source]")
        try {
            MainApp.setSimpleModel(buildSimpleModel())
            Log.msg(TAG, "[initMQTT] - - - - - - - <  verifica conexion de MonMttq  > - - - - - - - - - - - - - - - -")

            var appDirectory: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            var imei = DeviceCfg.getImei(context!!)
            Log.msg(TAG,"[initMQTT] appDirectory: "+appDirectory!!.absolutePath)
            Log.msg(TAG,"[initMQTT] imei: "+imei)

            MqttSettings.topic = "lock/"+imei
            MqttSettings.clientId = imei

            MqttSettings.keyStorePath = appDirectory.absolutePath //this.filesDir.toString() + "/Pictures/"
            if(!dpcValues.mqttAWS!!.isKeystorePresent()!!){
                Log.msg(TAG,"creo el KeyStore")
                dpcValues.mqttAWS!!.saveKeyStore()
            }

            if(!dpcValues.mqttAWS!!.isConnected)
                dpcValues.mqttAWS!!.connect(TAG)

        } catch (ex: Exception) {
            //Log.msg(TAG,"Inicio Proceso MQTT - Error: "+ex.getMessage());
            ErrorMgr.guardar(TAG, "initMQTT", ex.message)
        }
    }

    fun buildSimpleModel(): String {
        var model = Build.MODEL
        try {
            model = model.replace("(", "")
            model = model.replace(")", "")
            model = model.replace(" ", "_")
            //Log.msg(TAG, "MQTT ClientID: " + model);
        } catch (ex: Exception) {
            ErrorMgr.guardar("DeviceAdminService", "buildSimpleModel", ex.message)
        }
        return model
    }
    //
    companion object {
        //Statics
        lateinit var globaltimerMonitor: TimerMonitor
        lateinit var  contextGral: Context
        lateinit var componentName :ComponentName
        lateinit var devicePolicyManager :DevicePolicyManager
        lateinit var packageName:String

        fun inicializaGlobales(context: Context) {
            Log.msg("DPCAplication","inicializaGlobales-inicio")
            try{
                dpcValues.mContext = context

             //Estas deben ir en dpcValues
            //    timerMonitor = TimerMonitor(context)

                contextGral = context
                componentName  = ComponentName(context, DeviceAdminReceiver::class.java)
                devicePolicyManager =context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                packageName = context.packageName

            }catch (ex:Exception){
                ErrorMgr.guardar("DPCAplication","inicializaGlobales",ex.message)
            }
            Log.msg("DPCAplication","inicializaGlobales-Termino")
        }
    }
}


fun componentName(): ComponentName {
    return DPCAplication.componentName
}

fun dpm(): DevicePolicyManager {
    return DPCAplication.devicePolicyManager
}
fun DPCpackageName(): String {
    //var  packageName :String = DPCAplication.contextGral.packageName
    return DPCAplication.packageName

}
    fun getTimerMonitor(): TimerMonitor {
    return DPCAplication.globaltimerMonitor
}
