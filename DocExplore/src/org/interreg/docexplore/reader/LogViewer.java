package org.interreg.docexplore.reader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.nfd.JNativeFileDialog;

@SuppressWarnings("serial")
public class LogViewer extends JPanel
{
	JNativeFileDialog fd = new JNativeFileDialog();
	
	Calendar start;
	long startInMillis;
	int [] interactionsPerPeriod;
	static long period = 12*60*60*1000;
	Map<String, Integer> interactionsPerBook;
	Map<String, Integer> map;
	
	static Color [] colors = 
	{
		new Color(255, 0, 0),
		new Color(255, 106, 0),
		new Color(255, 216, 0),
		new Color(182, 255, 0),
		new Color(0, 255, 33),
		new Color(0, 255, 255),
		new Color(127, 201, 255),
		new Color(127, 146, 255),
		new Color(161, 127, 255),
		new Color(214, 127, 255),
		new Color(255, 127, 182),
		new Color(255, 127, 127),
		new Color(255, 178, 127),
		new Color(255, 233, 127),
		new Color(165, 255, 127),
		new Color(127, 255, 197),
		new Color(0, 148, 255),
		new Color(0, 38, 255),
		new Color(178, 0, 255),
		new Color(255, 0, 220),
	};
	
	public LogViewer()
	{
		super(new BorderLayout());
		
		fd.acceptFiles = true;
		fd.acceptFolders = false;
		fd.multipleSelection = false;
		
		JPanel charts = new JPanel(new LooseGridLayout(0, 2, 5, 5, true, true, SwingConstants.CENTER, SwingConstants.CENTER, true, true));
		
		JPanel interactionCanvas = new JPanel() {public void paintComponent(Graphics g)
		{
			g.clearRect(0, 0, getWidth(), getHeight());
			if (interactionsPerPeriod == null)
				return;
			
			int margin = 40;
			int y0 = getHeight()-margin;
			g.setColor(Color.black);
			g.drawLine(0, y0, getWidth()-1, y0);
			
			Calendar cal = (Calendar)start.clone();
			
			int max = 1;
			for (int i=0;i<interactionsPerPeriod.length;i++)
				max = Math.max(max, interactionsPerPeriod[i]);
			max = 100*(max/100+1);
			
			Font defaultFont = g.getFont();
			g.setFont(Font.decode("Arial-10"));
			
			int lastMonthX0 = 0;
			int curMonth = -1;
			String curMonthName = null;
			
			for (int i=0;i<interactionsPerPeriod.length;i++)
			{
				int x0 = i*getWidth()/interactionsPerPeriod.length;
				int x1 = (i+1)*getWidth()/interactionsPerPeriod.length;
				int h = interactionsPerPeriod[i]*(getHeight()-margin)/max;
				
				g.setColor(colors[6]);
				g.fillRect(x0, y0-h, x1-x0, h);
				g.setColor(Color.black);
				g.drawRect(x0, y0-h, x1-x0, h);
				
				if (i%2 == 0)
				{
					int month = cal.get(Calendar.MONTH);
					if (curMonth < 0)
					{
						curMonth = month;
						curMonthName = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
					}
					else if (curMonth != month)
					{
						g.setColor(Color.black);
						g.drawLine(x0, getHeight()-1-margin/2, x0, getHeight()-1);
						int namew = g.getFontMetrics().stringWidth(curMonthName);
						g.drawString(curMonthName, lastMonthX0+(x0-lastMonthX0-namew)/2, getHeight()-1-margin/2+16);
						curMonth = month;
						lastMonthX0 = x0;
						curMonthName = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
					}
					//g.drawString((month+1)+"/"+(cal.get(Calendar.DAY_OF_MONTH)), x0+2, y0+16);
					int dayw = g.getFontMetrics().stringWidth(""+cal.get(Calendar.DAY_OF_MONTH));
					g.drawString(""+cal.get(Calendar.DAY_OF_MONTH), x0+(2*(x1-x0)-dayw)/2, y0+16);
					cal.add(Calendar.DAY_OF_YEAR, 1);
					g.setColor(Color.black);
					g.drawLine(x0, y0, x0, getHeight()-1-margin/2);
				}
			}
			if (getWidth()-lastMonthX0 > 0)
			{
				int namew = g.getFontMetrics().stringWidth(curMonthName);
				g.drawString(curMonthName, lastMonthX0+(getWidth()-lastMonthX0-namew)/2, getHeight()-1-margin/2+16);
			}
			g.drawLine(0, getHeight()-1-margin/2, getWidth()-1, getHeight()-1-margin/2);
			
			g.setFont(defaultFont);
			g.setColor(Color.gray);
			for (int y=100;y<max;y+=100)
			{
				int h = y*(getHeight()-margin)/max;
				g.drawLine(0, y0-h, getWidth()-1, y0-h);
				g.drawString(""+(y), 2, y0-h-2);
			}
		}};
		interactionCanvas.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		interactionCanvas.setPreferredSize(new Dimension(500, 500));
		charts.add(interactionCanvas);
		
		JPanel bookCanvas = new JPanel() {public void paintComponent(Graphics _g)
		{
			Graphics2D g = (Graphics2D)_g;
			g.clearRect(0, 0, getWidth(), getHeight());
			if (interactionsPerPeriod == null)
				return;
			
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.black);
			int xc = getWidth()/2;
			int yc = xc;
			int ray = (int)(.95*xc);
			//g.drawOval(xc-ray, yc-ray, 2*ray, 2*ray);
			
			int total = 0;
			for (int count : interactionsPerBook.values())
				total += count;
			double curang = 0;
			int cnt = 0;
			for (Map.Entry<String, Integer> entry : interactionsPerBook.entrySet())
			{
				double ang = curang+2*Math.PI*entry.getValue()/total;
				Path2D.Double path = new Path2D.Double();
				path.moveTo(xc, yc);
				for (double a=curang;a<ang;a+=.02*Math.PI)
					path.lineTo(xc+ray*Math.cos(a), yc+ray*Math.sin(a));
				path.lineTo(xc+ray*Math.cos(ang), yc+ray*Math.sin(ang));
				path.lineTo(xc, yc);
				Area area = new Area(path);
				
				g.setColor(colors[cnt%colors.length]);
				g.fill(area);
				g.setColor(Color.black);
				g.draw(area);
				
				int y0 = (int)(yc+1.15*ray+20*cnt);
				g.setColor(colors[cnt%colors.length]);
				g.fillRect(5, y0-15, 15, 15);
				g.setColor(Color.black);
				g.drawRect(5, y0-15, 15, 15);
				g.drawString(String.format("%.1f%% %s", entry.getValue()*100f/total, entry.getKey()), 25, y0-3);
				
				curang = ang;
				cnt++;
			}
		}};
		bookCanvas.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		bookCanvas.setPreferredSize(new Dimension(200, 500));
		charts.add(bookCanvas);
		
		add(charts, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction("Open") {public void actionPerformed(ActionEvent arg0)
		{
			if (fd.showOpenDialog())
				try {parse(fd.getSelectedFile(), true);}
				catch (Exception e) {e.printStackTrace();}
		}}));
		buttonPanel.add(new JButton(new AbstractAction("Merge") {public void actionPerformed(ActionEvent arg0)
		{
			if (fd.showOpenDialog())
				try {parse(fd.getSelectedFile(), false);}
				catch (Exception e) {e.printStackTrace();}
		}}));
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private void addInteraction(long when)
	{
		int index = (int)((when-startInMillis)/period);
		if (index < 0)
			return;
		if (index >= interactionsPerPeriod.length)
			interactionsPerPeriod = Arrays.copyOf(interactionsPerPeriod, index+1);
		interactionsPerPeriod[index]++;
	}
	private void parse(File file, boolean reset) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		if (reset)
		{
			start = null;
			interactionsPerPeriod = new int [0];
			map = new TreeMap<String, Integer>();
		}
		
		String line = null, curBook = null;
		while ((line = reader.readLine()) != null)
		{
			int split = line.indexOf(" : ");
			if (split < 0)
				break;
			long when = Long.parseLong(line.substring(0, split));
			if (when < 1378159200000L || when > 1384642800000L)
			continue;
//			if (when < 1378764000000L || when > 1381010400000L)
//				continue;
//			if (when < 1381356000000L)
//				continue;
			String what = line.substring(split+3);
			
			if (start == null)
			{
				start = Calendar.getInstance();
				start.setTimeInMillis(when);
				start.set(Calendar.HOUR_OF_DAY, 0);
				start.set(Calendar.MINUTE, 0);
				start.set(Calendar.SECOND, 0);
				start.set(Calendar.MILLISECOND, 0);
				startInMillis = start.getTimeInMillis();
			}
			
			if (what.startsWith("Shelf entry selected: "))
				curBook = what.substring("Shelf entry selected: ".length(), what.lastIndexOf("("));
			if (what.startsWith("Displaying page") || what.startsWith("Displaying roi"))
			{
				addInteraction(when);
				Integer count = map.get(curBook);
				if (count == null)
					count = 0;
				map.put(curBook, count+1);
			}
		}
		
		if (reset)
			((JFrame)getTopLevelAncestor()).setTitle("Log Viewer - "+file.getName());
		else ((JFrame)getTopLevelAncestor()).setTitle(((JFrame)getTopLevelAncestor()).getTitle()+" - "+file.getName());
		
		interactionsPerBook = new TreeMap<String, Integer>(new Comparator<String>() {public int compare(String o1, String o2)
		{
			return map.get(o1)-map.get(o2);
		}});
		interactionsPerBook.putAll(map);
		
		repaint();
		
		reader.close();
	}
	
	public static void main(String [] args) throws Exception
	{
//		System.out.println(new Date(1378110036691L));
//		System.out.println(new Date("09/03/2013").getTime());
//		System.out.println(new Date("11/17/2013").getTime());
//		BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\Users\\Alex\\Documents\\activity expo 2013\\activityTableBasse.log")));
//		StringBuffer res = new StringBuffer();
//		String line = null;
//		long lim = new Date("10/06/2013").getTime();
//		while ((line = reader.readLine()) != null)
//		{
//			int split = line.indexOf(" : ");
//			if (split < 0)
//				break;
//			long when = Long.parseLong(line.substring(0, split));
//			if (when > lim)
//				when += 8*period;
//			String what = line.substring(split+3);
//			res.append(when).append(" : ").append(what).append("\n");
//		}
//		reader.close();
//		FileWriter out = new FileWriter(new File("C:\\Users\\Alex\\Documents\\activity expo 2013\\activityTableBasse2.log"));
//		out.write(res.toString());
//		out.close();
		
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e) {e.printStackTrace();}
		
		JFrame win = new JFrame("Log Viewer");
		win.add(new LogViewer());
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.pack();
		win.setVisible(true);
	}
}
