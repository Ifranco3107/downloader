package com.macropay.downloader.utils.app

import android.content.pm.PackageInstaller.SessionParams
import com.macropay.data.logs.ErrorMgr
import com.macropay.utils.broadcast.Sender
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.*
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import com.macropay.data.logs.Log
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.utils.activities.Dialogs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Exception

@Singleton
class Installer
@Inject constructor(
    @ApplicationContext var ctx: Context) {
    var TAG = "Installer"
    lateinit var mContext: Context

    var instalListener: InstallerStatus? = null
    var downloadId=0L
    var packageName = ""
    var packageFile = ""
    var location= ""
    var  isInstalled =false
    var isRegistered = false
    init {
        try{
            Log.msg(TAG, "[init] ------  ------")
            this.mContext = ctx
          //registerReceiver()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "init", ex.message)
        }
    }

    private val controlReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.msg(TAG, "[onReceive] action: [$action] ")
            when (action) {
                Sender.ACTION_INSTALL_STATUS ->  installStatus(intent)
                else -> Log.msg(TAG, "DEFAULT")
            }
        }
    }

    fun setOnInstallerStatus(listener: InstallerStatus) {
        this.instalListener = listener
        //this.instalListener!!.onInstalled(0,"pacakge","downloadLocation","fileDownloaded",InstallStatus.eEstado.DescargaIncorrecta)
    }

    fun installApp(id: Long, uri: String?, packageName: String): Boolean {
        Log.msg(TAG,"[installaApp] -1-")
        var sessionId = 0;
        lateinit var session: PackageInstaller.Session
        this.downloadId = id
        this.packageName = packageName
        this.location = uri!!
        isInstalled = isAppInstalled(packageName)
        registerReceiver()
        try {
            val uriFile = Uri.parse(uri)
            if (uriFile == null) {
                Log.msg(TAG, "[installApp] NO EXISTE EL ARCHIVO A INSTALAR:")
                return false
            }
            Log.msg(TAG, "[installApp] uri: $uri")
            Log.msg(TAG, "[installApp] uriFile: ${uriFile.toString()}")
            val inputstream = mContext.contentResolver.openInputStream(uriFile)
            if (inputstream == null) {
                Log.msg(TAG, "[installApp] NO SE PUDO LEER EL ARCHIVO A INSTALAR:")
                return false
            }
            Log.msg(TAG, "[installApp] - 1 -")
            val packageInstaller = mContext.packageManager.packageInstaller
            val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
            params.setAppPackageName(packageName)
            Log.msg(TAG, "[installApp] - 2 -")
            // set params
            sessionId = packageInstaller.createSession(params)
            session = packageInstaller.openSession(sessionId)

            Log.msg(TAG, "[installApp] - sessionId: $sessionId packageName: $packageName")

            // Carga el contenido del Archivo APK
            packageFile = extractFileName(packageName)
            val out = session.openWrite(packageFile, 0, -1)
            val buffer = ByteArray(65536)

            var sizeAPK = 0
            var c: Int
            var count = 0
            while (inputstream.read(buffer).also { c = it } != -1) {
                out.write(buffer, 0, c)
                sizeAPK += c
                count++
            }

            //Instala la aplicacion.
            session.fsync(out)

            inputstream.close()
            out.close()
            //
            Log.msg(TAG, "[installApp] - termino -1- abc")
            session.commit(getInstallIntent(ctx, sessionId, id, packageName)) //Install
            session.close()
            Log.msg(TAG, "[installApp] - termino...sessionId =$sessionId")
            Dialogs.lockIfUpdate("Installer.installApp")

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "installApp", ex.message)
            Sender.sendStatus("Error al instalar."+ ex.message)
            if (session!= null)
                session.commit(intentSend(mContext, sessionId, id, packageName)) //Error
            this.instalListener!!.onError(id,packageName, PackageInstaller.STATUS_FAILURE,ex.message,InstallStatus.eEstado.NoActualizada)

        }
        return true
    }
    private fun registerReceiver() {
        Log.msg(TAG,"[registerReceiver] isRegistered: $isRegistered" )
        try {

            if(controlReceiver != null){
                Log.msg(TAG,"[registerReceiver] NO ES NULL --> ")
              //  unRegisterReceiver()
            }

            val filter = IntentFilter()
          //  filter.addAction(Sender.ACTION_DOWNLOAD_STATUS)
            filter.addAction(Sender.ACTION_INSTALL_STATUS)
            filter.addAction(Sender.ACTION_UNINSTALL_STATUS)

            ctx.applicationContext.registerReceiver(controlReceiver, filter)
            isRegistered= true
            //mContext.registerReceiver(controlReceiver, filter, "com.macropay.dpcmacro.installer",null)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "registerReceiver", ex.message)
        }
    }
 private fun unRegisterReceiver() {
        try {
            if (controlReceiver != null) {
                Log.msg(TAG, "[unRegisterReceiver] controlReceiver")
                ctx.applicationContext.unregisterReceiver(controlReceiver)
            }
        } catch (ex: Exception) {
            //  ErrorMgr.guardar(TAG,"unRegisterReceiver",ex.getMessage(),false);
        }
    }
    fun uninstallPackage(context: Context, packageName: String) {
        Log.msg(TAG, "[uninstallPackage] package:($packageName)")
        try {
            registerReceiver()
            CoroutineScope(Dispatchers.IO).launch {
                Log.msg(TAG, "[uninstallPackage] -1-")
                blockUninstall(packageName,false)
                Log.msg(TAG, "[uninstallPackage] -2-")
                delay(1_000)
                delay(1_000)

                Log.msg(TAG, "[uninstallPackage] -3-")
                val packageInstaller = context.packageManager.packageInstaller
                packageInstaller.uninstall(packageName, getInstallIntent(context, 1, 99999L, packageName)) //Uninstall
                Log.msg(TAG, "[uninstallPackage] desinstalo:($packageName)")
            }


        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "uninstallPackage", ex.message)
        }
    }
    fun blockUninstall(pkgName: String?, enabled: Boolean) {
        Log.msg(TAG, "[blockUninstall] packageName: [$pkgName] enabled: $enabled")
        try{
            var  mDevicePolicyManager = mContext!!.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            var  mAdminComponentName = DeviceAdminReceiver.getComponentName(mContext!!)
            mDevicePolicyManager.setUninstallBlocked(mAdminComponentName, pkgName, enabled)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "blockUninstall", ex.message)
        }


    }
    private fun intentSend     (context: Context, sessionId: Int, id: Long, packageName: String): IntentSender {
        Log.msg(TAG, "[createInstallIntentSender] id: $id sessionId: $sessionId packageName: [$packageName]")
         val PI_INSTALL = 3439
        var msgAction = Sender.ACTION_INSTALL_STATUS
        if (id == 99999L) msgAction = Sender.ACTION_UNINSTALL_STATUS

        val intent = Intent(context, InstallReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            this.ctx.applicationContext,
            PI_INSTALL,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return pi.intentSender
    }
   private fun getInstallIntent(context: Context, sessionId: Int, id: Long, packageName: String): IntentSender {
        Log.msg(TAG, "[getInstallIntent] id: $id sessionId: $sessionId packageName: [$packageName]")

        var msgAction = Sender.ACTION_INSTALL_STATUS
        if (id == 99999L) msgAction = Sender.ACTION_UNINSTALL_STATUS

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            Intent(msgAction),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return pendingIntent.intentSender
    }
    fun installStatus(intent: Intent)
    {
        Log.msg(TAG, "[installStatus] ---------------------------------------------------------------------")
        var statusInstall = InstallStatus.eEstado.NoActualizada
        //Este mensaje es enviado por la clase Installer.createInstallIntentSender()
        try{
            val result = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
            val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)

            when (result) {
                PackageInstaller.STATUS_SUCCESS -> {
                    Log.msg(TAG, "[installStatus] se instalo correctamente...")
                    if(isInstalled)
                        statusInstall = InstallStatus.eEstado.Actualizada
                    else
                        statusInstall = InstallStatus.eEstado.Instalada

                }
                PackageInstaller.STATUS_FAILURE,
                PackageInstaller.STATUS_FAILURE_INVALID,
                PackageInstaller.STATUS_FAILURE_CONFLICT-> {
                    statusInstall = InstallStatus.eEstado.NoCompatible
                    // INSTALL_FAILED_VERSION_DOWNGRADE
                    val errorInstall = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                    var msgError = errorInstall

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
                            val errorInstall = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                            msgError = "Error: " +errorInstall
                        }
                    }


                    //Termina el when de tipo de error.
                    //terminoInstallApp(PackageInstaller.STATUS_SUCCESS, packageName)
                    Sender.sendStatus(msgError)
                    ErrorMgr.guardar(TAG, "installStatus", "Install failed: [$errorInstall] -$msgError ")
                }
                else -> {
                    statusInstall = InstallStatus.eEstado.NoCompatible
                    val errorInstall = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                    ErrorMgr.guardar(TAG, "installStatus", "Install failed:  unknown $errorInstall")
                }
            }
            //Termina el when...
            Log.msg(TAG,"installStatus: va lanzar el onInstalled. ----->")
            if(this.instalListener == null){
                Log.msg(TAG,"[installStatus] es NULL ")
            }
            this.instalListener!!.onInstalled(this.downloadId,this.packageName,this.location,this.packageFile,statusInstall)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "installStatus", ex.message)
            this.instalListener!!.onError(this.downloadId,this.packageName,900, ex.message, statusInstall)
        }
    }
    private fun extractFileName(packageName: String): String {
        var packageFile = "filename"
        try {
            val pointpos = packageName.lastIndexOf(".")
            if (pointpos > 0) packageFile = packageName.substring(pointpos + 1)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "extratFileName", ex.message)
        }
        return packageFile
    }

    fun clean() {
        try {
            unRegisterReceiver()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG,"clean",ex.message,false)
        }
    }

    fun isAppInstalled(packageName: String?): Boolean {
        //  Log.msg(TAG,"isInstalled")
        var installed = false
        installed = try {
            val pm = mContext!!.packageManager
            val packageInfo = pm.getPackageInfo(packageName!!, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return installed
    }
}
interface InstallerStatus {

    fun onInstalled(id: Long?, packageName: String, downloadLocation:String, fileDownloaded: String?, installstatus: InstallStatus.eEstado)
    fun onError(id: Long, packageName:String, error: Int, message: String?, installstatus: InstallStatus.eEstado)
}