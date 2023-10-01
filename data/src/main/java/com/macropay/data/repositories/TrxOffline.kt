package com.macropay.data.repositories

import android.os.Environment
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.macropay.data.di.Inject
import com.macropay.data.dto.request.ErrorDto
import com.macropay.data.dto.request.LocationDto
import com.macropay.data.dto.request.UpdateLockStatusDto
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.data.preferences.Values
import com.macropay.utils.FileMgr.eliminarArchivo
import com.macropay.utils.FileMgr.loadFile
import com.macropay.utils.Settings
import com.macropay.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object TrxOffline {
    private val TAG = "TrxOffline"
    private var bProcessing = false
    fun guardarTrx(trxName: String, content: String) {
        Log.msg(TAG, "[guardarTrx] Transaccion trxName: $trxName")

        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("hh:mm:ss")
        var fileLog = trxName + "_" + formatter.format(date) + ".trx"
        fileLog = fileLog.replace(":", "")
        // Log.msg(TAG, "[guardarTrx] GENERO Transaccion Log: $fileLog")
        // Log.msg(TAG, "[guardarTrx] JSON \n$content")
        //Define el nombre del archivo
        val storageDir: File = File(Values.context!!.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileLog)
        fileLog = storageDir.absolutePath

        //Graba el contenido
        val logFile = File(fileLog)
        try {
            if (logFile.exists())
                logFile.delete()

            logFile.createNewFile()
            val buf = BufferedWriter(FileWriter(logFile, false))
            buf.append(content)
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            ErrorMgr.guardar(TAG, "TrxOffline", e.message)
        }
    } //fin guardadrTrx

    fun guardarDtoTrx(transId:String,body :Any){
        try {
            val gson = Gson()
            var dataBody = gson.toJson(body)
            guardarTrx(transId, dataBody)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"guardarDtoTrx",ex.message)
        }
    }

    //Envia los Status pendientes por enviar...
    fun enviaStatusPendientes() {
        //  if (!ConnectionStatus.isOnline()) return
        if(!isEnrolled()) return
        Log.msg(TAG,"[enviaStatusPendientes] bProcessing $bProcessing")
        if(bProcessing)
            return
        bProcessing = true

        try {
            val files = getFilesTrx("STS",false) ?: return
            Log.msg(TAG, "[enviaStatusPendientes] "+files.size + " files por pendientes...");
            for (fileTrx in files) {
                procesaTrx(fileTrx)
               Utils. espera("proceso: ${fileTrx.name}",500)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "enviaStatusPendientes", ex.message)
        }
        bProcessing = false
    }

    //Envia todas las transacciones pendientes por transmitir.
    fun enviaTxtPendientes() {
        Log.msg(TAG,"[enviaTxtPendientes] bProcessing $bProcessing")
        if(bProcessing)
            return

       //  if (!ConnectionStatus.isOnline()) return
        if(!isEnrolled()) return
        bProcessing = true
        try {
            val files = getFilesTrx(".trx",true) ?: return
            // Log.msg(TAG, files.length + " files por pendientes...");
            for (fileTrx in files) {
                CoroutineScope(Dispatchers.Main)
                    .launch {
                        procesaTrx(fileTrx)
                    }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "enviaTxtPendientes", ex.message)
        }
        bProcessing = false
    }

    fun isEnrolled():Boolean{
        var bResult = false
        try{
            val KEY_STATUS = "statusEnrolamiento"
            val nivelName = Settings.getSetting(KEY_STATUS,"SinInstalar")

            bResult=  nivelName.equals("TerminoEnrolamiento")
          //  Log.msg(TAG,"nivelName: [$nivelName] bResult: $bResult")
        } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "isEnrolled", ex.message)
        }
        return bResult
    }


   private fun getFilesTrx(wildcard:String,ends:Boolean): Array<File>? {
        var files: Array<File>? = null
        try {
          //  Log.msg(TAG,"[getFilesTrx]wildcard: [$wildcard] ends: [$ends] ")
            val storageDir: File
            storageDir = File(Values.context!!.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString())
            //Buscar los archivos *.trx
            if(ends)
                files = storageDir.listFiles { d: File?, nameFile: String -> nameFile.endsWith(wildcard) }
            else
                files = storageDir.listFiles { d: File?, nameFile: String -> nameFile.startsWith(wildcard) }

            Arrays.sort(files, Comparator.comparingLong { obj: File -> obj.lastModified() })
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "getFilesTrx", ex.message)
        }
        return files
    }


    private fun procesaTrx(f: File) {
        val name = f.name
        //
        if (!f.canRead()) {
            Log.msg(TAG, "[procesaTrx] No se pudo leer: " + f.absolutePath)
            return
        }
        Log.msg(TAG, "[procesaTrx] - procesando...- " + f.name)
        var claveTrx = ""
        var strJSON = ""
        try {
            claveTrx = name.substring(0, 3)
            strJSON = loadFile(f.name, Values.context!!)
            if (strJSON == "") {
                Log.msg(TAG,"[procesaTrx] No se pudo leer Archivo " + f.absolutePath + " o esta vacio.")
                return
            }
            Log.msg(TAG,"[procesaTrx] transaccion: [$claveTrx] | $TAG|\n$strJSON")
            when (claveTrx){
                "GPS" -> sendGPS(strJSON,f)
                "STS" -> sendStatus(strJSON,f)
                "UAS" -> sendAppStatus(strJSON,f)
                "ERR" -> sendErr(strJSON,f)
                else ->{
                    Log.msg("[procesaTrx]", "[procesaTrx] ------> va Borraar el archivo:$name ------+++++")
                    eliminarArchivo(f)
                }
            }


        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "procesaTrx", e.message,"[$claveTrx] json: $strJSON")
            eliminarArchivo(f)
        }
        catch (e1:Exception){
            ErrorMgr.guardar(TAG, "procesaTrx", e1.message,"[$claveTrx] json: $strJSON")
            eliminarArchivo(f)
        }

    }

    fun sendGPS(strJSON:String,file:File){
        try{
            val gson = Gson()
            var locationDto = gson.fromJson(strJSON, LocationDto::class.java)

            Log.msg(TAG,"[sendStatus] STS -2-")
            Inject.inject().getSendLocationDevice().sendObject(locationDto,file)

        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "sendGPS", e.message,"[GPS*] json: $strJSON")
        }
        catch (e1:Exception){
            ErrorMgr.guardar(TAG, "sendGPS", e1.message,"[GPS+] json: $strJSON")
        }
    }

    fun sendStatus(strJSON:String,file:File){
        try{
            val gson = Gson()
            var updateLockStatusDto = gson.fromJson(strJSON, UpdateLockStatusDto::class.java)
            Inject.inject().getSendUpdateStatus().sendObject(updateLockStatusDto,file)
        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "sendStatus", e.message,"[STS*] json: $strJSON")
        }
        catch (e1:Exception){
            ErrorMgr.guardar(TAG, "sendStatus", e1.message,"[STS+] json: $strJSON")
        }
    }

    fun sendAppStatus(strJSON:String,file:File){
        try{
            Log.msg(TAG,"[sendAppStatus] -1-")
            val jsonUpdateStatus: JsonObject = Gson().fromJson(strJSON, JsonObject::class.java)
            Log.msg(TAG,"[sendAppStatus]  -2-")
            Inject.inject().getUpdateAppStatus().sendObject(jsonUpdateStatus,file)
        } catch (e: JSONException) {
            ErrorMgr.guardar(TAG, "sendAppStatus", e.message,"json: $strJSON")
        }
        catch (e1:Exception){
            ErrorMgr.guardar(TAG, "sendAppStatus", e1.message,"json: $strJSON")
        }
    }
    fun sendErr(strJSON:String,file:File){
        try{
            val gson = Gson()
            var errorDto = gson.fromJson(strJSON, ErrorDto::class.java)
            Inject.inject().getSendError().sendObject(errorDto,file)
        } catch (e: JSONException) {
            ErrorMgr.guardar(TrxOffline.TAG, "sendErr", e.message,"json: $strJSON")
        }
        catch (e1:Exception){
            ErrorMgr.guardar(TrxOffline.TAG, "sendErr", e1.message,"json: $strJSON")
        }
    }
    //
}
