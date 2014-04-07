package org.interreg.docexplore.reader;

import java.awt.image.BufferedImage;

public interface Graphics
{
	public static interface Renderable
	{
		public void render(Graphics g);
	}
	
	public int width();
	public int height();
	
	public void setColor(float r, float g, float b, float a);
	public void drawLine(double x1, double y1, double x2, double y2);
	public void setWidth(float f);
	public void fillTriangle(double x1, double y1, double x2, double y2, double x3, double y3);
	public void addImage(BufferedImage image, double x, double y, double w, double h);
	public void removeImage(BufferedImage image);
	
	public void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3);
	public void drawRect(double x1, double y1, double x2, double y2);
	public void fillRect(double x1, double y1, double x2, double y2);
}
