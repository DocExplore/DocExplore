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

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import org.interreg.docexplore.util.ImageUtils;

public class IconButton extends JButton
{
	private static final long serialVersionUID = 5582537475781109316L;
	
	public IconButton(String iconName)
	{
		this(ImageUtils.getIcon(iconName));
	}
	public IconButton(Icon icon)
	{
		super(icon);
		setPreferredSize(new Dimension(getIcon().getIconWidth()+12, getIcon().getIconHeight()+12));
	}
	
	public IconButton(String iconName, String toolTip)
	{
		this(iconName);
		this.setToolTipText(toolTip);
	}
	public IconButton(Icon icon, String toolTip)
	{
		this(icon);
		this.setToolTipText(toolTip);
	}
	
	public IconButton(String iconName, ActionListener action)
	{
		this(iconName);
		addActionListener(action);
	}
	
	public IconButton(String iconName, String toolTip, ActionListener action)
	{
		this(iconName, action);
		this.setToolTipText(toolTip);
	}
}
