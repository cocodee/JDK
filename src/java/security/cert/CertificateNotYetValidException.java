/*
 * @(#)CertificateNotYetValidException.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.security.cert;

/**
 * Certificate is not yet valid exception. This is thrown whenever
 * the current <code>Date</code> or the specified <code>Date</code>
 * is before the <code>notBefore</code> date/time in the Certificate
 * validity period.
 *
 * @author Hemma Prafullchandra
 * 1.4
 */
public class CertificateNotYetValidException extends CertificateException {

    /**
     * Constructs a CertificateNotYetValidException with no detail message. A
     * detail message is a String that describes this particular
     * exception.
     */
    public CertificateNotYetValidException() {
        super();
    }

    /**
     * Constructs a CertificateNotYetValidException with the specified detail
     * message. A detail message is a String that describes this
     * particular exception.
     *   
     * @param message the detail message.
     */
    public CertificateNotYetValidException(String message) {
        super(message);
    }
}
