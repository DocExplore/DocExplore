package org.interreg.docexplore.management.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;

import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;
import org.interreg.docexplore.manuscript.app.editors.PosterPageEditor;

@SuppressWarnings("serial")
public class MMTPageEditor extends PosterPageEditor
{
	public MMTPageEditor(DocumentEditorHost listener, Page page) throws Exception
	{
		this(listener, page, null);
	}
	public MMTPageEditor(DocumentEditorHost listener, Region region) throws Exception
	{
		this(listener, region.getPage(), region);
	}
	public MMTPageEditor(DocumentEditorHost listener, Page page, Region region) throws Exception
	{
		super(listener, page, region);
		
		JButton help = host.getAppHost().helpPanel.createHelpMessageButton(Lang.s("helpMmtPageMsg"));
		add(help);
		addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e)
		{
			help.setSize(help.getPreferredSize());
			help.setLocation(getWidth()-help.getWidth(), 0);
		}});
	}
}
