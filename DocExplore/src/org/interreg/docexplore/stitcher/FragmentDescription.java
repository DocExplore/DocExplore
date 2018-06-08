package org.interreg.docexplore.stitcher;

import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FragmentDescription
{
	public static double binWidth = 128;
	
	FragmentAssociation fa;
	Fragment fragment;
	List<POI> features = new ArrayList<POI>(0);
	
	@SuppressWarnings("unchecked")
	List<POI> [][] bins = new ArrayList [0][0];
	BufferedImage image = null;
	
	public FragmentDescription(FragmentAssociation fa, Fragment fragment)
	{
		this.fa = fa;
		this.fragment = fragment;
		resetBins();
	}
	
	public FragmentDescription(ObjectInputStream in, FragmentAssociation fa, List<Fragment> fragments) throws Exception
	{
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		this.fa = fa;
		this.fragment = fragments.get(in.readInt());
		int n = in.readInt();
		this.features = new ArrayList<POI>(n);
		for (int i=0;i<n;i++)
			features.add(POI.read(in, fragment, i));
		resetBins();
	}
	
	static int serialVersion = 0;
	public void write(ObjectOutputStream out, List<Fragment> fragments) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeInt(fragments.indexOf(fragment));
		out.writeInt(features.size());
		for (int i=0;i<features.size();i++)
			features.get(i).write(out);
	}
	
//	private BufferedImage getSubImage(BufferedImage full, Rectangle2D rect)
//	{
//		int x0 = (int)(full.getWidth()*rect.getX());
//		int y0 = (int)(full.getHeight()*rect.getY());
//		int x1 = (int)(full.getWidth()*(rect.getX()+rect.getWidth()));
//		int y1 = (int)(full.getHeight()*(rect.getY()+rect.getHeight()));
//		int m = x1-x0 > y1-y0 ? (y1-y0)/2 : (x1-x0)/2;
//		x0 = Math.max(0, x0-m);
//		y0 = Math.max(0, y0-m);
//		x1 = Math.min(full.getWidth()-1, x1+m);
//		y1 = Math.min(full.getHeight()-1, y1+m);
//		return full.getSubimage(x0, y0, x1-x0, y1-y0);
//	}
	void setImage() throws Exception
	{
		if (image != null)
			return;
		BufferedImage full = Fragment.getFull(fragment.file, fragment.link);
		this.image = full;//rect.getWidth()*rect.getHeight() > 0 ? getSubImage(full, rect) : full;
	}
	
	private void addToBins(POI poi)
	{
		int x = binx(poi.x);
		int y = biny(poi.y);
		List<POI> list = bins[x][y];
		if (list == null)
			list = bins[x][y] = new ArrayList<POI>();
		list.add(poi);
	}
	
	@SuppressWarnings("unchecked")
	void resetBins()
	{
		this.bins = new ArrayList [(int)(fragment.imagew/binWidth+1)][(int)(fragment.imageh/binWidth+1)];
		for (int i=0;i<features.size();i++)
			addToBins(features.get(i));
	}
	int binx(double x) {return Math.max(0, Math.min(bins.length-1, (int)(x/binWidth)));}
	int biny(double y) {return Math.max(0, Math.min(bins.length == 0 ? 0 : bins[0].length-1, (int)(y/binWidth)));}
	
	public void nearPOIs(double x, double y, double ray, Collection<POI> res)
	{
		int i0 = binx(x-ray), i1 = binx(x+ray);
		int j0 = biny(y-ray), j1 = biny(y+ray);
		for (int i=i0;i<=i1;i++)
			for (int j=j0;j<=j1;j++)
			{
				List<POI> list = bins[i][j];
				if (list == null)
					continue;
				for (int k=0;k<list.size();k++)
				{
					POI poi = list.get(k);
					if ((x-poi.x)*(x-poi.x)+(y-poi.y)*(y-poi.y) < ray*ray)
						res.add(poi);
				}
			}
	}
	public void containedPOIs(double x, double y, double w, double h, Collection<POI> res)
	{
		int i0 = binx(x), i1 = binx(x+w);
		int j0 = biny(y), j1 = biny(y+h);
		for (int i=i0;i<=i1;i++)
			for (int j=j0;j<=j1;j++)
			{
				List<POI> list = bins[i][j];
				if (list == null)
					continue;
				for (int k=0;k<list.size();k++)
				{
					POI poi = list.get(k);
					if (poi.x >= x && poi.y >= y && poi.x <= x+w && poi.y <= y+h)
						res.add(poi);
				}
			}
	}
	
	public void refreshFeatures()
	{
		features = new ArrayList<POI>(fragment.features.size());
		for (int i=0;i<fragment.features.size();i++)
			features.add(POI.copy(fragment.features.get(i), features.size()));
		resetBins();
	}
	
	POI add(double x, double y)
	{
		POI poi = new UserPOI(fragment, new double [] {x, y}, features.size());
		features.add(poi);
		addToBins(poi);
		return poi;
	}
	
	void remove(POI poi)
	{
		if (poi.index < features.size()-1)
		{
			features.set(poi.index, features.get(features.size()-1));
			features.get(poi.index).index = poi.index;
		}
		poi.index = -1;
		features.remove(features.size()-1);
		bins[binx(poi.x)][biny(poi.y)].remove(poi);
		List<Association> list = fa.associationsByPOI.get(poi);
		if (list != null)
			while (!list.isEmpty())
				fa.remove(list.get(list.size()-1));
	}
	
//	void move(POI poi, double x, double y)
//	{
//		bins[binx(x)][biny(y)].remove(poi);
//		poi.x = x;
//		poi.y = y;
//		addToBins(poi);
//		if (poi.descriptor != null)
//			poi.descriptor = null;
//	}
	
	double fromImageToLocalX(double x) {return x/image.getWidth();}
	double fromImageToLocalY(double y) {return y/image.getHeight();}
	
	public double [] fromFeature(double x, double y, double [] res)
	{
		double lx = x/image.getWidth();
		double ly = y/image.getHeight();
		res[0] = fragment.fromLocalX(lx, ly);
		res[1] = fragment.fromLocalY(lx, ly);
		return res;
	}
	
	double imageDistToRect(double lx, double ly)
	{
		boolean inx = lx >= 0 && lx <= 1;
		boolean iny = ly >= 0 && ly <= 1;
		if (inx && iny)
			return 0;
		double dx = inx ? 0 : Math.min(Math.abs(-lx),  Math.abs(1-lx));
		double dy = iny ? 0 : Math.min(Math.abs(-ly),  Math.abs(1-ly));
		return Math.max(dx*fragment.imagew, dy*fragment.imageh);
	}
}
