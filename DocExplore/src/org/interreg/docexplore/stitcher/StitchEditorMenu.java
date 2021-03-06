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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.interreg.docexplore.internationalization.Lang;

@SuppressWarnings("serial")
public class StitchEditorMenu extends JMenuBar
{
	StitchEditor editor;
	
	public StitchEditorMenu(final StitchEditor editor)
	{
		this.editor = editor;
		
		JMenu file = new JMenu(Lang.s("generalMenuFile"));
		add(file);
//		file.add(new JMenuItem(new AbstractAction("Delete and close") {@Override public void actionPerformed(ActionEvent e)
//		{
//			editor.view.set.remove(editor.map);
//			editor.getTopLevelAncestor().setVisible(false);
//		}}));
		file.add(new JMenuItem(new AbstractAction("Close") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.getTopLevelAncestor().setVisible(false);
		}}));
		JMenu view = new JMenu(Lang.s("generalMenuView"));
		add(view);
		view.add(new JMenuItem(new AbstractAction("Flip") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.flip();
		}}));
		view.add(new JMenuItem(new AbstractAction("Reverse") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.reverse();
		}}));
//		view.add(new JMenuItem(new AbstractAction("Toggle associations") {@Override public void actionPerformed(ActionEvent e)
//		{
//			editor.showAssociations = !editor.showAssociations;
//			editor.repaint();
//		}}));
//		view.add(new JMenuItem(new AbstractAction("Toggle distortions") {@Override public void actionPerformed(ActionEvent e)
//		{
//			editor.showAlpha = !editor.showAlpha;
//			editor.repaint();
//		}}));
		JMenu tools = new JMenu(Lang.s("generalMenuTools"));
		add(tools);
		tools.add(new JMenuItem(new AbstractAction("Refresh features") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.toolkit.refreshFeatures();
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));}});
		tools.add(new JMenuItem(new AbstractAction("Match features") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.toolkit.matchFeatures();
			//editor.toolkit.clean();
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));}});
		tools.add(new JMenuItem(new AbstractAction("Detect group") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.toolkit.detectGroup(true, false);
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));}});
		tools.add(new JMenuItem(new AbstractAction("Detect group (UI geometry)") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.toolkit.detectGroup(true, true);
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));}});
		tools.add(new JMenuItem(new AbstractAction("Associate") {@Override public void actionPerformed(ActionEvent e)
		{
			if (editor.left.selected != null && editor.right.selected != null)
			{
				Association a = null;
				List<Association> list = editor.map.associationsByPOI.get(editor.left.selected);
				if (list != null)
					for (int i=0;i<list.size();i++)
						if (list.get(i).other(editor.left.selected) == editor.right.selected)
							{a = list.get(i); break;}
				if (a != null)
					editor.map.remove(a);
				else
				{
					a = editor.map.add(editor.left.selected, editor.right.selected);
					//System.out.printf("Feature dist: %.3f, desc: %.3f\n", a.p1.featureDistance2(a.p2), a.p1.descriptorDistance2(a.p2));
				}
				editor.left.repaint();
				editor.right.repaint();
			}
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));}});
	}
}
