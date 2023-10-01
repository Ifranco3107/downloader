package com.macropay.downloader.timers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.macropay.downloader.receivers.AlarmReceiver
import com.macropay.data.logs.Log

class AlarmTimer {
    val TAG = "AlarmTimer"
    fun iniciarAlarm(context: Context) {
        Log.msg(TAG, "iniciarAlarm - Inicializa el TimerManager,.")
        // Get AlarmManager instance
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val minuto = 60_000L
        // Intent part
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = "GPS_ACTION"
        intent.putExtra("KEY_TEST_STRING", "Dato pasado al onReceive()")
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        // Alarm time - Tiempo de intervalo de ejecucion.
        val timeInterval = 5 * minuto
        val alarmTime = System.currentTimeMillis() + 5_000L
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, timeInterval, pendingIntent)
    }
}