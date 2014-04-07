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
	public DeleteBooksAction deleteBooks(List<Book> books) {return new DeleteBooksAction(books);}
	public DeletePagesAction deletePages(List<Page> pages) {return new DeletePagesAction(pages);}
	public AddBookAction addBook(String title, List<File> files) {return new AddBookAction(link, title, files);}
	public DeleteMetaDataAction deleteMetaData(AnnotatedObject document, MetaData annotation) {return new DeleteMetaDataAction(document, annotation);}
	public DeleteMetaDataAction deleteMetaDatas(AnnotatedObject document, List<MetaData> annotations) {return new DeleteMetaDataAction(document, annotations);}
	public AddMetaDataAction addMetaData(AnnotatedObject document, MetaData annotation) {return new AddMetaDataAction(document, annotation);}
	public AddMetaDataAction addMetaDatas(AnnotatedObject document, List<MetaData> annotations) {return new AddMetaDataAction(document, annotations);}
	public DeleteRegionsAction deleteRegion(Region region) {return new DeleteRegionsAction(region);}
	public DeleteRegionsAction deleteRegions(List<Region> regions) {return new DeleteRegionsAction(regions);}
	public AddRegionsAction addRegion(Page page, Point [] outline) {return new AddRegionsAction(page, outline);}
	public AddRegionsAction addRegions(Page page, List<Point []> outlines) {return new AddRegionsAction(page, outlines);}
	public MovePagesAction movePages(List<Page> pages, Page moveAfter) {return new MovePagesAction(pages, moveAfter);}
	public CropPageAction cropPage(Page page, int tlx, int tly, int brx, int bry) {return new CropPageAction(page, tlx, tly, brx, bry);}
}
