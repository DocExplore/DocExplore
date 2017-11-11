package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("serial")
public class FragmentView extends NavView
{
	Stitcher stitcher;
	
	Fragment selected = null;
	Fragment highlighted = null;
	Fragment knobFragment = null;
	FragmentKnob knob = null;
	
	List<Fragment> fullFragments = new ArrayList<Fragment>();
	
	public FragmentView(Stitcher stitcher)
	{
		this.stitcher = stitcher;
		
		requestFocusInWindow();
		addKeyListener((FragmentViewInputListener)inputListener);
		setPreferredSize(new Dimension(800, 600));
	}
	@Override protected NavViewInputListener createInputListener() {return new FragmentViewInputListener(this);}
	
	int serialVersion = 0;
	public void write(ObjectOutputStream out) throws Exception
	{
		super.write(out);
		out.writeInt(serialVersion);
	}
	
	public void read(ObjectInputStream in) throws Exception
	{
		super.read(in);
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		repaint();
	}
	
	public void fitView(double margin) {fitView(null, margin);}
	public void fitView(Collection<Fragment> fragments, double margin)
	{
		if (fragments == null)
			fragments = stitcher.fragmentSet.fragments;
		if (fragments.isEmpty())
			resetView();
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
			fitView(minx, miny, maxx, maxy, margin);
		}
		repaint();
	}
	
	public void toggleFull(Fragment f)
	{
		f.toggleFull();
		if (fullFragments.contains(f))
		{
			fullFragments.remove(f);
		}
		else
		{
			fullFragments.add(f);
			while (fullFragments.size() > 3)
			{
				fullFragments.get(0).toggleFull();
				fullFragments.remove(0);
			}
		}
		repaint();
	}
	
	void fragmentDeleted(Fragment f)
	{
		if (selected == f)
			selected = null;
		if (highlighted == f)
			highlighted = null;
		if (knobFragment == f)
			knobFragment = null;
		fullFragments.remove(f);
		repaint();
	}
	
	public void resetView()
	{
		super.resetView();
		fullFragments.clear();
		selected = null;
		highlighted = null;
		knobFragment = null;
		knob = null;
	}
	
	public List<Fragment> nearFragments(double x, double y, double ray, List<Fragment> near)
	{
		for (int i=0;i<stitcher.fragmentSet.fragments.size();i++)
			if (stitcher.fragmentSet.fragments.get(i).isNear(x, y, ray))
				near.add(stitcher.fragmentSet.fragments.get(i));
		return near;
	}
	
	List<Fragment> nearFragments = new ArrayList<Fragment>();
	public Fragment fragmentAt(double x, double y)
	{
		nearFragments.clear();
		stitcher.fragmentSet.fragmentsAt(x, y, nearFragments);
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
	
	public boolean boundsIntersect(Fragment f)
	{
		for (int i=0;i<stitcher.fragmentSet.fragments.size();i++)
			if (stitcher.fragmentSet.fragments.get(i) != f && stitcher.fragmentSet.fragments.get(i).boundsIntersect(f))
				return true;
		return false;
	}
	
	Color associationColor = new Color(0f, 0f, 1f, .5f);
	Line2D.Double line = new Line2D.Double();
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		for (int i=0;i<stitcher.fragmentSet.fragments.size();i++)
			stitcher.fragmentSet.fragments.get(i).drawImage(g);
		
		for (int i=0;i<stitcher.fragmentSet.fragments.size();i++)
		{
			Fragment f = stitcher.fragmentSet.fragments.get(i);
			f.drawOutline(g, pixelSize, f == selected, f == highlighted, f == knobFragment ? knob : null);
		}
		
		g.setColor(associationColor);
		for (int i=0;i<stitcher.fragmentSet.associations.size();i++)
		{
			FragmentAssociation fa = stitcher.fragmentSet.associations.get(i);
			line.setLine(
				fa.d1.fragment.uix+.5*fa.d1.fragment.ux+.5*fa.d1.fragment.vx, fa.d1.fragment.uiy+.5*fa.d1.fragment.uy+.5*fa.d1.fragment.vy, 
				fa.d2.fragment.uix+.5*fa.d2.fragment.ux+.5*fa.d2.fragment.vx, fa.d2.fragment.uiy+.5*fa.d2.fragment.uy+.5*fa.d2.fragment.vy);
			g.draw(line);
		}
	}
}
