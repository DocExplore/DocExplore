/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.align;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class EditorPanel extends JPanel
{
	JPanel view;
	LabeledImageViewer editor;
	LineSeparatorPanel lsp;
	LineExtentPanel lep;
	
	public EditorPanel(LabeledImageViewer editor)
	{
		super(new BorderLayout());
		
		this.view = new JPanel(new BorderLayout());
		this.editor = editor;
		this.lsp = new LineSeparatorPanel(editor);
		this.lep = new LineExtentPanel(editor, lsp);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		/*buttonPanel.add(new IconButton("view-image-48x48.png", "Voir l'image originale", new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				EditorPanel.this.editor.showLabels = false;
				EditorPanel.this.editor.repaint();
			}
		}));
		buttonPanel.add(new IconButton("view-labels-48x48.png", "Voir les composantes", new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				EditorPanel.this.editor.showLabels = true;
				EditorPanel.this.editor.repaint();
			}
		}));*/
		
		view.add(editor, BorderLayout.CENTER);
		add(view, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.NORTH);
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {resizeEditor();}});
	}
	
	private void resizeEditor()
	{
		/*if (editor.displayBuffer == null)
		{
			view.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			return;
		}
		
		double xk = (view.getWidth())*1./editor.displayBuffer.getWidth();
		double yk = (view.getHeight())*1./editor.displayBuffer.getHeight();
		//if (xk > 1) xk = 1;
		//if (yk > 1) yk = 1;
		double ratio = Math.min(xk, yk);
		int mw = (int)(view.getWidth()-ratio*editor.displayBuffer.getWidth());
		int mh = (int)(view.getHeight()-ratio*editor.displayBuffer.getHeight());
		view.setBorder(BorderFactory.createEmptyBorder(0, mw/2, mh, mw/2));
		view.invalidate();
		view.validate();*/
		editor.setupView();
		repaint();
	}
	
	public void setImage(AnalyzedImage limage)
	{
		editor.setImage(limage);
		resizeEditor();
	}
	
	public void setNLines(int n)
	{
		lsp.setNSeparators(n+1);
		lep.setNLines(n);
	}
}
