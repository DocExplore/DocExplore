package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.JPanel;

import org.interreg.docexplore.gui.ErrorHandler;

@SuppressWarnings("serial")
public class StitchEditor extends JPanel
{
	Stitcher stitcher;
	GridLayout layout;
	FragmentDescriptionView left, right;
	FragmentAssociation map = null;
	StitchEditorToolkit toolkit;
	
	boolean showAssociations = true, showAlpha = false;
	
	public StitchEditor(Stitcher stitcher)
	{
		super();
		
		this.stitcher = stitcher;
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
}
