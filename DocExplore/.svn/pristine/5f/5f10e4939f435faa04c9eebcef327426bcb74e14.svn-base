package org.interreg.docexplore.management.plugin.analysis;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalysisPluginTask
{
	public interface Listener
	{
		public void taskStarted(AnalysisPluginTask task);
		public void taskCompleted(AnalysisPluginTask task);
	}
	List<Listener> listeners = new LinkedList<Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	void notifyTaskStarted()
	{
		for (Listener listener : listeners)
			listener.taskStarted(this);
		setup.notifyTaskStarted(this);
	}
	void notifyTaskCompleted()
	{
		for (Listener listener : listeners)
			listener.taskCompleted(this);
		setup.notifyTaskCompleted(this);
		setup.win.notifications.addNotification(plugin.getTasks()[task]+" ("+plugin.getName()+") completed");
	}
	
	public AnalysisPluginSetup setup;
	public AnalysisPlugin plugin;
	public int task;
	public BufferedImage [] input = new BufferedImage [0];
	public Map<String, Object> params = new TreeMap<String, Object>();
	public Map<String, Component> results = new TreeMap<String, Component>();
	
	boolean started = false, completed = false, success = false;
	long startTime = -1, endTime = -1;
	Exception error = null;
	
	public AnalysisPluginTask(AnalysisPluginSetup setup, AnalysisPlugin plugin, int task)
	{
		this.setup = setup;
		this.plugin = plugin;
		this.task = task;
	}
	
	public void setParams(Map<String, Object> params) {this.params = params;}
	public void addParam(String name, Object value) {params.put(name, value);}
	public Object getParam(String name) {return params.get(name);}
	public void setInput(BufferedImage [] input) {this.input = input;}
	
	String customStatus = null;
	public void setCustomStatus(String customStatus) {this.customStatus = customStatus;}
	public boolean isCompleted() {return completed;}
	
	public synchronized void start()
	{
		if (started)
			return;
		
		startTime = System.currentTimeMillis();
		started = true;
		new Thread() {public void run()
		{
			notifyTaskStarted();
			try
			{
				plugin.process(input, params, task, results, AnalysisPluginTask.this);
				success = true;
			}
			catch (Exception e) {error = e; e.printStackTrace();}
			endTime = System.currentTimeMillis();
			completed = true;
			notifyTaskCompleted();
		}}.start();
	}
}
