package org.interreg.docexplore.management.export;

import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.annotate.TagHolder;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;

@SuppressWarnings("serial")
public class TagArea extends JPanel
{
	JRadioButton anyButton, allButton;
	JTextArea tagArea;
	
	public TagArea()
	{
		super(new LooseGridLayout(0, 1, 10, 10, true, false));
		
		this.anyButton = new JRadioButton(XMLResourceBundle.getBundledString("exportAnyTagLabel"));
		this.allButton = new JRadioButton(XMLResourceBundle.getBundledString("exportAllTagsLabel"));
		this.tagArea = new JTextArea(3, 40);
		
		add(anyButton);
		add(allButton);
		add(tagArea);
		
		ButtonGroup group = new ButtonGroup();
		group.add(anyButton);
		group.add(allButton);
		anyButton.setSelected(true);
		
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}
	
	public boolean matchesCriteria(AnnotatedObject object) throws DataLinkException
	{
		DocExploreDataLink link = (DocExploreDataLink)object.getLink();
		List<MetaData> tags = object.getMetaDataListForKey(link.tagKey);
		String [] criteria = tagArea.getText().split(",");
		
		for (String tag : criteria)
		{
			tag = tag.trim();
			if (tag.length() == 0)
				continue;
			
			boolean found = false;
			for (MetaData objectTag : tags)
				if (tag.equals(TagHolder.extractLocalizedTag(objectTag.getString(), Locale.getDefault().getLanguage())))
					{found = true; break;}
			
			if (found && anyButton.isSelected())
				 return true;
			if (!found && allButton.isSelected())
				 return false;
		}
		return true;
	}
}
