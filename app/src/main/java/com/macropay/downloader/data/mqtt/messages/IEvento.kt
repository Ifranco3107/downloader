package com.macropay.downloader.data.mqtt.messages

import com.macropay.data.dto.request.EventMQTT

interface IEvento {

   // fun execute(): Boolean
   // suspend
   fun execute(msg: EventMQTT): Boolean //=coroutineScope

}