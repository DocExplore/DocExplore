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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.interreg.docexplore.manuscript.MetaData;

public abstract class StyleManager implements StyleDialog.Listener
{
	public StyleDialog styleDialog;
	JPopupMenu menu = new JPopupMenu();
	JCheckBoxMenuItem [] items;
	MetaData md;
	
	public StyleManager()
	{
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
			new Style("Emphasis 1", Color.white, "Times New Roman", 16, true, false, false, false),
			new Style("Emphasis 2", Color.white, "Arial", 16, true, true, false, false),
			new Style("Title 1", Color.white, "Times New Roman", 48, true, false, false, false),
			new Style("Title 2", Color.white, "Arial", 40, true, false, false, false),
			new Style("Title 3", Color.white, "Times New Roman", 32, false, true, false, false),
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
	protected void writeMD() throws Exception
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
	
	Object current = null;
	public void showStyleMenu(Component comp, TextElement element)
	{
		current = element;
		for (int i=0;i<styleDialog.getNumStyles();i++)
			if (i == element.style)
				{items[i].setSelected(true); break;}
		menu.show(comp, comp.getWidth(), comp.getHeight());
	}
	public void showStyleMenu(Component comp, org.interreg.docexplore.authoring.rois.TextElement element)
	{
		current = element;
		for (int i=0;i<styleDialog.getNumStyles();i++)
			if (i == element.style)
				{items[i].setSelected(true); break;}
		menu.show(comp, comp.getWidth(), comp.getHeight());
	}

	protected void refreshMenu()
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
				{
					if (current instanceof TextElement) ((TextElement)current).setStyle(styleDialog.getStyle(index));
					else ((org.interreg.docexplore.authoring.rois.TextElement)current).setStyle(styleDialog.getStyle(index));
				}
			}});
			group.add(items[i]);
			menu.add(items[i]);
		}
//		JMenuItem edit = new JMenuItem(XMLResourceBundle.getBundledString("styleEdit")+"...", ImageUtils.getIcon("pencil-24x24.png"));
//		edit.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {styleDialog.setVisible(true);}});
//		menu.add(edit);
	}
	
	public abstract void stylesChanged(StyleDialog dialog);
}
