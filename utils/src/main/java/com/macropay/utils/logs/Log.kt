package com.macropay.utils.logs


import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import com.macropay.utils.Settings
import com.macropay.utils.preferences.Cons
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StaticFieldLeak")
object Log {
   private var ctx: Context? = null
    get() {return field}
    set(value) {field = value}

    var imei: String = "inst"
        get() {
            return field}
        set(value) {field = value}

    var debug = true
  //  private var prefix = "dpcMacro"
    private var prefix = Cons.TEXT_DPC_LOG
    var fileName: String = ""
        get() =  field
        set(value) {field = value}


    fun init(filePrefix: String, context: Context) {
      //  println("[Log] init filePrefix: "+filePrefix)
        this.ctx = context
        this.prefix = prefix

    }

    fun initialized(): Boolean {
        return this.ctx != null
    }

    fun msg(tag: String, logText: String?) {


        var text = logText
       // if (!debug) return
        try {
            fileName = getFilenameLog()
            //Formatea el texto.
            text = "${getToday()} |[$tag]| $logText"
            println(text)

            //Guarda el mensaje...
            save(text)

        } catch (e: IOException) {
            println("ERROR [msg] Error: "+ e.message +" msg:["+logText+"]")
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
}