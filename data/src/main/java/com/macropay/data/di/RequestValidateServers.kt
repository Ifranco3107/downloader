package com.macropay.data.di

import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.preferences.Defaults
import com.macropay.data.usecases.SendLockStatus
import com.macropay.utils.Settings
import com.macropay.utils.preferences.Cons
import com.macropay.utils.preferences.Kiosko
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RequestValidateServers
@Inject constructor() {

    private val TAG = "RequestValidateServers" //::class.java.simpleName

    @Inject
    lateinit var sendLockStatus: SendLockStatus


    suspend fun validServer(newUrl: String?): Boolean {

        Log.msg(TAG, "[validServer]")

        val kioskStatus = Kiosko.enabled
        val curLockId = "1"  //Settings.getSetting(Cons.KEY_CURRENT_LOCK_ID,"001")
        Log.msg(TAG, "[validServer] kioskStatus: " + kioskStatus)
        Log.msg(TAG, "[validServer] curLockId: " + curLockId)

        try {

            val result = sendLockStatus.sendHttpUrl(curLockId, kioskStatus, Kiosko.currentKiosko.name, newUrl)
            var validResponse = result.getCompleted()

            Log.msg(TAG, "[validServer] result: " + validResponse)
            return validResponse
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "validServer", ex.message)
            return false
        }

    }

    suspend fun checkServer() {

        try {
            //forzar el antiguo para pruebas en dev
            //Settings.setSetting(Cons.KEY_HTTP_SERVER, Defaults.SERVIDOR_HTTP_CURRENT_DEV)

            val currentServer = Settings.getSetting(Cons.KEY_HTTP_SERVER, "")
            //Obtener el simil de la url actual dependiendo del ambiente actual
            val newUrl = getNewUrl(currentServer)
            Log.msg(TAG, "[checkServer] url: Url actual: $currentServer")
            Log.msg(TAG, "[checkServer] url: Nueva url: $newUrl")

            //CAMBIANDO LA PREFERENCIA DE LA URL DEL SERVIDOR SIEMPRE Y CUANDO EL ACTUAL SEA DIFERENTE AL NUEVO
            if (newUrl != null) {
                if (currentServer != newUrl) {
                    val isValidNewServer = validServer(newUrl)
                    if (isValidNewServer) {
                        Log.msg(TAG, "[checkServer] url: Aplicando nueva url: $newUrl")
                        Settings.setSetting(Cons.KEY_HTTP_SERVER, newUrl)
                        Log.msg(TAG, "[checkServer] url: Url aplicando con éxito: $newUrl")
                        ErrorMgr.guardar(TAG,"checkServer","switch a nueva URL [$newUrl]")
                    } else {
                        Log.msg(TAG, "[checkServer] url: Nuevo url todavia no es válido")
                        Log.msg(TAG, "[checkServer] url: No se aplicó cambio")
                    }

                } else
                    Log.msg(TAG, "[checkServer] url: Es igual, no hay que actualizar a $newUrl")
            } else
                Log.msg(TAG, "[checkServer] url: " + "No hay coincidencias en las urls candidatas o ya esta actualizado.")

            Log.msg(TAG, "[checkServer] url: checkServer terminado")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG,"checkServerr",ex.message)
        }


    }

    //Funcion para obtener la URL equivalente de los nuevos enlaces
    private fun getNewUrl(currentUrl: String) =  when (currentUrl) {
        Defaults.SERVIDOR_HTTP_CURRENT_DEV -> Defaults.SERVIDOR_HTTP2_DEV
        Defaults.SERVIDOR_HTTP_CURRENT_QA -> Defaults.SERVIDOR_HTTP2_QA
        Defaults.SERVIDOR_HTTP_CURRENT_PROD -> Defaults.SERVIDOR_HTTP2_PROD
        else -> null
    }


}