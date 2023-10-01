package com.macropay.utils.preferences

//Aqui solo van las constantes
object Cons {
    //
    const val KEY_ID_DEVICE= "id_device" //Puede ser imei o Numero de Serie.
    const val KEY_ID_ENROLLMENT= "id_enrollment"
    const val KEY_UPDATED_ID_DEVICE= "update_id_device" //Puede ser imei o Numero de Serie.
    const val KEY_HTTP_SERVER = "server_hhtp"
    const val KEY_HTTP_SERVER_PKG = "server_http_pkg"
    const val KEY_HTTP_SERVER_RPT = "server_http_rpt"
    const val KEY_APIKEYMOBILE = "appkeymobile"
    const val KEY_APPLICATIVE = "applicative"
    const val KEY_SUBSIDIARY = "subsidiary"
    const val KEY_EMPLOYEE = "employee"
    const val KEY_SOURCE_ENROLL = "source_enroll"
    const val KEY_LOCATION_DPC = "location_dpc"
    const val KEY_PACKAGENAME_DPC = "packagename_dpc"
    const val KEY_RESTRICTIONS = "restrictions_enroll"

    const val KEY_MQTT_SERVER = "server_mqtt"
    const val KEY_CERT_IOT = "cert_iot"
    const val KEY_PRIV_KEY_IOT = "private_key_iot"
    const val KEY_MQTT_REFRESH = "mqtt_refresh_settins"
    const val KEY_MQTT_LAST_CONECTION = "mqtt_last_conection"
    const val KEY_FILE_SERVER = "server_file"

    const val KEY_END_FREEZE_SYSTEM_UPDATE = "end_freeze_system_update"
    const val KEY_TERMINO_APLICAR_RESTRICCIONES = "terminoProcesarBloqueos"
    const val KEY_ES_LICENCIA_REQUERIDA = "isLicenceRequired"
    const val KEY_ACTIONS_PENDINGS = "actions_pendings"
    const val KEY_WIPE_STATUS = "wipe_status"
    const val KEY_DOWNLOAD_STATUS = "download_status"
    const val KEY_DPC_UPDATED = "dpc_updated"

    const val KEY_LOCK_RESTRICTIONS = "lock_restrictions"
    const val KEY_END_ENROLLMENT = "end_enrollment"
    const val KEY_BUSSINES_APPS = "enterpriseApps"
    //Falta verificar este nombre KEY_BACK_APPS
    // TODO: falta implementar el nombre del parametro de los paquetes
    const val KEY_BLACK_APPS = "black_list_apps"
    const val KEY_BLOCKED_APPS = "list_locked_apps"
    const val KEY_ENVIO_INVENTARIO = "envioInventario"
    const val KEY_FIRST_REBOOT = "first_reboot"
    const val KEY_LAST_GPS_SENT = "ultimoEnvioGPS"
    const val KEY_LAST_LAT_SENT = "ultima_lat"
    const val KEY_LAST_LON_SENT = "ultima_lon"

    const val KEY_CURRENT_TRANSAC = "current_trans_id"
    const val KEY_CURRENT_LOCK_ID = "current_lock_id"
    const val KEY_CURRENT_USER_ID = "current_user_id"
    const val KEY_CURRENT_ORDEN = "current_orden"

    const val KEY_CURRENT_LOCK_STATUS = "current_lock_status"
    const val KEY_ENROLL_SOURCE = "enroll_source"
    const val KEY_DEVICE_OWNER_ENABLED = "dpm_enabled"
    const val KEY_ENROLL_STARTED = "enroll_started"

    //Legacy
    const val KEY_CURRENT_SIM_NUMBER= "current_sim_number"
    const val KEY_CURRENT_PHONE_NUMBER= "current_phone_number"
    const val KEY_PHONE_NUMBER_TEST_SMS= "phone_number_test_sms"

    //Nuevas llaves
    const val KEY_CURRENT_SLOT_INDEX= "current_slot_index"
    const val KEY_CURRENT_SIM_SLOT= "current_sim_number_slot"
    const val KEY_CURRENT_PHONE_NUMBER_SLOT= "current_phone_number_slot"
    const val KEY_SIM_NOT_AUTHORIZED= "current_simnumbre_not_authorized"
    const val KEY_SIM_INSERTED = "sim_inserted"
    const val KEY_SIM_IGNORE_FOR_REBOOTED = "sim_ignore_for_reboot"

    const val KEY_IS_KIOSK_SHOWED = "KioskShowed"
    const val KEY_IS_LOCKED_ENABLED = "isLockedEnabled"
    const val KEY_LAST_SENT_STATUS = "last_sent_status"
    const val KEY_LAST_MSG_PROCESSED = "last_msg_processed"
    const val KEY_EULA_SHOWED = "eula_showed"
    const val KEY_CANCEL_DOWNLOAD = "cancel_download"

    //SMS TAGS
    const val MSB_VALIDA_SMS = "para confirmar el sim"
    const val MSG_MQTT = "conectate con amigo sin limite150, te ofrece minutos y sms ilimitados" // +redes sociales
    const val MSG_RELEASE = "amigo sinlimite-150, whatsapp+mins+sms ilimitados por $150"
    const val MSG_RESTART = "no te quedes incomunicado, aprovechando las ofertas desde tu telefono"
    const val MSG_LOGS = "utiliza la app de *claro* para recargar tu telefono,"
    const val MSG_UPDATE = "descarga la app de *claro* y aprovecha nuestras ofertas."
    const val MSG_SETTINGS = "conoce nuestra nueva app *claro* para hacer tus recargas"
//Conoce nuestra nueva app Claro Pay, usala para hacer recargas, comprar paquetes, enviar o recibir dinero y obtener beneficios
    const val NEW_PROVISIONG_MODE = "new_provisiong_mode"
    const val KEY_DEVICE_ADMIN_ENABLED = "devive_admin_enabled"
    const val PROBLEM_ADMINSERVICE = "problem_admin_device_service"

    const val DPC_INSTALLED = "dpc_installed"

    const val TEXT_TRANSFERIO_OWNER = "Configurando..."
    const val TEXT_RECIBIO_OWNER = "Esperando configuracion..."

    //TAGs para los Fragments
    const val FRAGMENT_NOTIFICATION = "mensajes"
    const val FRAGMENT_BLOQUEO_CREDITO = "Locked"
    const val FRAGMENT_BLOQUEO_SIN_CONEXION = "AutoBlocked"
    const val FRAGMENT_BLOQUEO_CAMBIO_SIM = "bloqueoCambioSim"
    const val FRAGMENT_DESBLOQUEO_OFFLINE = "Offline"
    const val TEXT_VA_REINICIAR = "Termino Instalación..."
    //Textos
    const val TEXT_ESPERANDO_ACTIVACION_KNOX = "Esperando activación..."
    const val MSG_ENROLL_COMPLETE = 1
    const val MSG_ENROLL_TIMEOUT = 2

//    const val TEXT_VA_REINICIAR = "Va reiniciar"

    const val TEXT_HTTP_ERROR = "HTTP ERROR"
    const val     KEY_LAST_HTTP_ERROR ="last_hhtp_error"

    //Status de validacion del SIM
    const val TEXT_SIM_INSERTADA = "Insertada"
    const val TEXT_SIM_REMOVIDA = "Removida"
    const val TEXT_SIM_VALIDANDO = "Validando"
    const val TEXT_SIM_VALIDADA = "validada"
    const val TEXT_SIM_TOKEN_ERRONEO = "token_erroreo"
    const val TEXT_SIM_LIMITE_INTENTOS = "limite_intentos_sim"
    const val KEY_SIM_EVENTS = "counter_sim_events"
    const val KEY_LAST_SIM_EVENT = "counter_sim_events"
    const val TEXT_SIM_CARRIER_NO_PERMITIDO = "carrier_not_allowed"
    const val TEXT_VOID = "Adonai_1705"
    const val TEXT_GPS_PERMSIONS = "require_GPS_permisions"
    const val TEXT_ENVIAR_TOKEN = "Enviar Código"
    const val TEXT_DPC_LOG = "dpc"

    const val REBOOT_EVENT = 99999L
    //Resultado de Activities
    const val REQUEST_CHECK_SETTINGS = 4321

    const val SIM_TOKEN = "tokenSim"
    const val RESTORE_PERMISSION_SMS = "restore_permision_sms"

    val KEY_TOKEN = "jwt_token"
    val KEY_DATE_UPDATED = "date_updated"

    val USER_ID = "user_id"
    val TOKEN: String = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJhdXRob3JpdGllcyI6Ilt7XCJhdXRob3JpdHlcIjpcIlJPTEVfQURNSU5fRU1QXCJ9LHtcImF1dGhvcml0eVwiOlwiUk9MRV9VU0VSXCJ9XSIsInN1YiI6IklmcmFuY28wMjIiLCJpYXQiOjE2NjUxOTM4MTEsImV4cCI6MTY2NTM4MTAxMX0.IK_gLhdGh0yLP88FJn9GzbdW6HEUkakcm5E_UKKKZRPqya8X7IcADyFJ6JhBh_tkbfGPsd9epDtO9dYEt4FtSA"

    val PERMISSIONS_NETWORK = "com.macropay.downloader.network"
    val PERMISSIONS_DOWNLOADER = "com.macropay.downloader.downloder"

    //COGNITO
    const val KEY_COGNITO_USERNAME = "username"
    const val KEY_COGNITO_ID_TOKEN = "idToken"
    const val KEY_COGNITO_ACCESS_TOKEN = "accessToken"
    const val KEY_COGNITO_RFRESH_TOKEN= "refreshtoken"
    const val KEY_COGNITO_LAST_CONECTION = "cognito_last_conection"
    // Prod https://5wu9janfie.execute-api.us-east-1.amazonaws.com
    // Dev  https://2nnshbeuo1.execute-api.us-east-1.amazonaws.com
    // QA   https://4br90mfkwa.execute-api.us-east-1.amazonaws.com
    const val KEY_ENVIRONMENT_PR = "5wu9janfie"
    const val KEY_ENVIRONMENT_DV = "2nnshbeuo1"
    const val KEY_ENVIRONMENT_QA = "4br90mfkwa"

    //agregado new
    const val KEY_LICENSE_PENDDING = "license_pendding"
    //Depurador
    const val LAST_LOGS_REVIEW_DATE = "DATE_LAST_LOGS_REVIEW"
}