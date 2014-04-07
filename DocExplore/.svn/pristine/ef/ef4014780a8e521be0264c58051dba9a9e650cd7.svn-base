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
