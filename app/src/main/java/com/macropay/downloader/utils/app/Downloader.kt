package com.macropay.downloader.utils.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import com.macropay.downloader.data.mqtt.dto.ApkInfoRequest
import com.macropay.utils.broadcast.Sender
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

// https://www.digitalocean.com/community/tutorials/java-timer-timertask-example
// https://stackoverflow.com/questions/65164785/using-progressbar-with-downloadmanager
@Singleton
class DownloaderTemp
@Inject constructor(
    @ApplicationContext var context: Context) {

    var TAG = "Downloader"
    private val mContext: Context
    private val mDownloadManager: DownloadManager
    private var tmrDownloadMonitor = Timer()
    private var location :String = ""
    private var packageFile:String = ""
    private var statusDownload = 0
    private var inicioDownload = LocalDateTime.now()
    var intento= 0
        get() {return field}
        set(value) {field = value}
    private var countStatus=0
    var currentRequest :ApkInfoRequest? = null
    var hasError=false
        get() {return field}
        set(value) {field = value}

    // Use a background thread to check the progress of downloading
//    private var executor = Executors.newFixedThreadPool(1)
    private var mReboot = true
    private lateinit var  downloadProgress : DownloadProgress

    //
    var listenerDownload: DownloadStatus? = null

fun tiempo():Long{

    val secs =   Utils.tiempoTranscurrido(inicioDownload, ChronoUnit.SECONDS)
    return secs
}
    init {
        Log.msg(TAG, "[init] **** CONSTRUCTOR ******")
        this.mContext = context
        mDownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadProgress = DownloadProgress()
        intento=0
    }
/*   fun test(){
        // val tmrDownloadMonitor = Timer()
        val downloadProgress =DownloadProgress()
        //tmrDownloadMonitor.schedule(downloadProgress, 0, 1_000)

    }*/
    fun setReboot(bReboot: Boolean) {
        mReboot = bReboot
    }

    fun setOnDownloadStatus(listener: DownloadStatus) {
        this.listenerDownload = listener
    }

    fun downloadStatusProgress(){
        downloadProgress.setOnEventListener(  object: DownloadStatusProgress {
            override fun onAvance(porc: Long) {
                try{
                    val msg = "Avance: $porc %"
                    Sender.sendStatus(msg)
                }catch (ex:Exception){
                    ErrorMgr.guardar(TAG,"onAvance",ex.message)
                }
            }

            override fun onDownload(success: Boolean, referenceId: Long, packageName: String, status: InstallStatus.eEstado) {
                try{
                    hasError = false
                    Log.msg(TAG,"[onDownload] onDownload: $success - Notifica para que se instale...${tiempo()} segs.")
                    stopMonitor()
                    notityInstaller(referenceId,status)
                }catch (ex:Exception){
                    ErrorMgr.guardar(TAG,"onDownload",ex.message)
                }
            }

            override fun onError(code: Int, error: String?, idDownload: Long, packageName: String, status: InstallStatus.eEstado) {
                try{
                    Log.msg(TAG,"**********************************************************")
                    Log.msg(TAG,"[onError] onError: $code $error $idDownload")
                    Log.msg(TAG,"**********************************************************")
                    //currentRequest!!.downloadId = startDownload(packageFile,location)
                    hasError = true
                    cancel(idDownload)
                    stopMonitor()
                    //Avisa a EnrollActivity,  que hubo error.
                    Sender.sendEnrollStatus("download", code,error ,idDownload,mContext)
                    notifyError(code ,error ,idDownload,packageName,status)
                }catch (ex:Exception){
                    ErrorMgr.guardar(TAG,"onError",ex.message)
                }
            }

        })
    }
    fun notifyError(code: Int, error: String?,idDownload:Long, packageName: String, status: InstallStatus.eEstado){
        this.listenerDownload!!.onError(false,error,packageName,status)
    }
    fun download(app: ApkInfoRequest): Long {
      //  addReceiver()
        currentRequest = app

        var id = 0L
        countStatus= 0

        try {
            location = app.downloadLocation
            packageFile = extratFileName(app.packageName)
            Log.msg(TAG, "[download]-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-")
            Log.msg(TAG, "[download] Inicia descarga desde: [$location]")
            Log.msg(TAG, "[download] packageFile: $packageFile")

            //Elimina archivos descargados previamente de versiones anteriores.
            depurar(packageFile)

            //Inicia descarga
            app.downloadId = startDownload(packageFile,app.packageName,location)

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "download", ex.message)
        }
        return id
    }

    fun startDownload(packageFile:String,packageName:String,location:String ):Long= runBlocking(){
        Log.msg(TAG,"[startDownload]")
        var id:Long = 0L
        try {
            Sender.sendStatus("inicia descarga...")
            val request = DownloadManager.Request(Uri.parse(location))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            request.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, packageFile)

            // Inicia la descarga...
            id = mDownloadManager.enqueue(request)
            Log.msg(TAG, "[startDownload] id=$id")
            notityInstaller(id,InstallStatus.eEstado.InicioDescarga)

            //Recibe notificaciones de downloadProgres
            startMonitor(id,packageName,mContext)

        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "startDownload", ex.message)
        }
        return@runBlocking id
    }

    //-->
    private val downloadReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.msg(TAG,"[downloadReceiver] onReceive --> "+intent.action )
            try {
                Log.msg(TAG, "[downloadReceiver] DOWNLOAD_COMPLETE - ["+ intent.action + "] statusDownload: " + statusDownload)
/*                if (statusDownload != DownloadManager.STATUS_SUCCESSFUL && statusDownload != DownloadManager.STATUS_RUNNING) {
                    Log.msg(TAG, "[downloadReceiver] Aun no termina...,")
                    return
                }*/
                Log.msg(TAG,"[downloadReceiver] --------------------------------------------------------------")
                Log.msg(TAG,"[downloadReceiver] -------------   DOWNLOAD_COMPLETE  ---------------------------")
                Log.msg(TAG,"[downloadReceiver] --------------------------------------------------------------")

                //Inicializa
              //  resetExecutor()
               // stopMonitor()
                stopMonitor()

                try{
                    Thread.sleep(1_000)
                }catch (e:InterruptedException){
                    ErrorMgr.guardar(TAG,"downloadReceiver[Thread]",e.message)
                }

                //Verifica resultado de la descarga.
                val successful = getTerminateStatus()
                val referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                //Notifica al instalador...
                if(successful)
                    notityInstaller(referenceId,InstallStatus.eEstado.DescargaCompleta)

            } catch (e: Exception) {
                ErrorMgr.guardar(TAG, "downloadReceiver[Exception]", e.message)
            }
            catch ( e:InterruptedException) {
                ErrorMgr.guardar(TAG,"downloadReceiver[InterruptedException]",e.message)
            }
        }
    }
    private fun startMonitor(id:Long, packageName:String  , mContext:Context){
        try{
            inicioDownload = LocalDateTime.now()
            //==========================
            //Monitor de Avance
            //==========================
            downloadProgress = DownloadProgress()
            downloadStatusProgress()
            downloadProgress.initParms(mDownloadManager, id, packageName  , mContext)
            // downloadProgress.run()
            // stopMonitor()
            Log.msg(TAG,"[startMonitor] -1-")
            tmrDownloadMonitor = Timer()
            Log.msg(TAG,"[startMonitor] -2-")
            tmrDownloadMonitor.schedule(downloadProgress, 0, 1_000)
            Log.msg(TAG,"[startMonitor] -3-")
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"startMonitor",ex.message)
        }
    }
    private fun stopMonitor(){
        try{
            downloadProgress.stopMonitor()
            downloadProgress.cancel() //IFA 01Jul23
          //  tmrDownloadMonitor.purge() //IFA 02Ago23
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"stopMonitor",ex.message)
        }
    }
    private fun getTerminateStatus():Boolean{
        var  bResult = false
        try{
            downloadProgress.queryStatus()
            bResult = (downloadProgress.status == DownloadManager.STATUS_SUCCESSFUL)
            if(!bResult){
                Log.msg(TAG, "[getTerminateStatus] +++++++++++++++++++++++++++++++++++++++++++++++++++++++")
                Log.msg(TAG, "[getTerminateStatus] downloadStatus: [${downloadProgress.status}] downloadreason: [${downloadProgress.reasonId}]")
                Log.msg(TAG, "[getTerminateStatus] +++++++++++++++++++++++++++++++++++++++++++++++++++++++")

                //Solo por error de servidor, se cancela la descarga...
                if(downloadProgress.reasonId>=400 && downloadProgress.reasonId<=500 ){
                    Log.msg(TAG,"[getTerminateStatus] Cancelando la Descarga ")
                    mDownloadManager.remove(downloadProgress.downloadId)
                    Sender.sendHttpError(downloadProgress.status,"Error al descargar la aplicación","",0,mContext )
                    bResult = true;
                }

                //Si es error de ERROR_CANNOT_RESUME, no puede continuar despues de reestablecerse la red.
                if(downloadProgress.reasonId==1008 ){
                    intento++
                    Log.msg(TAG,"[getTerminateStatus] Reintenta - reinicia a descargar.- Intento: "+intento)
                    if(intento<3){
                        Log.msg(TAG, "[getTerminateStatus] " + downloadProgress +" "+ currentRequest!!.downloadId)
                        //Cancela la descarga actual..
                        //mDownloadManager.remove(currentRequest!!.downloadId)

                        //Inicia nuevamente la descarga
                        Log.msg(TAG, "[getTerminateStatus] -1-")
                       // downloadProgress.stopMonitor()

                        Log.msg(TAG, "[getTerminateStatus] -2-")

                        //TODO
                        //---  download(currentRequest!!)
                        currentRequest!!.downloadId = startDownload(packageFile, currentRequest!!.packageName ,location)

                        Log.msg(TAG, "[getTerminateStatus] -3-")

                        //  Sender.sendHttpError(this.status,"Error al descargar la aplicación","",0,mContext )
                      //  Sender.sendHttpError(this.status,"Problemas de red\nReconecta la Red y reintenta continuar...","download_",currentRequest!!.downloadId.toInt(),mContext )
                    }else
                    {
                        //Pendientes..21Enero...
                        //-configuracion WIFI,
                        //-Reintentar...
                        //-Verificar que se descarguen en el DPC
                        //-En el boton de ejecucion de la app.
                        //Probar los timeouts...

                        //Deja de intentar, y actiba los botones de WIFI y Continuar...
                        Log.msg(TAG,"[getTerminateStatus] manda mensaje para que se activen los botons de Config y Retry - id: "+ currentRequest!!.downloadId)
                        Sender.sendHttpError(downloadProgress.status,"Problemas de red\nReconecta la Red y reintenta continuar...","download_",currentRequest!!.downloadId.toInt(),mContext )
                    }
                }
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"getTerminateStatus",ex.message)
        }
        return bResult
    }

    private fun notityInstaller(referenceId:Long, status: InstallStatus.eEstado) {
        Log.msg(TAG, "[notityInstaller] - referenceId: $referenceId - [${status.name}]")
        try {
            val uriFile = mDownloadManager.getUriForDownloadedFile(referenceId)
            Log.msg(TAG, "[notityInstaller] - uriFile: $uriFile")
            var strUri = ""
            if (uriFile != null) strUri = uriFile.toString()
            Log.msg(TAG, "[notityInstaller] strUri: $strUri packageName: "+currentRequest!!.packageName)

            // Avisa que ya termino la descarga, para que se instale.
            this.listenerDownload!!.onDownloaded(referenceId, currentRequest!!.packageName, strUri, downloadProgress.descriptionStatus, downloadProgress.descriptionReason, downloadProgress.reasonId,status)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"notityInstaller",ex.message)
        }
    }


    // @Throws(Throwable::class)
    protected fun finalize() {
        //  super.finalize()
        Log.msg(TAG, "[finalize]")
        unRegisterReceiver()
    }
    fun cancel(id:Long){
        Log.msg(TAG,"[cancel] id: $id")
        try{
            mDownloadManager.remove(id)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG, "cancel", ex.message)
        }
    }

    fun clean() {
        Log.msg(TAG,"[clean]")
        try {
            unRegisterReceiver()
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "clean", ex.message)
        }
    }

    private fun addReceiver() {
        Log.msg(TAG, "[addReceiver]")
        try {
            //Desregistra, por si ya exisita un receiver..
            unRegisterReceiver()

            //Registra
            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            //this.mContext.registerReceiver(downloadReceiver, filter)
            this.mContext.registerReceiver(downloadReceiver, filter, "com.macropay.downloader.downloder",null)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "registerReceiver", ex.message)
        }
    }

    private fun unRegisterReceiver() {
        Log.msg(TAG,"unRegisterReceiver")
        try {
            if (downloadReceiver != null) {
                mContext.unregisterReceiver(downloadReceiver)
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

    private fun depurar(wilcardName: String) {
        //Log.msg(TAG, "[depurar]  wilcardName: $wilcardName")
        var files: Array<File>? = null
        try {
            val storageDir: File
            storageDir = File(this. mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString())
            //Buscar los archivos *.trx
            files = storageDir.listFiles { d: File?, nameFile: String -> nameFile.contains(wilcardName) }
            Arrays.sort(files, Comparator.comparingLong { obj: File -> obj.lastModified() })
          //  Log.msg(TAG, "[depurar]  files: "+files.size)
            files.forEach {
              //  Log.msg(TAG,"name: "+it.name)
                try{
                    it.delete()
            //        Log.msg(TAG,"name: "+it.name + " borrado")
                }catch (e:Exception){
                }
            }
        } catch (ex: Exception) {
            com.macropay.utils.logs.ErrorMgr.guardar(TAG, "depurar", ex.message)
        }
        return

    } //fin depurador
}

interface DownloadStatus {
    fun onDownloaded(id: Long?, packageName: String,fileDownloaded: String?, status: String?, razon: String?, razonid: Int, installstatus: InstallStatus.eEstado,)
    fun onError(success: Boolean, error: String?, packageName: String, status: InstallStatus.eEstado)
}
