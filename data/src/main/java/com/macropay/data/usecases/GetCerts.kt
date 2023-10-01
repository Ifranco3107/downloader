package com.macropay.data.usecases

import android.content.Context
import com.macropay.data.BuildConfig
import com.macropay.data.di.Session
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.repositories.CertsRepository
import com.macropay.utils.AES
import com.macropay.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCerts
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val TAG = "GetCerts"

    @Inject
    lateinit var certsRepository: CertsRepository
    /*
    @Inject
    lateinit var session: Session
*/

    @Inject
    lateinit var cabeceras: UserSessionCredentials

    //@Inject
    //lateinit var session: Session


    fun get() {

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = certsRepository.execute(cabeceras)
                    if (response.isSuccessful) {

                        var mqttServer = AES.decryptPCKS5(response.body()!!.data.urlIotClient.encrypted, response.body()!!.data.urlIotClient.key, response.body()!!.data.urlIotClient.iv)
                        var certificate = AES.decryptPCKS5(response.body()!!.data.cp.encrypted, response.body()!!.data.cp.key, response.body()!!.data.cp.iv)
                        var privateKey = AES.decryptPCKS5(response.body()!!.data.kc.encrypted, response.body()!!.data.kc.key, response.body()!!.data.kc.iv)

                        //  Log.msg(TAG, "[get] --- va guardar --")
                        //  Log.msg(TAG, "[get] mqttServer:  " + mqttServer)
                        // Log.msg(TAG,"[get] certificate: "+certificate)
                        // Log.msg(TAG,"[get] privateKey:  "+privateKey)
                        //TODO: 19Dic22 -
                        Settings.setSetting(Cons.KEY_MQTT_SERVER, mqttServer!!)
                        Settings.setSetting(Cons.KEY_CERT_IOT, certificate!!)
                        Settings.setSetting(Cons.KEY_PRIV_KEY_IOT, privateKey!!)
                        Settings.setSetting(Cons.KEY_MQTT_REFRESH, true)
                    }
                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "get", ex.message)
                }
            }

        }
    }


}