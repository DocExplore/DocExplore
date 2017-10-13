package org.interreg.docexplore.stitcher;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class LayoutDetector
{
	FragmentView view;
	
	public LayoutDetector(FragmentView view)
	{
		this.view = view;
	}
	
	public void process(float [] progress)
	{
		if (view.fragments.size() < 2)
			return;
		
		boolean [] attached = new boolean [view.fragments.size()];
		for (int i=0;i<attached.length;i++)
			attached[i] = false;
		int nAttached = 0;
		
		TreeSet<Integer> current = new TreeSet<Integer>();
		current.add(0);
		while (!current.isEmpty())
		{
			while (!current.isEmpty())
			{
				int index = current.first();
				current.remove(index);
				if (!attached[index])
				{
					attached[index] = true;
					nAttached++;
					progress[0] = nAttached*1f/attached.length;
				}
				for (int i=0;i<attached.length;i++)
					if (!attached[i])
				{
					FragmentAssociation fa = associate(view.fragments.get(index), view.fragments.get(i));
					if (fa != null)
					{
						view.associations.add(fa);
						attached[i] = true;
						nAttached++;
						progress[0] = nAttached*1f/attached.length;
						current.add(i);
					}
				}
			}
			for (int i=0;i<attached.length;i++)
				if (!attached[i])
					{current.add(i); break;}
		}
		view.repaint();
	}
	
	float [] subProgress = {0};
	List<Association> associations = new ArrayList<Association>();
	FragmentAssociation associate(Fragment f1, Fragment f2)
	{
		FragmentAssociation map = new FragmentAssociation(f1, f2);
		
		map.computeSurf();
		map.match(subProgress);
		GroupDetector.detect(map, associations);
		if (associations.size() < 5)
			return null;
		map.associations = associations;
		map.resetAssociationsByPOI();
		
		Tightener.tighten(map, subProgress);
		return map;
	}
}
