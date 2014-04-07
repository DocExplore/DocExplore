package org.interreg.docexplore.reader.book;

import java.awt.image.BufferedImage;

import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gui.Label;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;

public class ParchmentMini extends Label
{
	ParchmentEngine engine;
	
	public ParchmentMini(ReaderApp app, ParchmentEngine engine, BufferedImage image, float maxw, float maxh) throws Exception
	{
		super(app, image, false, false);
		
		this.engine = engine;
		float ratio = 1;
		if (w > maxw)
			ratio = maxw/w;
		if (ratio*h > maxh)
			ratio = maxh/h;
		w *= ratio;
		h *= ratio;
	}

	public void renderWidget()
	{
		GL10 gl = Gdx.gl10;
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_STENCIL_TEST);
		gl.glColor4f(0, 0, 0, .25f*fullAlpha*alpha);
		GfxUtils.fillQuad(x-.5f*w, y-.5f*w, 0, 0, x+w+.5f*w, y+h+.5f*w, 1, 1);
		
		super.renderWidget();
		
		float px = engine.camera.position.x+.5f;
		float ch = (float)(engine.book.pages.size()/engine.book.aspectRatio);
		float py = -engine.camera.position.y/ch;
		float pw = (float)(engine.camera.position.z*.8f);
		float ph = pw*Gdx.graphics.getHeight()/(ch*Gdx.graphics.getWidth());
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_STENCIL_TEST);
		gl.glColor4f(.5f, .5f, 1f, .75f*fullAlpha*alpha);
		GfxUtils.fillQuad(x+(px-.5f*pw)*w, y+(py-.5f*ph)*h, 0, 0, x+(px+.5f*pw)*w, y+(py+.5f*ph)*h, 1, 1);
	}
	
	public void clicked(float x, float y)
	{
		float mx = x/w, my = y/h;
		float cx = mx-.5f;
		float ch = (float)(engine.book.pages.size()/engine.book.aspectRatio);
		float cy = -my*ch;
		engine.setPos(cx, cy);
	}
	public void dragged(float x, float y)
	{
		clicked(x, y);
	}
}
