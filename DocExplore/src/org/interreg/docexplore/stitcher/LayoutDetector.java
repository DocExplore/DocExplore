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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LayoutDetector
{
	FragmentSet set;
	
	public LayoutDetector(FragmentSet set)
	{
		this.set = set;
		this.groupDetector = new GroupDetector();
	}
	
	public void process(float [] progress)
	{
		if (set.fragments.size() < 2)
			return;
		
		int cnt = 0;
		for (int i=0;i<set.fragments.size();i++)
			for (int j=i+1;j<set.fragments.size();j++)
		{
			FragmentAssociation fa = set.get(set.fragments.get(i), set.fragments.get(j));
			if (fa != null && fa.associations.size() > 0)
				continue;
			if (fa != null)
				set.remove(fa);
			fa = associate(set.fragments.get(i), set.fragments.get(j), false);
			if (fa != null)
				set.add(fa);
			cnt++;
			progress[0] = cnt*1f/((set.fragments.size()+1)*set.fragments.size()/2);
		}
	}
	
	float [] subProgress = {0};
	List<Association> associations = new ArrayList<Association>();
	Rectangle2D.Double bounds1 = new Rectangle2D.Double(), bounds2 = new Rectangle2D.Double();
	GroupDetector groupDetector;
	FragmentAssociation associate(Fragment f1, Fragment f2, boolean useUiRotation)
	{
		FragmentAssociation map = new FragmentAssociation(f1, f2, -1);
		
		map.refreshFeatures();
		FragmentAssociationUtils.match(map, subProgress, true);
		groupDetector.detect(map, associations, false, useUiRotation);
		
		if (associations.isEmpty())
			map = null;
		
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
	Set<FragmentAssociation> closedAssociations = new HashSet<>();
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
				closedAssociations.add(fa);
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
