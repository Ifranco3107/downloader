package com.macropay.downloader.utils.mqtt;

public class ConnectMQTT {

}

/*
import android.content.Context;

import com.macropay.dpcmacro.utils.network.ConnectionStatus;
import com.macropay.utils.phone.DeviceCfg;
import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;
import com.macropay.dpcmacro.data.preferences.MainApp;

import org.eclipse.paho.mqttv5.common.MqttException;

public class ConnectMQTT {
    private static ClientMQTT clientMqtt;
    private static final String TAG = "ConnectMQTT";
    private static boolean created = false;

    public static void connectAndSubscribe() throws Exception {
        Log.msg(TAG,"connectAndSubscribe-1-");
        clientMqtt.connectBroker();
        Log.msg(TAG,"connectAndSubscribe-2-");
        subscribe();
        Log.msg(TAG,"connectAndSubscribe-3-");
    }

    public static void reconnectBroker() throws MqttException {
        Log.msg(TAG,"reconnectBroker");
        if(clientMqtt == null)
            Log.msg(TAG,"reconnectBroker - clientMqtt == null ");

        clientMqtt.reconnectBroker();
    }

    public static void subscribe() throws Exception {
        String modelTopic = "lock/".concat(DeviceCfg.getImei(MainApp.getMainCtx()) );
        //+" clientMqtt.isConnected(): "+clientMqtt.isConnected()
        if(!modelTopic.isEmpty()) {
            if(clientMqtt == null)
                ErrorMgr.guardar(TAG,"subscribe","clientMqtt == null");
            else{
                Log.msg(TAG,"[subscribe] subscribeNewTopic("+modelTopic+")");
                clientMqtt.subscribeNewTopic(modelTopic);
            }

        } else {
            Log.msg(TAG,"[subscribe] Error al obtener topic");
        }
    }
    public static void unsubscribe() throws Exception {
        String modelTopic = "lock/".concat(DeviceCfg.getImei(MainApp.getMainCtx()) );
        //+" clientMqtt.isConnected(): "+clientMqtt.isConnected()
        if(!modelTopic.isEmpty()) {
            if(clientMqtt == null)
                ErrorMgr.guardar(TAG,"unsubscribe","clientMqtt == null");
            else{
                Log.msg(TAG,"[unsubscribe] Topic("+modelTopic+")");
                clientMqtt.unsubscribe(modelTopic);
            }

        } else {
            Log.msg(TAG,"[unsubscribe] Error al obtener topic");
        }
    }

//TODO revisar conexion..
    public static void connect(Context context,String source) {
      //  Log.msg(TAG,"createNetworkClient() - Inicio");
    //    MainApp.setMainCtx(context);
        if(clientMqtt == null) {
            Log.msg(TAG,"[connect] Va crear el objeto. - clientMqtt ["+source+"]");
            //clientMqtt = new ClientMQTT(context);
        }

       // boolean hasNetwork = NetworkMQTT.isConnected();
        boolean hasNetwork =  ConnectionStatus.isOnline();
        Log.msg(TAG,"[connect] hasNetwork: "+hasNetwork);
        if(hasNetwork){
            Log.msg(TAG,"[connect] Si hay red - Va ejecutar: connectProcess() ");
            connectProcess();
        }
    }
//TODO: Codigo de Paquito, 16FEB22
    public static void connectProcess() {
        if(clientMqtt == null)
        {
            Log.msg(TAG, "[connectProcess] En proceso de conexion...clientMqtt == null - Se sale para evitar conflico con la conexion en proceso.");
            return;
        }
        if (clientMqtt.isbConnecting()){
            Log.msg(TAG, "[connectProcess] En proceso de conexion... isbConnecting() - Se sale para evitar conflico con la conexion en proceso.");
            return;
        }
        // Log.msg(TAG, "En  1");
        boolean hasNetwork = ConnectionStatus.isOnline();
        // Log.msg(TAG, "En  2");
        if(!hasNetwork)
        {
            Log.msg(TAG, "[connectProcess] hasNetwork: "+hasNetwork +" Se sale del networkConnect.");
            return;
        }

        //----------------------
        if (!created) {
            created = true;
            try {
                Log.msg(TAG, "[connectProcess] INICIALIZA CONEXION - connectAndSubscribe();");
                connectAndSubscribe();
            } catch (Exception e) {
                created = false;
                ErrorMgr.guardar(TAG, "connectProcess- created:"+created , e.getMessage());
            }
        } else {
            Log.msg(TAG, "[connectProcess] REINTENTA CONEXION - retryConnect();");
            retryConnect();
        }
    }

    private static void retryConnect(){
        Log.msg(TAG, "[retryConnect] created; " + created);
        boolean bConected = false;
        if(clientMqtt != null)
            bConected =clientMqtt.isConnected();

        if (bConected){
            Log.msg(TAG, "[retryConnect] Cliente MQTT ya conectado");
            return;
        }

        try {
            reconnectBroker();
            //subscribe();
        } catch (Exception e) {
            ErrorMgr.guardar(TAG, "[connectProcess] [CATCH] " , e.getMessage());
            int l= 0;
            try {
                Log.msg(TAG,"[retryConnect][CATCH] Error: "+e.getMessage());
                Log.msg(TAG,"[retryConnect][CATCH]   - RESET CONEXION - Created = false");
                created = false;
                //clientMqtt.close(false);
                clientMqtt.forceDisconnect();
                l= 1;
            } catch (Exception mqttException) {
                ErrorMgr.guardar(TAG, "[retryConnect][CATCH]["+l+"]" , mqttException.getMessage());
            }
        }
    }

    public static boolean isConnected()
    {
        boolean bStatus = false;
        try {
        if (clientMqtt != null)
            bStatus=   clientMqtt.isConnected();
        else
            Log.msg(TAG,"*** clientMqtt == NULL");
        } catch (Exception e) {
            ErrorMgr.guardar(TAG, "isConnected", e.getMessage());
        }
        return bStatus;
    }

    public static void close(){
        clientMqtt.close(true);
    }
}
*/