package com.macropay.downloader.domain.usecases.manual

import android.content.Context
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.preferences.Defaults
import com.macropay.downloader.data.preferences.TipoParametro
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.Utils
import com.macropay.downloader.utils.app.InstallManager
import com.macropay.downloader.utils.app.PackageService
import com.macropay.downloader.utils.app.UpdateSystem
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import javax.inject.Inject

class InstallerDPC
@Inject constructor(@ApplicationContext val  context: Context,
                                      val updateSystem : UpdateSystem
)  {

    var TAG = "InstallDPC"
    //@Inject
  //  lateinit var packageService : PackageService
    fun download(): Boolean = runBlocking {
        try {
/*            val restrictioms = msg.toString()
            Log.msg(TAG, "[download] restrictioms: $restrictioms")
            Settings.setSetting(Cons.KEY_RESTRICTIONS,restrictioms)*/

            var apksJson =  getApkJson() //msg.message.getString("data").replace("\\", "")
            CoroutineScope(
                Dispatchers.Main).launch {
                // launch {
                Log.msg(TAG, "[download] inicio  updateSystem.instalarApp: ${apksJson}")
                val success = withContext(Dispatchers.IO) {
                    updateSystem.instalarApp(apksJson)
                }
                Log.msg(TAG, "[download] termino updateSystem.instalarApp")

            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "download", ex.message)
        }
        return@runBlocking true
    }
     fun getApkJson():String{
         val packname = Settings.getSetting(Cons.KEY_PACKAGENAME_DPC,Defaults.DPC_PACKAGENAME )
         val location = Settings.getSetting(Cons.KEY_LOCATION_DPC,Defaults.DPC_LOCATION)
         val apps = JSONArray()
         var apk: JSONObject
         Log.msg(TAG,"[getApkJson] packname: $packname")
         Log.msg(TAG,"[getApkJson] location: $location")
         apk = JSONObject()
         apk.put("download_location",location)
         apk.put("package_name",packname)
         apk.put("accion","instalar")
         apps.put(apk)
        return  apps.toString()
     }
        fun uninstallDPC(): Boolean {
            var bResult = false
            try {
                //if (Utils.isDeviceOwner(context)   ) {
                    updateSystem.uninstall(Defaults.DPC_PACKAGENAME)
                    bResult = true
                //}
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "uninstallDPC", ex.message)
            }
            return bResult
        }
    fun uninstallDownloader(): Boolean {
        var bResult = false
        try {
            //if (Utils.isDeviceOwner(context)   ) {
            updateSystem.uninstall(this.context.packageName)
            bResult = true
            //}
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "uninstallDownloader", ex.message)
        }
        return bResult
    }
}