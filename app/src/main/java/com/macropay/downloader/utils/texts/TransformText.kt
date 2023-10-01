package com.macropay.downloader.utils.texts

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import com.macropay.data.logs.ErrorMgr.guardar
import com.macropay.data.logs.Log.msg
import com.macropay.downloader.utils.texts.IncrustarDatos.incrustrar
import java.util.stream.Collectors


/*
Ejemplo de uso:
         String texto = "Hola {b}Ignacio{b} {i}Franco{i} de los angeles{lf}{tab}{B}Gisela{B} Franco --Juarez-- {LF}"+
                        "@Nombre {LF} su saldo es:{LF}{TAB}{B}$@saldo{B} {#2196F3}coloruno{#FF8943}{lf}" +
                        "pagantes antes del {lf}{lf}{#00FF00}lunes{#00FF00}{LF}" +
                        "{LINK}Politicas de privacidad{LINK}";
* */
object TransformText {
    private var sb: SpannableStringBuilder? = null
    val fcs = ForegroundColorSpan(Color.rgb(158, 158, 158))
    var inicioBoldFormat = 0
    var inicioItalicFormat = 0
    var inicioColorFormat = 0
    var inicioLinkFormat = 0
    var countCharEspeciales = 0
    var TAG = "TransformText"
    var colores: MutableMap<String?, String> = HashMap()
    private fun inicializar() {
        inicioBoldFormat = 0
        inicioItalicFormat = 0
        inicioColorFormat = 0
        inicioLinkFormat = 0
        countCharEspeciales = 0
        colores.clear()
    }
    fun underline(texto: String, textView: TextView){
        try{
            val content = SpannableString(texto)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            textView.text = content
        }catch (ex:Exception){
            guardar(TAG, "underline", ex.message)
        }
    }
    fun transform(texto: String, textView: TextView): SpannableStringBuilder? {
        var texto = texto
        inicializar()
        //Texto de control.
        texto = reemplazaCaracteresControl(texto)
        texto = reemplazaColores(texto)
        texto = incrustrar(texto)


        //Texto Limpio de caracteres de controles
        var textoNatural = texto.replace("**", "")
        textoNatural = textoNatural.replace("--", "")
        textoNatural = textoNatural.replace("##", "")
        for (keyColor in colores.keys) {
            //   Log.msg(TAG,"keyColor: "+ keyColor);
            textoNatural = textoNatural.replace(keyColor!!, "")
        }
        /*        Log.msg(TAG,"------ length texto: "+texto.length());
        Log.msg(TAG,"------------- texto: "+texto);
        Log.msg(TAG,"lenght textoNatural: "+textoNatural.length());
        Log.msg(TAG,"-------textoNatural: "+textoNatural);*/sb = SpannableStringBuilder(textoNatural)
        var charAnterior = ""
        try {
            // Copy character by character into array
            for (i in 0 until texto.length) {
                val curChar = texto.substring(i, i + 1)

                //BOLD
                if (curChar == charAnterior && charAnterior == "*") {
                    if (inicioBoldFormat == 0) {
                        countCharEspeciales += 2
                        inicioBoldFormat = i - countCharEspeciales
                    } else {
                        countCharEspeciales += 1
                        val bold = StyleSpan(Typeface.BOLD)
                        sb!!.setSpan(bold, inicioBoldFormat + 1, i - countCharEspeciales, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        inicioBoldFormat = 0
                        countCharEspeciales += 1
                    }
                }

                //ITALICA
                if (curChar == charAnterior && charAnterior == "-") {
                    if (inicioItalicFormat == 0) {
                        countCharEspeciales += 2
                        inicioItalicFormat = i - countCharEspeciales
                    } else {
                        countCharEspeciales += 1
                        val italic = StyleSpan(Typeface.ITALIC)
                        sb!!.setSpan(italic, inicioItalicFormat + 1, i - countCharEspeciales, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        inicioItalicFormat = 0
                        countCharEspeciales += 1
                    }
                }
                //COLOR
                if (curChar == charAnterior && !" -*\n".contains(curChar)) {
                    //Busca el color en colores,
                    val hexColor = colores[charAnterior + charAnterior]
                    if (hexColor != null) {
                        msg(TAG, "Color TAG: [$curChar] Aplicar ---> Encontro idColor: $hexColor")
                        if (inicioColorFormat == 0) {
                            //  Log.msg(TAG,"===============>");
                            //  Log.msg(TAG,"Inicio: [" + curChar + "]");
                            countCharEspeciales += 2
                            inicioColorFormat = i - countCharEspeciales
                        } else {
                            countCharEspeciales += 1
                            val fcs = ForegroundColorSpan(Color.parseColor(hexColor))
                            //  Log.msg(TAG,"Color Aplicando " + (inicioColorFormat + 1) + ", " + (i - countCharEspeciales));
                            //  Log.msg(TAG,"a: " + textoNatural.substring(inicioColorFormat + 1));
                            sb!!.setSpan(fcs, inicioColorFormat + 1, i - countCharEspeciales, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                            inicioColorFormat = 0
                            countCharEspeciales += 1
                        }
                    }
                }
                //Link
                if (curChar == charAnterior && charAnterior == "#") {
                    if (inicioLinkFormat == 0) {
                        // Log.msg(TAG,"===============>");
                        // Log.msg(TAG,"Inicio: [" + curChar + "]");
                        countCharEspeciales += 2
                        inicioLinkFormat = i - countCharEspeciales
                    } else {
                        // Log.msg(TAG,"<<<===============");
                        countCharEspeciales += 1
                        //Log.msg(TAG,"link Aplicando " + (inicioLinkFormat + 1) + ", " + (i - countCharEspeciales));
                        // Log.msg(TAG,"a: " + textoNatural.substring(inicioLinkFormat + 1));
                        val myActivityLauncher: ClickableSpan = object : ClickableSpan() {
                            override fun onClick(view: View) {
                                //Toast.makeText(, "texto desde el link", Toast.LENGTH_SHORT).show();
                                msg(TAG, "TEXTO DESDE EL LINK,")
                            }
                        }

                        //Link
                        //  SpannableString string = new SpannableString("Text with clickable text");
                        sb!!.setSpan(myActivityLauncher, inicioLinkFormat + 1, i - countCharEspeciales, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        inicioLinkFormat = 0
                        countCharEspeciales += 1
                    }
                }
                charAnterior = curChar
            }
        } catch (ex: Exception) {
            guardar(TAG, "transform", ex.message)
        }

        //
        textView.text = sb
        textView.linksClickable = true
        textView.movementMethod = LinkMovementMethod.getInstance()
        return sb
    }

    private fun reemplazaCaracteresControl(texto: String): String {
        var result = ""
        try {
            result = texto.replace("{LF}", "\n")
            result = result.replace("{lf}", "\n")
            result = result.replace("{TAB}", "     ")
            result = result.replace("{tab}", "     ")
            result = result.replace("{B}", "**")
            result = result.replace("{b}", "**")
            result = result.replace("{I}", "--")
            result = result.replace("{i}", "--")
            result = result.replace("{LINK}", "##")
            result = result.replace("{link}", "##")
        } catch (ex: Exception) {
            guardar(TAG, "", ex.message)
        }
        return result
    }

    //TODO
    //Reemplaza los colores
    private fun reemplazaColores(texto: String): String {
        var result = texto
        var charAnterior = ""
        try {
            // Copy character by character into array
            for (i in 0 until texto.length) {
                val ch = texto.substring(i, i + 1)
                // Log.msg(TAG,i + ".- " + ch);
                //BOLD
                if (ch == "#" && charAnterior == "{") {
                    val curColor = texto.substring(i, i + 7)
                    //   Log.msg(TAG,"color: [" + curColor+"]");
                    val idColor = addColor(curColor)
                    // Log.msg(TAG,"reemplazaColores: addColor: ["+idColor+"]");
                    if (idColor != null) result = result.replace("{$curColor}", idColor)
                }
                charAnterior = ch
            }
        } catch (ex: Exception) {
            guardar(TAG, "reemplazaColores", ex.message)
        }
        return result
    }

    private fun addColor(txtColor: String): String? {
        val iAsciiValue = colores.size + 65
        // Log.msg(TAG,"------------------------------ iAsciiValue:" +iAsciiValue);
        var idColor: String? = null //colores.get(txtColor);
        try {
            val existsColor = colores.entries.stream()
                .filter { (_, value): Map.Entry<String?, String> -> txtColor == value }
                .map { (key): Map.Entry<String?, String> -> key }
                .collect(Collectors.joining())
            if (existsColor.isEmpty()) {
                idColor = Character.toString(iAsciiValue.toChar())
                idColor += idColor
                //     Log.msg(TAG,"addColor -->idColor:" +idColor);
                colores[idColor] = txtColor
            } else {
                idColor = existsColor
                //   Log.msg(TAG,"addColor -->Ya existe- idColor: [" +idColor +"] color: ["  +txtColor +"]" );
            }
        } catch (ex: Exception) {
            guardar(TAG, "addColor", ex.message)
        }
        return idColor
    }
}