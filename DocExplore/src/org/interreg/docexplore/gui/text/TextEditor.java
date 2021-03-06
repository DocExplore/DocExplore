/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui.text;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class TextEditor extends JPanel
{
	private static final long serialVersionUID = 4030693184664043889L;
	
	public JTextPane textPane;
	TextToolbar toolBar;
	Color textBackground;
	
	@SuppressWarnings("serial")
	public TextEditor()
	{
		super(new BorderLayout());
		
		this.textBackground = Color.white;
		this.textPane = new JTextPane()
		{
			public void paintComponent(Graphics g)
			{
				g.setColor(textBackground);
				g.fillRect(0, 0, getWidth(), getHeight());
				super.paintComponent(g);
			}
		};
		textPane.setBackground(new Color(0, 0, 0, 0));
		textPane.setEditorKit(new HTMLEditorKit());
		textPane.setDocument(textPane.getEditorKit().createDefaultDocument());
		textPane.setOpaque(false);
		textPane.setCaretColor(Color.white);
		
		add(new JScrollPane(textPane), BorderLayout.CENTER);
		
		this.toolBar = new TextToolbar(textPane);
		add(toolBar, BorderLayout.SOUTH);
	}
	
	public void setTextBackground(Color color) {textBackground = color;}
	//public void setStyles(Color color, String fontName, int size) {toolBar.setStyles(color, fontName, size);}
	public void setDocument(HTMLDocument document) {textPane.setDocument(document);}
	public void setDefaultDocument() {textPane.setDocument(textPane.getEditorKit().createDefaultDocument());}
	public HTMLDocument getDocument() {return (HTMLDocument)textPane.getDocument();}
	
	public void append(String text) throws Exception
	{
		HTMLDocument doc = getDocument();
		Element [] elements = doc.getRootElements();
		Element body = null;
		for(int i=0;i<elements[0].getElementCount();i++)
		{
			Element element = elements[0].getElement(i);
			if (element.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.BODY)
				{body = element; break;}
		}
		doc.insertString(body.getEndOffset(), text, textPane.getInputAttributes());
	}
	
	public String getContent() throws Exception
	{
		String text = textPane.getText();
		int start = text.indexOf("<body>")+6;
		int end = text.lastIndexOf("</body>");
		return text.substring(start, end);
	}
}
