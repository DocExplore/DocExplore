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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group
{
	GroupDetector detector;
	FragmentAssociation map;
	double meddx, meddy;
	double meandx, meandy;
	List<Association> assocs;
	
	Map<POI, Integer> assocIndexes;
	double [] bounds1, bounds2, localBounds2;
	int index;
	
	Group(GroupDetector detector, FragmentAssociation map, double dx, double dy)
	{
		this.detector = detector;
		this.map = map;
		this.meddx = dx;
		this.meddy = dy;
		this.meandx = dx;
		this.meandy = dy;
		this.assocs = new ArrayList<Association>(1);
	}
	
	void updateDiff()
	{
		double dminx = 0, dminy = 0, dmaxx = 0, dmaxy = 0, sumx = 0, sumy = 0;
		for (int i=0;i<assocs.size();i++)
		{
			Association a = assocs.get(i);
			//double dx = a.p2.x-a.p1.x, dy = a.p2.y-a.p1.y;
			double dx = detector.rightx(a.p2.x, a.p2.y)-a.p1.x, dy = detector.righty(a.p2.x, a.p2.y)-a.p1.y;
			if (i == 0 || dx < dminx) dminx = dx;
			if (i == 0 || dy < dminy) dminy = dy;
			if (i == 0 || dx > dmaxx) dmaxx = dx;
			if (i == 0 || dy > dmaxy) dmaxy = dy;
			sumx += dx;
			sumy += dy;
		}
		meddx = .5*(dminx+dmaxx);
		meddy = .5*(dminy+dmaxy);
		meandx = sumx/assocs.size();
		meandx = sumy/assocs.size();
		
	}
	void computeBounds()
	{
		bounds(assocs, true, false, bounds1 = new double [4]);
		bounds(assocs, false, false, bounds2 = new double [4]);
		bounds(assocs, false, true, localBounds2 = new double [4]);
	}
	void bounds(List<Association> associations, boolean d1, boolean local, double [] bounds)
	{
		double minx = Double.MAX_VALUE, maxx = -Double.MAX_VALUE;
		double miny = Double.MAX_VALUE, maxy = -Double.MAX_VALUE;
		for (int i=0;i<associations.size();i++)
		{
			Association a = associations.get(i);
			double x = d1 ? a.p1.x : local ? detector.rightx(a.p2.x, a.p2.y) : a.p2.x;
			double y = d1 ? a.p1.y : local ? detector.righty(a.p2.x, a.p2.y) : a.p2.y;
			if (x < minx) minx = x;
			if (x > maxx) maxx = x;
			if (y < miny) miny = y;
			if (y > maxy) maxy = y;
		}
		bounds[0] = minx-1;
		bounds[1] = miny-1;
		bounds[2] = maxx+1;
		bounds[3] = maxy+1;
	}
	
	double deviation()
	{
		double sum = 0;
		for (int i=0;i<assocs.size();i++)
		{
			Association a = assocs.get(i);
			//double dx = a.p2.x-a.p1.x, dy = a.p2.y-a.p1.y;
			double dx = detector.rightx(a.p2.x, a.p2.y)-a.p1.x, dy = detector.righty(a.p2.x, a.p2.y)-a.p1.y;
			sum += (dx-meandx)*(dx-meandx)+(dy-meandy)*(dy-meandy);
		}
		return Math.sqrt(sum/assocs.size());
	}
	
	void computeMap()
	{
		this.assocIndexes = new HashMap<POI, Integer>(assocs.size()*2);
		for (int i=0;i<assocs.size();i++)
		{
			assocIndexes.put(assocs.get(i).p1, i);
			assocIndexes.put(assocs.get(i).p2, i);
		}
	}
	void addToMap(List<Association> add)
	{
		for (int i=0;i<add.size();i++)
		{
			Association a = add.get(i);
			Integer i1 = assocIndexes.get(a.p1), i2 = assocIndexes.get(a.p2);
			if (i1 != null && a.featureDistance2(!detector.useUiGeometry) < assocs.get(i1).featureDistance2(!detector.useUiGeometry))
			{
				assocIndexes.remove(assocs.get(i1).p1);
				assocIndexes.remove(assocs.get(i1).p2);
				assocs.set(i1, a);
				a.index = i1;
			}
			else if (i2 != null && a.featureDistance2(!detector.useUiGeometry) < assocs.get(i2).featureDistance2(!detector.useUiGeometry))
			{
				assocIndexes.remove(assocs.get(i2).p1);
				assocIndexes.remove(assocs.get(i2).p2);
				assocs.set(i2, a);
				a.index = i2;
			}
			else if (i1 != null || i2 != null)
				continue;
			else
			{
				a.index = assocs.size();
				assocs.add(a);
			}
			assocIndexes.put(a.p1, a.index);
			assocIndexes.put(a.p2, a.index);
		}
	}
	
	/**
	 * Proportion of pois that are part of the group within all pois in the group bounds.
	 */
	double matchingFactor() {return matchingFactor(true)*matchingFactor(false);}
	double matchingFactor(boolean left)
	{
		double [] bounds = left ? bounds1 : bounds2;
		FragmentDescription d = (left ? map.d1 : map.d2);
		d.containedPOIs(bounds[0], bounds[1], bounds[2]-bounds[0], bounds[3]-bounds[1], detector.near);
		double matching = assocs.size()*1f/Math.max(1, detector.near.size());
		detector.near.clear();
		return matching;
	}
	/**
	 * How well spread along the edge are the group bounds.
	 */
	double edgenessFactor() {return edgenessFactor(true)*edgenessFactor(false);}
	double edgenessFactor(boolean left)
	{
		double [] bounds = left ? bounds1 : bounds2;
		FragmentDescription desc = (left ? map.d1 : map.d2);
		double l = bounds[0]/desc.fragment.imagew, u = bounds[1]/desc.fragment.imageh, r = bounds[2]/desc.fragment.imagew, d = bounds[3]/desc.fragment.imageh;
		return Math.max(Math.max((1-l)*(d-u), r*(d-u)), Math.max((1-u)*(r-l), d*(r-l)));
	}
	/**
	 * How much longer is one dimension of the group bounds than the other.
	 */
	double areaFactor() {return areaFactor(true)*areaFactor(false);}
	double areaFactor(boolean left)
	{
		double [] bounds = left ? bounds1 : bounds2;
		FragmentDescription desc = (left ? map.d1 : map.d2);
		double l = bounds[0]/desc.fragment.imagew, u = bounds[1]/desc.fragment.imageh, r = bounds[2]/desc.fragment.imagew, d = bounds[3]/desc.fragment.imageh;
		//return Math.max((r-l)/(d-u+1), (d-u)/(r-l+1))*Math.max((r-l)*(r-l), (d-u)*(d-u));
		return Math.max(r-l, d-u);
	}
	/**
	 * How opposite are the group bounds on both fragments.
	 */
	double complementFactor()
	{
		double l1 = bounds1[0]/map.d1.fragment.imagew, u1 = bounds1[1]/map.d1.fragment.imageh, r1 = bounds1[2]/map.d1.fragment.imagew, d1 = bounds1[3]/map.d1.fragment.imageh;
		//double l2 = bounds2[0]/map.d2.fragment.imagew, u2 = bounds2[1]/map.d2.fragment.imageh, r2 = bounds2[2]/map.d2.fragment.imagew, d2 = bounds2[3]/map.d2.fragment.imageh;
		double l2 = localBounds2[0]/map.d1.fragment.imagew, u2 = localBounds2[1]/map.d1.fragment.imageh, r2 = localBounds2[2]/map.d1.fragment.imagew, d2 = localBounds2[3]/map.d1.fragment.imageh;
		return Math.min(1, Math.max(Math.max(Math.abs(l1-l2), Math.abs(u1-u2)), Math.max(Math.abs(r1-r2), Math.abs(d1-d2))));
	}
	double deviationFactor()
	{
		return Math.min(1, assocs.size()*Stitcher.groupSpreadRay/(deviation()));
	}
	double lateGroupConfidence()
	{
		double [] out = detector.model.compute(new double []
		{
			assocs.size(),
			matchingFactor(), 
			edgenessFactor(),
			areaFactor(),
			complementFactor()
		});
		return out[0];
//		return 
//			Math.pow(Math.min(matchingFactor(true), matchingFactor(false)), Stitcher.groupMatchingWeight)*
////			Math.pow(.5*(edgenessFactor(true)+edgenessFactor(false)), Stitcher.groupEdgenessWeight)*
//			Math.pow(Math.min(areaFactor(true), areaFactor(false)), Stitcher.groupAreaWeight)*
//			Math.pow(deviationFactor(), Stitcher.groupDeviationWeight)*
//			(1-1/Math.sqrt(assocs.size()));
	}
//	double earlyGroupConfidence()
//	{
//		return 
//			Math.pow(.5*(matchingFactor(true)+matchingFactor(false)), Stitcher.groupMatchingWeight)*
////			Math.pow(.5*(edgenessFactor(true)+edgenessFactor(false)), Stitcher.groupEdgenessWeight)*
////			Math.pow(.5*(areaFactor(true)+areaFactor(false)), Stitcher.groupAreaWeight)*
//			Math.pow(deviationFactor(), Stitcher.groupDeviationWeight);
//	}
	void dump()
	{
		//System.out.printf("size: %d, match: %.3f, edge: %.3f, area: %.3f, comp: %.3f => %.4f %.4f\n", 
		System.out.printf("size: %d, %.4f %.4f %.4f %.4f => %.4f\n",
			assocs.size(),
			matchingFactor(), 
			edgenessFactor(),
			areaFactor(),
			complementFactor(),
			lateGroupConfidence());
	}
}
