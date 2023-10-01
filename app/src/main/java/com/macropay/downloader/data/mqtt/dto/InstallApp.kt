package com.macropay.downloader.data.mqtt.dto

data class InstallApp(
    val download_location: String,
    val package_name: String
)