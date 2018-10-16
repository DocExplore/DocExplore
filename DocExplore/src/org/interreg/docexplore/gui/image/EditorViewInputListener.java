/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui.image;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class EditorViewInputListener extends NavViewInputListener
{
	EditorView view;
	
	public EditorViewInputListener(EditorView view)
	{
		super(view);
		
		this.view = view;
	}
	
	@Override public void mouseClicked(MouseEvent e)
	{
		super.mouseClicked(e);
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && e.getClickCount() > 1)
		{
			if (view.getOperation() != null)
			{
				view.getOperation().pointClicked(view, e.getX(), e.getY(), view.toViewX(e.getX()), view.toViewY(e.getY()), e.getModifiersEx(), e.getClickCount());
				view.checkForCompletion();
			}
		}
		else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 && e.getClickCount() == 1)
		{
			if (view.getOperation() != null)
				view.getOperation().contextMenuRequested(view, e.getX(), e.getY(), view.toViewX(e.getX()), view.toViewY(e.getY()), e.getModifiersEx());
		}
	}
	
	boolean dragging = false, draggingMaybe = false;
	int dragDownX = 0, dragDownY = 0, dragCurX = 0, dragCurY = 0;
	@Override public void mousePressed(MouseEvent e)
	{
		super.mousePressed(e);
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
		{
			draggingMaybe = true;
			dragDownX = dragCurX = e.getX();
			dragDownY = dragCurY = e.getY();
			view.checkForCompletion();
		}
	}
	
	@Override public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
		{
			if (draggingMaybe && !dragging)
				if (view.getOperation() != null)
					view.getOperation().pointClicked(view, e.getX(), e.getY(), view.toViewX(e.getX()), view.toViewY(e.getY()), e.getModifiersEx(), 1);
			if (dragging)
				if (view.getOperation() != null)
					view.getOperation().pointDropped(view, 
						e.getX(), e.getY(), view.toViewX(e.getX()), view.toViewY(e.getY()), 
						dragDownX, dragDownY, e.getX()-panCurX, e.getY()-panCurY, 
						e.getModifiersEx());
			dragging = draggingMaybe = false;
			view.checkForCompletion();
		}
	}
	
	static int dragThreshold = 3;
	@Override public void mouseDragged(MouseEvent e)
	{
		super.mouseDragged(e);
		if (draggingMaybe && !dragging)
		{
			int dx = e.getX()-dragDownX;
			int dy = e.getY()-dragDownY;
			if (dx*dx+dy*dy >= dragThreshold*dragThreshold)
			{
				dragging = true;
				if (view.getOperation() != null)
					view.getOperation().pointGrabbed(view, e.getX(), e.getY(), view.toViewX(e.getX()), view.toViewY(e.getY()), e.getModifiersEx());
			}
		}
		if (dragging)
		{
			int dx = e.getX()-dragCurX;
			int dy = e.getY()-dragCurY;
			if (view.getOperation() != null)
				view.getOperation().pointDragged(view, 
					e.getX(), e.getY(), view.toViewX(e.getX()), view.toViewY(e.getY()), 
					dragDownX, dragDownY, dx, dy, 
					e.getModifiersEx());
			dragCurX = e.getX();
			dragCurY = e.getY();
			view.checkForCompletion();
		}
	}
	
	@Override public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		if (view.getOperation() != null)
		{
			view.getOperation().pointHovered(view, e.getX(), e.getY(), view.toViewX(e.getX()), view.toViewY(e.getY()), e.getModifiersEx());
			view.checkForCompletion();
		}
	}
}
