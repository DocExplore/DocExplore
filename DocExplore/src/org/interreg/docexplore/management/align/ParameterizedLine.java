/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
