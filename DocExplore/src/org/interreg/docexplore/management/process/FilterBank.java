package org.interreg.docexplore.management.process;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.interreg.docexplore.management.process.filters.BackgroundRemoval;
import org.interreg.docexplore.management.process.filters.Sauvola;

public class FilterBank
{
	Set<Filter> filters;
	
	public FilterBank()
	{
		this.filters = new HashSet<Filter>();
		filters.add(new Sauvola());
		filters.add(new BackgroundRemoval());
	}
	
	@SuppressWarnings("serial")
	public JPopupMenu buildMenu(final FilterSequencePanel sequence)
	{
		JPopupMenu menu = new JPopupMenu();
		for (final Filter filter : filters)
			menu.add(new JMenuItem(new AbstractAction(filter.getName()) {
				public void actionPerformed(ActionEvent arg0) {sequence.addItem(filter);}}));
		return menu;
	}
}
