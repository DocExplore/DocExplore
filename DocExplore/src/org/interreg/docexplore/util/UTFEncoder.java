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
