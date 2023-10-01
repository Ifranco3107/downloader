package com.macropay.downloader.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.CallSuper

/*
class HiltBroadcasterReceiver {
}
*/
abstract class HiltBroadcasterReceiver : BroadcastReceiver() {
    @CallSuper
    override  fun onReceive(context: Context, intent: Intent) {}
   // abstract fun createBindDeviceOwnerServiceHelper(context: Context, targetUser: UserHandle): BindDeviceAdminServiceHelper<IDeviceOwnerService>
}