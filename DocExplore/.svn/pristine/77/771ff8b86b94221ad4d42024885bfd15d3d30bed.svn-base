package org.interreg.docexplore.reader.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;

public class TransparencyConverter
{
	public static void enableTransparency(File ... pages) throws Exception
	{
		BufferedImage current = ImageUtils.read(pages[0]);
		int w = current.getWidth(), h = current.getHeight();
		current = null;
		
		int lastRecto = pages.length%2 == 0 ? pages.length-2 : pages.length-1;
		for (int i=lastRecto;i>=0;i-=2)
			current = addTransparentPage(pages[i], w, h, current);
		
		current = null;
		for (int i=1;i<pages.length;i+=2)
			current = addTransparentPage(pages[i], w, h, current);
	}
	
	static BufferedImage addTransparentPage(File ori, int w, int h, BufferedImage current) throws Exception
	{
		System.out.println("reading "+ori.getName());
		BufferedImage image = ImageUtils.read(ori);
		if (image.getWidth() != w || image.getHeight() != h)
			image = ImageUtils.resize(image, w, h);
		
		String name = ori.getName().substring(0, ori.getName().lastIndexOf('.'));
		File tres = new File(ori.getParentFile(), "gent_"+name+".png");
		ImageUtils.write(image, "png", tres);
		
		if (current == null)
		{
			current = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = current.createGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, w, h);
		}
		Graphics2D g = current.createGraphics();
		g.drawImage(image, 0, 0, null);
		
		File res = new File(ori.getParentFile(), "gen_"+name+".png");
		ImageUtils.write(current, "png", res);
		
		return current;
	}
	
	static List<File> extractFiles(File xml) throws Exception
	{
		BufferedReader reader = new BufferedReader(new StringReader(StringUtils.readFile(xml)));
		FileWriter writer = new FileWriter(xml);
		List<File> res = new LinkedList<File>();
		String line;
		File imageDir = null;
		while ((line = reader.readLine()) != null)
		{
			if (line.trim().startsWith("<Page") && imageDir != null)
			{
				int from = line.indexOf("src=\"")+5;
				int to = line.indexOf("\"", from);
				String name = line.substring(from, to);
				String shortName = name.substring(0, name.lastIndexOf('.'));
				res.add(new File(imageDir, name));
				
				writer.write(line.substring(0, from));
				writer.write("gen_"+shortName+".png\" tsrc=\"gent_"+shortName+".png");
				writer.write(line.substring(to));
				writer.write("\n");
			}
			else
			{
				if (line.trim().startsWith("<Book"))
				{
					int from = line.indexOf("path=\"")+6;
					int to = line.indexOf("\"", from);
					imageDir = new File(xml.getParentFile(), line.substring(from, to));
				}
				writer.write(line);
				writer.write("\n");
			}
		}
		writer.close();
		reader.close();
		return res;
	}
	
	public static void main(String [] args) throws Exception
	{
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e) {e.printStackTrace();}
		
		JFileChooser chooser = new JFileChooser(".");
		chooser.setFileFilter(new javax.swing.filechooser.FileFilter()
		{
			public String getDescription() {return "Book specification";}
			public boolean accept(File f) {return f.isDirectory() || f.getName().endsWith(".xml");}
		});
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
			System.exit(0);
		File inputXml = chooser.getSelectedFile();
		List<File> images = extractFiles(inputXml);
		
		File [] pages = images.toArray(new File [0]);
		enableTransparency(pages);
	}
}
