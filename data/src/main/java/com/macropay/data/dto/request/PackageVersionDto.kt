package com.macropay.data.dto.request

import com.google.gson.annotations.SerializedName

data class PackageVersionDto(
    @SerializedName("apps")
    val apps: List<App>,
    @SerializedName("imei")
    val imei: String
)