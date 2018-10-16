/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.app.editors;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.gui.WrapLayout;

@SuppressWarnings("serial")
public class EditorHeader extends JPanel
{
	private JLabel titleLabel;
	public JPanel titlePanel, rightPanel;
	
	public EditorHeader()
	{
		super(new BorderLayout());
		
		setBackground(Color.white);
		
		WrapLayout leftLayout = new WrapLayout(WrapLayout.LEFT);
		leftLayout.setHgap(20);
		this.titlePanel = new JPanel(leftLayout);
		this.titleLabel = new JLabel("", SwingConstants.LEFT);
		titlePanel.add(titleLabel);
		JPanel leftPanel = new JPanel(new LooseGridLayout(1, 1, 0, 0, false, false, SwingConstants.LEFT, SwingConstants.TOP, false, false));
		leftPanel.setOpaque(false);
		leftPanel.add(titlePanel);
		add(leftPanel, BorderLayout.WEST);
		
		JPanel centerPanel = new JPanel(new LooseGridLayout(1, 1, 0, 0, true, false, SwingConstants.RIGHT, SwingConstants.CENTER, true, true));
		centerPanel.setOpaque(false);
		WrapLayout rightLayout = new WrapLayout(WrapLayout.RIGHT);
		rightLayout.setHgap(20);
		this.rightPanel = new JPanel(rightLayout);
		centerPanel.add(rightPanel);
		rightPanel.setOpaque(false);
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public void setTitle(String title, String subTitle)
	{
		titleLabel.setText("<html><big>"+title+"</big><br>"+subTitle+"</html></big>");
	}
	public void setTitleIcon(Icon icon)
	{
		titleLabel.setIcon(icon);
	}
}
