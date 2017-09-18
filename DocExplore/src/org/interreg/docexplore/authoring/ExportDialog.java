package org.interreg.docexplore.authoring;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class ExportDialog extends JDialog
{
	public final static int ReaderExport = 0;
	public final static int WebExport = 1;
	public final static int MobileExport = 2;
	
	public ExportDialog(final AuthoringToolFrame authoringTool)
	{
		super((Frame)null, XMLResourceBundle.getBundledString("generalExportDialog"), true);
		
		JPanel top = new JPanel(new BorderLayout());
		setContentPane(top);
		
		JPanel panel = new JPanel(new LooseGridLayout(0, 2, 10, 10, true, false, SwingConstants.CENTER, SwingConstants.CENTER, true, false));
		top.add(panel, BorderLayout.CENTER);
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("export-reader-128x128.png")) {public void actionPerformed(ActionEvent arg0)
		{
			ExportDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run()
				{
					try {authoringTool.readerExporter.doExport(authoringTool.editor.link);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				public float getProgress() {return (float)authoringTool.readerExporter.progress[0];}
			}, authoringTool.editor);
		}}));
		panel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("exportReader")+"</b><br>"+XMLResourceBundle.getBundledString("exportReaderDesc")+"</html>"));
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("export-web-128x128.png")) {public void actionPerformed(ActionEvent arg0)
		{
			ExportDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run()
				{
					try {authoringTool.webExporter.doExport(authoringTool.editor.link, false);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				public float getProgress() {return (authoringTool.webExporter.copyComplete ? .5f : 0f)+(float)(.5*authoringTool.webExporter.progress[0]);}
			}, authoringTool.editor);
		}}));
		panel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("exportWeb")+"</b><br>"+XMLResourceBundle.getBundledString("exportWebDesc")+"</html>"));
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("export-mobile-128x128.png")) {public void actionPerformed(ActionEvent arg0)
		{
			ExportDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run()
				{
					try {authoringTool.webExporter.doExport(authoringTool.editor.link, true);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				public float getProgress() {return (authoringTool.webExporter.copyComplete ? .5f : 0f)+(float)(.5*authoringTool.webExporter.progress[0]);}
			}, authoringTool.editor);
		}}));
		panel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("exportMobile")+"</b><br>"+XMLResourceBundle.getBundledString("exportMobileDesc")+"</html>"));
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel")) {@Override public void actionPerformed(ActionEvent arg0)
		{
			ExportDialog.this.setVisible(false);
		}}));
		top.add(buttons, BorderLayout.SOUTH);
		
		pack();
	}
}
