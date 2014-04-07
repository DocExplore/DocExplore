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
	public DeleteBooksAction deleteBooks(List<Book> books);
	public DeletePagesAction deletePages(List<Page> pages);
	public AddBookAction addBook(String title, List<File> files);
	public DeleteMetaDataAction deleteMetaData(AnnotatedObject document, MetaData annotation);
	public DeleteMetaDataAction deleteMetaDatas(AnnotatedObject document, List<MetaData> annotations);
	public AddMetaDataAction addMetaData(AnnotatedObject document, MetaData annotation);
	public AddMetaDataAction addMetaDatas(AnnotatedObject document, List<MetaData> annotations);
	public DeleteRegionsAction deleteRegion(Region region);
	public DeleteRegionsAction deleteRegions(List<Region> regions);
	public AddRegionsAction addRegion(Page page, Point [] outline);
	public AddRegionsAction addRegions(Page page, List<Point []> outlines);
	public MovePagesAction movePages(List<Page> pages, Page moveAfter);
	public CropPageAction cropPage(Page page, int tlx, int tly, int brx, int bry);
}
