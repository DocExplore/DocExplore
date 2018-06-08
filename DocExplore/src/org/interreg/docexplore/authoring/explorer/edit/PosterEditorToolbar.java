/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.FlowLayout;

import javax.swing.JPanel;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.app.ToolbarButton;

@SuppressWarnings("serial")
public class PosterEditorToolbar extends JPanel
{
	public PosterEditorToolbar(final PosterEditor editor)
	{
		super(new WrapLayout(FlowLayout.LEFT));
		
		editor.addMainWindowListener((ToolbarButton)add(new ToolbarButton("fill-poster-24x24.png", Lang.s("imageToolbarFillPoster"))
		{
			public void clicked()
			{
				try {editor.listener.onFillPosterHolesRequest(editor.view.curBook);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		}));
		editor.addMainWindowListener((ToolbarButton)add(new ToolbarButton("mirror-hor-24x24.png", Lang.s("imageToolbarMirrorHor"))
		{
			public void clicked()
			{
				try {editor.listener.onHorizontalMirrorPartsRequest(editor.view.curBook);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		}));
		editor.addMainWindowListener((ToolbarButton)add(new ToolbarButton("mirror-ver-24x24.png", Lang.s("imageToolbarMirrorVer"))
		{
			public void clicked()
			{
				try {editor.listener.onVerticalMirrorPartsRequest(editor.view.curBook);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		}));
		editor.addMainWindowListener((ToolbarButton)add(new ToolbarButton("rotate-left-24x24.png", Lang.s("imageToolbarRotateLeft"))
		{
			public void clicked()
			{
				try {editor.listener.onRotatePartsLeftRequest(editor.view.curBook);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		}));
		editor.addMainWindowListener((ToolbarButton)add(new ToolbarButton("rotate-right-24x24.png", Lang.s("imageToolbarRotateRight"))
		{
			public void clicked()
			{
				try {editor.listener.onRotatePartsRightRequest(editor.view.curBook);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		}));
	}
}
