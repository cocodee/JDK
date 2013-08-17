/*
 * @(#)ServiceInformation.java	1.2 00/01/12
 *
 * Copyright 1998-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package org.omg.CORBA;


/** ServiceInformation is an IDL struct in the CORBA module.
 *  It stores information about a CORBA service available in the
 *  ORB implementation. It is obtained from the ORB.get_service_information
 *  method.
 */

public final class ServiceInformation implements org.omg.CORBA.portable.IDLEntity
{
    public int[] service_options;
    public org.omg.CORBA.ServiceDetail[] service_details;

    public ServiceInformation() { }

    public ServiceInformation(int[] __service_options,
			      org.omg.CORBA.ServiceDetail[] __service_details)
    {
        service_options = __service_options;
        service_details = __service_details;
    }
}

