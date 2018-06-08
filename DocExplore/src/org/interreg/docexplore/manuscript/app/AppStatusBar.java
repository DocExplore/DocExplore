/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class AppStatusBar extends JPanel implements ManuscriptAppHost.AppListener
{
	ManuscriptAppHost host;
	JLabel connectionStatus;
	JLabel message;
	
	public AppStatusBar(final ManuscriptAppHost host)
	{
		super(new BorderLayout());
		
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
		this.host = host;
		this.connectionStatus = new JLabel();
		
		messagePanel.add(new JLabel(){public void paintComponent(Graphics g)
			{
				g.setColor(Color.black);
				g.fillOval(0, 0, getWidth(), getHeight());
				g.setColor(!host.getLink().isLinked() ? Color.red : Color.green);
				g.fillOval(2, 2, getWidth()-4, getHeight()-4);
			}
		}).setPreferredSize(new Dimension(11, 11));
		messagePanel.add(connectionStatus);
		messagePanel.add(new JLabel("|"));
		messagePanel.add(message = new JLabel(""));
		add(messagePanel, BorderLayout.CENTER);
		
		JButton help = new JButton(ImageUtils.getIcon("help-24x24.png"));
		help.setBorderPainted(false);
		help.setBorder(null);
		help.setContentAreaFilled(false);
		help.setEnabled(false);
		help.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {host.helpPanel.toggle();}});
		add(help, BorderLayout.EAST);
		
		host.helpPanel.addListener(new HelpPanel.Listener() {@Override public void onContentSet(String message) {help.setEnabled(message != null);}});
		host.addAppListener(this);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}
	
	public void setConnectionStatus(String s) {connectionStatus.setText(s); repaint();}
	public void setMessage(String s) {message.setText(s); repaint();}

	public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document) {}
	public void dataLinkChanged(DocExploreDataLink link) {setConnectionStatus(!link.isLinked() ? Lang.s("generalNoLinkStatus") : 
		link.getWrappedSource().getDescription());}
}
