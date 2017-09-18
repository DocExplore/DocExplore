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

import java.util.Map;
import java.util.TreeMap;

import org.interreg.docexplore.internationalization.LocalizedContent;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.book.roi.Shape;
import org.interreg.docexplore.reader.plugin.ClientPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BookSpecificationParser
{
	public static interface ConfigLoadingListener
	{
		public void loadingProgressed(BookSpecificationParser config, float status);
		public void loadingCompleted(BookSpecificationParser config);
	}
	
	ReaderApp app;
	float progress;
	Map<String, ClientPlugin> infoTypePlugins = new TreeMap<String, ClientPlugin>();
	
	public BookSpecificationParser(ReaderApp app)
	{
		this.app = app;
		for (ClientPlugin plugin : app.client.plugins)
			for (String type : plugin.getHandledTypes())
				infoTypePlugins.put(type, plugin);
	}
	
	public BookSpecification load(Document config, String baseUrl) throws Exception
	{
		this.progress = 0;
		
		Node book = null;
		NodeList list = config.getChildNodes();
		for (int i=0;i<list.getLength();i++)
		{
			Node node = list.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if (!node.getNodeName().equals("Book"))
				throw new Exception("Book tag expected, found '"+node.getNodeName()+"'");
			book = node;
			break;
		}
		
		NamedNodeMap atts = book.getAttributes();
		
		String title = null;
		Node titleNode = atts.getNamedItem("title");
		if (titleNode != null)
			title = titleNode.getNodeValue();
		
//		Node authorNode = atts.getNamedItem("author");
//		if (authorNode == null)
//			this.author = null;
//		else this.author = authorNode.getNodeValue();
		
		Node pathNode = atts.getNamedItem("path");
		if (pathNode == null)
			throw new Exception("Missing path attribute in book tag!");
		String pagesUrl = baseUrl+"/"+pathNode.getNodeValue()+"/";
		
		String supportedLangs;
		Node langNode = atts.getNamedItem("lang");
		if (langNode == null)
			supportedLangs = "";//LocalizedContent.getDefaultLocale().getLanguage();
		else supportedLangs = langNode.getNodeValue();
		
		double ratio = 1.2;
		Node ratioNode = atts.getNamedItem("aspectRatio");
		if (ratioNode != null)
			ratio = Double.parseDouble(ratioNode.getNodeValue());
		
		boolean parchment = false;
		Node parchmentNode = atts.getNamedItem("parchment");
		if (parchmentNode != null)
		{
			String val = parchmentNode.getNodeValue().toLowerCase();
			parchment = val.equals("1") || val.equals("t") || val.equals("true") || val.equals("y") || val.equals("yes");
		}
		
		String miniUri = null;
		if (parchment)
		{
			Node miniNode = atts.getNamedItem("mini");
			if (miniNode == null)
				throw new Exception("Missing mini attribute in book tag for parchment!");
			miniUri = pagesUrl+"/"+miniNode.getNodeValue();
		}
		
		String coverUri = null;
		Node coverNode = atts.getNamedItem("cover");
		if (coverNode != null)
			coverUri = pagesUrl+"/"+coverNode.getNodeValue();
		
		String innerCoverUri = null;
		Node innerCoverNode = atts.getNamedItem("innerCover");
		if (innerCoverNode != null)
			innerCoverUri = pagesUrl+"/"+innerCoverNode.getNodeValue();
		
//		URL ambientSound = null;
//		Node ambientNode = atts.getNamedItem("ambient");
//		if (ambientNode != null)
//			ambientSound = new URL(pagesUrl+ambientNode.getNodeValue());
		
		BookSpecification bookSpecif = new BookSpecification(app, title, ratio);
		bookSpecif.parchment = parchment;
		bookSpecif.miniUri = miniUri;
		bookSpecif.coverUri = coverUri;
		bookSpecif.innerCoverUri = innerCoverUri;
		
		list = book.getChildNodes();
		int pageNum = 0;
		for (int i=0;i<list.getLength();i++)
		{
			progress = i*1f/list.getLength();
			
			Node pageNode = list.item(i);
			if (pageNode.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if (pageNode.getNodeName().equals("Page"))
			{
				atts = pageNode.getAttributes();
				
				LocalizedContent<String> imageUrls = new LocalizedContent<String>();
				LocalizedContent<String> transImageUrls = new LocalizedContent<String>();
				LocalizedContent<String> soundUrls = new LocalizedContent<String>();
				readPageTag(pageNode, imageUrls, transImageUrls, soundUrls, pagesUrl, supportedLangs);
				PageSpecification pageSpecif = new PageSpecification(app, pageNum++, imageUrls, transImageUrls, soundUrls);
				bookSpecif.addPage(pageSpecif);
				
				//LocalizedContent<String> guideText = new LocalizedContent<String>();
				//LocalizedContent<URL> guideSound = new LocalizedContent<URL>();
				
				NodeList childList = pageNode.getChildNodes();
				for (int j=0;j<childList.getLength();j++)
				{
					Node childNode = childList.item(j);
					if (childNode.getNodeType() != Node.ELEMENT_NODE)
						continue;
					if (childNode.getNodeName().equals("Translation"))
						readPageTag(childNode, imageUrls, transImageUrls, soundUrls, pagesUrl, supportedLangs);
					else if (childNode.getNodeName().equals("RegionOfInterest"))
					{
						atts = childNode.getAttributes();
						Node shapeNode = atts.getNamedItem("region");
						if (shapeNode == null)
							throw new Exception("Missing shape attribute in region of interest");
						ROISpecification region = new ROISpecification(buildShape(shapeNode.getNodeValue()));
						pageSpecif.addRegion(region);
						NodeList infoList = childNode.getChildNodes();
						for (int k=0;k<infoList.getLength();k++)
						{
							Node infoNode = infoList.item(k);
							if (infoNode.getNodeType() != Node.ELEMENT_NODE)
								continue;
							readInfoTag(infoNode, region, pagesUrl, supportedLangs);
						}
					}
					else if (childNode.getNodeName().equals("PlainText"))
					{
						
					}
					else throw new Exception("RegionOfInterest tag expected, found '"+childNode.getNodeName()+"'");
				}
			}
			//else throw new Exception("Page tag expected, found '"+pageNode.getNodeName()+"'");
		}
		bookSpecif.validate();
		
		progress = 1;
		return bookSpecif;
	}
	
	public <Type> void addContent(String lang, LocalizedContent<Type> bundle, Type value)
	{
		String [] langs = lang.split(",");
		for (int i=0;i<langs.length;i++)
			bundle.addContent(langs[i].trim(), value);
	}
	
	void readPageTag(Node pageNode, LocalizedContent<String> imageUrls, LocalizedContent<String> transImageUrls, 
		LocalizedContent<String> soundUrls, String baseUrl, String supportedLangs) throws Exception
	{
		NamedNodeMap atts = pageNode.getAttributes();
		
		Node imageNode = atts.getNamedItem("image");
		if (imageNode == null)
			imageNode = atts.getNamedItem("src");
		Node transImageNode = atts.getNamedItem("tsrc");
		if (imageNode == null)
			throw new Exception("Missing image attribute in "+
				pageNode.getNodeName()+" tag!");
		
		Node soundNode = atts.getNamedItem("sound");
		Node langNode = atts.getNamedItem("lang");
		
		String imageUrl = baseUrl+"/"+imageNode.getNodeValue();
		String transImageUrl = transImageNode == null ? null : baseUrl+"/"+transImageNode.getNodeValue();
		String soundUrl = soundNode == null ? null : baseUrl+"/"+soundNode.getNodeValue();
		String lang = langNode != null ? langNode.getNodeValue() : supportedLangs;
		
		addContent(lang, imageUrls, imageUrl);
		if (transImageUrl != null)
			addContent(lang, transImageUrls, transImageUrl);
		if (soundUrl != null)
			addContent(lang, soundUrls, soundUrl);
	}
	
	void readInfoTag(Node infoNode, ROISpecification region, String baseUrl, String supportedLangs) throws Exception
	{
		if (!infoNode.getNodeName().equals("Info"))
			throw new Exception("Info tag expected, found '"+
				infoNode.getNodeName()+"'");
		
		NamedNodeMap atts = infoNode.getAttributes();
		Node typeNode = atts.getNamedItem("type");
		if (typeNode == null)
			throw new Exception("Missing type attribute in info tag!");
		
		Node langNode = atts.getNamedItem("lang");
		String lang = langNode != null ? langNode.getNodeValue() : supportedLangs;
		String [] langs = lang.split(",");
		
		ClientPlugin handler = infoTypePlugins.get(typeNode.getNodeValue());
		if (handler != null)
		{
			for (int i=0;i<langs.length;i++)
				region.addCustom(langs[i].trim(), handler.buildInfoElement(infoNode, atts, baseUrl));
		}
		else if (typeNode.getNodeValue().equals("text"))
		{
			for (int i=0;i<langs.length;i++)
				region.addText(langs[i].trim(), infoNode.getTextContent().trim());
		}
		else if (typeNode.getNodeValue().equals("image"))
		{
			int width = -1, height = -1;
			Node widthNode = atts.getNamedItem("width");
			if (widthNode != null)
				width = Integer.parseInt(widthNode.getNodeValue());
			Node heightNode = atts.getNamedItem("height");
			if (heightNode != null)
				height = Integer.parseInt(heightNode.getNodeValue());
			Node imageSrcNode = atts.getNamedItem("src");
			if (imageSrcNode == null)
				throw new Exception("Missing src attribute in image info tag!");
			
			String uri = baseUrl+imageSrcNode.getNodeValue();
			for (int i=0;i<langs.length;i++)
				region.addImage(langs[i].trim(), uri, width, height);
		}
//		else if (typeNode.getNodeValue().equals("video"))
//		{
//			int width = -1, height = -1;
//			Node widthNode = atts.getNamedItem("width");
//			if (widthNode != null)
//				width = Integer.parseInt(widthNode.getNodeValue());
//			Node heightNode = atts.getNamedItem("height");
//			if (heightNode != null)
//				height = Integer.parseInt(heightNode.getNodeValue());
//			Node imageSrcNode = atts.getNamedItem("src");
//			if (imageSrcNode == null)
//				throw new Exception("Missing src attribute in video info tag!");
//			addImageInfo(lang, region, baseUrl+imageSrcNode.getNodeValue(), width, height);
//		}
//		else if (typeNode.getNodeValue().equals("sound"))
//		{
//			Node soundSrcNode = atts.getNamedItem("src");
//			if (soundSrcNode == null)
//				throw new Exception("Missing src attribute in sound info tag!");
//			
//			addSoundInfo(lang, region, new URL(baseUrl+soundSrcNode.getNodeValue()));
//		}
	}
	
	Shape buildShape(String coords)
	{
		String [] textCoords = coords.split(",");
		
		if (textCoords.length < 4)
			throw new RuntimeException("Not enough coordinates in "+coords);
		if (textCoords.length%2 > 0)
			throw new RuntimeException("Odd number of coordinates in "+coords);
		
		float [][] points = new float [textCoords.length == 4 ? 4 : textCoords.length/2][2];
		if (textCoords.length == 4)
		{
			float x1 = Float.parseFloat(textCoords[0].trim()), y1 = Float.parseFloat(textCoords[1].trim());
			float x2 = Float.parseFloat(textCoords[2].trim()), y2 = Float.parseFloat(textCoords[3].trim());
			points[0][0] = x1; points[0][1] = y1;
			points[1][0] = x2; points[1][1] = y1;
			points[2][0] = x2; points[2][1] = y2;
			points[3][0] = x1; points[3][1] = y2;
		}
		else for (int i=0;i<textCoords.length;i+=2)
		{
			points[i/2][0] = Float.parseFloat(textCoords[i].trim());
			points[i/2][1] = Float.parseFloat(textCoords[i+1].trim());
		}
		
		return new Shape(points);
	}
}
