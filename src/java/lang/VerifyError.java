/*
 * @(#)VerifyError.java	1.2 00/01/12
 *
 * Copyright 1995-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.lang;

/**
 * Thrown when the "verifier" detects that a class file, 
 * though well formed, contains some sort of internal inconsistency 
 * or security problem. 
 *
 * @author  unascribed
 * @version 1.8, 09/21/98
 * @since   JDK1.0
 */
public
class VerifyError extends LinkageError {
    /**
     * Constructs an <code>VerifyError</code> with no detail message.
     */
    public VerifyError() {
	super();
    }

    /**
     * Constructs an <code>VerifyError</code> with the specified detail message.
     *
     * @param   s   the detail message.
     */
    public VerifyError(String s) {
	super(s);
    }
}
