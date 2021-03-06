/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.datalink.filesystem;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Vector;

public class RegionFile extends ObjectFile
{
	private static final long serialVersionUID = -5400368443866131417L;
	
	int pageId;
	int [][] outline;
	
	public RegionFile()
	{
	}

	public RegionFile(int id, int pageId, int [][] outline)
	{
		super(id);
		this.pageId = pageId;
		this.outline = outline;
	}
	
	public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		this.pageId = ois.readInt();
		this.outline = (int [][])ois.readObject();
	}
	
	public void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeInt(pageId);
		oos.writeObject(outline);
	}
	
	public List<Point> getOutline()
	{
		List<Point> res = new Vector<Point>(outline.length);
		for (int [] point : outline)
			res.add(new Point(point[0], point[1]));
		return res;
	}
}
