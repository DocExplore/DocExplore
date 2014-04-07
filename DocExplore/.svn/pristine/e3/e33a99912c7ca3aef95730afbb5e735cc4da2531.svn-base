package org.interreg.docexplore.reader.gfx;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;

public class Camera
{
	double [] eye, dir;
	
	public Camera()
	{
		double sqrt3i = .577;
		double dist = 3;
		
		this.eye = new double [] {-dist*sqrt3i, dist*sqrt3i, dist*sqrt3i};
		this.dir = new double [] {dist*sqrt3i, -dist*sqrt3i, -dist*sqrt3i};
	}
	
	Matrix4 model = new Matrix4();
	Vector3 position = new Vector3(), target = new Vector3(), cup = new Vector3();
	public void setup()
	{
		GL10 gl = Gdx.gl10;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glFrustumf(-.25f, .25f, -.25f, .25f, .25f, 100f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		double [] buf = Math3D.getVector3d(), up = Math3D.getVector3d();
		Math3D.crossProduct(Math3D.basej, dir, buf);
		Math3D.crossProduct(dir, buf, up);
		Math3D.normalize(up, up);
		
		position.set((float)eye[0], (float)eye[1], (float)eye[2]);
		target.set((float)(eye[0]+dir[0]), (float)(eye[1]+dir[1]), (float)(eye[2]+dir[2]));
		cup.set((float)up[0], (float)up[1], (float)up[2]);
		model.setToLookAt(position, target, cup);
//		Gdx.glu.gluLookAt(gl, (float)eye[0], (float)eye[1], (float)eye[2], 
//			(float)(eye[0]+dir[0]), (float)(eye[1]+dir[1]), (float)(eye[2]+dir[2]), 
//			(float)up[0], (float)up[1], (float)up[2]);
		
		Math3D.freeVector3d(buf);
		Math3D.freeVector3d(up);
	}
	
	public void unsetup()
	{
		GL10 gl = Gdx.gl10;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
	}
	
	FloatBuffer modelView = BufferUtils.newFloatBuffer(16);
	FloatBuffer projection = BufferUtils.newFloatBuffer(16);
	IntBuffer viewport = BufferUtils.newIntBuffer(16);
	FloatBuffer pos = BufferUtils.newFloatBuffer(4);
	public double [] unproject(double [] screen, double [] res)
	{
		GL10 gl = Gdx.gl10;
		Gdx.gl11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelView);
		Gdx.gl11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projection);
		gl.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		
		unproject((float)screen[0], (float)screen[1], (float)screen[2], 
			modelView.array(), 
			projection.array(), 
			viewport.array(), 
			res);
		//res[0] = pos.get(0); res[1] = pos.get(1); res[2] = pos.get(2);
		return res;
	}
	
	float [] fTempo = new float [8];
	int unproject(float objx, float objy, float objz, float [] modelview, float [] projection, int [] viewport, double [] res)
	{
	    //Modelview transform
	    fTempo[0]=modelview[0]*objx+modelview[4]*objy+modelview[8]*objz+modelview[12];  //w is always 1
	    fTempo[1]=modelview[1]*objx+modelview[5]*objy+modelview[9]*objz+modelview[13];
	    fTempo[2]=modelview[2]*objx+modelview[6]*objy+modelview[10]*objz+modelview[14];
	    fTempo[3]=modelview[3]*objx+modelview[7]*objy+modelview[11]*objz+modelview[15];
	    //Projection transform, the final row of projection matrix is always [0 0 -1 0]
	    //so we optimize for that.
	    fTempo[4]=projection[0]*fTempo[0]+projection[4]*fTempo[1]+projection[8]*fTempo[2]+projection[12]*fTempo[3];
	    fTempo[5]=projection[1]*fTempo[0]+projection[5]*fTempo[1]+projection[9]*fTempo[2]+projection[13]*fTempo[3];
	    fTempo[6]=projection[2]*fTempo[0]+projection[6]*fTempo[1]+projection[10]*fTempo[2]+projection[14]*fTempo[3];
	    fTempo[7]=-fTempo[2];
	    //The result normalizes between -1 and 1
	    if(fTempo[7]==0.0)        //The w value
	    return 0;
	    fTempo[7]=1f/fTempo[7];
	    //Perspective division
	    fTempo[4]*=fTempo[7];
	    fTempo[5]*=fTempo[7];
	    fTempo[6]*=fTempo[7];
	    //Window coordinates
	    //Map x, y to range 0-1
	    res[0]=(fTempo[4]*0.5+0.5)*viewport[2]+viewport[0];
	    res[1]=(fTempo[5]*0.5+0.5)*viewport[3]+viewport[1];
	    //This is only correct when glDepthRange(0.0, 1.0)
	    res[2]=(1.0+fTempo[6])*0.5;  //Between 0 and 1
	    return 1;
	}
	
	public void turn(double dx, double dy)
	{
		double [] left = Math3D.getVector3d(), up = Math3D.getVector3d(), buf = Math3D.getVector3d();
		
		Math3D.crossProduct(Math3D.basej, dir, left);
		Math3D.crossProduct(dir, left, up);
		Math3D.normalize(left, left);
		Math3D.normalize(up, up);
		
		Math3D.add(dir, Math3D.scale(left, dx, buf), dir);
		Math3D.add(dir, Math3D.scale(up, dy, buf), dir);
		Math3D.normalize(dir, dir);
		
		Math3D.freeVector3d(left);
		Math3D.freeVector3d(up);
		Math3D.freeVector3d(buf);
	}
	
	public void move(double dx, double dy, double dz)
	{
		double [] left = Math3D.getVector3d(), up = Math3D.getVector3d(), front = Math3D.getVector3d(), buf = Math3D.getVector3d();
		
		Math3D.set(front, dir[0], 0, dir[2]);
		Math3D.normalize(front, front);
		Math3D.crossProduct(Math3D.basej, front, left);
		Math3D.crossProduct(front, left, up);
		Math3D.normalize(left, left);
		Math3D.normalize(up, up);
		
		Math3D.add(eye, Math3D.scale(left, dx, buf), eye);
		Math3D.add(eye, Math3D.scale(up, dy, buf), eye);
		Math3D.add(eye, Math3D.scale(front, dz, buf), eye);
		
		Math3D.freeVector3d(left);
		Math3D.freeVector3d(up);
		Math3D.freeVector3d(front);
		Math3D.freeVector3d(buf);
	}
}
