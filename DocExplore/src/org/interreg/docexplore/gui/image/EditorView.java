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
