package org.interreg.docexplore.gui.image;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.TimerTask;

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
	
	protected void constrain()
	{
		if (image != null)
		{
			x0 = Math.max(0, Math.min(getImageWidth(), x0));
			y0 = Math.max(0, Math.min(getImageHeight(), y0));
			scale = Math.max(.01, Math.min(20, scale));
		}
	}
	
	public void setImage(BufferedImage image) {setImage(image, true);}
	public void setImage(BufferedImage image, boolean fit)
	{
		this.image = image;
		if (fit)
			fit();
	}
	//public BufferedImage getImage() {return image;}
	public int getImageWidth() {return image != null ? image.getWidth() : -1;}
	public int getImageHeight() {return image != null ? image.getHeight() : -1;}
	public BufferedImage getSubImage(int x, int y, int w, int h) {return image.getSubimage(x, y, w, h);}
	
	Stroke outline = new BasicStroke(1f);
	float miniSizeFactor = .1f;
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		if (image != null)
		{
			drawImage(g, pixelSize);
			drawMini(g, pixelSize);
		}
		super.drawView(g, pixelSize);
	}
	
	protected void drawImage(Graphics2D g, double pixelSize)
	{
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(image, 0, 0, null);
	}
	
	float miniAlpha = 0;
	protected void drawMini(Graphics2D g, double pixelSize)
	{
		int iw = getImageWidth(), ih = getImageHeight();
		double l = Math.max(0, Math.min(1, toViewX(0)/iw)), r = Math.max(0, Math.min(1, toViewX(getWidth())/iw));
		double u = Math.max(0, Math.min(1, toViewY(0)/ih)), d = Math.max(0, Math.min(1, toViewY(getHeight())/ih));
		
		float talpha = (r-l)*(d-u) < 1 ? 1 : 0;
		if (talpha != miniAlpha)
			miniAlpha = Math.max(talpha < miniAlpha ? talpha : 0, Math.min(talpha > miniAlpha ? talpha : 1, miniAlpha+(talpha < miniAlpha ? -.1f : .1f)));
		
		if (miniAlpha > 0)
		{
			g.setTransform(defaultTransform);
			Stroke oldStroke = g.getStroke();
			g.setStroke(outline);
			Composite oldComposite = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, miniAlpha));
			
			float miniSize = miniSizeFactor*Math.min(getWidth(), getHeight());
			float mw = iw < ih ? miniSize : iw*miniSize/ih;
			float mh = iw < ih ? ih*miniSize/iw : miniSize;
			renderMini(g, (int)mw, (int)mh);
			
			g.setColor(Color.white);
			g.drawRect(0, 0, (int)mw, (int)mh);
			g.setColor(Color.blue);
			g.drawRect((int)(l*mw), (int)(u*mh), (int)((r-l)*mw), (int)((d-u)*mh));
			
			g.setComposite(oldComposite);
			g.setStroke(oldStroke);
			g.setTransform(viewTransform);
		}
		
		if (miniAlpha != talpha)
			new Thread(repaintTask).start(); 
	}
	protected void renderMini(Graphics2D g, int mw, int mh)
	{
		g.drawImage(image, 0, 0, mw, mh, null);
	}
	Runnable repaintTask = new TimerTask() {@Override public void run() {try {Thread.sleep(30);} catch (Exception e) {} repaint();}};
	
	public void fit()
	{
		if (image != null)
			fitView(0, 0, getImageWidth(), getImageHeight(), .05);
	}
}
