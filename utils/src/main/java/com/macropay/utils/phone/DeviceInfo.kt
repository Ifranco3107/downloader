package com.macropay.utils.phone

import android.content.Context
import android.os.Build
import com.macropay.utils.Settings
import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.logs.Log
import com.macropay.utils.preferences.Cons
import java.lang.Exception

object DeviceInfo {
    private val TAG = "DeviceInfo"
    private val def = "00000"
    private val errPermission = "does not meet the requirements to access" // "The user 10257 does not meet the requirements to access device identifiers. "

    fun getDeviceID(): String {
        var deviceID = "Inst"
        try {
            //if (DeviceCfg.hasIMEI(context!!)) deviceID = DeviceCfg.getImei(context)
            deviceID = Settings.getSetting(Cons.KEY_ID_DEVICE, def)
            if(deviceID.equals(def)) {
                deviceID = DeviceCfg.getImei(Settings.context!!)
            }
        } catch (ex: Exception) {
            if(ex.message!!.contains(errPermission))
                deviceID = "Inst"

        }
        return deviceID
    }

    fun setDeviceID(context: Context?) {
        var deviceID = Build.getSerial()
        try {
            if (DeviceCfg.hasIMEI(context!!))
                deviceID = DeviceCfg.getImeiBySlot(context,0)

            //deviceID = DeviceCfg.getImei(context)
            Settings.setSetting(Cons.KEY_ID_DEVICE, deviceID!!)
            Log.msg(TAG,"[setDeviceID] deviceID: $deviceID")
        } catch (ex: Exception) {
            if(ex.message!!.contains(errPermission))
                deviceID = "Inst"
            else
                ErrorMgr.guardar(TAG, "setDeviceID", ex.message)
        }
    }
    fun getAndroidId(context: Context):String{
        var androidId = Build.getSerial()
        try{
            androidId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        } catch (ex: java.lang.Exception) {
            // ErrorMgr.guardar(TAG, "getDeviceID", ex.message)
        }
        return androidId
    }
}