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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public abstract class DataLinkView extends ExplorerView implements ViewMouseListener.DropTarget
{
	public final DataLinkExplorer explorer;
	public ViewInsertionManager vim;
	
	public DataLinkView(DataLinkExplorer explorer)
	{
		super(explorer);
		this.explorer = explorer;
		this.vim = new ViewInsertionManager(this);
		
		msg = XMLResourceBundle.getBundledString("helpCollectionMsg");
	}

	@Override protected String getPath(Object object) {return ((AnnotatedObject)object).getCanonicalUri();}

	public static enum DropType {None, Anywhere, OnItem, BetweenItems};
	public abstract DropType getDropType(ExplorerView source, List<ViewItem.Data> items);
	public abstract void itemsDropped(ExplorerView source, List<ViewItem.Data> items, Point where) throws Exception;
	
	public Point dragging = null;
	List<ViewItem.Data> draggedItems = null;
	public DropType dropType = null;
	@Override public void dropped(final ExplorerView source, List<ViewItem.Data> droppedItems, final Point where)
	{
		dragging = null;
		draggedItems = null;
		if (dropType != DropType.None)
		{
			final List<ViewItem.Data> items = new Vector<ViewItem.Data>(droppedItems);
			GuiUtils.blockUntilComplete(new Runnable() {public void run()
			{
				try {itemsDropped(source, items, where); repaint();}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e); explorer.explore(explorer.curPath);}
			}}, this);
		}
		else repaint();
	}
	
	@Override public void dragged(ExplorerView source, List<ViewItem.Data> items, Point where)
	{
		if (dragging == null)
			dragging = new Point(where);
		else dragging.setLocation(where);
		draggedItems = items;
		dropType = getDropType(source, items);
//		if (dropType != DropType.None)
//		{
//			try {itemsHovered(source, items, where);}
//			catch (Exception e) {ErrorHandler.defaultHandler.submit(e); explorer.explore(explorer.curPath);}
//		}
		repaint();
	}

	@Override public void exited()
	{
		dragging = null;
		draggedItems = null;
		repaint();
	}
	
	static Stroke stroke = new BasicStroke(3);
	public static Color dragColor = new Color(.2f, .3f, .6f, .75f);
	@Override public void paint(Graphics _g)
	{
		super.paint(_g);
		paintDropTarget(_g);
	}
		
	public void paintDropTarget(Graphics _g)
	{
		Graphics2D g = (Graphics2D)_g;
		if (dragging != null)
		{
			Stroke old = g.getStroke();
			g.setStroke(stroke);
			if (dropType != DropType.None)
			{
				g.setColor(dragColor);
				g.drawRect(dragging.x-5, dragging.y-5, 10, 10);
			}
			else
			{
				g.setColor(Color.red);
				g.drawLine(dragging.x-5, dragging.y-5, dragging.x+5, dragging.y+5);
				g.drawLine(dragging.x+5, dragging.y-5, dragging.x-5, dragging.y+5);
			}
			g.setStroke(old);
			
			String info = draggedItems.size()+" "+(draggedItems.size() == 1 ? "item" : "items");
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(info, g);
			int x0 = dragging.x+10, y0 = dragging.y;
			bounds.setRect(x0+bounds.getX()-2, y0+bounds.getY()-2, bounds.getWidth()+4, bounds.getHeight()+4);
			g.setColor(dragColor);
			g.fill(bounds);
			g.setColor(Color.white);
			g.drawString(info, x0, y0);
		}
	}
	
	@Override public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (dragging != null && (dropType == DropType.BetweenItems || dropType == DropType.OnItem))
			vim.paintInsertionArea(g, dragging.x, dragging.y, dropType == DropType.BetweenItems);
	}
	
	public void refreshSelection(Collection<?> newSelection)
	{
		explorer.explore(explorer.curPath);
		for (ViewItem item : this.items)
			if (newSelection.contains(item.data.object))
				selected.add(item);
		updateSelection();
	}
}
