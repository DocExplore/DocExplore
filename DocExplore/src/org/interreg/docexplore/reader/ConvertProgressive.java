package org.interreg.docexplore.reader;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

public class ConvertProgressive
{
	public static void main(String [] args) throws Exception
	{
		File inputFile = new File("serever-resources/book0/image9.png");
		BufferedImage image = ImageIO.read(inputFile);
		
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		ImageWriteParam params = writer.getDefaultWriteParam();
		params.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
		writer.setOutput(new FileImageOutputStream(
			new File("serever-resources/book0/"+inputFile.getName().substring(0, inputFile.getName().length()-4)+".jpg")));
		writer.write(null, new IIOImage(image, null, null), params);
	}
}
