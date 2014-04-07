package org.interreg.docexplore.management.annotate;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.ExpandingItem;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.util.GuiUtils;

/**
 * Base class for all meta data editing panels.
 * @author Burnett
 * @param <Type> The type of meta data that can be edited.
 */
@SuppressWarnings("serial")
public abstract class AnnotationEditor extends ExpandingItem
{
	AnnotationPanel panel;
//	JTextArea tagArea;
	public MetaData annotation;
	
	public String keyName;
	JLabel keyLabel;
	
	public final boolean readOnly;
	protected boolean changed = false;
	
	public AnnotationEditor(AnnotationPanel panel, MetaData annotation) throws DataLinkException
	{
		super();
		
		setLayout(new BorderLayout());
		
		this.panel = panel;
		this.annotation = annotation;
//		this.tagArea = new JTextArea(3, 30);
		this.keyLabel = new JLabel();
		this.readOnly = panel.handler.win.getDocExploreLink().readOnlyKeys.contains(annotation.getKey());
		
		fillContractedState();
	}
	
	protected void fillContractedState()
	{
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		labelPanel.add(panel.handler.buildDisplayLabel(panel.document, annotation));
		labelPanel.setOpaque(false);
		add(labelPanel, BorderLayout.WEST);
		
		if (!readOnly)
		{
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.setOpaque(false);
			buttonPanel.add(new IconButton("remove-24x24.png", new ActionListener() {public void actionPerformed(ActionEvent e) 
				{panel.removeAnnotation(AnnotationEditor.this);}}));
			add(buttonPanel, BorderLayout.EAST);
		}
	}
	
	protected void fillExpandedState()
	{
		final JPanel buttonPanel = new JPanel(new FlowLayout());
		try
		{
			buttonPanel.setOpaque(false);
			if (!readOnly)
			{
				buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("transcriptEditorApplyButton"))
					{public void actionPerformed(ActionEvent e)
				{
					apply(); contract();
				}}));
			}
			buttonPanel.add(new JButton(new AbstractAction(readOnly ? XMLResourceBundle.getBundledString("generalCloseLabel") : 
				XMLResourceBundle.getBundledString("generalCancelLabel"))
					{public void actionPerformed(ActionEvent e)
			{
				cancel(); contract();
			}}));
			
			add(buttonPanel, BorderLayout.SOUTH);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		
		//by default, editors will write their contents when closed. 
		//Subclasses must override this behavior if handling large amounts of data (set 'changed' to true only if actual change)
		changed = true;
		
		buttonPanel.addAncestorListener(new AncestorListener()
		{
			public void ancestorRemoved(AncestorEvent e)
			{
				if (changed)
					apply();
				changed = false;
				SwingUtilities.invokeLater(new Runnable() {public void run()
				{
					GuiUtils.blockUntilComplete(new Runnable() {public void run() {disposeExpandedState();}}, panel.handler.win);
				}});
			}
			public void ancestorMoved(AncestorEvent arg0) {}
			public void ancestorAdded(AncestorEvent arg0) {}
		});
	}
	
	void apply()
	{
		try {changed = false; write();}
		catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	void cancel()
	{
		try {changed = false;}
		catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	
	public void disposeExpandedState() {}
	
	@Override public void expand()
	{
		super.expand();
		SwingUtilities.invokeLater(new Runnable() {public void run() {
			makeVisible(panel.scrollPane, getBounds().y, getBounds().height);}});
	}
	@Override public void contract()
	{
		super.contract();
		SwingUtilities.invokeLater(new Runnable() {public void run() {
			makeVisible(panel.scrollPane, getBounds().y, getBounds().height);}});
	}
	
	void makeVisible(JScrollPane scrollPane, int y, int h)
	{
		Rectangle s = scrollPane.getViewport().getViewRect();
		if (h > s.height || y < s.y)
			{scrollPane.getViewport().setViewPosition(new Point(0, y)); return;}
		if (y+h > s.y+s.height)
			{scrollPane.getViewport().setViewPosition(new Point(0, y+h-s.height)); return;}
	}
	
//	void fillTagsArea() throws DataLinkException {tagArea.setText(buildTagString(annotation));}

	String buildTagString(AnnotatedObject document) throws DataLinkException
	{
		StringBuilder tagString = new StringBuilder();
		for (MetaData tag : document.getMetaDataListForKey(panel.handler.link.tagKey))
			tagString.append(TagHolder.extractLocalizedTag(tag.getString(), Locale.getDefault().getLanguage())+", ");
		if (tagString.length() > 0)
			tagString.delete(tagString.length()-2, tagString.length());
		return tagString.toString();
	}
	
	/**
	 * Writes the content of the panel to a meta data object.
	 * @param object A meta data object.
	 */
	public final void write() throws DataLinkException
	{
		writeObject(annotation);
		//panel.resortList();
	}
	
	public abstract void writeObject(MetaData object) throws DataLinkException;
	
	protected JPanel buildKeyPanel() throws DataLinkException
	{
		final JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		this.keyName = annotation.getKey().getName();
		if (keyName == null)
			keyName = annotation.getKey().getName("");
		keyLabel.setText("<html><big>"+keyName+"</big></html>");
		keyPanel.add(keyLabel);
		if (!readOnly)
			keyPanel.add(new IconButton("pencil-24x24.png", "", new ActionListener() {public void actionPerformed(ActionEvent e)
			{
				try
				{
					MetaDataKey key = panel.handler.requestKey(keyName);
					if (key == null)
						return;
					keyName = key.getName();
					keyLabel.setText("<html><big>"+keyName+"</big></html>");
					keyPanel.validate();
					keyPanel.repaint();
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}}));
		keyPanel.setOpaque(false);
		
		return keyPanel;
	}
}
