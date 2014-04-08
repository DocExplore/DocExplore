/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
