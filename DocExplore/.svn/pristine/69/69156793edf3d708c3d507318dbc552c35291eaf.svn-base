package org.interreg.docexplore.stitch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.imgscalr.Scalr;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Math2D;
import org.interreg.docexplore.util.Pair;

@SuppressWarnings("serial")
public class StitchedTrans extends JFrame
{
	static int maxDim = 512;
	static class Image
	{
		String name;
		File file;
		BufferedImage mini;
		int w, h;
		double x, y;
		
		Image(File file, double x, double y) throws Exception
		{
			this.name = file.getName();
			this.file = file;
			BufferedImage big = ImageUtils.read(file);
			this.w = big.getWidth();
			this.h = big.getHeight();
			this.mini = Scalr.resize(big, maxDim);
			this.x = x;
			this.y = y;
		}
	}
	
	static double arrowAng = Math.PI/4;
	static double ca1 = Math.cos(arrowAng), sa1 = Math.sin(arrowAng);
	static double ca2 = Math.cos(-arrowAng), sa2 = Math.sin(-arrowAng);
	
	LinkedList<Image> images = new LinkedList<Image>();
	double w = 0, h = 0;
	StitchedImageViewer viewer;
	TriangulatedBox<double []> tbox = null;
	Set<double []> points = new HashSet<double []>();
	HashSet<double [][]> links = new HashSet<double [][]>();
	double [] hovered = null, selected = null;
	double mx0 = 0, my0 = 0;
	
	@SuppressWarnings("unchecked")
	public StitchedTrans() throws Exception
	{
		this.viewer = new StitchedImageViewer()
		{
			public void paintComponent(Graphics _g)
			{
				super.paintComponent(_g);
				Graphics2D g = (Graphics2D)_g;
				
				g.setColor(Color.red);
				for (double [] p : points)
				{
					double x = viewer.fromImageX(p[0]);
					double y = viewer.fromImageY(p[1]);
					g.draw(new Line2D.Double(x-5, y, x+5, y));
					g.draw(new Line2D.Double(x, y-5, x, y+5));
				}
				g.setColor(Color.blue);
				for (double [][] link : links)
				{
					double x1 = viewer.fromImageX(link[0][0]), y1 = viewer.fromImageY(link[0][1]);
					double x2 = viewer.fromImageX(link[1][0]), y2 = viewer.fromImageY(link[1][1]);
					g.draw(new Line2D.Double(x1, y1, x2, y2));
					double dirx = x1-x2, diry = y1-y2;
					double l = Math.sqrt(dirx*dirx+diry*diry);
					dirx /= l; diry /= l;
					double dx1 = ca1*dirx-sa1*diry, dy1 = sa1*dirx+ca1*diry;
					double dx2 = ca2*dirx-sa2*diry, dy2 = sa2*dirx+ca2*diry;
					g.draw(new Line2D.Double(x2, y2, x2+10*dx1, y2+10*dy1));
					g.draw(new Line2D.Double(x2, y2, x2+10*dx2, y2+10*dy2));
				}
				if (tbox != null)
				{
					g.setColor(Color.green);
					for (TriangulatedBox.Node<double []> [] triangle : tbox.triangles)
					{
						double x1 = viewer.fromImageX(triangle[0].p[0]), y1 = viewer.fromImageY(triangle[0].p[1]);
						double x2 = viewer.fromImageX(triangle[1].p[0]), y2 = viewer.fromImageY(triangle[1].p[1]);
						double x3 = viewer.fromImageX(triangle[2].p[0]), y3 = viewer.fromImageY(triangle[2].p[1]);
						g.draw(new Line2D.Double(x1, y1, x2, y2));
						g.draw(new Line2D.Double(x2, y2, x3, y3));
						g.draw(new Line2D.Double(x3, y3, x1, y1));
					}
				}
				if (hovered != null)
				{
					g.setColor(Color.orange);
					double x = viewer.fromImageX(hovered[0]);
					double y = viewer.fromImageY(hovered[1]);
					g.draw(new Rectangle2D.Double(x-5, y-5, 10, 10));
				}
				if (selected != null)
				{
					g.setColor(Color.green);
					double x = viewer.fromImageX(selected[0]);
					double y = viewer.fromImageY(selected[1]);
					g.draw(new Rectangle2D.Double(x-5, y-5, 10, 10));
				}
				
				g.setColor(Color.white);
				g.drawString(String.format("%.2f, %.2f", mx0, my0), 0, 20);
				double mx = viewer.fromImageX(mx0), my = viewer.fromImageY(my0);
				g.draw(new Line2D.Double(0, my, viewer.getWidth(), my));
				g.draw(new Line2D.Double(mx, 0, mx, viewer.getHeight()));
			}
		};
		viewer.setSt(this);
		viewer.setPreferredSize(new Dimension(1600, 800));
		add(viewer);
		pack();
		
		viewer.addListener(new StitchedImageViewer.Listener()
		{
			public void repaintRequested(StitchedImageViewer viewer)
			{
			}
			public void pointCtrlClicked(StitchedImageViewer viewer, double x, double y, boolean left)
			{
				if (left && selected != null && hovered != null && selected != hovered)
				{
					Set<double [][]> dels = new HashSet<double [][]>();
					for (double [][] link : links)
						if (link[0] == selected || link[1] == selected || link[0] == hovered || link[1] == hovered)
							dels.add(link);
					for (double [][] link : dels)
						links.remove(link);
					links.add(new double [][] {selected, hovered});
					writeTransData();
					updateTbox();
					repaint();
				}
			}
			public void pointClicked(StitchedImageViewer viewer, double x, double y, boolean left)
			{
				if (left)
				{
					if (hovered != null)
						selected = hovered;
					else points.add(new double [] {x, y});
				}
				else if (hovered != null)
				{
					points.remove(hovered);
					double [][] remLink = null;
					for (double [][] link : links)
						if (link[0] == hovered || link[1] == hovered)
							{remLink = link; break;}
					if (remLink != null)
						links.remove(remLink);
					if (selected == hovered)
						selected = null;
					hovered = null;
					writeTransData();
					updateTbox();
				}
				repaint();
			}
			public void mouseMoved(StitchedImageViewer viewer, double x, double y)
			{
				double mx = viewer.fromImageX(x), my = viewer.fromImageY(y);
				double [] newHovered = null;
				for (double [] p : points)
				{
					double px = viewer.fromImageX(p[0]), py = viewer.fromImageY(p[1]);
					if ((mx-px)*(mx-px)+(my-py)*(my-py) < 5*5)
						{newHovered = p; break;}
				}
				hovered = newHovered;
				
				mx0 = x;
				my0 = y;
				
//				if (tbox != null)
//				{
//					if (getTransPoint(x, y, buf1) == null)
//						System.out.println("oob");
//					else System.out.println((buf1[0]-x)+","+(buf1[1]-y));
//				}
				
				repaint();
			}
		});
		
		File data = new File("trans-data");
		if (data.exists())
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(data));
			this.links = (HashSet<double [][]>)in.readObject();
			for (double [][] link : links)
			{
				points.add(link[0]);
				points.add(link[1]);
			}
			in.close();
			updateTbox();
			repaint();
		}
	}
	
	void writeTransData()
	{
		try
		{
			File data = new File("trans-data");
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(data, false));
			out.writeObject(links);
			out.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	void updateTbox()
	{
		if (links.isEmpty())
		{
			this.tbox = null;
			return;
		}
		double [][] points = new double [links.size()+4][];
		double [][] trans = new double [links.size()+4][];
		points[0] = new double [] {0, 0}; trans[0] = new double [] {0, 0};
		points[1] = new double [] {w, 0}; trans[1] = new double [] {0, 0};
		points[2] = new double [] {w, h}; trans[2] = new double [] {0, 0};
		points[3] = new double [] {0, h}; trans[3] = new double [] {0, 0};
		int cnt = 4;
		for (double [][] link : links)
		{
			points[cnt] = new double [] {link[1][0], link[1][1]};
			trans[cnt] = new double [] {link[0][0]-link[1][0], link[0][1]-link[1][1]};
			cnt++;
		}
		this.tbox = new TriangulatedBox<double[]>(points, trans);
	}
	
	public void append(File file) throws Exception
	{
		images.add(new Image(file, w, 0));
		w = images.getLast().x+images.getLast().w;
		h = Math.max(h, images.getLast().h);
		viewer.x0 = .5*w;
		viewer.y0 = .5*h;
		viewer.zoom = viewer.getWidth()/w;
		updateTbox();
		repaint();
	}
	
	double [] buf1 = {0, 0}, buf3 = {0, 0}, buf4 = {0, 0};
	double [] getTransPoint(double x, double y, double [] res)
	{
		TriangulatedBox.Node<double []> [] tri = tbox.getTriangle(x, y, buf3);
		if (tri == null)
			return null;
		
		Math2D.set(buf4, 0, 0);
		double k = (1-buf3[0]-buf3[1]);
		buf4[0] += k*tri[0].t[0]; buf4[1] += k*tri[0].t[1];
		k = buf3[0];
		buf4[0] += k*tri[1].t[0]; buf4[1] += k*tri[1].t[1];
		k = buf3[1];
		buf4[0] += k*tri[2].t[0]; buf4[1] += k*tri[2].t[1];
		return Math2D.set(res, x+buf4[0], y+buf4[1]);
	}
	
	LinkedList<Pair<String, BufferedImage>> cache = new LinkedList<Pair<String, BufferedImage>>();
	int cacheLim = 4;
	BufferedImage getImage(Image image) throws Exception
	{
		String name = image.name;
		Pair<String, BufferedImage> found = null;
		for (Pair<String, BufferedImage> pair : cache)
			if (pair.first == name)
				{found = pair; break;}
		if (found == null)
			found = new Pair<String, BufferedImage>(image.name, ImageUtils.read(image.file));
		else cache.remove(found);
		cache.addFirst(found);
		while (cache.size() > cacheLim)
			cache.removeLast();
		return found.second;
	}
	
	Image getImage(double x, double y)
	{
		for (Image image : images)
			if (x < image.w)
				return image;
			else x -= image.w;
		return null;
	}
	
	float [] getCol(BufferedImage image, int i, int j, float [] col)
	{
		if (i < 0 || j < 0 || i >= image.getWidth() || j >= image.getHeight())
			col[0] = col[1] = col[2] = 0;
		else
		{
			int rgb = image.getRGB(i, j);
			col[0] = ImageUtils.red(rgb)/255f;
			col[1] = ImageUtils.green(rgb)/255f;
			col[2] = ImageUtils.blue(rgb)/255f;
		}
		return col;
	}
	float [] blc = {0, 0, 0}, brc = {0, 0, 0}, tlc = {0, 0, 0}, trc = {0, 0, 0};
	float [] getSample(double x, double y, BufferedImage image, float [] res)
	{
		//x *= image.getWidth(); y *= image.getHeight();
		x -= .5; y -= .5;
		int i0 = (int)x, j0 = (int)y;
		x -= i0; y -= j0;
		//if (x<0 || y<0 || x>1 || y>1)
		//	System.out.println(x+","+y);
		double bl = (1-x)*(1-y), br = x*(1-y), tl = (1-x)*y, tr = x*y;
		getCol(image, i0, j0, blc);
		getCol(image, i0+1, j0, brc);
		getCol(image, i0, j0+1, tlc);
		getCol(image, i0+1, j0+1, trc);
		for (int i=0;i<3;i++)
			res[i] = (float)((bl*blc[i]+br*brc[i]+tl*tlc[i]+tr*trc[i])/(bl+br+tl+tr));
		return res;
	}
	
	float [] sample = {0, 0, 0};
	public void buildTrans() throws Exception
	{
		int adv = 0;
		for (Image image : images)
		{
			BufferedImage out = new BufferedImage(image.w, image.h, BufferedImage.TYPE_3BYTE_BGR);
			for (int i=0;i<out.getWidth();i++)
			{
				for (int j=0;j<out.getHeight();j++)
				{
					if (getTransPoint(image.x+i+.5, image.y+j+.5, buf1) == null)
						continue;
					Image fimage = getImage(buf1[0], buf1[1]);
					if (fimage == null)
						continue;
					BufferedImage data = getImage(fimage);
					getSample(buf1[0]-fimage.x, buf1[1]-fimage.y, data, sample);
					out.setRGB(i, j, ImageUtils.rgb(
						Math.min(255,  Math.max(0, (int)(255*sample[0]))), 
						Math.min(255,  Math.max(0, (int)(255*sample[1]))), 
						Math.min(255,  Math.max(0, (int)(255*sample[2])))));
				}
				int nadv = (int)((image.x+i)*100./w);
				if (nadv != adv)
					System.out.println((adv = nadv)+"%");
			}
			ImageUtils.write(out, "PNG", new File(image.file.getParent(), "t"+image.file.getName()));
		}
	}
	
	public static void main(String [] args) throws Exception
	{
		File dir = new File("C:\\Users\\Alex\\Documents\\manuscrits\\Ms U 18 bis v1");
		Map<Integer, File> files = new TreeMap<Integer, File>();
		for (File file : dir.listFiles())
			if (file.getName().startsWith("out") && file.getName().endsWith(".png"))
				files.put(Integer.parseInt(file.getName().substring(3, file.getName().length()-4)), file);
		StitchedTrans st = new StitchedTrans();
		st.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		st.setVisible(true);
		for (File file : files.values())
		{
			st.append(file);
			System.out.println(file.getName());
		}
		st.buildTrans();
	}
}
