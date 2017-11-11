package org.interreg.docexplore.stitcher;

import java.util.ArrayList;
import java.util.List;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.util.GuiUtils;

public class StitchEditorToolkit
{
	StitchEditor editor;
	
	public StitchEditorToolkit(StitchEditor editor)
	{
		this.editor = editor;
	}
	
	public void refreshFeatures()
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			@Override public void run()
			{
				try {editor.map.refreshFeatures();}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				editor.left.repaint();
				editor.right.repaint();
			}
		}, editor);
		editor.repaint();
	}
	
	public void matchFeatures()
	{
		if (editor.map == null)
			return;
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			float [] progress = {0};
			@Override public void run()
			{
				try {FragmentAssociationUtils.match(editor.map, progress);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
			@Override public float getProgress() {return progress[0];}
		}, editor);
		editor.repaint();
	}
	
	public void clean()
	{
		if (editor.map == null)
			return;
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			float [] progress = {0};
			@Override public void run()
			{
				try {editor.map.removeUnusedDescriptors(progress);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
			@Override public float getProgress() {return progress[0];}
		}, editor);
		editor.repaint();
	}
	
	public void detectGroup()
	{
		if (editor.map == null)
			return;
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			float [] progress = {0};
			@Override public void run()
			{
				try
				{
//					editor.map.refreshFeatures();
//					FragmentAssociationUtils.match(editor.map, progress, 0f, .9f);
					
					List<Association> res = new ArrayList<Association>();
					new GroupDetector().detect(editor.map, res);
					editor.map.associations = res;
					editor.map.resetAssociationsByPOI();
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
			@Override public float getProgress() {return progress[0];}
		}, editor);
		editor.left.repaint();
		editor.right.repaint();
		editor.repaint();
		editor.stitcher.view.repaint();
	}
}
