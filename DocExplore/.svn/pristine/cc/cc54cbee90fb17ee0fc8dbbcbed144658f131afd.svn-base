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
