package com.macropay.data.usecases

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.macropay.data.di.Session
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.ResponseData
import com.macropay.data.dto.request.LocationDto
import com.macropay.data.dto.request.MsgMQTT
import com.macropay.data.dto.request.UnrollDto
import com.macropay.data.preferences.Values
import com.macropay.data.repositories.LocationRepository
import com.macropay.data.repositories.UnrollRepository
import com.macropay.utils.Fechas
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.phone.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class SendUnroll
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val TAG = "SendUnroll"

    @Inject
    lateinit var unrollRepository: UnrollRepository

    @Inject
    lateinit var cabeceras: UserSessionCredentials


    fun test(txt: String) {
        Log.msg(TAG, "........")
        Log.msg(TAG, "test...." + txt)
        Log.msg(TAG, "........")

    }


    fun send( userId:String,  transId:String,data: JSONObject) {


                try {
                    //Log.msg(TAG, "[send] data: " + data.toString())

                    val msgMqtt: MsgMQTT = Gson().fromJson(data.toString(), MsgMQTT::class.java)
                    //Log.msg(TAG, "[send] msgMqtt: " + msgMqtt.toString())
                    msgMqtt.action = 1003
                    msgMqtt.imei = DeviceCfg.getImei(context)
                    val unrollDto = UnrollDto(
                        trans_id = transId,
                        user_id = userId,
                        imei = DeviceInfo.getDeviceID(),
                        hasImei = Values.hasImei.toString(),
                        lock_id = "1", // No se usa
                        enroll_id = "1",//No se usa
                        fec_liberacion = Fechas.getToday() //,
                        //data = msgMqtt
                    )

                    val gson = Gson()
                    val json = gson.toJson(unrollDto)
                   // Log.msg(TAG, "json: \n" + json)
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            try {
                                val response = unrollRepository.execute(unrollDto,cabeceras)
                            } catch (ex: Exception) {
                                val json = Gson().toJson(unrollDto)
                                ErrorMgr.guardar(TAG, "send", ex.message,json)
                            }
                        }
                    } //GlobalScope
                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "send", ex.message)
                }


    }





/*
    fun send( userId:String,  transId:String,data: JSONObject) {

        session.getCredenctials {
            it?.let {
                try {
                    //Log.msg(TAG, "[send] data: " + data.toString())

                    val msgMqtt: MsgMQTT = Gson().fromJson(data.toString(), MsgMQTT::class.java)
                    //Log.msg(TAG, "[send] msgMqtt: " + msgMqtt.toString())
                    msgMqtt.action = 1003
                    msgMqtt.imei = DeviceCfg.getImei(context)
                    val unrollDto = UnrollDto(
                        trans_id = transId,
                        user_id = userId,
                        imei = DeviceInfo.getDeviceID(),
                        hasImei = Values.hasImei.toString(),
                        lock_id = "1", // No se usa
                        enroll_id = "1",//No se usa
                        fec_liberacion = Fechas.getToday() //,
                        //data = msgMqtt
                    )

                    val gson = Gson()
                    val json = gson.toJson(unrollDto)
                   // Log.msg(TAG, "json: \n" + json)
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            try {
                                val response = unrollRepository.execute(unrollDto,it)
                            } catch (ex: Exception) {
                                val json = Gson().toJson(unrollDto)
                                ErrorMgr.guardar(TAG, "send", ex.message,json)
                            }
                        }
                    } //GlobalScope
                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "send", ex.message)
                }
            }
        }

    }
*/


}