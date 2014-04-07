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
		panel.handler.setTags(annotation, tags);
	}
}
