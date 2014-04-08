/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book.roi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.interreg.docexplore.reader.InputManager;
import org.interreg.docexplore.reader.book.BookEngine;
import org.interreg.docexplore.reader.book.ROISpecification;
import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gfx.Texture;
import org.interreg.docexplore.reader.gui.GuiEvent;
import org.interreg.docexplore.reader.plugin.ClientPlugin;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Pair;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;

public class ROIOverlay implements InputManager.Listener, GuiEvent.Source, BookEngine.Component
{
	public BookEngine engine;
	public float alpha;
	private float alphaGoal;
	float vmargin;
	List<OverlayElement> elements;
	
	float offset, maxOffset;
	float vOffset = 0;
	float startOffset;
	boolean scrolling = false;
	
	Texture up, down;
	
	public ROIOverlay(BookEngine engine)
	{
		this.engine = engine;
		this.alpha = alphaGoal = 0;
		this.offset = 0;
		this.maxOffset = 0;
		this.elements = new Vector<OverlayElement>();
		this.vmargin = .05f;
		
		setOffset(0);
		
		try
		{
			final BufferedImage upImage = ImageUtils.read(Thread.currentThread().getContextClassLoader().getResource(getClass().getPackage().getName().replace('.', '/')+"/up.png"));
			final BufferedImage downImage = ImageUtils.read(Thread.currentThread().getContextClassLoader().getResource(getClass().getPackage().getName().replace('.', '/')+"/down.png"));
			up = new Texture(upImage, false);
			down = new Texture(downImage, false);
		}
		catch (Exception e) {e.printStackTrace(); throw new RuntimeException(e);}
	}
	
	void setOffset(float newOffset)
	{
		vOffset = newOffset-this.offset;
		offset = newOffset;
		offset = offset < 0 ? 0 : offset > maxOffset ? maxOffset : offset;
	}
	
	int preferredWidth = 512;
	public void setup(ROISpecification region)
	{
		int scale = Math.max(1, (int)(Gdx.graphics.getWidth()/2f/preferredWidth+.5f));
		
		engine.app.logger.addEntry("Displaying roi "+region.page.regions.indexOf(region)+" of page "+region.page.pageNum);
		unsetup();
		if (region.contents.getContent("") != null)
			for (ROISpecification.InfoElement element : region.contents.getContent(""))
				try
				{
					boolean found = false;
					for (ClientPlugin plugin : engine.app.client.plugins)
					{
						if (plugin.buildOverlayElement(engine.app, elements, element, preferredWidth))
						{
							found = true;
							break;
						}
					}
					if (!found)
					{
						if (element instanceof ROISpecification.TextInfo)
							elements.add(new TextElement(this, (ROISpecification.TextInfo)element, preferredWidth, scale));
						else if (element instanceof ROISpecification.ImageInfo)
							elements.add(new ImageElement(this, (ROISpecification.ImageInfo)element));
					}
				}
				catch (Exception e) {e.printStackTrace();}
		offset = 0;
		vOffset = 0;
		maxOffset = 0;
	}
	
	public void unsetup()
	{
		for (OverlayElement element : elements)
			element.dispose();
		elements.clear();
	}
	
	public boolean grabbed(int x, int y, int pointer, int button)
	{
		startOffset = offset;
		scrolling = true;
		vOffset = 0;
		return true;
	}
	public boolean dragged(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		setOffset(startOffset+(fy-ty)*2.f/Gdx.graphics.getHeight());
		return true;
	}
	public boolean dropped(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		engine.app.logger.addEntry("roi scrolled to "+((int)(100*offset/maxOffset)));
		scrolling = false;
		return true;
	}
	public boolean clicked(int x, int y, int pointer, int button)
	{
		//System.out.println((x*2./Gdx.graphics.getWidth()-1));
		if (x*2./Gdx.graphics.getWidth()-1 > -2*offsetMargin)
		{
			float my = offset+y*2.f/Gdx.graphics.getHeight();
			float y0 = 0;
			for (OverlayElement element : elements)
			{
				float h = elementHeight(elementWidth(element), element);
				if (my > y0 && my < y0+h)
				{
					//System.out.println(element+" "+((x-Gdx.graphics.getWidth()/2)*2f/Gdx.graphics.getWidth())+","+((my-y0)/h));
					engine.app.logger.addEntry("roi element clicked : "+elements.indexOf(element));
					element.clicked((x-Gdx.graphics.getWidth()/2)*2f/Gdx.graphics.getWidth()+offsetMargin, (my-y0)/h);
					break;
				}
				y0 += h+vmargin*element.marginFactor();
			}
		}
		else activate(false);
		return true;
	}
	public boolean typed(int key)
	{
		if (!active)
			return false;
		if (key != Input.Keys.ESCAPE)
			return false;
		engine.app.mainTask.back.doClick();
		return true;
	}
	public boolean scrolled(int amount)
	{
		setOffset(offset+amount*.1f);
		return true;
	}
	public boolean touched(int x, int y, int pointer, int button) {return true;}
	public Object objectAt(int x, int y) {return null;}
	public void useConfig(Map<String, Object> config) {}
	public void command(String command)
	{
		if (command.equals("back"))
			engine.app.mainTask.back.doClick();
		else if (command.startsWith("set pos"))
		{
			float pos = Float.parseFloat(command.substring(8));
			pos = pos < 0 ? 0 : pos > 1 ? 1 : pos;
			setOffset(pos*maxOffset);
			vOffset = 0;
		}
	}
	
	public boolean leaving = false;
	public void update()
	{
		if (!active && alpha == 0)
		{
			if (leaving)
			{
				engine.renderer.setupLeftRoiMask();
				engine.renderer.setupRightRoiMask();
				unsetup();
				leaving = false;
			}
			return;
		}
		
		if (!scrolling)
		{
			offset += vOffset;
			vOffset *= .9f;
		}
		
		maxOffset = 0;
		for (OverlayElement element : elements)
			maxOffset += elementHeight(elementWidth(element), element)+vmargin*element.marginFactor();
		maxOffset -= 2;
		maxOffset = maxOffset < 0 ? 0 : maxOffset;
		offset = offset < 0 ? 0 : offset > maxOffset ? maxOffset : offset;
	}
	
	float elementWidth(OverlayElement element)
	{
		return element.getWidth(Gdx.graphics.getWidth()/2)*2f/Gdx.graphics.getWidth();
	}
	float elementHeight(float width, OverlayElement element)
	{
		return element.getHeight((int)(width*.5f*Gdx.graphics.getWidth()))*2f/Gdx.graphics.getHeight();
	}
	
	public boolean active = false;
	public void activate(boolean active)
	{
		if (this.active == active)
			return;
		this.active = active;
		
		if (active)
		{
			alphaGoal = 1;
			engine.app.input.addListenerFirst(this);
			engine.renderer.setupLeftRoiMask();
			engine.renderer.setupRightRoiMask();
			engine.attractor = engine.roiFrame;
		}
		else
		{
			engine.app.logger.addEntry("Exiting roi");
			alphaGoal = 0;
			engine.app.input.removeListener(this);
			leaving = true;
			engine.attractor = engine.globalFrame;
		}
		
		ActionEvent event = new ActionEvent(new Pair<GuiEvent.Source, Object>(this, active), 0, null);
		for (ActionListener listener : listeners)
			listener.actionPerformed(event);
		while (true)
			try {synchronized (currentMonitor) {currentMonitor.notifyAll(); break;}}
			catch (Exception e) {e.printStackTrace();}
	}
	public boolean isVisible() {return alpha > .03f;}
	
	float offsetMargin = .025f;
	public void render()
	{
		if (alpha < alphaGoal)
			alpha += .02f;
		else if (alpha > alphaGoal)
			alpha -= .02f;
		alpha = alpha < 0 ? 0 : alpha > 1 ? 1 : alpha;
		if (alpha == 0)
			return;
		GL10 gl = Gdx.gl10;
		
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_CULL_FACE);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(-1, 1, -1, 1, -1, 1);
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glColor4f(0, 0, 0, .75f*alpha);
		doQuad(-2*offsetMargin, 1, 1, -1);
		
		float y0 = 0;
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glColor4f(1, 1, 1, alpha);
		for (OverlayElement element : elements)
		{
			float w = elementWidth(element);
			float h = elementHeight(w, element);
			if (element.bind())
			{
				doQuad(-offsetMargin+.5f-.5f*w, 1-y0+offset, -offsetMargin+.5f+.5f*w, 1-y0-h+offset);
				y0 += h+vmargin*element.marginFactor();
			}
		}
		
		if (offset > 0)
		{
			up.bind();
			gl.glColor4f(1, 1, 1, .5f*alpha);
			float bw = .05f;
			doQuad(1-bw, 1, 1, 1-(bw*Gdx.graphics.getWidth()/Gdx.graphics.getHeight()));
		}
		if (offset < maxOffset)
		{
			down.bind();
			gl.glColor4f(1, 1, 1, .5f*alpha);
			float bw = .05f;
			doQuad(1-bw, (bw*Gdx.graphics.getWidth()/Gdx.graphics.getHeight())-1, 1, -1);
		}
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
		
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
	
	static Mesh quad = null;
	static void doQuad(float x1, float y1, float x2, float y2)
	{
		if (quad == null)
			quad = GfxUtils.buildQuad(0, 0, 0, 0, 1, 1, 1, 1);
		
		GL10 gl = Gdx.gl10;
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glTranslatef(x1, y1, 0);
		gl.glScalef(x2-x1, y2-y1, 1);
		quad.render(GL10.GL_TRIANGLES);
		gl.glPopMatrix();
	}

	Object instanceMonitor = new Object();
	Object currentMonitor = instanceMonitor;
	public Object getDefaultMonitor() {return instanceMonitor;}
	public Object getCurrentMonitor() {return currentMonitor;}
	public void setCurrentMonitor(Object o) {currentMonitor = o;}
	public Object waitForEvent() throws InterruptedException
	{
		 synchronized (currentMonitor) {currentMonitor.wait();}
		 return active;
	}

	List<ActionListener> listeners = new LinkedList<ActionListener>();
	public void addActionListener(ActionListener listener) {listeners.add(listener);}
	public void removeActionListener(ActionListener listener) {listeners.remove(listener);}
}
