/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book.roi;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.xml.parsers.DocumentBuilderFactory;

import org.interreg.docexplore.util.StringUtils;
import org.w3c.dom.Document;
import org.xhtmlrenderer.swing.Java2DRenderer;

public class FSTest
{
	public static void main(String [] args) throws Exception
	{
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
			new ByteArrayInputStream(("<xhtml><p style=\"text-align:justify\">"+StringUtils.escapeXmlChars("<<>Vous avez ici sous votre main, un exemple typique d’écriture cunéiforme sur tablette d’argile. L’écriture cunéiforme compte parmi les premières formes complexes connues d’écriture, qui sont apparues en Mésopotamie (région correspondant pour sa plus grande part à l'Irak actuel). Les scribes mésopotamiens consignaient entre autres des transactions commerciales, en traçant ces symboles dans la glaise, à l’aide de roseaux taillés appelés calames. Ainsi la tablette que vous avez ici fait l’inventaire d’un troupeau de chèvres et de moutons. L’argile de qualité étant rare, les tablettes étaient fréquemment remodelées et réutilisées.")+"</p></xhtml>").getBytes("UTF-8")));
		Java2DRenderer renderer = new Java2DRenderer(doc, 800, -1);
		BufferedImage res = renderer.getImage();
		
		JFrame win = new JFrame("FSTest");
		win.add(new JLabel(new ImageIcon(res)));
		win.pack();
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setVisible(true);
	}
}
