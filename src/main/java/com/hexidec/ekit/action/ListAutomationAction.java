/*
GNU Lesser General Public License

ListAutomationAction
Copyright (C) 2000 Howard Kistler

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

package com.hexidec.ekit.action;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.StringTokenizer;

import com.hexidec.ekit.EkitCore;
import com.hexidec.ekit.component.HTMLUtilities;

/**
 * Class for automatically creating bulleted lists from selected text
 */
public class ListAutomationAction extends HTMLEditorKit.InsertHTMLTextAction {
    protected EkitCore parentEkit;
    private HTML.Tag baseTag;
    private HTMLUtilities htmlUtilities;

    public ListAutomationAction(EkitCore ekit, String sLabel, HTML.Tag listType) {
        super(sLabel, "", listType, HTML.Tag.LI);
        parentEkit = ekit;
        baseTag = listType;
        htmlUtilities = new HTMLUtilities(ekit);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        try {
            JEditorPane jepEditor = parentEkit.getTextPane();
            String selTextBase = jepEditor.getSelectedText();
            int textLength = -1;
            if (selTextBase != null) {
                textLength = selTextBase.length();
            }
            if (selTextBase == null || textLength < 1) {
                int pos = parentEkit.getCaretPosition();
                parentEkit.setCaretPosition(pos);
                if (!"newListPoint".equals(ae.getActionCommand())) {
                    if (htmlUtilities.checkParentsTag(HTML.Tag.OL) || htmlUtilities.checkParentsTag(HTML.Tag.UL)) {
                        revertList(htmlUtilities.getListItemContainer());
                        return;
                    }
                }
                String listType = (baseTag == HTML.Tag.OL ? "ol" : "ul");
                StringBuilder sbNew = new StringBuilder();
                if (htmlUtilities.checkParentsTag(baseTag)) {
                    sbNew.append("<li></li>");
                    insertHTML(parentEkit.getTextPane(), parentEkit.getExtendedHtmlDoc(), parentEkit.getTextPane()
                                                                                                    .getCaretPosition(), sbNew
                                       .toString(), 0, 0, HTML.Tag.LI);
                } else {
                    boolean isLast = false;
                    int caretPos = parentEkit.getCaretPosition();
                    if (caretPos == parentEkit.getExtendedHtmlDoc().getLength()) {
                        isLast = true;
                    }
                    sbNew.append("<").append(listType).append("><li></li></").append(listType).append(">")
                         .append(isLast ? "<p>&nbsp;</p>" : "");
                    insertHTML(parentEkit.getTextPane(), parentEkit.getExtendedHtmlDoc(), parentEkit.getTextPane()
                                                                                                    .getCaretPosition(), sbNew
                                       .toString(), 0, 0, (listType.equals("ol") ? HTML.Tag.OL : HTML.Tag.UL));
                    parentEkit.setCaretPosition(caretPos + 1);
                }
                parentEkit.refreshOnUpdate();
            } else {
                if (htmlUtilities.checkParentsTag(HTML.Tag.OL) || htmlUtilities.checkParentsTag(HTML.Tag.UL)) {
                    revertList(htmlUtilities.getListItemContainer());
                } else {
                    String listType = (baseTag == HTML.Tag.OL ? "ol" : "ul");
                    HTMLDocument htmlDoc = (HTMLDocument) (jepEditor.getDocument());
                    int iStart = jepEditor.getSelectionStart();
                    int iEnd = jepEditor.getSelectionEnd();
                    String selText = htmlDoc.getText(iStart, iEnd - iStart);
/*
for(int ch = 0; ch < selText.length(); ch++)
{
	Element elem = htmlDoc.getCharacterElement(iStart + ch);
	System.out.println("elem " + ch + ": " + elem.getName());
	System.out.println("char " + ch + ": " + selText.charAt(ch) + " [" + Character.getNumericValue(selText.charAt(ch)) + "]");
	if(Character.getNumericValue(selText.charAt(ch)) < 0)
	{
		System.out.println("  is space?    " + ((selText.charAt(ch) == '\u0020') ? "YES" : "---"));
		System.out.println("  is return?   " + ((selText.charAt(ch) == '\r') ? "YES" : "---"));
		System.out.println("  is newline?  " + ((selText.charAt(ch) == '\n') ? "YES" : "---"));
		System.out.println("  is nextline? " + ((selText.charAt(ch) == '\u0085') ? "YES" : "---"));
		System.out.println("  is linesep?  " + ((selText.charAt(ch) == '\u2028') ? "YES" : "---"));
		System.out.println("  is parasep?  " + ((selText.charAt(ch) == '\u2029') ? "YES" : "---"));
		System.out.println("  is verttab?  " + ((selText.charAt(ch) == '\u000B') ? "YES" : "---"));
		System.out.println("  is formfeed? " + ((selText.charAt(ch) == '\u000C') ? "YES" : "---"));
	}
}
*/
                    StringBuilder sbNew = new StringBuilder();
                    sbNew.append("<").append(listType).append(">");
                    // tokenize on known linebreaks if present, otherwise manually parse on <br> break tags
                    if ((selText.contains("\r")) || (selText.contains("\n"))) {
                        String sToken = ((selText.contains("\r")) ? "\r" : "\n");
                        StringTokenizer stTokenizer = new StringTokenizer(selText, sToken);
                        while (stTokenizer.hasMoreTokens()) {
                            sbNew.append("<li>");
                            sbNew.append(stTokenizer.nextToken());
                            sbNew.append("</li>");
                        }
                    } else {
                        StringBuilder sbItem = new StringBuilder();
                        for (int ch = 0; ch < selText.length(); ch++) {
                            String elem = (htmlDoc.getCharacterElement(iStart + ch) != null ? htmlDoc
                                    .getCharacterElement(iStart + ch).getName().toLowerCase() : "[null]");
                            if (elem.equals("br") && sbItem.length() > 0) {
                                sbNew.append("<li>");
                                sbNew.append(sbItem.toString());
                                sbNew.append("</li>");
                                sbItem.delete(0, sbItem.length());
                            } else {
                                sbItem.append(selText.charAt(ch));
                            }
                        }
                    }
                    sbNew.append("</").append(listType).append(">");
                    htmlDoc.remove(iStart, iEnd - iStart);
                    insertHTML(jepEditor, htmlDoc, iStart, sbNew.toString(), 1, 1, null);
                }
            }
        } catch (BadLocationException ble) {
        }
    }

    private void revertList(Element element) {
//System.out.println("Reverting list " + element.toString());
        if (element == null) {
            return;
        }
        int pos = parentEkit.getCaretPosition();
        HTML.Tag tag = htmlUtilities.getHTMLTag(element);
        String source = parentEkit.getSourcePane().getText();
        boolean hit = false;
        String idString;
        int counter = 0;
        do {
            hit = false;
            idString = "revertomatictaggen" + counter;
            if (source.contains(idString)) {
                counter++;
                hit = true;
                if (counter > 10000) {
                    return;
                }
            }
        } while (hit);
        SimpleAttributeSet sa = new SimpleAttributeSet(element.getAttributes());
        sa.addAttribute("id", idString);
        parentEkit.getExtendedHtmlDoc().replaceAttributes(element, sa, tag);
        parentEkit.refreshOnUpdate();
        source = parentEkit.getSourcePane().getText();
        StringBuilder newHtmlString = new StringBuilder();
        int[] position = htmlUtilities.getPositions(element, source, true, idString);
        if (position == null) {
            return;
        }
        for (int aPosition : position) {
            if (aPosition < 0) {
                return;
            }
        }
        int beginStartTag = position[0];
        int endStartTag = position[1];
        int beginEndTag = position[2];
        int endEndTag = position[3];
        newHtmlString.append(source.substring(0, beginStartTag));
        String listText = source.substring(endStartTag, beginEndTag);
//System.out.println("Affected text is :" + listText);
        if (parentEkit.getEnterKeyIsBreak()) {
            listText = listText.replaceAll("<li>", "");
            listText = listText.replaceAll("</li>", "<br/>");
            newHtmlString.append("<br/>").append(listText);
        } else {
            listText = listText.replaceAll("<li>", "<p>");
            listText = listText.replaceAll("</li>", "</p>");
            newHtmlString.append(listText);
        }
//System.out.println("Updated text is :" + listText);
        newHtmlString.append(source.substring(endEndTag, source.length()));
        parentEkit.getTextPane().setText(newHtmlString.toString());
        parentEkit.refreshOnUpdate();
    }
}