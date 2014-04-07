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
