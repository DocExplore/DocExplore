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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.datalink.objects.MetaDataData;
import org.interreg.docexplore.util.ByteUtils;
import org.interreg.docexplore.util.Pair;
import org.interreg.docexplore.util.StringUtils;

public class MetaDataFS2
{
	public static File getMDRoot(File root)
	{
		File mdRoot = new File(root, "metadata");
		if (!mdRoot.exists())
			mdRoot.mkdir();
		return mdRoot;
	}
	public static File getMDDir(File root, int mdId) {return new File(getMDRoot(root), "metadata"+mdId);}
	public static File getKeyDir(File root, int keyId) {return new File(getMDRoot(root), "key"+keyId);}
	
	public static int getKeyId(File root, int mdId) throws IOException
	{
		File mdFile = new File(getMDDir(root, mdId), "index.xml");
		String data = StringUtils.readFile(mdFile);
		return Integer.parseInt(StringUtils.getTagContent(data, "Key").trim());
	}
	
	public static String getType(File root, int mdId) throws IOException
	{
		File mdFile = new File(getMDDir(root, mdId), "index.xml");
		String data = StringUtils.readFile(mdFile);
		return StringUtils.getTagContent(data, "Type").trim();
	}
	
	public static List<Pair<Integer, Integer>> getMetaData(File root, File dir) throws IOException
	{
		String data = StringUtils.readFile(new File(dir, "index.xml"));
		
		List<Pair<Integer, Integer>> mds = new LinkedList<Pair<Integer,Integer>>();
		List<String> mdDatas = StringUtils.getTagsContent(data, "MetaData");
		for (String mdData : mdDatas) try
		{
			int mdId = Integer.parseInt(mdData.trim());
			mds.add(new Pair<Integer, Integer>(mdId, getKeyId(root, mdId)));
		}
		catch (Exception e) {System.out.println("In "+dir.getAbsolutePath()); e.printStackTrace();}
		
		return mds;
	}
	
	public static void addMetaDataToObject(File dir, int metaDataId) throws IOException
	{
		File dataFile = new File(dir, "index.xml");
		String data = StringUtils.readFile(dataFile);
		int end = data.lastIndexOf('<');
		data = data.substring(0, end)+"\t<MetaData>"+metaDataId+"</MetaData>\n"+data.substring(end);
		StringUtils.writeFile(dataFile, data);
	}
	
	public static void removeMetaDataFromObject(File dir, int metaDataId) throws IOException
	{
		File dataFile = new File(dir, "index.xml");
		String data = StringUtils.readFile(dataFile);
		data = data.replace("\t<MetaData>"+metaDataId+"</MetaData>\n", "");
		StringUtils.writeFile(dataFile, data);
	}
	
	public static String getMetaDataKeyName(File root, int id, String language) throws IOException
	{
		File keyDir = getKeyDir(root, id);
		if (!keyDir.exists())
			return null;
		File langFile = new File(keyDir, language);
		if (!langFile.exists())
			return null;
		return StringUtils.readFile(langFile);
	}
	
	public static void setMetaDataKey(File root, int mdId, int keyId) throws IOException
	{
		File mdFile = new File(getMDDir(root, mdId), "index.xml");
		String data = StringUtils.readFile(mdFile);
		data = StringUtils.setTagContent(data, "Key", ""+keyId);
		StringUtils.writeFile(mdFile, data);
	}
	
	public static MetaDataData getMetaDataData(File root, int mdId) throws IOException
	{
		File mdDir = getMDDir(root, mdId);
		File mdFile = new File(mdDir, "index.xml");
		String data = StringUtils.readFile(mdFile);
		int keyId = Integer.parseInt(StringUtils.getTagContent(data, "Key").trim());
		String type = StringUtils.getTagContent(data, "Type").trim();
		return new MetaDataData(keyId, type, getMetaData(root, mdDir));
	}
	
	public static InputStream getMetaDataValue(File root, int mdId) throws IOException
	{
		File mdDir = getMDDir(root, mdId);
		return new FileInputStream(new File(mdDir, "value"));
	}
	
	public static File getMetaDataFile(File root, int mdId) throws IOException
	{
		File mdDir = getMDDir(root, mdId);
		return new File(mdDir, "value");
	}
	
	public static void setMetaDataValue(File root, int mdId, InputStream stream) throws IOException
	{
		File mdDir = getMDDir(root, mdId);
		ByteUtils.writeStream(stream, new FileOutputStream(new File(mdDir, "value")));
	}
	
	public static int addMetaData(File root, int keyId, String type) throws IOException
	{
		int id = DataLinkFS2.getNextId(root);
		File mdDir = getMDDir(root, id);
		mdDir.mkdir();
		StringUtils.writeFile(new File(mdDir, "index.xml"), "<Annotation>\n\t<Key>"+keyId+"</Key>\n\t<Type>"+type+"</Type>\n</Annotation>");
		return id;
	}
	
	public static void removeMetaData(File root, int mdId) throws IOException
	{
		File mdDir = getMDDir(root, mdId);
		FS2Utils.delete(mdDir);
	}
	
	public static List<Integer> getMetaDataIds(File root, int keyId, String type) throws IOException
	{
		List<Integer> res = new LinkedList<Integer>();
		
		File mdRoot = getMDRoot(root);
		for (File mdDir : mdRoot.listFiles())
			if (mdDir.isDirectory() && mdDir.getName().startsWith("metadata"))
		{
			String data = StringUtils.readFile(new File(mdDir, "index.xml"));
			if (data.indexOf("<Key>"+keyId+"</Key>") > 0)
				if (type == null || data.indexOf("<Type>"+type+"</Type>") > 0)
					res.add(Integer.parseInt(mdDir.getName().substring("metadata".length())));
		}
		
		return res;
	}
	
	public static int getMetaDataKeyId(File root, String name, String language) throws IOException
	{
		File mdRoot = getMDRoot(root);
		for (File keyDir : mdRoot.listFiles())
			if (keyDir.isDirectory() && keyDir.getName().startsWith("key"))
		{
			File nameFile = new File(keyDir, language);
			if (nameFile.exists() && StringUtils.readFile(nameFile).equals(name))
				return Integer.parseInt(keyDir.getName().substring("key".length()));
		}
		return -1;
	}
	
	public static int addMetaDataKey(File root) throws IOException
	{
		int id = DataLinkFS2.getNextId(root);
		File keyDir = getKeyDir(root, id);
		keyDir.mkdir();
		return id;
	}
	
	public static void removeMetaDataKey(File root, int keyId) throws IOException
	{
		File keyDir = getKeyDir(root, keyId);
		System.out.println("delete root "+keyDir.getAbsolutePath());
		FS2Utils.deleteRoot(keyDir);
	}
	
	public static void setMetaDataKeyName(File root, int keyId, String name, String language) throws IOException
	{
		File keyDir = getKeyDir(root, keyId);
		if (!keyDir.exists())
			keyDir.mkdirs();
		StringUtils.writeFile(new File(keyDir, language), name);
	}
	
	public static List<Integer> getMetaDataKeyIds(File root) throws IOException
	{
		List<Integer> res = new LinkedList<Integer>();
		File mdRoot = getMDRoot(root);
		for (File keyDir : mdRoot.listFiles())
			if (keyDir.isDirectory() && keyDir.getName().startsWith("key"))
				res.add(Integer.parseInt(keyDir.getName().substring("key".length())));
		return res;
	}
	
	public static List<Pair<Integer, String>> getMetaDataText(File root, File dir, Collection<Integer> keyIds) throws IOException
	{
		List<Pair<Integer, String>> res = new LinkedList<Pair<Integer,String>>();
		String indexFile = StringUtils.readFile(new File(dir, "index.xml"));
		List<String> mdsContent = StringUtils.getTagsContent(indexFile, "MetaData");
		for (String mdContent : mdsContent)
		{
			int mdId = Integer.parseInt(mdContent.trim());
			int keyId = getKeyId(root, mdId);
			if ((keyIds == null || keyIds.contains(keyId)) && getType(root, mdId).equals("txt"))
				res.add(new Pair<Integer, String>(keyId, new String(ByteUtils.readStream(getMetaDataValue(root, mdId)))));
		}
		return res;
	}
}
