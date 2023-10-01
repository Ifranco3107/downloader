package com.macropay.downloader.data.awsiot;
import com.amazonaws.util.Base64;
import com.amazonaws.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

final class PEM {

    /** Marker for beginning of PEM encoded cert/key. */
    private static final String BEGIN_MARKER = "-----BEGIN ";
    private static String  TAG = "PEM";


    private PEM() {
    }

    public static PrivateKey readPrivateKey(InputStream is)
            throws InvalidKeySpecException, IOException {
        List<PEMObject> objects = readPEMObjects(is);
       // Log.msg(TAG,"[PrivateKey] objects:" +objects.size());
        for (PEMObject object : objects) {
            switch (object.getPEMObjectType()) {
                case PRIVATE_KEY_PKCS1:{
                  //  Log.msg(TAG,"[PrivateKey] encontro: PRIVATE_KEY_PKCS1");
                    return RSA.INSTANCE.privateKeyFromPKCS1(object.getDerBytes());
                }
                default:
                    break;
            }
        }
        throw new IllegalArgumentException("Found no private key");
    }

    public static List<PEMObject> readPEMObjects(InputStream PEMStream) throws IOException {
       // Log.msg(TAG,"[readPEMObjects] -1-" );
        List<PEMObject> pemContents = new ArrayList<PEMObject>();
        boolean readingContent = false;
        String beginMarker = null;
        String endMarker = null;
        StringBuffer sb = null;
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(PEMStream, StringUtils.UTF8));
        try {
            while ((line = reader.readLine()) != null) {
                if (readingContent) {
                    if (line.indexOf(endMarker) != -1) {
                        // completed reading one PEM object
                        pemContents.add(new PEMObject(beginMarker, Base64.decode(sb.toString())));
                        readingContent = false;
                    } else {
                        sb.append(line.trim());
                    }
                } else {
                    if (line.indexOf(BEGIN_MARKER) != -1) {
                        readingContent = true;
                        beginMarker = line.trim();
                        endMarker = beginMarker.replace("BEGIN", "END");
                        sb = new StringBuffer();
                    }
                }
               // Log.msg(TAG,"[readPEMObjects] "+ line);
            }
            return pemContents;
        } finally {
            reader.close();
        }
    }
}
