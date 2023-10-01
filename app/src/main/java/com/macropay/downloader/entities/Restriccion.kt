package com.macropay.downloader.entities

import java.io.Serializable
import java.util.ArrayList


    class Restriccion(var name: String, var enabled: Boolean, var params: Array<Parametro>) : Serializable {
    var apps: ArrayList<String> = ArrayList<String>()
        get() = field
        set(value){field= value}

    var TAG = "Restriccion"
}