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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.interreg.docexplore.internationalization.Lang;

@SuppressWarnings("serial")
public class RenderEditorMenu extends JMenuBar
{
	RenderEditor editor;
	
	public RenderEditorMenu(RenderEditor editor)
	{
		this.editor = editor;
		
		JMenu file = new JMenu(Lang.s("generalMenuFile"));
		add(file);
		file.add(new JMenuItem(new AbstractAction(Lang.s("dialogCloseLabel")) {@Override public void actionPerformed(ActionEvent e)
		{
			editor.getTopLevelAncestor().setVisible(false);
		}}));
		
		JMenu view = new JMenu(Lang.s("generalMenuView"));
		add(view);
		view.add(new JMenuItem(new AbstractAction(Lang.s("stitcherFitView")) {@Override public void actionPerformed(ActionEvent e)
		{
			editor.view.fitView(.1);
		}}));
		
		JMenu tools = new JMenu(Lang.s("generalMenuTools"));
		add(tools);
		tools.add(new JMenuItem(new AbstractAction(Lang.s("stitcherFitBounds")) {@Override public void actionPerformed(ActionEvent e)
		{
			editor.view.fitBounds();
			editor.updateFields();
		}}));
		tools.add(new JMenuItem(new AbstractAction(Lang.s("stitcherRender")) {@Override public void actionPerformed(ActionEvent e)
		{
			editor.render();
		}}));
	}
}
