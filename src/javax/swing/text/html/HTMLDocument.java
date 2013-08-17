/*
 * @(#)HTMLDocument.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Component;
import java.util.*;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A document that models html.  The purpose of this model
 * is to support both browsing and editing.  As a result,
 * the structure described by an html document is not 
 * exactly replicated by default.  The element structure that 
 * is modeled by default, is built by the class
 * <code>HTMLDocument.HTMLReader</code>, which implements
 * the <code>HTMLEditorKit.ParserCallback</code> protocol
 * that the parser expects.  To change the structure one
 * can subclass HTMLReader, and reimplement the method
 * <a href="#getReader">getReader</a> to return the new
 * reader implementation.  The documentation for 
 * HTMLReader should be consulted for the details of
 * the default structure created.  The intent is that 
 * the document be non-lossy (although reproducing the
 * html format may result in a different format).
 * <p>
 * The document models only html, and makes no attempt to
 * store view attributes in it.  The elements are identified
 * by the <code>StyleContext.NameAttribute</code> attribute,
 * which should always have a value of type <code>HTML.Tag</code>
 * that identifies the kind of element.  Some of the elements
 * are (such as comments) are synthesized.  The HTMLFactory
 * uses this attribute to determine what kind of view to build.
 * <p>
 * This document supports incremental loading.  The
 * <code>TokenThreshold</code> property controls how
 * much of the parse is buffered before trying to update
 * the element structure of the document.  This property
 * is set by the EditorKit so that subclasses can disable
 * it.
 * <p>
 * The <code>Base</code> property determines the URL
 * against which relative URL's are resolved against.
 * By default, this will be the 
 * <code>Document.StreamDescriptionProperty</code> if
 * the value of the property is a URL.  If a &lt;base&gt;
 * tag is encountered, the base will become the URL specified
 * by that tag.  Because the base URL is a property, it
 * can of course be set directly.
 * <p>
 * The default content storage mechanism for this document
 * is a gap buffer (GapContent).  Alternatives can be supplied
 * by using the constructor that takes a Content implementation.
 *
 * @author  Timothy Prinzing
 * @author  Sunita Mani
 * @version 1.115 04/22/99
 */
public class HTMLDocument extends DefaultStyledDocument {

    /**
     * Constructs an html document.
     */
    public HTMLDocument() {
	this(new GapContent(BUFFER_SIZE_DEFAULT), new StyleSheet());
    }
    
    /**
     * Constructs an html document with the default content
     * storage implementation and the given style/attribute
     * storage mechanism.
     *
     * @param styles the styles
     */
    public HTMLDocument(StyleSheet styles) {
	this(new GapContent(BUFFER_SIZE_DEFAULT), styles);
    }

    /**
     * Constructs an html document with the given content
     * storage implementation and the given style/attribute
     * storage mechanism.
     *
     * @param c  the container for the content
     * @param styles the styles
     */
    public HTMLDocument(Content c, StyleSheet styles) {
        super(c, styles);
    }

    /**
     * Fetch the reader for the parser to use to load the document
     * with html.  This is implemented to return an instance of
     * HTMLDocument.HTMLReader.  Subclasses can reimplement this
     * method to change how the document get structured if desired
     * (e.g. to handle custom tags, structurally represent character
     * style elements, etc.).
     */
    public HTMLEditorKit.ParserCallback getReader(int pos) {
	Object desc = getProperty(Document.StreamDescriptionProperty);
	if (desc instanceof URL) { 
	    base = (URL) desc;
	}
	HTMLReader reader = new HTMLReader(pos);
	return reader;
    }

    /**
     * Fetch the reader for the parser to use to load the document
     * with html.  This is implemented to return an instance of
     * HTMLDocument.HTMLReader.  Subclasses can reimplement this
     * method to change how the document get structured if desired
     * (e.g. to handle custom tags, structurally represent character
     * style elements, etc.).
     *
     * @param popDepth number of ElementSpec.EndTagType to generate before
     *        inserting.
     * @param pushDepth number of ElementSpec.StartTagType with a direction
     *        of ElementSpec.JoinNextDirection that should be generated
     *        before inserting, but after the end tags have been generated.
     * @param insertTag first tag to start inserting into document.
     */
    public HTMLEditorKit.ParserCallback getReader(int pos, int popDepth,
						  int pushDepth,
						  HTML.Tag insertTag) {
	return getReader(pos, popDepth, pushDepth, insertTag, true);
    }

    /**
     * Fetch the reader for the parser to use to load the document
     * with html.  This is implemented to return an instance of
     * HTMLDocument.HTMLReader.  Subclasses can reimplement this
     * method to change how the document get structured if desired
     * (e.g. to handle custom tags, structurally represent character
     * style elements, etc.).
     *
     * @param popDepth number of ElementSpec.EndTagType to generate before
     *        inserting.
     * @param pushDepth number of ElementSpec.StartTagType with a direction
     *        of ElementSpec.JoinNextDirection that should be generated
     *        before inserting, but after the end tags have been generated.
     * @param insertTag first tag to start inserting into document.
     * @param insertInsertTag if false, all the Elements after insertTag will
     *        be inserted, otherwise insertTag will be inserted.
     */
    HTMLEditorKit.ParserCallback getReader(int pos, int popDepth,
					   int pushDepth,
					   HTML.Tag insertTag,
					   boolean insertInsertTag) {
	Object desc = getProperty(Document.StreamDescriptionProperty);
	if (desc instanceof URL) { 
	    base = (URL) desc;
	    getStyleSheet().setBase(base);
	}
	HTMLReader reader = new HTMLReader(pos, popDepth, pushDepth,
					   insertTag, insertInsertTag);
	return reader;
    }

    /**
     * Get the location to resolve relative url's against.  By
     * default this will be the documents url if the document
     * was loaded from a url.  If a base tag is found and
     * can be parsed, it will be used as the base location.
     */
    public URL getBase() {
	return base;
    }

    /**
     * Set the location to resolve relative url's against.  By
     * default this will be the documents url if the document
     * was loaded from a url.  If a base tag is found and
     * can be parsed, it will be used as the base location.
     */
    public void setBase(URL u) {
	base = u;
    }

    /**
     * Inserts new elements in bulk.  This is how elements get created
     * in the document.  The parsing determines what structure is needed
     * and creates the specification as a set of tokens that describe the
     * edit while leaving the document free of a write-lock.  This method
     * can then be called in bursts by the reader to acquire a write-lock
     * for a shorter duration (i.e. while the document is actually being
     * altered).
     *
     * @param offset the starting offset
     * @data the element data
     * @exception BadLocationException for an invalid starting offset
     * @see StyledDocument#insert
     * @exception BadLocationException  if the given position does not 
     *   represent a valid location in the associated document.
     */
    protected void insert(int offset, ElementSpec[] data) throws BadLocationException {
	super.insert(offset, data);
    }

    /**
     * Updates document structure as a result of text insertion.  This
     * will happen within a write lock.  This implementation simply
     * parses the inserted content for line breaks and builds up a set
     * of instructions for the element buffer.
     *
     * @param chng a description of the document change
     * @param attr the attributes
     */
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
	if(attr == null) {
	    attr = contentAttributeSet;
	}

	// If this is the composed text element, merge the content attribute to it
	else if (attr.isDefined(StyleConstants.ComposedTextAttribute)) {
	    ((MutableAttributeSet)attr).addAttributes(contentAttributeSet);
	}

	super.insertUpdate(chng, attr);
    }

    /**
     * Replaces the contents of the document with the given
     * element specifications.  This is called before insert if
     * the loading is done in bursts.  This is the only method called
     * if loading the document entirely in one burst.
     */
    protected void create(ElementSpec[] data) {
	super.create(data);
    }

    /**
     * Sets attributes for a paragraph.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see 
     * <A HREF="http://java.sun.com/products/jfc/swingdoc-archive/threads.html">Threads
     * and Swing</A> for more information.     
     *
     * @param offset the offset into the paragraph >= 0
     * @param length the number of characters affected >= 0
     * @param s the attributes
     * @param replace whether to replace existing attributes, or merge them
     */
    public void setParagraphAttributes(int offset, int length, AttributeSet s, 
				       boolean replace) {
	try {
	    writeLock();
	    // Make sure we send out a change for the length of the paragraph.
	    int end = Math.min(offset + length, getLength());
	    Element e = getParagraphElement(offset);
	    offset = e.getStartOffset();
	    e = getParagraphElement(end);
	    length = Math.max(0, e.getEndOffset() - offset);
	    DefaultDocumentEvent changes = 
		new DefaultDocumentEvent(offset, length,
					 DocumentEvent.EventType.CHANGE);
	    AttributeSet sCopy = s.copyAttributes();
	    int lastEnd = Integer.MAX_VALUE;
	    for (int pos = offset; pos <= end; pos = lastEnd) {
		Element paragraph = getParagraphElement(pos);
		if (lastEnd == paragraph.getEndOffset()) {
		    lastEnd++;
		}
		else {
		    lastEnd = paragraph.getEndOffset();
		}
		MutableAttributeSet attr = 
		    (MutableAttributeSet) paragraph.getAttributes();
		changes.addEdit(new AttributeUndoableEdit(paragraph, sCopy, replace));
		if (replace) {
		    attr.removeAttributes(attr);
		}
		attr.addAttributes(s);
	    }
	    changes.end();
	    fireChangedUpdate(changes);
	    fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
	} finally {
	    writeUnlock();
	}
    }

    /**
     * Fetch the StyleSheet with the document-specific display
     * rules(CSS) that were specified in the html document itself.
     */
    public StyleSheet getStyleSheet() {
	return (StyleSheet) getAttributeContext();
    }

    /**
     * Fetch an iterator for the following kind of html tag.
     * This can be used for things like iterating over the
     * set of anchors contained, iterating over the input
     * elements, etc.
     */
    public Iterator getIterator(HTML.Tag t) {
	if (t.isBlock()) {
	    // TBD
	    return null;
	}
	return new LeafIterator(t, this);
    }

    /**
     * Creates a document leaf element that directly represents
     * text (doesn't have any children).  This is implemented
     * to return an element of type 
     * <code>HTMLDocument.RunElement</code>.
     *
     * @param parent the parent element
     * @param a the attributes for the element
     * @param p0 the beginning of the range >= 0
     * @param p1 the end of the range >= p0
     * @return the new element
     */
    protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
	return new RunElement(parent, a, p0, p1);
    }

    /**
     * Creates a document branch element, that can contain other elements.
     * This is implemented to return an element of type 
     * <code>HTMLDocument.BlockElement</code>.
     *
     * @param parent the parent element
     * @param a the attributes
     * @return the element
     */
    protected Element createBranchElement(Element parent, AttributeSet a) {
	return new BlockElement(parent, a);
    }

    /**
     * Creates the root element to be used to represent the
     * default document structure.
     *
     * @return the element base
     */
    protected AbstractElement createDefaultRoot() {
	// grabs a write-lock for this initialization and
	// abandon it during initialization so in normal
	// operation we can detect an illegitimate attempt
	// to mutate attributes.
	writeLock();
	MutableAttributeSet a = new SimpleAttributeSet();
	a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.HTML);
	BlockElement html = new BlockElement(null, a.copyAttributes());
	a.removeAttributes(a);
	a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.BODY);
	BlockElement body = new BlockElement(html, a.copyAttributes());
	a.removeAttributes(a);
	a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
	BlockElement paragraph = new BlockElement(body, a.copyAttributes());
	a.removeAttributes(a);
	a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
	RunElement brk = new RunElement(paragraph, a, 0, 1);
	Element[] buff = new Element[1];
	buff[0] = brk;
	paragraph.replace(0, 0, buff);
	buff[0] = paragraph;
	body.replace(0, 0, buff);
	buff[0] = body;
	html.replace(0, 0, buff);
	writeUnlock();
	return html;
    }

    /**
     * Set the number of tokens to buffer before trying to update
     * the documents element structure.
     */
    public void setTokenThreshold(int n) {
	putProperty(TokenThreshold, new Integer(n));
    }

    /**
     * Get the number of tokens to buffer before trying to update
     * the documents element structure.  By default, this will
     * be <code>Integer.MAX_VALUE</code>.
     */
    public int getTokenThreshold() {
	Integer i = (Integer) getProperty(TokenThreshold);
	if (i != null) {
	    return i.intValue();
	}
	return Integer.MAX_VALUE;
    }

    /**
     * Sets how unknown tags are handled. If set to true, unknown
     * tags are put in the model, otherwise they are dropped.
     */
    public void setPreservesUnknownTags(boolean preservesTags) {
	preservesUnknownTags = preservesTags;
    }

    /**
     * @return true if unknown tags are to be preserved when parsing.
     */
    public boolean getPreservesUnknownTags() {
	return preservesUnknownTags;
    }

    /**
     * This method is responsible for processing HyperlinkEvent's that
     * are generated by documents in an HTML frame.  The HyperlinkEvent
     * type, as the parameter suggests, is HTMLFrameHyperlinkEvent.
     * In addition to the typical information contained in a HyperlinkEvent,
     * this event contains the element that corresponds to the frame in
     * which the click happened, (i.e. the source element) and the
     * target name.  The target name has 4 possible values:
     *    1) _self
     *	  2) _parent
     *    3) _top
     *	  4) a named frame
     *
     * If target is _self, the action is to change the value of the
     * HTML.Attribute.SRC attribute and fires a ChangedUpdate event.
     *
     * If the target is _parent, then it deletes the parent element,
     * which is a <frameset> element, and inserts a new <frame> element
     * and sets its HTML.Attribute.SRC attribute to have a value equal
     * to the destination url and fire an RemovedUpdate and InsertUpdate.
     *
     * If the target is _top, this method does nothing. In the implementation
     * of the view for a frame, namely the FrameView, the processing of _top
     * is handled.  Given that _top implies replacing the entire document,
     * it made sense to handle this outside of the document that it will 
     * replace.
     *
     * If the target is a named frame, then the element hierarchy is searched
     * for an element with a name equal to the target, its HTML.Attribute.SRC
     * attribute is updated and a ChangedUpdate event is fired.
     *
     * @param HTMLFrameHyperLinkEvent
     */
    public void processHTMLFrameHyperlinkEvent(HTMLFrameHyperlinkEvent e) {
 	String frameName = e.getTarget();
	Element element = e.getSourceElement();
	String urlStr = e.getURL().toString();

	

	if (frameName.equals("_self")) {
	    /*
	      The source and destination elements
	      are the same.
	    */
	    updateFrame(element, urlStr);
	} else if (frameName.equals("_parent")) {
	    /*
	      The destination is the parent of the frame.
	    */
	    updateFrameSet(element.getParentElement(), urlStr);
	} else {
	    /*
	      locate a named frame
	    */
	    Element targetElement = findFrame(frameName);
	    if (targetElement != null) {
		updateFrame(targetElement, urlStr);
	    }
	}
    }

    
    /**
     * Searches the element hierarchy for an FRAME element
     * that has its name attribute equal to the frameName
     *
     * @param frameName
     * @return element the element whose NAME attribute has
     *         a value of frameName.  returns null if not
     *         found.
     */
    private Element findFrame(String frameName) {
	ElementIterator it = new ElementIterator(this);
	Element next = null;

	while ((next = it.next()) != null) {
	    AttributeSet attr = next.getAttributes();
	    if (matchNameAttribute(attr, HTML.Tag.FRAME)) {
		String frameTarget = (String)attr.getAttribute(HTML.Attribute.NAME);
		if (frameTarget.equals(frameName)) {
		    break;
		}
	    }
	}
	return next;
    }

    /**
     * This method return true if the StyleConstants.NameAttribute is
     * equal to the tag that is passed in as a parameter.
     *
     */
    boolean matchNameAttribute(AttributeSet attr, HTML.Tag tag) {
	Object o = attr.getAttribute(StyleConstants.NameAttribute);
	if (o instanceof HTML.Tag) {
	    HTML.Tag name = (HTML.Tag) o;
	    if (name == tag) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Replaces a frameset branch Element with a frame leaf element.
     *
     * @param element the frameset element to remove.
     * @param url     the value for the SRC attribute for the
     *                new frame that will replace the frameset.
     */
    private void updateFrameSet(Element element, String url) {
	try {
	    int startOffset = element.getStartOffset();
	    int endOffset = element.getEndOffset();
	    remove(startOffset, endOffset - startOffset);
	    SimpleAttributeSet attr = new SimpleAttributeSet();
	    attr.addAttribute(HTML.Attribute.SRC, url);
	    attr.addAttribute(StyleConstants.NameAttribute, HTML.Tag.FRAME);
	    insertString(startOffset, " ", attr);
	} catch (BadLocationException e1) {
	    // Should handle this better
	}
    }


    /**
     * Updates the Frame elements HTML.Attribute.SRC attribute and
     * fires a ChangedUpdate event.
     *
     * @param element a FRAME element whose SRC attribute will be updated.
     * @param url     that has the new value for the SRC attribute
     */
    private void updateFrame(Element element, String url) {

	try {
	    writeLock();
	    DefaultDocumentEvent changes = new DefaultDocumentEvent(element.getStartOffset(),
								    1,
								    DocumentEvent.EventType.CHANGE);
	    AttributeSet sCopy = element.getAttributes().copyAttributes();
	    MutableAttributeSet attr = (MutableAttributeSet) element.getAttributes();
	    changes.addEdit(new AttributeUndoableEdit(element, sCopy, false));
	    attr.removeAttribute(HTML.Attribute.SRC);
	    attr.addAttribute(HTML.Attribute.SRC, url);
	    changes.end();
	    fireChangedUpdate(changes);
	    fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
	} finally {
	    writeUnlock();
	}
    }


    /**
     * Returns true if the document will be viewed in a frame.
     */
    boolean isFrameDocument() {
	return frameDocument;
    }
    
    /**
     * Sets a boolean state about whether the document will be
     * viewed in a frame.
     */
    void setFrameDocumentState(boolean frameDoc) {
 	this.frameDocument = frameDoc;
    }

    /**
     * Adds the specified map, this will remove a Map that has been
     * previously registered with the same name.
     */
    void addMap(Map map) {
	String     name = map.getName();

	if (name != null) {
	    Object     maps = getProperty(MAP_PROPERTY);

	    if (maps == null) {
		maps = new Hashtable(11);
		putProperty(MAP_PROPERTY, maps);
	    }
	    if (maps instanceof Hashtable) {
		((Hashtable)maps).put("#" + name, map);
	    }
	}
    }

    /**
     * Removes a previously registered map.
     */
    void removeMap(Map map) {
	String     name = map.getName();

	if (name != null) {
	    Object     maps = getProperty(MAP_PROPERTY);

	    if (maps instanceof Hashtable) {
		((Hashtable)maps).remove("#" + name);
	    }
	}
    }

    /**
     * Returns the Map associated with the given name.
     */
    Map getMap(String name) {
	if (name != null) {
	    Object     maps = getProperty(MAP_PROPERTY);

	    if (maps != null && (maps instanceof Hashtable)) {
		return (Map)((Hashtable)maps).get(name);
	    }
	}
	return null;
    }

    /**
     * Returns an Enumeration of the possible Maps.
     */
    Enumeration getMaps() {
	Object     maps = getProperty(MAP_PROPERTY);

	if (maps instanceof Hashtable) {
	    return ((Hashtable)maps).elements();
	}
	return null;
    }

    /**
     * Sets the content type language used for style sheets that do not
     * explicitly specify the type. The default is text/css.
     */
    /* public */
    void setDefaultStyleSheetType(String contentType) {
	putProperty(StyleType, contentType);
    }

    /**
     * Returns the content type language used for style sheets. The default
     *  is text/css.
     */
    /* public */
    String getDefaultStyleSheetType() {
	String retValue = (String)getProperty(StyleType);
	if (retValue == null) {
	    return "text/css";
	}
	return retValue;
    }

    //
    // Methods for inserting arbitrary HTML. At some point these
    // will be made public.
    //

    /** 
     * Replaces the children of the given element with the contents 
     * specified as an html string. 
     */ 
    void setInnerHTML(Element elem, String htmlText) throws
	             BadLocationException, IOException {
	if (elem != null && htmlText != null) {
	    int oldCount = elem.getElementCount();
	    // Insert at the end, and remove from the start. If we insert
	    // at the beginning, the remove from the end will force a join,
	    // which we do NOT want.
	    insertHTML(elem, elem.getEndOffset(), htmlText, null);
	    if (elem.getElementCount() > oldCount) {
		int start = elem.getStartOffset();
		int end = elem.getElement(oldCount).getStartOffset();
		remove(start, end - start);
	    }
	}
    }

    /** 
     * Replaces the given element in the parent with the contents 
     * specified as an html string. 
     */ 
    void setOuterHTML(Element elem, String htmlText) throws
	                    BadLocationException, IOException {
	if (elem != null && elem.getParentElement() != null &&
	    htmlText != null) {
	    insertHTML(elem.getParentElement(), elem.getStartOffset(),
		       htmlText, null);
	    // Remove old.
	    remove(elem.getStartOffset(), elem.getEndOffset() -
		   elem.getStartOffset());
	}
    }

    /** 
     * Inserts the html specified as a string at the start 
     * of the element. 
     */ 
    void insertAfterStart(Element elem, String htmlText) throws
	            BadLocationException, IOException {
	insertHTML(elem, elem.getStartOffset(), htmlText, null);
    }

    /** 
     * Inserts the html specified as a string at the end of 
     * the element. 
     */ 
    void insertBeforeEnd(Element elem, String htmlText) throws
	             BadLocationException, IOException {
	insertHTML(elem, elem.getEndOffset(), htmlText, null);
    }

    /** 
     * Inserts the html specified as string before the start of 
     * the given element. 
     */ 
    void insertBeforeStart(Element elem, String htmlText) throws
	             BadLocationException, IOException {
	if (elem != null) {
	    Element parent = elem.getParentElement();

	    if (parent != null) {
		insertHTML(parent, elem.getStartOffset(), htmlText, null);
	    }
	}
    }

    /** 
     * Inserts the html specified as a string after the 
     * the end of the given element. 
     */ 
    void insertAfterEnd(Element elem, String htmlText) throws
	            BadLocationException, IOException {
	if (elem != null) {
	    Element parent = elem.getParentElement();

	    if (parent != null) {
		insertHTML(parent, elem.getEndOffset(), htmlText, null);
	    }
	}
    }

    /** 
     * Fetch the element that has the given id attribute. 
     * If the element can't be found, null is returned. This is not threadsafe.
     */ 
    Element getElementByID(String id) {
	if (id == null) {
	    return null;
	}
	return getElementWithAttribute(getDefaultRootElement(),
				       HTML.Attribute.ID, id);
    }

    /**
     * Returns the child element of <code>e</code> that contains the
     * attribute, <code>attribute</code> with value <code>value</code>, or
     * null if one isn't found. This is not threadsafe.
     */
    Element getElementWithAttribute(Element e, Object attribute,
				    Object value) {
	AttributeSet attr = e.getAttributes();

	if (attr != null && attr.isDefined(attribute)) {
	    if (value.equals(attr.getAttribute(attribute))) {
		return e;
	    }
	}
	if (!e.isLeaf()) {
	    for (int counter = 0, maxCounter = e.getElementCount();
		 counter < maxCounter; counter++) {
		Element retValue = getElementWithAttribute
		                   (e.getElement(counter), attribute, value);

		if (retValue != null) {
		    return retValue;
		}
	    }
	}
	return null;
    }

    /**
     * Insets a string of HTML into the document at the given position.
     * <code>parent</code> is used to identify the tag to look for in
     * <code>html</code> (unless <code>insertTag</code>, in which case it
     * is used). If <code>parent</code> is a leaf this can have
     * unexpected results.
     */
    void insertHTML(Element parent, int offset, String html,
		    HTML.Tag insertTag) throws BadLocationException,
	                                          IOException {
	if (parent != null && html != null) {
	    // Determine the tag we are to look for in html.
	    Object name = (insertTag != null) ? insertTag : 
                                 parent.getAttributes().getAttribute
		                 (StyleConstants.NameAttribute);
	    HTMLEditorKit.Parser parser = getParser();

	    if (parser != null && name != null && (name instanceof HTML.Tag)) {
		int lastOffset = Math.max(0, offset - 1);
		Element charElement = getCharacterElement(lastOffset);
		Element commonParent = parent;
		int pop = 0;
		int push = 0;

		if (parent.getStartOffset() > lastOffset) {
		    while (commonParent != null &&
			   commonParent.getStartOffset() > lastOffset) {
			commonParent = commonParent.getParentElement();
			push++;
		    }
		    if (commonParent == null) {
			throw new BadLocationException("No common parent",
						       offset);
		    }
		}
		while (charElement != null && charElement != commonParent) {
		    pop++;
		    charElement = charElement.getParentElement();
		}
		if (charElement != null) {
		    // Found it, do the insert.
		    HTMLEditorKit.ParserCallback callback = getReader
			               (offset, pop - 1, push, (HTML.Tag)name,
					(insertTag != null));

		    parser.parse(new StringReader(html), callback, true);
		    callback.flush();
		}
	    }
	}
    }

    /**
     * Returns the parser to use. This comes from the property with
     * the name PARSER_PROPERTY, and may be null.
     */
    HTMLEditorKit.Parser getParser() {
	Object p = getProperty(PARSER_PROPERTY);

	if (p instanceof HTMLEditorKit.Parser) {
	    return (HTMLEditorKit.Parser)p;
	}
	return null;
    }


    // These two are provided for inner class access. The are named different
    // than the super class as the super class implementations are final.
    void obtainLock() {
	writeLock();
    }

    void releaseLock() {
	writeUnlock();
    }

    //
    // Provided for inner class access.
    //

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param e the event
     * @see EventListenerList
     */
    protected void fireChangedUpdate(DocumentEvent e) {
	super.fireChangedUpdate(e);
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param e the event
     * @see EventListenerList
     */
    protected void fireUndoableEditUpdate(UndoableEditEvent e) {
	super.fireUndoableEditUpdate(e);
    }

    /*
     * state defines whether the document is a frame document
     * or not.
     */
    private boolean frameDocument = false;
    private boolean preservesUnknownTags = true;

    /*
     * Used to store a button group for radio buttons in
     * a form.
     */
    private ButtonGroup radioButtonGroup;

    /**
     * Document property for the number of tokens to buffer 
     * before building an element subtree to represent them.
     */
    static final String TokenThreshold = "token threshold";


    /**
     * Document property key value. The value for the key will be a Vector
     * of Strings that are comments not found in the body.
     */
    public static final String AdditionalComments = "AdditionalComments";

    /**
     * Document property key value. The value for the key will be a 
     * String indicating the default type of stylesheet links.
     */
    /* public */ static final String StyleType = "StyleType";

    /**
     * The location to resolve relative url's against.  By
     * default this will be the documents url if the document
     * was loaded from a url.  If a base tag is found and
     * can be parsed, it will be used as the base location.
     */
    URL base;

    /**
     * Used for inserts when a null AttributeSet is supplied.
     */
    private static AttributeSet contentAttributeSet;

    /**
     * Property Maps are registered under, will be a Hashtable.
     */
    static String MAP_PROPERTY = "__MAP__";

    /**
     * Property the Parser is stored in. This is need by the methods that
     * insert HTML.
     */
    static String PARSER_PROPERTY = "__PARSER__";

    /**
     * Parser will callback with this tag to indicate what the end of line
     * string is. This is temporary until new API is added to indicate the
     * end of line string.
     */
    static HTML.UnknownTag EndOfLineTag;

    private static char[] NEWLINE;


    static {
	contentAttributeSet = new SimpleAttributeSet();
	((MutableAttributeSet)contentAttributeSet).
	                addAttribute(StyleConstants.NameAttribute,
				     HTML.Tag.CONTENT);
	NEWLINE = new char[1];
	NEWLINE[0] = '\n';
	EndOfLineTag = new HTML.UnknownTag("__EndOfLineTag__");
    }


    /**
     * An iterator to iterate over a particular type of
     * tag.  The iterator is not thread safe.  If reliable
     * access to the document is not already ensured by
     * the context under which the iterator is being used,
     * it's use should be performed under the protection of
     * Document.render. 
     */
    public static abstract class Iterator {

	/**
	 * Fetch the attributes for this tag.
	 */
	public abstract AttributeSet getAttributes();

	/**
	 * Start of the range for which the current occurence of
	 * the tag is defined and has the same attributes.
	 */
	public abstract int getStartOffset();
	
	/**
	 * End of the range for which the current occurence of
	 * the tag is defined and has the same attributes.
	 */
	public abstract int getEndOffset();

	/**
	 * Move the iterator forward to the next occurence
	 * of the tag it represents.
	 */
	public abstract void next();

	/**
	 * Indicates if the iterator is currently
	 * representing an occurence of a tag.  If
	 * false there are no more tags for this iterator.
	 */
	public abstract boolean isValid();

	/**
	 * Type of tag this iterator represents.
	 */
	public abstract HTML.Tag getTag();
    }

    /**
     * An iterator to iterate over a particular type of
     * tag.
     */
    static class LeafIterator extends Iterator {

	LeafIterator(HTML.Tag t, Document doc) {
	    tag = t;
	    pos = new ElementIterator(doc);
	    endOffset = 0;
	    next();
	}

	/**
	 * Fetch the attributes for this tag.
	 */
	public AttributeSet getAttributes() {
	    Element elem = pos.current();
	    if (elem != null) {
		AttributeSet a = (AttributeSet) 
		    elem.getAttributes().getAttribute(tag);
		return a;
	    }
	    return null;
	}

	/**
	 * Start of the range for which the current occurence of
	 * the tag is defined and has the same attributes.
	 */
	public int getStartOffset() {
	    Element elem = pos.current();
	    if (elem != null) {
		return elem.getStartOffset();
	    }
	    return -1;
	}
	
	/**
	 * End of the range for which the current occurence of
	 * the tag is defined and has the same attributes.
	 */
	public int getEndOffset() {
	    return endOffset;
	}

	/**
	 * Move the iterator forward to the next occurence
	 * of the tag it represents.
	 */
	public void next() {
	    for (nextLeaf(pos); isValid(); nextLeaf(pos)) {
		Element elem = pos.current();
		if (elem.getStartOffset() >= endOffset) {
		    AttributeSet a = pos.current().getAttributes();
		    if (a.isDefined(tag)) {
			// we found the next one
			setEndOffset();
			break;
		    }
		}
	    }
	}

	/**
	 * Type of tag this iterator represents.
	 */
        public HTML.Tag getTag() {
	    return tag;
	}

	public boolean isValid() {
	    return (pos.current() != null);
	}

	/**
	 * Move the given iterator to the next leaf element.
	 */
	void nextLeaf(ElementIterator iter) {
	    for (iter.next(); iter.current() != null; iter.next()) {
		Element e = iter.current();
		if (e.isLeaf()) {
		    break;
		}
	    }
	}

	/**
	 * March a cloned iterator forward to locate the end
	 * of the run.  This sets the value of endOffset.
	 */
	void setEndOffset() {
	    AttributeSet a0 = getAttributes();
	    endOffset = pos.current().getEndOffset();
	    ElementIterator fwd = (ElementIterator) pos.clone();
	    for (nextLeaf(fwd); fwd.current() != null; nextLeaf(fwd)) {
		Element e = fwd.current();
		AttributeSet a1 = (AttributeSet) e.getAttributes().getAttribute(tag);
		if ((a1 == null) || (! a1.equals(a0))) {
		    break;
		}
		endOffset = e.getEndOffset();
	    }
	}

	private int endOffset;
	private HTML.Tag tag;
	private ElementIterator pos;

    }

    /**
     * An html reader to load an html document with an html
     * element structure.  This is a set of callbacks from
     * the parser, implemented to create a set of elements
     * tagged with attributes.  The parse builds up tokens
     * (ElementSpec) that describe the element subtree desired,
     * and burst it into the document under the protection of
     * a write lock using the insert method on the document
     * outer class.
     * <p>
     * The reader can be configured by registering actions
     * (of type <code>HTMLDocument.HTMLReader.TagAction</code>)
     * that describe how to handle the action.  The idea behind
     * the actions provided is that the most natural text editing
     * operations can be provided if the element structure boils
     * down to paragraphs with runs of some kind of style 
     * in them.  Some things are more naturally specified 
     * structurally, so arbitrary structure should be allowed 
     * above the paragraphs, but will need to be edited with structural
     * actions.  The implecation of this is that some of the
     * html elements specified in the stream being parsed will
     * be collapsed into attributes, and in some cases paragraphs
     * will be synthesized.  When html elements have been
     * converted to attributes, the attribute key will be of
     * type HTML.Tag, and the value will be of type AttributeSet
     * so that no information is lost.  This enables many of the
     * existing actions to work so that the user can type input,
     * hit the return key, backspace, delete, etc and have a 
     * reasonable result.  Selections can be created, and attributes
     * applied or removed, etc.  With this in mind, the work done
     * by the reader can be categorized into the following kinds
     * of tasks:
     * <dl>
     * <dt>Block
     * <dd>Build the structure like it's specified in the stream.
     * This produces elements that contain other elements.
     * <dt>Paragraph
     * <dd>Like block except that it's expected that the element
     * will be used with a paragraph view so a paragraph element
     * won't need to be synthesized.
     * <dt>Character
     * <dd>Contribute the element as an attribute that will start
     * and stop at arbitrary text locations.  This will ultimately
     * be mixed into a run of text, with all of the currently 
     * flattened html character elements.
     * <dt>Special
     * <dd>Produce an embedded graphical element.
     * <dt>Form
     * <dd>Produce an element that is like the embedded graphical
     * element, except that it also has a component model associated
     * with it.
     * <dt>Hidden
     * <dd>Create an element that is hidden from view when the
     * document is being viewed read-only, and visible when the
     * document is being edited.  This is useful to keep the
     * model from losing information, and used to store things
     * like comments and unrecognized tags.
     *
     * </dl>
     * <p>
     * Currently, &lt;APPLET&gt;, &lt;PARAM&gt;, &lt;MAP&gt;, &lt;AREA&gt;, &lt;LINK&gt;,
     * &lt;SCRIPT&gt; and &lt;STYLE&gt; are unsupported.
     *
     * <p>
     * The assignment of the actions described is shown in the
     * following table for the tags defined in <code>HTML.Tag</code>.
     * <table>
     * <tr><td><code>HTML.Tag.A</code>         <td>CharacterAction
     * <tr><td><code>HTML.Tag.ADDRESS</code>   <td>CharacterAction
     * <tr><td><code>HTML.Tag.APPLET</code>    <td>HiddenAction
     * <tr><td><code>HTML.Tag.AREA</code>      <td>AreaAction
     * <tr><td><code>HTML.Tag.B</code>         <td>CharacterAction
     * <tr><td><code>HTML.Tag.BASE</code>      <td>BaseAction
     * <tr><td><code>HTML.Tag.BASEFONT</code>  <td>CharacterAction
     * <tr><td><code>HTML.Tag.BIG</code>       <td>CharacterAction
     * <tr><td><code>HTML.Tag.BLOCKQUOTE</code><td>BlockAction
     * <tr><td><code>HTML.Tag.BODY</code>      <td>BlockAction
     * <tr><td><code>HTML.Tag.BR</code>        <td>SpecialAction
     * <tr><td><code>HTML.Tag.CAPTION</code>   <td>BlockAction
     * <tr><td><code>HTML.Tag.CENTER</code>    <td>BlockAction
     * <tr><td><code>HTML.Tag.CITE</code>      <td>CharacterAction
     * <tr><td><code>HTML.Tag.CODE</code>      <td>CharacterAction
     * <tr><td><code>HTML.Tag.DD</code>        <td>BlockAction
     * <tr><td><code>HTML.Tag.DFN</code>       <td>CharacterAction
     * <tr><td><code>HTML.Tag.DIR</code>       <td>BlockAction
     * <tr><td><code>HTML.Tag.DIV</code>       <td>BlockAction
     * <tr><td><code>HTML.Tag.DL</code>        <td>BlockAction
     * <tr><td><code>HTML.Tag.DT</code>        <td>ParagraphAction
     * <tr><td><code>HTML.Tag.EM</code>        <td>CharacterAction
     * <tr><td><code>HTML.Tag.FONT</code>      <td>CharacterAction
     * <tr><td><code>HTML.Tag.FORM</code>      <td>CharacterAction
     * <tr><td><code>HTML.Tag.FRAME</code>     <td>SpecialAction
     * <tr><td><code>HTML.Tag.FRAMESET</code>  <td>BlockAction
     * <tr><td><code>HTML.Tag.H1</code>        <td>ParagraphAction
     * <tr><td><code>HTML.Tag.H2</code>        <td>ParagraphAction
     * <tr><td><code>HTML.Tag.H3</code>        <td>ParagraphAction
     * <tr><td><code>HTML.Tag.H4</code>        <td>ParagraphAction
     * <tr><td><code>HTML.Tag.H5</code>        <td>ParagraphAction
     * <tr><td><code>HTML.Tag.H6</code>        <td>ParagraphAction
     * <tr><td><code>HTML.Tag.HEAD</code>      <td>HeadAction
     * <tr><td><code>HTML.Tag.HR</code>        <td>SpecialAction
     * <tr><td><code>HTML.Tag.HTML</code>      <td>BlockAction
     * <tr><td><code>HTML.Tag.I</code>         <td>CharacterAction
     * <tr><td><code>HTML.Tag.IMG</code>       <td>SpecialAction
     * <tr><td><code>HTML.Tag.INPUT</code>     <td>FormAction
     * <tr><td><code>HTML.Tag.ISINDEX</code>   <td>IsndexAction
     * <tr><td><code>HTML.Tag.KBD</code>       <td>CharacterAction
     * <tr><td><code>HTML.Tag.LI</code>        <td>BlockAction
     * <tr><td><code>HTML.Tag.LINK</code>      <td>LinkAction
     * <tr><td><code>HTML.Tag.MAP</code>       <td>MapAction
     * <tr><td><code>HTML.Tag.MENU</code>      <td>BlockAction
     * <tr><td><code>HTML.Tag.META</code>      <td>MetaAction
     * <tr><td><code>HTML.Tag.NOFRAMES</code>  <td>BlockAction
     * <tr><td><code>HTML.Tag.OBJECT</code>    <td>SpecialAction
     * <tr><td><code>HTML.Tag.OL</code>        <td>BlockAction
     * <tr><td><code>HTML.Tag.OPTION</code>    <td>FormAction
     * <tr><td><code>HTML.Tag.P</code>         <td>ParagraphAction
     * <tr><td><code>HTML.Tag.PARAM</code>     <td>HiddenAction
     * <tr><td><code>HTML.Tag.PRE</code>       <td>PreAction
     * <tr><td><code>HTML.Tag.SAMP</code>      <td>CharacterAction
     * <tr><td><code>HTML.Tag.SCRIPT</code>    <td>HiddenAction
     * <tr><td><code>HTML.Tag.SELECT</code>    <td>FormAction
     * <tr><td><code>HTML.Tag.SMALL</code>     <td>CharacterAction
     * <tr><td><code>HTML.Tag.STRIKE</code>    <td>CharacterAction
     * <tr><td><code>HTML.Tag.S</code>         <td>CharacterAction
     * <tr><td><code>HTML.Tag.STRONG</code>    <td>CharacterAction
     * <tr><td><code>HTML.Tag.STYLE</code>     <td>StyleAction
     * <tr><td><code>HTML.Tag.SUB</code>       <td>CharacterAction
     * <tr><td><code>HTML.Tag.SUP</code>       <td>CharacterAction
     * <tr><td><code>HTML.Tag.TABLE</code>     <td>BlockAction
     * <tr><td><code>HTML.Tag.TD</code>        <td>BlockAction
     * <tr><td><code>HTML.Tag.TEXTAREA</code>  <td>FormAction
     * <tr><td><code>HTML.Tag.TH</code>        <td>BlockAction
     * <tr><td><code>HTML.Tag.TITLE</code>     <td>TitleAction
     * <tr><td><code>HTML.Tag.TR</code>        <td>BlockAction
     * <tr><td><code>HTML.Tag.TT</code>        <td>CharacterAction
     * <tr><td><code>HTML.Tag.U</code>         <td>CharacterAction
     * <tr><td><code>HTML.Tag.UL</code>        <td>BlockAction
     * <tr><td><code>HTML.Tag.VAR</code>       <td>CharacterAction
     * </table>
     */ 
    public class HTMLReader extends HTMLEditorKit.ParserCallback {

	public HTMLReader(int offset) {
	    this(offset, 0, 0, null);
	}

        public HTMLReader(int offset, int popDepth, int pushDepth,
			  HTML.Tag insertTag) {
	    this(offset, popDepth, pushDepth, insertTag, true);
	}

	/**
	 * This will generate a RuntimeException (will eventually generate
	 * a BadLocationException when API changes are alloced) if inserting
	 *  into non
	 * empty document, <code>insertTag</code> is non null, and
	 * <code>offset</code> is not in the body.
	 */
	// PENDING(sky): Add throws BadLocationException and remove
	// RuntimeException
        HTMLReader(int offset, int popDepth, int pushDepth,
		   HTML.Tag insertTag, boolean insertInsertTag) {
	    emptyDocument = (getLength() == 0);
	    isStyleCSS = "text/css".equals(getDefaultStyleSheetType());
	    this.offset = offset;
	    threshold = HTMLDocument.this.getTokenThreshold();
	    tagMap = new Hashtable(57);
	    TagAction na = new TagAction();
	    TagAction ba = new BlockAction();
	    TagAction pa = new ParagraphAction();
	    TagAction ca = new CharacterAction();
	    TagAction sa = new SpecialAction();
	    TagAction fa = new FormAction();
	    TagAction ha = new HiddenAction();
	    TagAction conv = new ConvertAction();

	    // register handlers for the well known tags
	    tagMap.put(HTML.Tag.A, new AnchorAction());
	    tagMap.put(HTML.Tag.ADDRESS, ca);
	    tagMap.put(HTML.Tag.APPLET, ha);
	    tagMap.put(HTML.Tag.AREA, new AreaAction());
	    tagMap.put(HTML.Tag.B, conv);
	    tagMap.put(HTML.Tag.BASE, new BaseAction());
	    tagMap.put(HTML.Tag.BASEFONT, ca);
	    tagMap.put(HTML.Tag.BIG, ca);
	    tagMap.put(HTML.Tag.BLOCKQUOTE, ba);
	    tagMap.put(HTML.Tag.BODY, ba);
	    tagMap.put(HTML.Tag.BR, sa);
	    tagMap.put(HTML.Tag.CAPTION, ba);
	    tagMap.put(HTML.Tag.CENTER, ba);
	    tagMap.put(HTML.Tag.CITE, ca);
	    tagMap.put(HTML.Tag.CODE, ca);
	    tagMap.put(HTML.Tag.DD, ba);
	    tagMap.put(HTML.Tag.DFN, ca);
	    tagMap.put(HTML.Tag.DIR, ba);
	    tagMap.put(HTML.Tag.DIV, ba);
	    tagMap.put(HTML.Tag.DL, ba);
	    tagMap.put(HTML.Tag.DT, pa);
	    tagMap.put(HTML.Tag.EM, ca);
	    tagMap.put(HTML.Tag.FONT, conv);
	    tagMap.put(HTML.Tag.FORM, ca);
	    tagMap.put(HTML.Tag.FRAME, sa);
	    tagMap.put(HTML.Tag.FRAMESET, ba);
	    tagMap.put(HTML.Tag.H1, pa);
	    tagMap.put(HTML.Tag.H2, pa);
	    tagMap.put(HTML.Tag.H3, pa);
	    tagMap.put(HTML.Tag.H4, pa);
	    tagMap.put(HTML.Tag.H5, pa);
	    tagMap.put(HTML.Tag.H6, pa);
	    tagMap.put(HTML.Tag.HEAD, new HeadAction());
	    tagMap.put(HTML.Tag.HR, sa);
	    tagMap.put(HTML.Tag.HTML, ba);
	    tagMap.put(HTML.Tag.I, conv);
	    tagMap.put(HTML.Tag.IMG, sa);
	    tagMap.put(HTML.Tag.INPUT, fa);
	    tagMap.put(HTML.Tag.ISINDEX, new IsindexAction());
	    tagMap.put(HTML.Tag.KBD, ca);
	    tagMap.put(HTML.Tag.LI, ba);
	    tagMap.put(HTML.Tag.LINK, new LinkAction());
	    tagMap.put(HTML.Tag.MAP, new MapAction());
	    tagMap.put(HTML.Tag.MENU, ba);
	    tagMap.put(HTML.Tag.META, new MetaAction());
	    tagMap.put(HTML.Tag.NOFRAMES, ba);
	    tagMap.put(HTML.Tag.OBJECT, sa);
	    tagMap.put(HTML.Tag.OL, ba);
	    tagMap.put(HTML.Tag.OPTION, fa);
	    tagMap.put(HTML.Tag.P, pa);
	    tagMap.put(HTML.Tag.PARAM, new ObjectAction());
	    tagMap.put(HTML.Tag.PRE, new PreAction());
	    tagMap.put(HTML.Tag.SAMP, ca);
	    tagMap.put(HTML.Tag.SCRIPT, ha);
	    tagMap.put(HTML.Tag.SELECT, fa);
	    tagMap.put(HTML.Tag.SMALL, ca);
	    tagMap.put(HTML.Tag.STRIKE, conv);	    
	    tagMap.put(HTML.Tag.S, ca);
	    tagMap.put(HTML.Tag.STRONG, ca);
	    tagMap.put(HTML.Tag.STYLE, new StyleAction());
	    tagMap.put(HTML.Tag.SUB, conv);
	    tagMap.put(HTML.Tag.SUP, conv);
	    tagMap.put(HTML.Tag.TABLE, ba);
	    tagMap.put(HTML.Tag.TD, ba);
	    tagMap.put(HTML.Tag.TEXTAREA, fa);
	    tagMap.put(HTML.Tag.TH, ba);
	    tagMap.put(HTML.Tag.TITLE, new TitleAction());
	    tagMap.put(HTML.Tag.TR, ba);
	    tagMap.put(HTML.Tag.TT, ca);
	    tagMap.put(HTML.Tag.U, conv);
	    tagMap.put(HTML.Tag.UL, ba);
	    tagMap.put(HTML.Tag.VAR, ca);
	    tagMap.put(EndOfLineTag, new EndOfLineAction());
	    // Clear out the old comments.
	    putProperty(AdditionalComments, null);

	    if (insertTag != null) {
		this.insertTag = insertTag;
		this.popDepth = popDepth;
		this.pushDepth = pushDepth;
		this.insertInsertTag = insertInsertTag;
		foundInsertTag = false;
	    }
	    else {
		foundInsertTag = true;
	    }
	    midInsert = (!emptyDocument && insertTag == null);
	    if (midInsert) {
		generateEndsSpecsForMidInsert();
	    }
	}

	/**
	 * Generates an initial batch of end ElementSpecs in parseBuffer
	 * to position future inserts into the body.
	 */
	private void generateEndsSpecsForMidInsert() {
	    int           count = heightToElementWithName(HTML.Tag.BODY,
						   Math.max(0, offset - 1));
	    boolean       joinNext = false;

	    if (count == -1 && offset > 0) {
		count = heightToElementWithName(HTML.Tag.BODY, offset);
		if (count != -1) {
		    // Previous isn't in body, but current is. Have to
		    // do some end specs, followed by join next.
		    count = depthTo(offset - 1) - 1;
		    joinNext = true;
		}
	    }
	    if (count == -1) {
		throw new RuntimeException("Must insert new content into body element-");
	    }
	    if (count != -1) {
		// Insert a newline, if necessary.
		try {
		    if (!joinNext && offset > 0 &&
			!getText(offset - 1, 1).equals("\n")) {
			SimpleAttributeSet newAttrs = new SimpleAttributeSet();
			newAttrs.addAttribute(StyleConstants.NameAttribute,
					      HTML.Tag.CONTENT);
			ElementSpec spec = new ElementSpec(newAttrs,
				    ElementSpec.ContentType, NEWLINE, 0, 1);
			parseBuffer.addElement(spec);
		    }
		    // Should never throw, but will catch anyway.
		} catch (BadLocationException ble) {}
		while (count-- > 0) {
		    parseBuffer.addElement(new ElementSpec
					   (null, ElementSpec.EndTagType));
		}
		if (joinNext) {
		    ElementSpec spec = new ElementSpec(null, ElementSpec.
						       StartTagType);

		    spec.setDirection(ElementSpec.JoinNextDirection);
		    parseBuffer.addElement(spec);
		}
	    }
	    // We should probably throw an exception if (count == -1)
	    // Or look for the body and reset the offset.
	}

	/**
	 * @return number of parents to reach the child at offset.
	 */
	private int depthTo(int offset) {
	    Element       e = getDefaultRootElement();
	    int           count = 0;

	    while (!e.isLeaf()) {
		count++;
		e = e.getElement(e.getElementIndex(offset));
	    }
	    return count;
	}

	/**
	 * @return number of parents of the leaf at <code>offset</code>
	 *         until a parent with name, <code>name</code> has been
	 *         found. -1 indicates no matching parent with
	 *         <code>name</code>.
	 */
	private int heightToElementWithName(Object name, int offset) {
	    Element       e = getCharacterElement(offset).getParentElement();
	    int           count = 0;

	    while (e != null && e.getAttributes().getAttribute
		   (StyleConstants.NameAttribute) != name) {
		count++;
		e = e.getParentElement();
	    }
	    return (e == null) ? -1 : count;
	}

	/**
	 * This will make sure the fake element (path at getLength())
	 * has the form HTML BODY P.
	 */
	private void adjustEndElement() {
	    int length = getLength();
	    if (length == 0) {
		return;
	    }
	    obtainLock();
	    try {
		Element[] pPath = getPathTo(length - 1);
		if (pPath.length > 2 &&
		    pPath[1].getAttributes().getAttribute
		    (StyleConstants.NameAttribute) == HTML.Tag.BODY &&
		    pPath[1].getEndOffset() == length) {

		    String lastText = getText(length - 1, 1);
		    DefaultDocumentEvent event;
		    Element[] added;
		    Element[] removed;
		    int index;
		    // Remove the fake second body.
		    added = new Element[0];
		    removed = new Element[1];
		    index = pPath[0].getElementIndex(length);
		    removed[0] = pPath[0].getElement(index);
		    ((BranchElement)pPath[0]).replace(index, 1, added);
		    ElementEdit firstEdit = new ElementEdit(pPath[0], index,
							    removed, added);

		    // And then add paragraph, or adjust deepest leaf.
		    if (pPath.length == 3 && pPath[2].getAttributes().
			getAttribute(StyleConstants.NameAttribute) ==
			HTML.Tag.P && !lastText.equals("\n")) {
			index = pPath[2].getElementIndex(length - 1);
			AttributeSet attrs = pPath[2].getElement(index).
				                 getAttributes();
			if (attrs.getAttributeCount() == 1 &&
			    attrs.getAttribute(StyleConstants.NameAttribute)
			    == HTML.Tag.CONTENT) {
			    // Can extend existing one.
			    added = new Element[1];
			    removed = new Element[1];
			    removed[0] = pPath[2].getElement(index);
			    int start = removed[0].getStartOffset();
			    added[0] = createLeafElement(pPath[2], attrs,
					   start, length + 1);
			    ((BranchElement)pPath[2]).replace(index, 1, added);
			    event = new DefaultDocumentEvent(start,
					length - start + 1, DocumentEvent.
					EventType.CHANGE);
			    event.addEdit(new ElementEdit(pPath[2], index,
							  removed, added));
			}
			else {
			    // Create new leaf.
			    SimpleAttributeSet sas = new SimpleAttributeSet();
			    sas.addAttribute(StyleConstants.NameAttribute,
					     HTML.Tag.CONTENT);
			    added = new Element[1];
			    added[0] = createLeafElement(pPath[2], sas,
							 length, length + 1);
			    ((BranchElement)pPath[2]).replace(index + 1, 0,
							      added);
			    event = new DefaultDocumentEvent(length, 1,
					    DocumentEvent.EventType.CHANGE);
			    removed = new Element[0];
			    event.addEdit(new ElementEdit(pPath[2], index + 1,
							  removed, added));
			}
		    }
		    else {
			// Create paragraph
			SimpleAttributeSet sas = new SimpleAttributeSet();
			sas.addAttribute(StyleConstants.NameAttribute,
					 HTML.Tag.P);
			BranchElement newP = (BranchElement)
				             createBranchElement(pPath[1],sas);
			added = new Element[1];
			added[0] = newP;
			removed = new Element[0];
			index = pPath[1].getElementIndex(length - 1) + 1;
			((BranchElement)pPath[1]).replace(index, 0, added);
			event = new DefaultDocumentEvent(length, 1,
					 DocumentEvent.EventType.CHANGE);
			event.addEdit(new ElementEdit(pPath[1], index,
						      removed, added));
			added = new Element[1];
			sas = new SimpleAttributeSet();
			sas.addAttribute(StyleConstants.NameAttribute,
					 HTML.Tag.CONTENT);
			added[0] = createLeafElement(newP, sas, length,
						     length + 1);
			newP.replace(0, 0, added);
		    }
		    event.addEdit(firstEdit);
		    event.end();
		    // And finally post the event.
		    fireChangedUpdate(event);
		    fireUndoableEditUpdate(new UndoableEditEvent(this, event));
		}
	    }
	    catch (BadLocationException ble) {
	    }
	    finally {
		releaseLock();
	    }
	}

	private Element[] getPathTo(int offset) {
	    Stack elements = new Stack();
	    Element e = getDefaultRootElement();
	    int index;
	    while (!e.isLeaf()) {
		elements.push(e);
		e = e.getElement(e.getElementIndex(offset));
	    }
	    Element[] retValue = new Element[elements.size()];
	    elements.copyInto(retValue);
	    return retValue;
	}
	    
	// -- HTMLEditorKit.ParserCallback methods --------------------

	/**
	 * This is the last method called on the reader.  It allows
	 * any pending changes to be flushed into the document.  
	 * Since this is currently loading synchronously, the entire
	 * set of changes are pushed in at this point.
	 */
        public void flush() throws BadLocationException {
	    flushBuffer();
	    if (emptyDocument) {
		adjustEndElement();
	    }
	}

	/**
	 * Called by the parser to indicate a block of text was
	 * encountered.
	 */
        public void handleText(char[] data, int pos) {
	    if (midInsert && !inBody) {
		return;
	    }
	    if (inTextArea) {
	        textAreaContent(data);
	    } else if (inPre) {
		preContent(data);
	    } else if (inTitle) {
		putProperty(Document.TitleProperty, new String(data));
	    } else if (option != null) {
		option.setLabel(new String(data));
	    } else if (inStyle) {
		if (styles != null) {
		    styles.addElement(new String(data));
		}
	    } else if (inBlock > 0) {
		if (data.length >= 1) {
		    addContent(data, 0, data.length);
		}
	    }
	}

	/**
	 * Callback from the parser.  Route to the appropriate
	 * handler for the tag.
	 */
	public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
	    if (midInsert && !inBody) {
		if (t == HTML.Tag.BODY) {
		    inBody = true;
		    // Increment inBlock since we know we are in the body,
		    // this is needed incase an implied-p is needed. If
		    // inBlock isn't incremented, and an implied-p is
		    // encountered, addContent won't be called!
		    inBlock++;
		}
		return;
	    }
	    if (!inBody && t == HTML.Tag.BODY) {
		inBody = true;
	    }
	    if (isStyleCSS && a.isDefined(HTML.Attribute.STYLE)) {
		// Map the style attributes.
		String decl = (String)a.getAttribute(HTML.Attribute.STYLE);
		a.removeAttribute(HTML.Attribute.STYLE);
		styleAttributes = getStyleSheet().getDeclaration(decl);
		a.addAttributes(styleAttributes);
	    }
	    else {
		styleAttributes = null;
	    }
	    TagAction action = (TagAction) tagMap.get(t);

	    if (action != null) {
		action.start(t, a);
	    } 
	}

        public void handleComment(char[] data, int pos) {
	    if (inStyle) {
		if (styles != null) {
		    styles.addElement(new String(data));
		}
	    }
	    else if (getPreservesUnknownTags()) {
		if (inBlock == 0) {
		    // Comment outside of body, will not be able to show it,
		    // but can add it as a property on the Document.
		    Object comments = getProperty(AdditionalComments);
		    if (comments != null && !(comments instanceof Vector)) {
			// No place to put comment.
			return;
		    }
		    if (comments == null) {
			comments = new Vector();
			putProperty(AdditionalComments, comments);
		    }
		    ((Vector)comments).addElement(new String(data));
		    return;
		}
		SimpleAttributeSet sas = new SimpleAttributeSet();
		sas.addAttribute(HTML.Attribute.COMMENT, new String(data));
		addSpecialElement(HTML.Tag.COMMENT, sas);
	    }
	}

	/**
	 * Callback from the parser.  Route to the appropriate
	 * handler for the tag.
	 */
	public void handleEndTag(HTML.Tag t, int pos) {
	    if (midInsert && !inBody) {
		return;
	    }
	    if (t == HTML.Tag.BODY) {
		inBody = false;
		if (midInsert) {
		    inBlock--;
		}
	    }
	    TagAction action = (TagAction) tagMap.get(t);
	    if (action != null) {
		action.end(t);
	    }
	}

	/**
	 * Callback from the parser.  Route to the appropriate
	 * handler for the tag.
	 */
	public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
	    if (midInsert && !inBody) {
		return;
	    }

	    if (isStyleCSS && a.isDefined(HTML.Attribute.STYLE)) {
		// Map the style attributes.
		String decl = (String)a.getAttribute(HTML.Attribute.STYLE);
		a.removeAttribute(HTML.Attribute.STYLE);
		styleAttributes = getStyleSheet().getDeclaration(decl);
		a.addAttributes(styleAttributes);
	    }
	    else {
		styleAttributes = null;
	    }

	    TagAction action = (TagAction) tagMap.get(t);
	    if (action != null) {
		action.start(t, a);
		action.end(t);
	    }
	    else if (getPreservesUnknownTags()) {
		// unknown tag, only add if should preserve it.
		addSpecialElement(t, a);
	    }
	}

	// ---- tag handling support ------------------------------

	/**
	 * Register a handler for the given tag.  By default
	 * all of the well-known tags will have been registered.
	 * This can be used to change the handling of a particular
	 * tag or to add support for custom tags.
	 */
        protected void registerTag(HTML.Tag t, TagAction a) {
	    tagMap.put(t, a);
	}

	/**
	 * This is an action to be performed in response
	 * to parsing a tag.  This allows customization
	 * of how each tag is handled and avoids a large
	 * switch statement.
	 */
	public class TagAction {

	    /**
	     * Called when a start tag is seen for the
	     * type of tag this action was registered
	     * to.  The tag argument indicates the actual
	     * tag for those actions that are shared across
	     * many tags.  By default this does nothing and
	     * completely ignores the tag.
	     */
	    public void start(HTML.Tag t, MutableAttributeSet a) {
	    }

	    /**
	     * Called when an end tag is seen for the
	     * type of tag this action was registered
	     * to.  The tag argument indicates the actual
	     * tag for those actions that are shared across
	     * many tags.  By default this does nothing and
	     * completely ignores the tag.
	     */
	    public void end(HTML.Tag t) {
	    }

	}

	public class BlockAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet attr) {
		blockOpen(t, attr);
	    }

	    public void end(HTML.Tag t) {
		blockClose(t);
	    }
	}

	public class ParagraphAction extends BlockAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		super.start(t, a);
		inParagraph = true;
	    }

	    public void end(HTML.Tag t) {
		super.end(t);
		inParagraph = false;
	    }
	}

	public class SpecialAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		addSpecialElement(t, a);
	    }

	}

	public class IsindexAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		blockOpen(HTML.Tag.IMPLIED, new SimpleAttributeSet());
		addSpecialElement(t, a);
		blockClose(HTML.Tag.IMPLIED);
	    }

	}


	public class HiddenAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		addSpecialElement(t, a);
	    }

	    public void end(HTML.Tag t) {
		if (!isEmpty(t)) {
		    MutableAttributeSet a = new SimpleAttributeSet();
		    a.addAttribute(HTML.Attribute.ENDTAG, "true");
		    addSpecialElement(t, a);
		}
	    }

	    boolean isEmpty(HTML.Tag t) {
		if (t == HTML.Tag.APPLET ||
		    t == HTML.Tag.SCRIPT) {
		    return false;
		}
		return true;
	    }
	}


	/**
	 * Subclass of HiddenAction to set the content type for style sheets,
	 * and to set the name of the default style sheet.
	 */
	class MetaAction extends HiddenAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		Object equiv = a.getAttribute(HTML.Attribute.HTTPEQUIV);
		if (equiv != null) {
		    equiv = ((String)equiv).toLowerCase();
		    if (equiv.equals("content-style-type")) {
			String value = (String)a.getAttribute
			               (HTML.Attribute.CONTENT);
			setDefaultStyleSheetType(value);
			isStyleCSS = "text/css".equals
			              (getDefaultStyleSheetType());
		    }
		    else if (equiv.equals("default-style")) {
			defaultStyle = (String)a.getAttribute
			               (HTML.Attribute.CONTENT);
		    }
		}
		super.start(t, a);
	    }

	    boolean isEmpty(HTML.Tag t) {
		return true;
	    }
	}


	/**
	 * End if overriden to create the necessary stylesheets that
	 * are referenced via the link tag. It is done in this manner
	 * as the meta tag can be used to specify an alternate style sheet,
	 * and is not guaranteed to come before the link tags.
	 */
	class HeadAction extends HiddenAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		inHead = true;
		// This check of the insertTag is put in to avoid considering
		// the implied-p that is generated for the head. This allows
		// inserts for HR to work correctly.
		if (insertTag == null || insertTag == HTML.Tag.HEAD) {
		    super.start(t, a);
		}
	    }

	    public void end(HTML.Tag t) {
		inHead = inStyle = false;
		// See if there is a StyleSheet to link to.
		if (styles != null) {
		    boolean isDefaultCSS = isStyleCSS;
		    for (int counter = 0, maxCounter = styles.size();
			 counter < maxCounter;) {
			Object value = styles.elementAt(counter);
			if (value == HTML.Tag.LINK) {
			    handleLink((AttributeSet)styles.
				       elementAt(++counter));
			    counter++;
			}
			else {
			    // Rule.
			    // First element gives type.
			    String type = (String)styles.elementAt(++counter);
			    boolean isCSS = (type == null) ? isDefaultCSS :
				            type.equals("text/css");
			    while (++counter < maxCounter &&
				   (styles.elementAt(counter)
				    instanceof String)) {
				if (isCSS) {
				    addCSSRules((String)styles.elementAt
						(counter));
				}
			    }
			}
		    }
		}
		if (insertTag == null || insertTag == HTML.Tag.HEAD) {
		    super.end(t);
		}
	    }

	    boolean isEmpty(HTML.Tag t) {
		return false;
	    }

	    private void handleLink(AttributeSet attr) {
		// Link.
		String type = (String)attr.getAttribute(HTML.Attribute.TYPE);
		if (type == null) {
		    type = getDefaultStyleSheetType();
		}
		// Only choose if type==text/css
		// Select link if rel==stylesheet.
		// Otherwise if rel==alternate stylesheet and
		//   title matches default style.
		if (type.equals("text/css")) {
		    String rel = (String)attr.getAttribute(HTML.Attribute.REL);
		    String title = (String)attr.getAttribute
			                       (HTML.Attribute.TITLE);
		    String media = (String)attr.getAttribute
				                   (HTML.Attribute.MEDIA);
		    if (media == null) {
			media = "all";
		    }
		    else {
			media = media.toLowerCase();
		    }
		    if (rel != null) {
			rel = rel.toLowerCase();
			if ((media.indexOf("all") != -1 ||
			     media.indexOf("screen") != -1) &&
			    (rel.equals("stylesheet") ||
			     (rel.equals("alternate stylesheet") &&
			      title.equals(defaultStyle)))) {
			    linkCSSStyleSheet((String)attr.getAttribute
					      (HTML.Attribute.HREF));
			}
		    }
		}
	    }
	}


	/**
	 * A subclass to add the AttributeSet to styles if the
	 * attributes contains an attribute for 'rel' with value
	 * 'stylesheet' or 'alternate stylesheet'.
	 */
	class LinkAction extends HiddenAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		String rel = (String)a.getAttribute(HTML.Attribute.REL);
		if (rel != null) {
		    rel = rel.toLowerCase();
		    if (rel.equals("stylesheet") ||
			rel.equals("alternate stylesheet")) {
			if (styles == null) {
			    styles = new Vector(3);
			}
			styles.addElement(t);
			styles.addElement(a.copyAttributes());
		    }
		}
		super.start(t, a);
	    }
	}

	class MapAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		lastMap = new Map((String)a.getAttribute(HTML.Attribute.NAME));
		addMap(lastMap);
	    }

	    public void end(HTML.Tag t) {
	    }
	}


	class AreaAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		if (lastMap != null) {
		    lastMap.addArea(a.copyAttributes());
		}
	    }

	    public void end(HTML.Tag t) {
	    }
	}


	class StyleAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		if (inHead) {
		    if (styles == null) {
			styles = new Vector(3);
		    }
		    styles.addElement(t);
		    styles.addElement(a.getAttribute(HTML.Attribute.TYPE));
		    inStyle = true;
		}
	    }

	    public void end(HTML.Tag t) {
		inStyle = false;
	    }

	    boolean isEmpty(HTML.Tag t) {
		return false;
	    }
	}
	    

	public class PreAction extends BlockAction {

	    public void start(HTML.Tag t, MutableAttributeSet attr) {
		inPre = true;
		blockOpen(t, attr);
		attr.addAttribute(CSS.Attribute.WHITE_SPACE, "pre");
		blockOpen(HTML.Tag.IMPLIED, attr);
	    }

	    public void end(HTML.Tag t) {
		blockClose(HTML.Tag.IMPLIED);
		// set inPre to false after closing, so that if a newline
		// is added it won't generate a blockOpen.
 		inPre = false;
		blockClose(t);
	    }
	}

	public class CharacterAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet attr) {
		pushCharacterStyle();
		charAttr.addAttribute(t, attr.copyAttributes());
		if (styleAttributes != null) {
		    charAttr.addAttributes(styleAttributes);
		}
		if (t == HTML.Tag.FORM) {
		    /* initialize a ButtonGroup when
		       FORM tag is encountered.  This will
		       be used for any radio buttons that
		       might be defined in the FORM.
		    */
		    radioButtonGroup = new ButtonGroup();
		}
	    }
	
	    public void end(HTML.Tag t) {
		popCharacterStyle();
		if (t == HTML.Tag.FORM) {
		    /*
		     * reset the button group to null since
		     * the form has ended.
		     */
		    radioButtonGroup = null;
		}
	    }
	}

	/**
	 * Provides conversion of HTML tag/attribute
	 * mappings that have a corresponding StyleConstants
	 * and CSS mapping.  The conversion is to CSS attributes.
	 */
	class ConvertAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet attr) {
		pushCharacterStyle();
		if (styleAttributes != null) {
		    charAttr.addAttributes(styleAttributes);
		}
		StyleSheet sheet = getStyleSheet();
		if (t == HTML.Tag.B) {
		    sheet.addCSSAttribute(charAttr, CSS.Attribute.FONT_WEIGHT, "bold");
		} else if (t == HTML.Tag.I) {
		    sheet.addCSSAttribute(charAttr, CSS.Attribute.FONT_STYLE, "italic");
		} else if (t == HTML.Tag.U) {
		    Object v = charAttr.getAttribute(CSS.Attribute.TEXT_DECORATION);
		    String value = "underline";
		    value = (v != null) ? value + "," + v.toString() : value; 
		    sheet.addCSSAttribute(charAttr, CSS.Attribute.TEXT_DECORATION, value);
		} else if (t == HTML.Tag.STRIKE) {
		    Object v = charAttr.getAttribute(CSS.Attribute.TEXT_DECORATION);
		    String value = "line-through";
		    value = (v != null) ? value + "," + v.toString() : value; 
		    sheet.addCSSAttribute(charAttr, CSS.Attribute.TEXT_DECORATION, value);
		} else if (t == HTML.Tag.SUP) {
		    Object v = charAttr.getAttribute(CSS.Attribute.VERTICAL_ALIGN);
		    String value = "sup";
		    value = (v != null) ? value + "," + v.toString() : value; 
		    sheet.addCSSAttribute(charAttr, CSS.Attribute.VERTICAL_ALIGN, value);
		} else if (t == HTML.Tag.SUB) {
		    Object v = charAttr.getAttribute(CSS.Attribute.VERTICAL_ALIGN);
		    String value = "sub";
		    value = (v != null) ? value + "," + v.toString() : value; 
		    sheet.addCSSAttribute(charAttr, CSS.Attribute.VERTICAL_ALIGN, value);
		} else if (t == HTML.Tag.FONT) {
		    String color = (String) attr.getAttribute(HTML.Attribute.COLOR);
		    if (color != null) {
			sheet.addCSSAttribute(charAttr, CSS.Attribute.COLOR, color);
		    }
		    String face = (String) attr.getAttribute(HTML.Attribute.FACE);
		    if (face != null) {
			sheet.addCSSAttribute(charAttr, CSS.Attribute.FONT_FAMILY, face);
		    }
		    String size = (String) attr.getAttribute(HTML.Attribute.SIZE);
		    if (size != null) {
			sheet.addCSSAttributeFromHTML(charAttr, CSS.Attribute.FONT_SIZE, size);
		    }
		}
	    }
	
	    public void end(HTML.Tag t) {
		popCharacterStyle();
	    }
	    
	}

	class AnchorAction extends CharacterAction {

	    public void start(HTML.Tag t, MutableAttributeSet attr) {
		// set flag to catch empty anchors
		emptyAnchor = true;
		super.start(t, attr);
	    }
	
	    public void end(HTML.Tag t) {
		if (emptyAnchor) {
		    // if the anchor was empty it was probably a
		    // named anchor point and we don't want to throw
		    // it away.
		    char[] one = new char[1];
		    one[0] = ' ';
		    addContent(one, 0, 1);
		}
		super.end(t);
	    }
	}

	class TitleAction extends HiddenAction {

	    public void start(HTML.Tag t, MutableAttributeSet attr) {
		inTitle = true;
		super.start(t, attr);
	    }
	
	    public void end(HTML.Tag t) {
		inTitle = false;
		super.end(t);
	    }

	    boolean isEmpty(HTML.Tag t) {
		return false;
	    }
	}


	class BaseAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet attr) {
		String href = (String) attr.getAttribute(HTML.Attribute.HREF);
		if (href != null) {
		    try {
			base = new URL(base, href);
			getStyleSheet().setBase(base);
		    } catch (MalformedURLException ex) {
		    }
		}
	    }
	}

	class ObjectAction extends SpecialAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		if (t == HTML.Tag.PARAM) {
		    addParameter(a);
		} else {
		    super.start(t, a);
		}
	    }

	    public void end(HTML.Tag t) {
		if (t != HTML.Tag.PARAM) {
		    super.end(t);
		}
	    }

	    void addParameter(AttributeSet a) {
		String name = (String) a.getAttribute(HTML.Attribute.NAME);
		String value = (String) a.getAttribute(HTML.Attribute.VALUE);
		if ((name != null) && (value != null)) {
		    ElementSpec objSpec = (ElementSpec) parseBuffer.lastElement();
		    MutableAttributeSet objAttr = (MutableAttributeSet) objSpec.getAttributes();
		    objAttr.addAttribute(name, value);
		}
	    }
	}

	/**
	 * Action to support forms by building all of the elements
	 * used to represent form controls.  This will process
	 * the &lt;input&gt;, &lt;textarea&gt;, &lt;select&gt;,
	 * and &lt;option&gt; tags.  The element created by
	 * this action is expected to have the attribute
	 * <code>StyleConstants.ModelAttribute</code> set to
	 * the model that holds the state for the form control.
	 * This enables multiple views, and allows document to
	 * be iterated over picking up the data of the form.
	 * The following are the model assignments for the
	 * various type of form elements.
 	 * <table>
 	 * <tr>
 	 *   <th>Element Type
 	 *   <th>Model Type
 	 * <tr>
 	 *   <td>input, type button
 	 *   <td>DefaultButtonModel
 	 * <tr>
 	 *   <td>input, type checkbox
 	 *   <td>JToggleButton.ToggleButtonModel
 	 * <tr>
 	 *   <td>input, type image
 	 *   <td>DefaultButtonModel
 	 * <tr>
 	 *   <td>input, type password
 	 *   <td>PlainDocument
 	 * <tr>
 	 *   <td>input, type radio
 	 *   <td>JToggleButton.ToggleButtonModel
 	 * <tr>
 	 *   <td>input, type reset
 	 *   <td>DefaultButtonModel
 	 * <tr>
 	 *   <td>input, type submit
 	 *   <td>DefaultButtonModel
 	 * <tr>
 	 *   <td>input, type text or type is null.
 	 *   <td>PlainDocument
 	 * <tr>
 	 *   <td>select
 	 *   <td>OptionComboBoxModel or an OptionListBoxModel, with an item type of Option
 	 * <tr>
 	 *   <td>textarea
 	 *   <td>TextAreaDocument
 	 * </table>
	 *
	 */
	public class FormAction extends SpecialAction {

	    public void start(HTML.Tag t, MutableAttributeSet attr) {
		if (t == HTML.Tag.INPUT) {
		    String type = (String) 
			attr.getAttribute(HTML.Attribute.TYPE);
		    /*
		     * if type is not defined teh default is
		     * assumed to be text.
		     */
		    if (type == null) {
			type = "text";
			attr.addAttribute(HTML.Attribute.TYPE, "text");
		    }
		    setModel(type, attr);
		} else if (t == HTML.Tag.TEXTAREA) {
		    inTextArea = true;
		    textAreaDocument = new TextAreaDocument();
		    attr.addAttribute(StyleConstants.ModelAttribute,
				      textAreaDocument);
		} else if (t == HTML.Tag.SELECT) {
		    int size = HTML.getIntegerAttributeValue(attr,
							     HTML.Attribute.SIZE,
							     1);
		    boolean multiple = ((String)attr.getAttribute(HTML.Attribute.MULTIPLE) != null);
		    if ((size > 1) || multiple) {
			OptionListModel m = new OptionListModel();
			if (multiple) {
			    m.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			}
			selectModel = m;
		    } else {
			selectModel = new OptionComboBoxModel();
		    }
		    attr.addAttribute(StyleConstants.ModelAttribute,
				      selectModel);

		}

		// build the element, unless this is an option.
		if (t == HTML.Tag.OPTION) {
		    option = new Option(attr);

		    if (selectModel instanceof OptionListModel) {
			OptionListModel m = (OptionListModel)selectModel;
			m.addElement(option);
			if (option.isSelected()) {
			    m.addSelectionInterval(optionCount, optionCount);
			    m.setInitialSelection(optionCount);
			}
		    } else if (selectModel instanceof OptionComboBoxModel) {
			OptionComboBoxModel m = (OptionComboBoxModel)selectModel;
			m.addElement(option);
			if (option.isSelected()) {
			    m.setSelectedItem(option);
			    m.setInitialSelection(option);
			}
		    }
		    optionCount++;
		} else {
		    super.start(t, attr);
		}
	    }

	    public void end(HTML.Tag t) {
		if (t == HTML.Tag.OPTION) {
		    option = null;
		} else {
		    if (t == HTML.Tag.SELECT) {
			selectModel = null;
			optionCount = 0;
		    } else if (t == HTML.Tag.TEXTAREA) {
			inTextArea = false;
			
			/* Now that the textarea has ended,
			 * store the entire initial text
			 * of the text area.  This will
			 * enable us to restore the initial
			 * state if a reset is requested.
			 */
			textAreaDocument.storeInitialText();
		    }
		    super.end(t);
		} 
	    }

	    void setModel(String type, MutableAttributeSet attr) {
		if (type.equals("submit") ||
		    type.equals("reset") ||
		    type.equals("image")) {

		    // button model
		    attr.addAttribute(StyleConstants.ModelAttribute,
				      new DefaultButtonModel());
		} else if (type.equals("text") ||
			   type.equals("password")) {
		    // plain text model
		    attr.addAttribute(StyleConstants.ModelAttribute,
				      new PlainDocument());
		} else if (type.equals("checkbox") ||
			   type.equals("radio")) {
		    JToggleButton.ToggleButtonModel model = new JToggleButton.ToggleButtonModel();
		    if (type.equals("radio")) {
			model.setGroup(radioButtonGroup);
		    }
		    attr.addAttribute(StyleConstants.ModelAttribute, model);
		}
	    }

	    /**
	     * If a &lt;select&gt; tag is being processed, this
	     * model will be a reference to the model being filled
	     * with the &lt;option&gt; elements (which produce
	     * objects of type <code>Option</code>.
	     */
	    Object selectModel;
	    int optionCount;
	}


	/**
	 * Used to record the end of line string.
	 * This is temporary until new API can be added.
	 */
	class EndOfLineAction extends TagAction {

	    public void start(HTML.Tag t, MutableAttributeSet a) {
		if (emptyDocument && a != null) {
		    Object prop = a.getAttribute("__EndOfLineString__");

		    if (prop != null && (prop instanceof String)) {
			putProperty(DefaultEditorKit.EndOfLineStringProperty,
				    prop);
		    }
		}
	    }

	    public void end(HTML.Tag t) {
	    }
	}


	// --- utility methods used by the reader ------------------

	/**
	 * Push the current character style on a stack in preparation
	 * for forming a new nested character style.
	 */
	protected void pushCharacterStyle() {
	    charAttrStack.push(charAttr.copyAttributes());
	}

	/**
	 * Pop a previously pushed character style off the stack
	 * to return to a previous style.
	 */
	protected void popCharacterStyle() {
	    if (!charAttrStack.empty()) {
		charAttr = (MutableAttributeSet) charAttrStack.peek();
		charAttrStack.pop();
	    }
	}

	/**
	 * Add the given content to the textarea document.
	 * This method gets called when we are in a textarea
	 * context.  Therefore all text that is seen belongs
	 * to the text area and is hence added to the
	 * TextAreaDocument associated with the text area.
	 */
	protected void textAreaContent(char[] data) {
	    try {
		textAreaDocument.insertString(textAreaDocument.getLength(), new String(data), null);
	    } catch (BadLocationException e) {
		// Should do something reasonable
	    }
	}
	
	/**
	 * Add the given content that was encountered in a 
	 * PRE element.  This synthesizes lines to hold the
	 * runs of text, and makes calls to addContent to
	 * actually add the text.
	 */
	protected void preContent(char[] data) {
	    int last = 0;
	    for (int i = 0; i < data.length; i++) {
		if (data[i] == '\n') {
		    addContent(data, last, i - last + 1);
		    blockClose(HTML.Tag.IMPLIED);
		    MutableAttributeSet a = new SimpleAttributeSet();
		    a.addAttribute(CSS.Attribute.WHITE_SPACE, "pre");
		    blockOpen(HTML.Tag.IMPLIED, a);
		    last = i + 1;
		}
	    }
	    if (last < data.length) {
		addContent(data, last, data.length - last);
	    }
	}

	/**
	 * Add an instruction to the parse buffer to create a 
	 * block element with the given attributes.
	 */
	protected void blockOpen(HTML.Tag t, MutableAttributeSet attr) {
	    if (impliedP) {
		impliedP = false;
		inParagraph = false;
		blockClose(HTML.Tag.IMPLIED);
	    }
		
	    inBlock++;

	    if (!foundInsertTag) {
		if (!isInsertTag(t)) {
		    return ;
		}
		foundInsertTag();
		if (!insertInsertTag) {
		    return;
		}
	    }
	    lastWasNewline = false;
	    attr.addAttribute(StyleConstants.NameAttribute, t);
	    ElementSpec es = new ElementSpec(
		attr.copyAttributes(), ElementSpec.StartTagType);
	    parseBuffer.addElement(es);
	}

	/**
	 * Add an instruction to the parse buffer to close out
	 * a block element of the given type.
	 */
	protected void blockClose(HTML.Tag t) {
	    if (!foundInsertTag) {
		return;
	    }

	    // Add a new line, if the last character wasn't one. This is
	    // needed for proper positioning of the cursor.
	    if(!lastWasNewline) {
		addContent(NEWLINE, 0, 1, (insertTag != null));
		lastWasNewline = true;
	    }

	    if (impliedP) {
		impliedP = false;
		inParagraph = false;
		blockClose(HTML.Tag.IMPLIED);
	    }
	    inBlock--;
	    
	    // an open/close with no content will be removed, so we
	    // add a space of content to keep the element being formed.
	    ElementSpec prev = (parseBuffer.size() > 0) ? 
		(ElementSpec) parseBuffer.lastElement() : null;
	    if (prev != null && prev.getType() == ElementSpec.StartTagType) {
		char[] one = new char[1];
		one[0] = ' ';
		addContent(one, 0, 1);
	    }
	    ElementSpec es = new ElementSpec(
		null, ElementSpec.EndTagType);
	    parseBuffer.addElement(es);
	}

	/**
	 * Add some text with the current character attributes.
	 *
	 * @param embedded the attributes of an embedded object.
	 */
	protected void addContent(char[] data, int offs, int length) {
	    addContent(data, offs, length, true);
	}

	/**
	 * Add some text with the current character attributes.
	 *
	 * @param embedded the attributes of an embedded object.
	 */
	protected void addContent(char[] data, int offs, int length,
				  boolean generateImpliedPIfNecessary) {
	    if (!foundInsertTag) {
		return;
	    }

	    if (generateImpliedPIfNecessary && (! inParagraph) && (! inPre)) {
		blockOpen(HTML.Tag.IMPLIED, new SimpleAttributeSet());
		inParagraph = true;
		impliedP = true;
	    }
	    emptyAnchor = false;
	    charAttr.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
	    AttributeSet a = charAttr.copyAttributes();
	    ElementSpec es = new ElementSpec(
		a, ElementSpec.ContentType, data, offs, length);
	    parseBuffer.addElement(es);

	    if (parseBuffer.size() > threshold) {
		try {
		    flushBuffer();
		} catch (BadLocationException ble) {
		}
	    }
	    if(length > 0) {
		lastWasNewline = (data[offs + length - 1] == '\n');
	    }
	}

	/**
	 * Add content that is basically specified entirely
	 * in the attribute set.
	 */
	protected void addSpecialElement(HTML.Tag t, MutableAttributeSet a) {
	    if ((t != HTML.Tag.FRAME) && (! inParagraph) && (! inPre)) {
		blockOpen(HTML.Tag.IMPLIED, new SimpleAttributeSet());
		inParagraph = true;
		impliedP = true;
	    }
	    if (!foundInsertTag) {
		if (!isInsertTag(t)) {
		    return ;
		}
		foundInsertTag();
		if (!insertInsertTag) {
		    return;
		}
	    }
	    emptyAnchor = false;
	    a.addAttributes(charAttr);
	    a.addAttribute(StyleConstants.NameAttribute, t);
	    char[] one = new char[1];
	    one[0] = ' ';
	    ElementSpec es = new ElementSpec(
		a.copyAttributes(), ElementSpec.ContentType, one, 0, 1);
	    parseBuffer.addElement(es);
	    // Set this to avoid generating a newline for frames, frames
	    // shouldn't have any content, and shouldn't need a newline.
	    if (t == HTML.Tag.FRAME) {
		lastWasNewline = true;
	    }
	}

	/**
	 * Flush the current parse buffer into the document.
	 */
	void flushBuffer() throws BadLocationException {
	    int oldLength = HTMLDocument.this.getLength();
	    ElementSpec[] spec = new ElementSpec[parseBuffer.size()];
	    parseBuffer.copyInto(spec);
	    if (oldLength == 0 && insertTag == null) {
		create(spec);
	    } else {
		insert(offset, spec);
	    }
	    parseBuffer.removeAllElements();
	    offset += HTMLDocument.this.getLength() - oldLength;
	}

	/**
	 * Adds the CSS rules in <code>rules</code>.
	 */
	void addCSSRules(String rules) {
	    StyleSheet ss = getStyleSheet();
	    ss.addRule(rules);
	}

	/**
	 * Adds the CSS stylesheet at <code>href</code> to the known list
	 * of stylesheets.
	 */
	void linkCSSStyleSheet(String href) {
	    URL url = null;
	    try {
		url = new URL(base, href);
	    } catch (MalformedURLException mfe) {
		try {
		    url = new URL(href);
		} catch (MalformedURLException mfe2) {
		    url = null;
		}
	    }
	    if (url != null) {
		getStyleSheet().importStyleSheet(url);
	    }
	}

	private boolean isInsertTag(HTML.Tag tag) {
	    return (insertTag == tag || (tag == HTML.Tag.IMPLIED &&
					 tag == HTML.Tag.P));
	}

	private void foundInsertTag() {
	    foundInsertTag = true;
	    if (popDepth > 0 || pushDepth > 0) {
		try {
		    if (offset == 0 || !getText(offset - 1, 1).equals("\n")) {
			// Need to insert a newline.
			AttributeSet newAttrs = null;
			boolean joinP = true;

			if (offset != 0) {
			    // Determine if we can use JoinPrevious, we can't
			    // if the Element has some attributes that are
			    // not meant to be duplicated.
			    Element charElement = getCharacterElement
				                    (offset - 1);
			    AttributeSet attrs = charElement.getAttributes();

			    if (attrs.isDefined(StyleConstants.
						ComposedTextAttribute)) {
				joinP = false;
			    }
			    else {
				Object name = attrs.getAttribute
				              (StyleConstants.NameAttribute);
				if (name instanceof HTML.Tag) {
				    HTML.Tag tag = (HTML.Tag)name;
				    if (tag == HTML.Tag.IMG ||
					tag == HTML.Tag.HR ||
					tag == HTML.Tag.COMMENT ||
					(tag instanceof HTML.UnknownTag)) {
					joinP = false;
				    }
				}
			    }
			}
			if (!joinP) {
			    // If not joining with the previous element, be
			    // sure and set the name (otherwise it will be
			    // inherited).
			    newAttrs = new SimpleAttributeSet();
			    ((SimpleAttributeSet)newAttrs).addAttribute
				              (StyleConstants.NameAttribute,
					       HTML.Tag.CONTENT);
			}
			ElementSpec es = new ElementSpec(newAttrs,
				     ElementSpec.ContentType, NEWLINE, 0,
				     NEWLINE.length);
			if (joinP) {
			    es.setDirection(ElementSpec.
					    JoinPreviousDirection);
			}
			parseBuffer.addElement(es);
		    }
		} catch (BadLocationException ble) {}
	    }
	    // pops
	    for (int counter = 0; counter < popDepth; counter++) {
		parseBuffer.addElement(new ElementSpec(null, ElementSpec.
						       EndTagType));
	    }
	    // pushes
	    for (int counter = 0; counter < pushDepth; counter++) {
		ElementSpec es = new ElementSpec(null, ElementSpec.
						 StartTagType);
		es.setDirection(ElementSpec.JoinNextDirection);
		parseBuffer.addElement(es);
	    }
	}

	int threshold;
	int offset;
	boolean inParagraph = false;
	boolean impliedP = false;
	boolean inPre = false;
	boolean inTextArea = false;
	TextAreaDocument textAreaDocument = null;
	boolean inTitle = false;
	boolean lastWasNewline = true;
	boolean emptyAnchor;
	/** True if (!emptyDocument && insertTag == null), this is used so
	 * much it is cached. */
	boolean midInsert;
	/** True when the body has been encountered. */
	boolean inBody;
	/** If non null, gives parent Tag that insert is to happen at. */
	HTML.Tag insertTag;
	/** If true, the insertTag is inserted, otherwise elements after
	 * the insertTag is found are inserted. */
	boolean insertInsertTag;
	/** Set to true when insertTag has been found. */
	boolean foundInsertTag;
	/** How many parents to ascend before insert new elements. */
	int popDepth;
	/** How many parents to descend (relative to popDepth) before
	 * inserting. */
	int pushDepth;
	/** Last Map that was encountered. */
	Map lastMap;
	/** Set to true when a style element is encountered. */
	boolean inStyle = false;
	/** Name of style to use. Obtained from Meta tag. */
	String defaultStyle;
	/** Vector describing styles that should be include. Will consist
	 * of a bunch of HTML.Tags, which will either be:
	 * <p>LINK: in which case it is followed by an AttributeSet
	 * <p>STYLE: in which case the following element is a String
	 * indicating the type (may be null), and the elements following
	 * it until the next HTML.Tag are the rules as Strings.
	 */
	Vector styles;
	/** True if inside the head tag. */
	boolean inHead = false;
	/** Set to true if the style language is text/css. Since this is
	 * used alot, it is cached. */
	boolean isStyleCSS;
	/** True if inserting into an empty document. */
	boolean emptyDocument;
	/** Attributes from a style Attribute. */
	AttributeSet styleAttributes;

	/**
	 * Current option, if in an option element (needed to
	 * load the label.
	 */
	Option option;

	protected Vector parseBuffer = new Vector();    // Vector<ElementSpec>
	protected MutableAttributeSet charAttr = new SimpleAttributeSet();
	Stack charAttrStack = new Stack();
	Hashtable tagMap;
	int inBlock = 0;	
	
    }

    /**
     * An element that represents a chunk of text that has
     * a set of html character level attributes assigned to
     * it.
     */
    public class RunElement extends LeafElement {

	/**
	 * Constructs an element that represents content within the
	 * document (has no children).
	 *
	 * @param parent  The parent element
	 * @param a       The element attributes
	 * @param offs0   The start offset >= 0
	 * @param offs1   The end offset >= offs0
	 */
	public RunElement(Element parent, AttributeSet a, int offs0, int offs1) {
	    super(parent, a, offs0, offs1);
	}

        /**
         * Gets the name of the element.
         *
         * @return the name, null if none
         */
        public String getName() {
	    Object o = getAttribute(StyleConstants.NameAttribute);
	    if (o != null) {
		return o.toString();
	    }
	    return super.getName();
	}

	/**
         * Gets the resolving parent.  HTML attributes are not inherited
	 * at the model level so we override this to return null.
         *
         * @return null, there are none
	 * @see AttributeSet#getResolveParent
	 */
        public AttributeSet getResolveParent() {
	    return null;
	}
    }

    /**
     * An element that represents a structual <em>block</em> of
     * html.
     */
    public class BlockElement extends BranchElement {

	/**
	 * Constructs a composite element that initially contains
	 * no children.
	 *
	 * @param parent  The parent element
         * @param a the attributes for the element
	 */
	public BlockElement(Element parent, AttributeSet a) {
	    super(parent, a);
	}

        /**
         * Gets the name of the element.
         *
         * @return the name, null if none
         */
        public String getName() {
	    Object o = getAttribute(StyleConstants.NameAttribute);
	    if (o != null) {
		return o.toString();
	    }
	    return super.getName();
	}

	/**
         * Gets the resolving parent.  HTML attributes are not inherited
	 * at the model level so we override this to return null.
         *
         * @return null, there are none
	 * @see AttributeSet#getResolveParent
	 */
        public AttributeSet getResolveParent() {
	    return null;
	}

    }

    /**
     * The following methods provide functionality required to
     * iterate over a the elements of the form and in the case
     * of a form submission, extract the data from each model
     * that is associated with each form element, and in the
     * case of reset, reinitialize the each model to its
     * initial state.
     */


    /**
     * This method searches the names of the attribute
     * in the set, for HTML.Tag.FORM.  And if found,
     * it returns the attribute set that is associated
     * with HTML.Tag.FORM.
     *
     * @param attr to search.
     * @return FORM attributes or null if not found.
     */ 
    AttributeSet getFormAttributes(AttributeSet attr) {
	
	Enumeration names = attr.getAttributeNames();
	while (names.hasMoreElements()) {
	    Object name = names.nextElement();
	    if (name instanceof HTML.Tag) {
		HTML.Tag tag = (HTML.Tag)name;
		if (tag == HTML.Tag.FORM) {
		    Object o = attr.getAttribute(tag);
		    if (o != null && o instanceof AttributeSet) {
			return (AttributeSet)o;
		    }
		}
	    }
	}
	return null;
    }
    

    /**
     * This method is used to determine which form elements are part 
     * of the same form as the element that triggered the submit or 
     * reset action.  This is determined by matching the form attributes 
     * associated with the trigger element, with the form attributes associated
     * with every other form element in the document.
     *
     * @param elemAttributes attributes associated with a form element.
     * @param formAttributes attributes associated with the trigger element.
     * @return true if matched false otherwise.
     */ 
    private boolean formMatchesSubmissionRequest(AttributeSet elemAttributes, 
						 AttributeSet formAttributes) {
	AttributeSet attr = getFormAttributes(elemAttributes);
	if (attr != null) {
	    return formAttributes.isEqual(attr);
	} 
	return false;
    }
    
    
    /**
     * This method is responsible for iterating over the 
     * element hierarchy, and extracting data from the 
     * models associated with the relevant form elements.
     * By relevant is meant, form elements that are part
     * of the same form whose element triggered the submit
     * action.
     *
     * @param buffer that contains that data to submit.
     * @param targetElement the element that triggered the 
     *                      form submission.
     */
    void getFormData(StringBuffer buffer, Element targetElement) {

	AttributeSet attr = targetElement.getAttributes();
	AttributeSet formAttributes = getFormAttributes(attr);
	ElementIterator it = new ElementIterator(getDefaultRootElement());
	Element next;

	if (formAttributes == null) {
	    return;
	}
	
	boolean startedLoading = false;

	while((next = it.next()) != null) {

	    AttributeSet elemAttr = next.getAttributes();

	    if (formMatchesSubmissionRequest(elemAttr, formAttributes)) {

		startedLoading = true;
		String type = (String) elemAttr.getAttribute(HTML.Attribute.TYPE);

		if (type != null && 
		    type.equals("submit") && 
		    next != targetElement) {
		    // do nothing - this submit isnt the trigger
		} else if (type == null || 
			   !type.equals("image")) {
		    // images only result in data if they triggered
		    // the submit and they require that the mouse click
		    // coords be appended to the data.  Hence its
		    // processing is handled by the view.
		    loadElementDataIntoBuffer(next, buffer);
		}
	    }  else if (startedLoading && next.isLeaf()) {
		/*
		  if startedLoading is true, this means that we
		  did find the form elements that pertain to
		  the form being submitted and so loading has started.
		  In this context, if we encounter a leaf element
		  that does not have the HTML.Tag.FORM attribute
		  set with the same attribute values, then we have
		  reached the end of the form and we can safely
		  quit.
		*/
		break;
	    }
	}
	return;
    }


    /**
     * This method is responsible for loading the data
     * associdated with the element into the buffer.
     * The format in which data is appended, depends
     * on the type of the form element.  Essentially
     * data is loaded in name/value pairs.
     * 
     */
    private void loadElementDataIntoBuffer(Element elem, StringBuffer buffer) {

	AttributeSet attr = elem.getAttributes();
	String name = (String)attr.getAttribute(HTML.Attribute.NAME);
	if (name == null) {
	    return;
 	}
	String value = null;
	HTML.Tag tag = (HTML.Tag) elem.getAttributes().getAttribute(StyleConstants.NameAttribute);

	if (tag == HTML.Tag.INPUT) {
	    value = getInputElementData(attr);
	} else if (tag ==  HTML.Tag.TEXTAREA) {
	    value = getTextAreaData(attr);
	} else if (tag == HTML.Tag.SELECT) {
	    loadSelectData(attr, buffer);
	}
	
	if (name != null && value != null) {
	    appendBuffer(buffer, name, value);
	}
    }


    /**
     * This methhod returns the data associated with an <input> form
     * element.  The value of "type" attributes is
     * used to determine the type of the model associated
     * with the element and then the relevant data is
     * extracted.
     */
    private String getInputElementData(AttributeSet attr) {
	
	Object model = attr.getAttribute(StyleConstants.ModelAttribute);
	String type = (String) attr.getAttribute(HTML.Attribute.TYPE);
	String value = null;
	
	if (type.equals("text") || type.equals("password")) {
	    Document doc = (Document)model;
	    try {
		value = doc.getText(0, doc.getLength());
	    } catch (BadLocationException e) {
		value = null;
	    }
	} else if (type.equals("submit") || type.equals("hidden")) {
	    value = (String) attr.getAttribute(HTML.Attribute.VALUE);
	    if (value == null) {
		value = "";
	    }
	} else if (type.equals("radio") || type.equals("checkbox")) {
	    ButtonModel m = (ButtonModel)model;
	    if (m.isSelected()) {
		value = (String) attr.getAttribute(HTML.Attribute.VALUE);
		if (value == null) {
		    value = "on";
		}
	    }
	}
	return value;
    }

    /**
     * This method returns the data associated with the <textarea> form
     * element.  This is doen by getting the text stored in the
     * Document model.
     */
    private String getTextAreaData(AttributeSet attr) {
	Document doc = (Document)attr.getAttribute(StyleConstants.ModelAttribute);
	try {
	    return doc.getText(0, doc.getLength());
	} catch (BadLocationException e) {
	    return null;
	}
    }


    /**
     * Loads the buffer with the data associated with the Select
     * form element.  Basically, only items that are selected
     * and have their name attribute set are added to the buffer.
     */
    private void loadSelectData(AttributeSet attr, StringBuffer buffer) {

	String name = (String)attr.getAttribute(HTML.Attribute.NAME);
	if (name == null) {
	    return;
	}
	Object m = attr.getAttribute(StyleConstants.ModelAttribute);
	if (m instanceof OptionListModel) {
	    OptionListModel model = (OptionListModel)m;
	    
	    for (int i = 0; i < model.getSize(); i++) {
		if (model.isSelectedIndex(i)) {
		    Option option = (Option) model.getElementAt(i);
		    appendBuffer(buffer, name, option.getValue());
		}
	    }
	} else if (m instanceof ComboBoxModel) {
	    ComboBoxModel model = (ComboBoxModel)m;
	    Option option = (Option)model.getSelectedItem();
	    if (option != null) {
		appendBuffer(buffer, name, option.getValue());
	    }
	}
    }

    /**
     * Responsible for appending name / value pairs into the 
     * buffer.  Both names and values are encoded using the 
     * URLEncoder.encode() method before being added to the
     * buffer.
     */
    private void appendBuffer(StringBuffer buffer, String name, String value) {
	ampersand(buffer);
	String encodedName = URLEncoder.encode(name);
	buffer.append(encodedName);
	buffer.append('=');
	String encodedValue = URLEncoder.encode(value);
	buffer.append(encodedValue);
    }

    /**
     * Appends an '&' as a separator if the buffer has
     * length > 0.
     */
    private void ampersand(StringBuffer buf) {
	if (buf.length() > 0) {
	    buf.append('&');
	}
    }

    /**
     * Iterates over the element hierarchy to determine if
     * the element parameter, which is assumed to be an
     * <input> element of type password or text, is the last
     * one of either kind, in the form to which it belongs.
     */
    boolean isLastTextOrPasswordField(Element elem) {
	ElementIterator it = new ElementIterator(getDefaultRootElement());
	Element next;
	boolean found = false;
	AttributeSet formAttributes = getFormAttributes(elem.getAttributes());

	while((next = it.next()) != null) {

	    AttributeSet elemAttr = next.getAttributes();

	    if (formMatchesSubmissionRequest(elemAttr, formAttributes)) {
		if (found) {
		    if (matchNameAttribute(elemAttr, HTML.Tag.INPUT)) {
			String type = (String) elemAttr.getAttribute(HTML.Attribute.TYPE);
	
			if (type.equals("text") || type.equals("password")) {
			    return false;
			}
		    }
		} 
		if (next == elem) {
		    found = true;
		}
	    } else if (found && next.isLeaf()) {
		// You can safely break, coz you have exited the
		// form that you care about.
		break;
	    }
	}
	return true;
    }

    /**
     * This method is responsible for resetting the form
     * to its initial state by reinitializinng the models
     * associated with each form element to its initial
     * value.
     *
     * param element that triggered the reset.
     */
    void resetForm(Element elem) {
	ElementIterator it = new ElementIterator(getDefaultRootElement());
	Element next;
	boolean startedReset = false;
	AttributeSet formAttributes = getFormAttributes(elem.getAttributes());

	while((next = it.next()) != null) {

	    AttributeSet elemAttr = next.getAttributes();

	    if (formMatchesSubmissionRequest(elemAttr, formAttributes)) {
		Object m = elemAttr.getAttribute(StyleConstants.ModelAttribute);
		if (m instanceof TextAreaDocument) {
		    TextAreaDocument doc = (TextAreaDocument)m;
		    doc.reset();
		} else	if (m instanceof PlainDocument) {
		    try {
			PlainDocument doc =  (PlainDocument)m;
			doc.remove(0, doc.getLength());
			if (matchNameAttribute(elemAttr, HTML.Tag.INPUT)) {
			    String value = (String)elemAttr.getAttribute(HTML.Attribute.VALUE);
			    if (value != null) {
				doc.insertString(0, value, null);
			    }
			}
		    } catch (BadLocationException e) {
		    }
		} else	if (m instanceof OptionListModel) {
		    OptionListModel model = (OptionListModel) m;
		    int size = model.getSize();
		    for (int i = 0; i < size; i++) {
			model.removeIndexInterval(i, i);
		    }
		    BitSet selectionRange = model.getInitialSelection();
		    for (int i = 0; i < selectionRange.size(); i++) {
			if (selectionRange.get(i)) {
			    model.addSelectionInterval(i, i);
			}
		    }
		} else 	if (m instanceof OptionComboBoxModel) {
		    OptionComboBoxModel model = (OptionComboBoxModel) m;
		    Option option = model.getInitialSelection();
		    if (option != null) {
			model.setSelectedItem(option);
		    }
		} else 	if (m instanceof JToggleButton.ToggleButtonModel) {
		    boolean checked = ((String)elemAttr.getAttribute(HTML.Attribute.CHECKED) != null);
		    JToggleButton.ToggleButtonModel model = (JToggleButton.ToggleButtonModel)m;
		    model.setSelected(checked);
		}
		startedReset = true;
	    } else if (startedReset && next.isLeaf()) {
		break;
	    }
	}
    }
 }
