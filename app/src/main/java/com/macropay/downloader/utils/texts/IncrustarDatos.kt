package com.macropay.downloader.utils.texts

import com.macropay.data.logs.ErrorMgr.guardar
import java.lang.Exception
import java.util.*

object IncrustarDatos {
    var inicioCampo = 0
    var TAG = "IncrustarDatos"
    @JvmStatic
    fun incrustrar(texto: String): String {
        var texto = texto
        var resultText = texto
        texto = "$texto "
        try {
            // Copy character by character into array
            for (i in 0 until texto.length) {
                val curChar = texto.substring(i, i + 1)
                //      System.out.println(i +".- "+curChar);
                //Si es igual que anterior
                if (curChar == "@") {
                    // System.out.println("inicio Campo: "+i);
                    inicioCampo = i
                }
                //Detecta el fin del nombre del campo.
                if (" *-\n".contains(curChar) && inicioCampo > 0) {

                    //   System.out.println("Termino..."+inicioCampo +" , " + (i-inicioCampo)); //(i-inicioCampo)-1
                    val campoName = texto.substring(inicioCampo, i)
                    // System.out.println("campoName: "+campoName);
                    resultText = resultText.replace(campoName, getValue(campoName))
                    inicioCampo = 0
                }
            }
        } catch (ex: Exception) {
            guardar(TAG, "", ex.message)
        }
        return resultText
    }

    private fun getValue(fieldName: String): String {
        // @Nombre @saldo
        var fieldName = fieldName
        var valor = ""
        try {
            fieldName = fieldName.replace("@", "")
            fieldName = fieldName.lowercase(Locale.getDefault())
            when (fieldName) {
                "nombre" -> valor = "Laura Elizabeth"
                "imei" -> valor = "412341234423"
                "saldo" -> valor = "1234,341234"
                "sucursal" -> valor = "MacroPay Plaza Fiesta"
                "clienteid" -> valor = "3412344"
            }
        } catch (ex: Exception) {
            guardar(TAG, "getValue", ex.message)
        }
        return valor
    }
}