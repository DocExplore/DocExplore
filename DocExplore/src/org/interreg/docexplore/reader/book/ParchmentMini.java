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
