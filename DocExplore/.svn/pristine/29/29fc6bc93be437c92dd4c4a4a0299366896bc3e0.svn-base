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
