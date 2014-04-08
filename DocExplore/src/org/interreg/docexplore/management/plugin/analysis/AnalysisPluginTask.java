/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
