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
