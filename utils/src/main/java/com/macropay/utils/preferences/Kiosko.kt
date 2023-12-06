package com.macropay.utils.preferences

import com.macropay.utils.Settings
import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.logs.Log


object Kiosko {
    private val TAG = "Kiosko"
    private val KEY_KIOSKO_ENABLED = "enableKiosko"
    private val KEY_KIOSKO_REQUIRED = "kioskoRequired"
    private val KEY_CURRENT_KIOSKO = "kiosko_actual"
    var enabled = false
        get() {
            val bEnabled =  Settings.getSetting(KEY_KIOSKO_ENABLED, false)
            return bEnabled
        }
        set(value) {
            Log.msg(TAG,"[SET] enabled= " +value)
            field = value
            Settings.setSetting(KEY_KIOSKO_ENABLED, field)
        }


    var kioskRequired:Boolean = false
        get() {
            val bEnabled =  Settings.getSetting(KEY_KIOSKO_REQUIRED, false)
            //Log.msg(TAG,"[get] kioskRequired: $bEnabled               <===========")
            return bEnabled
        }
        set(value) {
            field = value
            Settings.setSetting(KEY_KIOSKO_REQUIRED, field)
            //Log.msg(TAG,"[set] kioskRequired: $field")
        }

    //Tipo de kiosko /Credito,Cambio de Sim, Sin Red.
    var currentKiosko: eTipo
        get() {
            var tipoKiosko = eTipo.SinKiosko
            try {
                val nivelName = Settings.getSetting(KEY_CURRENT_KIOSKO,  eTipo.SinKiosko.name)
                tipoKiosko=  eTipo.valueOf(nivelName!!)
            } catch (ex: java.lang.Exception) {
                ErrorMgr.guardar("currentKiosko.Get", "currentKiosko", ex.message)
            }
            return tipoKiosko
        }

        set(value) {
            try {
                Log.msg(TAG,"[SET] "+KEY_CURRENT_KIOSKO + "= "+value.name)
                Settings.setSetting(KEY_CURRENT_KIOSKO,value.name)
            } catch (ex: java.lang.Exception) {
                ErrorMgr.guardar("currentKiosko-set", "currentKiosko", ex.message)
            }
        }


    //Tipos de kiosko.
    enum class eTipo {
        SinKiosko("SinKioko", 0),
        PorCredito("PorCredito", 1),
        PorNoConexion("PorNoConexion", 2),
        PorCambioSIM("PorCambioSIM", 3);

        //Funcionalidad adicional
        private var nombre: String? = null

        //Funciones.
        var nivel = 0
            private set

        //Constructor
        constructor(nombre: String, id: Int) {
            this.nombre = nombre
            nivel = id
        }
        constructor() {}


    }
    fun fromId(id: Int): eTipo {
        var tipo = eTipo.SinKiosko
        for (type in eTipo.values()) {
            try {
                if (type.nivel == id) {
                    tipo = type
                    break
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar("eTipo", "fromId", ex.message)
            }
        }
        return tipo
    }
}