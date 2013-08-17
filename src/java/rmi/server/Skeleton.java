/*
 * @(#)Skeleton.java	1.2 00/01/12
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package java.rmi.server;

import java.rmi.Remote;

/**
 * The <code>Skeleton</code> interface is used solely by the RMI
 * implementation.
 *
 * <p> Every version 1.1 (and version 1.1 compatible skeletons generated in
 * 1.2 using <code>rmic -vcompat</code>) skeleton class generated by the rmic
 * stub compiler implements this interface. A skeleton for a remote object is
 * a server-side entity that dispatches calls to the actual remote object
 * implementation.
 *
 * @version 1.9, 09/21/98
 * @author  Ann Wollrath
 * @since   JDK1.1
 * @deprecated no replacement.  Skeletons are no longer required for remote
 * method calls in JDK1.2 and greater.
 */
public interface Skeleton {
    /**
     * Unmarshals arguments, calls the actual remote object implementation,
     * and marshals the return value or any exception.
     *
     * @exception java.lang.Exception if a general exception occurs.
     * @since JDK1.1
     * @deprecated no replacement
     */
    void dispatch(Remote obj, RemoteCall theCall, int opnum, long hash)
	throws Exception;

    /**
     * Returns the operations supported by the skeleton.
     * @since JDK1.1
     * @deprecated no replacement
     */
    Operation[] getOperations();
}
