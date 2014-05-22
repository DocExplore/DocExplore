/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * An incoming resource on the client side that accumulates packets as they arrive.
 * @author Alexander Burnett
 *
 */
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
	
	/**
	 * Executed in its own thread to make sense of the received data. The stream is canceled if an exception is thrown.
	 * @param stream
	 * @throws Exception
	 */
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
	
	synchronized void addBuffer(byte [] buffer, long index)
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
	
	/**
	 * Allocator for implemented resource types.
	 * @author Alexander Burnett
	 *
	 * @param <T>
	 */
	public static interface Allocator<T extends StreamedResource>
	{
		public T allocate(ReaderClient client, String uri, File file);
		public T cast(StreamedResource stream);
	}
}
