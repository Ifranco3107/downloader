package com.macropay.downloader.ui.manual

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import android.telephony.TelephonyManager
import android.view.View
import android.widget.Toast
import com.macropay.data.BuildConfig
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.preferences.Defaults
import com.macropay.downloader.Auxiliares
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.R
import com.macropay.downloader.databinding.ActivityAdminBinding
import com.macropay.downloader.di.Inject.inject
import com.macropay.downloader.domain.usecases.manual.InstallerDPC
import com.macropay.downloader.domain.usecases.manual.TransferCtrl
import com.macropay.downloader.receivers.NetworkReceiver
import com.macropay.downloader.ui.common.mensajes.ToastDPC
import com.macropay.downloader.ui.provisioning.GetEnrollRestrictions
import com.macropay.downloader.ui.provisioning.QueryEnrollStatus
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.Utils
import com.macropay.downloader.utils.device.DeviceService
import com.macropay.downloader.utils.policies.FactoryReset
import com.macropay.utils.broadcast.Sender
import com.macropay.utils.logs.Log
import com.macropay.utils.network.Red
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class AdminActivity
@Inject constructor(): GetEnrollRestrictions(), View.OnClickListener {
    lateinit var  binding: ActivityAdminBinding
    private val TAG = "AdminActivity"

    @Inject
    lateinit var transferCtrl: TransferCtrl

    val DEVICE_ADMIN_ADD_RESULT_ENABLE = 1238
    var contTimeout = 0
    var ctx :Context? = null

    var bCerrando = false

    var contTaps = 0
    val TAPS_REQUERIDOS = 15
    private var mNetworkReceiver: BroadcastReceiver? = null
    @Inject
    lateinit var installerDPC: InstallerDPC
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Auxiliares.init(applicationContext)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        val view: View = binding.getRoot()
        setContentView(view)
        try{
            this.ctx = this
            if(Utils.isDeviceOwner(this))
            {
                avoidSystemError()
            }

            caracteristicas()
            binding.txtStatus.text = "Requiriendo permisos..."
            loadSettings()
            listeners()
           // iniciar()
            initProcess()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onCreate",ex.message)
        }
    }
    private fun listeners() {
        try{
            binding!!.btnRestart.setOnClickListener(this)
            binding!!.btnDescargar.setOnClickListener(this)
            binding!!.txtAndroidVersion.setOnClickListener(this)
            binding!!.txtStatus.setOnClickListener(this)
            if(BuildConfig.isTestTCL =="true"){
                binding!!.btnRestart.visibility =  View.GONE
                binding!!.btnUninstall.visibility =  View.GONE
                binding!!.btnTransfer.visibility =  View.GONE
                binding!!.btnTestService.setOnClickListener(this)
                binding!!.btnTestFRP.setOnClickListener(this)
                isFRPEnabled()
            }else{
                binding!!.btnTestService.visibility =  View.GONE
                binding!!.btnTestFRP.visibility =  View.GONE
                binding!!.btnUninstall.setOnClickListener(this)
                binding!!.btnTransfer.setOnClickListener(this)
            }
            if( Settings.getSetting(Cons.KEY_IS_SERVICE_RUNNING,false)){
                binding!!.txtMarca.text = "Servicio Corriendo OK"
            }else
            {
                binding!!.txtMarca.text = "Servicio no se levanto correctamente"
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"listeners",ex.message)
        }
    }
    private fun loadSettings(){/**/
        try{
            Settings.setSetting(Cons.KEY_APPLICATIVE,"downloader")
            Settings.setSetting(Cons.KEY_SUBSIDIARY,"macropay")
            Settings.setSetting(Cons.KEY_EMPLOYEE,"134567")
            Settings.setSetting(Cons.KEY_ENROLL_SOURCE,"manual")
            Settings.setSetting(Cons.KEY_HTTP_SERVER, Defaults.SERVIDOR_HTTP2_DEV)
            Settings.setSetting(Cons.KEY_HTTP_SERVER_PKG, Defaults.SERVIDOR_HTTP_PKG)
            Settings.setSetting(Cons.KEY_HTTP_SERVER_RPT, Defaults.SERVIDOR_HTTP_RPT)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"loadSettings",ex.message)
        }
    }
    private fun initProcess(){
        try{
           // com.macropay.data.logs.Log.msg(TAG,"[onCreate] bateria $battery% - mayor de $levelMinimo%")
            enrollStatus()
            receiverStatus()
            registerNetworkReceiver()
            setTimeoutEvent()

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"initProcess",ex.message)
        }
    }
/*    fun iniciaEnroll(v:View?){
        Log.msg(TAG,"leerRestricciones")
        try{
            val handlerLock = Handler(Looper.getMainLooper())
            handlerLock.postDelayed(
                {
                    Settings.setSetting(Cons.KEY_ENROLL_STARTED,true)
                    binding.txtStatus.text= "Leyendo configuración..."
                    Log.msg(TAG,"[leerRestricciones] inicia enrolamiento...")

                    //Inicia el enrolamiento....
                    provisioning.configEnroll(intent)

                }, 2_000)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"leerRestricciones",ex.message)
        }
    }*/
    fun enrollStatus(){
        this!!.setOnDownloadStatus(  object: QueryEnrollStatus {
            override fun onSuccess(body: EventMQTT) {
                com.macropay.data.logs.Log.msg(TAG, "[enrollStatus] onSuccess")
                showStatus("Leyo correctamente...")
                // provisioning.iniciar(body,TAG +".enrollStatus")
            }

            override fun onError(code: Int, error: String?) {
                com.macropay.data.logs.Log.msg(TAG, "[enrollStatus] onError")
                //  showStatus("ocurrio un error...")
                binding!!.cpPbar.visibility =View.GONE
                binding!!.txtError.text = "Ocurrio un error al enrolar"
                binding!!.lytWifiOptions.visibility = View.VISIBLE

                //Notifica a Central, que hubo error...
                enrollFailed.send(DeviceInfo.getDeviceID())
            }
        })
    }
    fun setTimeoutEvent(){
        //  retryIntentos =0
        val handlerLock = Handler(Looper.getMainLooper())
        val milisegs :Long = 5 * 60_000 //5 minutos
        handlerLock.postDelayed({
            com.macropay.data.logs.Log.msg(TAG,"[setTimeoutEvent]  listener!!.onError")
            //Si esta visible, ya no muestra, porque ya esta en proceso de recuperacion.
            if(binding.lytWifiOptions.visibility == View.GONE ){
                binding.lytWifiOptions.visibility = View.VISIBLE
                binding.cpPbar.visibility = View.GONE
                binding.btnRetry.tag =  Cons.REBOOT_EVENT
                binding.txtStatus.text = "Ocurrio un error durante el proceso..."
            } }, milisegs)
    }
    fun receiverStatus(){
        try{
            val filter = IntentFilter()
            filter.addAction(Sender.ACTION_STATUS_CHANGE)
            filter.addAction(Sender.ACTION_HTTP_ERROR)
            filter.addAction(Sender.ACTION_STATUS_ENROLLMENT)
            filter.addAction(Sender.ACTION_STATUS_NETWORK)

            //
            filter.addAction(Sender.ACTION_START_UPDATER)
            filter.addAction(Sender.ACTION_END_UPDATER)
            registerReceiver(mStatusReceiver, filter, "com.macropay.downloader.enrollstatus",null)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"receiverStatus",ex.message)
        }
    }

    /* private val mStatusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
         override fun onReceive(context: Context, intent: Intent) {
             if (!Sender.ACTION_STATUS_CHANGE.equals(intent.action)) {
                 return
             }
             val msg = intent.getStringExtra("msg")
             //Si ya le otorgaron permisos via ADB
             Cons.DPC_INSTALLED
             if (msg!!.contains(Cons.TEXT_RECIBIO_OWNER)) {
                 Log.msg(TAG, "RECIBIO PERMISOS DE owner...<---- ")
                 Toast.makeText(context, "Permisos recibidos...", Toast.LENGTH_LONG)
                 showImei()
                 enrolar()
             }
             if (msg.contains(Cons.TEXT_TRANSFERIO_OWNER)) {
                 Log.msg(TAG, "---> TRANSFIRIO PERMISOS DE owner...")
                 val handlerLock = Handler(Looper.getMainLooper())
                 handlerLock.postDelayed({ super@AdminActivity.finish() }, 1000)
             }
             showStatus(msg)
         }
     }*/
    private val mStatusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try{
                when (intent.action) {
                    Sender.ACTION_STATUS_CHANGE ->
                        try{
                            val msg = intent.getStringExtra("msg")
                            showStatus(msg)
                        }catch (ex:Exception ){
                            ErrorMgr.guardar(TAG,"mStatusReceiver",ex.message);
                        }
                    Sender.ACTION_HTTP_ERROR ->
                    {
                        val code = intent.getIntExtra("errorCode",400)
                        val message = intent.getStringExtra("error")
                        val url = intent.getStringExtra("url")
                        ToastDPC.showToast(context,"Error: "+code+"\n "+message)
                        val msg = "Error: "+code+"\n "+message
                        showStatus(msg)
                        binding!!.lytWifiOptions.visibility = View.VISIBLE
                        binding.btnRetry.visibility = View.VISIBLE
                        binding.cpPbar.visibility = View.GONE
                    }
                    //Este mensaje es enviado por el DeviceReceiver, cuado se asigne el DeviceOwner.
                    Sender.ACTION_STATUS_ENROLLMENT->{
                        com.macropay.data.logs.Log.msg(TAG,"[mStatusReceiver] ACTION_STATUS_ENROLLMENT")
                        showStatus("Leyendo configuración...")
                        provisioning.preInit()
                        queryRestrictions(TAG)
                    }
                    Cons.TEXT_RECIBIO_OWNER->{
                        Log.msg(TAG, "RECIBIO PERMISOS DE owner...<---- ")
                        Toast.makeText(context, "Permisos recibidos...", Toast.LENGTH_LONG)
                        showImei()
                        enrolar()
                    }
                    Cons.TEXT_TRANSFERIO_OWNER->{
                        Log.msg(TAG, "---> TRANSFIRIO PERMISOS DE owner...")
                        val handlerLock = Handler(Looper.getMainLooper())
                        handlerLock.postDelayed({
                            super@AdminActivity.finish()
                                                }, 1000)
                    }
                    Sender.ACTION_END_UPDATER -> {
                        val status = intent.getStringExtra("status")
                        Log.msg(TAG, "[ACTION_END_UPDATER] status: $status")

                        if (status.equals("1")){
                            Log.msg(TAG, "[ACTION_END_UPDATER] desinstallar")
                            //sendDPCVersion(context);
                            desinstallar()
                            var handler = Handler(Looper.getMainLooper())
                            handler!!.postDelayed({
                                Log.msg(TAG, "[ACTION_END_UPDATER] exitprocess")
                                exitProcess(0)
                            }, 3_000)

                        }
                    }
                    else -> {
                        com.macropay.data.logs.Log.msg(TAG,"indefinico: "+intent.action)
                    }
                }
            }catch (ex:Exception){
                ErrorMgr.guardar(TAG,"mStatusReceiver",ex.message)
            }
        }
    }

/*    private fun iniciar() {
        try {
            val bIsAdmin= Utils.isAdmin(ctx!!) //Boolean = mDpm.isAdminActive(mAdminComponent)
            val bIsOwner: Boolean = Utils.isDeviceOwner(ctx!!)
            Log.msg(TAG, "[iniciar] bIsAdmin: $bIsAdmin bIsOwner: $bIsOwner")
            //Verifica si no es Admin
            Log.msg(TAG, "[iniciar] ya tiene permisos... de admin, en espera de owner.( via ADB)")
            if (Utils.isDeviceOwner(ctx!!)) {
                Log.msg(TAG, "[iniciar] NO ES OWNER, INICIALIZA COUNTER")
                startCountDown(10, "iniciar")
            } else {
                Log.msg(TAG, "[iniciar] INICIALIZA INSTALAR...")
                showImei()
                val desinstalo: Boolean = installerDPC. uninstallDPC()
                val delay = (if (desinstalo) 3000 else 500).toLong()
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    Log.msg(TAG, "Instalo...")
                    enrolar()
                }, delay)
            }
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "iniciar", ex.message)
        }
    }*/
    override fun onClick(v: View?) {
        try {
            when (v!!.id) {
                R.id.btnRestart -> {
                    //No se usa..
                    //reqOwner( )
                    showDlgRequiereOwner()
                }
                R.id.btnDescargar -> {
                    enrolar()
                }
                R.id.txtAndroidVersion -> {
                   // showDlgRequiereOwner()
                }
                R.id.txtStatus -> {
                    showDlgRequiereOwner()
                }
                R.id.btnUninstall->{
                    desinstallar()
                }
                R.id.btnTransfer ->{
                    transfer()
                }

                R.id.btnTestService ->{
                    if(!Utils.isDeviceOwner(this)) {
                        ToastDPC.showPolicyRestriction(this.applicationContext,"Test Service","No es adminitrador ...")
                        return
                    }
                    System.exit(0)
                }
                R.id.btnTestFRP ->{
                    enableFRP()
                }
                else->{}
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onClick",ex.message)
        }
    }
    fun showDlgRequiereOwner() {
        val msg = "showDlgRequiereOwner"
        Log.msg(TAG, "[showDlgRequiereOwner] AUN no es ADMIN")
        val handler = Handler(Looper.getMainLooper())
        Log.msg(TAG, "[showDlgRequiereOwner] Intanta activar el admin. ")
        try {
            val explanation = "Debido a que el Telefono queda como garantia del prestamo, es necesario instalar esta aplicacion."
            val mAdminComponentName: ComponentName = DeviceAdminReceiver.getComponentName(this)
            val componentName = ComponentName(this, DeviceAdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation)
            //  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            handler.post {
                this.startActivityForResult(intent, DEVICE_ADMIN_ADD_RESULT_ENABLE)
            }
            Log.msg(TAG, "[showDlgRequiereOwner] Lanzo Activity. ")
            //launchSomeActivity.launch(intent);
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "showDlgRequiereOwner", ex.message)
        }
    }


    private fun showImei() {
        val imei: String = getImei(this)
        if (!imei.isEmpty()) {
            binding.txtIMEI.visibility = View.VISIBLE
            binding.txtIMEI.text = imei
        } else binding.txtIMEI.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun caracteristicas() {
        try {
            binding.txtStatus.text = "inicializando...20%"
            binding.txtMarca.text = Build.MANUFACTURER
            binding.txtModelo.text = Build.MODEL + " - " + Build.PRODUCT
            binding.txtAndroidVersion.text = "Android " + Build.VERSION.RELEASE + "  [ " + Build.VERSION.SDK_INT + " ]"
            //Version del DPC
            val packageInfo = this.packageManager.getPackageInfo(packageName, 0)
            val version = packageInfo.longVersionCode
            val versionName = packageInfo.versionName
            binding.txtVersion.text = "Versión: $version [$versionName]"
            showImei()
            binding.txtStatus.text = "inicializando...30%"
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "caracteristicas", ex.message)
        }
    }

    private fun registerNetworkReceiver() {
        com.macropay.data.logs.Log.msg(TAG,"registerNetworkReceiver")
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
    fun showStatus(status: String?) {
        try {
            this@AdminActivity .runOnUiThread(Runnable { binding.txtStatus.text = status })
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "showStatus", ex.message)
        }
    }


    private fun startCountDown(minutos: Long, tipo: String) {
        val milisegs = minutos * 60000
        object : CountDownTimer(milisegs, 3000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    val simpleDateFormat = SimpleDateFormat("hh:mm:ss")
                    var dateString = simpleDateFormat.format(millisUntilFinished)
                    dateString = dateString.substring(3)
                    if (Utils.isDeviceOwner(ctx!!)) {
                        Log.msg(TAG, "[startCountDown] ---> Recibio permisos de owner...$tipo")
                        binding.txtStatus.text = "Recibio permisos..."
                        cancel()
                        // installar(null);
                    } else {
                        Log.msg(TAG, "[startCountDown] Esperando permisos de owner. [$tipo] $dateString")
                        binding.txtStatus.text = "Sigue la instrucciones en la PC. [$tipo] $dateString"
                    }
                } catch (ex: java.lang.Exception) {
                    ErrorMgr.guardar(TAG, "startCountDown.onTick", ex.message)
                }
            }

            override fun onFinish() {
                //  binding.btnValidar.setText(Utils.TEXT_ENVIAR_TOKEN);
                //  binding.btnBackMain.setVisibility(View.VISIBLE);
                contTimeout = 0
            }
        }.start()
    }


    @SuppressLint("MissingPermission")
    fun getImei(context: Context): String {
        // Log.msg("getImei","--inicio --" );
        var imei = "" //tMgr.getDeviceId();
        try {
            val tMgr = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (tMgr == null) {
                Log.msg("getImei", "tMgr== NULL")
                return imei
            }
            imei = tMgr.imei
            if (imei == null) imei = ""
        } catch (e: java.lang.Exception) {
            imei = if (e.message!!.contains("does not meet the requirements")) "" else {
                ErrorMgr.guardar(TAG, "getImei*", e.message)
                "ERROR"
            }
        }
        return imei
    }

    fun enrolar(){
        val provisioning = inject().getProvision()
        provisioning.start(intent)
        //Sender.sendEnrollProcess(this,true,200,"")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.msg(TAG, "==================================================")
        Log.msg(TAG, "onActivityResult - requestCode: $requestCode")
        when (requestCode) {
            DEVICE_ADMIN_ADD_RESULT_ENABLE -> {
                when (resultCode) {
                    RESULT_CANCELED -> Log.msg(TAG, "RESULT_CANCELED")
                    RESULT_OK -> Log.msg(TAG, "RESULT_OK")
                    else -> {}
                }
            }

            else -> {}
        }
    }
    fun addWifi(view: View?) {
        stopMonitor() // dpcValues.timerMonitor!!.enabledKiosk(false,null,null)
        try {
            //
            DeviceService.configWifi(this)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "addWifi", ex.message)
        }
    }
    fun reintentar(view: View?) {
        try {
            if(!Red.isOnline){
                ToastDPC.showToast(this,"No hay conexión de Red")
            }
            com.macropay.data.logs.Log.msg(TAG, "[reintentar]=========< Inicio >=================")
            binding!!.cpPbar.visibility =View.VISIBLE
            binding!!.lytWifiOptions.visibility = View.GONE
            queryRestrictions(TAG)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "reintentar", ex.message)
        }
    }
    fun desinstallar(){
        try{
        val dmp = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if(dmp.isDeviceOwnerApp(this.packageName)){
            dmp.clearDeviceOwnerApp(this.packageName)
            dmp.wipeData(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA)
        }
        //Desinstala la app
        installerDPC.uninstallDownloader()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "desinstallar", ex.message)
        }
    }

    fun transfer(){
       Log.msg(TAG,"[transfer]  va tranferir control")
        transferCtrl.transfer("com.macropay.dpcmacro")
    }

    fun enableFRP(){
        if(!Utils.isDeviceOwner(this)) {
            ToastDPC.showPolicyRestriction(this.applicationContext,"FRP","No es adminitrador ...")
            return
        }
        var bEnabled= isFRPEnabled()
        var factory =FactoryReset(this)
        factory.setProtection(!bEnabled)
        com.macropay.data.logs.Log.msg(TAG, "[enableFRP] disableFRPPost 1")
        restrinctions.setRestriction(UserManager.DISALLOW_FACTORY_RESET, !bEnabled)

    }
    fun isFRPEnabled():Boolean{

        var  bEnabled = restrinctions. isEnabled(UserManager.DISALLOW_FACTORY_RESET)
        if (bEnabled){
            binding.btnTestFRP.text = "Habilitar FRP"
        }else
        {
            binding.btnTestFRP.text = "Deshabilitar FRP"
        }
        return bEnabled
    }
    fun reqOwner() {
        val bIsAdmin = Utils.isDeviceOwner(this)
        Log.msg(TAG,"[reqOwner] bIsAdmin")
/*      Log.msg(TAG, "reqOwner")
        val bIsAdmin: Boolean = mDpm.isDeviceOwnerApp(MainApp.DPC_packageName)
        Log.msg(TAG, MainApp.DPC_packageName + " bIsAdmin: " + bIsAdmin)
        var isDPCAdmin = false
        val admins: List<ComponentName> = mDpm.getActiveAdmins()
        if (admins != null) {
            var count = 0
            Log.msg(TAG, "--------< Admins >------------")
            for (component in admins) {
                count++
                isDPCAdmin = component.packageName.contains(MainApp.DPC_packageName)
                Log.msg(TAG, "$count .- $component")
                if (isDPCAdmin) break
            }
        }
        // mAdminComponent = new ComponentName(this, DeviceAdminReceiver.class);
        if (isDPCAdmin) {
            Log.msg(TAG, "Pide el OWER al DPC")
            Utils.sendRemoteCommand(this, 1, "10", "200")
        }*/
    }


    fun avoidSystemError() {
        com.macropay.data.logs.Log.msg(TAG,"[avoidSystemError]")
        try{
           // val versionName = packageService.dpcVersionName()
           // val enabled = !versionName.contains("dbg")
            com.macropay.data.logs.Log.msg(TAG,"[avoidSystemError] ")

            restrinctions.setRestriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, true)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"avoidSystemError*",ex.message)
        }
    }
}