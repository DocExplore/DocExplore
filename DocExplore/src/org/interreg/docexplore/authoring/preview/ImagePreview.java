/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.preview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.imgscalr.Scalr;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class ImagePreview extends PreviewPanel
{
	BufferedImage buffer = null;
	JLabel canvas;
	
	int frameCnt = 0;
	
	public ImagePreview()
	{
		canvas = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				if (buffer == null)
				{
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.gray);
					int n = 5;
					int i = frameCnt%n;
					g.fillRect((getWidth()*(2*i+1))/(2*n+1), (getHeight()-getWidth()/(2*n+1))/2, getWidth()/(2*n+1), getWidth()/(2*n+1));
				}
				else g.drawImage(buffer, 0, 0, Math.min(buffer.getWidth(), getWidth()), Math.min(buffer.getHeight(), getHeight()), null);
			}
		};
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(canvas);
		panel.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		add(panel);
	}
	
	@Override public void set(final Object object)
	{
		canvas.setPreferredSize(new Dimension(200, 200));
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				try
				{
					BufferedImage buffer = buildPreview(object);
					Insets insets = getInsets();
					setPreferredSize(new Dimension(buffer.getWidth()+insets.left+insets.right, 
						buffer.getHeight()+insets.bottom+insets.top));
					pack();
					updateLocation();
					ImagePreview.this.buffer = buffer;
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
		}, null);
	}
	
	Dimension preferredSize(int w, int h)
	{
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		if (w > screen.width) {h = (h*screen.width)/w; w = screen.width;}
		if (h > screen.height) {w = (w*screen.height)/h; h = screen.height;}
		return new Dimension(w, h);
	}
	
	public BufferedImage buildPreview(Object object) throws Exception
	{
		BufferedImage image = (BufferedImage)object;
		Dimension dim = preferredSize(image.getWidth(), image.getHeight());
		return Scalr.resize(image, Scalr.Method.QUALITY, dim.width, dim.height);
	}
}
