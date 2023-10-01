package com.macropay.utils


//mport android.util.Base64
import com.macropay.mylibrary.BuildConfig
import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.logs.Log
import com.macropay.utils.logs.Log.msg
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AES {
    private val TAG = "AES"
    // val algorithm = "AES/ECB/PKCS5Padding" // Electronic Codebook (ECB) Mode
    val algorithm = "AES/CBC/PKCS7Padding" //
    val phrase = BuildConfig.phmq
   //val salt = "Wr=s3ufriTrozOp!xoSpE+O@4thl_uQl"
  /*  fun decrypt(cipherText: String): String{
        var result = ""
        var ln = 0
        Log.msg(TAG,"[decrypt] =============================================")
        Log.msg(TAG,"[decrypt] phrase: "+phrase)
        Log.msg(TAG,"[decrypt] txt: "+ cipherText.length+"\n"+cipherText)
        try {
            ln =1
            val password: ByteArray = phrase.toByteArray()

            val secretKeyFactory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
            val keyspec: KeySpec = PBEKeySpec(phrase.toCharArray(), salt.toByteArray(), 65536, 256)
           val  secretKeyTemp = secretKeyFactory.generateSecret(keyspec)
            ln = 11
            val key = SecretKeySpec(secretKeyTemp.encoded, "AES")
            //val key = SecretKeySpec(phrase.toByteArray(), "AES")
            val iv = IvParameterSpec(ByteArray(16))
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, key, iv)
            ln =2
            //val plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText))
            try{
               // val encrypted: ByteArray = Base64.decode(cipherText, Base64.DEFAULT)
               // Log.msg(TAG,"decrypt] len[1]: "+encrypted.size)
            }catch (ex1:Exception){

            }

            ln =3
            val encrypted2: ByteArray? = fromHexString(cipherText)
            val encrypted3: ByteArray? = toByte(cipherText)

            ln =4

            Log.msg(TAG,"decrypt] len[2]: "+encrypted2!!.size)
            Log.msg(TAG,"decrypt] len[3]: "+encrypted3!!.size)
            ln =5
            val plainText = cipher.doFinal(encrypted2)
            val plainText3 = cipher.doFinal(encrypted3)
            Log.msg(TAG,"decrypt] plainText3: "+plainText3)
            ln =6
            result = String(plainText)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"decrypt $ln",ex.message)
        }
        return result
    }*/
   /* fun getAESDecrypt(data: String): String {
        Log.msg(TAG,"[getAESDecrypt]")
        val iv = ByteArray(16)
        val phrase = BuildConfig.phmq
        try {
            val secretKeyFactory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
            val keyspec: KeySpec = PBEKeySpec(AES.phrase.toCharArray(), salt.toByteArray(), 65536, 256)
            val  secretKeyTemp = secretKeyFactory.generateSecret(keyspec)
            val ivParameterSpec = IvParameterSpec(iv)
            val secretKey = SecretKeySpec(secretKeyTemp.encoded, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
           // val encrypted: ByteArray = Base64.decode(data, Base64.DEFAULT)
           return String(cipher.doFinal(Base64.getDecoder().decode(data)))
           // return String(cipher.doFinal(encrypted))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return "null"
    }*/

    fun fromHexString(s: String): ByteArray {
        val len: Int = s.length
        val data = ByteArray(len / 2)

        var i = 0
        try{
            while (i < len) {
                data[i / 2] = ( (Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16) ).toByte()
                i += 2
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"fromHexString [$i]",ex.localizedMessage)
        }
        return data
    }


    fun fromHexString2(s: String): ByteArray? {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0

        try{

        while (i < len) {
            data[i / 2] = ((((s[i].digitToIntOrNull(16) ?: (-1 shl 4)) + s[i + 1].digitToIntOrNull(16)!!) ?: -1)).toByte()
            i += 2
        }}catch (ex:Exception){
            ErrorMgr.guardar(TAG,"fromHexString [$i]",ex.localizedMessage)
        }
        return data
    }
    private fun toByte(hexString: String): ByteArray? {
        val len = hexString.length / 2
        Log.msg(TAG,"[toBye] len: "+len)
        val result = ByteArray(len)
        try{
            for (i in 0 until len) {
                result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).toByte()
            }
        }catch (ex:Exception) {
            ErrorMgr.guardar(TAG,"toByte",ex.message)
        }
        return result
    }


    //OK - se comento por warning de snik
   /* fun encryptOK( inputText: String): String {
        var result = ""
        try{
            val key = SecretKeySpec(phrase.toByteArray(charset("UTF-8")), "AES")
            val iv = IvParameterSpec(ByteArray(16))
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.ENCRYPT_MODE, key, iv)
            val cipherText = cipher.doFinal(inputText.toByteArray())
            result =  Base64.getEncoder().encodeToString(cipherText)
        }catch (ex:Exception) {
            ErrorMgr.guardar(TAG,"encryptOK",ex.message)
        }
        return result
    }*/
//Se comento por warning de Snyk
/*    fun decryptOK(cipherText: String): String {
        var result = ""
        try {
            val password: ByteArray = phrase.toByteArray(charset("UTF-8"))
            val key = SecretKeySpec(password, "AES")
            val iv = IvParameterSpec(ByteArray(16))
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, key, iv)
             val plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText))
             result = String(plainText)

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG,"decrypt",ex.message)
        }
        return result
    }*/
//Natalia
fun decryptPCKS5(encryptedText:String,keyStr:String,iv:String):String {
    var  decryptedText= ""
    var ln= 0
    try {
        //  val encryptedText = "c3ca11c6985141343efe9ca0ab9eb0d6c8b14955f3b4f9387155e40deb2a4e35"
        //val iv = "78048923cd6a93055ca2688e5cc2d713"
        // val keyStr = "y/+LluT1V5FneAuAVf+5wpcySHyreDGNfzQkXetHg28="
        ln = 1
        // Decode the key from base64
        val decodedKey = Base64.getDecoder().decode(keyStr)
        // Create the key from the decoded bytes
        val key: SecretKey = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        ln = 2
        // Convert the IV to bytes
        //val ivBytes = hexStringToByteArray(iv)
        val ivBytes = hexStringToByteArray(iv)
        ln = 3
        // Create the cipher and decrypt the message
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        ln = 4
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ivBytes))
        ln = 5
        val decryptedBytes = cipher.doFinal(hexStringToByteArray(encryptedText))
        ln = 6
        decryptedText = String(decryptedBytes, charset("UTF-8"))
    }catch (ex:Exception){
        ErrorMgr.guardar(TAG,"decryptPCKS5 $ln",ex.message)
    }
   // Log.msg(TAG,"decryptedText: --> [$decryptedText]")
    return decryptedText
}
 fun tests() {
//Se comento por warning de snik
     /*var  decryptedText= ""
     var ln= 0
     try {
         val encryptedText = "c3ca11c6985141343efe9ca0ab9eb0d6c8b14955f3b4f9387155e40deb2a4e35"
         val iv = "78048923cd6a93055ca2688e5cc2d713"
         val keyStr = "y/+LluT1V5FneAuAVf+5wpcySHyreDGNfzQkXetHg28="
         ln = 1
         // Decode the key from base64
         val decodedKey = Base64.getDecoder().decode(keyStr)
         // Create the key from the decoded bytes
         val key: SecretKey = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
         ln = 2
         // Convert the IV to bytes
         //val ivBytes = hexStringToByteArray(iv)
         val ivBytes = hexStringToByteArray(iv)
         ln = 3
         // Create the cipher and decrypt the message
         val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
         ln = 4
         cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ivBytes))
         ln = 5
         val decryptedBytes = cipher.doFinal(hexStringToByteArray(encryptedText))
         ln = 6
         decryptedText = String(decryptedBytes, charset("UTF-8"))
     }catch (ex:Exception){
         ErrorMgr.guardar(TAG,"tests $ln",ex.message)
     }
    Log.msg(TAG,"decryptedText: --> [$decryptedText]")*/
}

    private fun hexStringToByteArray(s: String): ByteArray {
       //Log. msg(TAG, "String: $s")
        val len = s.length
        val data = ByteArray(len / 2)
        try{
        var i = 0
        while (i < len) {
            data[i / 2] = ((s[i].digitToIntOrNull(16) ?: -1 shl 4)
            + s[i + 1].digitToIntOrNull(16)!! ?: -1).toByte()
          //  if(i<5){
          //  Log.msg(TAG,"$i .- "+ data[i / 2] + " --> "+ s[i])

            var digit1 = s[i].digitToIntOrNull(16)
            var digit2 = s[i+1].digitToIntOrNull(16)
            //Log.msg(TAG,"digit1: "+digit1  +" --> " +s[i])
            // Log.msg(TAG,"digit2: "+digit2 +" --> " +s[i+1])
            var val1 = (digit1?.shl(4))
            //Log.msg(TAG,"val1: "+val1)
            if (val1 != null) {
                data[i / 2] =(val1 + digit2!!).toByte()
                //      Log.msg(TAG,"val2: "+(val1 + digit2!!).toByte())
            }
            //}
            i += 2

        }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"",ex.message)
        }
/*        var idx=0
        data.forEach {
            idx++
            Log.msg(TAG, "$idx .- $it")
        }*/
        return data
    }


}