package org.interreg.docexplore.management.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.ByteImageSource;
import org.interreg.docexplore.util.ImageUtils;

public class RedactHack
{
	public static void redact(DocExploreDataLink link) throws Exception
	{
		for (int bookId : link.getLink().getAllBookIds())
		{
			Book book = link.getBook(bookId);
			int lastPage = book.getLastPageNumber();
			System.out.print(bookId+" : ");
			for (int i=1;i<=lastPage;i++)
			{
				Page page = book.getPage(i);
				Dimension dim = DocExploreDataLink.getImageDimension(page);
				BufferedImage redacted = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_3BYTE_BGR);
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				ImageUtils.write(redacted, "PNG", output);
				page.setImage(new ByteImageSource(output.toByteArray()));
				output.close();
				page.unloadImage();
				System.out.print(i+" ");
			}
			System.out.println();
		}
	}
}
