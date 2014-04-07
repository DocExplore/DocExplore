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
