/*
 * @(#)Checkbox.java	1.2 00/01/12
 *
 * Copyright 1995-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package java.awt;

import java.awt.peer.CheckboxPeer;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;


/**
 * A check box is a graphical component that can be in either an
 * "on" (<code>true</code>) or "off" (<code>false</code>) state.
 * Clicking on a check box changes its state from
 * "on" to "off," or from "off" to "on."
 * <p>
 * The following code example creates a set of check boxes in
 * a grid layout:
 * <p>
 * <hr><blockquote><pre>
 * setLayout(new GridLayout(3, 1));
 * add(new Checkbox("one", null, true));
 * add(new Checkbox("two"));
 * add(new Checkbox("three"));
 * </pre></blockquote><hr>
 * <p>
 * This image depicts the check boxes and grid layout
 * created by this code example:
 * <p>
 * <img src="doc-files/Checkbox-1.gif"
 * ALIGN=center HSPACE=10 VSPACE=7>
 * <p>
 * The button labeled <code>one</code> is in the "on" state, and the
 * other two are in the "off" state. In this example, which uses the
 * <code>GridLayout</code> class, the states of the three check
 * boxes are set independently.
 * <p>
 * Alternatively, several check boxes can be grouped together under
 * the control of a single object, using the
 * <code>CheckboxGroup</code> class.
 * In a check box group, at most one button can be in the "on"
 * state at any given time. Clicking on a check box to turn it on
 * forces any other check box in the same group that is on
 * into the "off" state.
 *
 * @version	1.49 08/19/98
 * @author 	Sami Shaio
 * @see         java.awt.GridLayout
 * @see         java.awt.CheckboxGroup
 * @since       JDK1.0
 */
public class Checkbox extends Component implements ItemSelectable {

    static {
        /* ensure that the necessary native libraries are loaded */
	Toolkit.loadLibraries();
        initIDs();
    }

    /**
     * The label of the Checkbox.
	 * This field can be null. If a label is not specified it 
	 * defaults to null or "".
	 * @serial
     * @see getLabel()
     * @see setLabel()
     */
    String label;

    /**
     * The state of the Checkbox.
	 * @serial
     * @see getState()
     * @see setLabel()
     */
    boolean state;

    /**
     * The check box group.
	 * This field can be null indicating that the checkbox
	 * is not a group checkbox.  
	 * @serial
     * @see getCheckBoxGroup()
     * @see setCheckBoxGroup()
     */
    CheckboxGroup group;

    transient ItemListener itemListener;

    private static final String base = "checkbox";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 7270714317450821763L;

    /**
     * Helper function for setState and CheckboxGroup.setSelectedCheckbox
     * Should remain package-private.
     */
    void setStateInternal(boolean state) {
	this.state = state;
	CheckboxPeer peer = (CheckboxPeer)this.peer;
	if (peer != null) {
	    peer.setState(state);
	}
    }

    /**
     * Creates a check box with no label. The state of this
     * check box is set to "off," and it is not part of any
     * check box group.
     */
    public Checkbox() {
        this("", false, null);
    }

    /**
     * Creates a check box with the specified label.  The state
     * of this check box is set to "off," and it is not part of
     * any check box group.
     * @param     label   a string label for this check box,
     *                        or <code>null</code> for no label.
     */
    public Checkbox(String label) {
	this(label, false, null);
    }

    /**
     * Creates a check box with the specified label
     * and sets the specified state.
     * This check box is not part of any check box group.
     * @param     label   a string label for this check box,
     *                        or <code>null</code> for no label.
     * @param     state    the initial state of this check box.
     */
    public Checkbox(String label, boolean state) {
        this(label, state, null);
    }

    /**
     * Creates a check box with the specified label, in the specified
     * check box group, and set to the specified state.

     * @param     label   a string label for this check box,
     *                        or <code>null</code> for no label.
     * @param     state   the initial state of this check box.
     * @param     group   a check box group for this check box,
     *                           or <code>null</code> for no group.
     * @since     JDK1.1
     */
    public Checkbox(String label, boolean state, CheckboxGroup group) {
	this.label = label;
	this.state = state;
	this.group = group;
	if (state && (group != null)) {
	    group.setSelectedCheckbox(this);
	}
    }

    /**
     * Constructs a Checkbox with the specified label, set to the
     * specified state, and in the specified check box group.
     *
     * @param     label   a string label for this check box,
     *                        or <code>null</code> for no label.
     * @param     group   a check box group for this check box,
     *                           or <code>null</code> for no group.
     * @param     state   the initial state of this check box.
     * @since     JDK1.1
     */
    public Checkbox(String label, CheckboxGroup group, boolean state) {
    	this(label, state, group);
    }

    /**
     * Construct a name for this component.  Called by getName() when the
     * name is null.
     */
    String constructComponentName() {
        synchronized (getClass()) {
	    return base + nameCounter++;
	}
    }

    /**
     * Creates the peer of the Checkbox. The peer allows you to change the
     * look of the Checkbox without changing its functionality.
     * @see     java.awt.Toolkit#createCheckbox(java.awt.Checkbox)
     * @see     java.awt.Component#getToolkit()
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
	    if (peer == null) 
	        peer = getToolkit().createCheckbox(this);
	    super.addNotify();
	}
    }

    /**
     * Gets the label of this check box.
     * @return   the label of this check box, or <code>null</code>
     *                  if this check box has no label.
     * @see      java.awt.Checkbox#setLabel
     */
    public String getLabel() {
	return label;
    }

    /**
     * Sets this check box's label to be the string argument.
     * @param    label   a string to set as the new label, or
     *                        <code>null</code> for no label.
     * @see      java.awt.Checkbox#getLabel
     */
    public void setLabel(String label) {
        boolean testvalid = false;

	synchronized (this) {
	    if (label != this.label && (this.label == null ||
					!this.label.equals(label))) {
	        this.label = label;
		CheckboxPeer peer = (CheckboxPeer)this.peer;
		if (peer != null) {
		    peer.setLabel(label);
		}
		testvalid = true;
	    }
	}
	    
	// This could change the preferred size of the Component.
	if (testvalid && valid) {
	    invalidate();
	}
    }

    /**
     * Determines whether this check box is in the "on" or "off" state.
     * The boolean value <code>true</code> indicates the "on" state,
     * and <code>false</code> indicates the "off" state.
     * @return    the state of this check box, as a boolean value.
     * @see       java.awt.Checkbox#setState
     */
    public boolean getState() {
	return state;
    }

    /**
     * Sets the state of this check box to the specified state.
     * The boolean value <code>true</code> indicates the "on" state,
     * and <code>false</code> indicates the "off" state.
     * @param     state   the boolean state of the check box.
     * @see       java.awt.Checkbox#getState
     */
    public void setState(boolean state) {
	/* Cannot hold check box lock when calling group.setSelectedCheckbox. */
    	CheckboxGroup group = this.group;
	if (group != null) {
	    if (state) {
		group.setSelectedCheckbox(this);
	    } else if (group.getSelectedCheckbox() == this) {
		state = true;
	    }
	}
	setStateInternal(state);
    }

    /**
     * Returns an array (length 1) containing the checkbox
     * label or null if the checkbox is not selected.
     * @see ItemSelectable
     */
    public Object[] getSelectedObjects() {
        if (state) {
            Object[] items = new Object[1];
            items[0] = label;
            return items;
        }
        return null;
    }

    /**
     * Determines this check box's group.
     * @return     this check box's group, or <code>null</code>
     *               if the check box is not part of a check box group.
     * @see        java.awt.Checkbox#setCheckboxGroup
     */
    public CheckboxGroup getCheckboxGroup() {
	return group;
    }

    /**
     * Sets this check box's group to be the specified check box group.
     * If this check box is already in a different check box group,
     * it is first taken out of that group.
     * @param     g   the new check box group, or <code>null</code>
     *                to remove this check box from any check box group.
     * @see       java.awt.Checkbox#getCheckboxGroup
     */
    public void setCheckboxGroup(CheckboxGroup g) {
    	CheckboxGroup group = this.group;
	if (group != null) {
	    group.setSelectedCheckbox(null);
	}
	/* Locking check box above could cause deadlock with
	 * CheckboxGroup's setSelectedCheckbox method.
	 */
	synchronized (this) {
	    this.group = g;
	    CheckboxPeer peer = (CheckboxPeer)this.peer;
	    if (peer != null) {
		peer.setCheckboxGroup(g);
	    }
	}
    }

    /**
     * Adds the specified item listener to receive item events from
     * this check box.
     * If l is null, no exception is thrown and no action is performed.
     *
     * @param         l    the item listener
     * @see           java.awt.event.ItemEvent
     * @see           java.awt.event.ItemListener
     * @see           java.awt.Checkbox#removeItemListener
     * @since         JDK1.1
     */
    public synchronized void addItemListener(ItemListener l) {
	if (l == null) {
	    return;
	}
        itemListener = AWTEventMulticaster.add(itemListener, l);
        newEventsOnly = true;
    }

    /**
     * Removes the specified item listener so that the item listener
     * no longer receives item events from this check box.
     * If l is null, no exception is thrown and no action is performed.
     *
     * @param         l    the item listener
     * @see           java.awt.event.ItemEvent
     * @see           java.awt.event.ItemListener
     * @see           java.awt.Checkbox#addItemListener
     * @since         JDK1.1
     */
    public synchronized void removeItemListener(ItemListener l) {
	if (l == null) {
	    return;
	}
        itemListener = AWTEventMulticaster.remove(itemListener, l);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        if (e.id == ItemEvent.ITEM_STATE_CHANGED) {
            if ((eventMask & AWTEvent.ITEM_EVENT_MASK) != 0 ||
                itemListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    /**
     * Processes events on this check box.
     * If the event is an instance of <code>ItemEvent</code>,
     * this method invokes the <code>processItemEvent</code> method.
     * Otherwise, it calls its superclass's <code>processEvent</code> method.
     * @param         e the event.
     * @see           java.awt.event.ItemEvent
     * @see           java.awt.Checkbox#processItemEvent
     * @since         JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ItemEvent) {
            processItemEvent((ItemEvent)e);
            return;
        }
	super.processEvent(e);
    }

    /**
     * Processes item events occurring on this check box by
     * dispatching them to any registered
     * <code>ItemListener</code> objects.
     * <p>
     * This method is not called unless item events are
     * enabled for this component. Item events are enabled
     * when one of the following occurs:
     * <p><ul>
     * <li>An <code>ItemListener</code> object is registered
     * via <code>addItemListener</code>.
     * <li>Item events are enabled via <code>enableEvents</code>.
     * </ul>
     * @param       e the item event.
     * @see         java.awt.event.ItemEvent
     * @see         java.awt.event.ItemListener
     * @see         java.awt.Checkbox#addItemListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processItemEvent(ItemEvent e) {
        if (itemListener != null) {
            itemListener.itemStateChanged(e);
        }
    }

    /**
     * Returns the parameter string representing the state of
     * this check box. This string is useful for debugging.
     * @return    the parameter string of this check box.
     */
    protected String paramString() {
	String str = super.paramString();
	String label = this.label;
	if (label != null) {
	    str += ",label=" + label;
	}
	return str + ",state=" + state;
    }


    /* Serialization support.
     */
	
	/*
    * Serialized data version
    * @serial
    */
    private int checkboxSerializedDataVersion = 1;

    /**
    * Writes default serializable fields to stream.  Writes
    * a list of serializable ItemListener(s) as optional data.
    * The non-serializable ItemListner(s) are detected and
    * no attempt is made to serialize them.
    *
    * @serialData Null terminated sequence of 0 or more pairs.
    *             The pair consists of a String and Object.
    *             The String indicates the type of object and
    *             is one of the following :
    *             itemListenerK indicating and ItemListener object.
    *
    * @see AWTEventMulticaster.save(ObjectOutputStream, String, EventListener)
    * @see java.awt.Component.itemListenerK
    */
    private void writeObject(ObjectOutputStream s)
      throws java.io.IOException
    {
      s.defaultWriteObject();

      AWTEventMulticaster.save(s, itemListenerK, itemListener);
      s.writeObject(null);
    }

	/*
    * Read the ObjectInputStream and if it isnt null
    * add a listener to receive item events fired
    * by the Checkbox.
    * Unrecognised keys or values will be Ignored.
    * @serial
    * @see removeActionListener()
    * @see addActionListener()
    */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException
    {
      s.defaultReadObject();

      Object keyOrNull;
      while(null != (keyOrNull = s.readObject())) {
	String key = ((String)keyOrNull).intern();

	if (itemListenerK == key)
	  addItemListener((ItemListener)(s.readObject()));

	else // skip value for unrecognized key
	  s.readObject();
      }
    }

    /**
     * Initialize JNI field and method ids
     */
    private static native void initIDs();
}
