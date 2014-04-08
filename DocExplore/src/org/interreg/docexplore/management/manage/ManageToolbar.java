/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.manage;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.gui.ToolbarButton;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;

@SuppressWarnings("serial")
public class ManageToolbar extends JPanel
{
	ManageComponent manageComp;
	ToolbarButton addButton, deleteButton, editButton, /*processButton, */exportButton, importButton;
	
	public ManageToolbar(final ManageComponent manageComp)
	{
		super(new FlowLayout(FlowLayout.LEFT));
		
		this.manageComp = manageComp;
		
		addButton = new ToolbarButton("add-24x24.png", XMLResourceBundle.getBundledString("manageAddBookLabel"));
		deleteButton = new ToolbarButton("remove-24x24.png", XMLResourceBundle.getBundledString("manageDeleteBookLabel"));
		editButton = new ToolbarButton("pencil-24x24.png", XMLResourceBundle.getBundledString("manageRenameLabel"));
//		processButton = new ToolbarButton("gears-24x24.png", XMLResourceBundle.getBundledString("manageProcessBookLabel"));
		exportButton = new ToolbarButton("export-24x24.png", XMLResourceBundle.getBundledString("manageExportBookLabel"));
		importButton = new ToolbarButton("import-24x24.png", XMLResourceBundle.getBundledString("manageImportBookLabel"));
		
		addButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0)
		{
			List<File> files = SelectPagesPanel.show();
			if (files == null)
				return;
			String title = JOptionPane.showInputDialog(manageComp.win, XMLResourceBundle.getBundledString("manageInputTitleLabel"));
			if (title == null || title.trim().length() == 0)
				return;
			try {if (manageComp.findTitle(title) != null)
			{
				JOptionPane.showMessageDialog(manageComp.win, XMLResourceBundle.getBundledString("manageRenameExistsMessage"));
				return;
			}}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			manageComp.handler.addBook(title, files);
			((CollectionNode)manageComp.bookList.getModel()).reload();
			manageComp.bookList.repaint();
		}});
		deleteButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0)
		{
			@SuppressWarnings({ "rawtypes", "unchecked" })
			List<Book> books = (List)Arrays.asList(manageComp.bookList.getSelectedValues());
			if (manageComp.handler.booksDeleted(books))
			{
				((CollectionNode)manageComp.bookList.getModel()).reload();
				manageComp.bookList.clearSelection();
				manageComp.bookList.repaint();
			}
		}});
		editButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0)
		{
			@SuppressWarnings({ "rawtypes", "unchecked" })
			List<Book> books = (List)Arrays.asList(manageComp.bookList.getSelectedValues());
			if (books.size() != 1)
				return;
			Book book = books.get(0);
			String title = JOptionPane.showInputDialog(manageComp.win, XMLResourceBundle.getBundledString("manageInputTitleLabel"), book.getName());
			if (title == null || title.trim().length() == 0)
				return;
			try
			{
				if (manageComp.findTitle(title) != null)
				{
					JOptionPane.showMessageDialog(manageComp.win, XMLResourceBundle.getBundledString("manageRenameExistsMessage"));
					return;
				}
				book.setName(title);
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			((CollectionNode)manageComp.bookList.getModel()).reload();
			manageComp.bookList.repaint();
		}});
//		processButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0)
//		{
//			@SuppressWarnings({ "rawtypes", "unchecked" })
//			List<Book> books = (List)Arrays.asList(manageComp.tree.getSelectedValues());
//			if (books.size() == 1)
//				manageComp.handler.pagesProcessed(getPages(books.get(0)));
//		}});
		exportButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0)
		{
			Book book = (Book)manageComp.bookList.getSelectedValue();
			File file = DocExploreTool.getFileDialogs().saveFile(DocExploreTool.getBookCategory());
			if (file == null)
				return;
			manageComp.exportBook(book, file);
		}});
		importButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0)
		{
			File file = DocExploreTool.getFileDialogs().openFile(DocExploreTool.getBookCategory());
			if (file == null)
				return;
			manageComp.importBook(file, (Book)manageComp.bookList.getSelectedValue());
		}});
		
		deleteButton.setEnabled(false);
		editButton.setEnabled(false);
//		processButton.setEnabled(false);
		exportButton.setEnabled(false);
		importButton.setEnabled(true);
		
		add(addButton);
		add(deleteButton);
		add(editButton);
//		add(processButton);
		add(exportButton);
		add(importButton);
	}
	
	List<Page> getPages(Book book)
	{
		Vector<Page> res = new Vector<Page>();
		try
		{
			int nPages = book.getLastPageNumber();
			res.ensureCapacity(nPages);
			for (int i=1;i<=nPages;i++)
				res.add(book.getPage(i));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return res;
	}
}
