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
	
	public double featureDistance2(POI poi, boolean useScaleAndOrientation)
	{
		if (poi instanceof SurfPOI)
			return descriptorDistance2(poi)+(useScaleAndOrientation ? Stitcher.surfScaleAndOrientationWeight*scaleAndOrientationDistance2((SurfPOI)poi) : 0);
		return Double.POSITIVE_INFINITY;
	}
	public double scaleAndOrientationDistance2(SurfPOI poi)
	{
		return (scale-poi.scale)*(scale-poi.scale)+(orientation-poi.orientation)*(orientation-poi.orientation);
	}
}
