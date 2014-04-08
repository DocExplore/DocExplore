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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.interreg.docexplore.gui.text.SwingRenderer;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.StringUtils;

@SuppressWarnings("serial")
public abstract class PreviewPanel extends JDialog
{
	public PreviewPanel()
	{
		super(JOptionPane.getRootFrame(), false);
		setUndecorated(true);
		setAlwaysOnTop(true);
		addMouseListener(new MouseAdapter()
		{
			@Override public void mouseReleased(MouseEvent e)
			{
				if (isVisible())
				{
					setVisible(false);
					dispose();
				}
			}
		});
		addFocusListener(new FocusAdapter()
		{
			@Override public void focusLost(FocusEvent e)
			{
				setVisible(false);
				dispose();
			}
		});
	}
	
	public void dispose() {}
	
	public abstract void set(Object object);
	public void setPreview(Object object, int x, int y)
	{
		set(object);
		pack();
		setLocation(x-getPreferredSize().width/2, y-getPreferredSize().height/2);
		updateLocation();
	}
	
	void updateLocation()
	{
		int w = getPreferredSize().width, h = getPreferredSize().height;
		int x = getX(), y = getY();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		if (x+w > screen.width)
			x = screen.width-w;
		if (y+h > screen.height)
			y = screen.height-h;
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		setLocation(x, y);
	}
	
	public static void previewImage(BufferedImage image, int mx, int my)
	{
		ImagePreview preview = new ImagePreview();
		preview.setPreview(image, mx, my);
		preview.setVisible(true);
	}
	public static void previewPage(Page page, int mx, int my)
	{
		PagePreview preview = new PagePreview();
		preview.setPreview(page, mx, my);
		preview.setVisible(true);
	}
	public static void previewRegion(Region region, int mx, int my)
	{
		RegionPreview preview = new RegionPreview();
		preview.setPreview(region, mx, my);
		preview.setVisible(true);
	}
	public static SwingRenderer textRenderer = null;
	public static void previewText(String text, int mx, int my)
	{
		if (textRenderer == null)
			textRenderer = new SwingRenderer();
		BufferedImage image = textRenderer.getImage("<div style=\"font-size:18\">"+StringUtils.escapeXmlChars(text)+"</div>", 
			Toolkit.getDefaultToolkit().getScreenSize().width/3, Color.white);
		previewImage(image, mx, my);
	}
}
