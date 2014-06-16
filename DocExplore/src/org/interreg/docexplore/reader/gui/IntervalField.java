package org.interreg.docexplore.reader.gui;

import org.interreg.docexplore.reader.Graphics;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.gfx.GfxUtils;
import org.interreg.docexplore.reader.gfx.Texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;

public class IntervalField extends IntegerField
{
	public int hiVal;
	
	public IntervalField(float w, float h, int val, int hiVal)
	{
		super(w, h, val);
		this.hiVal = hiVal;
	}
	
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
		
		if (hiVal == val)
			renderInteger(g, val, x+w-.1f*h);
		else renderInteger(g, val, renderDash(g, renderInteger(g, hiVal, x+w-.1f*h)));
		Gdx.gl10.glEnable(GL10.GL_DEPTH_TEST);
	}

	protected float renderDash(Graphics g, float x0)
	{
		float ch = .8f*h;
		float y0 = y+.5f*(h-ch);
		float cw = .5f*ch;
		g.setWidth(.2f*cw);
		g.drawLine(x0-.84f*cw, y0+.5f*ch, x0-.16f*cw, y0+.5f*ch);
		return x0-cw;
	}
	
	public void setPageInterval(int num, int lastPage)
	{
		num = 2*(num/2);
		if (num <= 0)
			val = hiVal = 1;
		else if (num >= lastPage)
			val = hiVal = lastPage;
		else
		{
			val = num;
			hiVal = num+1;
		}
	}
}
