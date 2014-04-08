/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
