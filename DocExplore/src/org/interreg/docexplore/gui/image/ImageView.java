package org.interreg.docexplore.gui.image;

import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

public class ImageView extends EditorView
{
	private static final long serialVersionUID = -7152689573523551765L;
	
	protected BufferedImage image = null;
	
	public ImageView() {this(null);}
	public ImageView(EditorView.Operation<? extends ImageView> defaultOperation)
	{
		super(defaultOperation);
		
		addComponentListener(new ComponentAdapter()
		{
			@Override public void componentResized(ComponentEvent e)
			{
				if (getScale() == 0)
					fit();
			}
		});
	}
	
	public void setImage(BufferedImage image) {setImage(image, true);}
	public void setImage(BufferedImage image, boolean fit)
	{
		this.image = image;
		if (fit)
			fit();
	}
	public BufferedImage getImage() {return image;}
	
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		if (image != null)
			g.drawImage(image, 0, 0, null);
		super.drawView(g, pixelSize);
	}
	
	public void fit()
	{
		if (image != null)
			fitView(0, 0, image.getWidth(), image.getHeight(), .05);
	}
}
