/*
 * @(#)IllegalAccessError.java	1.2 00/01/12
 *
 * Copyright 1995-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.lang;

/**
 * Thrown if an application attempts to access or modify a field, or 
 * to call a method that it does not have access to. 
 * <p>
 * Normally, this error is caught by the compiler; this error can 
 * only occur at run time if the definition of a class has 
 * incompatibly changed. 
 *
 * @author  unascribed
 * @version 1.11, 09/21/98
 * @since   JDK1.0
 */
public class IllegalAccessError extends IncompatibleClassChangeError {
    /**
     * Constructs an <code>IllegalAccessError</code> with no detail message.
     */
    public IllegalAccessError() {
	super();
    }

    /**
     * Constructs an <code>IllegalAccessError</code> with the specified 
     * detail message. 
     *
     * @param   s   the detail message.
     */
    public IllegalAccessError(String s) {
	super(s);
    }
}
