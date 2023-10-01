package com.macropay.utils.logs
import android.content.Context
import com.macropay.utils.FileMgr
import com.macropay.utils.Settings
import com.macropay.utils.preferences.Cons
import java.io.File
import java.time.LocalDate

class Depurador
 constructor( val context: Context) {
    private val TAG = this::class.simpleName.toString()
  //  private val TAG = "Depurador"
    private val directoryPath = "pictures"
    private val wildCard = ".log"

    private fun isTimeExpired():Boolean {
        var fechaHoy = LocalDate.now().plusDays(-1).toString()
        val fechaProcesada = Settings.getSetting(Cons.LAST_LOGS_REVIEW_DATE,fechaHoy)
        fechaHoy = LocalDate.now().toString()
        return !fechaProcesada.equals(fechaHoy)
    }

    private fun saveLastDateDebugProcess(){
        try {
            val fechaHoy = LocalDate.now().toString()
            Settings.setSetting(Cons.LAST_LOGS_REVIEW_DATE,fechaHoy)
        }catch (ex:java.lang.Exception){
            ErrorMgr.guardar(TAG, "saveLastDateDebugProcess", ex.message)
        }

    }

    //Esta fyncion se llama cada x tiempo
    fun reviewLogFiles() {
        /*Checamos si paso el tiempo para ejecutar el proceso de debug*/
        if (isTimeExpired()){
            deleteLogFiles()
            saveLastDateDebugProcess()
        }
    }

   private fun deleteLogFiles(){
        var allFiles: MutableList<File> = mutableListOf()
        try {
            allFiles = FileMgr.getFiles(directoryPath, wildCard, context)?.toMutableList() ?: mutableListOf()
            /*Si el tamaño de la lista es mayor a 7 corre el proceso de eliminacion de archivos*/
            if (allFiles.size > 7) {
                allFiles.reverse()
                allFiles.forEachIndexed { position, file ->
                    if (position > 6) {
                        FileMgr.eliminarArchivo(file)
                    }
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "runDebuggerLogsProcess", ex.message)
        }
    }
}