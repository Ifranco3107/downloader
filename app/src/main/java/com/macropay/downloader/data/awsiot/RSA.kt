/**
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.macropay.downloader.data.awsiot

import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.RSAPrivateCrtKeySpec
import kotlin.Throws

/**
 * Utility for RSA keys.
 */
internal object RSA {
    /** String identifying key type.  */
    private const val RSA = "RSA"

    /**
     * Returns a private key constructed from the given DER bytes in PKCS#1
     * format.
     *
     * @param pkcs1 byte array containing key data.
     * @return private key parsed from key data.
     * @throws InvalidKeySpecException if PKCS#1 key spec unavailable.
     */
    @Throws(InvalidKeySpecException::class)
    fun privateKeyFromPKCS1(pkcs1: ByteArray): PrivateKey {
        return try {
            val privateKeySpec = newRSAPrivateCrtKeySpec(pkcs1)
            val keyFactory = KeyFactory.getInstance(RSA)
            keyFactory.generatePrivate(privateKeySpec)
        } catch (e: IOException) {
            throw IllegalArgumentException(e)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException(e)
        }
    }
    // Extracted from:
    // http://oauth.googlecode.com/svn/code/branches/jmeter/jmeter/src/main/java/org/apache/jmeter/protocol/oauth/sampler/PrivateKeyReader.java
    // See p.41 of
    // http://www.emc.com/emc-plus/rsa-labs/pkcs/files/h11300-wp-pkcs-1v2-2-rsa-cryptography-standard.pdf
    /****************************************************************************
     * Amazon Modifications: Copyright 2016 Amazon.com, Inc. or its affiliates.
     * All Rights Reserved. Copyright (c) 1998-2010 AOL Inc. Licensed under the
     * Apache License, Version 2.0 (the "License"); you may not use this file
     * except in compliance with the License. You may obtain a copy of the
     * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
     * applicable law or agreed to in writing, software distributed under the
     * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
     * CONDITIONS OF ANY KIND, either express or implied. See the License for
     * the specific language governing permissions and limitations under the
     * License. Convert PKCS#1 encoded private key into RSAPrivateCrtKeySpec.
     *
     *
     * The ASN.1 syntax for the private key with CRT is
     *
     * <pre>
     * --
     * -- Representation of RSA private key with information for the CRT algorithm.
     * --
     * RSAPrivateKey ::= SEQUENCE {
     * version           Version,
     * modulus           INTEGER,  -- n
     * publicExponent    INTEGER,  -- e
     * privateExponent   INTEGER,  -- d
     * prime1            INTEGER,  -- p
     * prime2            INTEGER,  -- q
     * exponent1         INTEGER,  -- d mod (p-1)
     * exponent2         INTEGER,  -- d mod (q-1)
     * coefficient       INTEGER,  -- (inverse of q) mod p
     * otherPrimeInfos   OtherPrimeInfos OPTIONAL
     * }
    </pre> *
     *
     * @param keyInPkcs1 PKCS#1 encoded key
     * @return private key as an RSA Private Key Spec.
     * @throws IOException If key data cannot be read.
     */
    @Throws(IOException::class)
    private fun newRSAPrivateCrtKeySpec(keyInPkcs1: ByteArray): RSAPrivateCrtKeySpec {
        var parser = DerParser(keyInPkcs1)
        val sequence = parser.read()
        require(sequence.type == DerParser.SEQUENCE) {
            "Invalid DER: not a sequence" //$NON-NLS-1$
        }

        // Parse inside the sequence
        parser = sequence.parser
        parser.read() // Skip version
        val modulus = parser.read().integer
        val publicExp = parser.read().integer
        val privateExp = parser.read().integer
        val prime1 = parser.read().integer
        val prime2 = parser.read().integer
        val exp1 = parser.read().integer
        val exp2 = parser.read().integer
        val crtCoef = parser.read().integer
        return RSAPrivateCrtKeySpec(
            modulus, publicExp, privateExp, prime1, prime2,
            exp1, exp2, crtCoef
        )
    }
}