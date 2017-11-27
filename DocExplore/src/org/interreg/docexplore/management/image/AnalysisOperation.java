/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.image;

import java.awt.image.BufferedImage;

import org.interreg.docexplore.internationalization.XMLResourceBundle;

public class AnalysisOperation extends RectROIOperation
{
	public AnalysisOperation()
	{
		super();
	}
	
	@Override public void pointDropped(PageEditor view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers)
	{
		second.setLocation(Math.max(0, Math.min(view.getImage().getWidth()-1, (int)(vx+.5))), Math.max(0, Math.min(view.getImage().getHeight()-1, (int)(vy+.5))));
		
		int x = Math.min(first.x, second.x), y = Math.min(first.y, second.y);
		int w = Math.abs(first.x-second.x), h = Math.abs(first.y-second.y);
		BufferedImage area = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		area.createGraphics().drawImage(view.getImage().getSubimage(x, y, w, h), 0, 0, null);
		view.getHost().onAnalysisRequest(area);
		
		this.first = null;
		this.second = null;
	}
	
	public String getMessage() {return XMLResourceBundle.getBundledString("statusAnalysisMessage");}
}
