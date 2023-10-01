package com.macropay.data.dto.response

data class Cp(
    val encrypted: String,
    val iv: String,
    val key: String
)