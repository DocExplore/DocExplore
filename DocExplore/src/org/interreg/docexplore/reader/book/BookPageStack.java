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
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.interreg.docexplore.reader.book.page.PaperCurve;
import org.interreg.docexplore.reader.gfx.Bindable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;

/**
 * Model for a stack of paper with one side constrained to the book binding
 * @author Alexander Burnett
 *
 */
public class BookPageStack
{
	public final BookModel book;
	public final int pathLength;
	public final boolean left; //is this stack on the left side of the book (when viewed from above)
	float vx0;
	Mesh stackFront, stackSide, spineFill;
	public final float pageWidth, pageHeight;
	public final float margin;
	public int nStackPages;
	
	
	public final float [][] path, //points of the lower sheet
		projection, normals; //points of the upper sheet and normals
	
	public BookPageStack(BookModel book, float pageWidth, float pageHeight, int pathLength, boolean left)
	{
		this.book = book;
		this.pathLength = pathLength;
		this.left = left;
		this.vx0 = left ? -1 : 1;
		this.path = new float [pathLength][2];
		this.projection = new float [pathLength][2];
		this.normals = new float [pathLength][2];
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		this.margin = .5f*book.cover.coverHeight*(1-pageHeight);
		
		float [][] path = book.cover.path;
		//interleaved top row / bottom row
		this.stackFront = new Mesh(false, 2*pathLength, 6*(pathLength-1), book.attributes);
		//top side, bottom side, outer side
		this.stackSide = new Mesh(false, (2+2)*pathLength+4, 2*6*(pathLength-1)+6, book.attributes);
		this.spineFill = new Mesh(false, 2*(path.length-2), 2*3*(path.length-4), book.attributes);
		this.nStackPages = book.nPages/2+(!left && book.nPages%2==1 ? 1 : 0);
		
		//fill mesh indices now, since they will not change over time
		ShortBuffer frontIndices = stackFront.getIndicesBuffer();
		frontIndices.limit(frontIndices.capacity());
		ShortBuffer sideIndices = stackSide.getIndicesBuffer();
		sideIndices.limit(sideIndices.capacity());
		for (int i=0;i<pathLength-1;i++)
		{
			frontIndices.put(6*i, (short)(2*i));
			frontIndices.put(6*i+1, left ? (short)(2*i+1) : (short)(2*i+3));
			frontIndices.put(6*i+2, left ? (short)(2*i+3) : (short)(2*i+1));
			frontIndices.put(6*i+3, (short)(2*i));
			frontIndices.put(6*i+4, left ? (short)(2*i+3) : (short)(2*i+2));
			frontIndices.put(6*i+5, left ? (short)(2*i+2) : (short)(2*i+3));
			
			sideIndices.put(6*i, (short)(2*i));
			sideIndices.put(6*i+1, left ? (short)(2*i+1) : (short)(2*i+3));
			sideIndices.put(6*i+2, left ? (short)(2*i+3) : (short)(2*i+1));
			sideIndices.put(6*i+3, (short)(2*i));
			sideIndices.put(6*i+4, left ? (short)(2*i+3) : (short)(2*i+2));
			sideIndices.put(6*i+5, left ? (short)(2*i+2) : (short)(2*i+3));
			
			int ii0 = pathLength-1+i;
			int vi0 = pathLength+i;
			sideIndices.put(6*ii0, (short)(2*vi0));
			sideIndices.put(6*ii0+1, left ? (short)(2*vi0+3) : (short)(2*vi0+1));
			sideIndices.put(6*ii0+2, left ? (short)(2*vi0+1) : (short)(2*vi0+3));
			sideIndices.put(6*ii0+3, (short)(2*vi0));
			sideIndices.put(6*ii0+4, left ? (short)(2*vi0+2) : (short)(2*vi0+3));
			sideIndices.put(6*ii0+5, left ? (short)(2*vi0+3) : (short)(2*vi0+2));
		}
		
		int ii0 = 2*(pathLength-1);
		int vi0 = 2*pathLength;
		sideIndices.put(6*ii0, (short)(2*vi0));
		sideIndices.put(6*ii0+1, left ? (short)(2*vi0+1) : (short)(2*vi0+3));
		sideIndices.put(6*ii0+2, left ? (short)(2*vi0+3) : (short)(2*vi0+1));
		sideIndices.put(6*ii0+3, (short)(2*vi0));
		sideIndices.put(6*ii0+4, left ? (short)(2*vi0+3) : (short)(2*vi0+2));
		sideIndices.put(6*ii0+5, left ? (short)(2*vi0+2) : (short)(2*vi0+3));
		
		ShortBuffer spineIndices = spineFill.getIndicesBuffer();
		spineIndices.limit(spineIndices.capacity());
		for (int i=0;i<path.length-4;i++)
		{
			spineIndices.put(3*i, (short)(0));
			spineIndices.put(3*i+1, (short)(i+1));
			spineIndices.put(3*i+2, (short)(i+2));
			spineIndices.put(3*(path.length-4+i), (short)(path.length-2));
			spineIndices.put(3*(path.length-4+i)+1, (short)(path.length+i));
			spineIndices.put(3*(path.length-4+i)+2, (short)(path.length+i-1));
		}
		
		FloatBuffer spineData = spineFill.getVerticesBuffer();
		spineData.limit(spineData.capacity());
		for (int i=0;i<path.length-2;i++)
		{
			setVertex(spineData, i, path[i+1][0], margin, path[i+1][1], 0, -1, 0);
			setVertex(spineData, path.length-2+i, path[i+1][0], book.cover.coverHeight-margin, path[i+1][1], 0, 1, 0);
		}
	}
	
	void setVertex(FloatBuffer buffer, int i, float x, float y, float z,
			float nx, float ny, float nz)
	{
		buffer.put(i*book.vertexSize, x);
		buffer.put(i*book.vertexSize+1, y);
		buffer.put(i*book.vertexSize+2, z);
		buffer.put(i*book.vertexSize+3, nx);
		buffer.put(i*book.vertexSize+4, ny); 
		buffer.put(i*book.vertexSize+5, nz);
	}
	void setVertex(FloatBuffer buffer, int i, float x, float y, float z,
			float nx, float ny, float nz, float s, float t)
	{
		buffer.put(i*book.vertexSize, x);
		buffer.put(i*book.vertexSize+1, y);
		buffer.put(i*book.vertexSize+2, z);
		buffer.put(i*book.vertexSize+3, nx);
		buffer.put(i*book.vertexSize+4, ny); 
		buffer.put(i*book.vertexSize+5, nz);
		buffer.put(i*book.vertexSize+6, s);
		buffer.put(i*book.vertexSize+7, t);
	}
	
	public void update()
	{
		float hingex = book.cover.meshes[book.cover.innerBinding].getVerticesBuffer().get(
			book.cover.indices[book.cover.innerBinding][left ? book.cover.bottomLeftInnerHinge : book.cover.bottomRightInnerHinge]),
			hingey = book.cover.meshes[book.cover.innerBinding].getVerticesBuffer().get(
			book.cover.indices[book.cover.innerBinding][left ? book.cover.bottomLeftInnerHinge : book.cover.bottomRightInnerHinge]+2);
		float edgex = book.cover.meshes[book.cover.innerBinding].getVerticesBuffer().get(
			book.cover.indices[book.cover.innerBinding][left ? book.cover.bottomLeftInnerEdge : book.cover.bottomRightInnerEdge]),
			edgey = book.cover.meshes[book.cover.innerBinding].getVerticesBuffer().get(
			book.cover.indices[book.cover.innerBinding][left ? book.cover.bottomLeftInnerEdge : book.cover.bottomRightInnerEdge]+2);
		
		//recompute path and projection
		float sep = nStackPages*1.f/book.nPages;
		PaperCurve.compute(hingex, hingey, vx0, 1, edgex-hingex, edgey-hingey, 
			pageWidth*book.cover.coverLength, pathLength-1, (1f+1f*sep)*200, path);
		if (sep > 0)
			PaperCurve.project(path, book.cover.coverDepth*(left ? sep : -sep), 0, 
				pageWidth*book.cover.coverLength, projection);
		else for (int i=0;i<path.length;i++)
			{projection[i][0] = path[i][0]; projection[i][1] = path[i][1];}
		
		//refill the meshes
		FloatBuffer frontBuf = stackFront.getVerticesBuffer();
		frontBuf.limit(frontBuf.capacity());
		FloatBuffer sideBuf = stackSide.getVerticesBuffer();
		sideBuf.limit(sideBuf.capacity());
		for (int i=0;i<pathLength;i++)
		{
			float nx, ny;
			if (i == 0)
			{
				nx = -(projection[1][1]-projection[0][1]);
				ny = projection[1][0]-projection[0][0];
			}
			else if (i == pathLength-1)
			{
				nx = -(projection[pathLength-1][1]-projection[pathLength-2][1]);
				ny = projection[pathLength-1][0]-projection[pathLength-2][0];
			}
			else
			{
				nx = -(projection[i+1][1]-projection[i-1][1]);
				ny = projection[i+1][0]-projection[i-1][0];
			}
			float nl = (float)Math.sqrt(nx*nx+ny*ny);
			nx /= nl; ny /= nl;
			if (left)
				{nx = -nx; ny = -ny;}
			
			normals[i][0] = nx; normals[i][1] = ny;
			setVertex(frontBuf, 2*i, projection[i][0], margin, projection[i][1], nx, 0, ny, left ? 1-i*1.f/(pathLength-1) : i*1.f/(pathLength-1), 1);
			setVertex(frontBuf, 2*i+1, projection[i][0], book.cover.coverHeight-margin, projection[i][1], nx, 0, ny, left ? 1-i*1.f/(pathLength-1) : i*1.f/(pathLength-1), 0);
			
			setVertex(sideBuf, 2*i, path[i][0], margin, path[i][1], 0, -1, 0);
			setVertex(sideBuf, 2*i+1, projection[i][0], margin, projection[i][1], 0, -1, 0);
			setVertex(sideBuf, 2*(pathLength+i), path[i][0], book.cover.coverHeight-margin, path[i][1], 0, 1, 0);
			setVertex(sideBuf, 2*(pathLength+i)+1, projection[i][0], book.cover.coverHeight-margin, projection[i][1], 0, 1, 0);
		}
		
		float dx = projection[pathLength-1][0]-path[pathLength-1][0],
			dy = projection[pathLength-1][1]-path[pathLength-1][1];
		float nx = -dy, ny = dx;
		float nl = (float)Math.sqrt(nx*nx+ny*ny);
		nx /= nl; ny /= nl;
		if (!left)
			{nx = -nx; ny = -ny;}
		
		setVertex(sideBuf, 4*pathLength, path[pathLength-1][0], margin, path[pathLength-1][1], nx, 0, ny);
		setVertex(sideBuf, 4*pathLength+1, projection[pathLength-1][0], margin, projection[pathLength-1][1], nx, 0, ny);
		setVertex(sideBuf, 4*pathLength+2, path[pathLength-1][0], book.cover.coverHeight-margin, path[pathLength-1][1], nx, 0, ny);
		setVertex(sideBuf, 4*pathLength+3, projection[pathLength-1][0], book.cover.coverHeight-margin, projection[pathLength-1][1], nx, 0, ny);
	}
	
	public float [] projectionAt(float l, float [] res)
	{
		float unitLength = (book.cover.coverLength/(pathLength-1));
		int proji = (int)(l/unitLength);
		if (proji > pathLength-2)
			proji = pathLength-2;
		float k = (l-proji*unitLength)/unitLength;
		res[0] = projection[proji][0]+k*(projection[proji+1][0]-projection[proji][0]);
		res[1] = projection[proji][1]+k*(projection[proji+1][1]-projection[proji][1]);
		return res;
	}
	
	public float [] fromPage(float x, float y, float [] res)
	{
		projectionAt(left ? 1-x : x, res);
		res[2] = res[1];
		res[1] = margin+y*(book.cover.coverHeight-2*margin);
		return res;
	}
	
	public void dispose()
	{
		stackFront.dispose();
		stackSide.dispose();
		spineFill.dispose();
	}
	
	int cnt = 0;
	public void renderGeometryOnly()
	{
		if (nStackPages == 0)
			return;
		stackSide.render(GL10.GL_TRIANGLES);
		spineFill.render(GL10.GL_TRIANGLES);
		stackFront.render(GL10.GL_TRIANGLES);
	}
	public void render(Bindable pageTexture, Bindable regionMask)
	{
		if (nStackPages == 0)
			return;
		
		stackSide.render(GL10.GL_TRIANGLES);
		spineFill.render(GL10.GL_TRIANGLES);
		
		Gdx.gl10.glColor4f(1, 1, 1, 1);
		Gdx.gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
		pageTexture.bind();
		stackFront.render(GL10.GL_TRIANGLES);
		
		if (regionMask != null)
		{
			Gdx.gl10.glDisable(GL10.GL_LIGHTING);
			Gdx.gl10.glDepthFunc(GL10.GL_LEQUAL);
			float shade = book.app.bookEngine.roiOverlay.active || book.app.bookEngine.roiOverlay.leaving ? .5f*book.app.bookEngine.roiOverlay.alpha : 
				.5f*(cnt > 100 ? 200-cnt : cnt)/100f;
			
			Gdx.gl10.glColor4f(1, 1, 1, shade);
			regionMask.bind();
			stackFront.render(GL10.GL_TRIANGLES);
			
			Gdx.gl10.glEnable(GL10.GL_LIGHTING);
		}
		Gdx.gl10.glDisable(GL10.GL_TEXTURE_2D);
		Gdx.gl10.glDepthFunc(GL10.GL_LESS);
		cnt = (cnt+1)%200;
	}
}
