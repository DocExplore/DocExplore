/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.gui.text.TextToolbar;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class StyleDialog extends JDialog
{
	Style [] styles;
	JList styleList;
	JLabel preview;
	
	JTextField name;
	JToggleButton bold, italic, underline, centered;
	JPanel colorPanel;
	JColorChooser colorChooser;
	JComboBox fontBox;
	JComboBox sizeBox;
	
	JPopupMenu menu;
	
	boolean sendEvents = true;
	public StyleDialog(Style [] styles)
	{
		super((Frame)null, XMLResourceBundle.getBundledString("styleEdit"), true);
		setLayout(new BorderLayout());
		
		preview = new JLabel("", SwingConstants.CENTER);
		preview.setPreferredSize(new Dimension(300, 100));
		preview.setOpaque(true);
		preview.setBackground(Color.black);
		JPanel previewPanel = new JPanel(new BorderLayout());
		previewPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("stylePreview")));
		previewPanel.add(preview);
		
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		namePanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("styleName")));
		name = new JTextField(40);
		name.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override public void removeUpdate(DocumentEvent e) {synchronizeStyle();}
			@Override public void insertUpdate(DocumentEvent e) {synchronizeStyle();}
			@Override public void changedUpdate(DocumentEvent e) {synchronizeStyle();}
		});
		namePanel.add(name);
		
		bold = new JToggleButton(new AbstractAction("<html><b>B</b></html>") {@Override public void actionPerformed(ActionEvent e) {synchronizeStyle();}});
		italic = new JToggleButton(new AbstractAction("<html><i>I</i></html>") {@Override public void actionPerformed(ActionEvent e) {synchronizeStyle();}});
		underline = new JToggleButton(new AbstractAction("<html><u>U</u></html>") {@Override public void actionPerformed(ActionEvent e) {synchronizeStyle();}});
		centered = new JToggleButton(new AbstractAction("", ImageUtils.getIcon("align-center-12x12.png")) {@Override public void actionPerformed(ActionEvent e) {synchronizeStyle();}});
		this.colorPanel = new JPanel();
		colorPanel.setBackground(Color.black);
		colorPanel.setPreferredSize(new Dimension(15, 15));
		colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		this.colorChooser = new JColorChooser();
		final JDialog colorDialog = JColorChooser.createDialog(StyleDialog.this, 
			XMLResourceBundle.getBundledString("bgcolorPropertyName"), true, colorChooser, 
			new ActionListener() {public void actionPerformed(ActionEvent e) {colorPanel.setBackground(colorChooser.getColor()); synchronizeStyle();}}, 
			new ActionListener() {public void actionPerformed(ActionEvent e) {}});
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
		
		JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fontPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("styleFont")));
		this.fontBox = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		fontBox.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e) {if (e.getStateChange() != ItemEvent.SELECTED) return; synchronizeStyle();}});
		fontPanel.add(fontBox);
		this.sizeBox = new JComboBox(new Object []  {"8", "12", "16", "24", "32", "48", "72", "144"});
		sizeBox.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e) {if (e.getStateChange() != ItemEvent.SELECTED) return; synchronizeStyle();}});
		sizeBox.setEditable(true);
		//sizeBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {public void keyTyped(KeyEvent e) {System.out.println("!");synchronizeStyle();}});
		fontPanel.add(sizeBox);
		fontPanel.add(bold);
		fontPanel.add(italic);
		fontPanel.add(underline);
		fontPanel.add(centered);
		fontPanel.add(colorPanel);
		
		JPanel settingsPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, false, false));
		settingsPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("styleSettings")));
		settingsPanel.add(namePanel);
		settingsPanel.add(fontPanel);
		
		this.styles = styles;
		styleList = new JList(new DefaultListModel());
		styleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (Style style : styles)
			((DefaultListModel)styleList.getModel()).addElement(style);
		styleList.setCellRenderer(new ListCellRenderer()
		{
			@Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				JPanel panel = new JPanel(new BorderLayout());
				panel.setBorder(BorderFactory.createLineBorder(isSelected ? TextToolbar.styleHighLightedBackground : TextToolbar.styleBackground, 3));
				JLabel label = new JLabel(value.toString());
				label.setOpaque(true);
				label.setBackground(isSelected ? TextToolbar.styleHighLightedBackground : TextToolbar.styleBackground);
				panel.add(label, BorderLayout.CENTER);
				return panel;
			}
		});
		styleList.addListSelectionListener(new ListSelectionListener()
		{
			@Override public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;
				Style style = (Style)styleList.getSelectedValue();
				if (style == null)
					return;
				preview.setText(style.apply(XMLResourceBundle.getBundledString("styleTest")));
				
				sendEvents = false;
				name.setText(style.name);
				bold.setSelected(style.bold);
				italic.setSelected(style.italic);
				underline.setSelected(style.underline);
				centered.setSelected(style.centered);
				colorPanel.setBackground(style.color);
				fontBox.setSelectedItem(style.font);
				sizeBox.setSelectedItem(""+style.size);
				sendEvents = true;
			}
		});
		
//		JPanel styleButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		styleButtonPanel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("add-24x24.png")) {@Override public void actionPerformed(ActionEvent e)
//		{
//			StyleDialog.this.styles = Arrays.copyOf(StyleDialog.this.styles, StyleDialog.this.styles.length+1);
//			StyleDialog.this.styles[StyleDialog.this.styles.length-1] = new Style("New style", Color.white, "Times New Roman", 5, false, false, false);
//			((DefaultListModel)styleList.getModel()).addElement(StyleDialog.this.styles[StyleDialog.this.styles.length-1]);
//			styleList.setSelectedIndex(StyleDialog.this.styles.length-1);
//		}}));
//		styleButtonPanel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("remove-24x24.png")) {@Override public void actionPerformed(ActionEvent e)
//		{
//			int index = styleList.getSelectedIndex();
//			if (index < 0)
//				return;
//			Style [] newStyles = Arrays.copyOf(StyleDialog.this.styles, StyleDialog.this.styles.length-1);
//			for (int i=0;i<index;i++)
//				newStyles[i] = StyleDialog.this.styles[i];
//			for (int i=index+1;i<StyleDialog.this.styles.length;i++)
//				newStyles[i-1] = StyleDialog.this.styles[i];
//			StyleDialog.this.styles = newStyles;
//			((DefaultListModel)styleList.getModel()).remove(index);
//			styleList.setSelectedIndex(index >= newStyles.length ? newStyles.length-1 : index);
//		}}));
		
		JPanel stylePanel = new JPanel(new BorderLayout());
		stylePanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("styleStyles")));
		JScrollPane scrollPane = new JScrollPane(styleList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(200, 300));
		stylePanel.add(scrollPane, BorderLayout.NORTH);
//		stylePanel.add(styleButtonPanel, BorderLayout.SOUTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("styleClose")) {public void actionPerformed(ActionEvent e)
		{
			StyleDialog.this.setVisible(false);
			notifyListeners();
		}}));
		addWindowListener(new WindowAdapter() {public void windowClosed(WindowEvent e) {notifyListeners();}});
		//buttonPanel.add(new JButton(new AbstractAction("Cancel") {@Override public void actionPerformed(ActionEvent e) {StyleDialog.this.styles = null; StyleDialog.this.setVisible(false);}}));
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(stylePanel, BorderLayout.WEST);
		mainPanel.add(settingsPanel, BorderLayout.CENTER);
		mainPanel.add(previewPanel, BorderLayout.SOUTH);
		
		add(mainPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		SwingUtilities.invokeLater(new Runnable() {public void run() {styleList.setSelectedIndex(0);}});
		
		pack();
	}
	
	public static interface Listener
	{
		public void stylesChanged(StyleDialog dialog);
	}
	List<Listener> listeners = new LinkedList<Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	void notifyListeners() {for (Listener listener : listeners) listener.stylesChanged(this);}
	
	public void setStyles(Style [] styles)
	{
		if (styles.length != this.styles.length)
		{
			Style [] newStyles = Arrays.copyOf(styles, this.styles.length);
			for (int i=styles.length;i<newStyles.length;i++)
				newStyles[i] = this.styles[i];
			styles = newStyles;
		}
		this.styles = styles;
		
		styleList.setModel(new DefaultListModel());
		for (Style style : styles)
			((DefaultListModel)styleList.getModel()).addElement(style);
		styleList.revalidate();
		
		notifyListeners();
	}
	
	public int getNumStyles() {return styles.length;}
	public int indexOf(Style style)
	{
		for (int i=0;i<styles.length;i++)
			if (styles[i] == style)
				return i;
		return 0;
	}
	public Style getStyle(int i)
	{
		if (i >= 0 && i < styles.length)
			return styles[i];
		return styles[0];
	}
	
	void synchronizeStyle()
	{
		if (!sendEvents)
			return;
		Style style = (Style)styleList.getSelectedValue();
		if (style == null)
			return;
		style.name = name.getText();
		style.color = colorPanel.getBackground();
		style.font = (String)fontBox.getSelectedItem();
		try {style.size = Integer.parseInt(sizeBox.getSelectedItem().toString());}
		catch (Exception e) {e.printStackTrace(); style.size = 16;}
		style.bold = bold.isSelected();
		style.italic = italic.isSelected();
		style.underline = underline.isSelected();
		style.centered = centered.isSelected();
		styleList.setFixedCellHeight(0); styleList.setFixedCellWidth(0);
		styleList.setFixedCellHeight(-1); styleList.setFixedCellWidth(-1);
		pack();
		preview.setText(style.apply(XMLResourceBundle.getBundledString("styleTest")));
	}
	
	public static Style [] editStyles(Component owner, Style [] styles)
	{
		Style [] copy = new Style [styles.length];
		for (int i=0;i<styles.length;i++)
			copy[i] = new Style (styles[i]);
		StyleDialog dialog = new StyleDialog(copy);
		GuiUtils.centerOnComponent(dialog, owner);
		dialog.setVisible(true);
		return dialog.styles;
	}
}
