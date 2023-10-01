package com.macropay.utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Fechas {
    fun getTodayLocal(): String {
        val date = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
     //   formatter.timeZone = TimeZone.getTimeZone("UTC")
       // Log.msg("Fechas","fecha: $formatter.format(date)")
        return formatter.format(date)
    }

    fun getToday():String{
        val milisegundos_por_minuto =60_000
        var endPeriod = LocalDateTime.now()
       val tz = TimeZone.getDefault()
     //   val tz = TimeZone.getTimeZone("UTC")

      //  val mins :Long = (tz.getRawOffset() /milisegundos_por_minuto).toLong()
        val mins :Long = ((tz.getRawOffset() +tz.dstSavings) /milisegundos_por_minuto).toLong()
       // Log.msg("Fechas","[getUTCDate] mins: $mins getOffset: "+tz.getRawOffset())

        endPeriod = endPeriod.plusMinutes(mins*-1)
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime: String = endPeriod.format(dateTimeFormatter)
      //  Log.msg("Fechas","[getUTCDate] formattedDateTime: "+formattedDateTime)
        return formattedDateTime
    }
    fun getTodayUTC():LocalDateTime{
        val milisegundos_por_minuto =60_000
        var endPeriod = LocalDateTime.now()
        val tz = TimeZone.getDefault()
        //val mins :Long = (tz.getRawOffset() /milisegundos_por_minuto).toLong()
        val mins :Long = ((tz.getRawOffset() +tz.dstSavings) /milisegundos_por_minuto).toLong()
        // Log.msg("Fechas","[getUTCDate] mins: $mins getOffset: "+tz.getRawOffset())

        endPeriod = endPeriod.plusMinutes(mins*-1)
        return endPeriod
    }
    fun getFormatDateUTC(): String {
      val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return  getTodayUTC().format(dateTimeFormatter)

    }
    fun getFormatDateTimeUTC(): String {
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return  getTodayUTC().format(dateTimeFormatter)
    }
}