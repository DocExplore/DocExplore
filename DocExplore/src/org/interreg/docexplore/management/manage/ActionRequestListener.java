package org.interreg.docexplore.management.manage;

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
	public void onDeleteBooksRequest(final List<Book> books);
	public List<Page> onAppendPagesRequest(Book book, List<File> files);
	public List<MetaData> onAppendPartsRequest(Book book, List<File> files);
	public void onDeletePagesRequest(List<Page> pages);
	public void onDeletePartsRequest(Book book, List<MetaData> parts);
	public void onMovePagesRequest(List<Page> pages, Page moveAfter);
	public void onMovePartsRequest(Book book, MetaData part, int col, int row, boolean insertRow);
	public void onTransposePartsRequest(Book book);
	public void onCropPageRequest(Page page, int tlx, int tly, int brx, int bry);
	public MetaData onAddAnnotationRequest(AnnotatedObject object, MetaData annotation);
	public Region onAddRegionRequest(Page page, Point [] outline);
	public void onDeleteRegionRequest(Region region);
}
