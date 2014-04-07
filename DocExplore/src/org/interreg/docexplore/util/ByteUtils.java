package org.interreg.docexplore.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

public class ByteUtils
{
	public static byte [] readStream(InputStream in) throws IOException
	{
		LinkedList<ByteBuffer> buffers = new LinkedList<ByteBuffer>();
		int size = 0;
		while (true)
		{
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			int read = in.read(buffer.array());
			if (read > 0)
			{
				buffer.limit(read);
				buffers.add(buffer);
				size += read;
			}
			if (read < 0)
				break;
		}
		in.close();
		ByteBuffer res = ByteBuffer.allocate(size);
		for (ByteBuffer buffer : buffers)
			res.put(buffer);
		return res.array();
	}
	
	public static byte [] readStream(FileChannel channel) throws IOException
	{
		LinkedList<ByteBuffer> buffers = new LinkedList<ByteBuffer>();
		int size = 0;
		while (true)
		{
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			int read = channel.read(buffer);
			if (read > 0)
			{
				buffers.add(buffer);
				buffer.flip();
				size += read;
			}
			if (read < buffer.capacity())
				break;
		}
		ByteBuffer res = ByteBuffer.allocate(size);
		for (ByteBuffer buffer : buffers)
			res.put(buffer);
		return res.array();
	}
	
	public static byte [] readFile(File file) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		byte [] res = readStream(raf.getChannel());
		raf.close();
		return res;
	}
	
	public static void writeFile(File file, byte [] bytes) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileChannel channel = raf.getChannel();
		channel.truncate(0);
		channel.write(ByteBuffer.wrap(bytes));
		raf.close();
	}
	
	public static void writeStream(InputStream in, OutputStream out) throws IOException
	{
		byte [] buf = new byte [1024];
		int read;
//		int cnt = 0;
		while ((read = in.read(buf)) > 0)
		{
			out.write(buf, 0, read); 
//			cnt += read;
		}
		in.close();
		out.close();
	}
	
	public static void copyFileRecursive(File from, File destRoot) throws IOException
	{
		File to = new File(destRoot, from.getName());
		if (!from.isDirectory())
			copyFile(from, to);
		else
		{
			to.mkdirs();
			for (File file : from.listFiles())
				copyFileRecursive(file, to);
		}
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException
	{
		if (!destFile.exists())
		{
			destFile.createNewFile();
		}
		FileInputStream fIn = null;
		FileOutputStream fOut = null;
		FileChannel source = null;
		FileChannel destination = null;
		try
		{
			fIn = new FileInputStream(sourceFile);
			source = fIn.getChannel();
			fOut = new FileOutputStream(destFile);
			destination = fOut.getChannel();
			long transfered = 0;
			long bytes = source.size();
			while (transfered < bytes)
			{
				transfered += destination.transferFrom(source, 0, source.size());
				destination.position(transfered);
			}
		}
		finally
		{
			if (source != null)
				source.close();
			else if (fIn != null)
				fIn.close();
			if (destination != null)
				destination.close();
			else if (fOut != null)
				fOut.close();
		}
	}
	
	public static InputStream getEmptyInputStream()
	{
		return new InputStream() {public int read() throws IOException {return -1;}};
	}
	
	public static InputStream toInputStream(Object object) throws IOException
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		out.writeObject(object);
		byte [] data = bout.toByteArray();
		out.close();
		return new ByteArrayInputStream(data);
	}
}
