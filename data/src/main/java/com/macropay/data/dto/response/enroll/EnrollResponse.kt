package com.macropay.data.dto.response.enroll

data class EnrollResponse(
    val data: Data,
    val message: String,
    val status: Int,
    val id_registro:String
)