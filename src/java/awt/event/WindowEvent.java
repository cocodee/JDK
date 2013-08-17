/*
 * @(#)WindowEvent.java	1.2 00/01/12
 *
 * Copyright 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package java.awt.event;

import java.awt.Event;
import java.awt.Window;

/**
 * A low-level event which indicates that a window has changed its status.
 * This low-level event is generated by a Window object when it is opened,
 * closed, about to close, activated or deactivated, iconified or deconified.
 * <P>
 * The event is passed to every <code>WindowListener</code>
 * or <code>WindowAdapter</code> object which registered to receive such
 * events using the window's <code>addWindowListener</code> method.
 * (<code>WindowAdapter</code> objects implement the
 * <code>WindowListener</code> interface.) Each such listener object
 * gets this <code>WindowEvent</code> when the event occurs.
 *
 * @see WindowAdapter
 * @see WindowListener
 * @see <a href="http://java.sun.com/docs/books/tutorial/post1.0/ui/windowlistener.html">Tutorial: Writing a Window Listener</a>
 * @see <a href="http://www.awl.com/cp/javaseries/jcl1_2.html">Reference: The Java Class Libraries (update file)</a>
 *
 * @version 1.17 07/29/98
 * @author Carl Quinn
 * @author Amy Fowler
 */
public class WindowEvent extends ComponentEvent {

    /**
     * The first number in the range of ids used for window events.
     */
    public static final int WINDOW_FIRST        = 200;

    /**
     * The last number in the range of ids used for window events.
     */
    public static final int WINDOW_LAST         = 206;

    /**
     * The window opened event.  This event is delivered only
     * the first time a window is made visible.
     */
    public static final int WINDOW_OPENED	= WINDOW_FIRST; // 200

    /**
     * The "window is closing" event. This event is delivered when
     * the user attempts to close the window from the window's system menu.  
     * If the program does not explicitly hide or dispose the window
     * while processing this event, the window close operation will be
     * cancelled.
     */
    public static final int WINDOW_CLOSING	= 1 + WINDOW_FIRST; //Event.WINDOW_DESTROY

    /**
     * The window closed event. This event is delivered after
     * the window has been closed as the result of a call to dispose.
     */
    public static final int WINDOW_CLOSED	= 2 + WINDOW_FIRST;

    /**
     * The window iconified event. This event is delivered when
     * the window has been changed from a normal to a minimized state.
     * For many platforms, a minimized window is displayed as
     * the icon specified in the window's iconImage property.
     * @see Frame#setIconImage
     */
    public static final int WINDOW_ICONIFIED	= 3 + WINDOW_FIRST; //Event.WINDOW_ICONIFY

    /**
     * The window deiconified event type. This event is delivered when
     * the window has been changed from a minimized to a normal state.
     */
    public static final int WINDOW_DEICONIFIED	= 4 + WINDOW_FIRST; //Event.WINDOW_DEICONIFY

    /**
     * The window activated event type. This event is delivered
     * when the window becomes the user's active window, which means
     * that the window (or one of its subcomponents) will receive 
     * keyboard events.
     */
    public static final int WINDOW_ACTIVATED	= 5 + WINDOW_FIRST;

    /**
     * The window deactivated event type. This event is delivered
     * when a window is no longer the user's active window, which
     * means keyboard events will no longer be delivered to the
     * window or its subcomponents.
     */
    public static final int WINDOW_DEACTIVATED	= 6 + WINDOW_FIRST;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = -1567959133147912127L;

    /**
     * Constructs a WindowEvent object.
     * @param source the Window object that originated the event
     * @param id     an integer indicating the type of event
     */
    public WindowEvent(Window source, int id) {
        super(source, id);
    }

    /**
     * Returns the originator of the event.
     *
     * @return the Window object that originated the event
     */
    public Window getWindow() {
        return (source instanceof Window) ? (Window)source : null;
    }

    /**
     * Returns a parameter string identifying this event.
     * This method is useful for event-logging and for debugging.
     *
     * @return a string identifying the event and its attributes
     */
    public String paramString() {
        String typeStr;
        switch(id) {
          case WINDOW_OPENED:
              typeStr = "WINDOW_OPENED";
              break;
          case WINDOW_CLOSING:
              typeStr = "WINDOW_CLOSING";
              break;
          case WINDOW_CLOSED:
              typeStr = "WINDOW_CLOSED";
              break;
          case WINDOW_ICONIFIED:
              typeStr = "WINDOW_ICONIFIED";
              break;
          case WINDOW_DEICONIFIED:
              typeStr = "WINDOW_DEICONIFIED";
              break;
          case WINDOW_ACTIVATED:
              typeStr = "WINDOW_ACTIVATED";
              break;
          case WINDOW_DEACTIVATED:
              typeStr = "WINDOW_DEACTIVATED";
              break;
          default:
              typeStr = "unknown type";
        }
        return typeStr;
    }

}
