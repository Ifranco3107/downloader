package com.macropay.downloader.data.preferences

import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.Log


object KnoxStatus {

    private val TAG = "KnoxStatus"
    private val KEY_STATUS = "PendingAction"
    private val KEY_LICENSE_KNOX_ACTIVATED = "isLicenseKnoxActivated"
    private val KEY_FRP_KNOX_ACTIVATED = "isFRPKnoxActivated"
    //Acciones pendientes.
    enum class eAction {
        none,
        Lock,
        Unlock,
        DisableDownlaod,
        EnableDownload
    }
    var pendingAction = eAction.none
        get() {
            val action : eAction = eAction.valueOf(Settings.getSetting(KEY_STATUS, "none"))
            return action
        }
        set(value) {
            field = value
            Settings.setSetting(KEY_STATUS, field.name)
        }



    var isLicensedActived = false
        get() {
         //   Log.msg(TAG,"[GET] isLicensedActived")
            val actived :Boolean = Settings.getSetting(KEY_LICENSE_KNOX_ACTIVATED,false)
         //   Log.msg(TAG,"[GET] isLicensedActived: -1- : "+actived)
            return actived
        }
        set(value) {
           // Log.msg(TAG,"[SET] isLicensedActived- "+value)
            field = value
           // Log.msg(TAG,"[SET] isLicensedActived- -1-")
            Settings.setSetting(KEY_LICENSE_KNOX_ACTIVATED,value)
           // Log.msg(TAG,"[SET] isLicensedActived- -2-")
        }

    var isSecurityActived = false
        get() {
            Log.msg(TAG,"[GET] isSecurityActived")
            val actived :Boolean = Settings.getSetting(KEY_FRP_KNOX_ACTIVATED,false)
            Log.msg(TAG,"[GET] isSecurityActived: -1- : "+actived)
            return actived
        }
        set(value) {
            Log.msg(TAG,"[SET] isSecurityActived- "+value)
            field = value
            Log.msg(TAG,"[SET] isSecurityActived- -1-")
            Settings.setSetting(KEY_FRP_KNOX_ACTIVATED,value)
            Log.msg(TAG,"[SET] isSecurityActived- -2-")
        }


}



