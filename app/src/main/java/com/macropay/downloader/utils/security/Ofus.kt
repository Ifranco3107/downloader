package com.macropay.downloader.utils.security

import android.content.Context
import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.ErrorMgr.guardar
import com.macropay.utils.Fechas.getTodayUTC
import com.macropay.utils.phone.DeviceCfg.getImei
import com.macropay.utils.Fechas.getFormatDateUTC
import android.os.Build
import com.loopj.android.http.Base64
import com.macropay.downloader.data.preferences.MainApp
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

class Ofus(context: Context?) {
    var context: Context? = null
    var TAG = "Ofus"

    init {
        this.context = context
    }

    fun getToken(hora: String): String {
        var hora = hora
        if (hora.isEmpty()) hora = formatHour

        // Log.msg(TAG,"[getToken] hora: "+hora);
        val deviceID = getString(hora)
        msg(TAG, "[getToken] deviceID: $deviceID")
        var strCheckSum = checksum(deviceID)
        strCheckSum = margeHora(strCheckSum, hora)
        msg(TAG, "[getToken] strCheckSum: $strCheckSum hora: $hora")
        return strCheckSum
    }

    fun validar(token: String): Boolean {
        msg(TAG, "[validar] token capturado:$token")
        var bResult = false
        val hora = extractHora(token)
        msg(TAG, "[validar] hora:$hora")
        val tokenCalculated = getToken(hora)
        msg(TAG, "tokenCalculated:$tokenCalculated")
        bResult = tokenCalculated == token
        return bResult
    }

    private fun margeHora(token: String, hora: String): String {
        var newToken = ""
        // Log.msg(TAG,"[margeHora] token: "+token + " hora: " +hora);
        try {
            for (i in 0 until token.length - 1) {
                if (i <= hora.length) {
                    val digito = hora.substring(i, i + 1)
                    //            Log.msg(TAG, "[margeHora] " +i +".- digito: " +digito );
                    var digHora = digito.toInt()
                    //Log.msg(TAG, "[margeHora] " +i +".- digHora: " +digHora );
                    if (digHora > 0) digHora = 10 - digHora
                    newToken += digHora.toString()
                }
                newToken += token.substring(i, i + 1)
            }
            //  Log.msg(TAG,"[margeHora] newToken: "+ newToken );
        } catch (ex: Exception) {
            guardar(TAG, "margeHora", ex.message)
        }
        return newToken
    }

    private fun extractHora(token: String): String {
        //  Log.msg(TAG,"[extractHora] token: "+token );
        //  Log.msg(TAG,"[extractHora] len: "+token.length());
        var hora = ""
        val len = token.length
        try {
            var i = 0
            while (i < len) {
                val digito = token.substring(i, i + 1)
                msg(TAG, "[extractHora] $i.- $digito")
                var digHora = digito.toInt()
                if (digHora > 0) digHora = 10 - digHora
                hora += digHora.toString()
                i += 2
            }
            msg(TAG, "[extractHora] hora: $hora")
        } catch (ex: Exception) {
            guardar(TAG, "extractHora", ex.message)
        }
        return hora
    }

    fun checksum(txt: String?): String {
        var md: MessageDigest? = null
        var strChecksum = ""
        var token: Long = 0
        try {
            md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(txt!!.toByteArray())
            strChecksum = Base64.encodeToString(digest, Base64.DEFAULT)
            msg(TAG, "[checksum] strChecksum: " + strChecksum + " len: " + strChecksum.length)
            token = 0
            for (i in 0 until strChecksum.length - 1) {
                token += strChecksum[i].code.toLong() * (i + 1)
                //   Log.msg(TAG,"charAt "+i +".- " + strChecksum.charAt(i)  +" charAt= " +((long) strChecksum.charAt(i) )  +" (i+1) = "+(i+1)  +" == "+token );
            }
            //     Log.msg(TAG,"token: "+token);
        } catch (e: NoSuchAlgorithmException) {
            guardar(TAG, "checksum", e.message)
        }
        return java.lang.Long.toString(token)
    }

    /*private String getString(String hora){
        LocalDateTime curUTCDate = Fechas.INSTANCE.getTodayUTC();
        String strId = DeviceCfg.getImei(this.context);
        strId += Build.MANUFACTURER.toLowerCase(Locale.ROOT); //marca
        strId += LocalDateTime.now().atOffset(ZoneOffset.UTC).getDayOfMonth();
        strId += Build.MODEL.toLowerCase(Locale.ROOT);// modelo
        strId += LocalDateTime.now().atOffset(ZoneOffset.UTC).getDayOfWeek();
        strId += Build.VERSION.RELEASE;//os_version
        strId += LocalDateTime.now().atOffset(ZoneOffset.UTC).getMonth();
        strId += MainApp.getVersion(this.context); //dpc_version
        strId += LocalDateTime.now().atOffset(ZoneOffset.UTC).getYear();
        strId += MainApp.getVersionName(this.context) ; //dpc_version_name
        strId += getFormatDate();
        strId += hora;
        return strId;
    }*/
    private fun getString(hora: String): String? {
        val curUTCDate = getTodayUTC()
        msg(TAG, "[getString] curUTCDate: $curUTCDate")
        var strId: String? = getImei(context!!)
        strId += Build.MANUFACTURER.lowercase() //marca
        strId += curUTCDate.dayOfMonth
        strId += Build.MODEL.lowercase() // modelo
        strId += curUTCDate.dayOfWeek
        strId += Build.VERSION.RELEASE //os_version
        strId += curUTCDate.month
        strId += MainApp.getVersion(context) //dpc_version
        strId += curUTCDate.year
        strId += MainApp.getVersionName(context) //dpc_version_name
        strId += getFormatDateUTC() //getFormatDate();
        strId += hora
        return strId
    }

    val formatDate: String
        get() {
            val date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            return formatter.format(date)
        }

    //Formta en 24 horas.
    //    SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
    val formatHour: String
        get() {
            val date = Date()
            //Formta en 24 horas.
            //    SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
            val formatter = SimpleDateFormat("mmss")
            return formatter.format(date)
        }
}