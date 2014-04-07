package org.interreg.docexplore.reader.book.page;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.reader.util.Math3D;

public class Paper
{
	public static final float cork = .1f;
	public static final float gravity = -.0003f;
	public static final float vlim = .05f;
	public static final float damping = .6f;
	
	public static class Spring
	{
		SpringNode a, b;
		float desired;
		
		public Spring(SpringNode a, SpringNode b)
		{
			this.a = a;
			this.b = b;
			
			this.desired = Math3D.distance(a.node.point, b.node.point);
		}
		
		float [] buf = {0, 0, 0}, buf2 = {0, 0, 0};
		public void update()
		{
			Math3D.diff(b.node.point, a.node.point, buf);
			float dist = Math3D.norm(buf);
			Math3D.scale(buf, 1/dist, buf);
			
			float cor = cork*(dist-desired);
			Math3D.add(a.v, Math3D.scale(buf, -cor, buf2), a.v);
			Math3D.add(b.v, Math3D.scale(buf, cor, buf2), b.v);
		}
	}
	
	public static class SpringNode
	{
		public boolean isStatic;
		public final Grid.Node node;
		Spring [] springs;
		public final float [] v;
		
		public SpringNode(Grid.Node node)
		{
			this.isStatic = false;
			this.node = node;
			this.springs = new Spring [0];
			this.v = new float [] {0, 0, 0};
		}
		
		public void attract(double x, double y, double z, double cor)
		{
			double dx = x-node.point[0], dy = y-node.point[1], dz = z-node.point[2];
			double dist = Math.sqrt(dx*dx+dy*dy+dz*dz);
			dx /= dist; dy /= dist; dz /= dist;
			v[0] += cor*dx; v[1] += cor*dy; v[2] += cor*dz; 
		}
		
		public Spring addSpring(SpringNode snode)
		{
			springs = Arrays.copyOf(springs, springs.length+1);
			springs[springs.length-1] = new Spring(this, snode);
			return springs[springs.length-1];
		}
		
		public void adjust()
		{
			if (!isStatic)
			{
				if (Math3D.norm2(v) > vlim*vlim)
				{
					float l = Math3D.norm(v);
					Math3D.scale(v, vlim/l, v);
				}
			}
			else Math3D.set(v, 0, 0, 0);
		}
		public void update()
		{
			if (!isStatic)
			{
				Math3D.add(node.point, v, node.point);
//				if (node.point[1] < 0)
//				{
//					node.point[1] = 0;
//					if (v[1] < 0)
//						v[1] = 0;
//				}
			}
			Math3D.scale(v, damping, v);
		}
		
		public float dist2FromLine(float [] a, float [] v)
		{
			float [] proj = Math3D.getVector3f();
			float k = (Math3D.dotProduct(node.point, v)-Math3D.dotProduct(a, v))/Math3D.dotProduct(v, v);
			Math3D.diff(node.point, Math3D.add(a, Math3D.scale(v, k, proj), proj), proj);
			float d2 = Math3D.norm2(proj);
			Math3D.freeVector3f(proj);
			return d2;
		}
	}
	
	public final Grid grid;
	public final SpringNode [][] snodes;
	public final List<Spring> springs;
	
	public Paper(float [] p, float [] u, float [] v, int w, int h)
	{
		this.grid = new Grid(p, u, v, w, h);
		this.snodes = new SpringNode [w][h];
		
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
				snodes[i][j] = new SpringNode(grid.nodes[i][j]);
		
		this.springs = new LinkedList<Paper.Spring>();
		
//		int spread = 3;
//		for (int k=1;k<spread+1;k++)
//			for (int i=0;i<w;i++)
//				for (int j=0;j<h;j++)
//		{
//			SpringNode snode = snodes[i][j];
//			
//			for (int jt=j;jt<=j+k;jt++)
//				if (i+k < w && jt < h)
//					springs.add(snode.addSpring(snodes[i+k][jt]));
//			for (int it=i-k;it<i+k;it++)
//				if (it >= 0 && it < w && j+k < h)
//					springs.add(snode.addSpring(snodes[it][j+k]));
//			for (int jt=j+1;jt<j+k;jt++)
//				if (i-k >= 0 && jt < h)
//					springs.add(snode.addSpring(snodes[i-k][jt]));
//		}
		int spread = 4;
		for (int k=1;k<spread+1;k++)
			for (int i=0;i<w;i++)
				for (int j=0;j<h;j++)
		{
			SpringNode snode = snodes[i][j];
			
			if (i < w-k)
				springs.add(snode.addSpring(snodes[i+k][j]));
			if (j < h-k)
				springs.add(snode.addSpring(snodes[i][j+k]));
			if (i < w-k && j < h-k)
			{
				springs.add(snode.addSpring(snodes[i+k][j+k]));
				springs.add(snodes[i][j+k].addSpring(snodes[i+k][j]));
			}
		}
		System.out.println("springs : "+springs.size());
	}
	
	public void update()
	{
		for (Spring spring : springs)
			spring.update();
		for (SpringNode [] snodeList : snodes)
			for (SpringNode snode : snodeList)
				if (!snode.isStatic)
		{
			snode.v[2] += gravity;
			snode.adjust();
		}
		//autoCollide();
		for (SpringNode [] snodeList : snodes)
			for (SpringNode snode : snodeList)
				snode.update();
		grid.computeNormals();
	}
	
	public SpringNode closestToLine(float [] a, float [] v)
	{
		int w = snodes.length;
		int h = snodes[0].length;
		
		SpringNode min = null;
		float minDist = 0;
		float [] proj = Math3D.getVector3f();
		
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
		{
			SpringNode snode = snodes[i][j];
			
			float k = (Math3D.dotProduct(snode.node.point, v)-Math3D.dotProduct(a, v))/Math3D.dotProduct(v, v);
			Math3D.diff(snode.node.point, Math3D.add(a, Math3D.scale(v, k, proj), proj), proj);
			float d2 = Math3D.norm2(proj);
			
			if (min == null || d2 < minDist)
			{
				min = snode;
				minDist = d2;
			}
		}
		Math3D.freeVector3f(proj);
		
		return min;
	}
	
	void createBaseBeforeStep(SpringNode on, SpringNode in, SpringNode kn, 
		float [] o, float [] i, float [] j, float [] k)
	{
		Math3D.set(o, on.node.point);
		Math3D.diff(o, in.node.point, i);
		Math3D.diff(o, kn.node.point, k);
		Math3D.crossProduct(k, i, j);
	}
	
	void createBaseAfterStep(SpringNode on, SpringNode in, SpringNode kn, 
		float [] o, float [] i, float [] j, float [] k)
	{
		Math3D.add(on.node.point, on.v, o);
		Math3D.diff(o, Math3D.add(in.node.point, in.v, i), i);
		Math3D.diff(o, Math3D.add(kn.node.point, kn.v, k), k);
		Math3D.crossProduct(k, i, j);
	}
	
	public boolean nodeCrosses(float [] before, float [] after)
	{
		if (before[1]*after[1] > 0)
			return false;
		float t = -before[1]/(after[1]-before[1]);
		float ic = before[0]+t*(after[0]-before[0]);
		float jc = before[2]+t*(after[2]-before[2]);
		return ic > 0 && jc > 0 && ic+jc <= 1;
	}
}
