/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.gui.text.TextToolbar;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.StringUtils;
import org.interreg.docexplore.util.ZipUtils;

@SuppressWarnings("serial")
public class ServerConfigPanel extends JPanel
{
	public static class Book
	{
		String name, desc;
		File bookFile;
		File bookDir;
		int nPages = 0;
		boolean used = false;
		boolean deleted = false;
		
		public Book(File bookFile, String name, String desc, boolean used) throws Exception
		{
			this.bookFile = bookFile;
			this.name = name;
			this.desc = desc;
			this.used = used;
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(bookFile), Charset.forName("UTF-8")));
			String line = null;
			while ((line = reader.readLine()) != null)
				if (line.startsWith("<Book"))
				{
					int i = line.indexOf("path=\"");
					String path = line.substring(i+6, line.indexOf('"', i+7));
					this.bookDir = new File(bookFile.getParentFile(), path);
				}
				else if (line.trim().startsWith("<Page"))
					nPages++;
					
			reader.close();
		}
	}
	
	File serverDir;
	List<Book> books;
	JList bookList;
	
	JCheckBox usedBox;
	JTextField nameField;
	JTextPane descField;
	JButton deleteButton, exportButton;
	JTextField timeoutField;
	
	public ServerConfigPanel(final File config, final File serverDir) throws Exception
	{
		super(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		
		this.serverDir = serverDir;
		this.books = new Vector<Book>();
		this.bookList = new JList(new DefaultListModel());
		
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.setBorder(BorderFactory.createTitledBorder(Lang.s("cfgBooksLabel")));
		bookList.setOpaque(false);
		bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bookList.setCellRenderer(new ListCellRenderer()
		{
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				Book book = (Book)value;
				JLabel label = new JLabel("<html><b>"+book.name+"</b> - "+book.nPages+" pages</html>");
				label.setOpaque(true);
				if (isSelected)
				{
					label.setBackground(TextToolbar.styleHighLightedBackground);
					label.setForeground(Color.white);
				}
				if (book.deleted)
					label.setForeground(Color.red);
				else if (!book.used)
					label.setForeground(Color.gray);
				return label;
			}
		});
		bookList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;
				setFields((Book)bookList.getSelectedValue());
			}
		});
		JScrollPane scrollPane = new JScrollPane(bookList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(500, 300));
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		listPanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel importPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		importPanel.add(new JButton(new AbstractAction(Lang.s("cfgImportLabel"))
		{
			public void actionPerformed(ActionEvent e)
			{
				final File inFile = DocExploreTool.getFileDialogs().openFile(DocExploreTool.getIBookCategory());
				if (inFile == null)
					return;
				
				try
				{
					final File tmpDir = new File(serverDir, "tmp");
					tmpDir.mkdir();
					
					GuiUtils.blockUntilComplete(new ProgressRunnable()
					{
						float [] progress = {0};
						public void run()
						{
							try {ZipUtils.unzip(inFile, tmpDir, progress);}
							catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
						}
						public float getProgress() {return (float)progress[0];}
					}, ServerConfigPanel.this);
					
					File tmpFile = new File(tmpDir, "index.tmp");
					ObjectInputStream input = new ObjectInputStream(new FileInputStream(tmpFile));
					String bookFile = input.readUTF();
					String bookName = input.readUTF();
					String bookDesc = input.readUTF();
					input.close();
					
					new PresentationImporter().doImport(ServerConfigPanel.this, bookName, bookDesc, new File(tmpDir, bookFile));
					FileUtils.cleanDirectory(tmpDir);
					FileUtils.deleteDirectory(tmpDir);
					updateBooks();
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}));
		listPanel.add(importPanel, BorderLayout.SOUTH);
		add(listPanel);
		
		JPanel setupPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		setupPanel.setBorder(BorderFactory.createTitledBorder(Lang.s("cfgBookInfoLabel")));
		usedBox = new JCheckBox(Lang.s("cfgUseLabel"));
		usedBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Book book = (Book)bookList.getSelectedValue();
				if (book != null)
				{
					book.used = usedBox.isSelected();
					bookList.repaint();
				}
			}
		});
		setupPanel.add(usedBox);
		
		JPanel fieldPanel = new JPanel(new LooseGridLayout(0, 2, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		fieldPanel.add(new JLabel(Lang.s("cfgTitleLabel")));
		nameField = new JTextField(50);
		nameField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void removeUpdate(DocumentEvent e) {changedUpdate(e);}
			public void insertUpdate(DocumentEvent e) {changedUpdate(e);}
			public void changedUpdate(DocumentEvent e)
			{
				Book book = (Book)bookList.getSelectedValue();
				if (book == null)
					return;
				book.name = nameField.getText();
				bookList.repaint();
			}
		});
		fieldPanel.add(nameField);
		
		fieldPanel.add(new JLabel(Lang.s("cfgDescriptionLabel")));
		descField = new JTextPane();
		//descField.setWrapStyleWord(true);
		descField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void removeUpdate(DocumentEvent e) {changedUpdate(e);}
			public void insertUpdate(DocumentEvent e) {changedUpdate(e);}
			public void changedUpdate(DocumentEvent e)
			{
				Book book = (Book)bookList.getSelectedValue();
				if (book == null)
					return;
				book.desc = descField.getText();
			}
		});
		scrollPane = new JScrollPane(descField, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(420, 50));
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		fieldPanel.add(scrollPane);
		
		setupPanel.add(fieldPanel);
		
		exportButton = new JButton(new AbstractAction(Lang.s("cfgExportLabel"))
		{
			public void actionPerformed(ActionEvent e)
			{
				File file = DocExploreTool.getFileDialogs().saveFile(DocExploreTool.getIBookCategory());
				if (file == null)
					return;
				final Book book = (Book)bookList.getSelectedValue();
				final File indexFile = new File(serverDir, "index.tmp");
				try
				{
					final File outFile = file;
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(indexFile));
					out.writeUTF(book.bookFile.getName());
					out.writeUTF(book.name);
					out.writeUTF(book.desc);
					out.close();
					
					GuiUtils.blockUntilComplete(new ProgressRunnable()
					{
						float [] progress = {0};
						public void run()
						{
							try {ZipUtils.zip(serverDir, new File [] {indexFile, book.bookFile, book.bookDir}, outFile, progress, 0, 1, 9);}
							catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
						}
						public float getProgress() {return (float)progress[0];}
					}, ServerConfigPanel.this);
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				if (indexFile.exists())
					indexFile.delete();
			}
		});
		deleteButton = new JButton(new AbstractAction(Lang.s("cfgDeleteRestoreLabel"))
		{
			public void actionPerformed(ActionEvent e)
			{
				Book book = (Book)bookList.getSelectedValue();
				if (book == null)
					return;
				book.deleted = !book.deleted;
				bookList.repaint();
			}
		});
		
		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		actionsPanel.add(exportButton);
		actionsPanel.add(deleteButton);
		setupPanel.add(actionsPanel);
		
		add(setupPanel);
		
		JPanel optionsPanel = new JPanel(new LooseGridLayout(0, 2, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		optionsPanel.setBorder(BorderFactory.createTitledBorder(Lang.s("cfgOptionsLabel")));
		JPanel timeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		timeoutField = new JTextField(5);
		timeoutPanel.add(timeoutField);
		timeoutPanel.add(new JLabel(Lang.s("cfgTimeoutLabel")));
		optionsPanel.add(timeoutPanel);
		add(optionsPanel);
		
		updateBooks();
		setFields(null);
		
		final String xml = config.exists() ? StringUtils.readFile(config) : "<config></config>";
		String idle = StringUtils.getTagContent(xml, "idle");
		if (idle != null)
			try {timeoutField.setText(""+Integer.parseInt(idle));}
			catch (Throwable e) {}
	}
	
	void setFields(Book book)
	{
		if (book == null)
		{
			usedBox.setSelected(false);
			nameField.setText("");
			descField.setText("");
			usedBox.setEnabled(false);
			nameField.setEnabled(false);
			descField.setEnabled(false);
			descField.setOpaque(false);
			deleteButton.setEnabled(false);
			exportButton.setEnabled(false);
		}
		else
		{
			usedBox.setSelected(book.used);
			nameField.setText(book.name);
			descField.setText(book.desc);
			usedBox.setEnabled(true);
			nameField.setEnabled(true);
			descField.setEnabled(true);
			descField.setOpaque(true);
			deleteButton.setEnabled(true);
			exportButton.setEnabled(true);
		}
	}
	
	void updateBooks() throws Exception
	{
		books.clear();
		((DefaultListModel)bookList.getModel()).clear();
		
		Set<String> used = new TreeSet<String>();
		Map<String, String> descriptions = new TreeMap<String, String>();
		Map<String, String> titles = new TreeMap<String, String>();
		File index = new File(serverDir, "index.xml");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(index), Charset.forName("UTF-8")));
		String line = null;
		String curBook = null;
		String curDesc = null;
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			if (line.startsWith("<Book") || line.startsWith("<!-- Book"))
			{
				int i = line.indexOf("src=\"");
				curBook = line.substring(i+5, line.indexOf('"', i+6));
				curDesc = "";
				i = line.indexOf("title=\"");
				String title = line.substring(i+7, line.indexOf('"', i+8));
				titles.put(curBook, title);
				if (line.startsWith("<Book"))
					used.add(curBook);
			}
			else if (line.startsWith("</Book"))
			{
				descriptions.put(curBook, curDesc);
			}
			else if (curBook != null)
				curDesc = curDesc+(curDesc.length() > 0 ? "\n" : "")+line;
		}
		reader.close();
		
		File [] files = serverDir.listFiles();
		for (File file : files)
			if (!file.isDirectory() && file.getName().startsWith("book") && file.getName().endsWith(".xml") && titles.containsKey(file.getName()))
		{
			Book book = new Book(file, titles.get(file.getName()), descriptions.get(file.getName()), used.contains(file.getName()));
			books.add(book);
			((DefaultListModel)bookList.getModel()).addElement(book);
		}
	}
	
	void write() throws Exception
	{
		File index = new File(serverDir, "index.xml");
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(index, false), Charset.forName("UTF-8"));
		writer.write("<Index>\n");
		for (Book book : books)
		{
			if (!book.deleted)
			{
				if (book.used)
					writer.write("\t<Book title=\"");
				else writer.write("\t<!-- Book title=\"");
				writer.write(book.name);
				writer.write("\" src=\"");
				writer.write(book.bookFile.getName());
				writer.write("\">\n\t\t");
				writer.write(book.desc);
				if (book.used)
					writer.write("\n\t</Book>\n");
				else writer.write("\n\t</Book -->\n");
			}
			else
			{
				book.bookFile.delete();
				for (File file : book.bookDir.listFiles())
					file.delete();
				book.bookDir.delete();
			}
		}
		writer.write("</Index>");
		writer.close();
	}
}
