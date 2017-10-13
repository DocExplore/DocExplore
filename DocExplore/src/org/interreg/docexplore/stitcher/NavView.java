package org.interreg.docexplore.stitcher;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class NavView extends JPanel
{
	double x0 = 0, y0 = 0;
	double scale = 100;
	
	NavViewInputListener inputListener;
	
	public NavView()
	{
		setFocusable(true);
		setBackground(Color.darkGray);
		
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
		setView(.5*(maxx+minx), .5*(maxy+miny), Math.min(getWidth()/w, getHeight()/h));
	}
	
	public void scrollPixels(double px, double py)
	{
		x0 -= px/scale;
		y0 -= py/scale;
		onViewChange();
		repaint();
	}
	public void setScale(double scale)
	{
		this.scale = scale;
		onViewChange();
		repaint();
	}
	public void setView(double x0, double y0, double scale)
	{
		this.x0 = x0;
		this.y0 = y0;
		this.scale = scale;
		onViewChange();
		repaint();
	}
	
	protected void onViewChange() {}
	
	public double toViewX(double px) {return (px-getWidth()/2)/scale+x0;}
	public double toViewY(double py) {return (py-getHeight()/2)/scale+y0;}
	public double fromViewX(double x) {return scale*(x-x0)+getWidth()/2;}
	public double fromViewY(double y) {return scale*(y-y0)+getHeight()/2;}
	
	BasicStroke stroke = new BasicStroke(1);
	@Override protected void paintChildren(Graphics _g)
	{
		super.paintChildren(_g);
		
		Graphics2D g = (Graphics2D)_g;
		g.translate(getWidth()/2-scale*x0, getHeight()/2-scale*y0);
		g.scale(scale, scale);
		
		BasicStroke stroke = new BasicStroke((float)(2/scale));
		g.setStroke(stroke);
		
		drawView(g, scale);
	}
	
	protected void drawView(Graphics2D g, double pixelSize)
	{
		
	}
}
