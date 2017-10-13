package org.interreg.docexplore.stitcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Association
{
	FragmentAssociation fa;
	POI p1, p2;
	double strength;
	int index;
	
	public Association(FragmentAssociation fa, POI p1, POI p2, double strength, int index)
	{
		this.fa = fa;
		this.p1 = p1;
		this.p2 = p2;
		this.strength = strength;
		this.index = index;
	}
	
	static int serialVersion = 0;
	public Association(ObjectInputStream in, FragmentAssociation fa, int index) throws Exception
	{
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		this.fa = fa;
		this.p1 = fa.d1.features.get(in.readInt());
		this.p2 = fa.d2.features.get(in.readInt());
		this.strength = in.readDouble();
		this.index = index;
	}
	
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeInt(p1.index);
		out.writeInt(p2.index);
		out.writeDouble(strength);
	}
	
	public POI other(POI poi) {return poi == p1 ? p2 : poi == p2 ? p1 : null;}
	
	public double descriptorDistance2() {return p1.descriptorDistance2(p2);}
	public double uiDistance2() {return p1.uiDistance2(p2);}
}
