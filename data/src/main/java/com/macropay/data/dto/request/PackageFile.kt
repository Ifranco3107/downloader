package com.macropay.data.dto.request

import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import org.json.JSONObject
import org.json.JSONException
import java.io.Serializable

class PackageFile : Serializable, Comparable<PackageFile> {
    var packageName: String
    var name: String
    var version: Long = 0
    var versionName: String? = null
    var status = 0
    var usage: Long
    var tipo = 0

    constructor(package_name: String, name: String, version: Long, version_name: String?, tipo: Int, status: Int) {
        packageName = package_name
        this.name = name
        this.version = version
        versionName = version_name
        this.tipo = tipo
        this.status = status
        usage = 0
    }

    constructor(package_name: String, name: String, usage: Long) {
        packageName = package_name
        this.name = name
        this.usage = usage
    }

    //[{"package_name":"com.macropay.dpcmacro","status":1,"version":63,"version_name":"2.21"}]
    override fun toString(): String {
        val postData = JSONObject()
        try {
            postData.put("package_name", packageName)
            postData.put("version", version)
            postData.put("version_name", versionName)
            postData.put("status", status)
        } catch (ex: JSONException) {
            ErrorMgr.guardar("TAG", "toString", ex.message)
        }
        Log.msg("TAG", postData.toString())
        return postData.toString()
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }




    override fun compareTo(other: PackageFile): Int {
        //return  (int)(this.usage - o.getUsage());
        return (other.usage - usage).toInt()
    }


}

//Convertidores... .
//fun AdjuntoTable.toDomain() = Adjunto(
//Convertidores... .
fun PackageFile.toApp() = App(
    packageVersion  =versionName!! ,
    name = packageName,
    status = status.toString(),
    tipo = tipo ,
    usage= "0",
    version= version.toString(),
    versionName= versionName!!
 )
