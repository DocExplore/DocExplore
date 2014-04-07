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
