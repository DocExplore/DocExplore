package org.interreg.docexplore.stitcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupDetector
{
	static class Group
	{
		double dx, dy;
		List<Association> assocs;
		
		Map<POI, Integer> assocIndexes;
		double [] bounds1, bounds2;
		int index;
		
		Group(double dx, double dy)
		{
			this.dx = dx;
			this.dy = dy;
			this.assocs = new ArrayList<Association>(1);
		}
		
		void updateDiff()
		{
			double dminx = 0, dminy = 0, dmaxx = 0, dmaxy = 0;
			for (int i=0;i<assocs.size();i++)
			{
				Association a = assocs.get(i);
				double dx = a.p2.x-a.p1.x, dy = a.p2.y-a.p1.y;
				if (i == 0 || dx < dminx) dminx = dx;
				if (i == 0 || dy < dminy) dminy = dy;
				if (i == 0 || dx > dmaxx) dmaxx = dx;
				if (i == 0 || dy > dmaxy) dmaxy = dy;
			}
			dx = .5*(dminx+dmaxx);
			dy = .5*(dminy+dmaxy);
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
				if (i1 != null && a.featureDistance2() < assocs.get(i1).featureDistance2())
				{
					assocIndexes.remove(assocs.get(i1).p1);
					assocIndexes.remove(assocs.get(i1).p2);
					assocs.set(i1, a);
					a.index = i1;
				}
				else if (i2 != null && a.featureDistance2() < assocs.get(i2).featureDistance2())
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
	}
	
	double x0, y0;
	double w, h;
	List<Group> [][] groups;
	
	public GroupDetector()
	{
		this(32, 32);
	}
	@SuppressWarnings("unchecked")
	public GroupDetector(int nw, int nh)
	{
		this.groups = new List [nw][nh];
	}
	
	public void setup(FragmentAssociation map)
	{
		this.x0 = -Math.max(map.d1.fragment.imagew, map.d2.fragment.imagew);
		this.y0 = -Math.max(map.d1.fragment.imageh, map.d2.fragment.imageh);
		this.w = -2*x0;
		this.h = -2*y0;
		
		for (int i=0;i<groups.length;i++)
			for (int j=0;j<groups[0].length;j++)
				if (groups[i][j] != null)
					groups[i][j].clear();
	}
	
	int binx(double x) {return Math.max(0, Math.min(groups.length-1, (int)((x-x0)*groups.length/w)));}
	int biny(double y) {return Math.max(0, Math.min(groups[0].length-1, (int)((y-y0)*groups[0].length/h)));}
	void removeGroup(Group group)
	{
//		if (groups[binx(group.dx)][biny(group.dy)] == null)
//		{
//			System.out.print(binx(group.dx)+","+biny(group.dy)+"!!!");
//			for (int i=0;i<groups.length;i++)
//				for (int j=0;j<groups[0].length;j++)
//					if (groups[i][j] != null)
//						for (int k=0;k<groups[i][j].size();k++)
//							if (groups[i][j].get(k) == group)
//			{
//				System.out.println(i+","+j);
//			}
//		}
		int bx = binx(group.dx);
		int by = biny(group.dy);
		if (groups[bx][by] == null)
			return;
		groups[bx][by].remove(group);
	}
	void addGroup(Group group)
	{
		int i = binx(group.dx), j = biny(group.dy);
		if (groups[i][j] == null)
			groups[i][j] = new ArrayList<Group>(1);
		groups[i][j].add(group);
	}
	Group findNearestGroup(double dx, double dy, double ray) {return findNearestGroup(dx, dy, ray, null);}
	Group findNearestGroup(double dx, double dy, double ray, Group exclude)
	{
		int i0 = binx(dx-ray), i1 = binx(dx+ray);
		int j0 = biny(dy-ray), j1 = biny(dy+ray);
		Group min = null;
		double minDist = 0;
		for (int i=i0;i<=i1;i++)
			for (int j=j0;j<=j1;j++)
				if (groups[i][j] != null)
					for (int k=0;k<groups[i][j].size();k++)
		{
			Group g = groups[i][j].get(k);
			if (g == exclude)
				continue;
			double d = (g.dx-dx)*(g.dx-dx)+(g.dy-dy)*(g.dy-dy);
			if (d < ray*ray && (min == null || d < minDist))
			{
				min = g;
				minDist = d;
			}
		}
		return min;
	}
	void addAssociation(Association a)
	{
		double dx = a.p2.x-a.p1.x, dy = a.p2.y-a.p1.y;
		Group g = findNearestGroup(dx, dy, Stitcher.groupSpreadRay);
		if (g == null)
		{
			g = new Group(dx, dy);
			g.assocs.add(a);
		}
		else
		{
			removeGroup(g);
			g.assocs.add(a);
			g.updateDiff();
		}
		addGroup(g);
	}
	List<Group []> merges = new ArrayList<Group []>();
	int findSmallMerges()
	{
		for (int i=0;i<groups.length;i++)
			for (int j=0;j<groups[0].length;j++)
				if (groups[i][j] != null)
					for (int k=0;k<groups[i][j].size();k++)
		{
			Group g = groups[i][j].get(k);
			Group c = findNearestGroup(g.dx, g.dy, Stitcher.groupSpreadRay, g);
			if (c != null)
				merges.add(new Group [] {g, c});
		}
		if (merges.isEmpty())
			return 0;
		for (int i=0;i<merges.size();i++)
		{
			Group g1 = merges.get(i)[0];
			Group g2 = merges.get(i)[1];
			if (g1 == g2)
				continue;
			removeGroup(g1);
			removeGroup(g2);
			g1.assocs.addAll(g2.assocs);
			g1.updateDiff();
			addGroup(g1);
			for (int j=i+1;j<merges.size();j++)
				for (int k=0;k<2;k++)
					if (merges.get(j)[k] == g2) {merges.get(j)[k] = g1;}
		}
		int res = merges.size();
		merges.clear();
		return res;
	}
	int findLargeMerges(List<Group> groups)
	{
		int n = 0;
		for (int i=0;i<groups.size()-1;i++)
		{
			Group g1 = groups.get(i);
			double mx1 = .5*(g1.bounds1[0]+g1.bounds1[2]), my1 = .5*(g1.bounds1[1]+g1.bounds1[3]);
			for (int j=i+1;j<groups.size();j++)
			{
				Group g2 = groups.get(j);
				double mx2 = .5*(g2.bounds1[0]+g2.bounds1[2]), my2 = .5*(g2.bounds1[1]+g2.bounds1[3]);
				double dist = Math.sqrt((mx1-mx2)*(mx1-mx2)+(my1-my2)*(my1-my2));
				double ddist = Math.sqrt((g1.dx-g2.dx)*(g1.dx-g2.dx)+(g1.dy-g2.dy)*(g1.dy-g2.dy));
				//System.out.printf("%d %d => %.3f\n", i, j, ddist/dist);
				if (ddist/dist < Stitcher.groupDivergenceRatioThreshold)
					merges.add(new Group [] {g1, g2});
			}
		}
		for (int i=0;i<merges.size();i++)
		{
			Group g1 = merges.get(i)[0];
			Group g2 = merges.get(i)[1];
			if (g1 == g2)
				continue;
			
			g1.addToMap(g2.assocs);
			g1.updateDiff();
			
			groups.set(g2.index, groups.get(groups.size()-1));
			groups.get(g2.index).index = g2.index;
			groups.remove(groups.size()-1);
			
			for (int j=i+1;j<merges.size();j++)
				for (int k=0;k<2;k++)
					if (merges.get(j)[k] == g2) {merges.get(j)[k] = g1;}
		}
		merges.clear();
		return n;
	}
	
	void bounds(List<Association> associations, boolean d1, double [] bounds)
	{
		double minx = Double.MAX_VALUE, maxx = -Double.MAX_VALUE;
		double miny = Double.MAX_VALUE, maxy = -Double.MAX_VALUE;
		for (int i=0;i<associations.size();i++)
		{
			Association a = associations.get(i);
			double x = d1 ? a.p1.x : a.p2.x;
			double y = d1 ? a.p1.y : a.p2.y;
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
	
	ArrayList<POI> near = new ArrayList<POI>();
	ArrayList<Association> stack = new ArrayList<Association>();
	void spreadGroupLocally(FragmentAssociation map, Group g, Set<POI> associated1, Set<POI> associated2)
	{
		for (int i=0;i<g.assocs.size();i++)
		{
			Association a = g.assocs.get(i);
			stack.add(a);
		}
		boolean spread = false;
		while (!stack.isEmpty())
		{
			Association a = stack.remove(stack.size()-1);
			map.d1.nearPOIs(a.p1.x, a.p1.y, 3*Stitcher.groupSpreadRay, near);
			for (int i=0;i<near.size();i++)
			{
				POI p1 = near.get(i);
				if (associated1.contains(p1))
					continue;
				POI min = findLocalMatch(p1, map.d2, .15*Stitcher.groupSpreadRay, a.p2.x-a.p1.x, a.p2.y-a.p1.y, associated2);
				if (min != null)
				{
					Association newa = new Association(map, p1, min, 0);
					g.assocs.add(newa);
					associated1.add(p1);
					associated2.add(min);
					stack.add(newa);
					spread = true;
				}
			}
			near.clear();
		}
		if (spread)
			g.updateDiff();
	}
	void spreadGroupInternally(FragmentAssociation map, Group g, Set<POI> associated1, Set<POI> associated2, double [] bounds)
	{
		boolean spread = false;
		map.d1.containedPOIs(bounds[0], bounds[1], bounds[2]-bounds[0], bounds[3]-bounds[1], near);
		for (int i=0;i<near.size();i++)
		{
			POI p1 = near.get(i);
			if (associated1.contains(p1))
				continue;
			POI min = findLocalMatch(p1, map.d2, .15*Stitcher.groupSpreadRay, g.dx, g.dy, associated2);
			if (min != null)
			{
				Association newa = new Association(map, p1, min, 0);
				g.assocs.add(newa);
				associated1.add(p1);
				associated2.add(min);
				spread = true;
			}
		}
		near.clear();
		if (spread)
			g.updateDiff();
	}
	//returns level of confidence
	double [] bounds = {0, 0, 0, 0};
	Set<POI> associated1 = new HashSet<POI>();
	Set<POI> associated2 = new HashSet<POI>();
	void strengthenGroup(FragmentAssociation map, Group g)
	{
		for (int i=0;i<g.assocs.size();i++)
		{
			Association a = g.assocs.get(i);
			associated1.add(a.p1);
			associated2.add(a.p2);
		}
		//System.out.printf("%d => ", g.assocs.size());
		spreadGroupLocally(map, g, associated1, associated2);
		//System.out.printf("%d => ", g.assocs.size());
		bounds(g.assocs, true, bounds);
		spreadGroupInternally(map, g, associated1, associated2, bounds);
		//System.out.printf("%d ", g.assocs.size());
		
		associated1.clear();
		associated2.clear();
	}
	
	double confidence(FragmentAssociation map, Group g, double areaWeight)
	{
		bounds(g.assocs, true, g.bounds1 = new double [4]);
		map.d1.containedPOIs(g.bounds1[0], g.bounds1[1], g.bounds1[2]-g.bounds1[0], g.bounds1[3]-g.bounds1[1], near);
		double matching1 = g.assocs.size()*1f/Math.max(1, near.size());
		double l = g.bounds1[0]/map.d1.fragment.imagew, u = g.bounds1[1]/map.d1.fragment.imageh, r = g.bounds1[2]/map.d1.fragment.imagew, d = g.bounds1[3]/map.d1.fragment.imageh;
		double edgeness1 = Math.max(Math.max((1-l)*(d-u), r*(d-u)), Math.max((1-u)*(r-l), d*(r-l)));
		double area1 = Math.min(r-l, d-u);
		double conf1 = Math.pow(matching1, 2-Stitcher.groupEdgenessWeight)*Math.pow(edgeness1, Stitcher.groupEdgenessWeight);
		conf1 *= Math.pow(area1, areaWeight);
		near.clear();
		
		bounds(g.assocs, false, g.bounds2 = new double [4]);
		map.d2.containedPOIs(g.bounds2[0], g.bounds2[1], g.bounds2[2]-g.bounds2[0], g.bounds2[3]-g.bounds2[1], near);
		double matching2 = g.assocs.size()*1f/Math.max(1, near.size());
		l = g.bounds2[0]/map.d2.fragment.imagew; u = g.bounds2[1]/map.d2.fragment.imageh; r = g.bounds2[2]/map.d2.fragment.imagew; d = g.bounds2[3]/map.d2.fragment.imageh;
		double edgeness2 = Math.max(Math.max((1-l)*(d-u), r*(d-u)), Math.max((1-u)*(r-l), d*(r-l)));
		double area2 = Math.min(r-l, d-u);
		double conf2 = Math.pow(matching2, 2-Stitcher.groupEdgenessWeight)*Math.pow(edgeness2, Stitcher.groupEdgenessWeight);
		conf2 *= Math.pow(area2, areaWeight);
		near.clear();
		
//		double c = Math.min(conf1, conf2);
//		if (c >= .5*Stitcher.groupConfidenceThreshold && g.assocs.size() >= Stitcher.groupSizeThreshold) System.out.printf(">%d %.3f,%.3f %.3f,%.3f %.3f\n", 
//			g.assocs.size(), 
//			matching1, edgeness1, 
// 			matching2, edgeness2, 
//			c);
		
		return Math.min(conf1, conf2);
	}
	
	ArrayList<POI> local = new ArrayList<POI>();
	POI findLocalMatch(POI p1, FragmentDescription desc, double ray, double dx, double dy, Set<POI> exclude)
	{
		desc.nearPOIs(p1.x+dx, p1.y+dy, ray, local);
		
		double minDist = 0;
		POI min = null;
		for (int j=0;j<local.size();j++)
		{
			POI p2 = local.get(j);
			if (exclude.contains(p2))
				continue;
			double dist = p1.featureDistance2(p2);
			if (min == null || dist < minDist)
			{
				min = p2;
				minDist = dist;
			}
		}
		local.clear();
		if (min != null && minDist <= Stitcher.surfMatchThreshold)
			return min;
		return null;
	}
	
	public void detect(FragmentAssociation map, Collection<Association> res)
	{System.out.println("detect "+map.d1.fragment.file.getName()+"-"+map.d2.fragment.file.getName());
		setup(map);
		for (int i=0;i<map.associations.size();i++)
		{
			Association a = map.associations.get(i);
			addAssociation(a);
		}
		while (findSmallMerges() > 0);
		
		List<Group> largeGroups = new ArrayList<Group>();
		for (int i=0;i<groups.length;i++)
			for (int j=0;j<groups[0].length;j++)
				if (groups[i][j] != null)
				{
					for (int k=0;k<groups[i][j].size();k++)
					{
						Group g = groups[i][j].get(k);
						strengthenGroup(map, g);
						double c = confidence(map, g, 0);
						if (c < Stitcher.groupConfidenceThreshold || g.assocs.size() < Stitcher.groupSizeThreshold)
						{
							groups[i][j].set(k, groups[i][j].get(groups[i][j].size()-1));
							groups[i][j].remove(groups[i][j].size()-1);
							k--;
						}
						else
						{
							g.index = largeGroups.size();
							largeGroups.add(g);
							g.computeMap();
						}
					}
					if (groups[i][j].isEmpty())
						groups[i][j] = null;
				}
		while (findLargeMerges(largeGroups) > 0);
		
		Group max = null;
		double maxd = 0;
		for (int i=0;i<largeGroups.size();i++)
		{
			double d = confidence(map, largeGroups.get(i), Stitcher.groupAreaWeight);
			if (max == null || d > maxd)
			{
				maxd = d;
				max = largeGroups.get(i);
			}
		}
		if (max != null) System.out.println(maxd);
		if (max != null && maxd > Stitcher.groupConfidenceThreshold && max.assocs.size() > 2*Stitcher.groupSizeThreshold)
			res.addAll(max.assocs);
	}
}
