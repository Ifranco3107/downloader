package com.macropay.downloader.data.mqtt.messages

import android.content.Context
import com.macropay.data.dto.request.EventMQTT
import com.macropay.downloader.utils.Settings.setSetting
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import java.lang.Exception

class Parametros(var context: Context) : IEvento {
    var TAG = "Parametros"
    override  fun execute(msg: EventMQTT): Boolean {
        Log.msg(TAG, "settings: $msg")
        var parametro: String? = ""
        var tipoValor = "string"
        var valor = ""
        try {
            parametro = msg.message.getString("setting")
            tipoValor = msg.message.getString("tipo")
            valor = msg.message.getString("valor")
            when (tipoValor) {
                "string" ->
                    setSetting(parametro, valor)
                "int", "integer" ->
                    setSetting(parametro, valor.toInt())
                "long" ->
                    setSetting(parametro, valor.toLong())
                "bool", "boolean" ->
                    setSetting(parametro, java.lang.Boolean.parseBoolean(valor))
                else -> {
                    setSetting(parametro, valor)
                    return true
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "settings:  supportAction: $tipoValor", ex.message)
        }
        return true
    }
}