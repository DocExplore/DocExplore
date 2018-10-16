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
			ExportOptions options = ExportOptions.getOptions(host.getFrame(), host.plugins.metaDataPlugins, ReaderExport);
			if (options == null)
				return;
			ExportDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run()
				{
					try {host.readerExporter.doExport(host.getLink(), options);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				public float getProgress() {return (float)host.readerExporter.progress[0];}
			}, host.getFrame());
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("exportReader")+"</b><br>"+Lang.s("exportReaderDesc")+"</html>"));
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("export-web-128x128.png")) {public void actionPerformed(ActionEvent arg0)
		{
			ExportOptions options = ExportOptions.getOptions(host.getFrame(), host.plugins.metaDataPlugins, WebExport);
			if (options == null)
				return;
			ExportDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run()
				{
					try {host.webExporter.doExport(host.getLink(), false, options);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				public float getProgress() {return (host.webExporter.copyComplete ? .5f : 0f)+(float)(.5*host.webExporter.progress[0]);}
			}, host.getFrame());
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("exportWeb")+"</b><br>"+Lang.s("exportWebDesc")+"</html>"));
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("export-mobile-128x128.png")) {public void actionPerformed(ActionEvent arg0)
		{
			ExportOptions options = ExportOptions.getOptions(host.getFrame(), host.plugins.metaDataPlugins, MobileExport);
			if (options == null)
				return;
			ExportDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run()
				{
					try {host.webExporter.doExport(host.getLink(), true, options);}
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
