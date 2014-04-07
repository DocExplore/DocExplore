package org.interreg.docexplore.management.plugin.analysis;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.management.plugin.Plugin;

public interface AnalysisPlugin extends Plugin
{
	public void process(BufferedImage [] images, Map<String, Object> params, int task, Map<String, Component> results, AnalysisPluginTask monitor) throws Exception;
	public void init(MainWindow win) throws Exception;
	public String [] getTasks();
}
