/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book.page;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;

public class BookPageShadow
{
	BookPage page;
	Mesh mesh, quad;
	
	public BookPageShadow(BookPage page)
	{
		this.page = page;
		int w = page.paper.snodes.length;
		int h = page.paper.snodes[0].length;
		this.mesh = new Mesh(false, 2*w*h, 6*4*(w-1)*(h-1), 
			new VertexAttribute(VertexAttributes.Usage.Position, 3, "pos"));
		
		ShortBuffer indexBuffer = mesh.getIndicesBuffer();
		indexBuffer.limit(indexBuffer.capacity());
		
		int cnt = 0;
		for (int i=0;i<page.paper.snodes.length-1;i++)
			for (int j=0;j<page.paper.snodes[0].length-1;j++)
		{
			indexBuffer.put(cnt++, (short)(2*(j*w+i)));
			indexBuffer.put(cnt++, (short)(2*(j*w+i)+1));
			indexBuffer.put(cnt++, (short)(2*(j*w+i+1)+1));
			indexBuffer.put(cnt++, (short)(2*(j*w+i+1)));
			indexBuffer.put(cnt++, (short)(2*(j*w+i)));
			indexBuffer.put(cnt++, (short)(2*(j*w+i+1)+1));
			
			indexBuffer.put(cnt++, (short)(2*(j*w+i+1)));
			indexBuffer.put(cnt++, (short)(2*(j*w+i+1)+1));
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i+1)+1));
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i+1)));
			indexBuffer.put(cnt++, (short)(2*(j*w+i+1)));
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i+1)+1));
			
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i+1)));
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i+1)+1));
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i)+1));
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i)));
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i+1)));
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i)+1));
			
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i)));
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i)+1));
			indexBuffer.put(cnt++, (short)(2*(j*w+i)+1));
			indexBuffer.put(cnt++, (short)(2*(j*w+i)));
			indexBuffer.put(cnt++, (short)(2*((j+1)*w+i)));
			indexBuffer.put(cnt++, (short)(2*(j*w+i)+1));
		}
		
		mesh.getVerticesBuffer().limit(mesh.getVerticesBuffer().capacity());
		
		this.quad = new Mesh(true, 4, 6, new VertexAttribute(VertexAttributes.Usage.Position, 3, "pos"));
		indexBuffer = quad.getIndicesBuffer();
		indexBuffer.limit(indexBuffer.capacity());
		FloatBuffer vertexBuffer = quad.getVerticesBuffer();
		vertexBuffer.limit(vertexBuffer.capacity());
		indexBuffer.put(new short [] {0, 1, 2, 3, 0, 2});
		indexBuffer.flip();
		vertexBuffer.put(new float [] {0, 0, 0,   1, 0, 0,   1, 1, 0,   0, 1, 0});
		vertexBuffer.flip();
	}
	
	float [] lightPos = {0, 0, 0};
	public void update(float lx, float ly, float lz)
	{
		lightPos[0] = lx; lightPos[1] = ly; lightPos[2] = lz;
		//Matrix4.mulVec(page.book.invTrans.val, lightPos);
		
		FloatBuffer vertexBuffer = mesh.getVerticesBuffer();
		int w = page.paper.snodes.length;
		int h = page.paper.snodes[0].length;
		
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
		{
			Grid.Node node = page.paper.snodes[i][j].node;
			
			vertexBuffer.put(2*3*(j*w+i), node.point[0]);
			vertexBuffer.put(2*3*(j*w+i)+1, node.point[1]);
			vertexBuffer.put(2*3*(j*w+i)+2, node.point[2]);
			
			vertexBuffer.put(2*3*(j*w+i)+3, lightPos[0]+100*(node.point[0]-lightPos[0]));
			vertexBuffer.put(2*3*(j*w+i)+4, lightPos[1]+100*(node.point[1]-lightPos[1]));
			vertexBuffer.put(2*3*(j*w+i)+5, lightPos[2]+100*(node.point[2]-lightPos[2]));
		}
	}
	
	public void render()
	{
		GL11 gl = Gdx.gl11;
		gl.glEnable(GL11.GL_DEPTH_TEST);
		gl.glPolygonMode(GL10.GL_FRONT_AND_BACK, GL10.GL_FILL);
		gl.glClearStencil(0);
		gl.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		gl.glColorMask(false, false, false, false);
		gl.glDepthMask(false);
		gl.glDisable(GL11.GL_LIGHTING);
		gl.glEnable(GL10.GL_STENCIL_TEST);
		//page.book.setupModelview();
		
		gl.glDisable(GL11.GL_CULL_FACE);
		gl.glStencilFunc(GL10.GL_ALWAYS, 0, 1);
		gl.glStencilOp(GL10.GL_KEEP, GL10.GL_KEEP, GL10.GL_INCR);
//		gl.glCullFace(GL11.GL_FRONT);
		mesh.render(GL11.GL_TRIANGLES);
		
//		gl.glStencilFunc(GL10.GL_ALWAYS, 0, 0xffff);
//		gl.glStencilOp(GL10.GL_KEEP, GL10.GL_KEEP, GL10.GL_DECR);
//		gl.glCullFace(GL11.GL_BACK);
//		mesh.render(GL11.GL_TRIANGLES);
		
		gl.glDisable(GL11.GL_CULL_FACE);
		gl.glDisable(GL11.GL_DEPTH_TEST);
		gl.glColorMask(true, true, true, true);
		gl.glStencilFunc(GL10.GL_NOTEQUAL, 0, 1);
		gl.glStencilOp(GL10.GL_KEEP, GL10.GL_KEEP, GL10.GL_KEEP);
		gl.glColor4f(0, 0, 0, .5f);
		
		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL11.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, 1, 0, 1, -1, 1);
		
		quad.render(GL11.GL_TRIANGLES);
		
		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glMatrixMode(GL11.GL_PROJECTION);
		gl.glPopMatrix();
		
		gl.glEnable(GL11.GL_LIGHTING);
		gl.glEnable(GL11.GL_DEPTH_TEST);
		gl.glEnable(GL11.GL_CULL_FACE);
		gl.glDisable(GL10.GL_STENCIL_TEST);
		gl.glCullFace(GL11.GL_BACK);
		gl.glDepthMask(true);
		//page.book.unsetupModelview();
	}
	
	public void renderDebug()
	{
		GL11 gl = Gdx.gl11;
		gl.glClearStencil(0);
		gl.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		gl.glDepthMask(false);
		gl.glDisable(GL11.GL_LIGHTING);
		//page.book.setupModelview();
		gl.glColor4f(0, 0, 0, .125f);
		gl.glStencilFunc(GL10.GL_ALWAYS, 0, 0xffff);
		gl.glDisable(GL11.GL_CULL_FACE);
		
		mesh.render(GL11.GL_TRIANGLES);
		
		gl.glEnable(GL11.GL_LIGHTING);
		gl.glEnable(GL11.GL_DEPTH_TEST);
		gl.glEnable(GL11.GL_CULL_FACE);
		gl.glCullFace(GL11.GL_BACK);
		gl.glDepthMask(true);
		//page.book.unsetupModelview();
	}
}
