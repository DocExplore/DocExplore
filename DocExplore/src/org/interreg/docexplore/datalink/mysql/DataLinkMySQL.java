/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.datalink.mysql;

import java.awt.Point;
import java.io.File;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.objects.BookData;
import org.interreg.docexplore.datalink.objects.MetaDataData;
import org.interreg.docexplore.datalink.objects.PageData;
import org.interreg.docexplore.datalink.objects.RegionData;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.actions.ActionProvider;
import org.interreg.docexplore.manuscript.actions.DataLinkActionProvider;
import org.interreg.docexplore.util.Pair;
import org.interreg.docexplore.util.StringUtils;

public class DataLinkMySQL implements DataLink
{
	DataLinkMySQLSource source;
	Connection con;
	
	DataLinkMySQL(DataLinkMySQLSource source) throws ClassNotFoundException, SQLException
	{
		this.con = source.createConnection();
		this.source = source;
	}
	
	public DataLinkSource getSource() {return source;}
	
	public void release()
	{
		try
		{
			con.close(); 
			source.link = null;
			con = null;
			source = null;
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public void setProperty(String name, Object value) {}
	public boolean hasProperty(String name) {return false;}
	public Object getProperty(String name) {return null;}
	
	List<Pair<Integer, Integer>> getObjectMetaDataIdsAndKeys(int objectId) throws DataLinkException
	{
		List<Pair<Integer, Integer>> ids = new LinkedList<Pair<Integer, Integer>>();
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT om.metadataId, m.keyId FROM objectmetadata om, metadata m " +
				"WHERE om.objectId="+objectId+" AND m.objectId=om.metadataId");
			
			while (rs.next())
				ids.add(new Pair<Integer, Integer>(rs.getInt(1), rs.getInt(2)));
			
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error loading object "+objectId+" metadata ids", e);
		}
		return ids;
	}
	
	public void addMetaDataToObject(int objectId, int bookId, int pageNum, int regionId, int metaDataId) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.execute("INSERT INTO objectmetadata VALUES ("+objectId+", "+metaDataId+")");
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error inserting metadata "+metaDataId+" to object "+objectId, e);
		}
	}
	
	public void removeMetaDataFromObject(int objectId, int bookId, int pageNum, int regionId, int metaDataId) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.execute("DELETE FROM objectmetadata WHERE objectId="+objectId+" AND metadataId="+metaDataId);
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error removing metadata "+metaDataId+" from object "+objectId, e);
		}
	}
	
	public List<Integer> getAllBookIds() throws DataLinkException
	{
		List<Integer> ids = new LinkedList<Integer>();
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT objectId FROM book");
			
			while (rs.next())
				ids.add(rs.getInt(1));
			
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error loading books", e);
		}
		return ids;
	}
	
	List<Integer> getBookPageNumbers(int id) throws DataLinkException
	{
		List<Integer> ids = new LinkedList<Integer>();
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT pageNum FROM page WHERE bookId="+id);
			
			while (rs.next())
				ids.add(rs.getInt(1));
			
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error loading book "+id+" page numbers", e);
		}
		return ids;
	}
	
	public BookData getBookData(int id) throws DataLinkException
	{
		BookData bookData = null;
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT name FROM book WHERE book.objectId="+id);
			if (!rs.next())
				throw new DataLinkException(this, "No such book id : "+id);
			String name = StringUtils.unescapeSpecialChars(rs.getString(1));
			st.close();
			
			bookData = new BookData(name, getBookPageNumbers(id), getObjectMetaDataIdsAndKeys(id));
			
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error loading book "+id, e);
		}
		return bookData;
	}
	public void setBookName(int id, String name) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("UPDATE book SET name='"+StringUtils.escapeSpecialChars(name)+"' WHERE objectId="+id);
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error setting name for book "+id, e);
		}
	}
	
	public MetaDataData getMetaDataData(int id) throws DataLinkException
	{
		MetaDataData metaDataData = null;
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT keyId, type FROM metadata WHERE objectId="+id);
			if (!rs.next())
				throw new DataLinkException(this, "No such metadata id : "+id);
			int keyId = rs.getInt(1);
			String type = rs.getString(2);
			st.close();
			
			metaDataData = new MetaDataData(keyId, type, getObjectMetaDataIdsAndKeys(id));
			
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error loading book "+id, e);
		}
		return metaDataData;
	}
	
	public void setMetaDataKey(int objectId, int keyId) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("UPDATE metadata SET keyId="+keyId+" WHERE objectId="+objectId);
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error setting metadata key to "+keyId+" for metadata "+objectId, e);
		}
	}
	
	List<Integer> getPageRegionIds(int pageId) throws DataLinkException
	{
		List<Integer> ids = new LinkedList<Integer>();
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT objectId FROM region WHERE pageId="+pageId);
			while (rs.next())
				ids.add(rs.getInt(1));
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error loading region ids from page "+pageId, e);
		}
		return ids;
	}
	
	public PageData getPageData(int bookId, int pageNum) throws DataLinkException
	{
		PageData pageData = null;
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT objectId FROM page WHERE bookId="+bookId+" AND pageNum="+pageNum);
			if (!rs.next())
				throw new DataLinkException(this, "No such page number "+pageNum+" in book "+bookId);
			int pageId = rs.getInt(1);
			st.close();
			
			pageData = new PageData(pageId, getPageRegionIds(pageId), getObjectMetaDataIdsAndKeys(pageId));
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error loading page number "+pageNum+" in book "+bookId, e);
		}
		return pageData;
	}
	
	public RegionData getRegionData(int regionId, int bookId, int pageNum) throws DataLinkException
	{
		RegionData regionData = null;
//		try
//		{
//			Statement st = con.createStatement();
//			ResultSet rs = st.executeQuery("SELECT pageId FROM region WHERE objectId="+regionId);
//			if (!rs.next())
//				throw new DataLinkException(this, "No such region id : "+regionId);
//			int pageId = rs.getInt(1);
//			st.close();
//			
			regionData = new RegionData(getRegionOutline(regionId, bookId, pageNum), getObjectMetaDataIdsAndKeys(regionId));
//		}
//		catch (SQLException e)
//		{
//			throw new DataLinkException(this, "Error loading region "+regionId, e);
//		}
		return regionData;
	}
	
	List<Point> getRegionOutline(int regionId, int bookId, int pageNum) throws DataLinkException
	{
		List<Point> outline = new LinkedList<Point>();
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT x, y, rank FROM regionoutline WHERE regionId="+regionId+" ORDER BY rank ASC");
			while (rs.next())
				outline.add(new Point(rs.getInt(1), rs.getInt(2)));
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error loading outline of region "+regionId, e);
		}
		return outline;
	}
	
	public void setRegionOutline(int regionId, int bookId, int pageNum, Point [] outline) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("DELETE FROM regionoutline WHERE regionId="+regionId);
			
			StringBuffer query = new StringBuffer("INSERT INTO regionoutline VALUES ");
			for (int i=0;i<outline.length;i++)
				query.append((i>0?",":"")+"("+regionId+", "+i+", "+outline[i].x+", "+outline[i].y+")");
			st.executeUpdate(query.toString());
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error writing outline of region "+regionId, e);
		}
	}
	
	public int addBook(String name) throws DataLinkException
	{
		int id;
		try
		{
			Statement st = con.createStatement();
			st.execute("INSERT INTO object VALUES ()", Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = st.getGeneratedKeys();
			rs.next();
			
			id = rs.getInt(1);
			st.execute("INSERT INTO book VALUES ("+id+", '"+StringUtils.escapeSpecialChars(name)+"')");
			
			st.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error inserting book "+name, e);
		}
		return id;
	}
	
	public int addPage(int bookId, int pageNum, InputStream data) throws DataLinkException
	{
		int id;
		try
		{
			Statement st = con.createStatement();
			st.execute("INSERT INTO object VALUES ()", Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = st.getGeneratedKeys();
			rs.next();
			id = rs.getInt(1);
			st.close();
			
			PreparedStatement pst = con.prepareStatement("INSERT INTO page VALUES ("+id+", "+bookId+", ?, "+pageNum+")");
			pst.setBinaryStream(1, data);
			pst.execute();
			pst.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error inserting page number "+pageNum+" in book "+bookId, e);
		}
		return id;
	}
	
	public int addRegion(int pageId, int bookId, int pageNum) throws DataLinkException
	{
		int id;
		try
		{
			Statement st = con.createStatement();
			st.execute("INSERT INTO object VALUES ()", Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = st.getGeneratedKeys();
			rs.next();
			id = rs.getInt(1);
			
			st.execute("INSERT INTO region VALUES ("+id+", "+pageId+")");
			st.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error inserting region in page "+pageId, e);
		}
		return id;
	}
	
	public void setPageImage(int pageId, int bookId, int pageNum, InputStream file) throws DataLinkException
	{
		try
		{
			PreparedStatement pst = con.prepareStatement("UPDATE page SET data=? WHERE objectId="+pageId);
			pst.setBinaryStream(1, file);
			pst.execute();
			pst.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error writing image "+file, e);
		}
	}
	
	public byte [] getPageImage(int pageId, int bookId, int pageNum) throws DataLinkException
	{
		byte [] image = null;
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT data FROM page WHERE objectId="+pageId);
			rs.next();
			
			image = rs.getBytes(1);
			
			st.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error reading image of page "+pageId, e);
		}
		return image;
	}
	
	public void increasePageNumbers(int bookId, int fromPageNum) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("UPDATE page SET pageNum=pageNum+1 WHERE bookId="+bookId+" AND pageNum>="+fromPageNum+" ORDER BY pageNum DESC");
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error increasing page numbers beyond "+fromPageNum+" in book "+bookId, e);
		}
	}
	
	public void decreasePageNumbers(int bookId, int fromPageNum) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("UPDATE page SET pageNum=pageNum-1 WHERE bookId="+bookId+" AND pageNum>="+fromPageNum+" ORDER BY pageNum ASC");
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error decreasing page numbers beyond "+fromPageNum+" in book "+bookId, e);
		}
	}
	
	public void movePage(int bookId, int from, int to) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("UPDATE page SET pageNum=0 WHERE bookId="+bookId+" AND pageNum="+from);
			//TODO: do decrease and increase should be at manuscript level
			decreasePageNumbers(bookId, from);
			increasePageNumbers(bookId, to);
			st.executeUpdate("UPDATE page SET pageNum="+to+" WHERE bookId="+bookId+" AND pageNum=0");
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error moving page from number "+from+" to "+to+" in book "+bookId, e);
		}
	}
	
	public void removeBook(int bookId) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("DELETE FROM object WHERE id="+bookId);
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error deleting book "+bookId, e);
		}
	}
	
	public void removePage(int bookId, int pageNum) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("DELETE FROM object WHERE id=(" +
				"SELECT objectId FROM page WHERE pageNum="+pageNum+" AND bookId="+bookId+")");
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error deleting page "+pageNum+" of book "+bookId, e);
		}
	}
	
	public void removeRegion(int regionId, int bookId, int pageNum) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("DELETE FROM object WHERE id="+regionId);
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error deleting region "+regionId, e);
		}
	}
	
	public String getMetaDataKeyName(int id, String language) throws DataLinkException
	{
		String res;
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT name FROM metadatakeyname WHERE " +
				"keyId="+id+" AND lang='"+language+"'");
			if (!rs.next())
				res = null;
			else res = rs.getString(1);
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error getting name of metadata key "+id+
				" ("+language+")", e);
		}
		return res;
	}
	
	public int getMetaDataKeyId(String name, String language) throws DataLinkException
	{
		int res;
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT keyId FROM metadatakeyname WHERE " +
				"name='"+name+"' AND lang='"+language+"'");
			if (!rs.next())
				res = -1;
			else res = rs.getInt(1);
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error getting metadata key '"+name+
				"' ("+language+")", e);
		}
		return res;
	}
	
	public List<Integer> getMetaDataKeyIds() throws DataLinkException
	{
		List<Integer> res = new Vector<Integer>();
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT DISTINCT keyId FROM metadata");
			while (rs.next())
				res.add(rs.getInt(1));
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error getting name of metadata keys", e);
		}
		return res;
	}
	
	public int addMetaDataKey() throws DataLinkException
	{
		int id;
		try
		{
			Statement st = con.createStatement();
			st.execute("INSERT INTO metadatakey VALUES ()", Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = st.getGeneratedKeys();
			rs.next();
			id = rs.getInt(1);
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error creating metadata key", e);
		}
		return id;
	}
	public void removeMetaDataKey(int keyId) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("DELETE FROM metadatakey WHERE id="+keyId);
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error deleting metadata key "+keyId, e);
		}
	}
	
	public void setMetaDataKeyName(int keyId, String name, String language) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.execute("REPLACE INTO metadatakeyname VALUES ("+keyId+", '"+name+"', '"+language+"')");
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, this+"."+con+" Error creating metadata key name "+name, e);
		}
	}
	
	public InputStream getMetaDataValue(int metaDataId) throws DataLinkException
	{
		InputStream res = null;
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT value FROM metadata WHERE objectId="+metaDataId);
			rs.next();
			
			Blob blob = rs.getBlob(1);
			res = blob.getBinaryStream();
			blob.free();
			
			st.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error reading metadata value "+metaDataId, e);
		}
		return res;
	}
	
	public File getMetaDataFile(int metaDataId) throws DataLinkException
	{
		return null;
	}
	
	public void setMetaDataValue(int metaDataId, InputStream stream) throws DataLinkException
	{
		try
		{
			PreparedStatement pst = con.prepareStatement("UPDATE metadata SET value=? WHERE objectId="+metaDataId);
			pst.setBinaryStream(1, stream);
			pst.execute();
			pst.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error writing metadata value "+metaDataId, e);
		}
	}
	
	public void removeMetaData(int objectId) throws DataLinkException
	{
		try
		{
			Statement st = con.createStatement();
			st.executeUpdate("DELETE FROM object WHERE id="+objectId);
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error deleting metadata "+objectId, e);
		}
	}
	
	public int addMetaData(int keyId, String type) throws DataLinkException
	{
		int id;
		try
		{
			Statement st = con.createStatement();
			st.execute("INSERT INTO object VALUES ()", Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = st.getGeneratedKeys();
			rs.next();
			id = rs.getInt(1);
			
			st.execute("INSERT INTO metadata VALUES ("+id+", "+keyId+", '"+type+"', null)");
			st.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error creating metadata", e);
		}
		return id;
	}
	
	public List<Integer> getAssociatedMetaDataIds(int keyId, int associatedKeyId) throws DataLinkException
	{
		List<Integer> res = new LinkedList<Integer>();
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT DISTINCT ass.objectId FROM metadata md, objectmetadata omd, metadata ass " +
				"WHERE md.keyId="+keyId+" AND md.objectId=omd.objectId AND omd.metadataId=ass.objectId AND ass.keyId="+associatedKeyId);
			while (rs.next())
				res.add(rs.getInt(1));
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error retrieving associated values with key "+associatedKeyId+" for key "+keyId, e);
		}
		return res;
	}
	
	public List<Integer> getMetaDataIds(int keyId, String type) throws DataLinkException
	{
		List<Integer> res = new LinkedList<Integer>();
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT DISTINCT md.objectId FROM metadata md " +
				"WHERE md.keyId="+keyId+(type != null ? " AND md.type='"+type+"'" : ""));
			while (rs.next())
				res.add(rs.getInt(1));
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error retrieving associated values for key "+keyId, e);
		}
		return res;
	}
	
	public List<Pair<Integer, String>> getMetaDataText(String id, Collection<Integer> keyIds) 
		throws DataLinkException
	{
		List<Pair<Integer, String>> res = new LinkedList<Pair<Integer, String>>();
		try
		{
			StringBuilder sb = new StringBuilder("SELECT m.keyId, m.value FROM metadata m, objectmetadata om WHERE om.objectId=").append(id)
				.append(" AND om.metadataId=m.objectId AND m.type='txt'");
			if (keyIds != null)
			{
				sb.append(" AND m.keyId IN (");
				boolean first = true;
				for (int keyId : keyIds)
				{
					if (!first) sb.append(",");
					first = false;
					sb.append(keyId);
				}
				sb.append(")");
			}
			
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sb.toString());
			while (rs.next())
				res.add(new Pair<Integer, String>(rs.getInt(1), rs.getString(2)));
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Error getting metadata text for "+id, e);
		}
		return res;
	}
	
	public Map<String, Double> search(int keyId, String value, Collection<String> _subSet, String objectType, double relevance) throws DataLinkException
	{
		Map<String, Double> res = new TreeMap<String, Double>();
		
		Collection<Integer> subSet = null;
		if (_subSet != null)
		{
			subSet = new Vector<Integer>(_subSet.size());
			for (String s : _subSet)
				subSet.add(Integer.parseInt(s));
		}
		
		try
		{
			String idLabel = "o."+(objectType == null ? "id" : "objectId");
			StringBuilder sb = new StringBuilder("SELECT ");
			sb.append(idLabel).append(", fuzzySearch('").append(value.toLowerCase()).append("', LOWER(CONVERT(m.value USING latin1)), 1) AS score \nFROM metadata m, objectmetadata om, ");
			if (objectType != null)
				sb.append(objectType);
			else sb.append("object");
			sb.append(" o \nWHERE ");
			if (subSet != null)
			{
				sb.append(idLabel).append(" IN (");
				boolean first = true;
				for (int id : subSet)
				{
					if (!first) sb.append(",");
					sb.append(id);
					first = false;
				}
				sb.append(")\nAND om.objectId=").append(idLabel).append(" AND ");
			}
			sb.append("om.objectId=").append(idLabel).append(" AND m.objectId=om.metadataId AND m.type='txt'");
			if (keyId >= 0)
				sb.append(" AND m.keyId=").append(keyId);
			sb.append(" \nORDER BY score DESC");
			//System.out.println(sb.toString());
			
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sb.toString());
			while (rs.next())
			{
				double score = Double.parseDouble(rs.getString(2));
				if (score < relevance)
					continue;
				res.put(""+rs.getInt(1), score);
			}
			st.close();
		}
		catch (SQLException e)
		{
			throw new DataLinkException(this, "Search error", e);
		}
		return res;
	}
	
	public String getBookTitle(int bookId) throws DataLinkException
	{
		String name = null;
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT name FROM book WHERE objectId="+bookId);
			rs.next();
			name = rs.getString(1);
			st.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error reading title of book "+bookId, e);
		}
		return name;
	}
	public Pair<Integer, Integer> getPageBookIdAndNumber(String pageId) throws DataLinkException
	{
		Pair<Integer, Integer> pair = null;
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT bookId, pageNum FROM page WHERE objectId="+pageId);
			rs.next();
			pair = new Pair<Integer, Integer>(rs.getInt(1), rs.getInt(2));
			st.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error reading info of page "+pageId, e);
		}
		return pair;
	}
	public String getRegionPageId(String regionId) throws DataLinkException
	{
		int id = -1;
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT pageId FROM region WHERE objectId="+regionId);
			rs.next();
			id = rs.getInt(1);
			st.close();
		}
		catch (Exception e)
		{
			throw new DataLinkException(this, "Error reading page id of region "+regionId, e);
		}
		return ""+id;
	}
	
	public ActionProvider getActionProvider(DocExploreDataLink link) {return new DataLinkActionProvider(link);}
	public boolean supportsHistory() {return false;}
}
