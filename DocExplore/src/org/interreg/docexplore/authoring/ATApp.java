/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.fs2.DataLinkFS2Source;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.app.AppStatusBar;
import org.interreg.docexplore.manuscript.app.DocumentActionHandler;
import org.interreg.docexplore.manuscript.app.DocumentPanel;
import org.interreg.docexplore.util.GuiUtils;

public class ATApp extends JFrame
{
	private static final long serialVersionUID = 6037278838028408126L;
	
	DocExploreDataLink libLink;
	DocumentActionHandler documentActionHandler;
	Startup startup;
	
	// Contains all opened documents
	JPanel documentPanel;
	DocumentPanel document = null;
	
	public final ATAppHost host;
//	public MMTAnnotationPanel annotationPanel = null;
	public final ATToolBar toolBar;
	public final ATMenu menu;
	AppStatusBar statusBar;
	//MainMenuBar menuBar;
	JPanel centerPanel;
	
	File defaultFile = new File(DocExploreTool.getHomeDir(), "Untitled");
	boolean recovery = false;
	
	public ATApp(DocExploreDataLink libLink, Startup startup) throws Exception
	{
		super(Lang.s("atTitle"));
		
		this.startup = startup;
		this.libLink = libLink;
		this.host = new ATAppHost(this, startup);
		
		this.menu = new ATMenu(host);
		setJMenuBar(menu);
		
		//UIManager.put("swing.boldMetal", Boolean.FALSE);

		this.documentPanel = new JPanel(new BorderLayout());
		
		this.getContentPane().setLayout(new BorderLayout());
		this.centerPanel = new JPanel(new BorderLayout());//new LooseGridLayout(1, 0, 1, 1, false, true, SwingConstants.LEFT, SwingConstants.TOP));
		this.getContentPane().add(centerPanel, BorderLayout.CENTER);
		
		//this.menuBar = new MainMenuBar(this);
		//setJMenuBar(menuBar);
		
		centerPanel.add(documentPanel, BorderLayout.CENTER);
		
		startup.screen.setText("Initializing toolbar");
		this.toolBar = new ATToolBar(this);
		this.getContentPane().add(toolBar, BorderLayout.NORTH);
		this.statusBar = new AppStatusBar(host);
		this.getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		recovery = defaultFile.exists();
		DataLink fslink = new DataLinkFS2Source(defaultFile.getAbsolutePath()).getDataLink();
		fslink.setProperty("autoWrite", false);
		host.setLink(fslink);
		
		host.notifyActiveDocumentChanged();
		
		pack();
	}
	
	public void resetComponents()
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				host.historyManager.reset(host.getLink().supportsHistory() ? 20 : -1);
				getContentPane().invalidate();
				getContentPane().validate();
				repaint();
			}
		}, documentPanel);
	}
	
	public void removePanel() throws IllegalArgumentException
	{
		if (document != null)
			document.hidden();
		document = null;
		documentPanel.removeAll();
		documentPanel.validate();
		documentPanel.invalidate();
		documentPanel.repaint();
	}
	
	//DocumentPanel bookPanel = null;
	public DocumentPanel setPanel(final AnnotatedObject document, Object param)
	{
		if (this.document != null && document == this.document.getDocument())
			return this.document;
		try
		{
			final DocumentPanel [] panel = {null};
			/*if (bookPanel != null && bookPanel.getDocument() == document)
				panel[0] = bookPanel;
			else */GuiUtils.blockUntilComplete(new Runnable() {@Override public void run()
			{
				try {panel[0] = new DocumentPanel(host, document, param);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, false);}
			}}, ATApp.this);
			if (panel[0] == null)
				return null;
			if (this.document != null)
				this.document.hidden();
			documentPanel.removeAll();
			documentPanel.add(panel[0]);
			panel[0].shown();
			this.document = panel[0];
			documentPanel.validate();
			documentPanel.invalidate();
			documentPanel.repaint();
//			if (document instanceof Book)
//				bookPanel = panel[0];
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, false);}
		return null;
	}
	
	public AnnotatedObject baseDocument(AnnotatedObject document) {return document instanceof Region ? ((Region)document).getPage() : document;}
	public boolean isCurrentDocument(AnnotatedObject document)
	{
		if (this.document == null)
			return document == null;
		document = baseDocument(document);
		AnnotatedObject curDocument = baseDocument(this.document.getDocument());
		return document.getClass() == curDocument.getClass() && document.getId() == curDocument.getId();
	}
	
	public void close()
	{
		processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	boolean quit()
	{
		if (!menu.requestSave())
			return false;
		try {FileUtils.deleteDirectory(defaultFile);}
		catch (Exception e) {e.printStackTrace();}
		setVisible(false);
		return true;
	}
}
