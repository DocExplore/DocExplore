package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.interreg.docexplore.gui.image.NavView;
import org.interreg.docexplore.gui.image.NavViewInputListener;

@SuppressWarnings("serial")
public class RenderView extends NavView
{
	RenderEditor editor;
	double minx = 0, maxx = 0, miny = 1, maxy = 1;
	int highlighted = -1;
	
	public RenderView(RenderEditor editor)
	{
		this.editor = editor;
		
		requestFocusInWindow();
		setPreferredSize(new Dimension(800, 600));
		//addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e) {if (minx == maxx || miny == maxy) {fitBounds();}}});
		fitBounds();
	}
	@Override protected NavViewInputListener createInputListener() {return new RenderViewInputListener(this);}
	
	int serialVersion = 0;
	public void write(ObjectOutputStream out) throws Exception
	{
		super.write(out);
		out.writeInt(serialVersion);
	}
	
	public void read(ObjectInputStream in) throws Exception
	{
		super.read(in);
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		repaint();
	}
	
	public void fitView(double margin) {fitView(null, margin);}
	public void fitView(Collection<Fragment> fragments, double margin)
	{
		if (fragments == null)
			fragments = editor.fragmentView.set.fragments;
		double maxx = Math.max(minx+.001, this.maxx);
		double maxy = Math.max(miny+.001, this.maxy);
		fitView(minx, miny, maxx, maxy, margin);
	}
	
	public void fitBounds()
	{
		boolean first = true;
		for (Fragment f : editor.fragmentView.set.fragments)
		{
			if (first || f.minx < minx) minx = f.minx;
			if (first || f.maxx > maxx) maxx = f.maxx;
			if (first || f.miny < miny) miny = f.miny;
			if (first || f.maxy > maxy) maxy = f.maxy;
			first = false;
		}
		repaint();
	}
	
	Color clipColor = new Color(0f, 0f, 0f, .25f), renderingColor = new Color(0f, 0, 0, .5f);
	Rectangle2D.Double rect = new Rectangle2D.Double();
	Line2D.Double line = new Line2D.Double();
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		for (int i=0;i<editor.fragmentView.set.fragments.size();i++)
			editor.fragmentView.set.fragments.get(i).drawImage(g);
		
		double left = toViewX(0), top = toViewY(0), right = toViewX(getWidth()), bottom = toViewY(getHeight());
		g.setColor(clipColor);
		rect.setFrame(left, top, minx-left, bottom-top); g.fill(rect);
		rect.setFrame(minx, top, maxx-minx, miny-top); g.fill(rect);
		rect.setFrame(maxx, top, right-maxx, bottom-top); g.fill(rect);
		rect.setFrame(minx, maxy, maxx-minx, bottom-maxy); g.fill(rect);
		
		g.setColor(Color.gray);
		line.setLine(minx, top, minx, bottom); g.draw(line);
		line.setLine(left, miny, right, miny); g.draw(line);
		line.setLine(maxx, top, maxx, bottom); g.draw(line);
		line.setLine(left, maxy, right, maxy); g.draw(line);
		
		g.setColor(Color.red);
		for (int i=0;i<editor.metrics.nw-1;i++)
		{
			double x0 = minx+(maxx-minx)*(i+1)/editor.metrics.nw;
			line.setLine(x0, miny, x0, maxy);
			g.draw(line);
		}
		for (int j=0;j<editor.metrics.nh-1;j++)
		{
			double y0 = miny+(maxy-miny)*(j+1)/editor.metrics.nh;
			line.setLine(minx, y0, maxx, y0);
			g.draw(line);
		}
		
		g.setColor(Color.orange);
		if ((highlighted & 1) > 0) {line.setLine(minx, top, minx, bottom); g.draw(line);}
		if ((highlighted & 2) > 0) {line.setLine(left, miny, right, miny); g.draw(line);}
		if ((highlighted & 4) > 0) {line.setLine(maxx, top, maxx, bottom); g.draw(line);}
		if ((highlighted & 8) > 0) {line.setLine(left, maxy, right, maxy); g.draw(line);}
		
		if (editor.rendering)
		{
			double fw = maxx-minx, fh = maxy-miny;
			for (int i=0;i<editor.metrics.nw;i++)
				for (int j=0;j<editor.metrics.nh;j++)
					if (editor.rendered[i][j] < 1)
					{
						g.setColor(renderingColor);
						double x0 = minx+i*fw/editor.metrics.nw, x1 = minx+(i+1)*fw/editor.metrics.nw,
							y0 = miny+j*fh/editor.metrics.nh, y1 = miny+(j+1)*fh/editor.metrics.nh;
						double k = editor.rendered[i][j];
						rect.setFrame(x0+k*(x1-x0), y0, x1-x0-k*(x1-x0), y1-y0);
						g.fill(rect);
					}
		}
	}
}
