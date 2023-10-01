package com.macropay.downloader.utils.device

import android.content.Context
import android.content.Intent
import com.macropay.downloader.di.Inject
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import kotlin.Exception

class Remote {
    var TAG = "Remote"
    fun execute(context: Context?, intent: Intent) {
        Log.msg(TAG, "execRemote: ")
        try {
            val idCommand = intent.getIntExtra("idCommand", 0)
            val param1 = intent.getStringExtra("param1")
            val param2 = intent.getStringExtra("param2")
            Log.msg(TAG, "idCommand: : $idCommand param1: $param1 param2: $param2")
         //   showToast(context, "idCommand: : $idCommand param1: $param1 param2: $param2")

            when(idCommand){
                1 -> System.exit(0)
                2 -> DeviceService.reboot(context!!)
             //   3 -> sendStatusHttp(context!!)
                else -> System.exit(0)
            }

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "execRemote", ex.message)
        }
    }
/*    fun sendStatusHttp(context: Context?){
        try{
            var  restoreRestrictions : RestoreRestrictions = Inject.inject(context!!).getRestoreRestrictions()
            restoreRestrictions.syncMsgs()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "sendStatusHttp", ex.message)
        }
    }*/
}