package org.interreg.docexplore.stitcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class POI
{
	Fragment fragment;
	double x, y;
	double [] descriptor;
	int index;
	
	public POI(Fragment fragment, double [] d, int index)
	{
		this.fragment = fragment;
		initFromVector(d);
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
	
	static int serialVersion = 1;
	public POI(ObjectInputStream in, Fragment fragment, int index, int serialVersion) throws Exception
	{
		this.fragment = fragment;
		this.x = in.readDouble();
		this.y = in.readDouble();
		initFromStream(in);
		this.descriptor = (double [])in.readObject();
		this.index = index;
	}
	
	public static POI read(ObjectInputStream in, Fragment fragment, int index) throws Exception
	{
		String type = "surf";
		int serialVersion = in.readInt();
		if (serialVersion > 0)
			type = in.readUTF();
		if (type.equals("surf"))
			return new SurfPOI(in, fragment, index, serialVersion);
		else if (type.equals("user"))
			return new UserPOI(in, fragment, index, serialVersion);
		else if (type.equals("lbp"))
			return new LbpPOI(in, fragment, index, serialVersion);
		return null;
	}
	public static POI copy(POI poi, int index)
	{
		if (poi.type().equals("surf"))
			return new SurfPOI((SurfPOI)poi, index);
		if (poi.type().equals("user"))
			return new UserPOI((UserPOI)poi, index);
		if (poi.type().equals("lbp"))
			return new LbpPOI((LbpPOI)poi, index);
		return null;
	}
	
	protected abstract void initFromVector(double [] d);
	protected abstract void initFromStream(ObjectInputStream in) throws Exception;
	protected abstract void writeToStream(ObjectOutputStream out) throws Exception;
	public abstract String type();
	public abstract double matchThreshold();
	
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeUTF(type());
		out.writeDouble(x);
		out.writeDouble(y);
		writeToStream(out);
		out.writeObject(descriptor);
	}
	
	public double featureDistance2(POI poi)
	{
		return descriptorDistance2(poi);
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
