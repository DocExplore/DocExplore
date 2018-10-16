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

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

public class NavView extends JPanel
{
	private static final long serialVersionUID = 1252503368700161717L;
	
	protected double x0 = 0, y0 = 0;
	protected double scale = 100;
	
	protected NavViewInputListener inputListener;
	
	public NavView()
	{
		super(null);
		setFocusable(true);
		
		this.inputListener = createInputListener();
		addMouseListener(inputListener);
		addMouseMotionListener(inputListener);
		addMouseWheelListener(inputListener);
	}
	protected NavViewInputListener createInputListener() {return new NavViewInputListener(this);}
	
	int serialVersion = 0;
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeDouble(x0);
		out.writeDouble(y0);
		out.writeDouble(scale);
	}
	
	public void read(ObjectInputStream in) throws Exception
	{
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		double x0 = in.readDouble();
		double y0 = in.readDouble();
		double scale = in.readDouble();
		setView(x0, y0, scale);
	}
	
	public void resetView() {setView(0, 0, 100);}
	
	public void fitView(double minx, double miny, double maxx, double maxy, double margin)
	{
		double w = (1+margin)*(maxx-minx), h = (1+margin)*(maxy-miny);
		setView(.5*(maxx+minx), .5*(maxy+miny), Math.min(Math.max(0, getWidth())/w, Math.max(0, getHeight())/h));
	}
	
	protected void constrain() {}
	
	public void scrollPixels(double px, double py)
	{
		x0 -= px/scale;
		y0 -= py/scale;
		constrain();
		onViewChange();
		repaint();
	}
	public double getScale() {return scale;}
	public void setScale(double scale)
	{
		this.scale = scale;
		constrain();
		onViewChange();
		repaint();
	}
	public void setView(NavView view) {setView(view.x0, view.y0, view.scale);}
	public void setView(double x0, double y0, double scale)
	{
		this.x0 = x0;
		this.y0 = y0;
		this.scale = scale;
		//constrain();
		onViewChange();
		repaint();
	}
	
	protected void onViewChange() {}
	
	public double toViewX(double px) {return (px-getWidth()/2)/scale+x0;}
	public double toViewY(double py) {return (py-getHeight()/2)/scale+y0;}
	public double fromViewX(double x) {return scale*(x-x0)+getWidth()/2;}
	public double fromViewY(double y) {return scale*(y-y0)+getHeight()/2;}
	
	protected float defaultStrokeWidth = 2;
	protected AffineTransform defaultTransform = new AffineTransform(), viewTransform = new AffineTransform();
	BasicStroke stroke = new BasicStroke(1);
	@Override protected void paintChildren(Graphics _g)
	{
		if (scale != 0)
		{
			Graphics2D g = (Graphics2D)_g;
			
			viewTransform = g.getTransform();
			defaultTransform.setTransform(viewTransform);
			viewTransform.translate(getWidth()/2-scale*x0, getHeight()/2-scale*y0);
			viewTransform.scale(scale, scale);
			g.setTransform(viewTransform);
			
			BasicStroke stroke = new BasicStroke((float)(defaultStrokeWidth/scale));
			g.setStroke(stroke);
			
			drawView(g, scale);
			
			g.setTransform(defaultTransform);
		}
		super.paintChildren(_g);
	}
	
	protected void drawView(Graphics2D g, double pixelSize) {}
}
