/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.interreg.docexplore.gui.image.NavViewInputListener;
import org.interreg.docexplore.internationalization.Lang;

public class FragmentViewInputListener extends NavViewInputListener implements KeyListener
{
	public static final double knobRay = 8;
	
	FragmentView view;
	JPopupMenu menu;
	
	@SuppressWarnings("serial")
	public FragmentViewInputListener(FragmentView view)
	{
		super(view);
		this.view = view;
		
		this.menu = new JPopupMenu();
		JMenuItem associate, disassociate, edit;
		menu.add(associate = new JMenuItem(Lang.s("associate")) {{addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
		{
			view.set.add(view.selected, view.highlighted);
			view.repaint();
		}});}});
		menu.add(disassociate = new JMenuItem(Lang.s("disassociate")) {{addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
		{
			view.set.remove(view.set.get(view.selected, view.highlighted));
			view.repaint();
		}});}});
		menu.add(edit = new JMenuItem(Lang.s("editStitches")) {{addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
		{
			view.listener.onEditStitchesRequest(view.selected, view.highlighted);
		}});}});
		
		menu.addPopupMenuListener(new PopupMenuListener()
		{
			@Override public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				List<FragmentAssociation> associations = view.set.associationsByFragment.get(view.selected);
				boolean hasHighlighted = false;
				if (associations != null)
					for (FragmentAssociation assoc : associations)
						if (assoc.other(view.selected) == view.highlighted)
							{hasHighlighted = true; break;}
				associate.setEnabled(!hasHighlighted);
				disassociate.setEnabled(hasHighlighted);
				edit.setEnabled(hasHighlighted);
			}
			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
			@Override public void popupMenuCanceled(PopupMenuEvent e) {}
		});
		
		view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK), "toggleFragment");
		view.getActionMap().put("toggleFragment", new AbstractAction() {public void actionPerformed(ActionEvent e)
		{
			view.toggleFull(view.selected);
		}});
	}
	
	LayoutDetector layout = null;
	double offsetx = 0, offsety = 0, fragmentAng = 0;
	double edgex = 0, edgey = 0;
	@Override public void mousePressed(MouseEvent e)
	{
		super.mousePressed(e);
		
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		if (((e.getModifiers() & (InputEvent.BUTTON1_MASK)) != 0) && view.knobFragment != null)
		{
			view.selected = view.knobFragment;
			view.knob.onGrab(view.knobFragment, mx, my);
			view.knobFragment.alpha = .6f;
			view.repaint();
		}
		else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && view.highlighted != null)
		{
			view.selected = view.highlighted;
			view.selectedEdge = view.selected.nearEdge(mx, my, knobRay/view.getScale());
			view.set.moveToLast(view.selected);
			offsetx = view.selected.uix-view.toViewX(e.getX());
			offsety = view.selected.uiy-view.toViewY(e.getY());
			fragmentAng = view.selected.uiang;
			if (view.selectedEdge >= 0)
				{edgex = view.selected.toLocalEdgeX(mx, my, view.selectedEdge); edgey = view.selected.toLocalEdgeY(mx, my, view.selectedEdge);}
			view.selected.alpha = .6f;
			view.repaint();
		}
	}
	@Override public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && view.knobFragment == null && view.selected != null && 
			!view.selected.contains(view.toViewX(e.getX()), view.toViewY(e.getY())))
		{
			view.selected.alpha = 1;
			view.selected = null;
		}
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 
			&& view.highlighted != null && view.selected != null && view.highlighted != view.selected && view.knobFragment == null)
				menu.show(e.getComponent(), e.getX(), e.getY());
//		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0 
//			&& view.highlighted != null && view.selected != null && view.highlighted != view.selected && view.knobFragment == null)
//		{
//			FragmentAssociation fa = null;
//			List<FragmentAssociation> list = view.stitcher.fragmentSet.associationsByFragment.get(view.selected);
//			if (list != null)
//				for (int i=0;i<list.size();i++)
//					if (list.get(i).other(view.selected) == view.highlighted)
//						{fa = list.get(i); break;}
//			if (fa != null && fa.transform != null)
//			{
//				if (view.selected == fa.d1.fragment)
//					fa.transform.transform(view.selected, view.highlighted);
//				else fa.transform.itransform(view.selected, view.highlighted);
//			}
//		}
		if (view.selected != null)
			view.selected.alpha = 1;
		if (view.knobFragment != null)
			view.knobFragment.alpha = 1;
		layout = null;
		view.repaint();
	}
	
	private Fragment closestNear(Fragment exclude, double mx, double my)
	{
		Fragment closest = null;
		double minDist2 = 0;
		for (int i=0;i<near.size();i++)
			if (near.get(i) != exclude && near.get(i).contains(mx, my, knobRay/view.getScale()))
		{
			double dist2 = near.get(i).dist2ToCenter(mx, my);
			if (closest == null || dist2 < minDist2)
			{
				closest = near.get(i);
				minDist2 = dist2;
			}
		}
		return closest;
	}

	List<Fragment> near = new ArrayList<Fragment>();
	@Override public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		if (menu.isShowing())
			return;
		
		double viewRay = knobRay/view.getScale();
		near.clear();
		view.nearFragments(view.toViewX(e.getX()), view.toViewY(e.getY()), viewRay, near);
		
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		
		Fragment closestKnobFragment = null;
		FragmentKnob closestKnob = null;
		double minDist2 = 0;
		for (int i=0;i<near.size();i++)
			for (int j=0;j<FragmentKnob.values().length;j++)
		{
			double dist2 = FragmentKnob.values()[j].dist2(mx, my, near.get(i));
			if (dist2 <= viewRay*viewRay && (closestKnobFragment == null || dist2 < minDist2))
			{
				closestKnobFragment = near.get(i);
				closestKnob = FragmentKnob.values()[j];
				minDist2 = dist2;
			}
		}
		if (closestKnobFragment != view.knobFragment || closestKnob != view.knob)
		{
			view.knobFragment = closestKnobFragment;
			view.knob = closestKnob;
			view.repaint();
		}
		
		Fragment closest = closestNear(null, mx, my);
		int closestEdge = closest != null ? closest.nearEdge(mx, my, viewRay) : -1;
		if (closest != view.highlighted || closestEdge != view.highlightedEdge)
		{
			view.highlighted = closest;
			view.highlightedEdge = closestEdge;
			view.repaint();
		}
	}
	double [] highlightedEdgePoint = {0, 0}, 
		nearSelectedCorner = {0, 0}, farSelectedCorner = {0, 0}, nearHighlightedCorner = {0, 0}, farHighlightedCorner = {0, 0};
	@Override public void mouseDragged(MouseEvent e)
	{
		super.mouseDragged(e);
		
		double viewRay = knobRay/view.getScale();
		near.clear();
		view.nearFragments(view.toViewX(e.getX()), view.toViewY(e.getY()), viewRay, near);
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		
		Fragment closest = closestNear(view.selected, mx, my);
		int closestEdge = closest != null ? closest.nearEdge(mx, my, viewRay) : -1;
		if (closest != view.highlighted || closestEdge != view.highlightedEdge)
		{
			view.highlighted = closest;
			view.highlightedEdge = closestEdge;
			view.repaint();
		}
		
		if (view.knobFragment != null && (e.getModifiersEx() & (MouseEvent.BUTTON1_DOWN_MASK)) != 0)
		{
			view.knob.onDrag(view.knobFragment, mx, my, false, e.isShiftDown(), e.isControlDown(), view, viewRay);
			view.modified = true;
			if (layout == null) layout = new LayoutDetector(view.set);
			layout.consolidate(view.selected);
			view.repaint();
		}
		else if (view.selected != null && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
		{
			view.selected.setAngle(fragmentAng);
			view.selected.setPos(mx+offsetx, my+offsety);
			if (view.highlighted != null && view .highlightedEdge >= 0 && view.selectedEdge >= 0)
			{
				double newAng = view.highlighted.uiang+((view.highlightedEdge+4-view.selectedEdge)%4)*.5*Math.PI-Math.PI;
				view.selected.setAngle(newAng);
				
				//align selected edge with highlighted one
				double selectedEdgeX = view.selected.fromLocalX(edgex, edgey), selectedEdgeY = view.selected.fromLocalY(edgex, edgey);
				view.highlighted.edgePoint(mx, my, view.highlightedEdge, highlightedEdgePoint);
				view.selected.setPos(view.selected.uix+highlightedEdgePoint[0]-selectedEdgeX, view.selected.uiy+highlightedEdgePoint[1]-selectedEdgeY);
				
				//look for corners lining up
				view.selected.nearCornerPoint(view.selectedEdge, nearSelectedCorner);
				view.selected.farCornerPoint(view.selectedEdge, farSelectedCorner);
				view.highlighted.nearCornerPoint(view.highlightedEdge, nearHighlightedCorner);
				view.highlighted.farCornerPoint(view.highlightedEdge, farHighlightedCorner);
				double [] from = null, to = null;
				double minDist2 = -1, dist2 = 0;
				if ((dist2 = dist2(nearSelectedCorner, nearHighlightedCorner)) < viewRay*viewRay && (minDist2 < 0 || dist2 < minDist2)) 
					{minDist2 = dist2; from = nearSelectedCorner; to = nearHighlightedCorner;}
				if ((dist2 = dist2(nearSelectedCorner, farHighlightedCorner)) < viewRay*viewRay && (minDist2 < 0 || dist2 < minDist2)) 
					{minDist2 = dist2; from = nearSelectedCorner; to = farHighlightedCorner;}
				if ((dist2 = dist2(farSelectedCorner, nearHighlightedCorner)) < viewRay*viewRay && (minDist2 < 0 || dist2 < minDist2)) 
					{minDist2 = dist2; from = farSelectedCorner; to = nearHighlightedCorner;}
				if ((dist2 = dist2(farSelectedCorner, farHighlightedCorner)) < viewRay*viewRay && (minDist2 < 0 || dist2 < minDist2)) 
					{minDist2 = dist2; from = farSelectedCorner; to = farHighlightedCorner;}
				if (from != null)
					view.selected.setPos(view.selected.uix+to[0]-from[0], view.selected.uiy+to[1]-from[1]);
			}
			view.modified = true;
			if (layout == null) layout = new LayoutDetector(view.set);
			layout.consolidate(view.selected);
			view.repaint();
		}
	}
	private double dist2(double [] p1, double [] p2) {return (p1[0]-p2[0])*(p1[0]-p2[0])+(p1[1]-p2[1])*(p1[1]-p2[1]);}
	
	@Override public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SPACE && view.selected != null)
			view.toggleFull(view.selected);
		else if (e.getKeyCode() == KeyEvent.VK_DELETE && view.selected != null)
		{
			view.set.remove(view.selected);
			view.selected = null;
			view.modified = true;
			view.repaint();
		}
	}
	
	@Override public void keyTyped(KeyEvent e) {}
	@Override public void keyPressed(KeyEvent e) {}
}
