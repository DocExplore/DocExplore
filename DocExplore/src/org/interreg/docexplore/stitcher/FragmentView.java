package org.interreg.docexplore.stitcher;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class FragmentView extends JPanel
{
	Fragment selected = null;
	Fragment highlighted = null;
	Fragment knobFragment = null;
	FragmentKnob knob = null;
	
	List<Fragment> fragments = new ArrayList<Fragment>();
	
	double x0 = 0, y0 = 0;
	double scale = 100;
	
	FragmentViewMouseListener mouseListener;
	
	public FragmentView()
	{
		setFocusable(true);
		setBackground(Color.darkGray);
		
		this.mouseListener = new FragmentViewMouseListener(this);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addMouseWheelListener(mouseListener);
	}
	
	int serialVersion = 0;
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeDouble(x0);
		out.writeDouble(y0);
		out.writeDouble(scale);
		out.writeInt(fragments.size());
		for (int i=0;i<fragments.size();i++)
			fragments.get(i).write(out);
	}
	
	public void read(ObjectInputStream in) throws Exception
	{
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		x0 = in.readDouble();
		y0 = in.readDouble();
		scale = in.readDouble();
		int n = in.readInt();
		for (int i=0;i<n;i++)
			fragments.add(new Fragment(in));
		repaint();
	}
	
	public void importFragments(final File [] files)
	{
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			float progress = 0;
			@Override public void run()
			{
				double x0 = 0;
				for (int i=0;i<files.length;i++) try
				{
					progress = i*1f/files.length;
					Fragment f = new Fragment(files[i]);
					fragments.add(f);
					f.setPos(x0, f.uiy);
					while (boundsIntersect(f))
						f.setPos(x0 = f.uix+1.5, f.uiy);
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
			@Override public float getProgress() {return progress;}
		}, this);
		fitView(.1);
	}
	
	public void fitView(double margin) {fitView(null, margin);}
	public void fitView(Collection<Fragment> fragments, double margin)
	{
		if (fragments == null)
			fragments = this.fragments;
		if (fragments.isEmpty())
		{
			x0 = 0; 
			y0 = 0;
			scale = 100;
		}
		else
		{
			double minx = 0, maxx = 0, miny = 0, maxy = 0;
			boolean first = true;
			for (Fragment f : fragments)
			{
				if (first || f.minx < minx) minx = f.minx;
				if (first || f.maxx > maxx) maxx = f.maxx;
				if (first || f.miny < miny) miny = f.miny;
				if (first || f.maxy > maxy) maxy = f.maxy;
				first = false;
			}
			double w = (1+margin)*(maxx-minx), h = (1+margin)*(maxy-miny);
			x0 = .5*(maxx+minx);
			y0 = .5*(maxy+miny);
			scale = Math.min(getWidth()/w, getHeight()/h);
		}
		repaint();
	}
	
	public void clear()
	{
		fragments.clear();
		selected = null;
		highlighted = null;
		knobFragment = null;
		knob = null;
		x0 = 0;
		y0 = 0;
		scale = 100;
		repaint();
	}
	
	public void scrollPixels(double px, double py)
	{
		x0 -= px/scale;
		y0 -= py/scale;
	}
	
	public double toViewX(double x) {return (x-getWidth()/2)/scale+x0;}
	public double toViewY(double y) {return (y-getHeight()/2)/scale+y0;}
	public double fromViewX(double x) {return scale*(x-x0)+getWidth()/2;}
	public double fromViewY(double y) {return scale*(y-y0)+getHeight()/2;}
	
	public List<Fragment> nearFragments(double x, double y, double ray, List<Fragment> near)
	{
		for (int i=0;i<fragments.size();i++)
			if (fragments.get(i).isNear(x, y, ray))
				near.add(fragments.get(i));
		return near;
	}
	
	List<Fragment> nearFragments = new ArrayList<Fragment>();
	public Fragment fragmentAt(double x, double y)
	{
		nearFragments.clear();
		fragmentsAt(x, y, nearFragments);
		if (nearFragments.isEmpty())
			return null;
		if (nearFragments.size() == 1)
			return nearFragments.get(0);
		Fragment min = null;
		double minDist = 0;
		for (int i=0;i<nearFragments.size();i++)
		{
			Fragment fragment = nearFragments.get(i);
			double mx = fragment.uix+.5*fragment.ux+.5*fragment.vx;
			double my = fragment.uiy+.5*fragment.uy+.5*fragment.vy;
			double dist = (x-mx)*(x-mx)+(y-my)*(y-my);
			if (min == null || dist < minDist)
			{
				min = fragment;
				minDist = dist;
			}
		}
		return min;
	}
	public void fragmentsAt(double x, double y, Collection<Fragment> res)
	{
		for (int i=0;i<fragments.size();i++)
			if (fragments.get(i).contains(x, y))
				res.add(fragments.get(i));
	}
	
	public boolean boundsIntersect(Fragment f)
	{
		for (int i=0;i<fragments.size();i++)
			if (fragments.get(i) != f && fragments.get(i).boundsIntersect(f))
				return true;
		return false;
	}
	
	BasicStroke stroke = new BasicStroke(1);
	@Override protected void paintChildren(Graphics _g)
	{
		super.paintChildren(_g);
		
		Graphics2D g = (Graphics2D)_g;
		g.translate(getWidth()/2-scale*x0, getHeight()/2-scale*y0);
		g.scale(scale, scale);
		
		for (int i=0;i<fragments.size();i++)
			fragments.get(i).drawImage(g);
		
		BasicStroke stroke = new BasicStroke((float)(2/scale));
		g.setStroke(stroke);
		for (int i=0;i<fragments.size();i++)
			fragments.get(i).drawOutline(g, scale, fragments.get(i) == selected, fragments.get(i) == highlighted, fragments.get(i) == knobFragment ? knob : null);
	}
}
