package com.macropay.data.dto.response.enroll

data class Data(
    val nivel_bloqueo: String,
    val restrictions: List<Restriction>,
    val trans_id: Int,
    val user_id: String,
    val lock_id:Int
)