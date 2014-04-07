package org.interreg.docexplore.gui.text;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

public class TestText
{
	public static void main(String [] args)
	{
		JFrame frame = new JFrame("text");
		frame.setLayout(new BorderLayout());
		final JTextPane pane = new JTextPane();
		pane.setPreferredSize(new Dimension(200, 100));
		pane.setEditorKit(new HTMLEditorKit());
		pane.addFocusListener(new FocusListener()
		{
			public void focusLost(FocusEvent e) {}
			public void focusGained(FocusEvent e)
			{
				pane.setText("<font size=\"5\" face=\"Times New Roman\" color=\"#ff00ff\">!</font>");
			}
		});
		frame.add(pane, BorderLayout.NORTH);
		JButton button = new JButton("!");
		frame.add(button, BorderLayout.SOUTH);
		button.requestFocus();
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
