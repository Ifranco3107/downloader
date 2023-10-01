package com.macropay.downloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;


//import com.macropay.dpcmacro.data.remote.requests.PostLiberar;
import com.macropay.downloader.utils.samsung.KnoxConfig;
import com.macropay.downloader.utils.phone.PhoneMgr;

import com.macropay.downloader.utils.policies.Restrictions;
import com.macropay.downloader.utils.location.LocationDevice;
import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;


import org.json.JSONArray;

@SuppressLint("SetTextI18n")
public class MacroPolicies {
    private Context mContext ;
    private TextView txtStatus = null;
    private String TAG = "MacroPolicies";
    private PhoneMgr phoneMgr = null;
    JSONArray apps = null;
    String param= "";
    String transId= "";
    String userId= "";
    Handler handlerLock = null;
    Handler handlerPolicy = null;
    Restrictions restrinctions;
    LocationDevice locationDevice = null;
    KnoxConfig knoxConfig;
/*    public enum eNivel{
        Inicial,
        TipoBloqueo,
        Desbloqueo,
        BloqueoAutomatico,
        DesbloqueoAutomatico
    }*/

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public enum eNivel{
        Inicial("Inicial", 0),
        Bloqueo("TipoBloqueo", 1),
        Desbloqueo("Desbloqueo", 2),
        BloqueoPorNoConexion("BloqueoPorNoConexion", 3),
        DesbloqueoPorNoConexion("DesbloqueoPorNoConexion",4),
        BloqueoPorCambioSIM("BloqueoPorCambioSIM", 5),
        DesbloqueoPorCambioSIM("DesbloqueoPorCambioSIM",6),
        Liberar("Liberar",7);

        //Funcionalidad adicional
        private String nombre;
        private int nivel;
        //Constructor
        eNivel(String nombre, int id) {
            this.nombre = nombre;
            this.nivel = id;
        }
        eNivel() {
        }
        //Funciones.
        public int getNivel() {
            return this.nivel;
        }
        public static eNivel fromId(int id) {
            for (eNivel type : values()) {
                try {
                    if (type.getNivel() == id) {
                        return type;
                    }
                }
                catch (Exception ex){
                    ErrorMgr.INSTANCE.guardar("MacroPolicies","eNivel",ex.getMessage());
                }
            }
            return null;
        }
    }
    //
    public MacroPolicies(Context context ,TextView txtStatus ) {
        this.mContext = context;
        this.txtStatus = txtStatus;
        phoneMgr = new PhoneMgr(context);
        handlerLock = new Handler(Looper.getMainLooper());
        restrinctions = new Restrictions(mContext);
/*        if(Build.MANUFACTURER.toUpperCase().contains("SAMSUNG"))
            knoxConfig = new KnoxConfig(context);*/
    }

    private void showStatus(String  status){
        if(txtStatus!= null){
             this.txtStatus.setText(status);
        }
    }

    public void setApps(JSONArray apps) {
        this.apps = apps;
    }

    public void setParam(String param) {
        this.param = param;
    }
    public void setTransaccionId(String tranId) {
        this.transId = tranId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }


    private void activeXioami(Context context, boolean enable){
        Log.INSTANCE.msg(TAG,"MANUFACTURER: ["+Build.MANUFACTURER.toUpperCase() +"]");
        if( !Build.MANUFACTURER.toUpperCase().contains("XIAOMI"))
            return;


        Toast.makeText(context, "activeXioami", Toast.LENGTH_SHORT).show();
        //  KnoxPolicies.init(context);
        Log.INSTANCE.msg(TAG, "[activeXioami] enable: "+enable);

/*        if(handlerPolicy == null)
            handlerPolicy = new Handler();

        handlerPolicy.post(new Runnable() {
        @Override
        public void run() {*/

            try {
            /*   MIUI miui = new MIUI();
               Log.INSTANCE.msg(TAG," getVersion: "+ miui.getVersion());
               Log.INSTANCE.msg(TAG," getVersionName: "+ miui.getVersionName());
               Log.INSTANCE.msg(TAG," isMiui: "+ miui.isMiui());
               Log.INSTANCE.msg(TAG," isMiuiOptimizationEnabled: "+ miui.isMiuiOptimizationEnabled());*/
              // Log.INSTANCE.msg(TAG," isMiuiOptimizationEnabled2: "+ miui.isMiuiOptimizationEnabled2());
            } catch (Exception ex) {
                ErrorMgr.INSTANCE.guardar(TAG,"activeXioami",ex.getMessage());
            }
/*        }
        });*/
    }



   /* public void disableScrenCapturePost(boolean enabled)
    {
        //Log.INSTANCE.msg(TAG,"[2] inicia disableScrenCapturePost");
        try{
            if(handlerPolicy == null)
                handlerPolicy = new Handler(Looper.getMainLooper());
            //Handler handler = new Handler(Looper.getMainLooper());
                handlerPolicy.post(() -> this.disableScrenCapture(enabled));
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disableScrenCapturePost",ex.getMessage(),false);
        }
    }*/

   /* public void disableScrenCapture(Boolean bEnabled ) {
        //Log.INSTANCE.msg(TAG,"\t[2]inicia disableScrenCapture");
        try {
          //  Restrictions restrinctions = new Restrictions(this.mContext);
            restrinctions.disableScrenCapture(bEnabled);
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disableScrenCapture",ex.getMessage(),false);
        }
    }*/

/*    public void disabledHarwarePost(boolean enabled)
    {
        Log.INSTANCE.msg(TAG,"[3] inicia disabledHarwarePost: "+enabled);
        try{
            if(handlerPolicy == null)
                handlerPolicy = new Handler(Looper.getMainLooper());
            handlerPolicy.post(() -> this.disableHardware(enabled));
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disabledHarwarePost",ex.getMessage(),false);
        }
    }*/

  /*  public void disableHardware(Boolean bEnabled ) {
      Log.INSTANCE.msg(TAG,"[3 disableHardware] inicia: "+bEnabled);
        try {
            Restrictions restrinctions = new Restrictions(this.mContext);
            //IFA-28Nov -Se deshabilito solo para prueba
            if(MainApp.getVersionName(this.mContext).contains("dbg"))
                restrinctions.setRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER,false);
            else
                restrinctions.setRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER,bEnabled);

            restrinctions.setRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,bEnabled);
            restrinctions.setRestriction(UserManager.DISALLOW_ADJUST_VOLUME,bEnabled);
            restrinctions.setRestriction(UserManager.DISALLOW_INSTALL_APPS,bEnabled);
            restrinctions.setRestriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS,bEnabled);
            restrinctions.disableAutoTime(true);
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disableHardware",ex.getMessage(),false);
        }
    }*/

   /* public void disableAppsPost(boolean enabled, JSONArray apps)
    {
          Log.INSTANCE.msg(TAG,"[4] inicia disableLockApps: "+enabled);
        if(apps== null){
            Log.INSTANCE.msg(TAG,"disableAppsPost - apps == null");
            return;
        }
        try{
            if(handlerPolicy == null)
                handlerPolicy = new Handler(Looper.getMainLooper());
          //  Handler handler = new Handler(Looper.getMainLooper());
            handlerPolicy.post(() -> this.disableLockApps(enabled, apps));
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disableAppsPost",ex.getMessage(),false);
        }
        //Log.INSTANCE.msg(TAG,"[4] Termino disableLockApps: "+enabled);
    }*/
   /* public void disableLockApps(boolean bEnabled, JSONArray apps) {
        //  Log.INSTANCE.msg(TAG,"\t[4] inicia disableLockApps: "+bEnabled + " apps: "+apps.length());
        SuspendeApps suspendeApps = new SuspendeApps(this.mContext);
        try {
            for(int i=0; i< apps.length(); i++) {
                suspendeApps.addPackage(apps.getString(i));
            }
        } catch (JSONException ex) {
            ErrorMgr.guardar(TAG,"disableLockApps",ex.getMessage(),false);
        }
        suspendeApps.suspendeApps(bEnabled);
       // Log.INSTANCE.msg(TAG,"\t[4] Termino disableLockApps:");
    }*/



  /*  public void liberaAppsPost()
    {
         Log.INSTANCE.msg(TAG,"[4] inicia liberaAppsPost: ");
        try{
            if(handlerPolicy == null)
                handlerPolicy = new Handler(Looper.getMainLooper());
          //  Handler handler = new Handler(Looper.getMainLooper());
            handlerPolicy.post(new Runnable() {
                @Override
                public void run() {
                   // Looper.prepare();
                    try {
                        SuspendeApps suspendeApps = new SuspendeApps(mContext);
                        suspendeApps.liberaSuspendidas();
                    } catch (Exception ex) {
                        ErrorMgr.guardar(TAG,"liberaAppsPost",ex.getMessage(),false);
                    }
               //     Looper.loop();
                }

            });
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disableAppsPost",ex.getMessage(),false);
        }
        //Log.INSTANCE.msg(TAG,"[4] Termino disableLockApps: "+enabled);
    }*/


  /*  public void disablePhoneCallsPost(boolean enabled)
    {
        // Log.INSTANCE.msg(TAG,"[5] inicia disablePhoneCallsPost");
        try{
            if(handlerPolicy == null)
                handlerPolicy = new Handler(Looper.getMainLooper());
            //Handler handler = new Handler(Looper.getMainLooper());
            handlerPolicy.post(() ->  phoneMgr.disableCall(mContext,false));
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disablePhoneCallsPost",ex.getMessage(),enabled);
        }
    }*/
 /*   public void disableSafeBootPost(boolean enable)
    {
        Log.INSTANCE.msg(TAG,"inicia disableSafeBootPost enable: "+enable);
        try{
            if(handlerPolicy == null)
                handlerPolicy = new Handler(Looper.getMainLooper());
          //  Handler handler = new Handler(Looper.getMainLooper());
            handlerPolicy.post(() -> disableSafeBoot(enable));
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disableSafeBootPost",ex.getMessage(),enable);
        }
    }*/

  /*  public void disableSafeBoot(Boolean bEnabled ) {
        // Log.INSTANCE.msg(TAG,"inicia disableSafeBoot: "+bEnabled);
        if(restrinctions == null)
            restrinctions = new Restrictions(mContext);
        try{
            Log.INSTANCE.msg(TAG,"[disableSafeBoot] disableSafeBoot .");
            //Restrictions restrinctions = new Restrictions(this.mContext);
            restrinctions.setRestriction(UserManager.DISALLOW_SAFE_BOOT,bEnabled);
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disableSafeBoot",ex.getMessage(),false);
        }
    }*/



/*    public void disableFRPRestrictionsPost(boolean bEnabled)
    {
        Log.INSTANCE.msg(TAG,"inicia disableFRPRestrictionsPost enable: "+bEnabled);
        try{
            if(handlerPolicy == null) handlerPolicy = new Handler(Looper.getMainLooper());
            handlerPolicy.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.INSTANCE.msg(TAG,"disableFRPRestrictionsPost 1");
                        //Deshabilita que el usuario no pueda activar la opcion de UNKNOWN_SOURCES

// comentado para pruebas de XIOAMI
                        //IFA08Feb-Pruebas Xiaomi -Si funciono.
                        restrinctions.setRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,bEnabled); //true
                        //IFA08Feb-Pruebas Xiaomi - Si funciono.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            restrinctions.setRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY,bEnabled); //true
                        //TODO
                        restrinctions.setRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES,bEnabled); //true
                       // restrinctions.setSystemUpdatePolicy(bEnabled);
                    //    restrinctions.setRestriction(UserManager.DISALLOW_.DISALLOW_OEM_UNLOCK);
                      // OemLockManager.setOemUnlockAllowedByCarrier(boolean, byte[])
                        Log.INSTANCE.msg(TAG,"disableFRPRestrictionsPost 4");
//                        Log.INSTANCE.msg(TAG,"disableFactoryResetPost 3");
//                        //TEMPORALEMENTE SE DESACTVA PARA LOS XIAOMI.
//                        Log.INSTANCE.msg(TAG,"Build.MODEL.toUpperCase() "+Build.MODEL.toUpperCase());
//                        if(Build.MODEL.toUpperCase().contains("REDMI")
//                                || Build.MODEL.toUpperCase().contains("POCO")
//                                || Build.MODEL.toUpperCase().contains("M2102J20SG")
//                        )
//                            restrinctions.setRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES,false); //
//                        else
//                            restrinctions.setRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES,bEnabled); //true
//                        Log.INSTANCE.msg(TAG,"disableFactoryResetPost 4");
                    } catch (Exception ex) {
                        ErrorMgr.guardar(TAG,"disableFRPRestrictionsPost",ex.getMessage(),false);
                    }
                }
            });
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disableFactoryResetPost",ex.getMessage());
        }
    }*/
  /*  public void disableLocationPost(boolean bEnabled)
    {
        Log.INSTANCE.msg(TAG,"[disableLocationPost] inicia enable: "+bEnabled);
        try{
            if(handlerPolicy == null) handlerPolicy = new Handler(Looper.getMainLooper());
            handlerPolicy.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        restrinctions.setRestriction(UserManager.DISALLOW_CONFIG_LOCATION,bEnabled);
                    } catch (Exception ex) {
                        ErrorMgr.INSTANCE.guardar(TAG,"disableFRPRestrictionsPost",ex.getMessage());
                    }
                }

            });
        } catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"disableFactoryResetPost",ex.getMessage());
        }
    }*/
 /*   public void disableFRPPost(boolean bEnabled)
    {
        Log.INSTANCE.msg(TAG,"inicia disableFRPPost enable: "+bEnabled);
        try{
            if(handlerPolicy == null) handlerPolicy = new Handler(Looper.getMainLooper());
            handlerPolicy.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.INSTANCE.msg(TAG,"disableFRPPost 1");
                        Restrictions restrinctions = new Restrictions(mContext);
                        restrinctions.setRestriction(android.os.UserManager.DISALLOW_FACTORY_RESET,bEnabled); //true
                        //-->
                        FactoryReset factoryReset = new FactoryReset(mContext);
                        factoryReset.setProtection(bEnabled);
                        Log.INSTANCE.msg(TAG,"Termino disableFRPPost enable: "+bEnabled);
                    } catch (Exception ex) {
                        ErrorMgr.guardar(TAG,"disableFRPPost",ex.getMessage(),false);
                    }
                }

            });
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"disableFactoryResetPost",ex.getMessage());
        }
    }*/

/*    public void instalarApp( ) {
        try{
//            Log.INSTANCE.msg(TAG,"[instalarApp] ************************************************************************");
//            InstallManager installManager = new InstallManager(this.mContext);
//            String[] enterpriseApps = {};
//            enterpriseApps = Settings.getSetting("enterpriseApps",enterpriseApps);
//            Log.INSTANCE.msg(TAG,"****** ***** ***** Apps para instalar: "+enterpriseApps.length +" apps");
//            for(String app : enterpriseApps){
//                JSONObject appJson = new JSONObject(app);
//                String httpServer = SettingsApp.getServerHttp();
//                String location = httpServer + appJson.getString("location");
//                String packname = appJson.getString("packageName");
//
//                Log.INSTANCE.msg(TAG,"httpServer: ["+httpServer+"]");
//             //   installManager.addPackage(MainApp.getPackageName(),MainApp.getLocation());
//                Log.INSTANCE.msg(TAG,"[instalarApp] packname: "+packname + ",location: "+location);
//                installManager.addPackage(packname ,location);
//            }
//
//            installManager.setReboot(true);
//            boolean bResult = installManager.instalar();
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"instalarApp",ex.getMessage(),false);
        }
    }*/
/*
    public void bloqueaUninstall() {
        //Log.INSTANCE.msg(TAG,"inicia bloqueaUninstall: ");
        try{
            String packageName = MainApp.getPackageName();
            Log.INSTANCE.msg(TAG,"bloqueaUninstall. "+packageName);
          //  PackageService packageService = new PackageService(this.mContext);
          //  packageService.blockUninstall(packageName,true);

//            BlockUninstall blockUninstall = new BlockUninstall(this.mContext);
//            blockUninstall.block(packageName);
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"bloqueaUninstall",ex.getMessage(),false);
        }
    }*/

   /* public void activarlock(boolean bLocked ,eNivel nivel)
    {
        try{
            Log.INSTANCE.msg(TAG, "activarlock - ACTIVA NIVEL: " + nivel + " bLocked: "+bLocked);
            if (handlerLock == null) handlerLock = new Handler(Looper.getMainLooper());
            if (bLocked) {
                boolean bIsVisible = Utils.isLockedShowed();
               // if(!bIsVisible) {
                if (LockedActivity.fa == null) {
                    Log.INSTANCE.msg(TAG, "activarlock - showActivity(LockedActivity) :" + nivel);
                    handlerLock.post(() -> showActivity(LockedActivity.class, nivel));
                } else {
                    Log.INSTANCE.msg(TAG, "activarlock - YA EXISTE LA VENTANA ACTIVA NIVEL:" + nivel);
                    handlerLock.post(() ->  ((LockedActivity) LockedActivity.fa).showFragmentByBloqueo(Kiosko.eTipo.PorCredito));
                }
            }
            else{
                Log.INSTANCE.msg(TAG, "activarlock - UnlockedActivity: Post: " + nivel);
              //  showActivity(UnlockedActivity.class,nivel);
               // DeviceAdminService.timerMonitor().enabledBloqueo(false);
                handlerLock.post(() -> showActivity(UnlockedActivity.class,nivel));
                //handlerLock.postDelayed(() -> showActivity(UnlockedActivity.class,nivel),1000);
            }
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"activarlock",ex.getMessage(),false);
        }
    }*/

  /*  public void showActivity(Class activityClass ,eNivel nivel)
    {
        Log.INSTANCE.msg(TAG,"-- showActivity - nivel: "+nivel + " isLockTaskEnabled: "+Utils.isLockTaskEnabled(mContext) );
        if(LockedActivity.fa != null){
            Log.INSTANCE.msg(TAG,"-- showActivity - Ya existe LockedActivity.fa ");
        }

        try{
            Intent intentMain = new Intent(mContext,activityClass);
            intentMain.putExtra("nivel",nivel.ordinal());
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentMain.addCategory(Intent.CATEGORY_HOME);
            Log.INSTANCE.msg(TAG,"--> showActivity -> startActivity --------------------------------------> "+activityClass.getName());
            mContext.startActivity(intentMain);
        } catch(Exception ex){
            ErrorMgr.guardar(TAG,"showActivity - "+activityClass.getName(),ex.getMessage(),false);
        }
    }*/

    //Verifica si hay Activity  pendiente de mostrar, esto es por si durante la instalacion, llega algun mesaje de bloqueo
    //Generalmente cuando se enrola teniedo el SIM insertado.
/*    public void revisarDlgPendientes(){
        Log.INSTANCE.msg(TAG,"[revisarDlgPendientes] inicio");
        Log.INSTANCE.msg(TAG,"KEY_REQUIERE_VERIFICACION: "+Settings.getSetting(Cons.KEY_REQUIERE_VERIFICACION,false));
        Log.INSTANCE.msg(TAG,"KEY_SHOW_BIENVENIDA: "+Settings.getSetting(Cons.KEY_SHOW_BIENVENIDA,false));
        Log.INSTANCE.msg(TAG,"KEY_SHOW_EULA: "+Settings.getSetting(Cons.KEY_SHOW_EULA,false));
        try{
            if(Settings.getSetting(Cons.KEY_REQUIERE_VERIFICACION,false))
                Utils.activarActivity(this.mContext , QRValidationActivity.class);
            else
                if(Settings.getSetting(Cons.KEY_SHOW_EULA,false))
                    Utils.activarActivity(this.mContext , EULAActivity.class);
                else
                    if(Settings.getSetting(Cons.KEY_SHOW_BIENVENIDA,false))
                        Utils.activarActivity(this.mContext , BienvenidaActivity.class);
        }catch (Exception ex){
            ErrorMgr.guardar(TAG,"revisarDlgPendientes",ex.getMessage());
        }
    }*/
/*    private void getGPSPermmission() {
        Log.INSTANCE.msg(TAG,"[getGPSPermmission] Obtiene permisos GPS - activeLocation");
        try{
            if (Utils.SDK_INT == Build.VERSION_CODES.P) {
               // registerBroadcast();
                handlerLock.postDelayed(() -> {
                    Log.INSTANCE.msg(TAG,"[getGPSPermmission] Build.VERSION_CODES.P");
                    if(locationDevice == null)
                        locationDevice = new LocationDevice(this.mContext);
                    locationDevice.createLocationRequest_P(this.mContext,"MacroPolicies.getGPSPermmission");
                },10000);
            }
            else {
                SettingsApp.setGPSPermissionEnabled(true);
            }
        }catch (Exception ex){
            ErrorMgr.guardar(TAG,"getGPSPermmission",ex.getMessage());
        }
    }*/

  /*  public void terminaInstalacionM(Context context,String source ){
        //----
        Log.INSTANCE.msg(TAG,"+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        Log.INSTANCE.msg(TAG,"+++++++++++++++< terminaInstalacion >++++++++++++++++++++++++");

                try {
                    Log.INSTANCE.msg(TAG,"terminaInstalacion - source["+source+ "]");
                    Log.INSTANCE.msg(TAG,"Va validarisInstallationCompleted -6-");
                    //--->
                    if(isInstallationCompleted()) {
                        Log.INSTANCE.msg(TAG, "terminaInstalacion - 7 -");
                        SettingsApp.statusEnroll(SettingsApp.status.Instalado);
                        //TODO:     SettingsApp.statusEnroll(SettingsApp.status.Vendido);
                        //
                        eliminaKiosko();
                       //Deshabilita que puedan quitar los permisos de GPS
                        disableLocationPost(true);


                        Log.INSTANCE.msg(TAG, ".....................................................................");
                        Log.INSTANCE.msg(TAG, "TERMINO INSTALACION va a REBOOT");
                        Log.INSTANCE.msg(TAG, ".....................................................................");
                        String support12 = getAppVAlue(context);
                      // String support12 = packageService.getAppVAlue(context.getPackageName(),"support12");

                        Log.INSTANCE.msg(TAG,"supportAndroid12: ["+support12 +"]");
                        if(support12.equals("true"))
                            System.exit(0);
                        else
                            reboot();
                    }
                    else
                    {
                        Log.INSTANCE.msg(TAG,"*** Aun no Termina, no puede hacer reboot: ");
                       // Log.INSTANCE.msg(TAG,"*** NO pudo hacer reboot: isLicenseActived(): "+ KnoxPolicies.isLicenseActived() +" isAppsDownloaded(): "+SettingsApp.isAppsDownloaded() +" isGPSPermissionEnabled(): "+SettingsApp.isGPSPermissionEnabled());
                    }
                }catch (Exception ex){
                    ErrorMgr.guardar(TAG,"terminaInstalacion",ex.getMessage());
                }
        Log.INSTANCE.msg(TAG,"terminaInstalacion - 10 -");
    }*/
/*private String getAppVAlue(Context context){
    String sValue  ="";
    try {
        ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        Object value = (Object) ai.metaData.get("supportAndroid12");
        if(value != null)
            sValue = value.toString();
    }catch (Exception ex){
        ErrorMgr.guardar(TAG,"getAppVAlue",ex.getMessage());
    }
    return sValue;
}*/
 /*   private boolean isInstallationCompleted(){
        try{
            Boolean bKnoxLicenseActived = false;
            if (Build.MANUFACTURER.toUpperCase().contains("SAMSUNG")) {
                bKnoxLicenseActived=knoxConfig.isLicenseActived();
            }else
                bKnoxLicenseActived = true;

            Log.INSTANCE.msg(TAG,"............ isInstallationCompleted ..................") ;
            Log.INSTANCE.msg(TAG,"1.- isLicenseActived(): "+ bKnoxLicenseActived) ;
            Log.INSTANCE.msg(TAG,"2.- isAppsDownloaded(): "+SettingsApp.isAppsDownloaded());
            Log.INSTANCE.msg(TAG,"3.- isGPSPermissionEnabled(): "+SettingsApp.isGPSPermissionEnabled());

            //Verifica si ya se- Activo la Licencia de Knox
            if(!bKnoxLicenseActived)    {
                Log.INSTANCE.msg(TAG,Cons.TEXT_ESPERANDO_ACTIVACION_KNOX);
                Sender.INSTANCE.sendStatus(Cons.TEXT_ESPERANDO_ACTIVACION_KNOX);
             //   activeKnox(MainApp.getMainCtx(),true);
                return  false;
            }

            //Verifica si ya se asignaron los Permisos de GPS
            if(!SettingsApp.isGPSPermissionEnabled())    {
                Log.INSTANCE.msg(TAG,"Esperando activacion GPS");
                Sender.INSTANCE.sendStatus("Esperando activacion GPS...");
                Log.INSTANCE.msg(TAG,"Esperando activacion GPS- MSG ENVIADO");
                return false;
            }

            //Verifica si ya se terminaron de instalar las apps.
            if(!SettingsApp.isAppsDownloaded())    {
                Log.INSTANCE.msg(TAG,"Esperando descarga de apps");
                Sender.INSTANCE.sendStatus("Esperando descarga de apps...");
                Log.INSTANCE.msg(TAG,"Esperando descarga de Apps- MSG ENVIADO");
                return false;
            }
        }catch (Exception ex){
            ErrorMgr.guardar(TAG,"isInstallationCompleted",ex.getMessage());
        }

        Log.INSTANCE.msg(TAG,"isInstallationCompleted -termino- true");
        return true;
    }*/



/*    private  void eliminaKiosko(){
        Log.INSTANCE.msg(TAG,"eliminaKiosko");
        try {
            Sender.INSTANCE.sendStatus("Termino de descargar. ");

            for(int i=0;i<5;i++ ){
                Thread.sleep(200);
            }

            //Este mensaje es importante, sirve para avisarle al MainActivity para que quite el Kiosko
            Sender.INSTANCE.sendStatus(Cons.TEXT_VA_REINICIAR);

            for(int i=0;i<10;i++ ){
                Thread.sleep(200);
            }

        } catch ( InterruptedException ex) {
            ErrorMgr.guardar(TAG,"eliminaKiosko",ex.getMessage(),false);
        }
    }*/

    public void reboot(){
        try{
            Log.INSTANCE.msg(TAG,"reboot");
            Log.INSTANCE.msg(TAG,"\n\n\n\n\n\n");
            Handler handler = new Handler();
            handler.postDelayed(() ->  restrinctions.Reboot(),2000 ) ;

        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG,"reboot",ex.getMessage());
        }
    }

/*    public void enableWifi(){
        try{
            WifiManager wifi = (WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE);
            if(! wifi.isWifiEnabled() ){
                Log.INSTANCE.msg(TAG,"SE PRENDIO EL WIFI...");
                wifi.setWifiEnabled(true); // true or false to activate/deactivate wifi
            }

        } catch (Exception ex) {
            ErrorMgr.guardar(TAG,"enableWifi",ex.getMessage(),false);
        }
    }*/



/*    public  boolean isPackageInstalled( String packageName) {
        try {
            PackageManager pm = this.mContext.getPackageManager();
            pm.getPackageInfo(packageName, 0);

            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }*/
/*    boolean isUserApp(ApplicationInfo ai) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (ai.flags & mask) == 0;
    }*/

    /*private void registerBroadcast(){
        Log.INSTANCE.msg(TAG,"registerBroadcast");
        try{
            this.mContext.registerReceiver(mStatusReceiver, new IntentFilter(Utils.ACTION_STATUS_CHANGE));
        }catch (Exception ex){
            ErrorMgr.guardar(TAG,"registerBroadcast",ex.getMessage());
        }
    }
    private void unRegisterBroadcast(){
        this.mContext.registerReceiver(mStatusReceiver, new IntentFilter(Utils.ACTION_STATUS_CHANGE));
    }
    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Utils.ACTION_STATUS_CHANGE.equals(intent.getAction())) {
                return;
            }
            String msg = intent.getStringExtra("msg");
            Log.INSTANCE.msg(TAG,"ACTION_STATUS_CHANGE: "+msg);


            if(msg.contains(Utils.TEXT_GPS_ACCEPTED) ) {
                Log.INSTANCE.msg(TAG,"TEXT_GPS_ACCEPTED: ");
                terminaInstalacion(context,"broadcast");
                return;
            }

            showStatus(msg);
        }
    };
*/
/*    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void leerPropiedades( ) {

        //Build.BOARD // Motherboard
        //Build.BRAND // List Customizing
        //Build.SUPPORTED_ABIS // CPU instruction set
        //Build.DEVICE // device parameters
        //Build.DISPLAY // Display Parameters
        //Build.FINGDERPRINT // unique number public void leerPropiedades(View view ) {
        //Build.SERIAL // hardware serial number
        //Build.ID revision list //
        //Build.MANUFACTURER // hardware manufacturer
        //Build.MODEL // version
        //Build.HARDWARE // Hardware name
        //Build.PRODUCT // phone product name
        //Build.TAGS // build tags described
        //Build.TYPE // Builder Type
        //Build.VERSION.CODENAME // current codename
        //Build.VERSION.INCREMENTAL // source control version number
        //Build.VERSION.RELEASE // version string
        //Build.VERSION.SDK_INT // Android version number
        //Build.HOST // HOST value
        //Build.USER // User Name
        //Build.TIME // compile time

        // showStatus(DeviceCfg.getSystemProperty("ro.serialno","NA"));

        // showStatus("OPTIMIZATION:Str  "+DeviceCfg.getSystemProperty ("persist.sys.miui_optimization", "NA"));
        // showStatus("OPTIMIZATION:Int  "+DeviceCfg.getSystemProperty ("persist.sys.miui_optimization", 1980));
        // showStatus("OPTIMIZATION:Bool "+DeviceCfg.getSystemProperty ("persist.sys.miui_optimization", false));
        // showStatus("OPTIMIZATION:Proc "+DeviceCfg.getSystemProperty("persist.sys.miui_optimization"));
        // getprop persist.sys.miui_optimization
        Log.INSTANCE.msg(TAG+".[MacroPolicies]","No. Serie: "+ DeviceCfg.getSerialNumber());
        Log.INSTANCE.msg(TAG+".[MacroPolicies]","IMEI: "+ DeviceCfg.getImei(this.mContext));
        Log.INSTANCE.msg(TAG+".[MacroPolicies]","Telefono: "+ DeviceCfg.getPhoneNumber(this.mContext,0));
        Log.INSTANCE.msg(TAG+".[MacroPolicies]","Telefono: "+ DeviceCfg.getPhoneNumber(this.mContext));
        Log.INSTANCE.msg(TAG+".[MacroPolicies]","Fabricante: "+ Build.MANUFACTURER);
        Log.INSTANCE.msg(TAG+".[MacroPolicies]","Producto: "+ Build.PRODUCT);
        Log.INSTANCE.msg(TAG+".[MacroPolicies]","Version: "+Build.VERSION.RELEASE);
        Log.INSTANCE.msg(TAG+".[MacroPolicies]","SDK: "+ Build.VERSION.SDK_INT);
        Log.INSTANCE.msg(TAG+".[MacroPolicies]","Modeloo: "+ Build.MODEL);
    }*/

        /*    public void enabledAccesiblityPost(boolean enable)
    {
        Log.INSTANCE.msg(TAG,"inicia enabledAccesiblityPost enable: "+enable);
        try{
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> enabledAccesiblity(enable));
        } catch (Exception ex){
            ErrorMgr.guardar(TAG,"enabledAccesiblityPost",ex.getMessage(),enable);
        }
    }
    public void enabledAccesiblity(boolean enable){
        Log.INSTANCE.msg(TAG,"enabledAccesiblity enable: "+enable);
        try {
            Restrictions restrictions = new Restrictions(this.mContext);
            restrictions.enableAccessibility(enable);
        }catch (Exception ex){
            ErrorMgr.guardar(TAG,"enabledAccesiblity",ex.getMessage());
        }
    }*/
   /* private boolean isGPSEnabled()
    {
        Restrictions restrictions = new Restrictions(this.mContext);
        Boolean bStatus =   restrictions.isStatusRestriction(UserManager.DISALLOW_CONFIG_LOCATION);
        Log.INSTANCE.msg(TAG," isGPSEnabled(): "+bStatus) ;
        if (Util.SDK_INT == Build.VERSION_CODES.P) {
            //Pide permisos para Activar GPS
            if(!bStatus)
                activeLocation();
        }
        else
            bStatus= true;

        return bStatus;

    }*/
}
