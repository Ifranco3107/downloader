package com.macropay.downloader.data.testers

import com.macropay.data.dto.request.EventMQTT
import org.json.JSONObject
import org.json.JSONArray
import com.macropay.downloader.data.preferences.TipoParametro
import com.macropay.downloader.data.preferences.TipoBloqueo
import org.json.JSONException
import com.macropay.data.preferences.Defaults
import com.macropay.utils.preferences.Kiosko
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import java.lang.Exception

object ConfigInicial {
    var TAG = "ConfigInicial"

    /**
     * RESTRICCIONES INICIALES....
     */
    fun getInicial(bEnabledProp: Boolean): EventMQTT {
        Log.msg(TAG, "ConfigInicial - INICIAL")
        val bloqueo = JSONObject()
        try {
            val restricciones = JSONArray()
            var restriccion: JSONObject

            //Restriccion de bloqueo por conexion
            restricciones.put(bloqueoSinConexion(bEnabledProp))

            //Apps para instalar...
            restricciones.put(getInstallBussinesApps())

            //--------------
            restricciones.put(getBloqueaSaveBoot(bEnabledProp))
            restricciones.put(bloqueoSinConexion(bEnabledProp))

            //--------------
            restricciones.put(getBloqueoInstallUnknow(bEnabledProp))
            restricciones.put(getBloqueoUSBDebug(bEnabledProp))

            //restricciones.put(getBloqueoFRPGoogle(bEnabledProp))


            restricciones.put(getBloqueoWipeData(false))

            //bloqueo de llamadas
            restricciones.put(getBloqueoIncomingCall(bEnabledProp))
            restricciones.put(getBloqueoOutGoingCall(bEnabledProp))

            //
            restricciones.put(getBloqueoCambioSIM (bEnabledProp))
            restricciones.put(getBloqueoTrackingGPS(!bEnabledProp))

            //Enrolamiento
            restricciones.put(getShowEULA(true))
            restricciones.put(getShowBienvenida(true))
             restricciones.put(getRequeireValidacionQR(true))

            //-------------------------------------
            //Agrega las restricciones al bloqueo.
            bloqueo.put("trans_id","096f93bc-7200-43f4-a45d-208aedd96881")
            bloqueo.put("user_id","87654421")
            bloqueo.put("lock_id","10")
            bloqueo.put("restrictions", restricciones)
            bloqueo.put("settings", getSettings())
        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "", e.message)
        }
        val eventMQTT = EventMQTT("bloqueo", bloqueo, false)
        Log.msg(TAG, "message: " +eventMQTT.message)
        return eventMQTT
    }

    fun getSettings():  JSONArray{
        var parametros :JSONArray = JSONArray()
        var parametro = JSONObject()
        parametro.put("name", TipoParametro.limiteSinConexion)
        parametro.put("value","120") //Dias
        parametro.put("type","int")
        parametros.put(parametro)

        parametro = JSONObject()
        parametro.put("name", TipoParametro.frecNotificaStatus)
        parametro.put("value","1") //Dias
        parametro.put("type","int")
        parametros.put(parametro)

        parametro = JSONObject()
        parametro.put("name", TipoParametro.customerId)
        parametro.put("value","1243434343")
        parametro.put("type","string")
        parametros.put(parametro)

        parametro = JSONObject()
        parametro.put("name", TipoParametro.customerName)
        parametro.put("value","Carlos Lopez Almazan")
        parametro.put("type","string")
        parametros.put(parametro)

        return parametros
    }

    //Bloqueo por falta de pago.
    fun getBloqueo(bEnabledProp: Boolean?): EventMQTT {
        Log.msg(TAG, "getBloqueo")
        val restricciones = JSONArray()
        val restriccion = JSONObject()
        val bloqueo = JSONObject()
        val parametro = JSONObject()
        try {
            //bloqueo de llamadas salientes
            restricciones.put(getBloqueoOutGoingCall(bEnabledProp))

            //Restriccion con apps
            restricciones.put(getBloqueoApps(bEnabledProp))

            //disable_file_transfer
            restricciones.put(getBloqueoFileTransfer(bEnabledProp))

            //disable_install_apps
            restricciones.put(getBloqueoInstallApps(bEnabledProp))

            //disable_screen_capture
            restricciones.put(getBloqueoCapturaPantalla(bEnabledProp))

            //disable_manage_accounts
            restricciones.put(getBloqueoManageAccounts(bEnabledProp))

            //	show_kiosko
            restricciones.put(getBloqueoKiosko(bEnabledProp))
            restricciones.put(getBloqueoCambioSIM (bEnabledProp!!))
            //-------------------------------------
            //Agrega las restricciones al bloqueo.
            bloqueo.put("trans_id","096f93bc-7200-43f4-a45d-208aedd96881")
            bloqueo.put("user_id","1234567")
            bloqueo.put("lock_id","70")
            bloqueo.put("nivel_bloqueo","100")
            bloqueo.put("action","1001")
            bloqueo.put("restrictions", restricciones)
        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "getBloqueo", e.message)
        }
        val eventMQTT = EventMQTT("1001", bloqueo, false)
        Log.msg(TAG, "eventMQTT.message"+ eventMQTT.toString())
        return eventMQTT
    }

    //USado por ValidacionOfflineFragment, para desbloqueo offline...
    fun getMQTTUnlock(): EventMQTT {
        Log.msg(TAG, "getMQTTUnlock")
        val bloqueo = JSONObject()
        //-------------------------------------
        bloqueo.put("trans_id","096f93bc-7200-43f4-a45d-208aedd96881")
        bloqueo.put("user_id","offline")
        bloqueo.put("lock_id","2")
        bloqueo.put("action","1002")
        val eventMQTT = EventMQTT("1002", bloqueo, false)
        Log.msg(TAG, "[getMQTTUnlock] eventMQTT.message"+ eventMQTT.toString())
        return eventMQTT
    }
    //Bloqueo por falta de pago.
    fun getMQTTUnroll(): EventMQTT {
        Log.msg(TAG, "getBloqueo")
        val bloqueo = JSONObject()

       // "action":1003,
        // "user_id":1,
        // "trans_id":95,
        // "uninstall_dpc":false,
        // "uninstall_apps":true,
        // "hide_icon":true,
        // "show_release":true}
        //-------------------------------------
        bloqueo.put("trans_id","096f93bc-7200-43f4-a45d-208aedd96881")
        bloqueo.put("user_id","1234567")
        bloqueo.put("lock_id","70")
        bloqueo.put("action","1003")
        bloqueo.put("uninstall_dpc",false)
        bloqueo.put("uninstall_apps",true)
        bloqueo.put("hide_icon",true)
        bloqueo.put("show_release",true)

        val eventMQTT = EventMQTT("1003", bloqueo, false)
        Log.msg(TAG, "eventMQTT.message"+ eventMQTT.toString())
        return eventMQTT
    }

    //Configuracion de Restricciones, para liberar el telefono..
    fun getLiberar(eventMQTT: EventMQTT, hideIcon: Boolean,uninstallDpc:Boolean,uninstallApps:Boolean,showRelease:Boolean ) : EventMQTT {
        Log.msg(TAG, "getBloqueo")
        val restricciones = JSONArray()
        val restriccion = JSONObject()
        val bloqueo = JSONObject()
        val parametro = JSONObject()
        val bEnabledProp: Boolean = false
        try {
            restricciones.put(getBloqueoSMS(bEnabledProp))
            restricciones.put(getBloqueoUninstallBussinesApp(bEnabledProp))

            //Restriccion con apps
            restricciones.put(getBloqueoApps(bEnabledProp))

            //disable_file_transfer
            restricciones.put(getBloqueoFileTransfer(bEnabledProp))

            //disable_install_apps
            restricciones.put(getBloqueoInstallApps(bEnabledProp))

            //disable_screen_capture
            restricciones.put(getBloqueoCapturaPantalla(bEnabledProp))
            restricciones.put(getBloqueoManageAccounts(bEnabledProp))

            //--------------
            restricciones.put(getBloqueaSaveBoot(bEnabledProp))

            //Bloqueo por desconexion de internet.
            restricciones.put(bloqueoSinConexion(bEnabledProp))
            //Manejo de SIM
            restricciones.put(getBloqueoCambioSIM (bEnabledProp))
            restricciones.put(getBloqueoRemoveSIM (bEnabledProp))

            //GPS
            restricciones.put(getBloqueoTrackingGPS(bEnabledProp))

            //--------------
            restricciones.put(getBloqueoInstallUnknow(bEnabledProp))
            restricciones.put(getBloqueoUSBDebug(bEnabledProp))

            //---FRP
            //restricciones.put(getBloqueoFRPGoogle(bEnabledProp)) //No se usa..
            restricciones.put(getBloqueoDownloadFirmaware(bEnabledProp))
            restricciones.put(getBloqueoWipeData(bEnabledProp))

            //bloqueo de llamadas
            restricciones.put(getBloqueoIncomingCall(bEnabledProp))
            restricciones.put(getBloqueoOutGoingCall(bEnabledProp))


            //---------------------------------------------------------
           if( hideIcon){
               restricciones.put(getBloqueoIconApp(true))
           }


            if(uninstallDpc)
                restricciones.put(getUninstallDpc(true))

            if(uninstallApps)
                restricciones.put(getUninstallApps(bEnabledProp))

            if(showRelease)
                restricciones.put(getShowUnrolled(true))

            //-------------------------------------
            var lockId = "1"
            var transId = "096f93bc-7200-43f4-a45d-208aedd96881"
            var userId = "1234567"
            if (eventMQTT.message.has("trans_id")) transId = eventMQTT.message.getString("trans_id")
            if (eventMQTT.message.has("user_id")) userId = eventMQTT.message.getString("user_id")
            if (eventMQTT.message.has("lock_id")) lockId = eventMQTT.message.getString("lock_id")
            Log.msg(TAG,"[getLiberar] transId: "+transId)
            Log.msg(TAG,"[getLiberar] lockId: "+lockId + " " +eventMQTT.id)
            Log.msg(TAG,"[getLiberar] orden: "+eventMQTT.orden)

            //Agrega las restricciones al bloqueo.
            bloqueo.put("trans_id",transId)
            bloqueo.put("user_id",userId)
            bloqueo.put("nivel_bloqueo",lockId)
            bloqueo.put("orden",eventMQTT.orden)
            bloqueo.put("restrictions", restricciones)


        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "getBloqueo", e.message)
        }

        val newEventMQTT = EventMQTT("bloqueo", bloqueo, false)
        Log.msg(TAG, "eventMQTT.message"+ eventMQTT.toString())
        return newEventMQTT
    }

    //Configuracion de Restricciones, para liberar el telefono..
    fun getEmergency( ) : EventMQTT {
        Log.msg(TAG, "getBloqueo")
        val restricciones = JSONArray()
        val restriccion = JSONObject()
        val bloqueo = JSONObject()
        val parametro = JSONObject()
        val bEnabledProp: Boolean = false
        try {
            //disable_file_transfer
            restricciones.put(getBloqueoFileTransfer(bEnabledProp))
            //--------------
            restricciones.put(getBloqueoInstallUnknow(bEnabledProp))
            restricciones.put(getBloqueoUSBDebug(bEnabledProp))
          //  restricciones.put(getBloqueoIconApp(bEnabledProp))
            //-------------------------------------
            //Agrega las restricciones al bloqueo.
            bloqueo.put("trans_id","096f93bc-7200-43f4-a45d-208aedd96881")
            bloqueo.put("user_id","1234567")
            bloqueo.put("nivel_bloqueo","1")
            bloqueo.put("restrictions", restricciones)


        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "getBloqueo", e.message)
        }

        val eventMQTT = EventMQTT("bloqueo", bloqueo, false)
        Log.msg(TAG, "eventMQTT.message"+ eventMQTT.toString())
        return eventMQTT
    }

    fun bloqueoSinConexion(bEnabledProp: Boolean): JSONObject {
        val parametros_bloqueo_conexion = JSONArray()
        val parametro = JSONObject()
        val restriccion = JSONObject()
        try {
            parametro.put("name", TipoParametro.limiteSinConexion)
            parametro.put("value","120") //Dias
            parametro.put("type","int")
            parametros_bloqueo_conexion.put(parametro)

            //Genera la restriccion
            restriccion.put("name", TipoBloqueo.disable_lock_for_not_connection)
            restriccion.put("enabled", bEnabledProp)
            restriccion.put("params", parametros_bloqueo_conexion)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return restriccion
    }
    fun getBloqueoCambioSIM(bEnabledProp: Boolean): JSONObject {
        val parametros_bloqueo_cambio_SIM = JSONArray()
        var parametro = JSONObject()
        val restriccion = JSONObject()
        try {
            parametro.put("name", TipoParametro.carriers_allowed)
            parametro.put("value","12542134,334020,4312,1913")
            parametro.put("type","String")
            parametros_bloqueo_cambio_SIM.put(parametro)

            parametro = JSONObject()
            parametro.put("name", TipoParametro.requiere_validacion_SMS)
            parametro.put("value","true")
            parametro.put("type","Boolean")
            parametros_bloqueo_cambio_SIM.put(parametro)

            //Genera la restriccion
            restriccion.put("name", TipoBloqueo.disable_lock_for_sim_change)
            restriccion.put("enabled", bEnabledProp)
            restriccion.put("params", parametros_bloqueo_cambio_SIM)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return restriccion
    }



    fun getBloqueoRemoveSIM(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.enabledlockForRemoveSim)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }
    fun getBloqueoTrackingGPS(bEnabledProp: Boolean): JSONObject {
        val parametros_tracking_GPS = JSONArray()
        var parametro = JSONObject()
        val restriccion = JSONObject()
        try {

            parametro.put("name", TipoParametro.limiteSinEnvioGPS)
            parametro.put("value","60")
            parametro.put("type","Int")
            parametros_tracking_GPS.put(parametro)

            parametro = JSONObject()
            parametro.put("name", TipoParametro.rangoMismoLugarGPS)
            parametro.put("value","50")
            parametro.put("type","int")
            parametros_tracking_GPS.put(parametro)

            parametro = JSONObject()
            parametro.put("name", TipoParametro.frecuenciaCapturaGPS)
            parametro.put("value","15")
            parametro.put("type","int")
            parametros_tracking_GPS.put(parametro)

            //Genera la restriccion
            restriccion.put("name", TipoBloqueo.disable_tracking_GPS)
            restriccion.put("enabled", bEnabledProp)
            restriccion.put("params", parametros_tracking_GPS)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return restriccion
    }

    fun getInstallBussinesApps(): JSONObject {
        val appsEmpresa = JSONArray()
        val app = JSONObject()
        val restriccion = JSONObject()
        try {

            //http://34.235.143.236:3000/api/devices/mobile/bussiness/apk/download?appkeymobile=CMaz3r2r23r23r23r23ssd11SWSZXWEFWWFd99s
            //val appLocation = "http://34.235.143.236:3000/api/devices/mobile/bussiness/apk/download?appkeymobile=CMaz3r2r23r23r23r23ssd11SWSZXWEFWWFd99s"
            val appLocation = "http://alb-back-macrolocks3client-api-294866783.us-east-1.elb.amazonaws.com:3000/api/devices/mobile/bussiness/apk/download?appkeymobile=CMaz3r2r23r23r23r23ssd11SWSZXWEFWWFd99s"
            //Define las apps que va instalar.
            app.put("name", Defaults.APP_BUSSINES_PACKAGE)
            app.put("value", appLocation)
            app.put("type","string")
            appsEmpresa.put(app)

            //Genera la restriccion
            restriccion.put("name", TipoBloqueo.install_bussines_apps)
            restriccion.put("enabled","true")
            restriccion.put("params", appsEmpresa)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return restriccion
    }
    //TODO
    //TODO
    fun getBloqueoIncomingCall(bEnabledProp: Boolean?): JSONObject {
        val parametros_llamadas_entrantes = JSONArray()
        var parametro = JSONObject()
        var restriccion = JSONObject()
        try {
            val telsDefault = arrayOf("5534935662","5530171405","9995628676,9994956006")
            parametro.put("name", TipoParametro.numbers_exception_incoming)
            parametro.put("value", telsDefault.joinToString())
            parametro.put("type","string")
            parametros_llamadas_entrantes.put(parametro)
            /**
             * RESTRICCIONES DE BLOQUEO....
             */
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_incoming_calls )
            restriccion.put("enabled", bEnabledProp)
            restriccion.put("params", parametros_llamadas_entrantes)

        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "getBloqueoIncomingCall", e.message)
        }
        return restriccion
    }
    fun getBloqueoOutGoingCall(bEnabledProp: Boolean?): JSONObject {
        val parametros_llamadas_salientes = JSONArray()
        var parametro = JSONObject()
        var restriccion = JSONObject()
        try {
            val telsDefault = arrayOf("9994956006","5534935661","9995628676")
            parametro.put("name", TipoParametro.numbers_exception_outgoing)
            parametro.put("value", telsDefault.joinToString())
          // parametro.put("value", "")
            parametro.put("type","string")
            parametros_llamadas_salientes.put(parametro)
            /**
             * RESTRICCIONES DE BLOQUEO....
             */
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_outgoing_calls)
            restriccion.put("enabled", bEnabledProp)
            restriccion.put("params", parametros_llamadas_salientes)
        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "getBloqueoOutGoingCall", e.message)
        }
        return restriccion
    }

    //
    fun getShowEULA(bEnabledProp: Boolean?): JSONObject {
        val parametros_showEULA = JSONArray()
        var parametro = JSONObject()
        var restriccion = JSONObject()
        try {
            parametro.put("name", TipoParametro.eulaTitle)
            parametro.put("value", Defaults.EULA_TITLE)
            parametro.put("type","string")
            parametros_showEULA.put(parametro)

            parametro = JSONObject()
            parametro.put("name", TipoParametro.eulaBody)
            parametro.put("value", Defaults.EULA_DEFAULT)
            parametro.put("type","string")
            parametros_showEULA.put(parametro)
            /**
             * RESTRICCIONES DE BLOQUEO....
             */
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.showEula)
            restriccion.put("enabled", bEnabledProp)
            restriccion.put("params", parametros_showEULA)
        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "getShowEULA", e.message)
        }
        return restriccion
    }

    //
    fun getShowBienvenida(bEnabledProp: Boolean?): JSONObject {
        val parametros_showBienvenida = JSONArray()
        var parametro = JSONObject()
        var restriccion = JSONObject()
        try {
            parametro.put("name", TipoParametro.bienvenidaTitle)
            parametro.put("value", Defaults.BIENVENIDA_TITLE)
            parametro.put("type","string")
            parametros_showBienvenida.put(parametro)

            parametro = JSONObject()
            parametro.put("name", TipoParametro.bienvenidaBody)
            parametro.put("value", Defaults.BIENVENIDA_BODY)
            parametro.put("type","string")
            parametros_showBienvenida.put(parametro)
            /**
             * RESTRICCIONES DE BLOQUEO....
             */
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.showBienvenida)
            restriccion.put("enabled", bEnabledProp)
            restriccion.put("params", parametros_showBienvenida)
        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "getBloqueo", e.message)
        }
        return restriccion
    }

    fun getRequeireValidacionQR(bEnabledProp: Boolean?): JSONObject {
        val parametros_showBienvenida = JSONArray()
        var parametro = JSONObject()
        var restriccion = JSONObject()
        try {
            parametro.put("name", TipoParametro.codigoValidacionQR)
            parametro.put("value","12345678")
            parametro.put("type","string")
            parametros_showBienvenida.put(parametro)

            /**
             * RESTRICCIONES DE BLOQUEO....
             */
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.requiere_validacion_QR)
            restriccion.put("enabled", bEnabledProp)
            restriccion.put("params", parametros_showBienvenida)
        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "getRequeireValidacionQR", e.message)
        }
        return restriccion
    }

    fun getBloqueoApps(bEnabledProp: Boolean?): JSONObject {
        val parametros_bloquea_apps = JSONArray()
        var parametro = JSONObject()
        var restriccion = JSONObject()
        try {
            parametro.put("name", TipoParametro.apps_bloqueadas)
            parametro.put("value", apps)
            parametro.put("type","string")
            parametros_bloquea_apps.put(parametro)

            restriccion.put("name", TipoBloqueo.disable_list_apps)
            restriccion.put("enabled", bEnabledProp)
            restriccion.put("params", parametros_bloquea_apps)
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG,"getBloqueoApps",e.message)
        }
        return restriccion
    }

    fun getBloqueoFileTransfer(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_file_transfer)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }
    fun getBloqueoSMS(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_sms)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }
    fun getBloqueoUninstallBussinesApp(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_uninstall_bussines_apps)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }
    fun getBloqueoInstallApps(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_install_apps)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }
    ///

    //disable_screen_capture
    fun getBloqueoCapturaPantalla(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_screen_capture)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }

    //disable_manage_accounts
    fun getBloqueoManageAccounts(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_manage_accounts)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }

    //	show_kiosko
    fun getBloqueoKiosko(bEnabledProp: Boolean?): JSONObject {

        val parametros_Bloqueo = JSONArray()
        var parametro = JSONObject()
        var restriccion = JSONObject()
        try {
           // val tipoBloqueo = Settings.getSetting(TipoParametro.tipo_bloqueo, TipoParametro.bloqueo_credito)
            parametro.put("name", TipoParametro.tipo_bloqueo)
            parametro.put("value", Kiosko.eTipo.PorCredito.name)
            parametro.put("type","string")
            parametros_Bloqueo.put(parametro)

            parametro = JSONObject()
            parametro.put("name", TipoParametro.currentBalance)
            parametro.put("value",213434)
            parametro.put("type","int")
            parametros_Bloqueo.put(parametro)

            parametro = JSONObject()
            parametro.put("name", TipoParametro.loanMessage)
            parametro.put("value","por favor paga....")
            parametro.put("type","string")
            parametros_Bloqueo.put(parametro)

            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.show_kiosko)
            restriccion.put("enabled", bEnabledProp)
            restriccion.put("params", parametros_Bloqueo)
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG,"getBloqueoKiosko",e.message)
        }
        return restriccion
    }

    fun getBloqueaSaveBoot(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_save_boot_button)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }

    //--------------
    fun getBloqueoInstallUnknow(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_install_unknown_sources)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }

    fun getBloqueoUSBDebug(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_usb_debug)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }

    fun getBloqueoFRPGoogle(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_frp_google)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }

    fun getBloqueoDownloadFirmaware(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_download_firmaware)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }
    fun getBloqueoWipeData(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.disable_recovery_wipe_data)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }
    fun getBloqueoIconApp(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.hide_icon_dpc)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }
    fun getUninstallApps(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.uninstall_bussines_apps)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }
    fun getUninstallDpc(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.uninstall_dpc)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }


    fun getShowUnrolled(bEnabledProp: Boolean?): JSONObject {
        var restriccion = JSONObject()
        try {
            restriccion = JSONObject()
            restriccion.put("name", TipoBloqueo.show_unrolled)
            restriccion.put("enabled", bEnabledProp)
        } catch (e: Exception) {
        }
        return restriccion
    }
    val apps: String
        get() {
            val appsDefault = arrayOf(
                "com.whatsapp","com.facebook.katana","com.facebook.lite","com.facebook.orca","com.facebook.mlite",
                "com.whatsapp.w4b","org.telegram.messenger","com.google.android.apps.maps","com.waze","com.ubercab",
                "com.didiglobal.passenger","com.spotify.music","com.google.android.youtube","com.android.vending",
                "com.snapchat.android","com.google.android.gm","com.instagram.android","mx.com.bancoazteca.bazdigitalmovil",
                "com.google.android.googlequicksearchbox","com.bancomer.mbanking","com.citibanamex.banamexmobile",
                "com.android.chrome","org.mozilla.firefox","us.zoom.videomeetings","com.google.android.apps.meetings",
                "com.zhiliaoapp.musically","com.payclip.clip","mx.hsbc.hsbcmexico","com.linkedin.android","mx.com.miapp",
                "com.motorola.camera2","com.didiglobal.driver","com.google.android.apps.tachyon","com.google.android.apps.messaging",
                "com.netflix.mediaclient","com.ubercab.driver","com.ubercab.eats","com.google.android.apps.youtube.music",
                "com.google.android.apps.subscriptions.red","com.microsoft.office.outlook","com.android.camera2","com.ume.browser.cust"
            )
           /* val apps = JSONArray()
            try {
                for (appName in appsDefault) {
                    val parametro = JSONObject()
                    parametro.put("name","")
                    parametro.put("value", appName)
                    parametro.put("type","")
                    apps.put(parametro)
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "getApps", ex.message)
            }*/
            return appsDefault.joinToString()
        }
}