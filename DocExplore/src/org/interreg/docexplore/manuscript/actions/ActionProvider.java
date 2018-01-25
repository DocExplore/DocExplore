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

import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

public interface ActionProvider
{
	public AddPagesAction addPages(Book book, List<File> files);
	public AddPosterPartsAction addParts(Book book, List<File> files);
	public DeleteBooksAction deleteBooks(List<Book> books);
	public DeletePagesAction deletePages(List<Page> pages);
	public AddBookAction addBook(String title, List<File> files, boolean poster);
	public DeleteMetaDataAction deleteMetaData(AnnotatedObject document, MetaData annotation);
	public DeleteMetaDataAction deleteMetaDatas(AnnotatedObject document, List<MetaData> annotations);
	public DeletePosterPartsAction deletePart(Book book, MetaData part);
	public DeletePosterPartsAction deleteParts(Book book, List<MetaData> parts);
	public AddMetaDataAction addMetaData(AnnotatedObject document, MetaData annotation);
	public AddMetaDataAction addMetaDatas(AnnotatedObject document, List<MetaData> annotations);
	public DeleteRegionsAction deleteRegion(Region region);
	public DeleteRegionsAction deleteRegions(List<Region> regions);
	public AddRegionsAction addRegion(Page page, Point [] outline);
	public AddRegionsAction addRegions(Page page, List<Point []> outlines);
	public MovePagesAction movePages(List<Page> pages, Page moveAfter);
	public MovePartAction movePart(Book book, MetaData part, int col, int row, boolean insertRow);
	public CropPageAction cropPage(AnnotatedObject object, int tlx, int tly, int brx, int bry);
}
