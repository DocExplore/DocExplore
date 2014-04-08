/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

@SuppressWarnings("serial")
public class TextElementTransferHandler extends TransferHandler
{
	TextElement element;
	TransferHandler defaultHandler;
	
	public TextElementTransferHandler(TextElement pane)
	{
		this.element = pane;
		this.defaultHandler = pane.textPane.getTransferHandler();
	}
	
	public boolean importData(JComponent comp, Transferable t)
	{
		try
		{
			DataFlavor [] dfs = t.getTransferDataFlavors();
			String text = null;
			for (DataFlavor df : dfs)
			{
				if (df.getMimeType().contains("text/plain;") && df.getRepresentationClass() == String.class)
					{text = (String)t.getTransferData(df); break;}
			}
			if (text == null)
				return false;
			System.out.println(">>>"+element.textPane.getCaretPosition());
			element.textPane.getDocument().insertString(element.textPane.getCaretPosition(), text, null); 
			System.out.println(element.textPane.getText());
		}
		catch (Exception e) {e.printStackTrace();}
		return true;
	}
	public boolean importData(TransferSupport support)
	{
		return super.importData(support);
	}
	protected Transferable createTransferable(JComponent c) {return new StringSelection(element.textPane.getSelectedText());}
	protected void exportDone(JComponent source, Transferable data, int action) {}
	
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {return defaultHandler.canImport(comp, transferFlavors);}
	public boolean canImport(TransferSupport support) {return defaultHandler.canImport(support);}
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {defaultHandler.exportAsDrag(comp, e, action);}
	public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {defaultHandler.exportToClipboard(comp, clip, action);}
	public int getSourceActions(JComponent c) {return defaultHandler.getSourceActions(c);}
	public Icon getVisualRepresentation(Transferable t) {return defaultHandler.getVisualRepresentation(t);}
}
