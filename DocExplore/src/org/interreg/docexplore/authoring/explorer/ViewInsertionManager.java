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
