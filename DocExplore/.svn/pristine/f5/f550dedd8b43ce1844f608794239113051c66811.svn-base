package org.interreg.docexplore.reader.book;

import java.util.List;
import java.util.Vector;

import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.gfx.Bindable;
import org.interreg.docexplore.reader.gfx.StreamedTexture;
import org.interreg.docexplore.reader.gfx.Texture;
import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.util.ImageUtils;

public class BookSpecification
{
	ReaderApp app;
	public String title;
	double aspectRatio;
	public List<PageSpecification> pages;
	public boolean parchment = false;
	public String miniUri = null;
	public String coverUri = null, innerCoverUri = null;
	public Texture coverTex = null, innerCoverTex = null;

	public BookSpecification(ReaderApp app, String title, double aspectRatio)
	{
		this.app = app;
		this.title = title;
		this.pages = new Vector<PageSpecification>();
		this.aspectRatio = aspectRatio;
	}
	
	public void addPage(PageSpecification page)
	{
		pages.add(page);
	}
	
	public void validate()
	{
		if (!parchment && pages.size()%2 == 1)
		{
			addPage(new PageSpecification(app, pages.size(), null, null, null)
			{
				public Bindable getTexture() {return app.emptyTex;}
				public Bindable getTransTexture() {return app.emptyTex;}
				public boolean isLoaded() {return true;}
			});
		}
	}
	
	public void prepare(ReaderClient client)
	{
		if (coverUri != null) try
		{
			StreamedTexture stream = client.getResource(StreamedTexture.class, coverUri);
			stream.waitUntilComplete();
			coverTex = stream.texture;
			stream.texture = null;
			stream.release();
		}
		catch (Exception e) {e.printStackTrace();}
		
		if (coverTex == null)
			client.app.submitRenderTaskAndWait(new Runnable() {public void run()
			{
				try {coverTex = new Texture(
					ImageUtils.read(Thread.currentThread().getContextClassLoader().getResource(BookSpecification.class.getPackage().getName().replace('.', '/')+"/defaultCover.png")), false);}
				catch (Exception e) {e.printStackTrace();}
			}});
		
		if (innerCoverUri != null) try
		{
			StreamedTexture stream = client.getResource(StreamedTexture.class, innerCoverUri);
			stream.waitUntilComplete();
			innerCoverTex = stream.texture;
			stream.texture = null;
			stream.release();
		}
		catch (Exception e) {e.printStackTrace();}
		
		if (innerCoverTex == null)
			client.app.submitRenderTaskAndWait(new Runnable() {public void run()
			{
				try {innerCoverTex = new Texture(
					ImageUtils.read(Thread.currentThread().getContextClassLoader().getResource(BookSpecification.class.getPackage().getName().replace('.', '/')+"/defaultInnerCover.png")), false);}
				catch (Exception e) {e.printStackTrace();}
			}});
	}
	
	public void releaseAll()
	{
		for (PageSpecification page : pages)
			page.release();
		if (coverTex != null)
		{
			coverTex.dispose();
			coverTex = null;
		}
		if (innerCoverTex != null)
		{
			innerCoverTex.dispose();
			innerCoverTex = null;
		}
	}
}
