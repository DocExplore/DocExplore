/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.authoring.BookImporter;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Pair;

@SuppressWarnings("serial")
public class FilterPanel extends JButton implements BookImporter.PresentationFilter
{
	public boolean active = false;
	
	DocExploreDataLink link;
	JDialog filterDialog;
	JRadioButton seeAll, seeAnnotated, seeType, seeTag;
	JComboBox typeBox, tagBox;
	JCheckBox importNotEmpty, importKey;
	JComboBox keyBox;
	
	Vector<Object> tags = new Vector<Object>();
	Vector<Object> keys = new Vector<Object>();
	Vector<Object> types = new Vector<Object>();
	
	Icon offIcon = ImageUtils.getIcon("filter-off-24x24.png");
	Icon onIcon = ImageUtils.getIcon("filter-24x24.png");
	
	public FilterPanel(DocExploreDataLink link) throws DataLinkException
	{
		super(ImageUtils.getIcon("filter-off-24x24.png"));
		setToolTipText(Lang.s("filterConfig"));
		
		this.link = link;
		
		List<Integer> tagIds = link.tagKey.getMetaDataIds(MetaData.textType);
		Set<String> tagSet = new TreeSet<String>();
		for (int tagId : tagIds)
		{
			String text = DocExploreDataLink.getBestTagName(link.getMetaData(tagId));
			if (text != null && text.length() > 0)
				tagSet.add(text);
		}
		tags.addAll(tagSet);
		
		Collection<MetaDataKey> keyMds = link.getAllKeys();
		Set<String> keySet = new TreeSet<String>();
		for (MetaDataKey key : keyMds)
		{
			String text = key.getBestName();
			if (text != null && text.length() > 0)
				keySet.add(text);
		}
		keys.addAll(keySet);
		
		types.add(new Pair<String, String>(Lang.s("filterText"), "txt") {public String toString() {return first;}});
		types.add(new Pair<String, String>(Lang.s("filterImage"), "img") {public String toString() {return first;}});
		types.add(new Pair<String, String>(Lang.s("filterMedia"), "vid") {public String toString() {return first;}});
		
		this.filterDialog = new JDialog(JOptionPane.getRootFrame(), Lang.s("filterConfig"), false);
		JPanel content = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, false, false));
		content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JPanel pagePanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		pagePanel.setBorder(BorderFactory.createTitledBorder(Lang.s("filterPages")));
		seeAll = new JRadioButton(Lang.s("filterAllPages"));
		seeAnnotated = new JRadioButton(Lang.s("filterRoiPages"));
		seeType = new JRadioButton(Lang.s("filterAnnotPages"));
		seeTag = new JRadioButton(Lang.s("filterTagPages"));
		seeAll.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {updateButton(); notifyFilterChanged();}});
		seeAnnotated.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {updateButton(); notifyFilterChanged();}});
		seeType.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {updateButton(); notifyFilterChanged();}});
		seeTag.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {updateButton(); notifyFilterChanged();}});
		ButtonGroup group = new ButtonGroup();
		group.add(seeAll);
		group.add(seeAnnotated);
		group.add(seeType);
		group.add(seeTag);
		seeAll.setSelected(true);
		
		JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		typePanel.add(seeType);
		this.typeBox = new JComboBox(types);
		typeBox.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED && seeType.isSelected()) notifyFilterChanged();}});
		typePanel.add(typeBox);
		
		JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tagPanel.add(seeTag);
		this.tagBox = new JComboBox(tags);
		tagBox.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED && seeTag.isSelected()) notifyFilterChanged();}});
		tagPanel.add(tagBox);
		
		pagePanel.add(seeAll);
		pagePanel.add(seeAnnotated);
		pagePanel.add(typePanel);
		pagePanel.add(tagPanel);
		
		JPanel regionPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		regionPanel.setBorder(BorderFactory.createTitledBorder(Lang.s("filterRois")));
		importNotEmpty = new JCheckBox(Lang.s("filterAnnotRois"));
		importNotEmpty.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {updateButton();}});
		importKey = new JCheckBox(Lang.s("filterTagRois"));
		importKey.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
			if (!seeAll.isSelected()) notifyFilterChanged(); updateButton();}});
		
		JPanel rkeyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		rkeyPanel.add(importKey);
		this.keyBox = new JComboBox(keys);
		keyBox.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED && importKey.isSelected()) notifyFilterChanged();}});
		rkeyPanel.add(keyBox);
		
		regionPanel.add(importNotEmpty);
		regionPanel.add(rkeyPanel);
		
		content.add(pagePanel);
		content.add(regionPanel);
		filterDialog.add(content);
		
		filterDialog.pack();
		GuiUtils.centerOnScreen(filterDialog);
		
		addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			filterDialog.setVisible(true);
		}});
	}
	
	void updateButton()
	{
		if (seeAll.isSelected() && !importNotEmpty.isSelected() && !importKey.isSelected())
			setIcon(offIcon);
		else setIcon(onIcon);
	}
	
	public static interface Listener {public void filterChanged(FilterPanel filter);}
	List<Listener> listeners = new LinkedList<Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	public void notifyFilterChanged()
	{
		SwingUtilities.invokeLater(new Runnable() {public void run()
		{
			for (Listener listener : listeners)
				listener.filterChanged(FilterPanel.this);
		}});
	}
	
	public boolean keepAnnotation(AnnotatedObject from, AnnotatedObject to, MetaData annotation) throws DataLinkException
	{
		if (annotation.getKey() == link.tagKey)
			return false;
		if (annotation.getType().equals(MetaData.textType) && annotation.getString().trim().equals(""))
			return false;
		if (importKey.isSelected() && from instanceof Region)
			return annotation.getKey().getBestName().equals((String)keyBox.getSelectedItem());
		return true;
	}
	public boolean keepRegion(Region region) throws DataLinkException
	{
		if (!importNotEmpty.isSelected())
			return true;
		
		int nMds = 0;
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : region.getMetaData().entrySet())
			for (MetaData md : entry.getValue())
				if (keepAnnotation(region, null, md))
					nMds++;
		return nMds > 0;
	}
	
	@SuppressWarnings("unchecked")
	public boolean visible(Page page) throws DataLinkException
	{
		if (seeAnnotated.isSelected())
		{
			if (page.regions.isEmpty())
				return false;
			boolean res = false;
			for (int regionId : page.regions.keySet())
				if (!res)
			{
				Region region = page.getRegion(regionId);
				for (Map.Entry<MetaDataKey, List<MetaData>> entry : region.getMetaData().entrySet())
					if (!res)
						for (MetaData metaData : entry.getValue())
							if (keepAnnotation(region, null, metaData))
								{res = true; break;}
			}
			page.unloadRegions();
			return res;
		}
		else if (seeType.isSelected())
		{
			if (page.regions.isEmpty())
				return false;
			boolean res = false;
			String type = ((Pair<String, String>)typeBox.getSelectedItem()).second;
			for (int regionId : page.regions.keySet())
				if (!res)
			{
				Region region = page.getRegion(regionId);
				for (Map.Entry<MetaDataKey, List<MetaData>> entry : region.getMetaData().entrySet())
					if (!res)
						for (MetaData metaData : entry.getValue())
							if (keepAnnotation(region, null, metaData) && metaData.getType().equals(type))
								{res = true; break;}
			}
			page.unloadRegions();
			return res;
		}
		else if (seeTag.isSelected())
		{
			boolean res = false;
			String tag = (String)tagBox.getSelectedItem();
			List<MetaData> tags = page.getMetaDataListForKey(link.tagKey);
			for (MetaData md : tags)
			{
				String val = DocExploreDataLink.getBestTagName(md);
				if (val != null && tag.equals(val))
					{res = true; break;}
			}
			if (!res)
			{
				for (int regionId : page.regions.keySet())
					if (!res)
				{
					Region region = page.getRegion(regionId);
					tags = region.getMetaDataListForKey(link.tagKey);
					for (MetaData md : tags)
					{
						String val = DocExploreDataLink.getBestTagName(md);
						if (val != null && tag.equals(val))
							{res = true; break;}
					}
				}
				page.unloadRegions();
			}
			return res;
		}
		return true;
	}

	public boolean keepPage(Page page) throws DataLinkException {return true;}

	public void updateMetaData(MetaData md) throws DataLinkException {}
}
