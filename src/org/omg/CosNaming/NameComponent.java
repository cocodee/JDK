/*
 * @(#)NameComponent.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
/*
 * File: ./org/omg/CosNaming/NameComponent.java
 * From: nameservice.idl
 * Date: Tue Aug 11 03:12:09 1998
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package org.omg.CosNaming;
public final class NameComponent implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables
    public String id;
    public String kind;
    //	constructors
    public NameComponent() { }
    public NameComponent(String __id, String __kind) {
	id = __id;
	kind = __kind;
    }
}
