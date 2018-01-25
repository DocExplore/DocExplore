package org.interreg.docexplore.stitcher;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.util.ImageUtils;

public class Renderer
{
	Map<Fragment, BufferedImage> images = new HashMap<Fragment, BufferedImage>();
	List<Fragment> recent = new ArrayList<Fragment>();
	
	private int getPixel(Fragment f, double x, double y)
	{
		BufferedImage img = images.get(f);
		if (img != null)
			recent.remove(f);
		else
		{
			try
			{
				img = ImageUtils.read(f.file);
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
	public void render(FragmentSet set, String baseName, File renderDir, float [] progress)
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
		
		int w = (int)(ppu*(maxx-minx));
		int h = (int)(ppu*(maxy-miny));
		int nw = w/imageSizeLim, nh = h/imageSizeLim;
		if (nw*imageSizeLim < w) nw++;
		if (nh*imageSizeLim < h) nh++;
		int tilew = w/nw, tileh = h/nh;
		w = tilew*nw;
		h = tileh*nh;
		
		List<Fragment> list = new ArrayList<Fragment>();
		for (int i=0;i<nw;i++)
			for (int j=0;j<nh;j++)
		{
			BufferedImage img = new BufferedImage(tilew, tileh, BufferedImage.TYPE_3BYTE_BGR);
			for (int si=0;si<tilew;si++)
				for (int sj=0;sj<tileh;sj++)
			{
				progress[0] = (i*nh+j+si*1f/tilew)*1f/(nw*nh);
				double vx = minx+(si+i*tilew)*(maxx-minx)/w;
				double vy = miny+(sj+j*tileh)*(maxy-miny)/h;
				double r = 0, g = 0, b = 0;
				set.fragmentsAt(vx, vy, list);
				double n = 0;
				for (int k=0;k<list.size();k++)
				{
					Fragment f = list.get(k);
					double lx = f.toLocalX(vx, vy), ly = f.toLocalY(vx, vy);
					double ix = f.fromLocalToImageX(lx), iy = f.fromLocalToImageY(ly);
					double dx = ix+f.distortion.getDist(ix, iy, 0), dy = iy+f.distortion.getDist(ix, iy, 1);
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
			try {ImageUtils.write(img, "PNG", new File(renderDir, baseName+"_"+i+"_"+j+".png"));}
			catch (Exception e) {e.printStackTrace();}
		}
	}
}
