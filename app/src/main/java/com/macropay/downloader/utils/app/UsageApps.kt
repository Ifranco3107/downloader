package com.macropay.downloader.utils.app

import com.macropay.downloader.utils.Settings.getSetting
import androidx.appcompat.app.AppCompatActivity
import android.app.usage.UsageStatsManager
import androidx.annotation.RequiresApi
import android.os.Build
import com.macropay.data.dto.request.PackageFile
import android.app.usage.UsageStats
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import com.macropay.downloader.ui.provisioning.Permissions
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

//Attach
class UsageApps(context: Context) : AppCompatActivity() {
    var TAG = "UsageApps"
    private var context: Context? = null
    var mUsageStatsManager: UsageStatsManager? = null
    @Inject
    lateinit var packageService : PackageService

    init {
        try {
            this.context = context
            //   catj
/*            launchSomeActivity = registerForActivityResult(
                            new ActivityResultContracts.StartActivityForResult(),
                            new ActivityResultCallback<ActivityResult>() {
                                @Override
                                public void onActivityResult(ActivityResult result) {
                                    Log.msg(TAG, "resultCode: "+result.getResultCode()   +" Granted: " +isGranted());
                                    if (result.getResultCode() == Activity.RESULT_OK) {
                                        // refreshFriends();
                                    }

                                    Log.msg(TAG,"** onActivityResult "+result);

                                }
                            });*/
            mUsageStatsManager = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager //Context.USAGE_STATS_SERVICE
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "UsageApps", ex.message)
        }
    }
    internal enum class StatsUsageInterval(private val mStringRepresentation: String, val mInterval: Int) {
        DAILY("Daily", UsageStatsManager.INTERVAL_DAILY), WEEKLY("Weekly", UsageStatsManager.INTERVAL_WEEKLY), MONTHLY("Monthly", UsageStatsManager.INTERVAL_MONTHLY), YEARLY(
            "Yearly",
            UsageStatsManager.INTERVAL_YEARLY
        );

        companion object {
            fun getValue(stringRepresentation: String): StatsUsageInterval? {
                for (statsUsageInterval in values()) {
                    if (statsUsageInterval.mStringRepresentation == stringRepresentation) {
                        return statsUsageInterval
                    }
                }
                return null
            }
        }
    }

    private val launchSomeActivity: ActivityResultLauncher<Intent>? = null
    //getTotalTimeVisible  //getTotalTimeForegroundServiceUsed
    //Si tiene tiempo de uso y es app de usuario y no es app de negocio o esta app.

    /*                Log.msg(TAG, temp + "|" + packageService.isUserApp(usageStats.getPackageName()) + "|" + packageService.applicationName(usageStats.getPackageName()) + "|" + usageStats.getPackageName() +
                             "| " + strFirstTimeUsed +
                             "| " + strLastTimeUsed +
                             "| " + usageStats.getTotalTimeInForeground() / 1000 +
                             "| " + usageStats.getTotalTimeForegroundServiceUsed() / 1000 +
                             "| " + usageStats.getTotalTimeVisible() / 1000
                     );*/
//.getValue(strings[position]);
    //version   28 = Android 9 P
    //          29 = 10 Q
    //          30 = 11 R
    //          31 = 12 S
    //Q= Android 10 -
    @get:RequiresApi(api = Build.VERSION_CODES.Q)
    val usageApps: List<PackageFile>
        get() {
            Log.msg(TAG, "=======[ getUsageApps ]==================== ")
            val packages: MutableList<PackageFile> = ArrayList()
            var enterpriseApps = arrayOf<String?>()
            try {
                enterpriseApps = getSetting("enterpriseApps", enterpriseApps)
              //  val packageService = PackageService(context!!)
                val statsUsageInterval = StatsUsageInterval.MONTHLY //.getValue(strings[position]);
                val mDateFormat: DateFormat = SimpleDateFormat()
                val usageStatsList = getUsageStatistics(statsUsageInterval.mInterval)
                for (usageStats in usageStatsList) {
                    val lastTimeUsed = usageStats.lastTimeUsed
                    val strLastTimeUsed = mDateFormat.format(Date(lastTimeUsed))
                    val firstTimeUsed = usageStats.firstTimeStamp
                    val strFirstTimeUsed = mDateFormat.format(Date(firstTimeUsed))
                    val packageName = usageStats.packageName
                    //getTotalTimeVisible  //getTotalTimeForegroundServiceUsed
                    //Si tiene tiempo de uso y es app de usuario y no es app de negocio o esta app.
                    if (usageStats.totalTimeInForeground > 0 && packageService.isUserApp(packageName)
                        && !packageService.isAppEmpresarial(packageName)
                        && packageName != context!!.packageName
                    ) {
                        packages.add(PackageFile(packageName, packageService.applicationName(packageName), usageStats.totalTimeVisible / 1000))
                    }

/*                Log.msg(TAG, temp + "|" + packageService.isUserApp(usageStats.getPackageName()) + "|" + packageService.applicationName(usageStats.getPackageName()) + "|" + usageStats.getPackageName() +
                        "| " + strFirstTimeUsed +
                        "| " + strLastTimeUsed +
                        "| " + usageStats.getTotalTimeInForeground() / 1000 +
                        "| " + usageStats.getTotalTimeForegroundServiceUsed() / 1000 +
                        "| " + usageStats.getTotalTimeVisible() / 1000
                );*/
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "getUsageApps", ex.message)
            }
            return packages
        }

    fun getUsageStatistics(intervalType: Int): List<UsageStats> {
        // Get the app statistics since one year ago from the current time.
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)
        val queryUsageStats = mUsageStatsManager!!.queryUsageStats(intervalType, cal.timeInMillis, System.currentTimeMillis())
        Log.msg(TAG, "queryUsageStats.size(): " + queryUsageStats.size)
        if (queryUsageStats.size == 0) {
            Log.i(TAG, "No tiene permisos para leer la estadisticas ")
        }
        return queryUsageStats
    }

    fun requierePermisos(v: View?) {
        try {
            val intentMain = Intent(context, Permissions::class.java)
            Log.msg(TAG, "requierePermisos -1- ")
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(intentMain)
            Log.msg(TAG, "requierePermisos -2- ")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "requierePermisos", ex.message)
        }

/*        try{
            String packageName =  v.getContext().getPackageName();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setData(Uri.fromParts("package", packageName, null));
          //  startActivityForResult(intent, 2);
            launchSomeActivity.launch(intent);
            //  startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }catch (Exception ex){
            ErrorMgr.guardar(TAG,"requierePermisos",ex.getMessage());
        }*/
    }

    /*    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.msg(TAG,"requestCode: "+requestCode + " resultCode:"+resultCode  +" Granted: " +isGranted());

    }*/
    val isGranted: Boolean
        get() {
            val packageName = context!!.packageName
            val appOps = context!!.getSystemService(APP_OPS_SERVICE) as AppOpsManager
            var mode = 0
            mode = appOps.checkOpNoThrow("android:get_usage_stats", Process.myUid(), packageName)
            val granted = mode == AppOpsManager.MODE_ALLOWED
            Log.msg(TAG, "packageName: [$packageName] granted: $granted")
            return granted
        }


}