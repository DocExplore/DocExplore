package org.interreg.docexplore.management.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.gui.DocumentEditorHost;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.TileConfiguration;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class PosterPageEditor extends PageEditor
{
	enum TileState {Missing, Loading, Loaded};
	
	long posterId = 0;
	boolean isPoster;
	Page tilePage = null;
	
	TileConfiguration config;
	MetaData [][][] parts;
	TileState [][][] tileStates;
	BufferedImage [][][] tiles;
	
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
		this.isPoster = page != null && PosterUtils.isPoster(page.getBook());
		updateTileStates();
	}
	
	@Override public int getImageWidth() {return config == null ? super.getImageWidth() : config.getFullWidth();}
	@Override public int getImageHeight() {return config == null ? super.getImageHeight() : config.getFullHeight();}
	
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
			List<MetaData> configs = page.getBook().getMetaDataListForKey(host.getLink().tileConfigKey);
			config = configs.size() > 0 ? (TileConfiguration)new ObjectInputStream(configs.get(0).getValue()).readObject() : null;
			
			if (config != null)
			{
				tilePage = page;
				parts = config.getTiles(host.getLink());
				tileStates = new TileState [parts.length][][];
				tiles = new BufferedImage [parts.length][][];
				for (int l=0;l<parts.length;l++)
				{
					tileStates[l] = new TileState [parts[l].length][parts[l].length == 0 ? 0 : parts[l][0].length];
					tiles[l] = new BufferedImage [parts[l].length][parts[l].length == 0 ? 0 : parts[l][0].length];
				}
				for (int l=0;l<tileStates.length;l++)
					for (int i=0;i<tileStates[l].length;i++)
						for (int j=0;j<tileStates[l][i].length;j++)
							tileStates[l][i][j] = TileState.Missing;
				
				try {setImage(ImageUtils.read(parts[parts.length-1][0][0].getValue()));}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
	}
	private void clearTileStates()
	{
		posterId++;
		config = null;
		tiles = null;
		tilePage = null;
		parts = null;
	}
	
	static class TileRendering
	{
		BufferedImage image;
		int x0, y0, x1, y1, sw, sh;
		TileRendering() {}
		void set(BufferedImage image, int x0, int y0, int x1, int y1, int sw, int sh) {this.image = image; this.x0 = x0; this.y0 = y0; this.x1 = x1; this.y1 = y1; this.sw = sw; this.sh = sh;}
	}
	List<TileRendering> renderings = new ArrayList<TileRendering>();
	private synchronized int requestArea(int x0, int y0, int x1, int y1, double pixelSize, List<TileRendering> renderings)
	{
		int l = config.layerFor(pixelSize);
		int i0 = config.tilei(l, x0), i1 = config.tilei(l, x1), j0 = config.tilej(l, y0), j1 = config.tilej(l, y1);
		int n = 0;
		
		for (int i=0;i<tiles[l].length;i++)
			for (int j=0;j<tiles[l][i].length;j++)
				if (parts[l][i][j] != null)
		{
			if (i>=i0 && i<=i1 && j>=j0 && j<=j1)
			{
				if (tileStates[l][i][j] == TileState.Missing)
					requestTile(l, i, j);
				else if (tileStates[l][i][j] == TileState.Loaded)
				{
					
				}
				if (renderings.size() <= n)
					renderings.add(new TileRendering());
				int x = config.tilex(l, i), y = config.tiley(l, j);
				renderings.get(n++).set(tiles[l][i][j], x, y, x+config.tileFullw(l, i), y+config.tileFullh(l, j), config.tilew(l, i), config.tileh(l, j));
			}
			else
			{
				tileStates[l][i][j] = TileState.Missing;
				tiles[l][i][j] = null;
			}
		}
		return n;
	}
	private synchronized InputStream getTileSource(int l, int i, int j, long posterId) throws DataLinkException
	{
		return posterId == this.posterId && tileStates[l][i][j] == TileState.Loading ? parts[l][i][j].getValue() : null;
	}
	private synchronized void setTile(int l, int i, int j, long posterId, BufferedImage image)
	{
		if (posterId != this.posterId || tileStates[l][i][j] != TileState.Loading)
			return;
		tiles[l][i][j] = image;
		tileStates[l][i][j] = image == null ? TileState.Missing : TileState.Loaded;
		repaint();
	}
	private void requestTile(final int l, final int i, final int j)
	{
		final long posterId = this.posterId;
		tileStates[l][i][j] = TileState.Loading;
		tileLoader.submit(new Callable<Void>() {@Override public Void call() throws Exception
		{
			InputStream in = null;
			try {in = getTileSource(l, i, j, posterId);}
			catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e, true);}
			
			BufferedImage image = null;
			if (in != null)
				try {image = ImageUtils.read(in);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			
			setTile(l, i, j, posterId, image);
			return null;
		}});
	}
	
	@Override public void switchDocument(AnnotatedObject document) throws Exception
	{
		super.switchDocument(document);
		this.isPoster = PosterUtils.isPoster(page.getBook());
		updateTileStates();
	}
	
	@Override protected void drawImage(Graphics2D g, double pixelSize)
	{
		if (isPoster && config != null)
		{
			int n = requestArea((int)toViewX(0), (int)toViewY(0), (int)toViewX(getWidth()), (int)toViewY(getHeight()), pixelSize, renderings);
			if (n == 0)
				super.drawImage(g, pixelSize);
			else for (int i=0;i<n;i++)
			{
				TileRendering r = renderings.get(i);
				//TODO: use "best" tile if requested one unavailable
				if (r.image != null)
				{
					g.drawImage(r.image, r.x0, r.y0, r.x1, r.y1, 0, 0, r.sw, r.sh, null);
					r.image = null;
				}
				else
				{
					int fw = config.getFullWidth(), fh = config.getFullHeight();
					g.drawImage(image, r.x0, r.y0, r.x1, r.y1, r.x0*image.getWidth()/fw, r.y0*image.getHeight()/fh, r.x1*image.getWidth()/fw, r.y1*image.getHeight()/fh, null);
				}
			}
		}
		else super.drawImage(g, pixelSize);
	}
	
	@Override protected void renderMini(Graphics2D g, int mw, int mh)
	{
		if (!isPoster)
			super.renderMini(g, mw, mh);
		else
		{
			int l = config.getLastLayer();
			synchronized (this)
			{
				if (tileStates[l][0][0] == TileState.Missing)
					requestTile(l, 0, 0);
			}
			if (tiles[l][0][0] != null)
				g.drawImage(tiles[l][0][0], 0, 0, mw, mh, null);
		}
	}
	
	private static ExecutorService tileLoader = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()-1));
}
