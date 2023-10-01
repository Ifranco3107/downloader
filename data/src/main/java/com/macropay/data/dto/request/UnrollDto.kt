package com.macropay.data.dto.request



data class UnrollDto(
    val enroll_id: String,
    val fec_liberacion: String,
    val hasImei: String,
    val imei: String,
    val lock_id: String,
    val trans_id: String,
    val user_id: String
    //val data: MsgMQTT
)