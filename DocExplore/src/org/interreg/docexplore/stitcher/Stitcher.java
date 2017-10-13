package org.interreg.docexplore.stitcher;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.util.GuiUtils;

public class Stitcher extends DocExploreTool
{
	Startup startup;
	JFrame win, editorWin;
	StitcherMenu menu;
	FragmentView view;
	StitchEditor editor;
	boolean modified = false;
	
	public Stitcher(Startup startup) throws Exception
	{
		this.startup = startup;
		this.win = new JFrame("Stitcher");
		this.view = new FragmentView(this);
		
		win.setJMenuBar(this.menu = new StitcherMenu(this));
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.add(view);
		win.pack();
		win.setVisible(true);
		
		this.editorWin = new JFrame("Editor");
		editorWin.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.editor = new StitchEditor(this);
		editorWin.add(editor);
		editorWin.setJMenuBar(editor.buildMenu());
		editorWin.pack();
		
//		menu.load(new File("C:\\Users\\aburn\\Documents\\pres\\Msu_18bis-140\\msu18b.stch"), true);
//		editStitches(view.fragments.get(0), view.fragments.get(1));
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
		view.resetView();
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
	
	public void editStitches(final Fragment f1, final Fragment f2)
	{
		editorWin.setVisible(true);
		SwingUtilities.invokeLater(new Runnable() {@Override public void run()
		{
			GuiUtils.blockUntilComplete(new Runnable()
			{
				@Override public void run()
				{
					FragmentAssociation map = null;
					for (int i=0;i<view.associations.size();i++)
					{
						FragmentAssociation fa = view.associations.get(i);
						if (fa.d1.fragment == f1 && fa.d2.fragment == f2 || fa.d1.fragment == f2 && fa.d2.fragment == f1)
							{map = fa; break;}
					}
					if (map == null)
					{
						map = new FragmentAssociation(f1, f2);
						view.associations.add(map);
					}
					editor.setMap(map);
				}
			}, editorWin);
			editorWin.toFront();
			editorWin.repaint();
		}});
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
