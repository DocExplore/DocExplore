package org.interreg.docexplore.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;

public class GuiUtils
{
	public static void centerOnScreen(Component dialog)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	 
		GraphicsConfiguration gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gconf);
		dialog.setLocation((screenSize.width-(screenInsets.left+screenInsets.right)-dialog.getWidth())/2, 
			(screenSize.height-(screenInsets.bottom+screenInsets.top)-dialog.getHeight())/2);
	}
	
	public static void centerOnComponent(JDialog dialog, Component comp)
	{
		if (comp == null || !comp.isShowing())
			{centerOnScreen(dialog); return;}
		Rectangle b = comp.getBounds();
		Point p = comp.getLocationOnScreen();
		dialog.setLocation(p.x+(b.width-dialog.getWidth())/2, p.y+(b.height-dialog.getHeight())/2);
	}
	
	public static interface ProgressRunnable extends Runnable
	{
		public float getProgress();
	}
	
	public static JDialog busyNoProgressDialog = null, busyProgressDialog = null;
	static int [] cnt = {0};
	static int n = 10;
	static Color busyCol = Color.gray;
	static Color progressCol = Color.lightGray;
	
	public static void blockUntilComplete(final Runnable runnable, Component component)
		{blockUntilComplete(runnable, component, XMLResourceBundle.getString("gui-lrb", "dialogWaitLabel"));}
	public static void blockUntilComplete(final Runnable runnable, Component component, String message)
	{
		//Window win = component != null ? (Window)component.getTopLevelAncestor() : null;
		
		if (runnable instanceof ProgressRunnable)
		{
			busyProgressDialog = new JDialog(JOptionPane.getFrameForComponent(component), true);
			final JPanel back = buildProgressPanel((ProgressRunnable)runnable, message);
			busyProgressDialog.add(back);
			busyProgressDialog.pack();
		}
		else
		{
			busyNoProgressDialog = new JDialog(JOptionPane.getFrameForComponent(component), true);
			final JPanel back = buildUnkownProgressPanel(message);
			busyNoProgressDialog.add(back);
			busyNoProgressDialog.pack();
		}
		
		final JDialog busyDialog = runnable instanceof ProgressRunnable ? busyProgressDialog : busyNoProgressDialog;
		if (busyDialog.isVisible())
		{
			try {runnable.run();}
			catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
			return;
		}
		GuiUtils.centerOnComponent(busyDialog, component);
		
		final boolean [] running = {true};
		cnt[0] = 0;
		new Thread() {public void run()
		{
			try {runnable.run();}
			catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
			running[0] = false;
			while (!busyDialog.isVisible())
				try {Thread.sleep(100);} catch (Exception e) {}
			busyDialog.setVisible(false);
			
		}}.start();
		new Thread() {public void run()
		{
			while (running[0])
			{
				try {Thread.sleep(100);} catch (Exception e) {}
				cnt[0] = (cnt[0]+1)%n;
				busyDialog.repaint();
			}
		}}.start();
		busyDialog.setVisible(true);
	}
	
	@SuppressWarnings("serial")
	static JPanel buildUnkownProgressPanel(String message)
	{
		final JPanel back = new JPanel(new BorderLayout());
		back.setBackground(Color.white);
		back.setBorder(BorderFactory.createLineBorder(busyCol, 2));
		busyNoProgressDialog.setUndecorated(true);
		JLabel canvas = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				if (!busyNoProgressDialog.isVisible())
					return;
				g.setColor(back.getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(busyCol);
				int i = cnt[0]%n;
				g.fillRect((getWidth()*(2*i+1))/(2*n+1), (getHeight()-getWidth()/(2*n+1))/2, getWidth()/(2*n+1), getWidth()/(2*n+1));
			}
		};
		canvas.setPreferredSize(new Dimension(100, 50));
		back.add(canvas, BorderLayout.NORTH);
		JLabel waitLabel = new JLabel(message, SwingConstants.CENTER);
		waitLabel.setForeground(busyCol);
		back.add(waitLabel, BorderLayout.SOUTH);
		
		return back;
	}
	
	@SuppressWarnings("serial")
	static JPanel buildProgressPanel(final ProgressRunnable progress, String message)
	{
		final JPanel back = new JPanel(new BorderLayout());
		back.setBackground(Color.white);
		back.setBorder(BorderFactory.createLineBorder(busyCol, 2));
		busyProgressDialog.setUndecorated(true);
		JLabel canvas = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				if (!busyProgressDialog.isVisible())
					return;
				g.setColor(back.getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(progressCol);
				g.fillRect(getWidth()/(2*n+1), (getHeight()-getWidth()/(2*n+1))/2, getWidth()-2*getWidth()/(2*n+1), getWidth()/(2*n+1));
				g.setColor(busyCol);
				float val = progress.getProgress();
				g.fillRect(getWidth()/(2*n+1), (getHeight()-getWidth()/(2*n+1))/2, (int)(val*getWidth()-2*getWidth()/(2*n+1)), getWidth()/(2*n+1));
			}
		};
		canvas.setPreferredSize(new Dimension(100, 50));
		back.add(canvas, BorderLayout.NORTH);
		JLabel waitLabel = new JLabel(message, SwingConstants.CENTER);
		waitLabel.setForeground(busyCol);
		back.add(waitLabel, BorderLayout.SOUTH);
		
		return back;
	}
}
