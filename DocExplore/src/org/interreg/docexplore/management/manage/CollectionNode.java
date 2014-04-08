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

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.interreg.docexplore.manuscript.Book;

public class CollectionNode implements ListModel
{
	ManageComponent comp;
	ExecutorService service;
	Vector<Book> books;
	
	CollectionNode(ManageComponent comp)
	{
		this.comp = comp;
		this.service = Executors.newFixedThreadPool(4);
		reload();
	}
	
	public void reload()
	{
		this.books = new Vector<Book>();
		for (Book book : comp.handler.getBooks())
			books.add(book);
		notifyListeners();
	}
	
	List<ListDataListener> listeners = new LinkedList<ListDataListener>();
	public void addListDataListener(ListDataListener listener) {listeners.add(listener);}
	public void removeListDataListener(ListDataListener listener) {listeners.remove(listener);}
	public void notifyListeners()
	{
		if (comp.bookList == null)
			return;
		ListDataEvent e = new ListDataEvent(comp.bookList, ListDataEvent.CONTENTS_CHANGED, 0, books.size()-1);
		for (ListDataListener listener : listeners)
			listener.contentsChanged(e);
	}
	
	public Object getElementAt(int i) {return books.get(i);}
	public int getSize() {return books.size();}
	
}
