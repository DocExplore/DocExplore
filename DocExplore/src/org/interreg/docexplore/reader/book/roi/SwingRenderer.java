package org.interreg.docexplore.reader.book.roi;

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
import javax.swing.text.html.BlockView;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

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
		text = text.replace("<html>", "").replace("</html>", "").replace("\n", "<br/>");
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
	public int render(String text, BufferedImage res, Color background, int y0)
		{return render(text, res, res.getWidth(), background, y0, true);}
	public int render(String text, BufferedImage res, Color background, int y0, boolean newLine)
		{return render(text, res, res.getWidth(), background, y0, newLine);}
	public int render(String text, BufferedImage res, int width, Color background, int y0)
		{return render(text, res, width, background, y0, true);}
	public int render(String text, BufferedImage res, int width, Color background, int y0, boolean newLine)
	{
		text = text.replace("<html>", "").replace("</html>", "").replace("\n", "<br/>");
		pane.setText("<html>"+text+(newLine ? "<br/>&nbsp;" : "")+"</html>");
		pane.setSize(width, 1);
		Rectangle r = null;
		try {r = pane.modelToView(((HTMLDocument)pane.getDocument()).getEndPosition().getOffset()-1);}
		catch (Exception e) {throw new RuntimeException(e);}
		int h = r.y+(!newLine ? (int)(1.4*r.height) : 0);
		pane.setSize(width, h);
		
		Graphics2D g = res.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (background != null)
		{
			g.setBackground(background);
			g.clearRect(0, y0, Math.min(width, res.getWidth()), Math.min(h, res.getHeight()-y0));
		}
		if (y0 != 0)
			g.translate(0, y0);
		pane.paint(g);
		
		return h;
	}
	
	public static void main(String [] args) throws Exception
	{
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			if ("Nimbus".equals(info.getName()))
				{UIManager.setLookAndFeel(info.getClassName()); break;}
		
		SwingRenderer sr = new SwingRenderer();
		BufferedImage image = sr.getImage("<html><font style=\"font-size:32px\" color=\"#ffffff\" face=\"Times New Roman\"><i>One option would be to replace the KeySelectionManager interface with your own. You want to replace the JComboBox.KeySelectionManager as it is responsible for taking the inputted char and returns the row number (as an int) which should be selected.</i></font></html>", 
			600, new Color(0, 0, 0, 0));
		System.out.println(sr.pane.getDocument().getRootElements().length);
		System.out.println(((BlockView)sr.pane.getEditorKit().getViewFactory().create(sr.pane.getDocument().getRootElements()[0])).getHeight());
		
		JFrame frame = new JFrame();
		frame.add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	
}
