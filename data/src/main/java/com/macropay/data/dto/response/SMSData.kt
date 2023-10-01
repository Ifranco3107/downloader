package com.macropay.data.dto.response

data class SMSData(
    val codigo: String,
    val id_dispositivo: Int,
    val id_generacion_pin: Int,
    val no_telefono: String
)