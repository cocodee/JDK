/*
 * @(#)EmptyBorder.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package javax.swing.border;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Component;
import java.io.Serializable;

/**
 * A class which provides an empty, transparent border which
 * takes up space but does no drawing.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with 
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.19 08/28/98
 * @author David Kloba
 */
public class EmptyBorder extends AbstractBorder implements Serializable
{
    protected int left, right, top, bottom;

    /**
     * Creates an empty border with the specified insets.
     * @param top the top inset of the border
     * @param left the left inset of the border
     * @param bottom the bottom inset of the border
     * @param right the right inset of the border
     */
    public EmptyBorder(int top, int left, int bottom, int right)   {
        this.top = top; 
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    /**
     * Creates an empty border with the specified insets.
     * @param insets the insets of the border
     */
    public EmptyBorder(Insets insets)   {
        this.top = insets.top; 
        this.right = insets.right;
        this.bottom = insets.bottom;
        this.left = insets.left;
    }

    /**
     * Does no drawing by default.
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    }

    /**
     * Returns the insets of the border.
     * @param c the component for which this border insets value applies
     */
    public Insets getBorderInsets(Component c)       {
        return new Insets(top, left, bottom, right);
    }

    /** 
     * Reinitialize the insets parameter with this Border's current Insets. 
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     */
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = left;
        insets.top = top;
        insets.right = right;
        insets.bottom = bottom;
        return insets;
    }

    /**
     * Returns whether or not the border is opaque.
     * Returns false by default.
     */
    public boolean isBorderOpaque() { return false; }

}
