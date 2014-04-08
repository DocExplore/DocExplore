/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
