package org.interreg.docexplore.stitcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SurfPOI extends POI
{
	double trace, strength, scale, orientation;
	
	public SurfPOI(Fragment fragment, double [] surf, int index)
	{
		super(fragment, surf, index);
	}
	protected void initFromVector(double [] surf)
	{
		this.x = surf[0];
		this.y = surf[1];
		this.trace = surf[2];
		this.strength = surf[3];
		this.scale = surf[4];
		this.orientation = surf[5];
		this.descriptor = new double [surf.length-6];
		for (int i=0;i<descriptor.length;i++)
			descriptor[i] = surf[i+6];
	}
	public SurfPOI(SurfPOI poi, int index)
	{
		super(poi, index);
		
		this.trace = poi.trace;
		this.strength = poi.strength;
		this.scale = poi.scale;
		this.orientation = poi.orientation;
	}
	
	static int serialVersion = 0;
	public SurfPOI(ObjectInputStream in, Fragment fragment, int index, int serialVersion) throws Exception
	{
		super(in, fragment, index, serialVersion);
	}
	protected void initFromStream(ObjectInputStream in) throws Exception
	{
		this.trace = in.readDouble();
		this.strength = in.readDouble();
		this.scale = in.readDouble();
		this.orientation = in.readDouble();
	}
	
	protected void writeToStream(ObjectOutputStream out) throws Exception
	{
		out.writeDouble(trace);
		out.writeDouble(strength);
		out.writeDouble(scale);
		out.writeDouble(orientation);
	}
	
	public String type() {return "surf";}
	public double matchThreshold() {return Stitcher.surfMatchThreshold;}
	
	public double featureDistance2(POI poi)
	{
		if (poi instanceof SurfPOI)
			return descriptorDistance2(poi)+Stitcher.surfScaleAndOrientationWeight*scaleAndOrientationDistance2((SurfPOI)poi);
		return Double.POSITIVE_INFINITY;
	}
	public double scaleAndOrientationDistance2(SurfPOI poi)
	{
		return (scale-poi.scale)*(scale-poi.scale)+(orientation-poi.orientation)*(orientation-poi.orientation);
	}
}
