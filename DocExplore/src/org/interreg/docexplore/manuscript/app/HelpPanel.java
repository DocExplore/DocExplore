/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.app;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.app.editors.GuiConstants;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class HelpPanel extends JDialog
{
	public static interface Listener
	{
		public void onContentSet(String message);
	}
	
	JFrame frame;
	JPanel panel;
	ArrayDeque<String> stack = new ArrayDeque<>();
	JScrollPane scrollPane;
	JEditorPane messagePane;
	JButton back;
	boolean expanded = false;
	
	public HelpPanel(JFrame frame)
	{
		super(frame, false);
		
		this.frame = frame;
		setUndecorated(true);
		setAlwaysOnTop(true);
		setFocusableWindowState(false);
		this.panel = new JPanel(new LooseGridLayout(2, 1, 0, 0, true, false, SwingConstants.CENTER, SwingConstants.CENTER, true, false));
		panel.setBackground(Color.white);
		setContentPane(panel);
		
		JPanel iconPanel = new JPanel(new LooseGridLayout(1, 0, 0, 0, true, false, SwingConstants.CENTER, SwingConstants.CENTER, true, false));
		iconPanel.setOpaque(false);
		JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		backPanel.setOpaque(false);
		iconPanel.add(backPanel);
		this.back = new JButton(ImageUtils.getIcon("previous-24x24.png"));
		backPanel.add(back);
		backPanel.add(new JLabel(Lang.s("managePreviousLabel")));
		back.setContentAreaFilled(false);
		back.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {previous();}});
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		closePanel.setOpaque(false);
		iconPanel.add(closePanel);
		JButton close = new JButton(ImageUtils.getIcon("remove-24x24.png"));
		closePanel.add(new JLabel(Lang.s("dialogCloseLabel")));
		closePanel.add(close);
		close.setContentAreaFilled(false);
		close.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {toggle();}});
		panel.add(iconPanel);
		//icon.setEnabled(false);
		
		panel.setBorder(BorderFactory.createLineBorder(GuiConstants.borderColor, 1));
		this.messagePane = new JEditorPane("text/html", "");
		messagePane.setFont(new JLabel().getFont());
		messagePane.setEditable(false);
		messagePane.addHyperlinkListener(new HyperlinkListener() {@Override public void hyperlinkUpdate(HyperlinkEvent e)
		{
			if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
				return;
			setContent(Lang.s(e.getDescription()), false, true);
		}});
		panel.add(this.scrollPane = new JScrollPane(messagePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		
		frame.addWindowListener(new WindowAdapter()
		{
			@Override public void windowIconified(WindowEvent e) {if (expanded) toggle();}
			@Override public void windowDeiconified(WindowEvent e) {if (expanded) setVisible(true);}
			@Override public void windowDeactivated(WindowEvent e) {if (expanded) setVisible(false);}
			@Override public void windowActivated(WindowEvent e) {if (expanded) setVisible(true);}
			@Override public void windowClosing(WindowEvent e) {if (expanded) toggle();}
		});
		frame.addComponentListener(new ComponentListener()
		{
			@Override public void componentResized(ComponentEvent e) {if (expanded) toggle();}
			@Override public void componentMoved(ComponentEvent e) {if (expanded) updatePosition();}
			@Override public void componentShown(ComponentEvent e) {if (expanded) setVisible(true);}
			@Override public void componentHidden(ComponentEvent e) {if (expanded) setVisible(false);}
		});
	}
	
	List<Listener> listeners = new ArrayList<>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.add(listener);}
	
	private String fontFamily = new JLabel().getFont().getFamily();
	private void setContent(String message, boolean forceExpand, boolean push)
	{
		if (!push)
			stack.clear();
		stack.push(message == null ? null : message.replaceAll("%icons%", ClassLoader.getSystemResource("org/interreg/docexplore/gui/icons/").toString()));
		if (!expanded && forceExpand)
			toggle();
		else if (expanded)
			updateContent();
		for (Listener listener : listeners)
			listener.onContentSet(stack.peek());
		back.setEnabled(stack.size() > 1);
	}
	
	private void previous()
	{
		if (stack.size() < 2)
			return;
		stack.pop();
		setContent(stack.pop(), false, true);
	}
	
	public void toggle()
	{
		if (stack.isEmpty() || stack.peek() == null)
			return;
		expanded = !expanded;
		if (expanded)
			updateContent();
		setVisible(expanded);
		frame.requestFocus();
	}
	
	private void updateContent()
	{
		messagePane.setText(stack.isEmpty() || stack.peek() == null ? "" : "<div width='"+frame.getWidth()/3+"' style='font-size: 1.1em; font-family: "+fontFamily+"'>"+
			stack.peek()+
			"</div>");
		panel.setPreferredSize(new Dimension(frame.getWidth()/3+2*scrollPane.getVerticalScrollBar().getPreferredSize().width, frame.getContentPane().getHeight()));
		pack();
		updatePosition();
		messagePane.setCaretPosition(0);
	}
	
	Point point = new Point();
	public void updatePosition()
	{
		Container top = frame.getContentPane();
		point.setLocation(top.getWidth(), top.getHeight());
		SwingUtilities.convertPointToScreen(point, top);
		setLocation(point.x-getWidth(), point.y-getHeight());
		//System.out.println(getX()+" "+getY()+" "+content.getPreferredSize().width+" "+content.getPreferredSize().height);
	}
	
	public JButton createHelpButton(String s)
	{
		JButton button = new JButton("");
		button.setBorderPainted(false);
		button.setBorder(null);
		button.setContentAreaFilled(false);
		button.setIcon(ImageUtils.getIcon("help-24x24.png"));
		button.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {setContent(s, true, false);}});
		return button;
	}
	
	public JButton createHelpMessageButton(String s) {return createHelpButton(s);}
}
