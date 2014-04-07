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
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class StitchedImageViewer extends JPanel
{
	double x0 = 0, y0 = 0, zoom = 1;
	StitchedTrans st = null;
	
	int lastdragx = 0, lastdragy = 0; 
	public StitchedImageViewer()
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
		public void pointClicked(StitchedImageViewer viewer, double x, double y, boolean left);
		public void pointCtrlClicked(StitchedImageViewer viewer, double x, double y, boolean left);
		public void mouseMoved(StitchedImageViewer viewer, double x, double y);
		public void repaintRequested(StitchedImageViewer viewer);
	}
	List<Listener> listeners = new LinkedList<Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	void notifyPointClicked(double x, double y, boolean left)
	{
		if (st != null)
			for (Listener listener : listeners)
				listener.pointClicked(this, x, y, left);
	}
	void notifyPointCtrlClicked(double x, double y, boolean left)
	{
		if (st != null)
			for (Listener listener : listeners)
				listener.pointCtrlClicked(this, x, y, left);
	}
	void notifyMouseMoved(double x, double y)
	{
		if (st != null)
			for (Listener listener : listeners)
				listener.mouseMoved(this, x, y);
	}
	void notifyRepaint()
	{
		if (st != null)
			for (Listener listener : listeners)
				listener.repaintRequested(this);
	}
	
	public void repaint() {notifyRepaint(); super.repaint();}
	
	public void setSt(StitchedTrans st)
	{
		this.st = st;
		this.zoom = 1;
		if (st != null)
		{
			this.x0 = .5*st.w;
			this.y0 = .5*st.h;
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
		
		if (st != null)
		{
			AffineTransform t = new AffineTransform();
			t.scale(zoom, zoom);
			t.translate(.5*getWidth()/zoom-x0, .5*getHeight()/zoom-y0);
			for (StitchedTrans.Image image : st.images)
			{
				AffineTransform ti = (AffineTransform)t.clone();
				ti.translate(image.x, image.y);
				ti.scale(image.w*1./image.mini.getWidth(), image.h*1./image.mini.getHeight());
				((Graphics2D)g).drawImage(image.mini, ti, null);
			}
		}
		
		g.setColor(hasFocus() ? Color.white : Color.black);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
	}
}
