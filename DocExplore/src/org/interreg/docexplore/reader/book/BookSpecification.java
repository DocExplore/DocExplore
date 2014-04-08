/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
