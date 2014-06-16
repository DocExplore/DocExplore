package org.interreg.docexplore.reader.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.reader.Graphics;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gfx.Texture;
import org.interreg.docexplore.util.Pair;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;

public class Slider extends Widget implements GuiEvent.Source
{
	public float val, rval;
	
	public Slider(float w, float h, float val)
	{
		this.w = w;
		this.h = h;
		this.val = this.rval = val;
		valChanged();
	}
	
	public float x = -1, y = -1, w = 0, h = 0;
	public float destx = -1, desty = -1;
	float [] color = new float [] {GuiLayer.defaultColor[0], GuiLayer.defaultColor[1], GuiLayer.defaultColor[2]}, 
		destColor = new float [] {GuiLayer.defaultColor[0], GuiLayer.defaultColor[1], GuiLayer.defaultColor[2]};
	public void setColor(float r, float g, float b) {color[0] = r; color[0] = g; color[0] = b;}
	public void setColor(float [] to) {color[0] = to[0]; color[0] = to[1]; color[0] = to[2]; destColor[0] = to[0]; destColor[1] = to[1]; destColor[2] = to[2];}
	public void setDestColor(float [] to) {destColor[0] = to[0]; destColor[1] = to[1]; destColor[2] = to[2];}
	public void setPosition(float x, float y) {this.x = x; this.y = y; this.destx = x; this.desty = y;}
	public void setDestPosition(float x, float y) {this.destx = x; this.desty = y;}
	public float getX() {return active ? x : -1;}
	public float getY() {return active ? y : -1;}
	public float getWidth() {return active ? w : 0;}
	public float getHeight() {return active ? h : 0;}
	
	public void updateWidget()
	{
		float k = .1f;
		x += k*(destx-x);
		y += k*(desty-y);
		color[0] += k*(destColor[0]-color[0]);
		color[1] += k*(destColor[1]-color[1]);
		color[2] += k*(destColor[2]-color[2]);
	}

	static Texture circle = null;
	static float margink = .5f;
	
	public void renderWidget()
	{
		rval += .1f*(val-rval);
		if (circle == null)
			circle = new Texture(GfxUtils.getImage("circle.png"), false);
		Gdx.gl10.glDisable(GL10.GL_DEPTH_TEST);
		Graphics g = ReaderApp.app.debugGfx;
		float margin = margink*h;
		g.setColor(color[0], color[1], color[2], .75f*alpha);
		g.setWidth(3);
		//g.drawRect(x+margin, y+.333*h, x+w-margin, y+.667*h);
		g.drawLine(x+margin, y+.5*h, x+w-margin, y+.5*h);
		float x0 = x+margin+rval*(w-2*margin);
		//g.fillRect(x0-margin, y, x0+margin, y+h);
		g.fillTexturedRect(circle, x0-margin, y+.5f*h-margin, x0+margin, y+.5f*h+margin, 0, 0, 1, 1);
		g.setColor(1, 1, 1, .75f*alpha);
		float fillk = .85f;
		g.fillTexturedRect(circle, x0-fillk*margin, y+.5f*h-fillk*margin, x0+fillk*margin, y+.5f*h+fillk*margin, 0, 0, 1, 1);
		Gdx.gl10.glEnable(GL10.GL_DEPTH_TEST);
	}
	
	public void clicked(float x, float y) {dropped(x, y);}
	public void held(float x, float y) {dragged(x, y);}
	public void dragged(float x, float y)
	{
		float margin = margink*h;
		float k = (x-margin)/(w-2*margin);
		val = k < 0 ? 0 : k > 1 ? 1 : k;
		valChanged();
	}
	public void dropped(float x, float y)
	{
		dragged(x, y);
		ActionEvent event = new ActionEvent(new Pair<Object, Object>(this, null), 0, null);
		synchronized (currentMonitor)
		{
			for (ActionListener listener : listeners)
				listener.actionPerformed(event);
			currentMonitor.notifyAll();
		}
	}
	
	public void valChanged() {}

	List<ActionListener> listeners = new LinkedList<ActionListener>();
	public void addActionListener(ActionListener listener) {listeners.add(listener);}
	public void removeActionListener(ActionListener listener) {listeners.remove(listener);}
	Object instanceMonitor = new Object();
	Object currentMonitor = instanceMonitor;
	public Object getDefaultMonitor() {return instanceMonitor;}
	public Object getCurrentMonitor() {return currentMonitor;}
	public void setCurrentMonitor(Object o) {currentMonitor = o;}
	public Object waitForEvent() throws InterruptedException {synchronized (currentMonitor) {currentMonitor.wait();} return null;}
}
