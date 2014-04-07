package org.interreg.docexplore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class ConfigPanel extends JPanel
{
	JTabbedPane tabs;
	GeneralConfigPanel general;
	ServerConfigPanel server;
	public boolean wasValidated = false;
	
	public ConfigPanel(final File config, File serverDir) throws Exception
	{
		super(new BorderLayout());
		
		this.tabs = new JTabbedPane();
		
		int pw = 600, ph = 650;
		
		this.general = new GeneralConfigPanel(config);
		JScrollPane generalPane = new JScrollPane(general, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		generalPane.setPreferredSize(new Dimension(pw, ph));
		generalPane.getVerticalScrollBar().setUnitIncrement(15);
		tabs.addTab(XMLResourceBundle.getBundledString("cfgGeneralLabel"), generalPane);
		this.server = new ServerConfigPanel(config, serverDir);
		JScrollPane serverPane = new JScrollPane(server, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		serverPane.setPreferredSize(new Dimension(pw, ph));
		serverPane.getVerticalScrollBar().setUnitIncrement(15);
		tabs.addTab(XMLResourceBundle.getBundledString("cfgServerLabel"), serverPane);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("cfgOkLabel")) {public void actionPerformed(ActionEvent e)
		{
			wasValidated = true;
			try
			{
				general.write(config, server);
				server.write();
			}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			getTopLevelAncestor().setVisible(false);
		}}));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("cfgCancelLabel")) {
			public void actionPerformed(ActionEvent e) {getTopLevelAncestor().setVisible(false);}}));
		
		add(tabs, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public static boolean showConfigDialog() throws Exception
	{
		JDialog win = new JDialog((Frame)null, "Configuration", true);
		ConfigPanel configPanel = new ConfigPanel(new File(DocExploreTool.getHomeDir(), "config.xml"), new File(DocExploreTool.getHomeDir(), "reader"));
		win.add(configPanel);
		win.pack();
		win.setResizable(false);
		GuiUtils.centerOnScreen(win);
		win.setVisible(true);
		return configPanel.wasValidated;
	}
}
