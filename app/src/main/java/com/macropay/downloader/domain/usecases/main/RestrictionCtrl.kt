package com.macropay.downloader.domain.usecases.main

import android.content.Context
import com.google.gson.Gson
import com.macropay.data.dto.request.EventMQTT
import com.macropay.downloader.entities.Restriccion
import com.macropay.utils.Settings
import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.logs.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject

class RestrictionCtrl
@Inject constructor(
    @ApplicationContext val context: Context
) {
    var TAG = "RestrictionCtrl"
    lateinit var restricciones: Array<Restriccion>

    fun existsEnabled(restriccionName :String,msg: EventMQTT): Boolean  {
        var restriccion : Restriccion?= null
        var bEnabled = false
        Log.msg(TAG, "[aplicarSingle]: restriccionName: [$restriccionName]")
        try {
            var restriccionesJson: JSONArray? = null

            if (msg.message.has("restrictions"))
                restriccionesJson = msg.message.getJSONArray("restrictions")

            //TODO: Aqui marco error durente enrolamiento, [01Jun23-IFA: Revisar]
            setRestricciones(restriccionesJson!!)

            restriccion = restricciones.firstOrNull {
                it.name.equals(restriccionName)
            }

            if(restriccion != null){
                Log.msg(TAG,"[aplicarSingle] --> aplicando [$restriccion.name]")
                bEnabled = restriccion.enabled
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "aplicarSingle", ex.message)
        }
        //return (restriccion != null)
        return bEnabled
    }
    fun getRestriction(restriccionName :String,msg: EventMQTT): Restriccion  {
        var restriccion : Restriccion?= null
        Log.msg(TAG, "[aplicarSingle]: restriccionName: [$restriccionName]")
        try {
            var restriccionesJson: JSONArray? = null

            if (msg.message.has("restrictions"))
                restriccionesJson = msg.message.getJSONArray("restrictions")

            //TODO: Aqui marco error durente enrolamiento, [01Jun23-IFA: Revisar]
            setRestricciones(restriccionesJson!!)

            restriccion = restricciones.firstOrNull {
                it.name.equals(restriccionName)
            }

            if(restriccion != null){
                Log.msg(TAG,"[aplicarSingle] --> aplicando [$restriccion.name]")

            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "aplicarSingle", ex.message)
        }

        return restriccion!!
    }
    //---

    fun aplicarSingle(restriccionName :String,msg: EventMQTT): Boolean  {
        var restriccion : Restriccion?= null
        var bEnabled = false
        Log.msg(TAG, "[aplicarSingle]: restriccionName: [$restriccionName]")
        try {
            var restriccionesJson: JSONArray? = null

            if (msg.message.has("restrictions"))
                restriccionesJson = msg.message.getJSONArray("restrictions")

            //TODO: Aqui marco error durente enrolamiento, [01Jun23-IFA: Revisar]
            setRestricciones(restriccionesJson!!)

            restriccion = restricciones.firstOrNull {
                it.name.equals(restriccionName)
            }

            if(restriccion != null){
                Log.msg(TAG,"[aplicarSingle] --> aplicando [$restriccion.name]")
               // aplicar(restriccion)

                bEnabled = restriccion.enabled
                //TODO 02Dic-- en prueba,,,
                Settings.setSetting(restriccion.name,false)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "aplicarSingle", ex.message)
        }
        //return (restriccion != null)
        return bEnabled
    }

    fun setRestricciones(jsonRestricciones: JSONArray) {
        Log.msg(TAG, "[setRestricciones]  $jsonRestricciones")
        try {
            val gson = Gson()
            restricciones = gson.fromJson(jsonRestricciones.toString(), Array<Restriccion>::class.java)
            Log.msg(TAG, "[setRestricciones] creo arreglo de restricciones. ")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "setRestricciones", ex.message)
        }
    }
}