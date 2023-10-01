package com.macropay.data.repositories
import android.content.Context
import android.webkit.MimeTypeMap
import com.macropay.data.BuildConfig
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.response.LogResponse
import com.macropay.data.preferences.Defaults
import com.macropay.data.server.DeviceAPI
import com.macropay.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import java.io.File
import javax.inject.Inject


class LogsRepository
@Inject constructor(@ApplicationContext context: Context,
                    private val api: DeviceAPI
): DataResponse(){

    val TAG = "LogsRepository"
    val ctx :Context
    init {
      //  Log.msg(TAG,"init..")
        this.ctx = context
    }
   /* fun createPartFromString(stringData: String): RequestBody {
        val fileName = stringData.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        return stringData.toRequestBody("text/plain".toMediaTypeOrNull())
    }*/
    //
   @Multipart
    suspend fun execute(imei:String,file: File): Response<LogResponse> {
        val apiKeyMobile = Settings.getSetting(Cons.KEY_APIKEYMOBILE, Defaults.API_KEY)
        val url = UrlServer.getHttp() +BuildConfig.lo06 // "/api/logDevice"
        //IMEI
        val rbImei: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imei)

       //Archivo a subir
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        var imagenPerfil = MultipartBody.Part.createFormData("log_file", file.name, requestFile)

/*        Log.msg(TAG,"[execute] leng "+ requestFile.contentLength())
        Log.msg(TAG,"[execute] type "+ requestFile.contentType())
        Log.msg(TAG,"[execute] imei: ["+imei + "] file.name: [" + file.name+"]")
        Log.msg(TAG,"[execute] file: "+file.absolutePath)
        Log.msg(TAG,"[execute] ***  123 *** ")*/

        //Envia a Central...
        val response = api.updateLogs(url,rbImei,imagenPerfil ,apiKeyMobile)
       // Log.msg(TAG,"[execute]  --------------------------------------------------< 17Mar2023 >")

        if(response.isSuccessful){
            onSuccess(response.code(),response.message())
        }else{
            Log.msg(TAG,"[execute] isFailure: "+ response.code() +" error: "+ response.message().toString())
        }

       // Log.msg(TAG,"[execute] -termino-")
        return response
    }

    fun getPart(file:File) : MultipartBody.Part? {
        var multipart : MultipartBody.Part? = null
        try{
            var mimeType = getMimeType(file)
            var requestBody :RequestBody= RequestBody.create(MediaType.parse("multipart/form-data"), file)
            multipart  = MultipartBody.Part.createFormData("uploaded_file", file.name, requestBody)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"getPart",ex.message)
        }
        return multipart
    }
/*    fun getImageRequestBody(sourceFile: File) : RequestBody? {
        var requestBody: RequestBody? = null
        Thread {
            val mimeType = getMimeType(sourceFile);
            if (mimeType == null) {
                Log.e("file error", "Not able to get mime type")
                return@Thread
            }
            try {
                requestBody = sourceFile.path..toRequestBody("multipart/form-data".toMediaTypeOrNull())
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e("File upload", "failed")
            }
        }.start()

        return requestBody;
    }*/
    //
    private fun getMimeType(file: File): String? {
        var type: String? = null
        try{
            val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"getMimeType",ex.message)
        }
        return type
    }
}

