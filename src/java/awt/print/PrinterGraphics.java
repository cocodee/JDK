/*
 * @(#)PrinterGraphics.java	1.2 00/01/12
 *
 * Copyright 1998-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.awt.print;

/**
 * The <code>PrinterGraphics</code> interface is implemented by 
 * {@link java.awt.Graphics} objects that are passed to 
 * {@link Printable} objects to render a page. It allows an 
 * application to find the {@link PrinterJob} object that is 
 * controlling the printing.
 */

public interface PrinterGraphics {

    /**
     * Returns the <code>PrinterJob</code> that is controlling the
     * current rendering request.
     * @return the <code>PrinterJob</code> controlling the current
     * rendering request.
     * @see java.awt.print.Printable
     */
    PrinterJob getPrinterJob();

}
