package com.macropay.downloader.data.mqtt.messages

import android.content.Context
import com.macropay.data.dto.request.EventMQTT

import com.macropay.downloader.utils.app.UpdateSystem
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.ui.provisioning.EnrollActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.lang.Exception
import javax.inject.Inject


class UpdatePackage
@Inject constructor(@ApplicationContext val  context: Context,
                    val updateSystem : UpdateSystem
) : IEvento  {
// class UpdatePackage(var context: Context) : IEvento {
    var TAG = "UpdatePackage"
    override  fun execute(msg: EventMQTT): Boolean = runBlocking {
        try {
            if (EnrollActivity.fa != null) EnrollActivity.fa!!.finish()

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "execute[1] ", ex.message, msg.toString())
        }

        try {
            Log.msg(TAG, "Updater data: " + msg.message.getString("data"))
            var apksJson = msg.message.getString("data").replace("\\", "")
            CoroutineScope(
                Dispatchers.Main).launch {
           // launch {
                Log.msg(TAG, "[execute] inicio  updateSystem.instalarApp")
                val success = withContext(Dispatchers.IO) {
                    updateSystem.instalarApp(apksJson)
                }
                Log.msg(TAG, "[execute] termino updateSystem.instalarApp")

            }



        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "execute", ex.message, msg.toString())
        }
        return@runBlocking true
    }
}