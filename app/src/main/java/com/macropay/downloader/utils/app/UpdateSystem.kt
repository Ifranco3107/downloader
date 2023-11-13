package com.macropay.downloader.utils.app

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.PersistableBundle
import android.os.UserManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.macropay.downloader.data.mqtt.dto.ApkInfoRequest
import com.macropay.downloader.data.mqtt.dto.InstallApk
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.Settings.getSetting
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.Utils
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.utils.policies.Restrictions
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.reflect.Type
import javax.inject.Inject

class UpdateSystem
    @Inject constructor(@ApplicationContext val context:Context){

    val TAG = "UpdateSystem"
    //              class UpdateSystem(context: Context?) {
    var mContext: Context? = null
    var ApkDPC: ApkInfoRequest? = null

    @Inject
    lateinit var packageService: PackageService

    @Inject
    lateinit var installManager :InstallManager
    @Inject
    lateinit var restrinctions: Restrictions
    init {
        mContext = context
        //  packageService = PackageService(mContext!!)
       // mContext!!.registerReceiver(mStatusReceiver, IntentFilter(Sender.ACTION_END_UPDATER))
    }

    //Se llama en UpdatePackage, cuando llega el mensaje de actualizacion de apps.
    fun instalarApp(apksJson: String) {

        var appsToInstalljson = apksJson.replace(" ","")
        
        Log.msg(TAG, "[instalarApp] instalar: $appsToInstalljson")
        //Activa el permiso, para poder instalar aplicaciones.
        var ln = 0
        try {
           // val restrinctions = Restrictions(mContext)
            //TODO Verificar politica, para que se restaure o no al terminar de instalar.
            restrinctions.setRestriction(UserManager.DISALLOW_INSTALL_APPS, false)
            ln = 1
            //
            val gson = Gson()
            val userListType : Type = object : TypeToken<ArrayList<InstallApk>>() {}.type
            val apksToInstall = gson.fromJson<ArrayList<InstallApk>>(appsToInstalljson, userListType)


            Log.msg(TAG,"[instalarApp] 2.- "+apksToInstall.size +" apps para instalar...")

            ln = 2
            ApkDPC = null
            Utils.lastPackage = ""
            // SettingsApp.setDPCUpdated(false)
            Settings.setSetting(Cons.KEY_DPC_UPDATED,false)
            installManager.isReboot = false
            ln = 3
            for (apk in apksToInstall) {
                Log.msg(TAG,"[instalarApp] instalar: eAccion:          ["+apk.accion +"]")
                Log.msg(TAG,"[instalarApp] instalar: packageName:      [" + apk.package_name+"]")
                Log.msg(TAG,"[instalarApp] instalar: downloadLocation: [" + apk.download_location+"]")
                if (apk.accion.equals( ApkInfoRequest.eAccion.instalar.name)) {
                    //Agrega a la lista de Apps para instalar.
                    installManager.addPackage(apk.package_name, apk.download_location)
                }
                if (apk.accion.equals( ApkInfoRequest.eAccion.remover.name)) {
                    packageService.blockUninstall(apk.package_name, false)
                    packageService.uninstall(apk.package_name)
                }
            }
            ln = 4
            //
            Log.msg(TAG, "[instalarApp] installManager.appsToInstall(): " + installManager.appsToInstall())
            if (installManager.appsToInstall() > 0) {
                ln = 5
                //Instala aplicaciones.
                installManager.instalar()
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "instalarApp [$ln]", ex.message)
        }
    }

    fun uninstall(packageName:String): Boolean {
        var bResult = false
        try {
            if ( packageService.isInstalled(packageName)) {

                installManager.unInstall2(packageName)
                bResult = true
            }
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "uninstall", ex.message)
        }
        return bResult
    }

    fun unInstallManual(packageName: String){
        try{
            Log.msg(TAG, "[unInstallManual] +*-+*-+*-+*-+*-+*-+*-+*-+*-+*-+*-+*-+*-+*-+*-+*-+*-+*-")
            Log.msg(TAG, "[unInstallManual] packageName: [$packageName]")
            packageService.uninstallManual(packageName)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "uninstall2", ex.message)

        }
    }
    private fun setParameters(apkDPC: ApkInfoRequest): PersistableBundle {
        var imei = ""
        var idCliente = ""
        var nombreCliente = ""
        var saldo = ""
        var kioskoEnabled = false
        var nivel = 0
        SettingsApp.Nivel()
        var no_assistance = ""
        var no_emergency = ""
        try {
            kioskoEnabled = SettingsApp.isKiosko()
            nivel = SettingsApp.Nivel().ordinal
            idCliente = getSetting("idClient", "")
            nombreCliente = getSetting("nameClient", "")
            imei = getSetting("imei", "")
            saldo = getSetting("balance", "")
            no_emergency = getSetting("no_emergency", "")
            no_assistance = getSetting("no_assistance", "")
            Log.msg(TAG, "nivel:$nivel")
            Log.msg(TAG, "isKiosko: $kioskoEnabled")
            Log.msg(TAG, "imei: $imei")
            Log.msg(TAG, "idCliente: $idCliente")
            Log.msg(TAG, "nombreCliente: $nombreCliente")
            Log.msg(TAG, "saldo: $saldo")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "[1]UpdateDPC", ex.message)
        }
        //Si es Kiosko, pasa la informacion al Updater, para que la muestre.
        //  if(SettingsApp.isKiosko())
        val lObjPersistableBundle = PersistableBundle()
        try {
            lObjPersistableBundle.putString("dpc", apkDPC.packageName)
            lObjPersistableBundle.putString("location", apkDPC.downloadLocation)
            lObjPersistableBundle.putBoolean("isKiosko", SettingsApp.isKiosko())
            lObjPersistableBundle.putInt("nivel", SettingsApp.Nivel().ordinal)
            lObjPersistableBundle.putString("idCliente", idCliente)
            lObjPersistableBundle.putString("nombreCliente", nombreCliente)
            lObjPersistableBundle.putString("imei", imei)
            lObjPersistableBundle.putString("saldo", saldo)
            lObjPersistableBundle.putString("no_assistance", no_assistance)
            lObjPersistableBundle.putString("no_emergency", no_emergency)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "setParameters", ex.message)
        }
        return lObjPersistableBundle
    }


    private val isAdmin: Boolean
        private get() {
            var bIsAdmin = false
            try {
                val dpm = mContext!!.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                bIsAdmin = dpm.isDeviceOwnerApp(mContext!!.packageName)
                if (!bIsAdmin) {
                    Log.e(TAG, "No es ADMIN.")
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "isAdmin", ex.message)
            }
            return bIsAdmin
        }

    /*  public static void notificaCentral(Context context){
        //Notifica la actualizacion a Central
        try{
            PackageService packageService = new PackageService(context);
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();


            ApplicationInfo applicationInfo = pm.getApplicationInfo( packageName, 0);
            String appName =  (String) (applicationInfo != null ? pm.getApplicationLabel(applicationInfo) :"" );
            ServerHTTP serverHTTP = new ServerHTTP(context);
            List<Package> apps = new ArrayList<>();

            int tipoApp = packageService.tipoApp(packageName);
            apps.add(new Package(packageName,appName, MainApp.getVersion(context), MainApp.getVersionName(context),tipoApp,1));
            //Log.msg("PackageReceiver.onReceive",action + " - " +packageName + " versionCode: "+versionCode + " versionName: "+versionName );
            //serverHTTP.sendPackageUpdate(context,apps);
            PostPackages postPackages = new PostPackages(context);
            postPackages.execute(apps);
        } catch (Exception ex) {
            ErrorMgr.guardar(TAG, "notificaCentral", ex.getMessage());
        }
    }*/
    /*

//Comentado durante la migracion a KOTLIN- 09sEPT22-ifa
    public static void restauraSettings(Context context, PersistableBundle bundle){

        String packageName= "";
        String versionName= "";
        Long versionCode=0L;
        boolean mIsKiosko = false;
        int nivel = 0;
        MacroPolicies.eNivel curNivel = MacroPolicies.eNivel.Bloqueo;
        int resultUpdate =0;
        String msgResult = "";

        try{
            if(!SettingsApp.initialized())
                SettingsApp.init(context);

            SettingsApp.statusEnroll(SettingsApp.status.Vendido);
            mIsKiosko = bundle.getBoolean("isKiosko",false);
            packageName = bundle.getString("packageName");
            versionName = bundle.getString("versionName");
            versionCode = bundle.getLong("versionCode",0);
            resultUpdate= bundle.getInt("resultUpdate",0);
            msgResult = bundle.getString("msgResult");
            nivel= bundle.getInt("nivel",0);

            Log.msg(TAG, "packageName " + packageName);
            Log.msg(TAG, "versionCode " + versionCode);
            Log.msg(TAG, "versionName " + versionName);
            Log.msg(TAG, "IsKiosko " + mIsKiosko);
            Log.msg(TAG, "nivel " + nivel);
            Log.msg(TAG, "resultUpdate " + resultUpdate);
            Log.msg(TAG, "msgResult " + msgResult);


            //Sino existen parametros, los carga de los que le paso el Updater
            if(SettingsApp.isNewInstallation()) {
                Log.msg(TAG,"NUEVA INSTALACION");
                String idCliente = bundle.getString("idClient");
                String nombreCliente = bundle.getString("nameClient");
                String imei = bundle.getString("imei");
                String saldo = bundle.getString("saldo");
                String no_assistance = bundle.getString("no_assistance");
                String no_emergency = bundle.getString("no_emergency");
                int mNivel = bundle.getInt("nivel",0);
                curNivel = MacroPolicies.eNivel.fromId(mNivel);

                Log.msg(TAG, "idCliente " + idCliente);
                Log.msg(TAG, "nombreCliente " + nombreCliente);
                Log.msg(TAG, "saldo " + saldo);
                Log.msg(TAG, "mNivel " + mNivel);
                Log.msg(TAG, "curNivel " + curNivel);
                SettingsApp.setKiosko(mIsKiosko);
                SettingsApp.nivelBloqueo(curNivel);
                if(imei != null)  Settings.setSetting("imei", imei);
                if(idCliente != null) Settings.setSetting("idClient", idCliente);
                if(nombreCliente != null) Settings.setSetting("nameClient", nombreCliente);
                if(saldo != null) Settings.setSetting("balance", saldo);
                if(no_assistance != null) Settings.setSetting("no_assistance", no_assistance);
                if(no_emergency != null) Settings.setSetting("no_emergency", no_emergency);
            }

        } catch (Exception ex) {
            ErrorMgr.guardar(TAG, "onTransferOwnershipComplete", ex.getMessage());
        }
    }
*/



}