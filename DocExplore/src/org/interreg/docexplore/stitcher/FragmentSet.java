package org.interreg.docexplore.stitcher;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentSet
{
	List<Fragment> fragments = new ArrayList<Fragment>();
	List<FragmentAssociation> associations = new ArrayList<FragmentAssociation>();
	
	Map<Fragment, List<FragmentAssociation>> associationsByFragment;
	
	public FragmentSet()
	{
		refreshAssociationsByFragment();
	}
	
	int serialVersion = 0;
	public FragmentSet(ObjectInputStream in) throws Exception
	{
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		int n = in.readInt();
		for (int i=0;i<n;i++)
			fragments.add(new Fragment(in, i));
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
	
	public void fragmentsAt(double x, double y, Collection<Fragment> res)
	{
		for (int i=0;i<fragments.size();i++)
			if (fragments.get(i).contains(x, y))
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
	
	public Fragment add(File file) throws Exception
	{
		Fragment f = new Fragment(file, fragments.size());
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
}
