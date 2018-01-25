/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
import org.interreg.docexplore.internationalization.Lang;
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
		tabs.addTab(Lang.s("cfgGeneralLabel"), generalPane);
		this.server = new ServerConfigPanel(config, serverDir);
		JScrollPane serverPane = new JScrollPane(server, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		serverPane.setPreferredSize(new Dimension(pw, ph));
		serverPane.getVerticalScrollBar().setUnitIncrement(15);
		tabs.addTab(Lang.s("cfgServerLabel"), serverPane);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("cfgOkLabel")) {public void actionPerformed(ActionEvent e)
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
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("cfgCancelLabel")) {
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
