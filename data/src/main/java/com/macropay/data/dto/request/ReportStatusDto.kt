package com.macropay.data.dto.request

data class ReportStatusDto(
    val imei: String,
    val lock_id: String,
    val locked: String,
    val kiosk: String,
    val orden:Long,
    val mqtt: Int  //1:mqtt, 0,http
)