package org.interreg.docexplore.stitch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Pair;
import org.interreg.docexplore.util.Smooth;

@SuppressWarnings("serial")
public class Stitcher extends JPanel
{
	StitchSequencer sequencer;
	ImageBox [] boxes;
	TransBox [] transBoxes;
	File [] files;
	double minx, miny, maxx, maxy;
	List<Set<KeyPoint> []> keyPoints;
	Smooth spine;
	
	boolean dragging = false;
	double x0 = 0, y0 = 0, zoom = 1;
	public Stitcher(final StitchSequencer sequencer, List<StitchSequencer.ImageFile> files, List<Set<KeyPoint> []> keyPoints)
	{
		this.sequencer = sequencer;
		this.boxes = new ImageBox [keyPoints.size()];
		this.transBoxes = new TransBox [keyPoints.size()];
		this.files = new File [keyPoints.size()];
		this.keyPoints = keyPoints;
		
		for (int i=0;i<boxes.length;i++)
		{
			boxes[i] = new ImageBox(files.get(i).w, files.get(i).h, sequencer.spine.get(i));
			this.files[i] = files.get(i).file;
			if (i > 0)
			{
				double [][] stitches = findFurthestStitches(keyPoints.get(i-1)[0]);
				double [] to1 = {stitches[0][0], stitches[0][1]};
				double [] p1 = {stitches[1][0], stitches[1][1]};
				double [] to2 = {stitches[2][0], stitches[2][1]};
				double [] p2 = {stitches[3][0], stitches[3][1]};
				boxes[i].align(p1, p2, boxes[i-1], to1, to2);
			}
		}
		double dx = boxes[boxes.length-1].p[0]-boxes[0].p[0];
		double dy = boxes[boxes.length-1].p[1]-boxes[0].p[1];
		double a = (dy > 0 ? 1 : -1)*Math.acos(dx/Math.sqrt(dx*dx+dy*dy));
		double da = 0;
		for (int i=-2;i<=2;i++)
			if (Math.abs(-Math.PI+.5*i*Math.PI-a) < .25*Math.PI)
				{da = -Math.PI+.5*i*Math.PI-a; break;}
		
		if (da != 0)
		{
			for (int i=0;i<boxes.length;i++)
			{
				boxes[i] = new ImageBox(files.get(i).w, files.get(i).h, sequencer.spine.get(i));
				if (i == 0)
					boxes[i].rotate(da);
				this.files[i] = files.get(i).file;
				if (i > 0)
				{
					double [][] stitches = findFurthestStitches(keyPoints.get(i-1)[0]);
					double [] to1 = {stitches[0][0], stitches[0][1]};
					double [] p1 = {stitches[1][0], stitches[1][1]};
					double [] to2 = {stitches[2][0], stitches[2][1]};
					double [] p2 = {stitches[3][0], stitches[3][1]};
					boxes[i].align(p1, p2, boxes[i-1], to1, to2);
				}
				double [] bounds = boxes[i].bounds();
				if (i == 0 || bounds[0] < minx) minx = bounds[0];
				if (i == 0 || bounds[1] < miny) miny = bounds[1];
				if (i == 0 || bounds[2] > maxx) maxx = bounds[2];
				if (i == 0 || bounds[3] > maxy) maxy = bounds[3];
			}
		}
		
		for (int i=0;i<boxes.length;i++)
		{
			List<double [][]> stitches = buildStitchList(i);
			transBoxes[i] = new TransBox(boxes[i], stitches);
		}
		
		setPreferredSize(new Dimension(640, 480));
		setSize(640, 480);
		addMouseMotionListener(new MouseMotionAdapter()
		{
			int lastx = 0, lasty = 0;
			public void mouseDragged(MouseEvent e)
			{
				if (!dragging)
					dragging = true;
				else
				{
					x0 -= (e.getX()-lastx);
					y0 -= (e.getY()-lasty);
				}
				lastx = e.getX();
				lasty = e.getY();
				repaint();
			}
		});
		addMouseWheelListener(new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				int i = -e.getWheelRotation();
				while (i > 0)
					{zoom *= 1.1; x0 *= 1.1; y0 *= 1.1; i--;}
				while (i < 0)
					{zoom /= 1.1; x0 /= 1.1; y0 /= 1.1; i++;}
				repaint();
			}
		});
		addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e)
			{
				dragging = false;
			}
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON1)
					return;
				double x = (x0+e.getX())/zoom;
				double y = (y0+e.getY())/zoom;
				int index = getBoxAt(x, y);
				if (index <= 0)
					return;
				sequencer.backupX0 = x0;
				sequencer.backupY0 = y0;
				sequencer.backupZoom = zoom;
				sequencer.currentStitch = index;
				sequencer.setFiles();
				sequencer.fileList.repaint();
				((JDialog)getTopLevelAncestor()).setVisible(false);
			}
		});
		fitView();
		
		int nSpinePoints = 0;
//		for (int i=0;i<sequencer.spine.size();i++)
//			if (sequencer.spine.get(i) != null)
//				nSpinePoints++;
		if (nSpinePoints > 1)
		{
			double [][] spinePoints = new double [nSpinePoints][3];
			int cnt = 0;
			for (int i=0;i<sequencer.spine.size();i++)
				if (sequencer.spine.get(i) != null)
			{
				ImageBox box = boxes[i];
				box.toWorld(box.spine, buf1);
				spinePoints[cnt][0] = buf1[0];
				spinePoints[cnt][1] = buf1[1];
				spinePoints[cnt][2] = 0;
				cnt++;
			}
			this.spine = new Smooth(spinePoints);
		}
		else this.spine = null;
	}
	
	public void fitView()
	{
		zoom = Math.min(getWidth()/(maxx-minx), getHeight()/(maxy-miny));
		x0 = zoom*minx;
		y0 = zoom*miny;
		repaint();
	}
	
	List<double [][]> buildStitchList(int fileIndex)
	{
		List<double [][]> res = new Vector<double [][]>();
		if (fileIndex > 0)
			for (KeyPoint p : keyPoints.get(fileIndex-1)[0])
				if (p.link != null)
		{
			boxes[fileIndex-1].toWorld(p.x, p.y, buf1);
			boxes[fileIndex].fromWorld(buf1, buf2);
			res.add(new double [][] {{buf2[0], buf2[1]}, {p.link.x, p.link.y}});
		}
		return res;
	}
	
	double [][] findFurthestStitches(Set<KeyPoint> keyPoints)
	{
		KeyPoint a = null, b = null;
		double max = 0;
		for (KeyPoint p1 : keyPoints)
			for (KeyPoint p2 : keyPoints)
				if (p1 != p2 && p1.link != null && p2.link != null && (a == null || dist2(p1, p2) > max))
					{a = p1; b = p2; max = dist2(p1, p2);}
		return new double [][] {{a.x, a.y}, {a.link.x, a.link.y}, {b.x, b.y}, {b.link.x, b.link.y}};
	}
	double dist2(KeyPoint p1, KeyPoint p2) {return (p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y);}
	
	public void merge(boolean write) throws Exception
	{
		int w = (int)(maxx-minx);
		int h = (int)(maxy-miny);
		int rows = 0, cols = 0;
		if (w < h) {cols = 1; rows = h/w+1;}
		else {rows = 1; cols = w/h+1;}
		
		previewZoom = Math.min(getWidth()/(maxx-minx), getHeight()/(maxy-miny));
		preview = new BufferedImage((int)(w*previewZoom), (int)(h*previewZoom), BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = preview.createGraphics();
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		
		int cnt = 0;
		for (int i=0;i<cols;i++)
			for (int j=0;j<rows;j++)
		{
			px0 = i*w/cols; py0 = j*h/rows;
			px1 = (i+1)*w/cols; py1 = (j+1)*h/rows;
			BufferedImage image = buildImage((int)minx+px0, (int)miny+py0, px1-px0, py1-py0);
			g.drawImage(image, (int)(px0*previewZoom), (int)(py0*previewZoom), (int)(px1*previewZoom), (int)(py1*previewZoom), 0, 0, px1-px0, py1-py0, null);
			g.setColor(Color.green);
			g.drawRect((int)(px0*previewZoom), (int)(py0*previewZoom), (int)((px1-px0)*previewZoom)-1, (int)((py1-py0)*previewZoom)-1);
			g.drawString("out"+cnt+".png", (int)(px0*previewZoom)+10, (int)(py0*previewZoom)+10);
			if (write)
				ImageUtils.write(image, "PNG", new File("out"+(cnt++)+".png"));
		}
	}
	int px0 = 0, py0 = 0, px1 = 0, py1 = 0;
	BufferedImage preview = null;
	double previewZoom = 1;
	List<Pair<TransBox, BufferedImage>> nearBoxes = new LinkedList<Pair<TransBox, BufferedImage>>();
	BufferedImage buildImage(final int x0, final int y0, int w, int h) throws Exception
	{
		final BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		
		List<Pair<TransBox, BufferedImage>> nextNearBoxes = new LinkedList<Pair<TransBox, BufferedImage>>();
		for (int i=0;i<boxes.length;i++)
		{
			TransBox box = transBoxes[i];
			double [] bounds = box.box.bounds();
			if (bounds[0] > x0+w || bounds[1] > y0+h || bounds[2] < x0 || bounds[3] < y0)
				continue;
			
			BufferedImage image = null;
			for (Pair<TransBox, BufferedImage> pair : nearBoxes)
				if (pair.first == box)
					{image = pair.second; break;}
			if (image == null)
				image = ImageIO.read(files[i]);
			nextNearBoxes.add(new Pair<TransBox, BufferedImage>(box, image));
		}
		nearBoxes.clear();
		nearBoxes = nextNearBoxes;
		
		int sw = 1, sh = 1;
		final AtomicInteger [] count = {new AtomicInteger(sw*sh)};
		for (int sx=0;sx<sw;sx++)
			for (int sy=0;sy<sh;sy++)
		{
			final int i0 = sx*w/sw, i1 = (sx+1)*w/sw;
			final int j0 = sy*h/sh, j1 = (sy+1)*h/sh;
			
			new Thread() {public void run()
			{
				float [] col = {0, 0, 0}, colbuf = {0, 0, 0};
				double [] pos = {0, 0};//, sbuf = {0, 0, 0};
				for (int i=i0;i<i1;i++)
				{
					for (int j=j0;j<j1;j++)
					{
						col[0] = col[1] = col[2] = 0;
						double sum = 0;
						
						for (Pair<TransBox, BufferedImage> pair : nearBoxes)
						{
							pair.first.box.fromWorld(x0+i+.5, y0+j+.5, pos);
							pair.first.getTransPoint(pos[0], pos[1], pos);
							if (pos[0] < 0 || pos[1] < 0 || pos[0] > pair.first.box.w || pos[1] > pair.first.box.h)
								continue;
							pair.first.box.getSample(pos[0], pos[1], pair.second, colbuf);
							double k = Math.min(pos[0], Math.min(pos[1], Math.min(pair.first.box.w-pos[0], pair.first.box.h-pos[1])));
							col[0] += k*colbuf[0]; col[1] += k*colbuf[1]; col[2] += k*colbuf[2];
							sum += k;
						}
						
						if (sum > 0)
							{col[0] /= sum; col[1] /= sum; col[2] /= sum;}
						
						int dj = 0;
//						if (spine != null && x0+i+.5 > spine.points[0][0] && x0+i+.5 < spine.points[spine.points.length-1][0])
//						{
//							double k = (x0+i+.5-spine.points[0][0])/(spine.points[spine.points.length-1][0]-spine.points[0][0]);
//							spine.at(k, sbuf);
//							dj = (int)(sbuf[1]-(spine.points[0][1]+k*(spine.points[spine.points.length-1][1]-spine.points[0][1])));
//						}
						
						if (j-dj >= 0 && j-dj < res.getHeight())
							res.setRGB(i, j-dj, ImageUtils.rgb(
								Math.min(255,  Math.max(0, (int)(255*col[0]))), 
								Math.min(255,  Math.max(0, (int)(255*col[1]))), 
								Math.min(255,  Math.max(0, (int)(255*col[2])))));
					}
				}
				count[0].decrementAndGet();
			}}.start();
		}
		
		int pcnt = 0;
		while (count[0].get() > 0)
		{
			try {Thread.sleep(100);} catch (Exception e) {}
			if (pcnt%5 == 0)
				preview.createGraphics().drawImage(res, (int)(px0*previewZoom), (int)(py0*previewZoom), (int)(px1*previewZoom), (int)(py1*previewZoom), 0, 0, px1-px0, py1-py0, null);
			pcnt++;
		}
		
		return res;
	}
	
	public int getBoxAt(double x, double y)
	{
		double [] at = {x, y};
		double [] pos = {0, 0, 0};
		for (int i=0;i<transBoxes.length;i++)
		{
			transBoxes[i].box.fromWorld(at, pos);
			if (pos[0] >= 0 && pos[1] >= 0 && pos[0] < transBoxes[i].box.w && pos[1] <= transBoxes[i].box.h)
				return i;
		}
		return -1;
	}
	
	double [] buf1 = {0, 0}, buf2 = {0, 0}, buf3 = {0, 0}, buf4 = {0, 0, 0};
	public void paintComponent(Graphics _g)
	{
		Graphics2D g = (Graphics2D)_g;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (preview == null)
		{
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		else g.drawImage(preview, 0, 0, null);
		
		//g.setStroke(new BasicStroke((float)(1/zoom)));
		for (int i=0;i<boxes.length;i++)
		{
			ImageBox box = boxes[i];
			
			g.setColor(Color.blue);
			for (TriangulatedBox.Node<TransBox.TransNode> [] tri : transBoxes[i].triBox.triangles)
			{
				box.toWorld(tri[0].p, buf1);
				box.toWorld(tri[1].p, buf2);
				box.toWorld(tri[2].p, buf3);
				((Graphics2D)g).draw(new Line2D.Double(buf1[0]*zoom-x0, buf1[1]*zoom-y0, buf2[0]*zoom-x0, buf2[1]*zoom-y0));
				((Graphics2D)g).draw(new Line2D.Double(buf3[0]*zoom-x0, buf3[1]*zoom-y0, buf2[0]*zoom-x0, buf2[1]*zoom-y0));
				((Graphics2D)g).draw(new Line2D.Double(buf1[0]*zoom-x0, buf1[1]*zoom-y0, buf3[0]*zoom-x0, buf3[1]*zoom-y0));
			}
			
			g.setColor(Color.red);
			for (TriangulatedBox.Node<TransBox.TransNode> node : transBoxes[i].triBox.nodes)
			{
				box.toWorld(node.p, buf1);
				box.toWorld(node.p[0]+node.t.v[0], node.p[1]+node.t.v[1], buf2);
				((Graphics2D)g).draw(new Line2D.Double(buf1[0]*zoom-x0, buf1[1]*zoom-y0, buf2[0]*zoom-x0, buf2[1]*zoom-y0));
			}
			
			box.render(g, x0, y0, zoom);
		}
		
		g.setColor(Color.magenta);
		double oldx = 0, oldy = 0;
		boolean started = false;
		if (spine != null)
		{
//			for (int i=0;i<boxes.length;i++)
//			{
//				ImageBox box = boxes[i];
//				if (box.spine == null)
//					continue;
//				box.toWorld(box.spine, buf1);
			int nSteps = 200;
			for (int i=0;i<nSteps;i++)
			{
				spine.at(i*1./(nSteps-1), buf4);
				double sx = buf4[0]*zoom-x0, sy = buf4[1]*zoom-y0;
				if (started)
					((Graphics2D)g).draw(new Line2D.Double(oldx, oldy, sx, sy));
				started = true;
				oldx = sx;
				oldy = sy;
			}
		}
		
		int w = (int)(maxx-minx);
		int h = (int)(maxy-miny);
		int rows = 0, cols = 0;
		if (w < h) {cols = 1; rows = h/w+1;}
		else {rows = 1; cols = w/h+1;}
		
		int px0 = 0, px1 = 0;
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		int cnt = 0;
		for (int i=0;i<cols;i++)
			for (int j=0;j<rows;j++)
		{
			px0 = i*w/cols; py0 = j*h/rows;
			px1 = (i+1)*w/cols; py1 = (j+1)*h/rows;
			g.setColor(Color.green);
			g.drawRect((int)(px0*zoom-x0), (int)(py0*zoom-y0), (int)((px1-px0)*zoom)-1, (int)((py1-py0)*zoom)-1);
			g.drawString("out"+(cnt++)+".png", (int)(px0*zoom-x0)+10, (int)(py0*zoom-y0)+10);
		}
	}
}
