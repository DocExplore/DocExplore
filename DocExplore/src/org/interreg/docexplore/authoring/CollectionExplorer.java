/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
