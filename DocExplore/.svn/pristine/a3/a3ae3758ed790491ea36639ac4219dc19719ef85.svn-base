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
