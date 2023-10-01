package com.macropay.downloader.data.awsiot

import android.content.Context
import android.os.Environment
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos

import com.macropay.downloader.data.awsiot.MqttSettings.DefaultCertificate
import com.macropay.downloader.data.awsiot.MqttSettings.DefaultPrivateKey
import com.macropay.downloader.data.awsiot.MqttSettings.KEYSTORE_NAME
import com.macropay.downloader.data.awsiot.MqttSettings.KEYSTORE_PASSWORD
import com.macropay.downloader.data.awsiot.MqttSettings.certId
import com.macropay.downloader.data.awsiot.MqttSettings.clientId
import com.macropay.downloader.data.awsiot.MqttSettings.endPoint
import com.macropay.downloader.data.awsiot.MqttSettings.keyStorePath
import com.macropay.downloader.data.awsiot.MqttSettings.topic
//import com.macropay.dpcmacro.di.Inject
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.Utils
import com.macropay.downloader.utils.device.DeviceService
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.network.Red
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.KeyStore
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

//https://docs.aws.amazon.com/iot/latest/developerguide/iot-sdks.html
//https://github.com/awslabs/aws-sdk-android-samples#aws-sdk-for-android-samples
@Singleton
class Mqtt
    @Inject constructor(@ApplicationContext val  context: Context)
    : AWSUtils() {
//class mqtt(appContext: Context?) : AWSUtils() {
    private val TAG = "Mqtt"
    @Inject
    lateinit var applyMsgMqtt: ApplyMsgMqtt



    var ultimaConexion :LocalDateTime  =  LocalDateTime.now()
        set(value) {
            Settings.setSetting(Cons.KEY_MQTT_LAST_CONECTION,value)
            field = value
        }
       get() {
           return Settings.getSetting(Cons.KEY_MQTT_LAST_CONECTION, field)
       }

    init {
        Log.msg(TAG,"[init]")
        setContext(context)
        serverConfig()

/*        applyMsgMqtt = Inject.inject(appContext!!).getAApplyMsgMqtt()
        restoreRestrictions   = Inject.inject(appContext!!).getRestoreRestrictions()*/
        if(!isKeystorePresent()!!){
          //  Log.msg(TAG,"[init] va crear el keystore")
            saveKeyStore()
        }
    }

    var isConnected = false
    get() {
        return field
    }

    //AWS Identity and Access Management (IAM)
    private fun configMqtt() {
      //  Log.msg(TAG, "[configMqtt]")
        try{
            with(mqttManager!!){
                keepAlive = 30 //30 segundos,para hacer ping y detectar desconexion
                isAutoReconnect = true
               // setReconnectRetryLimits(10,120)
                connectionStabilityTime = 15 //10 segundos para esperar a la rconexion.
                maxAutoReconnectAttempts = 5
                setCleanSession(false)
                Log.msg(TAG, "[configMqtt]  setCleanSession(false)")
            }
        }
        catch (ex:Exception){
            ErrorMgr.guardar(TAG,"configMqtt",ex.message)
        }
    }

    private fun serverConfig(){
      //  Log.msg(TAG,"[serverConfig] -1-")
        try{
            var imei = DeviceCfg.getImei(getAppContext() )
            Log.msg(TAG, " imei: "+imei  +"")
            Log.msg(TAG,"[serverConfig] imei: "+imei )
            Log.msg(TAG,"[serverConfig] id  : "+ DeviceInfo.getDeviceID() )
            imei = DeviceInfo.getDeviceID()
            var appDirectory: File? = getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            MqttSettings.topic = "lock/"+imei
            MqttSettings.clientId = imei
            MqttSettings.keyStorePath = appDirectory!!.absolutePath //this.filesDir.toString() + "/Pictures/"
        } catch (ex:Exception){
                ErrorMgr.guardar(TAG,"serverConfig",ex.message)
        }
    //    Log.msg(TAG,"[serverConfig] -2-")
    }
    fun isKeystorePresent(): Boolean? {
        return AWSKeyStore.isKeystorePresent( keyStorePath, KEYSTORE_NAME)
    }

    fun saveKeyStore() {
        // save certificate and private key in a keystore
      //  Log.msg(TAG, "keyStorePath: ------------------------------------------------")
      //  Log.msg(TAG, "keyStorePath: " + keyStorePath)
        val certificate = Settings.getSetting(Cons.KEY_CERT_IOT, DefaultCertificate)
        val privateKey = Settings.getSetting(Cons.KEY_PRIV_KEY_IOT, DefaultPrivateKey)
        AWSKeyStore.saveCertificateAndPrivateKey(
            certId,
            certificate,
            privateKey,
            keyStorePath,
            KEYSTORE_NAME,
            KEYSTORE_PASSWORD
        )
    }
    fun refresh(){
        val reqRefresh = com.macropay.utils.Settings.getSetting(Cons.KEY_MQTT_REFRESH, false)
        if(reqRefresh){
          //  Log.msg(TAG,"[mqttConnect] refresh")
            saveKeyStore()
            com.macropay.utils.Settings.setSetting(Cons.KEY_MQTT_REFRESH, false)
        }
    }
    fun loadKeyStore(): KeyStore {
       // Log.msg(TAG, "[loadKeyStore]")
        return AWSKeyStore.getIotKeystore(
            certId,
            keyStorePath,
            KEYSTORE_NAME,
            KEYSTORE_PASSWORD
        )
    }

  //  @Throws(Exception::class)
    fun connect(source:String) {
      Log.msg(TAG,"[connect] -1- source: "+source +" endPoint: ["+endPoint+"]")

        //  initCredencials();
        isConnected = false
        if(!Red.isOnline)
        {
            //Log.msg(TAG,"[connect] sin red...prendiendo wifi")
            DeviceService.enableWifi(getAppContext())
            espera("Espera para activar WIFI")

            if(!Red.isOnline){
                Log.msg(TAG,"[connect] sin red...")
                //return
                espera("Sin Red, espara a reconectar")
            }
        }
        if(endPoint.isEmpty())
            checkSettings()

        try{
            //Crea el objeto de conexion...
            mqttManager = AWSIotMqttManager(clientId, endPoint)

            //Aplica los settings.
             configMqtt()

            //refresca keystore.
            refresh()


            //Conecta
            mqttConnect()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"connect",ex.message)
        }
     //   Log.msg(TAG, "[connect] termino: " )
    }

    @Throws(InterruptedException::class)
    private fun mqttConnect() {

        Log.msg(TAG,"[mqttConnect] - topic: [$topic] isOnline: ${Red.isOnline}" )
        if(!Red.isOnline)
            return

        //Log.msg(TAG,"mqttManager.keepAlive: "+mqttManager!!.keepAlive)
        try {
            val keyStore = loadKeyStore()
            mqttManager!!.setCleanSession(false)
          //  mqttManager!!.unsubscribeTopic(defineTopic())
         //   mqttManager!!.disconnect()
            //Application Layer Protocol Negotiation (ALPN)
            mqttManager!!.connectUsingALPN(keyStore, callbackStatus) // port 443.

            // wait for connection
            espera("espera para conectar")
           // isConnected=  AWSIotMqttClientStatus.Connected
            if (!isConnected) {
                Log.msg(TAG, "[mqttConnect] NO SE CONECTO....")
                return
            }

            // ensure subscribe propagates
            espera("Espera para suscripcion")


        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "mqttConnect", ex.message)
        }
    }

    fun espera(msg:String){
        try{
            Log.msg(TAG,"[espera] $msg")
            for (i in 1..4){
                Thread.sleep(500)
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "espera", ex.message)
        }
        catch (e:InterruptedException){
            ErrorMgr.guardar(TAG,"espera[Thread]",e.message)
        }
    }




    fun subscribe(){
        try{

            //var imei = DeviceCfg.getImei(getAppContext() )
            var imei = DeviceInfo.getDeviceID()
            MqttSettings.topic =defineTopic()
            MqttSettings.clientId = imei
           // mqttManager!!.setAutoResubscribe(true)
            //Es posible que marque error, porque por default tiene AutoResubscribe
            Log.msg(TAG,"[subscribe] topic: ${defineTopic()}")
            mqttManager?.subscribeToTopic(topic, AWSIotMqttQos.QOS1, callbackMessages)
        }catch (ex:Exception){
           // mqttManager!!.unsubscribeTopic(defineTopic())
            ErrorMgr.guardar(TAG,"subscribe*",ex.message,false)
            //
            disconnect()
        }
    }

    fun defineTopic():String{
        var topic = ""
        try{
            var imei = DeviceInfo.getDeviceID()
            topic =  "lock/"+imei
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"defineTopic",ex.message)
        }
        return topic
    }
    //
    var callbackStatus = AWSIotMqttClientStatusCallback { status, throwable ->
        try {
            isConnected = status == AWSIotMqttClientStatus.Connected
            if (isConnected) {
                val msgStatus = if (mqttManager!!.sessionPresent) "Persistente-ok" else ""
                Log.msg(TAG, "[callbackStatus] [$status] - " + MqttSettings.topic + " - " + msgStatus)
                //Revisa si tiene mas de 1 hora sin conectarse o no es persistente, si es asi, consulta si hay mensajes pendientes.

                //Se suscribe...
                subscribe()
                ultimaConexion = LocalDateTime.now()
            } else {
                Log.msg(TAG, "[callbackStatus] [$status]")
                checkSettings()
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"callbackStatus",ex.message)
        }
    }

    var callbackMessages = AWSIotMqttNewMessageCallback { topic, data ->
        Log.msg(TAG, "----------------< mensaje >------------------------")
        Log.msg(TAG, "data: " + String(data))

        try{
            applyMsgMqtt.addMessage(String(data))

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"callbackMessages",ex.message)
        }

        // messages.add(new String(data));
    }

    fun disconnect() {
        Log.msg(TAG,"[disconnect]")
        // disconnect
        mqttManager!!.disconnect()
    }

    fun checkSettings(){
     //   Log.msg(TAG,"[checkSettings]")
        try{
            var  mins = 0
            if (ultimaConexion != null)
                mins = Utils.tiempoTranscurrido(ultimaConexion, ChronoUnit.MINUTES).toInt()
       //     Log.msg(TAG,"[checkSettings] mins: "+ mins +" - "+ultimaConexion)
            if(mins >15 ||endPoint.isEmpty()){
              //  ultimaConexion = LocalDateTime.now()
                if (Red.isOnline){
                    com.macropay.utils.Settings.setSetting(Cons.KEY_MQTT_REFRESH, false)
                    Log.msg(TAG,"[checkSettings] Refrescar settings mins: [$mins]")
                    com.macropay.data.di.Inject.inject().getGetCerts().get()
                }

            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"checkSettings",ex.message)
        }
    }
    companion object {
        private val credentialsProvider: AWSCredentialsProvider? = null
        var mqttManager: AWSIotMqttManager? = null
    }


}