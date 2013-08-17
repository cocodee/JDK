/*
 * @(#)ActivationException.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.rmi.activation;

/**
 * General exception used by the activation interfaces.
 *
 * @author 	Ann Wollrath
 * @version	1.14, 07/08/98
 * @since 	JDK1.2
 */
public class ActivationException extends Exception {

    /**
     * Nested Exception to hold wrapped remote exceptions.
     *
     * @serial 
     */
    public Throwable detail;

    /** indicate compatibility with JDK 1.2 version of class */
    private static final long serialVersionUID = -4320118837291406071L;

    /**
     * Constructs an <code>ActivationException</code> with no specified
     * detail message.
     * @since JDK1.2
     */
    public ActivationException() {
	super();
    }

    /**
     * Constructs an <code>ActivationException</code> with detail
     * message, <code>s</code>.
     * @param s the detail message
     * @since JDK1.2
     */
    public ActivationException(String s) {
	super(s);
    }

    /**
     * Constructs an <code>ActivationException</code> with detail message,
     * <code>s</code>, and detail exception <code>ex</code>.
     *
     * @param s detail message
     * @param ex detail exception
     * @since JDK1.2
     */
    public ActivationException(String s, Throwable ex) {
	super(s);
	detail = ex;
    }

    /**
     * Produces the message, include the message from the nested
     * exception if there is one.
     * @return the message
     * @since JDK1.2
     */
    public String getMessage() {
	if (detail == null) 
	    return super.getMessage();
	else
	    return super.getMessage() + 
		"; nested exception is: \n\t" +
		detail.toString();
    }

    /**
     * Prints the composite message and the embedded stack trace to
     * the specified stream <code>ps</code>.
     * @param ps the print stream
     * @since JDK1.2
     */
    public void printStackTrace(java.io.PrintStream ps) {
	if (detail == null) {
	    super.printStackTrace(ps);
	} else {
	    synchronized(ps) {
		ps.println(this);
		detail.printStackTrace(ps);
	    }
	}
    }

    /**
     * Prints the composite message to <code>System.err</code>.
     * @since JDK1.2
     */
    public void printStackTrace() {
	printStackTrace(System.err);
    }

    /**
     * Prints the composite message and the embedded stack trace to
     * the specified print writer <code>pw</code>
     * @param pw the print writer
     * @since JDK1.2
     */
    public void printStackTrace(java.io.PrintWriter pw)
    {
	if (detail == null) {
	    super.printStackTrace(pw);
	} else {
	    synchronized(pw) {
		pw.println(this);
		detail.printStackTrace(pw);
	    }
	}
    }
}
