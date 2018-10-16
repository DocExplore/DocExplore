/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.ZipUtils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@SuppressWarnings("serial")
public class ErrorDialog extends JDialog
{
	JLabel message;
	JToggleButton detailsButton, sendButton;
	JTextPane detailsPane;
	JScrollPane scrollPane;
	JPanel sendPane;
	JTextField login, password;
	JCheckBox remember, screenshot;
	JTextArea comment;
	Throwable ex = null;
	String trace = "";
	File prefs;
	
	public ErrorDialog()
	{
		super(JOptionPane.getRootFrame(), Lang.s("errorGenericTitle"), true);
		
		this.prefs = new File(DocExploreTool.getHomeDir(), "edcache");
		
		getContentPane().setLayout(new BorderLayout(20, 20));
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		messagePanel.add(new JLabel(ImageUtils.getIcon("remove-24x24.png")));
		messagePanel.add(message = new JLabel(""));
		//messagePanel.getInsets().set(20, 20, 20, 20);
		getContentPane().add(messagePanel, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("dialogCloseLabel")) {public void actionPerformed(ActionEvent e)
		{
			ErrorDialog.this.setVisible(false);
		}}));
		buttonPanel.add(detailsButton = new JToggleButton(new AbstractAction(Lang.s("errorDetailsLabel")) {public void actionPerformed(ActionEvent e)
		{
			removePane(detailsButton);
			if (detailsButton.isSelected())
				ErrorDialog.this.getContentPane().add(scrollPane, BorderLayout.SOUTH);
			pack();
		}}));
//		buttonPanel.add(sendButton = new JToggleButton(new AbstractAction(Lang.s("errorSend")) {public void actionPerformed(ActionEvent e)
//		{
//			removePane(sendButton);
//			if (sendButton.isSelected())
//				ErrorDialog.this.getContentPane().add(sendPane, BorderLayout.SOUTH);
//			pack();
//		}}));
		buttonPanel.setPreferredSize(new Dimension(480, 40));
		getContentPane().add(buttonPanel, BorderLayout.CENTER);
		
		detailsPane = new JTextPane();
		scrollPane = new JScrollPane(detailsPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(480, 240));
		detailsPane.setBackground(getContentPane().getBackground());
		scrollPane.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(10, 10, 10, 10), 
			scrollPane.getBorder()));
		
		sendPane = new JPanel(new LooseGridLayout(0, 2, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		sendPane.add(new JLabel("Login"));
		sendPane.add(login = new JTextField(30));
		sendPane.add(new JLabel("Password"));
		sendPane.add(password = new JPasswordField(30));
		sendPane.add(new JLabel(""));
		sendPane.add(remember = new JCheckBox(Lang.s("errorRemember")));
		sendPane.add(new JLabel("Message"));
		sendPane.add(new JScrollPane(comment = new JTextArea(4, 40), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		sendPane.add(new JLabel(""));
		sendPane.add(screenshot = new JCheckBox(Lang.s("errorScreenshot")));
		sendPane.add(new JLabel(""));
		sendPane.add(new JButton(new AbstractAction(Lang.s("errorSend")) {public void actionPerformed(ActionEvent e)
		{
			((JButton)e.getSource()).setEnabled(false);
			new Thread() {public void run() 
			{
				try {send();}
				catch (Exception ex) {ex.printStackTrace();}
				((JButton)e.getSource()).setEnabled(true);
			}}.start();
		}}));
		comment.setLineWrap(true);
	}
	
	private void send() throws Exception
	{
		writePrefs();
		
		Path path = Files.createTempDirectory("errorReport");
		File dir = path.toFile();
		dir.deleteOnExit();
		
		if (screenshot.isSelected())
		{
			Window [] wins = Window.getOwnerlessWindows();
			for (int i=0;i<wins.length;i++)
				if (wins[i] != ErrorDialog.this && wins[i].isVisible())
					makeScreenshot(wins[i], new File(dir, "ss"+i+".jpg"));
		}
		Files.write(new File(dir, "stack.txt").toPath(), trace.getBytes());
		if (comment.getText().length() > 0)
			Files.write(new File(dir, "comment.txt").toPath(), comment.getText().getBytes());
		
		Path zip = Files.createTempFile("errorReport", ".zip");
		ZipUtils.zip(dir, zip.toFile());
		FileUtils.deleteDirectory(dir);
		
		Session session = new JSch().getSession(login.getText(), "pianosa.univ-rouen.fr", 22);
		session.setPassword(password.getText());
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect(15000);
		String serverDir = "/home/entitees/labo-litis/partages/docexplore-labo-litis/www/reports", target = "report"+randomName()+".zip";
		ChannelSftp sftp = (ChannelSftp)session.openChannel("sftp");
		sftp.connect(15000);
		try {sftp.mkdir(serverDir);} catch (Exception e) {}
		sftp.cd(serverDir);
		FileInputStream in = new FileInputStream(zip.toFile());
		sftp.put(in, serverDir+"/"+target);
		in.close();
		sftp.disconnect();
	}
	
	private static String randomName()
	{
		Random rand = new Random();
		String res = "";
		for (int i=0;i<11;i++)
			res += (char)('A'+rand.nextInt(26));
		return res;
	}
	
	private void readPrefs()
	{
		try
		{
			ObjectInputStream in = prefs.exists() ? new ObjectInputStream(new FileInputStream(prefs)) : null;
			login.setText(in == null ? "" : in.readUTF());
			remember.setSelected(in == null ? false : in.readBoolean());
			screenshot.setSelected(in == null ? false : in.readBoolean());
			if (remember.isSelected())
				password.setText(in == null ? "" : in.readUTF());
			if (in != null)
				in.close();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
	}
	private void writePrefs()
	{
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(prefs, false));
			out.writeUTF(login.getText());
			out.writeBoolean(remember.isSelected());
			out.writeBoolean(screenshot.isSelected());
			if (remember.isSelected())
				out.writeUTF(password.getText());
			out.close();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
	}
	
	private void removePane(Component origin)
	{
		if (origin != detailsButton) detailsButton.setSelected(false);
		if (origin != sendButton && sendButton != null) sendButton.setSelected(false);
		Component comp = ((BorderLayout)ErrorDialog.this.getContentPane().getLayout()).getLayoutComponent(BorderLayout.SOUTH);
		if (comp != null)
			getContentPane().remove(comp);
	}
	
	public void show(Throwable ex)
	{
		this.ex = ex;
		message.setText(Lang.s("errorGenericMessage"));
		StringWriter out = new StringWriter();
		ex.printStackTrace(new PrintWriter(out, true));
		trace = out.toString();
		detailsPane.setText(trace);
		comment.setText("");
		
		readPrefs();
		
		removePane(null);
		pack();
		GuiUtils.centerOnScreen(this);
		setVisible(true);
	}
	
	public static final void makeScreenshot(Container argFrame, File file)
	{
	    Rectangle rec = argFrame.getBounds();
	    if (rec.width <= 0 || rec.height <= 0)
	    	return;
	    BufferedImage bufferedImage = new BufferedImage(rec.width, rec.height, BufferedImage.TYPE_3BYTE_BGR);
	    argFrame.paint(bufferedImage.getGraphics());

	    try {ImageIO.write(bufferedImage, "JPG", file);} 
	    catch (IOException ioe) {ioe.printStackTrace();}
	}
	
	public static void main(String [] args)
	{
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e) {e.printStackTrace();}
		
		ErrorDialog dialog = new ErrorDialog();
		dialog.show(new Exception("Big problem!"));
		dialog.show(new Exception("Another big problem!"));
		System.exit(0);
	}
}
