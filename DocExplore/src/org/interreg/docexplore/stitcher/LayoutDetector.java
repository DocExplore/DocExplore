package org.interreg.docexplore.stitcher;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LayoutDetector
{
	FragmentSet set;
	
	public LayoutDetector(FragmentSet set)
	{
		this.set = set;
	}
	
	public void process(float [] progress)
	{
		if (set.fragments.size() < 2)
			return;
		
		int cnt = 0;
		for (int i=0;i<set.fragments.size();i++)
			for (int j=i+1;j<set.fragments.size();j++)
		{
			if (set.get(set.fragments.get(i), set.fragments.get(j)) == null)
			{
				FragmentAssociation fa = associate(set.fragments.get(i), set.fragments.get(j));
				if (fa != null)
				{
					System.out.println(set.fragments.get(i).file.getName()+" <-> "+set.fragments.get(j).file.getName()+" ("+fa.associations.size()+")");
					set.add(fa);
				}
			}
			cnt++;
			progress[0] = cnt*1f/((set.fragments.size()+1)*set.fragments.size()/2);
		}
	}
	
	float [] subProgress = {0};
	List<Association> associations = new ArrayList<Association>();
	Rectangle2D.Double bounds1 = new Rectangle2D.Double(), bounds2 = new Rectangle2D.Double();
	GroupDetector groupDetector = new GroupDetector();
	FragmentAssociation associate(Fragment f1, Fragment f2)
	{
		FragmentAssociation map = new FragmentAssociation(f1, f2, -1);
		
		map.refreshFeatures();
		FragmentAssociationUtils.match(map, subProgress);
		groupDetector.detect(map, associations);
		
		if (associations.isEmpty())
			map = null;
		
//		{
//			FragmentAssociationUtils.boundingRect(associations, f1, bounds1);
//			FragmentAssociationUtils.boundingRect(associations, f2, bounds2);
//			if (bounds1.contains(.5, .5) || bounds2.contains(.5, .5))
//				map = null;
//		}
		
		if (map != null)
		{
			map.associations.clear();
			map.associations.addAll(associations);
			map.resetAssociationsByPOI();
		}
		associations.clear();
		return map;
	}
	
	List<Fragment> stack = new ArrayList<Fragment>();
	Set<Fragment> closed = new HashSet<Fragment>();
	public void consolidate(Fragment f)
	{
		stack.add(f);
		while (!stack.isEmpty())
		{
			Fragment cur = stack.remove(stack.size()-1);
			closed.add(cur);
			List<FragmentAssociation> list = set.associationsByFragment.get(cur);
			if (list != null)
				for (int i=0;i<list.size();i++)
			{
				FragmentAssociation fa = list.get(i);
				Fragment next = fa.other(cur);
				if (closed.contains(next))
					continue;
				fa.transform(next);
				stack.add(next);
			}
		}
		closed.clear();
	}
}
