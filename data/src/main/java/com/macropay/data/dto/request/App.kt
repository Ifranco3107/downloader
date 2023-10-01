package com.macropay.data.dto.request

import com.google.gson.annotations.SerializedName

data class App(
    @SerializedName("packageVersion")
    val packageVersion: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("tipo")
    val tipo: Int,
    @SerializedName("usage")
    val usage: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("versionName")
    val versionName: String

)