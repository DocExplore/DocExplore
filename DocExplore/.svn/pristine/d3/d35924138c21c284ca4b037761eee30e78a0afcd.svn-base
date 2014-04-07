package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.interreg.docexplore.util.ByteUtils;

public class FS2Utils
{
	static Set<String> dirPrefixes = new TreeSet<String>();
	static Set<String> filePrefixes = new TreeSet<String>();
	static
	{
		dirPrefixes.add("book");
		dirPrefixes.add("page");
		dirPrefixes.add("region");
		dirPrefixes.add("metadata");
		dirPrefixes.add("key");
		
		filePrefixes.add("index");
		filePrefixes.add("value");
		filePrefixes.add("en");
		filePrefixes.add("fr");
		filePrefixes.add("image");
	};
	static boolean isFS2File(File file)
	{
		int index = 0;
		String name = file.getName();
		while (index < name.length() && Character.isLetter(name.charAt(index)))
			index++;
		name = name.substring(0, index);
		if (file.isDirectory())
			return dirPrefixes.contains(name);
		return filePrefixes.contains(name);
	}
	public static void delete(File file)
	{
		if (!isFS2File(file))
			return;
		if (file.isDirectory())
			for (File child : file.listFiles())
				delete(child);
		file.delete();
	}
	public static void deleteRoot(File file)
	{//System.out.println("del "+file.getAbsolutePath());
		if (file.isDirectory())
			for (File child : file.listFiles())
				delete(child);
		file.delete();
	}
	
	public static void copy(File from, File to) throws IOException
	{
		if (from.isDirectory())
		{
			if (!to.exists())
				to.mkdirs();
			for (File child : from.listFiles())
				copy(child, new File(to, child.getName()));
		}
		else ByteUtils.writeFile(to, ByteUtils.readFile(from));
	}
}
