/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class ProcessDialog extends JDialog
{
	JScrollPane scroll;
	JTextArea console;
	
	public ProcessDialog(String name)
	{
		super((Frame)null, "", true);
		
		setUndecorated(true);
		setAlwaysOnTop(true);
		JPanel content = new JPanel(new BorderLayout());
		content.add(new JLabel("<html><b>"+name+"</b></html>"), BorderLayout.NORTH);
		this.console = new JTextArea(24, 80);
		console.setBackground(Color.black);
		console.setForeground(Color.white);
		this.scroll = new JScrollPane(console);
		content.add(scroll, BorderLayout.CENTER);
		content.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		
		setContentPane(content);
		pack();
	}
	
	public void monitor(final Process p)
	{
		console.setText("");
		
		new Thread() {public void run()
		{
			while (!isVisible())
				try {Thread.sleep(50);}
				catch (Exception e) {}
			try
			{
				String line = null;
				BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
		        while ((line = stdOut.readLine()) != null)
		        {
		        	console.append(line+"\n"); 
		        	scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
		        }
		        stdOut = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		        while ((line = stdOut.readLine()) != null)
		        {
		        	console.append(line+"\n"); 
		        	scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
		        }
			}
			catch (Exception e) {e.printStackTrace();}
			setVisible(false);
		}}.start();
		
		setVisible(true);
	}
}
