/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
