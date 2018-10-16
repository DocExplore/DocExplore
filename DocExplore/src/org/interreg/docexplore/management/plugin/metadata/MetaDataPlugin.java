/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.plugin.metadata;

import java.io.File;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.interreg.docexplore.authoring.ExportOptions;
import org.interreg.docexplore.authoring.explorer.edit.InfoElement;
import org.interreg.docexplore.authoring.explorer.edit.MetaDataEditor;
import org.interreg.docexplore.authoring.preview.PreviewPanel;
import org.interreg.docexplore.authoring.rois.RegionSidePanel;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.management.annotate.AnnotationEditor;
import org.interreg.docexplore.management.annotate.MMTAnnotationPanel;
import org.interreg.docexplore.management.plugin.Plugin;
import org.interreg.docexplore.manuscript.MetaData;

/**
 * Base interface for metadata plugins
 */ 
public interface MetaDataPlugin extends Plugin
{
	//returns the type
	public String getType();
	public AnnotationEditor createEditor(MMTAnnotationPanel panel, MetaData annotation) throws DataLinkException;
	public JLabel createLabel(String keyName, MetaData annotation) throws DataLinkException;
	public Object createDefaultValue();
	public Collection<File> openFiles(boolean multiple);
	public InfoElement createInfoElement(MetaDataEditor editor, MetaData md, int width) throws DataLinkException;
	public org.interreg.docexplore.authoring.rois.InfoElement createInfoElement(RegionSidePanel editor, MetaData md, int width) throws DataLinkException;
	public boolean canPreview(Object object);
	public String getFileType();
	public Icon createIcon(Object object);
	public PreviewPanel getPreview(Object object, int mx, int my);
	public void setupExportOptions(ExportOptions options, int exportType);
	public void exportMetaData(MetaData md, StringBuffer xml, File bookDir, int id, ExportOptions options, int exportType);
}
