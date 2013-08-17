/*
 * @(#)CellEditorListener.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package javax.swing.event;

import javax.swing.event.ChangeEvent;
import java.util.EventListener;

/**
 * CellEditorListener defines the interface for an object that listens
 * to changes in a CellEditor
 *
 * @version 1.8 08/26/98
 * @author Alan Chung
 */

public interface CellEditorListener extends java.util.EventListener {

    /** This tells the listeners the editor has ended editing */
    public void editingStopped(ChangeEvent e);

    /** This tells the listeners the editor has canceled editing */
    public void editingCanceled(ChangeEvent e);
}

