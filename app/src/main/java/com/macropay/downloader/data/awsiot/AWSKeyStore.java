package com.macropay.downloader.data.awsiot;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.iot.AWSIotCertificateException;
import com.amazonaws.util.Base64;
import com.amazonaws.util.StringUtils;
import com.macropay.downloader.BuildConfig;
import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;


public final class AWSKeyStore {
    private  static final  String TAG = "AWSKeyStore";
    /** Constant for PEM string that begins a cert. */
    private static final String AWS_IOT_PEM_BEGIN_CERT_TAG = "-----BEGIN CERTIFICATE-----";
    /** Constant for PEM string that ends a cert. */
    private static final String AWS_IOT_PEM_END_CERT_TAG = "-----END CERTIFICATE-----";
    /** Constant for number of bits in key. */
    private static final Integer KEY_LENGTH_BITS = 2048;
    /** Constant for internal IoT SDK KeyStore password. */
    public static final String AWS_IOT_INTERNAL_KEYSTORE_PASSWORD = BuildConfig.awks; // "awsiotkeystorepassword";

    private AWSKeyStore() {
    }

    public static KeyPair generatePrivateAndPublicKeys() {
        KeyPairGenerator keyGen;

        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            ErrorMgr.INSTANCE.guardar(TAG,"KeyPair",e.getMessage());
            throw new AWSIotCertificateException("Error creating keypair generator.", e);
        }

        keyGen.initialize(KEY_LENGTH_BITS, new SecureRandom());
        return keyGen.generateKeyPair();
    }

    public static void saveCertificateAndPrivateKey(String certId,
                                                    String certPem,
                                                    PrivateKey privKey,
                                                    String keystorePath,
                                                    String keystoreName,
                                                    String keystorePassword) {

//Log.msg(TAG,"[saveCertificateAndPrivateKey] -1-");

     //   Log.msg(TAG,"[saveCertificateAndPrivateKey] -2-");
        byte[][] certBytes = parseDERFromPEM(certPem, AWS_IOT_PEM_BEGIN_CERT_TAG,
                AWS_IOT_PEM_END_CERT_TAG);
        int ln = 0;

        try {

            X509Certificate[] xCerts = new X509Certificate[certBytes.length];

            for (int i = 0; i < certBytes.length; i++) {
                xCerts[i] = generateCertificateFromDER(certBytes[i]);
            }
            ln = 1;
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            File keystoreFile = new File(keystorePath, keystoreName);
            if (keystoreFile.exists()) {
             //   Log.msg(TAG,"Borro el archivo: " +keystoreFile.getAbsolutePath());
                keystoreFile.delete();
            }
            // try to create keystore file
            createKeystore(keystorePath, keystoreName, keystorePassword);

            ln = 2;
            FileInputStream fis = new FileInputStream(keystoreFile);
            keystore.load(fis, keystorePassword.toCharArray());
            fis.close();
            ln = 3;
            keystore.setCertificateEntry(certId, xCerts[0]);
            keystore.setKeyEntry(certId,
                    privKey,
                    keystorePassword.toCharArray(),
                    xCerts); // we store the chain
            ln = 4;
            String keystoreFileAndPath;

            if (keystorePath.endsWith("/")) {
                keystoreFileAndPath = keystorePath + keystoreName;
            } else {
                keystoreFileAndPath = keystorePath + "/" + keystoreName;
            }
            ln = 5;
            FileOutputStream fos = new FileOutputStream(keystoreFileAndPath);
            keystore.store(fos, keystorePassword.toCharArray());
            fos.close();
         //   Log.msg(TAG,"CREO el archivo: " +keystoreFile.getAbsolutePath());
            ln = 6;
        } catch (IOException e) {
            ErrorMgr.INSTANCE.guardar(TAG,"[IOException] saveCertificateAndPrivateKey",e.getMessage());
            throw new AmazonClientException("Error saving certificate and key.["+ln+"]", e);
        } catch (CertificateException e) {
            ErrorMgr.INSTANCE.guardar(TAG,"[CertificateException] saveCertificateAndPrivateKey",e.getMessage());
            throw new AWSIotCertificateException("Error saving certificate and key.["+ln+"]", e);
        } catch (NoSuchAlgorithmException e) {
            ErrorMgr.INSTANCE.guardar(TAG,"[NoSuchAlgorithmException] saveCertificateAndPrivateKey",e.getMessage());
            throw new AWSIotCertificateException("Error saving certificate and key.["+ln+"]", e);
        } catch (KeyStoreException e) {
            ErrorMgr.INSTANCE.guardar(TAG,"[KeyStoreException] saveCertificateAndPrivateKey",e.getMessage());
            throw new AWSIotCertificateException("Error saving certificate and key.["+ln+"]", e);
        }
    }


    public static void saveCertificateAndPrivateKey(String certId,
                                                    String certPem,
                                                    String keyPem,
                                                    String keystorePath, String keystoreName, String keystorePassword) {

     //   Log.msg(TAG,"[saveCertificateAndPrivateKey] -1-");
        PrivateKeyReader privateKeyReader = new PrivateKeyReader(keyPem);
        PrivateKey privateKey;

        try {
            privateKey = privateKeyReader.getPrivateKey(keyPem);

        } catch (IOException e) {
            ErrorMgr.INSTANCE.guardar(TAG,"saveCertificateAndPrivateKey",e.getMessage());
            throw new AmazonClientException("An error occurred saving the certificate and key.", e);
        } catch (InvalidKeySpecException e) {
            ErrorMgr.INSTANCE.guardar(TAG,"[InvalidKeySpecException] saveCertificateAndPrivateKey",e.getMessage());
            throw new AWSIotCertificateException(
                    "An error occurred saving the certificate and key.", e);
        }

        //Log.msg(TAG,"certPem: \n" +certPem);
        saveCertificateAndPrivateKey(certId, certPem, privateKey, keystorePath, keystoreName,
                keystorePassword);
    }

    /**
     * Get certificate and private key from keystore on the file system.
     * Retrieves the certificate and private key from the filesystem keystore
     * and creates a temporary in-memory keystore to be used when connecting to
     * service.
     *
     * @param certId The certificate Id or alias.
     * @param keystorePath The path to keystore.
     * @param keystoreName The keystore filename.
     * @param keyStorePassword The password for the keystore.
     * @return KeyStore with private and public keys and certificate.
     */
    public static KeyStore getIotKeystore(String certId, String keystorePath, String keystoreName,
                                          String keyStorePassword) {
//Log.msg(TAG,"[getIotKeystore] -1-");

        String keystoreFileAndPath;

        if (keystorePath.endsWith("/")) {
            keystoreFileAndPath = keystorePath + keystoreName;
        } else {
            keystoreFileAndPath = keystorePath + "/" + keystoreName;
        }
   //     Log.msg(TAG,"[getIotKeystore] -2- keystoreFileAndPath: "+keystoreFileAndPath );
        int ln = 0;
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream fis = new FileInputStream(keystoreFileAndPath);
            ln = 1;
            keyStore.load(fis, keyStorePassword.toCharArray());
            fis.close();
            ln = 2;
            return getTempKeystore(keyStore, certId, keyStorePassword);

        } catch (CertificateException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key. ["+ln+"]", e);
        } catch (NoSuchAlgorithmException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.["+ln+"]", e);
        } catch (KeyStoreException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.["+ln+"]", e);
        } catch (IOException e) {
            throw new AmazonClientException("Error retrieving certificate and key.["+ln+"]", e);
        }
    }

    /**
     * Get certificate and private key from stream.
     *
     * @param certId The certificate Id or alias.
     * @param keyStoreInputStream an InputStream of a Keystore.
     * @param keyStorePassword The password for the keystore.
     * @return KeyStore with with private key and certificate.
     */
    public static KeyStore getIotKeystore(String certId, InputStream keyStoreInputStream,
                                          String keyStorePassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());
            return getTempKeystore(keyStore, certId, keyStorePassword);
        } catch (CertificateException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (KeyStoreException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (IOException e) {
            throw new AmazonClientException("Error retrieving certificate and key.", e);
        }
    }

    /**
     * Create an in-memory keystore from the keystore on the filesystem.
     *
     * @param customerKeystore the keystore provided by the customer.
     * @param certId the certificate / key aliases in the keystore.
     * @param customerKeystorePassword the password for the keystore.
     * @return a temporary keystore with the certificate and key aliases and
     *         password normalized for IoTSslHelper.
     */
    private static KeyStore getTempKeystore(KeyStore customerKeystore, String certId,
                                            String customerKeystorePassword) {
        try {
            KeyStore tempKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
            tempKeystore.load(null);

            Certificate[] certs = customerKeystore.getCertificateChain(certId);
            X509Certificate[] xcerts = new X509Certificate[certs.length];

            for (int i = 0; i < certs.length; i++) {
                xcerts[i] = (X509Certificate) certs[i];
            }

            tempKeystore.setCertificateEntry("cert-alias", xcerts[0]);
            Key key = customerKeystore.getKey(certId, customerKeystorePassword.toCharArray());
            tempKeystore.setKeyEntry("key-alias", key, AWS_IOT_INTERNAL_KEYSTORE_PASSWORD.toCharArray(), xcerts);

            return tempKeystore;

        } catch (CertificateException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (KeyStoreException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (UnrecoverableKeyException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (IOException e) {
            throw new AmazonClientException("Error retrieving certificate and key.", e);
        }
    }

    /**
     * Check if a keystore is present.
     *
     * @param keystorePath The path to keystore.
     * @param keystoreName The keystore filename.
     * @return presence of keystore.
     */
    public static Boolean isKeystorePresent(String keystorePath, String keystoreName) {
        File keystoreFile = new File(keystorePath, keystoreName);
        return keystoreFile.exists();
    }

    /**
     * Check if a cert/key alias is present in a keystore.
     *
     * @param certId The certificate Id or alias.
     * @param keystorePath The path to keystore.
     * @param keystoreName The keystore filename.
     * @param keystorePassword The Password for the keystore.
     * @return presence of cert/key alias in keystore.
     */
    public static Boolean keystoreContainsAlias(String certId, String keystorePath,
                                                String keystoreName, String keystorePassword) {

        Boolean containsAlias = false;

        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream fis = new FileInputStream(keystorePath + "/" + keystoreName);
            keystore.load(fis, keystorePassword.toCharArray());
            if (keystore.containsAlias(certId)) {
                containsAlias = true;
            }
            fis.close();

            return containsAlias;

        } catch (CertificateException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (KeyStoreException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (IOException e) {
            throw new AmazonClientException("Error retrieving certificate and key.", e);
        }
    }

    /**
     * Delete an certificate/private key entry from a keystore.
     *
     * @param certId The certificate Id or alias.
     * @param keystorePath The path to keystore.
     * @param keystoreName The keystore filename.
     * @param keystorePassword The Password for the keystore.
     */
    public static void deleteKeystoreAlias(String certId, String keystorePath, String keystoreName,
                                           String keystorePassword) {

        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream fis = new FileInputStream(keystorePath + "/" + keystoreName);
            keystore.load(fis, keystorePassword.toCharArray());
            fis.close();

            keystore.deleteEntry(certId);
            FileOutputStream fos = new FileOutputStream(keystorePath + "/" + keystoreName);
            keystore.store(fos, keystorePassword.toCharArray());

        } catch (CertificateException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (KeyStoreException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new AWSIotCertificateException("Error retrieving certificate and key.", e);
        } catch (IOException e) {
            throw new AmazonClientException("Error retrieving certificate and key.", e);
        }
    }

    /**
     * Parse a DER byte array from the contents of a PEM string.
     *
     * @param data string containing PEM encoded certificate.
     * @param beginDelimiter beginning delimiter of PEM (ala ----BEGIN ...).
     * @param endDelimiter beginning delimiter of PEM (ala ----END ...).
     * @return byte array containing certificate data parsed from PEM.
     */
    static byte[][] parseDERFromPEM(String data, String beginDelimiter, String endDelimiter) {
        String[] tokens = data.split(beginDelimiter);
        List<String> newTokens = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
            newTokens.add(tokens[i].split(endDelimiter)[0]);
        }

        byte[][] ders = new byte[newTokens.size()][];
        for (int i = 0; i < newTokens.size(); i++) {
            ders[i] = Base64.decode(newTokens.get(i));
        }
        return ders;
    }

    /**
     * Parse an X509 certificate from the DER bytes.
     *
     * @param certBytes certificate bytes in DER format.
     * @return X509 certificate parsed from bytes.
     * @throws CertificateException if certificate cannot be created from bytes.
     */
    static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    /**
     * Create a blank keystore on the filesystem in the requested path. Used to
     * write an initial keystore if file is not present.
     *
     * @param keystorePath path to keystore.
     * @param keystoreName name of keystore.
     * @param keystorePassword password for the keystore.
     * @throws KeyStoreException if keystore cannot be created.
     * @throws CertificateException if certificate cannot be stored.
     * @throws NoSuchAlgorithmException if key algorithm is not present.
     * @throws IOException if keystore file cannot be accessed.
     */
    static void createKeystore(String keystorePath, String keystoreName, String keystorePassword)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        if (keystorePath == null) {
            throw new IllegalArgumentException("keystorePath is null");
        }

        if (keystoreName == null) {
            throw new IllegalArgumentException("keystoreName is null");
        }

        if (keystorePassword == null) {
            throw new IllegalArgumentException("keystorePassword is null");
        }

        String keystoreFileAndPath;

        if (keystorePath.endsWith("/")) {
            keystoreFileAndPath = keystorePath + keystoreName;
        } else {
            keystoreFileAndPath = keystorePath + "/" + keystoreName;
        }
  //      Log.msg(TAG,"[1] keystoreFileAndPath: "+keystoreFileAndPath);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] password = keystorePassword.toCharArray();
        keyStore.load(null, password);
//Log.msg(TAG,"[2] keystoreFileAndPath: "+keystoreFileAndPath);
        FileOutputStream fos = new FileOutputStream(keystoreFileAndPath);
        keyStore.store(fos, password);
        fos.close();
    }
}

class PrivateKeyReader {
String TAG = "PrivateKeyReader";
    /**
     * PEM string to be parsed for a key.
     */
    private final String keyPemString;

    /**
     * Create a PEM private key reader.
     *
     * @param keyPemString The private key in PEM format.
     */
    public PrivateKeyReader(String keyPemString) {
        this.keyPemString = keyPemString;
    }

    public PrivateKey getPrivateKey(String keyPemString) throws IOException, InvalidKeySpecException {
      // Log.msg(TAG,"getPrivateKey: \n"+ keyPemString +"\n\n");
       // Log.msg(TAG,"[getPrivateKey] -1-");
        InputStream  keyInputStream = new ByteArrayInputStream(keyPemString.getBytes(StringUtils.UTF8));
       // Log.msg(TAG,"[getPrivateKey] -2-");
        if(keyInputStream == null)
            Log.INSTANCE.msg(TAG,"keyInputStream == null");
        return PEM.readPrivateKey(keyInputStream);
    }
}
