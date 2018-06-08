package org.interreg.docexplore.stitcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentAssociation
{
	FragmentDescription d1, d2;
	public List<Association> associations = new ArrayList<Association>(0);
	Map<POI, List<Association>> associationsByPOI = new HashMap<POI, List<Association>>();
	int index;
	
	private FragmentTransform transform = null;
	
	public FragmentAssociation(Fragment f1, Fragment f2, int index)
	{
		this.d1 = new FragmentDescription(this, f1);
		this.d2 = new FragmentDescription(this, f2);
		this.index = index;
		resetAssociationsByPOI();
		this.transform = FragmentTransform.build(this, null);
	}
	
	static int serialVersion = 0;
	public FragmentAssociation(ObjectInputStream in, int index, List<Fragment> fragments) throws Exception
	{
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		this.d1 = new FragmentDescription(in, this, fragments);
		this.d2 = new FragmentDescription(in, this, fragments);
		int n = in.readInt();
		this.associations = new ArrayList<Association>(n);
		for (int i=0;i<n;i++)
			associations.add(new Association(in, this, i));
		this.index = index;
		this.transform = (FragmentTransform)in.readObject();
		resetAssociationsByPOI();
	}
	
	public void write(ObjectOutputStream out, List<Fragment> fragments) throws Exception
	{
		out.writeInt(serialVersion);
		d1.write(out, fragments);
		d2.write(out, fragments);
		out.writeInt(associations.size());
		for (int i=0;i<associations.size();i++)
			associations.get(i).write(out);
		out.writeObject(transform);
	}
	
	public void resetAssociationsByPOI()
	{
		associationsByPOI = new HashMap<POI, List<Association>>();
		for (int i=0;i<associations.size();i++)
		{
			Association a = associations.get(i);
			a.index = i;
			
			List<Association> list = associationsByPOI.get(a.p1);
			if (list == null)
				associationsByPOI.put(a.p1, list = new ArrayList<Association>(1));
			list.add(a);
			
			list = associationsByPOI.get(a.p2);
			if (list == null)
				associationsByPOI.put(a.p2, list = new ArrayList<Association>(1));
			list.add(a);
		}
	}
	
	Association add(POI p1, POI p2)
	{
		if (p1.fragment == d2.fragment)
		{
			POI tmp = p1;
			p1 = p2;
			p2 = tmp;
		}
		Association a = new Association(this, p1, p2, associations.size());
		associations.add(a);
		List<Association> list = associationsByPOI.get(a.p1);
		if (list == null)
			associationsByPOI.put(a.p1, list = new ArrayList<Association>(1));
		list.add(a);
		list = associationsByPOI.get(a.p2);
		if (list == null)
			associationsByPOI.put(a.p2, list = new ArrayList<Association>(1));
		list.add(a);
//		if (p1.descriptor.length > 0 && p2.descriptor.length > 0)
//			System.out.println("surf dist: "+p1.descriptorDistance2(p2));
		transform = null;
		return a;
	}
	void remove(Association a)
	{
		if (a.index < associations.size()-1)
		{
			associations.set(a.index, associations.get(associations.size()-1));
			associations.get(a.index).index = a.index;
		}
		a.index = -1;
		associations.remove(associations.size()-1);
		List<Association> list = associationsByPOI.get(a.p1);
		list.remove(a);
		if (list.isEmpty())
			associationsByPOI.remove(a.p1);
		list = associationsByPOI.get(a.p2);
		list.remove(a);
		if (list.isEmpty())
			associationsByPOI.remove(a.p2);
		transform = null;
	}
	
	public Fragment other(Fragment f) {return d1.fragment == f ? d2.fragment : d2.fragment == f ? d1.fragment : null;}
	
	public void transform(Fragment res) {transform(res, null);}
	public void transform(Fragment res, float [] progress) {transform(res, progress, 0, 1);}
	public void transform(Fragment res, float [] progress, float ps, float pe)
	{
		if (transform == null)
			this.transform = FragmentTransform.build(this, progress, pe, ps);
		if (res != d1.fragment)
			transform.transform(d1.fragment, res);
		else transform.itransform(d2.fragment, res);
		
		if (progress != null) progress[0] = pe;
	}
	
	public double meanUIDistance()
	{
		double sum = 0;
		for (int i=0;i<associations.size();i++)
			sum += Math.sqrt(associations.get(i).uiDistance2());
		return sum/associations.size();
	}
	public double stdUIDistanceDev(double mean)
	{
		double dev = 0;
		for (int i=0;i<associations.size();i++)
		{
			double d = Math.sqrt(associations.get(i).uiDistance2());
			dev += (d-mean)*(d-mean);
		}
		return dev/associations.size();
	}
	
	public void refreshFeatures()
	{
		associations.clear();
		resetAssociationsByPOI();
		d1.refreshFeatures();
		d2.refreshFeatures();
		transform = null;
	}
	
	public void filterByUIDistance(double lim)
	{
		for (int i=0;i<associations.size();i++)
			if (Math.sqrt(associations.get(i).uiDistance2()) > lim)
				{remove(associations.get(i)); i--;}
	}
	
	public void removeUnusedDescriptors(float [] progress)
	{
		for (int i=0;i<d1.features.size();i++)
		{
			progress[0] = .5f*i*1f/d1.features.size();
			POI poi = d1.features.get(i);
			if (!associationsByPOI.containsKey(poi))
				{d1.remove(poi); i--;}
		}
		for (int i=0;i<d2.features.size();i++)
		{
			progress[0] = .5f+.5f*i*1f/d2.features.size();
			POI poi = d2.features.get(i);
			if (!associationsByPOI.containsKey(poi))
				{d2.remove(poi); i--;}
		}
	}
	
	public void clearDescriptors()
	{
		d1.features.clear();
		d2.features.clear();
		associations.clear();
		resetAssociationsByPOI();
	}
}
