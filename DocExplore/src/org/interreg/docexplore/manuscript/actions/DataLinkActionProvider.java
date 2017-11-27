/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.actions;

import java.awt.Point;
import java.io.File;
import java.util.List;

import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

public class DataLinkActionProvider implements ActionProvider
{
	protected DocExploreDataLink link;
	
	public DataLinkActionProvider(DocExploreDataLink link)
	{
		this.link = link;
	}
	
	public AddPagesAction addPages(Book book, List<File> files) {return new AddPagesAction(link, book, files);}
	public AddPosterPartsAction addParts(Book book, List<File> files) {return new AddPosterPartsAction(link, book, files);}
	public DeleteBooksAction deleteBooks(List<Book> books) {return new DeleteBooksAction(books);}
	public DeletePagesAction deletePages(List<Page> pages) {return new DeletePagesAction(pages);}
	public AddBookAction addBook(String title, List<File> files, boolean poster) {return new AddBookAction(link, title, files, poster);}
	public DeleteMetaDataAction deleteMetaData(AnnotatedObject document, MetaData annotation) {return new DeleteMetaDataAction(document, annotation);}
	public DeleteMetaDataAction deleteMetaDatas(AnnotatedObject document, List<MetaData> annotations) {return new DeleteMetaDataAction(document, annotations);}
	public DeletePosterPartsAction deletePart(Book book, MetaData part) {return new DeletePosterPartsAction(link, book, part);}
	public DeletePosterPartsAction deleteParts(Book book, List<MetaData> parts) {return new DeletePosterPartsAction(link, book, parts);}
	public AddMetaDataAction addMetaData(AnnotatedObject document, MetaData annotation) {return new AddMetaDataAction(document, annotation);}
	public AddMetaDataAction addMetaDatas(AnnotatedObject document, List<MetaData> annotations) {return new AddMetaDataAction(document, annotations);}
	public DeleteRegionsAction deleteRegion(Region region) {return new DeleteRegionsAction(region);}
	public DeleteRegionsAction deleteRegions(List<Region> regions) {return new DeleteRegionsAction(regions);}
	public AddRegionsAction addRegion(Page page, Point [] outline) {return new AddRegionsAction(page, outline);}
	public AddRegionsAction addRegions(Page page, List<Point []> outlines) {return new AddRegionsAction(page, outlines);}
	public MovePagesAction movePages(List<Page> pages, Page moveAfter) {return new MovePagesAction(pages, moveAfter);}
	public CropPageAction cropPage(Page page, int tlx, int tly, int brx, int bry) {return new CropPageAction(page, tlx, tly, brx, bry);}
	public MovePartAction movePart(Book book, MetaData part, int col, int row, boolean insertRow) {return new MovePartAction(link ,book, part, col, row, insertRow);}
}
