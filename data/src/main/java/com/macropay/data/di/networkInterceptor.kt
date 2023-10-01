package com.macropay.data.di


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.macropay.data.R
import com.macropay.data.preferences.Values
import com.macropay.data.logs.Log
import com.macropay.utils.Settings
import com.macropay.utils.preferences.Cons
import okhttp3.*
import okhttp3.internal.http2.ConnectionShutdownException
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


val networkInterceptor = object : Interceptor {
    private val TAG = "networkInterceptor"
    fun isNetworkAvailable(): Boolean {
        var isOnline = false
        try {
            val connectivityManager = Values.context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            isOnline = (connectivityManager?.activeNetworkInfo?.isConnected == true)


        }catch (ex:Exception){

        }
        return isOnline
    }


    val isOnline: Boolean
        get() {
            if (Values.context!! == null) {

                return false
            }
            var connected = false
            try {
                var connectivityManager: ConnectivityManager? = null
                connectivityManager = Values.context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (connectivityManager == null) {

                    return false
                }
                val curNetwork = connectivityManager!!.activeNetwork
                if (curNetwork != null) {
                    val capabilities = connectivityManager!!.getNetworkCapabilities(curNetwork)
                    if (capabilities != null) {
                        // Log.msg(TAG,"-3- NET_CAPABILITY_INTERNET: "+capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
                        // Log.msg(TAG,"-3- NET_CAPABILITY_VALIDATED: "+capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
                        // If we check only for "NET_CAPABILITY_INTERNET", we get "true" if we are connected to a wifi
                        // which has no access to the internet. "NET_CAPABILITY_VALIDATED" also verifies that we
                        // are online
                        connected = (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))

                        //   Log.msg(TAG,"isOnline.capabilities: "+connected);
                    }
                }

            } catch (e: Exception) {
               // ErrorMgr.guardar(TAG, "isOnline", e.message)
            }
            return connected
        }
  /*  fun responseDefult( chain: Interceptor.Chain):Response{
        val request: Request = chain.request()
        val response: Response = chain.proceed(request)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("code", 200)
            jsonObject.put("status", "OK")
            jsonObject.put("message", "Successful")
            val contentType: MediaType? = response.body()!!.contentType()
            val body = ResponseBody.create(contentType, jsonObject.toString())
            return response.newBuilder().body(body).build()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }*/
    /*fun respon(){
      val jsonObject = JSONObject()
      try {
          jsonObject.put("code", 200)
          jsonObject.put("status", "OK")
          jsonObject.put("message", "Successful")
          val contentType: MediaType? = response.body()!!.contentType()
          val body = ResponseBody.create(contentType, jsonObject.toString())
          response.newBuilder()
              .code(410) // Whatever code
              .body(body) // Whatever body
              .protocol(Protocol.HTTP_2)
              .message("Network Error")
              .request(chain.request())
              .build()
      } catch (e: JSONException) {
          e.printStackTrace()
      }
    }*/
    @SuppressLint("SuspiciousIndentation")
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response  {

         if(!isOnline){
            Log.msg(TAG,"[intercept] ----------------")
            Log.msg(TAG,"[intercept] No hay red...")
            Log.msg(TAG,"[intercept] ----------------")
            throw IOException("[intercept] No Network Available!")
        }

        val request: Request = chain.request()
        var url : String= ""
        try {
            url = request.url().toString()
            Log.msg(TAG,"[intercept] url: "+url)
        } catch (e: SocketTimeoutException) {
            throw IOException("[networkInterceptor] No hay conexion de RED...["+url+"]  error: "+e.message  +" isNetwork; " +isNetworkAvailable() +" isOnline: "+isOnline)
        }

       // var response = chain.proceed(request)
        var response: Response? = null
/*        val requestBuilder = chain.request().newBuilder()
          .addHeader("Content-Type", "application/json")
          .addHeader("Accept", "application/json")
        var  response  :Response =  chain.proceed(requestBuilder.build())*/
        try{
            //println("[networkInterceptor] response...")
           // Log.msg(TAG,"[intercept] response...*** new")
            //response =   chain.proceed(request)

            //--->
            response = chain.proceed(request)

            val bodyString = response.body()!!.string()

           return response.newBuilder()
                .body(ResponseBody.create(response.body()?.contentType(), bodyString))
                .build()

        }catch (e: Throwable) {
            Log.msg(TAG,"[intercept] ERROR: ")
/*            if (e is IOException) {
                Log.msg(TAG,"If IOException=> "+ e.message)
                throw e
            } else {
                Log.msg(TAG,"Else IOException=> "+ e.message)
                throw IOException(e)
            }*/
            var msg = ""
            var codeErr = 999
            when (e) {
                is SocketTimeoutException -> {
                    msg = "[SocketTimeoutException] Timeout - Please check your internet connection"
                    codeErr = 990
                }
                is UnknownHostException -> {
                    msg = "[UnknownHostException] Unable to make a connection. Please check your internet"
                    codeErr = 991
                }
                is ConnectionShutdownException -> {
                    msg = "[ConnectionShutdownException] Connection shutdown. Please check your internet"
                    codeErr = 992
                }
                is IOException -> {
                    msg = "[IOException] Server is unreachable, please try again later."
                    codeErr = 993
                }
                is IllegalStateException -> {
                    msg = "[IllegalStateException] ${e.message}"
                    codeErr = 994
                }
                else -> {
                    msg = "[else] ${e.message}"
                    codeErr = 995
                }
            }
            Settings.setSetting(Cons.KEY_LAST_HTTP_ERROR,codeErr)
            Log.msg(TAG,"[intercept] ERROR $msg")
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(codeErr)
                .message(msg)
                .body(ResponseBody.create(null, "{${e}}")).build()
        }

      Log.msg(TAG,"[intercept] response...ERROR")

    //  return response!!


      //  return chain.proceed(requestBuilder.build())
    }
}

/*            .addNetworkInterceptor(Interceptor(){ chain ->
                val original = chain.request()
                // Request customization: add request headers
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "MY_API_KEY") // <-- this is the important line

                val request = requestBuilder.build()
                //----
                try {
                    val response = chain.proceed(chain.request())
                    val content = response.toString()
                    response.close()
                    //Log.d("Interceptor", "isConnectionTimedOut() => $content")
                    println("isConnectionTimedOut() => $content")
                } catch (e: SocketTimeoutException) {
                    println("Errorr => "+e.message)
                    Log.d("Interceptor", "Errorr => "+e.message)
                    //ErrorMgr.guardar("Interceptor","---",e.message)
                }

                chain.proceed(request)
            })*/