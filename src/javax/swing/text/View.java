/*
 * @(#)View.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package javax.swing.text;

import java.awt.*;
import javax.swing.SwingConstants;
import javax.swing.event.*;

/**
<p>
A very important part of the text package is the View class.  As the name 
suggests it represents a view of the text model, or a piece of the text
model.  It is this class that is responsible for the look of the text component.  
The view is not intended to be some completely new thing that one must learn, 
but rather is much like a lightweight component.  In fact, the original View
implementation was a lightweight component.   There were several reasons why
the Component implementation was abandoned in favor of an alternative.

  <ol>
  <li>
  <p>There was barely had time to get the lightweight component support in the 
  1.1 version of the JDK.  There simply wasn't time to lighten up the component 
  further to where it would need to be to be used for text purposes.  The additions
  made to JComponent increased the memory consumption, and as it currently stands 
  it's much too heavy for representing text.
  </p>
  <li>
  <p>The layout semantics aren't quite right for text, and changing the current layout 
  semantics of component might break existing applications.
  </p>
  <li>
  <p>The component api uses integers, but in 1.2 one can use floating point device 
  independent coordinates.  An api that works in both 1.1 and 1.2 would be convenient 
  for minimizing transition difficulties.  The View class uses the Shape interface
  and float arguments to enable View implementations in JDK 1.2 and later while
  still functioning in the older 1.1 JDK.
  </p>
  </ol>

<p>
By default, a view is very light.  It contains a reference to the parent 
view from which it can fetch many things without holding state, and it 
contains a reference to a portion of the model (Element).  A view does not 
have to exactly represent an element in the model, that is simply a typical 
and therefore convenient mapping.  A view can alternatively maintain a couple 
of Position objects to maintain it's location in the model (i.e. represent 
a fragment of an element).  This is typically the result of formatting where 
views have been broken down into pieces.  The convenience of a substantial 
relationship to the element makes it easier to build factories to produce the 
views, and makes it easier  to keep track of the view pieces as the model is 
changed and the view must be changed to reflect the model.  Simple views 
therefore represent an Element directly and complex views do not.
<p>
A view has the following responsibilities:
  <dl>

    <dt><b>Participate in layout.</b>
    <dd>
    <p>The view has a setSize method which is like doLayout and setSize in 
    Component combined.  The view has a preferenceChanged method which is 
    like invalidate in Component except that one can invalidate just one axis 
    and the child requesting the change is identified.
    <p>A View expresses the size that it would like to be in terms of three
    values, a minimum, a preferred, and a maximum span.  Layout in a view is
    can be done independantly upon each axis.  For a properly functioning View
    implementation, the minimum span will be &lt;= the preferred span which in turn
    will be &lt;= the maximum span.
    </p>
    <p align=center><img src="View-flexibility.jpg">
    <p>The minimum set of methods for layout are:
    <ul>
    <li><a href="#getMinimumSpan">getMinimumSpan</a>
    <li><a href="#getPreferredSpan">getPreferredSpan</a>
    <li><a href="#getMaximumSpan">getMaximumSpan</a>
    <li><a href="#getAlignment">getAlignment</a>
    <li><a href="#preferenceChanged">preferenceChanged</a>
    <li><a href="#setSize">setSize</a>
    </ul>
  
  <p>The setSize method should be prepared to be called a number of times
    (i.e. It may be called even if the size didn't change).  The setSize method
    is generally called to make sure the View layout is complete prior to trying
    to perform an operation on it that requires an up-to-date layout.  A views
    size should <em>always</em> be set to a value within the minimum and maximum
    span specified by that view.  Additionally, the view must always call the
    preferenceChanged method on the parent if it has changed the values for the
    layout it would like, and expects the parent to honor.  The parent View is
    not required to recognize a change until the preferenceChanged has been sent.
    This allows parent View implementations to cache the child requirements if
    desired.  The calling sequence looks something like the following:
    </p>
    <p align=center><img src="View-layout.jpg">
    <p>The exact calling sequence is up to the layout functionality of
    the parent view (if the view has any children).  The view may collect
    the preferences of the children prior to determining what it will give 
    each child, or it might iteratively update the children one at a time.
    </p>

    <dt><b>Render a portion of the model.</b>
    <dd>
    <p>This is done in the paint method, which is pretty much like a component 
    paint method.  Views are expected to potentially populate a fairly large 
    tree.  A View has the following semantics for rendering:
    </p>
    <ul>
    <li>The view gets it's allocation from the parent at paint time, so it 
    must be prepared to redo layout if the allocated area is different from 
    what it is prepared to deal with.
    <li>The coordinate system is the same as the hosting Component (i.e. the
    Component returned by the <a href="#getContainer">getContainer</a> method).
    This means a child view lives in the same coordinate system as the parent
    view unless the parent has explicitly changed the coordinate system.
    To schedule itself to be repainted a view can call repaint on the hosting
    Component.
    <li>The default is to <em>not clip</em> the children.  It is more effecient
    to allow a view to clip only if it really feels it needs clipping.
    <li>The Graphics object given is not initialized in any way.  A view should
    set any settings needed.
    <li>A View is inherently transparent.  While a view may render into it's
    entire allocation, typically a view does not.  Rendering is performed by
    tranversing down the tree of View implementations.  Each View is responsible
    for rendering it's children.  This behavior is depended upon for thread
    safety.  While view implementations do not necessarily have to be implemented
    with thread safety in mind, other view implementations that do make use of
    concurrency can depend upon a tree traversal to guarantee thread safety.
    <li>The order of views relative to the model is up to the implementation.
    Although child views will typically be arranged in the same order that they
    occur in the model, they may be visually arranged in an entirely different 
    order.  View implementations may have Z-Order associated with them if the
    children are overlapping.
    </ul>
    <p>The methods for rendering are:
    <ul>
    <li><a href="#paint">paint</a>
    </ul>
    <p>

    <dt><b>Translate between the model and view coordinate systems.</b>
    <dd>
    <p>Because the view objects are produced from a factory and therefore cannot 
    necessarily be counted upon to be in a particular pattern, one must be able 
    to perform translation to properly locate spatial representation of the model.  
    The methods for doing this are:
    <ul>
    <li><a href="#modelToView">modelToView</a>
    <li><a href="#viewToModel">viewToModel</a>
    <li><a href="#getDocument">getDocument</a>
    <li><a href="#getElement">getElement</a>
    <li><a href="#getStartOffset">getStartOffset</a>
    <li><a href="#getEndOffset">getEndOffset</a>
    </ul>
    <p>The layout must be valid prior to attempting to make the translation.
    The translation is not valid, and must not be attempted while changes
    are being broadcasted from the model via a DocumentEvent.  
    </p>

    <dt><b>Respond to changes from the model.</b>
    <dd>
    <p>If the overall view is represented by many pieces (which is the best situation 
    if one want to be able to change the view and write the least amount of new code), 
    it would be impractical to have a huge number of DocumentListeners.  If each 
    view listened to the model, only a few would actually be interested in the 
    changes broadcasted at any given time.   Since the model has no knowledge of 
    views, it has no way to filter the broadcast of change information.  The view 
    hierarchy itself is instead responsible for propagating the change information.  
    At any level in the view hierarchy, that view knows enough about it's children to 
    best distribute the change information further.   Changes are therefore broadcasted 
    starting from the root of the view hierarchy.
    The methods for doing this are:
    <ul>
    <li><a href="#insertUpdate">insertUpdate</a>
    <li><a href="#removeUpdate">removeUpdate</a>
    <li><a href="#changedUpdate">changedUpdate</a>
    </ul>    
    <p>
 *
 * @author  Timothy Prinzing
 * @version 1.35 04/22/99
 */
public abstract class View implements SwingConstants {

    /**
     * Creates a new View object.
     *
     * @param elem the element to represent
     */
    public View(Element elem) {
	this.elem = elem;
    }

    /**
     * Returns the parent of the view.
     *
     * @return the parent, null if none
     */
    public View getParent() {
	return parent;
    }

    /**
     *  Returns a boolean that indicates whether
     *  the view is visible or not.  By default
     *  all views are visible.
     *
     * @return boolean value.
     */
    public boolean isVisible() {
	return true;
    }

	
    /**
     * Determines the preferred span for this view along an
     * axis.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @returns  the span the view would like to be rendered into.
     *           Typically the view is told to render into the span
     *           that is returned, although there is no guarantee.  
     *           The parent may choose to resize or break the view.
     * @see View#getPreferredSpan
     */
    public abstract float getPreferredSpan(int axis);

    /**
     * Determines the minimum span for this view along an
     * axis.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @returns  the minimum span the view can be rendered into.
     * @see View#getPreferredSpan
     */
    public float getMinimumSpan(int axis) {
	int w = getResizeWeight(axis);
	if (w == 0) {
	    // can't resize
	    return getPreferredSpan(axis);
	}
	return 0;
    }

    /**
     * Determines the maximum span for this view along an
     * axis.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @returns  the maximum span the view can be rendered into.
     * @see View#getPreferredSpan
     */
    public float getMaximumSpan(int axis) {
	int w = getResizeWeight(axis);
	if (w == 0) {
	    // can't resize
	    return getPreferredSpan(axis);
	}
	return Integer.MAX_VALUE;
    }
	
    /**
     * Child views can call this on the parent to indicate that
     * the preference has changed and should be reconsidered
     * for layout.  By default this just propagates upward to 
     * the next parent.  The root view will call 
     * <code>revalidate</code> on the associated text component.
     *
     * @param child the child view
     * @param width true if the width preference has changed
     * @param height true if the height preference has changed
     * @see javax.swing.JComponent#revalidate
     */
    public void preferenceChanged(View child, boolean width, boolean height) {
	View parent = getParent();
	if (parent != null) {
	    parent.preferenceChanged(this, width, height);
	}
    }

    /**
     * Determines the desired alignment for this view along an
     * axis.  By default this is simply centered.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @returns The desired alignment.  This should be a value
     *   >= 0.0 and <= 1.0 where 0 indicates alignment at the
     *   origin and 1.0 indicates alignment to the full span
     *   away from the origin.  An alignment of 0.5 would be the
     *   center of the view.
     */
    public float getAlignment(int axis) {
	return 0.5f;
    }

    /**
     * Renders using the given rendering surface and area on that
     * surface.  The view may need to do layout and create child
     * views to enable itself to render into the given allocation.
     *
     * @param g the rendering surface to use
     * @param allocation the allocated region to render into
     * @see View#paint
     */
    public abstract void paint(Graphics g, Shape allocation);

    /**
     * Establishes the parent view for this view.  This is
     * guaranteed to be called before any other methods if the
     * parent view is functioning properly.  This is also
     * the last method called, since it is called to indicate
     * the view has been removed from the hierarchy as 
     * well.  If this is reimplemented, 
     * <code>super.setParent()</code> should be called.
     *
     * @param parent the new parent, or null if the view is
     *  being removed from a parent it was previously added
     *  to
     */
    public void setParent(View parent) {
	this.parent = parent;
    }

    /** 
     * Returns the number of views in this view.  Since
     * the default is to not be a composite view this
     * returns 0.
     *
     * @return the number of views >= 0
     * @see View#getViewCount
     */
    public int getViewCount() {
	return 0;
    }

    /** 
     * Gets the nth child view.  Since there are no
     * children by default, this returns null.
     *
     * @param n the number of the view to get, >= 0 && < getViewCount()
     * @return the view
     */
    public View getView(int n) {
	return null;
    }

    /**
     * Fetches the allocation for the given child view. 
     * This enables finding out where various views
     * are located, without assuming the views store
     * their location.  This returns null since the
     * default is to not have any child views.
     *
     * @param index the index of the child, >= 0 && < getViewCount()
     * @param a  the allocation to this view.
     * @return the allocation to the child
     */
    public Shape getChildAllocation(int index, Shape a) {
	return null;
    }

    /**
     * Provides a way to determine the next visually represented model 
     * location that one might place a caret.  Some views may not be visible,
     * they might not be in the same order found in the model, or they just
     * might not allow access to some of the locations in the model.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @param direction the direction from the current position that can
     *  be thought of as the arrow keys typically found on a keyboard.
     *  This may be SwingConstants.WEST, SwingConstants.EAST, 
     *  SwingConstants.NORTH, or SwingConstants.SOUTH.  
     * @return the location within the model that best represents the next
     *  location visual position.
     * @exception BadLocationException
     * @exception IllegalArgumentException for an invalid direction
     */
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, 
					 int direction, Position.Bias[] biasRet) 
      throws BadLocationException {

	biasRet[0] = Position.Bias.Forward;
	switch (direction) {
	case NORTH:
	{
	    JTextComponent target = (JTextComponent) getContainer();
	    Rectangle r = target.modelToView(pos);
	    pos = Utilities.getPositionAbove(target, pos, r.x);
	}
	    break;
	case SOUTH:
	{
	    JTextComponent target = (JTextComponent) getContainer();
	    Rectangle r = target.modelToView(pos);
	    pos = Utilities.getPositionBelow(target, pos, r.x);
	}
	    break;
	case WEST:
	    if(pos == -1) {
		pos = Math.max(0, getEndOffset() - 1);
	    }
	    else {
		pos = Math.max(0, pos - 1);
	    }
	    break;
	case EAST:
	    if(pos == -1) {
		pos = getStartOffset();
	    }
	    else {
		pos = Math.min(pos + 1, getDocument().getLength());
	    }
	    break;
	default:
	    throw new IllegalArgumentException("Bad direction: " + direction);
	}
	return pos;
    }

    /**
     * Provides a mapping from the document model coordinate space
     * to the coordinate space of the view mapped to it.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @param b the bias toward the previous character or the
     *  next character represented by the offset, in case the 
     *  position is a boundary of two views. 
     * @return the bounding box of the given position is returned
     * @exception BadLocationException  if the given position does
     *   not represent a valid location in the associated document
     * @exception IllegalArgumentException for an invalid bias argument
     * @see View#viewToModel
     */
    public abstract Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException;

    /**
     * Provides a mapping from the document model coordinate space
     * to the coordinate space of the view mapped to it.
     *
     * @param p0 the position to convert >= 0
     * @param b0 the bias toward the previous character or the
     *  next character represented by p0, in case the 
     *  position is a boundary of two views. 
     * @param p1 the position to convert >= 0
     * @param b1 the bias toward the previous character or the
     *  next character represented by p1, in case the 
     *  position is a boundary of two views. 
     * @param a the allocated region to render into
     * @return the bounding box of the given position is returned
     * @exception BadLocationException  if the given position does
     *   not represent a valid location in the associated document
     * @exception IllegalArgumentException for an invalid bias argument
     * @see View#viewToModel
     */
    public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
	Shape s0 = modelToView(p0, a, b0);
	Shape s1;
	if (p1 == getEndOffset()) {
	    try {
		s1 = modelToView(p1, a, b1);
	    } catch (BadLocationException ble) {
		s1 = null;
	    }
	    if (s1 == null) {
		// Assume extends left to right.
		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a :
		                  a.getBounds();
		s1 = new Rectangle(alloc.x + alloc.width - 1, alloc.y,
				   1, alloc.height);
	    }
	}
	else {
	    s1 = modelToView(p1, a, b1);
	}
	Rectangle r0 = s0.getBounds();
	Rectangle r1 = (s1 instanceof Rectangle) ? (Rectangle) s1 :
	                                           s1.getBounds();
	if (r0.y != r1.y) {
	    // If it spans lines, force it to be the width of the view.
	    Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a :
		              a.getBounds();
	    r0.x = alloc.x;
	    r0.width = alloc.width;
	}
	r0.add(r1);
	return r0;
    }

    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.  The biasReturn argument will be
     * filled in to indicate that the point given is closer to the next
     * character in the model or the previous character in the model.
     *
     * @param x the X coordinate >= 0
     * @param y the Y coordinate >= 0
     * @param a the allocated region to render into
     * @return the location within the model that best represents the
     *  given point in the view >= 0.  The biasReturn argument will be
     * filled in to indicate that the point given is closer to the next
     * character in the model or the previous character in the model.
     */
    public abstract int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn);

    /**
     * Gives notification that something was inserted into the document 
     * in a location that this view is responsible for.
     *
     * @param e the change information from the associated document
     * @param a the current allocation of the view
     * @param f the factory to use to rebuild if the view has children
     * @see View#insertUpdate
     */
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    }

    /**
     * Gives notification from the document that attributes were removed 
     * in a location that this view is responsible for.
     *
     * @param e the change information from the associated document
     * @param a the current allocation of the view
     * @param f the factory to use to rebuild if the view has children
     * @see View#removeUpdate
     */
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    }

    /**
     * Gives notification from the document that attributes were changed
     * in a location that this view is responsible for.
     *
     * @param e the change information from the associated document
     * @param a the current allocation of the view
     * @param f the factory to use to rebuild if the view has children
     * @see View#changedUpdate
     */
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    }

    /**
     * Fetches the model associated with the view.
     *
     * @return the view model, null if none
     * @see View#getDocument
     */
    public Document getDocument() {
	return elem.getDocument();
    }

    /**
     * Fetches the portion of the model that this view is
     * responsible for.
     *
     * @return the starting offset into the model >= 0
     * @see View#getStartOffset
     */
    public int getStartOffset() {
	return elem.getStartOffset();
    }

    /**
     * Fetches the portion of the model that this view is
     * responsible for.
     *
     * @return the ending offset into the model >= 0
     * @see View#getEndOffset
     */
    public int getEndOffset() {
	return elem.getEndOffset();
    }

    /**
     * Fetches the structural portion of the subject that this
     * view is mapped to.  The view may not be responsible for the
     * entire portion of the element.
     *
     * @return the subject
     * @see View#getElement
     */
    public Element getElement() {
	return elem;
    }

    /**
     * Fetches the attributes to use when rendering.  By default
     * this simply returns the attributes of the associated element.
     * This method should be used rather than using the element
     * directly to obtain access to the attributes to allow
     * view-specific attributes to be mixed in or to allow the
     * view to have view-specific conversion of attributes by
     * subclasses.
     * Each view should document what attributes it recognizes
     * for the purpose of rendering or layout, and should always
     * access them through the AttributeSet returned by this method.
     */
    public AttributeSet getAttributes() {
	return elem.getAttributes();
    }

    /**
     * Tries to break this view on the given axis.  This is
     * called by views that try to do formatting of their
     * children.  For example, a view of a paragraph will
     * typically try to place its children into row and 
     * views representing chunks of text can sometimes be 
     * broken down into smaller pieces.
     * <p>
     * This is implemented to return the view itself, which
     * represents the default behavior on not being
     * breakable.  If the view does support breaking, the
     * starting offset of the view returned should be the
     * given offset, and the end offset should be less than
     * or equal to the end offset of the view being broken.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @param offset the location in the document model
     *   that a broken fragment would occupy >= 0.  This
     *   would be the starting offset of the fragment
     *   returned.
     * @param pos the position along the axis that the
     *  broken view would occupy >= 0.  This may be useful for
     *  things like tab calculations.
     * @param len specifies the distance along the axis
     *  where a potential break is desired >= 0.  
     * @return the fragment of the view that represents the
     *  given span, if the view can be broken.  If the view
     *  doesn't support breaking behavior, the view itself is
     *  returned.
     * @see ParagraphView
     */
    public View breakView(int axis, int offset, float pos, float len) {
	return this;
    }

    /**
     * Create a view that represents a portion of the element.
     * This is potentially useful during formatting operations
     * for taking measurements of fragments of the view.  If 
     * the view doesn't support fragmenting (the default), it 
     * should return itself.  
     *
     * @param p0 the starting offset >= 0.  This should be a value
     *   greater or equal to the element starting offset and
     *   less than the element ending offset.
     * @param p1 the ending offset > p0.  This should be a value
     *   less than or equal to the elements end offset and
     *   greater than the elements starting offset.
     * @returns the view fragment, or itself if the view doesn't
     *   support breaking into fragments.
     * @see LabelView
     */
    public View createFragment(int p0, int p1) {
	return this;
    }

    /**
     * Determines how attractive a break opportunity in 
     * this view is.  This can be used for determining which
     * view is the most attractive to call <code>breakView</code>
     * on in the process of formatting.  A view that represents
     * text that has whitespace in it might be more attractive
     * than a view that has no whitespace, for example.  The
     * higher the weight, the more attractive the break.  A
     * value equal to or lower than <code>BadBreakWeight</code>
     * should not be considered for a break.  A value greater
     * than or equal to <code>ForcedBreakWeight</code> should
     * be broken.
     * <p>
     * This is implemented to provide the default behavior
     * of returning <code>BadBreakWeight</code> unless the length
     * is greater than the length of the view in which case the 
     * entire view represents the fragment.  Unless a view has
     * been written to support breaking behavior, it is not
     * attractive to try and break the view.  An example of
     * a view that does support breaking is <code>LabelView</code>.
     * An example of a view that uses break weight is 
     * <code>ParagraphView</code>.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @param pos the potential location of the start of the 
     *   broken view >= 0.  This may be useful for calculating tab
     *   positions.
     * @param len specifies the relative length from <em>pos</em>
     *   where a potential break is desired >= 0.
     * @return the weight, which should be a value between
     *   ForcedBreakWeight and BadBreakWeight.
     * @see LabelView
     * @see ParagraphView
     * @see BadBreakWeight
     * @see GoodBreakWeight
     * @see ExcellentBreakWeight
     * @see ForcedBreakWeight
     */
    public int getBreakWeight(int axis, float pos, float len) {
	if (len > getPreferredSpan(axis)) {
	    return GoodBreakWeight;
	}
	return BadBreakWeight;
    }

    /**
     * Determines the resizability of the view along the
     * given axis.  A value of 0 or less is not resizable.
     *
     * @param axis View.X_AXIS or View.Y_AXIS
     * @return the weight
     */
    public int getResizeWeight(int axis) {
	return 0;
    }

    /**
     * Sets the size of the view.  This should cause 
     * layout of the view, if it has any layout duties.
     * The default is to do nothing.
     *
     * @param width the width >= 0
     * @param height the height >= 0
     */
    public void setSize(float width, float height) {
    }

    /**
     * Fetches the container hosting the view.  This is useful for
     * things like scheduling a repaint, finding out the host 
     * components font, etc.  The default implementation
     * of this is to forward the query to the parent view.
     *
     * @return the container, null if none
     */
    public Container getContainer() {
	View v = getParent();
	return (v != null) ? v.getContainer() : null;
    }

    /**
     * Fetches the ViewFactory implementation that is feeding
     * the view hierarchy.  Normally the views are given this
     * as an argument to updates from the model when they
     * are most likely to need the factory, but this
     * method serves to provide it at other times.
     *
     * @return the factory, null if none
     */
    public ViewFactory getViewFactory() {
	View v = getParent();
	return (v != null) ? v.getViewFactory() : null;
    }

    /**
     * The weight to indicate a view is a bad break
     * opportunity for the purpose of formatting.  This
     * value indicates that no attempt should be made to
     * break the view into fragments as the view has 
     * not been written to support fragmenting.
     * @see #getBreakWeight
     * @see GoodBreakWeight
     * @see ExcellentBreakWeight
     * @see ForcedBreakWeight
     */
    public static final int BadBreakWeight = 0;

    /**
     * The weight to indicate a view supports breaking,
     * but better opportunities probably exist.
     * 
     * @see #getBreakWeight
     * @see BadBreakWeight
     * @see GoodBreakWeight
     * @see ExcellentBreakWeight
     * @see ForcedBreakWeight
     */
    public static final int GoodBreakWeight = 1000;

    /**
     * The weight to indicate a view supports breaking,
     * and this represents a very attractive place to
     * break.
     *
     * @see #getBreakWeight
     * @see BadBreakWeight
     * @see GoodBreakWeight
     * @see ExcellentBreakWeight
     * @see ForcedBreakWeight
     */
    public static final int ExcellentBreakWeight = 2000;

    /**
     * The weight to indicate a view supports breaking,
     * and must be broken to be represented properly 
     * when placed in a view that formats it's children
     * by breaking them.
     *
     * @see #getBreakWeight
     * @see BadBreakWeight
     * @see GoodBreakWeight
     * @see ExcellentBreakWeight
     * @see ForcedBreakWeight
     */
    public static final int ForcedBreakWeight = 3000;

    /**
     * Axis for format/break operations.
     */
    public static final int X_AXIS = HORIZONTAL;

    /**
     * Axis for format/break operations.
     */
    public static final int Y_AXIS = VERTICAL;

    /**
     * Provides a mapping from the document model coordinate space
     * to the coordinate space of the view mapped to it. This is 
     * implemented to default the bias to Position.Bias.Forward
     * which was previously implied.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @return the bounding box of the given position is returned
     * @exception BadLocationException  if the given position does
     *   not represent a valid location in the associated document
     * @see View#modelToView
     * @deprecated
     */
    public Shape modelToView(int pos, Shape a) throws BadLocationException {
	return modelToView(pos, a, Position.Bias.Forward);
    }


    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.
     *
     * @param x the X coordinate >= 0
     * @param y the Y coordinate >= 0
     * @param a the allocated region to render into
     * @return the location within the model that best represents the
     *  given point in the view >= 0
     * @see View#viewToModel
     * @deprecated
     */
    public int viewToModel(float x, float y, Shape a) {
	sharedBiasReturn[0] = Position.Bias.Forward;
	return viewToModel(x, y, a, sharedBiasReturn);
    }

    // static argument available for viewToModel calls since only
    // one thread at a time may call this method.
    static final Position.Bias[] sharedBiasReturn = new Position.Bias[1];

    private View parent;
    private Element elem;

};

