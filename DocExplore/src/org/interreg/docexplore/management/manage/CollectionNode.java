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
