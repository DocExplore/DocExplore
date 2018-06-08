package org.interreg.docexplore.manuscript;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.util.MemoryImageSource;

public class TileConfiguration implements Serializable
{
	private static final long serialVersionUID = -1926191056604763700L;

	static class Layer implements Serializable
	{
		private static final long serialVersionUID = -3517469139397976374L;
		public final LayerMetrics metrics;
		int [][] tileIds;
		
		Layer(DocExploreDataLink link, MetaData [][] parts) throws DataLinkException
		{
			this.metrics = computeMetrics(link, parts);
		}
	}
	static class LayerMetrics implements Serializable
	{
		private static final long serialVersionUID = -6483047840479595051L;
		int [] widths, heights;
		public final int fullWidth, fullHeight;
		
		public LayerMetrics(int [] widths, int [] heights, int fullWidth, int fullHeight)
		{
			this.widths = widths;
			this.heights = heights;
			this.fullWidth = fullWidth;
			this.fullHeight = fullHeight;
		}
	}
	
	private Layer [] layers;
	
	public MetaData [][][] getTiles(DocExploreDataLink link) throws DataLinkException
	{
		MetaData [][][] tiles = new MetaData [layers.length][][];
		for (int l=0;l<tiles.length;l++)
		{
			tiles[l] = new MetaData [layers[l].tileIds.length][layers[l].tileIds.length == 0 ? 0 : layers[l].tileIds[0].length];
			for (int i=0;i<tiles[l].length;i++)
				for (int j=0;j<tiles[l][i].length;j++)
					if (layers[l].tileIds[i][j] >= 0)
						tiles[l][i][j] = link.getMetaData(layers[l].tileIds[i][j]);
		}
		return tiles;
	}
	
	public int tilei(int layer, int x)
	{
		return tilei(layers[layer].metrics.fullWidth*x/layers[0].metrics.fullWidth, layers[layer].metrics);
	}
	public int tilej(int layer, int y)
	{
		return tilej(layers[layer].metrics.fullHeight*y/layers[0].metrics.fullHeight, layers[layer].metrics);
	}
	public int tilex(int layer, int i)
	{
		return layers[0].metrics.fullWidth*tilex(i, layers[layer].metrics)/layers[layer].metrics.fullWidth;
	}
	public int tiley(int layer, int j)
	{
		return layers[0].metrics.fullHeight*tiley(j, layers[layer].metrics)/layers[layer].metrics.fullHeight;
	}
	public int tilew(int layer, int i) {return layers[layer].metrics.widths[i];}
	public int tileh(int layer, int j) {return layers[layer].metrics.heights[j];}
	public int tileFullw(int layer, int i) {return layers[0].metrics.fullWidth*layers[layer].metrics.widths[i]/layers[layer].metrics.fullWidth;}
	public int tileFullh(int layer, int j) {return layers[0].metrics.fullHeight*layers[layer].metrics.heights[j]/layers[layer].metrics.fullHeight;}
	public int layerFullw(int layer) {return layers[layer].metrics.fullWidth;}
	public int layerFullh(int layer) {return layers[layer].metrics.fullHeight;}
	public int getLastLayer() {return layers.length-1;}
	public int getHorizontalTiles(int layer) {return layers[layer].tileIds.length;}
	public int getVerticalTiles(int layer) {return layers[layer].tileIds.length == 0 ? 0 : layers[layer].tileIds[0].length;}
	public int getTileId(int l, int i, int j) {return layers[l].tileIds[i][j];}
	
	private int tilei(int x, LayerMetrics metrics)
	{
		int i = 0; 
		for (;i<metrics.widths.length && x>0;i++) 
			x -= metrics.widths[i]; 
		return Math.min(metrics.widths.length-1, Math.max(0, i-1));
	}
	private int tilej(int y, LayerMetrics metrics)
	{
		int j = 0; 
		for (;j<metrics.heights.length && y>0;j++) 
			y -= metrics.heights[j]; 
		return Math.min(metrics.heights.length-1, Math.max(0, j-1));
	}
	private int tilex(int i, LayerMetrics metrics)
	{
		int x = 0; 
		for (;i-1>=0;i--) 
			x += metrics.widths[i-1]; 
		return x;
	}
	private int tiley(int j, LayerMetrics metrics)
	{
		int y = 0; 
		for (;j-1>=0;j--) 
			y += metrics.heights[j-1]; 
		return y;
	}
	
	private void writeConfiguration(DocExploreDataLink link, AnnotatedObject object) throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new ObjectOutputStream(out).writeObject(this);
		out.close();
		MetaData config = new MetaData(link, link.tileConfigKey, MetaData.textType, new ByteArrayInputStream(out.toByteArray()));
		object.addMetaData(config);
		
		for (int l=0;l<layers.length;l++)
		{
			Layer layer = layers[l];
			for (int i=0;i<layer.tileIds.length;i++)
				for (int j=0;j<layer.tileIds[0].length;j++)
					if (layer.tileIds[i][j] >= 0)
						object.addMetaData(link.getMetaData(layer.tileIds[i][j]));
		}
	}
	
	static int sizeThreshold = 2048;
	public void build(DocExploreDataLink link, Book book, float [] progress) throws Exception
	{
		if (!PosterUtils.isPoster(book))
		{
			layers = null;
			return;
		}
		
		List<MetaData> oldConfigs = book.getMetaDataListForKey(link.tileConfigKey);
		for (MetaData config : oldConfigs)
			book.removeMetaData(config);
		List<MetaData> oldTiles = PosterUtils.getUpperTiles(link, book);
		for (MetaData tile : oldTiles)
			book.removeMetaData(tile);
		
		float progressForLayer = 0;
		MetaData [][] parts = PosterUtils.getBaseTilesArray(link, book);
		Layer layer = new Layer(link, parts);
		layer.tileIds = new int [parts.length][parts.length == 0 ? 0 : parts[0].length];
		for (int i=0;i<parts.length;i++)
			for (int j=0;j<parts[0].length;j++)
				layer.tileIds[i][j] = parts[i][j] == null ? -1 : parts[i][j].id;
		
		this.layers = new Layer [] {layer};
		while (true)
		{
			int tw = 1, th = 1;
			while (layer.metrics.fullWidth/tw > sizeThreshold) tw *= 2;
			while (layer.metrics.fullHeight/th > sizeThreshold) th *= 2;
			int childw = layer.metrics.fullWidth/tw, childh = layer.metrics.fullHeight/th;
			//System.out.println("layer: "+tw+","+th);
			
			int [][] newTiles = new int [tw][th];
			MetaData [][] newParts = new MetaData [tw][th];
			for (int i=0;i<tw;i++)
				for (int j=0;j<th;j++)
			{
				progress[0] = progressForLayer+.5f*(1-progressForLayer)*(i*th+j)/(tw*th);
				
				BufferedImage image = new BufferedImage(Math.max(1, childw), Math.max(1, childh), BufferedImage.TYPE_3BYTE_BGR);
				Graphics2D g = image.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				int x0 = i*layer.metrics.fullWidth/tw, x1 = (i+1)*layer.metrics.fullWidth/tw;
				int y0 = j*layer.metrics.fullHeight/th, y1 = (j+1)*layer.metrics.fullHeight/th;
				int ti0 = tilei(x0, layer.metrics), ti1 = tilei(x1, layer.metrics);
				int tj0 = tilej(y0, layer.metrics), tj1 = tilej(y1, layer.metrics);
				for (int ti=ti0;ti<ti1+1;ti++)
					for (int tj=tj0;tj<tj1+1;tj++)
						if (ti >= 0 && tj >= 0)
							if (parts[ti][tj] != null)
				{
					int cx = tilex(ti, layer.metrics), cy = tiley(tj, layer.metrics);
					int l = Math.max(cx, x0), r = Math.min(cx+layer.metrics.widths[ti], x1);
					int u = Math.max(cy, y0), d = Math.min(cy+layer.metrics.heights[tj], y1);//System.out.println(">>>"+parts[ti][tj]);
					if (parts[ti][tj].type.equals(MetaData.imageType))
						g.drawImage(parts[ti][tj].getImage(), l-x0, u-y0, r-x0, d-y0, l-cx, u-cy, r-cx, d-cy, null);
				}
				image = Scalr.resize(image, Method.QUALITY, image.getWidth()/2, image.getHeight()/2);
				newParts[i][j] = new MetaData(link, link.partKey, MetaData.imageType, new MemoryImageSource(image).getFile());//System.out.println(newParts[i][j].getValue());
				newParts[i][j].setMetaDataString(link.dimKey, image.getWidth()+","+image.getHeight());
				newTiles[i][j] = newParts[i][j].id;
			}
			
			layer = new Layer(link, newParts);
			layers = Arrays.copyOf(layers, layers.length+1);
			layers[layers.length-1] = layer;
			layer.tileIds = newTiles;
			parts = newParts;
			
			progressForLayer = 1-.5f*(1-progressForLayer);
			
			if (tw == 1 && th == 1)
				break;
		}
		
		writeConfiguration(link, book);
	}
	
	private static LayerMetrics computeMetrics(DocExploreDataLink link, MetaData [][] parts) throws DataLinkException
	{
		int [] widths = new int [parts.length];
		int [] heights = new int [parts.length == 0 ? 0 : parts[0].length];
		for (int i=0;i<widths.length;i++) widths[i] = 1;
		for (int i=0;i<heights.length;i++) heights[i] = 1;
		for (int i=0;i<parts.length;i++)
			for (int j=0;j<parts[0].length;j++)
		{
			int w = 1, h = 1;
			if (parts[i][j] == null)
				continue;
			String dims = parts[i][j].getMetaDataString(link.dimKey);
			if (dims == null)
				continue;
			String [] dim = dims.split(",");
			w = Integer.parseInt(dim[0]); h = Integer.parseInt(dim[1]);
			if (widths[i] == 1 || w < widths[i]) widths[i] = w;
			if (heights[j] == 1 || h < heights[j]) heights[j] = h;
		}
		int fullWidth = 0;
		int fullHeight = 0;
		for (int i=0;i<widths.length;i++) fullWidth += widths[i];
		for (int i=0;i<heights.length;i++) fullHeight += heights[i];
		return new LayerMetrics(widths, heights, fullWidth, fullHeight);
	}
	
	public int getFullWidth() {return layers[0].metrics.fullWidth;}
	public int getFullHeight() {return layers[0].metrics.fullHeight;}
	
	public int layerFor(double pixelSize)
	{
		int l = 0;
		while (pixelSize < .5 && l < layers.length-1)
		{
			pixelSize *= 2;
			l++;
		}
		return l;
	}
}
