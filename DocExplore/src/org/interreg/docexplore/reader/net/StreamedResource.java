package org.interreg.docexplore.reader.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

public abstract class StreamedResource extends InputStream
{
	public final ReaderClient client;
	public final String uri;
	public final File file;
	
	//protected ResourceBuffer buffer;
	protected Map<Long, byte []> buffers;
	int length;
	protected long lastIndex;
	boolean hasAllBuffers;
	boolean canceled;
	Exception error;
	boolean complete;
	int refCnt;
	
	protected StreamedResource(ReaderClient client, String uri, File file)
	{
		this.client = client;
		this.uri = uri;
		this.file = file;
		
		this.buffers = new TreeMap<Long, byte []>();
		this.length = 0;
		this.hasAllBuffers = false;
		this.lastIndex = -1;
		this.complete = false;
		this.canceled = false;
		this.error = null;
		this.refCnt = 1;
		
		new Thread()
		{
			public void run()
			{
				try
				{
					handle(StreamedResource.this);
					complete = true;
				}
				catch (Exception e)
				{
					if (!canceled)
						error = e;
				}
			}
		}.start();
	}
	
	public abstract void handle(InputStream stream) throws Exception;
	
	public void release()
	{
		refCnt--;
		if (refCnt <= 0)
			dispose();
	}
	
	protected void dispose() {client.cancelResource(uri);}
	public synchronized boolean isCanceled() {return canceled;}
	public boolean isComplete() {return complete;}
	
	public void waitUntilComplete()
	{
		while (!complete && !canceled && error == null)
			try {Thread.sleep(200);}
			catch (Exception e) {}
		if (error != null)
			error.printStackTrace();
	}
	
	public synchronized void addBuffer(byte [] buffer, long index)
	{
		if (lastIndex >= 0 && index > lastIndex)
			throw new RuntimeException("Stream overflow!");
		buffers.put(index, buffer);
		if (lastIndex >= 0 && buffers.size() > lastIndex)
			hasAllBuffers = true;
		this.length += length;
	}
	
	synchronized void setLastIndex(long lastIndex)
	{
		this.lastIndex = lastIndex;
		if (buffers.size() > lastIndex)
			hasAllBuffers = true;
	}
	
	byte [] curBuffer = null;
	long curBufferIndex = 0;
	int curIndex = 0;
	public void reset() throws IOException
	{
		super.reset();
		curBufferIndex = 0;
		curIndex = 0;
		curBuffer = null;
	}
	
	public int read() throws IOException
	{
		if (curBuffer == null)
		{
			while ((curBuffer = buffers.get(curBufferIndex)) == null)
			{
				synchronized (this)
				{
					if (hasAllBuffers == true && curBufferIndex > lastIndex)
						return -1;
					if (canceled)
						throw new IOException("Resource '"+uri+"' was canceled");
				}
				try {Thread.sleep(200);} catch (Exception e) {}
			}
		}
		
		byte b;
		b = curBuffer[curIndex++];
		if (curIndex == curBuffer.length)
		{
			curBufferIndex++;
			curBuffer = null;
			curIndex = 0;
		}
		
		return b & 0xff;
	}
	
	public ResourceRequest request() {return new ResourceRequest(uri);}
	
	public static interface Allocator<T extends StreamedResource>
	{
		public T allocate(ReaderClient client, String uri, File file);
		public T cast(StreamedResource stream);
	}
}
