package com.macropay.utils.logs

import android.annotation.SuppressLint
import android.content.Context


@SuppressLint("StaticFieldLeak")
object ErrorMgr {

    var context: Context? = null
        get() {return field}
        set(value) {field = value}
    fun init(ctx: Context?) {
        context = ctx
    }

    fun initialized(): Boolean {
        return context != null
    }
    fun guardar(clase: String, funcion: String, msg: String?) {
        try {
            val mensaje = "Clase: $clase "+
                "\n|ERROR|Funcion: $funcion "+
                "\n|ERROR|Error:$msg "
           Log.msg("ERROR", mensaje)

/*            val mensaje = "Clase: $clase\n\t\t| Funcion: $funcion\n\t\t| Error:$msg"
            msg("ERROR", mensaje)*/
        } catch (ex: Exception) {
            println("guardar[1]: ERROR: "+ex.message)
            return
        }
    }
    fun guardar(clase: String, funcion: String, msg: String?,data:String) {
        try {
            var mensaje = "Clase: $clase "+
                    "\n|ERROR|Funcion: $funcion "+
                    "\n|ERROR|Error:$msg "
            if(data != null){
                mensaje+=  "\n|ERROR|data:$data "
            }
            Log.msg("ERROR", mensaje)

/*            val mensaje = "Clase: $clase\n\t\t| Funcion: $funcion\n\t\t| Error:$msg"
            msg("ERROR", mensaje)*/
        } catch (ex: Exception) {
            println("guardar[2]: ERROR: "+ex.message)
            return
        }
    }
}