/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader;

import java.awt.image.BufferedImage;

import org.imgscalr.Scalr;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.reader.book.BookSpecification;
import org.interreg.docexplore.reader.book.BookSpecificationParser;
import org.interreg.docexplore.reader.gui.Button;
import org.interreg.docexplore.reader.gui.Dialog;
import org.interreg.docexplore.reader.gui.GuiEvent;
import org.interreg.docexplore.reader.gui.GuiEvent.Source;
import org.interreg.docexplore.reader.gui.GuiLayer;
import org.interreg.docexplore.reader.gui.IntervalField;
import org.interreg.docexplore.reader.gui.Label;
import org.interreg.docexplore.reader.gui.Slider;
import org.interreg.docexplore.reader.gui.Widget;
import org.interreg.docexplore.reader.net.StreamedXML;
import org.interreg.docexplore.reader.shelf.ShelfSpecification;
import org.interreg.docexplore.reader.shelf.ShelfSpecificationParser;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Pair;

import com.badlogic.gdx.Gdx;


public class ReaderMainTask extends Thread
{
	ReaderApp app;
	Dialog dialog;
	public Button back, zoom, zoomin, zoomout, reload, left, right;
	public Button open;
	public Slider slider;
	public IntervalField pageField;
	public Label logos, title, helpShelf, helpBook, helpRoi;
	
	public ReaderMainTask(final ReaderApp app) throws Exception
	{
		this.app = app;
		this.dialog = new Dialog(app);
		
		this.back = new Button(app, "home.png");
		back.setPosition(0, Gdx.graphics.getHeight()-back.h);
		this.zoom = new Button(app, "zoom.png");
		zoom.setPosition(back.x, back.y);
		this.zoomin = new Button(app, "zoomin.png");
		zoomin.holdable = true;
		this.zoomout = new Button(app, "zoomout.png");
		zoomout.holdable = true;
		this.reload = new Button(app, "reload.png");
		reload.setPosition(0, Gdx.graphics.getHeight()-reload.h);
		this.left = new Button(app, "left.png");
		left.setPosition(0, 0);//Gdx.graphics.getHeight()/2-left.h/2);
		this.right = new Button(app, "right.png");
		right.setPosition(Gdx.graphics.getWidth()-right.w, 0);//Gdx.graphics.getHeight()/2-left.h/2);
		this.logos = new Label(app, "logos.png", false);
		float logow = .5f;
		logos.h = logos.h*logow*Gdx.graphics.getWidth()/logos.w;
		logos.w = logow*Gdx.graphics.getWidth();
		logos.setPosition(Gdx.graphics.getWidth()/2-logos.w/2, 0);
		this.title = new Label(app, "title.png", false);
		float titlew = .33f;
		title.h = title.h*(titlew*Gdx.graphics.getWidth())/title.w;
		title.w = titlew*Gdx.graphics.getWidth();
		title.setPosition(Gdx.graphics.getWidth()/2-title.w/2, Gdx.graphics.getHeight()-title.h);
		this.open = new Button(app, "open.png");
		this.pageField = new IntervalField(.075f*Gdx.graphics.getWidth(), .0175f*Gdx.graphics.getWidth(), 1, 1);
		this.slider = new Slider(.2f*Gdx.graphics.getWidth(), .03f*Gdx.graphics.getWidth(), 0) {public void valChanged()
		{
			if (!app.bookEngine.active)
				return;
			pageField.setPageInterval((int)(val*app.bookEngine.book.pages.size()), app.bookEngine.book.pages.size());
		}};
		
		if (app.startup.showHelp)
		{
			BufferedImage finger = ImageUtils.read(Thread.currentThread().getContextClassLoader().getResource(Widget.class.getPackage().getName().replace('.', '/')+"/finger.png"));
			finger = Scalr.resize(finger, (int)(.05f*Gdx.graphics.getWidth()));
			this.helpShelf = new Label(app, Widget.renderText(finger, Lang.s("helpShelf1")+"<br>"+Lang.s("helpShelf2"), 
				Gdx.graphics.getWidth()/2), true, false);
			this.helpBook = new Label(app, Widget.renderText(finger, Lang.s("helpBook1")+"<br>"+Lang.s("helpBook2"), 
				Gdx.graphics.getWidth()/2), true, false);
			this.helpRoi = new Label(app, Widget.renderText(finger, Lang.s("helpRoi1")+"<br>"+Lang.s("helpRoi2"), 
				Gdx.graphics.getWidth()/2), true, false);
			
			float maxw = .75f*Gdx.graphics.getWidth();
			float maxh = .1f*Gdx.graphics.getHeight();
			float ratio = 1;
			if (helpShelf.w > maxw)
				ratio = maxw/helpShelf.w;
			if (ratio*helpShelf.h > maxh)
				ratio = maxh/helpShelf.h;
			helpBook.w *= ratio;
			helpBook.h *= ratio;
			helpBook.setPosition(.5f*(Gdx.graphics.getWidth()-helpBook.w), 0);
			helpBook.setColor(GuiLayer.defaultColor);
			helpBook.setDestColor(GuiLayer.defaultColor);
			helpShelf.w *= ratio;
			helpShelf.h *= ratio;
			helpShelf.setPosition(Gdx.graphics.getWidth()-helpShelf.w, Gdx.graphics.getHeight()-helpShelf.h);
			helpShelf.setColor(GuiLayer.defaultColor);
			helpShelf.setDestColor(GuiLayer.defaultColor);
			helpRoi.w *= ratio;
			helpRoi.h *= ratio;
			helpRoi.setPosition(0, 0);
			helpRoi.setColor(GuiLayer.defaultColor);
			helpRoi.setDestColor(GuiLayer.defaultColor);
			helpBook.fullAlpha = 1;
			helpShelf.fullAlpha = 1;
			helpRoi.fullAlpha = 1;
		}
		
		//open.setPosition(Gdx.graphics.getWidth()/2-open.w/2, Gdx.graphics.getHeight()-1.5f*open.h);
		
		this.logos.fullAlpha = .75f;
		this.title.fullAlpha = .75f;
		
		app.gui.addWidget(back);
		app.gui.addWidget(zoom);
		app.gui.addWidget(zoomin);
		app.gui.addWidget(zoomout);
		app.gui.addWidget(reload);
		app.gui.addWidget(left);
		app.gui.addWidget(right);
		app.gui.addWidget(logos);
		app.gui.addWidget(slider);
		app.gui.addWidget(pageField);
		app.gui.addWidget(title);
		app.gui.addWidget(open);
		app.gui.addWidget(dialog);
		
		if (app.startup.showHelp)
		{
			app.gui.addWidget(helpBook);
			app.gui.addWidget(helpShelf);
			app.gui.addWidget(helpRoi);
		}
	}
	
	float margin = 20;
	public void run()
	{
		try
		{
			//Thread.sleep(1000);
			dialog.set(Lang.s("waitLabel"), Lang.s("loadingLabel"));
			dialog.activate(true);
			Thread.sleep(1000);
			
			StreamedXML index = app.client.getResource(StreamedXML.class, "index.xml");
			index.waitUntilComplete();
			ShelfSpecification spec = new ShelfSpecificationParser().parse(index.doc, app.client);
			app.shelf.activate(true);
			app.shelf.setShelf(spec);
			
			app.input.timeout.start();
			boolean running = true;
			//SoundManager.Playback playback = null;
			
			while (running)
			{
				System.gc();
				app.logger.addEntry("Shelf view");
				
				dialog.activate(false);
				//reload.activate(true);
				logos.activate(true);
				left.activate(true);
				right.activate(true);
				//open.activate(true);
				title.activate(true);
				left.setDestPosition(0, (Gdx.graphics.getHeight()-left.h)/2);
				right.setDestPosition(Gdx.graphics.getWidth()-right.w, (Gdx.graphics.getHeight()-left.h)/2);
				title.setDestPosition(Gdx.graphics.getWidth()/2-title.w/2, logos.h);
				if (app.startup.showHelp)
					helpShelf.activate(true);
				ShelfSpecification.Entry entry = null;
				
//				if (playback != null)
//					playback.stop();
//				else playback = app.sound.play(app.client.getResource(StreamedSound.class, "book0/chopin.mp3"));
				if (app.shelf.shelf.entries.size() == 1)
					entry = app.shelf.shelf.entries.get(0);
				
				while (entry == null)
				{
					Pair<GuiEvent.Source, Object> pair = GuiEvent.waitForEvent(reload, left, right, app.shelf);
					
					if (pair.first == left)
						app.shelf.left();
					else if (pair.first == right)
						app.shelf.right();
					else if (pair.first == app.shelf && app.shelf.selectedEntry() != null)
					{
						if (pair.second != null && pair.second instanceof ShelfSpecification.Entry)
						{
							entry = (ShelfSpecification.Entry)pair.second;//app.shelf.selectedEntry();
							app.logger.addEntry("Shelf entry selected: "+entry.title+ "("+entry.src+")");
						
							dialog.set(Lang.s("confirmLabel"), Lang.s("readLabel").replace("%s", entry.title), 
								Lang.s("yesLabel"), Lang.s("noLabel"));
							app.input.addListener(dialog);
							dialog.activate(true);
							if (dialog.waitForClick() != 0)
							{
								entry = null;
								app.logger.addEntry("Shelf entry canceled");
							}
							else app.logger.addEntry("Shelf entry confirmed");
							
							dialog.activate(false);
							app.input.removeListener(dialog);
						}
						else if (pair.second == null)
						{
							entry = null;
							running = false;
							break;
						}
					}
					else if (pair.first == reload)
					{
						app.logger.addEntry("Shelf refresh");
						reload.activate(false);
						dialog.set(Lang.s("waitLabel"), Lang.s("loadingLabel"));
						dialog.activate(true);
						Thread.sleep(1000);
						
						index.release();
						try {index.close();} catch (Throwable t) {}
						index = app.client.getResource(StreamedXML.class, "index.xml");
						index.waitUntilComplete();
						spec = new ShelfSpecificationParser().parse(index.doc, app.client);
						app.shelf.setShelf(spec);
						dialog.activate(false);
						reload.activate(true);
					}
				}
				
				if (entry != null)
				{
					reload.activate(false);
					if (app.startup.showHelp)
						helpShelf.activate(false);
					
					StreamedXML bookSpec = app.client.getResource(StreamedXML.class, entry.src);
					bookSpec.waitUntilComplete();
					BookSpecification book = new BookSpecificationParser(app).load(bookSpec.doc, "");
					
					if (!book.parchment)
						bookReaderTask(book);
					else parchmentReaderTask(book);
					
					if (app.shelf.shelf.entries.size() == 1)
						break;
					app.shelf.activate(true);
				}
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			app.logger.addEntry("Fatal exception : "+e);
		}
		app.logger.addEntry("Shutdown");
		app.logger.dispose();
		System.exit(0);
	}
	
	void parchmentReaderTask(BookSpecification book) throws Exception
	{
		app.logger.addEntry("Parchment engine startup");
		app.parchmentEngine.setBook(book);
		app.parchmentEngine.activate(true);
		app.shelf.activate(false);
		back.setPosition(-back.w, Gdx.graphics.getHeight()-back.h);
		back.setDestPosition(0, Gdx.graphics.getHeight()-back.h);
		back.activate(true);
		left.activate(false);
		right.activate(false);
		logos.activate(false);
		title.setDestPosition(0, 0);
		zoomout.setPosition(-zoomout.w, Gdx.graphics.getHeight()-zoomout.h);
		zoomout.setDestPosition(.5f*Gdx.graphics.getWidth()-margin-zoomout.w, Gdx.graphics.getHeight()-zoomout.h);
		zoomin.setPosition(-zoomin.w, Gdx.graphics.getHeight()-zoomin.h);
		zoomin.setDestPosition(.5f*Gdx.graphics.getWidth()+margin, Gdx.graphics.getHeight()-zoomin.h);
		zoomout.activate(true);
		zoomin.activate(true);
		
		while (true)
		{
			Pair<Source, Object> clicked = GuiEvent.waitForEvent(back, zoomin, zoomout);
			if (clicked.first == back)
			{
//				dialog.set("Confirm", "Do you wish to return to the library?", "Yes", "No");
//				app.input.addListener(dialog);
//				dialog.activate(true);
//				int button = dialog.waitForClick();
//				dialog.activate(false);
//				app.input.removeListener(dialog);
//				if (button == 0)
					break;
			}
			else if (clicked.first == zoomin)
				app.parchmentEngine.changeZoom(-.05f);
			else if (clicked.first == zoomout)
				app.parchmentEngine.changeZoom(.05f);
		}
		
		app.logger.addEntry("Parchment engine shutdown");
		app.parchmentEngine.activate(false);
		//back.setDestPosition(-back.w, Gdx.graphics.getHeight()-back.h);
		back.activate(false);
		zoomout.setDestPosition(-zoomout.w, Gdx.graphics.getHeight()-zoomout.h);
		zoomout.activate(false);
		zoomin.setDestPosition(-zoomin.w, Gdx.graphics.getHeight()-zoomin.h);
		zoomin.activate(false);
		left.activate(true);
		right.activate(true);
	}
	
	void bookReaderTask(BookSpecification book) throws Exception
	{
		app.logger.addEntry("Book engine startup");
		app.bookEngine.setBook(book);
		app.bookEngine.activate(true);
		app.shelf.activate(false);
		back.activate(true);
		zoom.activate(true);
		back.setDestPosition(0, Gdx.graphics.getHeight()-back.h);
		zoom.setDestPosition(back.x+back.w+margin, back.y);
		left.activate(true);
		right.activate(true);
		logos.activate(false);
		title.activate(true);
		slider.activate(true);
		slider.setPosition(0, Gdx.graphics.getHeight()-slider.h);
		slider.setDestPosition(Gdx.graphics.getWidth()-slider.w, Gdx.graphics.getHeight()-slider.h);
		pageField.activate(true);
		pageField.setPosition(0, Gdx.graphics.getHeight()-pageField.h);
		pageField.setDestPosition(Gdx.graphics.getWidth()-slider.w-pageField.w, Gdx.graphics.getHeight()-pageField.h-.5f*(slider.h-pageField.h));
		left.setDestPosition(0, 0);
		right.setDestPosition(Gdx.graphics.getWidth()-right.w, 0);
		title.setDestPosition(Gdx.graphics.getWidth()/2-title.w/2, Gdx.graphics.getHeight()-title.h);
		if (app.startup.showHelp)
			helpBook.activate(true);
		
		while (true)
		{
			Pair<Source, Object> clicked = GuiEvent.waitForEvent(back, zoom, app.bookEngine.roiOverlay, left, right, slider);
			if (clicked.first == back)
			{
//				dialog.set("Confirm", "Do you wish to return to the library?", "Yes", "No");
//				app.input.addListener(dialog);
//				dialog.activate(true);
//				int button = dialog.waitForClick();
//				dialog.activate(false);
//				app.input.removeListener(dialog);
//				if (button == 0)
					break;
			}
			else if (clicked.first == slider)
			{
				app.bookEngine.setLeftPage(2*((int)(slider.val*book.pages.size())/2)-1);
			}
			else if (clicked.first == zoom)
			{
				title.activate(false);
				back.activate(false);
				left.activate(false);
				right.activate(false);
				slider.activate(false);
				pageField.activate(false);
				zoom.setDestColor(GuiLayer.defaultHighlightColor);
				zoom.setDestPosition((Gdx.graphics.getWidth()-zoom.w)/2, zoom.y);
				//zoomout.setPosition(zoom.destx-margin-zoomout.w, zoom.y);
				//zoomin.setPosition(zoom.destx+margin+zoom.w, zoom.y);
				zoomout.setPosition(zoom.x, zoom.y);
				zoomin.setPosition(zoom.x, zoom.y);
				zoomout.setDestPosition(zoom.destx-margin-zoomout.w, zoom.y);
				zoomin.setDestPosition(zoom.destx+margin+zoom.w, zoom.y);
				zoomout.activate(true);
				zoomin.activate(true);
				if (app.startup.showHelp)
					helpBook.activate(false);
				app.bookEngine.zoom.activate(true);
				app.input.moveListenerFirst(app.gui);
				
				while (true)
				{
					Pair<GuiEvent.Source, Object> pair = GuiEvent.waitForEvent(zoom, zoomin, zoomout);
					if (pair.first == zoom)
						break;
					else if (pair.first == zoomin)
						app.bookEngine.zoom.increaseZoom(-.03f);
					else if (pair.first == zoomout)
						app.bookEngine.zoom.increaseZoom(.03f);
				}
				
				app.bookEngine.zoom.activate(false);
				back.activate(true);
				zoom.setDestPosition(back.x+back.w+margin, back.y);
				title.activate(true);
				left.activate(true);
				right.activate(true);
				slider.activate(true);
				pageField.activate(true);
				zoom.setDestColor(GuiLayer.defaultColor);
				zoomout.setDestPosition(zoom.destx, zoom.desty);
				zoomin.setDestPosition(zoom.destx, zoom.desty);
				zoomout.activate(false);
				zoomin.activate(false);
				if (app.startup.showHelp)
					helpBook.activate(true);
			}
			else if (clicked.first == app.bookEngine.roiOverlay && (Boolean)clicked.second)
			{
				title.activate(false);
				back.activate(false);
				zoom.activate(false);
				left.activate(false);
				right.activate(false);
				slider.activate(false);
				pageField.activate(false);
				if (app.startup.showHelp)
				{
					helpBook.activate(false);
					helpRoi.activate(true);
				}
				
				boolean active = true;
				while (active)
				{
					clicked = GuiEvent.waitForEvent(app.bookEngine.roiOverlay, back);
					active = clicked.first == back ? false : (Boolean)clicked.second;
				}
				
				app.bookEngine.roiOverlay.activate(false);
				title.activate(true);
				left.activate(true);
				right.activate(true);
				back.activate(true);
				zoom.activate(true);
				slider.activate(true);
				pageField.activate(true);
				if (app.startup.showHelp)
				{
					helpBook.activate(true);
					helpRoi.activate(false);
				}
			}
			else if (clicked.first == right)
				app.bookEngine.turnRight();
			else if (clicked.first == left)
				app.bookEngine.turnLeft();
		}
		
		app.logger.addEntry("Book engine shutdown");
		app.bookEngine.activate(false);
		zoom.setDestPosition(back.x, back.y);
		back.activate(false);
		zoom.activate(false);
		left.activate(false);
		right.activate(false);
		slider.activate(false);
		slider.setDestPosition(0, Gdx.graphics.getHeight()-slider.h);
		pageField.activate(false);
		pageField.setDestPosition(0, Gdx.graphics.getHeight()-pageField.h);
		if (app.startup.showHelp)
			helpBook.activate(false);
	}
}
