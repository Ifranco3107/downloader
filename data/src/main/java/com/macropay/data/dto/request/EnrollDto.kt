package com.macropay.data.dto.request

data class EnrollDto(
    val dcp_version: String,
    val dcp_version_name: String,
    val fec_enroll: String,
    val hasImei: String,
    val id_bloqueo: String,
    val id_enrolado: String,
    val id_usuario: String,
    val imei: String,
    val marca: String,
    val modelo: String,
    val no_telefono: String,
    val os_version: String,
    val serie: String,
    val sistema_operativo: String,
    val ui_version: String,
    val ult_fec_act: String,
    val ult_fec_syncmovil: String,
    val username: String,
    val applicative:String,
    val subsidiary:String,
    val employee:String
)