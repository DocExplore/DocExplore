package org.interreg.docexplore.stitcher;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.util.GuiUtils;

public class Stitcher extends DocExploreTool
{
	public static final double surfFeatureThreshold = .0014f;
	public static final double surfMatchThreshold = .2;
	public static final double surfScaleAndOrientationWeight = .2;
	
	public static final double groupSpreadRay = 22;
	public static final int groupSizeThreshold = 5;
	public static final double groupEarlyConfidenceThreshold = .1;
	public static final double groupLateConfidenceThreshold = .35;
	public static final double groupDivergenceRatioThreshold = .11;
	
	public static final double groupMatchingWeight = 1;
	public static final double groupEdgenessWeight = .2;
	public static final double groupAreaWeight = .5;
	public static final double groupDeviationWeight = 0;
	
	Startup startup;
	JFrame win, editorWin;
	StitcherMenu menu;
	FragmentView view;
	StitchEditor editor;
	boolean modified = false;
	
	FragmentSet fragmentSet;
	
	public Stitcher(Startup startup) throws Exception
	{
		this.startup = startup;
		this.fragmentSet = new FragmentSet();
		
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
		editorWin.setJMenuBar(new StitchEditorMenu(editor));
		editorWin.pack();
		
//		menu.load(new File("C:\\Users\\aburn\\Documents\\pres\\Msu_18bis-140\\msu18b.stch"), true);
//		editStitches(view.fragments.get(0), view.fragments.get(1));
	}
	
	int serialVersion = 0;
	public void write(ObjectOutputStream out) throws Exception
	{
		out.writeInt(serialVersion);
		view.write(out);
		fragmentSet.write(out);
	}
	
	public void read(ObjectInputStream in) throws Exception
	{
		clear();
		@SuppressWarnings("unused")
		int serialVersion = in.readInt();
		view.read(in);
		fragmentSet = new FragmentSet(in);
		view.repaint();
	}
	
	public void remove(Fragment f)
	{
		fragmentSet.remove(f);
		view.repaint();
	}
	public void remove(FragmentAssociation fa)
	{
		fragmentSet.remove(fa);
		view.repaint();
	}
	
	public void importFragments(final File [] files)
	{
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			float progress = 0;
			@Override public void run()
			{
				double x0 = 0;
				for (int i=0;i<files.length;i++) try
				{
					progress = i*1f/files.length;
					Fragment f = fragmentSet.add(files[i]);
					f.setPos(x0, f.uiy);
					while (view.boundsIntersect(f))
						f.setPos(x0 = f.uix+1.5, f.uiy);
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
			@Override public float getProgress() {return progress;}
		}, win);
		view.fitView(.1);
	}
	
	void clear()
	{
		fragmentSet = new FragmentSet();
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
					FragmentAssociation map = fragmentSet.get(f1, f2);
					if (map == null)
						map = fragmentSet.add(f1, f2);
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
