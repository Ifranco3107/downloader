package com.macropay.downloader.domain.usecases.manual

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import com.macropay.data.preferences.Defaults
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.app.PackageService
import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.logs.Log
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Arrays
import javax.inject.Inject


class TransferCtrl
@Inject constructor(
    @ApplicationContext var context: Context) {

    @Inject
    lateinit var packageService : PackageService


    //private val mContext: Context? = null
    val TAG = "TransferCtrl"
    fun transfer(packageTarget:String) {
        try{
            //Transfirio el Owner a DPC
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                Log.msg(TAG, "[transfer] va transferir owner.")
                val bundle: PersistableBundle = getParameters()
                //UpdateDPC(packageTarget,bundle)
                transferControl(packageTarget,bundle)
            Log.msg(TAG, "[transfer] transfirio owner.")
            }, 3_000)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "transfer", ex.message)
        }
    }


    private  fun transferControl(target: String,bundle: PersistableBundle) {

        val source = context!!.packageName
        var ln = 1
        Log.msg(TAG, "[transferControl]")
        Log.msg(TAG, "[transferControl] +++++++++++++++++++++++++++++++++++++++++++++++++")
        if (!isOwner()) {
            Log.msg(TAG, "[transferControl] NO ES OWNER")
            return
        }
        if (!packageService.isInstalled(target)) {
            Log.msg(TAG, "[transferControl] NO esta instalada: [$target]")
            return
        }
        ln = 2
        Log.msg(TAG, "[transferControl] -1-")
        //NOTA:  ACTION_APPLICATION_DELEGATION_SCOPES_CHANGED
        // Aunque se pase el control al updater, sigue vivo el DPC, el Timer sigue funcionando.
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            Log.msg(TAG, "[transferControl] -2-")
            dpm.transferOwnership(
                ComponentName(source, "$source.DeviceAdminReceiver"),
                ComponentName(target, "$target.DeviceAdminReceiver"),
                bundle
            )
            Log.msg(TAG, "[transferControl] -3-")
            ln = 5
            Log.msg(TAG, "[transferControl]+++++++++++++++++++++++++++++++++++++++++++++++++")
            Log.msg(TAG, "[transferControl]++++++++++++[ TRANSFIRIO CONTROL ]++++++++++++++++")
            Log.msg(TAG, "[transferControl]--[$source] -----> [$target]")
            Log.msg(TAG, "[transferControl]+++++++++++++++++++++++++++++++++++++++++++++++++")
            isOwner()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "transferControl ln:[$ln]", ex.message)
        }
    }
    private fun UpdateDPC(target: String,bundle: PersistableBundle) {
        val source = context!!.packageName
        var ln = 1
        Log.msg(TAG, "[UpdateDPC]")
        Log.msg(TAG, "[UpdateDPC] +++++++++++++++++++++++++++++++++++++++++++++++++")
        ln = 2
        getDeviceAdmins()
        //NOTA:  ACTION_APPLICATION_DELEGATION_SCOPES_CHANGED
        // Aunque se pase el control al updater, sigue vivo el DPC, el Timer sigue funcionando.
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

            startApp(target)
            ln = 21
            //Version Service...
/*            val DELEGATIONS = arrayOf(
DELEGATION_PERMISSION_GRANT,
      DELEGATION_INSTALL_EXISTING_PACKAGE,
    DELEGATION_PACKAGE_ACCESS,
              DELEGATION_BLOCK_UNINSTALL,
                    DELEGATION_APP_RESTRICTIONS,


                DELEGATION_CERT_INSTALL,
                DELEGATION_ENABLE_SYSTEM_APP,
                DELEGATION_KEEP_UNINSTALLED_PACKAGES,
                DELEGATION_KEEP_UNINSTALLED_PACKAGES,
                DELEGATION_NETWORK_LOGGING,
                DELEGATION_CERT_SELECTION
            )*/
            ln = 3
            val otherScopes = Arrays.asList(
                DevicePolicyManager.DELEGATION_PERMISSION_GRANT,
                DevicePolicyManager.DELEGATION_INSTALL_EXISTING_PACKAGE,
                DevicePolicyManager.DELEGATION_PACKAGE_ACCESS,
                DevicePolicyManager.DELEGATION_BLOCK_UNINSTALL,
                DevicePolicyManager.DELEGATION_APP_RESTRICTIONS,
                DevicePolicyManager.DELEGATION_CERT_INSTALL,

                DevicePolicyManager.DELEGATION_ENABLE_SYSTEM_APP,
                DevicePolicyManager. DELEGATION_KEEP_UNINSTALLED_PACKAGES,
                DevicePolicyManager.DELEGATION_KEEP_UNINSTALLED_PACKAGES,
                DevicePolicyManager.DELEGATION_NETWORK_LOGGING,
                DevicePolicyManager.DELEGATION_CERT_SELECTION
            )
            ln = 4
            dpm.setDelegatedScopes(
                ComponentName(source, "$source.DeviceAdminReceiver"),
                target,
                otherScopes)
            Log.msg(TAG, "[UpdateDPC] Transfirio control a: [$target] via Delegate - deleyed")
            val handler = Handler()
            // handler.postDelayed(() ->  Utils.sendUpdaterStatus("instalar",lObjPersistableBundle),3000 ) ;
            ln = 5
            Log.msg(TAG, "[UpdateDPC] ++++++++++++++++++++[ ok ]+++++++++++++++++++")
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "UpdateDPC ln:[$ln]", ex.message)
        }

    }
    private fun getDeviceAdmins(){
        try{
            val intent = Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED)
            val items: MutableList<String> = ArrayList()
            val packageManager: PackageManager = context.packageManager
            val resolveInfos = packageManager.queryBroadcastReceivers(intent, 0)
            for (resolveInfo in resolveInfos) {
                val activityInfo = resolveInfo.activityInfo ?: continue
                Log.msg(TAG,"[getDeviceAdmins]  ${activityInfo.packageName}/${activityInfo.name}")
                items.add(activityInfo.packageName + "/" + activityInfo.name)
            }
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "getDeviceAdmins", ex.message)
        }
    }
    //Si es manual, debe pasar los paramtros adicionales...
    private fun getParameters(): PersistableBundle {
        Log.msg(TAG,"[getParameters]")
        val lObjPersistableBundle = PersistableBundle()
        try {
            val applicative =  Settings.getSetting(Cons.KEY_APPLICATIVE,"")
            val subsidiary = Settings.getSetting(Cons.KEY_SUBSIDIARY,"")
            val employee = Settings.getSetting(Cons.KEY_EMPLOYEE,"")

            val serverHttp =Settings.getSetting(Cons.KEY_HTTP_SERVER,Defaults.SERVIDOR_HTTP)
            val pkgServer =Settings.getSetting(Cons.KEY_HTTP_SERVER_PKG,Defaults.SERVIDOR_HTTP_PKG)
            val rptServer =Settings.getSetting(Cons.KEY_HTTP_SERVER_RPT,Defaults.SERVIDOR_HTTP_RPT)
            val serverMqtt= Settings.getSetting(Cons.KEY_MQTT_SERVER,Defaults.SERVIDOR_MQTT)
            val appkeymobile =  Settings.getSetting(Cons.KEY_APIKEYMOBILE,Defaults.API_KEY)
            val enrollSource =  Settings.getSetting(Cons.KEY_ENROLL_SOURCE,Defaults.ENROLL_SOURCE)
            val dpc_package = Settings.getSetting(Cons.KEY_PACKAGENAME_DPC,Defaults.DPC_PACKAGENAME)
            val dpc_location = Settings.getSetting(Cons.KEY_LOCATION_DPC,Defaults.DPC_LOCATION)
            val restrictioms= Settings.getSetting(Cons.KEY_RESTRICTIONS,"")
            lObjPersistableBundle.putString("server", serverHttp)
            lObjPersistableBundle.putString("server_pkg", pkgServer)
            lObjPersistableBundle.putString("server_rpt", rptServer)

            lObjPersistableBundle.putString("mqtt", serverMqtt)
            lObjPersistableBundle.putString("applicative", applicative)
            lObjPersistableBundle.putString("subsidiary", subsidiary)
            lObjPersistableBundle.putString("employee", employee)

            lObjPersistableBundle.putString("dpc_package", dpc_package)
            lObjPersistableBundle.putString("dpc_location", dpc_location)
            //lObjPersistableBundle.putString("restrictioms", restrictioms)

            lObjPersistableBundle.putString("checksum", appkeymobile)
            lObjPersistableBundle.putString("enroll_source", enrollSource)
            Log.msg(TAG,"[getParameters] lObjPersistableBundle: ${lObjPersistableBundle.toString()}")
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "setParameters", ex.message)
        }

        return lObjPersistableBundle
    }

    private fun isOwner(): Boolean {
        val mDpm: DevicePolicyManager
        val mAdminComponent: ComponentName
        mDpm = context!!.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mAdminComponent = ComponentName(context, DeviceAdminReceiver::class.java)
        val packageName = context.packageName
        Log.msg(TAG, "[isOwner] owner: " + mDpm.isDeviceOwnerApp(packageName))
        Log.msg(TAG, "[isOwner] admin:" + mDpm.isAdminActive(mAdminComponent))
        return mDpm.isDeviceOwnerApp(packageName)
    }

    private fun startApp(target: String){
        try{
        Log.msg(TAG, "[startApp] Va ejecutar: $target")
            val intent1 = Intent()
            //DevAdminService
            intent1.component = ComponentName(target, target + ".DeviceAdminService")  //DevAdminService DeviceAdminService
            context.startForegroundService(intent1)
        } catch (ex: java.lang.Exception) {
            ErrorMgr.guardar(TAG, "startApp", ex.message)
        }
    }
}