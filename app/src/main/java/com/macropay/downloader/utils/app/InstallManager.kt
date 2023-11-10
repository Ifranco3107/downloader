package com.macropay.downloader.utils.app

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import android.view.View
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.preferences.Defaults
import com.macropay.data.usecases.UpdateAppsStatus
import com.macropay.downloader.data.mqtt.dto.ApkInfoRequest
import com.macropay.downloader.data.preferences.TipoBloqueo
import com.macropay.downloader.domain.usecases.manual.TransferCtrl
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.policies.Restrictions
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallManager
    @Inject constructor(@ApplicationContext var context: Context,
                        val packageService : PackageService,
                        val downloader: DownloaderTemp,
                        val installer:Installer) {

    var TAG = "InstallManager"
    @Inject
    lateinit var restrinctions: Restrictions
    @Inject
    lateinit var transferCtrl: TransferCtrl
    private val appsToInstall: MutableSet<ApkInfoRequest?> = HashSet()
    private var mReboot = false

    var restorePolicyInstallApps = false
    var restorePolicyUnknowResources = false
    init {
        Log.msg(TAG,"[init]")
        donwloadStatus()
        installerStatus()
    }


    @Inject
    lateinit var updateAppStatus: UpdateAppsStatus

    var isReboot: Boolean
    get() = mReboot
    set(bReboot) {
        Log.msg(TAG, "[set] setReboot: $bReboot")
        mReboot = bReboot
    }

    fun appsToInstall(): Int {
        return appsToInstall.size
    }

    //
    fun addPackage(packageName: String, location: String) {
        Log.msg(TAG,"[addPackage] packageName: "+packageName)
        try{
            val exists =  appsToInstall.find { it!!.packageName.equals(packageName) }
            if(exists == null){
                //Log.msg(TAG,"[addPackage] packageName: NO EXISTE EN LA LISTA")
                if(context.packageName.equals(packageName)) {
                    Log.msg(TAG,"[addPackage] dpcLocation: "+location)
                    Settings.setSetting(Cons.KEY_LOCATION_DPC,location)
                }
                appsToInstall.add(ApkInfoRequest(packageName, location))
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"addPackage",ex.message)
        }
    }

    //Instala las aplicaciones.
    fun instalar(): Boolean {
        Log.msg(TAG, "[instalar] - Iniciar")
        if(Settings.getSetting(TipoBloqueo.disable_install_apps,false)) {
            Log.msg(TAG,"[instalar] va quitar permisos de disable_install_apps..")
            restorePolicyInstallApps = true
            restrinctions.setRestriction(UserManager.DISALLOW_INSTALL_APPS, false)
       }

        if(Settings.getSetting(TipoBloqueo.disable_install_unknown_sources,false)) {
            Log.msg(TAG,"[instalar] va quitar permisos de disable_install_unknown_sources..")
            restorePolicyUnknowResources = true
            restrinctions.setRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    restrinctions.setRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, false)

        }

        Log.msg(TAG, "[instalar] apps para instalar: " + appsToInstall.size + " this.isReboot: " + isReboot)
        downloader!!.setReboot(isReboot)
        val bResult = false
        try {
            //Instala las apps
            for (app in appsToInstall) {
                try {
                    Log.msg(TAG, "[instalar] startDownload: " + app!!.packageName + " - " + app.downloadLocation)
                    downloader!!.download(app)
                    if(downloader!!.hasError){
                        Log.msg(TAG,"[} ocurrio Error")
                        break
                    }

                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "instalar", ex.message)
                }
            }
            //
            Log.msg(TAG, "[instalar] TERMINO DE DESCARGAR "+appsToInstall.size +" apps" )
            //TODO: Quitar, solo debe ir cuando ya termino la insalacion. terminoInstallApp [378]
            appsToInstall.clear()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "instalar", ex.message)
        }
        return bResult
    }
fun unInstall2(packageName: String){
    Log.msg(TAG, "[unInstall2] packageName: [$packageName]")
    installer.uninstallPackage(this.context,packageName)
}

    fun unInstall(packageName: String): Boolean {
        Log.msg(TAG, "[unInstall] packageName: [$packageName]")
        try {
            //packageService.mContext = this.context
           // val packageService = PackageService(context)
            //Remueve permisos para que se pueda deinstalar la aplicacion
            packageService.blockUninstall(packageName,false)

            //Espera 1 segundo, para darle tiempo a remover permisos.
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                Log.msg(TAG, "[unInstall] desinstalando...: [$packageName]")
               packageService.uninstallPackage(this.context,packageName)
            }, 1000)


            //remuve de la lista de apps. para evitar que se trate de reinstalar.
            removeoAppsEmpresa(packageName)
            Log.msg(TAG, "[unInstall] termino...")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "unInstall", ex.message)
            return false
        }
        return true
    }

     fun unInstall(): Boolean {
         Log.msg(TAG, "[unInstall]  ${appsToInstall.size} para desinstalar")
         try {
             for (app in appsToInstall) {
                 unInstall(app!!.packageName)
             }
         } catch (ex: Exception) {
             ErrorMgr.guardar(TAG, "unInstall", ex.message)
             return false
         }
         return true
     }

    fun downloadStatusEvent(id: Long?, packageName: String, fileDownloaded: String?, status: String?, razon: String?, razonid: Int){
        try {
            Log.msg(TAG, "[downloadStatusEvent] id: $id packageName: [$packageName] fileDownloaded: [$fileDownloaded] status: [$status] razon: [$razon] razonid: [$razonid]")
            if (status == "STATUS_FAILED") {
                //if (razonid == 404) {
                if(razonid>=400 && razonid<=500 ){
                    Log.msg(TAG, "[downloadStatus] **** NO EXISTE EL ARCHIVO EN CENTRAL. ***")
                   //TODO:04Abril2023 ---A terminoInstallApp(razonid, packageName, intent)
                } else {
                    Log.msg(TAG, "[downloadStatus] **** Reintentando descargar... ***")
                    //TODO: Deberia volver a ejecucar
                    //instalar()
                    retryDownload(id!!)
                }
            } else {
                Log.msg(TAG, "[downloadStatus] ============[ Termino de descargar ]==========================" + packageName.isEmpty())
                if (!packageName.isEmpty()) {
                    Log.msg(TAG, "[downloadStatus] Instala la app: [$packageName]")

                    //Si se actualizo el DPC, lo registra, para que se avise a central.
                    setDPCStatus(packageName)

                    //Instala la aplicacion.
                    installer!!.installApp(id!!, fileDownloaded, packageName)
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "downloadStatus", ex.message)
        }
    }
    fun retryDownload(id:Long){
        Log.msg(TAG, "[downloadStatus] **** Reintentando descargar... ***")
        downloader!!.intento = 0
        var app = getAppInfo(id)

        if (app == null) {
            appsToInstall.add(app)
            app = downloader!!.currentRequest!!
            Log.msg(TAG,"No encontro la app. ")
        }
        Log.msg(TAG,"[downloadStatus] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
        Log.msg(TAG,"[downloadStatus] va volver a descargar la app: ["+app.packageName +"]")
        Log.msg(TAG,"[downloadStatus] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
        downloader!!.download(app)
    }

    //
    fun terminoInstallApp( packageName: String?) {
    //Marca como Instalada la Aplicacion..
    Log.msg(TAG,"[terminoInstallApp] Termino de instalar las apps.")
    try {
        setAsInstalled(packageName)
        //   String packageName = intent.getStringExtra("packageName");
        var bAllInstalled = false
        bAllInstalled = areAllAppsInstalled()
        Log.msg(TAG, "[terminoInstallApp] [$packageName] bAllInstalled: $bAllInstalled isReboot(): $isReboot")
        Log.msg(TAG,"[terminoInstallApp] -1- bAllInstalled: "+bAllInstalled)
        if (!bAllInstalled) {
            Log.msg(TAG,"[terminoInstallApp] -1- Aun no termina de descargar apps. ")
            return
        }

        //Asigna permisos para no desInstalar.
        if (Settings.getSetting(TipoBloqueo.disable_uninstall_bussines_apps, false)) {
            //Bloquea que se pueda desinstalar.
            bloqueaUninstall(packageName)
        }


        //- - - - - - - - - - - - - - - -
        // termino de instalar...
        //- - - - - - - - - - - - - - - -
        Log.msg(TAG,"[terminoInstallApp] -2- Termino de instalar la app. ")


        //Limpia la lista de apps para install.
        appsToInstall.clear()
        restoreRestrictionUnknowResources()
        clean("terminoInstallApp")
        //--
        val handlerLock = Handler(Looper.getMainLooper())
        handlerLock.postDelayed({
            Log.msg(TAG,"[terminoInstallApp]  va tranferir control")
            transferCtrl.transfer("com.macropay.dpcmacro")
        }, 3_000)


        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "terminoInstallApp", ex.message)
        }
    }
    private fun restoreRestrictionUnknowResources(){
        var ln = 0
        try{
            ln =1
            if(restorePolicyUnknowResources) {
                Log.msg(TAG,"[terminoInstallApp] -3- va quitar permisos de disable_install_unknown_sources..")
                restorePolicyUnknowResources = true
                ln =2
                restrinctions.setRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, true)
                ln =3
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    ln =4
                    restrinctions.setRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, true)
                }
            }
        } catch (ex: Exception) {
             ErrorMgr.guardar(TAG, "restoreRestrictionUnknowResources [$ln]", ex.message)
         }
    }
    //
    private fun setAsInstalled(packageName: String?) {
        try {
/*            val packageUpdated = appsToInstall.stream()
                .filter { curPackage: ApkInfoRequest? -> packageName == curPackage!!.packageName }
                .findAny()
                .orElse(null)
            if (packageUpdated != null) {
                Log.msg(TAG, "Actualizo: $packageName como instalada, ok")
                packageUpdated.isInstallCompleted = true
            }*/
            Log.msg(TAG,"[setAsInstalled] buscar [$packageName]")
            appsToInstall.forEach{
                Log.msg(TAG,"[setAsInstalled] [" + it!!.packageName +"] "+it!!.isInstallCompleted)
                //if(! it!!.packageName.equals(packageName)) {
                if( it!!.packageName ==(packageName)) {
                    Log.msg(TAG,"[setAsInstalled] encontro...")
                    it!!.isInstallCompleted= true
                }
                Log.msg(TAG,"[setAsInstalled] -- termino -- ");
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "setAsInstalled", ex.message)
        }
    }

    private fun areAllAppsInstalled(): Boolean {
        var bResult = true
        try {
/*            val packageUpdated = appsToInstall.stream()
                .filter { curPackage: ApkInfoRequest? -> !curPackage!!.isInstallCompleted }
                .findAny()
                .orElse(null)
            if(packageUpdated== null){
                Log.msg(TAG,"[areAllAppsInstalled] packageUpdated== null");
            }*/

            appsToInstall.forEach{
                Log.msg(TAG,"[areAllAppsInstalled] " + it!!.packageName +" "+it!!.isInstallCompleted)
                if(! it!!.isInstallCompleted) {
                    bResult = false
                }
            }
          //  bResult = packageUpdated == null
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "areAllAppsInstalled", ex.message)
        }
        return bResult
    }
    //Agrega la aplicacion a la lista de Aplicaciones de Negocio
    private fun addToAppsEmpresa(packageName: String, location: String) {
        if(packageName.equals(context.packageName)) {
            Log.msg(TAG,"[addToAppsEmpresa] NO agregue la app Macrolock")
            return
        }
        var location = location
       var httpServer = com.macropay.utils.Settings.getSetting(Cons.KEY_HTTP_SERVER, Defaults.SERVIDOR_HTTP)

        location = location.replace(httpServer, "")
        Log.msg(TAG, "[addToAppsEmpresa] packageName: $packageName location: $location")
        var enterpriseApps: Array<String?> = arrayOf<String?>()
        enterpriseApps = Settings.getSetting(Cons.KEY_BUSSINES_APPS, enterpriseApps)
        val app = Arrays.stream(enterpriseApps)
            .filter { curApp: String? -> curApp!!.contains(packageName) }
            .findAny()
            .orElse(null)

        //Agrega la app en la relacion de apps..
        if (app == null) {
            val postData = JSONObject()
            try {
                val newApps = arrayOfNulls<String>(enterpriseApps.size + 1)
                for (i in enterpriseApps.indices) {
                    newApps[i] = enterpriseApps[i]!!.replace("\\", "")
                   // Log.msg(TAG, "[addToAppsEmpresa] enterpriseApps[i]; " + enterpriseApps[i])
                }
                Log.msg(TAG, "[addToAppsEmpresa] -1- newApps.length: " + newApps.size)
                //Agrega la app nueva.
                postData.put("packageName", packageName)
                postData.put("location", location)
                newApps[newApps.size - 1] = postData.toString()
                Settings.setSetting(Cons.KEY_BUSSINES_APPS, newApps)
                //Log.msg(TAG,"-------- KEY_BUSSINES_APPS --------")
                //Log.msg(TAG, "[addToAppsEmpresa] -1- newApps.length: " + newApps.size)

/*                newApps.forEach {
                    Log.msg(TAG, "app: "+ it!!) }*/
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "addToAppsEmpresa", ex.message)
            }
        }
    }

    private fun removeoAppsEmpresa(packageName: String) {
        Log.msg(TAG, "[removeoAppsEmpresa] packageName: $packageName")
        try {
            var enterpriseApps: Array<String?> = arrayOf<String?>()
            enterpriseApps = Settings.getSetting(Cons.KEY_BUSSINES_APPS, enterpriseApps)
            val newApps = enterpriseApps.filter { it -> !it!!.contains(packageName) }
            if (newApps != null) {
                Log.msg(TAG, "newApps: " + newApps.toString() + " - " + newApps.count())
                for (i in newApps) {
                    Log.msg(TAG, i)
                }
                Settings.setSetting(Cons.KEY_BUSSINES_APPS, newApps.toTypedArray())
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "removeoAppsEmpresa", ex.message)
        }
    }

   /* private fun getPackage(referebceId: Long): String {
        var packageName = ""
        try {
            val packageDownloaded = appsToInstall.stream()
                .filter { curPackage: ApkInfoRequest? -> curPackage!!.downloadId == referebceId }
                .findAny()
                .orElse(null)
            if (packageDownloaded != null) {
                packageName = packageDownloaded.packageName
            } else {
                Log.msg(TAG, "[getPackage] ***** packageDownloaded == null ***** - referebceId: $referebceId")
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "getPackage", ex.message)
        }
        return packageName
    }*/

    private fun getAppInfo(referebceId: Long): ApkInfoRequest? {
        var appInfo: ApkInfoRequest? = null
        Log.msg(TAG, "[getAppInfo] referebceId: [$referebceId] size: ${appsToInstall.size}")
        appsToInstall.forEach({Log.msg(TAG,it!!.packageName)})
        try {
            val packageDownloaded = appsToInstall.stream()
                .filter { curPackage: ApkInfoRequest? -> curPackage!!.downloadId === referebceId }
                .findAny()
                .orElse(null)

            if (packageDownloaded != null) {
                appInfo = packageDownloaded
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "getAppInfo", ex.message)
        }
        return appInfo
    }

    fun bloqueaUninstall(packageName: String?) {
        Log.msg(TAG, "bloqueaUninstall")
        if (packageName == null) return
        try {
          //  val packageService = PackageService(context)
            packageService.blockUninstall(packageName, true)

            //BlockUninstall blockUninstall = new BlockUninstall(this.mContext);
            Log.msg(TAG, "bloqueaUninstall: $packageName")
            //blockUninstall.block(packageName);
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "bloqueaUninstall", ex.message)
        }
    }

    //Si se actualizo el DPC, lo registra, para que se avise a central.
    private fun setDPCStatus(packageName: String) {
        Log.msg(TAG, "[setDPCStatus] packageName: [$packageName]")
        try {
            //  Log.msg(TAG, "mContext.getPackageName(): ["+mContext.getPackageName() +"]" );
            //Si se esta actualizando el DPC, avisa a central la nueva version.
            if (context.packageName == packageName) {
                Log.msg(TAG, "Se actualizo.. DPC")
                //SettingsApp.setDPCUpdated(true)
                Settings.setSetting(Cons.KEY_DPC_UPDATED,true)

            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "setDPCStatus", ex.message)
        }
    }

    protected fun finalize() {
        //Log.msg(TAG, "[finalize]")
        try {
            clean("finalize")
        //    super.finalize()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "finalize", ex.message)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    fun clean(source:String) {
        Log.msg(TAG, "[clean]  source:["+source+"]")
        try {
          //  unRegisterReceiver()
            if (installer != null) installer!!.clean()
           // if (downloader != null) downloader!!.clean()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "clean", ex.message)
        }
    }

   /* private fun registerReceiver() {
        Log.msg(TAG,"[registerReceiver] - a -bRegister: $bRegister" )
        try {
            bRegister = true
            //Limpia si existe un receiver anterior.
            //unRegisterReceiver()

            val filter = IntentFilter()
            filter.addAction(Sender.ACTION_DOWNLOAD_STATUS)
            filter.addAction(Sender.ACTION_INSTALL_STATUS)
            //    context.registerReceiver(controlReceiver, filter)
           context.registerReceiver(controlReceiver, filter, "com.macropay.dpcmacro.installer",null)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "registerReceiver", ex.message)
        }
    }*/

/*    private fun unRegisterReceiver() {
        try {
            if (controlReceiver != null) {
                Log.msg(TAG, "[unRegisterReceiver] controlReceiver")
                context.unregisterReceiver(controlReceiver)
                controlReceiver = null
            }
        } catch (ex: Exception) {
            //  ErrorMgr.guardar(TAG,"unRegisterReceiver",ex.getMessage(),false);
        }
    }*/


    //Eventos de descarga...
    fun donwloadStatus(){
        downloader!!.setOnDownloadStatus(  object: DownloadStatus {
            override fun onDownloaded(id: Long?, packageName: String, fileDownloaded: String?, status: String?, razon: String?, razonid: Int, installstatus: InstallStatus.eEstado) {
                Log.msg(TAG,"[onDownloaded] : id: $id $packageName  ${installstatus.name} $fileDownloaded , $status , $razon -$razonid-")
                try{
                    if (id != null &&id<0L ) { return }
                    if(installstatus ==  InstallStatus.eEstado.DescargaCompleta)
                        downloadStatusEvent(id,packageName,fileDownloaded,status,razon,razonid)

                    //Notifica estatus
                    Log.msg(TAG,"[onDownloaded] status: [${installstatus.name}] key: ${installstatus.key}")
                    updateAppStatus.send(packageName,installstatus.key)
                }catch (ex:Exception){
                  ErrorMgr.guardar(TAG,"DownloadStatus.onDownloaded",ex.message)
                }
            }

            override fun onError(success: Boolean, error: String?, packageName: String, installstatus: InstallStatus.eEstado) {
                Log.msg(TAG,"[onError] +++++++++++++++++++++++++++++++++++++++++++++")
                try{
                    Log.msg(TAG,"[onError] onError: $success $error $packageName  ${installstatus.name} ")
                    updateAppStatus.send(packageName,installstatus.key)
                }catch (ex:Exception){
                    ErrorMgr.guardar(TAG,"DownloadStatus.onError",ex.message)
                }
            }
        })
    }

    //EVentos de Instalacion...
    fun installerStatus(){
        installer!!.setOnInstallerStatus(  object: InstallerStatus {
            override fun onInstalled(id: Long?, packageName: String, downloadLocation:String, fileDownloaded: String?, installstatus: InstallStatus.eEstado) {
                Log.msg(TAG,"[onInstalled]: id:$id $packageName  ${installstatus.name} $fileDownloaded ")
                try{
                    if (id != null &&id<0L ) { return }
                    //Agrega a la lista de apps. esto sirve para monitorearlas y evitar su desinstalacion.
                    addToAppsEmpresa(packageName!!, downloadLocation)
                    terminoInstallApp( packageName)
                    Log.msg(TAG,"[onInstalled] status: [${installstatus.name}] key: ${installstatus.key}")
                    updateAppStatus.send(packageName,installstatus.key)
                }catch (ex:Exception){
                    ErrorMgr.guardar(TAG,"installerStatus.onInstalled",ex.message)
                }
            }

            override fun onError(id: Long, packageName:String, error: Int, message: String?, installstatus: InstallStatus.eEstado) {
                Log.msg(TAG,"[onError] +++++++++++++++++++++++++++++++++++++++++++++")
                try{
                    Log.msg(TAG,"[onError] onError: $id $error $message")
                    updateAppStatus.send(packageName,installstatus.key)
                }catch (ex:Exception){
                    ErrorMgr.guardar(TAG,"installerStatus.onError",ex.message)
                }
            }

        })
    }
}








/*  fun installStatus(result:Int, packageName:String,message: String)
  {
      //Este mensaje es enviado por la clase Installer.createInstallIntentSender()
      try{
*//*            val result = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
            val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)*//*
            Log.msg(TAG, "[installStatus] result: [$result] packageName: [$packageName]")
            val errorInstall = message //intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
            var msgError = message
            when (result) {
                PackageInstaller.STATUS_SUCCESS -> {
                    Log.msg(TAG, "[installStatus] se instalo correctamente...")
                    terminoInstallApp(result, packageName)
                }
                PackageInstaller.STATUS_FAILURE,
                PackageInstaller.STATUS_FAILURE_INVALID,
                PackageInstaller.STATUS_FAILURE_CONFLICT-> {

                    // INSTALL_FAILED_VERSION_DOWNGRADE


                    //Este error sucede cuando ya existe una version Superior instalada.
                    //INSTALL_FAILED_INVALID_APK
                    //INSTALL_FAILED_INVALID_APK
                    when(errorInstall){
                        "INSTALL_FAILED_VERSION_DOWNGRADE" -> {
                            msgError = "Error ya existe una version superior."
                        }
                        "INSTALL_FAILED_INTERNAL_ERROR"->{
                            //Error generado en Telefonos XIAOMI -- [INSTALL_FAILED_INTERNAL_ERROR: Permission Denied]
                            msgError = "Este error es porque MIUI esta activo [Permission Denied]"
                        }
                        "INSTALL_FAILED_UPDATE_INCOMPATIBLE"->{
                            // Package com.grupomacro.macropay signatures do not match previously installed version; ignoring!
                            msgError = "Firma digital incompatible"
                        }
                        "INSTALL_FAILED_INVALID_APK"->{
                            // Archivo APK dañado.
                            msgError = "Archivo de instalacion dañado"
                        }
                        else -> {

                            msgError = "Error: " +errorInstall
                        }
                    }

                    //Termina el when de tipo de error.
                    terminoInstallApp(PackageInstaller.STATUS_SUCCESS, packageName)
                    Sender.sendStatus(msgError)
                    ErrorMgr.guardar(TAG, "installStatus", "Install failed: [$errorInstall] -$msgError ")
                }
                else -> {
                    ErrorMgr.guardar(TAG, "installStatus", "Install failed:  unknown $errorInstall")
                }
            }
            //Termina el when...

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "installStatus", ex.message)
        }
    }*/