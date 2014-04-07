package org.interreg.docexplore.reader.book.page;

import java.util.Map;

import org.interreg.docexplore.reader.InputManager;
import org.interreg.docexplore.reader.book.BookEngine;
import org.interreg.docexplore.reader.book.BookModel;
import org.interreg.docexplore.reader.book.page.Paper.SpringNode;
import org.interreg.docexplore.reader.gfx.Bindable;
import org.interreg.docexplore.util.Pair;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Hand implements BookEngine.Component, InputManager.Listener
{
	BookEngine engine;
	SpringNode held;
	BookModel book;
	PerspectiveCamera camera;
	BookPage page;
	
	public Hand(BookEngine engine, PerspectiveCamera camera) throws Exception
	{
		this.engine = engine;
		this.held = null;
		this.book = engine.model;
		this.camera = camera;
		this.page = new BookPage(book);
	}
	
	public void resetPage() throws Exception {page.dispose(); this.page = new BookPage(book);}
	
	float [] p = {0, 0, 0};
	float [] d = {0, 0, 0};
	float [] projk = {0, 0};
	boolean headedLeft = false;
	public boolean pageIsActive = false;
	
	public boolean touched(int x, int y, int pointer, int button) {return true;}
	public Object objectAt(int x, int y)
	{
		if (engine.roiOverlay.active)
			return null;
		Pair<Boolean, float []> pc = book.toPage(x, y);
		if (pc != null && pc.second != null)
			return engine.roiAt(pc.first, pc.second[0], 1-pc.second[1]);
		return null;
	}
	boolean useDrag = true;
	public void useConfig(Map<String, Object> config)
	{
		Object object = config.get("useDrag");
		if (object != null)
			useDrag = (Boolean)object;
	}
	public void command(String command)
	{
		if (engine.roiOverlay.active)
			return;
		if (command.equals("left"))
			book.app.mainTask.right.doClick();
		else if (command.equals("right"))
			book.app.mainTask.left.doClick();
		else if (command.equals("back"))
			book.app.mainTask.back.doClick();
		else if (command.equals("zoom"))
			book.app.mainTask.zoom.doClick();
	}
	public boolean clicked(int x, int y, int pointer, int button)
	{
		if (engine.app.waitDialog.isActive())
			return true;
		Pair<Boolean, float []> pc = book.toPage(x, y);
		if (pc != null && pc.second != null)
			engine.pageClicked(pc.first, pc.second[0], 1-pc.second[1]);
		else handleCoverInteraction(pc != null, pc == null || pc.first);
		return true;
	}
	void handleCoverInteraction(boolean onBook, boolean left)
	{
		if (engine.frontCoverView)
			engine.turnRight();
		else if (engine.backCoverView)
			engine.turnLeft();
		else if (onBook && left && book.leftStack.nStackPages == 0)
			engine.turnLeft();
		else if (onBook && !left && book.rightStack.nStackPages == 0)
			engine.turnRight();
	}
	float heldx, heldy, heldz, heldl;
	public boolean grabbed(int x, int y, int pointer, int button)
	{
		if (engine.app.waitDialog.isActive())
			return true;
		if (!useDrag)
			return true;
		Ray ray = camera.getPickRay(x, y);
		p[0] = ray.origin.x; p[1] =  ray.origin.y; p[2] = ray.origin.z;
		d[0] = ray.direction.x; d[1] = ray.direction.y; d[2] = ray.direction.z;
		
		boolean left = p[0]-d[2]*d[0] < 0;
		if (left && book.leftStack.nStackPages == 0 || !left && book.rightStack.nStackPages == 0 || engine.frontCoverView || engine.backCoverView)
		{
			handleCoverInteraction(true, left);
			return true;
		}
		boolean wasActive = pageIsActive;
		pageIsActive = true;
		if (!wasActive)
			engine.pageTurnStarted(left);
		
		page.setTo(left ? book.leftStack : book.rightStack);
		
		held = null;
		float mind2 = 0;
		for (int i=0;i<page.paper.snodes[0].length;i++)
		{
			float d2 = page.paper.snodes[page.paper.snodes.length-1][i].dist2FromLine(p, d);
			if (held == null || d2 < mind2) {held = page.paper.snodes[page.paper.snodes.length-1][i]; mind2 = d2;}
		}
		
		heldx = held.node.point[0]; heldy = held.node.point[1]; heldz = held.node.point[2];
		heldl = (float)Math.sqrt(heldx*heldx+heldz*heldz);
		if (held.isStatic)
			held = null;
		return true;
	}
	public boolean dragged(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		if (held == null)
			return true;
		if (!useDrag)
			return true;
		
		d[0] = camera.direction.x; d[1] = camera.direction.y; d[2] = camera.direction.z;
		Ray ray = camera.getPickRay(tx, ty);
		
		float k = (dotProduct(held.node.point, d)-dotProduct(ray.origin, d))/dotProduct(ray.direction, d);
		heldx = ray.origin.x+k*ray.direction.x;
		//heldy = ray.origin.y+k*ray.direction.y;
		heldz = ray.origin.z+k*ray.direction.z;
		constrainHold();
		return true;
	}
	public boolean dropped(int fx, int fy, int tx, int ty, int pointer, int button)
	{
		if (!useDrag)
			return true;
		held = null;
		
		if (pageIsActive)
		{
			double sumx = 0;
			for (int i=0;i<page.paper.snodes.length;i++)
				for (int j=0;j<page.paper.snodes[0].length;j++)
					sumx += page.paper.snodes[i][j].v[0];
			headedLeft = sumx < 0;
		}
		return true;
	}
	public boolean typed(int key)
	{
		if (!active)
			return false;
		if (key == Input.Keys.ESCAPE)
			book.app.mainTask.back.doClick();
		else if (key == Input.Keys.LEFT)
			book.app.mainTask.left.doClick();
		else if (key == Input.Keys.RIGHT)
			book.app.mainTask.right.doClick();
		else if (key == Input.Keys.UP)
			book.app.mainTask.zoom.doClick();
		else return false;
		return true;
	}
	public boolean scrolled(int amount) {return false;}
	
	public void turnLeft()
	{
		if (pageIsActive)
			return;
		pageIsActive = true;
		engine.pageTurnStarted(true);
		page.setTo(book.leftStack);
		held = null;
		headedLeft = false;
	}
	public void turnRight()
	{
		if (pageIsActive)
			return;
		pageIsActive = true;
		engine.pageTurnStarted(false);
		page.setTo(book.rightStack);
		held = null;
		headedLeft = true;
	}
	
	void constrainHold()
	{
		float l = (float)Math.sqrt(heldx*heldx+heldz*heldz);
		heldx *= heldl/l; heldz *= heldl/l;
	}
	
	public void update()
	{
		if (!pageIsActive)
			return;
		int nSteps = 5, nStepsFree = 2;
		for (int i=0;i<nSteps;i++)
		{
			if (i < nSteps-nStepsFree) updateConstraints();
			page.update();
		}
	}
	
	void updateConstraints()
	{
		if (held != null)
		{
			held.node.point[0] = heldx;
			held.node.point[1] = heldy;
			held.node.point[2] = heldz;
		}
		else if (pageIsActive)
		{
			double distFromEdge = 0;
			float [] ext = headedLeft ? book.leftStack.projection[book.leftStack.projection.length-1] :
				book.rightStack.projection[book.rightStack.projection.length-1];
			for (int i=0;i<page.paper.snodes[0].length;i++)
			{
				float y = .5f*book.cover.coverHeight*(1-book.leftStack.pageHeight)+
					book.pageHeight*book.cover.coverHeight*(1-i*1.f/(page.paper.snodes[0].length-1));
				SpringNode node = page.paper.snodes[page.paper.snodes.length-1][i];
				float d2 = (ext[0]-node.node.point[0])*(ext[0]-node.node.point[0])+(y-node.node.point[1])*(y-node.node.point[1]);
				distFromEdge += d2;
				node.attract(1.3f*ext[0], y, ext[1], .02+1f*(1/(100*d2+1)));
			}
			
			distFromEdge = 0;
			for (int i=0;i<page.paper.snodes[0].length;i++)
			{
				SpringNode node = page.paper.snodes[page.paper.snodes.length-1][i];
				distFromEdge += (node.node.point[0]-ext[0])*(node.node.point[0]-ext[0])+
					(node.node.point[2]-ext[1])*(node.node.point[2]-ext[1]);
			}
			
			//System.out.println(distFromEdge/activePage.paper.snodes[0].length);
			distFromEdge /= page.paper.snodes[0].length;
			if (distFromEdge/page.paper.snodes[0].length < .00002)
			{
				pageIsActive = false;
				engine.pageTurnEnded(headedLeft);
			}
		}
	}
	
	float dotProduct(float [] u, float [] v) {return u[0]*v[0]+u[1]*v[1]+u[2]*v[2];}
	float dotProduct(Vector3 u, float [] v) {return u.x*v[0]+u.y*v[1]+u.z*v[2];}
	
	boolean active = false;
	public void activate(boolean active)
	{
		if (this.active == active)
			return;
		this.active = active;
		if (active)
		{
			book.app.input.addListener(this);
		}
		else
		{
			book.app.input.removeListener(this);
		}
	}

	public void render()
	{
		if (!pageIsActive)
			return;
		
		int leftPageIndex = book.leftStack.nStackPages > 0 ? 1+2*(book.leftStack.nStackPages-1) : -1;
		Bindable pageFront = engine.book.pages.get(leftPageIndex+1).getTransTexture();
		Bindable pageBack = engine.book.pages.get(leftPageIndex+2).getTransTexture();
		page.render(pageFront, pageBack);
	}
}
