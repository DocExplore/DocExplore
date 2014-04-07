package org.interreg.docexplore.reader.shelf;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.reader.InputManager;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gfx.Texture;
import org.interreg.docexplore.reader.gui.GuiEvent;
import org.interreg.docexplore.util.Pair;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL10;

public class ShelfEngine implements InputManager.Listener, ReaderApp.Module, GuiEvent.Source
{
	public ReaderApp app;
	public ShelfSpecification shelf;
	public boolean active;
	
	BufferedImage entryBuffer;
	//Texture back;
	Texture [] textures;

	public ShelfEngine(ReaderApp app)
	{
		this.app = app;
		this.shelf = null;
		this.textures = null;
		this.entryBuffer = new BufferedImage(Gdx.graphics.getWidth()/9, Gdx.graphics.getHeight(), BufferedImage.TYPE_INT_ARGB);
		this.active = false;
	}
	
	int cursor = 0;
	public void setShelf(final ShelfSpecification shelf)
	{
		this.shelf = shelf;
		
		if (textures != null)
			app.submitRenderTaskAndWait(new Runnable() {public void run()
			{
				for (int i=0;i<textures.length;i++)
					if (textures[i] != null)
						textures[i].dispose();
			}});
		
		synchronized (this)
		{
			this.textures = new Texture [shelf.entries.size()];
			for (int i=0;i<textures.length;i++)
				textures[i] = null;
		}
		
		new Thread()
		{
			public void run()
			{
				for (int i=0;i<textures.length;i++)
				{
					GfxUtils.clear(entryBuffer, new Color(0, 0, 0, 0).getRGB());
					final int index = i;
					final int height = shelf.entries.get(i).renderTo(entryBuffer);
					app.submitRenderTaskAndWait(new Runnable() {public void run()
					{
						textures[index] = new Texture(entryBuffer.getWidth(), height, true, false);
						textures[index].setup(entryBuffer);
						textures[index].update();
					}});
				}
			}
		}.start();
		
		cursor = 0;
	}
	
	public void activate(boolean active)
	{
		if (this.active == active)
			return;
		this.active = active;
		
		if (active)
		{
			app.input.addListener(this);
		}
		else
		{
			app.input.removeListener(this);
		}
	}
	
	public synchronized void left()
	{
		if (textures == null || textures.length == 0)
			cursor = -1;
		else
		{
			cursor = (cursor+textures.length-1)%textures.length;
			offset += -1;
			
		}
	}
	public synchronized void right()
	{
		if (textures == null || textures.length == 0)
			cursor = -1;
		else
		{
			cursor = (cursor+1)%textures.length;
			offset += 1;
		}
	}
	public ShelfSpecification.Entry selectedEntry() {return cursor < 0 || shelf == null || shelf.entries == null ? null : shelf.entries.get(cursor);}
	
	int lastCursor = -1;
	public void update()
	{
		alpha = Math.max(0, Math.min(1, alpha+(active ? .02f : -.02f)));
		
		if (!dragging && textures != null && textures.length > 0)
		{
			offset += voffset;
			offset += -.05*offset;
			if (Math.abs(voffset) > .001)
			{
				int dec = (int)Math.round(offset);
				cursor = (cursor-dec)%textures.length;
				cursor = cursor < 0 ? textures.length-(-cursor)%textures.length : cursor;
				offset -= dec;
			}
		}
		voffset *= .9;
		
		if (cursor != lastCursor)
		{
			ShelfSpecification.Entry entry = selectedEntry();
			app.logger.addEntry("Current shelf entry: "+(entry == null ? "none" : entry.title+" ("+entry.src+")"));
			lastCursor = cursor;
		}
	}
	
	float [] defaultColor = {.7f, .7f, .7f};
	float [] selectColor = {.2f, .3f, 1};
	double offset = 0;
	float alpha = 0;
	public synchronized  void render()
	{
		if (alpha < .01)
			return;
		
		GL10 gl = Gdx.gl10;
		
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_STENCIL_TEST);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -1, 1);
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		if (textures == null)
			cursor = -1;
		else if (cursor > textures.length)
			cursor = textures.length-1;
		if (cursor >= 0)
		{
			for (int i=0;i<textures.length;i++)
			{
				int j = (textures.length/2+(i+1)/2*(i%2 == 0 ? 1 : -1))%textures.length;
				int index = (cursor+j)%textures.length;
				Texture tex = textures[index];
				if (tex != null)
				{
					double x = getEntryX(j);
					double y = getEntryY(j);
					float k = (float)(.75+.125*(y+1));
					k *= k;
					//float colk = (float)(.5*(y+1));
					gl.glColor4f(1, 1, 1, alpha*k);
					float x0 = (float)(.5*(Gdx.graphics.getWidth()-k*tex.width())+x*Gdx.graphics.getWidth()/4);
					float y0 = (float)(.5*(Gdx.graphics.getHeight()-k*tex.height())+y*Gdx.graphics.getHeight()/5);
					tex.bind();
					GfxUtils.fillQuad(x0, y0, 0, 0, x0+k*tex.width(), y0+k*tex.height(), 1, 1);
				}
			}
		}
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
		
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
	
	double getEntryX(int index) {return 1.5*Math.sin((index+offset)*2*Math.PI/textures.length);}
	double getEntryY(int index)
	{
		double y = Math.cos((index+offset)*2*Math.PI/textures.length);
		return y;//2*Math.pow(.5*(y+1), 8)-1;
	}
	
	boolean getEntryBounds(int j, float [] bounds)
	{
		if (textures == null)
			return false;
		
		Texture tex = textures[j];
		if (tex == null)
			return false;
		
		j += textures.length-cursor;
		double x = getEntryX(j);
		double y = getEntryY(j);
		float k = (float)(.75+.125*(y+1));
		k *= k;
		bounds[0] = (float)(.5*(Gdx.graphics.getWidth()-k*tex.width())+x*Gdx.graphics.getWidth()/4);
		bounds[1] = (float)(.5*(Gdx.graphics.getHeight()-k*tex.height())+y*Gdx.graphics.getHeight()/5);
		bounds[2] = bounds[0]+k*tex.width();
		bounds[3] = bounds[1]+k*tex.height();
		return true;
	}
	
	ShelfSpecification.Entry selectedEntry = null;
	float [] bounds = {0, 0, 0, 0};
	public boolean clicked(int x, int y, int pointer, int button)
	{
		for (int i=0;i<shelf.entries.size();i++)
			if (getEntryBounds(i, bounds) && x > bounds[0] && x < bounds[2] && y > bounds[1] && y < bounds[3])
		{
			if (cursor == i)
			{
				ShelfSpecification.Entry entry = selectedEntry();
				if (entry != null)
					doEvent(entry);
			}
			else
			{
				offset += (i+textures.length-cursor)%textures.length;
				offset = offset > textures.length/2 ? offset-textures.length : offset;
				cursor = i;
			}
			break;
		}
		return true;
	}
	public boolean typed(int key)
	{
		if (!active)
			return false;
		if (key == Input.Keys.ESCAPE)
			doEvent(null);
		else if (key == Input.Keys.LEFT)
			app.mainTask.right.doClick();
		else if (key == Input.Keys.RIGHT)
			app.mainTask.left.doClick();
		return false;
	}
	public boolean scrolled(int amount) {return false;}
	public Object objectAt(int x, int y)
	{
		return null;
	}

	public void useConfig(Map<String, Object> config) {}
	public void command(String command)
	{
		if (command.equals("left"))
			app.mainTask.right.doClick();
		else if (command.equals("right"))
			app.mainTask.left.doClick();
		else if (command.equals("ok"))
		{
			ShelfSpecification.Entry entry = selectedEntry();
			if (entry != null)
				doEvent(entry);
		}
	}
	public boolean touched(int x, int y, int pointer, int button) {return true;}
	boolean dragging = false;
	double offsetOri;
	double voffset = 0;
	public boolean dragged(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		if (textures != null && textures.length > 0)
		{
			if (dragging == false)
				{offsetOri = offset; voffset = 0;}
			dragging = true;
			voffset += .25*(offsetOri+.003*(tx-fx)-offset);
			offset = offsetOri+.003*(tx-fx);
		}
		return true;
	}

	public boolean dropped(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		if (dragging)
		{
			dragging = false;
			int dec = (int)Math.round(offset);
			cursor = (cursor-dec)%textures.length;
			cursor = cursor < 0 ? textures.length-(-cursor)%textures.length : cursor;
			offset -= dec;
		}
		return true;
	}

	public boolean grabbed(int x, int y, int pointer, int button)
	{
		return true;
	}
	
	void doEvent(Object object)
	{
		ActionEvent event = new ActionEvent(new Pair<Object, Object>(this, object), 0, null);
		synchronized (currentMonitor)
		{
			for (ActionListener listener : listeners)
				listener.actionPerformed(event);
			currentMonitor.notifyAll();
		}
	}
	
	Object instanceMonitor = new Object();
	Object currentMonitor = instanceMonitor;
	public Object getDefaultMonitor() {return instanceMonitor;}
	public Object getCurrentMonitor() {return currentMonitor;}
	public void setCurrentMonitor(Object o) {currentMonitor = o;}
	public Object waitForEvent() throws InterruptedException {synchronized (currentMonitor) {currentMonitor.wait();} return selectedEntry;}
	
	List<ActionListener> listeners = new LinkedList<ActionListener>();
	public void addActionListener(ActionListener listener) {listeners.add(listener);}
	public void removeActionListener(ActionListener listener) {listeners.remove(listener);}
}
