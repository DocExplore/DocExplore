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
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.imgscalr.Scalr;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.image.ImageView;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class ImagePreview extends PreviewPanel
{
	ImageView canvas;
	
	public ImagePreview()
	{
		canvas = new ImageView();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(canvas);
		panel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		add(panel);
		
		canvas.addMouseListener(new MouseAdapter()
		{
			@Override public void mousePressed(MouseEvent e)
			{
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == 0)
					return;
				if (ImagePreview.this.isVisible())
				{
					ImagePreview.this.setVisible(false);
					ImagePreview.this.dispose();
				}
			}
		});
	}
	
	@Override public void set(final Object object)
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				try
				{
					BufferedImage buffer = buildPreview(object);
					Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
					canvas.setPreferredSize(new Dimension(Math.min(buffer.getWidth(), 2*dim.width/3), Math.min(buffer.getHeight(), 2*dim.height/3)));
					Insets insets = getInsets();
					setPreferredSize(new Dimension(buffer.getWidth()+insets.left+insets.right, 
						buffer.getHeight()+insets.bottom+insets.top));
					pack();
					updateLocation();
					canvas.setImage(buffer);
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
