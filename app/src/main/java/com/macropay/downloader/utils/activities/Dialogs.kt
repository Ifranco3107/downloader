package com.macropay.downloader.utils.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.macropay.downloader.data.preferences.Status
import com.macropay.downloader.data.preferences.TipoBloqueo
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.downloader.ui.validacion.QRValidationActivity
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.ui.provisioning.EnrollActivity
import com.macropay.utils.broadcast.Sender
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko
import kotlinx.coroutines.*
import java.util.*

@SuppressLint("StaticFieldLeak")
object Dialogs {
    var TAG = "Dialogs"

    var handler = Handler(Looper.getMainLooper())

    var mContext: Context? = null
        get() {
            return field
        }
    set(value) {
        field = value
    }

    val homeIntent: Intent
        get() {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addCategory(Intent.CATEGORY_HOME)
            return intent
        }

    /** @return IntentFilter for the default home activity
     */
    val homeIntentFilter: IntentFilter
        get() {
            val filter = IntentFilter(Intent.ACTION_MAIN)
            filter.addCategory(Intent.CATEGORY_HOME)
            filter.addCategory(Intent.CATEGORY_DEFAULT)
            return filter
        }

    //
    fun showActivity(activityClass: Class<*>, status: Status.eStatus) {
        Log.msg(TAG, "-- showActivity - status: " + status )

        try {
            val intentMain = Intent(mContext, activityClass)
            intentMain.putExtra("status", status.name)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentMain.addCategory(Intent.CATEGORY_HOME)
            Log.msg(TAG, "--> showActivity -> startActivity --------------------------------------> " + activityClass.name)
            mContext!!.startActivity(intentMain)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "showActivity - " + activityClass.name, ex.message)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun dlgsPendientes():Boolean{
        val bPendientes = Settings.getSetting(TipoBloqueo.requiere_validacion_QR, false) ||
                Settings.getSetting(TipoBloqueo.showBienvenida, false)
                //|| Settings.getSetting(TipoBloqueo.showEula, false)

            Log.msg(TAG, "[dlgsPendientes] requiere_validacion_QR: " + Settings.getSetting(TipoBloqueo.requiere_validacion_QR, false))
            Log.msg(TAG, "[dlgsPendientes] showBienvenida: " + Settings.getSetting(TipoBloqueo.showBienvenida, false))
        //    Log.msg(TAG, "[dlgsPendientes] showEula: " + Settings.getSetting(TipoBloqueo.showEula, false))
            Log.msg(TAG, "[dlgsPendientes] bPendientes: $bPendientes")
        return bPendientes
    }


    //Verifica si hay Activity  pendiente de mostrar,
    //Generalmente cuando se enrola teniedo el SIM insertado.
    fun revisarDlgPendientes(source:String): Boolean {
        Log.msg(TAG, "[revisarDlgPendientes] =========================================")
        Log.msg(TAG, "[revisarDlgPendientes] inicio - source: ["+source+"]")

        if(Status.currentStatus ==Status.eStatus.Liberado) return false

        //val bPendientes = dlgsPendientes()

        try {

            // --->
           // Settings.setSetting(TipoBloqueo.requiere_validacion_QR, false)

            //Validacion de Codigo de Barras.
            if (Settings.getSetting(TipoBloqueo.requiere_validacion_QR, false)){
               // activarActivity(mContext, QRValidationActivity::class.java)
                Log.msg(TAG,"[revisarDlgPendientes] va mostrar: requiere_validacion_QR")
                activarTmrActivity(mContext, QRValidationActivity::class.java,null)
                return true;
            }

            //Si no tiene SIM - Apaga las restricciones de
            if(!DeviceCfg.hasIMEI(mContext!!)) {
                Log.msg(TAG,"[revisarDlgPendientes] NO TIENE SIMS - desactiva restricciones de SIM ")
                Settings.setSetting(TipoBloqueo.requireSimForEnroll, false)
                Settings.setSetting(TipoBloqueo.disable_lock_for_sim_change, false)
                Settings.setSetting(TipoBloqueo.enabledlockForRemoveSim, false)
            }




            Log.msg(TAG,"[revisarDlgPendientes] Kiosko.eTipo: "+ Kiosko.currentKiosko)
            if(Kiosko.eTipo.PorNoConexion == Kiosko.currentKiosko) {
                Log.msg(TAG, "[revisarDlgPendientes] Mando Mensaje de TipoBloqueo PorNoConexion...")
                Sender.sendBloqueo(true, mContext!!, Kiosko.eTipo.PorNoConexion)

                return true
            }
/*            if (Settings.getSetting(TipoBloqueo.showEula, false)){
                activarActivity(mContext, EULAActivity::class.java)
                return true;
            }*/


        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "revisarDlgPendientes", ex.message)
        }
        //
        return false
    }



    fun isRunning(context: Context, activityClass: Class<*>): Boolean {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val tasks = activityManager.getRunningTasks(Int.MAX_VALUE)
            for (task in tasks) {
                //Log.msg(TAG,"canonicalName $activityClass.canonicalName")
                if (activityClass.canonicalName.equals(task.baseActivity!!.className, ignoreCase = true))
                    return true
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "isRunning", ex.message)
        }
        return false
    }

    fun lockScreen(): Boolean {
        try {
            Log.msg(TAG,"[lockScreen] apagado de pantalla - lockNow()")
          dpcValues.mDpm!!.lockNow()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "lockScreen", ex.message)
        }
        return false
    }

    fun lockIfUpdate(source:String){
        try {
            val bDpc_updated = Settings.getSetting(Cons.KEY_DPC_UPDATED, false)
            //   Log.msg(TAG, "[lockIfUpdate] bDpc_updated: " + bDpc_updated +"["+ source +"]" )

            if (bDpc_updated) {
                Log.msg(TAG, "[lockIfUpdate] apago la pantalla.")
                lockScreen()
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"lockIfUpdate",ex.message)
        }
    }

    fun isIconHidden(context: Context):Boolean{
        var bHidden = false
        try {
            val p = context.packageManager
            val componentName = ComponentName(context, EnrollActivity::class.java)
            var curStatus = p.getComponentEnabledSetting(componentName)

            //PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
            //PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            bHidden= (curStatus == PackageManager.COMPONENT_ENABLED_STATE_DISABLED )
            Log.msg(TAG, "[isIconHidden] curStatus: $curStatus bHidden: "+bHidden)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"isIconHidden",ex.message)
        }
        return bHidden

    }
    fun showAppIcon(context: Context, bHide: Boolean) {
        var classActivity = EnrollActivity::class.java
        //var classActivity = context.packageName +  ".InitialAlias"
        /*        val hasProblemWithService = Settings.getSetting(Cons.PROBLEM_ADMINSERVICE,false)
                if(hasProblemWithService && bHide){
                    Log.msg(TAG,"[showAppIcon] hasProblemWithService ")
                    return
                }*/

        Log.msg(TAG, "[showAppIcon] : bHide: [$bHide]- $classActivity")
        try {
            val p = context.packageManager
            val componentName = ComponentName(context, classActivity)
            //  val componentName = ComponentName(context.packageName,context.packageName + ".EnrollActivity")
            if (bHide){
                // Log.msg(TAG,"[showAppIcon] COMPONENT_ENABLED_STATE_DISABLED -1- packagename: [$packagename] clase: [$EnrollActivity::class.java.name]")
                Log.msg(TAG,"[showAppIcon] COMPONENT_ENABLED_STATE_DISABLED")

                p.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                )
            }
            else{
                Log.msg(TAG,"[showAppIcon] COMPONENT_ENABLED_STATE_ENABLED")
                p.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "showAppIcon", e.message)
        }
    }
 /*   fun showAppIcon(context: Context, bHide: Boolean,classActivity:String) {
        var className = classActivity.replace(context.packageName,"")
        var classActivity = EnrollActivity::class.java
        Log.msg(TAG, "[showAppIcon] : bHide: [$bHide]- classname [$className]")
        try {
            val p = context.packageManager
            val componentName = ComponentName(context, InfoActivity::class.java)
            if (bHide){
                Log.msg(TAG,"[showAppIcon] COMPONENT_ENABLED_STATE_DISABLED ")
                p.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                )
            }
            else{
                Log.msg(TAG,"[showAppIcon] COMPONENT_ENABLED_STATE_ENABLED")
                p.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            }
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "showAppIcon", e.message)
        }
    }*/

    fun activarActivity(context: Context?, activityClass: Class<*>?) {
        //Boolean bDelay = Build.MODEL.toUpperCase().contains("REDMI") || Build.MODEL.toUpperCase().contains("M2102J20SG") ;
        val bDelay = Build.MANUFACTURER.uppercase(Locale.getDefault()).contains("XIAOMI")
        Log.msg(TAG, "[activarActivity] - bDelay: $bDelay ***************************************<+++>")
        try {
            //val handler = Handler(Looper.getMainLooper())
          //  val handler = Handler()
         //   handler.postDelayed({
            Log.msg(TAG, "Sin handler...")
           showActivity(context, activityClass)
         //   },2_000)
          //  dpcValues.timerMonitor!!.enabledKiosk(true, activityClass ,null)

           /* if (bDelay)
           handler.postDelayed({
                showActivity(context, activityClass) }, 1_000)
            else
                handler.post {
                    showActivity(context, activityClass)
                }*/
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "activarActivity", ex.message)
        }
    }
//intentMain
    fun activarTmrActivity(context: Context?, activityClass: Class<*>?, intent: Intent?) {
        Log.msg(TAG, "[activarTmrActivity]  ***************************************<+++>")
        try {
            Log.msg(TAG, "[activarTmrActivity] timerMonitor...")
            dpcValues.timerMonitor!!.enabledKiosk(true, activityClass ,intent)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "activarActivity", ex.message)
        }
    }

    fun showActivity(context: Context?, activityClass: Class<*>?) {
        try {
            Log.msg(TAG, "[showActivity] va mostrar la Activity [ "+activityClass!!.name +" ]")
            val intentMain = Intent(context, activityClass)

            //    Log.msg(TAG, "[showActivity] - activityClass: ["+activityClass.getName()+"]");
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            context!!.startActivity(intentMain)

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "showActivity", ex.message)
        }
    }
fun showStatus(msg:String){

    try{
       val source =  Settings.getSetting(Cons.KEY_ENROLL_SOURCE, EnrollActivity.javaClass.toString())
       // Log.msg(TAG,"[showStatus] $source  msg: $msg ${EnrollActivity.javaClass.toString()}")
       when (source) {
           "EnrollActivity"->{
               EnrollActivity.fa!!.showStatus(msg)
           }
           "FinalizeActivity"->{
           }
           else->{
               EnrollActivity.fa!!.showStatus(msg)

           }
       }
        // if(EnrollActivity.fa != null)

    }catch (ex:Exception){
        ErrorMgr.guardar(TAG,"showStatus",ex.message,false)
    }


}
    fun showActivityStatus(source: String,context: Context){
        try{
            val  isActivityShowed =   Settings.getSetting("EnrollShowed",false)
            if(isActivityShowed){
                 Log.msg(TAG,"[showActivityStatus]  YA EXISTE ACTIVA...[$source]")
                return
            }
            Log.msg(TAG,"[showActivityStatus]  no EXISTE ACTIVA...[$source]")
            return
        /*    Log.msg(TAG,"[showActivityStatus] - 1 - 07Nov 0900 - intenta mostrar en:[$source]")
            handler!!.post {
                // apply {
                val intentMain = Intent(context, EnrollActivity::class.java)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intentMain)

             //   for(n in 0..5)
               // {
               //     Sender.sendStatus("Preparando dispositivo..."+n)
               //     Thread.sleep(200)
               // }

                // }
             // Log.msg(TAG, "[showActivityStatus] [1] creo el Activity en intento: " + source)

            }*/
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"showActivityStatus",ex.message)
        }
    }
    fun playSound(context:Context){
        //  Log.msg(TAG,"[playSound]")
        try{
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, defaultSoundUri)
            r.play()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"playSound",ex.message)
        }
    }
}