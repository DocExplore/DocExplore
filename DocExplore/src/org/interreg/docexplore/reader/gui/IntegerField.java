/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.gui;

import org.interreg.docexplore.reader.Graphics;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gfx.Texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;

public class IntegerField extends Widget
{
	public int val;
	
	public IntegerField(float w, float h, int val)
	{
		this.w = w;
		this.h = h;
		this.val = val;
	}
	
	public float x = -1, y = -1, w = 0, h = 0;
	public float destx = -1, desty = -1;
	float [] color = new float [] {GuiLayer.defaultColor[0], GuiLayer.defaultColor[1], GuiLayer.defaultColor[2]}, 
		destColor = new float [] {GuiLayer.defaultColor[0], GuiLayer.defaultColor[1], GuiLayer.defaultColor[2]};
	public void setColor(float r, float g, float b) {color[0] = r; color[0] = g; color[0] = b;}
	public void setColor(float [] to) {color[0] = to[0]; color[0] = to[1]; color[0] = to[2]; destColor[0] = to[0]; destColor[1] = to[1]; destColor[2] = to[2];}
	public void setDestColor(float [] to) {destColor[0] = to[0]; destColor[1] = to[1]; destColor[2] = to[2];}
	public void setPosition(float x, float y) {this.x = x; this.y = y; this.destx = x; this.desty = y;}
	public void setDestPosition(float x, float y) {this.destx = x; this.desty = y;}
	public float getX() {return active ? x : -1;}
	public float getY() {return active ? y : -1;}
	public float getWidth() {return active ? w : 0;}
	public float getHeight() {return active ? h : 0;}
	
	public void updateWidget()
	{
		float k = .1f;
		x += k*(destx-x);
		y += k*(desty-y);
		color[0] += k*(destColor[0]-color[0]);
		color[1] += k*(destColor[1]-color[1]);
		color[2] += k*(destColor[2]-color[2]);
	}

	static Texture numbers = null;
	static float [] offsets = {0, .102f, .180f, .281f, .379f, .488f, .586f, .691f, .791f, .893f, 1};
	
	public void renderWidget()
	{
		if (numbers == null)
			numbers = new Texture(GfxUtils.getImage("numbers.png"), false);
		Graphics g = ReaderApp.app.debugGfx;
		Gdx.gl10.glDisable(GL10.GL_DEPTH_TEST);
		
		g.setColor(color[0], color[1], color[2], .15f*alpha);
		g.fillRect(x, y, x+w, y+h);
		g.setColor(color[0], color[1], color[2], .75f*alpha);
		g.setWidth(2);
		g.drawRect(x, y, x+w, y+h);
		
		renderInteger(g, val, x+w-.1f*h);
		Gdx.gl10.glEnable(GL10.GL_DEPTH_TEST);
	}
	
	protected float renderInteger(Graphics g, int val, float x0)
	{
		do
		{
			int digit = val%10;
			val /= 10;
			x0 = renderDigit(g, digit, x0);
		}
		while (val > 0);
		return x0;
	}
	protected float renderDigit(Graphics g, int digit, float x0)
	{
		float ch = .8f*h;
		float y0 = y+.5f*(h-ch);
		float s1 = offsets[digit], s2 = offsets[digit+1];
		float cw = .85f*ch*10*(s2-s1);
		g.fillTexturedRect(numbers, x0-cw, y0, x0, y0+ch, s1, 0, s2, 1);
		return x0-cw;
	}
}
