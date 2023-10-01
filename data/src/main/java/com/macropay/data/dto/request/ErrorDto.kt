package com.macropay.data.dto.request

data class ErrorDto(
    val imei: String,
    val fecha_alta: String,
    val app: String,
    val clase: String,
    val funcion: String,
    val error: String,
    val datos: String
)