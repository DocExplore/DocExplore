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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class FreeTextEditor extends AnnotationEditor
{
	JTextArea contentArea;
	
	public FreeTextEditor(AnnotationPanel panel, MetaData annotation) throws DataLinkException
	{
		super(panel, annotation);
		
		this.contentArea = new JTextArea(5, 40);
		contentArea.setLineWrap(true);
		contentArea.setWrapStyleWord(true);
		keyLabel.setIcon(ImageUtils.getIcon("free-64x64.png"));
	}
	
	protected void fillExpandedState()
	{
		super.fillExpandedState();
		
		try
		{
			JPanel mainPanel = new JPanel(new BorderLayout());
			mainPanel.setOpaque(false);
			
			mainPanel.add(buildKeyPanel(), BorderLayout.NORTH);
			
			contentArea.setText(annotation.getString());
			
			JPanel contentPanel = new JPanel(new BorderLayout());
			contentPanel.setOpaque(false);
			contentPanel.setBorder(BorderFactory.createTitledBorder(
				XMLResourceBundle.getBundledString("transcriptEditorContentLabel")));
			contentArea.setLineWrap(true);
			contentArea.setEditable(!readOnly);
			contentPanel.add(new JScrollPane(contentArea, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
			mainPanel.add(contentPanel, BorderLayout.CENTER);
			
			add(mainPanel, BorderLayout.NORTH);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public void writeObject(MetaData annotation) throws DataLinkException
	{
		annotation.setKey(annotation.getLink().getOrCreateKey(keyName));
		annotation.setString(contentArea.getText());
	}
}
