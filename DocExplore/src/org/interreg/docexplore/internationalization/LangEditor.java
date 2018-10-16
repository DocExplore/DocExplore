/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.internationalization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

public class LangEditor
{
	static class Key
	{
		String name;
		Map<String, String> values = new TreeMap<>();
		
		Key(String name)
		{
			this.name = name;
		}
		
		boolean sameAs(Key key)
		{
			for (String lang : values.keySet())
			{
				String value = key.values.get(lang);
				if (value == null || !value.equals(values.get(lang)))
					return false;
			}
			return true;
		}
	}
	
	static class Bundle
	{
		Map<String, Key> keys = new TreeMap<>();
		
		Bundle(File dir, String name) throws Exception
		{
			String [] entry = {"", ""};
			File [] files = dir.listFiles();
			for (int i=0;i<files.length;i++)
				if (files[i].getName().startsWith(name))
			{
				int end = files[i].getName().indexOf(".xml");
				int start = name.length()+1;
				String lang = start >= end ? "" : files[i].getName().substring(start, end);
				String data = FileUtils.readFileToString(files[i]);
				int pos = 0;
				while ((pos = extractEntry(data, pos, entry)) >= 0)
				{
					Key key = keys.get(entry[0]);
					if (key == null)
						keys.put(entry[0], key = new Key(entry[0]));
					key.values.put(lang, entry[1]);
				}
			}
		}
	}
	
	static int extractEntry(String data, int pos, String [] entry)
	{
		pos = data.indexOf("<entry", pos);
		if (pos < 0)
			return -1;
		pos = data.indexOf("key=\"", pos)+"key=\"".length();
		int end = data.indexOf("\"", pos);
		entry[0] = data.substring(pos, end);
		pos = data.indexOf(">", pos)+">".length();
		end = data.indexOf("</entry>", pos);
		entry[1] = data.substring(pos, end).trim();
		return end+"</entry>".length();
	}
	
	static Map<String, Bundle> extractBundles(File dir)
	{
		Map<String, Bundle> bundles = new TreeMap<>();
		File [] files = dir.listFiles();
		for (int i=0;i<files.length;i++)
			if (!files[i].getName().startsWith("lang-lrb") && files[i].getName().endsWith("-lrb.xml"))
		{
			String name = files[i].getName().substring(0, files[i].getName().length()-".xml".length());
			try {bundles.put(name, new Bundle(dir, name));}
			catch (Throwable t) {t.printStackTrace();}
		}
		return bundles;
	}
	
	static Map<String, List<Key>> extractKeys(Map<String, Bundle> bundles)
	{
		Map<String, List<Key>> keys = new TreeMap<>();
		for (Bundle bundle : bundles.values())
			for (Key key : bundle.keys.values())
		{
			List<Key> list = keys.get(key.name);
			if (list == null)
				keys.put(key.name, list = new ArrayList<>(1));
			boolean found = false;
			for (int i=0;i<list.size() && !found;i++)
				if (list.get(i).sameAs(key))
					found = true;
			if (!found)
				list.add(key);
		}
		return keys;
	}
	
	static class Entry
	{
		File file;
		String [] keys;
		int [] keyPos;
		String lrb;
		
		Entry(File file, String data, int pos) throws Exception
		{
			this.file = file;
			while (data.charAt(pos) != '(')
				pos++;
			int open = pos;
			pos++;
			int level = 0, quotes = 0;
			while (true)
			{
				if (data.charAt(pos) == '\"') quotes++;
				else if (data.charAt(pos) == ')') level--;
				else if (data.charAt(pos) == '(') level++;
				if (level < 0) break;
				pos++;
			}
			String [] vals = new String [quotes/2];
			int [] valPos = new int [quotes/2];
			pos = open;
			for (int i=0;i<vals.length;i++)
			{
				int from = data.indexOf('\"', pos);
				int to = data.indexOf('\"', from+1);
				vals[i] = data.substring(from+1, to);
				valPos[i] = from+1;
				pos = to+1;
			}
			
			if (vals.length > 1 && vals[0].endsWith("-lrb"))
			{
				lrb = vals[0];
				this.keys = new String [vals.length-1];
				this.keyPos = new int [vals.length-1];
				for (int i=0;i<keys.length;i++)
				{
					keys[i] = vals[i+1];
					keyPos[i] = valPos[i+1];
				}
			}
			else
			{
				lrb = lrb(file);
				keys = vals;
				keyPos = valPos;
			}
			
//			System.out.print(lrb+" ");
//			for (int i=0;i<keys.length;i++) System.out.print(keys[i]+" ");
//			System.out.println();
		}
	}
	
	public static String lrb(File file) throws Exception
	{
		String path = file.getCanonicalPath();
		int index = path.indexOf("org\\interreg\\docexplore\\");
		if (index >= 0)
		{
			String sub = path.substring(index+"org\\interreg\\docexplore\\".length());
			index = sub.indexOf('\\');
			if (index < 0)
				return "default-lrb";
			return sub.substring(0, index)+"-lrb";
		}
		return null;
	}
	
	public static void findEntries(File file, Collection<Entry> entries)
	{
		if (file.isDirectory())
		{
			File [] children = file.listFiles();
			for (int i=0;i<children.length;i++)
				if (children[i].isDirectory() || children[i].getName().endsWith(".java"))
					findEntries(children[i], entries);
		}
		else try
		{
			String data = FileUtils.readFileToString(file);
			int pos = 0;
			while ((pos = data.indexOf("Lang.s(", pos)) > 0)
			{
				try {entries.add(new Entry(file, data, pos));}
				catch (Throwable t) {t.printStackTrace();}
				pos++;
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public static Map<String, List<Entry>> entriesByKey(List<Entry> entries)
	{
		Map<String, List<Entry>> byKey = new TreeMap<>();
		for (Entry entry : entries)
			for (String key : entry.keys)
		{
			List<Entry> list = byKey.get(key);
			if (list == null)
				byKey.put(key, list = new ArrayList<>(1));
			list.add(entry);
		}
		return byKey;
	}
	
	public static void writeLang(Map<String, List<Key>> keys, String lang, BufferedWriter writer) throws Exception
	{
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n<properties>\n");
		writer.write("\t<comment>"+lang+"</comment>\n\n");
		for (List<Key> list : keys.values())
		{
			Key key = list.get(0);
			writer.write("\t<entry key=\""+key.name+"\">");
			String val = key.values.get(lang);
			if (val != null)
				writer.write(val);
			writer.write("</entry>\n");
		}
		writer.write("</properties>\n");
		writer.close();
	}
	
	public static void main(String [] args) throws Exception
	{
		if (args.length == 0)
			throw new Exception("Missing root file!");
		
		File root = new File(args[0]);
		List<Entry> entries = new ArrayList<>();
		findEntries(root, entries);
		Map<String, List<Entry>> entriesByKey = entriesByKey(entries);
		
		Map<String, Bundle> bundles = extractBundles(root);
		Map<String, List<Key>> keys = extractKeys(bundles);
		for (Map.Entry<String, List<Key>> pair : keys.entrySet())
			if (pair.getValue().size() > 1)
		{
			System.out.print(pair.getKey()+": ");
			List<Entry> list = entriesByKey.get(pair.getKey());
			if (list != null)
				for (Entry entry : list)
					System.out.print(entry.file.getCanonicalPath().substring(root.getCanonicalPath().length())+" ");
			System.out.println();
		}
		
		writeLang(keys, "", new BufferedWriter(new FileWriter(new File(root, "lang-lrb.xml"), false)));
		writeLang(keys, "fr", new BufferedWriter(new FileWriter(new File(root, "lang-lrb_fr.xml"), false)));
	}
}
