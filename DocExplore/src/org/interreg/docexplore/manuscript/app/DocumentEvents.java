/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.app;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

public enum DocumentEvents
{
	collectionChanged,
	bookAdded,
	bookDeleted,
	bookChanged,
	pageAdded,
	pageDeleted,
	pageChanged,
	regionAdded,
	regionDeleted,
	regionChanged,
	metadataAdded,
	metadataDeleted,
	metadataChanged,
	
	addPages,
	deletePages,
	addParts,
	deleteParts,
	addRegion,
	deleteRegion;
	
	public final String event;
	public final boolean isChangeEvent;
	
	private DocumentEvents()
	{
		StringBuffer s = new StringBuffer();
		for (int i=0;i<name().length();i++)
			if (Character.isUpperCase(name().charAt(i)))
				s.append('-').append(Character.toLowerCase(name().charAt(i)));
			else s.append(name().charAt(i));
		this.event = s.toString();
		this.isChangeEvent = name().endsWith("ed");
	}
	
	private static HashMap<String, DocumentEvents> events = new HashMap<String, DocumentEvents>();
	static
	{
		for (int i=0;i<values().length;i++)
			events.put(values()[i].event, values()[i]);
	}
	
	private static List<DocumentPanel> openDocuments = new ArrayList<DocumentPanel>();
	@SuppressWarnings("unchecked")
	public static void process(ManuscriptAppHost host, String action, Object param)
	{
		DocumentEvents event = events.get(action);
		boolean sendToActiveDocument = true;
		if (event != null)
		{
			sendToActiveDocument = false;
			if (event.isChangeEvent)
			{
				host.getDocuments(openDocuments);
				for (int i=0;i<openDocuments.size();i++)
					openDocuments.get(i).onActionRequest(action, param);
				openDocuments.clear();
				
				if (event == metadataAdded || event == metadataDeleted || event == metadataChanged) try
				{
					MetaData annotation = (MetaData)param;
					String bookId = annotation.getMetaDataString(host.getLink().bookKey);
					if (bookId != null)
						host.broadcastAction(DocumentEvents.bookChanged.event, host.getLink().getBook(Integer.parseInt(bookId)));
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
			else if (event == DocumentEvents.deletePages && param != null)
				host.getActionRequestListener().onDeletePagesRequest((List<Page>)param);
			else if (event == DocumentEvents.addPages && param != null)
				host.getActionRequestListener().onAppendPagesRequest((Book)((Object [])param)[0], (List<File>)((Object [])param)[1]);
			else if (event == DocumentEvents.deleteParts && param != null)
				host.getActionRequestListener().onDeletePartsRequest((Book)((Object [])param)[0], (List<MetaData>)((Object [])param)[1]);
			else if (event == DocumentEvents.addParts && param != null)
				host.getActionRequestListener().onAppendPartsRequest((Book)((Object [])param)[0], (List<File>)((Object [])param)[1]);
			else if (event == DocumentEvents.deleteRegion && param != null)
				host.getActionRequestListener().onDeleteRegionRequest((Region)param);
			else if (event == DocumentEvents.addRegion && param != null)
				host.getActionRequestListener().onAddRegionRequest((Page)((Object [])param)[0], (Point [])((Object [])param)[1]);
			else sendToActiveDocument = true;
		}
		if (sendToActiveDocument)
		{
			DocumentPanel panel = host.getActiveDocument();
			if (panel != null)
				panel.onActionRequest(action, param);
		}
	}
	
	public static void broadcastChanged(ManuscriptAppHost host, AnnotatedObject object)
	{
		if (object instanceof Book) host.broadcastAction(bookChanged.event, object);
		else if (object instanceof Page) host.broadcastAction(pageChanged.event, object);
		else if (object instanceof Region) host.broadcastAction(regionChanged.event, object);
		else if (object instanceof MetaData) host.broadcastAction(metadataChanged.event, object);
	}
}
