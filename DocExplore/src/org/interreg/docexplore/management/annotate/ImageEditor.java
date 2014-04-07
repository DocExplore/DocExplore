package org.interreg.docexplore.management.annotate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataImageSource;
import org.interreg.docexplore.util.FileImageSource;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageSource;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class ImageEditor extends AnnotationEditor
{
	ImageSource image;
	
	public ImageEditor(AnnotationPanel panel, MetaData annotation) throws DataLinkException
	{
		super(panel, annotation);
		
		image = new MetaDataImageSource(annotation);
	}
	
	void set()
	{
		try
		{
			BufferedImage bimage = image.getImage();
			imagePreview.setIcon(ImageUtils.createIconFromImage(bimage, 128));
			imagePreview.setText("");
			imageField.setText(image.getURI());
		}
		catch (Exception e)
		{
			imagePreview.setIcon(null);
			imagePreview.setText("Nothing to display");
			imageField.setText("");
		}
	}
	
	JLabel imagePreview = null;
	JTextField imageField = null;
	protected void fillExpandedState()
	{
		super.fillExpandedState();
		changed = false;
		
		try
		{
			JPanel imagePanel = new JPanel(new BorderLayout(5, 10));
			imagePanel.setOpaque(false);
			
			imagePanel.add(buildKeyPanel(), BorderLayout.NORTH);
			
			imageField = new JTextField(30);
			imageField.setEditable(false);
			imagePanel.add(imageField, BorderLayout.CENTER);
			JButton browseButton = new JButton(
				XMLResourceBundle.getBundledString("annotateBrowseLabel"));
			imagePreview = new JLabel(XMLResourceBundle.getBundledString("annotateNoDisplayLabel"));
			
			if (image != null)
				set();
			
			browseButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					File file = DocExploreTool.getFileDialogs().openFile(DocExploreTool.getImagesCategory());
					if (file != null)
					{
						try
						{
							FileImageSource image = new FileImageSource(file);
							image.getImage();
							ImageEditor.this.image = image;
							changed = true;
							set();
							validate();
							repaint();
						}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					}
				}
			});
			JPanel browsePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			browsePanel.setOpaque(false);
			if (!readOnly)
				browsePanel.add(browseButton);
			imagePanel.add(browsePanel, BorderLayout.EAST);
			
			imagePreview.addMouseListener(new MouseAdapter() {public void mouseClicked(MouseEvent e)
			{
				if (image == null)
					return;
				try
				{
					final BufferedImage fullImage = image.getImage();
					JDialog imageDialog = new JDialog(panel.handler.win, true);
					JLabel imageLabel = new JLabel(new ImageIcon(fullImage));
					imageDialog.add(new JScrollPane(imageLabel));
					imageDialog.pack();
					GuiUtils.centerOnScreen(imageDialog);
					imageDialog.setVisible(true);
				}
				catch (Exception ex) {}
			}});
			
			imagePreview.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			previewPanel.setOpaque(false);
			previewPanel.add(imagePreview);
			imagePanel.add(previewPanel, BorderLayout.SOUTH);
			imagePanel.setBorder(BorderFactory.createTitledBorder(
				XMLResourceBundle.getBundledString("annotateTypeImage")));
			
			add(imagePanel, BorderLayout.NORTH);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}

	public void writeObject(MetaData object) throws DataLinkException
	{
		annotation.setKey(annotation.getLink().getOrCreateKey(keyName));
		if (image != null && image instanceof FileImageSource)
			object.setValue(MetaData.imageType, image.getFile());
	}
}
