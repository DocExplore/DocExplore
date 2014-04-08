/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.align;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;

public class GenerateLabels
{
	public static void main(String [] args) throws Exception
	{
		File root = new File("C:\\sci\\align\\export_hd");
		File [] rootContent = root.listFiles(new FileFilter()
		{
			public boolean accept(File pathname)
			{
				return pathname.getAbsolutePath().toLowerCase().endsWith(".png");
			}
		});
		
		File outputDir = new File(root, "extraction");
		outputDir.mkdir();
		
		File outputXml = new File(outputDir, "labels.xml");
		OutputStreamWriter xmlWriter = new OutputStreamWriter(new FileOutputStream(outputXml));
		xmlWriter.write("<Labels>\n");
		
		int outCnt = 0;
		for (File imageFile : rootContent)
		{
			System.out.println(imageFile.getName());
			BufferedImage image = ImageUtils.read(imageFile);
			
			File xmlFile = new File(root, imageFile.getName().substring(0, imageFile.getName().length()-3)+"xml");
			if (!xmlFile.exists())
				continue;
			String xml = null;
			try {xml = StringUtils.readFile(xmlFile);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e); continue;}
			
			Vector<ParameterizedLine> lines = new Vector<ParameterizedLine>();
			Vector<Integer> lineIndices = new Vector<Integer>();
			
			int curSep = xml.indexOf("<Separator");
			if (curSep < 0)
				continue;
			
			while (curSep >= 0)
			{
				int nextSep = xml.indexOf("<Separator", curSep+10);
				if (nextSep < 0)
					nextSep = xml.length();
				
				int sepatt = xml.indexOf("y=\"", curSep);
				double sepy = Double.parseDouble(xml.substring(sepatt+3, xml.indexOf("\"", sepatt+3)));
				
				ParameterizedLine line = new ParameterizedLine(sepy);
				
				int curKnob = xml.indexOf("<Knob", curSep);
				while (curKnob > 0 && curKnob < nextSep)
				{
					int knobatt = xml.indexOf("x=\"", curKnob);
					double knobx = Double.parseDouble(xml.substring(knobatt+3, xml.indexOf("\"", knobatt+3)));
					knobatt = xml.indexOf("dy=\"", curKnob);
					double knobdy = Double.parseDouble(xml.substring(knobatt+4, xml.indexOf("\"", knobatt+4)));
					knobatt = xml.indexOf("ray=\"", curKnob);
					double knobray = Double.parseDouble(xml.substring(knobatt+5, xml.indexOf("\"", knobatt+5)));
					line.knobs.add(new ParameterizedLine.Knob(knobx, knobdy, knobray));
					
					curKnob = xml.indexOf("<Knob", curKnob+5);
				}
				
				lines.add(line);
				lineIndices.add(curSep);
				
				curSep = xml.indexOf("<Separator", curSep+10);
			}
			
			for (int i=0;i<lines.size();i++)
			{
				curSep = lineIndices.get(i);
				
				int ext = xml.indexOf("<Extent", curSep);
				int extatt = xml.indexOf("from=\"", ext);
				double extfrom = Double.parseDouble(xml.substring(extatt+6, xml.indexOf("\"", extatt+6)));
				extatt = xml.indexOf("to=\"", ext);
				double extto = Double.parseDouble(xml.substring(extatt+4, xml.indexOf("\"", extatt+4)));
				
				int lineLeft = (int)(image.getWidth()*extfrom);
				int lineRight = (int)(image.getWidth()*extto);
				//System.out.println("!"+lineLeft+","+lineRight);
				int curToken = xml.indexOf("<Token>", curSep);
				double leftDelim = -1;
				while (curToken > 0 && curToken < lineIndices.get(i+1))
				{
					String token = xml.substring(curToken+7, xml.indexOf("</Token>", curToken+7));
					
					int curDelim = xml.indexOf("<Delimiter>", curToken);
					double rightDelim = curDelim < 0 || curDelim > lineIndices.get(i+1) ? -1 : 
						Double.parseDouble(xml.substring(curDelim+11, xml.indexOf("</Delimiter>", curDelim+11)));
					
					int left = leftDelim < 0 ? lineLeft : (int)(lineLeft+leftDelim*(lineRight-lineLeft));
					int right = rightDelim < 0 ? lineRight : (int)(lineLeft+rightDelim*(lineRight-lineLeft));
					
					//System.out.print(token+"("+left+","+right+") ");
					BufferedImage out = extract(image, lines.get(i), lines.get(i+1), left, right);
					String imageName = "out"+(outCnt++)+".png";
					ImageIO.write(out, "png", new File(outputDir, imageName));
					
					xmlWriter.write("\t<Label image=\""+imageName+"\">"+token+"</Label>\n");
					
					curToken = xml.indexOf("<Token>", curToken+7);
					leftDelim = rightDelim;
				}//System.out.println();
			}
		}
		
		xmlWriter.write("</Labels>\n");
		xmlWriter.close();
	}
	
	static BufferedImage extract(BufferedImage image, ParameterizedLine topLine, ParameterizedLine bottomLine, 
		int left, int right)
	{
		BufferedImage res = null;
		if (left < 0) left = 0;
		if (right > image.getWidth()-1) right = image.getWidth()-1;
		
		double min = topLine.yAt(left*1./image.getWidth());
		for (int i=left+1;i<=right;i++)
		{
			double val = topLine.yAt(i*1./image.getWidth());
			if (val < min)
				min = val;
		}
		double max = bottomLine.yAt(left*1./image.getWidth());
		for (int i=left+1;i<=right;i++)
		{
			double val = bottomLine.yAt(i*1./image.getWidth());
			if (val > max)
				max = val;
		}
		int top = (int)(min*image.getHeight());
		int bottom = (int)(max*image.getHeight());
		
		res = new BufferedImage(right-left+1, bottom-top+1, BufferedImage.TYPE_INT_RGB);
		
		for (int x=0;x<right-left+1;x++)
		{
			int y0 = (int)(image.getHeight()*topLine.yAt((x+left)*1./image.getWidth())-top);
			int y1 = (int)(image.getHeight()*bottomLine.yAt((x+left)*1./image.getWidth())-top);
			
			for (int y=y0;y<y1+1;y++)
			{
				int tx = left+x, ty = top+y;
				tx = tx < 0 ? 0 : tx > image.getWidth()-1 ? tx = image.getWidth()-1 : tx;
				ty = ty < 0 ? 0 : ty > image.getHeight()-1 ? ty = image.getHeight()-1 : ty;
				res.setRGB(x, y, image.getRGB(tx, ty));
			}
		}
		
		return res;
	}
}
