package com.macropay.data.usecases
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.macropay.data.di.Session
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.request.CargarSimDto
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.repositories.CargarSIMRepository
import com.macropay.data.repositories.PhoneRepository
import com.macropay.utils.phone.DeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
//HiltViewModel
class SendCargarSIM
@Inject constructor(
    @ApplicationContext val context: Context
): ViewModel() {
    private val TAG = "SendCargarSIM"
    @Inject
    lateinit var cargarSIMRepository: CargarSIMRepository

    @Inject
    lateinit var cabeceras:UserSessionCredentials

    val phoneModel = MutableLiveData<JSONObject>()
    fun send( phoneNumber:String,iccId1:String,iccId2:String)  {

        //Crea el dto.
        val cargarSimDto  = CargarSimDto(
            imei =  DeviceInfo.getDeviceID(), //Este es el IMEI o el id del dispositivo.?
            no_telefono= phoneNumber,
            iccid_slot_1=iccId1,
            iccid_slot_2=iccId2
        )


        val gson = Gson()
        val json = gson.toJson(cargarSimDto)
        Log.msg(TAG,"phoneNumberDto: \n"+json)
        viewModelScope.launch {
            try{
                val result : Response<ResponseBody> =  withContext(Dispatchers.Main) {
                    cargarSIMRepository.execute(cargarSimDto,cabeceras)
                }
                if (result.isSuccessful) {
                    val responseBody: String = result.body()!!.string()
                    Log.msg(TAG,"[2] isSuccessful  responseBody: "+responseBody)
                    val json = JSONObject(responseBody)
                    phoneModel.value = (json)
                    Log.msg(TAG,"[4] isSuccessful ->send --->>>>>")
                }else {
                    val jsonError = JSONObject()
                    jsonError.put("code",500)
                    jsonError.put("data", result.message())
                    phoneModel.value = (jsonError)
                    ErrorMgr.guardar(TAG, "send","["+result.code() +"]" +result.message(),json)
                }
            }catch (ex:Exception){
                ErrorMgr.guardar(TAG, "send", ex.message,json)
            }

        }
    }
}