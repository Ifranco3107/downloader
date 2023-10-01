package com.macropay.downloader

import android.content.Context
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log

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
/*        try {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"uncaughtException[2]",ex.message)
        }*/


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
}