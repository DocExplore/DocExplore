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
import org.interreg.docexplore.internationalization.XMLResourceBundle;

public class MySQLConnectionDialog
{
	@SuppressWarnings("serial")
	public static DataLinkMySQLSource show()
	{
		final JDialog dialog = new JDialog(JOptionPane.getRootFrame(), XMLResourceBundle.getBundledString("databaseTitle"), true);
		JPanel fields = new JPanel(new LooseGridLayout(4, 2, 5, 5, SwingConstants.LEFT, SwingConstants.LEFT));
		fields.add(new JLabel(XMLResourceBundle.getBundledString("databaseUrlLabel")));
		JTextField urlField = (JTextField)fields.add(new JTextField(40));
		fields.add(new JLabel(XMLResourceBundle.getBundledString("databaseDatabaseLabel")));
		JTextField dbField = (JTextField)fields.add(new JTextField(40));
		fields.add(new JLabel(XMLResourceBundle.getBundledString("databaseUserLabel")));
		JTextField userField = (JTextField)fields.add(new JTextField(40));
		fields.add(new JLabel(XMLResourceBundle.getBundledString("databasePasswordLabel")));
		JPasswordField passField = (JPasswordField)fields.add(new JPasswordField(40));
		dialog.add(fields, BorderLayout.CENTER);
		
		final boolean [] ok = {false};
		JPanel buttons = new JPanel(new FlowLayout(SwingConstants.CENTER));
		buttons.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalOkLabel"))
			{public void actionPerformed(ActionEvent e) {ok[0] = true; dialog.setVisible(false);}}));
		buttons.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel"))
			{public void actionPerformed(ActionEvent e) {dialog.setVisible(false);}}));
		dialog.add(buttons, BorderLayout.SOUTH);
		
		dialog.pack();
		dialog.setVisible(true);
		
		if (!ok[0])
			return null;
		return new DataLinkMySQLSource(urlField.getText(), dbField.getText(), userField.getText(), new String(passField.getPassword()));
	}
}
