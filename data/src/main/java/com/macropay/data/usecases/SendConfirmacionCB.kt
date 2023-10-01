package com.macropay.data.usecases

import android.content.Context
import com.google.gson.Gson
import com.macropay.data.di.Session
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.ResponseData
import com.macropay.data.dto.request.ConfirmaCBDto
import com.macropay.data.repositories.ConfirmaCBRepository
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.phone.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendConfirmacionCB
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val TAG = "SendConfirmacionCB"

    @Inject
    lateinit var confirmaCBRepository: ConfirmaCBRepository

    @Inject
    lateinit var cabeceras: UserSessionCredentials

    fun send(codeBar: String) {

        val confirmaCBDto = ConfirmaCBDto(
            imei = DeviceInfo.getDeviceID(),
            codigo_barras = codeBar
        )

        val gson = Gson()
        val json = gson.toJson(confirmaCBDto)
        // Log.msg(TAG,"confirmaCBDto: \n"+json)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = confirmaCBRepository.execute(confirmaCBDto, cabeceras)
                } catch (ex: Exception) {
                    val json = Gson().toJson(confirmaCBDto)
                    ErrorMgr.guardar(TAG, "send", ex.message, json.toString())
                }
            }
        } //GlobalScope

    }

}