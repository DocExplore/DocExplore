package org.interreg.docexplore.util;

import java.io.File;
import java.nio.charset.Charset;

public class UTFEncoder
{
	static void encode(File root) throws Exception
	{
		for (File child : root.listFiles())
			if (child.getName().startsWith("book"))
		{
			File index = new File(child, "index.xml");
			StringUtils.writeFile(index, StringUtils.readFile(index, "x-MacRoman"), "UTF-8");
		}
		
		File mdDir = new File(root, "metadata");
		for (File child : mdDir.listFiles())
			if (child.getName().startsWith("metadata"))
		{
			File index = new File(child, "index.xml");
			if (index.exists() && StringUtils.readFile(index).contains("<Type>txt</Type>"))
			{
				File value = new File(child, "value");
				StringUtils.writeFile(value, StringUtils.readFile(value, "x-MacRoman"), "UTF-8");
			}
		}
	}
	
	public static void main(String [] args) throws Exception
	{
		for (String s : Charset.availableCharsets().keySet())
			System.out.println(s);
//		encode(new File("C:\\Users\\Alex\\DocExplore\\Untitled"));
	}
}
