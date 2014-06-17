/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.gfx;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL12;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.utils.BufferUtils;

public class Texture implements Bindable
{
	public static int nTextures = 0;
	
	private int id;
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
			hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		
		nTextures++;
	}
	public Texture(BufferedImage image, boolean repeat)
	{
		this(image.getWidth(), image.getHeight(), 
			image.getType() == BufferedImage.TYPE_4BYTE_ABGR || image.getType() == BufferedImage.TYPE_INT_ARGB || image.getType() == BufferedImage.TYPE_CUSTOM, 
			//image.getType() != BufferedImage.TYPE_3BYTE_BGR,
			repeat);
		setup(image);
		update();
	}
	
	public synchronized void setup(BufferedImage image)
	{
		data = toRGBABuffer(image, data);
	}
		
	public synchronized void update()
	{
		if (id < 0 || data == null)
			return;
		GL11 gl = Gdx.gl11;
		gl.glBindTexture(GL11.GL_TEXTURE_2D, id);
		gl.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, dataType, GL11.GL_UNSIGNED_BYTE, data);
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
		id = -1;
		if (data != null)
		{
			BufferUtils.disposeUnsafeByteBuffer(data);
			data = null;
		}
	}
	
	int dataType = BufferedImage.TYPE_3BYTE_BGR;
	public ByteBuffer toRGBABuffer(BufferedImage image) {return toRGBABuffer(image, null);}
	public ByteBuffer toRGBABuffer(BufferedImage image, ByteBuffer data)
	{
		int psize = hasAlpha ? 4 : 3;
		if (data == null)
			data = BufferUtils.newUnsafeByteBuffer(psize*width*height);
		
		data.position(0);
		if (image.getType() == BufferedImage.TYPE_3BYTE_BGR && image.getWidth() == width && image.getHeight() == height)
		{
			dataType = GL12.GL_BGR;
			data.put(((DataBufferByte)image.getData().getDataBuffer()).getData());
		}
		else if (image.getType() == BufferedImage.TYPE_INT_ARGB && image.getWidth() == width && image.getHeight() == height)
		{
			dataType = GL12.GL_BGRA;
			data.asIntBuffer().put(((DataBufferInt)image.getData().getDataBuffer()).getData());
		}
		else if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR && image.getWidth() == width && image.getHeight() == height)
		{
			dataType = GL11.GL_RGBA;
			data.put(((DataBufferByte)image.getData().getDataBuffer()).getData());
		}
		else
		{
			dataType = hasAlpha ? GL12.GL_BGRA : GL12.GL_BGR;
			int w = Math.min(width, image.getWidth()), h = Math.min(height, image.getHeight());
			for (int i=0;i<w;i++)
				for (int j=0;j<h;j++)
			{
				int col = image.getRGB(i, j);
				data.put(psize*(j*image.getWidth()+i), (byte)((col >> 0) & 0xff));
				data.put(psize*(j*image.getWidth()+i)+1, (byte)((col >> 8) & 0xff));
				data.put(psize*(j*image.getWidth()+i)+2, (byte)((col >> 16) & 0xff));
				if (hasAlpha)
					data.put(psize*(j*image.getWidth()+i)+3, (byte)((col >> 24) & 0xff));
			}
		}
		data.position(0);
		return data;
	}
	
	String getTypeName(int type)
	{
		switch (type)
		{
			case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
			case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR";
			case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE";
			case BufferedImage.TYPE_BYTE_BINARY: return "TYPE_BYTE_BINARY";
			case BufferedImage.TYPE_BYTE_GRAY: return "TYPE_BYTE_GRAY";
			case BufferedImage.TYPE_BYTE_INDEXED: return "TYPE_BYTE_INDEXED";
			case BufferedImage.TYPE_CUSTOM: return "TYPE_CUSTOM";
			case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB";
			case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE";
			case BufferedImage.TYPE_INT_BGR: return "TYPE_INT_BGR";
			case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB";
			case BufferedImage.TYPE_USHORT_555_RGB: return "TYPE_USHORT_555_RGB";
			case BufferedImage.TYPE_USHORT_565_RGB: return "TYPE_USHORT_565_RGB";
			case BufferedImage.TYPE_USHORT_GRAY: return "TYPE_USHORT_GRAY";
		}
		return "Unknown";
	}
}
