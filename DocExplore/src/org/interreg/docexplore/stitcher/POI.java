/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
	
	public double featureDistance2(POI poi, boolean useScaleAndOrientation)
	{
		return descriptorDistance2(poi);
	}
	public final double descriptorDistance2(POI poi)
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
