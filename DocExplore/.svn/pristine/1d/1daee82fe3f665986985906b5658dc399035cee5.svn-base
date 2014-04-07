package org.interreg.docexplore.authoring;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class ExportOptions extends JPanel
{
	JCheckBox resizeBox;
	JTextField resizeField;
	
	public ExportOptions()
	{
		super(new BorderLayout());
		
		JPanel resizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		this.resizeBox = new JCheckBox();
		resizeBox.setSelected(true);
		resizePanel.add(resizeBox);
		resizePanel.add(new JLabel(XMLResourceBundle.getBundledString("exportSizeLimit")));
		this.resizeField = new JTextField(5);
		resizeField.setText("1500");
		resizeField.getDocument().addDocumentListener(new DocumentListener()
		{
			boolean check = true;
			public void removeUpdate(DocumentEvent e) {}
			public void insertUpdate(DocumentEvent e)
			{
				if (!check)
					return;
				String res = "";
				for (int i=0;i<resizeField.getText().length();i++)
					if (Character.isDigit(resizeField.getText().charAt(i)))
						res += resizeField.getText().charAt(i);
				final String text = res;
				SwingUtilities.invokeLater(new Runnable() {public void run()
				{
					check = false;
					resizeField.setText(text);
					check = true;
				}});
			}
			public void changedUpdate(DocumentEvent e) {}
		});
		resizePanel.add(resizeField);
		add(resizePanel, BorderLayout.CENTER);
		add(new JLabel(XMLResourceBundle.getBundledString("exportSizeMessage")), BorderLayout.SOUTH);
		
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
	}
	
	public BufferedImage handlePage(BufferedImage image)
	{
		if (!resizeBox.isSelected())
			return image;
		int w = image.getWidth(), h = image.getHeight();
		int lim = Integer.parseInt(resizeField.getText());
		if (w <= lim && h <= lim)
			return image;
		if (w > h)
		{
			h = h*lim/w;
			w = lim;
		}
		else
		{
			w = w*lim/h;
			h = lim;
		}
		return ImageUtils.resize(image, w, h);
	}
	
	static ExportOptions options = new ExportOptions();
	public static ExportOptions getOptions(Component comp)
	{
		final JDialog dialog = new JDialog(JOptionPane.getRootFrame(), XMLResourceBundle.getBundledString("exportOptions"), true);
		dialog.setLayout(new BorderLayout());
		dialog.add(options, BorderLayout.CENTER);
		final boolean [] res = {false};
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getString("default-lrb", "cfgOkLabel")) {public void actionPerformed(ActionEvent e)
		{
			res[0] = true;
			dialog.setVisible(false);
		}}));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getString("default-lrb", "cfgCancelLabel")) {public void actionPerformed(ActionEvent e)
		{
			dialog.setVisible(false);
		}}));
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		if (comp != null)
			GuiUtils.centerOnComponent(dialog, comp);
		dialog.setVisible(true);
		if (res[0])
			return options;
		return null;
	}
}
