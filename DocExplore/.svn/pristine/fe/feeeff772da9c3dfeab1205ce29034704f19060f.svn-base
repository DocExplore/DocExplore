package org.interreg.docexplore.reader.gui;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.reader.InputManager;
import org.interreg.docexplore.reader.ReaderApp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;

public class GuiLayer implements ReaderApp.Module, InputManager.Listener
{
	ReaderApp app;
	List<Widget> widgets;
	
	public GuiLayer(ReaderApp app)
	{
		this.app = app;
		this.widgets = new LinkedList<Widget>();
	}
	
	public void addWidget(Widget widget) {widgets.add(widget);}
	public void removeWidget(Widget widget) {widgets.remove(widget);}
	
	public void update()
	{
		for (Widget widget : widgets)
			widget.update();
	}

	public void render()
	{
		GL10 gl = Gdx.gl10;
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -1, 1);
		
		for (Widget widget : widgets)
			widget.render();
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
	}
	
	public Widget widgetAt(int x, int y)
	{
		for (Widget widget : widgets)
		{
			float wx = widget.getX(), wy = widget.getY(), ww = widget.getWidth(), wh = widget.getHeight();
			if (x>wx && y>wy && x<wx+ww && y<wy+wh)
				return widget;
		}
		return null;
	}
	public Object objectAt(int x, int y)
	{
		for (Widget widget : widgets)
		{
			float wx = widget.getX(), wy = widget.getY(), ww = widget.getWidth(), wh = widget.getHeight();
			if (x>wx && y>wy && x<wx+ww && y<wy+wh)
			{
				if (widget instanceof Button)
					return widget;
				if (widget instanceof Dialog && ((Dialog)widget).hasButtonAt(x-wx, y-wy))
					return widget;
			}
		}
		return null;
	}
	public void useConfig(Map<String, Object> config) {}
	public void command(String command) {}

	public boolean touched(int x, int y, int pointer, int button)
	{
		Widget widget = widgetAt(x, y);
		if (widget == null)
			return false;
		widget.held(x-widget.getX(), y-widget.getY());
		return true;
	}
	public boolean grabbed(int x, int y, int pointer, int button)
	{
		Widget widget = widgetAt(x, y);
		if (widget == null)
			return false;
		//widget.held(x-widget.getX(), y-widget.getY());
		return true;
	}
	public boolean dragged(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		Widget widget = widgetAt(fx, fy);
		if (widget == null)
			return false;
		widget.dragged(tx-widget.getX(), ty-widget.getY());
		return true;
	}
	public boolean dropped(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		return false;
//		Widget widget = widgetAt(tx, ty);
//		if (widget == null)
//			return false;
//		widget.dropped(tx-widget.getX(), ty-widget.getY());
//		return true;
	}
	public boolean clicked(int x, int y, int pointer, int button)
	{
		Widget widget = widgetAt(x, y);
		if (widget == null)
			return false;
		widget.clicked(x-widget.getX(), y-widget.getY());
		return true;
	}
	public boolean typed(int key)
	{
		return false;
	}
	public boolean scrolled(int amount) {return false;}
	
	public static Stroke defaultStroke = new BasicStroke(2);
	public static Stroke thickStroke = new BasicStroke(4);
	public static float [] defaultColor = new float [] {.2f, .3f, 1};
	public static float [] defaultHighlightColor = new float [] {.6f, .9f, 1};
}
