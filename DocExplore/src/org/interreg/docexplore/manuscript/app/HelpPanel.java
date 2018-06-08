package org.interreg.docexplore.manuscript.app;

import java.awt.BorderLayout;
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
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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
	JButton icon;
	String message = null;
	JScrollPane scrollPane;
	JEditorPane messagePane;
	boolean expanded = false;
	
	public HelpPanel(JFrame frame)
	{
		super(frame, false);
		
		this.frame = frame;
		setUndecorated(true);
		setAlwaysOnTop(true);
		setFocusableWindowState(false);
		this.panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.white);
		setContentPane(panel);
		
		JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		iconPanel.setOpaque(false);
		iconPanel.add(this.icon = new JButton("<html>&#9662;</html>", ImageUtils.getIcon("help-24x24.png")));
		panel.add(iconPanel, BorderLayout.SOUTH);
		icon.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {toggle();}});
		//icon.setEnabled(false);
		
		panel.setBorder(BorderFactory.createLineBorder(GuiConstants.borderColor, 1));
		this.messagePane = new JEditorPane("text/html", "");
		messagePane.setFont(new JLabel().getFont());
		messagePane.setEditable(false);
		messagePane.addHyperlinkListener(new HyperlinkListener() {@Override public void hyperlinkUpdate(HyperlinkEvent e)
		{
			if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
				return;
			setContent(Lang.s(e.getDescription()), false);
		}});
		panel.add(this.scrollPane = new JScrollPane(messagePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		
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
	public void setContent(String message, boolean forceExpand)
	{
		this.message = message == null ? null : message.replaceAll("%icons%", ClassLoader.getSystemResource("org/interreg/docexplore/gui/icons/").toString());
		if (!expanded && forceExpand)
			toggle();
		else if (expanded)
			updateContent();
		for (Listener listener : listeners)
			listener.onContentSet(message);
	}
	
	public void toggle()
	{
		if (message == null)
			return;
		expanded = !expanded;
		if (expanded)
			updateContent();
		setVisible(expanded);
		frame.requestFocus();
	}
	
	private void updateContent()
	{
		messagePane.setText(message == null ? "" : "<div width='"+frame.getWidth()/3+"' style='font-size: 1.1em; font-family: "+fontFamily+"'>"+
			message+
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
		button.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {setContent(s, true);}});
		return button;
	}
	
	public JButton createHelpMessageButton(String s) {return createHelpButton(s);}
}
