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
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class ExportDialog extends JDialog
{
	public final static int ReaderExport = 0;
	public final static int WebExport = 1;
	public final static int MobileExport = 2;
	
	public ExportDialog(final ATAppHost host)
	{
		super((Frame)null, Lang.s("generalExportDialog"), true);
		
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
					try {host.readerExporter.doExport(host.getLink());}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				public float getProgress() {return (float)host.readerExporter.progress[0];}
			}, host.getFrame());
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("exportReader")+"</b><br>"+Lang.s("exportReaderDesc")+"</html>"));
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("export-web-128x128.png")) {public void actionPerformed(ActionEvent arg0)
		{
			ExportDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run()
				{
					try {host.webExporter.doExport(host.getLink(), false);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				public float getProgress() {return (host.webExporter.copyComplete ? .5f : 0f)+(float)(.5*host.webExporter.progress[0]);}
			}, host.getFrame());
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("exportWeb")+"</b><br>"+Lang.s("exportWebDesc")+"</html>"));
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("export-mobile-128x128.png")) {public void actionPerformed(ActionEvent arg0)
		{
			ExportDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run()
				{
					try {host.webExporter.doExport(host.getLink(), true);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				public float getProgress() {return (host.webExporter.copyComplete ? .5f : 0f)+(float)(.5*host.webExporter.progress[0]);}
			}, host.getFrame());
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("exportMobile")+"</b><br>"+Lang.s("exportMobileDesc")+"</html>"));
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(new JButton(new AbstractAction(Lang.s("generalCancelLabel")) {@Override public void actionPerformed(ActionEvent arg0)
		{
			ExportDialog.this.setVisible(false);
		}}));
		top.add(buttons, BorderLayout.SOUTH);
		
		pack();
	}
}
