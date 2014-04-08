/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


public class StringUtils
{
	public static String replaceIfNecessary(String s, String find, String replace)
	{
		if (s.indexOf(find) < 0)
			return s;
		return s.replace(find, replace);
	}
	
	public static String escapeSpecialChars(String s)
	{
		return replaceIfNecessary(
			replaceIfNecessary(
			replaceIfNecessary(
			replaceIfNecessary(
			replaceIfNecessary(s, 
				"&", "&amp;"), 
				"<", "&lt;"),
				">", "&gt;"),
				"\"", "&quot;"),
				"'", "&rsquo;");
	}
	
	public static String unescapeSpecialChars(String s)
	{
		return replaceIfNecessary(
			replaceIfNecessary(
			replaceIfNecessary(
			replaceIfNecessary(
			replaceIfNecessary(s, 
				"&lt;", "<"),
				"&gt;", ">"),
				"&quot;", "\""),
				"&rsquo;", "'"), 
				"&amp;", "&");
	}
	
	public static String buildHtmlStyledString(String text, String style)
	{
		boolean center = style.matches(".*text-align\\s*\\:\\s*center.*");
		boolean justify = style.matches(".*text-align\\s*\\:\\s*justify.*");
		if (center)
			style = style.replaceAll("text-align\\s*\\:\\s*center\\s*;{0,1}", "");
		if (justify)
			style = style.replaceAll("text-align\\s*\\:\\s*justify\\s*;{0,1}", "");
		
		return "<div style=\""+style+"\""+
			(center || justify ? (" align="+(justify ? "justify" : "center")) : "")+">"+
			StringUtils.unescapeSpecialChars(text)+
			"</div>";
	}
	
	public static String wordSubString(String s, int pos, int length, int margin) {return wordSubString(s, pos, length, margin, "", "");}
	public static String wordSubString(String s, int pos, int length, int margin, String insertBegin, String insertEnd)
	{
		int i0 = pos-margin;
		while (i0 > 0 && !Character.isWhitespace(s.charAt(i0-1)))
			i0--;
		if (i0 < 0)
			i0 = 0;
		
		int i1 = pos+length+margin;
		while (i1 < s.length()-1 && !Character.isWhitespace(s.charAt(i1+1)))
			i1++;
		if (i1 > s.length()-1)
			i1 = s.length()-1;
		
		return s.substring(i0, pos)+insertBegin+s.substring(pos, pos+length)+insertEnd+s.substring(pos+length, i1+1);
	}
	
	public static String breakDown(String s, int maxLineLength, int maxLines, String lineBreak)
	{
		List<String> lines = new Vector<String>();
		while (s.length() > maxLineLength && lines.size() < maxLines)
		{
			int ind = s.lastIndexOf(' ', maxLineLength);
			if (ind < 0)
				ind = maxLineLength;
			lines.add(s.substring(0, ind));
			s = s.substring(ind+1);
		}
		boolean overflow = s.length() > 0 && lines.size() == maxLines;
		if (s.length() > 0 && !overflow)
			lines.add(s);
		s = lines.get(0);
		for (int i=1;i<lines.size();i++)
			s += lineBreak+lines.get(i);
		if (overflow)
			s = s.substring(0, s.length()-3)+"...";
		return s;
	}
	
	public static String readFile(File file) throws IOException {return new String(ByteUtils.readFile(file));}
	public static String readFile(File file, String charset) throws IOException {return new String(ByteUtils.readFile(file), charset);}
	public static void writeFile(File file, String data, String charset) throws IOException {ByteUtils.writeFile(file, data.getBytes(charset));}
	public static void writeFile(File file, String data) throws IOException {ByteUtils.writeFile(file, data.getBytes());}
	
	public static String readStream(InputStream stream)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			while (true)
			{
				int read = stream.read();
				if (read < 0)
					break;
				sb.append((char)read);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}
	public static String readStream(Reader stream)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			while (true)
			{
				int read = stream.read();
				if (read < 0)
					break;
				sb.append((char)read);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}
	
	public static String escapeXmlChars(String s)
	{
		return s.replace("&", "&amp;").replace("<", "&lt;");
	}
	
	public static String formatMillis(long time, boolean showMillis)
	{
		long millis = time-(1000*(time/1000));
		time /= 1000;
		long seconds = time-(60*(time/60));
		time /= 60;
		long minutes = time-(60*(time/60));
		time /= 60;
		long hours = time;
		String res = hours+":"+(minutes < 10 ? "0" : "")+minutes+":"+(seconds < 10 ? "0" : "")+seconds;
		if (showMillis)
			res += "."+(millis < 100 ? "0" : "")+(millis < 10 ? "0" : "")+millis;
		return res;
	}
	
	public static String getTagContent(String xml, String tag)
	{
		int start = xml.indexOf("<"+tag+">");
		if (start < 0)
			return null;
		int end = xml.indexOf("</"+tag+">");
		if (end < 0)
			return null;
		return xml.substring(start+tag.length()+2, end);
	}
	public static String setTagContent(String xml, String tag, String content)
	{
		int start = xml.indexOf("<"+tag+">");
		if (start < 0)
			return null;
		int end = xml.indexOf("</"+tag+">");
		if (end < 0)
			return null;
		return xml.substring(0, start+tag.length()+2)+content+xml.substring(end);
	}
	public static List<String> getTagsContent(String xml, String tag)
	{
		int start = 0;
		List<String> contents = new LinkedList<String>();
		while (true)
		{
			start = xml.indexOf("<"+tag+">", start);
			if (start < 0)
				break;
			int end = xml.indexOf("</"+tag+">", start);
			if (end < 0)
				break;
			contents.add(xml.substring(start+tag.length()+2, end));
			start++;
		}
		return contents;
	}
	public static boolean getBoolean(String s)
	{
		if (s == null)
			return false;
		s = s.toLowerCase();
		return s.equals("1") || s.equals("y") || s.equals("yes") || s.equals("t") || s.equals("true");
	}
}
