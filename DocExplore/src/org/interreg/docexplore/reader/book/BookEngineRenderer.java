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
import java.nio.FloatBuffer;

import org.interreg.docexplore.reader.book.BookEngine.Component;
import org.interreg.docexplore.reader.gfx.Bindable;
import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gfx.Texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.utils.BufferUtils;

public class BookEngineRenderer
{
	BookEngine engine;
	
	BufferedImage roiMaskBuffer;
	Texture leftRoiMask, rightRoiMask;
	
	public BookEngineRenderer(BookEngine engine)
	{
		this.engine = engine;
		
		this.roiMaskBuffer = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		this.leftRoiMask = new Texture(roiMaskBuffer.getWidth(), roiMaskBuffer.getHeight(), true, false);
		this.rightRoiMask = new Texture(roiMaskBuffer.getWidth(), roiMaskBuffer.getHeight(), true, false);
	}
	
	public void setupLeftRoiMask()
	{
		BookSpecification spec = engine.book;
		BookModel model = engine.model;
		
		int leftPageIndex = model.leftStack.nStackPages > 0 ? 1+2*(model.leftStack.nStackPages-1) : -1;
		if (leftPageIndex >= 0)
		{
			boolean roiMode = engine.roiOverlay.active;
			if (roiMode && spec.pages.get(leftPageIndex).regions.contains(engine.selectedRegion))
				engine.selectedRegion.drawROIMask(roiMaskBuffer, 0x00000000, 0xff000000);
			else if (roiMode) GfxUtils.clear(roiMaskBuffer, 0xff000000);
			else spec.pages.get(leftPageIndex).drawROIMask(roiMaskBuffer, 0xffffffff, 0x00000000);
			leftRoiMask.setup(roiMaskBuffer);
			engine.app.submitRenderTask(new Runnable() {public void run() {leftRoiMask.update();}});
		}
	}
	public void setupRightRoiMask()
	{
		BookModel model = engine.model;
		
		boolean pageIsActive = engine.hand.pageIsActive;
		int leftPageIndex = model.leftStack.nStackPages > 0 ? 1+2*(model.leftStack.nStackPages-1) : -1;
		int rightPageIndex = model.rightStack.nStackPages > 0 ? leftPageIndex+1+(pageIsActive ? 2 : 0) : -1;
		if (rightPageIndex >= 0)
		{
			boolean roiMode = engine.roiOverlay.active;
			if (roiMode && engine.book.pages.get(rightPageIndex).regions.contains(engine.selectedRegion))
				engine.selectedRegion.drawROIMask(roiMaskBuffer, 0x00000000, 0xff000000);
			else if (roiMode) GfxUtils.clear(roiMaskBuffer, 0xff000000);
			else engine.book.pages.get(rightPageIndex).drawROIMask(roiMaskBuffer, 0xffffffff, 0x00000000);
			rightRoiMask.setup(roiMaskBuffer);
			engine.app.submitRenderTask(new Runnable() {public void run() {rightRoiMask.update();}});
		}
	}
	
	public void dispose()
	{
		leftRoiMask.dispose();
		rightRoiMask.dispose();
	}
	
	double cnt = 0;
	FloatBuffer ambient = (FloatBuffer)BufferUtils.newFloatBuffer(4).put(new float [] {.1f, .1f, .1f, 0}).flip();
	FloatBuffer diffuse = (FloatBuffer)BufferUtils.newFloatBuffer(4).put(new float [] {1f, 1f, 1f, 0}).flip();
	FloatBuffer position = (FloatBuffer)BufferUtils.newFloatBuffer(4).put(new float [] {-2f, -10f, 10f, 0}).flip();
	public final FloatBuffer tposition = BufferUtils.newFloatBuffer(4);
	public void render()
	{
		BookSpecification spec = engine.book;
		BookModel model = engine.model;
		
		GL10 gl = Gdx.gl10;
		
		cnt += .01;

		engine.attractor.attract(engine.camera, .075f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		engine.camera.apply(gl);
		
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambient);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, diffuse);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, position);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glTranslatef((float)engine.globalTrans, 0, 0);
		gl.glRotatef((float)(engine.globalRot*180/Math.PI), 0, 1, 0);
		
		boolean pageIsActive = engine.hand.pageIsActive;
		int leftPageIndex = model.leftStack.nStackPages > 0 ? 1+2*(model.leftStack.nStackPages-1) : -1;
		int rightPageIndex = model.rightStack.nStackPages > 0 ? leftPageIndex+1+(pageIsActive ? 2 : 0) : -1;
		
		Bindable leftPage = leftPageIndex >= 0 ? spec.pages.get(leftPageIndex).getTexture() : null;
		Bindable rightPage = rightPageIndex >= 0 ? spec.pages.get(rightPageIndex).getTexture() : null;
		Bindable pageFront = pageIsActive ? spec.pages.get(leftPageIndex+1).getTexture() : null;
		Bindable pageBack = pageIsActive ? spec.pages.get(leftPageIndex+2).getTexture() : null;
		
		Gdx.gl10.glEnable(GL10.GL_CULL_FACE);
		gl.glPolygonMode(GL10.GL_FRONT_AND_BACK, GL10.GL_FILL);
		
		boolean zoomed = engine.zoom.active;
		model.render(leftPage, rightPage, pageFront, pageBack, engine.book.coverTex, engine.book.innerCoverTex, zoomed ? null : leftRoiMask, zoomed ? null : rightRoiMask);
		
		for (Component extension : engine.components)
			extension.render();
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}
}
