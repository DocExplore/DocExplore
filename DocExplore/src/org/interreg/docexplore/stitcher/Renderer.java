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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	
	Comparator<Fragment> lowestIndex = new Comparator<Fragment>() {@Override public int compare(Fragment o1, Fragment o2) {return o1.index-o2.index;}};
	int imageSizeLim = 2048;
	int blendRay = 40;
	public void render(FragmentSet set, String baseName, RenderMetrics metrics, float [] progress)
	{
		for (int k=0;k<listeners.size();k++)
			listeners.get(k).onRenderStarted(metrics);
		
		FragmentDistortion [] distortions = new FragmentDistortion [set.fragments.size()];
		for (int i=0;i<set.fragments.size();i++)
			distortions[i] = new FragmentDistortion(set, set.fragments.get(i), distortions);
		
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
					double n = 0;
					set.fragmentsAt(vx, vy, 0, list);
					Collections.sort(list, lowestIndex);
					for (int k=0;k<list.size();k++)
					{
						Fragment f = list.get(k);
						double lx = f.toLocalX(vx, vy), ly = f.toLocalY(vx, vy);
						double ix = f.fromLocalToImageX(lx), iy = f.fromLocalToImageY(ly);
						double dx = distortions[f.index].getDist(ix, iy, 0), dy = distortions[f.index].getDist(ix, iy, 1);
						ix += dx;
						iy += dy;
						int rgb = getPixel(f, ix, iy);
						//img.setRGB(si, sj, rgb);
						if (rgb != 0)
						{
							double a = Math.min(Math.min(Math.min(ix, f.imagew-ix), iy), f.imageh-iy)/blendRay;
							if (a <= 0)
								continue;
							a = Math.min(1-n, a);
							r += a*ImageUtils.red(rgb); 
							g += a*ImageUtils.green(rgb); 
							b += a*ImageUtils.blue(rgb); 
							n = Math.min(1, n+a);
							if (n == 1)
								break;
						}
					}
					
//					for (int k=0;k<list.size();k++)
//					{
//						Fragment f = list.get(k);
//						double lx = f.toLocalX(vx, vy), ly = f.toLocalY(vx, vy);
//						double ix = f.fromLocalToImageX(lx), iy = f.fromLocalToImageY(ly);
//						double dx = ix+(f.distortion == null ? 0 : f.distortion.getDist(ix, iy, 0)), 
//							dy = iy+(f.distortion == null ? 0 : f.distortion.getDist(ix, iy, 1));
//						int rgb = getPixel(f, dx, dy);
//						if (rgb != 0)
//						{
//							float a = (float)Math.min(Math.min(ix,  f.imagew-ix), Math.min(iy,  f.imageh-iy));
//							a *= a;
//							{r += a*ImageUtils.red(rgb); g += a*ImageUtils.green(rgb); b += a*ImageUtils.blue(rgb); n += a;}
//						}
//					}
					
					if (n > 0)
						img.setRGB(si, sj, ImageUtils.rgba((int)(r/n), (int)(g/n), (int)(b/n), 255));
					list.clear();
				}
			}
			for (int k=0;k<listeners.size();k++)
				listeners.get(k).onImageRendered(i, j, img);
		}
		
		for (int k=0;k<listeners.size();k++)
			listeners.get(k).onRenderEnded();
	}
}
