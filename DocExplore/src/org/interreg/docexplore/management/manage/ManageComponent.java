/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.manage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.fs2.DataLinkFS2Source;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.management.merge.BookExporter;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.ManuscriptLink;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.ZipUtils;

public class ManageComponent extends JPanel
{
	private static final long serialVersionUID = -3824420946641467221L;
	
	MainWindow win;
	public final ManageHandler handler;
	ManageToolbar toolbar;
	CreateBookDialog createDialog;
	JList bookList;
	
	@SuppressWarnings("serial")
	public ManageComponent(final MainWindow win, final ManageHandler handler, boolean editable, boolean showPages)
	{
		super(new BorderLayout());
		
		this.win = win;
		this.handler = handler;
		this.bookList = new JList(new CollectionNode(this));
		setBorder(BorderFactory.createLineBorder(Color.black, 1));
		this.createDialog = new CreateBookDialog(this);
		
		bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bookList.setCellRenderer(new ManageCellRenderer(win));
		//tree.setRowHeight(52);
		add(new JScrollPane(bookList), BorderLayout.CENTER);
		
		if (editable)
		{
			bookList.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
				{
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
					{
						int index = bookList.locationToIndex(e.getPoint());
						if (index < 0 || !bookList.getCellBounds(index, index).contains(e.getPoint()))
							return;
						win.addTab((Book)bookList.getModel().getElementAt(index));
					}
				}
			});
			this.toolbar = new ManageToolbar(this);
			add(toolbar, BorderLayout.NORTH);
			
			bookList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;
				int count = bookList.getSelectedIndices().length;
				toolbar.deleteButton.setEnabled(count > 0);
				toolbar.editButton.setEnabled(count == 1);
//				toolbar.processButton.setEnabled(count == 1);
				toolbar.exportButton.setEnabled(count == 1);
			}});
		}
		bookList.setBackground(new JPanel().getBackground());
		
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");
		getActionMap().put("DEL", new AbstractAction() {public void actionPerformed(ActionEvent arg0)
		{
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Vector<Book> books = new Vector(Arrays.asList(bookList.getSelectedValues()));
			if (books.size() > 0)
				handler.onDeleteBooksRequest(books);
		}});
	}
	public ManageComponent(MainWindow win, ManageHandler handler) {this(win, handler, true, true);}
	
	public void setSingleSelection() {bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);}
	
	public void refresh()
	{
		((CollectionNode)bookList.getModel()).reload();
		repaint();
	}
	
	public Book getSelectedBook()
	{
		int index = bookList.getSelectedIndex();
		if (index < 0)
			return null;
		return ((Book)bookList.getModel().getElementAt(index));
	}
	
	public List<Comparable<?>> getSelectedObjects()
	{
		Vector<Comparable<?>> selection = new Vector<Comparable<?>>();
		for (int index : bookList.getSelectedIndices())
			selection.add((Book)bookList.getModel().getElementAt(index));
		return selection;
	}
	
	public void exportBook(final Book book, final File file)
	{
		GuiUtils.blockUntilComplete(new ProgressRunnable()
		{
			float [] progress = {0};
			BookExporter exporter = new BookExporter();
			public void run()
			{
				File tmpDir = null;
				
				try
				{
					tmpDir = new File(DocExploreTool.getHomeDir(), ".export-tmp");
					if (tmpDir.exists())
						FileUtils.deleteDirectory(tmpDir);
					tmpDir.mkdirs();
					
					DataLinkFS2Source source = new DataLinkFS2Source(tmpDir.getAbsolutePath());
					DataLink fs2link = source.getDataLink();
					final ManuscriptLink link = new ManuscriptLink(fs2link);
					exporter.add(book, link, null);
					link.getLink().release();
					
					ZipUtils.zip(tmpDir, file, progress);
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				
				if (tmpDir != null)
					try {FileUtils.deleteDirectory(tmpDir);} 
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
			public float getProgress() {return .5f*exporter.progress+.5f*progress[0];}
		}, win);
	}
	
	public void importBook(final File file, final Book selected)
	{
		GuiUtils.blockUntilComplete(new ProgressRunnable()
		{
			float [] progress = {0};
			BookExporter exporter = new BookExporter();
			public void run()
			{
				File tmpDir = null;
				
				try
				{
					tmpDir = new File(DocExploreTool.getHomeDir(), ".export-tmp");
					if (tmpDir.exists())
						FileUtils.deleteDirectory(tmpDir);
					tmpDir.mkdirs();
					
					ZipUtils.unzip(file, tmpDir);
					
					DataLinkFS2Source source = new DataLinkFS2Source(tmpDir.getAbsolutePath());
					DataLink fs2link = source.getDataLink();
					final ManuscriptLink link = new ManuscriptLink(fs2link);
					Book remote = link.getBook(link.getLink().getAllBookIds().get(0));
					
					Book merge = null;
					boolean cancel = false;
					if (selected != null && selected.getLastPageNumber() == remote.getLastPageNumber())
					{
						int res = JOptionPane.showConfirmDialog(win, XMLResourceBundle.getBundledString("manageMergeMessage").replace("%name", selected.getName()), 
							XMLResourceBundle.getBundledString("manageMergeLabel"), JOptionPane.YES_NO_CANCEL_OPTION);
						if (res == JOptionPane.CANCEL_OPTION)
							cancel = true;
						else if (res == JOptionPane.YES_OPTION)
							merge = selected;
					}
					if (!cancel && merge == null)
					{
						Book found = findTitle(remote.getName());
						if (found != null && found != selected && found.getLastPageNumber() == remote.getLastPageNumber())
						{
							int res = JOptionPane.showConfirmDialog(win, XMLResourceBundle.getBundledString("manageMergeMessage").replace("%name", found.getName()), 
								XMLResourceBundle.getBundledString("manageMergeLabel"), JOptionPane.YES_NO_CANCEL_OPTION);
							if (res == JOptionPane.CANCEL_OPTION)
								cancel = true;
							else if (res == JOptionPane.YES_OPTION)
								merge = found;
						}
					}
					
					String newTitle = null;
					if (!cancel && merge == null)
					{
						String title = remote.getName();
						while (findTitle(title) != null)
							if ((title = JOptionPane.showInputDialog(XMLResourceBundle.getBundledString("manageExistsMessage").replace("%name", title), 
								title)) == null)
									{cancel = true; break;}
						newTitle = title;
					}
					
					if (!cancel)
					{
						if (merge == null)
						{
							Book imported = exporter.add(remote, win.getDocExploreLink(), null);
							if (newTitle != null)
								imported.setName(newTitle);
						}
						else exporter.merge(remote, merge, null);
					}
					link.getLink().release();
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				
				if (tmpDir != null)
					try {FileUtils.deleteDirectory(tmpDir);} 
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
			public float getProgress() {return .5f*exporter.progress+.5f*progress[0];}
		}, win);
		refresh();
	}
	
	Book findTitle(String title) throws DataLinkException
	{
		List<Integer> bookIds = win.getDocExploreLink().getLink().getAllBookIds();
		for (int bookId : bookIds)
			if (title.equals(win.getDocExploreLink().getLink().getBookTitle(bookId)))
				return win.getDocExploreLink().getBook(bookId);
		return null;
	}
}
