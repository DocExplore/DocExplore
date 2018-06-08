package org.interreg.docexplore.stitcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LbpPOI extends POI
{
	public LbpPOI(Fragment fragment, double [] surf, int index)
	{
		super(fragment, surf, index);
	}
	protected void initFromVector(double [] surf)
	{
		this.x = surf[0];
		this.y = surf[1];
		this.descriptor = new double [surf.length-2];
		for (int i=0;i<descriptor.length;i++)
			descriptor[i] = surf[i+2];
	}
	public LbpPOI(LbpPOI poi, int index)
	{
		super(poi, index);
	}
	
	static int serialVersion = 0;
	public LbpPOI(ObjectInputStream in, Fragment fragment, int index, int serialVersion) throws Exception
	{
		super(in, fragment, index, serialVersion);
	}
	protected void initFromStream(ObjectInputStream in) throws Exception
	{
	}
	
	protected void writeToStream(ObjectOutputStream out) throws Exception
	{
	}
	
	public String type() {return "lbp";}
	public double matchThreshold() {return Stitcher.lbpMatchThreshold;}
	
	public double featureDistance2(POI poi)
	{
		if (poi instanceof LbpPOI)
			return descriptorDistance2(poi);
		return Double.POSITIVE_INFINITY;
	}
}
