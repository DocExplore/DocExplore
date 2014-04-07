package org.interreg.docexplore.reader.book;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.book.page.Hand;
import org.interreg.docexplore.reader.book.roi.ROIOverlay;
import org.interreg.docexplore.reader.book.zoom.BookZoom;
import org.interreg.docexplore.reader.gfx.CameraKeyFrame;
import org.interreg.docexplore.reader.gui.Dialog;
import org.lwjgl.input.Keyboard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;


public class BookEngine implements ReaderApp.Module
{
	public static interface Component
	{
		public void update();
		public void render();
		public void activate(boolean active);
	}
	
	public final ReaderApp app;
	public boolean active;
	
	public final BookModel model;
	public final PerspectiveCamera camera;
	public final Hand hand;
	public BookSpecification book;
	public BookEngineRenderer renderer;
	
	ROISpecification selectedRegion;
	public ROIOverlay roiOverlay;
	public CameraKeyFrame attractor;
	public final CameraKeyFrame globalFrame, roiFrame;
	
	public final BookZoom zoom;
	
	Dialog waitDialog = null;
	
	public List<Component> components;
	
	public BookEngine(ReaderApp app, float length, float height) throws Exception
	{
		this.app = app;
		this.active = false;
		this.model = new BookModel(app, length, height);
		
		this.camera = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = .125f;
		camera.far = 10;
		this.globalFrame = new CameraKeyFrame();
		this.roiFrame = new CameraKeyFrame();
		globalFrame.setup(0f, 1f*model.cover.coverHeight, viewDist*model.cover.coverHeight, 
			0, 0, -viewDist*model.cover.coverHeight, 
			0, 1, 1, 
			(float)(.14*Math.PI));

		this.attractor = globalFrame;
		this.renderer = new BookEngineRenderer(this);
		camera.update();
		
		model.setAngle((float)defaultAngle, (float)defaultAngle);
		//model.rotate(-Math.PI/2, 1, 0, 0);
		
		this.components = new LinkedList<Component>();
		this.hand = new Hand(this, camera);
		components.add(hand);
		
		this.roiOverlay = new ROIOverlay(this);
		components.add(roiOverlay);
		
		this.zoom = new BookZoom(this);
		components.add(zoom);
	}
	
	public void pageTurnStarted(boolean left)
	{
		if (left)
			{model.leftStack.nStackPages--; model.leftStack.update(); renderer.setupLeftRoiMask();}
		else {model.rightStack.nStackPages--; model.rightStack.update(); renderer.setupRightRoiMask();}
	}
	
	public void pageTurnEnded(boolean left)
	{
		if (left)
			{model.leftStack.nStackPages++; model.leftStack.update(); renderer.setupLeftRoiMask();}
		else {model.rightStack.nStackPages++; model.rightStack.update(); renderer.setupRightRoiMask();}
		updatePageCache();
		updateLayout();
	}

	public void pageClicked(boolean left, float x, float y)
	{
		selectedRegion = roiAt(left, x, y);
		if (selectedRegion == null)
			return;
		
		roiFrame.setup(model, selectedRegion, left);
		roiOverlay.setup(selectedRegion);
		roiOverlay.activate(true);
	}
	
	public ROISpecification roiAt(boolean left, float x, float y)
	{
		int pageIndex = left ? 1+2*(model.leftStack.nStackPages-1) : 2+2*(model.leftStack.nStackPages-1)+(hand.pageIsActive ? 2 : 0);
		for (ROISpecification test : book.pages.get(pageIndex).regions)
			if (test.shape.contains(x, y))
				return test;
		return null;
	}
	
	public boolean frontCoverView = false, backCoverView = false;
	public void turnLeft()
	{
		if (app.waitDialog.isActive())
			return;
		if (frontCoverView || hand.pageIsActive)
			return;
		else if (backCoverView)
		{
			backCoverView = false;
			animateCover(.001f, (float)defaultAngle, (float)(-.5*Math.PI), 0, .5f, 0);
		}
		else if (model.leftStack.nStackPages == 0)
		{
			frontCoverView = true;
			animateCover((float)defaultAngle, .001f, 0, (float)(.5*Math.PI), 0, -.5f);
		}
		else app.submitRenderTaskAndWait(new Runnable() {public void run() {hand.turnLeft();}});
	}
	public void turnRight()
	{
		if (app.waitDialog.isActive())
			return;
		if (backCoverView || hand.pageIsActive)
			return;
		else if (frontCoverView)
		{
			frontCoverView = false;
			animateCover(.001f, (float)defaultAngle, (float)(.5*Math.PI), 0, -.5f, 0);
		}
		else if (model.rightStack.nStackPages == 0)
		{
			backCoverView = true;
			animateCover((float)defaultAngle, .001f, 0, (float)(-.5*Math.PI), 0, .5f);
		}
		else app.submitRenderTaskAndWait(new Runnable() {public void run() {hand.turnRight();}});
	}
	public boolean coverOnly()
	{
		return (frontCoverView && globalRot > .499*Math.PI) || (backCoverView && globalRot < -.499*Math.PI);
	}
	
	int cacheSpread = 3;
	void updatePageCache()
	{
		int left = 1+2*(model.leftStack.nStackPages-1);
		app.logger.addEntry("Displaying page "+left);
		for (int i=0;i<book.pages.size();i++)
			if (i > left-cacheSpread && i < left+1+cacheSpread)
				book.pages.get(i).getTexture();
			else book.pages.get(i).release();
	}
	
	public static interface ROILayoutListener
	{
		public void roiLayoutChanged(ROISpecification [] rois);
	}
	List<ROILayoutListener> roiLayoutListeners = new LinkedList<ROILayoutListener>();
	public void addRoiLayoutListener(ROILayoutListener listener) {roiLayoutListeners.add(listener);}
	public void removeRoiLayoutListener(ROILayoutListener listener) {roiLayoutListeners.remove(listener);}
	
	Object roiMonitor = new Object();
	ROISpecification [] rois = new ROISpecification [0];
	public ROISpecification [] getRoiLayout() {synchronized (roiMonitor) {return rois;}} 
	void updateLayout()
	{
		synchronized (roiMonitor)
		{
			if (frontCoverView || backCoverView)
				rois = new ROISpecification [0];
			else
			{
				int leftPageIndex = model.leftStack.nStackPages > 0 ? 1+2*(model.leftStack.nStackPages-1) : -1;
				int rightPageIndex = model.rightStack.nStackPages > 0 ? leftPageIndex+1 : -1;
				
				PageSpecification leftPage = leftPageIndex >= 0 ? book.pages.get(leftPageIndex) : null;
				PageSpecification rightPage = rightPageIndex >= 0 ? book.pages.get(rightPageIndex) : null;
				rois = new ROISpecification [(leftPage != null ? leftPage.regions.size() : 0)+(rightPage != null ? rightPage.regions.size() : 0)];
				int roiCnt = 0;
				
				if (leftPageIndex >= 0)
					for (ROISpecification roi : leftPage.regions)
						rois[roiCnt++] = roi;
				if (rightPageIndex >= 0)
					for (ROISpecification roi : rightPage.regions)
						rois[roiCnt++] = roi;
			}
		}
		
		for (ROILayoutListener listener : roiLayoutListeners)
			listener.roiLayoutChanged(rois);
	}
	
	public void activate(boolean active)
	{
		if (this.active == active)
			return;
		this.active = active;
		
		if (active)
		{
			hand.activate(true);
		}
		else
		{
			hand.activate(false);
			app.waitDialog.activate(false);
			app.submitRenderTaskAndWait(new Runnable() {public void run() {book.releaseAll();}});
		}
	}
	
	float viewDist = 3.2f;
	public void setBook(final BookSpecification book)
	{
		app.waitDialog.activate(true);
		this.book = book;
		
		model.cover.set(1, (float)(1/book.aspectRatio));
		
		model.setAngle((float).001, (float).001);
		globalRot = .5*Math.PI;
		globalTrans = -.5f;
		frontCoverView = true;
		backCoverView = false;
		
		app.submitRenderTaskAndWait(new Runnable() {public void run()
		{
			try {hand.resetPage();}
			catch (Exception e) {e.printStackTrace(); System.exit(0);}
		}});
		book.prepare(app.client);
		
		model.nPages = book.pages.size()/2;
		model.leftStack.nStackPages = 0;
		model.rightStack.nStackPages = model.nPages;
		updatePageCache();
		
		renderer.setupLeftRoiMask();
		renderer.setupRightRoiMask();
		
		new CameraKeyFrame().setup(0f, .9f*model.cover.coverHeight, 1.75f*viewDist*model.cover.coverHeight, 
			0, .9f*model.cover.coverHeight, -1.75f*viewDist*model.cover.coverHeight, 
			0, 1, 1, 
			(float)(.14*Math.PI)).attract(camera, 1);
		
		float dist = viewDist*Math.max(model.cover.coverHeight, 1.4f*Gdx.graphics.getHeight()*model.cover.coverLength/Gdx.graphics.getWidth());
		globalFrame.setup(0f, model.cover.coverHeight, dist, 
			0, 0, -dist, 
			0, 1, 1, 
			(float)(.14*Math.PI));
	}

	public void dispose()
	{
		model.dispose();
		renderer.dispose();
	}

	float [] normalize(float [] v)
	{
		double l = Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
		v[0] /= l; v[1] /= l; v[2] /= l;
		return v;
	}
	
	public double defaultAngle = .4*Math.PI;
	public double coverAngle = defaultAngle;
	public double globalRot = 0, globalTrans = 0;
	public void update()
	{
		if (book == null || !active)
			return;
		
		for (Component extension : components)
			extension.update();
		
		if (Keyboard.isKeyDown(Keyboard.KEY_UP))
			globalFrame.pos[2] -= .01;
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			globalFrame.pos[2] += .01;
		
		int left = 1+2*(model.leftStack.nStackPages-1);
		boolean leftLoaded = left < 0 || book.pages.get(left).isLoaded();
		boolean rightLoaded = left+1 >= book.pages.size() || book.pages.get(left+1).isLoaded();
		app.waitDialog.activate(!leftLoaded || !rightLoaded);
	}
	
	public void render()
	{
		if (book == null || !active)
			return;
		renderer.render();
	}
	
	int nCoverSteps = 20;
	public void animateCover(final float fromAngle, final float toAngle, final float fromRot, final float toRot, final float fromTrans, final float toTrans)
	{
		final float [] angle = {0}, rot = {0}, trans = {0};
		final Runnable task = new Runnable() {public void run()
		{
			model.setAngle(angle[0], angle[0]);
			globalRot = rot[0];
			globalTrans = trans[0];
		}};
		new Thread() {public void run() 
		{
			for (int i=0;i<nCoverSteps;i++)
			{
				float k = i*1f/(nCoverSteps-1);
				angle[0] = fromAngle+k*(toAngle-fromAngle);
				rot[0] = fromRot+k*(toRot-fromRot);
				trans[0] = fromTrans+k*(toTrans-fromTrans);
				app.submitRenderTaskAndWait(task);
			}
			updateLayout();
		}}.start();
	}
}
