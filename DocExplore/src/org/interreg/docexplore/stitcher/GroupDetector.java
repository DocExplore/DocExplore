package org.interreg.docexplore.stitcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.interreg.docexplore.util.Pair;

public class GroupDetector
{
	static double distLim = 10;
	
	public static void detect(FragmentAssociation fa, Collection<Association> res)
	{
		List<Pair<double [], List<Association>>> groups = new ArrayList<Pair<double[], List<Association>>>(fa.associations.size());
		
		for (int i=0;i<fa.associations.size();i++)
		{
			Association a = fa.associations.get(i);
			double dx = a.p1.x-a.p2.x, dy = a.p1.y-a.p2.y;
			int min = closestGroup(dx, dy, -1, groups);
			double dist = 0;
			if (min >= 0)
			{
				double [] v = groups.get(min).first;
				dist = (dx-v[0])*(dx-v[0])+(dy-v[1])*(dy-v[1]);
			}
			if (min < 0 || dist > distLim*distLim)
			{
				List<Association> list = new ArrayList<Association>(1);
				list.add(a);
				groups.add(new Pair<double[], List<Association>>(new double [] {dx, dy}, list));
			}
			else
			{
				groups.get(min).second.add(a);
				updateGroup(groups.get(min));
			}
		}
		
//		int maxSize = -1, minSize = -1;
//		for (int i=0;i<groups.size();i++)
//		{
//			int size = groups.get(i).second.size();
//			if (minSize < 0 || size < minSize)
//				minSize = size;
//			if (maxSize < 0 || size > maxSize)
//				maxSize = size;
//		}
//		int med = (maxSize+minSize)/2;
//		for (int i=0;i<groups.size();i++)
//			if (groups.get(i).second.size() >= med)
//				res.addAll(groups.get(i).second);
		
		int max = -1, maxSize = -1;
		for (int i=0;i<groups.size();i++)
		{
			int size = groups.get(i).second.size();
			if (maxSize < 0 || size > maxSize)
			{
				maxSize = size;
				max = i;
			}
		}
		res.addAll(groups.get(max).second);
	}
	
	private static void updateGroup(Pair<double [], List<Association>> group)
	{
		double dx = 0, dy = 0;
		for (int i=0;i<group.second.size();i++)
		{
			Association a = group.second.get(i);
			dx += a.p1.x-a.p2.x;
			dy += a.p1.y-a.p2.y;
		}
		dx /= group.second.size();
		dy /= group.second.size();
		group.first[0] = dx;
		group.first[1] = dy;
	}
	
	private static int closestGroup(double dx, double dy, int exclude, List<Pair<double [], List<Association>>> groups)
	{
		int min = -1;
		double minDist = 0;
		for (int i=0;i<groups.size();i++)
			if (i != exclude)
		{
			double [] v = groups.get(i).first;
			double d = (dx-v[0])*(dx-v[0])+(dy-v[1])*(dy-v[1]);
			if (min < 0 || d < minDist)
			{
				min = i;
				minDist = d;
			}
		}
		return min;
	}
}
