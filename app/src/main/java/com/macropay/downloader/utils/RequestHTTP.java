package com.macropay.downloader.utils;

import com.android.volley.Response;

//import com.macropay.dpcmacro.MacroPolicies;

import org.json.JSONObject;

public abstract  class RequestHTTP implements Response.Listener<JSONObject>, Response.ErrorListener{
    String TAG = "RequestHTTP";

/*    public abstract void onProcessPolicies(JSONObject response);

    public String lastURI = "";
    protected Context context = null;
    final int HTTP_REINTENTOS = 3;
    final int HTTP_TIMEOUT= 15000;

    EndEnrollment enrollment = null;
    public RequestHTTP(Context context) {
        try{
            this.context = context;
            boolean hasNetwork = Red.INSTANCE.isOnline();
            enrollment = new EndEnrollment(context);
            if(!hasNetwork)
                Red.INSTANCE.enableWifi();
        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG,"RequestHTTP []",ex.getMessage());
        }
    }

    public RequestHTTP() {

    }

    public boolean sendRequest(int method,String pUrl,JSONObject postData){

      try {
          RequestQueue requestQueue = Volley.newRequestQueue(this.context);
          JsonObjectRequest request = new JsonObjectRequest(method, pUrl,
         // StringRequest request = new StringRequest( method, pUrl,
                  postData,
                  this,
                  this) {
              @Override
              public Map<String, String> getHeaders() {
                  Map<String, String> params = new HashMap<>();
                  params.put("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJhdXRob3JpdGllcyI6" +
                          "Ilt7XCJhdXRob3JpdHlcIjpcIlJPTEVfQURNSU5fR1JBTFwifV0iLCJzdWIiOiJ1c3JN" +
                          "YWNyb3BhbmVsIiwiaWF0IjoxNjMyMDg3MDMyLCJleHAiOjE2MzIyNzQyMzJ9.n_potHL" +
                          "2yN7lB4iFsdBTSUmbLmBMAEnUdo4u2MNUEJJ2-fcLqv8QbfOik4LnRZjQW81rP9YEfo-" +
                          "m1szmWrHMJA");
                  return params;
              }



          };

          request.setRetryPolicy(getRetroPolicicy());
          request.setShouldCache(false);
          requestQueue.add(request);
          return true;
      }catch (Exception ex){
          ErrorMgr.INSTANCE.guardar(TAG,"sendRequest",ex.getMessage());
          return false;
      }

    }



    public String getFormatDate() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    private void desbloquear(JSONObject response){
        //Actualiza el Setting a la fecha de hoy.
        Log.INSTANCE.msg(TAG , "[Response]\n\t"+TAG+"\t" +response.toString());
        try{
            //Actualiza de ultimo conexion a central
            SettingsApp.setUltimaNotificacion(LocalDateTime.now());
           // Log.INSTANCE.msg(TAG , "eNivel Actual: " +SettingsApp.Nivel());
            //Si tiene actualimente bloqueo automatico, lo desbloquea --- || SettingsApp.Nivel() == MacroPolicies.eNivel.DesbloqueoAutomatico

            if(Kiosko.INSTANCE.getCurrentKiosko() == Kiosko.eTipo.PorNoConexion){
                Log.INSTANCE.msg(TAG,"ENTRA EN DESBLOQUEO AUTOMATICO");
                Sender.INSTANCE.sendBloqueo(false,SettingsApp.context, Kiosko.eTipo.PorNoConexion );
             //   Sender.sendBloqueo(false,SettingsApp.context, MacroPolicies.eNivel.DesbloqueoPorNoConexion );
            }

        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"desbloquear",ex.getMessage());
        }
    }

    protected RetryPolicy getRetroPolicicy() {
        return new RetryPolicy() {
           int mCurrentRetryCount = 0;
           int retries= HTTP_REINTENTOS;
            @Override
            public int getCurrentTimeout() {
                Log.INSTANCE.msg(TAG, "getCurrentTimeout");
                return HTTP_TIMEOUT;
            }

            @Override
            public int getCurrentRetryCount() {
                Log.INSTANCE.msg(TAG, "getCurrentRetryCount :"+mCurrentRetryCount);
                //return 50000;
                return mCurrentRetryCount;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                mCurrentRetryCount++;
                Log.INSTANCE.msg(TAG, "retry ["+ mCurrentRetryCount+ "] "+ error.toString());
                if (mCurrentRetryCount >= retries) {
                    Map<String, String> Headers = Collections.emptyMap();
                    var httpServer = com.macropay.utils.Settings.getSetting(Cons.KEY_HTTP_SERVER, Defaults.SERVIDOR_HTTP)
                    String strData ="Timeout - server: "+ httpServer +", EndPoint: " +lastURI;
                    byte[] data = strData.getBytes();
                    NetworkResponse networkResponse =new NetworkResponse(HttpStatus.SC_REQUEST_TIMEOUT, data,
                            null,false,
                            SystemClock.elapsedRealtime());

                   Log.INSTANCE.msg(TAG, "retry Dispara Error... onErrorResponse ");
               //     onErrorResponse(new VolleyError("Timeout..."));
                    throw new VolleyError(networkResponse);

                }
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
            }
        };
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        Log.INSTANCE.msg(TAG,"[onErrorResponse] lastURI: ["+lastURI+"]");
        String nativeError = "";
        String errType = "";
        try {
            if(volleyError instanceof TimeoutError) errType="TimeoutError";
            if(volleyError instanceof ServerError) errType="ServerError";
            if(volleyError instanceof AuthFailureError)errType="AuthFailureError";
            if(volleyError instanceof NetworkError)errType="NetworkError";
            if(volleyError instanceof ParseError)errType="ParseError";
            if(volleyError instanceof NoConnectionError)errType="NoConnectionError";
            nativeError =volleyError.getMessage();
            Log.INSTANCE.msg(TAG,"[onErrorResponse] errType: " +errType);
            Log.INSTANCE.msg(TAG,"[onErrorResponse] getMessage: ["+nativeError+"]");
            Log.INSTANCE.msg(TAG,"[onErrorResponse] getMessage: ["+volleyError.toString()+"]");

            if(volleyError != null){

            }
            if(volleyError.networkResponse == null)
                Log.INSTANCE.msg(TAG,"networkResponse == null");
            else
                if(volleyError.networkResponse.data != null) Log.INSTANCE.msg(TAG,"networkResponse.data == null");

            if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                VolleyError responseError = new VolleyError(new String(volleyError.networkResponse.data));
                Log.INSTANCE.msg(TAG, "[onErrorResponse] status: " + volleyError.networkResponse.statusCode);
                Log.INSTANCE.msg(TAG, "[onErrorResponse] error: " + responseError.getMessage());
                Log.INSTANCE.msg(TAG, "[onErrorResponse] error: " + responseError.getCause());

                Sender.INSTANCE.sendStatus(Cons.TEXT_HTTP_ERROR + ": " + volleyError.networkResponse.statusCode + "\n" + responseError.getMessage());
                //
                if (lastURI.toLowerCase(Locale.ROOT).contains("/enrolldevice") && volleyError.networkResponse.statusCode != 200) {
                    Log.INSTANCE.msg(TAG, "*** Encendio bandera para volver a intetarlo en el Timer");
                    //Prende la bandera, para volver a intentar hacer el enrolamento en el Timer.
                    Settings.INSTANCE.setSetting("enrollamientoPendiente", true);

                    //Prende la bandera para indicar que ya termino de descargar las aplicacioens.
                    SettingsApp.setAppsDownloaded(true);

                    enrollment.terminaInstalacion(TAG);

                }
            } else {
                Log.INSTANCE.msg(TAG, "[onErrorResponse] *********** error = null  NO DEBE APARECER...===============");

           }
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"onErrorResponse",ex.getMessage());
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        Log.INSTANCE.msg(TAG,"lastURI: "+lastURI);
        Log.INSTANCE.msg(TAG,"[onResponse] response: "+response.toString());
        //Si es respuesta del erronlldevice
        try {
            if (lastURI.toLowerCase(Locale.ROOT).contains("/enrolldevice")) {
                //Recibe param
                Log.INSTANCE.msg(TAG, "** enrollDevice ** ");
//TODO;
                onProcessPolicies(response);

            }
            //Desbloquea el telefono, si estaba bloqueado por Inactividad.
            desbloquear(response);
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"onResponse",ex.getMessage());
        }
    }

    public void guardarTrx(String trxName,String content)
    {
       Log.INSTANCE.msg(TAG ,"[guardarTrx] Transaccion trxName: " +trxName);
       // Context context = this.getContext();

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
        String fileLog =trxName +"_"+ formatter.format(date)+".trx";
        fileLog = fileLog.replace(":","");
        Log.INSTANCE.msg(TAG, "GENERO Transaccion Log: " +fileLog);
        Log.INSTANCE.msg(TAG, "JSON \n" +content);
        //Define el nombre del archivo
        File storageDir=new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileLog);
        fileLog = storageDir.getAbsolutePath();

        //Graba el contenido
        File logFile = new File(fileLog);
        try
        {
            if (!logFile.exists())
                logFile.createNewFile();

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(content );
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
           ErrorMgr.INSTANCE.guardar(TAG,"",e.getMessage());
        }
    }   //fin guardadrTrx


    //
    public void enviaTxtPendientes(){
        if(!Red.INSTANCE.isOnline())
            return;

        try {
            File[] files = getFilesTrx();
            if(files == null) return;

           // Log.INSTANCE.msg(TAG, files.length + " files por pendientes...");
            for (File fileTrx : files) {
                procesaTrx(fileTrx);
            }
        }catch ( Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"enviaTxtPendientes",ex.getMessage());
        }
    }

    public File[]  getFilesTrx(){
        File[] files = null;
        try{
          //  File sdCardRoot = Environment.getExternalStorageDirectory();
            File storageDir;
            storageDir = new File( String.valueOf( context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
            //Buscar los archivos *.trx
            files = storageDir.listFiles((d, nameFile) -> nameFile.endsWith(".trx"));
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        } catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"",ex.getMessage());
        }
        return files;
    }


    private void procesaTrx( File f) {
        String name = f.getName();
        //
        if (!f.canRead()) {
            Log.INSTANCE.msg(TAG,"No se pudo leer: "+f.getAbsolutePath());
            return;
        }

        Log.INSTANCE.msg(TAG,"[enviaPendientes] - procesando...- " + f.getName());
        try {
            String claveTrx = name.substring(0, 3);
            String strJSON = FileMgr.INSTANCE.loadFile(f.getName(),context);
            if(strJSON.equals("")){
                Log.INSTANCE.msg(TAG,"No se pudo leer Archivo "+f.getAbsolutePath() + " o esta vacio.");
                return;
            }


            JSONObject postData = new JSONObject(strJSON);
            Log.INSTANCE.msg("[enviaPendientes]", "transaccion: [" +claveTrx + "]\n" + strJSON);

            if (claveTrx.equals("GPS")){
                lastURI = "/lock/api/mobile/sendDeviceLocation";
                String postUrl = MainApp.getServerHttp() +lastURI;
                sendRequest(Request.Method.POST, postUrl, postData);
            }

            Log.INSTANCE.msg("[enviaPendientes]", "[enviaPendientes] ------> va Borraar el archivo:" + name + " ------+++++");
            FileMgr.INSTANCE.eliminarArchivo(f);
        } catch (JSONException e) {
            ErrorMgr.INSTANCE.guardar(TAG,"procesaTrx",e.getMessage());
        }
    }*/
}
