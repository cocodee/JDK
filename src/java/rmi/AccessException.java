/*
 * @(#)AccessException.java	1.2 00/01/12
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.rmi;

/**
 * An <code>AccessException</code> is thrown by certain methods of the
 * <code>java.rmi.Naming</code> class (specifically <code>bind</code>,
 * <code>rebind</code>, and <code>unbind</code>) and methods of the
 * <code>java.rmi.activation.ActivationSystem</code> interface to
 * indicate that the caller does not have permission to perform the action
 * requested by the method call.  If the method was invoked from a non-local
 * host, then an <code>AccessException</code> is thrown.
 * 
 * @version 1.7, 09/21/98
 * @author  Ann Wollrath
 * @author  Roger Riggs
 * @since   JDK1.1
 * @see     java.rmi.Naming
 * @see     java.rmi.activation.ActivationSystem
 */
public class AccessException extends java.rmi.RemoteException {

    /* indicate compatibility with JDK 1.1.x version of class */
     private static final long serialVersionUID = 6314925228044966088L;

    /**
     * Constructs an <code>AccessException</code> with the specified
     * detail message.
     *
     * @param s the detail message
     * @since JDK1.1
     */
    public AccessException(String s) {
	super(s);
    }

    /**
     * Constructs an <code>AccessException</code> with the specified
     * detail message and nested exception.
     *
     * @param s the detail message
     * @param ex the nested exception
     * @since JDK1.1
     */
    public AccessException(String s, Exception ex) {
	super(s, ex);
    }
}
