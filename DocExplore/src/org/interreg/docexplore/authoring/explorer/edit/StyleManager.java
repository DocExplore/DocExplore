package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.interreg.docexplore.authoring.AuthoringToolFrame;
import org.interreg.docexplore.manuscript.MetaData;

public class StyleManager implements StyleDialog.Listener
{
	public AuthoringToolFrame tool;
	public StyleDialog styleDialog;
	JPopupMenu menu = new JPopupMenu();
	JCheckBoxMenuItem [] items;
	MetaData md;
	
	public StyleManager(AuthoringToolFrame tool)
	{
		this.tool = tool;
		this.styleDialog = new StyleDialog(defaultStyles());
		
		this.menu = new JPopupMenu();
		refreshMenu();
		styleDialog.addListener(this);
	}
	
	static Style [] defaultStyles()
	{
		return new Style []
		{
			new Style("Body", Color.white, "Times New Roman", 16, false, false, false, false),
			new Style("Subtitle", Color.white, "Times New Roman", 32, false, true, false, false),
			new Style("Title", Color.white, "Times New Roman", 48, true, false, false, false),
			new Style("Title 2", Color.white, "Arial", 48, true, false, false, false),
			new Style("Caption", Color.white, "Times New Roman", 14, false, true, true, false)
		};
	}
	
	public void setMD(MetaData md) throws Exception
	{
		this.md = md;
		String serialized = md.getString();
		if (serialized.equals(""))
		{
			styleDialog.setStyles(defaultStyles());
			writeMD();
		}
		else unserialize(serialized);
	}
	private void writeMD() throws Exception
	{
		md.setString(serialize());
	}
	
	public String serialize()
	{
		String res = "";
		for (int i=0;i<styleDialog.getNumStyles();i++)
			res += (i > 0 ? "\n" : "")+styleDialog.getStyle(i).serialize();
		return res;
	}
	public void unserialize(String val)
	{
		String [] vals = val.split("\n");
		Style [] styles = new Style [vals.length];
		for (int i=0;i<styles.length;i++)
			styles[i] = Style.unserialize(vals[i]);
		styleDialog.setStyles(styles);
	}
	
	public Style getDefaultStyle() {return styleDialog.getStyle(0);}
	
	public static Color styleBackground = new Color(0, 0, 0, 192);
	public static Color styleHighLightedBackground = new Color(16, 32, 64, 192);
	
	TextElement current = null;
	public void showStyleMenu(Component comp, TextElement element)
	{
		current = element;
		for (int i=0;i<styleDialog.getNumStyles();i++)
			if (i == element.style)
				{items[i].setSelected(true); break;}
		menu.show(comp, comp.getWidth(), comp.getHeight());
	}

	private void refreshMenu()
	{
		menu.removeAll();
		ButtonGroup group = new ButtonGroup();
		this.items = new JCheckBoxMenuItem [styleDialog.getNumStyles()];
		for (int i=0;i<styleDialog.getNumStyles();i++)
		{
			final int index = i;
			items[i] = new JCheckBoxMenuItem(styleDialog.getStyle(i).toString());
			items[i].setOpaque(true);
			items[i].setBackground(styleBackground);
			items[i].addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
			{
				if (current != null)
					current.setStyle(styleDialog.getStyle(index));
			}});
			group.add(items[i]);
			menu.add(items[i]);
		}
//		JMenuItem edit = new JMenuItem(XMLResourceBundle.getBundledString("styleEdit")+"...", ImageUtils.getIcon("pencil-24x24.png"));
//		edit.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {styleDialog.setVisible(true);}});
//		menu.add(edit);
	}
	
	public void stylesChanged(StyleDialog dialog)
	{
		refreshMenu();
		try {writeMD();}
		catch (Exception e) {e.printStackTrace();}
		if (tool.mdEditor != null)
			try {tool.mdEditor.reload();}
			catch (Exception e) {e.printStackTrace();}
	}
}
