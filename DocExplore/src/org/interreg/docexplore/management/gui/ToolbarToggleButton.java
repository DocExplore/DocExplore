package org.interreg.docexplore.management.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;

import org.interreg.docexplore.gui.IconToggleButton;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

public class ToolbarToggleButton extends IconToggleButton implements MainWindow.MainWindowListener
{
	private static final long serialVersionUID = 8928621174282726861L;
	
	public ToolbarToggleButton(String iconName, String toolTip)
	{
		super(iconName, toolTip);
		
		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (isSelected())
					toggled();
				else untoggled();
			}
		});
	}
	public ToolbarToggleButton(Icon icon, String toolTip)
	{
		super(icon, toolTip);
		
		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (isSelected())
					toggled();
				else untoggled();
			}
		});
	}

	public void toggled() {}
	public void untoggled() {}
	
	public void activeDocumentChanged(AnnotatedObject document)
	{
		setEnabled(document != null && (document instanceof Page || document instanceof Region));
	}

	public void dataLinkChanged(DocExploreDataLink link) {}
}
