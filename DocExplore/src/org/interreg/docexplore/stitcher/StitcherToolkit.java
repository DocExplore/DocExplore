/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.util.GuiUtils;

public class StitcherToolkit
{
	Stitcher stitcher;
	
	public StitcherToolkit(Stitcher stitcher)
	{
		this.stitcher = stitcher;
	}
	
	public void importFiles(final File [] files)
	{
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			float progress = 0;
			@Override public void run()
			{
				double x0 = 0;
				for (int i=0;i<files.length;i++) try
				{
					progress = i*1f/files.length;
					Fragment f = stitcher.fragmentSet.add(files[i], stitcher.detector);
					f.setPos(x0, f.uiy);
					while (stitcher.view.boundsIntersect(f))
						f.setPos(x0 = f.uix+1.5, f.uiy);
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
			@Override public float getProgress() {return progress;}
		}, stitcher.win);
		stitcher.view.fitView(.1);
	}
	
	public static void detectLayout(FragmentView view)
	{
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			float [] progress = {0};
			@Override public void run()
			{
				new LayoutDetector(view.set).process(progress);
				view.repaint();
			}
			@Override public float getProgress() {return progress[0];}
		}, view);
	}
	
	public static void stitchPoster(Book poster, DocExploreDataLink link, FragmentSet set, FeatureDetector detector, boolean useCurrentLayout, Component parent)
	{
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			float progress = 0;
			@Override public void run()
			{
				double x0 = 0;
				try
				{
					List<MetaData> mds = poster.getMetaDataListForKey(link.stitchKey);
					if (!mds.isEmpty())
						return;
					
					MetaData [][] tiles = PosterUtils.getBaseTilesArray(link, poster);
					//fragments corresponding with tiles (for easy association creation)
					Fragment [][] fragments = new Fragment [tiles.length][];
					
					//count number of tiles (no nulls or empties)
					int nTiles = 0;
					for (int i=0;i<tiles.length;i++)
					{
						fragments[i] = new Fragment [tiles[i].length];
						for (int j=0;j<tiles[i].length;j++)
							if (tiles[i][j] != null && tiles[i][j].getType().equals(MetaData.imageType))
								nTiles++;
					}
					//create the fragments
					int cnt = 0;
					for (int i=0;i<tiles.length;i++)
						for (int j=0;j<tiles[i].length;j++)
							if (tiles[i][j] != null && tiles[i][j].getType().equals(MetaData.imageType))
							{
								progress = cnt*1f/nTiles;
								Fragment f = set.add(tiles[i][j].getCanonicalUri(), link, detector);
								fragments[i][j] = f;
								if (useCurrentLayout)
								{
									int [] pos = PosterUtils.getPartPos(link, tiles[i][j]);
									f.setPos(1.5*pos[0], 1.5*pos[1]);
									if (i > 0 && fragments[i-1].length > j && fragments[i-1][j] != null) set.add(fragments[i-1][j], fragments[i][j]);
									if (j > 0 && fragments[i][j-1] != null) set.add(fragments[i][j-1], fragments[i][j]);
									if (i > 0 && j > 0 && fragments[i-1].length > j-1 && fragments[i-1][j-1] != null) set.add(fragments[i-1][j-1], fragments[i][j]);
									if (i > 0 && fragments[i-1].length > j+1 && fragments[i-1][j+1] != null) set.add(fragments[i-1][j+1], fragments[i][j]);
								}
								else f.setPos(x0 += 1.5, f.uiy);
								cnt++;
							}
					
//					if (useCurrentLayout)
//					{
//						GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
//						{
//							float [] progress = {0};
//							@Override public void run()
//							{
//								try
//								{
//									for (int i=0;i<set.associations.size();i++)
//									{
//										FragmentAssociation map = set.associations.get(i);
//										progress[0] = i*1f/set.associations.size();
//										map.refreshFeatures();
//										FragmentAssociationUtils.match(map, progress, progress[0], (i+1)*1f/set.associations.size());
//										List<Association> res = new ArrayList<Association>();
//										new GroupDetector().detect(map, res, true);
//										map.associations = res;
//										map.resetAssociationsByPOI();
//									}
//								}
//								catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
//							}
//							@Override public float getProgress() {return progress[0];}
//						}, parent);
//					}
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
			@Override public float getProgress() {return progress;}
		}, parent);
	}
	
	public static void consolidateAll(FragmentView view)
	{
		LayoutDetector layout = new LayoutDetector(view.set);
		Set<Fragment> consolidated = new HashSet<>();
		List<Fragment> stack = new ArrayList<Fragment>();
		
		for (Fragment f : view.set.fragments)
			if (!consolidated.contains(f))
			{
				layout.consolidate(f);
				stack.add(f);
				while (!stack.isEmpty())
				{
					Fragment cur = stack.remove(stack.size()-1);
					consolidated.add(cur);
					List<FragmentAssociation> list = view.set.associationsByFragment.get(cur);
					if (list != null)
						for (int i=0;i<list.size();i++)
					{
						FragmentAssociation fa = list.get(i);
						Fragment next = fa.other(cur);
						if (consolidated.contains(next))
							continue;
						stack.add(next);
					}
				}
			}
	}
}
