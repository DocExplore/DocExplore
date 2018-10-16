/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.util;

import java.io.File;

public class UTFEncoder
{
	static void encodeDB(File root, String from) throws Exception
	{
		for (File child : root.listFiles())
			if (child.getName().startsWith("book"))
		{
			File index = new File(child, "index.xml");
			StringUtils.writeFile(index, StringUtils.readFile(index, from), "UTF-8");
		}
		
		File mdDir = new File(root, "metadata");
		for (File child : mdDir.listFiles())
			if (child.getName().startsWith("metadata"))
			{
				File index = new File(child, "index.xml");
				if (index.exists() && StringUtils.readFile(index).contains("<Type>txt</Type>"))
				{
					File value = new File(child, "value");
					StringUtils.writeFile(value, StringUtils.readFile(value, from), "UTF-8");
				}
			}
			else if (child.getName().startsWith("key"))
			{
				for (File trans : child.listFiles())
					StringUtils.writeFile(trans, StringUtils.readFile(trans, from), "UTF-8");
			}
	}
	static void encodeReader(File root, String from) throws Exception
	{
		for (File child : root.listFiles())
			if (child.getName().endsWith(".xml"))
				StringUtils.writeFile(child, StringUtils.readFile(child, from), "UTF-8");
	}
	
	public static void main(String [] args) throws Exception
	{
//		for (String s : Charset.availableCharsets().keySet())
//			System.out.println(s);
		//encodeDB(new File("C:\\sci\\workspace\\DocExploreFactory\\HomeInit\\db"), "ISO-8859-1");
//		encodeReader(new File("C:\\sci\\workspace\\DocExploreFactory\\HomeInit\\reader"), "ISO-8859-1");
	}
}
