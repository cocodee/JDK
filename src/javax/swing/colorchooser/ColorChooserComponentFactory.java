/*
 * @(#)ColorChooserComponentFactory.java	1.2 00/01/12
 *
 * Copyright 1998-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package javax.swing.colorchooser;

import javax.swing.*;



/**
 * A class designed to produce preconfigured "accessory" objects to
 * insert into color choosers.
 *
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with 
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @version 1.11 08/28/98
 * @author Steve Wilson
 */
public class ColorChooserComponentFactory {

    private ColorChooserComponentFactory() { } // can't instantiate


    public static AbstractColorChooserPanel[] getDefaultChooserPanels() {
        AbstractColorChooserPanel[] choosers = { new DefaultSwatchChooserPanel(),
						 new DefaultHSBChooserPanel(),
						 new DefaultRGBChooserPanel() };
        return choosers;
    }

    public static JComponent getPreviewPanel() {
        return new DefaultPreviewPanel();
    }

}
