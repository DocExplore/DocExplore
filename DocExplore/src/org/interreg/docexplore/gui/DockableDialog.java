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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.util.ImageUtils;

public class DockableDialog extends JDialog
{
	private static final long serialVersionUID = -5841136997377810131L;
	JComponent anchor;
	WindowFocusListener wfl;
	double relw, relh;
	JPanel subContentPane = null;
	
	public DockableDialog(String title, JComponent anchor, double relw, double relh)
	{
		super(JOptionPane.getRootFrame(), title);
		
		this.anchor = anchor;
		this.wfl = new WindowFocusListener()
		{
			public void windowLostFocus(WindowEvent e) {setVisible(false);}
			public void windowGainedFocus(WindowEvent e) {}
		};
		this.relw = relw;
		this.relh = relh;
		
		JPanel glassPanel = new JPanel(new BorderLayout());
		JPanel dockPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		dockPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		dockPanel.setOpaque(false);
		final JToggleButton dockButton = new JToggleButton(ImageUtils.getIcon("pin-16x18.png"));
		dockButton.setToolTipText(Lang.s("dialogDockingLabel"));
		dockButton.setSelected(true);
		dockButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
				dispose();
				setUndecorated(dockButton.isSelected());
				setVisible(true);
				
				if (isUndecorated())
					SwingUtilities.invokeLater(new Runnable() {public void run() {addWindowFocusListener(wfl);}});
				else removeWindowFocusListener(wfl);
			}
		});
		dockPanel.add(dockButton);
		glassPanel.add(dockPanel, BorderLayout.NORTH);
		setGlassPane(glassPanel);
		glassPanel.setOpaque(false);
		glassPanel.setVisible(true);
		
		addWindowFocusListener(wfl);
		setUndecorated(true);
		setAlwaysOnTop(true);
	}
	
	public void setVisible(boolean visible)
	{
		if (visible && isUndecorated())
		{
			Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			setPreferredSize(new Dimension((int)(relw*(dim.width-insets.left-insets.right)), (int)(relh*(dim.height-insets.top-insets.bottom))));
			pack();
			
			if (anchor != null)
			{
				Point p = anchor.getLocation();
				SwingUtilities.convertPointToScreen(p, anchor);
				if (p.x+getWidth() > dim.width-insets.right)
					p.x = dim.width-insets.right-getWidth();
				setLocation(p);
			}
		}
		
		super.setVisible(visible);
		
		if (visible && isUndecorated())
			requestFocus();
	}
}
