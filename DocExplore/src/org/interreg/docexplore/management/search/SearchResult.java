package org.interreg.docexplore.management.search;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import org.interreg.docexplore.manuscript.AnnotatedObject.ObjectStub;
import org.interreg.docexplore.manuscript.Book.BookStub;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page.PageStub;
import org.interreg.docexplore.manuscript.Region.RegionStub;
import org.interreg.docexplore.util.Pair;
import org.interreg.docexplore.util.StringUtils;
import org.interreg.docexplore.util.search.FuzzySearch;

/**
 * An object that represents a single result of a search.
 * @author Burnett
 */
public class SearchResult extends JPanel
{
	private static final long serialVersionUID = 3103218855613111586L;
	
	SearchHandler handler;
	ObjectStub<?> stub;
	String name;
	BufferedImage preview;
	
	/**
	 * Creates a new search result object.
	 * @param name The name of the result that will be displayed in a larger font.
	 * @param description A description of the result.
	 * @param preview An image preview that is displayed when the user moves the cursor
	 * over the display of the result.
	 * @param maxDim Maximum size of the preview image on both dimensions. The preview image 
	 * is resized without changing the aspect ratio so that it's greatest dimension is 
	 * equal to this. A negative value means no resizing.
	 */
	public SearchResult(ObjectStub<?> stub, String description, double relevance, float maxDim)
	{
		super(new BorderLayout());
		
		this.stub = stub;
		this.handler = null;
		this.name = "Unknown";
		if (stub instanceof BookStub)
		{
			BookStub book = (BookStub)stub;
			name = book.name;
		}
		else if (stub instanceof PageStub)
		{
			PageStub page = (PageStub)stub;
			name = page.book.name+" p."+page.pageNumber;
		}
		else if (stub instanceof RegionStub)
		{
			RegionStub region = (RegionStub)stub;
			name = region.page.book.name+" p."+region.page.pageNumber+" ROI";
		}
		
		//this.description = description.equals("") || description == null ? 
		//	"<i>"+XMLResourceBundle.getBundledString("noDescriptionMsg")+"</i>" : description;
		
		/*if (maxDim > 0)
		{
			float ratio;
			if (preview.getWidth() > preview.getHeight())
				ratio = maxDim/preview.getWidth();
			else ratio = maxDim/preview.getHeight();
			
			this.preview = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio),
				AffineTransformOp.TYPE_BILINEAR).filter(preview, null);
		}
		else this.preview = preview;*/
		
		JLabel nameLabel = new JLabel("<html><b>"+name+"</b></html>");
		nameLabel.setForeground(Color.BLUE);
		//nameLabel.setFont(Font.decode("Arial-bold-16"));
		nameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		if (preview != null) nameLabel.addMouseMotionListener(new MouseAdapter()
		{
			JWindow popupWindow = null;
			public void mouseEntered(MouseEvent e)
			{
				popupWindow = new JWindow();
				Canvas canvas = new Canvas()
				{
					private static final long serialVersionUID = -4098516546318514554L;
					public void paint(Graphics g)
					{
						g.drawImage(SearchResult.this.preview, 0, 0, null);
					}
				};
				canvas.setSize(SearchResult.this.preview.getWidth(), 
					SearchResult.this.preview.getHeight());
				popupWindow.getContentPane().add(canvas);
				Point loc = SearchResult.this.getLocationOnScreen();
				popupWindow.setLocation(loc.x+SearchResult.this.getWidth(), loc.y);
				popupWindow.pack();
				popupWindow.setVisible(true);
			}
			public void mouseExited(MouseEvent e)
			{
				popupWindow.setVisible(false);
				popupWindow = null;
			}
			protected void finalize() throws Throwable
			{
				if (popupWindow != null)
				{
					popupWindow.setVisible(false);
					popupWindow = null;
				}
				super.finalize();
			}
		});
		nameLabel.addMouseListener(new MouseAdapter() {public void mouseClicked(MouseEvent e)
		{
			if (handler == null || e.getButton() != MouseEvent.BUTTON1) return;
			handler.resultClicked(SearchResult.this);
		}});
		
		if (description.length() > 0)
		{
			JLabel descArea = new JLabel("<html>"+description+"</html>");
			descArea.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			add(descArea, BorderLayout.CENTER);
		}
		
		add(nameLabel, BorderLayout.NORTH);
		add(new JLabel("<html><b>"+((int)(100*relevance))+"%&nbsp;&nbsp;</b></html>"), BorderLayout.WEST);
		
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.gray, 2),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)));
	}
	
	public ObjectStub<?> getStub() {return stub;}
	public String getName() {return name;}
	public BufferedImage getPreview() {return preview;}
	
	static int lim = 96;
	public static String highlight(List<Pair<MetaDataKey, String>> text, List<Pair<MetaDataKey, String>> criteria, double relevance)
	{
		StringBuffer sb = new StringBuffer();
		
		for (Pair<MetaDataKey, String> line : text)
		{
			String lineText = removeXml(line.second);
			List<int []> occs = new LinkedList<int []>();
			for (Pair<MetaDataKey, String> criterion : criteria)
				if (criterion.first == null || criterion.first == line.first)
					for (int [] occ : FuzzySearch.process(criterion.second, lineText, 2))
						if (occ[2]*1./criterion.second.length() >= relevance)
							occs.add(occ);
			if (occs.isEmpty())
				continue;
			
			for (int [] occ : occs)
			{
				sb.append(sb.length() != 0 ? " ... " : "");
				sb.append(StringUtils.wordSubString(lineText, occ[0], occ[1], 3, "<b>", "</b>"));
			}
		}
		String res = sb.toString();
		if (res.length() > lim)
			res = res.substring(0, lim)+"...";
		return res;
	}
	
	static String removeXml(String xml)
	{
		StringBuilder res = new StringBuilder();
		int begin = xml.indexOf("<");
		int end = -1;
		boolean first = true;
		while (begin >= 0)
		{
			int oldend = end;
			end = xml.indexOf(">", begin);
			if (end < 0)
				break;
			String ins = xml.substring(oldend+1, begin);
			if (ins.trim().length() > 0)
			{
				if (!first)
					res.append(" - ");
				first = false;
				res.append(ins);
			}
			begin = xml.indexOf("<", end);
		}
		res.append(xml.substring(end+1));
		//System.out.println(res.toString());
		return res.toString();
	}
}
