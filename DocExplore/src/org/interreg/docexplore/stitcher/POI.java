package org.interreg.docexplore.stitcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class POI
{
	Fragment fragment;
	double x, y;
	double [] descriptor;
	int index;
	
	public POI(Fragment fragment, double [] surf, int index)
	{
		this.fragment = fragment;
		this.x = surf[0];
		this.y = surf[1];
		this.descriptor = new double [surf.length-2];
		for (int i=0;i<descriptor.length;i++)
			descriptor[i] = surf[i+2];
		this.index = index;
	}
	public POI(POI poi, int index)
	{
		this.fragment = poi.fragment;
		this.x = poi.x;
		this.y = poi.y;
		this.descriptor = poi.descriptor;
		this.index = index;
	}
	
	static int serialVersion = 0;
	public POI(ObjectInputStream in, Fragment fragment, int index) throws Exception
	{
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		this.fragment = fragment;
		this.x = in.readDouble();
		this.y = in.readDouble();
		this.descriptor = (double [])in.readObject();
		this.index = index;
	}
	
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeObject(descriptor);
	}
	
	public double descriptorDistance2(POI poi)
	{
		double dist = 0;
		for (int k=0;k<descriptor.length;k++)
			dist += (descriptor[k]-poi.descriptor[k])*(descriptor[k]-poi.descriptor[k]);
		return dist;
	}
	public double uiDistance2(POI poi)
	{
		double lx1 = fragment.fromImageToLocalX(x), ly1 = fragment.fromImageToLocalY(y);
		double uix1 = fragment.fromLocalX(lx1, ly1), uiy1 = fragment.fromLocalY(lx1, ly1);
		double lx2 = poi.fragment.fromImageToLocalX(poi.x), ly2 = poi.fragment.fromImageToLocalY(poi.y);
		double uix2 = poi.fragment.fromLocalX(lx2, ly2), uiy2 = poi.fragment.fromLocalY(lx2, ly2);
		return ((uix1-uix2)*(uix1-uix2)+(uiy1-uiy2)*(uiy1-uiy2));
	}
}
