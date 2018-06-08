package org.interreg.docexplore.stitcher;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.util.GuiUtils;

public class Stitcher extends DocExploreTool implements FragmentView.Listener, RenderEditor.Listener, StitchEditor.Listener
{
	public static final double surfFeatureThreshold = .015f;
	public static final int surfMaxFeaturesThreshold = 1500;
	public static final double surfMatchThreshold = .4;
	public static final double surfScaleAndOrientationWeight = .5;
	
	public static final double lbpMatchThreshold = 750;
	
	public static final double groupSpreadRay = 22;
	public static final int groupEarlySizeThreshold = 5;
	public static final int groupLateSizeThreshold = 8;
	public static final double groupConfidenceThreshold = .5;
	public static final double groupDivergenceRatioThreshold = .11;
	
	Startup startup;
	ManuscriptAppHost host;
	Book poster;
	
	JFrame win, stitchEditorWin, renderEditorWin;
	StitcherMenu menu;
	FragmentView view;
	StitcherToolkit toolkit;
	StitchEditor stitchEditor;
	RenderEditor renderEditor;
	boolean showDetectedGroups = false;
	FeatureDetector detector;
	
	FragmentSet fragmentSet;
	
	public Stitcher(Startup startup, ManuscriptAppHost host, Book poster) throws Exception
	{
		this.startup = startup;
		this.host = host;
		this.poster = poster;
		this.fragmentSet = new FragmentSet();
		
		this.toolkit = new StitcherToolkit(this);
		this.win = new JFrame("Stitcher");
		this.view = new FragmentView(this);
		view.setFragmentSet(fragmentSet);
		
		win.setJMenuBar(this.menu = new StitcherMenu(this));
		win.add(view);
		win.pack();
		win.setVisible(true);
		
		this.stitchEditorWin = new JFrame("Stitch Editor");
		stitchEditorWin.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.stitchEditor = new StitchEditor(this, view);
		stitchEditorWin.add(stitchEditor);
		stitchEditorWin.setJMenuBar(new StitchEditorMenu(stitchEditor));
		stitchEditorWin.pack();
		
		this.renderEditorWin = new JFrame("Render Editor");
		renderEditorWin.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.renderEditor = new RenderEditor(this, view);
		renderEditorWin.add(renderEditor);
		renderEditorWin.setJMenuBar(new RenderEditorMenu(renderEditor));
		renderEditorWin.pack();
	}
	
	static int serialVersion = 1;
	public static void write(ObjectOutputStream out, FragmentView view, FragmentSet set) throws Exception
	{
		out.writeInt(serialVersion);
		if (view == null)
			out.writeBoolean(false);
		else
		{
			out.writeBoolean(true);
			view.write(out);
		}
		set.write(out);
	}
	public void write(ObjectOutputStream out) throws Exception
	{
		write(out, view, fragmentSet);
	}
	
	public static FragmentSet read(ObjectInputStream in, FragmentView view, DocExploreDataLink link) throws Exception
	{
		int serialVersion = in.readInt();
		if (serialVersion == 0 || in.readBoolean())
			view.read(in);
		FragmentSet fragmentSet = new FragmentSet(in, link);
		view.setFragmentSet(fragmentSet);
		return fragmentSet;
	}
	public void read(ObjectInputStream in) throws Exception
	{
		clear();
		fragmentSet = read(in, view, host.getLink());
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
	
	void clear()
	{
		fragmentSet = new FragmentSet();
		view.setFragmentSet(fragmentSet);
		view.modified = true;
	}
	
	void quit() {quit(true);}
	void quit(boolean force)
	{
		if (!menu.requestSave())
			return;
		startup.shutdown();
		win.setVisible(false);
		
		//DocExplore.main(new String [0]);
		if (force)
			System.exit(0);
	}
	
	@Override public void onEditStitchesRequest(final Fragment f1, final Fragment f2)
	{
		stitchEditorWin.setVisible(true);
		SwingUtilities.invokeLater(new Runnable() {@Override public void run()
		{
			GuiUtils.blockUntilComplete(new Runnable()
			{
				@Override public void run()
				{
					FragmentAssociation map = fragmentSet.get(f1, f2);
					if (map == null)
						map = fragmentSet.add(f1, f2);
					stitchEditor.setMap(map);
				}
			}, stitchEditorWin);
			stitchEditorWin.toFront();
			stitchEditorWin.repaint();
		}});
	}
	@Override public void onRenderRequest()
	{
		renderEditor.init();
		renderEditorWin.setVisible(true);
	}
	@Override public void onRenderEnded(List<MetaData> parts) {}
	@Override public void onSaveRequest(boolean force) {}
	@Override public void onCancelRequest() {}
	@Override public void onDetectLayoutRequest() {}
	
	@Override public boolean allowFileExports() {return true;}
	@Override public File getCurFile() {return menu.curFile;}
	@Override public DocExploreDataLink getLink() {return null;}
	
//	public static void stitchPoster(ManuscriptAppHost host, Book poster) throws Exception
//	{
//		Startup startup = new Startup("Stitcher", "logo.png", true, false, true, false);
//		Stitcher stitcher = new Stitcher(startup, host, poster);
//		stitcher.win.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		stitcher.win.setVisible(true);
//		if (startup.winSize == null)
//			stitcher.win.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		else
//		{
//			stitcher.win.setSize(startup.winSize[0], startup.winSize[1]);
//			GuiUtils.centerOnScreen(stitcher.win);
//		}
//		startup.startupComplete();
//		stitcher.win.toFront();
//		StitcherToolkit.importPoster(poster, host.getLink(), stitcher.view, stitcher.fragmentSet, stitcher.detector, true, stitcher.win);
//		stitcher.view.fitView(.1);
//	}
	
	public static void main(String [] args) throws Exception
	{
		Startup startup = new Startup("Stitcher", "logo.png", true, false, true, false);
		Stitcher stitcher = new Stitcher(startup, null, null);
		stitcher.win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
