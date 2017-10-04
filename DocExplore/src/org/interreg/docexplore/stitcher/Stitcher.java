package org.interreg.docexplore.stitcher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.util.GuiUtils;

public class Stitcher extends DocExploreTool
{
	Startup startup;
	JFrame win;
	StitcherMenu menu;
	FragmentView view;
	boolean modified = false;
	
	public Stitcher(Startup startup) throws Exception
	{
		this.startup = startup;
		this.win = new JFrame("Stitcher");
		this.view = new FragmentView();
		
		win.setJMenuBar(this.menu = new StitcherMenu(this));
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.add(view);
		win.pack();
		win.setVisible(true);
	}
	
	public void write(ObjectOutputStream out) throws Exception
	{
		view.write(out);
	}
	
	public void read(ObjectInputStream in) throws Exception
	{
		clear();
		view.read(in);
	}
	
	void clear()
	{
		view.clear();
		modified = true;
	}
	
	void quit()
	{
		if (!menu.requestSave())
			return;
		startup.shutdown();
		win.setVisible(false);
		
		//DocExplore.main(new String [0]);
		System.exit(0);
	}
	
	public static void main(String [] args) throws Exception
	{
		Startup startup = new Startup("Stitcher", "logo.png", true, false, true, false);
		
		Stitcher stitcher = new Stitcher(startup);
		
		if (startup.winSize == null)
			stitcher.win.setExtendedState(JFrame.MAXIMIZED_BOTH);
		else
		{
			stitcher.win.setSize(startup.winSize[0], startup.winSize[1]);
			GuiUtils.centerOnScreen(stitcher.win);
		}
		stitcher.win.setVisible(true);
		
		startup.startupComplete();
	}
}
