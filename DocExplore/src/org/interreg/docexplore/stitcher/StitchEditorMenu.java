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

@SuppressWarnings("serial")
class StitchEditorMenu extends JMenuBar
{
	StitchEditor editor;
	
	public StitchEditorMenu(final StitchEditor editor)
	{
		this.editor = editor;
		
		JMenu file = new JMenu("File");
		add(file);
		file.add(new JMenuItem(new AbstractAction("Delete and close") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.stitcher.remove(editor.map);
			editor.getTopLevelAncestor().setVisible(false);
		}}));
		file.add(new JMenuItem(new AbstractAction("Close") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.getTopLevelAncestor().setVisible(false);
		}}));
		JMenu view = new JMenu("View");
		add(view);
		view.add(new JMenuItem(new AbstractAction("Flip") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.flip();
		}}));
		view.add(new JMenuItem(new AbstractAction("Reverse") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.reverse();
		}}));
		view.add(new JMenuItem(new AbstractAction("Toggle associations") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.showAssociations = !editor.showAssociations;
			editor.repaint();
		}}));
		view.add(new JMenuItem(new AbstractAction("Toggle distortions") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.showAlpha = !editor.showAlpha;
			editor.repaint();
		}}));
		JMenu tools = new JMenu("Tools");
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
			editor.toolkit.detectGroup(false);
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));}});
		tools.add(new JMenuItem(new AbstractAction("Force detect group") {@Override public void actionPerformed(ActionEvent e)
		{
			editor.toolkit.detectGroup(true);
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
					System.out.printf("Feature dist: %.3f, desc: %.3f, scor: %.3f\n", a.p1.featureDistance2(a.p2), a.p1.descriptorDistance2(a.p2), a.p1.scaleAndOrientationDistance2(a.p2));
				}
				editor.left.repaint();
				editor.right.repaint();
			}
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));}});
	}
}
