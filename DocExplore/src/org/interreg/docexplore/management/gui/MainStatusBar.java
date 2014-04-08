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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.AnnotatedObject;

@SuppressWarnings("serial")
public class MainStatusBar extends JPanel implements MainWindow.MainWindowListener
{
	MainWindow win;
	JLabel connectionStatus;
	JLabel message;
	
	public MainStatusBar(MainWindow win)
	{
		super(new FlowLayout(FlowLayout.LEFT, 10, 3));
		
		this.win = win;
		this.connectionStatus = new JLabel();
		
		add(new JLabel(){public void paintComponent(Graphics g)
			{
				g.setColor(Color.black);	
				g.fillOval(0, 0, getWidth(), getHeight());
				g.setColor(!MainStatusBar.this.win.getDocExploreLink().isLinked() ? Color.red : Color.green);
				g.fillOval(2, 2, getWidth()-4, getHeight()-4);
			}
		}).setPreferredSize(new Dimension(11, 11));
		add(connectionStatus);
		add(new JLabel("|"));
		add(message = new JLabel(""));
		
		win.addMainWindowListener(this);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}
	
	public void setConnectionStatus(String s) {connectionStatus.setText(s); repaint();}
	public void setMessage(String s) {message.setText(s); repaint();}

	public void activeDocumentChanged(AnnotatedObject document) {}
	public void dataLinkChanged(DocExploreDataLink link) {setConnectionStatus(!link.isLinked() ? XMLResourceBundle.getBundledString("generalNoLinkStatus") : 
		link.getWrappedSource().getDescription());}
}
