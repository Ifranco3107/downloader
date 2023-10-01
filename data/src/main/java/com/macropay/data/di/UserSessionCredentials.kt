package com.macropay.data.di

import com.macropay.data.preferences.Defaults
import com.macropay.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.utils.preferences.Cons


class UserSessionCredentials (

    val userName:String? = "",
    val idToken:String? = "",
    val accessToken:String? = "",
    val refreshToken:String? =""
) {


    fun getHeaders(): HashMap<String, String> {
        val header = HashMap<String, String>()
        try{
            val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
            header["Content-Type"] = "application/json"
            header["username"] = userName!!
            header["idtoken"] = idToken!!
            header["accesstoken"] = accessToken!!
            header["refreshtoken"] = refreshToken!!
            header["appkeymobile"] = apiKeyMobile
        }catch (ex:Exception){
            ErrorMgr.guardar("UserSessionCredentials","getHeaders",ex.message)
        }

        return header
    }


    //cabeceras sin cognito
    fun getHeadersWithoutCognito(): HashMap<String, String> {
        val header = HashMap<String, String>()
        try{
            val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
            header["Content-Type"] = "application/json"
            header["appkeymobile"] = apiKeyMobile
        }catch (ex:Exception){
            ErrorMgr.guardar("UserSessionCredentials","getHeaders",ex.message)
        }

        return header
    }



}
