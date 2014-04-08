/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.gfx.Bindable;
import org.interreg.docexplore.util.Pair;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.collision.Ray;


public class BookModel
{
	public final ReaderApp app;
	public final BookCover cover;
	public final BookPageStack leftStack, rightStack;
	public int nPages;
	
	public final float pageWidth, pageHeight;
	
	public final VertexAttributes attributes;
	public final int vertexSize;
	
	public BookModel(ReaderApp app, float length, float height) throws Exception
	{
		this.app = app;
		this.attributes = new VertexAttributes(
			new VertexAttribute(VertexAttributes.Usage.Position, 3, "p"),
			new VertexAttribute(VertexAttributes.Usage.Normal, 3, "n"),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "texCoords"));
		this.vertexSize = attributes.vertexSize/4;
		
		this.cover = new BookCover(this, length, height, .0125f, .05f, .0025f, 11);
		this.pageWidth = .9f;
		this.pageHeight = .9f;
		this.nPages = 9;
		this.leftStack = new BookPageStack(this, pageWidth, pageHeight, 50, true);
		this.rightStack = new BookPageStack(this, pageWidth, pageHeight, 50, false);
	}
	
	public void setAngle(float la, float ra)
	{
		cover.setAngle(la, ra);
		leftStack.update();
		rightStack.update();
	}
	
	public void dispose()
	{
		cover.dispose();
		leftStack.dispose();
		rightStack.dispose();
	}
	
	float t = 0;
//	public void renderGeometryOnly(BookPage page)
//	{
//		cover.render();
//		
//		leftStack.renderGeometryOnly();
//		rightStack.renderGeometryOnly();
//		if (page != null)
//			page.renderGeometryOnly();
//	}
	public void render(Bindable leftPage, Bindable rightPage, Bindable pageFront, Bindable pageBack, Bindable coverTex, Bindable innerCoverTex,
		Bindable leftRegionMask, Bindable rightRegionMask)
	{
		Gdx.gl10.glColor4f(.5f, .4f, .2f, 1);
		cover.render(coverTex, innerCoverTex);
		
		Gdx.gl10.glColor4f(.9f, .8f, .7f, 1);
		leftStack.render(leftPage, leftRegionMask);
		Gdx.gl10.glColor4f(.9f, .8f, .7f, 1);
		rightStack.render(rightPage, rightRegionMask);
	}
	
	void proj(float px1, float py1, float vx1, float vy1, float px2, float py2, float vx2, float vy2, float [] res)
	{
		res[0] = (px2*vy2-py2*vx2-px1*vy2+py1*vx2)/(vx1*vy2-vy1*vx2);
		res[1] = vx2*vx2 > vy2*vy2 ? (px1+res[0]*vx1-px2)/vx2 : (py1+res[0]*vy1-py2)/vy2;
	}
	
	float [] p = {0, 0, 0};
	float [] d = {0, 0, 0};
	float [] projk = {0, 0};
	public Pair<Boolean, float []> toPage(int x, int y)
	{
		Ray ray = app.bookEngine.camera.getPickRay(x, y);
		p[0] = ray.origin.x; p[1] =  ray.origin.y; p[2] = ray.origin.z;
		d[0] = ray.direction.x; d[1] = ray.direction.y; d[2] = ray.direction.z;
		
		boolean left = p[0]-d[2]*d[0] < 0;
		BookPageStack stack = left ? leftStack : rightStack;
		if (stack.nStackPages == 0)
			return new Pair<Boolean, float []>(left, null);
		
		float mink = -1, cx = 0;
		for (int i=0;i<stack.projection.length-1;i++)
		{
			float vx = stack.projection[i+1][0]-stack.projection[i][0],
				vy = stack.projection[i+1][1]-stack.projection[i][1];
			proj(p[0], p[2], d[0], d[2], stack.projection[i][0], stack.projection[i][1], vx, vy, projk);
			if (projk[1] < 0 || projk[1] > 1)
				continue;
			if (mink < 0 || projk[0] < mink)
				{mink = projk[0]; cx = (i+projk[1])/stack.projection.length;}
		}
		if (mink < 0)
			return null;
		
		//TODO: ugly hack
		cx *= 1.02;
		if (left)
			cx = 1-cx;
		float cy = (p[1]+mink*d[1]-stack.margin)/(cover.coverHeight-2*stack.margin);
		return new Pair<Boolean, float []>(left, new float [] {cx, cy});
	}
}
