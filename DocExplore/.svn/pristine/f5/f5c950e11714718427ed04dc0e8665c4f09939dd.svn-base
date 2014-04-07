package org.interreg.docexplore.reader.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.interreg.docexplore.reader.InputManager;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.book.roi.SwingRenderer;
import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gfx.Texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;

public class Dialog extends Widget implements InputManager.Listener 
{
	ReaderApp app;
	
	String title, message;
	String [] buttons;
	
	BufferedImage buffer, backBuffer, buttonBuffer;
	Texture texture;
	float tmax;
	
	int height, titleAndMessageHeight;
	int [] buttonHeights;
	
	boolean ready = false;
	
	String defaultStyle, titleStyle, messageStyle;
	
	public Dialog(ReaderApp app)
	{
		this.app = app;
		this.buffer = new BufferedImage(2*Gdx.graphics.getWidth()/3, Gdx.graphics.getHeight(), BufferedImage.TYPE_INT_ARGB);
		this.backBuffer = new BufferedImage(buffer.getWidth(), buffer.getHeight(), BufferedImage.TYPE_INT_ARGB);
		this.buttonBuffer = new BufferedImage(Gdx.graphics.getWidth()/10, Gdx.graphics.getHeight()/10, BufferedImage.TYPE_INT_ARGB);
		
		defaultStyle = "font-family:Arial;font-weight:bold;color:white;";
		titleStyle = defaultStyle + "text-align:left;font-size:"+(int)(buffer.getWidth()/32)+"px;margin:"+(int)(buffer.getWidth()/48)+"px;";
		messageStyle = defaultStyle + "font-size:"+(int)(buffer.getWidth()/48)+"px;margin:"+(int)(buffer.getWidth()/32)+"px;";
		
		app.submitRenderTaskAndWait(new Runnable() {public void run() {texture = new Texture(buffer.getWidth(), buffer.getHeight(), true, false);}});
	}
	
	static Color background = new Color(0, 0, 0, 0);
	static SwingRenderer renderer = null;
	
	public void set(final String title, final String message, final String ... buttons)
	{
		this.ready = false;
		
		this.title = title;
		this.message = message;
		this.buttons = buttons;
		this.clickedButton = -1;
		
		new Thread()
		{
			public void run()
			{
				try
				{
					String html = "<html>" +
						(title != null ? "<div style=\"" + titleStyle + "\">"+title+"</div>" : "") +
						"<div style=\"" + messageStyle + "\">"+message+"</div>" +
						"</html>";
//					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
//						new ByteArrayInputStream((html).getBytes("UTF-8")));
					Graphics2D g = buffer.createGraphics();
					g.setBackground(background);
					g.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
					
					if (renderer == null)
						renderer = new SwingRenderer();
					titleAndMessageHeight = renderer.render(html, buffer, background, 0);
					
					int buth = 0;
					buttonHeights = new int [buttons.length];
					for (int i=0;i<buttons.length;i++)
					{
						html = "<html>" +
							"<center><div style=\"" + messageStyle + "\">"+buttons[i]+"</div></center>" +
							"</html>";
//						doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
//							new ByteArrayInputStream((html).getBytes("UTF-8")));
						buttonHeights[i] = renderer.render(html, buttonBuffer, background, 0);
						if (buttonHeights[i] > buth)
							buth = buttonHeights[i];
						int x0 = (i+1)*buffer.getWidth()/(buttons.length+1);
						g.drawImage(buttonBuffer, x0-buttonBuffer.getWidth()/2, titleAndMessageHeight, x0+buttonBuffer.getWidth()/2, titleAndMessageHeight+buttonHeights[i], 
							0, 0, buttonBuffer.getWidth(), buttonHeights[i], null);
						g.setColor(Color.white);
						g.setStroke(GuiLayer.defaultStroke);
						g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g.drawRoundRect(x0-buttonBuffer.getWidth()/2, titleAndMessageHeight, buttonBuffer.getWidth(), buttonHeights[i], 20, 20);
					}
					
					height = titleAndMessageHeight+(buttons.length > 0 ? buth+buffer.getWidth()/32 : 0);
					tmax = height*1f/buffer.getHeight();
					
					g = backBuffer.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setBackground(background);
					g.clearRect(0, 0, backBuffer.getWidth(), backBuffer.getHeight());
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setColor(new Color(1, 1, 1, .1f));
					g.fillRoundRect(2, 2, backBuffer.getWidth()-4, height-4, 30, 30);
					g.setStroke(GuiLayer.thickStroke);
					g.setColor(Color.white);
					g.drawRoundRect(2, 2, backBuffer.getWidth()-4, height-4, 30, 30);
					g.drawImage(buffer, 0, 0, null);
					
					texture.setup(backBuffer);
					app.submitRenderTaskAndWait(new Runnable() {public void run() {texture.update();}});
					ready = true;
				}
				catch (Exception e) {e.printStackTrace();}
			}
		}.start();
	}
	
	public void updateWidget() {}
	public void renderWidget()
	{
		if (!ready)
			return;
		
		GL10 gl = Gdx.gl10;
		
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_STENCIL_TEST);
		
		int x0 = (Gdx.graphics.getWidth()-buffer.getWidth())/2, y0 = (Gdx.graphics.getHeight()-height)/2;
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glColor4f(1, 1, 1, .75f*alpha);
		GfxUtils.fillQuad(0, 0, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0);
		//gl.glColor4f(1, 1, 1, .9f*alpha);
		//GfxUtils.doQuad(x0, y0, 0, 0, x0+buffer.getWidth(), y0+height, 0, 0);
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		texture.bind();
		gl.glColor4f(GuiLayer.defaultColor[0], GuiLayer.defaultColor[1], GuiLayer.defaultColor[2], alpha);
		GfxUtils.fillQuad(x0, y0, 0, 0, x0+buffer.getWidth(), y0+height, 1, tmax);
		
		if (clickedButton >= 0)
		{
			float bx1 = (clickedButton+1)*buffer.getWidth()*1f/(buttons.length+1)-.5f*buttonBuffer.getWidth();
			float by1 = titleAndMessageHeight;
			float bx2 = bx1+buttonBuffer.getWidth();
			float by2 = by1+buttonHeights[clickedButton];
			float s1 = bx1/texture.width(), t1 = by1/texture.height(), s2 = bx2/texture.width(), t2 = by2/texture.height();
			//float halpha = alpha < .5 ? 12*alpha*alpha-16*alpha*alpha*alpha : 16*alpha*alpha*alpha-36*alpha*alpha+24*alpha-4;
			float halpha = 1-(1-alpha)*(1-alpha)*(1-alpha)*(1-alpha);
			gl.glColor4f(GuiLayer.defaultHighlightColor[0], GuiLayer.defaultHighlightColor[1], GuiLayer.defaultHighlightColor[2], halpha);
			float boost = 0;
			GfxUtils.fillQuad(x()+bx1-boost, y()+by1-boost, s1, t1, x()+bx2+2*boost, y()+by2+2*boost, s2, t2);
		}
		
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	Object selectionMonitor = new Object();
	public int waitForClick() throws InterruptedException
	{
		synchronized (selectionMonitor)
		{
			selectionMonitor.wait();
			return clickedButton;
		}
	}
	
	int clickedButton = -1;
	public void doClick(int button) {synchronized (selectionMonitor) {clickedButton = button; selectionMonitor.notifyAll();}}
	public void clicked(float x, float y)
	{
		if (!active)
			return;
		y -= titleAndMessageHeight;
		for (int i=0;i<buttons.length;i++)
		{
			int xb0 = (i+1)*buffer.getWidth()/(buttons.length+1)-buttonBuffer.getWidth()/2;
			if (x>xb0 && x<xb0+buttonBuffer.getWidth() && y>0 && y<buttonHeights[i])
			{
				synchronized (selectionMonitor) {clickedButton = i; selectionMonitor.notifyAll();}
				break;
			}
		}
	}
	public boolean typed(int key)
	{
		return false;
	}
	public boolean scrolled(int amount) {return false;}
	public boolean hasButtonAt(float x, float y)
	{
		if (!active)
			return false;
		y -= titleAndMessageHeight;
		for (int i=0;i<buttons.length;i++)
		{
			int xb0 = (i+1)*buffer.getWidth()/(buttons.length+1)-buttonBuffer.getWidth()/2;
			if (x>xb0 && x<xb0+buttonBuffer.getWidth() && y>0 && y<buttonHeights[i])
				return true;
		}
		return false;
	}

	float x() {return (Gdx.graphics.getWidth()-buffer.getWidth())/2;}
	float y() {return (Gdx.graphics.getHeight()-height)/2;}
	public float getX() {return active ? x() : -1;}
	public float getY() {return active ? y() : -1;}

	public float getWidth() {return active ? buffer.getWidth() : 0;}
	public float getHeight() {return active ? height : 0;}
	
	public void command(String command)
	{
		if (buttons.length != 2)
			return;
		if (command.equals("left"))
			doClick(0);
		else if (command.equals("right"))
			doClick(1);
	}
	//unused input methods. This classes uses standard widget input
	public boolean touched(int x, int y, int pointer, int button) {return false;}
	public boolean grabbed(int x, int y, int pointer, int button) {return false;}
	public boolean dragged(int fx, int fy, int tx, int ty, int pointer, int button) {return false;}
	public boolean dropped(int fx, int fy, int tx, int ty, int pointer, int button) {return false;}
	public boolean clicked(int x, int y, int pointer, int button) {return false;}
	public Object objectAt(int x, int y) {return null;}
	public void useConfig(Map<String, Object> config) {}
}
