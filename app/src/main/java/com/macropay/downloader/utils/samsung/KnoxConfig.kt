package com.macropay.downloader.utils.samsung

import android.content.Context
import com.samsung.android.knox.EnterpriseDeviceManager
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager
import com.macropay.data.logs.ErrorMgr
import com.macropay.utils.broadcast.Sender
import android.os.Build
import android.os.CountDownTimer
import com.macropay.downloader.data.preferences.*


import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.downloader.utils.SettingsApp
import com.macropay.utils.preferences.Cons


import kotlin.Exception

/*class KnoxConfig
@Inject constructor (@ApplicationContext var context:Context)
{

    //var endEnrollment: EndEnrollment = EndEnrollment(dpcValues.mContext!!)
    @Inject
    lateinit var endEnrollment: EndEnrollment*/
object KnoxConfig {
    val TAG = "KnoxConfig"
    //Pagina para buscar el detalla de un producto Samsung: https://deviceinfohw.ru/devices/index.php?platform=PLATFORM&cpu=CPU&brand=samsung&filter=SM-A037M&submit=Search
    val modelosSinKnox = "a03sub" //a3coreub, a04eub
    var bCancel=false
    var context:Context? = null
        get() {
            return field}
        set(value) {field = value}

    var KPE_LICENSE_KEY = "KLM09-CS0N2-XQYUB-USAM6-01QKG-E8X09"
    //
    fun sinKnoxInstalled():Boolean{
        return (modelosSinKnox.contains(Build.PRODUCT))
    }
    fun activateLicense() {
        Log.msg(TAG, "[ActiveLicense] --------")
        if(modelosSinKnox.contains(Build.PRODUCT)){
            return
        }
        if (aPILevel == EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_NOT_SUPPORTED) {
            Log.msg(TAG, "[ActiveLicense] knox: $knoxVersion")
            return
        }
        try {
            if (!MainApp.isAdmin(context)) {
                Log.msg(TAG, "[ActiveLicense] NO ES ADMIN")
                return
            }
            if (context == null) {
                Log.msg(TAG, "[ActiveLicense] Conxtex = null")
                return
            }
            //https://docs.samsungknox.com/admin/knox-platform-for-enterprise/user-agreements-android-device-management.htm

            val licenseManager = KnoxEnterpriseLicenseManager.getInstance(context)
            if (licenseManager == null) {
                Log.msg(TAG, "[ActiveLicense] licenseManager = null")
                return
            }
            //
            Log.msg(TAG, "[ActiveLicense] va activar la licencia....")

            licenseManager.activateLicense(KPE_LICENSE_KEY)
            //startCountDown(15)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "activateLicense", ex.message)
        }
    }

    fun applySetting(action: KnoxStatus.eAction): Boolean {
        var bResult =false
        var ln = 1
        Log.msg(TAG, "[applySetting] action: $action")
        if(modelosSinKnox.contains(Build.PRODUCT)){
            return true
        }
        if (aPILevel == EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_NOT_SUPPORTED) {
            Log.msg(TAG, "[activeLock] knox: $knoxVersion")
            return true
        }

        val enterpriseDeviceManager = EnterpriseDeviceManager.getInstance(context)
        ln = 2

        // Get the RestrictionPolicy class where the setCameraState method lives
        val restrictionPolicy = enterpriseDeviceManager.restrictionPolicy
        var actionWipe = false
        var actionDownload = false
        var allowed = false
        ln = 3

        savePending(action)
        when(action)
        {
            KnoxStatus.eAction.Lock->{
                actionWipe = true
                allowed = false
            }
            KnoxStatus.eAction.Unlock->{
                actionWipe = true
                allowed = true
            }
            KnoxStatus.eAction.DisableDownlaod->{
                actionDownload=true
                allowed = false
            }
            KnoxStatus.eAction.EnableDownload->{
                allowed = true
                actionDownload = true
            }
            else->{}
        }
        //
        if(actionWipe)
            bResult = restrictionPolicy.allowFactoryReset(allowed)
        if(actionDownload)
            bResult =  restrictionPolicy.allowFirmwareRecovery(allowed)



        //
        if (true == bResult) {
            Sender.sendStatus("Se aplico correctamente...")
            Log.msg(TAG,"[applySetting] Se aplico correctamente...allowed: $allowed actionWipe: $actionWipe")
            val pendings =   removePending(action)
            if (requireDeactiveLicense(actionWipe,actionDownload,allowed)) {
                Log.msg(TAG, "[applySetting] Desactiva Licencia")
                deactivateLicense()
            }
            Log.msg(TAG, "Status.currentStatus: [" + Status.currentStatus + "] pendings: [$pendings]")

            //TODO = && pendings.equals("")
            if (Status.currentStatus == Status.eStatus.AplicoRestricciones) {
                //Configurando
                //Revisa si ya termino la instalacion...
                //    ToastDPC.showToast(context,"confirmaTerminoActivar" )
                confirmaTerminoActivar(TAG)
            }
            //}
        }

        return bResult
    }

    //Verifica si se requiere desactivar la licencia.
    fun requireDeactiveLicense(actionWipe:Boolean,actionDownload:Boolean,allowed:Boolean):Boolean{

        val pendings = Settings.getSetting(Cons.KEY_ACTIONS_PENDINGS,"")
        Log.msg(TAG,"[requireDeactiveLicense] actionWipe: $actionWipe actionDownload: $actionDownload pendings: [$pendings] allowed: $allowed")

        //Activa las banderas correspondientes...
        if(actionWipe)
            Settings.setSetting(Cons.KEY_WIPE_STATUS,allowed)

        if(actionDownload)
            Settings.setSetting(Cons.KEY_DOWNLOAD_STATUS,allowed)

        //Si hay alguna restriccion activa.
        val featureAllowed = ( Settings.getSetting(Cons.KEY_WIPE_STATUS,true)
                && Settings.getSetting(Cons.KEY_DOWNLOAD_STATUS,true))
        Log.msg(TAG,"NotfeatureAllowed: $featureAllowed pendings: [$pendings]")
        //Si no hay restriccion activa y no hay pendientes por procesar...
        return featureAllowed && pendings.equals("")
    }

    fun savePending(action: KnoxStatus.eAction){
        try{
            var pendings = Settings.getSetting(Cons.KEY_ACTIONS_PENDINGS,"")
            pendings += action.name+","
            Log.msg(TAG,"[savePending] pendings: <$action>")
            Settings.setSetting(Cons.KEY_ACTIONS_PENDINGS,pendings)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"savePending",ex.message)
        }
    }
    fun removePending(action: KnoxStatus.eAction):String{
        var pendings = Settings.getSetting(Cons.KEY_ACTIONS_PENDINGS,"")
        try{
            pendings = pendings.replace(action.name+",","")
            Log.msg(TAG,"[removePending] pendings: <$action> - pendings: [$pendings]")
            Settings.setSetting(Cons.KEY_ACTIONS_PENDINGS,pendings)
        }catch (ex:Exception){

            ErrorMgr.guardar(TAG,"removePending",ex.message)
        }
        return  pendings
    }

    fun applyPendings(){
        var pendings = Settings.getSetting(Cons.KEY_ACTIONS_PENDINGS,"")
        Log.msg(TAG,"[applyPendings] pendings: ["+pendings +"]")
        if(pendings.equals("")){
            return
        }
        try{
//            val pendingActions = arrayOf(pendings)
            val pendingActions =pendings.split(",")
            pendingActions.forEach {
                val action = it.replace(",","")
                Log.msg(TAG,"[applyPendings] va aplicar -> action: ["+action+"]")
                if(!action.isEmpty())
                    applySetting(KnoxStatus.eAction.valueOf(action))
            }

            Log.msg(TAG, "[applyPendings] termino de aplicar settings ")
            KnoxStatus.isLicensedActived = true
            confirmaTerminoActivar(TAG)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"applyPendings",ex.message)
        }
    }

    fun activeLock(bEnable: Boolean): Boolean {
        Log.msg(TAG, "[activeLock] enable: $bEnable")
        if(modelosSinKnox.contains(Build.PRODUCT)){
            return true
        }
        if (aPILevel == EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_NOT_SUPPORTED) {
            Log.msg(TAG, "[activeLock] knox: $knoxVersion")
            return true
        }
        var ln = 1
        var result = false
        try {
            Log.msg(TAG,"[activeLock] -1-")
            val enterpriseDeviceManager = EnterpriseDeviceManager.getInstance(context)
            ln = 2
            Log.msg(TAG,"[activeLock] -2-")
            // Get the RestrictionPolicy class where the setCameraState method lives
            val restrictionPolicy = enterpriseDeviceManager.restrictionPolicy
            ln = 3
            Log.msg(TAG,"[activeLock] -3-")
            //---

            result = restrictionPolicy.allowFactoryReset(!bEnable)
            Log.msg(TAG,"[activeLock] -4-")
            ln = 4
            restrictionPolicy.allowFirmwareRecovery(!bEnable)
            /*  The allowFirmwareRecovery API method is the stricter of the two policies.
              Having allowFirmwareRecovery set to false
              disallows all the methods of updating the device firmware,
              - flashing via download mode,
              - updating over the air (OTA) (From Knox Standard SDK v2.9, allowFirmwareRecovery does not affect OTA)
              - via computer using Samsung smart switch.
           */
            ln = 5
            if (true == result) {
                // Toast.makeText(mContext, "activeLock: "+!bEnable, Toast.LENGTH_SHORT).show();
                Sender.sendStatus("Se activo correctamente...".trimIndent())
                Log.msg(TAG, "[activeLock] result: OK")
                KnoxStatus.pendingAction =KnoxStatus.eAction.none
                if (!bEnable) {
                    Log.msg(TAG, "[activeLock] Desactiva Licencia")
                    deactivateLicense()
                } else {
                    //Log.msg(TAG, "SettingsApp.statusEnroll(): [" + SettingsApp.statusEnroll() + "]")
                    Log.msg(TAG, "Status.currentStatus: [" + Status.currentStatus + "]")
                    // if (SettingsApp.statusEnroll() == SettingsApp.status.Enrolo) {
                    if (Status.currentStatus == Status.eStatus.AplicoRestricciones) {
                        //Configurando
                        //Revisa si ya termino la instalacion...
                        confirmaTerminoActivar(TAG)
                    }
                }
            } else {
                Log.msg(TAG, "[activeLock] +++++++++++++++++++++++++++++++++++++++++++ ")
                Log.msg(TAG, "[activeLock] NO PUDO REALIZAR LA OPERACION CORRECTAMENTE. ")
                Log.msg(TAG, "[activeLock] +++++++++++++++++++++++++++++++++++++++++++ \n\n\n")
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "activeLock $ln", ex.message)
        }
        return result
    }

    fun confirmaTerminoActivar(source:String){
        Log.msg(TAG,"[confirmaTerminoActivar] source: "+source)
        try{
           // val endEnrollment: EndEnrollment = EndEnrollment(context!!)
            var requireLicense =   Settings.getSetting(Cons.KEY_ES_LICENCIA_REQUERIDA, false)
            var isEnableFRP = Settings.getSetting(TipoBloqueo.disable_recovery_wipe_data,false)
            Log.msg(TAG,"[confirmaTerminoActivar] requireLicense: "+requireLicense)
            Log.msg(TAG,"[confirmaTerminoActivar] isEnableFRP: "+isEnableFRP)
            Log.msg(TAG,"[confirmaTerminoActivar] KnoxStatus.isLicensedActived: "+KnoxStatus.isLicensedActived)

            Log.msg(TAG,"[confirmaTerminoActivar] Status.currentStatus: "+Status.currentStatus)

            //Se puso esta condicion, porque en ocasiones Knox no teminaba de desaactivar la licencia.
            //y despues entraba al LicenseReceiver y rebooteaba el equipo..
            //TODO: Monitorear como se comporta con liberacion...
            if((isEnableFRP ||requireLicense)
                && Status.currentStatus == Status.eStatus.AplicoRestricciones  ){
                Log.msg(TAG,"[confirmaTerminoActivar] va ejecutar --> terminaInstalacion ")
            //    endEnrollment.terminaInstalacion(TAG)
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"",ex.message)
        }
    }
    fun deactivateLicense() {
        Log.msg(TAG, "[DeactivateLicense]-----------------")
        if(modelosSinKnox.contains(Build.PRODUCT)){
            return
        }
        if (aPILevel == EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_NOT_SUPPORTED) {
            Log.msg(TAG, "knox: $knoxVersion")
            return
        }
        try {
            if (!MainApp.isAdmin(context)) {
                Log.msg(TAG, "[DeactivateLicense] NO ES ADMIN")
                return
            }
            if (context == null) {
                Log.msg(TAG, "[DeactivateLicense] Conxtex = null")
                return
            }
            val licenseManager = KnoxEnterpriseLicenseManager.getInstance(context)
            if (licenseManager == null) {
                Log.msg(TAG, "[DeactivateLicense] = null")
                return
            }
            Log.msg(TAG, "[DeactivateLicense] va desactivar la licencia....")
            licenseManager.deActivateLicense(KPE_LICENSE_KEY)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "DeactivateLicense", ex.message)
        }
    }//Aun no se activa la licencia,

    //		Error:Attempt to invoke virtual method 'com.samsung.android.knox.license.ActivationInfo$State com.samsung.android.knox.license.ActivationInfo.getState()' on a null object reference
    /* val isLicenseActived: Boolean
         get() {
             Log.msg(TAG, "**** isLicenseActived: [" + Build.MANUFACTURER.uppercase(Locale.getDefault()) + "]")
             var bActived = false
             try {
                 if(modelosSinKnox.contains(Build.PRODUCT)){
                     return true
                 }
                 if (Build.MANUFACTURER.uppercase(Locale.getDefault()).contains("SAMSUNG")) {
 //		Error:Attempt to invoke virtual method 'com.samsung.android.knox.license.ActivationInfo$State com.samsung.android.knox.license.ActivationInfo.getState()' on a null object reference
                     if (EnterpriseDeviceManager.getAPILevel() >= 33) {
                         val licenseManager = KnoxEnterpriseLicenseManager.getInstance(context)
                         val activationInfo = licenseManager.licenseActivationInfo

                         //activationInfo.activationDate
                         //activationInfo.packageName

                         if (activationInfo != null) {
                             Log.msg(TAG, "getState(): [" + activationInfo.state + "]")
                             bActived = activationInfo.state == ActivationInfo.State.ACTIVE
                         } else {
                             //Aun no se activa la licencia,
                             Log.msg(TAG, "[isLicenseActived] activationInfo == NULL - AUN NO SE ACTIVA LA LICENCIA")
                         }
                     } else {
                         //bActived = SettingsApp.islicensed()
                        // bActived =  Settings.getSetting(Cons.KEY_LICENSE_KNOX_ACTIVATED,false)
                         bActived =  KnoxStatus.isLicensedActived
                         if (aPILevel == EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_NOT_SUPPORTED) bActived = true
                     }
                 } else
                     bActived = true
             } catch (ex: Exception) {
                 ErrorMgr.guardar(TAG, "isLicenseActived", ex.message)
             }
             return bActived
         }*/

    private fun startCountDown(segs: Long) {
        val milisegs = segs * 1_000
        bCancel = false
        object : CountDownTimer(milisegs, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    if(bCancel)
                        this.cancel()
                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "startCountDown.onTick", ex.message)
                }
            }

            override fun onFinish() {
                //

                val bKnoxLicenseActived = SettingsApp.getSetting(Cons.KEY_ES_LICENCIA_REQUERIDA,false)
                Log.msg(TAG,"[onFinish] Revisa si ya necesita volver a mostrar el timer.. bKnoxLicenseActived: $bKnoxLicenseActived")
                if(bKnoxLicenseActived){
                    activateLicense()
                }
            }
        }.start()
    }

    //  companion object{
    val aPILevel: Int
        get() {
            Log.msg(TAG,"[aPILevel.get] Product: ${Build.PRODUCT}")
            var level =EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_NOT_SUPPORTED
            //Si el telefono no tiene knox, la app hace crash
            if(modelosSinKnox.contains(Build.PRODUCT)){
                Log.msg(TAG,"[aPILevel.get] se sale, no soporta knox")
                return level
            }

            try {
                level = EnterpriseDeviceManager.getAPILevel()
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "getAPILevel.get[0]", ex.message)
            }
            catch (e:NoSuchMethodError) {
                ErrorMgr.guardar(TAG, "getAPILevel.get[1]", e.message,false)
            }
            return level
        }


    val knoxVersion: String
        get() {
            //Si el telefono no tiene knox, la app hace crash
            if(modelosSinKnox.contains(Build.PRODUCT)){
                return "	Knox not supported"
            }

            val knoxLevel = aPILevel // EnterpriseDeviceManager.getAPILevel()
            var version = ""
            try {
                version = when (knoxLevel) {
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.BASE -> "KKnox 1.0 - API level 6"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_1_0_1 -> "KKnox 1.0.1 - API level 7"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_1_0_2 -> "Knox 1.0.2 - API level 8"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_1_1 -> "Knox 1.1 - API level 9"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_1_2 -> "Knox 1.2 - API level 10"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_0 -> "Knox 2.0 - API level 11"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_1 -> "Knox 2.1 - API level 12"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_2 -> "Knox 2.2 - API level 13"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_3 -> "Knox 2.3 - API level 14"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_4 -> "Knox 2.4 - API level 15"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_4_1 -> "Knox 2.4.1 - API level 16"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_5 -> "Knox 2.5 - API level 17"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_5_1 -> "Knox 2.5.1 - API level 18"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_6 -> "Knox 2.6 - API level 19"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_7 -> "Knox 2.7 - API level 20"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_7_1 -> "Knox 2.7.1 - API level 21"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_8 -> "Knox 2.8 - API level 22"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_2_9 -> "Knox 2.9 - API level 23"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_0 -> "Knox 3.0 - API level 24"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_1 -> "Knox 3.1 - API level 25"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_2 -> "Knox 3.2 - API level 26"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_2_1 -> "Knox 3.2.1 - API level 27"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_3 -> "Knox 3.3 - API level 28"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_4 -> "Knox 3.4 - API level 29"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_4_1 -> "Knox 3.4.1 - API level 30"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_5 -> "Knox 3.5 - API level 31"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_6 -> "Knox 3.6 - API level 32"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_7 -> "Knox 3.7 - API level 33"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_7_1 -> "Knox 3.7.1 - API level 34"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_3_8 -> "Knox 3.8 - API level 35"
                    EnterpriseDeviceManager.KNOX_VERSION_CODES.KNOX_NOT_SUPPORTED -> "Knox not supported"
                    else -> "default - knoxLevel = $knoxLevel"
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "getKnoxVersion", ex.message)
            }
            return version
        }
    //  }

}
