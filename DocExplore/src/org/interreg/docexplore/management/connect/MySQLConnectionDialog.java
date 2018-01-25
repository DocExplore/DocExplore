/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.connect;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.mysql.DataLinkMySQLSource;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.Lang;

public class MySQLConnectionDialog
{
	@SuppressWarnings("serial")
	public static DataLinkMySQLSource show()
	{
		final JDialog dialog = new JDialog(JOptionPane.getRootFrame(), Lang.s("databaseTitle"), true);
		JPanel fields = new JPanel(new LooseGridLayout(4, 2, 5, 5, SwingConstants.LEFT, SwingConstants.LEFT));
		fields.add(new JLabel(Lang.s("databaseUrlLabel")));
		JTextField urlField = (JTextField)fields.add(new JTextField(40));
		fields.add(new JLabel(Lang.s("databaseDatabaseLabel")));
		JTextField dbField = (JTextField)fields.add(new JTextField(40));
		fields.add(new JLabel(Lang.s("databaseUserLabel")));
		JTextField userField = (JTextField)fields.add(new JTextField(40));
		fields.add(new JLabel(Lang.s("databasePasswordLabel")));
		JPasswordField passField = (JPasswordField)fields.add(new JPasswordField(40));
		dialog.add(fields, BorderLayout.CENTER);
		
		final boolean [] ok = {false};
		JPanel buttons = new JPanel(new FlowLayout(SwingConstants.CENTER));
		buttons.add(new JButton(new AbstractAction(Lang.s("generalOkLabel"))
			{public void actionPerformed(ActionEvent e) {ok[0] = true; dialog.setVisible(false);}}));
		buttons.add(new JButton(new AbstractAction(Lang.s("generalCancelLabel"))
			{public void actionPerformed(ActionEvent e) {dialog.setVisible(false);}}));
		dialog.add(buttons, BorderLayout.SOUTH);
		
		dialog.pack();
		dialog.setVisible(true);
		
		if (!ok[0])
			return null;
		return new DataLinkMySQLSource(urlField.getText(), dbField.getText(), userField.getText(), new String(passField.getPassword()));
	}
}
