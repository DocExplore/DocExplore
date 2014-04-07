package org.interreg.docexplore.reader.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.interreg.docexplore.reader.book.roi.SwingRenderer;

import com.badlogic.gdx.Gdx;

public abstract class Widget
{
	boolean active = false;
	
	public float getX() {return -1;}
	public float getY() {return -1;}
	public float getWidth() {return 0;}
	public float getHeight() {return 0;}
	
	public void activate(boolean active)
	{
		if (this.active == active)
			return;
		this.active = active;
	}
	public boolean isActive() {return active;}
	
	public float alpha = 0;
	public void update()
	{
		if (active) alpha = Math.min(alpha+.02f, 1);
		else alpha = Math.max(alpha-.02f, 0);
		updateWidget();
	}
	public void render()
	{
		if (alpha == 0)
			return;
		renderWidget();
	}
	
	public abstract void updateWidget();
	public abstract void renderWidget();
	
	public void clicked(float x, float y) {}
	public void held(float x, float y) {}
	public void dragged(float x, float y) {}
	public void dropped(float x, float y) {}
	
	static String defaultStyle, messageStyle;
	static BufferedImage buffer = null;
	static Color background = new Color(0, 0, 0, 0);
	static SwingRenderer renderer = null;
	public static synchronized BufferedImage renderText(BufferedImage icon, String message, int w)
	{
		if (buffer == null)
		{
			buffer = new BufferedImage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()/2, BufferedImage.TYPE_INT_ARGB);
			defaultStyle = "font-family:Arial;font-weight:bold;color:white;";
			messageStyle = defaultStyle + "font-size:"+(int)(buffer.getWidth()/84)+"px;margin:"+(int)(buffer.getWidth()/192)+"px;";
		}
		
		String html = "<html>" +
			"<div style=\"" + messageStyle + "\">"+message+"</div>" +
			"</html>";
		
		Graphics2D g = buffer.createGraphics();
		g.setBackground(background);
		g.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
		
		if (renderer == null)
			renderer = new SwingRenderer();
		int textw = Math.min(buffer.getWidth(), w-icon.getWidth());
		int h = renderer.render(html, buffer, textw, background, 0, false);
		
		BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int i=0;i<icon.getWidth();i++)
			for (int j=0;j<Math.min(h, icon.getHeight());j++)
				res.setRGB(i, j, icon.getRGB(i, j));
		for (int i=0;i<textw;i++)
			for (int j=0;j<h;j++)
				res.setRGB(icon.getWidth()+i, j, buffer.getRGB(i, j));
		return res;
	}
}
