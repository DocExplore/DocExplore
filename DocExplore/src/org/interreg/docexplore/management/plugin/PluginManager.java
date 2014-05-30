/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
			Plugin plugin = plugin(config);
			if (plugin != null)
				System.out.println("Loaded plugin '"+plugin.getName()+"' ("+plugin.getClass().getSimpleName()+")");
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
	
	public Plugin plugin(PluginConfig config) throws Exception
	{
		if (!Plugin.class.isAssignableFrom(config.clazz))
			return null;
		Plugin pluginObject = (Plugin)config.clazz.newInstance();
		pluginObject.setHost(config.jarFile, config.dependencies);
		
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
