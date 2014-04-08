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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

import org.interreg.docexplore.authoring.explorer.edit.Style;
import org.interreg.docexplore.authoring.explorer.edit.StyleDialog;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.ImageUtils;

public class TextToolbar extends JPanel
{
	private static final long serialVersionUID = 2080901140355132329L;
	
	public static interface Listener
	{
		public void attributesChanged(JTextPane textPane);
	}
	
	public static class TextSize
	{
		String name;
		int value;
		public TextSize(String name, int value) {this.name = name; this.value = value;}
		public String toString() {return name;}
	}
	
	public JTextPane textPane;
	
	JComboBox styleBox;
	JToggleButton bold, italic, underline;
	JPanel colorPanel;
	JColorChooser colorChooser;
	ActionListener colorSelect;
	JComboBox fontBox;
	JComboBox sizeBox;
	
	boolean sendStyleEvents = true;
	
	public static Color styleBackground = new Color(0, 0, 0, 192);
	public static Color styleHighLightedBackground = new Color(16, 32, 64, 192);
	@SuppressWarnings("serial")
	public TextToolbar(JTextPane _textPane)
	{
		super(new WrapLayout(FlowLayout.LEFT));
		
		this.textPane = _textPane;
		
		JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		stylePanel.setBorder(BorderFactory.createTitledBorder("Style"));//LineBorder(getBackground().darker()));
		styleBox = new JComboBox(new DefaultComboBoxModel());
		styleBox.setRequestFocusEnabled(false);
		styleBox.setFocusable(false);
		((DefaultComboBoxModel)styleBox.getModel()).addElement(null);
		((DefaultComboBoxModel)styleBox.getModel()).addElement(new Style("Body", Color.white, "Times New Roman", 5, false, false, false, false));
		((DefaultComboBoxModel)styleBox.getModel()).addElement(new Style("Title 1", Color.white, "Times New Roman", 7, false, false, true, false));
		((DefaultComboBoxModel)styleBox.getModel()).addElement(new Style("Title 2", Color.white, "Times New Roman", 6, false, true, false, false));
		styleBox.setRenderer(new ListCellRenderer()
		{
			@Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				JPanel panel = new JPanel(new BorderLayout());
				panel.setBorder(BorderFactory.createLineBorder(isSelected ? styleHighLightedBackground : styleBackground, 3));
				JLabel label = new JLabel(value == null ? " " : value.toString());
				label.setOpaque(true);
				label.setBackground(isSelected ? styleHighLightedBackground : styleBackground);
				panel.add(label, BorderLayout.CENTER);
				return panel;
			}
		});
		JButton edit = new JButton(new AbstractAction("", ImageUtils.getIcon("pencil-24x24.png")) {@Override public void actionPerformed(ActionEvent e)
		{
			DefaultComboBoxModel model = (DefaultComboBoxModel)styleBox.getModel();
			Style [] styles = new Style [model.getSize()-1];
			for (int i=1;i<model.getSize();i++)
				styles[i-1] = (Style)model.getElementAt(i);
			styles = StyleDialog.editStyles(TextToolbar.this, styles);
			if (styles != null)
			{
				model.removeAllElements();
				model.addElement(null);
				for (int i=0;i<styles.length;i++)
					model.addElement(styles[i]);
				styleBox.setSelectedIndex(0);
			}
		}});
		styleBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() != ItemEvent.SELECTED) return;
				Style style = (Style)e.getItem();
				if (style == null)
					return;
				setStyle(style, true);
				styleBox.setSelectedIndex(0);
			}
		});
		stylePanel.add(styleBox);
		stylePanel.add(edit);
		
		JPanel decorationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		decorationPanel.setBorder(BorderFactory.createTitledBorder("Decoration"));
		bold = new JToggleButton(new AbstractAction("<html><b>B</b></html>") {@Override public void actionPerformed(ActionEvent e)
		{
			if (!sendStyleEvents || textPane == null) return;
			System.out.println(e.getID());
			new StyledEditorKit.BoldAction().actionPerformed(new ActionEvent(textPane, e.getID(), "")); 
			notifyAttributesChanged();
		}});
		bold.setRequestFocusEnabled(false);
		bold.setFocusable(false);
		italic = new JToggleButton(new AbstractAction("<html><i>I</i></html>") {@Override public void actionPerformed(ActionEvent e)
		{
			if (!sendStyleEvents || textPane == null) return;
			new StyledEditorKit.ItalicAction().actionPerformed(new ActionEvent(textPane, e.getID(), ""));
			notifyAttributesChanged();
		}});
		italic.setRequestFocusEnabled(false);
		italic.setFocusable(false);
		underline = new JToggleButton(new AbstractAction("<html><u>U</u></html>") {@Override public void actionPerformed(ActionEvent e)
		{
			if (!sendStyleEvents || textPane == null) return;
			new StyledEditorKit.UnderlineAction().actionPerformed(new ActionEvent(textPane, e.getID(), ""));
			notifyAttributesChanged();
		}});
		underline.setRequestFocusEnabled(false);
		underline.setFocusable(false);
		this.colorPanel = new JPanel();
		colorPanel.setBackground(Color.white);
		colorPanel.setPreferredSize(new Dimension(15, 15));
		colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		this.colorChooser = new JColorChooser();
		this.colorSelect = new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			colorPanel.setBackground(colorChooser.getColor());
			if (!sendStyleEvents || textPane == null) return;
			System.out.println(colorChooser.getColor().toString());
			new StyledEditorKit.ForegroundAction("", colorChooser.getColor()).actionPerformed(new ActionEvent(textPane, e.getID(), ""));
			notifyAttributesChanged();
		}};
		final JDialog colorDialog = JColorChooser.createDialog(getTopLevelAncestor(), 
			XMLResourceBundle.getBundledString("bgcolorPropertyName"), true, colorChooser, 
			new ActionListener() {public void actionPerformed(ActionEvent e)
			{
				colorSelect.actionPerformed(e);
				textPane.requestFocus();
			}}, 
			new ActionListener() {public void actionPerformed(ActionEvent e)
			{
				if (textPane != null)
					textPane.requestFocus();
			}});
		colorPanel.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON1)
					return;
				colorChooser.setColor(colorPanel.getBackground());
				colorDialog.setVisible(true);
			}
		});
		decorationPanel.add(bold);
		decorationPanel.add(italic);
		decorationPanel.add(underline);
		decorationPanel.add(colorPanel);
		
		JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fontPanel.setBorder(BorderFactory.createTitledBorder("Font"));
		this.fontBox = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		fontBox.setRequestFocusEnabled(false);
		fontBox.setFocusable(false);
		fontBox.setSelectedItem("Times New Roman");
		fontBox.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e)
		{
			if (e.getStateChange() != ItemEvent.SELECTED) return;
			if (!sendStyleEvents || textPane == null) return;
			new StyledEditorKit.FontFamilyAction("", "").actionPerformed(new ActionEvent(textPane, e.getID(), (String)e.getItem()));
			notifyAttributesChanged();
		}});
		fontPanel.add(fontBox);
		this.sizeBox = new JComboBox(new TextSize [] 
		{
			new TextSize("<html><font size=1>1</font></html>", 8),
			new TextSize("<html><font size=2>2</font></html>", 10),
			new TextSize("<html><font size=3>3</font></html>", 12),
			new TextSize("<html><font size=4>4</font></html>", 14),
			new TextSize("<html><font size=5>5</font></html>", 18),
			new TextSize("<html><font size=6>6</font></html>", 24),
			new TextSize("<html><font size=7>7</font></html>", 32)
		});
		sizeBox.setRequestFocusEnabled(false);
		sizeBox.setFocusable(false);
		sizeBox.setSelectedIndex(4);
		sizeBox.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e)
		{
			if (e.getStateChange() != ItemEvent.SELECTED) return;
			if (!sendStyleEvents || textPane == null) return;
			new StyledEditorKit.FontSizeAction("", ((TextSize)e.getItem()).value).actionPerformed(new ActionEvent(textPane, e.getID(), ""+((TextSize)e.getItem()).value));
			notifyAttributesChanged();
		}});
		fontPanel.add(sizeBox);
		
		JPanel hAlignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		hAlignPanel.setBorder(BorderFactory.createTitledBorder("Alignment"));
		JButton hAlignLeft = new JButton(ImageUtils.getIcon("align-left-12x12.png"));
		hAlignLeft.setRequestFocusEnabled(false);
		hAlignLeft.setFocusable(false);
		hAlignLeft.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			new StyledEditorKit.AlignmentAction("", StyleConstants.ALIGN_LEFT).actionPerformed(
				new ActionEvent(textPane, e.getID(), ""+StyleConstants.ALIGN_LEFT));
			notifyAttributesChanged();
		}});
		JButton hAlignCenter = new JButton(ImageUtils.getIcon("align-center-12x12.png"));
		hAlignCenter.setRequestFocusEnabled(false);
		hAlignCenter.setFocusable(false);
		hAlignCenter.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			new StyledEditorKit.AlignmentAction("", StyleConstants.ALIGN_CENTER).actionPerformed(
				new ActionEvent(textPane, e.getID(), ""+StyleConstants.ALIGN_CENTER));
			notifyAttributesChanged();
		}});
		JButton hAlignRight = new JButton(ImageUtils.getIcon("align-right-12x12.png"));
		hAlignRight.setRequestFocusEnabled(false);
		hAlignRight.setFocusable(false);
		hAlignRight.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			new StyledEditorKit.AlignmentAction("", StyleConstants.ALIGN_RIGHT).actionPerformed(
				new ActionEvent(textPane, e.getID(), ""+StyleConstants.ALIGN_RIGHT));
			notifyAttributesChanged();
		}});
		JButton hAlignJustify = new JButton(ImageUtils.getIcon("align-justify-12x12.png"));
		hAlignJustify.setRequestFocusEnabled(false);
		hAlignJustify.setFocusable(false);
		hAlignJustify.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			new StyledEditorKit.AlignmentAction("", StyleConstants.ALIGN_JUSTIFIED).actionPerformed(
				new ActionEvent(textPane, e.getID(), ""+StyleConstants.ALIGN_JUSTIFIED));
			notifyAttributesChanged();
		}});
		hAlignPanel.add(hAlignLeft);
		hAlignPanel.add(hAlignCenter);
		hAlignPanel.add(hAlignRight);
		hAlignPanel.add(hAlignJustify);
		
		//add(stylePanel);
		add(decorationPanel);
		add(fontPanel);
		add(hAlignPanel);
	}
	
	public void setStyle(Style style, boolean sendEvents)
	{
		setStyle(style.color, style.bold, style.italic, style.underline, style.size, style.font, sendEvents);
	}
	public void setStyle(Color color, Boolean bold, Boolean italic, Boolean underline, Integer size, String font, boolean sendEvents)
	{
		sendStyleEvents = sendEvents;
		
		if (color != null)
		{
			this.colorChooser.setColor(color);
			colorSelect.actionPerformed(new ActionEvent(colorPanel, 1001, ""));
		}
		if (bold != null)
			setToState(this.bold, bold);
		if (italic != null)
			setToState(this.italic, italic);
		if (underline != null)
			setToState(this.underline, underline);
		if (size != null)
		{
			this.sizeBox.setSelectedIndex(size-1); 
			if (sendStyleEvents)
				this.sizeBox.getItemListeners()[0].itemStateChanged(new ItemEvent(sizeBox, -1, this.sizeBox.getSelectedItem(), ItemEvent.SELECTED));
		}
		if (font != null)
		{
			this.fontBox.setSelectedItem(font);
			if (sendStyleEvents)
				this.fontBox.getItemListeners()[0].itemStateChanged(new ItemEvent(fontBox, -1, this.fontBox.getSelectedItem(), ItemEvent.SELECTED));
		}
		
		sendStyleEvents = true;
	}
	void setToState(JToggleButton button, boolean state)
	{
		if (!sendStyleEvents)
			button.setSelected(state);
		else
		{
			button.doClick();
			if (button.isSelected() != state)
				button.doClick();
		}
	}
	public void setStyle(AttributeSet attributes, boolean sendEvents)
	{
		String scolor = null, sitalic = null, sunderline = null, sbold = null, ssize = null, sfont = null;
		while (attributes != null)
		{
			if (scolor == null && attributes.isDefined(HTML.Tag.FONT) && ((SimpleAttributeSet)attributes.getAttribute(HTML.Tag.FONT)).isDefined(HTML.Attribute.COLOR))
				scolor = ((SimpleAttributeSet)attributes.getAttribute(HTML.Tag.FONT)).getAttribute(HTML.Attribute.COLOR).toString();
			if (scolor == null && attributes.isDefined(CSS.Attribute.COLOR))
				scolor = attributes.getAttribute(CSS.Attribute.COLOR).toString();
			if (sitalic == null && attributes.isDefined(CSS.Attribute.FONT_STYLE) && attributes.getAttribute(CSS.Attribute.FONT_STYLE).toString().contains("italic"))
				sitalic = "yes";
			if (sunderline == null && attributes.isDefined(CSS.Attribute.TEXT_DECORATION) && attributes.getAttribute(CSS.Attribute.TEXT_DECORATION).toString().contains("underline"))
				sunderline = "yes";
			if (sbold == null && attributes.isDefined(CSS.Attribute.FONT_WEIGHT) && attributes.getAttribute(CSS.Attribute.FONT_WEIGHT).toString().contains("bold"))
				sbold = "yes";
			if (ssize == null && attributes.isDefined(HTML.Tag.FONT) && ((SimpleAttributeSet)attributes.getAttribute(HTML.Tag.FONT)).isDefined(HTML.Attribute.SIZE))
				ssize = ((SimpleAttributeSet)attributes.getAttribute(HTML.Tag.FONT)).getAttribute(HTML.Attribute.SIZE).toString();
			if (ssize == null && attributes.isDefined(CSS.Attribute.FONT_SIZE))
				ssize = attributes.getAttribute(CSS.Attribute.FONT_SIZE).toString();
			if (sfont == null && attributes.isDefined(CSS.Attribute.FONT_FAMILY))
				sfont = attributes.getAttribute(CSS.Attribute.FONT_FAMILY).toString();
			attributes = attributes.getResolveParent();
		}
		
		Color color = null;
		if (scolor != null)
		{
			color = Color.white;
			if (scolor.startsWith("#"))
				color = Color.decode(scolor);
			else if (scolor.equals("white")) color = Color.white;
			else if (scolor.equals("black")) color = Color.black;
			else if (scolor.equals("red")) color = Color.red;
			else if (scolor.equals("green")) color = Color.green;
			else if (scolor.equals("blue")) color = Color.blue;
		}
		setStyle(color, sbold != null, sitalic != null, sunderline != null, ssize != null ? Integer.parseInt(ssize) : null, sfont, sendEvents);
	}
	public Style getStyle(String name)
	{
		return new Style(name, colorPanel.getBackground(), (String)fontBox.getSelectedItem(), ((TextSize)sizeBox.getSelectedItem()).value, 
			bold.isSelected(), italic.isSelected(), underline.isSelected(), false);
	}
	public AttributeSet getAttributes()
	{
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setFontFamily(set, fontBox.getSelectedItem().toString());
		StyleConstants.setForeground(set, colorChooser.getColor());
		StyleConstants.setFontSize(set, ((TextSize)sizeBox.getSelectedItem()).value);
		StyleConstants.setBold(set, bold.isSelected());
		StyleConstants.setItalic(set, italic.isSelected());
		StyleConstants.setUnderline(set, underline.isSelected());
		return set;
	}
	public void resendStyle()
	{
		new StyledEditorKit.ForegroundAction("", colorChooser.getColor()).actionPerformed(new ActionEvent(textPane, 1001, ""));
		int size = ((TextSize)sizeBox.getSelectedItem()).value;
		new StyledEditorKit.FontSizeAction("", size).actionPerformed(new ActionEvent(textPane, 1001, ""+size));
		new StyledEditorKit.FontFamilyAction("", "").actionPerformed(new ActionEvent(textPane, 1001, fontBox.getSelectedItem().toString()));
	}
	
	List<Listener> listeners = new LinkedList<TextToolbar.Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	void notifyAttributesChanged()
	{
		for (Listener listener : listeners)
			listener.attributesChanged(textPane);
	}
}
