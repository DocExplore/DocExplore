package org.interreg.docexplore.authoring;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.app.DocumentPanel;
import org.interreg.docexplore.manuscript.app.EditorSidePanel;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class CollectionExplorer extends JPanel implements EditorSidePanel
{
	CollectionExplorerHost host;
	DocumentPanel document = null;
	
	public CollectionExplorer(DataLink link, ATAppHost parent) throws Exception
	{
		super(new BorderLayout());
		
		this.host = new CollectionExplorerHost(this, parent);
		host.setLink(link);
		host.addDocument(null);
	}
	
	public AnnotatedObject baseDocument(AnnotatedObject document) {return document instanceof Region ? ((Region)document).getPage() : document;}
	public boolean isCurrentDocument(AnnotatedObject document)
	{
		if (this.document == null)
			return document == null;
		document = baseDocument(document);
		AnnotatedObject curDocument = baseDocument(document);
		return document.getClass() == curDocument.getClass() && document.getId() == curDocument.getId();
	}
	
	DocumentPanel explorer = null;
	public DocumentPanel setPanel(final AnnotatedObject document, Object param)
	{
		if (this.document != null && document == this.document.getDocument())
			return this.document;
		try
		{
			final DocumentPanel [] panel = {null};
			if (explorer != null && document == null)
				panel[0] = explorer;
			else GuiUtils.blockUntilComplete(new Runnable() {@Override public void run()
			{
				try {panel[0] = new DocumentPanel(host, document, param);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, false);}
			}}, CollectionExplorer.this);
			if (panel[0] == null)
				return null;
			if (this.document != null)
				this.document.hidden();
			removeAll();
			add(panel[0], BorderLayout.CENTER);
			this.document = panel[0];
			if (panel[0] != null)
				panel[0].shown();
			validate();
			invalidate();
			repaint();
			if (document == null)
				explorer = panel[0];
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, false);}
		return null;
	}
	public void removePanel() throws IllegalArgumentException
	{
		if (document != null)
			document.hidden();
		document = null;
		removeAll();
		validate();
		invalidate();
		repaint();
	}

	@Override public void setDocument(AnnotatedObject document) throws DataLinkException {}

	@Override public void onShow() {}
	@Override public void onHide() {}

	@Override public Component getComponent() {return this;}
	@Override public MetaData addAnnotation() {return null;}
}
