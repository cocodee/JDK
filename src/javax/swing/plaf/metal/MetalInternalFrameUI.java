/*
 * @(#)MetalInternalFrameUI.java	1.2 00/01/12
 *
 * Copyright 1998-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package javax.swing.plaf.metal;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.util.EventListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import javax.swing.plaf.*;

/**
 * Metal implementation of JInternalFrame.  
 * <p>
 *
 * @version 1.16 09/17/98
 * @author Steve Wilson
 */
public class MetalInternalFrameUI extends BasicInternalFrameUI {

  private MetalInternalFrameTitlePane titlePane;

  private PropertyChangeListener paletteListener;
  private PropertyChangeListener contentPaneListener;

  private static final Border handyEmptyBorder = new EmptyBorder(0,0,0,0);
  
  protected static String IS_PALETTE = "JInternalFrame.isPalette";

  public MetalInternalFrameUI(JInternalFrame b)   {
    super(b);
  }

  public static ComponentUI createUI(JComponent c)    {
      return new MetalInternalFrameUI( (JInternalFrame) c);
  }

  public void installUI(JComponent c) { 
    frame = (JInternalFrame)c;

    paletteListener = new PaletteListener();
    contentPaneListener = new ContentPaneListener();
    c.addPropertyChangeListener(paletteListener);
    c.addPropertyChangeListener(contentPaneListener);

    super.installUI(c);

    Object paletteProp = c.getClientProperty( IS_PALETTE );
    if ( paletteProp != null ) {
	setPalette( ((Boolean)paletteProp).booleanValue() );
    }

    Container content = frame.getContentPane();
    stripContentBorder(content);    
    //c.setOpaque(false);
  }

  
  public void uninstallUI(JComponent c) {                  
      c.removePropertyChangeListener(paletteListener);
      c.removePropertyChangeListener(contentPaneListener);

      Container cont = ((JInternalFrame)(c)).getContentPane();
      if (cont instanceof JComponent) {
	JComponent content = (JComponent)cont;
	if ( content.getBorder() == handyEmptyBorder) {
	  content.setBorder(null);
	}
      }
      super.uninstallUI(c);
  } 

  protected void installKeyboardActions(){
  }

  protected void uninstallKeyboardActions(){
  }

  private void stripContentBorder(Object c) {
        if ( c instanceof JComponent ) {
            JComponent contentComp = (JComponent)c;
            Border contentBorder = contentComp.getBorder();
   	    if (contentBorder == null || contentBorder instanceof UIResource) {
	        contentComp.setBorder( handyEmptyBorder );
            }
        }
  }

    
  protected JComponent createNorthPane(JInternalFrame w) {
    titlePane = new MetalInternalFrameTitlePane(w);
    return titlePane;
  }


    protected void replacePane(JComponent currentPane, JComponent newPane) {
        super.replacePane(currentPane, newPane);

	//fix for 4146355
        if(currentPane != null) {
	  if (currentPane instanceof MetalInternalFrameTitlePane) {  
            frame.removePropertyChangeListener((PropertyChangeListener)currentPane);
	  }
        }
    
    }


  public void setPalette(boolean isPalette) {
    if (isPalette) {
        LookAndFeel.installBorder(frame, "InternalFrame.paletteBorder");
    } else {
        LookAndFeel.installBorder(frame, "InternalFrame.border");
    }
    titlePane.setPalette(isPalette);

  }

  class PaletteListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
	String name = e.getPropertyName();
	if ( name.equals( IS_PALETTE ) ) {
	    if ( e.getNewValue() != null ) {
	        setPalette( ((Boolean)e.getNewValue()).booleanValue() );
	    }
	    else {
		setPalette( false );
	    }
        }
    }
  } // end class PaletteListener

  class ContentPaneListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
	String name = e.getPropertyName();
	if ( name.equals( JInternalFrame.CONTENT_PANE_PROPERTY ) ) {
	    stripContentBorder(e.getNewValue());
        }
    }
  } // end class ContentPaneListener


}

