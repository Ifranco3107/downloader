package com.macropay.downloader.data.preferences

//son parametros adicionales a una restriccion,
//o parametros individuales.
//Los valores de la restricion,tambien se manejan como parametro, pero estan en TipoBloqueo.
object TipoParametro {
    //TipoBloqueo.show_kiosko
    const val tipo_bloqueo = "tipo_bloqueo"
    const val currentBalance = "current_balance"
    const val loanMessage = "loan_message"

    //TipoBloqueo.disable_frp_google
  //  const val google_account_id = "google_account_id"
    const val google_account_id = "id_account"
    const val google_account_email = "email_account"

    //Posilbles tipos de bloqueo...
/*    const val bloqueo_credito = "bloqueo_credito" //credito,sim_change,sin_conexion
    const val bloqueo_sim_change = "bloqueo_sim_change"
    const val bloqueo_sin_conexion = "sin_conexion"*/

    // TipoBloqueo.disable_tracking_GPS
    const val limiteSinEnvioGPS = "gps_send_forced"
    const val rangoMismoLugarGPS = "gps_same_place_range"
    const val frecuenciaCapturaGPS = "gps_send_interval"

    // TipoBloqueo.bloqueo_sin_conexion
    const val limiteSinConexion = "limit_without_connection"

    // TipoBloqueo.bloqueo_sim_change
    const val carriers_allowed = "carriers_allowed" //Lista de carriens a bloquear.
    const val requiere_validacion_SMS = "require_validation_SMS"

    // TipoBloqueo.disable_incoming_calls
    const val numbers_exception_incoming = "numbers_exception_incoming"

    // TipoBloqueo.disable_outgoing_calls
    const val numbers_exception_outgoing = "numbers_exception_outgoing"
    //Propiedades del layout del activity
    const val emergencyNumber = "phone_emergency"
    const val callCenterNumber = "phone_call_center"


    // TipoBloqueo. disable_list_apps
    const val apps_bloqueadas = "apps_bloqueadas"

    //-------------------------------
    // Configuraciones de Enrolamiento
    // TipoBloqueo.requiere_validacion_QR
    const val codigoValidacionQR = "code_QR_F2"

    // TipoBloqueo.showEula
    const val eulaTitle = "eula_title"
    const val eulaBody = "eula_body"

    // TipoBloqueo.showBienvenida
    const val bienvenidaTitle = "welcome_title"
    const val bienvenidaBody = "welcome_body"

    //Parametros generales
    //Monitoreo de conexion
    const val medidaTiempo = "medida_tiempo"
    const val frecNotificaStatus = "interval_notification_status"

    //ESTAS DEBEN ESTAR EN CONS.
    //fecha de limite de Update System.
    const val KEY_END_FREEZE_SYSTEM_UPDATE = "end_freeze_system_update"

    //Variables que deben venir en el
    const val customerId = "customer_id"
    const val customerName = "customer_name"


}