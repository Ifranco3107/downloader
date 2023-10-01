package com.macropay.downloader.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.macropay.data.dto.request.PackageFile;
import com.macropay.downloader.MacroPolicies;
import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsApp {


    static SharedPreferences sharedPref = null;
    public static Context context;
    public static boolean kiosko = false ;
    public static List<PackageFile> appsUpdated = new ArrayList<>();

     static String TAG = "SettingsApp";
    public enum status{
        Pendiente, //Recien iniciado
        Enrolo,    //Registro en BD Central
        Configurando, //Aplico Politicas
        Instalado,  // sustituye a Terminado
        Vendido     //Scaneo correctamente el Codigo de Barras.
      //  Terminado  //Aolico Politicas Proceso Terminado.
    }

    public static void init(Context ctx){
        try{
         //   Log.msg(TAG,"Init");
            context  = ctx;
            sharedPref =  context.getSharedPreferences("downloader", context.MODE_PRIVATE);
        }
        catch (Exception ex)
        {
            ErrorMgr.INSTANCE.guardar(TAG,"init",ex.getMessage());
        }
    }

    public static boolean initialized(){
        return (sharedPref != null);
    }

/*   public static boolean initialized(){
            return (context != null);
    }*/

    public static boolean isNewInstallation()
    {
        boolean bNewInstalation = true;
        try{
            if(sharedPref == null) {
                ErrorMgr.INSTANCE.guardar(TAG,"isNewInstallation","sharedPref == null)");
                init(context);
            }
        }
        catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"isNewInstallation",ex.getMessage());
        }
        return bNewInstalation;
    }
    public static SettingsApp.status statusEnroll()
    {
        SettingsApp.status status = SettingsApp.status.Instalado;
        try{
            if(sharedPref == null) {
                ErrorMgr.INSTANCE.guardar(TAG,"GET statusEnroll","sharedPref == null)");
                init(context);
            }

            String statusName= sharedPref.getString("statusEnroll", "Pendiente") ;
            status= SettingsApp.status.valueOf(statusName);
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"statusEnroll",ex.getMessage());
        }

      return status;
    }

    public static void statusEnroll(SettingsApp.status status)
    {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG,"statusEnroll","sharedPref == null)");
            init(context);
        }

        Log.INSTANCE.msg(TAG,"se cambio statusEnroll a: "+status);
        try{
            String statusName = status.name();
            SharedPreferences.Editor editor= sharedPref.edit();
            editor.putString("statusEnroll",statusName);
            editor.commit();
        }
        catch (Exception ex)
        {
            ErrorMgr.INSTANCE.guardar(TAG,"statusEnroll",ex.getMessage());
        }
    }

    public static boolean isEnrolmentProcess(){
        return  ( SettingsApp.statusEnroll() == SettingsApp.status.Enrolo );
    }

    //
    public static MacroPolicies.eNivel Nivel()
    {
        MacroPolicies.eNivel curNivel = MacroPolicies.eNivel.Inicial;
        try{
            if(sharedPref == null) {
                ErrorMgr.INSTANCE.guardar(TAG,"Nivel()","sharedPref == null)");
                init(context);
            }
            String nivelName= sharedPref.getString("nivelBloqueo", "Inicial") ;
            curNivel= MacroPolicies.eNivel.valueOf(nivelName);
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"nivelBloqueo",ex.getMessage());
            curNivel = MacroPolicies.eNivel.Bloqueo;
        }
        return curNivel;
    }

   /* public static void nivelBloqueo(MacroPolicies.eNivel newNivel)
    {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "nivelBloqueo", "sharedPref == null)");
            init(context);
        }

        //Si ya fue liberado, ya no debe permitir cambar de estado... Sin Permisos...
        if (Nivel() == MacroPolicies.eNivel.Liberar)
        {
            ErrorMgr.INSTANCE.guardar(TAG,"nivelBloqueo","No se permite asignar el nivel:[" + newNivel + "] porque el telefono ya esta liberado.");
            return;
        }
       // Log.msg(TAG,"++++++++++ -----> se cambio nivel TipoBloqueo a: ["+newNivel+"]");
        try{
            String nivelName = newNivel.name();
            SharedPreferences.Editor editor= sharedPref.edit();
            editor.putString("nivelBloqueo",nivelName);
            editor.commit();
        }
        catch (Exception ex)
        {
            ErrorMgr.INSTANCE.guardar(TAG,"statusEnroll",ex.getMessage());
        }
    }*/
    //ConfirmoQR

    public static boolean isKiosko() {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "isKiosko", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getBoolean("kiosko", false) ;
    }

    public static void setKiosko(boolean kiosko) {
      //  Log.msg(TAG,"setKiosko: "+kiosko);
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "setKiosko", "sharedPref == null)");
            init(context);
        }
        try{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("kiosko",kiosko);
            editor.apply();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"setKiosko",ex.getMessage());
        }
    }
    /*public static String getServerHttp() {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "getServerHttp", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getString("serverHttp", "") ;
    }
*/
  /*  public static void setServerHttp(String server) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "SetServerHttp", "sharedPref == null)");
            init(context);
        }
        try{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("serverHttp",server);
            editor.apply();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"setServerHttp",ex.getMessage());
        }
    }*/
   /* public static String getServerMqtt() {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "getServerMqtt", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getString("ServerMqtt", "") ;
    }

    public static void setServerMqtt(String server) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "SetServerMqtt", "sharedPref == null)");
            init(context);
        }
        try{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("ServerMqtt",server);
            editor.apply();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"setServerMqtt",ex.getMessage());
        }
    }
*/
    // Int
    public static int getSetting(String key,int valDefault) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "getSetting", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getInt(key, valDefault) ;
    }

    public static void setSetting(String key,int value) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "setSetting", "sharedPref == null)");
            init(context);
        }
        try{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(key,value);
            editor.apply();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"int setSetting",ex.getMessage());
        }
    }
//Long
    public static Long getSetting(String key,Long valDefault) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "getSetting", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getLong(key, valDefault) ;
    }

    public static void setSetting( String key,Long value) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "setSetting", "sharedPref == null)");
            init(context);
        }
        try{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(key,value);
            editor.apply();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"Long setSetting",ex.getMessage());
        }
    }
    // Boolean
    public static Boolean getSetting(String key,Boolean valDefault) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "getSetting", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getBoolean(key, valDefault) ;
    }

    public static void setSetting( String key,Boolean value) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "setSetting", "sharedPref == null)");
            init(context);
        }
        try {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(key, value);
            editor.apply();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"boolean setSetting",ex.getMessage());
        }
    }

    // String
    public static String getSetting(String key,String valDefault) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "getSetting", "sharedPref == null)");
            init(context);
        }
        String result = "";
        try{
            result =  sharedPref.getString(key, valDefault) ;

        } catch(Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"String getSetting",ex.getMessage());
        }
        return result;
    }

    public static void setSetting(String key, String value) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "putDataSharedPref", "sharedPref == null)");
            init(context);
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key,value);
        editor.apply();
    }
    // String[]
    public static String[] getSetting(String key, String[] valDefault) {
        String[] valores =  valDefault;
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "getSetting", "sharedPref == null)");
            init(context);
        }
        Set<String> result = null;
        try{
            result = sharedPref.getStringSet(key, null);
            if(result != null)
                valores = result.toArray(new String[0]);
        }
        catch(Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"String[] getSetting",ex.getMessage());
        }
        return valores;
    }

    public static void setSetting(String key, String[] myArray) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "putDataSharedPref", "sharedPref == null)");
            init(context);
        }
        try{
            Set<String> mySet = new HashSet<String>(Arrays.asList(myArray));
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putStringSet(key,mySet);
            editor.apply();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"String[] setSetting",ex.getMessage());
        }
    }
    // LocalDateTime
    public static LocalDateTime getSetting(String key,LocalDateTime valDefault)
    {
        //Convirte a LocalDatetime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String strFecha=  sharedPref.getString(key, "");
        LocalDateTime dteFecha = valDefault;   //LocalDateTime.now();
        try {
            if (!strFecha.equals(""))
                dteFecha = LocalDateTime.parse(strFecha, formatter);
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"LocalDateTime getSetting",ex.getMessage());
        }
        return dteFecha ;
    }

    public static void setSetting(String key,LocalDateTime dteFecha)
    {
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String strFecha =   dteFecha.format(formatter);
            SharedPreferences.Editor editor= sharedPref.edit();
            editor.putString(key, strFecha);
            editor.commit();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"LocalDateTime setSetting",ex.getMessage());
        }
        return  ;
    }
    //
    public static boolean isAppsDownloaded() {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "appsDownloaded", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getBoolean("appsDownloaded", false) ;
    }

    public static void setAppsDownloaded(boolean bAppsDownloaded) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "setAppsDownloaded", "sharedPref == null)");
            init(context);
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("appsDownloaded",bAppsDownloaded);
        editor.apply();
    }
    public static boolean isGPSPermissionEnabled() {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "isGPSPermissionEnabled", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getBoolean("GPSPermissionEnabled", false) ;
    }

    public static void setGPSPermissionEnabled(boolean bPermissionsEnabled) {
        try {
            if (sharedPref == null) {
                ErrorMgr.INSTANCE.guardar(TAG, "setGPSPermissionEnabled", "sharedPref == null)");
                init(context);
            }
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("GPSPermissionEnabled", bPermissionsEnabled);
            editor.apply();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG, "setGPSPermissionEnabled", ex.getMessage());
        }
    }

    public static boolean permissionsAsigned() {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "permissionsAsigned", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getBoolean("permissionsAsigned", false) ;
    }

    public static void setPermissionsAsigned(boolean bpermissionsAsigned) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "setPermissionsAsigned", "sharedPref == null)");
            init(context);
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("permissionsAsigned",bpermissionsAsigned);
        editor.apply();
    }

    public static boolean canReboot(){
        boolean bREsult = isAppsDownloaded() && permissionsAsigned();
      //  bREsult = true;
        return bREsult;
    }



    public static LocalDateTime ultimaNotificacion()
    {
        //Convirte a LocalDatetime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String strFecha=  sharedPref.getString("ultimaNotificacion", "");
        LocalDateTime dteFecha = null;   //LocalDateTime.now();
        if (!strFecha.equals(""))
            dteFecha = LocalDateTime.parse(strFecha, formatter);
        return dteFecha ;
    }

    public static void setUltimaNotificacion(LocalDateTime dteFecha)
    { DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String strFecha =   dteFecha.format(formatter);
        SharedPreferences.Editor editor= sharedPref.edit();
        editor.putString("ultimaNotificacion", strFecha);
        editor.commit();
        return  ;
    }
/*    public static LocalDateTime ultimoEnvioGPS()
    {
        //Convirte a LocalDatetime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String strFecha=  sharedPref.getString("ultimoEnvioGPS", "");
        LocalDateTime dteFecha = null;   //LocalDateTime.now();
        if (!strFecha.equals(""))
            dteFecha = LocalDateTime.parse(strFecha, formatter);
        return dteFecha ;
    }*/

    public static void setUltimoEnvioGPS(LocalDateTime dteFecha)
    { DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String strFecha =   dteFecha.format(formatter);
        SharedPreferences.Editor editor= sharedPref.edit();
        editor.putString("ultimoEnvioGPS", strFecha);
        editor.commit();
        return  ;
    }

    public static LocalDateTime inicioUpdater()
    {
        //Convirte a LocalDatetime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String strFecha=  sharedPref.getString("inicioUpdater", "");
        LocalDateTime dteFecha = null;   //LocalDateTime.now();
        if (!strFecha.equals(""))
            dteFecha = LocalDateTime.parse(strFecha, formatter);
        else
            dteFecha = null;
        return dteFecha ;
    }

    public static void setinicioUpdater(LocalDateTime dteFecha)
    {  String strFecha = "";
        if(dteFecha != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            strFecha = dteFecha.format(formatter);
        }
        SharedPreferences.Editor editor= sharedPref.edit();
        editor.putString("inicioUpdater", strFecha);
        editor.commit();
     //   Log.msg(TAG,"inicioUpdater: "+ strFecha);
        return  ;
    }


    public static boolean isUpdating() {
        LocalDateTime inicioUpdater = SettingsApp.inicioUpdater();
        boolean bUpdating = false;
        try{
            //Sino es nulo, es porque esta activo el updater.
            if(inicioUpdater!= null)
            {
                Long minsUpdater = Utils.tiempoTranscurrido(inicioUpdater, ChronoUnit.MINUTES);
                if(minsUpdater < 10)  {
                    Log.INSTANCE.msg(TAG,"Updater en proceso...[Se sale del monitor...] mins: "+minsUpdater);
                    bUpdating = true;
                }
            }
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"isUpdating",ex.getMessage());
        }
        return bUpdating;
    }
/*    public static boolean islicensed() {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "islicensed", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getBoolean("licensed", false) ;
    }

    public static void setLicensed(boolean licenced) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "setLicensed", "sharedPref == null)");
            init(context);
        }
        try{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("licensed",licenced);
            editor.apply();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"setLicensed",ex.getMessage());
        }

    }*/
/*
    public static boolean isDPCUpdated() {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "isDPCUpdated", "sharedPref == null)");
            init(context);
        }
        return sharedPref.getBoolean("DPCUpdated", false) ;
    }

    public static void setDPCUpdated(boolean licenced) {
        if(sharedPref == null) {
            ErrorMgr.INSTANCE.guardar(TAG, "setDPCUpdated", "sharedPref == null)");
            init(context);
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("DPCUpdated",licenced);
        editor.apply();
    }*/

/*
    public static void createBan(String status) {
        //com.macropay.dpcmacro.fileprovider

      //  content://com.macropay.dpcmacro.fileprovider/allfiles/default_image.json

        String filename = "updater.txt";
        File logFile=new File(context.getExternalFilesDir("/"), filename);
       // File logFile =  storageDir ; //new File(storageDir);
        try
        {
            logFile.createNewFile();
            Date date = Calendar.getInstance().getTime();
            // SimpleDateFormat            formatter = new SimpleDateFormat("EEEE,hh:mm:ss a");

        String text = "{\"updater\":" +status +"}" ;
        String tag = "shared";
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            String today = formatter.format(date);
            text = today + " | " +tag + " | " + text;

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
           Log.msg(tag,"Genero Bandera en : " + logFile.getAbsolutePath() );
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
*/


}
