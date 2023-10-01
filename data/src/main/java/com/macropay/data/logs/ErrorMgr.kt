package com.macropay.data.logs
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import com.macropay.data.di.Inject
import com.macropay.data.preferences.Values
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StaticFieldLeak")
object ErrorMgr {

    private var context: Context? = null
        get() {return field}
        set(value) {field = value}
    fun init(ctx: Context?) {
        context = ctx
    }

    fun initialized(): Boolean {
        return context != null
    }
    fun guardar(clase: String, funcion: String, error: String?,report:Boolean) {
        try {
            val msgError  = if(error != null) error else "nulo"
            var mensaje = " Error en funcion: $funcion "+
                    "\n${getToday()} | $clase | ERROR: $msgError "

            Log.msg(clase, mensaje)

            if(report)
                reportError(clase,funcion,msgError,Log.lastMsg)

        } catch (ex: Exception) {
            println("guardar[1]: ERROR: "+ex.message)
            return
        }
    }
    fun guardar(clase: String, funcion: String, error: String?) {
        try {
           val msgError  = if(error != null) error else "nulo"
            var mensaje = " Error en funcion: $funcion "+
                    "\n${getToday()} | $clase | ERROR: $msgError "
            val dataLog = Log.lastMsg
            Log.msg(clase, mensaje)

            reportError(clase,funcion,msgError,dataLog)
/*            val mensaje = "Clase: $clase\n\t\t| Funcion: $funcion\n\t\t| Error:$msg"
            msg("ERROR", mensaje)*/
        } catch (ex: Exception) {
            println("guardar[2]: ERROR: "+ex.message)
            return
        }
    }
    fun guardar(clase: String, funcion: String, error: String?,data:String) {
        try {


            val msgError  = if(error != null) error else "nulo"
            var mensaje = " Error en funcion: $funcion "+
                    "\n${getToday()}|$clase| ERROR: $msgError "
            if(data != null){
                mensaje+=  "\n | $clase | DATA: $data "
            }
            Log.msg(clase, mensaje)
            reportError(clase,funcion,msgError,data)

        } catch (ex: Exception) {
            println("guardar[3]: ERROR: "+ex.message)
            return
        }
    }

    fun reportError(clase: String, funcion: String, msg: String?,data:String){
        try {
            if(isNetworkAvailable())
                Inject.inject().getSendError().send("MLapp",clase,funcion,msg!!,data)

        } catch (ex: Exception) {
            println("reportError: ERROR: "+ex.message)
            Log.msg("ErrorMgr","reportError: "+ex.message)
        }
    }

    fun isNetworkAvailable(): Boolean {
        var isOnline = false
        try {
            val connectivityManager = Values.context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            isOnline = (connectivityManager?.activeNetworkInfo?.isConnected == true)
        }catch (ex:Exception){
            println("isNetworkAvailable: ERROR: "+ex.message)
        }
        return isOnline
    }
    private fun getToday():String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("HH:mm:ss")
        val today = formatter.format(date)
        return today
    }

}
/*

import android.content.Context
import com.macropay.data.logs.Log.Companion.msg
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object ErrorMgr {
    var context: Context? = null
    @JvmStatic
    fun init(ctx: Context?) {
        context = ctx
    }

    fun initialized(): Boolean {
        return context != null
    }

    fun guardar(clase: String, funcion: String, msg: String, bAlerta: Boolean) {
        try {
            val mensaje = """
$date | $clase | Funcion: $funcion
$date | $clase | Error:$msg"""
            msg(clase, mensaje)

            //The user 10260 does not meet the requirements to access device identifiers.
            //Sino tiene el Owner, le manda mensaje al Updater, para que le regrese el control.
            //    if (mensaje.contains("No active admin") || mensaje.contains("does not meet the requirements to access device"))
            //        Utils.sendUpdaterStatus("transferOwner",SettingsApp.isKiosko(),SettingsApp.Nivel());
        } catch (ex: Exception) {
            return
        }
    }

    @JvmStatic
    fun guardar(clase: String, funcion: String, msg: String) {
        try {
            val mensaje = """
$date | $clase | Funcion: $funcion
$date | $clase | Error:$msg"""
            msg(clase, mensaje)
            //The user 10260 does not meet the requirements to access device identifiers.
            //Sino tiene el Owner, le manda mensaje al Updater, para que le regrese el control.
            // if (mensaje.contains("No active admin")|| mensaje.contains("does not meet the requirements to access device"))
            //     Utils.sendUpdaterStatus("transferOwner",SettingsApp.isKiosko(),SettingsApp.Nivel());
        } catch (ex: Exception) {
            return
        }
    }

    private val date: String
        private get() {
            var today = "00:00:00"
            try {
                val date = Calendar.getInstance().time
                val formatter = SimpleDateFormat("HH:mm:ss")
                today = formatter.format(date)
            } catch (ex: Exception) {
            }
            return today
        }
}*/
