/*
 * @(#)MotifPopupMenuSeparatorUI.java	1.2 00/01/12
 *
 * Copyright 1998-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.java.swing.plaf.motif;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.plaf.*;

/**
 * A Motif L&F implementation of PopupMenuSeparatorUI.  This implementation
 * is a "combined" view/controller.
 *
 * @version 1.6 11/05/98
 * @author Jeff Shapiro
 */

public class MotifPopupMenuSeparatorUI extends MotifSeparatorUI
{
    public static ComponentUI createUI( JComponent c )
    {
        return new MotifPopupMenuSeparatorUI();
    }

    public void paint( Graphics g, JComponent c )
    {
        Dimension s = c.getSize();

	g.setColor( c.getForeground() );
	g.drawLine( 0, 0, s.width, 0 );

	g.setColor( c.getBackground() );
	g.drawLine( 0, 1, s.width, 1 );
    }

    public Dimension getPreferredSize( JComponent c )
    {
        return new Dimension( 0, 2 );
    }

}
