package com.macropay.downloader.data.preferences;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Telephony;
import android.telecom.TelecomManager;

import com.macropay.downloader.ui.provisioning.EnrollActivity;
import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;

/*

                    ya no se usa, se distribuyo en Cons,Defaults

*/
public class MainApp {

static String TAG = "MainApp";
//192.168.162.62
/*
    //==========================================
    //     Produccion
    //==========================================
    private  static final String serverAddress = "https://lockmacropay.mx"; //+""192.168.162.62";

    private static final String servidorHTTP = serverAddress;
    private static final String servidorMqtt = "tcp://lockmacropay.mx:1773";

    private static final String packageName = "com.grupomacro.macropay";
    private static final String location = serverAddress+"/lock/uploads/grupomacroapp.apk";

    // private static final String packageNameUpdater = "com.macropay.updater";
    // private static final String locationUpdater = serverAddress+"/uploads/app-release.apk";
  */
    //==========================================
    //Desarrollo
    //==========================================
    private static final String servidorHTTP = "http://45.190.236.239:8888";
    private static final String servidorMqtt = "tcp://45.190.236.239:1773";

    private static final String packageName = "com.grupomacro.macropay";
    private static final String location = "/lock/uploads/grupomacroapp.apk";

    // private static final String packageNameUpdater = "com.macropay.updater";
    // private static final String locationUpdater = "http://45.190.236.239:8888/lock/uploads/app-release.apk";
    public static final String packageNameManual  = "com.macropay.macropaguitos";

    public static final String EULA_DEFAULT = "What is Lorem Ipsum?\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.\n" +
            "\n" +
            "Why do we use it?\n" +
            "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).\n" +
            "\n" +
            "\n" +
            "Where does it come from?\n" +
            "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32.\n" +
            "\n" +
            "The standard chunk of Lorem Ipsum used since the 1500s is reproduced below for those interested. Sections 1.10.32 and 1.10.33 from \"de Finibus Bonorum et Malorum\" by Cicero are also reproduced in their exact original form, accompanied by English versions from the 1914 translation by H. Rackham.";

    public static final String BIENVENIDA_DEFAULT = "!!Bienvenido a MacroPay !!!\n"+
            "Gracias por tu compra...\n"+
            "Esperamos darte la mejor Experiencia.";

    public static final String TEXT_BLOQUEO_DEFAULT = "Hola **@nombreCliente**, detectamos que tu equipo fue bloqueado por falta de pago.\n" +
            "No pierdas la comunicación,\n" +
            "puedes pagar:\n" +
            "Desde la APP en la opción [Paga en Línea]\n" +
            "en nuestras tiendas Macropay\n" +
            "en la tienda de tu preferencia\n" +
            "y sigue disfrutando de los beneficios de tu línea telefónica.\n" +
            "¡Te esperamos!";



    //private static final String servidorMqtt_ANT = "tcp://45.190.236.239:1883";

    private static String simpleModel;
    private static final String mainActivityName = "MainActivity";
    private static Context mainCtx;
    private static EnrollActivity  mainActivity;


    private static boolean locked = false;
    private static boolean kiosko = false;


    public static String getPackageName() {
        return packageName;
    }
    public static String getMainActivityName() {
        return mainActivityName;
    }

    //Ubicacion de la APP de MacroPay.
/*
    public static String getLocation() {
       // String locationAPK = "/lock/uploads/grupomacroapp.apk";
        return  getServerHttp()+location;
    }
*/

 /*   public static String getServerHttp() {

        String serverHttp = servidorHTTP;
        try{
            serverHttp = SettingsApp.getServerHttp();
            if(serverHttp.equals(""))  serverHttp = servidorHTTP;
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar("MainApp","getServerHttp",ex.getMessage());
        }
        return serverHttp;
    }*/

/*    public static String getServerMqtt() {
        String serverMqtt= servidorMqtt;
        try{
            serverMqtt = SettingsApp.getServerMqtt();
            if(serverMqtt.equals(""))  serverMqtt = servidorMqtt;
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar("MainApp","getServerMqtt",ex.getMessage());
        }
        return serverMqtt;
    }*/

    public static Context getMainCtx() {
        return mainCtx;
    }
    public static void setMainCtx(Context mainCtx) {
        MainApp.mainCtx = mainCtx;
    }
    
    public static void setMainActivity(EnrollActivity mainActivity) {
        MainApp.mainActivity = mainActivity;
    }
    public static EnrollActivity  getMainActivity() {
        return mainActivity;
    }
    
    public static void setSimpleModel(String clientId) {
        simpleModel = clientId;
    }
    public static String getSimpleModel() {
        /*

        String model;
        if(Settings.statusMTTl().equals("Pendiente") ){
            model = simpleModel;
            Settings.statusEMTTQ(model);
        }
        else
            model = "testprueba9181";

         */
        return simpleModel;
    }

    public static boolean isLocked() {
        return locked;
    }

    public static void setLocked(boolean locked) {
        MainApp.locked = locked;
    }

    public static String[] getAppPermitted(Context context){
        Log.INSTANCE.msg(TAG,"[getAppPermitted] ");
        String settingsApp = "com.android.settings";
        String samsumgCallUI = "com.samsung.android.incallui";
        String samsungLicKnox = "com.samsung.android.knox.containercore";

        try {
            // String smsApp  = "com.google.android.apps.messaging";
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            String defaultDialer = telecomManager.getDefaultDialerPackage();
            String  smsApp = Telephony.Sms.getDefaultSmsPackage(context);
            if(defaultDialer == null){ defaultDialer = "com.android.telefonia";}
            if(smsApp == null){smsApp = "com.android.sms";}


            //Si viene vacio, agrega este paquete. smsApp,
            String[] appPackages = {
                    context.getPackageName(),
                    MainApp.getPackageName(),
                    defaultDialer,
                    samsumgCallUI,
                    settingsApp,
                    samsungLicKnox,

                    "com.android.phone",
                    "com.android.dialer",
                    "com.android.server.telecom",

                    "com.samsung.android.cidmanager",
                    "com.samsung.android.kgclient",
                    "com.samsung.android.lool",
                    "com.samsung.android.mdm",
                    "com.samsung.android.messaging",
                    "com.samsung.klmsagent",
                    smsApp
            };
            for (String pack:appPackages
            ) {
                if(pack== null)
                 //   ErrorMgr.guardar(TAG,"getAppPermitted",pack +" == null");
                Log.INSTANCE.msg(TAG,pack);
            }
            return appPackages;
        }catch (Exception ex){

            ErrorMgr.INSTANCE.guardar(TAG,"getAppPermitted",ex.getMessage());
            String[] appPackages = {
                    context.getPackageName()};
            return appPackages;
        }

//return appPackages;


    }

    public static boolean isAdmin(Context context){
        boolean bIsAdmin = false;
        try{
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            bIsAdmin = dpm.isDeviceOwnerApp(context.getPackageName());
/*            if (!bIsAdmin) {
                Log.e(TAG, "TestDPC is not the device owner, cannot set up COSU device.");
            }*/
        }catch ( Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"isAdmin",ex.getMessage());
        }
        return bIsAdmin;
    }

    public static Long getVersion(Context context){
        Long version = 0L;
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = packageInfo.getLongVersionCode();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String getVersionName(Context context){
        String versionName = "";
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
