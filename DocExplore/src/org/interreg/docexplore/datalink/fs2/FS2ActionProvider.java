/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.datalink.fs2;

import java.awt.Point;
import java.io.File;
import java.util.List;

import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.ActionProvider;
import org.interreg.docexplore.manuscript.actions.AddBookAction;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;
import org.interreg.docexplore.manuscript.actions.AddPagesAction;
import org.interreg.docexplore.manuscript.actions.AddRegionsAction;
import org.interreg.docexplore.manuscript.actions.CropPageAction;
import org.interreg.docexplore.manuscript.actions.DeleteBooksAction;
import org.interreg.docexplore.manuscript.actions.DeleteMetaDataAction;
import org.interreg.docexplore.manuscript.actions.DeletePagesAction;
import org.interreg.docexplore.manuscript.actions.DeleteRegionsAction;
import org.interreg.docexplore.manuscript.actions.MovePagesAction;

public class FS2ActionProvider implements ActionProvider
{
	DocExploreDataLink link;
	
	public FS2ActionProvider(DocExploreDataLink link)// throws DataLinkException
	{
		if (link.getLink() == null || !(link.getLink() instanceof DataLinkFS2))
			throw new RuntimeException("Action provider requires FS2 DataLink!");
		this.link = link;
	}

	public AddPagesAction addPages(Book book, List<File> files) {return new AddFS2PagesAction(link, book, files);}
	public DeleteBooksAction deleteBooks(List<Book> books) {return new DeleteFS2BooksAction(link, books);}
	public DeletePagesAction deletePages(List<Page> pages) {return new DeleteFS2PagesAction(link, pages);}
	public AddBookAction addBook(String title, List<File> files) {return new AddFS2BookAction(link, title, files);}
	public DeleteMetaDataAction deleteMetaData(AnnotatedObject document, MetaData annotation)
		{return new DeleteFS2MetaDataAction((DataLinkFS2)link.getLink(), document, annotation);}
	public DeleteMetaDataAction deleteMetaDatas(AnnotatedObject document, List<MetaData> annotations)
		{return new DeleteFS2MetaDataAction((DataLinkFS2)link.getLink(), document, annotations);}
	public AddMetaDataAction addMetaData(AnnotatedObject document, MetaData annotation)
		{return new AddFS2MetaDataAction((DataLinkFS2)link.getLink(), document, annotation);}
	public AddMetaDataAction addMetaDatas(AnnotatedObject document, List<MetaData> annotations)
		{return new AddFS2MetaDataAction((DataLinkFS2)link.getLink(), document, annotations);}
	public DeleteRegionsAction deleteRegion(Region region) {return new DeleteFS2RegionsAction((DataLinkFS2)link.getLink(), region);}
	public DeleteRegionsAction deleteRegions(List<Region> regions) {return new DeleteFS2RegionsAction((DataLinkFS2)link.getLink(), regions);}
	public AddRegionsAction addRegion(Page page, Point [] outline) {return new AddFS2RegionsAction((DataLinkFS2)link.getLink(), page, outline);}
	public AddRegionsAction addRegions(Page page, List<Point []> outlines) {return new AddFS2RegionsAction((DataLinkFS2)link.getLink(), page, outlines);}
	public MovePagesAction movePages(List<Page> pages, Page moveAfter) {return new MovePagesAction(pages, moveAfter);}
	public CropPageAction cropPage(Page page, int tlx, int tly, int brx, int bry) {return new CropPageAction(page, tlx, tly, brx, bry);}
}
