package com.macropay.downloader.domain.usecases.provisioning


import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.*
import com.macropay.data.BuildConfig
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.usecases.EnrollDevice
import com.macropay.downloader.data.preferences.*

import com.macropay.downloader.domain.usecases.main.DPCAplication.Companion.inicializaGlobales

import com.macropay.downloader.ui.contrato.EULAActivity
import com.macropay.downloader.ui.provisioning.EnrollActivity
import com.macropay.downloader.ui.provisioning.FinalizeActivity
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.Utils
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.logs.Tracker
import com.macropay.downloader.domain.usecases.bloqueo.AppyPermissions
import com.macropay.downloader.domain.usecases.main.AppsCtrl
import com.macropay.downloader.domain.usecases.main.RestrictionCtrl
import com.macropay.downloader.domain.usecases.manual.InstallerDPC
import com.macropay.downloader.ui.manual.AdminActivity
import com.macropay.downloader.utils.activities.Dialogs
import com.macropay.utils.broadcast.Sender
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import org.json.JSONObject
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

//Llamado por:
//  DeviceAdminReceiver
//
//class Provisioning(context:Context) {
@Singleton
class Provisioning
@Inject constructor(
    @ApplicationContext val context: Context
) {


    val TAG = "Provisioning"

    //Depencencias...

/*    @Inject
    lateinit var enrollment:Enrollment

    @Inject
    lateinit var applyRestrictions: ApplyRestrictions
*/

    @Inject
    lateinit var appsCtrl: AppsCtrl

    @Inject
    lateinit var restrictionCtrl: RestrictionCtrl
    @Inject
    lateinit var appyPermissions: AppyPermissions

    @Inject
    lateinit var enrollDevice: EnrollDevice
    @Inject
    lateinit var installerDPC: InstallerDPC
    var mContext:Context

    init {
       // Log.msg(TAG,"[init] asigno el context")
        this.mContext = context
    }

    val scope = CoroutineScope(Job() + Dispatchers.Main)

    private val mUserId = 0
    var handler: Handler?


    var bundleProvision: PersistableBundle? = null
        get() {
             return field
        }
        set(field) {
            Log.msg(TAG, if ("setBundleProvision$field" == null) "= NULL" else "= OK - Not Null")
            bundleProvision = field
        }


    //----------------------------------------------------------------------------------------------------------------
    //
    //----------------------------------------------------------------------------------------------------------------
     fun start(intent: Intent) {
        Log.msg(TAG,"[start] ---- inicio ----- ")
        try {
            inicializaGlobales(mContext);
            handler = Handler(Looper.getMainLooper())
            val packageName = mContext.packageName
            val mDevicePolicyManager = mContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(packageName)
            val isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(packageName)
            Log.msg(TAG, "[start] isProfileOwner: $isProfileOwner")
            Log.msg(TAG, "[start] isDeviceOwner: $isDeviceOwner")

            //Lee los Settings que vienen el QR de Enrolamiento.

            leeQRSettings(mContext, intent)
            clearSettings()
            appyPermissions.autoGrant(mContext)

            Log.msg(TAG, "[start] isEnroladoManual: " + Settings.getSetting("isEnroladoManual", false))
            Log.msg(TAG, "[start] Manufacturer: " + Build.MANUFACTURER.uppercase(Locale.getDefault()))
            Log.msg(TAG, "[start] KEY_FIRST_REBOOT: " + Settings.getSetting(Cons.KEY_FIRST_REBOOT, false))

            //Si es SAMSUNG y es manual, necesita reinicirse la App, para que knox pueda tomar los permisos.
            if (Settings.getSetting("isEnroladoManual", false) && Build.MANUFACTURER.uppercase(Locale.getDefault()).contains("SAMSUNG")) {
                Log.msg(TAG,"[start] Enrolamiento MANUAL")

                //Reinicia la app, y en el DeviceServiceAdmin,continua con el proceso.
                Settings.setSetting("restartInstall", true)
                 handler!!.postDelayed({
                    Log.msg(TAG, "[start] RESTART APP.\n\n\n\n")
                    //  System.exit(0);
                    mDevicePolicyManager.reboot(Utils.getComponentName(mContext))
                }, 3000)
            } else {
                Log.msg(TAG,"[start] ***********************")
                Log.msg(TAG,"[start] *   Enrolamiento QR   *")
                Log.msg(TAG,"[start] ***********************")
                if(BuildConfig.isTestTCL.equals("true")){
                    Log.msg(TAG,"[start] es Tester TCL")
                    return
                }
               GlobalScope.launch {
                   val success = withContext(Dispatchers.Main){
                       if(!Dialogs.isRunning(context,AdminActivity::class.java)){
                           Log.msg(TAG,"[start] va mostrar la AdminActivity")
                           Dialogs.showActivity(context,AdminActivity::class.java)
                       }

                        Log.msg(TAG, "[start] 1.- inicia Proceso.\n\n\n\n")
                       Sender.sendEnrollProcess(context,true,200,"")
                       Log.msg(TAG, "[start] 2.- termino proceso")
                   } //Withcontest
                } //
            }

            Log.msg(TAG, "[start] 3 Termino....")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "start", ex.message)
        }
    }
    fun configEnroll(intent: Intent) {
        Log.msg(TAG,"[configEnroll] ---- inicio ----- ")
        try {
            inicializaGlobales(mContext);
            handler = Handler(Looper.getMainLooper())
            val packageName = mContext.packageName
            val mDevicePolicyManager = mContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(packageName)
            val isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(packageName)
            Log.msg(TAG, "[configEnroll] isProfileOwner: $isProfileOwner")
            Log.msg(TAG, "[configEnroll] isDeviceOwner: $isDeviceOwner")

            //Lee los Settings que vienen el QR de Enrolamiento.

            leeQRSettings(mContext, intent)
            clearSettings()

            Log.msg(TAG, "[configEnroll] isEnroladoManual: " + Settings.getSetting("isEnroladoManual", false))
            Log.msg(TAG, "[configEnroll] Manufacturer: " + Build.MANUFACTURER.uppercase(Locale.getDefault()))
            Log.msg(TAG, "[configEnroll] KEY_FIRST_REBOOT: " + Settings.getSetting(Cons.KEY_FIRST_REBOOT, false))

            //Si es SAMSUNG y es manual, necesita reinicirse la App, para que knox pueda tomar los permisos.
            if (Settings.getSetting("isEnroladoManual", false) && Build.MANUFACTURER.uppercase(Locale.getDefault()).contains("SAMSUNG")) {
                Log.msg(TAG,"[configEnroll] Enrolamiento MANUAL")

                //Reinicia la app, y en el DeviceServiceAdmin,continua con el proceso.
                Settings.setSetting("restartInstall", true)
                handler!!.postDelayed({
                    Log.msg(TAG, "[configEnroll] RESTART APP.\n\n\n\n")
                    //  System.exit(0);
                    mDevicePolicyManager.reboot(Utils.getComponentName(mContext))
                }, 3000)
            } else {
                Log.msg(TAG,"[configEnroll] ***********************")
                Log.msg(TAG,"[configEnroll] *   Enrolamiento QR   *")
                Log.msg(TAG,"[configEnroll] ***********************")
                //Tenia --> IO
                GlobalScope.launch {
                    val success = withContext(Dispatchers.Main){
                        Log.msg(TAG, "[configEnroll] 1.- inicia Proceso.\n\n\n\n")
                        Sender.sendEnrollProcess(context,true,200,"")
                        //iniciaProceso12()
                        Log.msg(TAG, "[configEnroll] 2.- termino proceso")
                    } //Withcontest
                } //
            }

            Log.msg(TAG, "[configEnroll] 3 Termino....")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "configEnroll", ex.message)
        }
    }
   fun  clearSettings(){
       try {
           Settings.setSetting(Cons.KEY_FIRST_REBOOT, false)
           Status.currentStatus = Status.eStatus.SinInstalar
           Settings.setSetting(TipoBloqueo.showBienvenida, false)
           Settings.setSetting(TipoBloqueo.showEula, false)
           Settings.setSetting(TipoBloqueo.show_kiosko, false)
           Settings.setSetting(TipoBloqueo.disable_lock_for_sim_change, false)
           Settings.setSetting(TipoBloqueo.requireSimForEnroll, false)
           Settings.setSetting(TipoBloqueo.enabledlockForRemoveSim, false)
       }catch (ex:Exception){
           ErrorMgr.guardar(TAG,"clearSettings",ex.message)
       }
   }

    //Proceso para Android 12
    fun iniciaProceso12()   {
        Sender.sendStatus("Inicializando...10%")
        try {
            Log.msg(TAG, "[iniciaProceso] --------------< 1 Registra fecha de instalacion.. >--------------------------------- ")
            if (!SettingsApp.initialized()) SettingsApp.init(mContext)

            SettingsApp.setSetting("fechaInstalacion", LocalDateTime.now())
            Sender.sendStatus("Inicializando...20%")
            //hace Grant a los permisos
            Log.msg(TAG, "[iniciaProceso] --------------< 3 Aplicando permisos. >--------------------------------- ")
           // appyPermissions.autoGrant(mContext)
            Sender.sendStatus("Inicializando...30%")
            Log.msg(TAG, "[iniciaProceso] --------------< 4 INICIALIZA EL log >---------------------------------")
            Log.init("downloader", mContext)

            //Avisa a Central
            Log.msg(TAG, "[iniciaProceso]--------------< 5 Registra en el Server. >--------------------------------- ")
            handler!!.post {
                Sender.sendStatus("Inicializando...40%")
                registraEnServer(TAG + ".iniciaProceso")
            }

            Log.msg(TAG, "[iniciaProceso]--------------< 6 Requiere permisos de GPS,< Para Android 9 >--------------------------------- ")

            //Requerir permisos que requieren ser aceptados por el usuario.
            //Sino esta habilitado el Tracking GPS, se sale...
            val enableTracking = Settings.getSetting(TipoBloqueo.disable_tracking_GPS, false)
            Log.msg(TAG, "[iniciaProceso] enableTracking: $enableTracking")
            Log.msg(TAG, "[iniciaProceso] currentStatus: " + Status.currentStatus)

            appyPermissions.getGPSPermmission(mContext)

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "iniciaProceso", ex.message)
        }
    }

    fun preInit(){
        Log.msg(TAG, "[preInit] --------------< 1 Registra fecha de instalacion.. >--------------------------------- ")
        if (!SettingsApp.initialized()) SettingsApp.init(mContext)
        SettingsApp.setSetting("fechaInstalacion", LocalDateTime.now())
        Sender.sendStatus("Inicializando...20%")
        //hace Grant a los permisos
        Log.msg(TAG, "[preInit] --------------< 3 Aplicando permisos. >--------------------------------- ")
      //  appyPermissions.autoGrant(mContext)
        Sender.sendStatus("Inicializando...30%")
        Log.msg(TAG, "[preInit] --------------< 4 INICIALIZA EL log >---------------------------------")
        Log.init("downloader", mContext)
    }
    //Inicia a aplicar las restricciones....
    fun downloadDPC(source:String)   {
        Log.msg(TAG,"[iniciar] source: [$source]")
        Sender.sendStatus("Inicializando...")
        try {
            //Avisa a Central
            Log.msg(TAG, "[iniciar]--------------< 1 Aplicando restricciones. >--------------------------------- ")
            Sender.sendStatus("Inicializando...10%")
            CoroutineScope(Dispatchers.IO).async {
                Sender.sendStatus("Inicializando.. 25%")
                installerDPC.download()
            }


        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "iniciar", ex.message)
        }
    }

    //Inicia a aplicar las restricciones....
    fun iniciar(eventMQTT:EventMQTT,source:String)   {
        Log.msg(TAG,"[iniciar] source: [$source]")
        Sender.sendStatus("Inicializando...")
        try {
            //Avisa a Central
            Log.msg(TAG, "[iniciar]--------------< 1 Aplicando restricciones. >--------------------------------- ")
            Sender.sendStatus("Inicializando...10%")
           CoroutineScope(Dispatchers.IO).async {
               Sender.sendStatus("Inicializando.. 15%")
             //  applyEnrollTest(1111)
               applyEnroll(eventMQTT,TAG+".iniciar")
           }


        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "iniciar", ex.message)
        }
    }

    suspend fun  applyEnroll(eventMQTT:EventMQTT,source:String) {
        Log.msg(TAG, "[applyEnroll] inicio - [$source]")
        Sender.sendStatus("Inicializando...20%")
        if(FinalizeActivity.fa != null){
            Log.msg(TAG,"[applyEnroll] Va cerrar FinalizeActivity")
            FinalizeActivity.fa!!.cerrar(TAG+".applyEnroll")
            Log.msg(TAG,"[applyEnroll] Cerro FinalizeActivity")
        }

        Sender.sendStatus("Inicializando...25%")
        var EULAShowed = isEULARequired(eventMQTT)
        Log.msg(TAG,"[applyEnroll] EULAShowed: "+EULAShowed)

        //Verifica si muestra EULA
        if (EULAShowed) {
            Log.msg(TAG,"[applyEnroll] MOSTRAR eula")
            scope.async {
                showEULAActivity(eventMQTT.message)
            }
        }else{
            Log.msg(TAG, "[applyEnroll]--------------------------------------------------------------- ")
            Log.msg(TAG, "[applyEnroll]-----<    2 Muestra pantalla de enrollActivity    >------------ ")
            Log.msg(TAG, "[applyEnroll]--------------------------------------------------------------- ")
            scope.async {
                Sender.sendStatus("Inicializando...30%")
                showActivity1("uno")
            }

            Log.msg(TAG,"[applyEnroll]  Aplica Restricciones de enrolamiento. ")
            Log.msg(TAG,"[applyEnroll]--+-+-+-+-+-+-+---")

            CoroutineScope(Dispatchers.IO).async {
                Sender.sendStatus("Inicializando...40%")
                val restriccion =restrictionCtrl. getRestriction(TipoBloqueo.install_bussines_apps,  eventMQTT)
                appsCtrl.downloadApps(restriccion)
            }

            Log.msg(TAG,"[applyEnroll] -+-+-+-+-+-+-+--->>> 2")

            //Ver si aun se necesita esta variable.
            Settings.setSetting("enrollamientoPendiente", false)
        }
    }
    fun isEULARequired(eventMQTT:EventMQTT):Boolean{
        Log.msg(TAG,"[isEULARequired:[isEULARequired] ")
        var EULAShowed = false
        try{
            //Verifica si  la llemada se hizo desde "FinalizeActivity" o de "EnrollActivity", para determinar si muestra o el EULA
            val source = Settings.getSetting(Cons.KEY_ENROLL_SOURCE, "FinalizeActivity")
            if (source.contains("FinalizeActivity")){
                //Verifica si existe el EULA
                EULAShowed = restrictionCtrl.existsEnabled(TipoBloqueo.showEula,eventMQTT)
            }
            else{
                //No se llamo desde el FinalizeActivity, entonces no muestra el EULA
                Settings.setSetting(TipoBloqueo.showEula,false)
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"isEULARequired",ex.message)
        }
        return EULAShowed
    }

    suspend fun showEULAActivity(jsonRestriccions: JSONObject){
        try{
            apply {
            Log.msg(TAG,"[showEULAActivity] - 1 ")
            val intentMain =  Intent(context, EULAActivity::class.java)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            intentMain.putExtra("restricciones",jsonRestriccions.toString())
           // context.startActivity(intentMain)
            Dialogs.activarTmrActivity(Dialogs.mContext,  EULAActivity::class.java,intentMain)
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"showEULAActivity",ex.message)
        }
    }

    suspend fun showActivity1(source: String){
        Log.msg(TAG,"[showActivity1] - 1 - [$source]")
        if(isShowed()){
            Sender.sendStatus("Preparando dispositivo...")
            Log.msg(TAG,"[showActivity1] YA EXISTE ACTIVA...")
            return
        }
        apply {
            val intentMain = Intent(mContext.applicationContext, EnrollActivity::class.java)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            Log.msg(TAG, "[showActivity1]  intento: " + source)
            mContext.applicationContext.startActivity(intentMain)

        }
        Sender.sendStatus("Preparando dispositivo...")
    }


    fun isShowed():Boolean{
        var isActivityShowed = false
        try{
            Log.msg(TAG,"[isShowed] -1-")
            //isActivityShowed= (  EnrollActivity.fa != null)
            isActivityShowed =   Settings.getSetting("EnrollShowed",false)
            Log.msg(TAG,"[isShowed] -1- "+isActivityShowed)
        }catch (ex:Exception){
            //  ErrorMgr.guardar(TAG,"muestraActivity",ex.message)
            isActivityShowed = false;
        }
        return isActivityShowed
    }
    fun registraEnServer(source:String)  { // this: CoroutineScope
        try{
            Log.msg(TAG,"[registraEnServer] Inicio  - source: ["+source+"]")
            GlobalScope.launch {
             //   Settings.setSetting(Cons.KEY_ID_DEVICE,DeviceCfg.getImei(context))
                DeviceInfo.setDeviceID(context)

                var result = enrollDevice.send()
                var response = result.getCompleted()
                if(response.isSucces){
                    Log.msg(TAG,"[registraEnServer] --->> SUCESS")
                    Log.msg(TAG,"[registraEnServer] --->> result : "+response.code)
                    Log.msg(TAG,"[registraEnServer] --->> result : "+response.body)
                    //Registra status...
                    Status.currentStatus = Status.eStatus.RegistroEnServer
                    Sender.sendStatus("Leyo correctamente...")

                    var jsonObject  =  JSONObject(response.body)
                    val eventMQTT = EventMQTT("bloqueo", jsonObject, false)

                    Log.msg(TAG,"----> va verificar si existe showEula -v2")
                    //Verifica si existe el EULA
                    val EULAShowed = restrictionCtrl.aplicarSingle(TipoBloqueo.showEula,eventMQTT)
                    Log.msg(TAG,"[registraEnServer] EULAShowed: "+EULAShowed)

                    if(FinalizeActivity.fa != null){
                        Log.msg(TAG,"Va cerrar FinalizeActivity")
                        FinalizeActivity.fa!!.cerrar(TAG+".registraEnServer")
                    }
                    Log.msg(TAG,"Cerro FinalizeActivity")
                    //Verifica si muestra EULA
                    if (EULAShowed) {
                        Log.msg(TAG,"[registraEnServer] MOSTRAR eula")
                        scope.async {
                            showEULAActivity(jsonObject)
                        }
                    }else{
                        Log.msg(TAG, "[registraEnServer]----------------------------------------------- ")
                        Log.msg(TAG, "[registraEnServer]-----<    2 Muestra pantalla..    >------------ ")
                        Log.msg(TAG, "[registraEnServer]----------------------------------------------- ")
                        scope.async { showActivity1("uno") }

                        Log.msg(TAG,"[registraEnServer] Aplica Restricciones")
                        Log.msg(TAG,"[registraEnServer]--+-+-+-+-+-+-+--->>> 1 >>")
                       // appsCtrl.applyBloqueoInicial(eventMQTT)
                        val restriccion =restrictionCtrl. getRestriction(TipoBloqueo.install_bussines_apps,  eventMQTT)
                        appsCtrl.downloadApps(restriccion)
                        Log.msg(TAG,"-+-+-+-+-+-+-+--->>> 2")

                        //Ver si aun se necesita esta variable.
                        Settings.setSetting("enrollamientoPendiente", false)
                    }
                }
                else
                {
                    Settings.setSetting("enrollamientoPendiente", true)
                    Log.msg(TAG,"[registraEnServer] --->> FAILED")
                    Log.msg(TAG,"[registraEnServer] --->> result : "+response.code)
                    Log.msg(TAG,"[registraEnServer] --->> result : "+response.body)
                    ErrorMgr.guardar(TAG,"registraEnServer",response.code.toString() +"\n"+response.body)
                }
            }
            Log.msg(TAG,"termino registraEnServer")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"registraEnServer",ex.message)
        }
    }

    fun notifyEndEnrollement(){
        val isEndEnrollment = Settings.getSetting(Cons.KEY_END_ENROLLMENT, false)
        Log.msg(TAG,"[notifyEndEnrollement] isEndEnrollment: $isEndEnrollment")

        //Notificar que ya termino el enrolamiento...
        if (!isEndEnrollment){
            Log.msg(TAG,"[notifyEndEnrollement] YA ESTA NOTIFICADO")
           return
        }
        try {
            Tracker.status(TAG,"notifyEndEnrollement","isEndEnrollment: ${isEndEnrollment}")
            Settings.setSetting(Cons.KEY_END_ENROLLMENT, false)

            GlobalScope.launch {
            withContext(Dispatchers.Main) {
                    Log.msg(TAG,"[notifyEndEnrollement] Va notificar ")
                  //  enrollment.endEnrolment()

                }
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"registraEnServer",ex.message)
        }
    }

    fun leeQRSettings(context: Context, intent: Intent) {
        var bundle = intent.getParcelableExtra<PersistableBundle>(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE)
        try {
            var location = intent.getBundleExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION)
            Log.msg(TAG, "[leeQRSettings] location: [" + location.toString() +"] --> 10Mar...")
            Settings.setSetting(Cons.KEY_LOCATION_DPC,location.toString())
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"leeQRSettings",ex.message)
        }

            if (bundle == null) {
            Log.msg(TAG, "[leeQRSettings] - bundle == null")
            bundle = intent.getParcelableExtra("bundleManual") //EL bundle se envia cuando es Instalacion ADB.
        }
        if (bundle == null) {
            Log.msg(TAG, "[leeQRSettings] - bundle es definido.... Tomara del ProvisitionActivity")
            bundle = bundleProvision
            if (bundle == null)
                Log.msg(TAG, "ERROR ****************** bundleProvision viene Nullo")
        }

        //
        val qrParameters = QRParameters()
        qrParameters.leeQRSettings(bundle, context)
        //
    }

    init {
        handler = Handler(Looper.getMainLooper())
    }

    suspend fun  isEnrollmentFinished():Boolean{
        Log.msg(TAG,"[isEnrollmentFinished] - 1 - "+Status.currentStatus)
        if (Status.currentStatus.ordinal >= Status.eStatus.TerminoEnrolamiento.ordinal)
        {
            Log.msg(TAG,"[isEnrollmentFinished] Enrolamiento terminado...")
            return true;
        }
        Log.msg(TAG,"[isEnrollmentFinished] - currentStatus: "+Status.currentStatus)
        when(Status.currentStatus){
            Status.eStatus.SinInstalar->{
                //Inicia el proceso de enrolamiento
                Log.msg(TAG,"[isEnrollmentFinished] -  iniciaProceso12()" )
                iniciaProceso12()
            }
            Status.eStatus.RegistroEnServer,
            Status.eStatus.AplicoRestricciones->{
                //recibio la respuesta de la API, pero no aplico las restricciones.
                //Vuelve a ejecutar el registro, para que inicie el proceso nuevamente...
                Log.msg(TAG,"[isEnrollmentFinished] -  registraEnServer()" )
                registraEnServer(TAG+".isEnrollmentFinished")
            }

/*            Status.eStatus.AplicoRestricciones->{
                //
                Log.msg(TAG,"[isEnrollmentFinished] -  AplicoRestricciones()" )
            }*/
            Status.eStatus.ConfirmoQR->{
                Log.msg(TAG,"[isEnrollmentFinished] -  revisarDlgPendientes()" )
                Dialogs.revisarDlgPendientes(TAG)
            //    notifyEndEnrollement()
            }
            else -> {}
        }
        return false;
    }

    //Revisa que las apps,esten instaladas...
   /* fun revisarBussinesApps() {
        Log.msg(TAG, "[revisarBussinesApps] ---- Revisa Apps ----v26Oct ")
        if (Status.currentStatus == Status.eStatus.Liberado) return
        //Si, puede desinstalar las apps empresariles.
        if (Settings.getSetting(TipoBloqueo.disable_uninstall_bussines_apps,false)) return

        try {
            //
            GlobalScope.launch {
                withContext(Dispatchers.Main){
                   // enrollment = inject(mContext!!).getEnrollment()
                    val eventMQTT =revisaKernelApps()
                   if (eventMQTT.message != null) {
                        //TODO: hay que revisar que estatus debe tener... LockMgr.eProcess.lock,
                       RestrictionCtrl.downloadApps(eventMQTT, LockMgr.eProcess.lock,"revisarBussinesApps")
                    }
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "revisarBussinesApps", ex.message)
        }
    }*/

/*
    fun revisaKernelApps():EventMQTT {
        Log.msg(TAG, "[revisaKernelApps]")
         var eventMQTT : EventMQTT? = null //("","",false )
        try {
            var enterpriseApps: Array<String?> = arrayOf<String?>()
            enterpriseApps = Settings.getSetting(Cons.KEY_BUSSINES_APPS, enterpriseApps)
            Log.msg(TAG, "[revisaKernelApps] enterpriseApps:  " + enterpriseApps.size)
            if (enterpriseApps != null) {
               // val httpServer = SettingsApp.getServerHttp()
                val  httpServer = com.macropay.utils.Settings.getSetting(Cons.KEY_HTTP_SERVER, Defaults.SERVIDOR_HTTP)
                Log.msg(TAG, "[revisaKernelApps] httpServer: [$httpServer]")

                //Revisa que las apps este instaladas...
                for (app in enterpriseApps) {
                    val appJson = JSONObject(app)
                    val location = httpServer + appJson.getString("location")
                    val packname = appJson.getString("packageName")

                    Log.msg(TAG, "[revisaKernelApps] packname: $packname")
                }
            }
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "revisaKernelApps", ex.message)
        }

        return eventMQTT!!
    }*/


}