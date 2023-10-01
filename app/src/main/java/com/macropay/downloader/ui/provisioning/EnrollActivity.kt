package com.macropay.downloader.ui.provisioning

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.*
import android.telephony.PhoneNumberUtils
import android.view.View
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.R

import com.macropay.downloader.data.preferences.*
import com.macropay.downloader.databinding.ActivityEnrollBinding

import com.macropay.downloader.receivers.NetworkReceiver
import com.macropay.downloader.ui.common.mensajes.ToastDPC
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.Utils
import com.macropay.downloader.utils.device.DeviceService
import com.macropay.downloader.utils.location.LocationDevice
import com.macropay.downloader.utils.samsung.KnoxConfig

import com.macropay.utils.broadcast.Sender
import com.macropay.utils.network.Red
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import com.samsung.android.knox.EnterpriseDeviceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EnrollActivity
    @Inject constructor()
    : GetEnrollRestrictions() {

    lateinit var  binding: ActivityEnrollBinding
/*    @Inject
    lateinit var endEnrollment: EndEnrollment*/
    @Inject
    lateinit var locationDevice: LocationDevice



    val handler = Handler(Looper.getMainLooper())

    val TAG = "EnrollActivity"
    var contTaps = 0
    val TAPS_REQUERIDOS= 25
    var retryIntentos =0
    private var mNetworkReceiver: BroadcastReceiver? = null

    companion object {
        lateinit var fa: EnrollActivity
    }
    private fun closePrevInstance(){
        try{
            if(fa!= null){
                Log.msg(TAG,"[closePrevInstance] va cerrar instancia anterior...")
                fa.enabledKiosk(false)
                fa.finish()
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "closePrevInstance", ex.message)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.msg(TAG, "")
        Log.msg(TAG, "")
        Log.msg(TAG, "[OnCreate]")
        Log.msg(TAG, "[OnCreate] *************************** Inicio *************************[" + SettingsApp.statusEnroll() + "]")

        binding = ActivityEnrollBinding.inflate(layoutInflater)
        val view: View = binding.getRoot()
        setContentView(view)
        Log.msg(TAG, "[OnCreate] -1-")
        try {
            closePrevInstance()

            fa = this
            MainApp.setMainCtx(this)
            MainApp.setMainActivity(this)
            Settings.setSetting("EnrollShowed",true)

            registerStatusReceiver()
            registerNetworkReceiver()
            //Caracteristicas del telefono.
            caracteristicas()

            binding.txtStatus.text = "Inicializando..."

            if(requierePermisosGPS()){
                Log.msg(TAG,"va pedir permisos de GPS.")
                binding.txtStatus.text = "Inicializando...1%"
                activeLocation()
            }

            turnScreenOnAndKeyguard()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                Log.msg(TAG, "[OnCreate] -4- setShowWhenLocked")
                binding.txtStatus.text = "Inicializando...2%"
                setShowWhenLocked(true);
            }
            this.enabledKiosk(true)
            binding.txtStatus.text = "Inicializando...3%"
            setTimeoutEvent()
            enrollStatus()
            binding!!.lytWifiOptions.visibility = View.GONE

            autoRetry()

       /*     //Todo: temporal para pruebas..===>
            if(retryIntentos==0) {

                handler.postDelayed({
                    binding.txtMarca.text = "TIMEOUT "+retryIntentos
                    Red.enableWifi(false)
                }, 10_000)
            }
            //TODO <===========
*/
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "onCreate", ex.message)
        }
        Log.msg(TAG, "[OnCreate] termino...")
    }
    private fun autoRetry(){
        val bundle = intent.extras
        var bAutoRetry = false
        try{
            Log.msg(TAG,"[autoRetry] -1- ")
            if(bundle != null){
                if(bundle!!.containsKey("auto_retry"))
                    bAutoRetry = bundle!!.getBoolean("auto_retry", false)
            }


            Log.msg(TAG,"[autoRetry] autoRetry: $bAutoRetry")
            if(bAutoRetry){
                Log.msg(TAG,"[autoRetry] va reintentar")
                if(binding.txtIMEI.text.contains("inst")){
                    Log.msg(TAG,"[autoRetry] Aun no tiene permisos de DeviceOwner")
                    binding.txtStatus.text="El telefono requiere FactoryReset"
                } else {
                    Log.msg(TAG,"[autoRetry] inicia reintento de enrolamiento. ")
                    recordFailStatus()
                    reintentar(null)
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "autoRetry", ex.message)
        }
    }
    fun recordFailStatus(){
        try{
            val imei:String = binding.txtIMEI.text.toString()
            enrollFailed.send(imei)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"recordFailStatus",ex.message)
        }

    }

    fun enrollStatus(){
        this!!.setOnDownloadStatus(  object: QueryEnrollStatus {
            override fun onSuccess(body: EventMQTT) {
                Log.msg(TAG, "[enrollStatus] onSuccess")
             //   provisioning.iniciar(body)
            }

            override fun onError(code: Int, error: String?) {
                Log.msg(TAG, "[enrollStatus] onError")
                //  showStatus("ocurrio un error...")
                binding!!.cpPbar.visibility =View.GONE
                binding!!.txtError.text = "Ocurrio un error al enrolar"
                binding!!.lytWifiOptions.visibility = View.VISIBLE
            }
        })
    }
    private fun showShutdwon(){
        try{
            Log.msg(TAG,"[showShutdwon] *********************[ -1- ]***************************************")
/*            Log.msg(TAG, "[showShutdwon]")
            val intent = Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN")
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)*/

        //    RecoverySystem.rebootWipeUserData(this);
            binding.lytWifiOptions.visibility = View.GONE
            binding.txtStatus.text = "Se requiere realizar Factory Reset\n"+
                  " - Apague el equipo. \n"+
                  " - Presione tecla [bajar volumen]+ [encendidp] por unos segundos."


            val handlerLock = Handler(Looper.getMainLooper())
            handlerLock.postDelayed(
                {
                    Log.msg(TAG,"[showShutdwon] *********************[ -2- ]***************************************")
                    enabledKiosk(false)
                    this.finish()
                    Log.msg(TAG,"[showShutdwon] *********************[ -3- ]***************************************")
                }
                , 10_000)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"showShutdwon",ex.message)
        }
    }
    fun setTimeoutEvent(){
        retryIntentos =0
        val handlerLock = Handler(Looper.getMainLooper())
        val milisegs :Long = 5 * 60_000 //5 minutos
        handlerLock.postDelayed({
            Log.msg(TAG,"[setTimeoutEvent]  listener!!.onError")
            //Si esta visible, ya no muestra, porque ya esta en proceso de recuperacion.
            if(binding.lytWifiOptions.visibility == View.GONE ){
                binding.lytWifiOptions.visibility = View.VISIBLE
                binding.cpPbar.visibility = View.GONE
                binding.btnRetry.tag =  Cons.REBOOT_EVENT
                binding.txtStatus.text = "Ocurrio un error durante el proceso..."
            }
        }, milisegs)
    }
    private fun registerStatusReceiver(){
        Log.msg(TAG,"[registerStatusReceiver]")
        try{
            val filter = IntentFilter()
            filter.addAction(Sender.ACTION_STATUS_CHANGE)
            filter.addAction(Sender.ACTION_HTTP_ERROR)
            filter.addAction(Sender.ACTION_STATUS_ENROLL)
            filter.addAction(Sender.ACTION_STATUS_NETWORK)
           // registerReceiver(mStatusReceiver, filter)
            registerReceiver(mStatusReceiver, filter, "com.macropay.downloader.enrollstatus",null)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"registerStatusReceiver",ex.message)
        }
    }

    private fun registerNetworkReceiver() {
        Log.msg(TAG,"registerNetworkReceiver")
        try {
            unregisterNetworkReceiver()
            val filter = IntentFilter()
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            mNetworkReceiver = NetworkReceiver()
            this.registerReceiver(mNetworkReceiver, filter, R.string.name_permissions_network.toString(),null)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "registerNetworkReceiver", ex.message)
        }
    }

    private fun unregisterNetworkReceiver() {
        try {
            if (mNetworkReceiver != null) {
                this.unregisterReceiver(mNetworkReceiver)
                mNetworkReceiver = null
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "unregisterNetworkReceiver", ex.message)
        }
    }
    private fun requierePermisosGPS():Boolean {
        var bResult= false
        try {
            val params = intent.extras
            bResult = params!!.getBoolean("requierePermisosGPS", false)
        } catch (ex: Exception) {
           // ErrorMgr.guardar(TAG, "requierePermisosGPS", ex.message)
        }
        return bResult
    }

    @SuppressLint("SetTextI18n")
    private fun caracteristicas() {
        try {

            binding.txtMarca.text = Build.MANUFACTURER
            if(Build.MANUFACTURER.contains("samsung") && KnoxConfig.aPILevel == EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_NOT_SUPPORTED){
                binding.txtMarca.text = Build.MANUFACTURER + " - Knox unavailable"
                Log.msg(TAG, "knox: NO SOPORTADO")
            }
            binding.txtModelo.text = Build.MODEL + " - " + Build.PRODUCT
            binding.txtAndroidVersion.text = "Android " + Build.VERSION.RELEASE + "  [ " + Build.VERSION.SDK_INT + " ]"
            //Version del DPC
            val packageInfo = this.packageManager.getPackageInfo(packageName, 0)
            val version = packageInfo.longVersionCode
            val versionName = packageInfo.versionName
            binding.txtVersion.text = "$versionName ($version)"
            binding.txtIMEI.text = DeviceInfo.getDeviceID() // DeviceCfg.getImei(this)
            val numTel = Settings.getSetting(Cons.KEY_CURRENT_PHONE_NUMBER, "")
            Log.msg(TAG, "numTel: [$numTel]")
            if (!numTel.isEmpty()) {
                //Log.msg(TAG , DeviceCfg.getCountryId(this,0);
                binding.txtNumTel.visibility = View.VISIBLE
                binding.txtNumTel.text = PhoneNumberUtils.formatNumber(numTel, "+52", "mx")
            } else
                binding.txtNumTel.visibility = View.GONE

            binding.txtStatus.text = "Inicializando...30%"
            if (Settings.getSetting("restartInstall", false)) binding.btnCancelar.visibility = View.VISIBLE
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "caracteristicas", ex.message)
        }
        Log.msg(TAG, "caracteristicas- termino...")
    }

    fun showStatus(status: String?) {
        try {
            this@EnrollActivity.runOnUiThread(Runnable {
                binding.txtStatus.text = status

                if(binding.txtIMEI.text.contains("inst"))
                    binding.txtIMEI.text = DeviceCfg.getImei(this)
            })
        } catch (ex:Exception) {
            ErrorMgr.guardar(TAG, "showStatus", ex.message)
        }
    }
    fun addWifi(view: View?) {
        dpcValues.timerMonitor!!.enabledKiosk(false,null,null)
        try {
            //
            DeviceService.configWifi(this)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "addWifi", ex.message)
        }
    }
    fun apagar(view: View?){
        ToastDPC.showToast(this,"Apaga el equipo, para Factory Reset")
        showShutdwon()
    }
    fun reintentar(view: View?) {
        retryIntentos++
        try {
            if(binding.txtIMEI.text.contains("inst")){
                //Si aun no tiene permisos de Owner, require factory reset.
                ToastDPC.showToast(this,"Apaga el equipo, para Factory Reset")
                showShutdwon()
                return
            }

            if(!Red.isOnline){
                ToastDPC.showToast(this,"No hay conexión de Red")
            }
            Log.msg(TAG, "[reintentar] ================================================================")
            Log.msg(TAG, "[reintentar] ===[     var iniciar el reintento de enrolamiento.          ]===")
            Log.msg(TAG, "[reintentar] ================================================================")
            binding!!.cpPbar.visibility =View.VISIBLE
            binding!!.lytWifiOptions.visibility = View.GONE

            //Ejecuta el enrolamiento
            queryRestrictions(TAG)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "reintentar", ex.message)
        }
    }
/*    fun retryDownload(view: View?) {
        try {
            binding.lytWifiOptions.visibility = View.GONE
            binding.cpPbar.visibility = View.VISIBLE
            var downloadId :Long=0
            Log.msg(TAG,"[retryDownload]  binding.btnRetry.tag: "+ binding.btnRetry.tag)
            if(binding.btnRetry.tag != null)
                downloadId = binding.btnRetry.tag as Long

            Log.msg(TAG,"[retryDownload] downloadId: ["+downloadId  +"]  retryIntentos: [$retryIntentos]")

            //Reintenta el proceso completo
            if(downloadId == Cons.REBOOT_EVENT || retryIntentos>4){
                showStatus("Reintentando el enrolamiento...")
                Log.msg(TAG,"[retryDownload] REBOOT")
              //  reboot()
                //provisioning.registraEnServer("retryDownload")
                queryRestrictions()
                return
            }

            retryIntentos++
            //Reintenta la descarga...
            if(downloadId >0 ){
                Log.msg(TAG,"[retryDownload] envio mensaje a Installer.")
                showStatus("Reintentando el instalacion...[$downloadId]")
                Sender.sendDownloadStatus(downloadId, "", "STATUS_FAILED", "ERROR_CANNOT_RESUME", 1008)
            }

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "retryDownload", ex.message)
        }
    }*/
    private val mStatusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Sender.ACTION_STATUS_CHANGE ->
                    try{
                        val msg = intent.getStringExtra("msg")
                        Log.msg(TAG,"[mStatusReceiver] msg --> $msg ")
                        if (msg!!.contains(Cons.TEXT_VA_REINICIAR)) {
                            enabledKiosk(false)
                        }

                        //Pide permisisos...
                        if (msg.contains(Cons.TEXT_GPS_PERMSIONS)) {

                            Log.msg(TAG, "[mStatusReceiver] RECIBIO MSG PARA PERDIR PERMISOS...")
                            activeLocation()
                            return
                        }
                        //
                        showStatus(msg)

                    }catch (ex:Exception ){
                        ErrorMgr.guardar(TAG,"mStatusReceiver",ex.message);
                    }
                Sender.ACTION_STATUS_ENROLL,
                Sender.ACTION_HTTP_ERROR->{
                    try{
                        binding.lytWifiOptions.visibility = View.VISIBLE
                        binding.cpPbar.visibility = View.GONE
                        val code = intent.getIntExtra("code",-1)
                        val message = intent.getStringExtra("msg")
                        val process = intent.getStringExtra("process")
                        val idDownload :Long = intent.getLongExtra("id",0)

                        //
                        Log.msg(TAG,"[mStatusReceiver] ACTION_STATUS_ENROLL code: $code message: $message process: $process id:$idDownload")

                        var statusRed = "Sin Red"
                        if(Red.isOnline)
                            statusRed = Red.getSSID(context) // +" - " + Red.ConnectionQuality(context)


                        binding.txtStatus.text =statusRed
                        binding.txtError.text = message


                        //Notifica a Central, que hubo error...
                        enrollFailed.send(DeviceInfo.getDeviceID())

                    }catch (ex:Exception ){
                        ErrorMgr.guardar(TAG,"[mStatusReceiver] ACTION_STATUS_ENROLL",ex.message);
                    }

                }
                Sender.ACTION_STATUS_NETWORK->{
                    Log.msg(TAG,"Status NetWork. ACTION_STATUS_NETWORK" )
                     try{

                        val bEnabled = intent.getBooleanExtra("enabled",false)
                        if(bEnabled){
                            showStatus("Conectado a:\n"+ Red.getSSID(context))
                            binding.btnWifi.visibility= View.GONE
                            binding.btnRetry.visibility= View.VISIBLE
                        }
                        else {
                            if(binding!!.lytWifiOptions.visibility == View.GONE)
                                binding!!.lytWifiOptions.visibility = View.VISIBLE

                            showStatus("Sin conexión")
                            binding.btnWifi.visibility = View.VISIBLE
                            binding.btnRetry.visibility= View.GONE
                        }
                    }catch (ex:Exception ){
                        ErrorMgr.guardar(TAG,"showSystemUI",ex.message);
                    }
                }
              /*  Sender.ACTION_HTTP_ERROR ->
                {

                    //13:42:23 |[Sender]| [sendHttpError] *****************************************
                    //13:42:23 |[Sender]| [sendHttpError] errorCode: [501] error: [KPE: Network disconnected] url:[knox]
                    //13:42:23 |[Sender]| [sendHttpError] *****************************************
                    //13:42:23 | LicenseReceiver | [onReceive]  [1] errorMessage: KPE: Network disconnected
                    //13:42:23 | EnrollActivity | [mStatusReceiver] ACTION_HTTP_ERROR
                    //13:42:23 | EnrollActivity | [mStatusReceiver] code: 501 message: KPE: Network disconnected url: knox segs: 0
                    //

                    Log.msg(TAG,"[mStatusReceiver] ACTION_HTTP_ERROR")
                    val code = intent.getIntExtra("errorCode",-1)
                    val message = intent.getStringExtra("error")
                    val url = intent.getStringExtra("url")
                    val segs = intent.getIntExtra("segs",0)
                    Log.msg(TAG,"[mStatusReceiver] code: $code message: $message url: $url segs: $segs")
                }*/
                else -> {
                    Log.msg(TAG,"[mStatusReceiver] indefinido: "+intent.action)
                }
            }
        }
    }

    fun cancelarInstManual(view: View?) {
        Sender.sendRemoteCommand(this, 1, "1", "2")
    }
/*    fun reintentar(view: View?) {
        try {
            val  source = binding.btnReintentar.tag.toString()
            Log.msg(TAG, "reintentar MANUALMENTE**: ["+source+"]")

            when(source){
                "knox"->{
                    Log.msg(TAG,"[forceKnox] va volver a activar licencia...")
                    KnoxConfig.activateLicense()
                }
                "download_paused"-> {
                    Log.msg(TAG, "pausado")
                    Settings.setSetting(Cons.KEY_CANCEL_DOWNLOAD,true)
                }
                "download_failed"-> {
                    Log.msg(TAG, "fallo.")
                    Settings.setSetting(Cons.KEY_CANCEL_DOWNLOAD,true)
                }
                else->{
                    binding.txtStatus.text= "Leyendo configuración..."
                    provisioning.start(intent)
                }
            }
            binding.btnReintentar.visibility = View.GONE
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "restart", ex.message)
        }
    }*/



    private fun activeLocation() {
        Log.msg(TAG, "Obtiene la posicion GPS - lastLocation")
        try {
            if (Utils.SDK_INT == Build.VERSION_CODES.P) {
               // val locationDevice = LocationDevice(this)
                locationDevice.createLocationRequest_P(this, TAG+".activeLocation")
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "lastLocation", ex.message)
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.msg(TAG, "==================================================")
        Log.msg(TAG, "onActivityResult - requestCode: $requestCode")
        when (requestCode) {
            Cons.REQUEST_CHECK_SETTINGS -> {
                Log.msg(TAG, "REQUEST_CHECK_SETTINGS: resultCode; $resultCode")
                when (resultCode) {
                    RESULT_OK -> {
                        Log.msg(TAG, "OK - Acepto los permisos de GPS - DISALLOW_CONFIG_LOCATION true")
                        permissionsAcceptedGPS()
                        //No se necesita,
                        // restrinctions.setRestriction(UserManager.DISALLOW_CONFIG_LOCATION,true)
                    }
                    RESULT_CANCELED -> {
                        // The user was asked to change settings, but chose not to
                        Log.msg(TAG, "Cancelo - No Activo Location- Vuelve hacer la peticion de permisos.")
                        activeLocation()
                    }
                    else -> {}
                }
            }
            else -> {}
        }
    }*/

   /* fun permissionsAcceptedGPS() {
        Log.msg(TAG, "permissionsAcceptedGPS()")
        try {
            SettingsApp.setGPSPermissionEnabled(true)

*//*            if(!SettingsApp.isEnrolmentProcess()){
                Log.msg(TAG,"No esta en proceso de enrolamiento, No es necesario Reboot.")
                return
            }*//*
            if(requierePermisosGPS()){
                Log.msg(TAG,"No esta en proceso de enrolamiento, No es necesario Reboot.")
                enabledKiosk(false)
                this.finish()
            }else
            {
                Log.msg(TAG,"En proceso de enrolamiento, Verifica si ya es posible hacer el  Reboot.")
                val handlerPolicy = Handler(Looper.getMainLooper())
                handlerPolicy.post {
                    endEnrollment.terminaInstalacion(TAG)
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "permissionsAcceptedGPS", ex.message)
        }
    }*/

    //textVersion
    fun testerSalir(view: View?) {
        contTaps++
        if (contTaps < TAPS_REQUERIDOS) return
        contTaps = 0
        showEditDialog(1)
    }

    fun testRemoveSec(view: View?) {
        contTaps++
        if (contTaps < TAPS_REQUERIDOS) return
        contTaps = 0
        showEditDialog(2)
    }

    fun forceKnox(view: View?) {
        Log.msg(TAG,"[forceKnox] -----------------------------------------")
        Log.msg(TAG,"[forceKnox] status:" +Status.currentStatus)
        var requireLicense =   Settings.getSetting(Cons.KEY_ES_LICENCIA_REQUERIDA, false)
        var isEnableFRP = Settings.getSetting(TipoBloqueo.disable_recovery_wipe_data,false)
        Log.msg(TAG,"[forceKnox] requireLicense: "+requireLicense)
        Log.msg(TAG,"[forceKnox] isEnableFRP: "+isEnableFRP)

        if(Status.currentStatus != Status.eStatus.TerminoEnrolamiento ){
            Log.msg(TAG,"----> aui debef fsdfjlsjl fsdj fra")

        }
        ToastDPC.showToast(this,"activate Knox")
        Log.msg(TAG,"[forceKnox] isLicensedActived: "+KnoxStatus.isLicensedActived)

     KnoxConfig.deactivateLicense()

        val handlerPolicy = Handler(Looper.getMainLooper())
        handlerPolicy.postDelayed( {
            Log.msg(TAG,"[forceKnox] va volver a activar licencia...")
            KnoxConfig.activateLicense()
        },1000)


    }

    override fun onDestroy() {
        super.onDestroy()
        //handlerTimeout.removeMessages(Cons.MSG_ENROLL_TIMEOUT);
        unregisterNetworkReceiver()
    }
    override fun onBackPressed() {
     //   super.onBackPressed()
        Log.msg(TAG,"[onBackPressed] esta bloqueado...")
     return
    }
}