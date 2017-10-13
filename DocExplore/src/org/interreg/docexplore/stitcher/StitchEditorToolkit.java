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
	
	public void computeFeatures()
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			@Override public void run()
			{
				try {editor.map.computeSurf();}
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
				try {editor.map.match(progress);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
			@Override public float getProgress() {return progress[0];}
		}, editor);
		editor.repaint();
	}
	
	public void filterMatches()
	{
		if (editor.map == null)
			return;
		editor.map.filterByDescriptor();
		clean();
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
	
	public void tighten()
	{
		if (editor.map == null)
			return;
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			float [] progress = {0};
			@Override public void run()
			{
				Tightener.tighten(editor.map, progress);
			}
			@Override public float getProgress() {return progress[0];}
		}, editor);
		editor.repaint();
	}
	
	public void coarseMatch()
	{
		if (editor.map == null)
			return;
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			float [] progress = {0}, sub = {0};
			@Override public void run()
			{
				try
				{
					editor.map.computeSurf();
					progress[0] = .33f;
					editor.map.match(sub);
					double err = Double.MAX_VALUE, me = Double.MAX_VALUE, sd = Double.MAX_VALUE;
					while (err > 10)
					{
						editor.map.filterByDescriptor();
						//editor.map.removeUnusedDescriptors(sub);
						Tightener.tighten(editor.map, progress, progress[0], progress[0]+.5f*(1-progress[0]));
						me = editor.map.meanUIDistance();
						sd = Math.sqrt(editor.map.stdUIDistanceDev(me));
						err = me*editor.map.d1.fragment.imagew/editor.map.d1.fragment.uiw;
						System.out.println(me+" "+err+" "+me/sd+" "+editor.map.associations.size());
					}
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
	
	public void groupMatch()
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
					editor.map.computeSurf();
					progress[0] = .33f;
					editor.map.match(progress, .33f, .67f);
					
					List<Association> res = new ArrayList<Association>();
					GroupDetector.detect(editor.map, res);
					editor.map.associations = res;
					editor.map.resetAssociationsByPOI();
					
					Tightener.tighten(editor.map, progress, .67f, 1);
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
