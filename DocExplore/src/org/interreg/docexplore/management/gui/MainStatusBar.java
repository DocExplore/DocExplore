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
