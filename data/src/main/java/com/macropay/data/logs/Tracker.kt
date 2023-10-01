package com.macropay.data.logs
import com.macropay.data.di.Inject
import com.macropay.data.logs.ErrorMgr
object Tracker {
    fun status(clase: String, process: String, msg: String?){
        try{
            val comment = "[$clase] $msg"
            Inject.inject().getSendComments().send(process,"MLapp", comment )
        }catch (ex:Exception){

        }
    }
}