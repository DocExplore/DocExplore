package org.interreg.docexplore.management.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.gui.DocumentEditorHost;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class PosterPageEditor extends PageEditor
{
	enum TileState {Missing, Loading, Loaded};
	
	long posterId = 0;
	boolean isPoster;
	Page tilePage = null;
	
	MetaData [][] parts;
	TileState [][] tileStates;
	BufferedImage [][] tiles;
	int [] widths, heights;
	int fullWidth, fullHeight;
	
	public PosterPageEditor(DocumentEditorHost listener, Page page) throws Exception
	{
		this(listener, page, null);
	}
	public PosterPageEditor(DocumentEditorHost listener, Region region) throws Exception
	{
		this(listener, region.getPage(), region);
	}
	protected PosterPageEditor(DocumentEditorHost listener, Page page, Region region) throws Exception
	{
		super(listener, page, region);
		this.isPoster = PosterUtils.isPoster(page.getBook());
		updateTileStates();
	}
	
	private synchronized void updateTileStates()
	{
		boolean doUpdate = false;
		if (!isPoster && tilePage != null)
			clearTileStates();
		else if (isPoster && page != tilePage)
		{
			clearTileStates();
			doUpdate = true;
		}
		if (doUpdate) try
		{
			this.tilePage = page;
			Book book = tilePage.getBook();
			parts = PosterUtils.getPosterPartsArray(host.getLink(), book);
			tileStates = new TileState [parts.length][parts.length == 0 ? 0 : parts[0].length];
			tiles = new BufferedImage [parts.length][parts.length == 0 ? 0 : parts[0].length];
			widths = new int [parts.length];
			heights = new int [parts.length == 0 ? 0 : parts[0].length];
			for (int i=0;i<widths.length;i++) 
				widths[i] = -1;
			for (int i=0;i<heights.length;i++) 
				heights[i] = -1;
			for (int i=0;i<tileStates.length;i++)
				for (int j=0;j<tileStates[0].length;j++)
			{
				tileStates[i][j] = TileState.Missing;
				tiles[i][j] = null;
				int w = 0, h = 0;
				if (parts[i][j] != null)
				{
					String [] dim = parts[i][j].getMetaDataString(host.getLink().dimKey).split(",");
					w = Integer.parseInt(dim[0]);
					h = Integer.parseInt(dim[1]);
				}
				if (widths[i] < 0 || w < widths[i])
					widths[i] = w;
				if (heights[j] < 0 || h < heights[j])
					heights[j] = h;
			}
			fullWidth = fullHeight = 0;
			for (int i=0;i<widths.length;i++) 
				fullWidth += widths[i];
			for (int i=0;i<heights.length;i++) 
				fullHeight += heights[i];
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
	}
	private void clearTileStates()
	{
		posterId++;
		tiles = null;
		tilePage = null;
		parts = null;
		widths = null;
		heights = null;
	}
	
	static class TileRendering
	{
		BufferedImage image;
		int x0, y0, x1, y1;
		TileRendering() {}
		void set(BufferedImage image, int x0, int y0, int x1, int y1) {this.image = image; this.x0 = x0; this.y0 = y0; this.x1 = x1; this.y1 = y1;}
	}
	List<TileRendering> renderings = new ArrayList<TileRendering>();
	private int tilei(int x) {int i = 0; for (;i<widths.length && x>0;i++) x -= widths[i]; return Math.max(0, i-1);}
	private int tilej(int y) {int i = 0; for (;i<heights.length && y>0;i++) y -= heights[i]; return Math.max(0, i-1);}
	private int tilex(int i) {int x = 0; for (;i-1>=0;i--) x += widths[i-1]; return x;}
	private int tiley(int j) {int y = 0; for (;j-1>=0;j--) y += heights[j-1]; return y;}
	private synchronized int requestArea(int vx0, int vy0, int vx1, int vy1, double pixelSize, List<TileRendering> renderings)
	{
		int iw = image.getWidth(), ih = image.getHeight();
		int x0 = vx0*fullWidth/iw, y0 = vy0*fullHeight/ih;
		int x1 = vx1*fullWidth/iw, y1 = vy1*fullHeight/ih;
		int i0 = tilei(x0), i1 = tilei(x1), j0 = tilej(y0), j1 = tilej(y1);
		int n = 0;
		boolean useTiles = pixelSize > 1;//iw*pixelSize/fullWidth > .5;
		
		for (int i=0;i<tiles.length;i++)
			for (int j=0;j<tiles[0].length;j++)
				if (parts[i][j] != null)
		{
			if (i>=i0 && i<=i1 && j>=j0 && j<=j1 && useTiles)
			{
				if (tileStates[i][j] == TileState.Missing)
					requestTile(i, j);
				else if (tileStates[i][j] == TileState.Loaded)
				{
					if (renderings.size() <= n)
						renderings.add(new TileRendering());
					int x = tilex(i), y = tiley(j);
					renderings.get(n++).set(tiles[i][j], x*iw/fullWidth, y*ih/fullHeight, (x+widths[i])*iw/fullWidth, (y+heights[j])*ih/fullHeight);
				}
			}
			else
			{
				tileStates[i][j] = TileState.Missing;
				tiles[i][j] = null;
			}
		}
		return n;
	}
	private synchronized InputStream getTileSource(int i, int j, long posterId) throws DataLinkException
	{
		return posterId == this.posterId && tileStates[i][j] == TileState.Loading ? parts[i][j].getValue() : null;
	}
	private synchronized void setTile(int i, int j, long posterId, BufferedImage image)
	{
		if (posterId != this.posterId || tileStates[i][j] != TileState.Loading)
			return;
		tiles[i][j] = image;
		tileStates[i][j] = image == null ? TileState.Missing : TileState.Loaded;
		repaint();
	}
	private void requestTile(final int i, final int j)
	{
		final long posterId = this.posterId;
		tileStates[i][j] = TileState.Loading;
		tileLoader.submit(new Callable<Void>() {@Override public Void call() throws Exception
		{
			InputStream in = null;
			try {in = getTileSource(i, j, posterId);}
			catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e, true);}
			
			BufferedImage image = null;
			if (in != null)
				try {image = ImageUtils.read(in);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			
			setTile(i, j, posterId, image);
			return null;
		}});
	}
	
	@Override public void switchDocument(AnnotatedObject document) throws Exception
	{
		super.switchDocument(document);
		this.isPoster = PosterUtils.isPoster(page.getBook());
		updateTileStates();
	}

	@Override protected void drawOverlay(Graphics2D g, double pixelSize)
	{
		if (isPoster)
		{
			int n = requestArea((int)toViewX(0), (int)toViewY(0), (int)toViewX(getWidth()), (int)toViewY(getHeight()), pixelSize, renderings);
			for (int i=0;i<n;i++)
			{
				TileRendering r = renderings.get(i);
				g.drawImage(r.image, r.x0, r.y0, r.x1-r.x0, r.y1-r.y0, null);
				r.image = null;
			}
		}
		super.drawOverlay(g, pixelSize);
	}
	
	private static ExecutorService tileLoader = new ForkJoinPool(Math.max(2, Runtime.getRuntime().availableProcessors()-1));
	
}
