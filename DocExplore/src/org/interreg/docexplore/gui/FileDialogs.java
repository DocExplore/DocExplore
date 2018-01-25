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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.nfd.JNativeFileDialog;

public class FileDialogs
{
	public static class Category implements Serializable
	{
		private static final long serialVersionUID = -568802345017021281L;
		
		public final String name;
		public final Collection<String> filters;
		private File current;
		
		public Category(String name, Collection<String> filters, File current)
		{
			this.name = name;
			this.filters = filters;
			this.current = current;
		}
		
		public boolean conatins(String name)
		{
			int dot = name.lastIndexOf('.');
			if (dot < 0)
				return false;
			String ext = name.substring(dot+1);
			return filters.contains(ext);
		}
		
		public String filterLabel()
		{
			String label = name+" (";
			boolean first = true;
			for (String ext : filters)
			{
				label += (first ? "" : ", ")+"*."+ext;
				first = false;
			}
			label += ")";
			return label;
		}
	}
	
	public File getDefaultDocumentDir()
	{
		File dir = new File(System.getProperty("user.home")+"/Documents");
		if (!dir.exists())
			dir = fdcache.getParentFile();
		return dir;
	}
	
	Map<String, Category> categories;
	public final File fdcache;
	JNativeFileDialog fileDialog = new JNativeFileDialog();
	
	@SuppressWarnings("unchecked")
	public FileDialogs(File rootDir)
	{
		this.fdcache = new File(rootDir, ".fdcache");
		if (fdcache.exists()) try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fdcache));
			categories = (Map<String, Category>)in.readObject();
			in.close();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		
		if (categories == null)
			categories = new TreeMap<String, Category>();
	}
	
	private void writeCategories()
	{
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fdcache));
			out.writeObject(categories);
			out.close();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
	}
	
	public Category getOrCreateCategory(String name, Collection<String> filters)
	{
		Category category = categories.get(name);
		if (category != null)
			return category;
		
		category = new Category(name, filters, getDefaultDocumentDir());
		categories.put(name, category);
		writeCategories();
		return category;
	}
	
	public File openFile(Category category) {return openFile(category, null);}
	public File openFile(Category category, String title)
	{
		fileDialog.acceptFiles = true;
		fileDialog.acceptFolders = false;
		fileDialog.multipleSelection = false;
		fileDialog.title = title != null ? title : Lang.s("fileOpen")+" "+category.name;
		fileDialog.setCurrentFile(category.current.exists() ? category.current : getDefaultDocumentDir());
		fileDialog.setFileFilter(category.filterLabel(), category.filters);
		if (!fileDialog.showOpenDialog())
			return null;
		File file = fileDialog.getSelectedFile();
		if (file != null)
		{
			category.current = file.getParentFile();
			writeCategories();
		}
		return file;
	}
	
	public File saveFile(Category category) {return saveFile(category, null);}
	public File saveFile(Category category, String title)
	{
		fileDialog.acceptFiles = true;
		fileDialog.acceptFolders = false;
		fileDialog.multipleSelection = false;
		fileDialog.title = title != null ? title : Lang.s("fileSave")+" "+category.name;
		fileDialog.setCurrentFile(category.current.exists() ? category.current : getDefaultDocumentDir());
		fileDialog.setFileFilter(category.filterLabel(), category.filters);
		if (!fileDialog.showSaveDialog())
			return null;
		File file = fileDialog.getSelectedFile();
		if (file != null)
		{
			category.current = file.getParentFile();
			writeCategories();
		}
		if (!category.conatins(file.getName()))
			file = new File(file.getParent(), file.getName()+"."+category.filters.iterator().next());
		return file;
	}
	public File [] openFiles(Category category) {return openFiles(category, null);}
	public File [] openFiles(Category category, String title)
	{
		fileDialog.acceptFiles = true;
		fileDialog.acceptFolders = false;
		fileDialog.multipleSelection = true;
		fileDialog.title = title != null ? title : Lang.s("fileOpen")+" "+category.name;
		fileDialog.setCurrentFile(category.current.exists() ? category.current : getDefaultDocumentDir());
		fileDialog.setFileFilter(category.filterLabel(), category.filters);
		if (!fileDialog.showOpenDialog())
			return null;
		File [] files = fileDialog.getSelectedFiles();
		if (files != null && files.length > 0 && files[0] != null)
		{
			category.current = files[0].getParentFile();
			writeCategories();
		}
		return files;
	}
}
