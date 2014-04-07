package org.interreg.docexplore.management.align;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class TranscriptionPanel extends JPanel implements LabeledImageViewer.EditorListener
{
	EditorPanel panel;
	
	JList lines;
	DefaultListModel linesModel;
	LineBreakdown [] breakdowns;
	
	AlignmentBatch batch;
	AlignmentSetter as;
	
	public TranscriptionPanel(EditorPanel panel)
	{
		super(new FlowLayout(FlowLayout.LEFT));
		//setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBorder(BorderFactory.createTitledBorder(" "));
		
		this.panel = panel;
		this.linesModel = new DefaultListModel();
		this.lines = new JList(linesModel);
		this.breakdowns = new LineBreakdown [0];
		this.as = new AlignmentSetter(this);
		
		lines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lines.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				TranscriptionPanel.this.panel.editor.repaint();
			}});
		add(new JScrollPane(lines));
		
		JPanel buttonPanel = new JPanel(new LooseGridLayout(2, 0, 5, 5, SwingConstants.CENTER, SwingConstants.TOP));
		buttonPanel.add(new IconButton("pencil-48x48.png", "Modifier la transcription", new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				final JDialog dialog = new JDialog((Frame)null, "Transcription", true);
				dialog.setLayout(new BorderLayout());
				StringBuffer trans = new StringBuffer();
				for (int i=0;i<linesModel.getSize();i++)
				{
					if (i > 0) trans.append('\n');
					trans.append(linesModel.get(i));
				}
				JTextArea area = new JTextArea(linesModel.getSize()+1, 64);
				area.setText(trans.toString());
				dialog.add(new JScrollPane(area), BorderLayout.CENTER);
				
				final boolean [] ok = {false};
				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 5));
				buttonPanel.add(new JButton(new AbstractAction("OK") 
					{public void actionPerformed(ActionEvent e) {ok[0] = true; dialog.setVisible(false);}}));
				buttonPanel.add(new JButton(new AbstractAction("Annuler") 
					{public void actionPerformed(ActionEvent e) {dialog.setVisible(false);}}));
				dialog.add(buttonPanel, BorderLayout.SOUTH);
				
				dialog.pack();
				GuiUtils.centerOnScreen(dialog);
				dialog.setVisible(true);
				
				if (ok[0])
				{
					setTranscription(area.getText(), true);
					TranscriptionPanel.this.panel.editor.repaint();
				}
			}}));
		buttonPanel.add(new IconButton("gears-48x48.png", "Détection automatique", new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (lines.getSelectedIndex() < 0)
				{
					AlignmentDetector.detect(TranscriptionPanel.this);
					TranscriptionPanel.this.panel.repaint();
				}
				else
				{
					AlignmentDetector.springDetect(TranscriptionPanel.this, lines.getSelectedIndex());
					TranscriptionPanel.this.panel.repaint();
				}
			}}));
		buttonPanel.add(new IconButton("quit-48x48.png", "Ne rien sélectionner", new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (as.curLine != -1)
					{as.cancel(); as.curLine = -1; TranscriptionPanel.this.panel.repaint();}
				else lines.clearSelection();
			}}));
		buttonPanel.add(new IconButton("hand2-48x48.png", "Détection manuelle", new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (linesModel.getSize() < 1)
					return;
				
				if (lines.getSelectedIndex() == -1)
					as.setLine(0);
				else as.setLine(lines.getSelectedIndex());
			}}));
		final JSlider slider = new JSlider(JSlider.VERTICAL, 40, 400, 120);
		slider.setPreferredSize(new Dimension(48, 48));
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				LineBreakdown.setFontSize(slider.getValue()/10f);
				TranscriptionPanel.this.panel.repaint();
			}});
		buttonPanel.add(slider);
		add(buttonPanel);
		
		this.batch = new AlignmentBatch(this);
		
		panel.editor.addEditorListener(this);
	}
	
	public void mouseMoved(LabeledImageViewer editor, double x, double y)
	{
		boolean changed = false;
		for (int i=0;i<breakdowns.length;i++)
		{
			LineBreakdown lb = breakdowns[i];
			//double r = editor.fromScreenLength(5);
			changed = changed || lb.mouseMoved(i, x, y, editor);
		}
		changed = changed || as.mouseMoved(x, y);
		
		if (changed)
			panel.editor.repaint();
	}
	public void mouseDragged(LabeledImageViewer editor, int button, double x, double y)
	{
		if ((button & InputEvent.BUTTON1_MASK) == 0)
			return;
		
		boolean changed = false;
		for (int i=0;i<breakdowns.length;i++)
		{
			LineBreakdown lb = breakdowns[i];
			changed = changed || lb.mouseDragged(i, x, y);
		}
		if (changed)
			panel.editor.repaint();
	}
	public void mouseClicked(LabeledImageViewer editor, int button, int modifiers, double x, double y) 
		{as.mouseClicked(button, x, y);}
	
	public void setTranscription(String trans) {setTranscription(trans, false);}
	public void setTranscription(String trans, boolean update)
	{
		String [] transLines = trans.split("\n");
		if (transLines.length != linesModel.getSize())
			update = false;
		
		Set<Integer> updated = null;
		if (update)
		{
			updated = new TreeSet<Integer>();
			for (int i=0;i<transLines.length;i++)
				if (!transLines[i].equals(linesModel.get(i).toString()))
					updated.add(i);
		}
		else linesModel.clear();
		
		for (int i=0;i<transLines.length;i++)
			if (!update || updated.contains(i))
		{
			String line = transLines[i];
			String trimmed = line.trim();
			if (trimmed.length() < 1)
				continue;
			if (update)
				linesModel.setElementAt(trimmed, i);
			else linesModel.addElement(trimmed);
		}
		if (!update)
		{
			panel.setNLines(linesModel.getSize());
			this.breakdowns = new LineBreakdown [linesModel.size()];
		}
		
		for (int i=0;i<breakdowns.length;i++)
			if (!update || updated.contains(i))
		{
			breakdowns[i] = new LineBreakdown(this);
			breakdowns[i].setLine((String)linesModel.get(i));
		}
		
		/*if (!update)
			AlignmentDetector.detect(this);
		else
		{
			for (int i=0;i<transLines.length;i++)
				if (!update || updated.contains(i))
					AlignmentDetector.springDetect(this, i);
		}*/
		panel.repaint();
	}
	
	public void rendered(LabeledImageViewer editor, Graphics2D g)
	{
		if (panel.lsp.separators.length < 2)
			return;
		
		//int w = editor.canvas.getWidth();
		int h = editor.displayBuffer.getHeight();
		int y0 = 0;//(editor.canvas.getHeight()-h)/2;
		g.setColor(new Color(255, 255, 255, 127));
		
		g.fillRect(0, y0, editor.displayBuffer.getWidth(), (int)(h*panel.lsp.separators[0].y));
		int lastl = (int)(y0+h*panel.lsp.separators[panel.lsp.separators.length-1].y);
		g.fillRect(0, lastl, editor.displayBuffer.getWidth(), y0+h-lastl);
		
		for (int i=0;i<breakdowns.length;i++)
		{
			LineBreakdown lb = breakdowns[i];
			lb.render(g, i);
		}
		
		int index = lines.getSelectedIndex();
		if (index == -1)
			return;
		
		g.setColor(new Color(255, 255, 255, 127));
		for (int i=0;i<linesModel.getSize();i++)
			if (i != index)
		{
			int l0 = (int)(y0+h*panel.lsp.separators[i].y);
			int l1 = (int)(y0+h*panel.lsp.separators[i+1].y);
			g.fillRect(0, l0, editor.displayBuffer.getWidth(), l1-l0);
		}
	}
}
