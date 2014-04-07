package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.reader.book.roi.Java2DRenderer;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class TextElement extends InfoElement
{
	JTextPane textPane;
	boolean sizeEvent = true;
	
	StyleManager styles;
	int style = 0;
	JButton styleMenuButton;
	
	public TextElement(final MetaDataEditor editor, int width, MetaData md) throws Exception
	{
		super(editor, md);
		inner.setLayout(new LooseGridLayout(0, 1, 0, 0, false, false, SwingConstants.CENTER, SwingConstants.CENTER, true, true));
		
		this.styles = editor.pageEditor.view.explorer.tool.styleManager;
		readStyle();
		
		this.textPane = new JTextPane();
		textPane.getInsets().set(0, 0, 0, 0);
		textPane.setBorder(null);
		styles.styleDialog.getStyle(style).apply(textPane);
		textPane.setOpaque(false);
		textPane.setText(md.getString());
		textPane.setBackground(new Color(0, 0, 0, 0));
		textPane.setPreferredSize(new Dimension(width, 1));
		textPane.setSize(width, 1);
		textPane.setCaretColor(Color.white);
		add(textPane);
		updateSize();
		
		textPane.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override public void removeUpdate(DocumentEvent e) {updateSize();}
			@Override public void insertUpdate(DocumentEvent e) {updateSize();}
			@Override public void changedUpdate(DocumentEvent e) {updateSize();}
		});
		textPane.addCaretListener(new CaretListener() {@Override public void caretUpdate(CaretEvent arg0) {updateSize();}});
		textPane.addFocusListener(new FocusAdapter()
		{
			@Override public void focusGained(FocusEvent e) {}
			@Override public void focusLost(FocusEvent e) {try {save();} catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}}
		});
		
		styleMenuButton = new JButton(new AbstractAction("<html><b>"+styles.styleDialog.getStyle(style).name+"</b></html>", ImageUtils.getIcon("down-mono-11x11.png")) {public void actionPerformed(ActionEvent e)
		{
			styles.showStyleMenu((JButton)e.getSource(), TextElement.this);
		}});
		styleMenuButton.setHorizontalTextPosition(SwingConstants.LEFT);
		//JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		JPanel stylePanel = new JPanel(new LooseGridLayout(1, 0, 5, 0, false, false, SwingConstants.CENTER, SwingConstants.CENTER, true, false));
		JLabel styleLabel = new JLabel("Style");
		styleLabel.setForeground(Color.white);
		stylePanel.add(styleLabel);
		stylePanel.add(styleMenuButton);
		stylePanel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("pencil-mono-16x16.png")) {public void actionPerformed(ActionEvent e)
		{
			styles.styleDialog.setVisible(true);
		}}) {{setPreferredSize(new Dimension(16, 16)); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setBackground(Color.black);}});
		styleMenuButton.setContentAreaFilled(false);
		styleMenuButton.setBorderPainted(false);
		styleMenuButton.setForeground(Color.white);
		stylePanel.setOpaque(false);
		stylePanel.setBorder(BorderFactory.createLineBorder(Color.white, 1));
		stylePanel.setPreferredSize(new Dimension(96, 24));
		toolbar.add(stylePanel);
	}
	
	public void setStyle(final Style style)
	{
		this.style = styles.styleDialog.indexOf(style);
		style.apply(textPane);
		styleMenuButton.setText("<html><b>"+style.name+"</b></html>");
		try {writeStyle();}
		catch (Exception e) {e.printStackTrace();}
		updateSize();
	}
	
	void save() throws Exception
	{
		md.setString(textPane.getText());
		writeStyle();
	}
	public static MetaData getStyleMD(MetaData md) throws DataLinkException
	{
		MetaDataKey key = md.getLink().getOrCreateKey("style", "");
		List<MetaData> list = md.getMetaDataListForKey(key);
		MetaData res = null;
		if (list.isEmpty())
		{
			res = new MetaData(md.getLink(), key, "0");
			md.addMetaData(res);
		}
		else res = list.get(0);
		return res;
	}
	public static Style getStyle(MetaData md, StyleManager styles) throws DataLinkException
	{
		MetaData styleMD = getStyleMD(md);
		int style = 0;
		try {style = Integer.parseInt(styleMD.getString());}
		catch (Exception e) {}
		return styles.styleDialog.getStyle(style);
	}
	void readStyle() throws Exception
	{
		MetaData styleMD = getStyleMD(md);
		try {style = Integer.parseInt(styleMD.getString());}
		catch (Exception e) {style = 0;}
	}
	void writeStyle() throws Exception
	{
		MetaData styleMD = getStyleMD(md);
		styleMD.setString(""+style);
	}
	
	public String getContent()
	{
		String text = textPane.getText();
		int start = text.indexOf("<body>")+6;
		int end = text.lastIndexOf("</body>");
		return text.substring(start, end).trim();
	}
	public int getContentLength()
	{
		String text = textPane.getText();
		int start = text.indexOf("<body>")+11;
		int end = text.lastIndexOf("</body>")-3;
		return end-start;
	}
	int preferredHeight()
	{
		Rectangle r = null;
		try {r = textPane.modelToView(textPane.getDocument().getEndPosition().getOffset()-1);}
		catch (Exception e) {return textPane.getHeight();}
		return r.y+r.height;
	}
	void updateSize()
	{
		if (!sizeEvent)
			return;
		textPane.setPreferredSize(new Dimension(textPane.getWidth(), preferredHeight())); 
		if (getParent() != null)
			getParent().validate();
	}
	
	static Java2DRenderer jr = new Java2DRenderer();
	public BufferedImage getPreview(int width, Color back)
	{
		try {return jr.getImage(getStyle(md, styles).apply(md.getString()), width, back);}
		catch (Exception e) {e.printStackTrace();}
		return null;
	}
}
