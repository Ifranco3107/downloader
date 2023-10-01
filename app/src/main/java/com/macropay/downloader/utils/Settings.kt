package com.macropay.downloader.utils

import android.content.Context
import android.content.SharedPreferences
import com.macropay.data.logs.ErrorMgr
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.Exception

object Settings {
    var sharedPref: SharedPreferences? = null
    var context:Context? = null
        get() {
            return field}
        set(value) {field = value}

    var TAG = "Settings"
  //  @JvmStatic
    fun init(ctx: Context?) {
        try {
            if (sharedPref != null) return
           // Log.msg(TAG, "Init")
            this.context = ctx
            sharedPref = context!!.getSharedPreferences("downloader", Context.MODE_PRIVATE)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "init", ex.message)
        }
    }

   // @JvmStatic
    fun initialized(): Boolean {
        return sharedPref != null
    }

    // Int
    fun getSetting(key: String, valDefault: Int): Int {
        var result = valDefault
        try{
            init(context)
            result = sharedPref!!.getInt(key, valDefault)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "int getSetting - "+key, ex.message)
        }
        return  result
    }

    fun setSetting(key: String, value: Int) {
        init(context)
        try {
            val editor = sharedPref!!.edit()
            editor.putInt(key, value)
            editor.apply()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "int setSetting", ex.message)
        }
    }

    //Long
    fun getSetting(key: String, valDefault: Long): Long {
        init(context)
        // Log.msg(TAG,"key:"+ key )
        val valor  = sharedPref!!.getLong(key, valDefault!!)
        // Log.msg(TAG,"valor: ["+valor.toString()+"]")
        return valor
    }

    fun setSetting(key: String, value: Long) {
        init(context)
        try {
            val editor = sharedPref!!.edit()
            editor.putLong(key, value!!)
            editor.apply()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "Long setSetting", ex.message)
        }
    }

    // Boolean
    fun getSetting(key: String, valDefault: Boolean?): Boolean {
        init(context)
        return sharedPref!!.getBoolean(key, valDefault!!)
    }

    fun setSetting(key: String, value: Boolean?) {
        init(context)
        try {
            val editor = sharedPref!!.edit()
            editor.putBoolean(key, value!!)
            editor.apply()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "boolean setSetting", ex.message)
        }
    }

    // String
    fun getSetting(key: String, valDefault: String): String {
        init(context)
        var result: String = ""
        try {
            result = sharedPref!!.getString(key, valDefault).toString()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "String getSetting", ex.message)
        }
        return result
    }

    fun setSetting(key: String, value: String) {
        init(context)
        val editor = sharedPref!!.edit()
        editor.putString(key, value)
        editor.apply()
    }

    // String[]
    fun getSetting(key: String, valDefault: Array<String?>): Array<String?> {
        var valores = valDefault
        init(context)
        var result: Set<String?>? = null
        try {
            result = sharedPref!!.getStringSet(key, null)
            if (result != null) valores = result.toTypedArray()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "String[] getSetting", ex.message)
        }
        return valores
    }

    fun setSetting(key: String, myArray: Array<String?>) {
        init(context)
        try {
            val mySet: Set<String> = HashSet(Arrays.asList(*myArray))
            val editor = sharedPref!!.edit()
            editor.putStringSet(key, mySet)
            editor.apply()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "String[] setSetting", ex.message)
        }
    }

    // LocalDateTime
    @JvmStatic
    fun getSetting(key: String, valDefault: LocalDateTime): LocalDateTime {
        //Convirte a LocalDatetime
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val strFecha = sharedPref!!.getString(key, "")
        var dteFecha = valDefault //LocalDateTime.now();
        try {
            if (strFecha != "") dteFecha = LocalDateTime.parse(strFecha, formatter)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "LocalDateTime getSetting", ex.message)
        }
        return dteFecha
    }

    fun setSetting(key: String, dteFecha: LocalDateTime) {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val strFecha = dteFecha.format(formatter)
            val editor = sharedPref!!.edit()
            editor.putString(key, strFecha)
            editor.commit()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "LocalDateTime setSetting", ex.message)
        }
        return
    }

    enum class status {
        Pendiente,  //Recien iniciado
        Enrolo,  //Registro en BD Central
        Configurando,  //Aplico Politicas
        Instalado,  // sustituye a Terminado
        Vendido //Scaneo correctamente el Codigo de Barras.
        //  Terminado  //Aolico Politicas Proceso Terminado.
    }
}