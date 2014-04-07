package org.interreg.docexplore.reader.gfx;

import java.util.Arrays;

import org.interreg.docexplore.reader.book.BookModel;
import org.interreg.docexplore.reader.book.BookPageStack;
import org.interreg.docexplore.reader.book.ROISpecification;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

public class CameraKeyFrame
{
	public final float [] pos, dir, up;
	public float fov;

	public CameraKeyFrame(float [] pos, float [] dir, float [] up, float fov)
	{
		this.pos = pos;
		this.dir = dir;
		this.up = up;
		this.fov = fov;
	}
	public CameraKeyFrame(CameraKeyFrame frame)
	{
		this(Arrays.copyOf(frame.pos, 3), Arrays.copyOf(frame.dir, 3), Arrays.copyOf(frame.up, 3), frame.fov);
	}
	public CameraKeyFrame()
	{
		this.pos = new float [] {0, 0, 0};
		this.dir = new float [] {0, 0, 1};
		this.up = new float [] {0, 0, 0};
		this.fov = 90;
	}
	
	float [] buf1 = {0, 0, 0}, buf2 = {0, 0, 0};
	public CameraKeyFrame setup(float ex, float ey, float ez, float tx, float ty, float tz, float ux, float uy, float uz, float fov)
	{
		Math3D.set(pos, ex, ey, ez);
		Math3D.set(dir, tx, ty, tz);
		Math3D.diff(pos, dir, dir);
		
		Math3D.set(up, ux, uy, uz);
		Math3D.crossProduct(up, dir, buf1);
		Math3D.crossProduct(dir, buf1, up);
		Math3D.normalize(dir, dir);
		Math3D.normalize(up, up);
		this.fov = fov;
		
		return this;
	}
	
	float [] tl = {0, 0, 0}, br = {0, 0, 0}, middle = {0, 0, 0};
	public void setup(BookModel model, ROISpecification region, boolean left)
	{
		BookPageStack stack = left ? model.leftStack : model.rightStack;
		stack.fromPage(region.shape.minx, 1-region.shape.miny, tl);
		stack.fromPage(region.shape.maxx, 1-region.shape.maxy, br);
		
		float margin = .15f;
		float xm = margin*(br[0]-tl[0]);
		tl[0] -=  xm; br[0] += xm;
		float ym = margin*(tl[1]-br[1]);
		tl[1] += ym; br[1] -= ym;
		
		float fov = (float)(.1*Math.PI);
		float aspect = 1;//Gdx.graphics.getWidth()/Gdx.graphics.getHeight();
		float viewDistForX = (float)(aspect*(br[0]-tl[0])/Math.sin(fov));
		float viewDistForY = (float)(aspect*(tl[1]-br[1])/Math.sin(fov));
		float viewDist = Math.max(viewDistForX, viewDistForY);
		float midx = (br[0]+tl[0])/2, midy = (tl[1]+br[1])/2, midz = (tl[2]+br[2])/2;
		float right = (float)(.5*Math.sin(fov)*viewDist);
		
		if (viewDist < .5f)
			viewDist = .5f;
		
		setup(midx+right, midy, midz+viewDist, 
			midx+right, midy, midz, 
			0, 1, 1, fov);
	}
	
	void attract(Vector3 v, float [] f, float amount)
	{
		v.x += amount*(f[0]-v.x);
		v.y += amount*(f[1]-v.y);
		v.z += amount*(f[2]-v.z);
	}
	
	public void attract(PerspectiveCamera camera, float amount)
	{
		attract(camera.position, pos, amount);
		attract(camera.direction, dir, amount);
		attract(camera.up, up, amount);
		camera.fieldOfView += amount*(180*fov/Math.PI-camera.fieldOfView);
		camera.normalizeUp();
		camera.update();
	}
}
