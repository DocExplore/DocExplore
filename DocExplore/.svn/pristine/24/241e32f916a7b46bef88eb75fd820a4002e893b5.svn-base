package org.interreg.docexplore.stitch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.interreg.docexplore.gui.LooseGridLayout;


@SuppressWarnings("serial")
public class StitchSequencer extends JPanel
{
	static class ImageFile implements Serializable 
	{
		private static final long serialVersionUID = 405290219670927969L;
		
		File file;
		int w, h;
		
		public ImageFile(File file) {this.file = file; this.w = 0; this.h = 0;}
		public String toString() {return file.getName();}
	}
	
	KeyPointPanel pointPanel;
	Vector<ImageFile> files;
	JList fileList;
	Vector<Set<KeyPoint> []> keyPoints;
	Vector<double []> spine;
	int currentStitch = 0;
	
	double backupX0 = 0, backupY0 = 0, backupZoom = 1;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public StitchSequencer()
	{
		super(new LooseGridLayout(1, 3, 5, 5, true, true, SwingConstants.LEFT, SwingConstants.TOP, true, true));
		
		this.pointPanel = new KeyPointPanel();
		
		ObjectInputStream in = null;
		try
		{
			in = new ObjectInputStream(new FileInputStream(new File("stitch-data")));
			files = (Vector)in.readObject();
			keyPoints = (Vector)in.readObject();
			spine = (Vector)in.readObject();
//			spine = new Vector<double []>();
//			for (ImageFile file : files)
//				spine.add(null);
			currentStitch = 0;
			
			for (int i=0;i<files.size();i++)
				if (!files.get(i).file.exists())
				{
					files.remove(i);
					i--;
				}
			setFiles();
		}
		catch (Exception e)
		{
			this.files = new Vector<ImageFile>();
			this.keyPoints = new Vector<Set<KeyPoint> []>();
		}
		try {in.close();} catch (Exception e) {}
		
		this.fileList = new JList(files);
		fileList.setCellRenderer(new ListCellRenderer()
		{
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				boolean cur = index == currentStitch || index == currentStitch+1;
				JLabel label = new JLabel("<html>"+(cur ? "<b>" : "")+((ImageFile)value).toString()+(cur ? "</b>" : "")+"</html>");
				if (cellHasFocus)
				{
					label.setOpaque(true);
					label.setBackground(Color.blue);
					label.setForeground(Color.white);
				}
				return label;
			}
		});
		fileList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() < 2)
					return;
				currentStitch = fileList.getSelectedIndex();
				write();
				setFiles();
				refreshFiles();
			}
		});
		
		JPanel buttonPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, false, false));
		buttonPanel.add(new JButton(new AbstractAction("Open") {public void actionPerformed(ActionEvent arg0) {open();}}));
		buttonPanel.add(new JButton(new AbstractAction("Remove") {public void actionPerformed(ActionEvent arg0)
		{
			int index = fileList.getSelectedIndex();
			if (index < 0)
				return;
//			files.remove(files.size()-1);
//			keyPoints.remove(keyPoints.size()-1);
			files.remove(index);
			keyPoints.remove(index);
			spine.remove(index);
			refreshFiles();
		}}));
		buttonPanel.add(new JButton(new AbstractAction("Next") {public void actionPerformed(ActionEvent arg0)
		{
			keyPoints.get(currentStitch)[0] = pointPanel.leftPoints;
			keyPoints.get(currentStitch)[1] = pointPanel.rightPoints;
			spine.setElementAt(pointPanel.leftSpinePoint, currentStitch);
			spine.setElementAt(pointPanel.rightSpinePoint, currentStitch+1);
			
			currentStitch++;
			while (keyPoints.size() <= currentStitch)
				keyPoints.add(new Set [] {new HashSet(), new HashSet()});
			
			write();
			setFiles();
			fileList.repaint();
		}}));
		buttonPanel.add(new JButton(new AbstractAction("Stitch") {public void actionPerformed(ActionEvent arg0)
		{
			final Stitcher stitcher = new Stitcher(StitchSequencer.this, files, keyPoints);
			stitcher.x0 = backupX0;
			stitcher.y0 = backupY0;
			stitcher.zoom = backupZoom;
			JDialog dialog = new JDialog((Frame)StitchSequencer.this.getTopLevelAncestor(), "Preview", false);
			dialog.setLayout(new BorderLayout());
			dialog.add(stitcher, BorderLayout.CENTER);
			JPanel goPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			goPanel.add(new JButton(new AbstractAction("Stitch!") {public void actionPerformed(final ActionEvent ae)
			{
				((JButton)ae.getSource()).setEnabled(false);
				final boolean [] done = {false};
				new Thread() {public void run()
				{
					while (!done[0])
					{
						try {Thread.sleep(200);} catch (Exception e) {}
						stitcher.repaint();
					}
				}}.start();
				
				new Thread() {public void run()
				{
					try {stitcher.merge(true);}
					catch (Exception e) {e.printStackTrace();}
					done[0] = true;
					((JButton)ae.getSource()).setEnabled(true);
				}}.start();
			}}));
			goPanel.add(new JButton(new AbstractAction("Fit") {public void actionPerformed(final ActionEvent ae) {stitcher.fitView();}}));
			dialog.add(goPanel, BorderLayout.SOUTH);
			dialog.pack();
			dialog.setVisible(true);
		}}));
		buttonPanel.add(new JButton(new AbstractAction("Preview") {public void actionPerformed(ActionEvent arg0)
		{
			Vector<ImageFile> files = new Vector<ImageFile>();
			files.add(StitchSequencer.this.files.get(currentStitch));
			files.add(StitchSequencer.this.files.get(currentStitch+1));
			Vector<Set<KeyPoint> []> keyPoints = new Vector<Set<KeyPoint> []>();
			keyPoints.add(StitchSequencer.this.keyPoints.get(currentStitch));
			keyPoints.add(StitchSequencer.this.keyPoints.get(currentStitch+1));
			final Stitcher stitcher = new Stitcher(StitchSequencer.this, files, keyPoints);
			JDialog dialog = new JDialog((Frame)StitchSequencer.this.getTopLevelAncestor(), "Preview", false);
			dialog.setLayout(new BorderLayout());
			dialog.add(stitcher, BorderLayout.CENTER);
			JPanel goPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			goPanel.add(new JButton(new AbstractAction("Stitch!") {public void actionPerformed(final ActionEvent ae)
			{
				((JButton)ae.getSource()).setEnabled(false);
				final boolean [] done = {false};
				new Thread() {public void run()
				{
					while (!done[0])
					{
						try {Thread.sleep(200);} catch (Exception e) {}
						stitcher.repaint();
					}
				}}.start();
				
				new Thread() {public void run()
				{
					try {stitcher.merge(false);}
					catch (Exception e) {e.printStackTrace();}
					done[0] = true;
					((JButton)ae.getSource()).setEnabled(true);
				}}.start();
			}}));
			goPanel.add(new JButton(new AbstractAction("Fit") {public void actionPerformed(final ActionEvent ae) {stitcher.fitView();}}));
			dialog.add(goPanel, BorderLayout.SOUTH);
			dialog.pack();
			dialog.setVisible(true);
		}}));
		
		add(new JScrollPane(fileList));
		add(buttonPanel);
		add(pointPanel);
	}
	
	void setFiles()
	{
		try
		{
//			if (stitches.size() > 1)
//				for (int i=0;i<stitches.size()+1;i++)
//					if (i < files.size() && files.get(i).w == 0 && files.get(i).h == 0) try
//					{
//						BufferedImage image = ImageIO.read(files.get(i).file);
//						files.get(i).w = image.getWidth();
//						files.get(i).h = image.getHeight();
//					}
//					catch (Exception e) {e.printStackTrace();}
			
			if (currentStitch+1 < files.size())
			{
				pointPanel.setFiles(files.get(currentStitch).file, files.get(currentStitch+1).file, keyPoints.get(currentStitch)[0], keyPoints.get(currentStitch)[1], 
					spine.get(currentStitch), spine.get(currentStitch+1));
				files.get(currentStitch).w = pointPanel.left.image.getWidth();
				files.get(currentStitch).h = pointPanel.left.image.getHeight();
				files.get(currentStitch+1).w = pointPanel.right.image.getWidth();
				files.get(currentStitch+1).h = pointPanel.right.image.getHeight();
			}
			else pointPanel.setFiles(null, null, null, null, null, null);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	void write()
	{
		ObjectOutputStream out = null;
		try
		{
			out = new ObjectOutputStream(new FileOutputStream(new File("stitch-data")));
			out.writeObject(files);
			out.writeObject(keyPoints);
			out.writeObject(spine);
		}
		catch (Exception e) {}
		try {out.close();} catch (Exception e) {}
	}
	
	JFileChooser fileChooser = new JFileChooser();
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void open()
	{
		fileChooser.setFileFilter(new FileFilter()
		{
			public String getDescription() {return "Images";}
			public boolean accept(File f) {return true;}
		});
		fileChooser.setMultiSelectionEnabled(true);
		if (fileChooser.showOpenDialog(StitchSequencer.this) != JFileChooser.APPROVE_OPTION)
			return;
		Set<File> sorted = new TreeSet<File>(new Comparator<File>() {public int compare(File o1, File o2) {return o1.getName().compareTo(o2.getName());}});
		for (File file : fileChooser.getSelectedFiles())
			sorted.add(file);
		for (File file : sorted)
		{
			if (!files.isEmpty())
				keyPoints.add(new Set [] {new HashSet(), new HashSet()});
			files.add(new ImageFile(file));
		}
		setFiles();
		write();
		refreshFiles();
	}
	
	public void refreshFiles()
	{
		DefaultListModel model = new DefaultListModel();
		for (ImageFile file : files)
			model.addElement(file);
		fileList.setModel(model);
		fileList.repaint();
	}
	
	public static void main(String [] args) throws Exception
	{
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e) {e.printStackTrace();}
		
		JFrame win = new JFrame("Stitch");
		StitchSequencer ss = new StitchSequencer();
		win.add(ss);
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.pack();
		win.setVisible(true);
	}
}
