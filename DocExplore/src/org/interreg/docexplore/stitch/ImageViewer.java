/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitch;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageViewer extends JPanel
{
	double x0 = 0, y0 = 0, zoom = 1;
	BufferedImage image = null;
	
	int lastdragx = 0, lastdragy = 0; 
	public ImageViewer()
	{
		addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseMoved(MouseEvent e) {notifyMouseMoved(toImageX(e.getX()), toImageY(e.getY()));}
			
			public void mouseDragged(MouseEvent e)
			{
				x0 -= (e.getX()-lastdragx)/zoom;
				y0 -= (e.getY()-lastdragy)/zoom;
				lastdragx = e.getX();
				lastdragy = e.getY();
				repaint();
			}
		});
		addMouseListener(new MouseListener()
		{
			public void mouseReleased(MouseEvent arg0) {}
			
			public void mousePressed(MouseEvent e)
			{
				lastdragx = e.getX();
				lastdragy = e.getY();
			}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent e) {requestFocus();}
			public void mouseClicked(MouseEvent e)
			{
				if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0)
					notifyPointCtrlClicked(toImageX(e.getX()), toImageY(e.getY()), e.getButton() == MouseEvent.BUTTON1);
				else notifyPointClicked(toImageX(e.getX()), toImageY(e.getY()), e.getButton() == MouseEvent.BUTTON1);
			}
		});
		addMouseWheelListener(new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				zoom *= Math.pow(1.15, -e.getWheelRotation());
				repaint();
			}
		});
		setFocusable(true);
		addFocusListener(new FocusListener()
		{
			public void focusLost(FocusEvent arg0) {repaint();}
			public void focusGained(FocusEvent arg0) {repaint();}
		});
	}
	
	public static interface Listener
	{
		public void pointClicked(ImageViewer viewer, double x, double y, boolean left);
		public void pointCtrlClicked(ImageViewer viewer, double x, double y, boolean left);
		public void mouseMoved(ImageViewer viewer, double x, double y);
		public void repaintRequested(ImageViewer viewer);
	}
	List<Listener> listeners = new LinkedList<Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	void notifyPointClicked(double x, double y, boolean left)
	{
		if (image != null)
			for (Listener listener : listeners)
				listener.pointClicked(this, x, y, left);
	}
	void notifyPointCtrlClicked(double x, double y, boolean left)
	{
		if (image != null)
			for (Listener listener : listeners)
				listener.pointCtrlClicked(this, x, y, left);
	}
	void notifyMouseMoved(double x, double y)
	{
		if (image != null)
			for (Listener listener : listeners)
				listener.mouseMoved(this, x, y);
	}
	void notifyRepaint()
	{
		if (image != null)
			for (Listener listener : listeners)
				listener.repaintRequested(this);
	}
	
	public void repaint() {notifyRepaint(); super.repaint();}
	
	public void setImage(BufferedImage image)
	{
		this.image = image;
		this.zoom = 1;
		if (image != null)
		{
			this.x0 = .5*image.getWidth();
			this.y0 = .5*image.getHeight();
		}
		repaint();
	}
	
	public double toImageX(int x) {return x0+(x-.5*getWidth())/zoom;}
	public double toImageY(int y) {return y0+(y-.5*getHeight())/zoom;}
	public double fromImageX(double x) {return zoom*(x-x0)+.5*getWidth();}
	public double fromImageY(double y) {return zoom*(y-y0)+.5*getHeight();}
	
	public boolean isVisible(double ix, double iy)
	{
		double x = fromImageX(ix), y = fromImageY(iy);
		return x > 0 && x < getWidth() && y > 0 && y < getHeight();
	}
	
	public void paintComponent(Graphics g)
	{
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.gray);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if (image != null)
		{
			AffineTransform t = new AffineTransform();
			t.scale(zoom, zoom);
			t.translate(.5*getWidth()/zoom-x0, .5*getHeight()/zoom-y0);
			((Graphics2D)g).drawImage(image, t, null);
		}
		
		g.setColor(hasFocus() ? Color.white : Color.black);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		
	}
}
