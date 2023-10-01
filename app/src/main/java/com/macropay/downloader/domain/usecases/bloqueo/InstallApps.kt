package com.macropay.downloader.domain.usecases.bloqueo

import android.content.Context
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.preferences.Defaults
import com.macropay.downloader.data.preferences.Status
import com.macropay.downloader.entities.Parametro
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.app.InstallManager
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class InstallApps
@Inject constructor(
    @ApplicationContext val context: Context,
    val installManager : InstallManager)
{

    var TAG = "InstallApps"
    fun reintentar(){
        try {
            installManager.instalar()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "reintentar", ex.message)
        }
    }
    fun instalar(apps :Array<Parametro> ) {
        Log.msg(TAG, "[instalarApp] ************************************************************************")
        Log.msg(TAG, "[instalarApp] ====> statusEnroll: "+ SettingsApp.statusEnroll())
        Log.msg(TAG, "[instalarApp] ====> currentStatus: "+ Status.currentStatus )
        Log.msg(TAG, "[instalarApp] ====> Apps para instalar: " + apps.size + " apps")

        try {
            //si es status.Instalado, es porque esta en el proceso de enrolamiento,
            // y requeire reboot al terminar.
//            installManager.isReboot =  ( SettingsApp.statusEnroll() == SettingsApp.status.Enrolo )
            //TerminoEnrolamiento
            installManager.isReboot =  ( Status.currentStatus == Status.eStatus.AplicoRestricciones)
            Log.msg(TAG, "[instalarApp] ====> installManager.isReboot: " + installManager.isReboot )

            //Carga las apps para installar.
            for (app in apps) {
                //val httpServer = SettingsApp.getServerHttp()
                val httpServer = cleanURL() //Settings.getSetting(Cons.KEY_FILE_SERVER,Defaults.SERVIDOR_FILE)
                var location = httpServer + cleanFile( app.value)
                //   location = "http://52.201.234.28:3005/api/devices/mobile/apk/download?appkeymobile=CMaz3r2r23r23r23r23ssd11SWSZXWEFWWFd99s"

                location = app.value
                val packname =  app.name
                //Log.msg(TAG, "[instalarApp] httpServer: [$httpServer]")
                Log.msg(TAG, "[instalarApp] packname: $packname,location: $location")

                installManager.addPackage(packname, location)
            }

            //Instala las apps.
            // installManager.isReboot =false
            Log.msg(TAG, "[instalarApp] ... inicia la instalacion ...")
            val bResult = installManager.instalar()
            Log.msg(TAG, "[instalarApp] ... termino la instalacion ...")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "instalarApp", ex.message)
        }
    }
    fun cleanURL():String{
        var server= Settings.getSetting(Cons.KEY_FILE_SERVER, Defaults.SERVIDOR_FILE)
        if(server.endsWith("/"))
            server = server.substring(0,server.length-1)
        return server
    }
    fun cleanFile(filename:String):String{
        var file = filename
        if(!filename.startsWith("/"))
            file = "/"+filename
        return file
    }

}