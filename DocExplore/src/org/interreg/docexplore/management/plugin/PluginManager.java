package org.interreg.docexplore.management.plugin;

import java.util.Vector;

import org.interreg.docexplore.Startup;
import org.interreg.docexplore.Startup.PluginConfig;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.management.plugin.analysis.AnalysisPlugin;
import org.interreg.docexplore.management.plugin.analysis.AnalysisPluginSetup;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;

public class PluginManager implements HostInterface
{
	public Vector<AnalysisPlugin> analysisPlugins = new Vector<AnalysisPlugin>();
	public Vector<MetaDataPlugin> metaDataPlugins = new Vector<MetaDataPlugin>();
	public final AnalysisPluginSetup analysisPluginSetup;
	
	public PluginManager(Startup startup)
	{
		for (PluginConfig config : startup.plugins) try
		{
			Plugin plugin = null;
			if ((plugin = plugin(config.clazz.newInstance())) != null)
			{
				plugin.setHost(config.jarFile, config.dependencies);
				System.out.println("Loaded plugin '"+plugin.getName()+"' ("+plugin.getClass().getSimpleName()+")");
			}
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
		
		this.analysisPluginSetup = new AnalysisPluginSetup(this);
	}
	
	public void initAnalysisPlugins(MainWindow win)
	{
		analysisPluginSetup.win = win;
		for (AnalysisPlugin plugin : analysisPlugins)
			try {plugin.init(win);}
			catch (Exception e) {e.printStackTrace();}
	}
	
	public Plugin plugin(Object pluginObject)
	{
		if (pluginObject instanceof MetaDataPlugin)
		{
			metaDataPlugins.add((MetaDataPlugin)pluginObject);
			return (MetaDataPlugin)pluginObject;
		}
		else if (pluginObject instanceof AnalysisPlugin)
		{
			analysisPlugins.add((AnalysisPlugin)pluginObject);
			return (AnalysisPlugin)pluginObject;
		}
		return null;
	}
}
