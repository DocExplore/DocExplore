package org.interreg.docexplore.reader.gfx;

import java.io.File;
import java.io.FileInputStream;

import org.interreg.docexplore.util.ImageUtils;

public class PngBench
{
	public static void main(String [] args) throws Exception
	{
		File [] inputs = new File []
		{
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image0.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image1.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image2.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image3.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image4.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image5.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image6.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image7.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image8.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image9.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image10.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image11.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image12.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image13.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image14.PNG"),
			new File("C:\\Users\\Alex\\Documents\\specs docexplore\\Leber5770-envoi-Litis-96-1\\image15.PNG"),
		};
		
		FileInputStream [] streams = new FileInputStream [inputs.length];
		for (int i=0;i<inputs.length;i++)
			streams[i] = new FileInputStream(inputs[i]);
		
		long start = System.currentTimeMillis();
		final int [] cnt = {streams.length};
		
//		for (final FileInputStream stream : streams)
//			new Thread() {public void run()
//			{
//				try {ImageUtils.readPNG(stream);}
//				catch (Exception e) {e.printStackTrace();}
//				cnt[0]--;
//			}}.start();
//		while (cnt[0] > 0)
//			Thread.yield();
		
//		for (final FileInputStream stream : streams)
//			new Thread() {public void run()
//			{
//				try {ImageIO.read(stream);}
//				catch (Exception e) {e.printStackTrace();}
//				cnt[0]--;
//			}}.start();
//		while (cnt[0] > 0)
//			Thread.yield();
		
		for (final FileInputStream stream : streams)
			new Thread() {public void run()
			{
				try {ImageUtils.read(stream);}
				catch (Exception e) {e.printStackTrace();}
				cnt[0]--;
			}}.start();
		while (cnt[0] > 0)
			Thread.yield();
		
		System.out.println("completed in "+(System.currentTimeMillis()-start));
	}
}
