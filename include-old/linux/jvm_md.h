/*
 * @(#)jvm_md.h	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

#ifndef _JAVASOFT_JVM_MD_H_
#define _JAVASOFT_JVM_MD_H_

/*
 * This file is currently collecting system-specific dregs for the
 * JNI conversion, which should be sorted out later.
 */

#include <dirent.h>		/* For DIR */
#include <sys/param.h>		/* For MAXPATHLEN */
#include <unistd.h>		/* For F_OK, R_OK, W_OK */

#define JNI_ONLOAD_SYMBOLS   {"JNI_OnLoad"}
#define JNI_ONUNLOAD_SYMBOLS {"JNI_OnUnload"}

#define JNI_LIB_PREFIX "lib"
#define JNI_LIB_SUFFIX ".so"

#define JVM_MAXPATHLEN MAXPATHLEN

#define JVM_R_OK    R_OK
#define JVM_W_OK    W_OK
#define JVM_X_OK    X_OK
#define JVM_F_OK    F_OK

/*
 * File I/O
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>

/* O Flags */

#define JVM_O_RDONLY     O_RDONLY
#define JVM_O_WRONLY     O_WRONLY
#define JVM_O_RDWR       O_RDWR
#define JVM_O_O_APPEND   O_APPEND
#define JVM_O_EXCL       O_EXCL
#define JVM_O_CREAT      O_CREAT


#endif /* !_JAVASOFT_JVM_MD_H_ */
















