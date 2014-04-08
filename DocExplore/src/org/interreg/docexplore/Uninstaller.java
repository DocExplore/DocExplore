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

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

public class Uninstaller
{
	static int getPID()
	{
		String name = ManagementFactory.getRuntimeMXBean().getName();
		boolean inpid = false;
		String pid = "";
		for (int i=0;i<name.length();i++)
		{
			char c = name.charAt(i);
			if (Character.isDigit(c))
			{
				inpid = true;
				pid += c;
			}
			else if (inpid)
				break;
		}
		if (pid.length() == 0)
			return -1;
		return Integer.parseInt(pid);
	}
	
	@SuppressWarnings("serial")
	static List<File> getUninstallRoots()
	{
		final JDialog dialog = new JDialog((Frame)null, "Uninstall DocExplore", true);
		final List<File> res = new Vector<File>();
		final boolean [] ok = {false};
		final List<String> homes = DocExploreTool.getHomes();
		
		dialog.getContentPane().setLayout(new LooseGridLayout(0, 1, 10, 10, false, false, SwingConstants.CENTER, SwingConstants.CENTER, false, false));
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		messagePanel.add(new JLabel(ImageUtils.getIcon("trash-64x64.png")));
		messagePanel.add(new JLabel("<html><b>You are about to remove DocExplore from your computer.</b><br/><br/>"
			+"However you may choose to keep your home directories which can still be used if you ever install DocExplore again.<br/>"
			+"Please select the home directories you would like to uninstall:</html>"));
		dialog.getContentPane().add(messagePanel);
		
		JPanel boxPanel = new JPanel(new LooseGridLayout(0, 1, 10, 10, false, false, SwingConstants.LEFT, SwingConstants.TOP, false, false));
		final JCheckBox [] boxes = new JCheckBox [homes.size()];
		for (int i=0;i<homes.size();i++)
		{
			JCheckBox box = new JCheckBox("<html><b>Home directory</b> "+homes.get(i)+"</html>");
			boxes[i] = box;
			boxPanel.add(box);
		}
		dialog.getContentPane().add(boxPanel);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction("OK") {public void actionPerformed(ActionEvent arg0)
		{
			ok[0] = true;
			for (int i=0;i<boxes.length;i++)
				if (boxes[i].isSelected())
					res.add(new File(homes.get(i)));
			dialog.setVisible(false);
		}}));
		buttonPanel.add(new JButton(new AbstractAction("Cancel") {public void actionPerformed(ActionEvent arg0)
		{
			dialog.setVisible(false);
		}}));
		dialog.getContentPane().add(buttonPanel);
		
		dialog.pack();
		GuiUtils.centerOnScreen(dialog);
		dialog.setVisible(true);
		
		if (!ok[0])
			return null;
		res.add(DocExploreTool.getExecutableDir());
		res.add(DocExploreTool.getPluginDir());
		res.add(new File(System.getProperty("user.home")+"/.docexplore"));
		return res;
	}
	
	public static void main(String [] args)
	{
		List<File> files = Uninstaller.getUninstallRoots();
		if (files != null)
		{
			int pid = getPID();
			System.out.println(":loop\ntasklist | find /i \" "+pid+" \" /c");
			for (File file : files)
				System.out.println(file.getAbsolutePath());
		}
		System.exit(0);
	}
}
