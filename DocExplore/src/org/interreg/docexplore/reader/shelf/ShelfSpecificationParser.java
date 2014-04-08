/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
