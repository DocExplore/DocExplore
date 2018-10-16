/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.gui;

import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.fs2.DataLinkFS2Source;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.manage.CreateBookDialog;
import org.interreg.docexplore.management.merge.BookExporter;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.ManuscriptLink;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.manuscript.app.editors.CollectionEditor;
import org.interreg.docexplore.manuscript.app.editors.ManuscriptEditor;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.ZipUtils;

@SuppressWarnings("serial")
public class MMTCollectionEditor extends CollectionEditor
{

	public MMTCollectionEditor(DocumentEditorHost host) throws DataLinkException
	{
		super(host);
		
		topPanel.titlePanel.add(host.getAppHost().helpPanel.createHelpMessageButton(Lang.s("helpMmtCollectionMsg")));
	}
	
	@Override public void onActionRequest(String action, Object param) throws Exception
	{
		if (action.equals("add"))
		{
			CreateBookDialog dialog = new CreateBookDialog(host.getAppHost());
			GuiUtils.centerOnScreen(dialog);
			dialog.setVisible(true);
		}
		else if (action.equals("delete"))
		{
			List<Book> books = this.books.getSelectedElements();
			if (books.size() > 0)
				host.getAppHost().getActionRequestListener().onDeleteBooksRequest(books);
		}
		else if (action.equals("import"))
			importBook();
		else if (action.equals("export"))
			exportBook();
		else super.onActionRequest(action, param);
	}
	
	public void exportBook()
	{
		List<Book> books = this.books.getSelectedElements();
		final Book book = books.size() == 1 ? books.get(0) : null;
		if (book == null)
			return;
		final File file = DocExploreTool.getFileDialogs().saveFile(DocExploreTool.getBookCategory());
		if (file == null)
			return;
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
		}, MMTCollectionEditor.this);
	}

	private void importBook()
	{
		final File file = DocExploreTool.getFileDialogs().openFile(DocExploreTool.getBookCategory());
		if (file == null)
			return;
		List<Book> books = this.books.getSelectedElements();
		final Book selected = books.size() == 1 ? books.get(0) : null;
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
						int res = JOptionPane.showConfirmDialog(MMTCollectionEditor.this, Lang.s("manageMergeMessage").replace("%name", selected.getName()), 
							Lang.s("manageMergeLabel"), JOptionPane.YES_NO_CANCEL_OPTION);
						if (res == JOptionPane.CANCEL_OPTION)
							cancel = true;
						else if (res == JOptionPane.YES_OPTION)
							merge = selected;
					}
					if (!cancel && merge == null)
					{
						Book found = ManuscriptEditor.findTitle(host.getAppHost().getLink(), remote.getName());
						if (found != null && found != selected && found.getLastPageNumber() == remote.getLastPageNumber())
						{
							int res = JOptionPane.showConfirmDialog(MMTCollectionEditor.this, Lang.s("manageMergeMessage").replace("%name", found.getName()), 
								Lang.s("manageMergeLabel"), JOptionPane.YES_NO_CANCEL_OPTION);
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
						while (ManuscriptEditor.findTitle(host.getAppHost().getLink(), title) != null)
							if ((title = JOptionPane.showInputDialog(Lang.s("manageExistsMessage").replace("%name", title), 
								title)) == null)
									{cancel = true; break;}
						newTitle = title;
					}
					
					if (!cancel)
					{
						if (merge == null)
						{
							Book imported = exporter.add(remote, host.getAppHost().getLink(), null);
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
		}, MMTCollectionEditor.this);
		
		host.getAppHost().broadcastAction(DocumentEvents.collectionChanged.event);
	}
}
