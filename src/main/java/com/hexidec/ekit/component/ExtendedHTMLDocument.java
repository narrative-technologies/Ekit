/*
GNU Lesser General Public License

PropertiesDialog
Copyright (C) 2003 Frits Jalvingh, Jerry Pommer & Howard Kistler

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.hexidec.ekit.component;

import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.UndoableEdit;
import java.util.Enumeration;

public class ExtendedHTMLDocument extends HTMLDocument {

    private static final long serialVersionUID = 1813479797588760512L;

    public ExtendedHTMLDocument(AbstractDocument.Content c, StyleSheet styles) {
        super(c, styles);
    }

    public ExtendedHTMLDocument(StyleSheet styles) {
        super(styles);
    }

    public ExtendedHTMLDocument() {
    }

    @Override
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
        // in the super's implementation of this method is where we were getting the <p style="margin-top: 0">
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
     * ?berschreibt die Attribute des Elements.
     *
     * @param e   Element bei dem die Attribute ge?ndert werden sollen
     * @param a   AttributeSet mit den neuen Attributen
     * @param tag Angabe was für ein Tag das Element ist
     */
    public void replaceAttributes(Element e, AttributeSet a, HTML.Tag tag) {
        if ((e != null) && (a != null)) {
            try {
                writeLock();
                int start = e.getStartOffset();
                DefaultDocumentEvent changes = new DefaultDocumentEvent(start, e.getEndOffset() - start, DocumentEvent.EventType.CHANGE);
                AttributeSet sCopy = a.copyAttributes();
                changes.addEdit(new AttributeUndoableEdit(e, sCopy, false));
                MutableAttributeSet attr = (MutableAttributeSet) e.getAttributes();
                Enumeration aNames = attr.getAttributeNames();
                Object value;
                Object aName;
                while (aNames.hasMoreElements()) {
                    aName = aNames.nextElement();
                    value = attr.getAttribute(aName);
                    if (value != null && !value.toString().equalsIgnoreCase(tag.toString())) {
                        attr.removeAttribute(aName);
                    }
                }
                attr.addAttributes(a);
                changes.end();
                fireChangedUpdate(changes);
                fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
            } finally {
                writeUnlock();
            }
        }
    }

    public void removeElements(Element e, int index, int count)
            throws BadLocationException {
        writeLock();
        int start = e.getElement(index).getStartOffset();
        int end = e.getElement(index + count - 1).getEndOffset();
        try {
            Element[] removed = new Element[count];
            Element[] added = new Element[0];
            for (int counter = 0; counter < count; counter++) {
                removed[counter] = e.getElement(counter + index);
            }
            DefaultDocumentEvent dde = new DefaultDocumentEvent(start, end - start, DocumentEvent.EventType.REMOVE);
            ((AbstractDocument.BranchElement) e).replace(index, removed.length, added);
            dde.addEdit(new ElementEdit(e, index, removed, added));
            UndoableEdit u = getContent().remove(start, end - start);
            if (u != null) {
                dde.addEdit(u);
            }
            postRemoveUpdate(dde);
            dde.end();
            fireRemoveUpdate(dde);
            if (u != null) {
                fireUndoableEditUpdate(new UndoableEditEvent(this, dde));
            }
        } finally {
            writeUnlock();
        }
    }
}