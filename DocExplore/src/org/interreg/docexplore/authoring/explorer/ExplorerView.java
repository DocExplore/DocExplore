/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.gui.text.SwingRenderer;

@SuppressWarnings("serial")
public abstract class ExplorerView extends JPanel
{
	public final Explorer explorer;
	public List<ViewItem> items;
	public Set<ViewItem> selected;
	boolean iconMode = true;
	
	public ExplorerView(final Explorer explorer)
	{
		super(new WrapLayout());
		((WrapLayout)getLayout()).setHgap(10);
		((WrapLayout)getLayout()).setAlignOnBaseline(true);
		setBackground(Color.white);
		
		this.explorer = explorer;
		this.items = new Vector<ViewItem>();
		this.selected = new HashSet<ViewItem>();
		
		ViewMouseListener vml = new ViewMouseListener(this);
		addMouseListener(vml);
		addMouseMotionListener(vml);
	}
	
	public static interface SelectionListener
	{
		public void selectionChanged(ExplorerView view);
	}
	List<SelectionListener> selectionListeners = new LinkedList<ExplorerView.SelectionListener>();
	public void addSelectionListener(SelectionListener listener) {selectionListeners.add(listener);}
	public void removeSelectionListener(SelectionListener listener) {selectionListeners.remove(listener);}
	public void notifySelectionChanged() {for (SelectionListener listener : selectionListeners) listener.selectionChanged(this);}
	
	protected void opened(ViewItem item)
	{
		try {explorer.explore(getPath(item.data.object));}
		catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	
	public void shown() {}
	public void hidden() {}
	
	public static Color selection = new Color(.3f, .4f, .7f);
	protected void updateSelection()
	{
		for (ViewItem item : items)
			if (selected.contains(item))
			{
				item.setOpaque(true);
				item.setBackground(selection);
				item.nameLabel.setForeground(Color.white);
			}
			else
			{
				item.setOpaque(false);
				item.nameLabel.setForeground(Color.black);
			}
		notifySelectionChanged();
		repaint();
	}
	
	public abstract boolean canHandle(String path) throws Exception;
	protected abstract List<ViewItem> buildItemList(String path) throws Exception;
	protected abstract Icon getIcon(Object object);
	protected abstract String getPath(Object object);
	
	public static final SwingRenderer helpRenderer = new SwingRenderer();
	public static final Color background = new Color(255, 255, 255, 127);
	public static final Color selectionColor = new Color(192, 192, 255, 127);
	public static final Color selectionOutlineColor = new Color(96, 96, 127, 127);
	public String msg = "";
	boolean selectionRectVisible = false;
	int [][] selectionRect = {{0, 0}, {0, 0}};
	public void paintChildren(Graphics g)
	{
		super.paintChildren(g);
		
		if (selectionRectVisible)
		{
			int x = selectionRect[1][0] < 0 ? selectionRect[0][0]+selectionRect[1][0] : selectionRect[0][0];
			int y = selectionRect[1][1] < 0 ? selectionRect[0][1]+selectionRect[1][1] : selectionRect[0][1];
			int w = Math.abs(selectionRect[1][0]), h = Math.abs(selectionRect[1][1]);
			g.setColor(selectionColor);
			g.fillRect(x, y, w, h);
			g.setColor(selectionOutlineColor);
			g.drawRect(x, y, w, h);
		}
		
		if (explorer.tool.displayHelp && msg.length() > 0)
		{
			Rectangle visible = explorer.scrollPane.getViewport().getViewRect();
			BufferedImage help = helpRenderer.getImage(
				"<html><div style=\"font-family: Arial; font-size: 24; font-weight: bold; color: rgb(128, 128, 128)\">"+msg+"</div></html>", 
				visible.width, background);
			g.drawImage(help, visible.x, visible.y+visible.height-help.getHeight(), null);
		}
	}
	
	public void setPath(String path) throws Exception
	{
		this.items = buildItemList(path);
		selected.clear();
		updateLayout();
	}
	
	public void updateLayout()
	{
		removeAll();
		iconMode = explorer.iconMode;
		if (iconMode)
		{
			setLayout(new WrapLayout());
			((WrapLayout)getLayout()).setHgap(10);
			((WrapLayout)getLayout()).setAlignOnBaseline(true);
			for (ViewItem item : items)
				item.setForIcon(this);
		}
		else
		{
			setLayout(new LooseGridLayout(0, 1, 0, 1, true, false, SwingConstants.LEFT, SwingConstants.CENTER, true, false));
			for (ViewItem item : items)
				item.setForList();
		}
		for (ViewItem item : items)
			add(item);
		updateSelection();
		explorer.scrollPane.validate();
	}
}
