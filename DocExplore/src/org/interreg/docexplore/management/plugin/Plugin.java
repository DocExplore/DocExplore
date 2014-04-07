package org.interreg.docexplore.management.plugin;

import java.io.File;

public interface Plugin
{
	public String getName();
	public void setHost(File jarFile, File dependencies);
}
