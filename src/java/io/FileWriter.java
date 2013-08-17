/*
 * @(#)FileWriter.java	1.2 00/01/12
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.io;


/**
 * Convenience class for writing character files.  The constructors of this
 * class assume that the default character encoding and the default byte-buffer
 * size are acceptable.  To specify these values yourself, construct an
 * OutputStreamWriter on a FileOutputStream.
 *
 * @see OutputStreamWriter
 * @see FileOutputStream
 *
 * @version 	1.7, 98/09/21
 * @author	Mark Reinhold
 * @since	JDK1.1
 */

public class FileWriter extends OutputStreamWriter {

    public FileWriter(String fileName) throws IOException {
	super(new FileOutputStream(fileName));
    }

    public FileWriter(String fileName, boolean append) throws IOException {
	super(new FileOutputStream(fileName, append));
    }

    public FileWriter(File file) throws IOException {
	super(new FileOutputStream(file));
    }

    public FileWriter(FileDescriptor fd) {
	super(new FileOutputStream(fd));
    }

}
