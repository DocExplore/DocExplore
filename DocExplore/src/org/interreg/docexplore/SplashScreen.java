/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.interreg.docexplore.util.ImageUtils;

public class SplashScreen extends JPanel
{
	private static final long serialVersionUID = -4494494801280290970L;
	
	public final static String versionString = "2.0";

	JLabel status;
	
	@SuppressWarnings("serial")
	public SplashScreen(String logo)
	{
		super(new BorderLayout());
		
		setBackground(Color.white);
		status = new JLabel("");
		status.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
		//JLabel version = new JLabel(versionString);
		//version.setVerticalAlignment(SwingConstants.BOTTOM);
		add(new JLabel(ImageUtils.getIcon(logo)) {protected void paintComponent(Graphics _g)
			{
				super.paintComponent(_g);
				Graphics2D g = (Graphics2D)_g;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setFont(Font.decode("Arial-italic-18"));
				g.setColor(Color.black);
				g.drawString("version "+versionString, 10, 20);
				
			}}, 
		BorderLayout.CENTER);
		add(status, BorderLayout.SOUTH);
		//add(version, BorderLayout.EAST);
		setBorder(BorderFactory.createLineBorder(Color.black, 2));
	}
	
	public void setText(String text)
	{
		status.setText(text);
		status.repaint();
	}
}
