package com.macropay.downloader.receivers



import android.content.Context
import android.content.Intent
import com.macropay.downloader.utils.app.PackageService
import com.macropay.data.dto.request.PackageFile
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.Utils
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.data.preferences.TipoBloqueo
import com.macropay.downloader.utils.Settings
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PackageReceiver : HiltBroadcasterReceiver() {
    private var mContext: Context? = null

    @Inject
    lateinit var packageService : PackageService

    override  fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent) // <- Esto es importante para que haga la Inject de sendSIM
        //Log.msg(TAG,"[onReceive]")
        mContext = context
        val action = intent.action

        try {
            val packageName = getPackageNameFromIntent(intent)
            //Log.msg(TAG,"[onReceive] packageName: "+packageName  + " action: "+action)
            var accion = 0
            val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
            if (replacing) accion = 1 else {
                if (Intent.ACTION_PACKAGE_ADDED == action) accion = 1
                if (Intent.ACTION_PACKAGE_REMOVED == action) accion = 0
            }

            //Busca el APK, en el lista actual.
            var packageUpdated = SettingsApp.appsUpdated.stream()
                .filter { curPackage: PackageFile -> packageName == curPackage.packageName }
                .findAny()
                .orElse(null)
            if (packageUpdated == null) {
                val versionCode = packageService!!.versionCode(packageName)
                val versionName = packageService!!.versionName(packageName)
                val appName = packageService!!.applicationName(packageName) //<-- al Desinstalarla aplicacion, marca error, porque ua no existe la aplicacion.
                val tipoApp = packageService!!.tipoApp(packageName!!)
                packageUpdated = PackageFile(packageName, appName, versionCode, versionName, tipoApp, 1)
                SettingsApp.appsUpdated.add(packageUpdated)
                Log.msg(TAG, "Agrego: $packageName [$appName]")

                if(packageUpdated.status==1)
                    suspendApkBackList(packageUpdated.packageName)
            } else {
                // Log.msg(TAG,"Actualizo: "+ packageName);
                packageUpdated.status = accion
            }
        } catch (e: Exception) {
            Utils.lastPackage = ""
            ErrorMgr.guardar(TAG, "onReceive", e.message)
        }
    }

    //Suspender aplicaciones de la lista negra
    private fun suspendApkBackList(packageName: String){
        try {
            var paquetesString = ""
            //verificar si la funcionalidad esta habilitada
            val enabledBlackApps = Settings.getSetting(TipoBloqueo.black_list_apps,false)
            if (enabledBlackApps) {
                paquetesString = Settings.getSetting(Cons.KEY_BLACK_APPS,  "")
            }
            val appsLocked = Settings.getSetting(Cons.KEY_BLOCKED_APPS,  "")
            paquetesString+=appsLocked
            if(paquetesString.isEmpty()){
                Log.msg(TAG,"[suspendApkBackList] funcionalidad no habilitada.")
                return
            }
            var appsBlack: ArrayList<String> = paquetesString.split(",") as ArrayList<String>
            //normalizar los nombres packageName blackList
            appsBlack.map { it.trim() }

            //verificar si el paquete instalado esta en la lista negra
            if (appsBlack.isNotEmpty()){
                val paquete =  appsBlack.find { elemento -> elemento == packageName }
                paquete?.let {
                    Log.msg(TAG,"[suspendApkBackList] paquete encontrado: $it")
                    packageService.suspendApp(it,true)
                }
            }

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "[suspendApkBackList]", ex.message)
        }
    }
    private fun getPackageNameFromIntent(intent: Intent): String? {
        return if (intent.data == null) {
            null
        } else intent.data!!.schemeSpecificPart
    }

    companion object {
        private const val TAG = "PackageReceiver"
    }
}