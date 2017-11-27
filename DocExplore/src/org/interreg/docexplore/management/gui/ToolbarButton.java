/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
	
	public static interface ToolbarButtonListener
	{
		public void onToolbarButton(ToolbarButton button);
	};
	
	ToolbarButtonListener listener;
	String action;
	
	public ToolbarButton(String iconName, String toolTip) {this(null, null, iconName, toolTip);}
	public ToolbarButton(ToolbarButtonListener listener, String action, String iconName, String toolTip)
	{
		super(iconName, toolTip);
		this.listener = listener;
		this.action = action;
		addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {clicked();}});
		setFocusable(false);
	}
	public ToolbarButton(Icon icon, String toolTip) {this(null, null, icon, toolTip);}
	public ToolbarButton(ToolbarButtonListener listener, String action, Icon icon, String toolTip)
	{
		super(icon, toolTip);
		this.listener = listener;
		this.action = action;
		addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {clicked();}});
		setFocusable(false);
	}

	public void clicked()
	{
		if (listener != null)
			listener.onToolbarButton(this);
	}
	
	public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document)
	{
		setEnabled(document != null);
	}

	public void dataLinkChanged(DocExploreDataLink link) {}
}
