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
import java.util.List;

import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

public interface ActionRequestListener
{
	public Book onAddBookRequest(String title, List<File> files, boolean poster);
	public void onDeleteBooksRequest(List<Book> books);
	public List<Page> onAppendPagesRequest(Book book, List<File> files);
	public List<MetaData> onAppendPartsRequest(Book book, List<File> files);
	public void onDeletePagesRequest(List<Page> pages);
	public void onDeletePartsRequest(Book book, List<MetaData> parts);
	public void onMovePagesRequest(List<Page> pages, Page moveAfter);
	public void onMovePartsRequest(Book book, MetaData part, int col, int row, boolean insertRow);
	public void onAddEmptyPartRequest(Book book, int col, int row, boolean insertRow);
//	public void onTransposePartsRequest(Book book);
	public void onFillPosterHolesRequest(Book book);
	public void onHorizontalMirrorPartsRequest(Book book);
	public void onVerticalMirrorPartsRequest(Book book);
	public void onHorizontalMirrorMetaDataRequest(MetaData annotation);
	public void onVerticalMirrorMetaDataRequest(MetaData annotation);
	public void onHorizontalMirrorPageRequest(Page page);
	public void onVerticalMirrorPageRequest(Page page);
	public void onRotatePartsLeftRequest(Book book);
	public void onRotatePartsRightRequest(Book book);
	public void onRotateMetaDataLeftRequest(MetaData annotation);
	public void onRotateMetaDataRightRequest(MetaData annotation);
	public void onRotatePageLeftRequest(Page page);
	public void onRotatePageRightRequest(Page page);
	public void onCropPageRequest(AnnotatedObject object, int tlx, int tly, int brx, int bry);
	public MetaData onAddAnnotationRequest(AnnotatedObject object, MetaData annotation);
	public void onAddRetroactiveAnnotationsRequest(AnnotatedObject object, List<MetaData> annotations);
	public Region onAddRegionRequest(Page page, Point [] outline);
	public void onDeleteRegionRequest(Region region);
}
