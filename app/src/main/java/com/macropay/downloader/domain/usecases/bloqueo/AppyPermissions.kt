package com.macropay.downloader.domain.usecases.bloqueo


import android.app.admin.DevicePolicyManager

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import android.os.Handler
import android.os.Looper



import com.macropay.downloader.domain.usecases.main.dpm
import com.macropay.downloader.domain.usecases.main.componentName
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.Utils
import com.macropay.utils.broadcast.Sender
import com.macropay.downloader.utils.location.LocationDevice
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

//Requiere los permisos necesarios para que funcione la app.
class AppyPermissions
    @Inject constructor(@ApplicationContext var context:Context) {

    // SystemUtils.autoSetAccessibilityPermission(context, Const.APUPPET_PACKAGE_NAME, Const.APUPPET_SERVICE_CLASS_NAME);
    @Inject
    lateinit var locationDevice: LocationDevice
    var handler = Handler(Looper.getMainLooper())
    var TAG = "AppyPermissions"
    private var permisosGranted = false

    // Pide permisos para Activar GPS
    fun getGPSPermmission(context: Context) {
            Log.msg(TAG, "[getGPSPermmission] Obtiene permisos GPS - activeLocation ***")
            try {
                if (Utils.SDK_INT == Build.VERSION_CODES.P) {
                    // registerBroadcast();
                    Log.msg(TAG, "[getGPSPermmission] version Android P")
                    handler.postDelayed({
                        Log.msg(TAG, "[getGPSPermmission] Build.VERSION_CODES.P: "+Utils.SDK_INT)
                        //if (locationDevice == null) locationDevice = LocationDevice(context)
                        locationDevice.createLocationRequest_P(context, TAG+".getGPSPermmission")
                    }, 10000)
                } else {
                    Log.msg(TAG, "[getGPSPermmission] Version > P.: "+Utils.SDK_INT)
                    SettingsApp.setGPSPermissionEnabled(true)
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "getGPSPermmission", ex.message)
            }
        }

        fun revokePermissions(context: Context) {


        }
    // @TargetApi(Build.VERSION_CODES.M)
    fun autoGrant(context: Context): Boolean {


        //TODO 17Dic22 Dialogs.showActivityStatus("grant[1]",context)
        val permissions = getRuntimePermissions(context.packageManager, context.packageName)
Log.msg(TAG,"[autoGrant] va aplicar: ${permissions.size} permissions")

        var count = 0

        for (permission in permissions) {
            try {
              //  Log.msg(TAG, "[autoGrant] "+count.toString()+".- "+permission.toString() )
                count++
                if (dpm().getPermissionGrantState(componentName(), context.packageName, permission) != DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED) {
                    val success = dpm().setPermissionGrantState(componentName(), context.packageName, permission, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)
                    val progress = (count * 100 / permissions.size)
                    Log.msg(TAG, "[autoGrant] "+count.toString()+".- "+permission.toString()+ " success: "+success+ " - " +progress +" %" )
                    Sender.sendStatus("Preparando: $progress %")
                }

            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "autoGrant", ex.message)
            } catch (ex: NoSuchMethodError) {
                ErrorMgr.guardar(TAG, "autoGrant", ex.message)
            }
        }

        //Termino de aplicar los permisos...
        permisosGranted = true

        Sender.sendStatus("Preparado: 100 %")
        Log.init(Cons.TEXT_DPC_LOG, context)
        Log.msg(TAG,"[autoGrant] termino...")
        return true
    }
/*        fun autoGrant(context: Context): Boolean {
           Log.msg(TAG,"autoGrant....")
            if (!MainApp.isAdmin(context)) {
                Log.msg(TAG, "No es ADMIN")
                return false
            }
            val permissions = getRuntimePermissions(context.packageManager, context.packageName)

            val dmp = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val mAdminComponent = ComponentName(context, DeviceAdminReceiver::class.java)
            var count = 0
            for (permission in permissions) {
                try {
                    count++
                    if (dmp.getPermissionGrantState( mAdminComponent, context.packageName, permission) != DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED) {
                        val success = dmp.setPermissionGrantState( mAdminComponent!!, context.packageName, permission, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)
                        Log.msg(TAG, count.toString()+".- "+permission.toString()+ " success: "+success)
                        val progress = (count * 100 / permissions.size)
                        Log.msg(TAG, count.toString()+".- "+permission.toString()+ " success: "+success+ " - " +progress +" %" )
                        Sender.sendStatus("Preparando: $progress %")
                    }

                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "autoGrant", ex.message)
                } catch (ex: NoSuchMethodError) {
                    ErrorMgr.guardar(TAG, "autoGrant", ex.message)
                }
            }
            permisosGranted = true
            return true
        }*/

        private fun getRuntimePermissions(packageManager: PackageManager, packageName: String): List<String> {
            val permissions: MutableList<String> = ArrayList()
            val packageInfo: PackageInfo?
            packageInfo = try {
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            } catch (e: PackageManager.NameNotFoundException) {
                return permissions
            }
            if (packageInfo != null && packageInfo.requestedPermissions != null) {
                for (requestedPerm in packageInfo.requestedPermissions) {
                    if (isRuntimePermission(packageManager, requestedPerm)) {
                        permissions.add(requestedPerm)
                    }
                }
            }
            Log.msg(TAG,"")
            return permissions
        }

        private fun isRuntimePermission(packageManager: PackageManager, permission: String): Boolean {
            try {
                val pInfo = packageManager.getPermissionInfo(permission, 0)
                if (pInfo != null) {
                    if (pInfo.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE
                        == PermissionInfo.PROTECTION_DANGEROUS
                    ) {
                        return true
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
            }
            return false
        }

        fun autoSetAccessibilityPermission(context: Context, str: String?, str2: String?) {
            val contentResolver = context.contentResolver
            //        Settings.Secure.putString(contentResolver, "enabled_accessibility_services", str + MqttTopic.TOPIC_LEVEL_SEPARATOR + str2);
//        Settings.Secure.putString(context.getContentResolver(), "accessibility_enabled", "1");
        }

}