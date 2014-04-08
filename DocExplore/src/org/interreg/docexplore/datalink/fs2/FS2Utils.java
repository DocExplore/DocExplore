/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
