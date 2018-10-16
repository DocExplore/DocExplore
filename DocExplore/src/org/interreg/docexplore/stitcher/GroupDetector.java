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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.interreg.docexplore.stitcher.network.Network;

public class GroupDetector
{
	double x0, y0;
	double w, h;
	List<Group> [][] groups;
	Network model;
	boolean useUiGeometry;
	double [][] rot = {{0, 0}, {0, 0}};
	
	public GroupDetector()
	{
		this(2, 32);
	}
	@SuppressWarnings("unchecked")
	public GroupDetector(int nw, int nh)
	{
		this.groups = new List [nw][nh];
		try
		{
			ObjectInputStream in = new ObjectInputStream(ClassLoader.getSystemResourceAsStream("org/interreg/docexplore/stitcher/model"));
			this.model = (Network)in.readObject();
		}
		catch (Exception e) {e.printStackTrace(); System.exit(0);}
	}
	
	public void setup(FragmentAssociation map, boolean useUiRotation)
	{
		this.x0 = -Math.max(map.d1.fragment.imagew, map.d2.fragment.imagew);
		this.y0 = -Math.max(map.d1.fragment.imageh, map.d2.fragment.imageh);
		this.w = -2*x0;
		this.h = -2*y0;
		
		for (int i=0;i<groups.length;i++)
			for (int j=0;j<groups[0].length;j++)
				if (groups[i][j] != null)
					groups[i][j].clear();
		
		this.useUiGeometry = useUiRotation;
		if (useUiRotation)
		{
			double ang = map.d2.fragment.uiang-map.d1.fragment.uiang;
			double ca = Math.cos(ang), sa = Math.sin(ang);
			rot[0][0] = ca; rot[1][0] = -sa;
			rot[0][1] = sa; rot[1][1] = ca;
		}
		else
		{
			rot[0][0] = 1; rot[1][0] = 0;
			rot[0][1] = 0; rot[1][1] = 1;
		}
	}
	
	double rightx(double x, double y) {return rot[0][0]*x+rot[1][0]*y;}
	double righty(double x, double y) {return rot[0][1]*x+rot[1][1]*y;}
	double leftx(double x, double y) {return rot[0][0]*x-rot[1][0]*y;}
	double lefty(double x, double y) {return -rot[0][1]*x+rot[1][1]*y;}
	
	int binx(double x) {return Math.max(0, Math.min(groups.length-1, (int)((x-x0)*groups.length/w)));}
	int biny(double y) {return Math.max(0, Math.min(groups[0].length-1, (int)((y-y0)*groups[0].length/h)));}
	void removeGroup(Group group)
	{
//		if (groups[binx(group.dx)][biny(group.dy)] == null)
//		{
//			System.out.print(binx(group.dx)+","+biny(group.dy)+"!!!");
//			for (int i=0;i<groups.length;i++)
//				for (int j=0;j<groups[0].length;j++)
//					if (groups[i][j] != null)
//						for (int k=0;k<groups[i][j].size();k++)
//							if (groups[i][j].get(k) == group)
//			{
//				System.out.println(i+","+j);
//			}
//		}
		int bx = binx(group.meddx);
		int by = biny(group.meddy);
		if (groups[bx][by] == null)
			return;
		groups[bx][by].remove(group);
	}
	void addGroup(Group group)
	{
		int i = binx(group.meddx), j = biny(group.meddy);
		if (groups[i][j] == null)
			groups[i][j] = new ArrayList<Group>(1);
		groups[i][j].add(group);
	}
	Group findNearestGroup(double dx, double dy, double ray) {return findNearestGroup(dx, dy, ray, null);}
	Group findNearestGroup(double dx, double dy, double ray, Group exclude)
	{
		int i0 = binx(dx-ray), i1 = binx(dx+ray);
		int j0 = biny(dy-ray), j1 = biny(dy+ray);
		Group min = null;
		double minDist = 0;
		for (int i=i0;i<=i1;i++)
			for (int j=j0;j<=j1;j++)
				if (groups[i][j] != null)
					for (int k=0;k<groups[i][j].size();k++)
		{
			Group g = groups[i][j].get(k);
			if (g == exclude)
				continue;
			double d = (g.meddx-dx)*(g.meddx-dx)+(g.meddy-dy)*(g.meddy-dy);
			if (d < ray*ray && (min == null || d < minDist))
			{
				min = g;
				minDist = d;
			}
		}
		return min;
	}
	void addAssociation(Association a)
	{
		//double dx = a.p2.x-a.p1.x, dy = a.p2.y-a.p1.y;
		double dx = rightx(a.p2.x, a.p2.y)-a.p1.x, dy = righty(a.p2.x, a.p2.y)-a.p1.y;
		Group g = findNearestGroup(dx, dy, Stitcher.groupSpreadRay);
		if (g == null)
		{
			g = new Group(this, a.fa, dx, dy);
			g.assocs.add(a);
		}
		else
		{
			removeGroup(g);
			g.assocs.add(a);
			g.updateDiff();
		}
		addGroup(g);
	}
	List<Group []> merges = new ArrayList<Group []>();
	int findSmallMerges()
	{
		for (int i=0;i<groups.length;i++)
			for (int j=0;j<groups[0].length;j++)
				if (groups[i][j] != null)
					for (int k=0;k<groups[i][j].size();k++)
		{
			Group g = groups[i][j].get(k);
			Group c = findNearestGroup(g.meddx, g.meddy, Stitcher.groupSpreadRay, g);
			if (c != null)
				merges.add(new Group [] {g, c});
		}
		if (merges.isEmpty())
			return 0;
		for (int i=0;i<merges.size();i++)
		{
			Group g1 = merges.get(i)[0];
			Group g2 = merges.get(i)[1];
			if (g1 == g2)
				continue;
			removeGroup(g1);
			removeGroup(g2);
			g1.assocs.addAll(g2.assocs);
			g1.updateDiff();
			addGroup(g1);
			for (int j=i+1;j<merges.size();j++)
				for (int k=0;k<2;k++)
					if (merges.get(j)[k] == g2) {merges.get(j)[k] = g1;}
		}
		int res = merges.size();
		merges.clear();
		return res;
	}
	int findLargeMerges(List<Group> groups)
	{
		int n = 0;
		for (int i=0;i<groups.size()-1;i++)
		{
			Group g1 = groups.get(i);
			double mx1 = .5*(g1.bounds1[0]+g1.bounds1[2]), my1 = .5*(g1.bounds1[1]+g1.bounds1[3]);
			for (int j=i+1;j<groups.size();j++)
			{
				Group g2 = groups.get(j);
				double mx2 = .5*(g2.bounds1[0]+g2.bounds1[2]), my2 = .5*(g2.bounds1[1]+g2.bounds1[3]);
				double dist = Math.sqrt((mx1-mx2)*(mx1-mx2)+(my1-my2)*(my1-my2));
				double ddist = Math.sqrt((g1.meddx-g2.meddx)*(g1.meddx-g2.meddx)+(g1.meddy-g2.meddy)*(g1.meddy-g2.meddy));
				//System.out.printf("%d %d => %.3f\n", i, j, ddist/dist);
				if (ddist/dist < Stitcher.groupDivergenceRatioThreshold)
					merges.add(new Group [] {g1, g2});
			}
		}
		for (int i=0;i<merges.size();i++)
		{
			Group g1 = merges.get(i)[0];
			Group g2 = merges.get(i)[1];
			if (g1 == g2)
				continue;
			
			g1.addToMap(g2.assocs);
			g1.updateDiff();
			
			groups.set(g2.index, groups.get(groups.size()-1));
			groups.get(g2.index).index = g2.index;
			groups.remove(groups.size()-1);
			
			for (int j=i+1;j<merges.size();j++)
				for (int k=0;k<2;k++)
					if (merges.get(j)[k] == g2) {merges.get(j)[k] = g1;}
		}
		merges.clear();
		return n;
	}
	
	ArrayList<POI> near = new ArrayList<POI>();
	ArrayList<Association> stack = new ArrayList<Association>();
	void spreadGroupLocally(FragmentAssociation map, Group g, Set<POI> associated1, Set<POI> associated2)
	{
		for (int i=0;i<g.assocs.size();i++)
		{
			Association a = g.assocs.get(i);
			stack.add(a);
		}
		boolean spread = false;
		while (!stack.isEmpty())
		{
			Association a = stack.remove(stack.size()-1);
			map.d1.nearPOIs(a.p1.x, a.p1.y, 3*Stitcher.groupSpreadRay, near);
			for (int i=0;i<near.size();i++)
			{
				POI p1 = near.get(i);
				if (associated1.contains(p1))
					continue;
				//POI min = findLocalMatch(p1, map.d2, .3*Stitcher.groupSpreadRay, a.p2.x-a.p1.x, a.p2.y-a.p1.y, associated2);
				POI min = findLocalMatch(p1, map.d2, .3*Stitcher.groupSpreadRay, rightx(a.p2.x, a.p2.y)-a.p1.x, righty(a.p2.x, a.p2.y)-a.p1.y, associated2);
				if (min != null)
				{
					Association newa = new Association(map, p1, min, 0);
					g.assocs.add(newa);
					associated1.add(p1);
					associated2.add(min);
					stack.add(newa);
					spread = true;
				}
			}
			near.clear();
		}
		if (spread)
			g.updateDiff();
	}
	void spreadGroupInternally(FragmentAssociation map, Group g, Set<POI> associated1, Set<POI> associated2)
	{
		boolean spread = false;
		map.d1.containedPOIs(g.bounds1[0], g.bounds1[1], g.bounds1[2]-g.bounds1[0], g.bounds1[3]-g.bounds1[1], near);
		for (int i=0;i<near.size();i++)
		{
			POI p1 = near.get(i);
			if (associated1.contains(p1))
				continue;
			POI min = findLocalMatch(p1, map.d2, .3*Stitcher.groupSpreadRay, g.meddx, g.meddy, associated2);
			if (min != null)
			{
				Association newa = new Association(map, p1, min, 0);
				g.assocs.add(newa);
				associated1.add(p1);
				associated2.add(min);
				spread = true;
			}
		}
		near.clear();
		if (spread)
			g.updateDiff();
	}
	//returns level of confidence
	Set<POI> associated1 = new HashSet<POI>();
	Set<POI> associated2 = new HashSet<POI>();
	void strengthenGroup(FragmentAssociation map, Group g)
	{
		for (int i=0;i<g.assocs.size();i++)
		{
			Association a = g.assocs.get(i);
			associated1.add(a.p1);
			associated2.add(a.p2);
		}
		//System.out.printf("%d => ", g.assocs.size());
		spreadGroupLocally(map, g, associated1, associated2);
		//System.out.printf("%d => ", g.assocs.size());
		g.computeBounds();
		spreadGroupInternally(map, g, associated1, associated2);
		//System.out.printf("%d ", g.assocs.size());
		g.computeBounds();
		
		associated1.clear();
		associated2.clear();
	}
	
	ArrayList<POI> local = new ArrayList<POI>();
	POI findLocalMatch(POI p1, FragmentDescription desc, double ray, double dx, double dy, Set<POI> exclude)
	{
		//desc.nearPOIs(p1.x+dx, p1.y+dy, ray, local);
		double tx = p1.x+dx, ty = p1.y+dy;
		desc.nearPOIs(leftx(tx, ty), lefty(tx, ty), ray, local);
		
		double minDist = 0;
		POI min = null;
		for (int j=0;j<local.size();j++)
		{
			POI p2 = local.get(j);
			if (exclude.contains(p2))
				continue;
			double dist = p1.featureDistance2(p2, !useUiGeometry);
			if (min == null || dist < minDist)
			{
				min = p2;
				minDist = dist;
			}
		}
		local.clear();
		if (min != null && minDist <= min.matchThreshold())
			return min;
		return null;
	}
	
	public void detect(FragmentAssociation map, Collection<Association> res, boolean force, boolean useUiGeometry)
	{
		setup(map, useUiGeometry);
		
		for (int i=0;i<map.associations.size();i++)
		{
			Association a = map.associations.get(i);
			addAssociation(a);
		}
		while (findSmallMerges() > 0);
		
		List<Group> lateGroups = new ArrayList<Group>();
		for (int i=0;i<groups.length;i++)
			for (int j=0;j<groups[0].length;j++)
				if (groups[i][j] != null)
				{
					for (int k=0;k<groups[i][j].size();k++)
					{
						Group g = groups[i][j].get(k);
						strengthenGroup(map, g);
						boolean reject = g.assocs.size() < Stitcher.groupEarlySizeThreshold;
//						if (!reject)
//						{
//							double c = g.earlyGroupConfidence();
//							reject = c < Stitcher.groupEarlyConfidenceThreshold;
//						}
						if (reject)
						{
							groups[i][j].set(k, groups[i][j].get(groups[i][j].size()-1));
							groups[i][j].remove(groups[i][j].size()-1);
							k--;
						}
						else
						{
							g.index = lateGroups.size();
							lateGroups.add(g);
							g.computeMap();
							//g.dump();
							//showGroup(g);
						}
					}
					if (groups[i][j].isEmpty())
						groups[i][j] = null;
				}
		while (findLargeMerges(lateGroups) > 0);
		
//		for (int i=0;i<lateGroups.size();i++)
//		{
//			res.addAll(lateGroups.get(i).assocs);
//		}
		
		Group max = null;
		double maxd = 0;
		boolean showGroups = false;//stitcher.showDetectedGroups;
		for (int i=0;i<lateGroups.size();i++)
		{
			Group g = lateGroups.get(i);
			g.computeBounds();
			double d = g.lateGroupConfidence();
			//g.dump();
			if (showGroups)
				showGroups = !showGroup(g, (i+1)+"/"+lateGroups.size());
			if (d > maxd && g.assocs.size() >= Stitcher.groupLateSizeThreshold && d > Stitcher.groupConfidenceThreshold)
			{
				maxd = d;
				max = g;
			}
		}
		if (max == null && force)
			for (int i=0;i<lateGroups.size();i++)
			{
				Group g = lateGroups.get(i);
				double d = g.lateGroupConfidence();
				if (d > maxd)
				{
					maxd = d;
					max = g;
				}
			}
				
		if (max != null)
			max.dump();
		if (max != null)
			res.addAll(max.assocs);
	}
	
	@SuppressWarnings("serial")
	public static boolean showGroup(final Group gr, String label)
	{
		//gr.dump();
		boolean [] skip = {false};
		JDialog dialog = new JDialog((Frame)null, label, true);
		dialog.add(new JPanel()
		{
			{setPreferredSize(new Dimension(1920, 1080));}
			@Override protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				
				Fragment f1 = gr.map.d1.fragment, f2 = gr.map.d2.fragment;
				int scale = 6;
				g.drawImage(f1.mini, 0, 0, f1.imagew/scale, f1.imageh/scale, null);
				int xoff = f1.imagew/scale;
				g.drawImage(f2.mini, xoff, 0, f2.imagew/scale, f2.imageh/scale, null);
				g.setColor(Color.red);
				for (int i=0;i<gr.assocs.size();i++)
				{
					int x1 = (int)(gr.assocs.get(i).p1.x/scale);
					int y1 = (int)(gr.assocs.get(i).p1.y/scale);
					int x2 = (int)(xoff+gr.assocs.get(i).p2.x/scale);
					int y2 = (int)(gr.assocs.get(i).p2.y/scale);
					g.drawLine(x1, y1, x2, y2);
				}
			}
		}, BorderLayout.CENTER);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
		buttons.add(new JButton("Confirm") {{addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
		{
			writeGroundTruthData(gr, 1);
			dialog.setVisible(false);
		}});}});
		buttons.add(new JButton("Deny") {{addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
		{
			writeGroundTruthData(gr, 0);
			dialog.setVisible(false);
		}});}});
		buttons.add(new JButton("Skip") {{addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
		{
			dialog.setVisible(false);
		}});}});
		buttons.add(new JButton("Skip all") {{addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
		{
			skip[0] = true;
			dialog.setVisible(false);
		}});}});
		dialog.add(buttons, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setVisible(true);
		return skip[0];
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void writeGroundTruthData(Group gr, double val)
	{
		List<double [][]> data = null;
		try {ObjectInputStream in = new ObjectInputStream(new FileInputStream(
			new File("C:\\Users\\aburn\\Documents\\work\\git\\DocExplore\\DocExplore\\src\\org\\interreg\\docexplore\\stitcher\\data")));
			data = (List)in.readObject(); in.close();} catch (Exception ex) {}
		if (data == null) data = new ArrayList<>();
		data.add(new double [][] {{gr.assocs.size(), gr.matchingFactor(), gr.edgenessFactor(), gr.areaFactor(), gr.complementFactor()}, {val}});
		try {ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
			new File("C:\\Users\\aburn\\Documents\\work\\git\\DocExplore\\DocExplore\\src\\org\\interreg\\docexplore\\stitcher\\data")));
			out.writeObject(data); out.close();} catch (Exception ex) {ex.printStackTrace();}
	}
}
