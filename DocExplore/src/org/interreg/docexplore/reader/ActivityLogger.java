/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;

public class ActivityLogger
{
	static class Entry
	{
		long timestamp;
		String value;
		
		Entry(String value)
		{
			this.timestamp = System.currentTimeMillis();
			this.value = value;
		}
	}
	
	File dumpFile;
	LinkedList<Entry> entries = new LinkedList<Entry>();
	boolean running = true;
	
	public ActivityLogger(File file, final long period)
	{
		this.dumpFile = file;
		
		new Thread() {public void run()
		{
			while (running)
			{
				flush();
				try {Thread.sleep(period);}
				catch (Exception e) {}
			}
		}}.start();
	}
	
	public void addEntry(String value)
	{
		Entry entry = new Entry(value);
		synchronized (entries) {entries.add(entry);}
	}
	
	public synchronized void dispose()
	{
		flush();
		running = false;
	}
	
	public synchronized void flush()
	{
		if (!running)
			return;
		try
		{
			synchronized (entries)
			{
				if (entries.isEmpty())
					return;
				FileWriter out = new FileWriter(dumpFile, true);
				for (Entry entry : entries)
					out.write(entry.timestamp+" : "+entry.value+"\n");
				entries.clear();
				out.flush();
				out.close();
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
