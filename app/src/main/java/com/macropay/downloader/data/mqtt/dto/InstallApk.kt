package com.macropay.downloader.data.mqtt.dto

import com.macropay.data.logs.Log

/*
data class AppUpdate(
    val download_location: String,
    val package_name: String,
//    val accion: String? = "instalar"
)*/
class  InstallApk{
    constructor(download_location: String, package_name: String,accion: String) {
        this.download_location = download_location
        this.package_name = package_name
        this.accion = accion
    }
/*
    constructor(package_name: String, download_location: String) {
        Log.msg("AppUpdate","package_name: "+package_name)
        this.package_name = package_name
        this.download_location = download_location
        var accion = ApkInfoRequest.eAccion.instalar
    }
*/


    var download_location: String =""
        get(){
            return field
        }
        set(value) {
            Log.msg("AppUpdate","value: "+value)
            field = value
        }
    var package_name: String =""
        get(){
            return field
        }
        set(value) {
            field = value
        }
    var accion: String ="instalar"
        get(){
            return field
        }
        set(value) {
            field = value
        }
    /*
        val download_location: String,
        val package_name: String,
    //    val accion: String? = "instalar"*/
}