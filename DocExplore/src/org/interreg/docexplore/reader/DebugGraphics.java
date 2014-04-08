/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gfx.Texture;
import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.BufferUtils;

public class DebugGraphics implements Graphics, ReaderApp.Module
{
	GL10 gl = Gdx.gl10;
	
	ReaderApp app;
	Mesh twoMesh, threeMesh;
	public DebugGraphics(ReaderApp app)
	{
		this.app = app;
		
		twoMesh = new Mesh(false, 2, 2, new VertexAttribute(VertexAttributes.Usage.Position, 3, ""));
		ShortBuffer indices = twoMesh.getIndicesBuffer();
		indices.limit(indices.capacity());
		indices.put(0, (short)0);
		indices.put(1, (short)1);
		
		threeMesh = new Mesh(false, 3, 3, new VertexAttribute(VertexAttributes.Usage.Position, 3, ""));
		indices = threeMesh.getIndicesBuffer();
		indices.limit(indices.capacity());
		indices.put(0, (short)0);
		indices.put(1, (short)1);
		indices.put(2, (short)2);
	}
	
	List<Graphics.Renderable> listeners = new LinkedList<Graphics.Renderable>();
	public void addListener(Graphics.Renderable listener) {listeners.add(listener);}
	public void removeListener(Graphics.Renderable listener) {listeners.remove(listener);}
	
	public void update()
	{
		
	}
	
	public int width() {return Gdx.graphics.getWidth();}
	public int height() {return Gdx.graphics.getHeight();}
	
	public void setWidth(float f)
	{
		gl.glLineWidth(f);
	}
	
	public void setColor(float r, float g, float b, float a)
	{
		gl.glColor4f(r, g, b, a);
	}
	
	public void drawLine(double x1, double y1, double x2, double y2)
	{
		FloatBuffer data = twoMesh.getVerticesBuffer();
		data.limit(data.capacity());
		data.put(0, (float)x1).put(1, (float)y1).put(2, 0);
		data.put(3, (float)x2).put(4, (float)y2).put(5, 0);
		twoMesh.render(GL10.GL_LINES);
	}
	
	public void fillTriangle(double x1, double y1, double x2, double y2, double x3, double y3)
	{
		FloatBuffer data = threeMesh.getVerticesBuffer();
		data.limit(data.capacity());
		data.put(0, (float)x1).put(1, (float)y1).put(2, 0);
		data.put(3, (float)x2).put(4, (float)y2).put(5, 0);
		data.put(6, (float)x3).put(7, (float)y3).put(8, 0);
		Gdx.gl10.glPolygonMode(GL10.GL_FRONT_AND_BACK, GL10.GL_FILL);
		//Gdx.gl10.glPolygonMode(GL10.GL_FRONT_AND_BACK, GL10.GL_LINE);
		threeMesh.render(GL10.GL_TRIANGLES);
		Gdx.gl10.glPolygonMode(GL10.GL_FRONT_AND_BACK, GL10.GL_FILL);
	}
	
	public void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3)
	{
		drawLine(x1, y1, x2, y2);
		drawLine(x2, y2, x3, y3);
		drawLine(x3, y3, x1, y1);
	}
	public void drawRect(double x1, double y1, double x2, double y2)
	{
		drawLine(x1, y1, x2, y1);
		drawLine(x2, y1, x2, y2);
		drawLine(x2, y2, x1, y2);
		drawLine(x1, y2, x1, y1);
	}
	public void fillRect(double x1, double y1, double x2, double y2)
	{
		fillTriangle(x1, y1, x2, y1, x2, y2);
		fillTriangle(x2, y2, x1, y2, x1, y1);
	}
	
	static class ImageOverlay
	{
		Texture tex;
		double x, y, w, h;
		float r, g, b, a;
		
		public ImageOverlay() {this.tex = null;}
		
		public void set(double x, double y, double w, double h, float r, float g, float b, float a)
		{
			this.w = w;
			this.h = h;
			this.x = x;
			this.y = y;
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
		}
	}
	Map<BufferedImage, ImageOverlay> overlays = new HashMap<BufferedImage, ImageOverlay>();
	FloatBuffer currentColor = BufferUtils.newFloatBuffer(16);
	public void addImage(BufferedImage image, double x, double y, double w, double h)
	{
		synchronized (overlays)
		{
			ImageOverlay overlay = overlays.remove(image);
			if (overlay == null)
				overlay = new ImageOverlay();
			Gdx.gl11.glGetFloatv(GL11.GL_CURRENT_COLOR, currentColor);
			currentColor.position(0).limit(currentColor.capacity());
			overlay.set(x, y, w, h, currentColor.get(0), currentColor.get(1), currentColor.get(2), currentColor.get(3));
			overlays.put(image, overlay);
		}
	}
	public void removeImage(BufferedImage image)
	{
		synchronized (overlays)
		{
			final ImageOverlay overlay = overlays.remove(image);
			if (overlay != null && overlay.tex != null)
				app.submitRenderTask(new Runnable() {public void run() {overlay.tex.dispose();}});
		}
	}

	public void render()
	{
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, -1, 1);
		
		for (Graphics.Renderable listener : listeners)
			try {listener.render(this);}
			catch (Throwable e) {e.printStackTrace();}
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		synchronized (overlays)
		{
			for (Map.Entry<BufferedImage, ImageOverlay> entry : overlays.entrySet())
			{
				ImageOverlay overlay = entry.getValue();
				if (overlay.tex == null)
					overlay.tex = new Texture(entry.getKey(), false);
				gl.glColor4f(overlay.r, overlay.g, overlay.b, overlay.a);
				overlay.tex.bind();
				GfxUtils.fillQuad((float)overlay.x, (float)overlay.y, 0, 0, 
					(float)(overlay.x+overlay.w), (float)(overlay.y+overlay.h), 1, 1);
			}
		}
		gl.glColor4f(1, 1, 1, 1);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

}
