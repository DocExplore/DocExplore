package org.interreg.docexplore.reader.gfx;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;

public class GfxUtils
{
	public static Mesh quadTMesh = null, quadTCMesh = null;
	public static void fillQuad(float x1, float y1, float s1, float t1, float x2, float y2, float s2, float t2)
	{
		doQuad(x1, y1, s1, t1, x2, y2, s2, t2, GL10.GL_TRIANGLE_FAN);
	}
	public static void drawQuad(float x1, float y1, float s1, float t1, float x2, float y2, float s2, float t2, float width)
	{
		Gdx.gl10.glLineWidth(width);
		Gdx.gl10.glEnable(GL10.GL_LINE_SMOOTH);
		doQuad(x1, y1, s1, t1, x2, y2, s2, t2, GL10.GL_LINE_LOOP);
	}
	public static void doQuad(float x1, float y1, float s1, float t1, float x2, float y2, float s2, float t2, int primitive)
	{
		if (quadTMesh == null)
		{
			quadTMesh = new Mesh(false, 4, 4, new VertexAttribute(VertexAttributes.Usage.Position, 3, "p"),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "tc"));
			FloatBuffer vertexBuffer = quadTMesh.getVerticesBuffer();
			vertexBuffer.limit(vertexBuffer.capacity());
			
			ShortBuffer indexBuffer = quadTMesh.getIndicesBuffer();
			indexBuffer.limit(indexBuffer.capacity());
			indexBuffer.put(new short [] {0, 1, 2, 3});
			indexBuffer.flip();
		}
		
		FloatBuffer vertexBuffer = quadTMesh.getVerticesBuffer();
		vertexBuffer.put(x1).put(y1).put(0).put(s1).put(t1).
			put(x2).put(y1).put(0).put(s2).put(t1).
			put(x2).put(y2).put(0).put(s2).put(t2).
			put(x1).put(y2).put(0).put(s1).put(t2).flip();
		
		quadTMesh.render(primitive);
	}
	public static Mesh buildQuad(float x1, float y1, float s1, float t1, float x2, float y2, float s2, float t2)
	{
		Mesh mesh = new Mesh(false, 4, 6, new VertexAttribute(VertexAttributes.Usage.Position, 3, "p"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "tc"));
		FloatBuffer vertexBuffer = mesh.getVerticesBuffer();
		vertexBuffer.limit(vertexBuffer.capacity());
		vertexBuffer.put(x1).put(y1).put(0).put(s1).put(t1).
			put(x2).put(y1).put(0).put(s2).put(t1).
			put(x2).put(y2).put(0).put(s2).put(t2).
			put(x1).put(y2).put(0).put(s1).put(t2).flip();
		
		ShortBuffer indexBuffer = mesh.getIndicesBuffer();
		indexBuffer.limit(indexBuffer.capacity());
		indexBuffer.put(new short [] {0, 1, 2, 0, 2, 3}).flip();
		return mesh;
	}
	
	public static void doQuad(float x1, float y1, float s1, float t1, float r1, float g1, float b1, float a1,
		float x2, float y2, float s2, float t2, float r2, float g2, float b2, float a2,
		float x3, float y3, float s3, float t3, float r3, float g3, float b3, float a3,
		float x4, float y4, float s4, float t4, float r4, float g4, float b4, float a4)
	{
		if (quadTCMesh == null)
		{
			quadTCMesh = new Mesh(false, 4, 6, new VertexAttribute(VertexAttributes.Usage.Position, 3, "p"),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "tc"), 
				new VertexAttribute(VertexAttributes.Usage.Color, 4, "c"));
			FloatBuffer vertexBuffer = quadTCMesh.getVerticesBuffer();
			vertexBuffer.limit(vertexBuffer.capacity());
			
			ShortBuffer indexBuffer = quadTCMesh.getIndicesBuffer();
			indexBuffer.limit(indexBuffer.capacity());
			indexBuffer.put(new short [] {0, 1, 2, 0, 2, 3});
			indexBuffer.flip();
		}
		
		FloatBuffer vertexBuffer = quadTCMesh.getVerticesBuffer();
		vertexBuffer.put(x1).put(y1).put(0).put(s1).put(t1).put(r1).put(g1).put(b1).put(a1).
			put(x2).put(y2).put(0).put(s2).put(t2).put(r2).put(g2).put(b2).put(a2).
			put(x3).put(y3).put(0).put(s3).put(t3).put(r3).put(g3).put(b3).put(a3).
			put(x4).put(y4).put(0).put(s4).put(t4).put(r4).put(g4).put(b4).put(a4).flip();
		
		quadTCMesh.render(GL10.GL_TRIANGLES);
	}
	
	public static void clear(BufferedImage image, int col)
	{
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				image.setRGB(i, j, col);
	}
}
