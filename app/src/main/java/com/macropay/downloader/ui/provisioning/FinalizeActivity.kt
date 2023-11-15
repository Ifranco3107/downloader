/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.macropay.downloader.ui.provisioning

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import android.view.View
import com.macropay.data.BuildConfig
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.usecases.EnrollDevice
import com.macropay.downloader.Auxiliares
import com.macropay.downloader.R

import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.databinding.FinalizeActivityBinding

import com.macropay.downloader.receivers.NetworkReceiver
import com.macropay.downloader.ui.common.mensajes.ToastDPC
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.device.Battery
import com.macropay.downloader.utils.device.DeviceService
import com.macropay.downloader.utils.policies.Restrictions


import com.macropay.utils.broadcast.Sender
import com.macropay.utils.network.Red
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class FinalizeActivity
@Inject constructor()
    : GetEnrollRestrictions() {

    var TAG = "FinalizeActivity"
    lateinit var binding: FinalizeActivityBinding
    private var bClosed =false
    @Inject
    lateinit var enrollDevice: EnrollDevice

/*    @Inject
    lateinit var restrinctions: Restrictions*/

/*    @Inject
    lateinit var enrollment: Enrollment*/


/*    @Inject
    lateinit var provisioning :  Provisioning*/

    lateinit var eventMQTT: EventMQTT
    var bCerrando = false

    var contTaps = 0
    val TAPS_REQUERIDOS = 15
    private var mNetworkReceiver: BroadcastReceiver? = null

    companion object {
       // @JvmField
        var fa: FinalizeActivity? = null
    }

    //Porcentaje minimo de bateria, para que pueda iniciar el enrolamiento.
    val levelMinimo= 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Auxiliares.init(applicationContext)
        try{
            binding = FinalizeActivityBinding.inflate(layoutInflater)
            val view: View = binding!!.root
            setContentView(view)
            fa = this

            caracteristicas()

            //      this.enabledKiosk(true) //Muestra pantalla de Pinap screen

            this.hideSystemUI(window)

            val battery= Battery.batteryLevel(this).roundToInt()

            binding!!.lytWifiOptions.visibility = View.GONE
            binding.txtStatus.text="$battery% de bateria..."
 //Red.enableWifi(false)
            if(battery < levelMinimo) {
                Log.msg(TAG,"[onCreate] bateria $battery% - menor de $levelMinimo%")
                lowBattery(battery)
            }else{
                Log.msg(TAG,"[onCreate] bateria $battery% - mayor de $levelMinimo%")
                enrollStatus()
                receiverStatus()
                registerNetworkReceiver()
                setTimeoutEvent()
                if(!BuildConfig.isTestTCL.equals("true")) {
                    // Las restricciones se leen hasta que recibe el mensaje de DeviceReceiver, cuando ya se establecio el Device Owner
                    iniciaEnroll(null)
                }else{
                    avoidSystemError(true)
                    cerrar(TAG)
                }

            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"onCreate",ex.message)
        }
    }

    fun iniciaEnroll(v:View?){
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
    }

    fun receiverStatus(){
        try{
            val filter = IntentFilter()
            filter.addAction(Sender.ACTION_STATUS_CHANGE)
            filter.addAction(Sender.ACTION_HTTP_ERROR)
            filter.addAction(Sender.ACTION_STATUS_ENROLLMENT)
            filter.addAction(Sender.ACTION_STATUS_NETWORK)

            registerReceiver(mStatusReceiver, filter, "com.macropay.downloader.enrollstatus",null)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"receiverStatus",ex.message)
        }
    }


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
                        Log.msg(TAG,"[mStatusReceiver] ACTION_STATUS_ENROLLMENT")
                        showStatus("Leyendo configuración...")
                        provisioning.preInit()
                        queryRestrictions(TAG)
                    }
                    else -> {
                        Log.msg(TAG,"indinido: "+intent.action)
                    }
                }
            }catch (ex:Exception){
                ErrorMgr.guardar(TAG,"mStatusReceiver",ex.message)
            }
        }
    }

    fun showStatus(status: String?) {

        try {
            this@FinalizeActivity.runOnUiThread(Runnable {
                binding.txtStatus.text = status

            })
        } catch (ex:Exception) {
            ErrorMgr.guardar(TAG, "showStatus", ex.message)
        }
    }

    fun lowBattery(level:Int){
        try{
            var status = Battery.batteryStatus(this.applicationContext)
            var msgStatus  =  if(status)  "Cargando... " else "Cargue la bateria por favor."
            binding.txtStatus.text= "Bateria Baja.\n\n"+
                    "Actualmente tiene $level% \n"+
                    "es necesario minimo $levelMinimo%\n\n"+
                    msgStatus

            val handlerLock = Handler(Looper.getMainLooper())
            handlerLock.postDelayed(
                {
                    Log.msg(TAG,"[lowBattery] RESULT_CANCELED")
                    setResult(RESULT_CANCELED)
                    finish()
                }, 10_000)
        } catch (ex:Exception) {
            ErrorMgr.guardar(TAG, "lowBattery", ex.message)
        }
    }
    fun cerrar(source:String) {
        Log.msg(TAG,"[cerrar] source: $source bCerrando: $bCerrando")
        if(bCerrando){
            Log.msg(TAG,"[cerrar] ya esta cerrando...")
            return
        }
        try {
            bCerrando = true
            this.showSystemUI(window)


             binding.txtStatus.text= "Aplicando...."
             Log.msg(TAG, "[cerrar] RESULT_OK")
             setResult(RESULT_OK)
             finish()
        } catch (ex: Exception) {
             bCerrando = false
             ErrorMgr.guardar(TAG,"cerrar",ex.message)
        }
    }

    private fun caracteristicas() {
        try {
             binding.txtMarca.text = Build.MANUFACTURER
             binding.txtModelo.text = Build.MODEL + " - " + Build.PRODUCT
             binding.txtAndroidVersion.text = "Android " + Build.VERSION.RELEASE + "  [ " + Build.VERSION.SDK_INT + " ]"
             //Version del DPC
             val packageInfo = this.packageManager.getPackageInfo(packageName, 0)
             val version = packageInfo.longVersionCode
             val versionName = packageInfo.versionName
             binding.txtVersion.text = "Versión: $versionName ($version)"
             binding.txtStatus.text = "Preparando instalación..."
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "caracteristicas", ex.message)
        }
    }
    fun reintentar(view: View?) {
        try {
             if(!Red.isOnline){
                 ToastDPC.showToast(this,"No hay conexión de Red")
             }
             Log.msg(TAG, "[reintentar]=========< Inicio >=================")
             binding!!.cpPbar.visibility =View.VISIBLE
             binding!!.lytWifiOptions.visibility = View.GONE
             queryRestrictions(TAG)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "reintentar", ex.message)
        }
    }

fun wifiOff(view: View?) {
    try {
         Log.msg(TAG,"**************************************************************************")
         Log.msg(TAG,"**************************** salida forazada *****************************")
        /*            contTaps++
         if (contTaps < TAPS_REQUERIDOS) return
         contTaps = 0*/
         binding.txtMarca.text ="Sin Red"
         Log.msg(TAG,"apaga la Red")
        //   Red.enableWifi(false)
    } catch (ex: Exception) {
     ErrorMgr.guardar(TAG, "addWifi", ex.message)
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
    fun enrollStatus(){
        this!!.setOnDownloadStatus(  object: QueryEnrollStatus {
             override fun onSuccess(body: EventMQTT) {
                 Log.msg(TAG, "[enrollStatus] onSuccess")
                 showStatus("Leyo correctamente...")
                // provisioning.iniciar(body,TAG +".enrollStatus")
                }

             override fun onError(code: Int, error: String?) {
                 Log.msg(TAG, "[enrollStatus] onError")
               //  showStatus("ocurrio un error...")
                 binding!!.cpPbar.visibility =View.GONE
                 binding!!.txtError.text = "Ocurrio un error al enrolar"
                 binding!!.lytWifiOptions.visibility = View.VISIBLE

                 //Notifica a Central, que hubo error...
                 enrollFailed.send(DeviceInfo.getDeviceID())
             }
        })
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
//textVersion
    fun testerSalir(view: View?) {
        Log.msg(TAG,"**************************************************************************")
        Log.msg(TAG,"**************************** salida forazada *****************************")
        contTaps++
        if (contTaps < TAPS_REQUERIDOS) return
        contTaps = 0
        bCerrando = false
        cerrar("TesterSañor")
        showEditDialog(1)
    }

    fun setTimeoutEvent(){
    //  retryIntentos =0
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
         } }, milisegs)
    }
    fun avoidSystemError(bEnabled:Boolean=true) {
        com.macropay.utils.logs.Log.msg(TAG,"[avoidSystemError] va a asignar: bEnabled: $bEnabled")
        try{
            restrinctions.setRestriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, bEnabled)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"avoidSystemError*",ex.message)
        }
    }
}