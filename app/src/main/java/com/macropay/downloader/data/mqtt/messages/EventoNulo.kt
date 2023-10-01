package com.macropay.downloader.data.mqtt.messages

import android.content.Context
import com.macropay.data.dto.request.EventMQTT
import com.macropay.data.logs.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

//class EventoNulo(var context: Context) : IEvento {


class EventoNulo
@Inject constructor(@ApplicationContext val  context: Context)
    : IEvento  {
    var TAG = "EventoNulo"

    override  fun execute(msg: EventMQTT): Boolean {
        Log.msg(TAG, "evento nulo [execute]")
        return false
    }
}