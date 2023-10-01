package com.macropay.utils

import android.os.Build
import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.logs.Log
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object Utils {

        val TAG = "Utils"
       // var mContext: Context? = null
      //  private val DEFAULT_BUFFER_SIZE = 4096

        // TODO: Update to S when VERSION_CODES.R becomes available.
        val R_VERSION_CODE = 30
        private val IS_RUNNING_R = Build.VERSION.CODENAME.length == 1 && Build.VERSION.CODENAME[0] == 'R'

        val Q_VERSION_CODE = 29
        val SDK_INT = if (IS_RUNNING_R) Build.VERSION_CODES.CUR_DEVELOPMENT else Build.VERSION.SDK_INT

        fun tiempoTranscurrido(dteFechaAnterior: LocalDateTime?, escala: ChronoUnit?): Long {
                var tiempo = 0L
                try {
                        val dteHoy = LocalDateTime.now()
                        val tempDateTime = LocalDateTime.from(dteFechaAnterior)
                        tiempo = tempDateTime.until(dteHoy, escala)
                } catch (ex: Exception) {
                      ErrorMgr.guardar(TAG, "tiempoTranscurrido: ", ex.message)
                }
                return tiempo
        }

        fun isNumeric(toCheck: String): Boolean {
                val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
                return toCheck.matches(regex)
        }

        fun espera(msg:String,ms:Long){
                try{
                        Log.msg(TAG,"[espera] $msg")
                        for (i in 1..4){
                                Thread.sleep(ms)
                        }
                }catch (ex: java.lang.Exception){
                        ErrorMgr.guardar(TAG, "espera", ex.message)
                }
                catch (e:InterruptedException){
                        ErrorMgr.guardar(TAG,"espera[Thread]",e.message)
                }
        }
}