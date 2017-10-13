package org.interreg.docexplore.stitcher;

import java.awt.geom.Rectangle2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentAssociation
{
	FragmentDescription d1, d2;
	List<Association> associations = new ArrayList<Association>(0);
	Map<POI, List<Association>> associationsByPOI = new HashMap<POI, List<Association>>();
	double lengthRef;
	
	public FragmentAssociation(Fragment f1, Fragment f2) {this(f1, f2, false);}
	public FragmentAssociation(Fragment f1, Fragment f2, boolean useOverlap)
	{
		if (useOverlap)
		{
			this.d1 = new FragmentDescription(this, f1, f1.overlap(f2));
			this.d2 = new FragmentDescription(this, f2, f2.overlap(f1));
		}
		else
		{
			this.d1 = new FragmentDescription(this, f1, new Rectangle2D.Double(0, 0, 1, 1));
			this.d2 = new FragmentDescription(this, f2, new Rectangle2D.Double(0, 0, 1, 1));
		}
		this.lengthRef = .25*Math.max(d1.fragment.uiw*d1.rect.getWidth(), d1.fragment.uih*d1.rect.getHeight());
		lengthRef *= lengthRef;
		resetAssociationsByPOI();
	}
	
	static int serialVersion = 0;
	public FragmentAssociation(ObjectInputStream in, List<Fragment> fragments) throws Exception
	{
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		this.d1 = new FragmentDescription(in, this, fragments);
		this.d2 = new FragmentDescription(in, this, fragments);
		int n = in.readInt();
		this.associations = new ArrayList<Association>(n);
		for (int i=0;i<n;i++)
			associations.add(new Association(in, this, i));
		this.lengthRef = in.readDouble();
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
		out.writeDouble(lengthRef);
	}
	
	void resetAssociationsByPOI()
	{
		associationsByPOI = new HashMap<POI, List<Association>>();
		for (int i=0;i<associations.size();i++)
		{
			Association a = associations.get(i);
			
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
	
	public void computeSurf()
	{
		associations.clear();
		resetAssociationsByPOI();
		d1.compute();
		d2.compute();
	}
	
	public void match(float [] progress)
	{
		match(progress, 0, 1);
	}
	public void match(float [] progress, float ps, float pe)
	{
		associations.clear();
		for (int i=0;i<d1.features.size();i++)
		{
			progress[0] = ps+i*1f/d1.features.size()*(pe-ps);
			
			POI poi1 = d1.features.get(i);
			POI min = null;
			double minDist = 0;
			for (int j=0;j<d2.features.size();j++)
			{
				POI poi2 = d2.features.get(j);
				double dist = poi1.descriptorDistance2(poi2);
				if (min == null || dist < minDist)
				{
					min = poi2;
					minDist = dist;
				}
			}
			if (minDist < .15)
				associations.add(new Association(this, poi1, min, 1/(1+minDist), associations.size()));
		}
		resetAssociationsByPOI();
		
		for (int i=0;i<d2.features.size();i++)
		{
			List<Association> list = associationsByPOI.get(d2.features.get(i));
			if (list == null || list.size() < 2)
				continue;
			double maxStrength = -1;
			for (int j=0;j<list.size();j++)
				if (list.get(j).strength > maxStrength)
					maxStrength = list.get(j).strength;
			while (list.get(0).strength < maxStrength) remove(list.get(0));
			while (list.get(list.size()-1).strength < maxStrength) remove(list.get(list.size()-1));
			while (list.size() > 1) remove(list.get(list.size()-1));
		}
	}
	
	public void filterByUIDistance(double lim)
	{
		for (int i=0;i<associations.size();i++)
			if (Math.sqrt(associations.get(i).uiDistance2()) > lim)
				{remove(associations.get(i)); i--;}
	}
	
	public void filterByDescriptor()
	{
		double avg = 0;
		for (int i=0;i<associations.size();i++)
			avg += associations.get(i).strength;
		avg /= associations.size();
		for (int i=0;i<associations.size();i++)
			if (associations.get(i).strength < avg)
				remove(associations.get(i--));
//		System.out.println(">"+r+"/"+(associations.size()+r)+" "+(r*100/(associations.size()+r)));
		
//		Set<Association> set = new TreeSet<Association>(new Comparator<Association>() {@Override public int compare(Association o1, Association o2)
//		{
//			return o1.strength-o2.strength < 0 ? -1 : o1.strength == o2.strength ? 0 : 1;
//		}});
//		set.addAll(associations);
//		
//		int remove = associations.size()-associations.size()/100;
//		for (Association a : set)
//			if (--remove == 0)
//				break;
//			else remove(a);
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
}
