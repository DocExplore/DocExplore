package org.interreg.docexplore.stitcher;

import java.awt.AlphaComposite;
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
import java.util.ArrayList;
import java.util.List;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.util.ImageUtils;

public class Fragment
{
	static int serialVersion = 1;
	static int miniSize = 128;
	
	String file;
	DocExploreDataLink link;
	
	final int imagew, imageh;
	BufferedImage mini;
	double uix, uiy;
	double uiw;
	double uiang;
	List<POI> features = new ArrayList<POI>(0);
	int index;
	
	BufferedImage full = null;
	
	AffineTransform transform;
	double uih, ux, uy, vx, vy;
	double minx, miny, maxx, maxy;
	
	FragmentDistortion distortion = null;
	
	static BufferedImage getFull(String file, DocExploreDataLink link) throws Exception
	{
		BufferedImage img = null;
		if (file.startsWith("docex://"))
			img = link.getMetaData(Integer.parseInt(file.substring(9))).getImage();
		else img = ImageUtils.read(new File(file));
		return img;
	}
	static String getName(String file)
	{
		if (file.startsWith("docex://"))
			return file.substring(8);
		return new File(file).getName();
	}
	
	public Fragment(String file, DocExploreDataLink link, int index, FeatureDetector detector) throws Exception
	{
		BufferedImage img = getFull(file, link);
		if (img == null)
			throw new NullPointerException();
		
		this.file = file;
		this.link = link;
		this.imagew = img.getWidth();
		this.imageh = img.getHeight();
		this.mini = ImageUtils.createIconSizeImage(img, miniSize);
		this.uix = 0; this.uiy = 0;
		this.uiw = 1;
		this.uiang = 0;
		this.features = detector.computeFeatures(this, img);
		this.index = index;
		
		update();
	}
	
	public Fragment(ObjectInputStream in, DocExploreDataLink link, int index) throws Exception
	{
		this.link = link;
		int serialVersion = in.readInt();
		this.file = serialVersion < 1 ? ((File)in.readObject()).getAbsolutePath() : in.readUTF();
		this.imagew = in.readInt();
		this.imageh = in.readInt();
		ByteArrayInputStream imgIn = new ByteArrayInputStream((byte [])in.readObject());
		this.mini = ImageUtils.read(imgIn);
		imgIn.close();
		this.uix = in.readDouble(); this.uiy = in.readDouble();
		this.uiw = in.readDouble();
		this.uiang = in.readDouble();
		int n = in.readInt();
		this.features = new ArrayList<POI>(n);
		for (int i=0;i<n;i++)
			features.add(POI.read(in, this, i));
		this.distortion = (FragmentDistortion)in.readObject();
		
		this.index = index;
		update();
	}
	
	public int index() {return index;}
	
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeUTF(file);
		out.writeInt(imagew);
		out.writeInt(imageh);
		ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
		ImageUtils.write(mini, "PNG", imgOut);
		out.writeObject(imgOut.toByteArray());
		imgOut.close();
		out.writeDouble(uix); out.writeDouble(uiy);
		out.writeDouble(uiw);
		out.writeDouble(uiang);
		out.writeInt(features.size());
		for (int i=0;i<features.size();i++)
			features.get(i).write(out);
		out.writeObject(distortion);
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
	public void toggleFull()
	{
		if (full != null)
			full = null;
		else try {full = getFull(file, link);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		update();
	}
	
	double fromImageToLocalX(double x) {return x/imagew;}
	double fromImageToLocalY(double y) {return y/imageh;}
	double fromLocalToImageX(double x) {return x*imagew;}
	double fromLocalToImageY(double y) {return y*imageh;}
	public double toLocalX(double x, double y) {return ((x-uix)*ux+(y-uiy)*uy)/(ux*ux+uy*uy);}
	public double toLocalY(double x, double y) {return ((x-uix)*vx+(y-uiy)*vy)/(vx*vx+vy*vy);}
	public double fromLocalX(double x, double y) {return uix+x*ux+y*vx;}
	public double fromLocalY(double x, double y) {return uiy+x*uy+y*vy;}
	public boolean contains(double x, double y) {return contains(x, y, 0);}
	public boolean contains(double x, double y, double r)
	{
		if (x < minx-r || x > maxx+r || y < miny-r || y > maxy+r)
			return false;
		double rw = r/uiw, rh = r/uih;
		double lx = toLocalX(x, y);
		if (lx < -rw || lx > 1+rw) return false;
		double ly = toLocalY(x, y);
		if (ly < -rh || ly > 1+rh) return false;
		return true;
	}
	
	public int nearEdge(double x, double y, double r)
	{
		if (x < minx-r || x > maxx+r || y < miny-r || y > maxy+r)
			return -1;
		double rw = r/uiw, rh = r/uih;
		double lx = toLocalX(x, y);
		if (lx > -rw && lx < rw) return 0;
		if (lx > 1-rw && lx < 1+rw) return 2;
		double ly = toLocalY(x, y);
		if (ly > -rh && ly < rh) return 1;
		if (ly > 1-rh && ly < 1+rh) return 3;
		return -1;
	}
	public double toLocalEdgeX(double x, double y, int edge)
	{
		if (edge == 1 || edge == 3) return toLocalX(x, y);
		return edge == 0 ? 0 : 1;
	}
	public double toLocalEdgeY(double x, double y, int edge)
	{
		if (edge == 0 || edge == 2) return toLocalY(x, y);
		return edge == 1 ? 0 : 1;
	}
	public double [] edgePoint(double x, double y, int edge, double [] res)
	{
		double lx = toLocalEdgeX(x, y, edge), ly = toLocalEdgeY(x, y, edge);
		res[0] = fromLocalX(lx, ly);
		res[1] = fromLocalY(lx, ly);
		return res;
	}
	public double [] nearCornerPoint(int edge, double [] res)
	{
		if (edge == 0) {res[0] = uix+vx; res[1] = uiy+vy;}
		if (edge == 1) {res[0] = uix; res[1] = uiy;}
		if (edge == 2) {res[0] = uix+ux; res[1] = uiy+uy;}
		if (edge == 3) {res[0] = uix+ux+vx; res[1] = uiy+uy+vy;}
		return res;
	}
	public double [] farCornerPoint(int edge, double [] res)
	{
		if (edge == 3) {res[0] = uix+vx; res[1] = uiy+vy;}
		if (edge == 0) {res[0] = uix; res[1] = uiy;}
		if (edge == 1) {res[0] = uix+ux; res[1] = uiy+uy;}
		if (edge == 2) {res[0] = uix+ux+vx; res[1] = uiy+uy+vy;}
		return res;
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
		BufferedImage img = full != null ? full : mini;
		transform.scale(uiw/img.getWidth(), uih/img.getHeight());
	}
	
	public Rectangle2D overlap(Fragment f)
	{
		double minx, miny, maxx, maxy;
		double x = minx = maxx = toLocalX(f.uix, f.uiy);
		double y = miny = maxy = toLocalY(f.uix, f.uiy);
		for (int i=0;i<3;i++)
		{
			double fx = 0, fy = 0;
			switch (i)
			{
				case 0: fx = f.uix+f.ux; fy = f.uiy+f.uy; break;
				case 1: fx = f.uix+f.ux+f.vx; fy = f.uiy+f.uy+f.vy; break;
				case 2: fx = f.uix+f.vx; fy = f.uiy+f.vy; break;
			}
			x = toLocalX(fx, fy);
			y = toLocalY(fx, fy);
			minx = Math.min(minx, x);
			miny = Math.min(miny, y);
			maxx = Math.max(maxx, x);
			maxy = Math.max(maxy, y);
		}
		minx = Math.max(0, Math.min(1, minx));
		miny = Math.max(0, Math.min(1, miny));
		maxx = Math.max(0, Math.min(1, maxx));
		maxy = Math.max(0, Math.min(1, maxy));
		if ((maxx-minx)*(maxy-miny) == 0)
			return new Rectangle2D.Double(0, 0, 1, 1);
		double w = maxx-minx, h = maxy-miny;
		if (w > h)
		{
			return new Rectangle2D.Double(0, miny, 1, h);
		}
		else
		{
			return new Rectangle2D.Double(minx, 0, w, 1);
		}
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
			g.setFont(font.deriveFont((float)(16f/pixelSize)));
			String name = getName(file);
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(name, g);
			double sh = g.getFont().getSize2D();
			g.setColor(new Color(0f, 0f, 0f, .5f));
			g.fill(new Rectangle2D.Double(uix, uiy-sh, bounds.getWidth(), 1.5*sh));
			g.setColor(textColor);
			g.drawString(name, (float)uix, (float)uiy);
		}
		
		if (knob != null)
		{
			g.setColor(Color.red);
			double kx = uix+knob.ax*ux+knob.ay*vx;
			double ky = uiy+knob.ax*uy+knob.ay*vy;
			double r = FragmentViewInputListener.knobRay;
			knobRect.setRect(kx-r/pixelSize, ky-r/pixelSize, 2*r/pixelSize, 2*r/pixelSize);
			g.draw(knobRect);
			
		}
	}
	public void drawEdge(Graphics2D g, double pixelSize, int edge)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.blue);
		if (edge == 1) {line.setLine(uix, uiy, uix+ux, uiy+uy); g.draw(line);}
		if (edge == 2) {line.setLine(uix+ux, uiy+uy, uix+ux+vx, uiy+uy+vy); g.draw(line);}
		if (edge == 3) {line.setLine(uix+ux+vx, uiy+uy+vy, uix+vx, uiy+vy); g.draw(line);}
		if (edge == 0) {line.setLine(uix+vx, uiy+vy, uix, uiy); g.draw(line);}
	}
	float alpha = 1;
	public void drawImage(Graphics2D g)
	{
		g.setComposite(AlphaComposite.SrcOver.derive(alpha));
		g.drawImage(full != null ? full : mini, transform, null);
	}
}
