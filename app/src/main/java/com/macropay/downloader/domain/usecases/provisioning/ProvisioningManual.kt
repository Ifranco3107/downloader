package com.macropay.downloader.domain.usecases.provisioning


import android.app.admin.DevicePolicyManager
import android.content.Context
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ProvisioningManual
    @Inject constructor(@ApplicationContext var context: Context,) {
    var TAG = "ProvisioningManual"


    @Inject
   lateinit var provisioning : Provisioning

    suspend fun continuaInstall() {
        Log.msg(TAG, "++++++ CONTINUA INSTALACION +++++++")
        val packageName = context.packageName
        val mDevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(packageName)
        val isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(packageName)
        Log.msg(TAG, "[onCreate] isProfileOwner: $isProfileOwner")
        Log.msg(TAG, "[onCreate] isDeviceOwner: $isDeviceOwner")
        Settings.setSetting("restartInstall", false)


        //
        //provisioning = Provisioning(context)
        provisioning.iniciaProceso12()
    }
}