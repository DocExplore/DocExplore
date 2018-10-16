/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.SystemUtils;
import org.interreg.docexplore.DocExplore;
import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.gui.ErrorHandler;

public class ReaderElectronApp extends DocExploreTool
{
	public static void main(String [] args)
	{
		Startup startup = new Startup("Reader", null, false, false, true, false);
		int [] mode = selectMode(startup);
		if (mode == null)
			return;
		
		File readerDir = new File(DocExploreTool.getHomeDir(), "reader");
		File appDir = null;System.out.println(SystemUtils.IS_OS_WINDOWS);
		if (SystemUtils.IS_OS_WINDOWS)
		{
			appDir = new File(DocExploreTool.getExecutableDir(), "readerapp");
			if (!appDir.exists())
				appDir = new File(DocExplore.readerApp());
		}
		String app = SystemUtils.IS_OS_WINDOWS ? appDir.getAbsolutePath()+"/electron.exe" : 
			SystemUtils.IS_OS_MAC_OSX ? "/Applications/DocExplore.app/Contents/MacOS/Electron" : 
			"electron";
		
		try
		{
			Runtime.getRuntime().exec(
				mode.length == 0 ? new String [] {app, readerDir.getAbsolutePath(), "true"} : 
					new String [] {app, readerDir.getAbsolutePath(), ""+mode[0], ""+mode[1]},
				null,
				appDir);
		}
		catch (Exception e)
		{
			ErrorHandler.defaultHandler.submit(e);
		}
		System.exit(0);
	}
	
	static int [][] baseRes = {{800, 600}, {1024, 768}, {1280, 720}, {1280, 1024}, {1366, 768}, {1440, 900}, {1600, 900}}; 
	public static int [][] windowedModes()
	{
		List<int []> modes = new ArrayList<int []>();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		for (int k=1;k*baseRes[0][0] <= dim.width;k++)
		{
			for (int i=0;i<baseRes.length;i++)
				if (k*baseRes[i][0] <= dim.width && k*baseRes[i][1] <= dim.height)
					modes.add(new int [] {k*baseRes[i][0], k*baseRes[i][1]});
		}
		if (modes.get(modes.size()-1)[0] != dim.width || modes.get(modes.size()-1)[1] != dim.height)
			modes.add(new int [] {dim.width, dim.height});
		return modes.toArray(new int [modes.size()][]);
	}
	
	public static int [] selectMode(Startup startup)
	{
		if (startup.winSize != null)
			return new int [] {startup.winSize[0], startup.winSize[1]};
		if (startup.fullscreen)
			return new int [] {};
		
		final JDialog modeDialog = new JDialog((Frame)null, "Mode", true);
		JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		int [][] modes = windowedModes();
		String [] modeNames = new String [modes.length];
		for (int i=0;i<modes.length;i++)
			modeNames[i] = modes[i][0]+"x"+modes[i][1];
		@SuppressWarnings({ "rawtypes", "unchecked" })
		JList modeList = new JList(modeNames);
		modeList.setSelectedIndex(modeNames.length-1);
		JScrollPane scrollPane = new JScrollPane(modeList);
		mainPanel.add(scrollPane, BorderLayout.NORTH);
		JCheckBox fsBox = new JCheckBox("Fullscreen");
		fsBox.setSelected(true);
		mainPanel.add(fsBox, BorderLayout.CENTER);
		JButton okButton = new JButton("OK");
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(okButton, BorderLayout.SOUTH);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		final boolean [] ok = {false};
		okButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
			{ok[0] = true; modeDialog.setVisible(false);}});
		modeDialog.add(mainPanel);
		modeDialog.pack();
		scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	 
		GraphicsConfiguration gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gconf);
		modeDialog.setLocation((screenSize.width-(screenInsets.left+screenInsets.right)-modeDialog.getWidth())/2, 
			(screenSize.height-(screenInsets.bottom+screenInsets.top)-modeDialog.getHeight())/2);
		modeDialog.setVisible(true);
		if (!ok[0])
		{
			DocExplore.main(new String [0]);
			return null;
		}
		if (fsBox.isSelected())
			return new int [] {};
		return modes[modeList.getSelectedIndex()];
	}
}
