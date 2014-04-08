/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLink.DataLinkSource;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.ManuscriptLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.FileImageSource;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Pair;

/**
 * A special data link that wraps around an existing link and creates meta data keys
 * if needed.
 * @author Burnett
 */
public final class DocExploreDataLink extends ManuscriptLink
{
	public MetaDataKey transcriptionKey;
	public MetaDataKey linkKey;
	public MetaDataKey tagKey;
	public MetaDataKey tagsKey;
	public MetaDataKey miniKey;
	public MetaDataKey dimKey;
	public MetaDataKey sourceKey;
	
	public Set<MetaDataKey> readOnlyKeys, functionalKeys;
	
	public DocExploreDataLink()
	{
		super(null);
		
		try {setLink(null);}
		catch (Exception e) {}
	}
	
	public static interface Listener
	{
		public void dataLinkChanged(DataLink link);
	}
	List<Listener> listeners = new LinkedList<DocExploreDataLink.Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	public void notifyDataLinkChanged()
	{
		for (Listener listener : listeners)
			listener.dataLinkChanged(link);
	}
	
	public void setLink(DataLink link) throws DataLinkException
	{
		if (this.link != null)
			release();
		
		this.link = link;
		clearPersistentObjects();
		
		if (link != null)
		{
			Object autoWrite = link.getProperty("autoWrite");
			boolean wasAutoWrite = autoWrite == null ? true : (Boolean)autoWrite;
			if (wasAutoWrite)
				link.setProperty("autoWrite", false);
			try
			{
				this.transcriptionKey = getOrCreateKey("transcription", "en");
				transcriptionKey.setName("transcription", "fr");
				this.linkKey = getOrCreateKey("link", "en");
				linkKey.setName("lien", "fr");
				this.tagKey = getOrCreateKey("tag", "en");
				tagKey.setName("tag", "fr");
				this.tagsKey = getOrCreateKey("tags", "");
				this.miniKey = getOrCreateKey("mini", "");
				this.dimKey = getOrCreateKey("dimension", "");
				this.sourceKey = getOrCreateKey("source-file", "");
				
				this.readOnlyKeys = new TreeSet<MetaDataKey>();
				readOnlyKeys.add(miniKey);
				readOnlyKeys.add(dimKey);
				readOnlyKeys.add(sourceKey);
				
				this.functionalKeys = new TreeSet<MetaDataKey>();
				functionalKeys.add(miniKey);
				functionalKeys.add(dimKey);
				functionalKeys.add(sourceKey);
				functionalKeys.add(transcriptionKey);
				functionalKeys.add(linkKey);
				functionalKeys.add(tagKey);
				functionalKeys.add(tagsKey);
				
				if (wasAutoWrite)
					link.setProperty("autoWrite", true);
			}
			catch (DataLinkException e)
			{
				if (wasAutoWrite)
					link.setProperty("autoWrite", true);
				throw e;
			}
		}
		else
		{
			this.transcriptionKey = null;
			this.linkKey = null;
			this.tagKey = null;
			this.miniKey = null;
		}
		
		notifyDataLinkChanged();
	}
	
	public boolean isLinked() {return link != null;}
	public DataLinkSource getWrappedSource() {return link.getSource();}
	public void release()
	{
		link.release(); 
		link = null;
	}
	
	public MetaData getOrCreateTag(String name, String lang) throws DataLinkException
	{
		if (link == null)
			return null;
		lang = lang.equals("") ? "en" : lang;
		Set<MetaData> tags = tagKey.getMetaData(MetaData.textType);
		for (MetaData tag : tags)
			if (tag.getString().contains("<tag lang=\""+lang+"\">"+name+"</tag>"))
				return tag;
		if (!lang.equals("en"))
			for (MetaData tag : tags)
				if (tag.getString().contains("<tag lang=\"en\">"+name+"</tag>"))
					return tag;
		String value = "<tag lang=\""+lang+"\">"+name+"</tag>";
		if (lang.length() != 0)
			value += "<tag lang=\"en\">"+name+"</tag>";
		MetaData tag = new MetaData(this, tagKey, value);
		return tag;
	}
	public MetaData getOrCreateTag(String name) throws DataLinkException
	{
		return getOrCreateTag(name, Locale.getDefault().getLanguage());
	}
	
	public static String getBestTagName(MetaData tag) throws DataLinkException
	{
		String name = getTagName(tag);
		if (name == null)
			name = getTagName(tag, "");
		return name;
	}
	public static String getTagName(MetaData tag) throws DataLinkException
	{
		return getTagName(tag, Locale.getDefault().getLanguage());
	}
	public static String getTagName(MetaData tag, String lang) throws DataLinkException
	{
		lang = lang.equals("") ? "en" : lang;
		String tagXml = tag.getString();
		String head = "<tag lang=\""+lang+"\">";
		int start = tagXml.indexOf(head);
		if (start < 0)
			return null;
		int end = tagXml.indexOf("</tag>", start);
		return tagXml.substring(start+head.length(), end);
	}
	public static void setTagName(MetaData tag, String name) throws DataLinkException
	{
		setTagName(tag, name, Locale.getDefault().getLanguage());
	}
	public static void setTagName(MetaData tag, String name, String lang) throws DataLinkException
	{
		lang = lang.equals("") ? "en" : lang;
		String tagXml = tag.getString();
		String head = "<tag lang=\""+lang+"\">";
		int start = tagXml.indexOf(head);
		if (start < 0)
			tagXml += head+name+"</tag>";
		else
		{
			int end = tagXml.indexOf("</tag>", start);
			tagXml = tagXml.substring(0, start)+head+name+tagXml.substring(end);
		}
		tag.setString(tagXml);
	}
	
	public static Dimension getImageDimension(Page page) throws DataLinkException {return getImageDimension(page, false);}
	public static Dimension getImageDimension(Page page, boolean noUnload) throws DataLinkException
	{
		ManuscriptLink link = page.getLink();
		MetaDataKey dimKey = link.getOrCreateKey("dimension", "");
		List<MetaData> dims = page.getMetaDataListForKey(dimKey);
		
		for (MetaData data : dims)
			if (data.getType().equals(MetaData.textType))
		{
			String [] values = data.getString().split(",");
			if (values.length != 2)
				continue;
			try
			{
				int w = Integer.parseInt(values[0]);
				int h = Integer.parseInt(values[1]);
				return new Dimension(w, h);
			}
			catch (Exception e) {}
		}
		
		try
		{
			BufferedImage image = page.getImage().getImage();
			page.addMetaData(new MetaData(link, dimKey, image.getWidth()+","+image.getHeight()));
			if (!noUnload)
				page.unloadImage();
			return new Dimension(image.getWidth(), image.getHeight());
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	
	public static BufferedImage getImageMini(Page page) throws Exception {return getImageMini(page, false);}
	public static BufferedImage getImageMini(Page page, boolean noUnload) throws Exception
	{
		MetaDataKey miniKey = page.getLink().getKey("mini", "");
		BufferedImage mini = null;
		List<MetaData> minis = page.getMetaDataListForKey(miniKey);
		
		if (!minis.isEmpty())
		{
			mini = minis.get(0).getImage();
			return mini;
		}
		
		mini = ImageUtils.createIconSizeImage(page.getImage().getImage(), 64f);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageUtils.write(mini, "png", os);
		InputStream is = new ByteArrayInputStream(os.toByteArray());
		page.addMetaData(new MetaData(page.getLink(), miniKey, MetaData.imageType, is));
		if (!noUnload)
			page.unloadImage();
		return mini;
	}
	
	public static boolean isSamePage(AnnotatedObject d1, AnnotatedObject d2)
	{
		Page p1 = (Page)(d1 instanceof Page ? d1 : d1 instanceof Region ? ((Region)d1).getPage() : null);
		if (p1 == null)
			return false;
		return p1 == (Page)(d2 instanceof Page ? d2 : d2 instanceof Region ? ((Region)d2).getPage() : null);
	}
	
	public static File getOrExtractMetaDataFile(MetaData annotation) throws DataLinkException
	{
		File file = annotation.getFile();
		if (file == null)
		{
			InputStream value = annotation.getValue();
			File outputDir = new File(DocExploreTool.getHomeDir(), "temp");
			outputDir.mkdirs();
			file = new File(outputDir, "md_"+Double.doubleToLongBits(Math.random()));
			FileOutputStream stream = null;
			try
			{
				stream = new FileOutputStream(file);
				byte [] buffer = new byte [2048];
				int read;
				while ((read = value.read(buffer)) >= 0)
					stream.write(buffer, 0, read);
				stream.close();
				value.close();
			}
			catch (Exception e) {throw new DataLinkException(annotation.getLink().getLink(), e);}
		}
		return file;
	}
	
	public List<Pair<Page, File>> appendPages(Book book, final List<File> files, final List<Pair<Page, File>> failed, final double [] progress)
	{
		final List<Pair<Page, File>> pages = new LinkedList<Pair<Page, File>>();
		final AtomicInteger cnt = new AtomicInteger(0);
		ExecutorService service = Executors.newFixedThreadPool(2);
		
		try
		{
			for (final File file : files)
			{
				final Page page = book.appendPage(new FileImageSource(file));
				pages.add(new Pair<Page, File>(page, file));
				service.execute(new Runnable() {public void run()
				{
					try
					{
						page.addMetaData(new MetaData(DocExploreDataLink.this, sourceKey, file.getName()));
						BufferedImage image = page.getImage().getImage();
						page.addMetaData(new MetaData(DocExploreDataLink.this, dimKey, image.getWidth()+","+image.getHeight()));
						getImageMini(page);
					}
					catch (Exception e)
					{
						ErrorHandler.defaultHandler.submit(e, true);
						synchronized (failed) {failed.add(new Pair<Page, File>(page, file));}
					}
					
					cnt.incrementAndGet();
					progress[0] = cnt.get()*1./files.size();
				}});
			}
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		
		while (cnt.get() < files.size())
			try {Thread.sleep(50);}
			catch (Exception e) {}
		return pages;
	}
}
