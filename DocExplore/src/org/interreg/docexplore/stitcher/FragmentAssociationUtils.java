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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import info.debatty.java.lsh.LSHSuperBit;

public class FragmentAssociationUtils
{
	static class FeatureBins
	{
		int stages, buckets;
		LSHSuperBit lsh;
		List<POI> [] data;
		
		@SuppressWarnings("unchecked")
		public FeatureBins(int stages, int buckets)
		{
			this.stages = stages;
			this.buckets = buckets;
			this.lsh = new LSHSuperBit(stages, buckets, 64, 1234);
			int n = 1;
			for (int i=0;i<stages;i++)
				n *= buckets;
			data = new List [n];
		}
		void clear()
		{
			for (int i=0;i<data.length;i++)
				if (data[i] != null)
					data[i].clear();
		}
		void add(List<POI> pois)
		{
			for (int i=0;i<pois.size();i++)
				if (pois.get(i).descriptor != null)
			{
				int bin = bin(lsh.hash(pois.get(i).descriptor));
				if (data[bin] == null)
					data[bin] = new ArrayList<POI>(1);
				data[bin].add(pois.get(i));
			}
		}
		int bin(int [] hash)
		{
			int l = data.length;
			int b = 0;
			for (int i=0;i<hash.length;i++)
			{
				int nl = l/buckets;
				b += hash[i]*nl;
				l = nl;
			}
			return b;
		}
		List<POI> near(POI poi)
		{
			return data[bin(lsh.hash(poi.descriptor))];
		}
	}
	
	static FeatureBins bins = new FeatureBins(6, 4);
	public static void match(FragmentAssociation fa, float [] progress, boolean useScaleAndOrientation)
	{
		match(fa, progress, 0, 1, useScaleAndOrientation);
	}
	public static void match(FragmentAssociation fa, float [] progress, float ps, float pe, boolean useScaleAndOrientation)
	{
		fa.associations.clear();
		
		bins.add(fa.d2.features);
		for (int i=0;i<fa.d1.features.size();i++)
		{
			progress[0] = ps+i*1f/fa.d1.features.size()*(pe-ps);
			
			POI poi1 = fa.d1.features.get(i);
			if (poi1.descriptor == null)
				continue;
			
			//List<POI> search = fa.d2.features;
			List<POI> search = bins.near(poi1);
			if (search == null)
				continue;
			
			POI min = null;
			double minDist = 0;
			for (int j=0;j<search.size();j++)
			{
				POI poi2 = search.get(j);
				if (poi2.descriptor == null)
					continue;
				double dist = poi1.featureDistance2(poi2, useScaleAndOrientation);
				if (min == null || dist < minDist)
				{
					min = poi2;
					minDist = dist;
				}
			}//System.out.println(">>>"+minDist);
			if (min != null && minDist < min.matchThreshold())
				fa.add(poi1, min);
		}
		fa.resetAssociationsByPOI();
		bins.clear();
		
		for (int i=0;i<fa.d2.features.size();i++)
		{
			List<Association> list = fa.associationsByPOI.get(fa.d2.features.get(i));
			if (list == null || list.size() < 2)
				continue;
			int min = -1;
			double minDist = -1;
			for (int j=0;j<list.size();j++)
			{
				double dist = list.get(j).featureDistance2(useScaleAndOrientation);
				if (min < 0 || dist < minDist)
				{
					min = j;
					minDist = dist;
				}
			}
			while (list.size() > min+1) fa.remove(list.get(list.size()-1));
			while (list.size() > 1) fa.remove(list.get(0));
		}
	}
	
	public static void boundingRect(List<Association> associations, Fragment f, Rectangle2D.Double rect)
	{
		double minx = Double.MAX_VALUE, maxx = -Double.MAX_VALUE;
		double miny = Double.MAX_VALUE, maxy = -Double.MAX_VALUE;
		for (int i=0;i<associations.size();i++)
		{
			Association a = associations.get(i);
			POI poi = a.p1.fragment == f ? a.p1 : a.p2;
			double x = poi.fragment.fromImageToLocalX(poi.x);
			double y = poi.fragment.fromImageToLocalY(poi.y);
			if (i == 0 || x < minx) minx = x;
			if (i == 0 || x > maxx) maxx = x;
			if (i == 0 || y < miny) miny = y;
			if (i == 0 || y > maxy) maxy = y;
		}
		
		rect.setRect(minx, miny, maxx-minx, maxy-miny);
	}
	
//	public static void tightenRectShortestDimension(FragmentDescription d)
//	{
//		double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
//		boolean hor = d.rect.getWidth() < d.rect.getHeight();
//		for (int i=0;i<d.features.size();i++)
//		{
//			POI poi = d.features.get(i);
//			if (d.fa.associationsByPOI.get(poi) == null)
//				continue;
//			double k = hor ? d.fragment.fromImageToLocalX(poi.x) : d.fragment.fromImageToLocalY(poi.y);
//			if (i == 0 || k < min) min = k;
//			if (i == 0 || k > max) max = k;
//		}
//		if (hor) {d.rect.setRect(min, d.rect.getY(), max-min, d.rect.getHeight());}
//		else {d.rect.setRect(d.rect.getX(), min, d.rect.getWidth(), max-min);}
//	}
}
