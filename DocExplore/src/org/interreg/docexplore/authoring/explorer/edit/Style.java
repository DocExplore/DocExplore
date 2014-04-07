package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Style
{
	public String name;
	public Color color;
	public String font;
	public int size;
	public boolean bold, italic, underline, centered;
	
	public Style(String name, Color color, String font, int size, boolean bold, boolean italic, boolean underline, boolean centered)
	{
		this.name = name;
		this.color = color;
		this.font = font;
		this.size = size;
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
		this.centered = centered;
	}
	public Style(Style style)
	{
		this.name = style.name;
		this.color = style.color;
		this.font = style.font;
		this.size = style.size;
		this.bold = style.bold;
		this.italic = style.italic;
		this.underline = style.underline;
		this.centered = style.centered;
	}
	
	static int [] webSizes = {6, 8, 10, 12, 14, 18, 24, 32};
	
	public String serialize()
	{
		return name+"-"+color.getRed()+"-"+color.getGreen()+"-"+color.getBlue()+"-"+font+"-"+size+"-"+(bold ? "y" : "n")+(italic ? "y" : "n")+(underline ? "y" : "n")+(centered ? "y" : "n");
	}
	public static Style unserialize(String style)
	{
		String [] vals = style.split("-");
		Style res = new Style(vals[0], new Color(Integer.parseInt(vals[1]), Integer.parseInt(vals[2]), Integer.parseInt(vals[3])), vals[4], Integer.parseInt(vals[5]), 
			vals[6].charAt(0) == 'y', vals[6].charAt(1) == 'y', vals[6].charAt(2) == 'y', vals[6].length() > 3 && vals[6].charAt(3) == 'y');
		if (res.size < 8)
			res.size = webSizes[res.size];
		return res;
	}
	
	public void apply(JTextPane comp)
	{
		String fontName = font+"-";
		if (bold || italic)
			fontName += (bold ? "bold" : "")+(italic ? "italic" : "");
		else fontName += "plain";
		fontName += "-"+size;//webSizes[size];
		Font font = Font.decode(fontName);
		int margin = size/8;
		comp.setBorder(BorderFactory.createEmptyBorder(0, margin, 0, 0));
		comp.setFont(font);
		comp.setForeground(color);
		StyledDocument doc = comp.getStyledDocument();
		SimpleAttributeSet attrs = new SimpleAttributeSet();
		StyleConstants.setAlignment(attrs, centered ? StyleConstants.ALIGN_CENTER : StyleConstants.ALIGN_LEFT);
		doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
	}
	public String apply(String text)
	{
		int margin = size/8;
		return "<html><div style=\"" +
			"margin-left:"+margin+"px; margin-right:"+2*margin+"px; " + 
			"font-size:"+size+"px; " +
			"color:#"+toHex(color)+"; " +
			"font-family:"+font +"; " +
			"\">"+(centered ? "<center>" : "")+(bold ? "<b>" : "")+(italic ? "<i>" : "")+(underline ? "<u>" : "")+text+
			(centered ? "</center>" : "")+(bold ? "</b>" : "")+(italic ? "</i>" : "")+(underline ? "</u>" : "")+"</div></html>";
	}
	
	public String toString() {return apply(name);}
	
	public static String toHex(Color color)
	{
		return (color.getRed() < 16 ? "0" : "")+Integer.toHexString(color.getRed())+
			(color.getGreen() < 16 ? "0" : "")+Integer.toHexString(color.getGreen())+
			(color.getBlue() < 16 ? "0" : "")+Integer.toHexString(color.getBlue());
	}
}
