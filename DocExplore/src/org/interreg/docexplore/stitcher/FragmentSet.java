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

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.manuscript.DocExploreDataLink;

public class FragmentSet
{
	public List<Fragment> fragments = new ArrayList<Fragment>();
	public List<FragmentAssociation> associations = new ArrayList<FragmentAssociation>();
	
	Map<Fragment, List<FragmentAssociation>> associationsByFragment;
	
	public FragmentSet()
	{
		refreshAssociationsByFragment();
	}
	
	int serialVersion = 0;
	public FragmentSet(ObjectInputStream in, DocExploreDataLink link) throws Exception
	{
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		int n = in.readInt();
		for (int i=0;i<n;i++)
			fragments.add(new Fragment(in, link, i));
		n = in.readInt();
		for (int i=0;i<n;i++)
			associations.add(new FragmentAssociation(in, i, fragments));
		refreshAssociationsByFragment();
	}
	
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeInt(fragments.size());
		for (int i=0;i<fragments.size();i++)
			fragments.get(i).write(out);
		out.writeInt(associations.size());
		for (int i=0;i<associations.size();i++)
			associations.get(i).write(out, fragments);
	}
	
	void refreshAssociationsByFragment()
	{
		associationsByFragment = new HashMap<Fragment, List<FragmentAssociation>>();
		for (int i=0;i<associations.size();i++)
		{
			FragmentAssociation fa = associations.get(i);
			List<FragmentAssociation> list = associationsByFragment.get(fa.d1.fragment);
			if (list == null)
				associationsByFragment.put(fa.d1.fragment, list = new ArrayList<FragmentAssociation>(1));
			list.add(fa);
			list = associationsByFragment.get(fa.d2.fragment);
			if (list == null)
				associationsByFragment.put(fa.d2.fragment, list = new ArrayList<FragmentAssociation>(1));
			list.add(fa);
		}
	}
	
	public void fragmentsAt(double x, double y, double r, Collection<Fragment> res)
	{
		for (int i=0;i<fragments.size();i++)
			if (fragments.get(i).contains(x, y, r))
				res.add(fragments.get(i));
	}
	
	public FragmentAssociation get(Fragment f1, Fragment f2)
	{
		FragmentAssociation map = null;
		List<FragmentAssociation> list = associationsByFragment.get(f1);
		if (list != null)
			for (int i=0;i<list.size();i++)
				if (list.get(i).other(f1) == f2)
					{map = list.get(i); break;}
		return map;
	}
	
	public FragmentAssociation add(Fragment f1, Fragment f2)
	{
		FragmentAssociation fa = new FragmentAssociation(f1, f2, -1);
		add(fa);
		return fa;
	}
	public void add(FragmentAssociation fa)
	{
		fa.index = associations.size();
		associations.add(fa);
		List<FragmentAssociation> list = associationsByFragment.get(fa.d1.fragment);
		if (list == null)
			associationsByFragment.put(fa.d1.fragment, list = new ArrayList<FragmentAssociation>(1));
		list.add(fa);
		list = associationsByFragment.get(fa.d2.fragment);
		if (list == null)
			associationsByFragment.put(fa.d2.fragment, list = new ArrayList<FragmentAssociation>(1));
		list.add(fa);
	}
	
	public void moveToLast(Fragment f)
	{
		fragments.set(f.index, fragments.get(fragments.size()-1));
		fragments.set(fragments.size()-1, f);
		fragments.get(f.index).index = f.index;
		f.index = fragments.size()-1;
	}
	
	public Fragment add(File file, FeatureDetector detector) throws Exception
	{
		Fragment f = new Fragment(file.getAbsolutePath(), null, fragments.size(), detector);
		fragments.add(f);
		return f;
	}
	public Fragment add(String file, DocExploreDataLink link, FeatureDetector detector) throws Exception
	{
		Fragment f = new Fragment(file, link, fragments.size(), detector);
		fragments.add(f);
		return f;
	}
	
	public void remove(Fragment f)
	{
		if (f.index < fragments.size()-1)
		{
			fragments.set(f.index, fragments.get(fragments.size()-1));
			fragments.get(f.index).index = f.index;
		}
		f.index = 1;
		fragments.remove(fragments.size()-1);

		List<FragmentAssociation> list = associationsByFragment.get(f);
		if (list != null)
			while (!list.isEmpty())
				remove(list.get(list.size()-1));
	}
	
	public void remove(FragmentAssociation fa)
	{
		if (fa.index < associations.size()-1)
		{
			associations.set(fa.index, associations.get(associations.size()-1));
			associations.get(fa.index).index = fa.index;
		}
		fa.index = -1;
		associations.remove(associations.size()-1);
		
		List<FragmentAssociation> list = associationsByFragment.get(fa.d1.fragment);
		list.remove(fa);
		if (list.isEmpty())
			associationsByFragment.remove(fa.d1.fragment);
		list = associationsByFragment.get(fa.d2.fragment);
		list.remove(fa);
		if (list.isEmpty())
			associationsByFragment.remove(fa.d2.fragment);
	}
	
	public void clearAssociations()
	{
		associations.clear();
		associationsByFragment.clear();
	}
}
