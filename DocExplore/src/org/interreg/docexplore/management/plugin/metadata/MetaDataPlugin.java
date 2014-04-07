package org.interreg.docexplore.management.plugin.metadata;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.interreg.docexplore.authoring.explorer.edit.InfoElement;
import org.interreg.docexplore.authoring.explorer.edit.MetaDataEditor;
import org.interreg.docexplore.authoring.preview.PreviewPanel;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.management.annotate.AnnotationEditor;
import org.interreg.docexplore.management.annotate.AnnotationPanel;
import org.interreg.docexplore.management.plugin.Plugin;
import org.interreg.docexplore.manuscript.MetaData;

public interface MetaDataPlugin extends Plugin
{
	public String getType();
	public AnnotationEditor createEditor(AnnotationPanel panel, MetaData annotation) throws DataLinkException;
	public JLabel createLabel(String keyName, MetaData annotation) throws DataLinkException;
	public InputStream createDefaultValue();
	public Collection<File> openFiles(boolean multiple);
	public InfoElement createInfoElement(MetaDataEditor editor, MetaData md, int width) throws DataLinkException;
	public boolean canPreview(Object object);
	public String getFileType();
	public Icon createIcon(Object object);
	public PreviewPanel getPreview(Object object, int mx, int my);
	public void exportMetaData(MetaData md, StringBuffer xml, File bookDir, int id);
}
