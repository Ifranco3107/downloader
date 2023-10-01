package com.macropay.data.dto.response.enroll

data class Restriction(
    val enabled: Boolean,
    val name: String,
    val params: List<Param>
)