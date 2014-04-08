/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book.page;

import java.nio.ShortBuffer;

import org.interreg.docexplore.reader.book.BookModel;
import org.interreg.docexplore.reader.book.BookPageStack;
import org.interreg.docexplore.reader.book.page.Paper.SpringNode;
import org.interreg.docexplore.reader.gfx.Bindable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;

public class BookPage
{
	BookModel book;
	Paper paper;
	Mesh frontPage, backPage;
	
	public BookPage(BookModel book) throws Exception
	{
		this.book = book;
		
		double a = -Math.PI/10;
		this.paper = new Paper(new float [] {book.leftStack.projection[0][0], book.cover.coverHeight-book.leftStack.margin, book.leftStack.projection[0][1]}, //p
			new float [] {(float)(Math.sin(a)*book.pageWidth*book.cover.coverLength), 0, (float)(Math.cos(a)*book.pageWidth*book.cover.coverLength)}, //u
			new float [] {0, -(book.cover.coverHeight-2*book.leftStack.margin), 0}, //v
			10, 10);
			//3, 3);
		for (int i=0;i<paper.snodes[0].length;i++)
			paper.snodes[0][i].isStatic = true;
		
		this.frontPage = new Mesh(false, paper.snodes.length*paper.snodes[0].length, 6*(paper.snodes.length-1)*(paper.snodes[0].length-1), 
			book.attributes);
		this.backPage = new Mesh(false, paper.snodes.length*paper.snodes[0].length, 6*(paper.snodes.length-1)*(paper.snodes[0].length-1), 
			book.attributes);
		
		ShortBuffer frontIndexBuffer = frontPage.getIndicesBuffer();
		frontIndexBuffer.limit(frontIndexBuffer.capacity());
		ShortBuffer backIndexBuffer = backPage.getIndicesBuffer();
		backIndexBuffer.limit(backIndexBuffer.capacity());
		
		int cnt = 0;
		for (int i=0;i<paper.snodes.length-1;i++)
			for (int j=0;j<paper.snodes[0].length-1;j++)
		{
			frontIndexBuffer.put(cnt, (short)(j*paper.snodes.length+i));
			frontIndexBuffer.put(cnt+1, (short)((j+1)*paper.snodes.length+i));
			frontIndexBuffer.put(cnt+2, (short)((j+1)*paper.snodes.length+(i+1)));
			frontIndexBuffer.put(cnt+3, (short)(j*paper.snodes.length+i));
			frontIndexBuffer.put(cnt+4, (short)((j+1)*paper.snodes.length+(i+1)));
			frontIndexBuffer.put(cnt+5, (short)(j*paper.snodes.length+(i+1)));
			
			backIndexBuffer.put(cnt, (short)(j*paper.snodes.length+i));
			backIndexBuffer.put(cnt+2, (short)((j+1)*paper.snodes.length+i));
			backIndexBuffer.put(cnt+1, (short)((j+1)*paper.snodes.length+(i+1)));
			backIndexBuffer.put(cnt+3, (short)(j*paper.snodes.length+i));
			backIndexBuffer.put(cnt+5, (short)((j+1)*paper.snodes.length+(i+1)));
			backIndexBuffer.put(cnt+4, (short)(j*paper.snodes.length+(i+1)));
			
			cnt += 6;
		}
		
		frontPage.getVerticesBuffer().limit(frontPage.getVerticesBuffer().capacity());
		backPage.getVerticesBuffer().limit(backPage.getVerticesBuffer().capacity());
	}
	
	public void setTo(BookPageStack stack)
	{
		for (int i=1;i<paper.snodes.length;i++)
		{
			float l = i*book.pageWidth*book.cover.coverLength/(paper.snodes.length-1);
			stack.projectionAt(l, buf1);
			for (int j=0;j<paper.snodes[0].length;j++)
				paper.snodes[i][j].node.set(buf1[0], paper.snodes[0][j].node.point[1], buf1[1]);
		}
		paper.grid.write(frontPage.getVerticesBuffer(), backPage.getVerticesBuffer(), book.attributes);
	}
	
	float [] buf1 = {0, 0}, buf2 = {0, 0};
	public void update()
	{
		book.leftStack.projectionAt(0, buf1);
		book.rightStack.projectionAt(0, buf2);
			
		for (int j=0;j<paper.snodes[0].length;j++)
		{
			paper.snodes[0][j].node.point[0] = (buf1[0]+buf2[0])/2;
			paper.snodes[0][j].node.point[2] = (buf1[1]+buf2[1])/2;
		}
		
		paper.update();
		
		for (int i=0;i<paper.snodes.length;i++)
			for (int j=0;j<paper.snodes[0].length;j++)
		{
			SpringNode snode = paper.snodes[i][j];
			if (snode.node.point[0] < book.leftStack.projection[0][0])
				collide(snode, book.leftStack);
			if (snode.node.point[0] > book.rightStack.projection[0][0])
				collide(snode, book.rightStack);
		}
		
		paper.grid.write(frontPage.getVerticesBuffer(), backPage.getVerticesBuffer(), book.attributes);
	}
	
	float [] projBuf = {0, 0, 0}, normalBuf = {0, 0, 0};
	double cor = .00001, lim = .0000001;
	void collide(SpringNode snode, BookPageStack stack)
	{
		if (snode.isStatic)
			return;
		boolean noStack = stack.nStackPages == 0;
		float dist = projectOnPath(snode.node.point[0], snode.node.point[2], 
			noStack ? stack.path : stack.projection, stack.normals, projBuf, normalBuf);
		if (dist < lim)
			return;
		if ((snode.node.point[0]-projBuf[0])*normalBuf[0]+(snode.node.point[2]-projBuf[1])*normalBuf[1] > 0)
			return;
		
		dist = (float)Math.sqrt(dist);
		float cx = (projBuf[0]-snode.node.point[0])/dist, cy = (projBuf[1]-snode.node.point[2])/dist;
		snode.node.point[0] += cx*(dist+cor);
		snode.node.point[2] += cy*(dist+cor);
	}
	
	float projectOnPath(float x, float y, float [][] path, float [][] normals, float [] proj, float [] normal)
	{
		float minDist = (x-path[0][0])*(x-path[0][0])+(y-path[0][1])*(y-path[0][1]);
		
		for (int i=0;i<path.length-1;i++)
		{
			float k = dist(x, y, path[i][0], path[i][1], path[i+1][0]-path[i][0], path[i+1][1]-path[i][1]);
			float dist = Float.MAX_VALUE;
			if (k > 0 && k < 1)
			{
				float ix = path[i][0]+k*(path[i+1][0]-path[i][0]), iy = path[i][1]+k*(path[i+1][1]-path[i][1]);
				dist = (x-ix)*(x-ix)+(y-iy)*(y-iy);
			}
			if (dist < minDist)
			{
				minDist = dist;
				
				proj[0] = path[i][0]+k*(path[i+1][0]-path[i][0]);
				proj[1] = path[i][1]+k*(path[i+1][1]-path[i][1]);
				normal[0] = normals[i][0]+normals[i+1][0];
				normal[1] = normals[i][1]+normals[i+1][1];
				float nl = normal[0]*normal[0]+normal[1]*normal[1];
				normal[0] /= nl; normal[1] /= nl;
			}
			
			dist = (x-path[i+1][0])*(x-path[i+1][0])+(y-path[i+1][1])*(y-path[i+1][1]);
			if (dist < minDist)
			{
				minDist = dist;
				
				proj[0] = path[i+1][0];
				proj[1] = path[i+1][1];
				normal[0] = normals[i+1][0];
				normal[1] = normals[i+1][1];
			}
		}
		return minDist;
	}
	
	float dist(float x, float y, float px, float py, float vx, float vy) {return ((x*vx+y*vy)-(px*vx+py*vy))/(vx*vx+vy*vy);}
	
	public void dispose()
	{
		frontPage.dispose();
		backPage.dispose();
	}
	
	public void renderGeometryOnly()
	{
		frontPage.render(GL10.GL_TRIANGLES);
		backPage.render(GL10.GL_TRIANGLES);
	}
	public void render(Bindable front, Bindable back)
	{
		Gdx.gl10.glColor4f(1, 1, 1, 1);
		Gdx.gl10.glEnable(GL10.GL_POLYGON_OFFSET_FILL);
		Gdx.gl10.glPolygonOffset(0, -20000);
		Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
		
		front.bind();
		frontPage.render(GL10.GL_TRIANGLES);
		
		back.bind();
		backPage.render(GL10.GL_TRIANGLES);
		
		Gdx.gl10.glDisable(GL10.GL_TEXTURE_2D);
		Gdx.gl10.glDisable(GL10.GL_POLYGON_OFFSET_FILL);
	}
}
