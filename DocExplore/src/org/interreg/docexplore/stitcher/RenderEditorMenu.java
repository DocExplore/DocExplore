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
