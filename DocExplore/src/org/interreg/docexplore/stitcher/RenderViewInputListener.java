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

import java.awt.event.MouseEvent;

import org.interreg.docexplore.gui.image.NavViewInputListener;

public class RenderViewInputListener extends NavViewInputListener
{
	RenderView view;
	
	public RenderViewInputListener(RenderView view)
	{
		super(view);
		this.view = view;
	}
	
	int ray = 10;
	@Override public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		double l = ray/view.getScale();
		int hightlighted = 0;
		if (!view.editor.rendering)
		{
			if ((view.minx-mx)*(view.minx-mx) < l*l) hightlighted += 1;
			if ((view.miny-my)*(view.miny-my) < l*l) hightlighted += 2;
			if ((view.maxx-mx)*(view.maxx-mx) < l*l) hightlighted += 4;
			if ((view.maxy-my)*(view.maxy-my) < l*l) hightlighted += 8;
		}
		
		if (hightlighted != view.highlighted)
		{
			view.highlighted = hightlighted;
			view.repaint();
		}
	}
	
	@Override public void mouseDragged(MouseEvent e)
	{
		super.mouseDragged(e);
		if (view.highlighted == -1 || view.editor.rendering)
			return;
		
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		if ((view.highlighted & 1) > 0) view.minx = Math.min(mx, view.maxx);
		else if ((view.highlighted & 4) > 0) view.maxx = Math.max(mx, view.minx);
		if ((view.highlighted & 2) > 0) view.miny = Math.min(my, view.maxy);
		else if ((view.highlighted & 8) > 0) view.maxy = Math.max(my, view.miny);
		view.editor.updateFields();
	}
}
