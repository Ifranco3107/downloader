package com.macropay.data.dto.response

data class SyncReportResponse(
    val code: Int,
    val data: List<Evento>
)