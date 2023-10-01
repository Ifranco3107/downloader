package com.macropay.downloader

import android.content.Context
import com.macropay.data.preferences.Values
import com.macropay.downloader.data.preferences.MainApp
import com.macropay.downloader.data.preferences.dpcValues
import com.macropay.utils.network.Red
import com.macropay.downloader.utils.Settings
import com.macropay.downloader.utils.SettingsApp
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.utils.activities.Dialogs
import com.macropay.utils.phone.DeviceCfg
import com.macropay.downloader.utils.samsung.KnoxConfig
import com.macropay.utils.broadcast.Sender

object Auxiliares {
    private val TAG = "Auxiliares"
    fun init(context: Context) {
        var ln = 0
        try {
            ln = 1
            //Inicializar auxiliares
            Log.init("downloader", context)

            ErrorMgr.init(context)
            ln = 2
            SettingsApp.init(context)
            ln = 3
            Settings.init(context)
            ln = 4
            MainApp.setMainCtx(context)
            ln = 5
            Red.ctxStatus =context
            ln = 6
            dpcValues.mContext = context
            ln = 7
            Dialogs.mContext = context
            ln = 8
            KnoxConfig.context = context
            ln = 9

            //Valores para el modulo de data,
            Values.context = context
            ln = 10
            Values.hasImei = DeviceCfg.hasIMEI(context)
            ln = 11
            Values.imei = DeviceCfg.getImei(context).toString()
            ln = 12
            Sender.ctx = context
            ln = 13
            //
            com.macropay.utils.logs.Log.init("downloader",context)
            ln = 14
            com.macropay.utils.Settings.init(context) //TODO: se debe pasar el settings de App a Utils.
            ln = 15

           // Log.msg(TAG,"[Auxiliares] -14-")
        } catch (ex: Exception) {
            println("ERROR [Auxiliares] init ")
            println("ERROR [Auxiliares] ln $ln ")
//            ErrorMgr.guardar(TAG, "init", ex.message)
        }
    }
}