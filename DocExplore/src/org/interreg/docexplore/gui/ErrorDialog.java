/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class ErrorDialog extends JDialog
{
	JLabel message;
	JToggleButton detailsButton;
	JTextPane detailsPane;
	JScrollPane scrollPane;
	Throwable ex = null;
	
	public ErrorDialog()
	{
		super(JOptionPane.getRootFrame(), Lang.s("errorGenericTitle"), true);
		
		getContentPane().setLayout(new BorderLayout(20, 20));
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		messagePanel.add(new JLabel(ImageUtils.getIcon("remove-24x24.png")));
		messagePanel.add(message = new JLabel(""));
		//messagePanel.getInsets().set(20, 20, 20, 20);
		getContentPane().add(messagePanel, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("dialogCloseLabel")) {public void actionPerformed(ActionEvent e)
		{
			ErrorDialog.this.setVisible(false);
		}}));
		buttonPanel.add(detailsButton = new JToggleButton(new AbstractAction(Lang.s("errorDetailsLabel")) {public void actionPerformed(ActionEvent e)
		{
			if (detailsButton.isSelected())
			{
				ErrorDialog.this.getContentPane().add(scrollPane, BorderLayout.SOUTH);
			}
			else
			{
				ErrorDialog.this.getContentPane().remove(scrollPane);
			}
			pack();
		}}));
		buttonPanel.setPreferredSize(new Dimension(480, 40));
		getContentPane().add(buttonPanel, BorderLayout.CENTER);
		
		detailsPane = new JTextPane();
		scrollPane = new JScrollPane(detailsPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(480, 240));
		detailsPane.setBackground(getContentPane().getBackground());
		scrollPane.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(10, 10, 10, 10), 
			scrollPane.getBorder()));
	}
	
	public void show(Throwable ex)
	{
		this.ex = ex;
		message.setText(Lang.s("errorGenericMessage"));
		StringWriter out = new StringWriter();
		ex.printStackTrace(new PrintWriter(out, true));
		detailsPane.setText(out.toString());
		detailsButton.setSelected(false);
		getContentPane().remove(scrollPane);
		pack();
		GuiUtils.centerOnScreen(this);
		setVisible(true);
	}
	
	public static void main(String [] args)
	{
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e) {e.printStackTrace();}
		
		ErrorDialog dialog = new ErrorDialog();
		dialog.show(new Exception("Big problem!"));
		dialog.show(new Exception("Another big problem!"));
		System.exit(0);
	}
}
