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
