package com.macropay.downloader.data.preferences

import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log

//Estado del proceso de instalaciond e Macrolock
object Status {
/*    enum class status {
        Pendiente,  //Recien iniciado
        Enrolo,  //Registro en BD Central
        Configurando,  //Aplico Politicas
        Instalado,  // sustituye a Terminado
        Vendido //Scaneo correctamente el Codigo de Barras.
        //  Terminado  //Aolico Politicas Proceso Terminado.
    }*/
    val TAG = "Status"
    val KEY_STATUS = "statusEnrolamiento"

    var currentStatus: eStatus
    get() {

        var  status = eStatus.SinInstalar
        try {
            val nivelName = Settings.getSetting(KEY_STATUS,eStatus.SinInstalar.name)
            status=  eStatus.valueOf(nivelName!!)
        } catch (ex:Exception) {
            ErrorMgr.guardar("Status.Get", "currentStatus", ex.message)
            status = eStatus.SinInstalar
        }
        return status
    }

    set(value) {
        Log.msg(TAG,"set: "+value)
        //Si ya fue liberado, ya no debe permitir cambar de estado... Sin Permisos...
/*        if (value == eStatus.Liberado) {
            ErrorMgr.guardar(TAG, "Status.Set", "No se permite asignar el nivel:[$value] porque el telefono ya esta liberado.")
            return
        }*/
        try {
            Settings.setSetting(KEY_STATUS,value.name)

        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "statusEnroll", ex.message)
        }
    }

    //Tipos de kiosko.
    enum class eStatus {
        SinInstalar("SinInstalar", 0),
        RegistroEnServer("RegistroEnServer", 1),        //Recibio respuesta el API.                 Se asigna en: PostEnrollment.onProcessPolicies
        AplicoRestricciones("AplicoRestricciones", 2), //Se aplicaron las Retricciones Iniciales.   Se asigna en Enrollment.applyBloqueoInicial
        ConfirmoQR("ConfirmoQR", 3), //Confirmo el QR, al parecer NO SE USA....NI SE NECESITA, ya que tiene su propio parametro TipoBloqueo.requiere_validacion_QR
        TerminoEnrolamiento("TerminoEnrolamiento", 4),  //Termino,                                  se asigna en: Enrollment.terminaInstalacion
        Liberado("Liberado", 5); //Cuando ya el telefono fue liquidado, y se libera del todo bloqueo.

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

     //   companion object {

            fun fromId(id: Int): eStatus {
                var tipo = eStatus.SinInstalar
                for (type in eStatus.values()) {
                    try {
                        if (type.nivel == id) {
                            tipo = type
                            break
                        }
                    } catch (ex: Exception) {
                        ErrorMgr.guardar("eStatus", "fromId", ex.message)
                    }
                }
                return tipo
            }
        }
   // }
}