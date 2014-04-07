package org.interreg.docexplore.reader.gfx;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL12;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.utils.BufferUtils;

public class Texture implements Bindable
{
	public static int nTextures = 0;
	
	public final int id;
	int width, height;
	ByteBuffer data;
	public boolean inited, hasAlpha;
	public Texture(int width, int height, boolean hasAlpha, boolean repeat)
	{
		this.width = width;
		this.height = height;
		this.data = null;
		this.inited = false;
		this.hasAlpha = hasAlpha;
		
		GL11 gl = Gdx.gl11;
		int [] ida = {0};
		gl.glGenTextures(1, ida, 0);
		this.id = ida[0];
		gl.glBindTexture(GL11.GL_TEXTURE_2D, id);
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, repeat ? GL11.GL_REPEAT : GL11.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, repeat ? GL11.GL_REPEAT : GL11.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		//gl.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
		org.lwjgl.opengl.GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, width, height, 0, 
			hasAlpha ? GL11.GL_RGBA : GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		
		nTextures++;
	}
	public Texture(BufferedImage image, boolean repeat)
	{
		this(image.getWidth(), image.getHeight(), image.getType() != BufferedImage.TYPE_3BYTE_BGR, repeat);
		setup(image);
		update();
	}
	
	public synchronized void setup(BufferedImage image)
	{
		data = toRGBABuffer(image, data);
	}
		
	public synchronized void update()
	{
		GL11 gl = Gdx.gl11;
		gl.glBindTexture(GL11.GL_TEXTURE_2D, id);
		gl.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, hasAlpha ? GL11.GL_RGBA : GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, data);
		//org.lwjgl.util.glu.GLU.gluBuild2DMipmaps(target, components, width, height, format, type, data)
		inited = true;
	}
	
	public int width() {return width;}
	public int height() {return height;}
	
	public void bind()
	{
		Gdx.gl11.glBindTexture(GL11.GL_TEXTURE_2D, id);
	}
	
	public void dispose()
	{
		int [] ida = {id};
		Gdx.gl11.glDeleteTextures(1, ida, 0);
		nTextures--;
	}
	
	public ByteBuffer toRGBABuffer(BufferedImage image) {return toRGBABuffer(image, null);}
	public ByteBuffer toRGBABuffer(BufferedImage image, ByteBuffer data)
	{
		int psize = hasAlpha ? 4 : 3;
		if (data == null)
			data = BufferUtils.newByteBuffer(psize*width*height);
		
		if (image.getType() == BufferedImage.TYPE_3BYTE_BGR && image.getWidth() == width && image.getHeight() == height)
		{
			data.position(0);
			data.put(((DataBufferByte)image.getData().getDataBuffer()).getData());
			data.position(0);
		}
		else
		{
			int w = Math.min(width, image.getWidth()), h = Math.min(height, image.getHeight());
			for (int i=0;i<w;i++)
				for (int j=0;j<h;j++)
			{
				int col = image.getRGB(i, j);
				data.put(psize*(j*image.getWidth()+i), (byte)((col >> 16) & 0xff));
				data.put(psize*(j*image.getWidth()+i)+1, (byte)((col >> 8) & 0xff));
				data.put(psize*(j*image.getWidth()+i)+2, (byte)((col >> 0) & 0xff));
				if (hasAlpha)
					data.put(psize*(j*image.getWidth()+i)+3, (byte)((col >> 24) & 0xff));
			}
		}
		return data;
	}
	

}
