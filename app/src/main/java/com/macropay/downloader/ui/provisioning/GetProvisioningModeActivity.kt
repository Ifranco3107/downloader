//https://stackoverflow.com/questions/70111346/android-12-device-owner-provisioning
package com.macropay.downloader.ui.provisioning

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.view.View
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.R

import com.macropay.utils.preferences.Cons
import java.lang.Exception

@SuppressLint("NewApi")
class GetProvisioningModeActivity : Activity() {
    private val TAG = "GetProvisioningModeActivity"

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        Log.init("downloader", this)
        ErrorMgr.init(this)

        Log.msg(TAG, "[onCreate]")
        try {
            setContentView(R.layout.activity_get_provisioning_mode)
            Settings.init(this)
            Settings.setSetting(Cons.NEW_PROVISIONG_MODE,true)
            onDoButtonClick(null)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "onCreate", ex.message)
        }
    }

    private fun onDoButtonClick(button: View?) {
        Log.msg(TAG, "[onDoButtonClick] PROVISIONING_MODE_FULLY_MANAGED_DEVICE")
        try {
            val intent = Intent()
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_MODE, DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE)
            finishWithIntent(intent)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "onDoButtonClick", ex.message)
        }
    }

    override fun onBackPressed() {
        Log.msg(TAG, "[onBackPressed]")
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    private fun finishWithIntent(intent: Intent) {
        Log.msg(TAG, "[finishWithIntent] RESULT_OK")
        setResult(RESULT_OK, intent)
        finish()
    }
}