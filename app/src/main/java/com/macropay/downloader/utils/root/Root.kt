package com.macropay.downloader.utils.root

import android.Manifest
import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.Log.d
import com.macropay.data.logs.Log.i
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception

class Root {
    var TAG = "Root"
    fun checkAdb(context: Context) {
        //if ADB disabled
        msg(TAG, "checkAdb -1-")
        //if(!adbEnabled(context))
        run {

            //open developer options settings
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            context.startActivity(intent)
        }
        msg(TAG, "checkAdb -2-")
    }

    /*
    public void req(){

        Process p;
        try {

            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = getRootSession(p);
            os.writeBytes("any command you want");
            os.flush();
        } catch (IOException e1) {
            Log.msg("root", "Error: " + e1.getMessage());
        }

    }
*/
    fun checkRoot() {
        msg(TAG, "checkRoot -1-")
        //String TAG = "checkBoot()";
        rootCommand("com.macropay.downloader")
        msg(TAG, "checkRoot -2-")
        val p: Process
        try {
            p = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(p.outputStream)
            os.writeBytes("ls /data\n")
            os.writeBytes("exit\n")
            os.flush()
            try {
                p.waitFor()
                if (p.exitValue() != 1) {
                    //    nextScreen();
                    msg(TAG, "success getting root")
                } else {
                    //  TextView tv = (TextView) findViewById(R.id.actionMsg);
                    //  tv.setText(R.string.root_error);
                    msg(TAG, "failing getting root")
                }
            } catch (e: InterruptedException) {
                msg(TAG, "failing getting root")
            }
        } catch (e: IOException) {
            msg(TAG, "failing getting root")
        }
    }

    fun logPermissionLevel(context: Context) {
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = context.packageManager.getPackageInfo("android", PackageManager.GET_PERMISSIONS)
            if (packageInfo.permissions != null) {
                // For each defined permission
                for (permission in packageInfo.permissions) {
                    // Dump permission info
                    if (permission.name.contains("CHANGE_COMPONENT_ENABLED_STATE")) {
                        var protectionLevel: String
                        protectionLevel = when (permission.protectionLevel) {
                            PermissionInfo.PROTECTION_NORMAL -> "normal"
                            PermissionInfo.PROTECTION_DANGEROUS -> "dangerous"
                            PermissionInfo.PROTECTION_SIGNATURE -> "signature"
                            PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM -> "signatureOrSystem"
                            PermissionInfo.PROTECTION_FLAG_APPOP -> "FLAG_APPOP"
                            PermissionInfo.PROTECTION_FLAG_DEVELOPMENT -> "PROTECTION_FLAG_DEVELOPMENT"
                            PermissionInfo.PROTECTION_FLAG_SYSTEM -> "FLAG_SYSTEM"
                            PermissionInfo.PROTECTION_MASK_BASE -> "MASK_BASE"
                            PermissionInfo.PROTECTION_MASK_FLAGS -> "MASK_FLAGS"
                            else -> "<unknown> " + permission.protectionLevel
                        }
                        d("myLogs PermissionCheck", permission.name + " " + protectionLevel)
                    }
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            msg("logPermissionLevel", e.toString())
            e.printStackTrace()
        }
    }

    fun USBDebug(context: Context) {
        msg("USBDebug", "Habilitar")
        if (context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED) msg("USBDebug", "requestPermissions OK")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            msg("USBDebug", "requestPermissions")
            ActivityCompat.requestPermissions(
                (context as Activity), arrayOf(Manifest.permission.WRITE_SECURE_SETTINGS),
                0
            )
        }

//Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED

//Settings.Global.ADB_ENABLED = true;
        msg("USBDebug", "Habilitar")
        try {
            Settings.Secure.putInt(context.contentResolver, Settings.Global.ADB_ENABLED, 1)
        } catch (e: Exception) {
            msg("USBDebug", "Error:" + e.message)
        }

//        context.getContentResolver().acquireContentProviderClient()
//        Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.ADB_ENABLED,1);
//        Settings.Secure
    }

    /*
    private void initListener() {
        drawSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if(checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{Manifest.permission.WRITE_SECURE_SETTINGS},
                            000);
                }

            }
        });

        anmiationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //DEFAULT : 0
                //0 : original, show animation
                //1 : hide animation
                int result = isChecked ? 1 : 0;
                Settings.Global.putInt(getContentResolver(), Settings.Global.ANIMATION_SETTING, result);
                Log.i(TAG, "---->>anmiationSwitch status changed, now is : " + Settings.Global.getString(getContentResolver(), Settings.Global.ANIMATION_SETTING));
            }
        });

    }
*/
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?, grantResults: IntArray
    ) {
        when (requestCode) {
            0 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                i(TAG, "---->>Permission Granted")
            } else {
                // Permission Denied
                i(TAG, "---->>Permission Denied ：" + grantResults[0])
            }
            else -> {}
        }
    }

    val isRoot: Boolean
        get() {
            var root = false
            try {
                root = if (!File("/system/bin/su").exists()
                    && !File("/system/xbin/su").exists()
                ) {
                    false
                } else {
                    true
                }
            } catch (e: Exception) {
            }
            return root
        }

    companion object {
        val API = Build.VERSION.SDK_INT
        const val ENABLED = 1
        const val DISABLED = 0
        fun adbEnabled(context: Context): Boolean {
            return if (API > 16) ENABLED == Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                DISABLED
            ) else ENABLED == Settings.Secure.getInt(context.contentResolver, Settings.Secure.ADB_ENABLED, DISABLED)
        }

        fun rootCommand(packageName: String): Boolean {
            var process: Process? = null
            var os: DataOutputStream? = null
            try {
                process = Runtime.getRuntime().exec("su")
                os = DataOutputStream(process.outputStream)
                os.writeBytes("pm grant $packageName android.permission.WRITE_SECURE_SETTINGS \n")
                os.writeBytes("exit\n")
                os.flush()
            } catch (e: Exception) {
                i("*** DEBUG ***", "Error:  " + e.message)
                return false
            } finally {
                try {
                    os?.close()
                    process!!.destroy()
                } catch (e: Exception) {
                }
            }
            i("*** DEBUG ***", "---->>Root SUCccess ")
            return true
        }
    }
}