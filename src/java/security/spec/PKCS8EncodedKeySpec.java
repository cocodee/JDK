/*
 * @(#)PKCS8EncodedKeySpec.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.security.spec;

/**
 * This class represents the DER encoding of a private key, according to the
 * format specified in the PKCS #8 standard.
 *
 * @author Jan Luehe
 *
 * @version 1.9 98/12/03
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see EncodedKeySpec
 * @see X509EncodedKeySpec
 *
 * @since JDK1.2
 */

public class PKCS8EncodedKeySpec extends EncodedKeySpec {

    /**
     * Creates a new PKCS8EncodedKeySpec with the given encoded key.
     *
     * @param encodedKey the key, which is assumed to be
     * encoded according to the PKCS #8 standard.
     */
    public PKCS8EncodedKeySpec(byte[] encodedKey) {
	super(encodedKey);
    }

    /**
     * Returns the key bytes, encoded according to the PKCS #8 standard.
     *
     * @return the PKCS #8 encoding of the key.
     */
    public byte[] getEncoded() {
	return super.getEncoded();
    }

    /**
     * Returns the name of the encoding format associated with this
     * key specification.
     *
     * @return the string <code>"PKCS#8"</code>.
     */
    public final String getFormat() {
	return "PKCS#8";
    }
}
