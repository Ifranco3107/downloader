package com.macropay.downloader.utils.app

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.net.Uri
import com.macropay.data.dto.request.PackageFile
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.usecases.SendPackageVersion
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.SettingsApp
import com.macropay.utils.broadcast.Sender
import com.macropay.utils.network.Red
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageService
@Inject constructor(
    @ApplicationContext private val ctx: Context,
    val sendPackageVersion: SendPackageVersion ) {

    var TAG = "PackageService"
    lateinit var mDevicePolicyManager: DevicePolicyManager
    lateinit var mAdminComponentName: ComponentName
    var mContext: Context? = null
    init {
        Log.msg(TAG,"- init - 07May a")
        try{
            mContext = ctx
            mDevicePolicyManager = mContext!!.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            mAdminComponentName = DeviceAdminReceiver.getComponentName(mContext!!)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "init", ex.message)
        }
    }

    fun versionCode(packageName: String?): Long {
        return try {
            // Log.msg(TAG,"versionCode")
            val pm = mContext!!.packageManager
            val packageInfo = pm.getPackageInfo(packageName!!, 0)
            packageInfo.longVersionCode
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    fun versionName(packageName: String?): String {
        return try {
            //  Log.msg(TAG,"versionName")
            val pm =  mContext!!.packageManager
            val packageInfo = pm.getPackageInfo(packageName!!, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "0"
        }
    }
    fun dpcPackageName():String{
        return mContext!!.packageName
    }

    fun dpcVersionCode():Long{
        return versionCode(dpcPackageName())

    }
    fun dpcVersionName():String{
        return versionName(dpcPackageName())

    }

    fun applicationName(packageName: String?): String {
        //  Log.msg(TAG,"applicationName")
        var appName = ""
        var ln = 0
        try {
            ln = 1
            val pm = dpcValues.mContext!!.packageManager
            ln = 2
            val applicationInfo = pm.getApplicationInfo(packageName!!, 0)
            ln = 3
            appName = (if (applicationInfo != null)
                        pm.getApplicationLabel(applicationInfo)
                        else "") as String
            ln = 4
        } catch (ex: Exception) {
         //   ErrorMgr.guardar(TAG, "applicationName [$ln]", ex.message,"packageName: "+ packageName!!)
        }
        return appName
    }

    fun isInstalled(packageName: String?): Boolean {
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


    fun isUserApp(packageName: String): Boolean {
       // Log.msg(TAG,"isUserApp")
        var bIsUserApp = false
        try {
         // Log.msg(TAG,"[isUserApp] packageName: "+packageName )
           // var ai: ApplicationInfo? = null
            //val pm = dpcValues.mContext!!.packageManager
            val pm = mContext!!.packageManager

            var ai: ApplicationInfo = pm.getApplicationInfo(packageName, 0)

            val mask = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
            bIsUserApp = ai.flags and mask == 0

            val isSysApp = ai.flags and ApplicationInfo.FLAG_SYSTEM == 1
            val isSysUpd = ai.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 1

            //val isExternalStorage = ai.flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE == 1
            // boolean isExternalStorage = (ai.flags & ApplicationInfo.FLAG_IS_GAME) == 1;
            val appName = (if (ai != null) pm.getApplicationLabel(ai) else "") as String
            bIsUserApp = !isSysApp && !isSysUpd && !appName.contains("com.")


            if (!bIsUserApp) {
                bIsUserApp = "YouTube,Chrome,Google Play Store,Mensajes,Gmail,Maps,Cámara,Reproductor de video,Galería,Teléfono".contains(appName)
            }
            //return isSysApp || isSysUpd;
            //  Log.msg(TAG,"isSysApp: "+isSysApp +" isSysUpd: "+isSysUpd +" isExternalStorage: "+isExternalStorage + " ai.category: "+ai.category);
        } catch (ex: PackageManager.NameNotFoundException) {
            ErrorMgr.guardar(TAG, "isUserApp", ex.message)
        }
        return bIsUserApp
    }

    fun isAppEmpresarial(packageName: String?): Boolean {
      //  Log.msg(TAG,"isAppEmpresarial")
        var enterpriseApps: Array<String?> = arrayOf<String?>()
        var app: String? = null
        try {
            enterpriseApps = Settings.getSetting("enterpriseApps", enterpriseApps)
            app = Arrays.stream(enterpriseApps)
                .filter { curApp: String? -> curApp!!.contains(packageName!!) }
                .findAny()
                .orElse(null)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "isAppEmpresarial", ex.message)
        }
        //Agrega la app en la relacion de apps..
        return app != null
    }

    fun tipoApp(packageName: String): Int {
        // Log.msg(TAG,"tipoApp")
        var tipo = 3 //1.-app kernel, 2.-app Negocio,3.- usuario
        try {
            if (mContext!!.packageName == packageName) tipo = 1
            if (isAppEmpresarial(packageName)) tipo = 2
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "tipoApp", ex.message)
        }
        return tipo
    }

    fun getAppVAlue(packageName: String?, variable: String?): String {
        // Log.msg(TAG,"getAppVAlue")
        var sValue = ""
        var ai: ApplicationInfo? = null
        try {
            val pm = dpcValues.mContext!!.packageManager
            //    ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            ai = pm.getApplicationInfo(packageName!!, PackageManager.GET_META_DATA)
            val value = ai.metaData[variable]
            if (value != null) sValue = value.toString()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "getAppVAlue", ex.message)
        }
        return sValue
    }

    fun laucherApp(packageName: String): Boolean {
        // Log.msg(TAG,"laucherApp")
        if (!isInstalled(packageName)) return false
        try {
            val pm = dpcValues.mContext!!.packageManager
            val packageInfo = pm.getPackageInfo(packageName, 0)
            val resolveIntent = Intent("android.intent.action.MAIN", null)
            resolveIntent.setPackage(packageInfo.packageName)
            val activities = pm.queryIntentActivities(resolveIntent, 0)
            Log.msg(TAG, "[laucherApp] Number of activities is: " + activities.size)
            if (activities.size > 0) {
                val itentActivity = activities[0]
                //  packageName = itentActivity.activityInfo.packageName;
                val className = itentActivity.activityInfo.name
                val intent = Intent("android.intent.action.MAIN")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.component = ComponentName(packageName, className)
                Log.msg(TAG, "[laucherApp] Started activity with packagename: $packageName and     classname: $className")
                mContext!!.startActivity(intent)
                Log.msg(TAG, "[laucherApp] - lanzo la app")
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "laucherApp", ex.message)
        }
        /*        Log.msg(TAG+".[laucherApp]","laucherApp: "+packageName);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(packageName,packageName+".MainActivity"));
        this.mContext.startActivity(intent);*/
        return true
    }

    fun blockUninstall(pkgName: String?, enabled: Boolean) {
        if (enabled) mDevicePolicyManager.setUninstallBlocked(mAdminComponentName, pkgName, true) else mDevicePolicyManager.setUninstallBlocked(mAdminComponentName, pkgName, false)
    }

    fun uninstall(packageName: String) {
        Log.msg(TAG, "[uninstall] ($packageName)")
        try {
            val packageInstaller = mContext!!.packageManager.packageInstaller
            //     Log.msg(TAG,"createInstallIntentSender: id: "+ id+" sessionId: " +sessionId + " packageName: ["+packageName+"]");
            val msgAction = Sender.ACTION_UNINSTALL_STATUS
            val pendingIntent = PendingIntent.getBroadcast(ctx, 1, Intent(msgAction), 0)
            packageInstaller.uninstall(packageName, pendingIntent.intentSender)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "uninstall", ex.message)
        }
    }

    fun uninstallPackage(context: Context, packageName: String?) {
        Log.msg(TAG, "[uninstallPackage] -----------------------------------")
        Log.msg(TAG, "[uninstallPackage] ($packageName)")
        val packageManger = context.packageManager
        val packageInstaller = packageManger.packageInstaller
        val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
        try {
            params.setAppPackageName(packageName)
            var sessionId = packageInstaller.createSession(params)
            val msgAction = Sender.ACTION_UNINSTALL_STATUS
            val pendingIntent = PendingIntent.getBroadcast(context, sessionId, Intent(msgAction), 0)
            packageInstaller.uninstall(packageName!!, pendingIntent.intentSender)
            /*packageInstaller.uninstall(packageName!!,
                PendingIntent.getBroadcast(mContext!!, sessionId,
                Intent("android.intent.action.MAIN"), 0).intentSender)
*/
            Log.msg(TAG, "[uninstallPackage] ***********************************************")

/*            val appPackage = "juanito.ovik.moviefinder2"
            val intent: Intent = Intent(getActivity(), getActivity().getClass())
            val sender = PendingIntent.getActivity(getActivity(), 0, intent, 0)
            val mPackageInstaller: PackageInstaller = getActivity().getPackageManager().getPackageInstaller()
            mPackageInstaller.uninstall(appPackage, sender.intentSender)*/
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "uninstallPackage", ex.message)
        }
    }

    //--
    fun uninstallPackage( packageName: String?) {
        Log.msg(TAG, "[uninstallPackage] ***********************************************")
        Log.msg(TAG, "[uninstallPackage] ($packageName)")
        val packageManger = mContext!!.packageManager
        val packageInstaller = packageManger.packageInstaller
        val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
        try {
            params.setAppPackageName(packageName)
            var sessionId = packageInstaller.createSession(params)
            val msgAction = Sender.ACTION_UNINSTALL_STATUS
            val pendingIntent = PendingIntent.getBroadcast(mContext!!, sessionId, Intent(msgAction), 0)
            packageInstaller.uninstall(packageName!!, pendingIntent.intentSender)
            /*packageInstaller.uninstall(packageName!!,
                PendingIntent.getBroadcast(mContext!!, sessionId,
                Intent("android.intent.action.MAIN"), 0).intentSender)
*/
            Log.msg(TAG, "[uninstallPackage] ***********************************************")

/*            val appPackage = "juanito.ovik.moviefinder2"
            val intent: Intent = Intent(getActivity(), getActivity().getClass())
            val sender = PendingIntent.getActivity(getActivity(), 0, intent, 0)
            val mPackageInstaller: PackageInstaller = getActivity().getPackageManager().getPackageInstaller()
            mPackageInstaller.uninstall(appPackage, sender.intentSender)*/
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "uninstallPackage", ex.message)
        }
    }


    //
fun uninstallManual(packageName: String){
    try{

        val uri: Uri = Uri.fromParts("package", packageName, null)
        val uninstallIntent = Intent(Intent.ACTION_DELETE, uri)
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext!!.startActivity(uninstallIntent)
    }catch (ex:Exception){
        ErrorMgr.guardar(TAG, "uninstallManual", ex.message)
    }
}
    fun suspendApps(arrPackages: Array<String>, mEnabled: Boolean) {
        Log.msg(TAG, "[suspendApps] mEnabled: $mEnabled")
        try {
           // for (appName in arrPackages)
           //     Log.msg(TAG, "[suspendApps] : [$appName]")

            mDevicePolicyManager.setPackagesSuspended(mAdminComponentName, arrPackages, mEnabled)
          //  Log.msg("suspendApps", "Termimo Correctamente ")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "suspendeApps", ex.message)
        }
    }
    fun suspendApp(packname:String,bEnabled: Boolean){
        Log.msg(TAG,"[suspendApp] $packname")
        try{
            val app = "$packname,"
            var apps :ArrayList<String>  = app.split(",") as ArrayList<String>
            suspendApps(apps.toTypedArray(), bEnabled)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"suspendApp",ex.message)
        }
    }
    fun lockApplication(packname:String,bEnabled: Boolean){
        Log.msg(TAG,"[lockApplication] $packname")
        try{
            val app = "$packname,"
            var appsLocked = Settings.getSetting(Cons.KEY_BLOCKED_APPS,  "")
            if(bEnabled)
                appsLocked += app
            else
                appsLocked= appsLocked.replace( app,"")
            Log.msg(TAG,"[lockApplication] appsLocked: $appsLocked")
            Settings.setSetting(Cons.KEY_BLOCKED_APPS, appsLocked)
            suspendApp(packname,bEnabled)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"lockApplication",ex.message)
        }
    }
    fun appsInstaladas(): Int {
        Log.msg(TAG,"[appsInstaladas] - inicio -")
        //PackageService packageService = new PackageService(this.mContext);
        val apps: MutableList<PackageFile> = ArrayList()
        try {
            val pm = ctx.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
            Log.msg(TAG,"[appsInstaladas] "+packages.size +" por enviar...")
            var count = 0
            for (packageInfo in packages) {
               // Log.msg(TAG,"count: "+count +" - "+packageInfo.packageName)
                try{
                    if (isUserApp(packageInfo.packageName)) {
                       // Log.msg(TAG,"packageName: "+packageInfo.packageName)
                        //String appName =  (String) (packageInfo.applicationInfo != null ? pm.getApplicationLabel(packageInfo.applicationInfo) :"" );
                        val appName = applicationName(packageInfo.packageName)
                        val tipoApp = tipoApp(packageInfo.packageName)
                        Log.msg(TAG, packageInfo.packageName + " - " + packageInfo.versionCode + " [" + packageInfo.versionName + "]")
                        apps.add(PackageFile(packageInfo.packageName, appName, packageInfo.versionCode.toLong(), packageInfo.versionName, tipoApp, 1))
                    }
                    count ++
                }catch (ex:Exception){
                    ErrorMgr.guardar(TAG,"appsInstaladas[1]",ex.message)
                }
            } //TERMINA FOR...

           sendPackageVersion.send(apps)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "appsInstaladas[2]", ex.message)
        }
        return apps.size
    }
     fun enviaApps() {
         Log.msg(TAG,"[enviaApps] isOnline: "+ Red.isOnline)
        if (!Red.isOnline) return
         Log.msg(TAG,"[enviaApps] apps: "+SettingsApp.appsUpdated.size)
        if (SettingsApp.appsUpdated.size == 0) return
        try {
            com.macropay.downloader.di.Inject.inject().getSendPackageVersion().send(SettingsApp.appsUpdated)
            SettingsApp.appsUpdated.clear()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "enviaApps", ex.message)
        }
    }
    /*fun uninstall(packageName: String, context: Context): Boolean {
        Log.d(TAG, "Uninstalling package $packageName")
        return try {
            context.packageManager.deletePackage(packageName, deleteObserver, PackageManager.DELETE_ALL_USERS)
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }*/
}
/*private class DeleteObserver : IPackageDeleteObserver {
    private TAG = "DeleteObserver"
    @Throws(RemoteException::class)
    fun packageDeleted(packageName: String?, returnCode: Int) {
        if (packageName != null) {
            Log.d(TAG, "Successfully uninstalled package $packageName")
            callback.onAppUninstalled(true, packageName)
        } else {
            Log.e(TAG, "Failed to uninstall package.")
            callback.onAppUninstalled(false, null)
        }
    }

    fun asBinder(): IBinder? {
        return null
    }
}*/

/**
 * Callback to give the flow back to the calling class.
 */
interface InstallerCallback {
    fun onAppInstalled(success: Boolean, packageName: String?)
    fun onAppUninstalled(success: Boolean, packageName: String?)
}