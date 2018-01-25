/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.process;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

public class MetaDataExport
{
	public static void export(final ProcessDialog dialog, final int pageNum) throws DataLinkException
	{
		final MetaDataKey key = MetaDataKeyBox.showDialog((DocExploreDataLink)dialog.pages.get(0).getLink(), 
			Lang.s("processFieldNameTitle"), 
			Lang.s("processFieldNameLabel"), true);
		if (key == null)
			return;
		
		final JDialog progressDialog = new JDialog(dialog, true);
		progressDialog.setUndecorated(true);
		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.add(new JLabel("Processing..."), BorderLayout.NORTH);
		innerPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.black, 2), 
			BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		progressDialog.add(innerPanel, BorderLayout.CENTER);
		progressDialog.pack();
		GuiUtils.centerOnScreen(progressDialog);
		
		new Thread()
		{
			public void run()
			{
				dialog.setCurrentPage(pageNum);
				dialog.viewPanel.updatePreview(dialog.filterPanel);
				saveInMetaData(dialog.pages.get(pageNum), dialog.viewPanel.filtered, key, null);
				dialog.repaint();
				try {Thread.sleep(100);}
				catch (InterruptedException e) {}
				
				while (!progressDialog.isVisible())
					try {Thread.sleep(100);}
					catch (InterruptedException e) {}
				progressDialog.setVisible(false);
			}
		}.start();
		
		progressDialog.setVisible(true);
	}
	
	public static void exportAll(final ProcessDialog dialog) throws DataLinkException
	{
		final MetaDataKey key = MetaDataKeyBox.showDialog((DocExploreDataLink)dialog.pages.get(0).getLink(), 
			Lang.s("processFieldNameTitle"), 
			Lang.s("processFieldNameLabel"), true);
		if (key == null)
			return;
		
		final JDialog progressDialog = new JDialog(dialog, true);
		progressDialog.setUndecorated(true);
		final JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.add(new JLabel("Processing..."), BorderLayout.NORTH);
		innerPanel.add(progressBar, BorderLayout.SOUTH);
		innerPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.black, 2), 
			BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		progressDialog.add(innerPanel, BorderLayout.CENTER);
		progressDialog.pack();
		GuiUtils.centerOnScreen(progressDialog);
		
		progressBar.setValue(0);
		new Thread()
		{
			public void run()
			{
				for (int i=0;i<dialog.pages.size();i++)
				{
					dialog.setCurrentPage(i);
					dialog.viewPanel.updatePreview(dialog.filterPanel);
					saveInMetaData(dialog.pages.get(i), dialog.viewPanel.filtered, key, null);
					progressBar.setValue((i+1)*100/dialog.pages.size());
					dialog.repaint();
					try {Thread.sleep(100);}
					catch (InterruptedException e) {}
				}
				while (!progressDialog.isVisible())
					try {Thread.sleep(100);}
					catch (InterruptedException e) {}
				progressDialog.setVisible(false);
			}
		}.start();
		
		progressDialog.setVisible(true);
	}
	
	
	
	public static void saveInMetaData(Page page, BufferedImage image, MetaDataKey key, List<String> tags)
	{
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageUtils.write(image, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());
			
			MetaData md = new MetaData(page.getLink(), key, MetaData.imageType, is);
			page.addMetaData(md);
			
			/*if (tags!=null && !tags.isEmpty())
			{
				DocExploreDataLink link = (DocExploreDataLink)page.getLink();
				for (String tag : tags)
				{
					
				}
			}*/
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
}
