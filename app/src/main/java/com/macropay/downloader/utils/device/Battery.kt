package com.macropay.downloader.utils.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.macropay.downloader.R
import com.macropay.data.logs.ErrorMgr

object Battery {
    private val TAG = "Battery"
    fun batteryLevel(context: Context):Float{
        var batteryPct: Float=0f
        try{
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                //context.registerReceiver(null, ifilter)
                context.registerReceiver(null, ifilter, R.string.name_permissions_battery.toString(),null)

            }
            batteryPct = batteryStatus?.let { intent ->
                val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                level * 100 / scale.toFloat()
            }!!
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "batteryLevel", ex.message)
        }
        return batteryPct
    }
    fun batteryStatus(context: Context):Boolean{
        var isCharging = false
        try{
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                // context.registerReceiver(null, ifilter)
                context.registerReceiver(null, ifilter, R.string.name_permissions_battery.toString(),null)
            }
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            isCharging   = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "batteryLevel", ex.message)
        }
        return isCharging
    }
}