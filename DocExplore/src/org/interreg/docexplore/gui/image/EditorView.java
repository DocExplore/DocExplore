/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui.image;

import java.awt.Graphics2D;

public class EditorView extends NavView
{
	private static final long serialVersionUID = -2351797486690743148L;

	public static interface Operation<T extends EditorView>
	{
		public void pointClicked(T view, int cx, int cy, double vx, double vy, int modifiers, int clickCount);
		public void pointHovered(T view, int cx, int cy, double vx, double vy, int modifiers);
		public void pointGrabbed(T view, int cx, int cy, double vx, double vy, int modifiers);
		public void pointDragged(T view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers);
		public void pointDropped(T view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers);
		public void contextMenuRequested(T view, int cx, int cy, double vx, double vy, int modifiers);
		
		public void render(T view, Graphics2D g, double pixelSize);
		public boolean completed();
		public String getMessage();
	}
	
	@SuppressWarnings("rawtypes")
	private Operation defaultOperation;
	@SuppressWarnings("rawtypes")
	private Operation operation;
	
	public EditorView() {this(null);}
	public EditorView(Operation<? extends EditorView> defaultOperation)
	{
		this.defaultOperation = defaultOperation;
		this.operation = defaultOperation;
	}
	
	void checkForCompletion()
	{
		if (operation == null)
			return;
		if (operation.completed())
			cancelOperation();
		if (operation != null)
			onMessageChanged(operation.getMessage());
	}
	protected void onMessageChanged(String s) {}
	
	@Override protected NavViewInputListener createInputListener()
	{
		return new EditorViewInputListener(this);
	}
	
	public void setOperation(Operation<? extends EditorView> operation)
	{
		this.operation = operation;
		checkForCompletion();
		repaint();
	}
	@SuppressWarnings("unchecked")
	public Operation<EditorView> getOperation() {return operation;}
	public void cancelOperation() {this.operation = defaultOperation;}
	
	@SuppressWarnings("unchecked")
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		super.drawView(g, pixelSize);
		if (operation != null)
			operation.render(this, g, pixelSize);
	}
}
