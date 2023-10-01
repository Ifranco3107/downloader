package com.macropay.data.dto.response

data class UrlIotClient(
    val encrypted: String,
    val iv: String,
    val key: String
)