package com.macropay.data.dto.request
import com.google.gson.Gson
import org.json.JSONObject
import java.io.Serializable
import java.lang.Exception




class EventMQTT(action: String, message: JSONObject, procesado: Boolean) : Serializable {
    var action: String
    var message: JSONObject
    var isProcesado: Boolean
    var id: Long
    var orden: Long = 0L

    init {
        id = System.currentTimeMillis()
        this.action = action
        this.message = message
        isProcesado = procesado
        try {
            if (message.has("orden")) orden = message.getLong("orden")
        } catch (ex: Exception) {
            // ErrorMgr.guardar("EventMQTT","constructor",ex.getMessage());
        }
    }

    override fun equals(obj: Any?): Boolean {
        if (obj is EventMQTT) {
            return obj.orden === orden
        }
        return false

        //return super.equals(obj);
    }

    override fun toString(): String {
        //Gson gson = new Gson();
        val  json :String = Gson().toJson(this)
        return json
    }
}