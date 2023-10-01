package com.macropay.downloader.utils.device

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.DevAdminService
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.data.preferences.dpcValues.timerMonitor
import com.macropay.downloader.utils.Utils
import com.macropay.downloader.utils.activities.Dialogs

object DeviceService {
        fun enableWifi(context: Context) {
            try {
                val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (!wifi.isWifiEnabled) {
                    Log.msg(TAG, "SE PRENDIO EL WIFI...")
                    wifi.isWifiEnabled = true // true or false to activate/deactivate wifi
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "enableWifi", ex.message)
            }
        }

        /*private fun configUSB() {
            val sysConfig = SysConfig()
            val iValor = sysConfig.getProperty(SysConfig.SERVICE_ADB_ENABLE, 190)
            Log.msg(TAG, "[] iValor: $iValor")
            var valor = sysConfig.getProperty(SysConfig.SYS_USB_CONFIG, "xxx")
            Log.msg(TAG, "[] valor: $valor")
            sysConfig.setProperty(SysConfig.SYS_USB_CONFIG, "nada")
            Log.msg(TAG, "[] valor: asigno valor ")
            valor = sysConfig.getProperty(SysConfig.SYS_USB_CONFIG, "xxx")
            Log.msg(TAG, "[] nuevo valor: $valor")
        }
*/


        var TAG = "DeviceService"
        fun hideKeyboard(view: View?) {       // val view = this.currentFocus
            if (view != null) {
                val imm = view.context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)

                //  imm.showSoftInput(view,0)
            }
        }

        fun showKeyboard(view: View?) {       // val view = this.currentFocus
            if (view != null) {
                val imm = view.context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                //imm.hideSoftInputFromWindow(view.windowToken, 0)
                //imm.showSoftInput(view,0)
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
        }

        fun reboot(context: Context) {
            try {
                Log.msg(TAG, "reboot")
                Log.msg(TAG, "\n\n\n\n\n\n")
                val handler = Handler()
                try {
                    handler.postDelayed({
                        val mDevicePolicyManager: DevicePolicyManager
                        val mAdminComponentName: ComponentName
                        mDevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                        mAdminComponentName = DeviceAdminReceiver.getComponentName(context)
                        mDevicePolicyManager.reboot(mAdminComponentName)
                    }, 2000)
                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "Reboot", ex.message)
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "reboot", ex.message)
            }
        }
        fun isAndrodiGO(context:Context):Boolean{
            var isEnabled = false
            try{
                val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                isEnabled =  manager.isLowRamDevice();
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "configWifi", ex.message)
            }
            return isEnabled
        }
        fun configWifi(context: Context) {
            timerMonitor!!.enabledKiosk(false,null,null)
            try {
                //
                //  DeviceService.startWiFI();
                Log.msg(TAG, "[configWifi] sdk: ${Utils.SDK_INT} ver: 6 ${isAndrodiGO(context)}")


                var intent: Intent
                if (Utils.SDK_INT <= 28 || isAndrodiGO(context)){ //30  tenia 28
                    Log.msg(TAG, "[configWifi]  ACTION_WIFI_SETTINGS")
                    intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    intent.putExtra("show_back_button", false)
                }
                else{
                    Log.msg(TAG, "[configWifi]  ACTION_PICK_WIFI_NETWORK")
                    intent = Intent(WifiManager.ACTION_PICK_WIFI_NETWORK)
                    intent.putExtra("show_back_button", false)
                }
                Log.msg(TAG, "[configWifi] -1-")
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                context.startActivity(intent)
                Log.msg(TAG, "[configWifi] -2-")
/*var bundle = Bundle()

                bundle.putBoolean("extra_prefs_show_button_bar", false)
                bundle.putBoolean("extra_prefs_set_back_text", false)
                bundle.putBoolean("extra_prefs_set_next_text", false)

intent.putExtras(bundle)*/
                //   intent.putExtra("extra_prefs_set_next_text", null as String?)
                //Log.msg(TAG, "[configWifi] Util.SDK_INT: ${Utils.SDK_INT} ver:2")
                //intent.putExtra("extra_prefs_show_button_bar",true)
/*                Log.msg(TAG, "[configWifi] -1-")
                intent.putExtra("only_access_point",true)
                Log.msg(TAG, "[configWifi] -2-")
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                Log.msg(TAG, "[configWifi] -3-")
                context.startActivity(intent)
                Log.msg(TAG, "[configWifi] -4-")*/


         //       val intent = Intent(WifiManager.ACTION_PICK_WIFI_NETWORK)

               // intent.putExtra("only_access_points", true);
             //   intent.putExtra("extra_prefs_show_button_bar", true);
               // intent.putExtra("wifi_enable_next_on_connect", true);


            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "configWifi", ex.message)
            }
        }


   /* fun configWifi2(context: Context) {
        timerMonitor!!.enabledKiosk(false,null,null)
        try {
            //
            //  DeviceService.startWiFI();
            Log.msg(TAG, "[configWifi] ==========================================")
            Log.msg(TAG, "[configWifi] Util.SDK_INT: ${Utils.SDK_INT} ver: 6")
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            intent.putExtra("show_back_button", false)
            Log.msg(TAG, "[configWifi] -1-")
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            context.startActivity(intent)
            Log.msg(TAG, "[configWifi] -2-")


        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "configWifi", ex.message)
        }
    }*/
    fun activeService(context: Context, bEnabled: Boolean) {
        val classService =  DevAdminService::class.java
        Log.msg(Dialogs.TAG, "[activeService] : enabled: [$bEnabled]-classService: [$classService]")
        try {
            val p = context.packageManager
            val componentName = ComponentName(context,classService)
            if (!bEnabled){
                Log.msg(Dialogs.TAG,"[activeService] COMPONENT_ENABLED_STATE_DISABLED [desctivo]")
                p.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                )
            }
            else{
                Log.msg(Dialogs.TAG,"[activeService] COMPONENT_ENABLED_STATE_ENABLED [activo] ENBLED")
                p.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            }
        } catch (e: Exception) {
            ErrorMgr.guardar(Dialogs.TAG, "activeService", e.message)
        }
    }

    fun isServiceEnabled(context: Context):Boolean{
        var bEnabled = false
        try {
            val p = context.packageManager
            val componentName = ComponentName(context, DevAdminService::class.java)
            var curStatus = p.getComponentEnabledSetting(componentName)

            bEnabled= (curStatus == PackageManager.COMPONENT_ENABLED_STATE_ENABLED )
            Log.msg(Dialogs.TAG, "[isServiceEnabled]  bEnabled: "+bEnabled)
        }catch (ex:Exception){
            ErrorMgr.guardar(Dialogs.TAG,"isServiceEnabled",ex.message)
        }
        return bEnabled

    }
}