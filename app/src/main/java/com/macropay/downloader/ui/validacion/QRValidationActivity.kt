package com.macropay.downloader.ui.validacion

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.macropay.data.usecases.SendConfirmacionCB
import com.macropay.downloader.data.preferences.TipoBloqueo
import com.macropay.downloader.data.preferences.TipoParametro
import com.macropay.downloader.data.preferences.dpcValues.timerMonitor
import com.macropay.downloader.databinding.ActivityQrValidationBinding
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.activities.Dialogs.handler
import com.macropay.downloader.utils.activities.Dialogs.revisarDlgPendientes
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.utils.policies.KioskScreen
import com.macropay.utils.phone.DeviceCfg
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class QRValidationActivity
@Inject constructor()
    : KioskScreen() {

    private var binding: ActivityQrValidationBinding? = null
    private var beepManager: BeepManager? = null
    private var codigoLeido: String? = null
    private var valorCodeBar: String? = null
    private var TAG = "QRValidationActivity"
    private var contTaps = 0
    private var autoFocus = false
    private var redColor = true
    private var turnedTorch = false
    private var bClosed = false

    @Inject
    lateinit var sendConfirmacionCB: SendConfirmacionCB


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrValidationBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        try {
            val formats: Collection<BarcodeFormat> = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
            binding!!.barcodeScanner.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
            binding!!.barcodeScanner.initializeFromIntent(intent)
            binding!!.barcodeScanner.decodeContinuous(callback)
            binding!!.barcodeScanner.setStatusText("...")
            beepManager = BeepManager(this)
            valorCodeBar = Settings.getSetting(TipoParametro.codigoValidacionQR, "045296191018")
            Log.msg(TAG, "[onCreate] valorCodeBar: $valorCodeBar")
            caracteristicas()
            this.enabledKiosk(true)
activity = this

            //TODO 28Nov2021
            turnScreenOnAndKeyguard()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                Log.msg(TAG, "[OnCreate] -4- setShowWhenLocked")
                setShowWhenLocked(true);
            }

            Log.msg(TAG, "[onCreate] termino...")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onCreate",ex.message)
        }
    }

    private val callback: BarcodeCallback = object : BarcodeCallback {


        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == codigoLeido) {
                // Prevent duplicate scans
                return
            }
            try{
            codigoLeido = result.text
            beepManager!!.playBeepSoundAndVibrate()
            Log.msg(TAG, "[barcodeResult] codigo leido: $codigoLeido")
            Log.msg(TAG, "[barcodeResult] valorCodeBar: $valorCodeBar")
            val handlerPolicy = Handler(Looper.getMainLooper())
            if (codigoLeido == valorCodeBar) {
                binding!!.txtStatus.setTextColor(Color.GREEN)
                binding!!.txtStatus.text = "Codigo Correcto.."


                binding!!.barcodeScanner.pause()

                //Confirma en Central que ya se marco la venta
                confirmaVenta(valorCodeBar!!)
                Settings.setSetting(TipoBloqueo.requiere_validacion_QR, false)

               // espera(1_000)
                //Apaga bandera y revisa DlgPendientes.
                cerrar()
               // espera(1_000)
                handler.postDelayed ({
                    revisarDlgPendientes(TAG)
                },1_000)


            } else {
                if (redColor) binding!!.txtStatus.setTextColor(Color.RED) else binding!!.txtStatus.setTextColor(Color.BLACK)
                redColor = !redColor
                beepManager!!.playBeepSoundAndVibrate()
                binding!!.txtStatus.setTextColor(Color.RED)
                binding!!.txtStatus.text = "El código es incorrecto..."
            }
            }catch (ex:Exception){
                ErrorMgr.guardar(TAG,"barcodeResult",ex.message)
            }
        }
        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }
    private fun espera(segs:Long){
        try{

            Thread.sleep(segs)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"espera",ex.message)
        }
    }

    private fun confirmaVenta(codeBar:String) {
        try {
            sendConfirmacionCB.send(codeBar)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "confirmaVenta", ex.message)
        }
    }
    private fun cerrar(){
        Log.msg(TAG,"[cerrar]")
        try {
            timerMonitor!!.enabledKiosk(false, null, null)
            this.enabledKiosk(false)
            this.finish()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"cerrar",ex.message)
        }
    }
    private fun caracteristicas() {
        Log.msg(TAG,"[caracteristicas] -1-")
        try {
            binding!!.txtMarca.text = Build.MANUFACTURER
            binding!!.txtModelo.text = Build.MODEL + " - " + Build.PRODUCT
            binding!!.txtAndroidVersion.text = "Android " + Build.VERSION.RELEASE + "  [ " + Build.VERSION.SDK_INT + " ]"
            binding!!.txtIMEI.text = DeviceCfg.getImei(this)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "caracteristicas", ex.message)
        }
        Log.msg(TAG,"[caracteristicas] -2-")
    }

    //OK
    fun setAutoFocus(view: View?) {
        Log.msg(TAG,"[setAutoFocus]")
        try{
            autoFocus = !autoFocus
            binding!!.barcodeScanner.pause()
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({ binding!!.barcodeScanner.resume() }, 500)
            binding!!.barcodeScanner.cancelPendingInputEvents()
            binding!!.barcodeScanner.decodeContinuous(null)
            binding!!.barcodeScanner.decodeContinuous(callback)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"setAutoFocus",ex.message)
        }
    }

    fun setTurnTorch(view: View?) {
        Log.msg(TAG,"[setTurnTorch]")
        try{
            autoFocus = !autoFocus
            if (turnedTorch) binding!!.barcodeScanner.setTorchOff() else binding!!.barcodeScanner.setTorchOn()
            turnedTorch = !turnedTorch
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"setTurnTorch",ex.message)
        }
    }

    fun testCodigo(view: View?) {
        Log.msg(TAG,"[testCodigo]")
        try {
            binding!!.txtModelo.text = valorCodeBar
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"testCodigo",ex.message)
        }
    }

    fun refreshCode(view: View?) {
        Log.msg(TAG, "[refreshCode]")
        try {
            //Revisar, al parecer no se usa.
            //val postCodebar = PostCodebar(this)
           // postCodebar.execute()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "refreshCode", ex.message)
        }
    }

    fun restart(view: View?) {
        Log.msg(TAG, "[restart] Reinicio telefono")
        try {
          //  val restrinctions = Restrictions(this)
            restrinctions.Reboot()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "restart", ex.message)
        }
    }

    fun testerSalir(view: View?) {
        Log.msg(TAG,"[testerSalir] $contTaps")
        try{
            contTaps++
            if (contTaps < 20) return
            contTaps = 0
            showEditDialog(1)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"testerSalir",ex.message)
        }
    }

    override fun onResume() {
        Log.msg(TAG, "[onResume]")
        try {
         super.onResume()
            if (!Settings.getSetting(TipoBloqueo.requiere_validacion_QR, false)){
                Log.msg(TAG,"[onResume] Se salio...")
                return
            }

            if(binding != null)
                binding!!.barcodeScanner.resume()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onResume",ex.message)
        }
    }

    override fun onPause() {
        Log.msg(TAG, "[onPause]")
        try {
            super.onPause()
/*            if (!Settings.getSetting(TipoBloqueo.requiere_validacion_QR, false)){
                Log.msg(TAG,"[onPause] Se salio...")
                return
            }*/

            if(binding != null)
                binding!!.barcodeScanner.pause()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onPause",ex.message)
        }
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.msg(TAG, "[onKeyDown]")
        if(binding != null)
            return binding!!.barcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
        return false
    }
}