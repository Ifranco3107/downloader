package com.macropay.data.dto.response

data class Cap(
    val encrypted: String,
    val iv: String,
    val key: String
)