package org.interreg.docexplore.management.plugin.analysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class PluginResultFrame extends JDialog
{
	JTabbedPane tabs;
	
	public PluginResultFrame(Frame win)
	{
		super(win, XMLResourceBundle.getBundledString("pluginResultLabel"));
		GuiUtils.centerOnComponent(this, win);
		setLayout(new BorderLayout());
		
		this.tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);
		
		tabs.setPreferredSize(new Dimension(800, 600));
		pack();
		//setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
	
	public void addResults(Map<String, Component> results)
	{
		for (Map.Entry<String, Component> entry : results.entrySet())
		{
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(entry.getValue(), BorderLayout.CENTER);
			tabs.addTab(entry.getKey(), panel);
		}
	}
}
