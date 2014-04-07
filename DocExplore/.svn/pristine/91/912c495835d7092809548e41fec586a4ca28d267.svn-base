package org.interreg.docexplore.reader.shelf;

import java.awt.image.BufferedImage;

import org.interreg.docexplore.reader.gfx.StreamedImage;
import org.interreg.docexplore.reader.net.ReaderClient;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ShelfSpecificationParser
{
	public ShelfSpecification parse(Document doc, ReaderClient client) throws Exception
	{
		ShelfSpecification spec = new ShelfSpecification();
		
		Node index = null;
		NodeList list = doc.getChildNodes();
		for (int i=0;i<list.getLength();i++)
		{
			Node node = list.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if (node.getNodeName().equals("Index"))
				{index = node; break;}
		}
		if (index == null)
			throw new Exception("Missing Index tag in index file!");
		
		list = index.getChildNodes();
		for (int i=0;i<list.getLength();i++)
		{
			Node book = list.item(i);
			if (book.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if (!book.getNodeName().equals("Book"))
				continue;
			
			String title = null, uri = null, desc = book.getTextContent(), cover = null;
			NamedNodeMap atts = book.getAttributes();
			for (int j=0;j<atts.getLength();j++)
			{
				Node att = atts.item(j);
				if (att.getNodeType() != Node.ATTRIBUTE_NODE)
					continue;
			
				if (att.getNodeName().equals("title"))
					title = att.getNodeValue();
				else if (att.getNodeName().equals("src"))
					uri = att.getNodeValue();
			}
			if (title == null || uri == null)
				throw new Exception("Missing attributes in book tag!");
			cover = uri.substring(0, uri.length()-3)+"png";
			
			BufferedImage coverImage = null;
			if (cover != null)
			{
				try
				{
					StreamedImage image = client.getResource(StreamedImage.class, cover);
					image.waitUntilComplete();
					coverImage = image.image;
				}
				catch (Exception e) {e.printStackTrace();}
				if (coverImage == null)
					System.out.println("No shelf cover found for '"+title+"'");
			}
			spec.addEntry(title, desc, uri, coverImage);
		}
		
		return spec;
	}
}
