package com.macropay.data.dto.request

data class SIMDto(
    val codigo: String,
    val dispositivo_so_id: String,
    val ext_telefono: String,
    val iccid_slot_1: String,
    val iccid_slot_2: String,
    val imei: String, // id de enrolamiento
    val imei_slot:String, //imei correspondiente al Slot
    val no_telefono: String,
    val carrier_id: String,
    val carrier_name: String,
    val country_code: String,
    val display_text: String,
    val mcc: String,
    val mnc: String,
    val subscriber_id: String,
    val confirmar:Boolean
)

