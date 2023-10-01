package com.macropay.data.dto.request

import com.google.gson.annotations.SerializedName

data class LocationDto(
    @SerializedName("fec_gps")
    val fec_gps: String,
    @SerializedName("imei")
    val imei: String,
    @SerializedName("latitud")
    val latitud: String,
    @SerializedName("longitud")
    val longitud: String,
    @SerializedName("metros")
    val metros: Int
)