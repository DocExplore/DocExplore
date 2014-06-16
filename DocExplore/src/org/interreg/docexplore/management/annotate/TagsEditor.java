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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class TagsEditor extends AnnotationEditor
{
	JTextArea tagArea;

	public TagsEditor(AnnotationPanel panel, MetaData annotation) throws DataLinkException
	{
		super(panel, annotation);
		
		this.tagArea = new JTextArea(3, 30);
	}
	
	protected void fillExpandedState()
	{
		super.fillExpandedState();
		
		JPanel tagPanel = new JPanel(new BorderLayout());
		tagPanel.setOpaque(false);
		tagPanel.setBorder(BorderFactory.createTitledBorder(
			XMLResourceBundle.getBundledString("tagTagLabel")));
		tagPanel.add(new JScrollPane(tagArea, 
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.NORTH);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JPanel tagBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel tagHelpPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.setOpaque(false);
		tagBoxPanel.setOpaque(false);
		tagHelpPanel.setOpaque(false);
		
		Set<MetaData> tags = panel.handler.getSuggestedTags(annotation);
		Vector<TagHolder> tagVector = TagHolder.createTagVector(tags);
		tagVector.insertElementAt(TagHolder.emptyTagHolder, 0);
		final JComboBox predefinedTags = new JComboBox(tagVector);
		predefinedTags.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() != ItemEvent.SELECTED)
					return;
				String tag = ((TagHolder)e.getItem()).extractLocalizedTag(Locale.getDefault().getLanguage());
				
				if (!tag.equals(""))
				{
					String current = tagArea.getText().trim();
					if (current.length() == 0)
						tagArea.setText(tag);
					else if (current.endsWith(","))
						tagArea.setText(current+" "+tag);
					else tagArea.setText(current+", "+tag);
				}
				
				predefinedTags.setSelectedIndex(0);
			}
		});
		try {fillTagsArea();}
		catch (Exception e) {e.printStackTrace();}
		
		tagBoxPanel.add(new JLabel(XMLResourceBundle.getBundledString("tagAddTagLabel")));
		tagBoxPanel.add(predefinedTags);
		tagHelpPanel.add(new IconButton("help-24x24.png", 
			XMLResourceBundle.getBundledString("tagTagTooltip")));
		
		bottomPanel.add(tagBoxPanel, BorderLayout.WEST);
		bottomPanel.add(tagHelpPanel, BorderLayout.EAST);
		tagPanel.add(bottomPanel, BorderLayout.CENTER);
		add(tagPanel, BorderLayout.CENTER);
		
		add(new JLabel("<html><big>"+XMLResourceBundle.getBundledString("tagTagLabel")+"</big></html>", 
				ImageUtils.getIcon("tag-64x64.png"), 
			SwingConstants.LEFT), BorderLayout.NORTH);
	}
	
	void fillTagsArea() throws DataLinkException {tagArea.setText(buildTagString(panel.document));}
	
//	public void write() throws DataLinkException
//	{
//		StringTokenizer tokenizer = new StringTokenizer(tagArea.getText(), ",", false);
//		Set<String> tags = new TreeSet<String>();
//		while (tokenizer.hasMoreTokens())
//		{
//			String tag = tokenizer.nextToken().trim();
//			if (tag.length() > 0)
//				tags.add(tag);
//		}
//		panel.handler.setTags(panel.document, tags);
//	}

	public void writeObject(MetaData object) throws DataLinkException
	{
		StringTokenizer tokenizer = new StringTokenizer(tagArea.getText(), ",", false);
		Set<String> tags = new TreeSet<String>();
		while (tokenizer.hasMoreTokens())
		{
			String tag = tokenizer.nextToken().trim();
			if (tag.length() > 0)
				tags.add(tag);
		}
		panel.handler.setTags(panel.document, tags);
	}
}
