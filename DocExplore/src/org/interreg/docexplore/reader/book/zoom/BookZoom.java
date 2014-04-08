/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book.zoom;

import java.util.Map;

import org.interreg.docexplore.reader.InputManager;
import org.interreg.docexplore.reader.book.BookEngine;
import org.interreg.docexplore.reader.gfx.CameraKeyFrame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class BookZoom implements BookEngine.Component, InputManager.Listener
{
	BookEngine engine;
	CameraKeyFrame frame;
	float zoom = .5f;
	
	public BookZoom(BookEngine engine)
	{
		this.engine = engine;
		this.frame = new CameraKeyFrame();
		frame.setup(0, .5f*engine.model.cover.coverHeight, 2f*engine.model.cover.coverHeight, 
				0, .5f*engine.model.cover.coverHeight, 0, 
				0, 1, 0, 
				engine.globalFrame.fov);
	}
	
	public void increaseZoom(float amount)
	{
		zoom -= amount;
		this.zoom = zoom < 0 ? 0 : zoom > 1 ? 1 : zoom;
	}
	//zoom : [0:1]
	public void setZoom(float zoom)
	{
		this.zoom = zoom < 0 ? 0 : zoom > 1 ? 1 : zoom;
	}
	public void setPos(float x, float y)
	{
		x = x < 0 ? 0 : x > 1 ? 1 : x;
		y = y < 0 ? 0 : y > 1 ? 1 : y;
		frame.pos[0] = -engine.model.cover.coverLength+x*2*engine.model.cover.coverLength;
		frame.pos[1] = y*engine.model.cover.coverHeight;
	}
	public void incPos(float x, float y)
	{
		float x0 = (frame.pos[0]+engine.model.cover.coverLength)/(2*engine.model.cover.coverLength);
		float y0 = frame.pos[1]/engine.model.cover.coverHeight;
		setPos(x0+x, y0+y);
	}
	
	public boolean touched(int x, int y, int pointer, int button) {return true;}
	float grabbedx = 0, grabbedz = 0;
	public boolean grabbed(int x, int y, int pointer, int button)
	{
		grabbedx = engine.camera.position.x;
		grabbedz = engine.camera.position.y;
		return true;
	}

	public boolean dragged(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		ty = 2*fy-ty;
		float wx = engine.camera.position.x+(fx-Gdx.graphics.getWidth()/2)*1f/(Gdx.graphics.getWidth()/2);
		float wz = engine.camera.position.y+(fy-Gdx.graphics.getHeight()/2)*1f/(Gdx.graphics.getHeight()/2);
		wx *= engine.camera.position.z/(2*engine.model.cover.coverHeight);
		wz *= engine.camera.position.z/(2*engine.model.cover.coverHeight);
		
		float nx = engine.camera.position.x+(tx-Gdx.graphics.getWidth()/2)*1f/(Gdx.graphics.getWidth()/2);
		float nz = engine.camera.position.y+(ty-Gdx.graphics.getHeight()/2)*1f/(Gdx.graphics.getHeight()/2);
		nx *= engine.camera.position.z/(2*engine.model.cover.coverHeight);
		nz *= engine.camera.position.z/(2*engine.model.cover.coverHeight);
		
		frame.pos[0] = grabbedx+wx-nx;
		frame.pos[1] = grabbedz+wz-nz;
		//System.out.println(frame.pos[0]+","+frame.pos[1]);
		frame.pos[0] = frame.pos[0] < -engine.model.cover.coverLength ? -engine.model.cover.coverLength : 
			frame.pos[0] > engine.model.cover.coverLength ? engine.model.cover.coverLength : frame.pos[0];
		frame.pos[1] = frame.pos[1] < 0 ? 0 : frame.pos[1] > engine.model.cover.coverHeight ? engine.model.cover.coverHeight : frame.pos[1];
		
		return true;
	}
	
	public boolean typed(int key)
	{
		if (!active)
			return false;
		if (key == Input.Keys.ESCAPE)
			engine.app.mainTask.zoom.doClick();
		return true;
	}
	public boolean scrolled(int amount)
	{
		increaseZoom(amount*.1f);
		return true;
	}
	
	public Object objectAt(int x, int y) {return null;}
	public void useConfig(Map<String, Object> config) {}
	public void command(String command)
	{
		if (command.equals("back"))
			engine.app.mainTask.zoom.doClick();
		else if (command.startsWith("set zoom")) try
		{
			float zoom = Float.parseFloat(command.substring(9));
			setZoom(zoom);
		}
		catch (Exception e) {e.printStackTrace();}
		else if (command.startsWith("set pos")) try
		{
			String [] coords = command.substring(8).split(" ");
			float x = Float.parseFloat(coords[0]);
			float y = Float.parseFloat(coords[1]);
			setPos(x, y);
		}
		catch (Exception e) {e.printStackTrace();}
	}

	public boolean dropped(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		engine.app.logger.addEntry("Zoom scrolled");
		return true;
	}
	public boolean clicked(int x, int y, int pointer, int button) {return true;}

	public void update()
	{
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
			incPos(-.03f, 0);
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
			incPos(.03f, 0);
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
			incPos(0, -.03f);
		if (Gdx.input.isKeyPressed(Input.Keys.UP))
			incPos(0, .03f);
		
		frame.pos[2] = (1+(1-zoom)*2.5f)*engine.model.cover.coverHeight;
	}
	
	public void render() {}

	public boolean active = false;
	public void activate(boolean active)
	{
		if (this.active == active)
			return;
		this.active = active;
		
		if (active)
		{
			engine.app.logger.addEntry("Zooming");
			engine.app.input.removeListener(engine.hand);
			engine.app.input.addListenerFirst(this);
			engine.attractor = frame;
		}
		else
		{
			engine.app.logger.addEntry("Exiting zoom");
			engine.app.input.addListener(engine.hand);
			engine.app.input.removeListener(this);
			engine.attractor = engine.globalFrame;
		}
	}

}
