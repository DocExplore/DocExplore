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
