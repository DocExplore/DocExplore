package org.interreg.docexplore.stitcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class POI
{
	Fragment fragment;
	double x, y, trace, strength, scale, orientation;
	double [] descriptor;
	int index;
	
	public POI(Fragment fragment, double [] surf, int index)
	{
		this.fragment = fragment;
		this.x = surf[0];
		this.y = surf[1];
		this.trace = surf[2];
		this.strength = surf[3];
		this.scale = surf[4];
		this.orientation = surf[5];
		this.descriptor = new double [surf.length-6];
		for (int i=0;i<descriptor.length;i++)
			descriptor[i] = surf[i+6];
		this.index = index;
	}
	public POI(Fragment fragment, double x, double y, int index)
	{
		this.fragment = fragment;
		this.x = x;
		this.y = y;
		this.descriptor = new double [0];
		this.index = index;
	}
	public POI(POI poi, int index)
	{
		this.fragment = poi.fragment;
		this.x = poi.x;
		this.y = poi.y;
		this.trace = poi.trace;
		this.strength = poi.strength;
		this.scale = poi.scale;
		this.orientation = poi.orientation;
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
		this.trace = in.readDouble();
		this.strength = in.readDouble();
		this.scale = in.readDouble();
		this.orientation = in.readDouble();
		this.descriptor = (double [])in.readObject();
		this.index = index;
	}
	
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(trace);
		out.writeDouble(strength);
		out.writeDouble(scale);
		out.writeDouble(orientation);
		out.writeObject(descriptor);
	}
	
	public double featureDistance2(POI poi)
	{
		return descriptorDistance2(poi)+Stitcher.surfScaleAndOrientationWeight*scaleAndOrientationDistance2(poi);
	}
	public double descriptorDistance2(POI poi)
	{
		double dist = 0;
		for (int k=0;k<descriptor.length;k++)
			dist += (descriptor[k]-poi.descriptor[k])*(descriptor[k]-poi.descriptor[k]);
		return dist;
	}
	public double scaleAndOrientationDistance2(POI poi)
	{
		return (scale-poi.scale)*(scale-poi.scale)+(orientation-poi.orientation)*(orientation-poi.orientation);
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
