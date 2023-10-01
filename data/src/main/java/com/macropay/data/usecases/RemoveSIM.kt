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
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.repositories.PhoneRepository
import com.macropay.data.repositories.RemoveSimRepository
import com.macropay.utils.phone.DeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
//@HiltViewModel
class RemoveSIM
@Inject constructor(
    @ApplicationContext val context: Context
): ViewModel() {
    private val TAG = "RemoveSIM"
    @Inject
    lateinit var removeSimRepository: RemoveSimRepository
    @Inject
    lateinit var cabeceras:UserSessionCredentials
    /*
    @Inject
    lateinit var session: Session
    */
 //   private var movieLiveData = MutableLiveData<PhoneNumberResponse>()
    val phoneModel = MutableLiveData<JSONObject>()
    fun send( iccID: String)  {
        val simInfo = JsonObject()
        simInfo.addProperty("imei", DeviceInfo.getDeviceID())
        simInfo.addProperty("iccid_slot_1",iccID)
        simInfo.addProperty("iccid_slot_2","")


        val gson = Gson()
        val json = gson.toJson(simInfo)
        Log.msg(TAG,"simInfo: \n"+json)
        viewModelScope.launch {
            try{
                val result : Response<ResponseBody> =  withContext(Dispatchers.Main) {
                    removeSimRepository.execute(simInfo,cabeceras)
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