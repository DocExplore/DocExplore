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
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.ExpandingItemList;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.app.EditorSidePanel;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.manuscript.app.editors.EditorHeader;
import org.interreg.docexplore.manuscript.app.editors.GuiConstants;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class MMTAnnotationPanel extends JPanel implements EditorSidePanel
{
	ManuscriptAppHost win;
	AnnotationHandler handler;
	AnnotatedObject document;
	ExpandingItemList annotationList;
	JScrollPane scrollPane;
	ListFilter filter;
	IconButton copy, paste;
	EditorHeader header;
	
	public MMTAnnotationPanel(final ManuscriptAppHost win)
	{
		super(new BorderLayout());
		
		add(this.header = new EditorHeader(), BorderLayout.NORTH);
		header.setTitle(Lang.s("transcriptAvailableLabel"), "");
		header.setTitleIcon(ImageUtils.getIcon("free-64x64.png"));
		
		final DocExploreDataLink link = win.getLink();
		final JPanel mainPanel = new JPanel(new BorderLayout());
		this.handler = new AnnotationHandler(win, win.plugins);
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
		annotationList.setBackground(Color.white);
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(scrollPane, BorderLayout.CENTER);
		
		header.titlePanel.add(win.helpPanel.createHelpMessageButton(Lang.s("helpMmtAnnotationsMsg")));
		header.titlePanel.add(new IconButton("add-24x24.png", Lang.s("annotateAddTooltip"), 
			new ActionListener() {public void actionPerformed(ActionEvent e) {addAnnotation();}}));
		header.titlePanel.add(copy = new IconButton("copy-24x24.png", Lang.s("generalMenuEditCopy"), 
			new ActionListener() {public void actionPerformed(ActionEvent e) {win.clipboard.copy(document); paste.setEnabled(true);}}));
		header.titlePanel.add(paste = new IconButton("paste-24x24.png", Lang.s("generalMenuEditPaste"), 
			new ActionListener() {public void actionPerformed(ActionEvent e)
			{
				try {win.clipboard.paste(document); setDocument(document); MMTAnnotationPanel.this.validate(); MMTAnnotationPanel.this.repaint();}
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
		
		mainPanel.add(listPanel, BorderLayout.CENTER);
		add(new JScrollPane(mainPanel), BorderLayout.CENTER);
	}
	
	@Override public void onShow() {}
	@Override public void onHide()
	{
		annotationList.contractAll();
	}
	
	void removeAnnotation(AnnotationEditor editor)
	{
		try {handler.deleteMetaData(document, editor.annotation);}
		catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	public MetaData addAnnotation()
	{
		try
		{
			MetaData object = handler.createMetaData(document);
			if (object == null)
				return null;
			AnnotationEditor editor = getEditorFor(object);
			if (editor == null)
				return null;
			annotationList.contractAll();
			editor.expand();
			return object;
		}
		catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		return null;
	}
	
	public void setDocument(AnnotatedObject document) throws DataLinkException
	{
		this.document = document;
		annotationList.removeAll();
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : document.getMetaData().entrySet())
			for (MetaData annotation : entry.getValue())
				if (annotation.getKey() != handler.win.getLink().tagKey && filter.matches(annotation))
					addObject(annotation);
		annotationList.validate();
		annotationList.repaint();
	}
	
	Color odd = Color.lightGray, even = Color.gray;
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
		editor.setBackground(annotationList.getComponentCount()%2 == 0 ? GuiConstants.listOddColor : GuiConstants.listEvenColor);
		//editor.setBackground(Color.white);
		//editor.setBorder(BorderFactory.createLineBorder(annotationList.getComponentCount()%2 == 0 ? oddDark : evenDark, 2));
		//editor.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
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

	@Override public Component getComponent() {return this;}
}
