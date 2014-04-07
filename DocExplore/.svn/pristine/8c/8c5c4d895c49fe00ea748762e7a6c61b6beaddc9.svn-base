package org.interreg.docexplore.authoring.explorer;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.SwingUtilities;

public class ViewMouseListener implements MouseListener, MouseMotionListener
{
	public static interface DropTarget
	{
		public void dropped(ExplorerView source, List<ViewItem.Data> items, Point where);
		public void dragged(ExplorerView source, List<ViewItem.Data> items, Point where);
		public void exited();
	}
	
	static Transferable currentTransferable = null;
	static List<ViewItem.Data> currentList = null;
	static DropTarget currentDropTarget = null;
	
	ExplorerView view;
	
	public ViewMouseListener(final ExplorerView view)
	{
		this.view = view;
		makeFileSystemDropTarget(view);
	}
	
	public static void makeFileSystemDropTarget(final Component view)
	{
		new java.awt.dnd.DropTarget(view, new DropTargetListener()
		{
			private List<ViewItem.Data> getDataList(Transferable t)
			{
				if (t == currentTransferable)
					return currentList;
				currentTransferable = t;
				currentList = null;
				if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) try
				{
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
					currentList = new LinkedList<ViewItem.Data>();
					for (File file : files)
						currentList.add(new ViewItem.Data(null, null, file));
				}
				catch (Exception e) {e.printStackTrace();}
				return currentList;
			}
			
			public void drop(DropTargetDropEvent e)
			{
				Transferable t = e.getTransferable();
				e.acceptDrop(e.getDropAction());
				List<ViewItem.Data> dataList = getDataList(t);
				if (dataList == null)
					return;
				Point mouse = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(mouse, view);
				DropTarget target = findTarget(mouse, view);
				if (target != currentDropTarget)
				{
					if (currentDropTarget != null)
						currentDropTarget.exited();
					currentDropTarget = target;
				}
				if (target != null)
				{
					target.dropped(null, dataList, mouse);
					target.exited();
				}
			}
			
			public void dragOver(DropTargetDragEvent e)
			{
				Transferable t = e.getTransferable();
				List<ViewItem.Data> dataList = getDataList(t);
				if (dataList == null)
					return;
				Point mouse = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(mouse, view);
				DropTarget target = findTarget(mouse, view);
				if (target != currentDropTarget)
				{
					if (currentDropTarget != null)
						currentDropTarget.exited();
					currentDropTarget = target;
				}
				if (target != null)
					target.dragged(null, dataList, mouse);
			}
			
			public void dragExit(DropTargetEvent arg0)
			{
				if (currentDropTarget != null)
				{
					currentDropTarget.exited();
					currentDropTarget = null;
				}
			}
			
			public void dropActionChanged(DropTargetDragEvent e) {}
			public void dragEnter(DropTargetDragEvent arg0) {}
		});
	}
	
	List<ViewItem.Data> draggedItems = new Vector<ViewItem.Data>();
	ViewItem lastSelected = null;
	boolean isDragging = false;
	@Override public void mouseReleased(MouseEvent e)
	{
		Component clickedComp = view.getComponentAt(e.getPoint());
		ViewItem clicked = clickedComp != null && clickedComp instanceof ViewItem ? (ViewItem)clickedComp : null;
		
		if (isDragging)
		{
			Point coord = new Point(e.getPoint());
			DropTarget target = findTarget(coord, view);
			if (target != null)
				target.dropped(view, draggedItems, coord);
		}
		else if (view.selectionRectVisible)
		{
			view.selectionRectVisible = false;
			view.repaint();
		}
		else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1 && clicked != null)
		{
			boolean ctrl = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
			boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
			
			if (!ctrl)
				view.selected.clear();
			
			if (shift)
			{
				int from = lastSelected != null ? view.items.indexOf(lastSelected) : 0;
				int to = view.items.indexOf(clicked);
				for (int i=from;i!=to;i+=(from<to ? 1 : -1))
					view.selected.add(view.items.get(i));
				view.selected.add(clicked);
			}
			else if (ctrl && view.selected.contains(clicked))
				view.selected.remove(clicked);
			else view.selected.add(clicked);
			
			if (!shift)
				lastSelected = clicked;
			
			view.updateSelection();
		}
		else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1 && clicked == null)
		{
			view.selected.clear();
			view.updateSelection();
		}
		else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 && clicked != null)
		{
			view.opened(clicked);
		}
		
		isDragging = false;
		draggedItems.clear();
	}
	
	static DropTarget findTarget(Point point, Component view)
	{
		Point coord = new Point();
		Window [] windows = Window.getWindows();
		for (int i=windows.length-1;i>=0;i--)
		{
			Window win = windows[i];
			if (!win.isShowing())
				continue;
			coord.setLocation(point);
			coord = SwingUtilities.convertPoint(view, coord, win);
			if (!win.contains(coord))
				continue;
			Component comp = SwingUtilities.getDeepestComponentAt(win, coord.x, coord.y);
			if (comp == null)
				continue;
			while (comp != null)
			{
				if (!(comp instanceof DropTarget))
					comp = comp.getParent();
				else break;
			}
			if (comp != null)
			{
				coord = SwingUtilities.convertPoint(win, coord, comp);
				point.setLocation(coord);
				return (DropTarget)comp;
			}
			break;
		}
		return null;
	}
	
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	
	ViewItem pressed = null;
	@Override public void mousePressed(MouseEvent e)
	{
		Component clicked = view.getComponentAt(e.getPoint());
		if (clicked instanceof ViewItem)
			pressed = (ViewItem)clicked;
		else
		{
			pressed = null;
			view.selectionRect[0][0] = e.getX();
			view.selectionRect[0][1] = e.getY();
		}
	}

	DropTarget lastTarget = null;
	@Override public void mouseDragged(MouseEvent e)
	{
		boolean ctrl = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
		boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
		
		if (pressed != null)
		{
			if (!view.selected.contains(pressed))
			{
				view.selected.clear();
				view.selected.add(pressed);
				view.updateSelection();
			}
			
			if (!isDragging)
			{
				draggedItems.clear();
				for (ViewItem item : view.items)
					if (view.selected.contains(item))
						draggedItems.add(item.data);
			}
			isDragging = true;
			
			Point coord = new Point(e.getPoint());
			DropTarget target = findTarget(coord, view);
			if (target != null)
				target.dragged(view, draggedItems, coord);
			if (lastTarget != null && lastTarget != target)
				lastTarget.exited();
			lastTarget = target;
		}
		else
		{
			isDragging = false;
			view.selectionRect[1][0] = e.getX()-view.selectionRect[0][0];
			view.selectionRect[1][1] = e.getY()-view.selectionRect[0][1];
			if (!view.selectionRectVisible)
			{
				fromRect.clear();
				if (!ctrl)
					view.selected.clear();
			}
			view.selectionRectVisible = true;
			updateRectSelection(ctrl, shift);
		}
	}
	
	Set<ViewItem> fromRect = new HashSet<ViewItem>();
	void updateRectSelection(boolean ctrl, boolean shift)
	{
		int x = view.selectionRect[1][0] < 0 ? view.selectionRect[0][0]+view.selectionRect[1][0] : view.selectionRect[0][0];
		int y = view.selectionRect[1][1] < 0 ? view.selectionRect[0][1]+view.selectionRect[1][1] : view.selectionRect[0][1];
		int w = Math.abs(view.selectionRect[1][0]), h = Math.abs(view.selectionRect[1][1]);
		
		for (ViewItem item : view.items)
		{
			Rectangle rect = item.getBounds();
			if (rect.x < x+w && rect.x+rect.width > x && rect.y < y+h && rect.y+rect.height > y)
			{
				if (!view.selected.contains(item))
					{view.selected.add(item); fromRect.add(item);}
			}
			else if (fromRect.contains(item))
				{view.selected.remove(item); fromRect.remove(item);}
		}
		view.updateSelection();
	}

	@Override public void mouseMoved(MouseEvent arg0)
	{
		if (lastTarget != null)
		{
			lastTarget.exited();
			lastTarget = null;
		}
	}
}
