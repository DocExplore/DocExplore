package org.interreg.docexplore.stitcher;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.interreg.docexplore.stitcher.fast.FeaturePoint;
import org.interreg.docexplore.util.ImageUtils;

public class FeatureUtils
{
	private static class Corner implements Comparable<Corner>
	{
		int index;
		double strength;
		
		public Corner(int index, double strength)
		{
			this.index = index;
			this.strength = strength;
		}
		@Override public int compareTo(Corner o)
		{
			return strength < o.strength ? 1 : -1;
		}
	}
	
	static int [][] neighbors = {{-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}};
	static short gradient(BufferedImage image, int x, int y)
	{
		int crgb = image.getRGB(x, y);
		int cr = ImageUtils.red(crgb), cg = ImageUtils.green(crgb), cb = ImageUtils.blue(crgb);
		int max = 0;
		for (int i=0;i<8;i++)
		{
			int xt = x+neighbors[i][0];
			int yt = y+neighbors[i][1];
			if (!(xt > 0 && yt > 0 && xt < image.getWidth()-1 && yt < image.getHeight()-1))
				continue;
			int rgb = image.getRGB(xt, yt);
			int diff = Math.max(Math.abs(cr-ImageUtils.red(rgb)), Math.max(Math.abs(cg-ImageUtils.green(rgb)), Math.abs(cb-ImageUtils.blue(rgb))));
			if (diff > max)
				max = diff;
		}
		return (short)max;
	}
	
	static short [][][] separate(BufferedImage image)
	{
		short [][][] channels = new short [3][image.getWidth()][image.getHeight()];
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
		{
			int rgb = image.getRGB(i, j);
			channels[0][i][j] = (short)ImageUtils.red(rgb);
			channels[1][i][j] = (short)ImageUtils.green(rgb);
			channels[2][i][j] = (short)ImageUtils.blue(rgb);
		}
		return channels;
	}
	static double isExtremum(int [][] image, int x, int y)
	{
		int r = ImageUtils.red(image[x][y]), g = ImageUtils.green(image[x][y]), b = ImageUtils.blue(image[x][y]);
		int rmin = 255, gmin = 255, bmin = 255, rmax = 0, gmax = 0, bmax = 0;
		for (int i=0;i<8;i++)
		{
			int rgb = image[x+neighbors[i][0]][y+neighbors[i][1]];
			int pr = ImageUtils.red(rgb), pg = ImageUtils.green(rgb), pb = ImageUtils.blue(rgb);
			if (pr < rmin) rmin = pr;
			if (pg < gmin) gmin = pg;
			if (pb < bmin) bmin = pb;
			if (pr > rmax) rmax = pr;
			if (pg > gmax) gmax = pg;
			if (pb > bmax) bmax = pb;
		}
		//return r < rmin || g < gmin || b < bmin || r > rmax || g > gmax || b > bmax;
		return Math.max(rmin-r, Math.max(gmin-g, Math.max(bmin-b, Math.max(r-rmax, Math.max(g-gmax, b-bmax)))))/255.;
	}
	
	public static List<int []> detect2(BufferedImage image) {return detect2(image, 16);}
	public static List<int []> detect2(BufferedImage image, int ray)
	{
		int [][] blurred = fastblur(image, 16);
		TreeSet<Corner> corners = new TreeSet<>();
		HashMap<Integer, Corner> cornersByIndex = new HashMap<>();
		
		for (int x=1;x<image.getWidth()-1;x++)
			for (int y=1;y<image.getHeight()-1;y++)
			{
				double strength = isExtremum(blurred, x, y);
				if (strength <= 0)
					continue;
				Corner corner = new Corner(y*image.getWidth()+x, strength);
				corners.add(corner);
				cornersByIndex.put(corner.index, corner);
			}
		
		List<int []> points = new ArrayList<>();
		for (Corner top : corners)
			if (cornersByIndex.containsKey(top.index))
		{
//			for (int di=-ray;di<=ray;di++)
//				for (int dj=-ray;dj<=ray;dj++)
//					if (di*di+dj*dj <= ray*ray)
//			{
//				int xt = top.index%image.getWidth()+di, yt = top.index/image.getWidth()+dj;
//				if (xt < 0 || yt < 0 || xt >= image.getWidth() || yt >= image.getHeight())
//					continue;
//				int index = yt*image.getWidth()+xt;
//				cornersByIndex.remove(index);
//			}
			points.add(new int [] {top.index%image.getWidth(), top.index/image.getWidth()});
		}
		
		return points;
	}
	
	public static List<int []> detect(BufferedImage image) {return detect(image, .35, 16);}
	public static List<int []> detect(BufferedImage image, double strengthThreshold, int ray)
	{
		short [][][] channels = separate(image);
		
		TreeSet<Corner> corners = new TreeSet<>();
		HashMap<Integer, Corner> cornersByIndex = new HashMap<>();
		
		for (int x=3;x<image.getWidth()-3;x++)
			for (int y=3;y<image.getHeight()-3;y++)
		{
			double strength = corner(channels, x, y, strengthThreshold);
			if (strength < 0)
				continue;
			Corner corner = new Corner(y*image.getWidth()+x, strength);
			corners.add(corner);
			cornersByIndex.put(corner.index, corner);
		}
		
		List<int []> points = new ArrayList<>();
		for (Corner top : corners)
			if (cornersByIndex.containsKey(top.index))
		{
			for (int di=-ray;di<=ray;di++)
				for (int dj=-ray;dj<=ray;dj++)
					if (di*di+dj*dj <= ray*ray)
			{
				int xt = top.index%image.getWidth()+di, yt = top.index/image.getWidth()+dj;
				if (xt < 0 || yt < 0 || xt >= image.getWidth() || yt >= image.getHeight())
					continue;
				int index = yt*image.getWidth()+xt;
				cornersByIndex.remove(index);
			}
			points.add(new int [] {top.index%image.getWidth(), top.index/image.getWidth()});
		}
		
		return points;
	}
	
	//static int [][] offset = {{-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}};
	//static int [][] offset = {{-2, 0}, {-2, -1}, {-1, -2}, {0, -2}, {1, -2}, {2, -1}, {2, 0}, {2, 1}, {1, 2}, {0, 2}, {-1, 2}, {-2, 1}};
	static int [][] offset = {{-3, 0}, {-3, -1}, {-1, -3}, {-2, -2}, {0, -3}, {1, -3}, {2, -2}, {3, -1}, {3, 0}, {3, 1}, {2, 2}, {1, 3}, {0, 3}, {-1, 3}, {-2, 2}, {-2, 1}};
	private static double corner(short [][][] image, int x, int y, double strengthThreshold)
	{
		return Math.max(corner(image[0], x, y, strengthThreshold), Math.max(corner(image[1], x, y, strengthThreshold), corner(image[2], x, y, strengthThreshold)));
	}
	private static double corner(short [][] image, int x, int y, double strengthThreshold)
	{
		int avg = 0, min = 255, max = 0;
		int n = offset.length;
		for (int i=0;i<n;i++)
		{
			short v = image[x+offset[i][0]][y+offset[i][1]];
			if (v < min) min = v;
			if (v > max) max = v;
			avg += v;
		}
		avg /= 8;
		
		double strength = (max-min)/255.;
		if (strength < strengthThreshold)
			return -1;
		
		int nTrans = 0;
		for (int i=0;i<n;i++)
			if ((image[x+offset[i][0]][y+offset[i][1]]-avg)*(image[x+offset[(i+1)%n][0]][y+offset[(i+1)%n][1]]-avg) < 0)
				nTrans++;
		if (nTrans != 2)
			return -1;
		
		int transSize = 0;
		for (int i=0;i<n;i++)
			if (image[x+offset[i][0]][y+offset[i][1]]-avg < 0)
				transSize++;
		
		int shape = Math.max(transSize, n-transSize)-n/2;
		if (shape < n/4)
			return -1;
		return strength;
	}
	static int [][] fastblur(BufferedImage img, int radius){

	    int w= img.getWidth();
	    int h=img.getHeight();
	    int wm=w-1;
	    int hm=h-1;
	    int wh=w*h;
	    int div=radius+radius+1;
	    int r[]=new int[wh];
	    int g[]=new int[wh];
	    int b[]=new int[wh];
	    int rsum,gsum,bsum,x,y,i,p,p1,p2,yp,yi,yw;
	    int vmin[] = new int[Math.max(w,h)];
	    int vmax[] = new int[Math.max(w,h)];
	    int [][] res = new int [w][h];
	    
	    for (x=0;x<w;x++)
	    	for (y=0;y<h;y++)
	    		res[x][y] = img.getRGB(x, y);
	    
	    int dv[]=new int[256*div];
	    for (i=0;i<256*div;i++){
	        dv[i]=(i/div);
	    }

	    yw=yi=0;

	    for (y=0;y<h;y++){
	        rsum=gsum=bsum=0;
	        for(i=-radius;i<=radius;i++){
	        	int ind = yi+Math.min(wm,Math.max(i,0));
	            p=res[ind%res.length][ind/res.length];
	            rsum+=(p & 0xff0000)>>16;
	            gsum+=(p & 0x00ff00)>>8;
	            bsum+= p & 0x0000ff;
	        }
	        for (x=0;x<w;x++){

	            r[yi]=dv[rsum];
	            g[yi]=dv[gsum];
	            b[yi]=dv[bsum];

	            if(y==0){
	                vmin[x]=Math.min(x+radius+1,wm);
	                vmax[x]=Math.max(x-radius,0);
	            }
	            int ind = yw+vmin[x];
	            p1=res[ind%res.length][ind/res.length];
	            ind = yw+vmax[x];
	            p2=res[ind%res.length][ind/res.length];

	            rsum+=((p1 & 0xff0000)-(p2 & 0xff0000))>>16;
	            gsum+=((p1 & 0x00ff00)-(p2 & 0x00ff00))>>8;
	            bsum+= (p1 & 0x0000ff)-(p2 & 0x0000ff);
	            yi++;
	        }
	        yw+=w;
	    }

	    for (x=0;x<w;x++){
	        rsum=gsum=bsum=0;
	        yp=-radius*w;
	        for(i=-radius;i<=radius;i++){
	            yi=Math.max(0,yp)+x;
	            rsum+=r[yi];
	            gsum+=g[yi];
	            bsum+=b[yi];
	            yp+=w;
	        }
	        yi=x;
	        for (y=0;y<h;y++){
	        	res[yi%res.length][yi/res.length]=0xff000000 | (dv[rsum]<<16) | (dv[gsum]<<8) | dv[bsum];
	            if(x==0){
	                vmin[y]=Math.min(y+radius+1,hm)*w;
	                vmax[y]=Math.max(y-radius,0)*w;
	            }
	            p1=x+vmin[y];
	            p2=x+vmax[y];

	            rsum+=r[p1]-r[p2];
	            gsum+=g[p1]-g[p2];
	            bsum+=b[p1]-b[p2];

	            yi+=w;
	        }
	    }
	    return res;
	}
	
	public static List<FeaturePoint> filterWeakest(List<FeaturePoint> ori, int w, int h, int ray)
	{
		TreeSet<FeaturePoint> corners = new TreeSet<>(new Comparator<FeaturePoint>() {@Override public int compare(FeaturePoint o1, FeaturePoint o2)
		{
			return o2.score() > o1.score() ? 1 : -1;
		}});
		List<FeaturePoint> points = new ArrayList<>();
		FeaturePoint [][] features = new FeaturePoint [w][h];
		for (FeaturePoint point : ori)
		{
			features[point.x()][point.y()] = point;
			corners.add(point);
		}
		
		for (FeaturePoint top : corners)
			if (features[top.x()][top.y()] != null)
		{
			for (int di=-ray;di<=ray;di++)
				for (int dj=-ray;dj<=ray;dj++)
					if (di*di+dj*dj <= ray*ray)
			{
				int xt = top.x()+di, yt = top.y()+dj;
				if (xt < 0 || yt < 0 || xt >= w || yt >= h)
					continue;
				features[xt][yt] = null;
			}
			points.add(top);
		}
		
		return points;
	}
	public static List<double []> filterWeakestSurf(List<double []> v, int n)
	{
		if (v.size() <= n)
			return v;
		TreeSet<double []> pois = new TreeSet<>(new Comparator<double []>() {@Override public int compare(double [] o1, double [] o2)
			{return o2[3] > o1[3] ? 1 : -1;}});
		pois.addAll(v);
		List<double []> points = new ArrayList<>(n);
		for (double [] top : pois)
		{
			points.add(top);
			if (points.size() == n)
				break;
		}
		return points;
	}
}
