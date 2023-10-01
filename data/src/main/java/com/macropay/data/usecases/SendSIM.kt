package com.macropay.data.usecases

import android.content.Context
import com.google.gson.Gson
import com.macropay.data.di.Session
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.request.SIMDto
import com.macropay.data.repositories.SimRepository
import com.macropay.utils.Fechas
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.phone.DeviceCfg
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendSIM
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val TAG = "SendSIM"

    @Inject
    lateinit var simRepository: SimRepository

    /*
    @Inject
    lateinit var session: Session
*/


    @Inject
    lateinit var cabeceras: UserSessionCredentials

    fun send(token:String,
             androidId:String,
             imei:String,
             carrierId:String,
             carrierName:String,
             countryCode:String,
             displayText:String,
             mcc:Int,
             mnc:Int,
             no_telefono:String,
             iccID:String,
             subcriberID:String,
             imei_slot:String,
             slot:Int,
             confirmar:Boolean
    ) {
Log.msg(TAG,"[send] iccID: "+iccID)
                //Envia dto para confirmar la validacion de SMS
                val iccId1 = if(slot<=0) iccID else ""
                val iccId2 = if(slot==1) iccID else ""
//Para ver3, tra adcional el campo SERIE.
                val simDto = SIMDto(
                    codigo= token,
                    dispositivo_so_id=androidId,
                    ext_telefono ="+52",
                    iccid_slot_1=iccId1,
                    iccid_slot_2=iccId2,
                    imei =imei,
                    imei_slot = imei_slot,
                    no_telefono=no_telefono,
                    carrier_id=carrierId,
                    carrier_name=carrierName,
                    country_code=countryCode,
                    display_text=displayText,
                    mcc=mcc.toString(),
                    mnc=mnc.toString(),
                    subscriber_id=subcriberID,
                    confirmar=confirmar
                )

                val gson = Gson()
                val json = gson.toJson(simDto)
                Log.msg(TAG,"SimDto: "+json)
                GlobalScope.launch {
                    withContext(Dispatchers.IO){
                        try{
                            val response = simRepository.execute(simDto,cabeceras)
                            if(response.isSuccessful){
                                Log.msg(TAG, "[send]" +response.code() + " "+response.message())
                            }else
                            {

                                ErrorMgr.guardar(TAG, "send", response.message().toString(),json)
                            }
                        }catch (ex:Exception){

                            ErrorMgr.guardar(TAG, "send", ex.message,json)
                        }
                    }

        }
        //GlobalScope
    }
}