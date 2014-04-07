package org.interreg.docexplore.management.annotate;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class TranscriptionEditor extends AnnotationEditor
{
	JTextField authorField;
	JTextArea contentArea;
	
	public TranscriptionEditor(AnnotationPanel panel, MetaData annotation) throws DataLinkException
	{
		super(panel, annotation);
		
		this.authorField = new JTextField(60);
		this.contentArea = new JTextArea(5, 40);
		contentArea.setLineWrap(true);
		contentArea.setWrapStyleWord(true);
	}
	
	protected void fillExpandedState()
	{
		super.fillExpandedState();
		
		try
		{
			String value = annotation.getString();
			authorField.setText(value.substring(value.indexOf("<author>")+"<author>".length(), value.indexOf("</author>")));
			contentArea.setText(value.substring(value.indexOf("<content>")+"<content>".length(), value.indexOf("</content>")));
			
			JPanel mainPanel = new JPanel(new BorderLayout());
			mainPanel.setOpaque(false);
			mainPanel.add(new JLabel("<html><big>"+XMLResourceBundle.getBundledString("transcriptEditorLabel")+"</big></html>", 
					ImageUtils.getIcon("transcription-64x64.png"), 
				SwingConstants.LEFT), BorderLayout.NORTH);
			
			JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			authorPanel.setOpaque(false);
			authorPanel.add(new JLabel(XMLResourceBundle.getBundledString("transcriptEditorAuthorLabel")));
			authorPanel.add(authorField);
			mainPanel.add(authorPanel, BorderLayout.CENTER);
			
			JPanel contentPanel = new JPanel(new BorderLayout());
			contentPanel.setOpaque(false);
			contentPanel.setBorder(BorderFactory.createTitledBorder(
				XMLResourceBundle.getBundledString("transcriptEditorContentLabel")));
			contentArea.setLineWrap(true);
			contentPanel.add(new JScrollPane(contentArea, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
			mainPanel.add(contentPanel, BorderLayout.SOUTH);
			
			add(mainPanel, BorderLayout.NORTH);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public void writeObject(MetaData annotation) throws DataLinkException
	{
		annotation.setString("<author>"+authorField.getText()+"</author>"+"<content>"+contentArea.getText()+"</content>");
	}
}
