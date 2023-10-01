/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.macropay.downloader.data.awsiot;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.RawRes;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.internal.keyvaluestore.KeyNotFoundException;
import com.macropay.downloader.BuildConfig;
import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;


public abstract class AWSUtils {
    protected static final String TEST_CONFIGURATION_FILENAME = "testconfiguration.json";

   private static Context appContext;

    public static void setContext(Context context){
        appContext= context;
    }
    public static Context getAppContext(){
        return appContext;
    }
    public static final class JSONConfiguration {
        private JSONObject mJSONObject;

        private JSONConfiguration(JSONObject mJSONObject) {
            this.mJSONObject = mJSONObject ;
        }

        public JSONObject getPackageConfigure(String packageName) {
            try {

                JSONObject packagesJson = mJSONObject.getJSONObject("packages");
                if(packagesJson== null){
                    Log.INSTANCE.msg(TAG,"NO LEYO EL JSON <packages>");

                }
                Log.INSTANCE.msg(TAG,"packageName: ["+packageName+"] --> ");
                return   packagesJson.getJSONObject(packageName);


            }
            catch (JSONException | NullPointerException configurationFileError) {
                throw new RuntimeException(
                    "Failed to get configuration for package = " + packageName + " from  " +
                        TEST_CONFIGURATION_FILENAME + ".", configurationFileError
                );
            }
        }

        String getAccessKey() throws KeyNotFoundException {
            return extractStringByPath("credentials.accessKey");
        }

        String getSecretKey() throws KeyNotFoundException {
            return extractStringByPath("credentials.secretKey");
        }

        String getSessionToken() throws KeyNotFoundException {
            return extractStringByPath("credentials.sessionToken");
        }

        String getAccountId() throws KeyNotFoundException {
            return extractStringByPath("credentials.accountId");
        }

        private String extractStringByPath(String path) throws KeyNotFoundException {
            Log.INSTANCE.msg(TAG,"path:"+path);
            return extractStringByPath(mJSONObject, path);
        }

        // This is a poor man's implementation of JSONPath, that just handles literals,
        // with the '.' meaning "down one more level." This will break if your key contains a period.
        private String extractStringByPath(JSONObject container, String path) throws KeyNotFoundException {
            int indexOfFirstPeriod = path.indexOf(".");
            if (indexOfFirstPeriod != -1) {
                String firstPortion = path.substring(0, indexOfFirstPeriod);
                String rest = path.substring(indexOfFirstPeriod + 1);

                try {
                    return extractStringByPath(container.getJSONObject(firstPortion), rest);
                } catch(JSONException e) {
                    throw new KeyNotFoundException("could not find " + path);
                }
            }
            try {
                return container.getString(path);
            } catch (JSONException jsonException) {
                throw new RuntimeException(
                    "Failed to get key " + path + " from " + TEST_CONFIGURATION_FILENAME +
                        ", please check that it is correctly formed.", jsonException
                );
            }
        }
    }

    /**
     * An implementation of AWSCredentialProvider that fetches the credentials
     * from test configuration json file.
     */
    final static class JSONCredentialProvider implements AWSCredentialsProvider {
        @Override
        public AWSCredentials getCredentials() {
            try {
                return new BasicSessionCredentials(getAccessKey(), getSecretKey(), getSessionToken());
            } catch (KeyNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void refresh() {
        }
    }

    public static final String TAG = "AWSUtils";
    /** Shared AWS credentials, loaded from a properties file */
    public static AWSCredentials credentials;
    private static JSONConfiguration mJSONConfiguration;

    public static JSONConfiguration getJSONConfiguration() {
        if (mJSONConfiguration != null) {
            return mJSONConfiguration;
        }
       // Log.msg(TAG,"getJSONConfiguration");
        int ln = 1;
        try {
            int periodIndex = TEST_CONFIGURATION_FILENAME.indexOf(".");
            Log.INSTANCE.msg(TAG,"TEST_CONFIGURATION_FILENAME: "+TEST_CONFIGURATION_FILENAME);
            Log.INSTANCE.msg(TAG,"periodIndex");
            ln = 2;
            String resourceName = TEST_CONFIGURATION_FILENAME.substring(0, periodIndex);

            ln = 3;
            String fileContents = readRawResourceContents(resourceName);
            ln = 4;
            mJSONConfiguration  = new JSONConfiguration(new JSONObject(fileContents));
            ln = 5;
            return mJSONConfiguration;
        } catch (JSONException configurationFileError) {
            ErrorMgr.INSTANCE.guardar(TAG,"getJSONConfiguration ["+ln+"]",configurationFileError.getMessage());
            throw new RuntimeException(
                "Failed to read " + TEST_CONFIGURATION_FILENAME + " please check that it is correctly formed.",
                configurationFileError
            );
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static String readRawResourceContents(  String rawResourceName) {
//Log.msg(TAG,"[readRawResourceContents] rawResourceName: "+rawResourceName);


        Resources resources = appContext.getResources();
        String packageName = appContext.getPackageName();

        Log.INSTANCE.msg(TAG,"[readRawResourceContents] packageName: "+packageName);

        @RawRes int resourceId = resources.getIdentifier(rawResourceName, "raw", packageName);
        Log.INSTANCE.msg(TAG,"[readRawResourceContents] resourceId: "+resourceId);
        InputStream inputStream = resources.openRawResource(resourceId);

        Scanner in = new Scanner(inputStream);
        StringBuilder sb = new StringBuilder();

        while (in.hasNextLine()) {
            sb.append(in.nextLine());
        }
        in.close();
        return sb.toString();
    }

    public static String getAccessKey() throws KeyNotFoundException {
        return getJSONConfiguration().getAccessKey();
    }

    public static String getSecretKey() throws KeyNotFoundException {
        return getJSONConfiguration().getSecretKey();
    }

    public static String getSessionToken() throws KeyNotFoundException {
        return getJSONConfiguration().getSessionToken();
    }

    public static String getAccountId() throws KeyNotFoundException {
        return getJSONConfiguration().getAccountId();
    }

    public static JSONObject  getPackageConfigure(String packageName) {
        Log.INSTANCE.msg(TAG,"[getPackageConfigure] packageName: "+packageName);

        JSONObject configuration = getJSONConfiguration().getPackageConfigure(packageName);

        if(configuration == null)
            Log.INSTANCE.msg(TAG,"No configuration for package \" + packageName + . Did you include a  +\n" +
                    "                tesconfiguration.json with the test package?");
        else
            Log.INSTANCE.msg(TAG,"[getPackageConfigure] configuration: "+configuration.toString());
        return configuration;
    }

    public static void setUpCredentials() {
        if (credentials == null) {
            AWSCredentialsProvider provider = new JSONCredentialProvider();
            AWSCredentialsProviderChain chain = new AWSCredentialsProviderChain(provider);
            credentials = chain.getCredentials();
        }
    }

    /**
     * Asserts that the specified String is not null and not empty.
     *
     * @param s The String to test.
     */
/*    protected void assertNotEmpty(String s) {
        assertNotNull(s);
        assertTrue(s.length() > 0);
    }*/

    /**
     * Asserts that the contents in the specified file are exactly equal to the
     * contents read from the specified input stream. The input stream will be
     * closed at the end of this method. If any problems are encountered, or the
     * stream's contents don't match up exactly with the file's contents, then
     * this method will fail the current test.
     *
     * @param expectedFile The file containing the expected contents.
     * @param inputStream The stream that will be read, compared to the expected
     *            file contents, and finally closed.
     */
    protected void assertFileEqualsStream(File expectedFile, InputStream inputStream) {
        try {
            InputStream expectedInputStream = new FileInputStream(expectedFile);
            assertStreamEqualsStream(expectedInputStream, inputStream);
        } catch (FileNotFoundException e) {

            ErrorMgr.INSTANCE.guardar(TAG,"Expected file doesn't exist: " , e.getMessage());
        }
    }

    protected void assertStreamEqualsStream(InputStream expectedInputStream, InputStream inputStream) {
        try {
         if(doesStreamEqualStream(expectedInputStream, inputStream)){
             Log.INSTANCE.msg(TAG,"doesStreamEqualStream == true");
         }
        } catch (IOException e) {
            e.printStackTrace();
            ErrorMgr.INSTANCE.guardar(TAG,"assertStreamEqualsStream" , e.getMessage());
        }
    }

    /*protected void assertFileEqualsFile(File expectedFile, File file) {
        if (expectedFile.exists() == false)
            fail("Expected file doesn't exist");
        if (file.exists() == false)
            fail("Testing file doesn't exist");

        Assert.assertEquals(expectedFile.length(), file.length());

        try {
            FileInputStream expectedInputStream = new FileInputStream(expectedFile);
            FileInputStream testedInputStream = new FileInputStream(file);

            assertStreamEqualsStream(expectedInputStream, testedInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to compare files: " + e.getMessage());
        }
    }*/

    /**
     * Asserts that the contents in the specified string are exactly equal to
     * the contents read from the specified input stream. The input stream will
     * be closed at the end of this method. If any problems are encountered, or
     * the stream's contents don't match up exactly with the string's contents,
     * then this method will fail the current test.
     *
     * @param expectedString The string containing the expected data.
     * @param inputStream The stream that will be read, compared to the expected
     *            string data, and finally closed.
     */
/*    protected void assertStringEqualsStream(String expectedString, InputStream inputStream) {
        try {
            InputStream expectedInputStream = new ByteArrayInputStream(
                    expectedString.getBytes(StringUtils.UTF8));
            assertTrue(doesStreamEqualStream(expectedInputStream, inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error reading from stream: " + e.getMessage());
        }
    }*/

    /**
     * Returns true if, and only if, the contents read from the specified input
     * streams are exactly equal. Both input streams will be closed at the end
     * of this method.
     *
     * @param expectedInputStream The input stream containing the expected
     *            contents.
     * @return True if the two input streams contain the same data.
     * @throws IOException If any problems are encountered comparing the file
     *             and stream.
     */
    protected boolean doesStreamEqualStream(InputStream expectedInputStream,
            InputStream actualInputStream) throws IOException {
        byte[] expectedDigest = null;
        byte[] actualDigest = null;

        try {
            expectedDigest = calculateMD5Digest(expectedInputStream);
            actualDigest = calculateMD5Digest(actualInputStream);
            //Log.msg(TAG,"[doesStreamEqualStream] - version 1.-");
            //Log.msg(TAG,"[doesStreamEqualStream] - MessageDigest: "+MessageDigest.isEqual(expectedDigest, actualDigest));
          //  Log.msg(TAG,"[doesStreamEqualStream] - Arrays: "+Arrays.equals(expectedDigest, actualDigest));
            return  MessageDigest.isEqual(expectedDigest, actualDigest);
        //    return Arrays.equals(expectedDigest, actualDigest);
        } catch (NoSuchAlgorithmException nse) {
            throw new AmazonClientException(nse.getMessage(), nse);
        } finally {
            try {
                expectedInputStream.close();
            } catch (Exception e) {
            }
            try {
                actualInputStream.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Calculates the MD5 digest for the given input stream and returns it.
     */
    private byte[] calculateMD5Digest(InputStream is) throws NoSuchAlgorithmException, IOException {
        int bytesRead = 0;
        byte[] buffer = new byte[2048];
        String digest =BuildConfig.msgd; //MD5
        MessageDigest md5 = MessageDigest.getInstance(digest);

        while ((bytesRead = is.read(buffer)) != -1) {
            md5.update(buffer, 0, bytesRead);
        }
        return md5.digest();
    }

    protected byte[] drainInputStream(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            byte[] buffer = new byte[1024];
            long bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, (int) bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (Exception e) {
            }
        }
    }


}
