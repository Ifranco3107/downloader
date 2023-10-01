package com.macropay.data.server
import com.google.gson.JsonObject
import com.macropay.data.dto.request.*
import com.macropay.data.dto.response.*
import com.macropay.data.dto.response.enroll.EnrollResponse
import com.macropay.data.usecases.EnrollFailed
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*


//Todos en endPoints de Device
interface DeviceAPI {
//  https://roco9jla6l.execute-api.us-east-1.amazonaws.com/api/devices/enroll/Device
////https://roco9jla6l.execute-api.us-east-1.amazonaws.com/api/devices/enroll/Device
 //        @Header("Authorization") authHeader: String?,

    //suspend

    //Enrolamiento
   // @POST("api/devices/enroll/Device")
    @POST
    suspend  fun enroll(@Url url:String,@Body enrollDto: EnrollDto,
                        @HeaderMap headers: Map<String, String>?,
            ): Response<EnrollResponse>

    //Respuesta de confirmacion, de que se aplico un Bloqueo.
   // @PUT("api/devices/update/LockStatus")
    @Headers("Content-Type: application/json")
    @PUT
    suspend  fun updateLockStatus(@Url url:String,@Body updateLockStatusDto: UpdateLockStatusDto,
                                  @HeaderMap headers: Map<String, String>,
    ): Response<MessageResponse>

    //Desenrolamiento del dispositivo
    //@POST("api/device/mobile/unrollDevice")
    @POST
    suspend  fun unroll(@Url url:String,@Body unrollDto: UnrollDto,
                        @HeaderMap headers: Map<String, String>,
    ): Response<UnrollResponse>


    //Desenrolamiento del dispositivo
    //@PUT("api/devices/send/DeviceLocation")
    @PUT
    suspend  fun locationDevice(@Url url:String,@Body locationDto: LocationDto,
                                @HeaderMap headers: Map<String, String>,
    ): Response<LocationResponse>


    //@PUT("/api/locks/mobile/sync/report")
    @PUT
    suspend  fun reportStatus(@Url url:String,@Body reportStatusDto: ReportStatusDto,
                              @HeaderMap headers: Map<String, String>,
    ): Response<ReportStatusResponse>
    @Headers("Content-Type: application/json")
    @PUT
    suspend  fun reportStatusHttp(@Url url:String,@Body reportStatusDto: ReportStatusDto,
                                  @HeaderMap headers: Map<String, String>?,
    ): Response<SyncReportResponse>
    //SyncReportResponseMqtt

    //Desenrolamiento del dispositivo
    //@POST("api/devices/update/packageVersion")
    @POST
    suspend  fun packageVersion(@Url url:String,@Body packageVersionDto: PackageVersionDto,
                                @HeaderMap headers: Map<String, String>,
    ): Response<PackageVersionResponse>



    //Envia el numero telefonico del nuevo SIM,
    //Este endPoint envia un SMS a dicho Numero telefonico.
  //
    //@POST("api/locks/confirm/new/number")
    @POST
    suspend  fun sendPhoneNumber(@Url url:String, @Body phoneNumberDto: JsonObject,
                                 @HeaderMap headers: Map<String, String>,
    ): Response<ResponseBody>



    //Confirma que recibio el SMS correcto, y envia los datos del SIM
  //  @POST("/api/locks/confirm/new/number")
    //@POST("/api/locks/mobile/confirmarcodigo")
    @POST
    suspend  fun sendSIM(@Url url:String,@Body SimDto: SIMDto,
                         @HeaderMap headers: Map<String, String>,
    ): Response<SIMResponse>


    @PUT
    suspend  fun sendConfirmacionCB(@Url url:String,@Body confirmaCBDto: ConfirmaCBDto,
                                    @HeaderMap headers: Map<String, String>,
    ): Response<ConfirmaCBResponse>


    @GET
    suspend fun getCerts(@Url url:String,
                          @HeaderMap headers: Map<String, String>): Response<CertsEncResponse>
  //  @FormUrlEncoded
    @Multipart
    @POST
    suspend fun updateLogs(@Url url:String,
                           @Part ("imei") imei: RequestBody,
                           @Part log_file     : MultipartBody.Part,
                           @Header("appkeymobile") appKey: String?): Response<LogResponse>
// @Part ("log_file\"; filename=\"logFile.log\" ")  log_file :RequestBody,
// @Part ("log_file") log_file:RequestBody,

    @POST
    suspend  fun sendError(@Url url:String,@Body errorDto: ErrorDto,
                                 @HeaderMap headers: Map<String, String>,
    ): Response<ResponseBody>
    @POST
    suspend  fun sendEnrollFailed(@Url url:String,@Body enrollFailedDto: EnrollFailedDto,
                         @HeaderMap headers: Map<String, String>,
    ): Response<EnrollFailedResponse>
    @POST
    suspend  fun sendCargarSim(@Url url:String, @Body cargarSimDto: CargarSimDto,
                                 @HeaderMap headers: Map<String, String>,
    ): Response<ResponseBody>

    @POST
    suspend  fun getDeviceInfo(@Url url:String,@Body deviceId: JsonObject,
                        @HeaderMap headers: Map<String, String>?,
    ): Response<DeviceInfo>

    @POST
    suspend  fun sendRemoveSim(@Url url:String, @Body simInfo: JsonObject,
                                 @HeaderMap headers: Map<String, String>,
    ): Response<ResponseBody>

    @POST
    suspend  fun updateAppStatus(@Url url:String, @Body simInfo: JsonObject,
                               @HeaderMap headers: Map<String, String>,
    ): Response<ResponseBody>

    @POST
    suspend  fun sendComments(@Url url:String, @Body CommentsDto: JsonObject,
                                 @HeaderMap headers: Map<String, String>,
    ): Response<ResponseBody>

}



