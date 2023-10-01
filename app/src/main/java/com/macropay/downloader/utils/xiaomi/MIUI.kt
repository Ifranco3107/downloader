package com.macropay.downloader.utils.xiaomi

import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.ErrorMgr.guardar
import android.annotation.SuppressLint
import android.os.Build
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.reflect.InvocationTargetException

object MIUI {
    var TAG = "MIUI"/*   if (miuiOptimizationEnabled == null){
                miuiOptimizationEnabled = getSystemProperty("persist.sys.miui_optimization");
               // miuiOptimizationEnabled = getSystemPropertyUsingProcess("persist.sys.miui_optimization");
            }*/

    //Esquema 2
    //      /**@return true iff we've detected that MIUI OS has MIUI optimization enabled.   //  Returns null when failed to detect anything about it*/
    //"1".equals(SystemProperties.get("ro.miui.restrict_imei_p")
    val isMiuiOptimizationEnabled: Boolean
        get() {
            var isOptedOutOfMiuiOptimization = false
            try {
                val miuiOptimizationEnabled = getSystemPropertyEx("persist.sys.miui_optimization")
                msg(TAG, "miuiOptimizationEnabled -2- : [$miuiOptimizationEnabled]")
                /*   if (miuiOptimizationEnabled == null){
              miuiOptimizationEnabled = getSystemProperty("persist.sys.miui_optimization");
             // miuiOptimizationEnabled = getSystemPropertyUsingProcess("persist.sys.miui_optimization");
          }*/if (miuiOptimizationEnabled != null) {
                    msg(TAG, "miuiOptimizationEnabled -3- : [$miuiOptimizationEnabled]")
                    return miuiOptimizationEnabled === "true"
                }
            } catch (ex: Exception) {
                guardar(TAG, "isMiuiOptimizationEnabled-1", ex.message)
            }
            //Esquema 2
            isOptedOutOfMiuiOptimization = false
            try {
                @SuppressLint("PrivateApi") val clazz = Class.forName("android.miui.AppOpsUtils")
                isOptedOutOfMiuiOptimization = clazz.getMethod("isXOptMode").invoke(null) as Boolean
                msg(TAG, "isOptedOutOfMiuiOptimization -5- $isOptedOutOfMiuiOptimization")
            } catch (e: Exception) {
                guardar(TAG, "isMiuiOptimizationEnabled-3", e.message)
            }
            return isOptedOutOfMiuiOptimization
        }//    ErrorMgr.guardar(TAG,"isMiuiOptimizationEnabled2-4",e.getMessage());

    //La pone true, porque la validacion es contraria (false es igual a true);
    //            String miuiOptimizationEnabled = getSystemProperty("persist.sys.miui_optimization");
    @get:SuppressLint("PrivateApi")
    val isMiuiOptimizationEnabled2: Boolean
        //  Log.msg(TAG,"isMiuiOptimizationEnabled2 -1- miuiOptimizationEnabled= "+miuiOptimizationEnabled);
        //Esquema 2
        // Log.msg(TAG,"isMiuiOptimizationEnabled2 -4-");
        // Log.msg(TAG,"isMiuiOptimizationEnabled2 -5- "+isOptedOutOfMiuiOptimization);
        get() {
            var isOptedOutOfMiuiOptimization = false
            try {
//            String miuiOptimizationEnabled = getSystemProperty("persist.sys.miui_optimization");
                val miuiOptimizationEnabled = getSystemProperty("persist.sys.miui_optimization", "")
                //  Log.msg(TAG,"isMiuiOptimizationEnabled2 -1- miuiOptimizationEnabled= "+miuiOptimizationEnabled);
                //Esquema 2
                var clazz: Class<*>? = null
                clazz = Class.forName("android.miui.AppOpsUtils")
                // Log.msg(TAG,"isMiuiOptimizationEnabled2 -4-");
                isOptedOutOfMiuiOptimization = clazz.getMethod("isXOptMode").invoke(null) as Boolean
                // Log.msg(TAG,"isMiuiOptimizationEnabled2 -5- "+isOptedOutOfMiuiOptimization);
            } catch (e: IllegalAccessException) {
                guardar(TAG, "isMiuiOptimizationEnabled2-1", e.message)
            } catch (e: InvocationTargetException) {
                guardar(TAG, "isMiuiOptimizationEnabled2-2", e.message)
            } catch (e: NoSuchMethodException) {
                guardar(TAG, "isMiuiOptimizationEnabled2-3", e.message)
            } catch (e: ClassNotFoundException) {
                //    ErrorMgr.guardar(TAG,"isMiuiOptimizationEnabled2-4",e.getMessage());
                isOptedOutOfMiuiOptimization = true //La pone true, porque la validacion es contraria (false es igual a true);
            }
            return !isOptedOutOfMiuiOptimization
        }
    val isMiui: Boolean
        get() {
            val versionName = getSystemProperty("ro.miui.ui.version.name")
            return versionName != ""
        }
    val version: String?
        get() {
            var version: String? = ""
            try {
                version = getSystemProperty("ro.miui.ui.version.code")
            } catch (ex: Exception) {
                guardar(TAG, "versionMIUI", ex.message)
            }
            return version
        }

    // !TextUtils.isEmpty(versionName) ? versionName : "???";
    val versionName: String?
        get() {
            var versionName: String? = ""
            try {
                versionName = getSystemProperty("ro.miui.ui.version.name")
            } catch (ex: Exception) {
                guardar(TAG, "getMiuiVersionName", ex.message)
            }
            return versionName // !TextUtils.isEmpty(versionName) ? versionName : "???";
        }

    /**
     * Getters/Setters
     */
    //GET
    fun getSystemPropertyUsingProcess(propertyName: String?): String? {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("getprop  \$propertyName")
            val bis = BufferedReader(InputStreamReader(process.inputStream))
            val result = bis.readLine()
            process.waitFor()
            return result
        } catch (e: InterruptedException) {
            guardar(TAG, "getSystemPropertyUsingProcess", e.message)
        } catch (e: IOException) {
        } finally {
            if (process != null) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) process.destroyForcibly() else process.destroy()
        }
        return null
    }

    //GETTERS
    fun getSystemProperty(propName: String): String? {
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    guardar(TAG, "getSystemProperty", e.message)
                }
            }
        }
        return line
    }

    @SuppressLint("PrivateApi")
    fun getSystemPropertyEx(key: String?): String? {
        var result = ""
        try {
            result = Class.forName("android.os.SystemProperties")
                .getDeclaredMethod("get", String::class.java)
                .invoke(null, key) as String
        } catch (ex: Exception) {
            guardar(TAG, "getMiuiVersionName", ex.message)
        }
        return result
    }

    //GET String
    fun getSystemProperty(key: String?, defaultValue: String): String {
        try {
            @SuppressLint("PrivateApi") val clz = Class.forName("android.os.SystemProperties")
            val get = clz.getMethod("get", String::class.java, String::class.java)
            return get.invoke(clz, key, defaultValue) as String
        } catch (e: Exception) {
            guardar(TAG, "getSystemProperty-String", e.message)
        }
        return defaultValue
    }

    //GET Integer
    fun getSystemProperty(key: String?, defaultValue: Int): Int {
        try {
            val mClassType = Class.forName("android.os.SystemProperties")
            val mGetIntMethod = mClassType.getDeclaredMethod("getInt", String::class.java, Int::class.javaPrimitiveType)
            mGetIntMethod.isAccessible = true
            // return (Integer) mGetIntMethod.invoke(null, "ro.build.version.sdk", 14);
            return mGetIntMethod.invoke(null, key, defaultValue) as Int
        } catch (e: ClassNotFoundException) {
            guardar(TAG, "getSystemProperty-Int", e.message)
        } catch (e: NoSuchMethodException) {
            guardar(TAG, "getSystemProperty-Int", e.message)
        } catch (e: IllegalArgumentException) {
            guardar(TAG, "getSystemProperty-Int", e.message)
        } catch (e: IllegalAccessException) {
            guardar(TAG, "getSystemProperty-Int", e.message)
        } catch (e: InvocationTargetException) {
            guardar(TAG, "getSystemProperty-Int", e.message)
        }
        return defaultValue
    }

    //GET Boolean
    fun getSystemProperty(key: String?, defaultValue: Boolean): Boolean {
        try {
            val clz = Class.forName("android.os.SystemProperties")
            val get = clz.getMethod("get", String::class.java, Boolean::class.java)
            return get.invoke(clz, key, defaultValue) as Boolean
        } catch (e: Exception) {
            guardar(TAG, "getSystemProperty-Boolean", e.message)
        }
        return defaultValue
    }

    //============= SETTERS ======================
    //SET String
    fun setSystemProperty(key: String?, newValue: String?): Boolean {
        var bResult = false
        try {
            val clz = Class.forName("android.os.SystemProperties")
            val set = clz.getMethod("set", String::class.java, String::class.java)
            set.invoke(clz, key, newValue)
            bResult = true
        } catch (e: Exception) {
            guardar(TAG, "setSystemProperty-String", e.message)
        }
        return bResult
    }

    //SET String   PropertyPermission
    fun setSystemProperty(key: String?, newValue: Boolean?): Boolean {
        var bResult = false
        try {
            val clz = Class.forName("android.os.SystemProperties")
            val set = clz.getMethod("set", String::class.java, Boolean::class.java)
            set.invoke(clz, key, newValue)
            bResult = true
        } catch (e: Exception) {
            guardar(TAG, "setSystemProperty-Boolean", e.message)
        }
        return bResult
    }

    fun setSystemProperty(key: String?, newValue: Int): Boolean {
        var bResult = false
        try {
            val clz = Class.forName("android.os.SystemProperties")
            val set = clz.getMethod("set", String::class.java, Int::class.javaPrimitiveType)
            set.invoke(clz, key, newValue)
            bResult = true
        } catch (e: Exception) {
            guardar(TAG, "setSystemProperty-Int", e.message)
        }
        return bResult
    } /*


    public static String getMiuiVersionName() {
        String versionName = Utils.getSystemProperty("ro.miui.ui.version.name");
        return !TextUtils.isEmpty(versionName) ? versionName : "???";
    }

    public static int getMiuiVersionCode() {
        try {
            return Integer.parseInt(Objects.requireNonNull(Utils.getSystemProperty("ro.miui.ui.version.code")));
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getActualMiuiVersion() {
        return Build.VERSION.INCREMENTAL;
    }

    private static int[] parseVersionIntoParts(String version) {
        try {
            String[] versionParts = version.split("\\.");
            int[] intVersionParts = new int[versionParts.length];

            for (int i = 0; i < versionParts.length; i++)
                intVersionParts[i] = Integer.parseInt(versionParts[i]);

            return intVersionParts;
        } catch (Exception e) {
            return new int[]{-1};
        }
    }

*/
    /**
     * @return 0 if versions are equal, values less than 0 if ver1 is lower than ver2, value more than 0 if ver1 is higher than ver2
     */
    /*
    private static int compareVersions(String version1, String version2) {
        if (version1.equals(version2))
            return 0;

        int[] version1Parts = parseVersionIntoParts(version1);
        int[] version2Parts = parseVersionIntoParts(version2);

        for (int i = 0; i < version2Parts.length; i++) {
            if (i >= version1Parts.length)
                return -1;

            if (version1Parts[i] < version2Parts[i])
                return -1;

            if (version1Parts[i] > version2Parts[i])
                return 1;
        }

        return 1;
    }

    public static boolean isActualMiuiVersionAtLeast(String targetVer) {
        return compareVersions(getActualMiuiVersion(), targetVer) >= 0;
    }

    @SuppressLint("PrivateApi")
    public static boolean isMiuiOptimizationDisabled() {
        if ("0".equals(Utils.getSystemProperty("persist.sys.miui_optimization")))
            return true;

        try {
            return (boolean) Class.forName("android.miui.AppOpsUtils")
                    .getDeclaredMethod("isXOptMode")
                    .invoke(null);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isFixedMiui() {
        return isActualMiuiVersionAtLeast("20.2.20") || isMiuiOptimizationDisabled();
    }

*/
}