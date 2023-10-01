package com.macropay.utils

import android.content.Context
import android.os.Environment
import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.logs.Log
import java.io.*
import java.util.*

object FileMgr {
    private val TAG = "FileMgr"
    fun saveFile(fileName: String, text: String, context: Context) {
        var fileName = fileName
        val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        fileName = storageDir.absolutePath
        val logFile = File(fileName)

        if(logFile.exists())
            logFile.delete()
        storageDir.name
        Log.msg(TAG,"Guardando fileName: "+fileName);
        Log.msg(TAG,"Guardando name: "+storageDir.name);
        Log.msg(TAG,"Guardando storageDir: "+storageDir);
        try {
            logFile.createNewFile()
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append(text)
            buf.newLine()
            buf.close()
            Log.msg(TAG,"guardo text: "+text);
         //   Settings.setSetting(storageDir.name,text)

        } catch (e: IOException) {
            ErrorMgr.guardar(TAG, "saveFile", e.message)
        }
    }

    fun loadFile(fileName: String, context: Context): String {
        Log.msg(TAG,"[loadFile] fileName: $fileName")
        var fileName = fileName
        val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        Log.msg(TAG,"Leyendo name: "+storageDir.name);
        Log.msg(TAG,"Leyendo storageDir: "+storageDir);

        if (!storageDir.exists()) {
            Log.msg(TAG, "No existe el archivo: $fileName ${storageDir.absolutePath}")
            return ""
        }
        fileName = storageDir.absolutePath
        //  Log.msg(TAG,"Leyendo fileName: "+fileName);
        //  Log.msg(TAG,"Leyendo storageDir: "+storageDir);
        var data = ""
        try {
            val fileData = StringBuffer()
            val reader = BufferedReader(FileReader(fileName))
            val buf = CharArray(1024)
            var numRead = 0
            while (reader.read(buf).also { numRead = it } != -1) {
                val readData = String(buf, 0, numRead)
                fileData.append(readData)
            }
            reader.close()
            data = fileData.toString()
            Log.msg(TAG,"Leyendo data: "+data);
            if(data.isEmpty() && storageDir.name.contains("lock"))
            {
                Log.msg(TAG,"[loadFile] LEYO DE SETTINGS: "+storageDir.name )
                data =Settings.getSetting(storageDir.name,"")
                Log.msg(TAG,"[loadFile] LEYO DE SETTINGS: data: "+data )
            }
        } catch (e: IOException) {
            ErrorMgr.guardar(TAG, "loadFile", e.message)
        }
        return data
    }

    fun eliminar(fileName: String, context: Context){
        try{
            val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            eliminarArchivo(storageDir)
        } catch (e: Exception) {
            ErrorMgr.guardar(TAG, "eliminar", e.message)
        }

    }

    fun eliminarArchivo(f: File): Boolean {
        var bResult = false
        try {
            if(f.exists()){
                Log.msg(TAG,"[eliminarArchivo] elimina archivo: ${f.name}")
                f.delete()
            }else
                Log.msg(TAG,"[eliminarArchivo] no existe el archivo: ${f.name}")

            bResult = true
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "eliminarArchivo", "[Ex] archivo: [" + f.name + "] ${ex.message}")
        }
         catch (ex1: IOException) {
            ErrorMgr.guardar(TAG, "eliminarArchivo", "[IO]archivo: [" + f.name + "] ${ex1.message}")
        }
        return bResult
    }

    fun getFiles(directory:String, wildcard:String, context: Context): Array<File>? {
        var files: Array<File>? = null
        try {
            //  File sdCardRoot = Environment.getExternalStorageDirectory();
            val storageDir: File
           // storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString())
            storageDir = File(context.getExternalFilesDir(directory).toString())
            //Buscar los archivos
            files = storageDir.listFiles { d: File?, nameFile: String -> nameFile.contains(wildcard) }
            Arrays.sort(files, Comparator.comparingLong { obj: File -> obj.lastModified() })
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "getFilesLock", ex.message)
        }
        return files
    }
}