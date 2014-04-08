/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.interreg.docexplore.util.GuiUtils;

public abstract class ExpandingItem extends JPanel
{
	private static final long serialVersionUID = -4835832257983193792L;

	boolean isExpanded;
	ExpandingItemList list;
	
	public ExpandingItem()
	{
		this.isExpanded = false;
		this.list = null;
		
		addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON1 || list == null || isExpanded)
					return;
				list.contractAll();
				expand();
			}
		});
	}
	
	public void expand()
	{
		if (list == null || isExpanded)
			return;
		isExpanded = true;
		removeAll();
		GuiUtils.blockUntilComplete(new Runnable() {public void run() {fillExpandedState();}}, this);
		
		list.getParent().validate();
	}
	public void contract()
	{
		if (list == null || !isExpanded)
			return;
		isExpanded = false;
		removeAll();
		fillContractedState();
		
		list.getParent().validate();
	}
	
	protected abstract void fillContractedState();
	protected abstract void fillExpandedState();

//	protected void paintChildren(Graphics g)
//	{
//		//if (isOpaque())
//		{//System.out.println(getBackground().getRed()+","+getBackground().getGreen()+","+getBackground().getBlue()+","+getBackground().getAlpha());
//			g.setColor(getBackground());
//			g.fillRect(0, 0, getWidth(), getHeight());
//		}
//		
//		super.paintChildren(g);
//	}
	
	
}
