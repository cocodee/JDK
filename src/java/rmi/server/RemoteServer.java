/*
 * @(#)RemoteServer.java	1.2 00/01/12
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package java.rmi.server;

import java.rmi.*;

/**
 * The <code>RemoteServer</code> class is the common superclass to server
 * implementations and provides the framework to support a wide range
 * of remote reference semantics.  Specifically, the functions needed
 * to create and export remote objects (i.e. to make them remotely
 * available) are provided abstractly by <code>RemoteServer</code> and
 * concretely by its subclass(es).
 *
 * @version 1.18, 07/08/98
 * @author  Ann Wollrath
 * @since   JDK1.1
 */
public abstract class RemoteServer extends RemoteObject
{
    private static String logname = "RMI";
    private static LogStream log;

    /* indicate compatibility with JDK 1.1.x version of class */
    private static final long serialVersionUID = -4100238210092549637L;

    /**
     * Constructs a <code>RemoteServer</code>.
     * @since JDK1.1
     */
    protected RemoteServer() {
	super();
    }

    /**
     * Constructs a <code>RemoteServer</code> with the given reference type.
     *
     * @param ref the remote reference
     * @since JDK1.1
     */
    protected RemoteServer(RemoteRef ref) {
	super(ref);
    }

    /**
     * Return the hostname of the current client.  When called from a
     * thread actively handling a remote method invocation the
     * hostname of the client is returned.
     * @exception ServerNotActiveException If called outside of servicing
     * a remote method invocation.
     * @since JDK1.1
     */
    public static String getClientHost() throws ServerNotActiveException {
	try {
	    Class refClass = Class.forName(RemoteRef.packagePrefix +
					   ".UnicastServerRef");
	    ServerRef ref = (ServerRef)refClass.newInstance();
	    return ref.getClientHost();
	} catch (ServerNotActiveException e) {
	    throw e;
	} catch (Exception e) {
	    throw new ServerNotActiveException("Client host unobtainable");
	}
    }

    /**
     * Log RMI calls to the output stream <I>out</I>. If <I>out</I> is
     * null, call logging is turned off.
     * @param out the output stream to which RMI calls should be logged
     * @since JDK1.1
     */
    public static void setLog(java.io.OutputStream out) 
    {
	if (out == null) {
	    log = null;
	} else {
	    LogStream tempLog = LogStream.log(logname);
	    tempLog.setOutputStream(out);
	    log = tempLog;
	}
    }
    
    /**
     * Returns stream for the RMI call log.
     * @return the call log
     * @since JDK1.1
     */
    public static java.io.PrintStream getLog() 
    {
	return log;
    }

    static 
    {
	// initialize log
	try {
	    Boolean tmp = (Boolean)java.security.AccessController.doPrivileged(
                    new sun.security.action.GetBooleanAction("java.rmi.server.logCalls"));
	    boolean logCalls = tmp.booleanValue();
	    log = logCalls ? LogStream.log(logname) : null;
	} catch (Exception e) {
	}
    }
}
