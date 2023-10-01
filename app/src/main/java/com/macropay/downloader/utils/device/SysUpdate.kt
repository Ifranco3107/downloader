package com.macropay.downloader.utils.device

import android.app.admin.DevicePolicyManager
import android.app.admin.FreezePeriod
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.utils.SettingsApp
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons
import java.lang.Exception
import java.time.LocalDateTime
import java.time.MonthDay
import java.util.ArrayList

object SysUpdate {
    private lateinit var mDevicePolicyManager: DevicePolicyManager
    private lateinit var mAdminComponentName: ComponentName

    var TAG = "SysUpdate"
    fun setSystemUpdatePolicy(enable: Boolean,context: Context): LocalDateTime? {
        Log.msg(TAG, "[setSystemUpdatePolicy] enable: $enable")
        var endPeriod = LocalDateTime.now()
        val periods: MutableList<FreezePeriod> = ArrayList()
        try {
            mDevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            mAdminComponentName = DeviceAdminReceiver.getComponentName(context) as ComponentName

            //
            var policy = mDevicePolicyManager!!.systemUpdatePolicy
            //   Log.msg(TAG, "[setSystemUpdatePolicy] ========= policy Actual: "+policy.getPolicyType());
            //Limpia el periodo anterior
            mDevicePolicyManager!!.setSystemUpdatePolicy(mAdminComponentName!!, null)
            if (!enable) {
                return null
            }

            //Define el plazo de Freeze, el maximo son 60 dias.
            endPeriod = endPeriod.plusDays(50)
            val start = MonthDay.of(LocalDateTime.now().month, LocalDateTime.now().dayOfMonth)
            val end = MonthDay.of(endPeriod.month, endPeriod.dayOfMonth)
            Log.msg(TAG, "[setSystemUpdatePolicy] start: $start")
            Log.msg(TAG, "[setSystemUpdatePolicy] end: $end")
            val freezePeriod = FreezePeriod(start, end)
            periods.add(freezePeriod)
            for (period in periods) {
                Log.msg(TAG, "[setSystemUpdatePolicy] Nuevo period: {" + period.start + "}  {" + period.end + "} -- [" + period.toString() + "]")
            }

            //Asigna el nuevo freeze
            policy = SystemUpdatePolicy.createPostponeInstallPolicy()
            policy.freezePeriods = periods
            mDevicePolicyManager!!.setSystemUpdatePolicy(mAdminComponentName!!, policy)
            Log.msg(TAG, "[setSystemUpdatePolicy] Termino... endPeriod: $endPeriod")
            SettingsApp.setSetting(Cons.KEY_END_FREEZE_SYSTEM_UPDATE, endPeriod)
        } catch (ex: Exception) {
            Log.msg(TAG, "ERROR: $ex")
            ErrorMgr.guardar(TAG, "setSystemUpdatePolicy ", ex.message)
        }
        return endPeriod
    }
}