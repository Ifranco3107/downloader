package com.macropay.downloader.receivers

import android.content.Context
import android.content.Intent
//import com.macropay.dpcmacro.di.Inject
import com.macropay.downloader.utils.app.InstallManager
import com.macropay.downloader.utils.location.LocationDevice
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.utils.app.PackageService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : HiltBroadcasterReceiver() {


    val TAG = "AlarmReceiver"
/*    @Inject
    lateinit var restoreRestrictions: RestoreRestrictions*/

    @Inject
    lateinit var locationDevice: LocationDevice

    @Inject
    lateinit var installManager: InstallManager


    @Inject
    lateinit var packageService : PackageService
    override fun onReceive(contetx: Context, intent: Intent) {
        super.onReceive(contetx, intent)

        Log.msg(TAG, "onReceive -----------------------------------[ALARM]")
        try{
            // Is triggered when alarm goes off, i.e. receiving a system broadcast
            if (intent.action != "GPS_ACTION") {
                return;
            }
/*            monMQTT()
            sendPendingTrx()
            lastLocation(contetx)
            checkUnroll(contetx)
            checkLicensePendding()
            checkLockNotConnection()
            enviaInventario()
            checkIconDbg(contetx)
            depurarLogs(contetx)*/
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "onReceive", ex.message)
        }
     //   errorTest()

    }
   /* private fun lastLocation(context: Context) {
        Log.msg(TAG, "[lastLocation] Obtiene la posicion GPS - lastLocation")
        try {

            locationDevice.currentPos
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "lastLocation", ex.message)
        }
    }

    private fun monMQTT(){
        if(Status.currentStatus == Status.eStatus.Liberado){
            return
        }
        try {
            //Si es cambio de dia, pone el cabecero con los datos del equipo.

            if (!Red.isOnline) {
                Log.msg(TAG, "[monMQTT] prende el wifi...")
                Red.enableWifi()
            }
            if (!Red.isOnline) {
                Log.msg(TAG, "[monMQTT] sin red...")
                return
            }
            val isMQTTConnected = dpcValues.mqttAWS!!.isConnected

            Log.msg(TAG, "[monMQTT] isMQTTConnected: [$isMQTTConnected] isOnline: "+ Red.isOnline)

          if (!isMQTTConnected) {
              var ultimaConexion = Settings.getSetting(Cons.KEY_MQTT_LAST_CONECTION, LocalDateTime.now())
              var  mins  = Utils.tiempoTranscurrido(ultimaConexion, ChronoUnit.MINUTES).toInt()
              Log.msg(TAG,"[monMQTT] mins: "+ mins +" - "+ultimaConexion)
              if(mins >=60 ) {
                  Log.msg(TAG,"[monMQTT] No se ha conectado desde "+ ultimaConexion )
                  Settings.setSetting(Cons.KEY_MQTT_LAST_CONECTION, LocalDateTime.now())
                  restoreRestrictions.syncMsgs()

              }else{
                  Log.msg(TAG, "[monMQTT] Reintenta conectar el MQTT - Se forza la conexion, por si hay algun problema.")
                  dpcValues.mqttAWS!!.disconnect()
                  Thread.sleep(500)
                  dpcValues.mqttAWS!!.checkSettings()
                  dpcValues.mqttAWS!!.connect(TAG)
              }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "run: ", ex.message)
        }
    }

    private fun sendPendingTrx(){
        if (!Red.isOnline) {
            Log.msg(TAG, "[sendPendingTrx] sin red...")
            return
        }
        try{
            //Verifica si hay transacciones pendientes de enviar...
            TrxOffline.enviaTxtPendientes()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "run: ", ex.message)
        }
    }

    private fun checkUnroll(context: Context){
        try{
        if(Status.currentStatus == Status.eStatus.Liberado){
            val uninstallDPC = Settings.getSetting( TipoBloqueo.uninstall_dpc,false)
            val hideDPC = Settings.getSetting( TipoBloqueo.hide_icon_dpc,false)
            val showUnroll = Settings.getSetting( TipoBloqueo.show_unrolled,false)
            Log.msg(TAG,"[checkUnroll] uninstallDPC: "+uninstallDPC )
            Log.msg(TAG,"[checkUnroll] hideDPC: "+hideDPC )
            Log.msg(TAG,"[checkUnroll] showUnroll: "+showUnroll )

            if(showUnroll) {
                Log.msg(TAG,"[checkUnroll] pendiente de mostrar la ventana de despedida")
                return
            }

            if(uninstallDPC)
            {
                Log.msg(TAG,"[checkUnroll] DESINSTALAR...")
                uninstallApp(context)

                val handler =  Handler(Looper.getMainLooper());
                handler.postDelayed({
                        if( !Dialogs.isIconHidden(context)){
                            Log.msg(TAG,"[checkUnroll] ESCONDE ICON...")
                            Dialogs.showAppIcon(context!!,true) //Unroll -> Esconde el icono
                        }
                     },2_000 )
            }else{
               // val hideDPC = Settings.getSetting( TipoBloqueo.hide_icon_dpc,false)
                if(hideDPC && !Dialogs.isIconHidden(context))
                {
                    Log.msg(TAG,"[hideIcon] va esconder el icono")
                    Dialogs.showAppIcon(context!!,true) //Unroll -> Esconde el icono
                    Settings.setSetting(TipoBloqueo.hide_icon_dpc,false)
                }
            }
        }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "checkUnroll", ex.message)
        }

    }

    fun uninstallApp(context: Context ) {
    //    var apps: Array<String?> = arrayOf<String?>(context.packageName)
        try {
            val packageName = context.packageName
            if(isInstalled(packageName,context)){
                installManager.addPackage(packageName, "")
                //Desinstala las apps.
                Log.msg(TAG, "[uninstallApp] ... inicia ...")
                val bResult = installManager.unInstall()

            }else
                Settings.setSetting( TipoBloqueo.uninstall_dpc,false)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "uninstallApp", ex.message)
        }
    }
    fun isInstalled(packageName: String?,context: Context): Boolean {
        //  Log.msg(TAG,"isInstalled")
        var installed = false
        installed = try {
            val pm = context.packageManager
            val packageInfo = pm.getPackageInfo(packageName!!, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return installed
    }

    fun checkLicensePendding(){
        val pendding =  Settings.getSetting(Cons.KEY_LICENSE_PENDDING,false)
        Log.msg(TAG,"[checkLicensePendding] pendding: "+pendding)
        if (pendding && Red.isOnline) {
            KnoxConfig.activateLicense()
        }
    }

    fun checkLockNotConnection(){
        Log.msg(TAG,"[checkLockNotConnection] ")
        try{
            showKiosko.checkLockByConnection()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "checkLockNotConnection", ex.message)
        }
    }
    private fun enviaInventario() {
        Log.msg(TAG, "[enviaInventario]")
        if(Settings.getSetting(Cons.KEY_ENVIO_INVENTARIO,false)) return
        try {
            Log.msg(TAG, "[enviaInventario]--- INVENTARIO DE APPs INSTALADAS---")
            packageService.appsInstaladas()

            Settings.setSetting(Cons.KEY_ENVIO_INVENTARIO, true)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "enviaInventario", ex.message)
        }
    }
    private fun checkIconDbg(context: Context){
        try {
            if(Log.isDbgIconEnabled()) {
                Log.msg(TAG,"[start] mostrar icon")
                Settings.setSetting(TipoBloqueo.hide_icon_dpc,false)
                Dialogs.showAppIcon(context,false) //checkIconDbg
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "checkIconDbg", ex.message)
        }
    }

    private fun errorTest(){
        Log.msg(TAG,"errorTest -1-")
        val xxx = "dfasjdhk"
        val yyy = xxx.toInt()
        Log.msg(TAG,"errorTest -2-$yyy")
    }
    private fun depurarLogs(context: Context){
        try{
            val depurador =  Depurador(context)
            depurador.reviewLogFiles()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "checkIconDbg", ex.message)
        }
    }*/
}