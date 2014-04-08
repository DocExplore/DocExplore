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
import org.interreg.docexplore.reader.gfx.Bindable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;

/**
 * A model for a book cover with solid binding.
 * The dimensions are as follows when viewed from profile :
 *        ________________________
 *       / _______________________   | bindingWidth
 *      / /                        ^
 *     / /                         |
 *    / / spineWidth               |
 *   | |<->                        | coverDepth
 *    \ \         coverLength      |
 *     \ \ <---------------------> |
 *      \ \_______________________ v
 *       \________________________ 
 * 
 * coverHeight is the length of the projection of this shape to create the cover.
 * 
 * @author Alexander Burnett
 *
 */
public class BookCover
{
	BookModel book;
	
	public float coverLength, coverHeight, coverDepth, bindingWidth, spineWidth;
	int outerBinding, innerBinding;//, topBinding, bottomBinding, frontBinding; //mesh ids
	float [][] path; //the points that make the inner binding
	
	Mesh [] meshes;
	int topLeftInnerEdge, topLeftOuterEdge, bottomLeftInnerEdge, bottomLeftOuterEdge,
		topRightInnerEdge, topRightOuterEdge, bottomRightInnerEdge, bottomRightOuterEdge,
		topLeftInnerHinge, topLeftOuterHinge, bottomLeftInnerHinge, bottomLeftOuterHinge,
		topRightInnerHinge, topRightOuterHinge, bottomRightInnerHinge, bottomRightOuterHinge; //vertex ids
	
	int [][] indices; //indexes of vertices in meshes : indices[mesh id][vertex id]
		//necessary since a same vertex can have different ids in different meshes
	
	public BookCover(BookModel book, 
		float coverLength, float coverHeight, float coverDepth, float bindingWidth, float spineWidth, int nSpinePoints)
	{
		this.book = book;
		this.coverLength = coverLength;
		this.coverHeight = coverHeight;
		this.coverDepth = coverDepth;
		this.bindingWidth = bindingWidth;
		this.spineWidth = spineWidth;
		
		int pathLength = 4+nSpinePoints;
		path = createBindingPath(nSpinePoints);
		
		//create indices for meshes and indices arrays
		int mcnt = 0;
		this.outerBinding = mcnt++;
		this.innerBinding = mcnt++;
//		this.topBinding = mcnt++;
//		this.bottomBinding = mcnt++;
//		this.frontBinding = mcnt++;
		
		this.meshes = new Mesh [mcnt];
		meshes[outerBinding] = new Mesh(false, 2*pathLength, 6*(pathLength-1), book.attributes);
		meshes[innerBinding] = new Mesh(false, 2*pathLength, 6*(pathLength-1), book.attributes);
//		meshes[topBinding] = new Mesh(false, 2*pathLength, 6*(pathLength-1), book.attributes);
//		meshes[bottomBinding] = new Mesh(false, 2*pathLength, 6*(pathLength-1), book.attributes);
//		meshes[frontBinding] = new Mesh(false, 8, 12, book.attributes);
		
		int vcnt = 0;
		this.topLeftInnerEdge = vcnt++;
		this.topLeftOuterEdge = vcnt++;
		this.bottomLeftInnerEdge = vcnt++;
		this.bottomLeftOuterEdge = vcnt++;
		this.topRightInnerEdge = vcnt++;
		this.topRightOuterEdge = vcnt++;
		this.bottomRightInnerEdge = vcnt++;
		this.bottomRightOuterEdge = vcnt++;
		this.topLeftInnerHinge = vcnt++;
		this.topLeftOuterHinge = vcnt++;
		this.bottomLeftInnerHinge = vcnt++;
		this.bottomLeftOuterHinge = vcnt++;
		this.topRightInnerHinge = vcnt++;
		this.topRightOuterHinge = vcnt++;
		this.bottomRightInnerHinge = vcnt++;
		this.bottomRightOuterHinge = vcnt++;
		
		this.indices = new int [mcnt][vcnt];
		for (int i=0;i<indices.length;i++)
			for (int j=0;j<indices[i].length;j++)
				indices[i][j] = -1;
		indices[outerBinding][topLeftOuterEdge] = 0;
		indices[outerBinding][bottomLeftOuterEdge] = book.vertexSize;
		indices[outerBinding][topLeftOuterHinge] = 2*book.vertexSize;
		indices[outerBinding][bottomLeftOuterHinge] = 3*book.vertexSize;
		indices[outerBinding][topRightOuterEdge] = (2*pathLength-2)*book.vertexSize;
		indices[outerBinding][bottomRightOuterEdge] = (2*pathLength-1)*book.vertexSize;
		indices[outerBinding][topRightOuterHinge] = (2*pathLength-4)*book.vertexSize;
		indices[outerBinding][bottomRightOuterHinge] = (2*pathLength-3)*book.vertexSize;
		indices[innerBinding][topLeftInnerEdge] = indices[outerBinding][topLeftOuterEdge];
		indices[innerBinding][bottomLeftInnerEdge] = indices[outerBinding][bottomLeftOuterEdge];
		indices[innerBinding][topLeftInnerHinge] = indices[outerBinding][topLeftOuterHinge];
		indices[innerBinding][bottomLeftInnerHinge] = indices[outerBinding][bottomLeftOuterHinge];
		indices[innerBinding][topRightInnerEdge] = indices[outerBinding][topRightOuterEdge];
		indices[innerBinding][bottomRightInnerEdge] = indices[outerBinding][bottomRightOuterEdge];
		indices[innerBinding][topRightInnerHinge] = indices[outerBinding][topRightOuterHinge];
		indices[innerBinding][bottomRightInnerHinge] = indices[outerBinding][bottomRightOuterHinge];
//		indices[topBinding][topLeftOuterEdge] = 0;
//		indices[topBinding][topLeftInnerEdge] = book.vertexSize;
//		indices[topBinding][topLeftOuterHinge] = 2*book.vertexSize;
//		indices[topBinding][topLeftInnerHinge] = 3*book.vertexSize;
//		indices[topBinding][topRightOuterEdge] = (2*pathLength-2)*book.vertexSize;
//		indices[topBinding][topRightInnerEdge] = (2*pathLength-1)*book.vertexSize;
//		indices[topBinding][topRightOuterHinge] = (2*pathLength-4)*book.vertexSize;
//		indices[topBinding][topRightInnerHinge] = (2*pathLength-3)*book.vertexSize;
//		indices[bottomBinding][bottomLeftOuterEdge] = indices[topBinding][topLeftOuterEdge];
//		indices[bottomBinding][bottomLeftInnerEdge] = indices[topBinding][topLeftInnerEdge];
//		indices[bottomBinding][bottomLeftOuterHinge] = indices[topBinding][topLeftOuterHinge];
//		indices[bottomBinding][bottomLeftInnerHinge] = indices[topBinding][topLeftInnerHinge];
//		indices[bottomBinding][bottomRightOuterEdge] = indices[topBinding][topRightOuterEdge];
//		indices[bottomBinding][bottomRightInnerEdge] = indices[topBinding][topRightInnerEdge];
//		indices[bottomBinding][bottomRightOuterHinge] = indices[topBinding][topRightOuterHinge];
//		indices[bottomBinding][bottomRightInnerHinge] = indices[topBinding][topRightInnerHinge];
//		indices[frontBinding][bottomLeftInnerEdge] = 0;
//		indices[frontBinding][topLeftInnerEdge] = book.vertexSize;
//		indices[frontBinding][topLeftOuterEdge] = 2*book.vertexSize;
//		indices[frontBinding][bottomLeftOuterEdge] = 3*book.vertexSize;
//		indices[frontBinding][bottomRightInnerEdge] = 4*book.vertexSize;
//		indices[frontBinding][bottomRightOuterEdge] = 5*book.vertexSize;
//		indices[frontBinding][topRightOuterEdge] = 6*book.vertexSize;
//		indices[frontBinding][topRightInnerEdge] = 7*book.vertexSize;
		
		short [] outerIndexBuffer = new short [6*(pathLength-1)];
		short [] innerIndexBuffer = new short [6*(pathLength-1)];
		for (int i=0;i<pathLength-1;i++)
		{
			outerIndexBuffer[6*i] = (short)(2*i);
			outerIndexBuffer[6*i+1] = (short)(2*i+2);
			outerIndexBuffer[6*i+2] = (short)(2*i+1);
			
			outerIndexBuffer[6*i+3] = (short)(2*i+1);
			outerIndexBuffer[6*i+4] = (short)(2*i+2);
			outerIndexBuffer[6*i+5] = (short)(2*i+3);
			
			innerIndexBuffer[6*i] = (short)(2*i);
			innerIndexBuffer[6*i+1] = (short)(2*i+1);
			innerIndexBuffer[6*i+2] = (short)(2*i+2);
			
			innerIndexBuffer[6*i+3] = (short)(2*i+1);
			innerIndexBuffer[6*i+4] = (short)(2*i+3);
			innerIndexBuffer[6*i+5] = (short)(2*i+2);
		}
		
		meshes[outerBinding].setIndices(outerIndexBuffer);
		meshes[innerBinding].setIndices(innerIndexBuffer);
//		meshes[topBinding].setIndices(innerIndexBuffer);
//		meshes[bottomBinding].setIndices(outerIndexBuffer);
//		meshes[frontBinding].setIndices(new short [] {0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7});
		
		set(coverLength, coverHeight, coverDepth, bindingWidth, spineWidth);
	}
	
	public void set(float coverLength, float coverHeight)
	{
		set(coverLength, coverHeight, coverDepth, bindingWidth, spineWidth);
	}
	public void set(float coverLength, float coverHeight, float coverDepth, float bindingWidth, float spineWidth)
	{
		this.coverLength = coverLength;
		this.coverHeight = coverHeight;
		this.coverDepth = coverDepth;
		this.bindingWidth = bindingWidth;
		this.spineWidth = spineWidth;
		
		float [] outerVertexBuffer = new float [2*path.length*book.vertexSize];
		float [] innerVertexBuffer = new float [2*path.length*book.vertexSize];
//		float [] edgeTopVertexBuffer = new float [2*path.length*book.vertexSize];
//		float [] edgeBottomVertexBuffer = new float [2*path.length*book.vertexSize];
		float [] normal = {0, 0};
		for (int i=0;i<path.length;i++)
		{
			if (i == 0) {normal[0] = -1; normal[1] = 0;}
			else if (i == path.length-1) {normal[0] = 1; normal[1] = 0;}
			else getPathNormal(path, i, normal);
			float so = i == 0 ? 1 : i == path.length-1 ? 0 : .5f;
//			float s = i == 0 || i == path.length-1 ? 1 : 0;
			
			setVertex(outerVertexBuffer, 2*i*book.vertexSize, path[i][0]+bindingWidth*normal[0], coverHeight, path[i][1]+bindingWidth*normal[1], normal[0], 0, normal[1], so, 0);
			setVertex(outerVertexBuffer, (2*i+1)*book.vertexSize, path[i][0]+bindingWidth*normal[0], 0, path[i][1]+bindingWidth*normal[1], normal[0], 0, normal[1], so, 1);
			setVertex(innerVertexBuffer, 2*i*book.vertexSize, path[i][0], coverHeight, path[i][1], -normal[0], 0, -normal[1], 1-so, 0);
			setVertex(innerVertexBuffer, (2*i+1)*book.vertexSize, path[i][0], 0, path[i][1], -normal[0], 0, -normal[1], 1-so, 1);
//			setVertex(edgeTopVertexBuffer, 2*i*book.vertexSize, path[i][0]+bindingWidth*normal[0], coverHeight, path[i][1]+bindingWidth*normal[1], 0, 1, 0, s, 1);
//			setVertex(edgeTopVertexBuffer, (2*i+1)*book.vertexSize, path[i][0], coverHeight, path[i][1], 0, 1, 0, s, 1);
//			setVertex(edgeBottomVertexBuffer, 2*i*book.vertexSize, path[i][0]+bindingWidth*normal[0], 0, path[i][1]+bindingWidth*normal[1], 0, -1, 0, s, 0);
//			setVertex(edgeBottomVertexBuffer, (2*i+1)*book.vertexSize, path[i][0], 0, path[i][1], 0, -1, 0, s, 0);
		}
		
		meshes[outerBinding].setVertices(outerVertexBuffer);
		meshes[innerBinding].setVertices(innerVertexBuffer);
//		meshes[topBinding].setVertices(edgeTopVertexBuffer);
//		meshes[bottomBinding].setVertices(edgeBottomVertexBuffer);
		
//		float [] frontEdgeVertexBuffer = new float [book.vertexSize*8];
//		setVertex(frontEdgeVertexBuffer, 0, path[0][0], 0, path[0][1], 0, 0, 1, 0, 0);
//		setVertex(frontEdgeVertexBuffer, book.vertexSize, path[0][0], coverHeight, path[0][1], 0, 0, 1, 0, 1);
//		setVertex(frontEdgeVertexBuffer, 2*book.vertexSize, path[0][0]-bindingWidth, coverHeight, path[0][1], 0, 0, 1, 0, 1);
//		setVertex(frontEdgeVertexBuffer, 3*book.vertexSize, path[0][0]-bindingWidth, 0, path[0][1], 0, 0, 1, 0, 0);
//		setVertex(frontEdgeVertexBuffer, 4*book.vertexSize, path[path.length-1][0], 0, path[path.length-1][1], 0, 0, 1, 0, 0);
//		setVertex(frontEdgeVertexBuffer, 5*book.vertexSize, path[path.length-1][0]+bindingWidth, 0, path[path.length-1][1], 0, 0, 1, 0, 0);
//		setVertex(frontEdgeVertexBuffer, 6*book.vertexSize, path[path.length-1][0]+bindingWidth, coverHeight, path[path.length-1][1], 0, 0, 1, 0, 1);
//		setVertex(frontEdgeVertexBuffer, 7*book.vertexSize, path[path.length-1][0], coverHeight, path[path.length-1][1], 0, 0, 1, 0, 1);
//		meshes[frontBinding].setVertices(frontEdgeVertexBuffer);
	}
	
	void setVertex(int vertex, float x, float y, float z)
	{
		for (int i=0;i<meshes.length;i++)
			if (indices[i][vertex] > -1)
		{
			Mesh mesh = meshes[i];
			mesh.getVerticesBuffer().put(indices[i][vertex], x);
			mesh.getVerticesBuffer().put(indices[i][vertex]+1, y);
			mesh.getVerticesBuffer().put(indices[i][vertex]+2, z);
		}
	}
	
	void setNormal(int meshIndex, int vertex, float x, float y, float z)
	{
		Mesh mesh = meshes[meshIndex];
		mesh.getVerticesBuffer().put(indices[meshIndex][vertex]+3, x);
		mesh.getVerticesBuffer().put(indices[meshIndex][vertex]+4, y);
		mesh.getVerticesBuffer().put(indices[meshIndex][vertex]+5, z);
	}
	
	public void setAngle(float la, float ra)
	{
		float llx = (float)(-Math.sin(la)), llz = (float)(Math.cos(la));
		float rlx = (float)(-Math.sin(ra)), rlz = (float)(Math.cos(ra));
		setVertex(topLeftInnerEdge, -coverDepth/2+coverLength*llx, coverHeight, spineWidth+coverLength*llz);
		setVertex(bottomLeftInnerEdge, -coverDepth/2+coverLength*llx, 0, spineWidth+coverLength*llz);
		setVertex(topRightInnerEdge, coverDepth/2-coverLength*rlx, coverHeight, spineWidth+coverLength*rlz);
		setVertex(bottomRightInnerEdge, coverDepth/2-coverLength*rlx, 0, spineWidth+coverLength*rlz);
		
		float lnx = llz, lnz = -llx;
		float rnx = rlz, rnz = -rlx;
		setVertex(topLeftOuterEdge, -coverDepth/2+coverLength*llx-bindingWidth*lnx, coverHeight, spineWidth+coverLength*llz-bindingWidth*lnz);
		setVertex(bottomLeftOuterEdge, -coverDepth/2+coverLength*llx-bindingWidth*lnx, 0, spineWidth+coverLength*llz-bindingWidth*lnz);
		setVertex(topRightOuterEdge, coverDepth/2-coverLength*rlx+bindingWidth*rnx, coverHeight, spineWidth+coverLength*rlz-bindingWidth*rnz);
		setVertex(bottomRightOuterEdge, coverDepth/2-coverLength*rlx+bindingWidth*rnx, 0, spineWidth+coverLength*rlz-bindingWidth*rnz);
		
		setNormal(innerBinding, topLeftInnerEdge, lnx, 0, lnz);
		setNormal(innerBinding, bottomLeftInnerEdge, lnx, 0, lnz);
		setNormal(outerBinding, topLeftOuterEdge, -lnx, 0, -lnz);
		setNormal(outerBinding, bottomLeftOuterEdge, -lnx, 0, -lnz);
		setNormal(innerBinding, topRightInnerEdge, -rnx, 0, rnz);
		setNormal(innerBinding, bottomRightInnerEdge, -rnx, 0, rnz);
		setNormal(outerBinding, topRightOuterEdge, rnx, 0, -rnz);
		setNormal(outerBinding, bottomRightOuterEdge, rnx, 0, -rnz);
		
		setNormal(innerBinding, topLeftInnerHinge, lnx, 0, lnz);
		setNormal(innerBinding, bottomLeftInnerHinge, lnx, 0, lnz);
		setNormal(outerBinding, topLeftOuterHinge, -lnx, 0, -lnz);
		setNormal(outerBinding, bottomLeftOuterHinge, -lnx, 0, -lnz);
		setNormal(innerBinding, topRightInnerHinge, -rnx, 0, rnz);
		setNormal(innerBinding, bottomRightInnerHinge, -rnx, 0, rnz);
		setNormal(outerBinding, topRightOuterHinge, rnx, 0, -rnz);
		setNormal(outerBinding, bottomRightOuterHinge, rnx, 0, -rnz);
		
//		setNormal(frontBinding, topLeftInnerEdge, llx, 0, llz);
//		setNormal(frontBinding, bottomLeftInnerEdge, llx, 0, llz);
//		setNormal(frontBinding, topLeftOuterEdge, llx, 0, llz);
//		setNormal(frontBinding, bottomLeftOuterEdge, llx, 0, llz);
//		setNormal(frontBinding, topRightInnerEdge, -rlx, 0, rlz);
//		setNormal(frontBinding, bottomRightInnerEdge, -rlx, 0, rlz);
//		setNormal(frontBinding, topRightOuterEdge, -rlx, 0, rlz);
//		setNormal(frontBinding, bottomRightOuterEdge, -rlx, 0, rlz);
	}
	
	public void dispose()
	{
		for (Mesh mesh : meshes)
			mesh.dispose();
	}
	
	void setVertex(float [] vertexBuffer, int index, float x, float y, float z, float nx, float ny, float nz, float s, float t)
	{
		vertexBuffer[index++] = x;
		vertexBuffer[index++] = y;
		vertexBuffer[index++] = z;
		vertexBuffer[index++] = nx;
		vertexBuffer[index++] = ny;
		vertexBuffer[index++] = nz;
		vertexBuffer[index++] = s;
		vertexBuffer[index++] = t;
	}
	
	void getPathNormal(float [][] path, int index, float [] normal)
	{
		float dx1 = path[index-1][0]-path[index][0], dy1 = path[index-1][1]-path[index][1],
			dx2 = path[index+1][0]-path[index][0], dy2 = path[index+1][1]-path[index][1];
		float l1 = (float)Math.sqrt(dx1*dx1+dy1*dy1), l2 = (float)Math.sqrt(dx2*dx2+dy2*dy2);
		dx1 /= l1; dy1 /= l1;
		dx2 /= l2; dy2 /= l2;
		float nx = -(dx1+dx2), ny = -(dy1+dy2);
		float ln = (float)Math.sqrt(nx*nx+ny*ny);
		normal[0] = nx/ln;
		normal[1] = ny/ln;
	}
	void setPathNode(float [] node, float x, float y, float t)
	{
		node[0] = x;
		node[1] = y;
		node[2] = t;
	}
	float [][] createBindingPath(int nSpinePoints)
	{
		float [][] path = new float [4+nSpinePoints][3];
		setPathNode(path[0], -.5f*coverDepth, coverLength+spineWidth, 0);
		setPathNode(path[1], -.5f*coverDepth, spineWidth, .45f);
		setPathNode(path[2+nSpinePoints], .5f*coverDepth, spineWidth, .55f);
		setPathNode(path[3+nSpinePoints], .5f*coverDepth, coverLength+spineWidth, 1);
		
		float [] p0 = {-.5f*coverDepth, spineWidth};
		float [] p1 = {.5f*coverDepth, spineWidth};
		float [] n0 = {0, -2*spineWidth};
		float [] n1 = {0, 2*spineWidth};
		
		for (int i=0;i<nSpinePoints;i++)
		{
			float t = (i+1)*1f/(nSpinePoints+1);
			setPathNode(path[2+i], 
				(1-t)*(p0[0]+n0[0]*t)+t*(p1[0]-n1[0]+n1[0]*t),
				(1-t)*(p0[1]+n0[1]*t)+t*(p1[1]-n1[1]+n1[1]*t), 
				.55f+.1f*t);
		}
		
		return path;
	}
	
	public void render(Bindable coverTex, Bindable innerCoverTex)
	{
		GL10 gl = Gdx.gl10;
		gl.glColor4f(1, 1, 1, 1);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		if (innerCoverTex != null && !book.app.bookEngine.coverOnly())
		{
			innerCoverTex.bind();
			meshes[innerBinding].render(GL10.GL_TRIANGLES);
		}
		if (coverTex != null)
		{
			coverTex.bind();
			meshes[outerBinding].render(GL10.GL_TRIANGLES);
		}
		
//		meshes[topBinding].render(GL10.GL_TRIANGLES);
//		meshes[bottomBinding].render(GL10.GL_TRIANGLES);
//		meshes[frontBinding].render(GL10.GL_TRIANGLES);
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
}
