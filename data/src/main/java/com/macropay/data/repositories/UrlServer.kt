package com.macropay.data.repositories

import com.macropay.data.preferences.Defaults
import com.macropay.utils.Settings
import com.macropay.utils.preferences.Cons

object UrlServer {
    //Si viene con // al final. la quita
    fun getHttp():String{
        var httpServer =  Settings.getSetting(Cons.KEY_HTTP_SERVER, Defaults.SERVIDOR_HTTP)
        if(httpServer.endsWith("/"))
            httpServer = httpServer.substring(0,httpServer.length-1)
        return httpServer
    }
    fun getHttpRpt():String{
        var httpServer =  Settings.getSetting(Cons.KEY_HTTP_SERVER_RPT,Defaults.SERVIDOR_HTTP_RPT)
        if(httpServer.endsWith("/"))
            httpServer = httpServer.substring(0,httpServer.length-1)
        return httpServer
    }

    fun getHttpPackage():String{
        var httpServer =  Settings.getSetting(Cons.KEY_HTTP_SERVER_PKG,Defaults.SERVIDOR_HTTP_PKG)
        if(httpServer.endsWith("/"))
            httpServer = httpServer.substring(0,httpServer.length-1)
        return httpServer
    }
}
