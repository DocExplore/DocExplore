/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.process.align;


public class ClearComponentFunction implements LabeledImage.LabelFunction
{
	public ClearComponentFunction()
	{
	}
	
	LabeledImage.Label getNear(LabeledImage image, int i, int j)
	{
		for (int di=-1;di<=1;di++)
			for (int dj=-1;dj<=1;dj++)
				if (di!=0 || dj!=0)
		{
			int x = i+di, y = j+dj;
			if (x < 0 || x >= image.data.length || y < 0 || y >= image.data[0].length || image.data[i][j] == image.data[x][y])
				continue;
			return image.data[x][y];
		}
		return null;
	}
	
	int curComp = -1;
	LabeledImage.Label near = null;
	boolean deleted;
	public void doPixel(LabeledImage image, LabeledImage.Label label, int comp, int x, int y)
	{
		if (curComp != comp)
		{
			near = getNear(image, x, y);
			if (near == null)
				return;
			
			curComp = comp;
		}
		image.data[x][y] = near;
		near.size++;
		label.size--;
	}

	public void beforeStart(LabeledImage image, LabeledImage.Label label)
	{
		curComp = -1;
		near = null;
		deleted = false;
	}

	public void afterEnd(LabeledImage image, LabeledImage.Label label)
	{
		if (label.size == 0)
		{
			image.labels.remove(label);
			deleted = true;
		}
		else
		{
			for (int i=0;i<label.components.size();i++)
			{
				int [] where = label.components.get(i);
				if (image.data[where[0]][where[1]] != label)
					label.components.remove(i--);
			}
			
			if (label.components.isEmpty())
			{
				image.labels.remove(label);
				deleted = true;
			}
		}
	}

}
