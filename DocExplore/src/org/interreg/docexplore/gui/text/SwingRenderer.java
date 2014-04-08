/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui.text;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.interreg.docexplore.reader.book.roi.Java2DRenderer;

public class SwingRenderer
{
	JTextPane pane;
	
	public SwingRenderer()
	{
		this.pane = new JTextPane();
		pane.setOpaque(false);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setBackground(new Color(0, 0, 0, 0));
		pane.getInsets().set(0, 0, 0, 0);
		pane.setEditorKit(new HTMLEditorKit());
	}
	
	public BufferedImage getImage(String text, int width, Color background)
	{
		text = text.replace("<html>", "").replace("</html>", "").replaceAll("\n", "<br/>&nbsp");
		pane.setText("<html>"+text+"<br/>&nbsp;</html>");
		pane.setSize(width, 1);
		Rectangle r = null;
		try {r = pane.modelToView(((HTMLDocument)pane.getDocument()).getEndPosition().getOffset()-1);}
		catch (Exception e) {throw new RuntimeException(e);}
		pane.setSize(width, r.y);
	   
		BufferedImage res = new BufferedImage(width, r.y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = res.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setBackground(background);
		g.clearRect(0, 0, res.getWidth(), res.getHeight());
	    pane.paint(g);

		return res;
	}
	public int render(String text, BufferedImage res, Color background)
		{return render(text, res, res.getWidth(), background);}
	public int render(String text, BufferedImage res, int width, Color background)
	{
		text = text.replace("<html>", "").replace("</html>", "").replaceAll("\n", "<br/>&nbsp");
		System.out.println(text);
		pane.setText("<html>"+text+"<br/>&nbsp;</html>");
		pane.setSize(width, 1);
		Rectangle r = null;
		try {r = pane.modelToView(((HTMLDocument)pane.getDocument()).getEndPosition().getOffset()-1);}
		catch (Exception e) {throw new RuntimeException(e);}
		pane.setSize(width, r.y);
		
		Graphics2D g = res.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setBackground(background);
		g.clearRect(0, 0, Math.min(width, res.getWidth()), Math.min(r.y, res.getHeight()));
		pane.paint(g);
		
		return r.y;
	}
	
	public static void main(String [] args) throws Exception
	{
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			if ("Nimbus".equals(info.getName()))
				{UIManager.setLookAndFeel(info.getClassName()); break;}
		
//		BufferedImage image = new SwingRenderer().sr.getImage("<html><font style=\"font-size:32px\" color=\"#ffffff\" face=\"Times New Roman\"><i>One option would be to replace the KeySelectionManager interface with your own. You want to replace the JComboBox.KeySelectionManager as it is responsible for taking the inputted char and returns the row number (as an int) which should be selected.</i></font></html>", 
//				600, new Color(0, 0, 0, 0));
		BufferedImage image = new Java2DRenderer().getImage("<html><div style=\"margin-left:4px; margin-right:4px; font-size:32px; color:#ffffff; font-family:Arial\"><i>A glyph is a shape used to render a character or a sequence of characters. In simple writing systems, such as Latin, typically one glyph represents one character. In general, however, characters and glyphs do not have one-to-one correspondence. For example, the character 'á' LATIN SMALL LETTER A WITH ACUTE, can be represented by two glyphs: one for 'a' and one for '´'. On the other hand, the two-character string \"fi\" can be represented by a single glyph, an \"fi\" ligature. In complex writing systems, such as Arabic or the South and South-East Asian writing systems, the relationship between characters and glyphs can be more complicated and involve context-dependent selection of glyphs as well as glyph reordering. A font encapsulates the collection of glyphs needed to render a selected set of characters as well as the tables needed to map sequences of characters to corresponding sequences of glyphs.</i></div></html>", 
			512, new Color(0, 0, 0, 255));
		
		JFrame frame = new JFrame();
		frame.add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	
}
