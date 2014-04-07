package org.interreg.docexplore.management.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;

import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.AnnotatedObject;

public class ToolbarButton extends IconButton implements MainWindow.MainWindowListener
{
	private static final long serialVersionUID = -5038557065370024502L;
	
	public ToolbarButton(String iconName, String toolTip)
	{
		super(iconName, toolTip);
		addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {clicked();}});
	}
	public ToolbarButton(Icon icon, String toolTip)
	{
		super(icon, toolTip);
		addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {clicked();}});
	}

	public void clicked() {}
	
	public void activeDocumentChanged(AnnotatedObject document)
	{
		setEnabled(document != null);
	}

	public void dataLinkChanged(DocExploreDataLink link) {}
}
