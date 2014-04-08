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

import java.nio.FloatBuffer;

import org.interreg.docexplore.reader.util.Math3D;

import com.badlogic.gdx.graphics.VertexAttributes;

public class Grid
{
	public static class Node
	{
		public final float [] point;
		public final float [] normal;
		
		public Node(float x, float y, float z)
		{
			this.point = new float [] {x, y, z};
			this.normal = new float [] {0, 1, 0};
		}
		
		public void write(FloatBuffer frontBuffer, FloatBuffer backBuffer, int index)
		{
			frontBuffer.put(index, (float)point[0]);
			frontBuffer.put(index+1, (float)point[1]);
			frontBuffer.put(index+2, (float)point[2]);
			frontBuffer.put(index+3, (float)normal[0]);
			frontBuffer.put(index+4, (float)normal[1]);
			frontBuffer.put(index+5, (float)normal[2]);
			
			backBuffer.put(index, (float)point[0]);
			backBuffer.put(index+1, (float)point[1]);
			backBuffer.put(index+2, (float)point[2]);
			backBuffer.put(index+3, (float)-normal[0]);
			backBuffer.put(index+4, (float)-normal[1]);
			backBuffer.put(index+5, (float)-normal[2]);
		}
		
		public void set(float x, float y, float z) {point[0] = x; point[1] = y; point[2] = z;}
	}
	
	Node [][] nodes;
	
	public Grid(float [] p, float [] u, float [] v, int w, int h)
	{
		this.nodes = new Node [w][h];
		
		for (int i=0;i<w;i++)
		{
			float ki = i*1.f/(w-1);
			for (int j=0;j<h;j++)
			{
				float kj = j*1.f/(h-1);
				nodes[i][j] = new Node(p[0]+ki*u[0]+kj*v[0], p[1]+ki*u[1]+kj*v[1], p[2]+ki*u[2]+kj*v[2]);
			}
		}
	}
	
	float [] hor = {0, 0, 0}, ver = {0, 0, 0};
	public void computeNormals()
	{
		int w = nodes.length;
		int h = nodes[0].length;
		
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
		{
			float [] left = i == 0 ? nodes[0][j].point : nodes[i-1][j].point, right = i == w-1 ? nodes[w-1][j].point : nodes[i+1][j].point;
			float [] bottom = j == 0 ? nodes[i][0].point : nodes[i][j-1].point, top = j == h-1 ? nodes[i][h-1].point : nodes[i][j+1].point;
			
			Math3D.diff(right, left, hor);
			Math3D.diff(bottom, top, ver);
			Math3D.crossProduct(hor, ver, nodes[i][j].normal);
			Math3D.normalize(nodes[i][j].normal, nodes[i][j].normal);
		}
	}
	
	public void write(FloatBuffer frontBuffer, FloatBuffer backBuffer, VertexAttributes attributes)
	{
		int w = nodes.length;
		int h = nodes[0].length;
		
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
		{
			int index = attributes.vertexSize/(Float.SIZE/8)*(j*w+i);
			
			nodes[i][j].write(frontBuffer, backBuffer, index);
			frontBuffer.put(index+6, (float)(i*1./(w-1)));
			frontBuffer.put(index+7, (float)(j*1./(h-1)));
			backBuffer.put(index+6, (float)(1-i*1./(w-1)));
			backBuffer.put(index+7, (float)(j*1./(h-1)));
		}
	}
	/*public void render(boolean side)
	{
		int w = nodes.length;
		int h = nodes[0].length;
		
		GL11.glBegin(GL11.GL_QUADS);
		for (int i=0;i<w-1;i++)
			for (int j=0;j<h-1;j++)
		{
			texCoord(i*1./(w-1), j*1./(h-1), side);
			nodes[i][j].render(side);
			
			if (side)
			{
				texCoord(i*1./(w-1), (j+1)*1./(h-1), side);
				nodes[i][j+1].render(side);
			}
			else
			{
				texCoord((i+1)*1./(w-1), j*1./(h-1), side);
				nodes[i+1][j].render(side);
			}
			
			texCoord((i+1)*1./(w-1), (j+1)*1./(h-1), side);
			nodes[i+1][j+1].render(side);
			
			if (side)
			{
				texCoord((i+1)*1./(w-1), j*1./(h-1), side);
				nodes[i+1][j].render(side);
			}
			else
			{
				texCoord(i*1./(w-1), (j+1)*1./(h-1), side);
				nodes[i][j+1].render(side);
			}
		}
		GL11.glEnd();
	}*/
}
