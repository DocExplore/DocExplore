package org.interreg.docexplore.stitch;

import java.util.List;

import org.interreg.docexplore.stitch.TriangulatedBox.Node;
import org.interreg.docexplore.util.Math2D;

public class TransBox
{
	static class TransNode
	{
		double w;
		double [] v;
		
		TransNode(double w, double vx, double vy) {this.w = w; this.v = new double [] {vx, vy};}
	}
	
	ImageBox box;
	TriangulatedBox<TransNode> triBox;
	
	public TransBox(ImageBox box, List<double [][]> stitches)
	{
		this.box = box;
		double [][] points = new double [stitches.size()+5][2];
		TransNode [] nodes = new TransNode [stitches.size()+5];
		for (int i=0;i<stitches.size();i++)
		{
			points[i][0] = stitches.get(i)[0][0];
			points[i][1] = stitches.get(i)[0][1];
			nodes[i] = new TransNode(1, stitches.get(i)[1][0]-stitches.get(i)[0][0], stitches.get(i)[1][1]-stitches.get(i)[0][1]);
		}
		Math2D.set(points[stitches.size()], 0, 0);
		Math2D.set(points[stitches.size()+1], box.w, 0);
		Math2D.set(points[stitches.size()+2], box.w, box.h);
		Math2D.set(points[stitches.size()+3], 0, box.h);
		Math2D.set(points[stitches.size()+4], .5*box.w+.01, .5*box.h+.01);
		nodes[stitches.size()] = new TransNode(0, 0, 0);
		nodes[stitches.size()+1] = new TransNode(0, 0, 0);
		nodes[stitches.size()+2] = new TransNode(0, 0, 0);
		nodes[stitches.size()+3] = new TransNode(0, 0, 0);
		nodes[stitches.size()+4] = new TransNode(1, 0, 0);
		
		triBox = new TriangulatedBox<TransBox.TransNode>(points, nodes);
	}
	
	double [] buf1 = {0, 0}, buf2 = {0, 0}, buf3 = {0, 0}, buf4 = {0, 0};
	TriangulatedBox.Node<TransNode> [] getTriangle(double x, double y, double [] res)
	{
		for (Node<TransNode> [] tri : triBox.triangles)
		{
			Math2D.set(buf1, x, y);
			triBox.coords(buf1, tri[0].p, tri[1].p, tri[2].p, buf2);
			if (buf2[0] > 0 && buf2[1] > 0 && buf2[0]+buf2[1] <= 1)
				{Math2D.set(res, buf2); return tri;}
		}
		return null;
	}
	public double [] getTransPoint(double x, double y, double [] res)
	{
		TriangulatedBox.Node<TransNode> [] tri = getTriangle(x, y, buf3);
		if (tri == null)
			return Math2D.set(res, x, y);
		
		Math2D.set(buf4, 0, 0);
		double wtot = 0;
		
		double w = tri[0].t.w*(1-buf3[0]-buf3[1]);
		buf4[0] += w*tri[0].t.v[0]; buf4[1] += w*tri[0].t.v[1];
		wtot += w;
		
		w = tri[1].t.w*buf3[0];
		buf4[0] += w*tri[1].t.v[0]; buf4[1] += w*tri[1].t.v[1];
		wtot += w;
		
		w = tri[2].t.w*buf3[1];
		buf4[0] += w*tri[2].t.v[0]; buf4[1] += w*tri[2].t.v[1];
		wtot += w;
		
		if (wtot < .001)
			return Math2D.set(res, x, y);
		return Math2D.set(res, x+buf4[0]/wtot, y+buf4[1]/wtot);
	}
}
