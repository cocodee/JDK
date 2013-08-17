/*
 * @(#)AccessibleText.java	1.2 00/01/12
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package javax.accessibility;


import java.lang.*;
import java.util.*;
import java.awt.*;
import javax.swing.text.*;


/**
 * <P>The AccessibleText interface should be implemented by all 
 * classes that present textual information on the display.  This interface
 * provides the standard mechanism for an assistive technology to access 
 * that text via its content, attributes, and spatial location.  
 * Applications can determine if an object supports the AccessibleText 
 * interface by first obtaining its AccessibleContext (see {@link Accessible})
 * and then calling the {@link AccessibleContext#getAccessibleText} method of 
 * AccessibleContext.  If the return value is not null, the object supports this
 * interface.
 *
 * @see Accessible
 * @see Accessible#getAccessibleContext
 * @see AccessibleContext
 * @see AccessibleContext#getAccessibleText
 *
 * @version	1.13 01/20/98 07:53:43
 * @author	Peter Korn
 */
public interface AccessibleText {

    /**
     * Constant used to indicate that the part of text that should be
     * retrieved is a character.
     * 
     * @see #getAtIndex
     * @see #getAfterIndex
     * @see #getBeforeIndex
     */
    public static final int CHARACTER = 1;

    /**
     * Constant used to indicate that the part of text that should be 
     * retrieved is a word.
     * 
     * @see #getAtIndex
     * @see #getAfterIndex
     * @see #getBeforeIndex
     */
    public static final int WORD = 2;

    /**
     * Constant used to indicate that the part of text that should be 
     * retrieved is a sentence.
     * 
     * @see #getAtIndex
     * @see #getAfterIndex
     * @see #getBeforeIndex
     */
    public static final int SENTENCE = 3;

    /**
     * Given a point in local coordinates, return the zero-based index
     * of the character under that Point.  If the point is invalid,
     * this method returns -1.
     *
     * @param p the Point in local coordinates
     * @return the zero-based index of the character under Point p; if 
     * Point is invalid returns -1.
     */
    public int getIndexAtPoint(Point p);

    /**
     * Determine the bounding box of the character at the given 
     * index into the string.  The bounds are returned in local
     * coordinates.  If the index is invalid an empty rectangle is returned.
     *
     * @param i the index into the String
     * @return the screen coordinates of the character's the bounding box,
     * if index is invalid returns an empty rectangle.
     */
    public Rectangle getCharacterBounds(int i);

    /**
     * Return the number of characters (valid indicies) 
     *
     * @return the number of characters
     */
    public int getCharCount();

    /**
     * Return the zero-based offset of the caret.
     *
     * Note: That to the right of the caret will have the same index
     * value as the offset (the caret is between two characters).
     * @return the zero-based offset of the caret.
     */
    public int getCaretPosition();

    /**
     * Return the String at a given index. 
     *
     * @param part the CHARACTER, WORD, or SENTENCE to retrieve
     * @param index an index within the text
     * @return the letter, word, or sentence
     */
    public String getAtIndex(int part, int index);

    /**
     * Return the String after a given index.
     *
     * @param part the CHARACTER, WORD, or SENTENCE to retrieve
     * @param index an index within the text
     * @return the letter, word, or sentence
     */
    public String getAfterIndex(int part, int index);

    /**
     * Return the String before a given index.
     *
     * @param part the CHARACTER, WORD, or SENTENCE to retrieve
     * @param index an index within the text
     * @return the letter, word, or sentence
     */
    public String getBeforeIndex(int part, int index);

    /**
     * Return the AttributeSet for a given character at a given index
     *
     * @param i the zero-based index into the text 
     * @return the AttributeSet of the character
     */
    public AttributeSet getCharacterAttribute(int i);

    /**
     * Returns the start offset within the selected text.
     * If there is no selection, but there is
     * a caret, the start and end offsets will be the same.
     *
     * @return the index into the text of the start of the selection
     */
    public int getSelectionStart();

    /**
     * Returns the end offset within the selected text.
     * If there is no selection, but there is
     * a caret, the start and end offsets will be the same.
     *
     * @return the index into teh text of the end of the selection
     */
    public int getSelectionEnd();

    /**
     * Returns the portion of the text that is selected. 
     *
     * @return the String portion of the text that is selected
     */
    public String getSelectedText();
}
