package com.macropay.downloader.data.preferences


import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.data.awsiot.Mqtt
import com.macropay.downloader.timers.TimerMonitor
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.downloader.di.Inject
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.preferences.Cons


@SuppressLint("StaticFieldLeak")
object dpcValues {

    var mContext: Context? = null
        get() {
        return field
    }
    set(value) {
      //  Log.msg("dpcValues","se asigno el context general...")
        field = value
    }
    var mActivity: AppCompatActivity? = null
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var mDpm: DevicePolicyManager? = null
        get() {
            return  mContext!!.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        }

    var mAdminComponent: ComponentName? = null
       get() {
           return ComponentName(mContext!!, DeviceAdminReceiver::class.java)
       }

    var appPackageName: String? = null
        get() {
            return mContext!!.packageName
        }

    var isOwner: Boolean = false
        get() {
            return mDpm!!.isDeviceOwnerApp(appPackageName)
        }

    var isAdmin: Boolean = false
        get() {
          return  mDpm!!.isAdminActive(mAdminComponent!!)
        }

    //Lista de apps de Negocio
    var enterpriseApps: Array<String?>
    get() {
        return  Settings.getSetting(Cons.KEY_BUSSINES_APPS,  arrayOf<String?>())
    }
    set(value) {

        Settings.setSetting(Cons.KEY_BUSSINES_APPS,  value)
        Log.msg("SET","KEY_BUSSINES_APPS: "+value.toString())
    }
    var imei: String ="inst"
        get() {
            return DeviceCfg.getImei(mContext!!)
        }

    var timerMonitor: TimerMonitor? = null
        get() {
/*            if(field == null){
            //    Log.msg("dpcValues","creo el timerMonitor...")
                field =  TimerMonitor( mContext!!)
            }*/

            return    field
        }
    set(value) {
        field = value
    }
    var mqttAWS: Mqtt? = null
        get() {
            if(field == null){
                Log.msg("dpcValues","[mqttAWS] creo el mqttAWS...")
                //field =  Mqtt( mContext!!)
                field = Inject.inject(mContext!!).getMqtt()
            }
          //  Log.msg("dpcValues","creo el mqttAWS... -2-")
            return    field
        }
        set(value) {
            field = value
        }

    var curLockId :Int = 0
        get() {
            return    field
        }
        set(value) {
            field = value
        }
    var isProvisioning :Boolean = false
        get() {
            return    field
        }
        set(value) {
            field = value
        }
/*    lateinit var  contextGral: Context
    lateinit var componentName :ComponentName
    lateinit var devicePolicyManager :DevicePolicyManager
    lateinit var packageName:String*/


}