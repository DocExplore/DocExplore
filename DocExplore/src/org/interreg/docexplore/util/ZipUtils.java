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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

public class ZipUtils
{
	public static void zip(File directory, File zipfile) throws Exception
		{zip(directory, directory.listFiles(), zipfile, null, 0, 1, 9);}
	public static void zip(File directory, File zipfile, int level) throws Exception
		{zip(directory, directory.listFiles(), zipfile, null, 0, 1, level);}
	public static void zip(File directory, File zipfile, float [] progress) throws Exception
		{zip(directory, directory.listFiles(), zipfile, progress, 0, 1, 9);}
	public static void zip(File directory, File zipfile, float [] progress, float progressOffset, float progressAmount) throws Exception
		{zip(directory, directory.listFiles(), zipfile, progress, progressOffset, progressAmount, 9);}
	public static void zip(File directory, File zipfile, float [] progress, int level) throws Exception
		{zip(directory, directory.listFiles(), zipfile, progress, 0, 1, level);}
	public static void zip(File directory, File zipfile, float [] progress, float progressOffset, float progressAmount, int level) throws Exception
		{zip(directory, directory.listFiles(), zipfile, progress, progressOffset, progressAmount, level);}
//	public static void zip(File directory, File [] files, File zipfile, double [] progress, int level) throws IOException
//	{
//		URI base = directory.toURI();
//		Deque<File> queue = new LinkedList<File>();
//		OutputStream out = new FileOutputStream(zipfile, false);
//		Closeable res = null;
//		try
//		{
//			int nEntries = count(files, queue, 0);
//			while (!queue.isEmpty())
//			{
//				File dir = queue.pop();
//				nEntries = count(dir.listFiles(), queue, nEntries);
//			}
//			
//			ZipOutputStream zout = new ZipOutputStream(out);
//			zout.setLevel(level);
//			res = zout;
//			
//			int cnt = zip(files, queue, base, 0, nEntries, progress, zout);
//			while (!queue.isEmpty())
//			{
//				File dir = queue.pop();
//				cnt = zip(dir.listFiles(), queue, base, cnt, nEntries, progress, zout);
//			}
//		}
//		finally {res.close();}
//	}
	public static void zip(File directory, File [] files, File zipfile, float [] progress, float progressOffset, float progressAmount, int level) throws Exception
	{
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		OutputStream out = new FileOutputStream(zipfile, false);
		Closeable res = null;
		try
		{
			int nEntries = count(files, queue, 0);
			while (!queue.isEmpty())
			{
				File dir = queue.pop();
				nEntries = count(dir.listFiles(), queue, nEntries);
			}
			
			ZipArchiveOutputStream zout = (ZipArchiveOutputStream)new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, out);
			zout.setLevel(level);
			res = zout;
			
			int cnt = zip(files, queue, base, 0, nEntries, progress, progressOffset, progressAmount, zout);
			while (!queue.isEmpty())
			{
				File dir = queue.pop();
				cnt = zip(dir.listFiles(), queue, base, cnt, nEntries, progress, progressOffset, progressAmount, zout);
			}
		}
		finally {res.close();}
	}
	static int count(File [] files, Deque<File> queue, int nEntries) throws IOException
	{
		for (File kid : files)
			if (kid.isDirectory())
				queue.push(kid);
			else nEntries++;
		return nEntries;
	}
//	static int zip(File [] files, Deque<File> queue, URI base, int cnt, int nEntries, double [] progress, ZipOutputStream zout) throws IOException
//	{
//		for (File kid : files)
//		{
//			String name = base.relativize(kid.toURI()).getPath();
//			if (kid.isDirectory())
//			{
//				queue.push(kid);
//				name = name.endsWith("/") ? name : name + "/";
//				zout.putNextEntry(new ZipEntry(name));
//			}
//			else
//			{
//				zout.putNextEntry(new ZipEntry(name));
//				copy(kid, zout);
//				zout.closeEntry();
//				
//				cnt++;
//				if (progress != null)
//					progress[0] = cnt*1./nEntries;
//			}
//		}
//		return cnt;
//	}
	static int zip(File [] files, Deque<File> queue, URI base, int cnt, int nEntries, float [] progress, float progressOffset, float progressAmount, ArchiveOutputStream zout) throws IOException
	{
		for (File kid : files)
		{
			String name = base.relativize(kid.toURI()).getPath();
			if (kid.isDirectory())
			{
				queue.push(kid);
				name = name.endsWith("/") ? name : name + "/";
				ArchiveEntry entry = zout.createArchiveEntry(kid, name);
				zout.putArchiveEntry(entry);
				zout.closeArchiveEntry();
			}
			else
			{
				ArchiveEntry entry = zout.createArchiveEntry(kid, name);
				zout.putArchiveEntry(entry);
				copy(kid, zout);
				zout.closeArchiveEntry();
				
				cnt++;
				if (progress != null)
					progress[0] = progressOffset+cnt*progressAmount/nEntries;
			}
		}
		return cnt;
	}

	public static void unzip(File zipfile, File directory) throws IOException
		{unzip(zipfile, directory, null, 0, 1, true);}
	public static void unzip(File zipfile, File directory, boolean overwrite) throws IOException
		{unzip(zipfile, directory, null, 0, 1, overwrite);}
	public static void unzip(File zipfile, File directory, float [] progress) throws IOException
		{unzip(zipfile, directory, progress, 0, 1, true);}
	public static void unzip(File zipfile, File directory, float [] progress, boolean overwrite) throws IOException
		{unzip(zipfile, directory, progress, 0, 1, overwrite);}
	public static void unzip(File zipfile, File directory, float [] progress, float progressOffset, float progressAmount) throws IOException
		{unzip(zipfile, directory, progress, progressOffset, progressAmount, true);}
//	public static void unzip(File zipfile, File directory, double [] progress, boolean overwrite) throws IOException
//	{
//		ZipFile zfile = new ZipFile(zipfile);
//		
//		int nEntries = 0;
//		Enumeration<? extends ZipEntry> entries = zfile.entries();
//		while (entries.hasMoreElements())
//			if (!entries.nextElement().isDirectory())
//				nEntries++;
//		
//		int cnt = 0;
//		entries = zfile.entries();
//		while (entries.hasMoreElements())
//		{
//			ZipEntry entry = entries.nextElement();
//			File file = new File(directory, entry.getName());
//			if (entry.isDirectory())
//				file.mkdirs();
//			else
//			{
//				if (!file.exists() || overwrite)
//				{
//					file.getParentFile().mkdirs();
//					InputStream in = zfile.getInputStream(entry);
//					try {copy(in, file);}
//					finally {in.close();}
//				}
//				
//				cnt++;
//				if (progress != null)
//					progress[0] = cnt*1./nEntries;
//			}
//		}
//		zfile.close();
//	}
	public static void unzip(File zipfile, File directory, float [] progress, float progressOffset, float progressAmount, boolean overwrite) throws IOException
	{
		org.apache.commons.compress.archivers.zip.ZipFile zfile = new org.apache.commons.compress.archivers.zip.ZipFile(zipfile);
		
		int nEntries = 0;
		Enumeration<ZipArchiveEntry> entries = zfile.getEntries();
		while (entries.hasMoreElements())
			if (!entries.nextElement().isDirectory())
				nEntries++;
		
		int cnt = 0;
		entries = zfile.getEntries();
		while (entries.hasMoreElements())
		{
			ZipArchiveEntry entry = entries.nextElement();
			File file = new File(directory, entry.getName());
			if (entry.isDirectory())
				file.mkdirs();
			else
			{
				if (!file.exists() || overwrite)
				{
					file.getParentFile().mkdirs();
					InputStream in = zfile.getInputStream(entry);
					try {copy(in, file);}
					finally {in.close();}
				}
				
				cnt++;
				if (progress != null)
					progress[0] = progressOffset+cnt*progressAmount/nEntries;
			}
		}
		zfile.close();
	}

	static byte [] copyBuffer = new byte [1024];
	private static synchronized void copy(InputStream in, OutputStream out) throws IOException
	{
		while (true)
		{
			int readCount = in.read(copyBuffer);
			if (readCount < 0)
				break;
			out.write(copyBuffer, 0, readCount);
		}
	}

	private static void copy(File file, OutputStream out) throws IOException
	{
		InputStream in = new FileInputStream(file);
		try {copy(in, out);}
		finally {in.close();}
	}

	private static void copy(InputStream in, File file) throws IOException
	{
		OutputStream out = new FileOutputStream(file);
		try {copy(in, out);}
		finally {out.close();}
	}
}
