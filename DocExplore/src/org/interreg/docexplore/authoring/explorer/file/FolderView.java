package org.interreg.docexplore.authoring.explorer.file;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.authoring.explorer.Explorer;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.ViewItem;
import org.interreg.docexplore.authoring.preview.PreviewPanel;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class FolderView extends ExplorerView
{
	public FolderView(Explorer explorer)
	{
		super(explorer);
		
		addMouseListener(new MouseAdapter()
		{
			@Override public void mouseReleased(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON3)
					return;
				final Component comp = getComponentAt(e.getPoint());
				if (comp == null || !(comp instanceof ViewItem))
					return;
				final Point p = new Point(e.getPoint());
				SwingUtilities.convertPointToScreen(p, FolderView.this);
				
				new Thread() {public void run()
				{
					File file = (File)((ViewItem)comp).data.object;
					if (file.isDirectory())
						return;
					if (ImageUtils.isSupported(file.getName()))
					{
						final BufferedImage [] image = {null};
						GuiUtils.blockUntilComplete(new Runnable() {public void run()
						{
							try {image[0] = ImageUtils.read((File)((ViewItem)comp).data.object);}
							catch (Exception e) {e.printStackTrace();}
						}}, null);
						if (image[0] != null)
							PreviewPanel.previewImage(image[0], p.x, p.y);
					}
					else FolderView.this.explorer.tool.createPreview(file, p.x, p.y);
				}}.start();
			}
		});
		
		msg = XMLResourceBundle.getBundledString("helpFolderMsg");
	}

	public boolean canHandle(String path) throws Exception
	{
		if (path.equals(""))
			return false;
		return new File(path).isDirectory();
	}

	protected List<ViewItem> buildItemList(String path) throws Exception
	{
		List<ViewItem> res = new LinkedList<ViewItem>();
		File file = new File(path);
		if (!file.isDirectory())
			return res;
		File [] children = file.listFiles();
		if (children != null)
			for (File child : children)
		{
			String type = child.isDirectory() ? "Folder" : null;
			if (type == null && ImageUtils.isSupported(child.getName()))// && !explorer.tool.fileIsPreviewable(file))
				type = "Image";
			if (type == null)
				type = explorer.tool.getFileType(child);
			if (type != null)
				res.add(new ViewItem(child.getName(), type, child));
		}
		return res;
	}

	Icon folderIcon = ImageUtils.getIcon("folder-48x48.png");
	Icon fileIcon = ImageUtils.getIcon("image-file-48x48.png");
	protected Icon getIcon(Object object)
	{
		File file = (File)object;
		Icon icon = file.isDirectory() ? folderIcon : null;
		if (icon == null && ImageUtils.isSupported(file.getName()))
			icon = fileIcon;
		if (icon == null)
			icon = explorer.tool.getIcon(file);
		return icon;
	}

	protected String getPath(Object object)
	{
		try {return ((File)object).getCanonicalPath();}
		catch (Exception e) {return e.getMessage();}
	}
}
