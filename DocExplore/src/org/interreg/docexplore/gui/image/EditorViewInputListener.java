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
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0 || view.getOperation() == null)
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
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0 || view.getOperation() == null)
			super.mousePressed(e);
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
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0 || view.getOperation() == null)
			super.mouseReleased(e);
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
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0 || view.getOperation() == null)
			super.mouseDragged(e);
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
			int dx = e.getX()-panCurX;
			int dy = e.getY()-panCurY;
			if (view.getOperation() != null)
				view.getOperation().pointDragged(view, 
					e.getX(), e.getY(), view.toViewX(e.getX()), view.toViewY(e.getY()), 
					dragDownX, dragDownY, dx, dy, 
					e.getModifiersEx());
			panCurX = e.getX();
			panCurY = e.getY();
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
