package org.interreg.docexplore.manuscript.app.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;

@SuppressWarnings("serial")
public abstract class IconPanelEditor<DataType> extends DragDropPanel
{
	JPanel iconPanel;
	
	Point dragPos;
	IconPanelElement<DataType> dragSource = null;
	Icon dragIcon = null;
	IconPanelElement<DataType> lastSelected = null;
	int lastMovedIndex = -2;
	int iconSize, wGap;
	
	JScrollPane scrollPane;
	
	public IconPanelEditor(int iconSize)
	{
		super(new BorderLayout(), false, true);
		
		this.iconSize = iconSize;
		this.wGap = iconSize/10;
		
		
		this.iconPanel = new JPanel(null) {@Override protected void paintChildren(Graphics g) {super.paintChildren(g); paintOver((Graphics2D)g, getWidth(), getHeight());}};
		iconPanel.setBackground(Color.white);
		iconPanel.addMouseListener(new MouseAdapter()
		{
			@Override public void mousePressed(MouseEvent e)
			{
				boolean ctrl = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
				boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
				if (!ctrl && !shift)
					unselectAll();
			}
		});
		
		this.scrollPane = new JScrollPane(iconPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e) {refresh(true);}});
	}
	
	@SuppressWarnings("unchecked")
	public IconPanelElement<DataType> getIcon(int index) {return (IconPanelElement<DataType>)iconPanel.getComponents()[index];}
	@SuppressWarnings("unchecked")
	public void unselectAll()
	{
		for (Component pageLabel : iconPanel.getComponents())
			((IconPanelElement<DataType>)pageLabel).setSelected(false);
	}
	
	public int iconIndex(IconPanelElement<DataType> icon)
	{
		Component [] comps = iconPanel.getComponents();
		for (int i=0;i<comps.length;i++)
			if (comps[i] == icon)
				return i;
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public List<DataType> getSelectedElements()
	{
		List<DataType> res = new ArrayList<DataType>();
		for (Component component : iconPanel.getComponents())
			if (((IconPanelElement<DataType>)component).selected)
				res.add(((IconPanelElement<DataType>)component).data);
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public IconPanelElement<DataType> closestIcon(int x, int y)
	{
		Component min = null;
		double minDist = 0;
		for (Component component : iconPanel.getComponents())
		{
			Rectangle bounds = component.getBounds();
			double dx = bounds.getCenterX()-x, dy = bounds.getCenterY()-y;
			double dist = dx*dx+dy*dy;
			if (min == null || dist < minDist)
			{
				min = component;
				minDist = dist;
			}
		}
		return (IconPanelElement<DataType>)min;
	}
	
	protected abstract Collection<DataType> getData();
	protected abstract void onIconInit(IconPanelElement<DataType> icon);
	public abstract void onIconOpened(IconPanelElement<DataType> icon);
	protected abstract String labelFor(DataType data);
	public abstract boolean iconsAcceptDrags();
	public abstract boolean iconsAcceptDrops();
	
	public void refresh() {refresh(false);}
	public void refresh(boolean layoutOnly)
	{
		int nCols = Math.max(1, scrollPane.getViewport().getExtentSize().width/(iconSize+wGap));
		iconPanel.setLayout(new LooseGridLayout(0, nCols, wGap, wGap, false, false, SwingConstants.CENTER, SwingConstants.TOP, false, false));
		
		if (!layoutOnly)
		{
			iconPanel.removeAll();
			for (DataType data : getData())
				iconPanel.add(new IconPanelElement<DataType>(this, data));
			new Thread() {@SuppressWarnings("unchecked") public void run()
			{
				try
				{
					int cnt = 0;
					for (Component comp : iconPanel.getComponents())
					{
						onIconInit((IconPanelElement<DataType>)comp);
						cnt++;
						if (cnt%10 == 0)
							{validate(); repaint();}
					}
					validate();
					repaint();
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}}.start();
		}
		
		iconPanel.revalidate();
		iconPanel.repaint();
	}
	
	public void paintOver(Graphics2D g, int w, int h)
	{
		
	}
}
