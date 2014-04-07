package org.interreg.docexplore.management.plugin;

import java.io.File;
import java.lang.reflect.Method;

import org.interreg.docexplore.gui.ErrorHandler;

public class PluginImpl implements Plugin
{
	public Object plugin;
	public Method getName, setHost;
	
	public PluginImpl(Object plugin) throws Exception
	{
		this.plugin = plugin;
		
		this.getName = plugin.getClass().getMethod("getName");
		this.setHost = plugin.getClass().getMethod("setHost");
	}

	public String getName()
	{
		try {return (String)getName.invoke(plugin);} catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	
	public void setHost(File jarFile, File dependencies)
	{
		try {setHost.invoke(plugin);} catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
}
