package com.macropay.data.usecases;

import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptJava {

  /*
  //Se comento por Snyk
  public static void tests() {
        String encryptedText = "c3ca11c6985141343efe9ca0ab9eb0d6c8b14955f3b4f9387155e40deb2a4e35";
        String iv = "78048923cd6a93055ca2688e5cc2d713";
        String keyStr = "y/+LluT1V5FneAuAVf+5wpcySHyreDGNfzQkXetHg28=";
        try {
            // Decode the key from base64
            byte[] decodedKey = Base64.getDecoder().decode(keyStr);
            // Create the key from the decoded bytes
            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            // Convert the IV to bytes
            byte[] ivBytes = hexStringToByteArray(iv);
            // Create the cipher and decrypt the message
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));
            byte[] decryptedBytes = cipher.doFinal(hexStringToByteArray(encryptedText));
            String decryptedText = new String(decryptedBytes, "UTF-8");
            System.out.println("--------------");
            System.out.println("java: decryptedText: "+decryptedText);
            System.out.println("+++++++++++++++++++");
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar("EncryptJava","test",ex.getMessage());
        }
    }
*/

    private static byte[] hexStringToByteArray(String s) {
        Log.INSTANCE.msg("java","String: "+ s );
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
          /*  if (i < 5) {


            Log.INSTANCE.msg("java",i+" .- "+ data[i / 2] );
            int digit1 = Character.digit(s.charAt(i), 16);
            int digit2 = Character.digit(s.charAt(i+1), 16);
            Log.INSTANCE.msg("java","digit1: "+digit1  +" --> " +s.charAt(i));
            Log.INSTANCE.msg("java","digit2: "+digit2  +" --> " +s.charAt(i+1));
            var val1 = (digit1 << 4);
            Log.INSTANCE.msg("java","val1: "+val1);
            Log.INSTANCE.msg("java","val2: "+(byte) (val1 +digit2));
            }*/
        }
/*        for (int i = 0; i < data.length; i ++) {
            Log.INSTANCE.msg("java ",i+" - "+data[i]);
        }*/
        return data;
    }

}
