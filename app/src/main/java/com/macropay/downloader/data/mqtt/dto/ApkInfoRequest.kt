package com.macropay.downloader.data.mqtt.dto

import com.google.gson.Gson

/*
[
  {
    "download_location": "http://3.87.88.183:3000/api/devices/mobile/bussiness/apk/download?appkeymobile=CMaz3r2r23r23r23r23ssd11SWSZXWEFWWFd99s",
    "package_name": "com.macropay.dpcmacro",
	"accion":"instalar"
  }
]
*/

class ApkInfoRequest {
    enum class eAccion {
        instalar, remover
    }

    constructor() {}
    constructor(packageName: String, downloadLocation: String) {
        this.packageName = packageName
        this.downloadLocation = downloadLocation
        var accion = eAccion.instalar
    }


    var packageName: String =""
        get(){
            return field
        }
        set(value) {
             field = value
        }
    var downloadLocation: String =""
        get(){
            return field
        }
        set(value) {
            field = value
        }
    var downloadId: Long = 0
        get(){
            return field
        }
        set(value) {
            field = value
        }
    var isDownloadCompleted = false
        get(){
            return field
        }
        set(value) {
            field = value
        }
    var isInstallCompleted = false
        get(){
            return field
        }
        set(value) {
            field = value
        }
    var versionCode = 0
        get(){
            return field
        }
        set(value) {
            field = value
        }
    var accion :eAccion = eAccion.instalar
        get(){
            return field
        }
        set(value) {
            field = value
        }


    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}