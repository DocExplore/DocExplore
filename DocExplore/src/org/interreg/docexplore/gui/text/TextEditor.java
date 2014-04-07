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
