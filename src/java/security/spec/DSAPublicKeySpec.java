/*
 * @(#)DSAPublicKeySpec.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.security.spec;

import java.math.BigInteger;

/**
 * This class specifies a DSA public key with its associated parameters.
 *
 * @author Jan Luehe
 *
 * @version 1.11 98/12/03
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see DSAPrivateKeySpec
 * @see X509EncodedKeySpec
 *
 * @since JDK1.2
 */

public class DSAPublicKeySpec implements KeySpec {

    private BigInteger y;
    private BigInteger p;
    private BigInteger q;
    private BigInteger g;

    /**
     * Creates a new DSAPublicKeySpec with the specified parameter values.
     * 
     * @param y the public key.
     * 
     * @param p the prime.
     * 
     * @param q the sub-prime.
     * 
     * @param g the base.
     */
    public DSAPublicKeySpec(BigInteger y, BigInteger p, BigInteger q,
			    BigInteger g) {
	this.y = y;
	this.p = p;
	this.q = q;
	this.g = g;
    }

    /**
     * Returns the public key <code>y</code>.
     *
     * @return the public key <code>y</code>.
     */
    public BigInteger getY() {
	return this.y;
    }

    /**
     * Returns the prime <code>p</code>.
     *
     * @return the prime <code>p</code>.
     */
    public BigInteger getP() {
	return this.p;
    }

    /**
     * Returns the sub-prime <code>q</code>.
     *
     * @return the sub-prime <code>q</code>.
     */
    public BigInteger getQ() {
	return this.q;
    }

    /**
     * Returns the base <code>g</code>.
     *
     * @return the base <code>g</code>.
     */
    public BigInteger getG() {
	return this.g;
    }
}
