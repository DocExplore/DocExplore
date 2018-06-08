package org.interreg.docexplore.manuscript.app.editors;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public abstract class DragDropPanel extends JPanel
{
	private static Collection<Object> ddData = null;
	private static Transferable currentSystemDrag = null;
	
	protected boolean dragsEnabled, dropsEnabled;
	
	public DragDropPanel() {this(null, true, true);}
	public DragDropPanel(boolean dragsEnabled, boolean dropsEnabled) {this(null, dragsEnabled, dropsEnabled);}
	public DragDropPanel(LayoutManager layout) {this(layout, true, true);}
	public DragDropPanel(LayoutManager layout, boolean dragsEnabled, boolean dropsEnabled)
	{
		super(layout);
		
		this.dragsEnabled = dragsEnabled;
		this.dropsEnabled = dropsEnabled;
		
		MouseAdapter adapter = new MouseAdapter()
		{
			Point point = new Point();
			boolean dragging = false;
			DragDropPanel lastPanel = null;
			@Override public void mouseDragged(MouseEvent e)
			{
				if (!DragDropPanel.this.dragsEnabled)
					return;
				if (!dragging)
				{
					dragging = true;
					ddData = setDraggedData(e.getX(), e.getY());
				}
				if (ddData == null)
					return;
				
				point.setLocation(e.getPoint());
				DragDropPanel panel = findTarget(point, DragDropPanel.this);
				if (panel != lastPanel && lastPanel != null)
				{
					lastPanel.setCursor(Cursor.getDefaultCursor());
					lastPanel.onDragExited(ddData, DragDropPanel.this);
				}
				if (panel != lastPanel && panel != null)
					panel.setCursor(dragCursor);
				lastPanel = panel;
				if (panel == null)
					return;
				panel.onIncomingDrag(ddData, DragDropPanel.this, point.x, point.y);
			}
			@Override public void mouseReleased(MouseEvent e)
			{
				if (!DragDropPanel.this.dragsEnabled)
					return;
				if (!dragging)
					return;
				dragging = false;
				if (ddData == null)
					return;
				Collection<Object> data = ddData;
				ddData = null;
				
				point.setLocation(e.getPoint());
				DragDropPanel panel = findTarget(point, DragDropPanel.this);
				if (panel != lastPanel && lastPanel != null)
				{
					lastPanel.setCursor(Cursor.getDefaultCursor());
					lastPanel.onDragExited(ddData, DragDropPanel.this);
				}
				lastPanel = null;
				if (panel == null)
					return;
				panel.setCursor(Cursor.getDefaultCursor());
				panel.onIncomingDrop(data, DragDropPanel.this, point.x, point.y);
			}
		};
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		
		new java.awt.dnd.DropTarget(this, new DropTargetListener()
		{
			@SuppressWarnings("unchecked")
			private Collection<Object> getData(Transferable t)
			{
				if (t == currentSystemDrag)
					return ddData;
				currentSystemDrag = t;
				ddData = null;
				if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
					try {ddData = (Collection<Object>)t.getTransferData(DataFlavor.javaFileListFlavor);}
					catch (Exception e) {e.printStackTrace();}
				return ddData;
			}
			
			public void drop(DropTargetDropEvent e)
			{
				if (!DragDropPanel.this.dropsEnabled)
					return;
				e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				Collection<Object> data = getData(e.getTransferable());
				ddData = null;
				if (data == null)
					return;
				onIncomingDrop(data, null, e.getLocation().x, e.getLocation().y);
			}
			
			public void dragOver(DropTargetDragEvent e)
			{
				if (!DragDropPanel.this.dropsEnabled)
					return;
				Collection<Object> data = getData(e.getTransferable());
				if (data == null)
					return;
				onIncomingDrag(data, null, e.getLocation().x, e.getLocation().y);
			}
			
			public void dragExit(DropTargetEvent arg0)
			{
				if (!DragDropPanel.this.dropsEnabled)
					return;
				onDragExited(ddData, null);
			}
			public void dropActionChanged(DropTargetDragEvent e) {}
			public void dragEnter(DropTargetDragEvent arg0) {}
		});
	}
	
	public abstract Collection<Object> setDraggedData(int x, int y);
	public abstract void onIncomingDrag(Collection<Object> data, DragDropPanel source, int x, int y);
	public abstract void onIncomingDrop(Collection<Object> data, DragDropPanel source, int x, int y);
	public abstract void onDragExited(Collection<Object> data, DragDropPanel source);
	
	private static DragDropPanel findTarget(Point point, Component view)
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
				if (!(comp instanceof DragDropPanel) || !((DragDropPanel)comp).dropsEnabled)
					comp = comp.getParent();
				else break;
			}
			if (comp != null)
			{
				coord = SwingUtilities.convertPoint(win, coord, comp);
				point.setLocation(coord);
				return (DragDropPanel)comp;
			}
			break;
		}
		return null;
	}
	
	private static Cursor dragCursor;
	static
	{
		dragCursor = Toolkit.getDefaultToolkit().createCustomCursor(ImageUtils.getImageFromIcon(ImageUtils.getIcon("drag-cursor-32x32.png")), new Point(0, 0), "drag-cursor");
	}
}
