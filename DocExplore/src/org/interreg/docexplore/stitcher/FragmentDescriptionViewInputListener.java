/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import org.interreg.docexplore.gui.image.NavViewInputListener;

public class FragmentDescriptionViewInputListener extends NavViewInputListener
{
	public static final double poiRay = 2;
	
	FragmentDescriptionView view;
	
	
	public FragmentDescriptionViewInputListener(FragmentDescriptionView view)
	{
		super(view);
		
		this.view = view;
	}
	
	@Override public void mousePressed(MouseEvent e)
	{
		super.mousePressed(e);
		
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
		{
			view.setSelected(view.highlighted);
		}
	}
	
	@Override public void mouseDragged(MouseEvent e)
	{
		super.mouseDragged(e);
		
//		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0 && view.highlighted != null && view.highlighted == view.selected)
//		{
//			double x = view.toViewX(e.getX()), y = view.toViewY(e.getY());
//			view.desc.move(view.selected, x, y);
//			view.repaint();
//		}
	}
	
	@Override public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
		{
			if (view.highlighted != null && view.highlighted == view.selected)
			{
				view.desc.remove(view.selected);
				view.setHighlighted(null);
				view.setSelected(null);
			}
			else
			{
				double x = view.toViewX(e.getX()), y = view.toViewY(e.getY());
				if (x >= 0 && x <= view.desc.fragment.imagew && y >= 0 && y <= view.desc.fragment.imageh)
				{
					view.desc.add(x, y);
					view.repaint();
				}
			}
		}
	}
	
	@Override public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		if (view.desc == null)
			return;
		
		double x = view.toViewX(e.getX()), y = view.toViewY(e.getY());
		double x0 = view.toViewX(e.getX()-poiRay)-1, y0 = view.toViewY(e.getY()-poiRay)-1;
		double x1 = view.toViewX(e.getX()+poiRay)+1, y1 = view.toViewY(e.getY()+poiRay)+1;
		int i0 = view.desc.binx(x0), j0 = view.desc.biny(y0), i1 = view.desc.binx(x1), j1 = view.desc.biny(y1);
		POI min = null;
		double minDist = 0;
		for (int i=i0;i<=i1;i++)
			for (int j=j0;j<=j1;j++)
				if (view.desc.bins[i][j] != null)
					for (int k=0;k<view.desc.bins[i][j].size();k++)
					{
						POI poi = view.desc.bins[i][j].get(k);
						double d = (x-poi.x)*(x-poi.x)+(y-poi.y)*(y-poi.y);
						if (min == null || d < minDist)
						{
							min = poi;
							minDist = d;
						}
					}
		if (min != null && minDist <= (poiRay/view.getScale()+1)*(poiRay/view.getScale()+1))
			view.setHighlighted(min);
		else view.setHighlighted(null);
	}
}
