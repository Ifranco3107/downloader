package com.macropay.data.dto.request

data class Restriction(
    val enabled: Boolean,
    val name: String,
    val params: List<Param>
)