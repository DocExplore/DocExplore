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
	public void onRotatePartsLeftRequest(Book book);
	public void onRotatePartsRightRequest(Book book);
	public void onCropPageRequest(AnnotatedObject object, int tlx, int tly, int brx, int bry);
	public MetaData onAddAnnotationRequest(AnnotatedObject object, MetaData annotation);
	public void onAddRetroactiveAnnotationsRequest(AnnotatedObject object, List<MetaData> annotations);
	public Region onAddRegionRequest(Page page, Point [] outline);
	public void onDeleteRegionRequest(Region region);
}
