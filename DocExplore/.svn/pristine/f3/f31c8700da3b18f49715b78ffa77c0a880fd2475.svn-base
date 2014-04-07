package org.interreg.docexplore.stitch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class KeyPointPanel extends JPanel
{	
	ImageViewer left, right;
	File leftFile, rightFile;
	
	Set<KeyPoint> leftPoints = new HashSet<KeyPoint>(), rightPoints = new HashSet<KeyPoint>();
	double [] leftSpinePoint = null, rightSpinePoint = null;
	
	public KeyPointPanel()
	{
		super(new LooseGridLayout(0, 2, 0, 5, true, true, SwingConstants.LEFT, SwingConstants.TOP, true, true));
		
		left = new ImageViewer();
		right = new ImageViewer();
		left.addListener(new ImageViewer.Listener()
		{
			public void pointClicked(ImageViewer viewer, double x, double y, boolean leftClick)
			{
				if (hovered != null)
				{
					if (leftClick)
						updateSelectedPoint(viewer, hovered);
					else deletePoint(viewer, hovered);
				}
				else leftPoints.add(new KeyPoint(x, y)); 
				repaint();
			}
			public void pointCtrlClicked(ImageViewer viewer, double x, double y, boolean leftClick)
			{
				leftSpinePoint = new double [] {x, y};
				repaint();
			}
			public void repaintRequested(ImageViewer viewer) {repaint();}
			public void mouseMoved(ImageViewer viewer, double x, double y) {updateHoveredPoint(viewer, x, y);}
		});
		right.addListener(new ImageViewer.Listener()
		{
			public void pointClicked(ImageViewer viewer, double x, double y, boolean leftClick)
			{
				if (hovered != null)
				{
					if (leftClick)
						updateSelectedPoint(viewer, hovered);
					else deletePoint(viewer, hovered);
				}
				else rightPoints.add(new KeyPoint(x, y));
				repaint();
			}
			public void pointCtrlClicked(ImageViewer viewer, double x, double y, boolean leftClick)
			{
				rightSpinePoint = new double [] {x, y};
				repaint();
			}
			public void repaintRequested(ImageViewer viewer) {repaint();}
			public void mouseMoved(ImageViewer viewer, double x, double y) {updateHoveredPoint(viewer, x, y);}
		});
		
		left.setPreferredSize(new Dimension(640, 640));
		right.setPreferredSize(new Dimension(640, 640));
		add(left);
		add(right);
	}
	
	KeyPoint hovered = null, selected = null;
	void updateSelectedPoint(ImageViewer viewer, KeyPoint point)
	{
		if (selected == point)
			selected = null;
		else if (selected == null || (viewer == left && leftPoints.contains(selected)) || (viewer == right && rightPoints.contains(selected)))
			selected = point;
		else
		{
			selected.linkTo(point);
			selected = null;
		}
	}
	void updateHoveredPoint(ImageViewer viewer, double x, double y)
	{
		Set<KeyPoint> points = viewer == left ? leftPoints : rightPoints;
		KeyPoint newHovered = null;
		for (KeyPoint point : points)
		{
			double ix = point.x;
			double iy = point.y;
			double d2 = ((x-ix)*(x-ix)+(y-iy)*(y-iy))*(viewer.zoom*viewer.zoom);
			if (d2 < 25)
			{
				newHovered = point; 
				break;
			}
		}
		if (newHovered != hovered) repaint();
		hovered = newHovered;
	}
	void deletePoint(ImageViewer viewer, KeyPoint point)
	{
		point.clearLink();
		if (viewer == left)
			leftPoints.remove(point);
		else rightPoints.remove(point);
		if (selected == point)
			selected = null;
		if (hovered == point)
			hovered = null;
	}
	
	public void paintChildren(Graphics _g)
	{
		super.paintChildren(_g);
		Graphics2D g = (Graphics2D)_g;
		
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (left.image != null && right.image != null)
			for (KeyPoint leftPoint : leftPoints)
				if (leftPoint.link != null)
		{
			g.setColor(Color.red);
			
			double x1 = left.fromImageX(leftPoint.x);
			double y1 = left.fromImageY(leftPoint.y);
			if (x1 < 0 || x1 > left.getWidth() || y1 < 0 || y1 > left.getHeight())
				continue;
			
			KeyPoint rightPoint = leftPoint.link;
			double x2 = right.fromImageX(rightPoint.x);
			double y2 = right.fromImageY(rightPoint.y);
			if (x2 < 0 || x2 > right.getWidth() || y2 < 0 || y2 > right.getHeight())
				continue;
			
			x1 += left.getX();
			y1 += left.getY();
			x2 += right.getX();
			y2 += right.getY();
			g.draw(new Line2D.Double(x1, y1, x2, y2));
		}
		
		if (left.image != null)
		{
			for (KeyPoint leftPoint : leftPoints)
			{
				g.setColor(Color.red);
				
				double x1 = left.fromImageX(leftPoint.x);
				double y1 = left.fromImageY(leftPoint.y);
				if (x1 < 0 || x1 > left.getWidth() || y1 < 0 || y1 > left.getHeight())
					continue;
				
				x1 += left.getX();
				y1 += left.getY();
				g.draw(new Line2D.Double(x1-5, y1, x1+5, y1));
				g.draw(new Line2D.Double(x1, y1-5, x1, y1+5));
				if (hovered == leftPoint || selected == leftPoint)
				{
					g.setColor(selected == leftPoint ? Color.green : Color.blue);
					g.draw(new Rectangle2D.Double(x1-5, y1-5, 10, 10));
				}
			}
			if (leftSpinePoint != null)
			{
				g.setColor(Color.magenta);
				double x1 = left.fromImageX(leftSpinePoint[0]);
				double y1 = left.fromImageY(leftSpinePoint[1]);
				if (!(x1 < 0 || x1 > left.getWidth() || y1 < 0 || y1 > left.getHeight()))
				{
					x1 += left.getX();
					y1 += left.getY();
					g.draw(new Line2D.Double(x1-5, y1, x1+5, y1));
					g.draw(new Line2D.Double(x1, y1-5, x1, y1+5));
				}
			}
		}
		if (right.image != null)
		{
			for (KeyPoint rightPoint : rightPoints)
			{
				g.setColor(Color.red);
				
				double x1 = right.fromImageX(rightPoint.x);
				double y1 = right.fromImageY(rightPoint.y);
				if (x1 < 0 || x1 > right.getWidth() || y1 < 0 || y1 > right.getHeight())
					continue;
				
				x1 += right.getX();
				y1 += right.getY();
				g.draw(new Line2D.Double(x1-5, y1, x1+5, y1));
				g.draw(new Line2D.Double(x1, y1-5, x1, y1+5));
				if (hovered == rightPoint || selected == rightPoint)
				{
					g.setColor(selected == rightPoint ? Color.green : Color.blue);
					g.draw(new Rectangle2D.Double(x1-5, y1-5, 10, 10));
				}
			}
			if (rightSpinePoint != null)
			{
				g.setColor(Color.magenta);
				double x1 = right.fromImageX(rightSpinePoint[0]);
				double y1 = right.fromImageY(rightSpinePoint[1]);
				if (!(x1 < 0 || x1 > right.getWidth() || y1 < 0 || y1 > right.getHeight()))
				{
					x1 += right.getX();
					y1 += right.getY();
					g.draw(new Line2D.Double(x1-5, y1, x1+5, y1));
					g.draw(new Line2D.Double(x1, y1-5, x1, y1+5));
				}
			}
		}
	}
	
	void drawPoint(Graphics g, double x, double y, ImageViewer viewer, Color col)
	{
		int i0 = (int)(.5*viewer.getWidth()+(x-viewer.x0)*viewer.zoom), j0 = (int)(.5*viewer.getHeight()+(y-viewer.y0)*viewer.zoom);
		g.setColor(col);
		g.drawLine(i0-5, j0, i0+5, j0);
		g.drawLine(i0, j0-5, i0, j0+5);
	}
	
	public void setFiles(File file1, File file2, Set<KeyPoint> leftPoints, Set<KeyPoint> rightPoints, double [] leftSpinePoint, double [] rightSpinePoint) throws Exception
	{
		if (file1 == null || file2 == null)
		{
			left.setImage(null);
			right.setImage(null);
			return;
		}
		
		BufferedImage image1 = ImageUtils.read(file1);
		BufferedImage image2 = ImageUtils.read(file2);
		left.setImage(image1);
		right.setImage(image2);
		this.leftPoints = leftPoints;
		this.rightPoints = rightPoints;
		this.leftSpinePoint = leftSpinePoint;
		this.rightSpinePoint = rightSpinePoint;
		hovered = null;
		selected = null;
	}
}
