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
			new ByteArrayInputStream(("<xhtml><p style=\"text-align:justify\">"+StringUtils.escapeXmlChars("<<>Vous avez ici sous votre main, un exemple typique d��criture cun�iforme sur tablette d�argile. L��criture cun�iforme compte parmi les premi�res formes complexes connues d��criture, qui sont apparues en M�sopotamie (r�gion correspondant pour sa plus grande part � l'Irak actuel). Les scribes m�sopotamiens consignaient entre autres des transactions commerciales, en tra�ant ces symboles dans la glaise, � l�aide de roseaux taill�s appel�s calames. Ainsi la tablette que vous avez ici fait l�inventaire d�un troupeau de ch�vres et de moutons. L�argile de qualit� �tant rare, les tablettes �taient fr�quemment remodel�es et r�utilis�es.")+"</p></xhtml>").getBytes("UTF-8")));
		Java2DRenderer renderer = new Java2DRenderer(doc, 800, -1);
		BufferedImage res = renderer.getImage();
		
		JFrame win = new JFrame("FSTest");
		win.add(new JLabel(new ImageIcon(res)));
		win.pack();
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setVisible(true);
	}
}
