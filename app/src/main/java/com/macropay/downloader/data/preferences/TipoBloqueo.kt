package com.macropay.downloader.data.preferences


object TipoBloqueo {

    const val disable_save_boot_button = "disable_save_boot_button"
    const val disable_install_unknown_sources = "disable_install_unknown_sources"
    const val disable_usb_debug = "disable_usb_debug"
    const val disable_config_location = "disable_config_location"
    const val disable_frp_google = "disable_frp_google" //FRP con cuenta de google
    //const val disable_wipe_google = "disable_wipe_google" //Evitar hacer Restablecimiento de dato en Google

    const val disable_download_firmaware = "disable_download_firmware"
    const val disable_recovery_wipe_data = "disable_recovery_wipe_data" //Menu de recovery.

    const val disable_manage_accounts = "disable_manage_accounts"
    const val disable_screen_capture = "disable_screen_capture"
    const val disable_file_transfer = "disable_file_transfer"
    const val disable_install_apps = "disable_install_apps"
    const val disable_sms = "disable_sms"
    const val disable_camera = "disable_camera"


    const val disable_uninstall_bussines_apps = "disable_uninstall_bussines_apps"
    const val disable_usage_apps = "disable_usage_apps" //Por implementar - Estadistica de uso de apps.
    const val disable_report_phone_number = "disable_report_phone_number" //Por implementar.- Enviar los numeros telefonicos a los que llama el cliente.

    //Bloqueos con parametros
    const val disable_list_apps = "disable_list_apps" //Requiere lista de apps para bloquear
    const val disable_incoming_calls = "disable_incoming_calls" //Requiere lista de telefonos de excepcion.
    const val disable_outgoing_calls = "disable_outgoing_calls" //Requiere lista de telefonos de excepcion.
    const val disable_lock_for_not_connection = "lock_no_conection" //Requiere Limite de tiempo para bloqueo.

    const val disable_lock_for_sim_change = "lock_sim_change" //
    const val requireSimForEnroll = "require_sim_for_enroll" //
    const val enabledlockForRemoveSim = "enabled_lock_for_remove_sim"

    const val disable_tracking_GPS = "disable_tracking_GPS" //Require parametros:Frecuencia de Envio,etc.

    //Configuracion de Enrolamiento.
    const val requiere_validacion_QR = "require_QR_validation" //Requiere codigo de validacion.
    const val showBienvenida = "show_welcome" //Requiere Titulo y texto de bienvenida.
    const val showEula = "show_eula" //Requiere Titulo y Texto de EULA
    const val install_bussines_apps = "install_bussines_apps" //Lista de apps, para instalar.

    //Libeacion..
    const val hide_icon_dpc = "hide_icon_dpc"
    const val uninstall_dpc = "uninstall_dpc"
    const val uninstall_bussines_apps = "uninstall_bussines_apps" //Lista de apps para desinstalar.
    const val show_unrolled = "show_unrolled"

    //Show Kiosko
    const val show_kiosko = "show_kiosk"

    //No son opcionales, se debe aplicar Obligatoriamente.
    const val disable_system_update = "disable_system_update"

    //Nuevas propiedades

    //Encriptado
    const val disable_storage_encrypt = "disable_storage_encrypt"
    //Lista black
    const val black_list_apps = "enabled_black_list_apps"

}