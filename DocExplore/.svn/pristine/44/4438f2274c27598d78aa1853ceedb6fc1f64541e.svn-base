package org.interreg.docexplore.management.align;

import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

public class ParameterizedLine
{
	public static class Knob
	{
		double x, dy, ray;

		public Knob(double x, double dy, double ray)
		{
			this.x = x;
			this.dy = dy;
			this.ray = ray;
		}
		
		public double influence(double px)
		{
			double dx2 = (px-x)*(px-x);
			double r2 = ray*ray;
			if (dx2 > r2)
				return 0;
			double k = (r2-dx2)/r2;
			return k*k*k;
		}
	}
	
	public double y;
	public Set<Knob> knobs;
	
	public ParameterizedLine(double y)
	{
		this.y = y;
		this.knobs = new HashSet<Knob>();
	}
	
	int knobRay = 7;
	public Knob knobAt(double mx, double my, LabeledImageViewer editor)
	{
		double r = knobRay*editor.cz;
		for (Knob knob : knobs)
		{
			double dx = mx-knob.x, dy = my-y-knob.dy;
			if (editor.toScreenLength(dx, dy) <= r*r)
				return knob;
		}
		return null;
	}
	
	public double yAt(double x) {return y+dyAt(x);}
	public double dyAt(double x0)
	{
		double totinf = 0;
		for (Knob knob : knobs)
			totinf += knob.influence(x0);
		double yinf = totinf < 1 ? 1-totinf : 0;
		double ry = 0;
		totinf += yinf;
		for (Knob knob : knobs)
			ry += knob.influence(x0)*knob.dy/totinf;
		return ry;//yinf*y+(1-yinf)*ry;
	}
	
	public void renderKnobs(Graphics2D g, int w, int h, Knob highlighted, double pixelSize)
	{
		for (Knob knob : knobs)
		{
			int cx = (int)(w*knob.x);
			int cy = (int)(h*(y+knob.dy));
			g.fillOval(cx-knobRay, cy-knobRay, 2*knobRay, 2*knobRay);
			
			if (knob == highlighted)
			{
				int ray = (int)(w*knob.ray);
				g.drawOval(cx-ray, cy-ray, 2*ray, 2*ray);
			}
		}
	}
	
	public void render(Graphics2D g, int w, int h)
	{
		int nSteps = 200;
		int oldx = 0, oldy = (int)(h*yAt(0));
		for (int i=1;i<nSteps;i++)
		{
			double cx = i*1./(nSteps-1);
			double cy = yAt(cx);
			g.drawLine(oldx, oldy, (int)(w*cx), (int)(h*cy));
			oldx = (int)(w*cx);
			oldy = (int)(h*cy);
		}
		
	}
}
