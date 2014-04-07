package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.authoring.BookImporter;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.actions.DeleteMetaDataAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public abstract class InfoElement extends JPanel
{
	MetaDataEditor editor;
	MetaData md;
	JPanel toolbar;
	
	static Color outline = new Color(.25f, 0, .05f);
	public InfoElement(final MetaDataEditor editor, final MetaData md)
	{
		super(new BorderLayout());
		setOpaque(false);
		
		this.editor = editor;
		this.md = md;
		
		toolbar = new JPanel(new LooseGridLayout(1, 0, 5, 5, true, false, SwingConstants.CENTER, SwingConstants.CENTER, true, false));
		toolbar.setBackground(Color.gray);
		//toolbar.setOpaque(false);

		try {toolbar.add(new JLabel(""+BookImporter.getRank(md)));}
		catch (Exception e) {e.printStackTrace();}
		toolbar.add(new JButton(new AbstractAction("", ImageUtils.getIcon("up-mono-24x24.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					int rank = BookImporter.getRank(md);
					if (rank == 0)
						return;
					editor.monitoredRank = rank-1;
					editor.pageEditor.view.explorer.tool.historyManager.doAction(new ChangeRankAction(editor, md, rank, rank-1));
				}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}) {{setPreferredSize(new Dimension(24, 24)); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setBackground(Color.black);}});
		toolbar.add(new JButton(new AbstractAction("", ImageUtils.getIcon("first-mono-24x24.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					int rank = BookImporter.getRank(md);
					if (rank == 0)
						return;
					editor.monitoredRank = 0;
					editor.pageEditor.view.explorer.tool.historyManager.doAction(new ChangeRankAction(editor, md, rank, 0));
				}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}) {{setPreferredSize(new Dimension(24, 24)); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setBackground(Color.black);}});
		toolbar.add(new JButton(new AbstractAction("", ImageUtils.getIcon("down-mono-24x24.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					int rank = BookImporter.getRank(md);
					if (rank == BookImporter.getHighestRank(editor.document))
						return;
					editor.monitoredRank = rank+1;
					editor.pageEditor.view.explorer.tool.historyManager.doAction(new ChangeRankAction(editor, md, rank, rank+1));
				}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}) {{setPreferredSize(new Dimension(24, 24)); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setBackground(Color.black);}});
		toolbar.add(new JButton(new AbstractAction("", ImageUtils.getIcon("last-mono-24x24.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					int rank = BookImporter.getRank(md);
					int max = BookImporter.getHighestRank(editor.document);
					if (rank == max)
						return;
					editor.monitoredRank = max;
					editor.pageEditor.view.explorer.tool.historyManager.doAction(new ChangeRankAction(editor, md, rank, max));
				}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}) {{setPreferredSize(new Dimension(24, 24)); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setBackground(Color.black);}});
		toolbar.add(new JButton(new AbstractAction("", ImageUtils.getIcon("remove-mono-24x24.png"))
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					final AnnotatedObject document = editor.document;
					final int rank = BookImporter.getRank(md);
					DeleteMetaDataAction action = editor.pageEditor.view.explorer.getActionProvider().deleteMetaData(document, md);
					editor.pageEditor.view.explorer.tool.historyManager.doAction(new WrappedAction(action)
					{
						public void doAction() throws Exception
						{
							BookImporter.remove(md, document);
							editor.reload();
						}
						public void undoAction() throws Exception
						{
							BookImporter.insert(md, document, rank);
							editor.reload();
						}
					});
				}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}) {{setPreferredSize(new Dimension(24, 24)); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setBackground(Color.black);}});
		
		addMouseListener(new MouseAdapter() {public void mouseReleased(MouseEvent e)
		{
			try {editor.monitoredRank = BookImporter.getRank(md);}
			catch (Exception ex) {ex.printStackTrace();}
		}});
		
		JPanel panel = new JPanel(new LooseGridLayout(0, 1, 0, 0, false, false, SwingConstants.LEFT, SwingConstants.CENTER, true, true));
		panel.setOpaque(false);
		//panel.setBackground(Color.black);
		add(panel, BorderLayout.CENTER);
		panel.add(inner);
		inner.setBorder(BorderFactory.createLineBorder(Color.white, 1));
		inner.setOpaque(false);
		setBorder(BorderFactory.createLineBorder(outline, 1));
		add(toolbar, BorderLayout.SOUTH);
	}
	
	JPanel inner = new JPanel(new BorderLayout());
	public Component add(Component comp)
	{
		inner.add(comp, BorderLayout.CENTER);
		return comp;
	}
	
	public void dispose() {}
	
	public abstract BufferedImage getPreview(int width, Color back);
}
