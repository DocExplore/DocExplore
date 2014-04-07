package org.interreg.docexplore.reader;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.interreg.docexplore.reader.book.ROISpecification;
import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gfx.Texture;
import org.interreg.docexplore.reader.gui.Dialog;
import org.interreg.docexplore.reader.gui.GuiLayer;
import org.interreg.docexplore.reader.gui.Label;
import org.interreg.docexplore.util.Pair;
import org.lwjgl.input.Mouse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Vector3;

public class InputManager extends InputAdapter implements ReaderApp.Module, InputPluginHost
{
	public static interface Listener
	{
		public boolean touched(int x, int y, int pointer, int button);
		public boolean grabbed(int x, int y, int pointer, int button);
		public boolean dragged(int fx, int fy, int tx, int ty, int pointer, int button);
		public boolean dropped(int fx, int fy, int tx, int ty, int pointer, int button);
		public boolean clicked(int x, int y, int pointer, int button);
		public boolean typed(int key);
		public boolean scrolled(int amount);
		
		public Object objectAt(int x, int y);
		public void useConfig(Map<String, Object> config);
		public void command(String command);
	}
	
	LinkedList<Listener> listeners;
	ReaderApp app;
	Map<String, Object> config;
	Map<BufferedImage, Texture> cursors;
	Texture cursor;
	int [] cursorHotSpot;
	TimeoutMonitor timeout;
	
	public InputManager(ReaderApp app)
	{
		this.app = app;
		Gdx.input.setInputProcessor(this);
		this.listeners = new LinkedList<Listener>();
		this.config = new TreeMap<String, Object>();
		this.cursors = new HashMap<BufferedImage, Texture>();
		this.cursor = null;
		this.cursorHotSpot = new int [] {0, 0};
		this.timeout = new TimeoutMonitor(app);
		
		addMonitoredKey(Input.Keys.ESCAPE);
		addMonitoredKey(Input.Keys.LEFT);
		addMonitoredKey(Input.Keys.RIGHT);
		addMonitoredKey(Input.Keys.UP);
	}
	
	Object listenerMonitor = new Object();
	public void addListener(Listener listener) {listener.useConfig(config); synchronized (listenerMonitor) {listeners.add(listener);}}
	public void addListenerFirst(Listener listener) {listener.useConfig(config); synchronized (listenerMonitor) {listeners.addFirst(listener);}}
	public void moveListenerFirst(Listener listener) {synchronized (listenerMonitor) {listeners.remove(listener); listeners.addFirst(listener);}}
	public void removeListener(Listener listener) {synchronized (listenerMonitor) {listeners.remove(listener);}}
	
	int [][] dragOrigin = new int [8][4];

	public boolean touchMoved(int x, int y) {timeout.reset(); return false;}
	
	public boolean scrolled(int amount)
	{
		notifyScrolled(amount);
		return true;
	}
	
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		timeout.reset();
		dragOrigin[pointer][0] = x;
		dragOrigin[pointer][1] = y;
		dragOrigin[pointer][2] = button;
		dragOrigin[pointer][3] = 0;
		notifyTouched(x, y, pointer, button);
		return true;
	}

	int dragLim = 15;
	public boolean touchDragged(int x, int y, int pointer)
	{
		timeout.reset();
		int dx = x-dragOrigin[pointer][0];
		int dy = y-dragOrigin[pointer][1];
		if (dragOrigin[pointer][3] > 0)
			notifyDragged(dragOrigin[pointer][0], dragOrigin[pointer][1], x, y, pointer, dragOrigin[pointer][2]);
		else if (dx*dx+dy*dy > dragLim*dragLim)
		{
			dragOrigin[pointer][3] = 1;
			notifyGrabbed(dragOrigin[pointer][0], dragOrigin[pointer][1], pointer, dragOrigin[pointer][2]);
			notifyDragged(dragOrigin[pointer][0], dragOrigin[pointer][1], x, y, pointer, dragOrigin[pointer][2]);
		}
		return true;
	}

	boolean autoGenerateClicks = true;
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		timeout.reset();
		int dx = x-dragOrigin[pointer][0];
		int dy = y-dragOrigin[pointer][1];
		if (dx*dx+dy*dy > dragLim*dragLim)
			notifyDropped(dragOrigin[pointer][0], dragOrigin[pointer][1], x, y, pointer, dragOrigin[pointer][2]);
		else if (autoGenerateClicks) notifyClicked(x, y, pointer, dragOrigin[pointer][2]);
		return true;
	}
	
	public Object objectAtMouse()
	{
		synchronized (listenerMonitor)
		{
			int mx = Mouse.getX(), my = Gdx.graphics.getHeight()-Mouse.getY();
			for (Listener listener : listeners)
			{
				Object object = listener.objectAt(mx, my);
				if (object != null)
					return object;
				if (listener instanceof GuiLayer && ((GuiLayer)listener).widgetAt(mx, my) != null)
					return null;
			}
		}
		return null;
	}
	
	public void addToConfig(String key, Object value)
	{
		config.put(key, value);
		synchronized (listenerMonitor)
		{
			for (Listener listener : listeners)
				listener.useConfig(config);
		}
	}
	
	public void sendCommand(String command)
	{
		timeout.reset();
		synchronized (listenerMonitor)
		{
			for (Listener listener : listeners)
			{
				System.out.println("sending '"+command+"' to "+listener);
				try {listener.command(command);}
				catch (Throwable e) {e.printStackTrace();}
			}
		}
	}
	
	public void setAutoGenerateClicks(boolean b)
	{
		autoGenerateClicks = b;
	}
	public void generateClick(int x, int y, int pointer)
	{
		timeout.reset();
		notifyClicked(x, y, pointer, dragOrigin[pointer][2]);
	}
	public void useStandardInput(boolean b)
	{
		Gdx.input.setInputProcessor(b ? this : null);
	}
	
	public void notifyTouched(final int x, final int y, final int pointer, final int button)
	{
		timeout.reset();
		app.submitRenderTask(new Runnable() {public void run()
		{
			synchronized (listenerMonitor)
			{
				for (Listener listener : listeners)
					if (listener.touched(x, y, pointer, button))
						break;
			}
		}});
	}
	public void notifyGrabbed(final int x, final int y, final int pointer, final int button)
	{
		timeout.reset();
		app.submitRenderTask(new Runnable() {public void run()
		{
			synchronized (listenerMonitor)
			{
				for (Listener listener : listeners)
					if (listener.grabbed(x, y, pointer, button))
						break;
			}
		}});
	}
	public void notifyDragged(final int fx, final int fy, final int tx, final int ty, final int pointer, final int button)
	{
		timeout.reset();
		app.submitRenderTask(new Runnable() {public void run()
		{
			synchronized (listenerMonitor)
			{
				for (Listener listener : listeners)
					if (listener.dragged(fx, fy, tx, ty, pointer, button))
						break;
			}
		}});
	}
	public void notifyDropped(final int fx, final int fy, final int tx, final int ty, final int pointer, final int button)
	{
		timeout.reset();
		app.submitRenderTask(new Runnable() {public void run()
		{
			synchronized (listenerMonitor)
			{
				for (Listener listener : listeners)
					if (listener.dropped(fx, fy, tx, ty, pointer, button))
						break;
			}
		}});
	}
	public void notifyClicked(final int x, final int y, final int pointer, final int button)
	{
		timeout.reset();
		app.submitRenderTask(new Runnable() {public void run()
		{
			synchronized (listenerMonitor)
			{
				for (Listener listener : listeners)
					if (listener.clicked(x, y, pointer, button))
						break;
			}
		}});
	}
	public void notifyKeyTyped(final int key)
	{
		timeout.reset();
		app.submitRenderTask(new Runnable() {public void run()
		{
			synchronized (listenerMonitor)
			{
				for (Listener listener : listeners)
					if (listener.typed(key))
						break;
			}
		}});
	}
	public void notifyScrolled(final int amount)
	{
		timeout.reset();
		app.submitRenderTask(new Runnable() {public void run()
		{
			synchronized (listenerMonitor)
			{
				for (Listener listener : listeners)
					if (listener.scrolled(amount))
						break;
			}
		}});
	}
	
	public float [] fromPageToScreen(boolean left, float x, float y)
	{
		float [] pos = {0, 0, 0};
		y = 1-y;
		(left ? app.bookEngine.model.leftStack : app.bookEngine.model.rightStack).fromPage(x, y, pos);
		Vector3 vec = new Vector3(pos);
		app.bookEngine.camera.project(vec);
		pos[0] = vec.x;
		pos[1] = Gdx.graphics.getHeight()-vec.y;
		return pos;
	}
	
	List<LayoutListener> layoutListeners = new LinkedList<LayoutListener>();
	public void addLayoutListener(LayoutListener listener) {layoutListeners.add(listener);}
	public void removeLayoutListener(LayoutListener listener) {layoutListeners.remove(listener);}
	Class<?> elemClass = new String [0].getClass(), whereClass = new Point[0].getClass();
	public void notifyLayoutChange(ROISpecification [] rois)
	{
		for (LayoutListener listener : layoutListeners)
			try {listener.layoutChanged(rois);}
			catch (Throwable e) {e.printStackTrace();}
	}
	
	public int getDisplayWidth() {return Gdx.graphics.getWidth();}
	public int getDisplayHeight() {return Gdx.graphics.getHeight();}
	public void setCursor(int x, int y) {if (Mouse.isCreated()) Mouse.setCursorPosition(x, getDisplayHeight()-y);}
	
	public void addRenderable(Graphics.Renderable renderable) {app.debugGfx.addListener(renderable);}
	public void removeRenderable(Graphics.Renderable renderable) {app.debugGfx.removeListener(renderable);}
	
	public void setCursor(final BufferedImage image, final int hsx, final int hsy)
	{
		app.submitRenderTaskAndWait(new Runnable() {public void run()
		{
			cursor = cursors.get(image);
			if (cursor == null)
			{
				cursor = new Texture(image, false);
				cursors.put(image, cursor);
			}
			cursorHotSpot[0] = hsx;
			cursorHotSpot[1] = hsy;
		}});
	}
	
	public void render()
	{
		if (cursor == null)
			return;
			
		GL10 gl = Gdx.gl10;
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -1, 1);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glColor4f(1, 1, 1, 1);
		
		if (cursor != null)
		{
			cursor.bind();
			int my = Gdx.graphics.getHeight()-Mouse.getY();
			GfxUtils.fillQuad(Mouse.getX()-cursorHotSpot[0], my-cursorHotSpot[1], 0, 0, 
				Mouse.getX()-cursorHotSpot[0]+cursor.width(), my-cursorHotSpot[1]+cursor.height(), 1, 1);
		}
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
		app.debugGfx.render();
	}
	
	Vector<Pair<Integer, Boolean>> monitoredKeys = new Vector<Pair<Integer,Boolean>>();
	public void addMonitoredKey(int key) {monitoredKeys.add(new Pair<Integer, Boolean>(key, false));}
	
	public void update()
	{
		for (Pair<Integer, Boolean> pair : monitoredKeys)
		{
			boolean was = pair.second;
			pair.second = Gdx.input.isKeyPressed(pair.first);
			if (was && !pair.second)
				notifyKeyTyped(pair.first);
		}
	}
	
	Label customLabel = null;
	public void setCustomLabel(BufferedImage image)
	{
		if (image == null)
		{
			if (customLabel != null)
				customLabel.activate(false);
			return;
		}
		if (customLabel == null) try
		{
			customLabel = new Label(app, image, false, false);
			app.gui.addWidget(customLabel);
		}
		catch (Exception e) {e.printStackTrace();}
		else
		{
			customLabel.texture.setup(image);
			app.submitRenderTask(new Runnable() {public void run() {customLabel.texture.update();}});
		}
		customLabel.setPosition(.5f*(Gdx.graphics.getWidth()-customLabel.w), .5f*(Gdx.graphics.getHeight()-customLabel.h));
		customLabel.activate(true);
	}
	
	public String getReaderState()
	{
		if (listeners.getLast() instanceof Dialog)
			return "dialog";
		else if (app.shelf.active)
			return "shelf";
		else if (app.bookEngine.active)
		{
			if (app.bookEngine.roiOverlay.active)
				return "roi";
			else if (app.bookEngine.zoom.active)
				return "zoom";
			return "book";
		}
		return "";
	}
}
