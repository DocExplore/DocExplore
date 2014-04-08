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
import java.util.HashSet;
import java.util.Set;



public class FillBackgroundFunction implements LabeledImage.LabelFunction
{
	LabeledImage.Label background;
	Set<LabeledImage.Label> neighbors;
	boolean deleted;
	
	public FillBackgroundFunction(LabeledImage.Label background)
	{
		this.background = background;
		this.neighbors = new HashSet<LabeledImage.Label>();
	}
	
	public void doPixel(LabeledImage image, LabeledImage.Label label, int comp, int i, int j)
	{
		if (label == background)
			return;
		
		for (int di=-1;di<2;di++)
			for (int dj=-1;dj<2;dj++)
				if (di != 0 || dj != 0)
					if (i+di >= 0 && i+di < image.data.length && j+dj >= 0 && j+dj < image.data[0].length)
						neighbors.add(image.data[i+di][j+dj]);
	}

	public void beforeStart(LabeledImage image, LabeledImage.Label label)
	{
		this.neighbors.clear();
		this.deleted = false;
	}

	public void afterEnd(LabeledImage image, LabeledImage.Label label)
	{
		if (neighbors.contains(background))
			return;
		
		for (int i=0;i<image.data.length;i++)
			for (int j=0;j<image.data[0].length;j++)
				if (image.data[i][j] == label)
					image.data[i][j] = background;
		
		background.components.addAll(label.components);
		background.size += label.size;
		image.labels.remove(label);
		
		deleted = true;
	}
}
