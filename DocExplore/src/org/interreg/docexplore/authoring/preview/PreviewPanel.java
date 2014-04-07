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
