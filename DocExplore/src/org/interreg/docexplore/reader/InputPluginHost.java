/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader;

import java.awt.image.BufferedImage;

import org.interreg.docexplore.reader.book.ROISpecification;

public interface InputPluginHost
{
	public int getDisplayWidth();
	public int getDisplayHeight();
	
	public boolean touchDown(int x, int y, int pointer, int button);
	public boolean touchDragged(int x, int y, int pointer);
	public boolean touchUp(int x, int y, int pointer, int button);
	public void generateClick(int x, int y, int pointer);
	
	public void setCursor(int x, int y);
	public void setCursor(final BufferedImage image, final int hsx, final int hsy);
	
	public Object objectAtMouse();
	public void addToConfig(String key, Object value);
	public void sendCommand(String command);
	public void setAutoGenerateClicks(boolean b);
	public void useStandardInput(boolean b);
	public String getReaderState();
	
	public float [] fromPageToScreen(boolean left, float x, float y);
	
	public static interface LayoutListener
	{
		public void layoutChanged(ROISpecification [] rois);
	}
	public void addLayoutListener(LayoutListener listener);
	public void removeLayoutListener(LayoutListener listener);
	public void notifyLayoutChange(ROISpecification [] rois);
	
	public void addRenderable(Graphics.Renderable renderable);
	public void removeRenderable(Graphics.Renderable renderable);
	public void setCustomLabel(BufferedImage image);
}
