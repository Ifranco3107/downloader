package com.macropay.downloader.utils.mqtt;

public class ClientMQTT{

}

/*

import android.content.Context;

import com.macropay.dpcmacro.data.mqtt.messages.MsgProxyMQTT;
import com.macropay.dpcmacro.utils.network.ConnectionStatus;
import com.macropay.utils.phone.DeviceCfg;
import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;
import com.macropay.dpcmacro.data.preferences.MainApp;


import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.json.JSONException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ClientMQTT implements MqttCallback{
    private static boolean bProcessing = false;
    private boolean bConnecting = false;
    private final String brokerUrl = MainApp.getServerMqtt();
    private final String clientId = DeviceCfg.getImei(MainApp.getMainCtx());

    private final String TAG_Callback = "CallbackMQTT";
    private final MsgProxyMQTT msgProxyMQTT;
    private final MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient mqttClient;
    private Context context;
    private String TAG = "ClientMQTT";
    LocalDateTime dteUltimoEvento = LocalDateTime.now();

    public ClientMQTT (Context context) {
            this.context = context;
            msgProxyMQTT = new MsgProxyMQTT(context);
    }
    //TODO: Codigo de Paquito, 16FEB22
    public void connectBroker() throws MqttException {
        Log.msg(TAG,"<>+<>-<>+<>-<>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<>");
        Log.msg(TAG,"<>+<>              INICIA MQTT -connectBroke                             <>+<>");
        Log.msg(TAG,"<>+<> brokerUrl:"+brokerUrl +" clientId: "+clientId );
        Log.msg(TAG,"<>+<>-<>+<>-<>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<>");
        mqttClient = new MqttClient(brokerUrl,clientId,persistence);
        MqttConnectionOptions connOpts = new MqttConnectionOptions();
        connOpts.setCleanStart(false);
        //connOpts.setKeepAliveInterval(60);
        //connOpts.setSessionExpiryInterval(null); //Comentado por Paquito 16FEB22
        connOpts.setSessionExpiryInterval(0xFFFFFFFFL);
        connOpts.setUserName("DEVICE_SUB");
        connOpts.setPassword("3A235A64E6C438BC5D3B53F63ED779438BB34E2347CA8A73C44822CFC9B929C8".getBytes(StandardCharsets.UTF_8));
        //connOpts.setConnectionTimeout();
        connOpts.setAutomaticReconnect(false); //
        //connOpts.setAutomaticReconnectDelay(10,60); //Agregado 16Dic2021 -FLA,IFA
        mqttClient.connect(connOpts);
        mqttClient.setCallback(this);
        Log.msg(TAG,"Mqtt Connected to broker: " + brokerUrl + " with ClientID " + clientId);
        Log.msg(TAG,"<>+<>-<>+<>-<>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<>");
        Log.msg(TAG,"<>+<>                       TERMINO MQTT                                  <>+<>");
        Log.msg(TAG,"<>+<>-<>+<>-<>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<>");
    }

  public void connectBroker() throws MqttException {
        Log.msg(TAG,"<>+<>-<>+<>-<>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<>");
        Log.msg(TAG,"<>+<>              INICIA MQTT -connectBroke                             <>+<>");
        Log.msg(TAG,"<>+<>-<>+<>-<>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<>");
        Log.msg(TAG,"connectBroker: [" + brokerUrl + "] ClientID " + clientId);
        mqttClient = new MqttClient(brokerUrl,clientId,persistence);
        MqttConnectionOptions connOpts = new MqttConnectionOptions();
        Log.msg(TAG,"connectBroker - 1 -");
        connOpts.setCleanStart(false);
        connOpts.setKeepAliveInterval(60);
        connOpts.setSessionExpiryInterval(4294967295L);
        Log.msg(TAG,"connectBroker - 2 -");
        connOpts.setUserName("DEVICE_SUB");
        connOpts.setPassword("3A235A64E6C438BC5D3B53F63ED779438BB34E2347CA8A73C44822CFC9B929C8".getBytes(StandardCharsets.UTF_8));
        Log.msg(TAG,"connectBroker - 3 -");
        //connOpts.setConnectionTimeout();
        connOpts.setAutomaticReconnect(true); //
        connOpts.setAutomaticReconnectDelay(30,60); //Agregado 16Dic2021 -FLA,IFA
        mqttClient.connect(connOpts);
        Log.msg(TAG,"connectBroker - 4 -");
        mqttClient.setCallback(this);
        Log.msg(TAG,"Mqtt Connected to broker: " + brokerUrl + " with ClientID " + clientId);
        Log.msg(TAG,"<>+<>-<>+<>-<>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<>");
        Log.msg(TAG,"<>+<>                       TERMINO MQTT                                  <>+<>");
        Log.msg(TAG,"<>+<>-<>+<>-<>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<><>+<>-<>+<>");

    }


    public void reconnectBroker() throws MqttException{
       Log.msg(TAG,"reconnectBroker");
       if(mqttClient == null)
           Log.msg(TAG,"reconnectBroker - mqttClient == null");

       mqttClient.reconnect();

    }

    public void forceDisconnect() throws MqttException {
        mqttClient.disconnectForcibly(1000,1000,true);
    }
    //TODO: Codigo de Paquito, 16FEB22
    public void close(boolean bForced){
        try{
            if(mqttClient.isConnected())
                mqttClient.disconnectForcibly(1000,1000,true);

            if(bForced)
                mqttClient.close(true);
            else
                mqttClient.close();
        }catch (Exception e) {
            ErrorMgr.guardar(TAG, "close("+bForced+")", e.getMessage());
        }
    }

public void close(boolean bForced){
        try{
            if(mqttClient.isConnected())
                mqttClient.disconnectForcibly();

            if(bForced)
                mqttClient.close(true);
            else
                mqttClient.close();
        }catch (Exception e) {
            ErrorMgr.guardar(TAG, "close("+bForced+")", e.getMessage());
        }
    }

    public void subscribeNewTopic(String topic) throws MqttException {
        if (mqttClient == null){
            ErrorMgr.guardar(TAG,"subscribeNewTopic","mqttClient == null");
            return;
        }
        mqttClient.subscribe(topic,2);
        Log.msg(TAG,"[subscribeNewTopic] Listening topic: " + topic);

try {
            mqttClient.subscribe(topic, 0, null, new IMqttActionListener() {


                @Override
                public void onSuccess(org.eclipse.paho.client.mqttv3.IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(org.eclipse.paho.client.mqttv3.IMqttToken asyncActionToken, Throwable exception) {

                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }



    }


    public void unsubscribe(String topic) throws MqttException {
        if (mqttClient == null){

        return;
        }
        mqttClient.unsubscribe(topic);
        Log.msg(TAG,"[unsubscribe] topic: " + topic);
    }
    public  boolean isConnected() {
        boolean bConnected = false;
        try{
            //  long seconds=  tiempoTranscurrido(dteUltimoEvento,ChronoUnit.SECONDS);
            if(mqttClient != null)
                bConnected=mqttClient.isConnected();

                //Se pudo para que no entre cuando ya esta procesando un Mensaje.
if (seconds<15) bConnected = true;

        } catch (Exception e) {
            ErrorMgr.guardar(TAG, "isConnected", e.getMessage());
        }
        return bConnected;
    }

    public static void setProcessing(boolean pProcessing,String mensaje) {
        Log.msg("clientMQTT","setProcessing: " + mensaje +": " +pProcessing) ;
        bProcessing = pProcessing;
    }

    public static boolean isbProcessing() {
        return bProcessing;
    }

    public boolean isbConnecting() {
        return this.bConnecting;
    }

    public void setbConnecting(boolean bConnecting) {
      //  Log.msg("clientMQTT", "setbConnecting("+bConnecting+")...");
        this.bConnecting = bConnecting;
    }


******************************************************************************************************
    *                       Eventos del CALLBACK
    *******************************************************************************************************

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
       if (bConnecting)
            return;
        bConnecting = true;

        boolean hasNetwork = ConnectionStatus.isOnline();
     //  boolean hasNetwork = NetworkMQTT.isConnected();
        if (!hasNetwork)
            Log.msg(this.TAG_Callback,"Disconnected: hasNetwork: " + hasNetwork );
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        Log.msg(this.TAG_Callback,"Mqtt Error: " + exception.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws JSONException {
        bProcessing = true;
        dteUltimoEvento = LocalDateTime.now();
       // Log.msg(this.TAG,"messageArrived - Message: " + message.toString());
        msgProxyMQTT.busMessage(message.toString());
    }

    @Override
    public void deliveryComplete(IMqttToken token) {
        Log.msg(this.TAG_Callback,"messageArrived.DeliveryComplete");
    }

    //TODO: Codigo de Paquito, 16FEB22
    @Override
    public void connectComplete(boolean automatic, String serverURI) {
        Log.msg(TAG,"--- connectComplete, Automatic: " + automatic + " ----");
        try {
            if(ConnectionStatus.isOnline()){
                Log.msg(TAG,"[connectComplete] connectComplete. Conexion de red OK -  se va a suscribir() - +isConnected(): "+isConnected() );
                //IFA 13Ene22 - Se agrego la condicion isConected(), para evitar la reconexion constante, y que genere errores.
                if(isConnected())
                    ConnectMQTT.subscribe();
                else
                    Log.msg(TAG,"NO ESTA Conectado. : mqttClient.isConnected");
            }
            else
                Log.msg(TAG,"[connectComplete] connectComplete. NO HAY CONEXION de RED" );

        } catch (Exception e) {
            ErrorMgr.guardar(TAG, "connectComplete", e.getMessage());
        }
    }

    @Override
    public void connectComplete(boolean automatic, String serverURI) {
        Log.msg(TAG,"--- connectComplete ----");
        try {
            Log.msg(TAG,"[connectComplete] mqttClient.isConnected: "+isConnected()  );
         //   Log.msg(TAG,"[connectComplete] isConnected: "+isConnected() +" isConnecting: " +isbConnecting() );
         //   Log.msg(TAG, "[connectComplete] isOnline: "+ ConnectionStatus.isOnline()+ " isWIFI: "+ConnectionStatus.isWiFi());

        } catch (Exception e) {
            ErrorMgr.guardar(TAG, "connectComplete", e.getMessage());
        }

        if(!automatic)
            Log.msg(TAG,"[connectComplete] ***** connectComplete. NO SE PUDO CONECTAR A:  " + serverURI);
        else{

            try {
                if(ConnectionStatus.isOnline()){
                    Log.msg(TAG,"[connectComplete] connectComplete. Conexion de red OK -  se va a suscribir() - +isConnected(): "+isConnected() );
                    //IFA 13Ene22 - Se agrego la condicion isConected(), para evitar la reconexion constante, y que genere errores.
                    if(isConnected())
                        ConnectMQTT.subscribe();
                    else
                        Log.msg(TAG,"NO ESTA Conectado. : mqttClient.isConnected");
                }
                else
                    Log.msg(TAG,"[connectComplete] connectComplete. NO HAY CONEXION de RED" );

            } catch (Exception e) {
                ErrorMgr.guardar(TAG, "connectComplete", e.getMessage());


            }
        }
    }


    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        Log.msg(this.TAG_Callback,"AuthPacketArrived");
    }
    private Long tiempoTranscurrido(LocalDateTime dteFechaAnterior , ChronoUnit escala) {

        LocalDateTime dteHoy = LocalDateTime.now();
        LocalDateTime tempDateTime = LocalDateTime.from(dteFechaAnterior);
        Long tiempo = tempDateTime.until(dteHoy, escala);
        return tiempo;
    }

    public boolean subscribeAllTopic(String topic){
        return true;
    }



    public void unsubscribeTopic(String topic){
        try {
            mqttClient.unsubscribe(topic);
        } catch (MqttException me) {
            Log.e(TAG,me.toString());
        }
    }


}
*/
