package com.macropay.downloader

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.ContextCompat.getSystemService
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.receivers.AlarmReceiver
import com.macropay.downloader.ui.manual.AdminActivity
import com.macropay.utils.broadcast.Sender
import kotlin.system.exitProcess


class DefaultExceptionHandler(context: Context) : Thread.UncaughtExceptionHandler {
    val TAG = "DefaultExceptionHandler"
    var context: Context

    init {
        this.context = context
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        try{
            Log.msg(TAG,"[uncaughtException] message:  "+ex.message)
            Log.msg(TAG,"[uncaughtException] cause:  "+ex.cause)
            Log.msg(TAG,"[uncaughtException] localizedMessage: "+ex.localizedMessage)
            Log.msg(TAG,"[uncaughtException] stackTrace: "+ex.stackTraceToString())
            ErrorMgr.guardar(TAG,"uncaughtException[1]",ex.localizedMessage)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"uncaughtException[1]",ex.message)
        }
/*    */


       /* val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            ApplicationClass.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT
        )
        getSystemService(Context.ALARM_SERVICE)
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent)
        //finishing the activity.
        activity.finish();*/
        //Stopping application
        System.exit(0);

       // restrinctions.setRestriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, true)
    }

    fun restartServictestere(){
        try {
/*            Log.msg(TAG,"[restartService] -1- 0")
            val intent = Intent(context, DevAdminService::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getService(context, 0, intent, 0)
            val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent)
            Log.msg(TAG,"[restartService] -2- ")*/

            Log.msg(TAG,"[restartService] -1- ")
/*            val pendingIntent = PendingIntent.getService(
                context, 0, Intent(
                    context,
                    DevAdminService::class.java
                ), PendingIntent.FLAG_UPDATE_CURRENT
            )
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am[AlarmManager.RTC,  System.currentTimeMillis() + 1000] = pendingIntent*/

            val elapsed= System.currentTimeMillis() + 1000
/*
            val intent: Intent = Intent(context, DevAdminService::class.java)
            val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC, elapsed, pendingIntent)*/

            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(MainApp.instance!!.getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT)

            Log.msg(TAG,"[restartService] -2- -->AlarmReceiver 1122")
            val mgr = MainApp.instance!!.getBaseContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr[AlarmManager.RTC, System.currentTimeMillis() + 1000] = pendingIntent
            System.exit(2)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"restartService",ex.message)
        }
    }

    fun restartService(){

        try{
             var alarmMgr: AlarmManager? = null
             lateinit var alarmIntent: PendingIntent

            alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                intent.setPackage(context.packageName);
                intent.setAction(Sender.ACTION_DPC_STATUS);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES or Intent.FLAG_RECEIVER_FOREGROUND);
                PendingIntent.getBroadcast(context, 0, intent, 0)
            }
            Log.msg(TAG,"[restartService] -2- -->AlarmReceiver exitProcess")
            alarmMgr?.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60 * 1000,
                alarmIntent
            )
            //System.exit(2)
            exitProcess(2)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"restartService",ex.message)
        }
    }
    fun restartServicebad() {
        Log.d(TAG, "[restartService] Alarm is being scheduled")
        val intent: Intent = Intent(context, DevAdminService::class.java)
        //intent.setPackage(context.packageName);
        //intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES )
        val pintent = PendingIntent.getService(MainApp.instance!!.getBaseContext(), 0, intent, 0)
        val alarm =MainApp.instance!!.getBaseContext(). getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarm!![AlarmManager.RTC, System.currentTimeMillis() + 1000] = pintent
    Log.d(TAG, "[restartService] exitProcess RTC")
        //System.exit(2)
    exitProcess(2)
    }
    //OK -Si funciona....
    fun restartActivity() {
        try{
            val intent: Intent = Intent(context, AdminActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(MainApp.instance!!.getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT)
            Log.msg(TAG,"[restartActivity] -2- -->AdminActivity 2156")
            val mgr = MainApp.instance!!.getBaseContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr[AlarmManager.RTC, System.currentTimeMillis() + 1000] = pendingIntent
            System.exit(2)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"restartActivity",ex.message)
        }
    }


}