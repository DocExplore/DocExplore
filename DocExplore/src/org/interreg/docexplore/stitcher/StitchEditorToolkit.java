/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
	
	public void clearFeatures()
	{
		editor.map.clearDescriptors();
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
				try {FragmentAssociationUtils.match(editor.map, progress, false);}
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
	
	public void detectGroup(boolean force, boolean useUiGeometry)
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
					editor.map.refreshFeatures();
					FragmentAssociationUtils.match(editor.map, progress, 0f, .9f, !useUiGeometry);
					
					List<Association> res = new ArrayList<Association>();
					new GroupDetector().detect(editor.map, res, force, useUiGeometry);
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
		editor.view.repaint();
	}
}
