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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.interreg.docexplore.stitcher.fast.Fast12;
import org.interreg.docexplore.stitcher.fast.FeaturePoint;
import org.interreg.docexplore.stitcher.sauvola.FastSauvola;

import de.lmu.ifi.dbs.jfeaturelib.LibProperties;
import de.lmu.ifi.dbs.jfeaturelib.features.SURF;
import ij.process.ColorProcessor;

public enum FeatureDetector
{
	Surf 
	{
		@Override public List<POI> computeFeatures(Fragment fragment, BufferedImage image) throws Exception
		{
			FastSauvola fs = new FastSauvola();
			fs.initialize(image);
			int [] res = fs.call();
			ColorProcessor cp = new ColorProcessor(FastSauvola.toImage(res, image));
			SURF desc = new SURF(4, 4, (float)(Stitcher.surfFeatureThreshold), 2, false, false, false, 1, false);
			desc.run(cp);
			List<double []> v = FeatureUtils.filterWeakestSurf(desc.getFeatures(), Stitcher.surfMaxFeaturesThreshold);
			List<POI> features = new ArrayList<POI>(v.size());
			for (int i=0;i<v.size();i++)
				features.add(new SurfPOI(fragment, v.get(i), features.size()));
			return features;
		}
	}, 
	Lbp
	{
//		private short gray(int rgb) {return (short)((ImageUtils.red(rgb)+ImageUtils.green(rgb)+ImageUtils.blue(rgb))/3);}
		@Override public List<POI> computeFeatures(Fragment fragment, BufferedImage image) throws Exception
		{
//			int [][] gray = new int [image.getHeight()][image.getWidth()];
//			for (int i=0;i<image.getWidth();i++)
//				for (int j=0;j<image.getHeight();j++)
//					gray[j][i] = gray(image.getRGB(i, j));
			FastSauvola fs = new FastSauvola();
			fs.initialize(image);
			int [] res = fs.call();
			int [][] gray = new int [image.getHeight()][image.getWidth()];
			for (int i=0;i<image.getWidth();i++)
				for (int j=0;j<image.getHeight();j++)
					gray[j][i] = res[j*image.getWidth()+i] == 0 ? 0 : 255;
//			List<FeaturePoint> points = CornerDetector.filterWeakest(
//				Fast12.detectWithNonMax(gray, image.getWidth(), image.getHeight(), 50, -1), image.getWidth(), image.getHeight(), 16);
			List<FeaturePoint> points = Fast12.detectWithNonMax(gray, image.getWidth(), image.getHeight(), 20, 3000);System.out.println(points.size());
			ColorProcessor proc = new ColorProcessor(image);
			LocalBinaryPatterns lbp = new LocalBinaryPatterns();
			LibProperties props = new LibProperties();
			props.setProperty(LibProperties.LBP_RADIUS, 16);
			props.setProperty(LibProperties.LBP_NUM_POINTS, 16);
			props.setProperty(LibProperties.LBP_CONSTANT, 0);
			props.setProperty(LibProperties.LBP_HISTOGRAM_SIZE, 64);
			props.setProperty(LibProperties.LBP_NEIGHBORHOOD_SIZE, 8);
			lbp.setProperties(props);
			lbp.setImageProcessor(proc);
			List<POI> features = new ArrayList<>(points.size());
			for (int i=0;i<points.size();i++)
				features.add(new LbpPOI(fragment, lbp.processPixel(points.get(i).x(), points.get(i).y()), features.size()));
			return features;
		}
	};
	
	public abstract List<POI> computeFeatures(Fragment fragment, BufferedImage full) throws Exception;
}
