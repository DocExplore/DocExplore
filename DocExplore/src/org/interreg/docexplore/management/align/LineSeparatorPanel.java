package org.interreg.docexplore.management.align;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class LineSeparatorPanel implements LabeledImageViewer.EditorListener
{
	LabeledImageViewer editor;
	ParameterizedLine [] separators;
	int highlighted;
	ParameterizedLine.Knob knob;
	
	public LineSeparatorPanel(LabeledImageViewer editor)
	{
		this.editor = editor;
		editor.addEditorListener(this);
		this.separators = new ParameterizedLine [0];
		this.highlighted = -1;
		this.knob = null;
	}
	
	public void mouseMoved(LabeledImageViewer editor, double x, double y)
	{
		int hovered = separatorAt(x, y, editor);
		
		ParameterizedLine.Knob hoveredKnob = hovered == -1 ? null : 
			separators[hovered].knobAt(x, y, editor);
		if (hovered == highlighted && hoveredKnob == knob)
			return;
		
		highlighted = hovered;
		knob = hoveredKnob;
		LineSeparatorPanel.this.editor.repaint();
	}
	public void mouseDragged(LabeledImageViewer editor, int button, double x, double y)
	{
		if (highlighted < 0)
			return;
		
		if ((button & InputEvent.BUTTON1_MASK) > 0 && (button & InputEvent.SHIFT_MASK) == 0)
		{
			if (knob != null) putKnobAt(highlighted, knob, x, y);
			else putSeparatorAt(highlighted, x, y);
		}
		else if ((button & InputEvent.BUTTON1_MASK) > 0 && (button & InputEvent.SHIFT_MASK) > 0 && knob != null)
			putKnobRayAt(highlighted, knob, x, y);
	}
	public void mouseClicked(LabeledImageViewer editor, int button, int modifiers, double x, double y)
	{
		if ((button != MouseEvent.BUTTON1 || (modifiers & InputEvent.SHIFT_MASK) == 0) &&
				button != MouseEvent.BUTTON2)
			return;
		if (highlighted < 0)
			return;
		
		if (knob != null)
		{
			separators[highlighted].knobs.remove(knob);
			editor.repaint();
		}
		else
		{
			knob = new ParameterizedLine.Knob(0, 0, .05);
			separators[highlighted].knobs.add(knob);
			putKnobAt(highlighted, knob, x, y);
		}
	}
	
	private void putKnobAt(int i, ParameterizedLine.Knob knob, double x, double y)
	{
		knob.x = x;
		knob.dy = y-separators[i].y;
		editor.repaint();
	}
	private void putKnobRayAt(int i, ParameterizedLine.Knob knob, double x, double y)
	{
		double dx = x-knob.x;
		knob.ray = Math.sqrt(dx*dx);
		knob.ray = knob.ray < .002 ? .002 : knob.ray > .1 ? .1 : knob.ray;
		editor.repaint();
	}
	
	private void putSeparatorAt(int i, double x, double y)
	{
		double s = y-separators[i].dyAt(x);
		if (s > 1) s = 1;
		if (s < 0) s = 0;
		
		double lim = .1/separators.length;
		if (separators.length > i+1 && s > separators[i+1].y-lim)
			s = separators[i+1].y-lim;
		if (i > 0 && s < separators[i-1].y+lim)
			s = separators[i-1].y+lim;
		
		
		separators[i].y = s;
		editor.repaint();
	}
	
	private int separatorAt(double x, double y, LabeledImageViewer editor)
	{
		double r = 4*editor.cz;
		for (int i=0;i<separators.length;i++)
		{
			double sy = separators[i].yAt(x);
			double ly = editor.toScreenYLength(y-sy);
			if (ly*ly < r*r)
				return i;
		}
		
		return -1;
	}
	
	public void setNSeparators(int n)
	{
		this.separators = new ParameterizedLine [n];
		for (int i=0;i<n;i++)
			separators[i] = new ParameterizedLine(i*1./(n-1));
		
		editor.repaint();
	}
	
	Stroke stroke = new BasicStroke(1);
	Color glassRed = new Color(255, 0, 0, 127);
	Color glassYellow = new Color(255, 255, 0, 127);
	public void rendered(LabeledImageViewer editor, Graphics2D g)
	{
		int h = editor.displayBuffer.getHeight();
		int w = editor.displayBuffer.getWidth();
		
		g.setStroke(stroke);
		for (int i=0;i<separators.length;i++)
		{
			if (i == highlighted) g.setColor(glassYellow);
			else g.setColor(glassRed);
			separators[i].render(g, w, h);
			if (i == highlighted)
				separators[i].renderKnobs(g, w, h, knob, editor.fromScreenLength(10));
		}
	}
}
