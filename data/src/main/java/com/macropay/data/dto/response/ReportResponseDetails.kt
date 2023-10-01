package com.macropay.data.dto.response

data class ReportResponseDetails(
    val envios_exitosos: List<Long>,
    val envios_fallidos: List<Any>,
    val imei: String
)