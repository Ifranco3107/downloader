package com.macropay.data.usecases

import android.content.Intent
import com.macropay.data.preferences.Values
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log

object Sender {
    val TAG = "Sender"
    val ACTION_STATUS_ENROLLMENT = "com.macropay.downloader.action.STATUS_ENROLLMENT"

    fun sendEnrollResult(success:Boolean,code:Int, body: String) {
        Log.msg(TAG,"sendEnrollStatus")
        try {
            val intent: Intent = Intent(ACTION_STATUS_ENROLLMENT)
            intent.setPackage(Values.context!!.getPackageName())
            intent.putExtra("success", success)
            intent.putExtra("code", code)
            intent.putExtra("body", body)
            Values.context!!.sendBroadcast(intent)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendEnrollStatus", ex.message)
        }
    }

}