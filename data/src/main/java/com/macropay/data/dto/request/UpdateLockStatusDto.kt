package com.macropay.data.dto.request


import com.google.gson.JsonObject


data class UpdateLockStatusDto(
    val imei: String,
    val lock_id: String,
    val locked: String,
    val kiosk: String,
    val trans_id: String,
    val ult_fec_act: String,
    val ult_fec_syncmovil: String,
    val user_id: String,
    val data: MsgMQTT?
)