package com.macropay.data.dto.response

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String
)