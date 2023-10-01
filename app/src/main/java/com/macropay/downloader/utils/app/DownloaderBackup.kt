package com.macropay.downloader.utils.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import com.macropay.downloader.data.mqtt.dto.ApkInfoRequest
import com.macropay.downloader.utils.SettingsApp
import com.macropay.utils.broadcast.Sender
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit



/**
 *
 *
 *
 *
 *
 *
 *                                              NO SE USA: se utiliza la clase: Downloader
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * **/
// https://stackoverflow.com/questions/65164785/using-progressbar-with-downloadmanager
class DownloaderBackup(context: Context) {
    var TAG = "Downloader"
    private val mContext: Context
    private val mDownloadManager: DownloadManager
    private var statusDownload = 0
    private var descripcionStatusDownload = ""
    private var mRazonDownload = ""
    private var mRazonId = 0
    private var countStatus=0
    var progress = 0L
        get() {return field}
        set(value) {field = value}
    // Use a background thread to check the progress of downloading
    private var executor = Executors.newFixedThreadPool(1)
    private var mReboot = true


    var intento= 0
        get() {return field}
        set(value) {field = value}

    fun setReboot(bReboot: Boolean) {
        mReboot = bReboot
    }
    init {
        Log.msg(TAG, "**** CONSTRUCTOR ******")
        this. mContext = context
        mDownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    }

    fun download(app: ApkInfoRequest): Long {
        addReceiver()
        var id = 0L
        countStatus=0
        try {
            val location = app.downloadLocation
            Log.msg(TAG, "-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-")
            Log.msg(TAG, "Inicia descarga desde: [$location]")
            val request = DownloadManager.Request(Uri.parse(location))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)

            val packageFile = extratFileName(app.packageName)
            Log.msg(TAG, "packageFile: $packageFile")
            request.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, packageFile)

            //Borra el archivo si ya existe.
            deleteTempDownload(Environment.DIRECTORY_DOWNLOADS, packageFile)
            // Inicia la descarga...
            id = mDownloadManager.enqueue(request)
            app.downloadId = id
            Log.msg(TAG, "id=$id")
            //Monitor de Avance
            monitor(id)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "download", ex.message)
        }
        return id
    }

    private fun deleteTempDownload(dirType: String, fileName: String) {
        try {
            // File fileDownloaded = new File(this.mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), subPath);
            val fileDownloaded = File(dirType, fileName)
            if (fileDownloaded.exists()) {
                fileDownloaded.delete()
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "deleteTempDownload", ex.message)
        }
    }

    //-->
    private val downloadReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.msg(TAG,"[downloadReceiver] onReceive --> "+intent.action )
            try {
                val referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                terminateDownload(referenceId)

                //
            } catch (e: Exception) {
                ErrorMgr.guardar(TAG, "downloadReceiver.onReceive", e.message)
            }
            // super.onReceive(context, intent);
        }
    }
    private fun terminateDownload(referenceId:Long){
        Log.msg(TAG, "[downloadReceiver] DOWNLOAD_COMPLETE - id: " + referenceId + "] statusDownload: " + statusDownload)
        try{
            if (statusDownload != DownloadManager.STATUS_SUCCESSFUL && statusDownload != DownloadManager.STATUS_RUNNING) {
                Log.msg(TAG, "[downloadReceiver] Aun no termina...,")
                return
            }

            //Inicializa
            resetExecutor()

            //Verifica resultado de la descarga.
            val successful = openDownloadedResponse(mContext, referenceId)
            Log.msg(TAG, "[terminateDownload] DOWNLOAD_COMPLETE - successful: $successful")
            val uriFile = mDownloadManager.getUriForDownloadedFile(referenceId)
            var strUri = ""
            if (uriFile != null) strUri = uriFile.toString()
            Log.msg(TAG, "[terminateDownload] strUri: $strUri")

            // Avisa que ya termino la descarga, para que se instale.
            //  if(successful)
            Sender.sendDownloadStatus(referenceId, strUri, descripcionStatusDownload, mRazonDownload, mRazonId)
            Log.msg(TAG, "[terminateDownload] envio mensaje al InstallManager... ok")

        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "terminateDownload", e.message)
        }
    }
    private fun openDownloadedResponse(context: Context, downloadId: Long): Boolean {
        Log.msg(TAG ,"openDownloadedResponseid: "+downloadId);
        var bResult = false
        try {
            //  DownloadManager downloadManager = (DownloadManager) context.getSystemService (Context.DOWNLOAD_SERVICE);
            val query = DownloadManager.Query()
            query.setFilterById(downloadId)
            val cursor = mDownloadManager.query(query)
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                @SuppressLint("Range") val downloadreason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                val msgError = getDownloadStatus(cursor, downloadId)
                //TODO
                mRazonDownload = downloadreason.toString()
                mRazonId = downloadreason
                if (downloadStatus != 8) {
                    Log.msg(TAG, "[downloadReceiver] +++++++++++++++++++++++++++++++++++++++++++++++++++++++")
                    Log.msg(TAG, "[downloadReceiver] downloadStatus: [$downloadStatus] downloadreason: [$downloadreason]")
                    Log.msg(TAG, "[downloadReceiver] msgError: $msgError")
                    Log.msg(TAG, "[downloadReceiver] +++++++++++++++++++++++++++++++++++++++++++++++++++++++")
                    //if(downloadreason>=400){
                    if(downloadreason>=400 && downloadreason<=500 ){
                        mDownloadManager.remove(downloadId)
                        Sender.sendHttpError(downloadStatus,"Error al descargar la aplicación","",0,mContext )
                        //Sender.sendHttpError(downloadStatus,"Error al descargar la aplicación","",0,context!!)
                    }
                }
                if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                    bResult = true
                } else {
                    try {
                        //Regresa a estado de Enrolo, para que vuela a empezar a descargar.
                        if (mReboot) SettingsApp.statusEnroll(SettingsApp.status.Enrolo)
                        //ERROR;
                        ErrorMgr.guardar(TAG, "openDownloadedResponse",
                            """Status: $downloadStatus : ${descripcionStatusDownload}
		                             Razon: $downloadreason : ${mRazonDownload}""", msgError)

                        //TODO: Cancela la descarga...
                        mDownloadManager.remove(downloadId)
                        if (downloadreason == 404) {
                            Sender.sendStatus("No existe la app: ")
                        } else
                            Sender.sendStatus("Red Inestable")
                    } catch (ex: Exception) {
                    }
                    //ErrorMgr.guardar("","","Status: "+downloadStatus +"\n\t\tError: "+ downloadreason,true);
                }
            }
            cursor.close()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "openDownloadedResponse", ex.message)
        }
        return bResult
    }

    //--
    private fun getDownloadStatus(cursor: Cursor, DownloadId: Long): String {
        var statusText = ""
        var reasonText = ""
        try{
            //column for download  status
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(columnIndex)
            //column for reason code if the download failed or paused
            val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
            val reason = cursor.getInt(columnReason)
            //get the download filename
            // int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
            val filename = "archivo...$DownloadId" //cursor.getString(filenameIndex);

            statusDownload = status
            // Log.msg(TAG,"------> statusDownload: "+statusDownload)
            when (status) {
                DownloadManager.STATUS_FAILED -> {
                    statusText = "STATUS_FAILED"
                    when (reason) {
                        DownloadManager.ERROR_CANNOT_RESUME -> reasonText = "ERROR_CANNOT_RESUME"
                        DownloadManager.ERROR_DEVICE_NOT_FOUND -> reasonText = "ERROR_DEVICE_NOT_FOUND"
                        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> reasonText = "ERROR_FILE_ALREADY_EXISTS"
                        DownloadManager.ERROR_FILE_ERROR -> reasonText = "ERROR_FILE_ERROR"
                        DownloadManager.ERROR_HTTP_DATA_ERROR -> reasonText = "ERROR_HTTP_DATA_ERROR"
                        DownloadManager.ERROR_INSUFFICIENT_SPACE -> reasonText = "ERROR_INSUFFICIENT_SPACE"
                        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> reasonText = "ERROR_TOO_MANY_REDIRECTS"
                        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> reasonText = "ERROR_UNHANDLED_HTTP_CODE"
                        DownloadManager.ERROR_UNKNOWN -> reasonText = "ERROR_UNKNOWN"
                    }
                    Sender.sendStatus("Error descargando app: "+ reasonText)
                }
                DownloadManager.STATUS_PAUSED -> {
                    statusText = "STATUS_PAUSED"
                    when (reason) {
                        DownloadManager.PAUSED_QUEUED_FOR_WIFI -> reasonText = "PAUSED_QUEUED_FOR_WIFI"
                        DownloadManager.PAUSED_UNKNOWN -> reasonText = "PAUSED_UNKNOWN"
                        DownloadManager.PAUSED_WAITING_FOR_NETWORK -> reasonText = "PAUSED_WAITING_FOR_NETWORK"
                        DownloadManager.PAUSED_WAITING_TO_RETRY -> reasonText = "PAUSED_WAITING_TO_RETRY"
                    }
                    //AQui entra cuando hay problemas de red..
                    //Sender.sendStatus("Red Inestable: \n"+ reasonText +"\n"+getTime())
                    //     Sender.sendStatus("Red Inestable: \n"+ "Avance: $progress %" +"\n"+getTime())
                    countStatus++
                    if(countStatus > 60) {
                        countStatus = 0
                        //Sender.sendHttpError(status, "Problemas de red\nConfigura la Red y reintenta continuar...", "download_", DownloadId.toInt(), mContext)
                        Sender.sendHttpError(status, "Problemas de red\nAvance: $progress %  " +getTime(), "download_", DownloadId.toInt(), mContext)

                    }
                    Thread.sleep(500)
                   // Log.msg(TAG,"[getDownloadStatus] reasonText: "+reasonText)
                }
                DownloadManager.STATUS_PENDING -> statusText = "STATUS_PENDING"
                DownloadManager.STATUS_RUNNING -> {
                    statusText = "STATUS_RUNNING"
                    //Mostrar avance de descarga,,,,
                    try {
                        countStatus = 0
                        @SuppressLint("Range") var totalBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if(totalBytes < 0) totalBytes = 35389440
                        if (totalBytes > 0) {
                            @SuppressLint("Range") val downloadedBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            progress = (downloadedBytes * 100 / totalBytes)
                            if(progress>100) progress = 100
                           // Log.msg(TAG, "avance: " + progress + "%");
                            Sender.sendStatus("Avance: $progress%")
                        }
                    } catch (ex: Exception) {
                        ErrorMgr.guardar(TAG, "STATUS_RUNNING", ex.message)
                    }
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    Log.msg(TAG,"[DownloadManager] STATUS_SUCCESSFUL")
                    statusText = "STATUS_SUCCESSFUL"
                    reasonText = "" // "Filename:\n" + filename;
                    Sender.sendStatus("Avance: 100 %")
                }
                else->{
                    Log.msg(TAG,"otro status ---> "+status)
                }
            }
            descripcionStatusDownload = statusText
            mRazonDownload = reasonText
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"getDownloadStatus",ex.message)
        }
        return "$statusText - $reasonText"
    }

    // @Throws(Throwable::class)
    protected fun finalize() {
        //  super.finalize()
        Log.msg(TAG, "[finalize]")
        unRegisterReceiver()
    }

    fun clean() {
        try {
            unRegisterReceiver()
        } catch (ex: Exception) {
        }
    }

    private fun addReceiver() {
        Log.msg(TAG, "[registerReceiver]")
        try {
            if(mContext == null) Log.msg(TAG,"mContext = null")
            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
           // this.mContext.registerReceiver(downloadReceiver, filter )
            this.mContext.registerReceiver(downloadReceiver, filter, "com.macropay.downloader.downloader",null)

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "registerReceiver", ex.message)
        }
    }

    private fun unRegisterReceiver() {
        Log.msg(TAG,"unRegisterReceiver")
        try {
            if (downloadReceiver != null) {
                mContext.unregisterReceiver(downloadReceiver)
                //downloadReceiver = null
            }
        } catch (ex: Exception) {
            //    ErrorMgr.guardar(TAG, "unRegisterReceiver", ex.message)
        }
    }

    private fun extratFileName(packageName: String): String {
        var packageFile = "filename"
        try {
            val pointpos = packageName.lastIndexOf(".")
            if (pointpos > 0) packageFile = packageName.substring(pointpos + 1)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "extratFileName", ex.message)
        }
        return packageFile
    }

    private fun resetExecutor() {
        try {
            if (!executor.isShutdown) {
                Log.msg(TAG, "[resetExecutor] RESET el - Executor - del monitoreo de Avance de descarga")
                //Detiene el monitor de avance de descarga.
                executor.shutdown()
                executor.awaitTermination(5, TimeUnit.SECONDS)
                executor = Executors.newFixedThreadPool(1)
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "resetExecutor", ex.message)
        }
    }

    private fun monitor(downloadId: Long) {
        var ln = 0
        try {
            ln = 1
            resetExecutor()
            ln = 3
            // Run a task in a background thread to check download progress
            executor.execute {
                var progress = 0
                var isDownloadFinished = false
                try {
                    while (!isDownloadFinished) {
                        Thread.sleep(1_000)
                        val cursor = mDownloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                        if (cursor.moveToFirst()) {
                            @SuppressLint("Range")
                            val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val reason = cursor.getInt(columnReason)

                            when (downloadStatus) {
                                DownloadManager.STATUS_RUNNING ->
                                    getDownloadStatus(cursor, downloadId)
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    Log.msg(TAG,"[monitor] STATUS_SUCCESSFUL")
                                    progress = 100
                                    isDownloadFinished = true
                                    terminateDownload(downloadId)
                                }
                                DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {
                                      Log.msg(TAG,"[monitor] STATUS_PAUSED o STATUS_PENDING -> ")

                                    //TODO:IFA: 02Abril2023
                                    getDownloadStatus(cursor, downloadId)
                                  var reasonText = "reason: "+reason
                                    when (reason) {
                                        DownloadManager.PAUSED_QUEUED_FOR_WIFI -> reasonText = "PAUSED_QUEUED_FOR_WIFI"
                                        DownloadManager.PAUSED_UNKNOWN -> reasonText = "PAUSED_UNKNOWN"
                                        DownloadManager.PAUSED_WAITING_FOR_NETWORK -> reasonText = "PAUSED_WAITING_FOR_NETWORK"
                                        DownloadManager.PAUSED_WAITING_TO_RETRY -> reasonText = "PAUSED_WAITING_TO_RETRY"
                                    }
                                    Sender.sendStatus("Red Inestable: \n"+ reasonText +"\n"+getTime())
                                }
                                DownloadManager.STATUS_FAILED ->{
                                    Log.msg(TAG,"[monitor] STATUS_FAILED ")
                                    isDownloadFinished = true
                                }

                                else->{
                                    Log.msg(TAG,"status: + "+downloadStatus)
                                }
                            }
                        }
                    }
                    Log.msg(TAG,"isDownloadFinished: "+isDownloadFinished)
                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "execute.run- progress: $progress", ex.message)
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "monitor ln=$ln", ex.message)
        }
    }
    private fun getTime():String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("HH:mm:ss")
        val today = formatter.format(date)
        return today
    }

}