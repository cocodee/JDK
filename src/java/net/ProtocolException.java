/*
 * @(#)ProtocolException.java	1.2 00/01/12
 *
 * Copyright 1995-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.net;

import java.io.IOException;

/**
 * Thrown to indicate that there is an error in the underlying 
 * protocol, such as a TCP error. 
 *
 * @author  Chris Warth
 * @version 1.11, 09/21/98
 * @since   JDK1.0
 */
public 
class ProtocolException extends IOException { 
    /**
     * Constructs a new <code>ProtocolException</code> with the 
     * specified detail message. 
     *
     * @param   host   the detail message.
     */
    public ProtocolException(String host) {
	super(host);
    }
    
    /**
     * Constructs a new <code>ProtocolException</code> with no detail message.
     */
    public ProtocolException() {
    }
}
