/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.annotate;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.gui.DocumentPanel;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;
import org.interreg.docexplore.manuscript.actions.DeleteMetaDataAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

public class AnnotationHandler
{
	DocExploreDataLink link;
	MainWindow win;
	
	public AnnotationHandler(MainWindow win)
	{
		this.link = win.getDocExploreLink();
		this.win = win;
	}
	
	int limChars = 32;
	public JLabel buildDisplayLabel(AnnotatedObject document, MetaData annotation)
	{
		try
		{
			if (annotation.getKey() == link.transcriptionKey)
			{
				String value = annotation.getString();
				String author = value.substring(value.indexOf("<author>")+"<author>".length(), value.indexOf("</author>"));
				String content = value.substring(value.indexOf("<content>")+"<content>".length(), value.indexOf("</content>"));
				return new JLabel("<html><b>"+XMLResourceBundle.getBundledString("transcriptDisplayLabel")+
						"<u>"+author+"</u></b>&nbsp;&nbsp;&nbsp<i>"+
						content.substring(0, Math.min(limChars, content.length()))+(content.length() > limChars ? "..." : "")+"</i></html>", 
					ImageUtils.getIcon("transcription-32x32.png"), SwingConstants.HORIZONTAL);
			}
			else if (annotation.getKey() == link.tagsKey)
			{
				StringBuilder tagString = new StringBuilder();
				for (MetaData tag : document.getMetaDataListForKey(link.tagKey))
					tagString.append(TagHolder.extractLocalizedTag(tag.getString(), Locale.getDefault().getLanguage())+", ");
				if (tagString.length() > 0)
					tagString.delete(tagString.length()-2, tagString.length());
				String content = tagString.toString();
				return new JLabel("<html><b>"+XMLResourceBundle.getBundledString("tagTagLabel")+"</b>&nbsp;&nbsp;&nbsp<i>"+
						content.substring(0, Math.min(limChars, content.length()))+(content.length() > limChars ? "..." : "")+"</i></html>", 
					ImageUtils.getIcon("tag-32x32.png"), SwingConstants.HORIZONTAL);
			}
			else
			{
				String keyName = annotation.getKey().getName();
				if (keyName == null)
					keyName = annotation.getKey().getName("");
				if (annotation.getType().equals(MetaData.imageType))
					return new JLabel("<html><b>"+keyName+"</b></html>", ImageUtils.getIcon("image-32x32.png"), SwingConstants.HORIZONTAL);
				else if (annotation.getType().equals(MetaData.textType))
				{
					String content = annotation.getString();
					return new JLabel("<html><b>"+keyName+"</b>&nbsp;&nbsp;&nbsp<i>"+
							content.substring(0, Math.min(limChars, content.length()))+(content.length() > limChars ? "..." : "")+"</i></html>",
						ImageUtils.getIcon("free-32x32.png"), SwingConstants.HORIZONTAL);
				}
				else for (MetaDataPlugin plugin : win.pluginManager.metaDataPlugins)
					if (annotation.getType().toString().equals(plugin.getType()))
				{
					return plugin.createLabel(keyName, annotation);
				}
			}
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		return new JLabel("Annotation id #"+annotation.getId());
	}
	
	public Set<MetaData> getSuggestedTags(MetaData annotation)
	{
		try {return link.tagKey.getMetaData(MetaData.textType);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return new TreeSet<MetaData>();
	}
	
	public void setTags(AnnotatedObject document, Set<String> tagNames) throws DataLinkException
	{
		List<MetaData> tags = document.getMetaDataListForKey(link.tagKey);
		for (MetaData tag : tags)
			document.removeMetaData(tag);
		for (String tagName : tagNames)
		{
			MetaData tag = link.getOrCreateTag(tagName);//new MetaData(link, link.tagKey, "<tag lang=\""+lang+"\">"+tagName+"</tag>");
			document.addMetaData(tag);
		}
	}
	
	public void deleteMetaData(final AnnotatedObject document, MetaData annotation) throws DataLinkException
	{
		final DeleteMetaDataAction deletePagesAction = win.getActionProvider().deleteMetaData(document, annotation);
		try
		{
			win.historyManager.doAction(new WrappedAction(deletePagesAction)
			{
				public void doAction() throws Exception {super.doAction(); refreshAnnotationPanel(document);}
				public void undoAction() throws Exception {super.undoAction(); refreshAnnotationPanel(document);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void refreshAnnotationPanel(AnnotatedObject document) throws Exception
	{
		DocumentPanel panel = win.getPanelForDocument(document);
		if (panel == null)
			return;
		panel.annotationPanel.setDocument(document);
	}
	
	public JRadioButton buildRadioButton(JPanel panel, String text, Icon icon, Map<JRadioButton, String> labels)
	{
		JRadioButton button = new JRadioButton();
		JPanel subPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		subPanel.add(button);
		subPanel.add(new JLabel(text, icon, SwingConstants.LEFT));
		panel.add(subPanel);
		labels.put(button, text);
		return button;
	}
	
	@SuppressWarnings("serial")
	public MetaData createMetaData(final AnnotatedObject document) throws DataLinkException
	{
		final JDialog dialog = new JDialog(win, XMLResourceBundle.getBundledString("annotateAnnotationLabel"), true);
		JPanel mainPanel = new JPanel(new BorderLayout());
		dialog.add(mainPanel);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		mainPanel.add(new JLabel(XMLResourceBundle.getBundledString("annotateAskTypeLabel")), BorderLayout.NORTH);
		
		Map<JRadioButton, String> labels = new HashMap<JRadioButton, String>();
		JPanel choicePanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, SwingConstants.LEFT, SwingConstants.TOP));
		JRadioButton transButton = buildRadioButton(choicePanel, XMLResourceBundle.getBundledString("transcriptEditorLabel"), ImageUtils.getIcon("transcription-32x32.png"), labels);
		JRadioButton textButton = buildRadioButton(choicePanel, XMLResourceBundle.getBundledString("annotateFreeTextLabel"), ImageUtils.getIcon("free-32x32.png"), labels);
		JRadioButton imageButton = buildRadioButton(choicePanel, XMLResourceBundle.getBundledString("annotateTypeImage"), ImageUtils.getIcon("image-32x32.png"), labels);
		JRadioButton tagsButton = buildRadioButton(choicePanel, XMLResourceBundle.getBundledString("tagTagLabel"), ImageUtils.getIcon("tag-32x32.png"), labels);
		JRadioButton [] pluginButtons = new JRadioButton [win.pluginManager.metaDataPlugins.size()];
		for (int i=0;i<win.pluginManager.metaDataPlugins.size();i++)
		{
			MetaDataPlugin plugin = win.pluginManager.metaDataPlugins.get(i);
			pluginButtons[i] = buildRadioButton(choicePanel, plugin.getFileType(), ImageUtils.getIcon("video-32x32.png"), labels);
		}
		ButtonGroup group = new ButtonGroup();
		group.add(transButton);
		group.add(textButton);
		group.add(imageButton);
		group.add(tagsButton);
		for (JRadioButton pluginButton : pluginButtons)
			group.add(pluginButton);
		transButton.setSelected(true);
		mainPanel.add(choicePanel, BorderLayout.CENTER);
		
		final boolean [] ok = {false};
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalOkLabel")) {
			public void actionPerformed(ActionEvent e) {ok[0] = true; dialog.setVisible(false);}}));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel")) {
			public void actionPerformed(ActionEvent e) {dialog.setVisible(false);}}));
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		dialog.pack();
		GuiUtils.centerOnScreen(dialog);
		dialog.setVisible(true);
		if (!ok[0])
			return null;
		
		MetaData annotation = null;
		MetaDataPlugin plugin = null;
		String sourceUri = null;
		
		if (transButton.isSelected())
			annotation = new MetaData(link, link.transcriptionKey, "<author></author><content></content>");
		else if (tagsButton.isSelected())
		{
			List<MetaData> mds = document.getMetaDataListForKey(link.tagsKey);
			if (mds != null && mds.size() > 0)
				return mds.get(0);
			annotation = new MetaData(link, link.tagsKey, "");
		}
		else
		{
			String preselect = null;
			Enumeration<AbstractButton> buttons = group.getElements();
			while (buttons.hasMoreElements())
			{
				AbstractButton button = buttons.nextElement();
				if (button.isSelected())
					preselect = labels.get(button);
			}
			MetaDataKey key = requestKey(preselect);
			if (key == null)
				return null;
			if (imageButton.isSelected())
			{
				File file = DocExploreTool.getFileDialogs().openFile(DocExploreTool.getImagesCategory());
				if (file != null) try
				{
					FileInputStream in = new FileInputStream(file);
					try
					{
						if (ImageUtils.read(in) == null)
							throw new Exception("Unable to read image file!");
					}
					catch (Exception e) {in.close(); throw e;}
					annotation = new MetaData(link, key, MetaData.imageType, new FileInputStream(file));
					sourceUri = file.getAbsolutePath();
				}
				catch (Exception e) {throw new DataLinkException(link.getLink(), "Error opening image : '"+file.getAbsolutePath()+"'", e);}
			}
			else if (textButton.isSelected())
				annotation = new MetaData(link, key, "");
			else for (int i=0;i<pluginButtons.length;i++)
				if (pluginButtons[i].isSelected())
				{
					plugin = win.pluginManager.metaDataPlugins.get(i);
					Object val = plugin.createDefaultValue();
					InputStream stream = null;
					if (val instanceof InputStream)
						stream = (InputStream)val;
					else if (val instanceof File)
						try {stream = new FileInputStream((File)val); sourceUri = ((File)val).getAbsolutePath();}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					if (stream != null)
						annotation = new MetaData(link, key, plugin.getType(), stream);
					break;
				}
		}
		if (annotation != null)
		{
			if (sourceUri != null)
				annotation.addMetaData(new MetaData(link, link.getOrCreateKey("source-uri"), sourceUri));
			final AddMetaDataAction addMetDataAction = win.getActionProvider().addMetaData(document, annotation);
			try
			{
				win.historyManager.doAction(new WrappedAction(addMetDataAction)
				{
					public void doAction() throws Exception {super.doAction(); refreshAnnotationPanel(document);}
					public void undoAction() throws Exception {super.undoAction(); refreshAnnotationPanel(document);}
				});
			}
			catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
		}
		return annotation;
	}
	
	public AnnotationEditor getEditorFor(AnnotationPanel panel, MetaData annotation) throws DataLinkException
	{
		if (annotation.getKey() == link.transcriptionKey)
			return new TranscriptionEditor(panel, annotation);
		else if (annotation.getKey() == link.tagsKey)
			return new TagsEditor(panel, annotation);
		else if (annotation.getType().equals(MetaData.textType))
			return new FreeTextEditor(panel, annotation);
		else if (annotation.getType().equals(MetaData.imageType))
			return new ImageEditor(panel, annotation);
		else for (MetaDataPlugin plugin : win.pluginManager.metaDataPlugins)
			if (annotation.getType().toString().equals(plugin.getType()))
				return plugin.createEditor(panel, annotation);
		return null;
	}
	
	@SuppressWarnings("serial")
	MetaDataKey requestKey(String preselect) throws DataLinkException
	{
		final boolean [] ok = {false};
		final JDialog dialog = new JDialog(win, XMLResourceBundle.getBundledString("annotateKeyLabel"), true);
		JPanel mainPanel = new JPanel(new BorderLayout());
		dialog.add(mainPanel);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		mainPanel.add(new JLabel(XMLResourceBundle.getBundledString("annotateAskKeyLabel")), BorderLayout.NORTH);
		
		Collection<MetaDataKey> allKeys = link.getAllKeys();
		Set<String> keys = new TreeSet<String>(new Comparator<String>()
			{Collator collator = Collator.getInstance(Locale.getDefault());
			public int compare(String o1, String o2) {return collator.compare(o1, o2);}});
		boolean foundPreselect = false;
		for (MetaDataKey key : allKeys)
			if (!link.functionalKeys.contains(key))
		{
			String keyName = key.getName();
			if (keyName != null && keyName.trim().length() > 0)
				keys.add(keyName);
			if (keyName != null && preselect != null && keyName.equals(preselect))
				foundPreselect = true;
		}
		if (!foundPreselect)
			keys.add(preselect);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		JComboBox keyBox = new JComboBox(keys.toArray());
		keyBox.setEditable(true);
		keyBox.setSelectedItem(preselect);
		mainPanel.add(keyBox, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		final JButton okButton, cancelButton;
		buttonPanel.add(okButton = new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalOkLabel")) {
			public void actionPerformed(ActionEvent e) {ok[0] = true; dialog.setVisible(false);}}));
		buttonPanel.add(cancelButton = new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel")) {
			public void actionPerformed(ActionEvent e) {dialog.setVisible(false);}}));
		okButton.setDefaultCapable(true);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
		mainPanel.getActionMap().put("ESC", new AbstractAction() {public void actionPerformed(ActionEvent e) {cancelButton.doClick();}});
		mainPanel.getActionMap().put("OK", new AbstractAction() {public void actionPerformed(ActionEvent e) {okButton.doClick();}});
		//keyBox.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {okButton.doClick();}});
		
		dialog.pack();
		dialog.setResizable(false);
		GuiUtils.centerOnScreen(dialog);
		dialog.setVisible(true);
		if (!ok[0])
			return null;
		
		String value = keyBox.getSelectedItem().toString();
		if (value.trim().length() == 0)
			value = preselect;
		
		MetaDataKey key = link.getOrCreateKey(value);
		if (link.functionalKeys.contains(key))
		{
			JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), XMLResourceBundle.getBundledString("keyNameReservedMessage"), "", JOptionPane.ERROR_MESSAGE);
			return requestKey(preselect);
		}
		return key;
	}
}
