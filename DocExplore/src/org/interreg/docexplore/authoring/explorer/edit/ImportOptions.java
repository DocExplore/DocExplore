/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.interreg.docexplore.authoring.AuthoringToolFrame;
import org.interreg.docexplore.authoring.BookImporter;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.GuiUtils;

public class ImportOptions implements BookImporter.PresentationFilter
{
	static Map<String, String> cachedOptions = new TreeMap<String, String>();
	
	@SuppressWarnings("serial")
	public static class AnnotationPanel extends JPanel
	{
		ImportPanel importPanel;
		String annotationName;
		@SuppressWarnings("rawtypes")
		JComboBox shouldImportBox;
		@SuppressWarnings("rawtypes")
		JComboBox styleBox;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public AnnotationPanel(ImportPanel importPanel, String annotationName)
		{
			super(new FlowLayout(FlowLayout.LEFT));
			
			this.importPanel = importPanel;
			this.annotationName = annotationName;
			
			add(new JLabel(XMLResourceBundle.getBundledString("importAnnotationFilter").replace("%name", annotationName)));
			add(shouldImportBox = new JComboBox(new Object [] {XMLResourceBundle.getBundledString("importImported"), XMLResourceBundle.getBundledString("importDiscarded")}));
			final JLabel textStyleLabel = new JLabel(XMLResourceBundle.getBundledString("importTextStyle"));
			add(textStyleLabel);
			Object [] styles = new Object [importPanel.importOptions.tool.styleManager.styleDialog.styles.length];
			for (int i=0;i<styles.length;i++)
				styles[i] = importPanel.importOptions.tool.styleManager.styleDialog.styles[i].name;
			add(styleBox = new JComboBox(styles));
			
			shouldImportBox.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;
				textStyleLabel.setVisible(shouldImportBox.getSelectedIndex() == 0);
				styleBox.setVisible(shouldImportBox.getSelectedIndex() == 0);
			}});
			
			String val = null;
			if ((val = cachedOptions.get("ap-"+annotationName+"-si")) != null)
				shouldImportBox.setSelectedIndex(Integer.parseInt(val));
			if ((val = cachedOptions.get("ap-"+annotationName+"-sb")) != null)
			{
				for (int i=0;i<styles.length;i++)
					if (styles[i].equals(val))
					{
						styleBox.setSelectedIndex(i);
						break;
					}
			}
		}
		
		void cache()
		{
			cachedOptions.put("ap-"+annotationName+"-si", ""+shouldImportBox.getSelectedIndex());
			cachedOptions.put("ap-"+annotationName+"-sb", styleBox.getSelectedItem().toString());
		}
	}
	
	@SuppressWarnings("serial")
	public static class ImportPanel extends JPanel
	{
		ImportOptions importOptions;
		JCheckBox emptyPageBox, usePageTagsBox, emptyRegionBox, useRegionTagsBox;
		JTextField pageTagField, regionTagField;
		Map<String, AnnotationPanel> annotationPanels = new TreeMap<String, ImportOptions.AnnotationPanel>();
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public ImportPanel(DocExploreDataLink sourceLink, List<Page> pages, ImportOptions importOptions) throws DataLinkException
		{
			super(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
			this.importOptions = importOptions;
			
			JPanel pagesPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
			Set<String> pageTags = new TreeSet<String>();
			for (Page page : pages)
				for (MetaData tag : page.getMetaDataListForKey(sourceLink.tagKey))
					pageTags.add(DocExploreDataLink.getBestTagName(tag));
			pagesPanel.add(this.emptyPageBox = new JCheckBox(XMLResourceBundle.getBundledString("importPageRegions")));
			if (!pageTags.isEmpty())
			{
				pagesPanel.add(this.usePageTagsBox = new JCheckBox(XMLResourceBundle.getBundledString("importPageTags")));
				JPanel tagPanel = new JPanel(new WrapLayout(WrapLayout.LEFT));
				tagPanel.add(this.pageTagField = new JTextField(30));
				Object [] availableTags = new Object [pageTags.size()+1];
				availableTags[0] = "";
				int cnt = 1;
				for (String tag : pageTags)
					availableTags[cnt++] = tag;
				tagPanel.add(new JComboBox(availableTags) {{addItemListener(new ItemListener() {public void itemStateChanged(java.awt.event.ItemEvent e)
				{
					if (e.getStateChange() != ItemEvent.SELECTED)
						return;
					JComboBox box = (JComboBox)e.getSource();
					if (box.getSelectedIndex() == 0)
						return;
					if (pageTagField.getText().length() > 0)
						pageTagField.setText(pageTagField.getText()+", "+e.getItem().toString());
					else pageTagField.setText(e.getItem().toString());
					box.setSelectedIndex(0);
				};});}});
				pagesPanel.add(tagPanel);
			}
			pagesPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("importPages")));
			add(pagesPanel);
			
			JPanel regionsPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
			Set<String> regionTags = new TreeSet<String>();
			for (Page page : pages)
				for (Region region : page.getRegions())
					for (MetaData tag : region.getMetaDataListForKey(sourceLink.tagKey))
						regionTags.add(DocExploreDataLink.getBestTagName(tag));
			regionsPanel.add(this.emptyRegionBox = new JCheckBox(XMLResourceBundle.getBundledString("importRegionAnnotations")));
			if (!regionTags.isEmpty())
			{
				regionsPanel.add(this.useRegionTagsBox = new JCheckBox(XMLResourceBundle.getBundledString("importRegionTags")));
				JPanel tagPanel = new JPanel(new WrapLayout(WrapLayout.LEFT));
				tagPanel.add(this.regionTagField = new JTextField(30));
				Object [] availableTags = new Object [regionTags.size()+1];
				availableTags[0] = "";
				int cnt = 1;
				for (String tag : regionTags)
					availableTags[cnt++] = tag;
				tagPanel.add(new JComboBox(availableTags) {{addItemListener(new ItemListener() {public void itemStateChanged(java.awt.event.ItemEvent e)
				{
					if (e.getStateChange() != ItemEvent.SELECTED)
						return;
					JComboBox box = (JComboBox)e.getSource();
					if (box.getSelectedIndex() == 0)
						return;
					if (regionTagField.getText().length() > 0)
						regionTagField.setText(regionTagField.getText()+", "+e.getItem().toString());
					else regionTagField.setText(e.getItem().toString());
					box.setSelectedIndex(0);
				};});}});
				regionsPanel.add(tagPanel);
			}
			regionsPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("importRegions")));
			add(regionsPanel);
			
			Set<String> keyStrings = new TreeSet<String>();
			for (Page page : pages)
				for (Region region : page.getRegions())
					for (MetaDataKey key : region.getMetaData().keySet())
						if (key != sourceLink.tagKey && key != sourceLink.tagsKey)
							keyStrings.add(key.getBestName());
			if (!keyStrings.isEmpty())
			{
				JPanel annotationsPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
				for (String annotationName : keyStrings)
				{
					AnnotationPanel annotationPanel = new AnnotationPanel(this, annotationName);
					annotationsPanel.add(annotationPanel);
					annotationPanels.put(annotationName, annotationPanel);
				}
				JScrollPane scrollPane = new JScrollPane(annotationsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("importAnnotations")));
				scrollPane.setPreferredSize(new Dimension(800, 320));
				add(scrollPane);
			}
			
			String val = null;
			if ((val = cachedOptions.get("ip-epb")) != null)
				emptyPageBox.setSelected(Boolean.parseBoolean(val));
			if (usePageTagsBox != null && (val = cachedOptions.get("ip-uptb")) != null)
				usePageTagsBox.setSelected(Boolean.parseBoolean(val));
			if (pageTagField != null && (val = cachedOptions.get("ip-ptf")) != null)
				pageTagField.setText(val);
			if ((val = cachedOptions.get("ip-erb")) != null)
				emptyRegionBox.setSelected(Boolean.parseBoolean(val));
			if (useRegionTagsBox != null && (val = cachedOptions.get("ip-urtb")) != null)
				useRegionTagsBox.setSelected(Boolean.parseBoolean(val));
			if (regionTagField != null && (val = cachedOptions.get("ip-rtf")) != null)
				regionTagField.setText(val);
		}
		
		//TODO: incredibly ugly! Parameter order changed in haste to avoid same erasure type as above constructor...
		public ImportPanel(DocExploreDataLink sourceLink, ImportOptions importOptions, List<Region> regions) throws DataLinkException
		{
			super(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
			this.importOptions = importOptions;
			
			Set<String> keyStrings = new TreeSet<String>();
			for (Region region : regions)
				for (MetaDataKey key : region.getMetaData().keySet())
					if (key != sourceLink.tagKey && key != sourceLink.tagsKey)
						keyStrings.add(key.getBestName());
			if (!keyStrings.isEmpty())
			{
				JPanel annotationsPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
				for (String annotationName : keyStrings)
				{
					AnnotationPanel annotationPanel = new AnnotationPanel(this, annotationName);
					annotationsPanel.add(annotationPanel);
					annotationPanels.put(annotationName, annotationPanel);
				}
				annotationsPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("importAnnotations")));
				add(annotationsPanel);
			}
		}
		
		void cache()
		{
			if (emptyPageBox != null)
			{
				cachedOptions.put("ip-epb", ""+emptyPageBox.isSelected());
				if (usePageTagsBox != null)
				{
					cachedOptions.put("ip-uptb", ""+usePageTagsBox.isSelected());
					cachedOptions.put("ip-ptf", ""+pageTagField.getText());
				}
				cachedOptions.put("ip-erb", ""+emptyRegionBox.isSelected());
				if (useRegionTagsBox != null)
				{
					cachedOptions.put("ip-urtb", ""+useRegionTagsBox.isSelected());
					cachedOptions.put("ip-rtf", ""+regionTagField.getText());
				}
			}
			for (AnnotationPanel panel : annotationPanels.values())
				panel.cache();
		}
	}
	
	AuthoringToolFrame tool;
	ImportPanel importPanel = null;
	DocExploreDataLink link;
	Set<String> pageTags, regionTags;
	
	public ImportOptions(AuthoringToolFrame tool)
	{
		this.tool = tool;
	}
	
	public boolean showOptions(Frame parent, DocExploreDataLink sourceLink, List<Page> pages) throws DataLinkException
		{return showOptions(parent, sourceLink, pages, null);}
	public boolean showOptions(Frame parent, DocExploreDataLink sourceLink, Region region) throws DataLinkException
		{return showOptions(parent, sourceLink, null, Collections.singletonList(region));}
	public boolean showOptionsForRegions(Frame parent, DocExploreDataLink sourceLink, List<Region> regions) throws DataLinkException
		{return showOptions(parent, sourceLink, null, regions);}
	@SuppressWarnings("serial")
	public boolean showOptions(Frame parent, DocExploreDataLink sourceLink, List<Page> pages, List<Region> regions) throws DataLinkException
	{
		this.link = sourceLink;
		final JDialog dialog = new JDialog(parent, XMLResourceBundle.getBundledString("importTitle"), true);
		dialog.getContentPane().setLayout(new BorderLayout());
		if (pages != null)
			dialog.getContentPane().add(this.importPanel = new ImportPanel(sourceLink, pages, this), BorderLayout.CENTER);
		else dialog.getContentPane().add(this.importPanel = new ImportPanel(sourceLink, this, regions), BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final boolean [] ok = {false};
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalOkLabel")) 
			{public void actionPerformed(ActionEvent arg0) {ok[0] = true; dialog.setVisible(false);}}));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel")) 
			{public void actionPerformed(ActionEvent arg0) {dialog.setVisible(false);}}));
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setResizable(false);
		GuiUtils.centerOnComponent(dialog, parent);
		dialog.setVisible(true);
		
		if (ok[0])
		{
			if (importPanel.usePageTagsBox != null && importPanel.usePageTagsBox.isSelected())
			{
				pageTags = new TreeSet<String>();
				String [] tags = importPanel.pageTagField.getText().split(",");
				for (int i=0;i<tags.length;i++)
					pageTags.add(tags[i].trim());
			}
			if (importPanel.useRegionTagsBox != null && importPanel.useRegionTagsBox.isSelected())
			{
				regionTags = new TreeSet<String>();
				String [] tags = importPanel.regionTagField.getText().split(",");
				for (int i=0;i<tags.length;i++)
					regionTags.add(tags[i].trim());
			}
			importPanel.cache();
		}
		
		return ok[0];
	}

	public boolean keepAnnotation(AnnotatedObject from, AnnotatedObject to, MetaData annotation) throws DataLinkException
	{
		if (from instanceof Page && annotation.getKey() == link.miniKey)
			return true;
		if (from instanceof MetaData && annotation.getKey().getBestName().equals("source-uri"))
			return true;
		if (!(from instanceof Region))
			return false;
		String name = annotation.getKey().getBestName();
		AnnotationPanel panel = importPanel.annotationPanels.get(name);
		if (panel == null || panel.shouldImportBox.getSelectedIndex() != 0)
			return false;
		return true;
	}

	public boolean keepPage(Page page) throws DataLinkException
	{
		if (importPanel.emptyPageBox.isSelected())
		{
			boolean found = false;
			for (Region region : page.getRegions())
				if (keepRegion(region))
				{
					found = true;
					break;
				}
			if (!found)
				return false;
		}
		if (importPanel.usePageTagsBox != null && importPanel.usePageTagsBox.isSelected())
		{
			boolean found = false;
			List<MetaData> mds = page.getMetaDataListForKey(link.tagKey);
			for (MetaData md : mds)
				if (pageTags.contains(DocExploreDataLink.getBestTagName(md)))
				{
					found = true;
					break;
				}
			if (!found)
				return false;
		}
		return true;
	}

	public boolean keepRegion(Region region) throws DataLinkException
	{
		if (importPanel.emptyRegionBox.isSelected())
		{
			boolean found = false;
			for (List<MetaData> mds : region.getMetaData().values())
				for (MetaData md : mds)
					if (keepAnnotation(region, null, md))
					{
						found = true;
						break;
					}
			if (!found)
				return false;
		}
		if (importPanel.useRegionTagsBox != null && importPanel.useRegionTagsBox.isSelected())
		{
			boolean found = false;
			List<MetaData> mds = region.getMetaDataListForKey(link.tagKey);
			for (MetaData md : mds)
				if (regionTags.contains(DocExploreDataLink.getBestTagName(md)))
				{
					found = true;
					break;
				}
			if (!found)
				return false;
		}
		return true;
	}
	
	public void updateMetaData(MetaData md) throws DataLinkException
	{
		if (!md.getType().equals(MetaData.textType))
			return;
		AnnotationPanel panel = importPanel.annotationPanels.get(md.getKey().getBestName());
		if (panel == null)
			return;
		String styleName = panel.styleBox.getSelectedItem().toString();
		for (int i=0;i<tool.styleManager.styleDialog.styles.length;i++)
			if (styleName.equals(tool.styleManager.styleDialog.styles[i].name))
			{
				MetaData styleMD = TextElement.getStyleMD(md);
				styleMD.setString(""+i);
				break;
			}
	}
}
