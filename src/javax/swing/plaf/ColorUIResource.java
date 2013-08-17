/*
 * @(#)ColorUIResource.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package javax.swing.plaf;

import java.awt.Color;
import javax.swing.plaf.UIResource;


/*
 * A subclass of Color that implements UIResource.  UI
 * classes that create colors should use this class.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 * 
 * @see javax.swing.plaf.UIResource
 * @version 1.7 08/28/98
 * @author Hans Muller
 * 
 */
public class ColorUIResource extends Color implements UIResource
{
    public ColorUIResource(int r, int g, int b) {
	super(r, g, b);
    }

    public ColorUIResource(int rgb) {
	super(rgb);
    }

    public ColorUIResource(float r, float g, float b) {
	super(r, g, b);
    }

    public ColorUIResource(Color c) {
	super(c.getRed(), c.getGreen(), c.getBlue());
    }
}
