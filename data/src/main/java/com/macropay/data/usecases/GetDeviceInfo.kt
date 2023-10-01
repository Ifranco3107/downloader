package com.macropay.data.usecases

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.macropay.data.di.UserSessionCredentials
import com.macropay.utils.Settings.getSetting
import com.macropay.data.logs.Log
import com.macropay.data.repositories.GetDeviceInfoRepository
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject

class GetSIMAutorizados
@Inject constructor(
    @ApplicationContext val context: Context) : ViewModel(){
    private val TAG = "GetSIMAutorizados"

    @Inject
    lateinit var cabeceras: UserSessionCredentials
    @Inject
    lateinit var getDeviceInfoRepository: GetDeviceInfoRepository

    suspend fun send(): Deferred<DeviceInformation> = coroutineScope {

        Log.msg(TAG, "[send] -inicio-")
        var applicative =   getSetting(Cons.KEY_APPLICATIVE,"")
        var subsidiary =   getSetting(Cons.KEY_SUBSIDIARY,"")
        var employee =   getSetting(Cons.KEY_EMPLOYEE,"")
        var enrollId = DeviceInfo.getDeviceID()
        Log.msg(TAG,"[send] enrollId: "+ enrollId)
        Log.msg(TAG,"[send] SIMs:"+DeviceCfg.getCountSIM(context))
        if(DeviceCfg.getCountSIM(context) >1){
            Log.msg(TAG,"[send] SIM 1:"+DeviceCfg.getImeiBySlot(context,0))
            Log.msg(TAG,"[send] SIM 2"+DeviceCfg.getImeiBySlot(context,1))
        }
        val phoneDto = JsonObject()
        phoneDto.addProperty("imei", DeviceInfo.getDeviceID())
        Log.msg(TAG,"enrolldto: \n"+phoneDto)

        //val credenciales = session.getCredentials()
        var result = async{getDeviceInfoRepository.execute(phoneDto,cabeceras)}

        result.await()

        result
    }






}

class DeviceInformation(val isSucces: Boolean,val code:Int ,val body:String)