package com.macropay.downloader.ui.contrato

import android.os.Bundle
import com.macropay.downloader.data.preferences.TipoParametro
import com.macropay.data.logs.ErrorMgr
import com.macropay.downloader.ui.common.swipebar.OnStateChangeListener
import android.os.Looper
import com.macropay.downloader.data.preferences.TipoBloqueo
import android.content.Context
import android.content.Intent
import android.graphics.text.LineBreaker
import android.os.Handler
import android.os.PersistableBundle
import android.view.View
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.preferences.Defaults
import com.macropay.downloader.databinding.ActivityEulaactivityBinding
import com.macropay.downloader.ui.provisioning.EnrollActivity
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.downloader.domain.usecases.main.AppsCtrl
import com.macropay.downloader.domain.usecases.main.RestrictionCtrl
import com.macropay.downloader.utils.activities.Dialogs
import com.macropay.downloader.utils.policies.KioskScreen
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.json.JSONObject
import javax.inject.Inject
import kotlin.Exception


@AndroidEntryPoint
class EULAActivity
@Inject constructor()
    : KioskScreen() {
    private var binding: ActivityEulaactivityBinding? = null
    var context: Context? = null
    var TAG = "EULAActivity"
    private var bClosed =false

    private var eventMQTT : EventMQTT? = null
    @Inject
    lateinit var appsCtrl: AppsCtrl

    @Inject
    lateinit var restrictionCtrl: RestrictionCtrl
/*    @Inject
    lateinit var enrollment: Enrollment*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var ln = 0
        try {
            binding = ActivityEulaactivityBinding.inflate(layoutInflater)
            setContentView(binding!!.root)
            context = context
            ln = 1
            this.enabledKiosk(true)
ln =2
            val txtTitleEULA = Settings.getSetting(TipoParametro.eulaTitle, Defaults.EULA_TITLE)
            ln =3
            if (txtTitleEULA == "") binding!!.txtTitulo.visibility = View.GONE else binding!!.txtTitulo.text = txtTitleEULA
            ln =4
            var txtEULA = Settings.getSetting(TipoParametro.eulaBody, Defaults.EULA_DEFAULT)
            //Sustituir lo que esta entre comillas, a color azul.
            //txtEULA = txtEULA.replace("\"","{#2196F3}")
            //txtEULA = txtEULA.replace("\"\"".toRegex(),"{#2196F3}").trim().replace("\\s{2,}".toRegex(),"")
            ln =5

            txtEULA = txtEULA.replace('"','\"');
            ln =6
            binding!!.textEULA.text = txtEULA
            ln =7
            //TransformText.transform(txtEULA, binding!!.textEULA)
            binding!!.textEULA.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
            binding!!.swipeBtnSalir.setOnStateChangeListener(stateChangeListener)
            ln =8
            var jsonString  =  intent.getStringExtra("restricciones")
            ln =9
            eventMQTT = EventMQTT("bloqueo", JSONObject(jsonString), false)
            ln =10
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "onCreate [$ln]", ex.message)
        }
    }

    var stateChangeListener = OnStateChangeListener {
        try {

            if(!bClosed) {
                bClosed = true
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    this.enabledKiosk(false)

                    finish()
                    //Apaga bandera y revisa DlgPendientes.
                    Settings.setSetting(TipoBloqueo.showEula, false)
                    Settings.setSetting(Cons.KEY_EULA_SHOWED, true)

                    GlobalScope.launch {
                        iniciarEnrolamiento()
                    }
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "onStateChange", ex.message)
        }
    }
    suspend fun iniciarEnrolamiento() {
        Log.msg(TAG, "[iniciarEnrolamiento] --------------------------------------------------------------")
        Log.msg(TAG, "[iniciarEnrolamiento] --------------------< iniciar >------------------------------------------")
        try {
            //Muestra la activity de Enroll
            CoroutineScope(Dispatchers.Main)
                .launch {
                    delay(500)

                    showEnrollActivity(TAG)
                }
            runBlocking {
                Log.msg(TAG, "[iniciarEnrolamiento] -+-+-+-+-+-+-+--->>>  inicia applyBloqueoInicial >>")
               // enrollment.applyBloqueoInicial(eventMQTT!!)
                val restriccion =restrictionCtrl.getRestriction(TipoBloqueo.install_bussines_apps,  eventMQTT!!)
                appsCtrl.downloadApps(restriccion)
                Log.msg(TAG, "[iniciarEnrolamiento] -+-+-+-+-+-+-+--->>> Termino applyBloqueoInicial...")
                //Ver si aun se necesita esta variable.
                Settings.setSetting("enrollamientoPendiente", false)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "iniciarEnrolamiento", ex.message)
        }
    }
   /* suspend fun iniciarEnrolamiento() {
        Log.msg(TAG, "[iniciarEnrolamiento]")
        try {
            //Muestra la activity de Enroll
            runBlocking {
                withContext(Dispatchers.Main) {
                    showEnrollActivity(TAG)
                }
            }
            delay(1_000)
            runBlocking {
                Log.msg(TAG, "[iniciarEnrolamiento] -+-+-+-+-+-+-+--->>>  inicia Enrolamiento >>")
                enrollment.applyBloqueoInicial(eventMQTT!!)
                Log.msg(TAG, "[iniciarEnrolamiento] -+-+-+-+-+-+-+--->>> Termino Enrolamiento...")
                //Ver si aun se necesita esta variable.
                Settings.setSetting("enrollamientoPendiente", false)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "iniciarEnrolamiento", ex.message)
        }
    }*/



    fun showEnrollActivity(source: String){
        Log.msg(TAG,"[showEnrollActivity] ---------------------------------------------------------")
        Log.msg(TAG,"[showEnrollActivity] source: ($source)")
        var ln= 1
        try{
            CoroutineScope(Dispatchers.Main)
                .launch {

                //Detiene el timer de monitoreo de kiosko
                Log.msg(TAG,"[showEnrollActivity] 1.-Deteniendo) -  ExecutorKiosk")
      /*          ln = 2
                dpcValues.timerMonitor!!.enabledKiosk(false, null, null)
                ln = 3*/

                //Espera un segundo para empezar
                for (i in 1..3){
                    Log.msg(TAG,"[showEnrollActivity] 2.- DELAY - Espera que termine evento anterior $i segs.")
                    delay(500)
                }
                ln=4

                val intentMain = Intent(Dialogs.mContext, EnrollActivity::class.java)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                ln=5

                Dialogs.activarTmrActivity(Dialogs.mContext, EnrollActivity::class.java,intentMain)
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"showEnrollActivity [$ln]",ex.message)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        Log.msg(TAG,"[onPostCreate]")
    }
    override fun onStart() {
        super.onStart()
            try{
            Log.msg(TAG,"[onStart]")
          //  dpcValues.timerMonitor!!.enabledKiosk(false, null, null)
        }catch (ex:Exception){
                ErrorMgr.guardar(TAG,"onStart",ex.message)
        }
    }
}