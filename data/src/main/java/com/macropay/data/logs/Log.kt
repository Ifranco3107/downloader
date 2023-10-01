package com.macropay.data.logs


import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import com.macropay.utils.Settings
import com.macropay.utils.phone.DeviceInfo
import com.macropay.utils.preferences.Cons
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Log {

    var ctx: Context? = null
        get() {return field}
        set(value) {field = value}

    var imei: String = "inst"
        get() {
            return field}
        set(value) {field = value}

    var debug = true
    private var prefix = Cons.TEXT_DPC_LOG

    var fileName: String = ""
        get() =  field
        set(value) {field = value}

    var lastMsg = ""
        get() =  field

    fun init(filePrefix: String, context: Context) {
        //  println("[Log] init filePrefix: "+filePrefix)
        this.ctx = context
        this.prefix = prefix

    }

    fun initialized(): Boolean {
        return this.ctx != null
    }

    fun msg(tag: String, msg: String?) {


        var text = msg
        // if (!debug) return
        try {
            fileName = getFilenameLog()
            //Formatea el texto.
            text = "${getToday()} | $tag | $msg"
            println(text)
            lastMsg = text
            //Guarda el mensaje...
            save(text)

        } catch (e: IOException) {
            println("ERROR [msg] Error: "+ e.message +" msg:["+msg+"]")
        }
    }
    //USado solo en getImei,porque se cicla al obtener el IMEI.
/*    fun msgForced(tag: String, msg: String?) {
        if(fileName.isEmpty())
            fileName = getFilenameLogFoced("initializing")
*//*        else
            fileName = getFilenameLog()*//*
        var text = msg
        // if (!debug) return
        try {

            //Formatea el texto.
            text = "${getToday()} | $tag |** $msg"
           // println(text)

            //Guarda el mensaje...
            save(text)

        } catch (e: IOException) {
            println("LOG "+e.message)
        }
    }*/
    fun d(tag: String, text: String, e: Exception) {
        msg(tag, """$text${e.message}""".trimIndent())
    }

    fun d(tag: String, text: String?) {
        msg(tag, text)
    }

    fun e(tag: String, text: String, e: Exception) {
        msg(tag, """$text${e.message}""".trimIndent())
    }

    fun e(tag: String, text: String?) {
        msg(tag, text)
    }

    fun w(tag: String, text: String?) {
        msg(tag, text)
    }

    fun i(tag: String, text: String?) {
        msg(tag, text)
    }

    private fun getFilenameLog(): String{
        var fullNameLog = ""
        var fileNameLog = "initial.log"
        var idDevice = "inst"
        try {
            idDevice =  getDeviceID()// DeviceCfg.getImei(ctx!!)
        } catch (ex: Exception) {
            idDevice = "inst"
            println("[getFilenameLog][1] error: "+ ex.message)
        }

        try {
            fileNameLog = this.prefix + "_" + getTodayFile() + "-" + idDevice+".log"
            fullNameLog = File(this.ctx!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileNameLog).toString()
        } catch (ex: Exception) {
            println("[getFilenameLog][2] error: "+ ex.message)
        }
        return fullNameLog
    }

     fun isDbgIconEnabled(): Boolean{
        var existsFlag  =false
        var fileNameLog = "dpc_dbg.log"
        try {
            val fileFlag :File= File(this.ctx!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileNameLog)
            existsFlag = fileFlag.exists()
            if(existsFlag){
                fileFlag.delete()
            }
        } catch (ex: Exception) {
            println("[isDbgIconEnabled "+ ex.message)
        }
        return existsFlag
    }

    private fun getFilenameLogFoced(default:String): String{
        var fullNameLog = ""
        var fileNameLog = ""
        try {
            println("[getFilenameLogFoced] - inicia")
            fileNameLog = default+".log"
            println("[getFilenameLogFoced] - fileNameLog: "+fileNameLog )

            fullNameLog = File(this.ctx!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileNameLog).toString()
            println("[getFilenameLogFoced] fullNameLog: " +fullNameLog)
        } catch (ex: Exception) {
            println("[getFilenameLogFoced] error: "+ ex.message)
        }
        return fullNameLog
    }
    fun getDeviceID(): String {
        var deviceID = "Inst"
        try {
            deviceID = Settings.getSetting(Cons.KEY_ID_DEVICE, Build.getSerial())
        } catch (ex: java.lang.Exception) {
            // ErrorMgr.guardar(TAG, "getDeviceID", ex.message)
        }
        return deviceID
    }



    private fun save(msg:String){
        //Guarda el mensaje....
        //println("[save] "+fileName)
        try{
            val logFile = File(fileName)
            logFile.createNewFile()
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append(msg)
            buf.newLine()
            buf.close()
        }
        catch (ex:Exception){
            println("[save] error: "+ ex.message)
            println("[save] error: fileName: "+fileName)
            println("[save] error: msg:  "+msg)
        }

    }
    private fun getToday():String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("HH:mm:ss")
        val today = formatter.format(date)
        return today
    }

    private fun getTodayFile():String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("ddMMyy")
        val today = formatter.format(date)
        return today
    }



    fun fileName(): String {
        //System.out.println("[Log] filename: "+fileLog);
        return getFilenameLog()
    }

    fun getFilenameLog(dias: Int): String {
        // Date date = new Date(); // Calendar.getInstance().getTime();
        var fechaLog = LocalDateTime.now()
        //  SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
        if (dias > 0) {
            fechaLog = fechaLog.plusDays((dias * -1).toLong()) //   LocalDateTime.from(fechaLog.toInstant()).plusDays(dias*-1);
        }
        val customFormat = DateTimeFormatter.ofPattern("ddMMyy")
        val today = fechaLog.format(customFormat)
        // String today = formatter.format(fechaLog);
        var fileNameLog = prefix
        try {
            //String imei=   DeviceCfg.getImei(context);
            val imei: String = DeviceInfo.getDeviceID() //.getImei(context);
            if (!prefix.contains("boot")) fileNameLog = prefix + "_" + today + "-" + imei
            fileNameLog += ".log"
            val storageDir = File(ctx!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileNameLog)
            fileNameLog = storageDir.absolutePath
        } catch (ex: Exception) {
            msg("Log", ex.message)
        }
        return fileNameLog
    }
}




/*


import android.content.Context
import android.os.Environment
import com.macropay.dpcmacro.utils.device.DeviceService
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Log(fileLog: String, context: Context) {
    init {
        var fileLog = fileLog
        val debugFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Debug.dbg")
        debug = true // (debugFile.exists());
        //debug = fileLog.contains("REST_Services");
        if (!debug) return
        try {
            prefijo = fileLog
            this.fileLog = fileLog
            this.context = context


            //System.out.println("fileLog; 1[" +fileLog +"]");
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat("ddMMyy")
            val today = formatter.format(date)
            // String imei= DeviceCfg.getImei(context);
            val imei: String = DeviceInfo.getDeviceID() //.getImei(context);
            if (!fileLog.contains("boot")) fileLog = fileLog + "_" + today + "-" + imei + ".log"

            // System.out.println("fileLog; 1[" +fileLog +"]");
            val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileLog)
            this.fileLog = storageDir.absolutePath // filename;
            //  msg("Log",getFilenameLog( ));
            //      System.out.println("this.fileLog ; 2[" +this.fileLog  +"]"); [save] error:
            //      System.out.println("fileLog; 3[" +fileLog +"]");
        } catch (ex: Exception) {
            if (initialized()) msg("Log", ex.message) else println("Log; ERROR[" + ex.message + "]")
        }
    }

    companion object {
        var fileLog = ""
        var context: Context? = null
        var debug = false
        var prefijo = ""
        @JvmStatic
        fun init(fileLog: String, context: Context) {
            Log(fileLog, context)
        }

        fun initialized(): Boolean {
            return context != null
        }//.getImei(context);

        // String imei= DeviceCfg.getImei(context);
        private val filenameLog: String
            private get() {
                val date = Calendar.getInstance().time
                val formatter = SimpleDateFormat("ddMMyy")
                val today = formatter.format(date)
                var fileNameLog = prefijo
                try {
                    // String imei= DeviceCfg.getImei(context);
                    val imei: String = DeviceInfo.getDeviceID() //.getImei(context);
                    if (!prefijo.contains("boot")) fileNameLog = prefijo + "_" + today + "-" + imei
                    fileNameLog += ".log"
                } catch (ex: Exception) {
                    msg("Log", ex.message)
                }
                return fileNameLog
            }

        fun getFilenameLog(dias: Int): String {
            // Date date = new Date(); // Calendar.getInstance().getTime();
            var fechaLog = LocalDateTime.now()
            //  SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy");
            if (dias > 0) {
                fechaLog = fechaLog.plusDays((dias * -1).toLong()) //   LocalDateTime.from(fechaLog.toInstant()).plusDays(dias*-1);
            }
            val customFormat = DateTimeFormatter.ofPattern("ddMMyy")
            val today = fechaLog.format(customFormat)
            // String today = formatter.format(fechaLog);
            var fileNameLog = prefijo
            try {
                //String imei=   DeviceCfg.getImei(context);
                val imei: String = DeviceInfo.getDeviceID() //.getImei(context);
                if (!prefijo.contains("boot")) fileNameLog = prefijo + "_" + today + "-" + imei
                fileNameLog += ".log"
                val storageDir = File(context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileNameLog)
                fileNameLog = storageDir.absolutePath
            } catch (ex: Exception) {
                msg("Log", ex.message)
            }
            return fileNameLog
        }

        fun d(tag: String, text: String, e: Exception) {
            msg(
                tag, """
     $text
     ${e.message}
     """.trimIndent()
            )
        }

        @JvmStatic
        fun d(tag: String, text: String?) {
            msg(tag, text)
        }

        fun e(tag: String, text: String, e: Exception) {
            msg(
                tag, """
     $text
     ${e.message}
     """.trimIndent()
            )
        }

        @JvmStatic
        fun e(tag: String, text: String?) {
            msg(tag, text)
        }

        @JvmStatic
        fun w(tag: String, text: String?) {
            msg(tag, text)
        }

        @JvmStatic
        fun i(tag: String, text: String?) {
            msg(tag, text)
        }

        @JvmStatic
        fun msg(tag: String, text: String?) {
            var text = text
            if (!debug) return
            val logFile = File(fileLog)
            try {
                logFile.createNewFile()
                val date = Calendar.getInstance().time
                // SimpleDateFormat            formatter = new SimpleDateFormat("EEEE,hh:mm:ss a");
                val formatter = SimpleDateFormat("HH:mm:ss")
                val today = formatter.format(date)
                text = "$today | $tag | $text"
                val buf = BufferedWriter(FileWriter(logFile, true))
                buf.append(text)
                buf.newLine()
                buf.close()
                //     buf.flush();
                println(text)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun fileName(): String {
            //System.out.println("[Log] filename: "+fileLog);
            return fileLog
        }

        val log: String
            get() {
                var strLog = ""
                try {
                    strLog = FileMgr.loadFile(filenameLog, context)
                } catch (ex: Exception) {
                }
                return strLog
            }
    }
}*/
