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
