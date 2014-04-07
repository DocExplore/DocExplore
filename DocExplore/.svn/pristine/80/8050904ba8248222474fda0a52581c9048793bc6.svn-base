package org.interreg.docexplore.management.annotate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.ExpandingItemList;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;

@SuppressWarnings("serial")
public class AnnotationPanel extends JPanel
{
	AnnotationHandler handler;
	AnnotatedObject document;
	ExpandingItemList annotationList;
	JScrollPane scrollPane;
	ListFilter filter;
	IconButton copy, paste;
	
	public AnnotationPanel(final MainWindow win)
	{
		super(new BorderLayout());
		
		final DocExploreDataLink link = win.getDocExploreLink();
		final JPanel mainPanel = new JPanel(new BorderLayout());
		this.handler = new AnnotationHandler(win);
		this.annotationList = new ExpandingItemList();
		this.filter = new ListFilter(link);
		filter.addListener(new ListFilter.Listener() {@Override public void filterChanged(ListFilter filter)
		{
			try {setDocument(document);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			validate();
			repaint();
		}});
		
		this.scrollPane = new JScrollPane(annotationList);
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(scrollPane, BorderLayout.CENTER);
		listPanel.setBorder(BorderFactory.createTitledBorder(
			XMLResourceBundle.getBundledString("transcriptAvailableLabel")));
		
		JPanel listButPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
		listButPanel.add(new IconButton("add-24x24.png", XMLResourceBundle.getBundledString("annotateAddTooltip"), 
			new ActionListener() {public void actionPerformed(ActionEvent e) {addAnnotation();}}));
		listButPanel.add(copy = new IconButton("copy-24x24.png", XMLResourceBundle.getBundledString("generalMenuEditCopy"), 
			new ActionListener() {public void actionPerformed(ActionEvent e) {win.clipboard.copy(document); paste.setEnabled(true);}}));
		listButPanel.add(paste = new IconButton("paste-24x24.png", XMLResourceBundle.getBundledString("generalMenuEditPaste"), 
			new ActionListener() {public void actionPerformed(ActionEvent e)
			{
				try {win.clipboard.paste(document); setDocument(document); AnnotationPanel.this.validate(); AnnotationPanel.this.repaint();}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}}));
		paste.setEnabled(win.clipboard.hasCopiedId());
//		final IconToggleButton filterButton = new IconToggleButton("gears-24x24.png", XMLResourceBundle.getBundledString("annotateFilterTooltip"));
//		filterButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
//		{
//			if (filterButton.isSelected())
//			{
//				filter.refreshKeys(link);
//				mainPanel.add(filter, BorderLayout.SOUTH);
//			}
//			else mainPanel.remove(filter);
//			mainPanel.validate();
//		}});
//		listButPanel.add(filterButton);
		
		listPanel.add(listButPanel, BorderLayout.SOUTH);
		mainPanel.add(listPanel, BorderLayout.CENTER);
		add(new JScrollPane(mainPanel), BorderLayout.CENTER);
	}
	
	public void contractAllAnnotations()
	{
		annotationList.contractAll();
	}
	
	void removeAnnotation(AnnotationEditor editor)
	{
		try {handler.deleteMetaData(document, editor.annotation);}
		catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	public void addAnnotation()
	{
		try
		{
			MetaData object = handler.createMetaData(document);
			if (object == null)
				return;
			AnnotationEditor editor = getEditorFor(object);
			if (editor == null)
				return;
			annotationList.contractAll();
			editor.expand();
		}
		catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	
	public void setDocument(AnnotatedObject document) throws DataLinkException
	{
		this.document = document;
		annotationList.removeAll();
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : document.getMetaData().entrySet())
			for (MetaData annotation : entry.getValue())
				if (annotation.getKey() != handler.link.tagKey && filter.matches(annotation))
					addObject(annotation);
		annotationList.validate();
		annotationList.repaint();
	}
	
	Color odd = new Color(1f, .95f, .95f), even = new Color(.95f, .95f, 1f);
	Color oddDark = new Color(.75f, .45f, .45f), evenDark = new Color(.45f, .45f, .75f);
	/**
	 * Adds a meta data object to the list.
	 * @param object A meta data object.
	 */
	public AnnotationEditor addObject(MetaData object) throws DataLinkException
	{
		AnnotationEditor editor = handler.getEditorFor(this, object);
		if (editor == null)
			return null;
		editor.setOpaque(true);
		editor.setBackground(annotationList.getComponentCount()%2 == 0 ? odd : even);
		editor.setBorder(BorderFactory.createLineBorder(annotationList.getComponentCount()%2 == 0 ? oddDark : evenDark, 2));
		annotationList.addItem(editor);
		return editor;
	}
	public AnnotationEditor getEditorFor(MetaData object)
	{
		for (Component comp : annotationList.getComponents())
			if (comp instanceof AnnotationEditor && ((AnnotationEditor)comp).annotation == object)
				return (AnnotationEditor)comp;
		return null;
	}
}
