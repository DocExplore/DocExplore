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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Pair;
import org.interreg.docexplore.util.StringUtils;

public class AlignmentBatch
{
	JList aligns;
	DefaultListModel alignsModel;
	
	public AlignmentBatch(final TranscriptionPanel tp)
	{
		this.alignsModel = new DefaultListModel();
		this.aligns = new JList(alignsModel);
		
		JLabel filler = new JLabel();
		filler.setPreferredSize(new Dimension(30, 30));
		tp.add(filler);
		
		aligns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tp.add(new JScrollPane(aligns));
		
		JPanel buttonPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, SwingConstants.CENTER, SwingConstants.TOP));
		buttonPanel.add(new IconButton("browse-48x48.png", "Ouvrir une liste de transcriptions", new ActionListener() {
			File current = new File(".");
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setMultiSelectionEnabled(false);
				chooser.setCurrentDirectory(current);
				int res = chooser.showOpenDialog(null);
				if (res == JFileChooser.APPROVE_OPTION)
				{
					current = chooser.getSelectedFile();
					try {readBatch(current);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
			}
		}));
		buttonPanel.add(new IconButton("next-48x48.png", "Valider et ouvrir la transcription suivante", new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try {writeCurrent(tp);}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				
				int index = aligns.getSelectedIndex();
				if (alignsModel.getSize() > index+1)
					aligns.setSelectedIndex(index+1);
				else
				{
					aligns.clearSelection();
					tp.panel.setImage(null);
					tp.setTranscription(null);
				}
			}
		}));
		
		aligns.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;
				try
				{
					@SuppressWarnings("unchecked")
					Pair<File, String> pair = (Pair<File, String>)alignsModel.get(aligns.getSelectedIndex());
					tp.panel.setImage(new AnalyzedImage(ImageUtils.read(pair.first), false, false));
					if (!readCurrent(tp))
					{
						String trans = pair.second.replaceAll("\\(", "").replaceAll("\\)", "");
						tp.setTranscription(trans);
					}
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}});
		
		tp.add(buttonPanel);
	}
	
	boolean readCurrent(TranscriptionPanel tp) throws Exception
	{
		@SuppressWarnings("unchecked")
		Pair<File, String> pair = (Pair<File, String>)alignsModel.get(aligns.getSelectedIndex());
		int ind = pair.first.getName().lastIndexOf('.');
		if (ind == -1) ind = pair.first.getName().length();
		String outputFilename = pair.first.getName().substring(0, ind)+".xml";
		File output = new File(pair.first.getParent(), outputFilename);
		if (!output.exists())
			return false;
		
		String xml = null;
		try {xml = StringUtils.readFile(output);}
		catch (Exception e)
		{
			ErrorHandler.defaultHandler.submit(e);
			return false;
		}
		
		String [] lines = xml.split("<Separator");
		StringBuffer trans = new StringBuffer();
		for (int i=1;i<lines.length-1;i++)
		{
			if (i > 1)
				trans.append('\n');
			
			String [] tokens = lines[i].split("<Token");
			for (int j=1;j<tokens.length;j++)
			{
				int end = tokens[j].indexOf("</");
				if (end == -1)
					continue;
				if (j > 1)
					trans.append(' ');
				trans.append(tokens[j].substring(1, end));
			}
		}
		tp.setTranscription(trans.toString());
		
		Vector<Integer> lineIndices = new Vector<Integer>();
		int curSep = xml.indexOf("<Separator");
		if (curSep < 0)
		{
			ErrorHandler.defaultHandler.submit(new Exception("Invalid format : "+output.getAbsolutePath()));
			return true;
		}
		
		while (curSep >= 0)
		{
			int nextSep = xml.indexOf("<Separator", curSep+10);
			if (nextSep < 0)
				nextSep = xml.length();
			
			int sepatt = xml.indexOf("y=\"", curSep);
			double sepy = Double.parseDouble(xml.substring(sepatt+3, xml.indexOf("\"", sepatt+3)));
			
			ParameterizedLine line = new ParameterizedLine(sepy);
			
			int curKnob = xml.indexOf("<Knob", curSep);
			while (curKnob > 0 && curKnob < nextSep)
			{
				int knobatt = xml.indexOf("x=\"", curKnob);
				double knobx = Double.parseDouble(xml.substring(knobatt+3, xml.indexOf("\"", knobatt+3)));
				knobatt = xml.indexOf("dy=\"", curKnob);
				double knobdy = Double.parseDouble(xml.substring(knobatt+4, xml.indexOf("\"", knobatt+4)));
				knobatt = xml.indexOf("ray=\"", curKnob);
				double knobray = Double.parseDouble(xml.substring(knobatt+5, xml.indexOf("\"", knobatt+5)));
				line.knobs.add(new ParameterizedLine.Knob(knobx, knobdy, knobray));
				
				curKnob = xml.indexOf("<Knob", curKnob+5);
			}
			
			tp.panel.lsp.separators[lineIndices.size()] = line;
			lineIndices.add(curSep);
			
			curSep = xml.indexOf("<Separator", curSep+10);
		}
		
		for (int i=0;i<lineIndices.size()-1;i++)
		{
			curSep = lineIndices.get(i);
			
			int ext = xml.indexOf("<Extent", curSep);
			int extatt = xml.indexOf("from=\"", ext);
			tp.panel.lep.extents[i][0] =
				Double.parseDouble(xml.substring(extatt+6, xml.indexOf("\"", extatt+6)));
			extatt = xml.indexOf("to=\"", ext);
			tp.panel.lep.extents[i][1] =
				Double.parseDouble(xml.substring(extatt+4, xml.indexOf("\"", extatt+4)));
			
			int curDelim = xml.indexOf("<Delimiter>", curSep);
			int delimIndex = 0;
			while (curDelim > 0 && curDelim < lineIndices.get(i+1))
			{
				tp.breakdowns[i].delimiters[delimIndex++] =  
					Double.parseDouble(xml.substring(curDelim+11, xml.indexOf("</Delimiter>", curDelim+11)));
				curDelim = xml.indexOf("<Delimiter>", curDelim+11);
			}
		}
		
		return true;
	}
	
	void writeCurrent(TranscriptionPanel tp) throws Exception
	{
		@SuppressWarnings("unchecked")
		Pair<File, String> pair = (Pair<File, String>)alignsModel.get(aligns.getSelectedIndex());
		int ind = pair.first.getName().lastIndexOf('.');
		if (ind == -1) ind = pair.first.getName().length();
		String outputFilename = pair.first.getName().substring(0, ind)+".xml";
		File output = new File(pair.first.getParent(), outputFilename);
		
		StringBuffer sb = new StringBuffer();
		sb.append("<Align>\n");
		for (int i=0;i<tp.panel.lsp.separators.length;i++)
		{
			ParameterizedLine separator = tp.panel.lsp.separators[i];
			sb.append("\t<Separator y=\"").append(separator.y).append("\">\n");
			
			for (ParameterizedLine.Knob knob : separator.knobs)
				sb.append("\t\t<Knob x=\"").append(knob.x).append("\" dy=\"").append(knob.dy).append("\" ray=\"").append(knob.ray).append("\"/>\n");
			
			if (i < tp.panel.lsp.separators.length-1)
			{
				sb.append("\t\t<Extent from=\"").append(tp.panel.lep.extents[i][0]).append("\" to=\"").append(tp.panel.lep.extents[i][1]).append("\"/>\n");
				LineBreakdown lb = tp.breakdowns[i];
				for (int j=0;j<lb.words.length;j++)
				{
					sb.append("\t\t<Token>").append(lb.words[j]).append("</Token>\n");
					if (j < lb.words.length-1)
						sb.append("\t\t<Delimiter>").append(lb.delimiters[j]).append("</Delimiter>\n");
				}
			}
			
			sb.append("\t</Separator>\n");
		}
		sb.append("</Align>");
		
		Writer writer = new OutputStreamWriter(new FileOutputStream(output, false));
		writer.write(sb.toString());
		writer.close();
	}
	
	public void setAligns(Collection<Pair<File, String>> batch)
	{
		alignsModel.removeAllElements();
		for (Pair<File, String> pair : batch)
		{
			final File file = pair.first;
			alignsModel.addElement(new Pair<File, String>(pair.first, pair.second)
			{
				public String toString() {return file.getName();}
			});
		}
		
		aligns.setSelectedIndex(0);
	}
	
	public void readBatch(File file) throws Exception
	{
		String content = StringUtils.readFile(file, "UTF-8");
		String [] images = content.split("<image");
		
		List<Pair<File, String>> aligns = new LinkedList<Pair<File,String>>();
		for (int i=1;i<images.length;i++)
		{
			int index = images[i].indexOf("src=\"");
			if (index == -1)
				continue;
			int endIndex = images[i].indexOf("\"", index+5);
			String fileName = images[i].substring(index+5, endIndex);
			if (fileName.length() < 1)
				continue;
			
			index = images[i].indexOf("label=\"");
			if (index == -1)
				continue;
			endIndex = images[i].indexOf("\"", index+7);
			String label = images[i].substring(index+7, endIndex);
			if (label.length() < 1)
				continue;
			
			aligns.add(new Pair<File, String>(new File(file.getParent(), fileName), label));
		}
		setAligns(aligns);
	}
}
