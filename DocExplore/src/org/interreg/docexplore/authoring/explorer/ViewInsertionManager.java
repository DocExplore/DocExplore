package org.interreg.docexplore.authoring.explorer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class ViewInsertionManager
{
	ExplorerView view;
	
	public ViewInsertionManager(ExplorerView view)
	{
		this.view = view;
	}
	
	public int insertionIndex(int x, int y)
	{
		if (view.items.isEmpty())
			return 0;
		if (view.explorer.iconMode)
		{
			for (int i=0;i<view.items.size();i++)
			{
				ViewItem item = view.items.get(i);
				if (item.getY() < y && item.getY()+item.getHeight() > y && x < item.getX()+item.getWidth()/2)
					return i;
			}
			for (int i=view.items.size()-1;i>=0;i--)
			{
				ViewItem item = view.items.get(i);
				if (item.getY() < y && item.getY()+item.getHeight() > y && x > item.getX()+item.getWidth()/2)
					return i+1;
			}
			for (int i=0;i<view.items.size();i++)
			{
				ViewItem item = view.items.get(i);
				if (item.getY() > y)
					return i;
			}
			for (int i=view.items.size()-1;i>=0;i--)
			{
				ViewItem item = view.items.get(i);
				if (item.getY()+item.getHeight() < y)
					return i+1;
			}
		}
		else
		{
			for (int i=0;i<view.items.size();i++)
			{
				ViewItem item = view.items.get(i);
				if (item.getY()+item.getHeight()/2 > y)
					return i;
			}
			return view.items.size();
		}
		return -1;
	}
	
	public ViewItem itemAt(int x, int y)
	{
		Component comp = view.getComponentAt(x, y);
		if (comp != null && comp instanceof ViewItem)
			return (ViewItem)comp;
		return null;
	}
	
	public Stroke stroke = new BasicStroke(3);
	public void paintInsertionArea(Graphics _g, int x, int y, boolean isInsertionDrop)
	{
		Graphics2D g = (Graphics2D)_g;
		Stroke old = g.getStroke();
		g.setStroke(stroke);
		int index = insertionIndex(x, y);
		if (index < 0)
			return;
		ViewItem before = index < view.items.size() ? view.items.get(index) : null;
		ViewItem after = index > 0 ? view.items.get(index-1) : null;
		
		g.setColor(Color.red);
		
		ViewItem item = itemAt(x, y);
		if (item != null && !isInsertionDrop)
		{
			g.drawRect(item.getX(), item.getY(), item.getWidth(), item.getHeight());
		}
		else if (isInsertionDrop)
		{
			if (view.explorer.iconMode)
			{
				if (before != null)
					g.drawLine(before.getX()-2, before.getY(), before.getX()-2, before.getY()+before.getHeight());
				if (after != null)
					g.drawLine(after.getX()+after.getWidth()+1, after.getY(), after.getX()+after.getWidth()+1, after.getY()+after.getHeight());
			}
			else
			{
				if (before != null)
					g.drawLine(before.getX(), before.getY()-1, before.getX()+before.getWidth(), before.getY()-1);
				if (after != null)
					g.drawLine(after.getX(), after.getY()+after.getHeight(), after.getX()+after.getWidth(), after.getY()+after.getHeight());
			}
		}
		
		g.setStroke(old);
	}
}
