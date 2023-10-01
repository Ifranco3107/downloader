package com.macropay.downloader.utils.app

import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.preferences.Kiosko

class InstallStatus
{
    //Estado de instalacion.
    enum class eEstado {
        Instalada("installed", 1),
        Actualizada("updated", 2),
        NoActualizada("not-updated", 3),
        InicioDescarga("download-started", 4),
        DescargaCompleta("download-completed", 5),
        DescargaIncorrecta("download-failed", 6),
        NoCompatible("not-compatible", 7);

        //Funcionalidad adicional
        var key: String = "null"

        //Funciones.
        var id = 0
            private set

        //Constructor
        constructor(key: String, idStatus: Int) {
            this.key = key
            this. id = idStatus
        }
        constructor() {}

        fun getKey(name:String):String{
         val status =    eEstado.values().find { it.name == name }
            return status!!.key
        }

        fun fromId(id: Int): Kiosko.eTipo {
            var tipo = Kiosko.eTipo.SinKiosko
            for (type in Kiosko.eTipo.values()) {
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

}