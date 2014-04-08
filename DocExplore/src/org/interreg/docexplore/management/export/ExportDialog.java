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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.process.MetaDataKeyBox;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class ExportDialog extends JDialog
{
	List<Page> pages;
	
	DocExploreDataLink link;
	
	JRadioButton areaPage, areaRoi;
	JRadioButton areaAllRois, areaTaggedRois;
	TagArea areaRoiTags;
	
	JRadioButton contentPage, contentAnnotation;
	JRadioButton contentAnnotationField, contentAnnotationTag;
	MetaDataKeyBox contentAnnotationFieldName;
	TagArea contentAnnotationTags;
	
	JRadioButton labelNone, labelFile;
	JTextField labelFileName;
	JRadioButton labelTranscription, labelAnnotationField, labelAnnotationTag;
	MetaDataKeyBox labelAnnotationFieldName;
	TagArea labelTranscriptionTags, labelAnnotationTags;
	
	JTextField folderName;
	JComboBox imageFormat;
	JRadioButton fillBlack, fillWhite;
	
	public ExportDialog(DocExploreDataLink link) throws DataLinkException
	{
		super((Frame)null, XMLResourceBundle.getBundledString("exportTitleLabel"), true);
		
		setLayout(new BorderLayout());
		this.pages = null;
		this.link = link;
		
		JPanel mainPanel = new JPanel(new LooseGridLayout(0, 2, 10, 10, true, false));
		//JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JPanel areaPanel = new JPanel(new LooseGridLayout(0, 1, 10, 10, true, false));
		areaPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("exportAreaLabel")));
		areaPanel.add(areaPage = new JRadioButton(XMLResourceBundle.getBundledString("exportAreaPageLabel")));
		areaPanel.add(areaRoi = new JRadioButton(XMLResourceBundle.getBundledString("exportAreaRoiLabel")));
		JPanel areaRoiPanel = new JPanel(new LooseGridLayout(0, 1, 10, 10, true, false));
		//areaRoiPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		areaRoiPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		areaRoiPanel.add(areaAllRois = new JRadioButton(XMLResourceBundle.getBundledString("exportAllRoisLabel")));
		areaRoiPanel.add(areaTaggedRois = new JRadioButton(XMLResourceBundle.getBundledString("exportTaggedRoisLabel")));
		areaTaggedRois.setEnabled(false);
		areaRoiTags = new TagArea();
		//areaRoiTags.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		areaRoiPanel.add(areaRoiTags);
		areaPanel.add(areaRoiPanel);
		mainPanel.add(areaPanel);
		
		JPanel contentPanel = new JPanel(new LooseGridLayout(0, 1, 10, 10, true, false));
		contentPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("exportContentLabel")));
		contentPanel.add(contentPage = new JRadioButton(XMLResourceBundle.getBundledString("exportContentPageLabel")));
		contentPanel.add(contentAnnotation = new JRadioButton(XMLResourceBundle.getBundledString("exportContentAnnotationLabel")));
		JPanel contentAnnotationPanel = new JPanel(new LooseGridLayout(0, 1, 10, 10, true, false));
		//contentAnnotationPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		contentAnnotationPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		JPanel contentFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		contentFieldPanel.add(contentAnnotationField = new JRadioButton(XMLResourceBundle.getBundledString("exportAnnotationFieldNameLabel")));
		contentFieldPanel.add(contentAnnotationFieldName = new MetaDataKeyBox(link, true));
		contentAnnotationPanel.add(contentFieldPanel);
		contentAnnotationPanel.add(contentAnnotationTag = new JRadioButton(XMLResourceBundle.getBundledString("exportAnnotationTagLabel")));
		contentAnnotationTags = new TagArea();
		//contentAnnotationTags.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		contentAnnotationPanel.add(contentAnnotationTags);
		contentPanel.add(contentAnnotationPanel);
		mainPanel.add(contentPanel);
		
		JPanel labelPanel = new JPanel(new LooseGridLayout(0, 1, 10, 10, true, false));
		labelPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("exportLabelLabel")));
		labelPanel.add(labelNone = new JRadioButton(XMLResourceBundle.getBundledString("exportLabelNoneLabel")));
		JPanel labelFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		labelFilePanel.add(labelFile = new JRadioButton(XMLResourceBundle.getBundledString("exportLabelFileLabel")));
		labelFilePanel.add(labelFileName = new JTextField(20));
		labelPanel.add(labelFilePanel);
		JPanel labelSourcePanel = new JPanel(new LooseGridLayout(0, 1, 10, 10, true, false));
		//labelSourcePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		labelSourcePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		labelSourcePanel.add(labelTranscription = new JRadioButton(XMLResourceBundle.getBundledString("exportTranscriptionLabelsLabel")));
		labelTranscriptionTags = new TagArea();
		//labelTranscriptionTags.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		labelSourcePanel.add(labelTranscriptionTags);
		JPanel labelAnnotationFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		labelAnnotationFieldPanel.add(labelAnnotationField = new JRadioButton(XMLResourceBundle.getBundledString("exportAnnotationFieldLabelsLabel")));
		labelAnnotationFieldPanel.add(labelAnnotationFieldName = new MetaDataKeyBox(link, true));
		labelSourcePanel.add(labelAnnotationFieldPanel);
		labelSourcePanel.add(labelAnnotationTag = new JRadioButton(XMLResourceBundle.getBundledString("exportAnnotationTagLabelsLabel")));
		labelAnnotationTags = new TagArea();
		//labelAnnotationTags.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		labelSourcePanel.add(labelAnnotationTags);
		labelPanel.add(labelSourcePanel);
		mainPanel.add(labelPanel);
		
		JPanel generalPanel = new JPanel(new LooseGridLayout(0, 1, 10, 10, true, false));
		generalPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("exportGeneralLabel")));
		JPanel generalFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		generalFolderPanel.add(new JLabel(XMLResourceBundle.getBundledString("exportFolderLabel")));
		generalFolderPanel.add(folderName = new JTextField(30));
		generalFolderPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalBrowseLabel"))
		{
			JFileChooser chooser = new JFileChooser() {public boolean accept(File f) {return f.isDirectory();}};
			public void actionPerformed(ActionEvent arg0)
			{
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (chooser.showOpenDialog(ExportDialog.this) == JFileChooser.APPROVE_OPTION)
				{
					try {folderName.setText(chooser.getSelectedFile().getCanonicalPath());}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					chooser.setCurrentDirectory(chooser.getSelectedFile());
				}
			}
		}));
		generalPanel.add(generalFolderPanel);
		JPanel generalFormatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		generalFormatPanel.add(new JLabel(XMLResourceBundle.getBundledString("exportFormatLabel")));
		generalFormatPanel.add(imageFormat = new JComboBox(new Object [] {"PNG", "JPG"}));
		generalPanel.add(generalFormatPanel);
		JPanel generalFillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		generalFillPanel.add(new JLabel(XMLResourceBundle.getBundledString("exportFillLabel")));
		generalFillPanel.add(fillBlack = new JRadioButton(XMLResourceBundle.getBundledString("exportFillBlackLabel")));
		generalFillPanel.add(fillWhite = new JRadioButton(XMLResourceBundle.getBundledString("exportFillWhiteLabel")));
		generalPanel.add(generalFillPanel);
		mainPanel.add(generalPanel);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("exportExportLabel")) {
			public void actionPerformed(ActionEvent e)
			{
				if (pages == null)
					return;
				
				final JDialog dialog = new JDialog(JOptionPane.getRootFrame(), true);
				dialog.setUndecorated(true);
				dialog.add(new JLabel(XMLResourceBundle.getBundledString("exportExportingLabel")), BorderLayout.NORTH);
				final JProgressBar progress = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
				dialog.add(progress, BorderLayout.SOUTH);
				dialog.pack();
				GuiUtils.centerOnScreen(dialog);
				
				new Thread() {public void run()
				{
					try
					{
						int cnt = 0;
						PageExport export = new PageExport(ExportDialog.this);
						for (Page page : pages)
						{
							progress.setValue(cnt*100/pages.size());
							try {export.exportPage(page);}
							catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
							cnt++;
						}
						export.complete();
					}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
					
					while (!dialog.isVisible())
						try {Thread.sleep(100);}
						catch (Exception e) {}
					dialog.setVisible(false);
				}}.start();
				
				dialog.setVisible(true);
			}}));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCloseLabel")) {
			public void actionPerformed(ActionEvent e)
				{setVisible(false);}}));
		
		add(new JScrollPane(mainPanel), BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		ButtonGroup areaGroup = new ButtonGroup();
		areaGroup.add(areaPage); areaGroup.add(areaRoi);
		ButtonGroup areaRoiGroup = new ButtonGroup();
		areaRoiGroup.add(areaAllRois); areaRoiGroup.add(areaTaggedRois);
		ButtonGroup contentGroup = new ButtonGroup();
		contentGroup.add(contentPage); contentGroup.add(contentAnnotation);
		ButtonGroup contentAnnotationGroup = new ButtonGroup();
		contentAnnotationGroup.add(contentAnnotationField); contentAnnotationGroup.add(contentAnnotationTag);
		ButtonGroup labelGroup = new ButtonGroup();
		labelGroup.add(labelNone); labelGroup.add(labelFile);
		ButtonGroup labelSourceGroup = new ButtonGroup();
		labelSourceGroup.add(labelTranscription); labelSourceGroup.add(labelAnnotationField); labelSourceGroup.add(labelAnnotationTag);
		ButtonGroup fillGroup = new ButtonGroup();
		fillGroup.add(fillBlack); fillGroup.add(fillWhite);
		
		areaPage.setSelected(true);
		areaAllRois.setSelected(true);
		contentPage.setSelected(true);
		contentAnnotationField.setSelected(true);
		labelNone.setSelected(true);
		labelTranscription.setSelected(true);
		fillBlack.setSelected(true);
		
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	 
		GraphicsConfiguration gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gconf);
		int maxWidth = screenSize.width-(screenInsets.left+screenInsets.right),
			maxHeight = screenSize.height-(screenInsets.bottom+screenInsets.top);
		setPreferredSize(new Dimension(Math.min(getWidth(), maxWidth), Math.min(getHeight(), maxHeight)));
		pack();
	}
	
	public void setInput(List<Page> pages)
	{
		this.pages = pages;
	}
}
