/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;

public class MetaDataClipboard
{
	class Entry
	{
		File file;
		String key;
		String type;
		String style;
		
		Entry(MetaData annotation) throws Exception
		{
			this.file = new File(dir, "value"+entries.size());
			FileUtils.copyFile(annotation.getFile(), file);
			this.key = annotation.getKey().getName("");
			this.type = annotation.getType();
			List<MetaData> list = annotation.getMetaDataListForKey(annotation.getLink().getOrCreateKey("style", ""));
			if (list != null && list.size() > 0)
				this.style = list.get(0).getString();
			else this.style = null;
		}
		
		MetaData add(AnnotatedObject object) throws Exception
		{
			MetaData annotation = new MetaData(object.getLink(), object.getLink().getOrCreateKey(key, ""), type, new FileInputStream(file));
			if (style != null)
			{
				MetaData styleAnnotation = new MetaData(object.getLink(), annotation.getLink().getOrCreateKey("style", ""), style);
				annotation.addMetaData(styleAnnotation);
			}
			int rank = BookImporter.getHighestRank(object)+1;
			object.addMetaData(annotation);
			BookImporter.setRank(annotation, rank);
			return annotation;
		}
	}
	
	Frame tool;
	File dir;
	List<Entry> entries = new LinkedList<Entry>();
	
	public MetaDataClipboard(Frame tool, File dir)
	{
		this.tool = tool;
		this.dir = dir;
		if (!dir.exists())
			dir.mkdirs();
		else try {FileUtils.cleanDirectory(dir);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
	}
	
	public void copyMetaData(final AnnotatedObject object) throws Exception
	{
		final Exception [] ex = {null};
		GuiUtils.blockUntilComplete(new ProgressRunnable()
		{
			float progress = 0;
			public void run()
			{
				try
				{
					try {FileUtils.cleanDirectory(dir);}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
					entries.clear();
					
					Map<Integer, MetaData> mds = new TreeMap<Integer, MetaData>();
					for (Map.Entry<MetaDataKey, List<MetaData>> entry : object.getMetaData().entrySet())
						for (MetaData md : entry.getValue())
							mds.put(BookImporter.getRank(md), md);
					
					int cnt = 0;
					for (MetaData annotation : mds.values())
					{
						entries.add(new Entry(annotation));
						progress = (++cnt)*1f/mds.size();
					}
				}
				catch (Exception e) {ex[0] = e;}
			}
			public float getProgress() {return progress;}
		}, tool);
		
		if (ex[0] != null)
			throw ex[0];
	}
	
	public List<MetaData> pasteMetaData(final AnnotatedObject object) throws Exception
	{
		final List<MetaData> res = new LinkedList<MetaData>();
		final Exception [] ex = {null};
		GuiUtils.blockUntilComplete(new ProgressRunnable()
		{
			float progress = 0;
			public void run()
			{
				try
				{
					for (Entry entry : entries)
						res.add(entry.add(object));
				}
				catch (Exception e) {ex[0] = e;}
			}
			public float getProgress() {return progress;}
		}, tool);
		
		if (ex[0] != null)
			throw ex[0];
		return res;
	}
	
	public boolean canPaste() {return entries.size() > 0;}
}
