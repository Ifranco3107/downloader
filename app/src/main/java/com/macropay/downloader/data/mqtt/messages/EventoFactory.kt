package com.macropay.downloader.data.mqtt.messages

import android.content.Context

import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


//object EventoFactory {
class EventoFactory
@Inject constructor (@ApplicationContext val context: Context) {

    var TAG = "EventoFactory"

    @Inject
    lateinit var updatePackage: UpdatePackage
    @Inject
    lateinit var commands: Commands


    //ApplyMsgMqtt
    fun getEvento(idAction: String, context: Context?): IEvento {
        Log.msg(TAG, "[getEvento] idAction: $idAction")
        var event: IEvento = EventoNulo(context!!)
        try {
         when (idAction) {

             "1004" -> event = updatePackage // Inject.inject(context!!).getUpdatePackage() // UpdatePackage(context!!)

             "9001",
             "9002" -> event = commands // event = Parametros(context!!)
             "9999" -> event = EventoNulo(context!!)
             else ->{
                 Log.msg(TAG, "[else] idAction: $idAction")
                 event = EventoNulo(context!!)
             }
         }


        } catch (ex: Exception) {
         ErrorMgr.guardar(TAG, "getEvento", ex.message)
        }

        return event
        }
}

