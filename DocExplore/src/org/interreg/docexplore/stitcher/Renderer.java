package org.interreg.docexplore.stitcher;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.util.ImageUtils;

public class Renderer
{
	static class RenderMetrics
	{
		double ppu;
		double minx, miny, maxx, maxy;
		int nw, nh, w, h, tilew, tileh;
		
		public void set(FragmentSet set, int imageSizeLim, double kppu)
		{
			double ppu = 0;
			double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE, maxx = -Double.MAX_VALUE, maxy = -Double.MAX_VALUE;
			for (int i=0;i<set.fragments.size();i++)
			{
				Fragment f = set.fragments.get(i);
				if (f.minx < minx) minx = f.minx;
				if (f.miny < miny) miny = f.miny;
				if (f.maxx > maxx) maxx = f.maxx;
				if (f.maxy > maxy) maxy = f.maxy;
				
				double fppu = f.imagew/f.uiw;
				if (fppu > ppu)
					ppu = fppu;
			}
			set(set, minx, miny, maxx, maxy, imageSizeLim, ppu, kppu);
		}
		public void set(FragmentSet set, double minx, double miny, double maxx, double maxy, int imageSizeLim, double kppu)
		{
			double ppu = 0;
			for (int i=0;i<set.fragments.size();i++)
			{
				Fragment f = set.fragments.get(i);
				double fppu = f.imagew/f.uiw;
				if (fppu > ppu)
					ppu = fppu;
			}
			set(set, minx, miny, maxx, maxy, imageSizeLim, ppu, kppu);
		}
		public void set(FragmentSet set, double minx, double miny, double maxx, double maxy, int imageSizeLim, double ppu, double kppu)
		{
			this.ppu = ppu*kppu;
			this.minx = minx;
			this.miny = miny;
			this.maxx = maxx;
			this.maxy = maxy;
			
			this.w = (int)(this.ppu*(maxx-minx));
			this.h = (int)(this.ppu*(maxy-miny));
			this.nw = w/imageSizeLim;
			this.nh = h/imageSizeLim;
			if (nw*imageSizeLim < w) nw++;
			if (nh*imageSizeLim < h) nh++;
			this.tilew = nw == 0 ? 0 : w/nw;
			this.tileh = nh == 0 ? 0 : h/nh;
		}
	}
	
	public static interface Listener
	{
		public void onRenderStarted(RenderMetrics metrics);
		public void onImageProgressed(int i, int j, float f);
		public void onImageRendered(int i, int j, BufferedImage image);
		public void onRenderEnded();
	}
	
	Map<Fragment, BufferedImage> images = new HashMap<Fragment, BufferedImage>();
	List<Fragment> recent = new ArrayList<Fragment>();
	
	List<Listener> listeners = new ArrayList<>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	
	private int getPixel(Fragment f, double x, double y)
	{
		BufferedImage img = images.get(f);
		if (img != null)
			recent.remove(f);
		else
		{
			try
			{
				img = Fragment.getFull(f.file, f.link);
				if (img != null)
					images.put(f, img);
			}
			catch (Exception e) {}
		}
		if (img == null)
			return 0;
		recent.add(f);
		while (images.size() > 8)
		{
			images.remove(recent.get(0));
			recent.remove(0);
		}
		int i = (int)x, j = (int)y;
		if (i < 0 || j < 0 || i >= img.getWidth() || j >= img.getHeight())
			return 0;
		return img.getRGB(i, j);
	}
	
	int imageSizeLim = 2048;
	public void render(FragmentSet set, String baseName, RenderMetrics metrics, float [] progress)
	{
		for (int k=0;k<listeners.size();k++)
			listeners.get(k).onRenderStarted(metrics);
		
		List<Fragment> list = new ArrayList<Fragment>();
		for (int i=0;i<metrics.nw;i++)
			for (int j=0;j<metrics.nh;j++)
		{
			BufferedImage img = new BufferedImage(metrics.tilew, metrics.tileh, BufferedImage.TYPE_3BYTE_BGR);
			for (int si=0;si<metrics.tilew;si++)
			{
				if (si%10 == 0)
					for (int k=0;k<listeners.size();k++)
						listeners.get(k).onImageProgressed(i, j, si*1f/metrics.tilew);
				for (int sj=0;sj<metrics.tileh;sj++)
				{
					progress[0] = (i*metrics.nh+j+si*1f/metrics.tilew)*1f/(metrics.nw*metrics.nh);
					double vx = metrics.minx+(si+i*metrics.tilew)*(metrics.maxx-metrics.minx)/metrics.w;
					double vy = metrics.miny+(sj+j*metrics.tileh)*(metrics.maxy-metrics.miny)/metrics.h;
					double r = 0, g = 0, b = 0;
					set.fragmentsAt(vx, vy, 0, list);
					double n = 0;
					for (int k=0;k<list.size();k++)
					{
						Fragment f = list.get(k);
						double lx = f.toLocalX(vx, vy), ly = f.toLocalY(vx, vy);
						double ix = f.fromLocalToImageX(lx), iy = f.fromLocalToImageY(ly);
						double dx = ix+(f.distortion == null ? 0 : f.distortion.getDist(ix, iy, 0)), 
							dy = iy+(f.distortion == null ? 0 : f.distortion.getDist(ix, iy, 1));
						int rgb = getPixel(f, dx, dy);
						if (rgb != 0)
						{
							float a = (float)Math.min(Math.min(ix,  f.imagew-ix), Math.min(iy,  f.imageh-iy));
							a *= a;
							{r += a*ImageUtils.red(rgb); g += a*ImageUtils.green(rgb); b += a*ImageUtils.blue(rgb); n += a;}
						}
					}
					list.clear();
					if (n > 0)
						img.setRGB(si, sj, ImageUtils.rgba((int)(r/n), (int)(g/n), (int)(b/n), 255));
				}
			}
			for (int k=0;k<listeners.size();k++)
				listeners.get(k).onImageRendered(i, j, img);
		}
		
		for (int k=0;k<listeners.size();k++)
			listeners.get(k).onRenderEnded();
	}
}
