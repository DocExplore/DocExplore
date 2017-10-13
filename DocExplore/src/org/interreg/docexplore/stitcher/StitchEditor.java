package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.gui.ErrorHandler;

@SuppressWarnings("serial")
public class StitchEditor extends JPanel
{
	Stitcher stitcher;
	GridLayout layout;
	FragmentDescriptionView left, right;
	FragmentAssociation map = null;
	StitchEditorToolkit toolkit;
	
	public StitchEditor(Stitcher stitcher)
	{
		super();
		
		this.stitcher = stitcher;
		this.toolkit = new StitchEditorToolkit(this);
		
		setLayout(this.layout = new GridLayout(1, 2, 5, 5));
		add(this.left = new FragmentDescriptionView(this));
		add(this.right = new FragmentDescriptionView(this));
		setPreferredSize(new Dimension(800, 600));
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
	
	Point point = new Point();
	Color lineCol = new Color(1f, 0, 0, .5f);
	@Override protected void paintChildren(Graphics g)
	{
		super.paintChildren(g);
		
		if (map != null)
			for (int i=0;i<map.associations.size();i++)
			{
				Association a = map.associations.get(i);
				float s = (float)(a.strength*a.strength*a.strength*a.strength);
//				if (s == 0)
//					continue;
				g.setColor(new Color(s, 0, 1-s, .5f));
				point.x = (int)left.fromViewX(a.p1.x);
				point.y = (int)left.fromViewY(a.p1.y);
				Point conv = SwingUtilities.convertPoint(left, point, this);
				int x0 = conv.x;
				int y0 = conv.y;
				point.x = (int)right.fromViewX(a.p2.x);
				point.y = (int)right.fromViewY(a.p2.y);
				conv = SwingUtilities.convertPoint(right, point, this);
				int x1 = conv.x;
				int y1 = conv.y;
				g.drawLine(x0, y0, x1, y1);
			}
	}
	
	public JMenuBar buildMenu()
	{
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		bar.add(file);
		file.add(new JMenuItem(new AbstractAction("Clear and close") {@Override public void actionPerformed(ActionEvent e)
		{
			stitcher.view.associations.remove(map);
			StitchEditor.this.getTopLevelAncestor().setVisible(false);
		}}));
		file.add(new JMenuItem(new AbstractAction("Close") {@Override public void actionPerformed(ActionEvent e)
		{
			StitchEditor.this.getTopLevelAncestor().setVisible(false);
		}}));
		JMenu view = new JMenu("View");
		bar.add(view);
		view.add(new JMenuItem(new AbstractAction("Flip") {@Override public void actionPerformed(ActionEvent e)
		{
			flip();
		}}));
		JMenu tools = new JMenu("Tools");
		bar.add(tools);
		tools.add(new JMenuItem(new AbstractAction("Compute features") {@Override public void actionPerformed(ActionEvent e)
		{
			toolkit.computeFeatures();
		}}));
		tools.add(new JMenuItem(new AbstractAction("Match features") {@Override public void actionPerformed(ActionEvent e)
		{
			toolkit.matchFeatures();
			toolkit.clean();
		}}));
		tools.add(new JMenuItem(new AbstractAction("Filter matches") {@Override public void actionPerformed(ActionEvent e)
		{
			toolkit.filterMatches();
			toolkit.clean();
		}}));
		tools.add(new JMenuItem(new AbstractAction("Tighten") {@Override public void actionPerformed(ActionEvent e)
		{
			toolkit.tighten();
		}}));
		tools.add(new JMenuItem(new AbstractAction("Coarse match") {@Override public void actionPerformed(ActionEvent e)
		{
			toolkit.coarseMatch();
		}}));
		tools.add(new JMenuItem(new AbstractAction("Group match") {@Override public void actionPerformed(ActionEvent e)
		{
			toolkit.groupMatch();
		}}));
		return bar;
	}
}
