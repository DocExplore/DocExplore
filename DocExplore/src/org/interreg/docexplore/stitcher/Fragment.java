package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

import org.interreg.docexplore.util.ImageUtils;

public class Fragment
{
	static int serialVersion = 0;
	static int miniSize = 128;
	
	File file;
	final int imagew, imageh;
	BufferedImage mini;
	double uix, uiy;
	double uiw;
	double uiang;
	
	AffineTransform transform;
	double uih, ux, uy, vx, vy;
	double minx, miny, maxx, maxy;
	
	public Fragment(File file) throws Exception
	{
		BufferedImage img = ImageIO.read(file);
		if (img == null)
			throw new NullPointerException();
		
		this.file = file;
		this.imagew = img.getWidth();
		this.imageh = img.getHeight();
		this.mini = ImageUtils.createIconSizeImage(img, miniSize);
		this.uix = 0; this.uiy = 0;
		this.uiw = 1;
		this.uiang = 0;
		
		update();
	}
	
	public Fragment(ObjectInputStream in) throws Exception
	{
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		this.file = (File)in.readObject();
		this.imagew = in.readInt();
		this.imageh = in.readInt();
		ByteArrayInputStream imgIn = new ByteArrayInputStream((byte [])in.readObject());
		this.mini = ImageIO.read(imgIn);
		imgIn.close();
		this.uix = in.readDouble(); this.uiy = in.readDouble();
		this.uiw = in.readDouble();
		this.uiang = in.readDouble();
		
		update();
	}
	
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeObject(file);
		out.writeInt(imagew);
		out.writeInt(imageh);
		ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
		ImageIO.write(mini, "PNG", imgOut);
		out.writeObject(imgOut.toByteArray());
		imgOut.close();
		out.writeDouble(uix); out.writeDouble(uiy);
		out.writeDouble(uiw);
		out.writeDouble(uiang);
	}
	
	public void setPos(double uix, double uiy)
	{
		this.uix = uix;
		this.uiy = uiy;
		update();
	}
	public void setSize(double uiw)
	{
		this.uiw = uiw;
		update();
	}
	public void setAngle(double uiang)
	{
		this.uiang = uiang;
		update();
	}
	
	public double toLocalX(double x, double y) {return ((x-uix)*ux+(y-uiy)*uy)/(ux*ux+uy*uy);}
	public double toLocalY(double x, double y) {return ((x-uix)*vx+(y-uiy)*vy)/(vx*vx+vy*vy);}
	public double fromLocalX(double x, double y) {return uix+x*ux+y*vx;}
	public double fromLocalY(double x, double y) {return uiy+x*uy+y*vy;}
	public boolean contains(double x, double y)
	{
		if (x < minx || x > maxx || y < miny || y > maxy)
			return false;
		double lx = toLocalX(x, y);
		if (lx < 0 || lx > 1) return false;
		double ly = toLocalY(x, y);
		if (ly < 0 || ly > 1) return false;
		return true;
	}
	
	public boolean boundsIntersect(Fragment f)
	{
		return !(minx > f.maxx || maxx < f.minx || miny > f.maxy || maxy < f.miny);
	}
	
	public double [] closestPoint(double x, double y, double [] res)
	{
		double lx = toLocalX(x, y);
		double ly = toLocalY(x, y);
		lx = Math.max(0, Math.min(1, lx));
		ly = Math.max(0, Math.min(1, ly));
		if (lx >= 0 && lx <= 1 && ly >= 0 && ly <= 1)
		{
			res[0] = x;
			res[1] = y;
		}
		else
		{
			res[0] = fromLocalX(lx, ly);
			res[1] = fromLocalY(lx, ly);
		}
		return res;
	}
	
	public boolean isNear(double x, double y, double ray)
	{
		if (x+ray < minx || x-ray > maxx || y+ray < miny || y-ray > maxy)
			return false;
		return dist2(x, y) <= ray*ray;
	}
	double [] point = {0, 0};
	public double dist2(double x, double y)
	{
		closestPoint(x, y, point);
		return (x-point[0])*(x-point[0])+(y-point[1])*(y-point[1]);
	}
	public double dist2ToCenter(double x, double y)
	{
		double cx = fromLocalX(.5, .5), cy = fromLocalY(.5, .5);
		return (x-cx)*(x-cx)+(y-cy)*(y-cy);
	}
	
	public void update()
	{
		this.uih = imageh*this.uiw/imagew;
		this.ux = uiw*Math.cos(uiang);
		this.uy = uiw*Math.sin(uiang);
		this.vx = -imageh*uy/imagew;
		this.vy = imageh*ux/imagew;
		
		this.minx = Math.min(uix, Math.min(uix+ux, Math.min(uix+ux+vx, uix+vx)));
		this.maxx = Math.max(uix, Math.max(uix+ux, Math.max(uix+ux+vx, uix+vx)));
		this.miny = Math.min(uiy, Math.min(uiy+uy, Math.min(uiy+uy+vy, uiy+vy)));
		this.maxy = Math.max(uiy, Math.max(uiy+uy, Math.max(uiy+uy+vy, uiy+vy)));
		
		this.transform = new AffineTransform();
		transform.translate(uix, uiy);
		transform.rotate(uiang);
		transform.scale(uiw/mini.getWidth(), uih/mini.getHeight());
	}
	
	Line2D.Double line = new Line2D.Double();
	Rectangle2D.Double knobRect = new Rectangle2D.Double();
	public void drawOutline(Graphics2D g, double pixelSize, boolean selected, boolean highlighted, FragmentKnob knob)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(selected ? Color.yellow : highlighted ? Color.orange : Color.black);
		line.setLine(uix, uiy, uix+ux, uiy+uy); g.draw(line);
		line.setLine(uix+ux, uiy+uy, uix+ux+vx, uiy+uy+vy); g.draw(line);
		line.setLine(uix+ux+vx, uiy+uy+vy, uix+vx, uiy+vy); g.draw(line);
		line.setLine(uix+vx, uiy+vy, uix, uiy); g.draw(line);
		
		if (highlighted || selected)
		{
			Color textColor = g.getColor();
			Font font = g.getFont();
			g.setFont(font.deriveFont((float)(font.getSize2D()/pixelSize)));
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(file.getName(), g);
			double sh = g.getFont().getSize2D();
			g.setColor(new Color(0f, 0f, 0f, .5f));
			g.fill(new Rectangle2D.Double(uix, uiy-sh, bounds.getWidth(), 1.5*sh));
			g.setColor(textColor);
			g.drawString(file.getName(), (float)uix, (float)uiy);
		}
		
		if (knob != null)
		{
			g.setColor(Color.red);
			double kx = uix+knob.ax*ux+knob.ay*vx;
			double ky = uiy+knob.ax*uy+knob.ay*vy;
			double r = FragmentViewMouseListener.knobRay;
			knobRect.setRect(kx-r/pixelSize, ky-r/pixelSize, 2*r/pixelSize, 2*r/pixelSize);
			g.draw(knobRect);
			
		}
	}
	public void drawImage(Graphics2D g)
	{
		g.drawImage(mini, transform, null);
	}
}
