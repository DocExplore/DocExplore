package org.interreg.docexplore.stitcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class UserPOI extends POI
{

	public UserPOI(Fragment fragment, double [] d, int index)
	{
		super(fragment, d, index);
	}

	public UserPOI(ObjectInputStream in, Fragment fragment, int index, int serialVersion) throws Exception
	{
		super(in, fragment, index, serialVersion);
	}

	public UserPOI(UserPOI poi, int index)
	{
		super(poi, index);
	}

	@Override protected void initFromVector(double [] d)
	{
		this.x = d[0];
		this.y = d[1];
		this.descriptor = null;
	}

	@Override protected void initFromStream(ObjectInputStream in) throws Exception
	{
	}

	@Override protected void writeToStream(ObjectOutputStream out) throws Exception
	{
	}

	@Override public String type() {return "user";}
	public double matchThreshold() {return 0;}
}
