package org.interreg.docexplore.management.process;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.interreg.docexplore.gui.LooseGridLayout;

@SuppressWarnings("serial")
public class FilterPanel extends JScrollPane
{
	FilterSequencePanel sequencePanel;
	
	public FilterPanel(FilterBank bank)
	{
		super(VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JPanel panel = new JPanel(new LooseGridLayout(0, 1, 10, 10, true, true));
		
		JPanel paramPanel = new JPanel();
		paramPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
		this.sequencePanel = new FilterSequencePanel(bank, paramPanel);
		sequencePanel.setBorder(BorderFactory.createTitledBorder("Sequence"));
		
		panel.add(sequencePanel);
		panel.add(paramPanel);
		setViewportView(panel);
	}
}
