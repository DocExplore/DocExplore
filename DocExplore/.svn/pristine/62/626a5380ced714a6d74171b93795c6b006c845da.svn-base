package org.interreg.docexplore.util;


import java.util.LinkedList;


public abstract class BufferStore<T>
{
	public LinkedList<T> buffers;
	
	public BufferStore()
	{
		buffers = new LinkedList<T>();
	}
	
	protected abstract T newBuffer();
	public int out = 0;
	public T get()
	{
		synchronized (buffers)
		{out++;
			if (!buffers.isEmpty())
				return buffers.pop();
		}
		return newBuffer();
	}
	
	public void free(T buffer)
	{
		synchronized (buffers)
		{out--;
			buffers.add(buffer);
		}
	}
}
