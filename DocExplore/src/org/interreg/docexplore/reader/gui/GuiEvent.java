package org.interreg.docexplore.reader.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.interreg.docexplore.util.Pair;


public class GuiEvent
{
	public static interface Source
	{
		public Object getDefaultMonitor();
		public Object getCurrentMonitor();
		public void setCurrentMonitor(Object o);
		public Object waitForEvent() throws InterruptedException;
		
		public void addActionListener(ActionListener listener);
		public void removeActionListener(ActionListener listener);
	}
	
	public static Pair<Source, Object> waitForEvent(Source ... sources) throws InterruptedException
	{
		Object commonMonitor = new Object();
		final Pair<Source, Object> res = new Pair<Source, Object>(null, null);
		ActionListener listener = new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			@SuppressWarnings("unchecked")
			Pair<Source, Object> pair = (Pair<Source, Object>)e.getSource();
			res.first = pair.first;
			res.second = pair.second;
		}};
		
		synchronized (commonMonitor)
		{
			for (Source button : sources)
				button.setCurrentMonitor(commonMonitor);
			for (Source button : sources)
				button.addActionListener(listener);
			
			commonMonitor.wait();
			
			for (Source button : sources)
				button.removeActionListener(listener);
			for (Source button : sources)
				button.setCurrentMonitor(button.getDefaultMonitor());
		}
		
		return res;
	}
}
