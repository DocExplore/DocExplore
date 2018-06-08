package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.List;

import javax.swing.JPanel;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.app.editors.ConfigurationEditor;

@SuppressWarnings("serial")
public class StitchEditor extends JPanel implements ConfigurationEditor
{
	public static interface Listener
	{
		public void onCancelRequest();
		public void onSaveRequest(boolean force);
	}
	
	Listener listener;
	public final FragmentView view;
	GridLayout layout;
	FragmentDescriptionView left, right;
	FragmentAssociation map = null;
	StitchEditorToolkit toolkit;
	
	boolean showAssociations = true, showAlpha = false;
	
	public StitchEditor(Listener listener, FragmentView view)
	{
		super();
		
		this.listener = listener;
		this.view = view;
		this.toolkit = new StitchEditorToolkit(this);
		
		setLayout(this.layout = new GridLayout(1, 2, 5, 5));
		add(this.left = new FragmentDescriptionView(this));
		add(this.right = new FragmentDescriptionView(this));
		setPreferredSize(new Dimension(800, 600));
		requestFocusInWindow();
	}
	
	public void setMap(FragmentAssociation map)
	{
		if (this.map != null)
		{
			map.d1.image = null;
			map.d2.image = null;
		}
		this.map = map;
		try
		{
			map.d1.setImage();
			map.d2.setImage();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		left.setImageDescription(map.d1);
		right.setImageDescription(map.d2);
		view.modified = true;
		repaint();
	}
	
	public FragmentDescriptionView otherView(FragmentDescriptionView v) {return v == left ? right : left;}
	
	public void flip()
	{
		if (layout.getRows() == 1)
		{
			layout.setRows(2);
			layout.setColumns(1);
		}
		else
		{
			layout.setRows(1);
			layout.setColumns(2);
		}
		layout.layoutContainer(this);
		repaint();
	}
	public void reverse()
	{
		boolean reversed = getComponent(0) == right;
		remove(left);
		remove(right);
		if (reversed) {add(left); add(right);}
		else {add(right); add(left);}
		layout.layoutContainer(this);
		repaint();
	}
	public void fit()
	{
		left.fit();
		right.fit();
	}
	public void link()
	{
		if (left.selected != null && right.selected != null)
		{
			Association a = null;
			List<Association> list = map.associationsByPOI.get(left.selected);
			if (list != null)
				for (int i=0;i<list.size();i++)
					if (list.get(i).other(left.selected) == right.selected)
						{a = list.get(i); break;}
			if (a != null)
				map.remove(a);
			else
			{
				a = map.add(left.selected, right.selected);
				//System.out.printf("Feature dist: %.3f, desc: %.3f\n", a.p1.featureDistance2(a.p2), a.p1.descriptorDistance2(a.p2));
			}
			left.repaint();
			right.repaint();
		}
	}
	
	Point point = new Point();
	Color associationCol = new Color(1f, 0, 1f, .5f);
	@Override protected void paintChildren(Graphics g)
	{
		super.paintChildren(g);
		
//		if (map != null)
//			for (int i=0;i<map.associations.size();i++)
//			{
//				Association a = map.associations.get(i);
//				g.setColor(associationCol);
//				point.x = (int)left.fromViewX(a.p1.x);
//				point.y = (int)left.fromViewY(a.p1.y);
//				Point conv = SwingUtilities.convertPoint(left, point, this);
//				int x0 = conv.x;
//				int y0 = conv.y;
//				point.x = (int)right.fromViewX(a.p2.x);
//				point.y = (int)right.fromViewY(a.p2.y);
//				conv = SwingUtilities.convertPoint(right, point, this);
//				int x1 = conv.x;
//				int y1 = conv.y;
//				g.drawLine(x0, y0, x1, y1);
//			}
	}

	@Override public Component getComponent() {return this;}
	@Override public boolean allowGoto() {return false;}
	@Override public void onActionRequest(String action, Object param)
	{
		if (action.equals("back"))
		{
			new LayoutDetector(view.set).consolidate(left.desc.fragment);
			listener.onCancelRequest();
		}
		else if (action.equals("flip"))
			flip();
		else if (action.equals("reverse"))
			reverse();
		else if (action.equals("refresh-poi"))
			toolkit.refreshFeatures();
		else if (action.equals("detect"))
			toolkit.detectGroup(true);
		else if (action.equals("fit"))
			fit();
		else if (action.equals("toggle-link"))
			link();
		else if (action.equals("clear-poi"))
			toolkit.clearFeatures();
	}

	@Override public void onCloseRequest() {if (view.modified) listener.onSaveRequest(false);}
	@Override public void refresh() {repaint();}
	@Override public void setReadOnly(boolean b) {}
}
