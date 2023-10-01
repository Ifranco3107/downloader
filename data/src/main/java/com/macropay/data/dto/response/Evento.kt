package com.macropay.data.dto.response

data class Evento(
    val fecha: String,
    val id_cola_mensaje: Int,
    val imei: String,
    val mensaje: String,
    val orden: Long
)