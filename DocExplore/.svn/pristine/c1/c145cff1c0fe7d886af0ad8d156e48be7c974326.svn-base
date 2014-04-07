package org.interreg.docexplore.reader.book.roi;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.interreg.docexplore.reader.book.ROISpecification;
import org.interreg.docexplore.reader.gfx.Texture;
//TODO: change "load" -> "open"
public class TextElement implements OverlayElement
{
	ROIOverlay overlay;
	BufferedImage buffer;
	Texture texture;
	
	static Color background = new Color(0, 0, 0, 0);
	static Java2DRenderer renderer = null;
	static SwingRenderer srenderer = null;
	
	public TextElement(ROIOverlay overlay, ROISpecification.TextInfo text, int width, int scale) throws Exception
	{
		this.overlay = overlay;
		
		String html = text.getText();
		if (scale > 1)
		{
			int index = 0;
			while ((index = html.indexOf("<div style=\"", index)) >= 0)
			{
				int end = html.indexOf("\"", index+"<div style=\"".length());
				String div = html.substring(index, end);
						
				int tok = div.indexOf("margin-left:");
				if (tok > 0)
				{
					int tokend = div.indexOf("px", tok);
					div = div.substring(0, tok+"margin-left:".length())+(scale*Integer.parseInt(div.substring(tok+"margin-left:".length(), tokend)))+div.substring(tokend);
				}
				tok = div.indexOf("margin-right:");
				if (tok > 0)
				{
					int tokend = div.indexOf("px", tok);
					div = div.substring(0, tok+"margin-right:".length())+(scale*Integer.parseInt(div.substring(tok+"margin-right:".length(), tokend)))+div.substring(tokend);
				}
				tok = div.indexOf("font-size:");
				if (tok > 0)
				{
					int tokend = div.indexOf("px", tok);
					div = div.substring(0, tok+"font-size:".length())+(scale*Integer.parseInt(div.substring(tok+"font-size:".length(), tokend)))+div.substring(tokend);
				}
				html = html.substring(0, index)+div+html.substring(end);
				index = end;
			}
		}
		if (renderer == null)
		{
			renderer = new Java2DRenderer();
			srenderer = new SwingRenderer();
		}
		if (html.contains("<font"))
			this.buffer = srenderer.getImage(html, width, background);
		else this.buffer = renderer.getImage(html, width*scale, background);
		
		overlay.engine.app.submitRenderTask(new Runnable() {public void run()
		{
			texture = new Texture(buffer, false);
		}});
		
//		new Thread()
//		{
//			public void run()
//			{
//				JFrame win = new JFrame("test");
//				win.add(new JLabel(new ImageIcon(buffer)));
//				win.pack();
//				win.setVisible(true);
//			}
//		}.start();
	}
	
	public boolean bind()
	{
		if (texture == null)
			return false;
		texture.bind();
		return true;
	}

	public int getWidth(int maxWidth)
	{
		return maxWidth;
	}
	public int getHeight(int width)
	{
		return buffer.getHeight()*width/buffer.getWidth();
	}
	
	public void dispose() {if (texture != null) overlay.engine.app.submitRenderTask(
		new Runnable() {public void run() {texture.dispose();}});}
	
	public boolean clicked(float x, float y) {return false;}
	
	public float marginFactor() {return 1;}
}
