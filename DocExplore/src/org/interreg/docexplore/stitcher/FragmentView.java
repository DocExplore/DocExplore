package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Line2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.interreg.docexplore.gui.image.NavView;
import org.interreg.docexplore.gui.image.NavViewInputListener;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.app.editors.ConfigurationEditor;

@SuppressWarnings("serial")
public class FragmentView extends NavView implements ConfigurationEditor
{
	public static interface Listener
	{
		public void onEditStitchesRequest(Fragment f1, Fragment f2);
		public void onRenderRequest();
		public void onRenderEnded(List<MetaData> parts);
		public void onSaveRequest(boolean force);
		public void onCancelRequest();
		public void onDetectLayoutRequest();
	}
	
	Listener listener;
	Fragment selected = null;
	int selectedEdge = -1;
	Fragment highlighted = null;
	int highlightedEdge = -1;
	Fragment knobFragment = null;
	FragmentKnob knob = null;
	public FragmentSet set = new FragmentSet();
	
	List<Fragment> fullFragments = new ArrayList<Fragment>();
	public boolean modified = false;
	
	public FragmentView(Listener listener)
	{
		this.listener = listener;
		requestFocusInWindow();
		addKeyListener((FragmentViewInputListener)inputListener);
		setPreferredSize(new Dimension(800, 600));
		addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e) {if (scale == 0) fitView(.3);}});
		setBackground(Color.white);
	}
	@Override protected NavViewInputListener createInputListener() {return new FragmentViewInputListener(this);}
	public void setListener(Listener listener) {this.listener = listener;}
	
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
			fragments = set.fragments;
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
	
	public void setFragmentSet(FragmentSet set)
	{
		this.set = set;
		resetView();
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
	
//	public Pair<Fragment, Integer> edgeAt(double x, double y, double r)
//	{
//		
//	}
	
	public List<Fragment> nearFragments(double x, double y, double ray, List<Fragment> near)
	{
		for (int i=0;i<set.fragments.size();i++)
			if (set.fragments.get(i).isNear(x, y, ray))
				near.add(set.fragments.get(i));
		return near;
	}
	
//	double nearRay = 10;
//	List<Fragment> nearFragments = new ArrayList<Fragment>();
//	public Fragment fragmentAt(double x, double y)
//	{
//		nearFragments.clear();
//		set.fragmentsAt(x, y, nearRay/scale, nearFragments);
//		if (nearFragments.isEmpty())
//			return null;
//		if (nearFragments.size() == 1)
//			return nearFragments.get(0);
//		Fragment min = null;
//		double minDist = 0;
//		for (int i=0;i<nearFragments.size();i++)
//		{
//			Fragment fragment = nearFragments.get(i);
//			double mx = fragment.uix+.5*fragment.ux+.5*fragment.vx;
//			double my = fragment.uiy+.5*fragment.uy+.5*fragment.vy;
//			double dist = (x-mx)*(x-mx)+(y-my)*(y-my);
//			if (min == null || dist < minDist)
//			{
//				min = fragment;
//				minDist = dist;
//			}
//		}
//		return min;
//	}
	
	public boolean boundsIntersect(Fragment f)
	{
		for (int i=0;i<set.fragments.size();i++)
			if (set.fragments.get(i) != f && set.fragments.get(i).boundsIntersect(f))
				return true;
		return false;
	}
	
	Color associationColor = new Color(0f, 0f, 1f, .5f), emptyAssociationColor = new Color(1f, 0f, 0f, .5f);
	Line2D.Double line = new Line2D.Double();
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		for (int i=0;i<set.fragments.size();i++)
			set.fragments.get(i).drawImage(g);
		
		for (int i=0;i<set.fragments.size();i++)
		{
			Fragment f = set.fragments.get(i);
			f.drawOutline(g, pixelSize, f == selected, f == highlighted, f == knobFragment ? knob : null);
		}
		
		if (highlighted != null && highlightedEdge >= 0)
			highlighted.drawEdge(g, pixelSize, highlightedEdge);
		if (selected != null && selectedEdge >= 0)
			selected.drawEdge(g, pixelSize, selectedEdge);
		
		for (int i=0;i<set.associations.size();i++)
		{
			FragmentAssociation fa = set.associations.get(i);
			g.setColor(fa.associations.size() > 0 ? associationColor : emptyAssociationColor);
			line.setLine(
				fa.d1.fragment.uix+.5*fa.d1.fragment.ux+.5*fa.d1.fragment.vx, fa.d1.fragment.uiy+.5*fa.d1.fragment.uy+.5*fa.d1.fragment.vy, 
				fa.d2.fragment.uix+.5*fa.d2.fragment.ux+.5*fa.d2.fragment.vx, fa.d2.fragment.uiy+.5*fa.d2.fragment.uy+.5*fa.d2.fragment.vy);
			g.draw(line);
		}
	}
	
	@Override public void onActionRequest(String action, Object param)
	{
		if (action.equals("render-stitch"))
			listener.onRenderRequest();
		else if (action.equals("unstitch"))
			listener.onCancelRequest();
		else if (action.equals("clear-stitches"))
		{
			set.clearAssociations();
			repaint();
		}
		else if (action.equals("detect-stitches"))
			listener.onDetectLayoutRequest();
		else if (action.equals("save-stitches"))
			listener.onSaveRequest(true);
		else if (action.equals("fit"))
			fitView(.3);
	}
	
	@Override public Component getComponent() {return this;}
	@Override public void refresh() {repaint();}
	@Override public void setReadOnly(boolean b) {}
	@Override public void onCloseRequest() {if (modified) listener.onSaveRequest(false);}
	@Override public boolean allowGoto() {return false;}
}
