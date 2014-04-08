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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class RTCompiler
{
	final File workingDirectory;
	final ClassLoader flycLoader;
	
	public RTCompiler(final File workingDirectory)
	{
		this.workingDirectory = workingDirectory;
		if (!workingDirectory.exists())
			workingDirectory.mkdirs();
		
		this.flycLoader = new ClassLoader(ClassLoader.getSystemClassLoader())
		{
			protected Class<?> findClass(String name) throws ClassNotFoundException
			{
				File fileDirectory = workingDirectory;
				String fileName = name;
				if (name.contains("."))
				{
					int index = name.lastIndexOf(".");
					fileDirectory = new File(workingDirectory, 
						name.substring(0, index).replace('.', '/'));
					fileName = name.substring(index+1);
				}
				File objectFile = new File (fileDirectory, fileName+".class");
				if (!objectFile.exists())
					throw new ClassNotFoundException(name);
				
				try
				{
					FileInputStream input = new FileInputStream(objectFile);
					byte [] buf = new byte [(int)(objectFile.length())];
					input.read(buf);
					input.close();
					return defineClass(name, buf, 0, buf.length);
				}
				catch (IOException e)
				{
					throw new ClassNotFoundException(name, e);
				}
			}
		};
	}
	public RTCompiler() {this(new File(".", "flyc-src"));}
	
	public String getClassName(String code)
	{
		int index = code.indexOf("class")+6;
		while (Character.isWhitespace(code.charAt(index)))
			index++;
		int length = 1;
		while (Character.isJavaIdentifierPart(code.charAt(index+length)))
			length++;
		return code.substring(index, index+length);
	}
	
	public String getClassPackage(String code)
	{
		int index = code.indexOf("package")+8;
		if (index < 8)
			return "";
		while (Character.isWhitespace(code.charAt(index)))
			index++;
		int endIndex = code.indexOf(";", index);
		return code.substring(index, endIndex).trim();
	}
	
	public File getPackageDirectory(String packidge)
	{
		packidge = packidge.replace('.', '/');
		File directory = new File(workingDirectory, packidge);
		if (!directory.exists())
			directory.mkdirs();
		return directory;
	}
	
	public Class<?> compile(String code) throws Exception
	{
		int index = code.indexOf("class")+6;
		while (Character.isWhitespace(code.charAt(index)))
			index++;
		int length = 1;
		while (Character.isJavaIdentifierPart(code.charAt(index+length)))
			length++;
		String name = code.substring(index, index+length);
		
		String packidge = getClassPackage(code);
		File fileDirectory = getPackageDirectory(packidge);
		File sourceFile = new File(fileDirectory, name+".java");
		FileWriter writer = new FileWriter(sourceFile);
		writer.write(code);
		writer.close();
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		String classPath = System.getProperty("java.class.path");
		if (compiler.run(null, output, output, 
			"-cp", classPath+";"+workingDirectory.getCanonicalPath(), 
			"-sourcepath", workingDirectory.getCanonicalPath(), 
			fileDirectory.getCanonicalPath()+"/"+name+".java") != 0)
				throw new Exception(new String(output.toByteArray()));
		
		return flycLoader.loadClass((packidge.equals("") ? "" : packidge+".")+name);
	}
	
	/*public static void main(String [] args)
	{
		try
		{
			Flyc flyc = new Flyc();
			flyc.compile(
				"package flyctest;" +
				"public class FlycTest implements flyc.FlycTestInterface {" +
					"public void go() {" +
						"System.out.println(\"woho!\");" +
					"}" +
				"}");
			Class<?> clazz = 
				flyc.compile(
					"public class FlycTest2 extends flyctest.FlycTest {" +
						"public void go() {" +
							"System.out.println(\"woho2!\");" +
						"}" +
					"}");
			FlycTestInterface fti = (FlycTestInterface)clazz.newInstance();
			fti.go();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}*/
}
