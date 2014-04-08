/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.plugin;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.book.ROISpecification;
import org.interreg.docexplore.reader.book.ROISpecification.InfoElement;
import org.interreg.docexplore.reader.book.roi.OverlayElement;
import org.interreg.docexplore.reader.net.ReaderClient;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public interface ClientPlugin
{
	public void setHost(ReaderClient client, File jarFile, File dependencies);
	public Set<String> getHandledTypes();
	public ROISpecification.InfoElement buildInfoElement(Node element, NamedNodeMap atts, String baseUrl) throws Exception;
	public boolean buildOverlayElement(ReaderApp app, List<OverlayElement> elements, InfoElement infoElement, int width);
}
