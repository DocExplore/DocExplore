/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.gui.image.ImageView;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.gui.DocumentEditorHost;
import org.interreg.docexplore.management.manage.SelectPagesPanel;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.TileConfiguration;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

public class PosterPartsEditor extends JPanel implements BookEditor.ConfigurationEditor
{
	private static final long serialVersionUID = -3584515946932650576L;
	
	protected DocumentEditorHost host;
	Book book;
	JPanel partPanel;
	
	Point dragPos;
	PartLabel dragSource = null;
	Icon dragIcon = null;
	PartLabel lastMovedComp = null;
	
	ImageView posterPreview;
	IconButton refreshButton;
	
	public PosterPartsEditor(final DocumentEditorHost host, final Book book) throws DataLinkException
	{
		super(new BorderLayout(5, 5));
		
		this.host = host;
		this.book = book;
		this.partPanel = new JPanel(new LooseGridLayout(1, 1, 2, 2, true, true, SwingConstants.CENTER, SwingConstants.CENTER, false, false));
		partPanel.setBackground(Color.white);
		
		WrapLayout topLayout = new WrapLayout();
		topLayout.setHgap(10);
		JPanel topPanel = new JPanel(topLayout);
		
		topPanel.add(posterPreview = new ImageView());
		posterPreview.setBorder(BorderFactory.createLineBorder(Color.gray));
		//posterPreview.setHorizontalAlignment(SwingConstants.CENTER);
		posterPreview.addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e) {posterPreview.fit();}});
		posterPreview.addMouseListener(new MouseAdapter() {@Override public void mouseReleased(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1)
				try {host.onDocumentEditorRequest(book.getPage(1));}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
		
		topPanel.add(refreshButton = new IconButton("refresh-24x24.png", Lang.s("manageRefreshLabel")));
		refreshButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
		{
			GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable() {@Override public void run()
			{
				try
				{
					if (book.getLastPageNumber() > 0)
					{
						TileConfiguration config = new TileConfiguration();
						config.build(host.getLink(), book, progress);
						
						Page page = book.getPage(1);
						page.setMetaDataString(host.getLink().dimKey, config.getFullWidth()+","+config.getFullHeight());
						//DocExploreDataLink.getImageMini(page);
						book.setMetaDataString(host.getLink().upToDateKey, "true");
					}
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				refreshPreview();
			}
			float [] progress = {0}; @Override public float getProgress() {return progress[0];}}, PosterPartsEditor.this);
		}});
		add(topPanel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(partPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);
		
		addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e)
		{
			refreshPreview();
		}});
		refreshPreview();
		
		refresh();
	}
	
	public List<PartLabel> getSelectedPartLabels()
	{
		Vector<PartLabel> parts = new Vector<PartLabel>();
		for (Component component : partPanel.getComponents())
			if (component instanceof PartLabel && ((PartLabel)component).selected)
				parts.add((PartLabel)component);
		return parts;
	}
	public List<MetaData> getSelectedParts()
	{
		List<PartLabel> icons = getSelectedPartLabels();
		List<MetaData> parts = new ArrayList<MetaData>(icons.size());
		for (PartLabel icon : icons)
			parts.add(icon.part);
		return parts;
	}
	
	public void refresh()
	{
		try
		{
			partPanel.removeAll();
			MetaData [][] mds = PosterUtils.getBaseTilesArray(host.getLink(), book);
			if (mds.length > 0 && mds[0].length > 0)
			{
				int [] rowLengths = new int [mds[0].length];
				for (int j=0;j<rowLengths.length;j++)
				{
					rowLengths[j] = 0;
					for (int i=mds.length;i>0;i--)
						if (mds[i-1][j] != null)
							{rowLengths[j] = i; break;}
				}
				int w = 2*mds.length+1, h = 2*mds[0].length+1;
				((LooseGridLayout)partPanel.getLayout()).setCols(w);
				((LooseGridLayout)partPanel.getLayout()).setRows(h);
				for (int j=0;j<h;j++)
					for (int i=0;i<w;i++)
				{
					int x = i/2, y = j/2;
					if (i%2 == 0 && j%2 == 0)
					{
						partPanel.add(new JLabel(""));
					}
					else if (i%2 == 1 && j%2 == 1)
					{
						if (mds[x][y] != null)
							partPanel.add(new PartLabel(this, mds[x][y], x, y));
						else partPanel.add(new JLabel(""));
					}
					else
					{
						if (j%2 == 1 && ((x >= 1 && mds[x-1][y] != null) || (x < mds.length && mds[x][y] != null)) ||
							i%2 == 1 && ((y >= 1 && mds[x][y-1] != null) || (y < mds[0].length && mds[x][y] != null)))
								partPanel.add(new PartLabel(this, null, x, y, j%2 == 0));
						else partPanel.add(new JLabel(""));
					}
				}
			}
		}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		
		new Thread() {public void run()
		{
			try
			{
				int cnt = 0;
				for(Component comp : partPanel.getComponents())
					if (comp instanceof PartLabel)
				{
					PartLabel label = (PartLabel)comp;
					MetaData part = label.part;
					if (part != null)
						try {label.mini.setIcon(new ImageIcon(ImageUtils.read(part.getMetaDataListForKey(host.getLink().miniKey).get(0).getValue())));}
						catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e, true);}
					
					cnt++;
					if (cnt%10 == 0)
						{validate(); repaint();}
				}
				validate();
				repaint();
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		}}.start();
		
		validate();
		repaint();
	}
	
	private void refreshPreview()
	{
		int pw = getParent() != null ? Math.min(3*getParent().getWidth()/4, getParent().getHeight()/2) : 0;
		if (pw == 0)
			return;
		try
		{
			if (book.getLastPageNumber() == 0)
			{
				posterPreview.setImage(null);
				return;
			}
			List<MetaData> configs = book.getMetaDataListForKey(host.getLink().tileConfigKey);
			TileConfiguration config = configs.size() > 0 ? (TileConfiguration)new ObjectInputStream(configs.get(0).getValue()).readObject() : null;
			if (config != null)
			{
				BufferedImage image = host.getLink().getMetaData(config.getTileId(config.getLastLayer(), 0, 0)).getImage();
				posterPreview.setImage(image);
			}
			else posterPreview.setImage(new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR));
			posterPreview.setPreferredSize(new Dimension(pw, pw));
			posterPreview.revalidate();
			posterPreview.repaint();
		}
		catch (Exception e)
		{
			posterPreview.setImage(null); 
			ErrorHandler.defaultHandler.submit(e, false);
		}
	}
	
	@Override public void onActionRequest(String action) throws Exception
	{
		if (book == null)
			return;
		if (action.equals("transpose"))
			host.getActionListener().onTransposePartsRequest(book);
	}
	
	Color none = new Color(0, 0, 0, 0);
	protected void paintChildren(Graphics g)
	{
		super.paintChildren(g);
		
		if (dragSource == null)
			{dragIcon = null; return;}
		if (dragIcon == null)
		{
			Icon icon = dragSource.mini.getIcon();
			BufferedImage di = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D gdi = di.createGraphics();
			gdi.setBackground(none);
			gdi.clearRect(0, 0, di.getWidth(), di.getHeight());
			icon.paintIcon(this, gdi, 0, 0);
			for (int i=0;i<di.getWidth();i++)
				for (int j=0;j<di.getHeight();j++)
			{
				int argb = di.getRGB(i, j);
				di.setRGB(i, j, ((((argb >> 24) & 0xff)/2) << 24)+(argb & 0x00ffffff));
			}
			dragIcon = new ImageIcon(di);
		}
		
		int x = dragPos.x-dragIcon.getIconWidth()/2, y = dragPos.y-dragIcon.getIconHeight()/2;
		dragIcon.paintIcon(this, g, x, y);
	}

	@Override public Component getComponent() {return this;}
	
	@Override public void onDeletePagesRequest()
	{
		List<MetaData> parts = getSelectedParts();
		host.getActionListener().onDeletePartsRequest(book, parts);
	}

	@Override public void onAddPagesRequest()
	{
		List<File> files = SelectPagesPanel.show();
		if (files == null)
			return;
		host.getActionListener().onAppendPartsRequest(book, files);
	}
	
	boolean readOnly = false;
	@Override public void setReadOnly(boolean b) {readOnly = b;}
}
