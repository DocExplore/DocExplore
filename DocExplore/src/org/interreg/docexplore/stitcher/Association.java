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

public class Association
{
	FragmentAssociation fa;
	POI p1, p2;
	int index;
	
	public Association(FragmentAssociation fa, POI p1, POI p2, int index)
	{
		this.fa = fa;
		this.p1 = p1;
		this.p2 = p2;
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
		this.index = index;
	}
	
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		out.writeInt(p1.index);
		out.writeInt(p2.index);
	}
	
	public POI poiFor(Fragment f) {return f == fa.d1.fragment ? p1 : f == fa.d2.fragment ? p2 : null;}
	
	public POI other(POI poi) {return poi == p1 ? p2 : poi == p2 ? p1 : null;}
	
	public double featureDistance2(boolean useScaleAndOrientation) {return p1.featureDistance2(p2, useScaleAndOrientation);}
	public double uiDistance2() {return p1.uiDistance2(p2);}
}
