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
import java.io.File;
import java.io.InputStream;

import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.StreamedResource;

public class StreamedTexture extends StreamedImage implements Bindable
{
	public Texture texture;
	
	static long delay = 500;
	protected StreamedTexture(ReaderClient client, final String uri, File file)
	{
		super(client, uri, file);
		
		this.texture = null;
	}
	
	public void handle(InputStream stream) throws Exception
	{
		super.handle(stream);
		if (!isCanceled())
		{
			client.app.submitRenderTaskAndWait(new InitTextureTask(StreamedTexture.this));
			texture.setup(image);
			client.app.submitRenderTaskAndWait(new UpdateTextureTask(StreamedTexture.this));
//			client.app.submitRenderTaskAndWait(new Runnable() {public void run()
//			{
//				new InitTextureTask(StreamedTexture.this).run();
//				texture.setup(image);
//				new UpdateTextureTask(StreamedTexture.this).run();
//			}});
		}
		else client.app.submitRenderTaskAndWait(new DisposeTextureTask(StreamedTexture.this));
	}
	
	public void bind()
	{
		//if (texture != null)
		if (isComplete())
			texture.bind();
		else Filler.getBindable().bind();
	}
	
	protected void dispose()
	{
		if (texture != null)
			texture.dispose();
		texture = null;
		super.dispose();
	}
	
	public static class InitTextureTask implements Runnable
	{
		StreamedTexture stream;
		public InitTextureTask(StreamedTexture stream) {this.stream = stream;}
		public void run()
		{
			if (stream.texture == null)
				stream.texture = new Texture(stream.image.getWidth(), stream.image.getHeight(), stream.image.getType() != BufferedImage.TYPE_3BYTE_BGR, false);
		}
	}
	public static class UpdateTextureTask implements Runnable
	{
		StreamedTexture stream;
		public UpdateTextureTask(StreamedTexture stream) {this.stream = stream;}
		public void run() {if (stream.texture != null) stream.texture.update();}
	}
	public static class DisposeTextureTask implements Runnable
	{
		StreamedTexture stream;
		public DisposeTextureTask(StreamedTexture stream) {this.stream = stream;}
		public void run() {if (stream.texture != null) stream.texture.dispose();}
	}
	
	public static final Allocator<StreamedTexture> allocator = new Allocator<StreamedTexture>()
	{
		public StreamedTexture allocate(ReaderClient client, String uri, File file) {return new StreamedTexture(client, uri, file);}
		public StreamedTexture cast(StreamedResource stream) {return (StreamedTexture)stream;}
	};
}
