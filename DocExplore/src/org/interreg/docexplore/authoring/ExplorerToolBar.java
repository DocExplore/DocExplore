package org.interreg.docexplore.authoring;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.manuscript.app.ToolbarButton;
import org.interreg.docexplore.manuscript.app.ToolbarButton.ToolbarButtonListener;

@SuppressWarnings("serial")
public class ExplorerToolBar extends JPanel implements ToolbarButtonListener
{
	ManuscriptAppHost host;
	JPanel toolPanel;
	
	public ExplorerToolBar(ManuscriptAppHost host, boolean upOnly)
	{
		super(new BorderLayout());
		setOpaque(false);
		this.host = host;
		
		toolPanel = new JPanel(new WrapLayout(WrapLayout.LEFT));
		toolPanel.setOpaque(false);
		
		if (!upOnly) addButton(new ToolbarButton(this, "prev", "previous-24x24.png", Lang.s("imageToolbarPreviousHistoric")));
		addButton(new ToolbarButton(this, "up", "up-24x24.png", Lang.s("imageToolbarUp")));
		if (!upOnly) addButton(new ToolbarButton(this, "next", "next-24x24.png", Lang.s("imageToolbarNextHistoric")));
		add(toolPanel, BorderLayout.CENTER);
	}

	public void addButton(ToolbarButton button) {toolPanel.add(button);}

	@Override public void onToolbarButton(ToolbarButton button)
	{
		if (button.action != null)
			host.broadcastAction(button.action, null);
	}
	
}
