package org.interreg.docexplore.stitcher;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class FragmentView extends NavView
{
	Stitcher stitcher;
	List<Fragment> fragments = new ArrayList<Fragment>();
	List<FragmentAssociation> associations = new ArrayList<FragmentAssociation>();
	
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
		out.writeInt(fragments.size());
		for (int i=0;i<fragments.size();i++)
			fragments.get(i).write(out);
		out.writeInt(associations.size());
		for (int i=0;i<associations.size();i++)
			associations.get(i).write(out, fragments);
	}
	
	public void read(ObjectInputStream in) throws Exception
	{
		super.read(in);
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		int n = in.readInt();
		for (int i=0;i<n;i++)
			fragments.add(new Fragment(in));
		n = in.readInt();
		for (int i=0;i<n;i++)
			associations.add(new FragmentAssociation(in, fragments));
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
	
	public void delete(Fragment f)
	{
		if (selected == f)
			selected = null;
		if (highlighted == f)
			highlighted = null;
		if (knobFragment == f)
			knobFragment = null;
		fragments.remove(f);
		fullFragments.remove(f);
		repaint();
	}
	
	public void resetView()
	{
		super.resetView();
		fragments.clear();
		associations.clear();
		fullFragments.clear();
		selected = null;
		highlighted = null;
		knobFragment = null;
		knob = null;
	}
	
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
	
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		for (int i=0;i<fragments.size();i++)
			fragments.get(i).drawImage(g);
		
		for (int i=0;i<fragments.size();i++)
			fragments.get(i).drawOutline(g, pixelSize, fragments.get(i) == selected, fragments.get(i) == highlighted, fragments.get(i) == knobFragment ? knob : null);
	}
}
