package com.macropay.data.dto.response

data class Kc(
    val encrypted: String,
    val iv: String,
    val key: String
)