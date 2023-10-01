package com.macropay.downloader.utils.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.broadcast.Sender
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


/*
*
*
* */


//https://www.digitalocean.com/community/tutorials/java-timer-timertask-example
class DownloadProgress: TimerTask() {

    private val query: DownloadManager.Query = DownloadManager.Query()
    private var totalBytes: Long = 0
    private var lastEvent = LocalDateTime.now()
    private val TAG = "DownloadProgress"
    val handlerLock = Handler(Looper.getMainLooper())
    var avanceAnterior = -1L


    //Escuha los eventos del proceso de descarga del archivo apk
    var listener: DownloadStatusProgress? = null
    fun setOnEventListener(listener: DownloadStatusProgress) {
        this.listener = listener
    }

    init {
/*        this.downloadId = downloadId
        this.manager = manager
        this.mCtx = context
        Log.msg(TAG,"[init]  id: " +downloadId)
        query.setFilterById(downloadId)
        setTimeoutEvent()*/


    }
    fun initParms(manager: DownloadManager,downloadId: Long,packageName: String, context: Context) {
        Log.msg(TAG,"[initParms]  id: " +downloadId)
        this.downloadId = downloadId
        this.packageName = packageName
        this.manager = manager
        this.mCtx = context

        query.setFilterById(downloadId)
        //Pone un timeout para poner un limite de tiempo para la descarga...
        setTimeoutEvent()
        lastEvent = LocalDateTime.now()
    }

     var downloadId :Long= 0
         set(value) {field = value}
    var  packageName :String= ""
        set(value) {field = value}


    var manager :DownloadManager? = null
        set(value) {field = value}
    var mCtx: Context? = null
        set(value) {field = value}
    var status = 0
        get() {return field}
        set(value) {field = value}
    var descriptionStatus = ""
        get() {return field}
        set(value) {field = value}
    var descriptionReason = ""
        get() {return field}
        set(value) {field = value}
    var reasonId = 0
        get() {return field}
        set(value) {field = value}

    var progress = 0L
        get() {return field}
        set(value) {field = value}

    private var intento= 0
    private var countStatus=0

    //we are going to use a handler to be able to run in our TimerTask

    fun setTimeoutEvent(){

        handlerLock.removeCallbacks(this)
        val milisegs = 5 * 60_000L
        handlerLock.postDelayed({
            Log.msg(TAG,"[setTimeoutEvent]  postDelayed: llego a Timeout...id $downloadId")
            if(downloadId > 0)
                listener!!.onError(1,"Timeout", downloadId,packageName,InstallStatus.eEstado.DescargaIncorrecta)
        }, milisegs)

    }
    fun stopMonitor(){
        try{
            Log.msg(TAG,"[stopMonitor] ")
            downloadId = 0
            handlerLock.removeCallbacks(this)

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"stopMonitor",ex.message)
        }
    }
    @SuppressLint("Range")
    override fun run() {
       // Log.msg(TAG,"[run] id: " +this.downloadId)
      //  Sender.sendStatus("inicia descarga...",mCtx!!)
        var counter =0
        countStatus =0
        lastEvent = LocalDateTime.now()
        try{
/*            CoroutineScope(Dispatchers.IO)
            .launch {
                while (downloadId > 0) {
                    val secs =   Utils.tiempoTranscurrido(lastEvent,ChronoUnit.SECONDS)
                    if(secs >=1) {
                        lastEvent = LocalDateTime.now()*/
                       // Log.msg(TAG,"[run] $secs secs v1")
                        manager!!.query(query).use {
                            if (it.moveToFirst()) {
                                monProgress(it)
                                it.close()
                            }
                      /*  }
                        //Thread.sleep(1_000)
                        delay(2_000)
                    }
                }*/
            }
           // Log.msg(TAG,"[run] termino while...")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"run[Exception]",ex.message)
        }
        catch ( e:InterruptedException) {
            ErrorMgr.guardar(TAG,"run[InterruptedException]",e.message)
        }

    }

    @SuppressLint("Range")
    fun monProgress(it: Cursor){
        try{
            status = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
            reasonId = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_REASON))
            //Define las textos del status
            getStatusDescription(status,reasonId)

            when (status) {

                DownloadManager.STATUS_RUNNING -> {
                    try{
                        progress =  getProgress(it)
                        if(progress != avanceAnterior ){
                            avanceAnterior = progress
     /*                       val msg = "Avance: $progress %"
                            //  Sender.sendStatus(msg)
                            Sender.sendStatus(msg,mCtx!!)*/
                             this.listener!!.onAvance(progress)
                        }
                        Log.msg(TAG, "[run] STATUS_RUNNING - progress: $progress")
                    }catch (ex:Exception){
                        ErrorMgr.guardar(TAG,"running",ex.message)
                    }
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    Log.msg(TAG, "[run] STATUS_SUCCESSFUL - termino la descarga..."+this.descriptionStatus)
                    progress= 100
                    Sender.sendStatus("Avance: $progress %",mCtx!!)
                 //   this.interrupt()
                    this.listener!!.onDownload(true,this.downloadId,this.packageName,InstallStatus.eEstado.DescargaCompleta)
                    this.downloadId = -1
                    handlerLock.removeCallbacks(this)

                }


                //=================[ Problemas en la red ]=======================
                DownloadManager.STATUS_PAUSED -> {
                    Log.msg(TAG,"[run] " +this.descriptionStatus + " [ "+ this.descriptionReason+"] : countStatus: "+countStatus  +" id: "+this.downloadId)

                    Sender.sendStatus("Red Inestable.",mCtx!!)

                    espera(500)
                    //Error de RED
                    countStatus++
                    if(countStatus > 60){
                        countStatus=0
                        this.listener!!.onError(1,"Avance: $progress %",this.downloadId,packageName,InstallStatus.eEstado.DescargaIncorrecta)
                        this.downloadId = -1
                      //  this.interrupt()
                    }
                }

                //=================[ Problemas al descargar el archivo ]=======================
                DownloadManager.STATUS_FAILED -> {
                    //No pudo descargar, porque no existe el archivo o no responde esa url status,reasonId
                    Log.msg(TAG,"[run] $status [" +this.descriptionStatus + "] $reasonId  [ "+ this.descriptionReason+"]")
                    //this.interrupt()
                    this.listener!!.onError(2,this.descriptionStatus + " [ "+ this.descriptionReason+"]",downloadId,packageName,InstallStatus.eEstado.DescargaIncorrecta)
                    this.downloadId = -1
                    handlerLock.removeCallbacks(this)
                }

                DownloadManager.STATUS_PENDING -> {
                    Log.msg(TAG,"[run] " +this.descriptionStatus + " [ "+ this.descriptionReason+"]")
                    //TODO, como tratar este error? actualmente no se sale del ciclo.
                    espera(500)
                }
                else->{
                    Log.msg(TAG,"otro status ---> "+status)
                }
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"monProgress",ex.message)
        }
    }
    private fun espera(mils:Long){
        try{
            Thread. sleep(mils) }
        catch (e:InterruptedException){
            ErrorMgr.guardar(TAG,"espera[Thread]",e.message)
        }
    }
    @SuppressLint("Range")
    fun queryStatus() :Int{
        try{
            manager!!.query(query).use {
                if (it.moveToFirst()) {
                    status = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    reasonId = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_REASON))
                    //Define las textos del status
                    getStatusDescription(status, reasonId)
                }
                it.close()
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"queryStatus",ex.message)
        }
        return status
    }

    @SuppressLint("Range")
    private fun getProgress(it:Cursor):Long{
        var percentProgress = 0L
        try{
            if (totalBytes <= 0) {
                totalBytes = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)).toLong()
            }
            val downloadedBytes = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            //update progress
            var total :Long =  if ( totalBytes >0) totalBytes else  65798144L //  35389440L
            percentProgress = ((downloadedBytes * 100L) / total)
            if(percentProgress> 100) percentProgress = 95

          // Log.msg(TAG,"[getProgress] progress: $percentProgress  downloadedBytes: $downloadedBytes")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"getProgress",ex.message)
        }
        return percentProgress
    }

    private fun getStatusDescription(status:Int,reason:Int ) {
        try{
            when (status) {
                DownloadManager.STATUS_FAILED -> {
                    descriptionStatus = "STATUS_FAILED"
                    when (reason) {
                        DownloadManager.ERROR_CANNOT_RESUME -> descriptionReason = "ERROR_CANNOT_RESUME"
                        DownloadManager.ERROR_DEVICE_NOT_FOUND -> descriptionReason = "ERROR_DEVICE_NOT_FOUND"
                        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> descriptionReason = "ERROR_FILE_ALREADY_EXISTS"
                        DownloadManager.ERROR_FILE_ERROR -> descriptionReason = "ERROR_FILE_ERROR"
                        DownloadManager.ERROR_HTTP_DATA_ERROR -> descriptionReason = "ERROR_HTTP_DATA_ERROR"
                        DownloadManager.ERROR_INSUFFICIENT_SPACE -> descriptionReason = "ERROR_INSUFFICIENT_SPACE"
                        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> descriptionReason = "ERROR_TOO_MANY_REDIRECTS"
                        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> descriptionReason = "ERROR_UNHANDLED_HTTP_CODE"
                        DownloadManager.ERROR_UNKNOWN -> descriptionReason = "ERROR_UNKNOWN"
                    }
                }
                //========================================
                DownloadManager.STATUS_PAUSED -> {
                    descriptionStatus = "STATUS_PAUSED"
                    when (reason) {
                        DownloadManager.PAUSED_QUEUED_FOR_WIFI -> descriptionReason = "PAUSED_QUEUED_FOR_WIFI"
                        DownloadManager.PAUSED_UNKNOWN -> descriptionReason = "PAUSED_UNKNOWN"
                        DownloadManager.PAUSED_WAITING_FOR_NETWORK -> descriptionReason = "PAUSED_WAITING_FOR_NETWORK"
                        DownloadManager.PAUSED_WAITING_TO_RETRY -> descriptionReason = "PAUSED_WAITING_TO_RETRY"
                    }
                }
                DownloadManager.STATUS_PENDING -> descriptionStatus = "STATUS_PENDING"
                DownloadManager.STATUS_RUNNING -> {
                    descriptionStatus = "STATUS_RUNNING"
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    //   Log.msg(TAG,"------> DownloadManager.STATUS_SUCCESSFUL")
                    descriptionStatus = "STATUS_SUCCESSFUL"
                    descriptionReason = "" // "Filename:\n" + filename;
                }
                else->{
                    Log.msg(TAG,"otro status ---> "+status)
                }
            }

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"getStatusDescription",ex.message)
        }
        return
    }
    private fun getTime():String{
        var today = ""
        try{
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat("HH:mm:ss")
            today = formatter.format(date)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"getTime",ex.message)
        }
        return today
    }
/*    private fun notifyError(reasonId:Int,reasonText:String, progress:Int,downloadId:Long){
        try{
            var segs = 0
            if (startNetProblem != null)
                segs= Utils.tiempoTranscurrido(startNetProblem, ChronoUnit.SECONDS).toInt()

            //Define el texto a mostrar.
            var msg = ""
            if(progress>0)
                msg = "Avance: $progress%\n\n"

            msg += "[]Red Inestable\n"
            msg += reasonText +"\n"
            if(segs>0)
                msg +=  segs.toString()
            else
                msg +=  getTime()


            //Avisa al EnrollActivity, el status de la descarga.
            Sender.sendHttpError(reasonId,msg,"download_paused" , segs, mContext)
            Thread.sleep(1_000)

            Log.msg(TAG,"[notifyError] reasonId: $reasonId statusText: $reasonText - reasonText: $reasonText segs: $segs" )

            //Si el usuario cancelo en el boton del EnrollActivity.
            val userCanceloDownload= Settings.getSetting(Cons.KEY_CANCEL_DOWNLOAD,false)

            if(userCanceloDownload) {
                Log.msg(TAG,"[notifyError] usuario cancelo la descarga.")
                //Cancela la descarga... para continuar...
                mDownloadManager.remove(downloadId)
                intento++
                if(intento <3){
                    Log.msg(TAG, "[downloadStatus] **** Reintentando descargar... ***")
                    if (currentRequest != null)  download(currentRequest!!)
                }else
                {
                    intento= 0
                    Sender.sendDownloadStatus(downloadId, "cancelar", descripcionStatusDownload, mRazonDownload, mRazonId)

                }

            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"notifyError",ex.message)
        }
    }*/

}

interface DownloadStatusProgress {
    fun onAvance(porc:Long)
    fun onDownload(success: Boolean,referenceId:Long , packageName: String,status:  InstallStatus.eEstado)
    fun onError(code: Int, error: String?,idDownload:Long, packageName: String,status:  InstallStatus.eEstado)
}