/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class ExportOptions extends JPanel
{
	JCheckBox resizeBox, transparencyBox;
	JTextField resizeField;
	
	Map<String, JPanel> pluginOptions = new TreeMap<String, JPanel>();
	
	public ExportOptions()
	{
		super(new LooseGridLayout(0, 1, 5, 5, true, true, SwingConstants.LEFT, SwingConstants.TOP, false, false));
		
		JPanel dimensionOptionPanel = new JPanel(new BorderLayout());
		JPanel resizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		this.resizeBox = new JCheckBox();
		resizePanel.add(resizeBox);
		resizePanel.add(new JLabel(Lang.s("exportSizeLimit")));
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
		resizePanel.add(new JLabel("pixels"));
		dimensionOptionPanel.add(resizePanel, BorderLayout.CENTER);
		dimensionOptionPanel.add(new JLabel(Lang.s("exportSizeMessage")), BorderLayout.SOUTH);
		dimensionOptionPanel.setBorder(BorderFactory.createTitledBorder(Lang.s("exportDimensionOption")));
		add(dimensionOptionPanel);
		
		JPanel transparencyOptionPanel = new JPanel(new BorderLayout());
		JPanel transparencyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		this.transparencyBox = new JCheckBox();
		transparencyPanel.add(transparencyBox);
		transparencyPanel.add(new JLabel(Lang.s("exportTransparencyLabel")));
		transparencyOptionPanel.add(transparencyPanel, BorderLayout.CENTER);
		transparencyOptionPanel.add(new JLabel(Lang.s("exportTransparencyMessage")), BorderLayout.SOUTH);
		transparencyOptionPanel.setBorder(BorderFactory.createTitledBorder(Lang.s("exportTransparencyOption")));
		add(transparencyOptionPanel);
		
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
	}
	
	public void addPluginPanel(String name, JPanel panel)
	{
		pluginOptions.put(name, panel);
		add(panel);
	}
	public JPanel getPluginPanel(String name) {return pluginOptions.get(name);}
	
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
	public static ExportOptions getOptions(Component comp, List<MetaDataPlugin> plugins, int exportType)
	{
		for (MetaDataPlugin plugin : plugins)
			plugin.setupExportOptions(options, exportType);
		
		final JDialog dialog = new JDialog(JOptionPane.getRootFrame(), Lang.s("exportOptions"), true);
		dialog.setLayout(new BorderLayout());
		dialog.add(options, BorderLayout.CENTER);
		final boolean [] res = {false};
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("cfgOkLabel")) {public void actionPerformed(ActionEvent e)
		{
			res[0] = true;
			dialog.setVisible(false);
		}}));
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("cfgCancelLabel")) {public void actionPerformed(ActionEvent e)
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
