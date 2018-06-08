package org.interreg.docexplore.manuscript;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;
import org.interreg.docexplore.manuscript.actions.CompoundAction;
import org.interreg.docexplore.manuscript.actions.DeleteMetaDataAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.manuscript.app.ToolbarButton;
import org.interreg.docexplore.manuscript.app.ToolbarButton.ToolbarButtonListener;
import org.interreg.docexplore.manuscript.app.editors.ManuscriptEditor;
import org.interreg.docexplore.stitcher.Association;
import org.interreg.docexplore.stitcher.FeatureDetector;
import org.interreg.docexplore.stitcher.Fragment;
import org.interreg.docexplore.stitcher.FragmentAssociation;
import org.interreg.docexplore.stitcher.FragmentAssociationUtils;
import org.interreg.docexplore.stitcher.FragmentSet;
import org.interreg.docexplore.stitcher.FragmentView;
import org.interreg.docexplore.stitcher.GroupDetector;
import org.interreg.docexplore.stitcher.RenderEditor;
import org.interreg.docexplore.stitcher.StitchEditor;
import org.interreg.docexplore.stitcher.Stitcher;
import org.interreg.docexplore.stitcher.StitcherToolkit;
import org.interreg.docexplore.util.GuiUtils;

public class StitcherUtils
{
	public static void saveStitches(Component parent, FragmentView view, Book book, DocExploreDataLink link)
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				ObjectOutputStream out = null;
				try
				{
					MetaData wip = null;
					List<MetaData> mds = book.getMetaDataListForKey(link.stitchKey);
					if (!mds.isEmpty())
						wip = mds.get(0);
					else book.addMetaData(wip = new MetaData(link, link.stitchKey, ""));
					ByteArrayOutputStream array = new ByteArrayOutputStream();
					out = new ObjectOutputStream(array);
					Stitcher.write(out, view, view.set);
					ByteArrayInputStream in = new ByteArrayInputStream(array.toByteArray());
					wip.setValue(MetaData.textType, in);
					in.close();
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				if (out != null) try {out.close();} catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		}, parent);
	}
	
	public static void loadStitches(Component parent, FragmentView view, Book book, DocExploreDataLink link)
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				try
				{
					List<MetaData> mds = book.getMetaDataListForKey(link.stitchKey);
					if (mds.isEmpty())
						return;
					ObjectInputStream in = new ObjectInputStream(mds.get(0).getValue());
						Stitcher.read(in, view, link);
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				view.fitView(.3);
			}
		}, parent);
	}
	
	public static void putInStitches(ManuscriptAppHost host, Book poster)
	{
		try
		{
			FragmentSet set = new FragmentSet();
			StitcherToolkit.stitchPoster(poster, host.getLink(), set, FeatureDetector.Surf, true, host.frame);
			
			MetaData wip = null;
			List<MetaData> mds = poster.getMetaDataListForKey(host.getLink().stitchKey);
			if (!mds.isEmpty())
				return;
			wip = new MetaData(host.getLink(), host.getLink().stitchKey, "");
			ByteArrayOutputStream array = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(array);
			Stitcher.write(out, null, set);
			out.close();
			ByteArrayInputStream in = new ByteArrayInputStream(array.toByteArray());
			wip.setValue(MetaData.textType, in);
			in.close();
			
			AddMetaDataAction action = host.getLink().actionProvider().addMetaData(poster, wip);
			host.historyManager.submit(new WrappedAction(action)
			{
				@Override public String description() {return Lang.s("imageToolbarStitch");}
				@Override public void doAction() throws Exception
				{
					super.doAction();
					host.removeDocument(poster);
					host.addDocument(poster);
				}
				@Override public void undoAction() throws Exception
				{
					super.undoAction();
					host.removeDocument(poster);
					host.addDocument(poster);
				}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public static void removeStitches(ManuscriptAppHost host, Book poster)
	{
		try
		{
			List<MetaData> mds = poster.getMetaDataListForKey(host.getLink().stitchKey);
			DeleteMetaDataAction action = host.getLink().actionProvider().deleteMetaDatas(poster, mds);
			host.historyManager.submit(new WrappedAction(action)
			{
				@Override public String description() {return Lang.s("imageToolbarUnstitch");}
				@Override public void doAction() throws Exception
				{
					super.doAction();
					host.removeDocument(poster);
					host.addDocument(poster);
				}
				@Override public void undoAction() throws Exception
				{
					super.undoAction();
					host.removeDocument(poster);
					host.addDocument(poster);
				}
			});
			
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public static void renderStitches(ManuscriptAppHost host, Book poster, FragmentView view)
	{  
		try
		{
			//saveStitches(host.frame, view, poster, host.getLink());
			poster.setMetaDataString(host.getLink().stitchRenderKey, "true");
			host.removeDocument(poster);
			host.addDocument(poster, view);
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public static void editStitches(ManuscriptAppHost host, Book poster, FragmentView view, Fragment left, Fragment right)
	{  
		try
		{
			//saveStitches(host.frame, view, poster, host.getLink());
			poster.setMetaDataString(host.getLink().stitchEditKey, left.index()+","+right.index());
			host.removeDocument(poster);
			host.addDocument(poster, view);
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public static void cancelRender(ManuscriptAppHost host, Book poster, FragmentView view)
	{
		try
		{
			poster.setMetaDataString(host.getLink().stitchRenderKey, "false");
			host.removeDocument(poster);
			host.addDocument(poster, view);
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public static void stopEdit(ManuscriptAppHost host, Book poster, FragmentView view)
	{
		try
		{
			poster.setMetaDataString(host.getLink().stitchEditKey, "");
			host.removeDocument(poster);
			host.addDocument(poster, view);
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public static void integrateFragmentView(ManuscriptEditor editor, FragmentView view)
	{
		ToolbarButtonListener listener = new ToolbarButtonListener() {@Override public void onToolbarButton(ToolbarButton button)
		{
			try {editor.onActionRequest(button.action);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		}};
		
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "save-stitches", "save-stitches-24x24.png", Lang.s("imageToolbarSaveStitches")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "clear-stitches", "clear-stitches-24x24.png", Lang.s("imageToolbarClearStitches")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "detect-stitches", "detect-stitches-24x24.png", Lang.s("imageToolbarDetectStitches")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "render-stitch", "render-stitch-24x24.png", Lang.s("imageToolbarRenderStitches")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "unstitch", "previous-24x24.png", Lang.s("imageToolbarUnstitch")));
	}
	
	public static RenderEditor buildRenderEditor(ManuscriptEditor editor, FragmentView view)
	{
		RenderEditor renderer = new RenderEditor(editor, view);
		ToolbarButtonListener listener = new ToolbarButtonListener() {@Override public void onToolbarButton(ToolbarButton button)
		{
			try {editor.onActionRequest(button.action);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		}};

		editor.topPanel.rightPanel.add(renderer.tools);
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "fit-bounds", "bounds-24x24.png", Lang.s("stitcherFitBounds")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "render", "render-stitch-24x24.png", Lang.s("stitcherRender")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "cancel", "previous-24x24.png", Lang.s("generalCancelLabel")));
		return renderer;
	}
	public static StitchEditor buildStitchEditor(ManuscriptEditor editor, FragmentView view)
	{
		StitchEditor stitcher = new StitchEditor(editor, view);
		ToolbarButtonListener listener = new ToolbarButtonListener() {@Override public void onToolbarButton(ToolbarButton button)
		{
			try {editor.onActionRequest(button.action);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		}};
		try
		{
			String [] s = editor.book.getMetaDataString(editor.host.getAppHost().getLink().stitchEditKey).split(",");
			stitcher.setMap(view.set.get(view.set.fragments.get(Integer.parseInt(s[0])), view.set.fragments.get(Integer.parseInt(s[1]))));
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}

		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "flip", "stitch-flip-24x24.png", Lang.s("stitcherFlipView")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "reverse", "stitch-reverse-24x24.png", Lang.s("stitcherReverseView")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "refresh-poi", "stitch-poi-24x24.png", Lang.s("stitcherRefreshPois")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "clear-poi", "erase-pois-24x24.png", Lang.s("stitcherClearPois")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "toggle-link", "toggle-stitch-24x24.png", Lang.s("stitcherToggleLink")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "detect", "detect-stitches-24x24.png", Lang.s("imageToolbarDetectStitches")));
		editor.topPanel.rightPanel.add(new ToolbarButton(listener, "back", "previous-24x24.png", Lang.s("generalCancelLabel")));
		return stitcher;
	}
	
	public static void updatePoster(ManuscriptAppHost host, Book poster, List<MetaData> parts)
	{
		try
		{
			DocExploreDataLink link = host.getLink();
			List<MetaData> oldParts = poster.getMetaDataListForKey(link.partKey);
			poster.setMetaDataString(host.getLink().stitchRenderKey, "false");
			host.historyManager.submit(new CompoundAction(
				host.getLink().actionProvider().deleteMetaDatas(poster, oldParts),
				host.getLink().actionProvider().addMetaDatas(poster, parts))
			{
				@Override public String description() {return Lang.s("imageToolbarRenderStitches");}
				@Override public void doAction() throws Exception
				{
					super.doAction();
					poster.setMetaDataString(link.upToDateKey, "false");
					DocumentEvents.broadcastChanged(host, poster);
				}
				@Override public void undoAction() throws Exception
				{
					super.undoAction();
					poster.setMetaDataString(link.upToDateKey, "false");
					DocumentEvents.broadcastChanged(host, poster);
				}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public static void detectLayout(FragmentView view)
	{
		boolean hasEmptyAssociations = false;
		for (int i=0;i<view.set.associations.size();i++)
			if (view.set.associations.get(i).associations.isEmpty())
				{hasEmptyAssociations = true; break;}
		
		if (hasEmptyAssociations && 
			JOptionPane.showConfirmDialog(view, Lang.s("useLayoutMessage"), Lang.s("useLayoutLabel"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
		{
			GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
			{
				float [] progress = {0};
				@Override public void run()
				{
					try
					{
						for (int i=0;i<view.set.associations.size();i++)
						{
							FragmentAssociation map = view.set.associations.get(i);
							if (!map.associations.isEmpty())
								continue;
							progress[0] = i*1f/view.set.associations.size();
							map.refreshFeatures();
							FragmentAssociationUtils.match(map, progress, progress[0], (i+1)*1f/view.set.associations.size());
							List<Association> res = new ArrayList<Association>();
							new GroupDetector().detect(map, res, true);
							map.associations = res;
							map.resetAssociationsByPOI();
						}
					}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					view.repaint();
				}
				@Override public float getProgress() {return progress[0];}
			}, view);
		}
		else StitcherToolkit.detectLayout(view);
	}
}
