/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.authoring.BookImporter;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class MetaDataEditor extends JPanel
{
	public SlideEditor pageEditor;
	List<InfoElement> elements;
	public AnnotatedObject document;
	JPanel elementPanel;
	JScrollPane scrollPane;
	JButton paste;
	
	int monitoredRank = -1;
	
	public static int preferredWidth = 512;
	static ROIPreview previewDialog = new ROIPreview(preferredWidth);
	
	public MetaDataEditor(final SlideEditor pageEditor)
	{
		super(new BorderLayout());
		
		this.pageEditor = pageEditor;
		this.elements = new Vector<InfoElement>();
		
		JPanel toolbar = new JPanel(new WrapLayout(FlowLayout.LEFT));
		//toolbar.add(new JLabel(XMLResourceBundle.getBundledString("generalAddAnnotation")));
		
		JPanel addButtons = new JPanel(new WrapLayout(FlowLayout.LEFT));
		addButtons.setBorder(BorderFactory.createTitledBorder(Lang.s("generalAddAnnotation")));
		JButton addText = new JButton(new AbstractAction("", ImageUtils.getIcon("free-32x32.png")) {public void actionPerformed(ActionEvent e)
		{
			try
			{
				final AnnotatedObject document = MetaDataEditor.this.document;
				final MetaData newMd = new MetaData(document.getLink(), BookImporter.getDisplayKey(document.getLink()), ""); 
				final int maxRank = BookImporter.getHighestRank(document);
				final int insertRank = monitoredRank >= 0 && monitoredRank < maxRank ? monitoredRank+1 : maxRank+1;
				AddMetaDataAction action = pageEditor.getHost().getAppHost().getLink().actionProvider().addMetaData(document, newMd);
				pageEditor.getHost().getAppHost().historyManager.submit(new WrappedAction(action)
				{
					public void doAction() throws Exception {BookImporter.insert(newMd, document, insertRank); monitoredRank = insertRank; reload();}
					public void undoAction() throws Exception {BookImporter.remove(newMd, document); monitoredRank = insertRank-1; reload();}
				});
			}
			catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
		addText.setToolTipText(Lang.s("annotationText"));
		addButtons.add(addText);
		
		JButton addImage = new JButton(new AbstractAction("", ImageUtils.getIcon("image-32x32.png")) {public void actionPerformed(ActionEvent e)
		{
			File [] files = DocExploreTool.getFileDialogs().openFiles(DocExploreTool.getImagesCategory());
			if (files == null)
				return;
			try {addFiles(files);}
			catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
		addImage.setToolTipText(Lang.s("annotationImage"));
		addButtons.add(addImage);
		
		if (pageEditor.getHost().getAppHost().plugins.hasPluginForMetaDataType("vid"))
		{
			JButton addMedia = new JButton(new AbstractAction("", ImageUtils.getIcon("video-32x32.png")) {public void actionPerformed(ActionEvent e)
			{
				MetaDataPlugin plugin = pageEditor.getHost().getAppHost().plugins.getPluginForMetaDataType("vid");
				Collection<File> files = plugin.openFiles(true);
				if (files != null)
					try {addFiles(files.toArray(new File [0]));}
					catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}});
			addMedia.setToolTipText(Lang.s("annotationMedia"));
			addButtons.add(addMedia);
		}
		
		toolbar.add(addButtons);
		
		JPanel previewButtons = new JPanel(new WrapLayout(FlowLayout.LEFT));
		previewButtons.setBorder(BorderFactory.createTitledBorder(Lang.s("generalPreview")));
		JButton preview = new JButton(new AbstractAction("", ImageUtils.getIcon("preview-32x32.png")) {public void actionPerformed(ActionEvent e)
		{
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				float [] progress = {0};
				public void run()
				{
					try
					{
						previewDialog.set(pageEditor.getPage().getImage().getImage(), ((Region)document).getOutline(), elements, progress);
						previewDialog.setVisible(true);
					}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				public float getProgress() {return progress[0];}
			}, MetaDataEditor.this);
		}});
		previewButtons.add(preview);
		toolbar.add(previewButtons);
		
		JPanel copyButtons = new JPanel(new WrapLayout(FlowLayout.LEFT));
		copyButtons.setBorder(BorderFactory.createTitledBorder(Lang.s("generalCopyPaste")));
		JButton copy = new JButton(ImageUtils.getIcon("copy-24x24.png"));
		paste = new JButton(ImageUtils.getIcon("paste-24x24.png"));
		copy.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			if (document == null)
				return;
			try
			{
				//pageEditor.getHost().getAppHost().clipboard.copyMetaData(document);
				paste.setEnabled(true);
			}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
		paste.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
//			if (document == null || !pageEditor.getHost().getAppHost().clipboard.canPaste())
//				return;
			try
			{
//				List<MetaData> annotations = pageEditor.getHost().getAppHost().clipboard.pasteMetaData(document);
//				if (!annotations.isEmpty())
//				{
//					pageEditor.view.explorer.metaDataImported((Region)document, annotations);
//					reload();
//				}
			}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
		paste.setEnabled(false);
		copyButtons.add(copy);
		copyButtons.add(paste);
		toolbar.add(copyButtons);
		
		this.elementPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.CENTER, SwingConstants.BOTTOM, false, false));
		elementPanel.setOpaque(true);
		elementPanel.setBackground(Color.black);
		elementPanel.addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e)
		{
			for (InfoElement element : elements)
				if (element instanceof TextElement)
					((TextElement)element).updateSize();
		}});
		
		this.scrollPane = new JScrollPane(elementPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(15);
		add(scrollPane, BorderLayout.CENTER);
		add(toolbar, BorderLayout.SOUTH);
	}
	
	void metaDataImported(List<MetaData> annotations) throws Exception
	{
		if (!annotations.isEmpty())
		{
			metaDataImported((Region)document, annotations);
			reload();
		}
	}
	void addFiles(File [] files) throws Exception
	{
		List<MetaData> annotations = MetaDataUtils.importFiles(pageEditor.getHost().getAppHost().plugins.metaDataPlugins, (Region)document, files);
		metaDataImported(annotations);
	}
//	void copyAnnotations(Region region) throws Exception
//	{
//		List<MetaData> annotations = MetaDataUtils.copyMetaData((Region)document, region);
//		metaDataImported(annotations);
//	}
	
	public void reload() throws Exception {setDocument(document);}
	
	public void setDocument(AnnotatedObject document) throws Exception
	{
		if (!(document instanceof Region))
			document = null;
		//paste.setEnabled(pageEditor.getHost().getAppHost().clipboard.canPaste());
		
		if (this.document != document)
			monitoredRank = -1;
		this.document = document;
		
		for (InfoElement element : elements)
			element.dispose();
		elements.clear();
		
		Map<Integer, MetaData> mds = new TreeMap<Integer, MetaData>();
		if (document != null)
			for (Map.Entry<MetaDataKey, List<MetaData>> entry : document.getMetaData().entrySet())
				for (MetaData md : entry.getValue())
				{
					if (md.getType().equals(MetaData.textType) || md.getType().equals(MetaData.imageType) || 
						pageEditor.getHost().getAppHost().plugins.hasPluginForMetaDataType(md.getType()))
							mds.put(BookImporter.getRank(md), md);
					else System.out.println("discarded "+md.getType());
				}
		
		final InfoElement [] monitoredElement = {null};
		for (Map.Entry<Integer, MetaData> entry : mds.entrySet()) try
		{
			MetaData md = entry.getValue();
			final InfoElement element = md.getType().equals(MetaData.textType) ? new TextElement(this, preferredWidth, md) :
				md.getType().equals(MetaData.imageType) ? new ImageElement(this, preferredWidth, md) : 
				createInfoElement(this, md, preferredWidth);
			if (element == null)
				throw new NullPointerException("Couldn't create info element for "+md.getType()+" "+md.getCanonicalUri());
			if (entry.getKey() == monitoredRank)
				monitoredElement[0] = element;
			elements.add(element);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		monitoredRank = -1;
		
		SwingUtilities.invokeLater(new Runnable() {public void run()
		{
			elementPanel.removeAll();
			for (InfoElement element : elements)
				elementPanel.add(element);
			
//			elementPanel.getParent().validate();
//			elementPanel.getParent().repaint();
			scrollPane.validate();
			scrollPane.repaint();
			
			if (monitoredElement[0] != null)
			{
				final InfoElement element = monitoredElement[0];
				scrollPane.getViewport().setViewPosition(new Point(0, element.getBounds().y));
				if (element instanceof TextElement)
					((TextElement)element).textPane.requestFocus();
			}
		}});
	}
	
	public InfoElement createInfoElement(MetaDataEditor editor, MetaData md, int width) throws DataLinkException
	{
		for (MetaDataPlugin plugin : pageEditor.getHost().getAppHost().plugins.metaDataPlugins)
			if (plugin.getType().equals(md.getType()))
				return plugin.createInfoElement(editor, md, width);
		return null;
	}
	
	public void metaDataImported(final Region region, Collection<MetaData> annotations)
	{
		try
		{
			final AddMetaDataAction action = pageEditor.getHost().getAppHost().getLink().actionProvider().addMetaDatas(region, new LinkedList<MetaData>());
			pageEditor.getHost().getAppHost().historyManager.submit(new WrappedAction(action)
			{
				public void doAction() throws Exception
				{
					action.cacheDir = cacheDir;
					if (action.annotations.isEmpty()) 
						return; 
					super.doAction();
					reload();
				}
				public void undoAction() throws Exception
				{
					super.undoAction();
					reload();
				}
			});
			action.annotations.addAll(annotations);
		}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
}
