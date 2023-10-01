package com.macropay.data.dto.request

data class MsgMQTT(
    var action: Int,
    val id_dispositivo_estatus_pasos: Int,
    var imei: String,
    val lock_id: String,
    val orden: Long,
    val restrictions: List<Restriction>,
    val trans_id: String,
    val user_id: String
)