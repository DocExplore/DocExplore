/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book;

import java.util.Map;

import org.interreg.docexplore.reader.InputManager;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.gfx.CameraKeyFrame;
import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gfx.StreamedImage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;

public class ParchmentEngine implements ReaderApp.Module, InputManager.Listener
{
	public final ReaderApp app;
	public boolean active;
	
	public final PerspectiveCamera camera;
	public BookSpecification book;
	public final CameraKeyFrame globalFrame;
	
	ParchmentMini mini = null;
	
	float cx = 0, cy = 0, cz = 2;
	
	public ParchmentEngine(ReaderApp app)
	{
		this.app = app;
		this.active = false;
		
		this.camera = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = .125f;
		camera.far = 10;
		this.globalFrame = new CameraKeyFrame();
		resetCamera();
		camera.update();
	}
	
	void resetCamera()
	{
		this.cx = 0;
		this.cy = 0;
		this.cz = 2;
		globalFrame.pos[0] = 0;
		globalFrame.pos[1] = 0;
		globalFrame.pos[2] = 2;
		camera.position.x = 0;
		camera.position.y = 0;
		camera.position.z = 2;
		globalFrame.setup(cx, cy, cz, 
			cx, cy, 0, 
			0, 1, 0, 
			(float)(.14*Math.PI));
	}
	
	public void activate(boolean active)
	{
		if (this.active == active)
			return;
		this.active = active;
		if (active)
			app.input.addListener(this);
		else
		{
			app.input.removeListener(this);
			mini.activate(false);
			app.waitDialog.activate(false);
			app.submitRenderTaskAndWait(new Runnable() {public void run()
			{
				for (PageSpecification page : book.pages)
					page.release();
			}});
		}
	}
	
	public void setBook(final BookSpecification book)
	{
		this.book = book;
		
		app.submitRenderTask(new Runnable() {public void run()
		{
			if (mini != null)
			{
				mini.texture.dispose();
				app.gui.removeWidget(mini);
			}
			StreamedImage image = app.client.getResource(StreamedImage.class, book.miniUri);
			image.waitUntilComplete();
			try {mini = new ParchmentMini(app, ParchmentEngine.this, image.image, .025f*Gdx.graphics.getWidth(), .9f*Gdx.graphics.getHeight());}
			catch (Exception e) {throw new RuntimeException(e);}
			app.gui.addWidget(mini);
			mini.setPosition(.975f*Gdx.graphics.getWidth()-mini.w, .5f*(Gdx.graphics.getHeight()-mini.h));
			mini.activate(true);
		}});
		
		//updatePageCache();
		book.pages.get(0).getTexture();
		resetCamera();
		lastPage = -1;
	}
	
	public void setPos(float cx, float cy)
	{
		if (app.waitDialog.isActive())
			return;
		this.cx = cx;
		this.cy = cy;
		globalFrame.pos[0] = cx;
		globalFrame.pos[1] = cy;
		camera.position.x = cx;
		camera.position.y = cy;
	}
	
	public void changeZoom(float val)
	{
		cz = Math.min(3, Math.max(.4f, cz+val));
	}
	
	int pageSpread = 1;
	int lastPage = -1;
	public void update()
	{
		if (book == null || !active)
			return;
		
		globalFrame.setup(cx, cy, cz, 
			cx, cy, 0, 
			0, 1, 0, 
			(float)(.14*Math.PI));
		
		int page = Math.max(0, Math.min(book.pages.size()-1, (int)(-camera.position.y*book.aspectRatio)));
		if (page != lastPage)
		{
			app.logger.addEntry("Displaying page "+page);
			lastPage = page;
		}
		for (int i=0;i<book.pages.size();i++)
			if (Math.abs(page-i) <= pageSpread)
				book.pages.get(i).getTexture();
			else book.pages.get(i).release();
		
		cx = Math.max(-.5f, Math.min(.5f, cx));
		cy = Math.min(0, Math.max((float)(-book.pages.size()/book.aspectRatio), cy));
		
		app.waitDialog.activate(!book.pages.get(page).isLoaded());
	}

	double cnt = 0;
	public void render()
	{
		if (book == null || !active)
			return;
		
		GL10 gl = Gdx.gl10;
		
		cnt += .01;

		globalFrame.attract(camera, .075f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		camera.apply(gl);
		
		Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
		Gdx.gl10.glDisable(GL10.GL_CULL_FACE);
		Gdx.gl10.glDisable(GL10.GL_LIGHTING);
		gl.glPolygonMode(GL10.GL_FRONT_AND_BACK, GL10.GL_FILL);
		
		int page = (int)(-camera.position.y*book.aspectRatio);
		int p0 = Math.max(0, page-pageSpread), p1 = Math.min(book.pages.size()-1, page+pageSpread);
		for (int i=p0;i<=p1;i++)
		{
			book.pages.get(i).getTexture().bind();
			GfxUtils.fillQuad(-.5f, -(float)(i/book.aspectRatio), 0, 0, .5f, -(float)((i+1)/book.aspectRatio), 1, 1);
		}
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	public boolean touched(int x, int y, int pointer, int button)
	{
		return false;
	}
	int lastx = -1, lasty = -1;
	public boolean grabbed(int x, int y, int pointer, int button)
	{
		lastx = x;
		lasty = y;
		return true;
	}
	public boolean dragged(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		if (!app.waitDialog.isActive())
		{
			cx += -(tx-lastx)*camera.position.z/Gdx.graphics.getWidth();
			cy += (ty-lasty)*camera.position.z*book.aspectRatio/Gdx.graphics.getHeight();
			lastx = tx;
			lasty = ty;
		}
		return true;
	}
	public boolean typed(int key)
	{
		if (!active)
			return false;
		if (key == Input.Keys.ESCAPE)
			app.mainTask.back.doClick();
		else return false;
		return true;
	}
	public boolean scrolled(int amount) {return false;}
	public boolean dropped(int fx, int fy, int tx, int ty, int pointer, int button) {return false;}
	public boolean clicked(int x, int y, int pointer, int button) {return false;}
	public Object objectAt(int x, int y) {return null;}
	public void useConfig(Map<String, Object> config) {}
	public void command(String command) {}
}
