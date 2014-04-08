/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class LooseGridLayout implements LayoutManager
{
	public static final int LEFT = SwingConstants.LEFT;
	public static final int RIGHT = SwingConstants.RIGHT;
	public static final int TOP = SwingConstants.TOP;
	public static final int BOTTOM = SwingConstants.BOTTOM;
	public static final int CENTER = SwingConstants.CENTER;
	
	int rows, cols;
	int hgap, vgap;
	boolean hfill, vfill;
	int halign, valign;
	boolean hjustified, vjustified;
	
	public LooseGridLayout(int rows, int cols, int hgap, int vgap, 
		boolean hfill, boolean vfill, int halign, int valign, 
		boolean hjustified, boolean vjustified)
	{
		this.rows = cols != 0 ? 0 : rows;
		this.cols = cols;
		this.hgap = hgap;
		this.vgap = vgap;
		this.hfill = hfill;
		this.vfill = vfill;
		this.halign = halign;
		this.valign = valign;
		this.hjustified = hjustified;
		this.vjustified = vjustified;
	}
	public LooseGridLayout(int rows, int cols, int hgap, int vgap, 
		boolean hfill, boolean vfill, int halign, int valign)
	{
		this(rows, cols, hgap, vgap, hfill, vfill, halign, valign, true, true);
	}
	public LooseGridLayout(int rows, int cols, int hgap, int vgap, int halign, int valign)
	{
		this(rows, cols, hgap, vgap, false, false, halign, valign);
	}
	public LooseGridLayout(int rows, int cols, int hgap, int vgap, boolean hfill, boolean vfill)
	{
		this(rows, cols, hgap, vgap, hfill, vfill, CENTER, CENTER);
	}
	public LooseGridLayout(int rows, int cols, int hgap, int vgap)
	{
		this(rows, cols, hgap, vgap, false, false);
	}
	public LooseGridLayout(int rows, int cols)
	{
		this(rows, cols, 5, 5);
	}
	
	public void layoutContainer(Container parent)
	{
		Insets insets = parent.getInsets();
		int topInset = insets.top;
		int leftInset = insets.left;
		
		Dimension rawSize = parent.getSize();
		Dimension size = new Dimension((int)(rawSize.getWidth()-(leftInset+insets.right)), 
			(int)(rawSize.getHeight()-(topInset+insets.bottom)));
		
		//System.out.println("parent size : "+size.getWidth()+", "+size.getHeight());
		Dimension rawPreferred = preferredLayoutSize(parent);
		Dimension preferred = new Dimension((int)(rawPreferred.getWidth()-(leftInset+insets.right)), 
				(int)(rawPreferred.getHeight()-(topInset+insets.bottom)));
		
		int ncols = columns(parent);
		int nrows = rows(parent);
		
		double gapHeight = vgap*(nrows+1);
		double heightRatio = 1;
		if (vjustified || size.getHeight()<preferred.getHeight())
			heightRatio = (size.getHeight()-gapHeight)/(preferred.getHeight()-gapHeight);
		
		double gapWidth = hgap*(ncols+1);
		double widthRatio = 1;
		if (hjustified || size.getWidth()<preferred.getWidth())
			widthRatio = (size.getWidth()-gapWidth)/(preferred.getWidth()-gapWidth);
		
		//System.out.println("ratio : "+widthRatio+", "+heightRatio);
		
		double [] leftAnchors = new double [ncols];
		double [] topAnchors = new double [nrows];
		double [] colWidth = new double [ncols];
		double [] rowHeight = new double [nrows];
		
		for (int i=0;i<ncols;i++)
		{
			colWidth[i] = columnWidth(parent, i, false)*widthRatio;
			leftAnchors[i] = hgap+(i == 0 ? leftInset : leftAnchors[i-1]+colWidth[i-1]);
		}
		for (int i=0;i<nrows;i++)
		{
			rowHeight[i] = rowHeight(parent, i, false)*heightRatio;
			topAnchors[i] = vgap+(i == 0 ? topInset : topAnchors[i-1]+rowHeight[i-1]);
		}
		
		for (int i=0;i<nrows;i++)
		{
			double top = topAnchors[i];
			for (int j=0;j<ncols;j++)
			{
				double left = leftAnchors[j];
				
				Component component = get(parent, i, j);
				if (component == null) continue;
				
				Dimension dim = component.getPreferredSize();
				//double width = dim.getWidth()*widthRatio;
				//double height = dim.getHeight()*heightRatio;
				
				double width = dim.getWidth();
				double height = dim.getHeight();
				
				if (hfill)
					width = colWidth[j];
				else if (width > colWidth[j])
					width *= widthRatio;
				
				if (vfill)
					height = rowHeight[i];
				else if (height > rowHeight[i])
					height *= heightRatio;
				
				double x, y;
				switch (halign)
				{
					case LEFT : x = left; break;
					case RIGHT : x = left+colWidth[j]-width; break;
					case CENTER : default : x = left+(colWidth[j]-width)/2;
				}
				switch (valign)
				{
					case TOP : y = top; break;
					case BOTTOM : y = top+rowHeight[i]-height; break;
					case CENTER : default : y = top+(rowHeight[i]-height)/2;
				}
				//component.setBounds((int)(left+(colWidth[j]-width)/2), (int)(top+(rowHeight[i]-height)/2), (int)width, (int)height);
				component.setBounds((int)x, (int)y, (int)width, (int)height);
			}
		}
	}
	
	Component get(Container parent, int row, int col)
	{
		Component [] components = parent.getComponents();
		int index = cols == 0 ? row+rows*col : cols*row+col;
		return index >= components.length ? null : components[index];
	}
	
	int rows(Container parent)
	{
		return rows != 0 ? rows : (parent.getComponents().length-1)/cols+1;
	}
	int columns(Container parent)
	{
		return cols != 0 ? cols : (parent.getComponents().length-1)/rows+1;
	}
	int rowHeight(Container parent, int row, boolean minimum)
	{
		int height = 0;
		for (int i=0;;i++)
		{
			if (cols > 0 && i >= cols) break;
			Component component = get(parent, row, i);
			if (component == null) break;
			
			Dimension size = minimum ? component.getMinimumSize() : component.getPreferredSize();
			if (height < size.getHeight())
				height = (int)size.getHeight();
		}
		
		return height;
	}
	int columnWidth(Container parent, int col, boolean minimum)
	{
		int width = 0;
		for (int i=0;;i++)
		{
			if (rows > 0 && i >= rows) break;
			Component component = get(parent, i, col);
			if (component == null) break;
			
			Dimension size = minimum ? component.getMinimumSize() : component.getPreferredSize();
			if (width < size.getWidth())
				width = (int)size.getWidth();
		}
		
		return width;
	}

	public Dimension minimumLayoutSize(Container parent)
	{
		int nrows = rows(parent);
		int ncols = columns(parent);
		
		int height = vgap;
		for (int i=0;i<nrows;i++) height += vgap+rowHeight(parent, i, true);
		
		int width = hgap;
		for (int i=0;i<ncols;i++) width += hgap+columnWidth(parent, i, true);
		
		Insets insets = parent.getInsets();
		return new Dimension(width+insets.left+insets.right, height+insets.top+insets.bottom);
	}

	public Dimension preferredLayoutSize(Container parent)
	{
		int nrows = rows(parent);
		int ncols = columns(parent);
		
		int height = vgap;
		for (int i=0;i<nrows;i++) height += vgap+rowHeight(parent, i, false);
		
		int width = hgap;
		for (int i=0;i<ncols;i++) width += hgap+columnWidth(parent, i, false);
		
		Insets insets = parent.getInsets();
		return new Dimension(width+insets.left+insets.right, height+insets.top+insets.bottom);
	}

	public void removeLayoutComponent(Component comp) {}
	public void addLayoutComponent(String name, Component comp) {}
	
	@SuppressWarnings("serial")
	public static void main(String [] args)
	{
		//LooseGridLayout layout = new LooseGridLayout(0, 3, 5, 5, false, false);
		LooseGridLayout layout = new LooseGridLayout(0, 3, 5, 5, false, false, 
			SwingConstants.RIGHT, SwingConstants.BOTTOM, true, false);
		JPanel panel = new JPanel(layout);
		
		for (int i=0;i<8;i++)
		{
			final int j = i;
			JPanel sub = new JPanel() {public String toString() {return "panel"+j;}};
			sub.setBorder(BorderFactory.createLineBorder(Color.black));
			//ugliest thing I've ever done, but practical...
			switch (i)
			{
				case 1 : sub.setPreferredSize(new Dimension(150, 70)); break;
				case 2 : sub.setPreferredSize(new Dimension(80, 150)); break;
				case 3 : sub.setPreferredSize(new Dimension(30, 70)); break;
				case 5 : sub.setPreferredSize(new Dimension(70, 30)); break;
				case 6 : sub.setPreferredSize(new Dimension(70, 50)); break;
				default : sub.setPreferredSize(new Dimension(100, 100)); break;
			}
			panel.add(sub);
		}
		
		/*for (int i=0;i<4;i++) for (int j=0;j<4;j++)
		{
			System.out.println("get("+i+","+j+") -> "+layout.get(panel, i, j));
		}*/
		System.out.println("rows "+layout.rows(panel)+", cols "+layout.columns(panel));
		for (int i=0;i<layout.rows(panel);i++)
		{
			System.out.println("row "+i+" height : "+layout.rowHeight(panel, i, false));
		}
		for (int j=0;j<layout.columns(panel);j++)
		{
			System.out.println("column "+j+" height : "+layout.columnWidth(panel, j, false));
		}
		System.out.println("preferred : "+layout.preferredLayoutSize(panel));
		
		//panel.doLayout();
		
		JFrame frame = new JFrame("Test layout");
		frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e) {System.exit(0);}});
		frame.add(panel, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
	}
}
