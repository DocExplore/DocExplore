package org.interreg.docexplore.management.process;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class ProcessDialog extends JDialog
{
	FilterBank bank;
	FilterPanel filterPanel;
	ViewPanel viewPanel;
	
	List<Page> pages;
	int currentPage;
	JPanel navPanel;
	
	public ProcessDialog()
	{
		super(JOptionPane.getRootFrame(), XMLResourceBundle.getBundledString("processTitleLabel"), true);
		
		this.pages = null;
		this.currentPage = 0;
		this.bank = new FilterBank();
		this.filterPanel = new FilterPanel(bank);
		this.viewPanel = new ViewPanel();
		
		viewPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		this.navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		fillNavigation();
		navPanel.setBorder(BorderFactory.createTitledBorder("Page"));
		JPanel outerNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		outerNavPanel.add(navPanel);
		viewPanel.add(outerNavPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		viewPanel.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("processPreviewLabel")) {public void actionPerformed(ActionEvent e)
			{viewPanel.updatePreview(filterPanel);}}));
		
		JButton processButton = new JButton(XMLResourceBundle.getBundledString("processProcessLabel"), 
			ImageUtils.getIcon("down-11x11.png"));
		processButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			JPopupMenu whichMenu = new JPopupMenu();
			
			JMenu allImages = new JMenu(XMLResourceBundle.getBundledString("processAllLabel"));
			allImages.add(new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("processInFilesLabel")) {public void actionPerformed(ActionEvent arg0)
			{
				
			}}));
			allImages.add(new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("processInMetadataLabel")) {public void actionPerformed(ActionEvent arg0)
				{try {MetaDataExport.exportAll(ProcessDialog.this);} catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}}}));
			
			JMenu currentImage = new JMenu(XMLResourceBundle.getBundledString("processCurrentLabel"));
			currentImage.add(new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("processInFileLabel")) {public void actionPerformed(ActionEvent arg0)
			{
				
			}}));
			currentImage.add(new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("processInMetadataLabel")) {public void actionPerformed(ActionEvent arg0)
				{try {MetaDataExport.export(ProcessDialog.this, currentPage);} catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}}}));
			
			whichMenu.add(allImages);
			whichMenu.add(currentImage);
			Component comp = (Component)e.getSource();
			whichMenu.show(comp, comp.getWidth()/2, comp.getHeight()/2);
		}});
		buttonPanel.add(processButton);
		
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCloseLabel")) {public void actionPerformed(ActionEvent e)
			{ProcessDialog.this.setVisible(false);}}));
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterPanel, viewPanel);
		add(splitPane, BorderLayout.CENTER);
		
		addComponentListener(new ComponentAdapter()
		{
			public void componentShown(ComponentEvent e) {componentResized(e);}
			public void componentResized(ComponentEvent e) {redoLayout();}
		});
		pack();
	}
	
	public void redoLayout()
	{
		Rectangle bounds = new Rectangle(getSize());//getBounds();
		viewPanel.unfilteredCanvas.setPreferredSize(new Dimension(bounds.width/3, 4*bounds.height/5));
		viewPanel.filteredCanvas.setPreferredSize(new Dimension(bounds.width/3, 4*bounds.height/5));
		invalidate();
		repaint();
	}
	
	void setCurrentPage(int i)
	{
		if (pages == null || i >= pages.size() || i == currentPage)
			return;
		
		if (currentPage >= 0 && currentPage < pages.size())
			pages.get(currentPage).unloadImage();
		try
		{
			viewPanel.unfiltered = i >= 0 ? pages.get(i).getImage().getImage() : null;
			currentPage = i;
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		fillNavigation();
		repaint();
	}
	
	public void setInput(List<Page> pages)
	{
		setCurrentPage(-1);
		this.pages = pages;
		currentPage = -1;
		setCurrentPage(0);
	}
	
	void fillNavigation()
	{
		navPanel.removeAll();
		if (pages == null)
			return;
		for (int i=0;i<pages.size();i++)
			navPanel.add(createNavigationButton(i));
		navPanel.validate();
	}
	
	JComponent createNavigationButton(final int page)
	{
		String label = "<u>"+(page+1)+"</u>";
		if (page == currentPage)
			label = "<b>"+label+"</b>";
		label = "<html>"+label+"</html>";
		
		JComponent res = null;
		if (page != currentPage)
		{
			final JLabel button = new JLabel(label);
			button.setForeground(Color.blue);
			button.addMouseListener(new MouseAdapter()
			{
				public void mouseEntered(MouseEvent e) {button.getTopLevelAncestor().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
				public void mouseExited(MouseEvent e) {button.getTopLevelAncestor().setCursor(Cursor.getDefaultCursor());}
				public void mouseClicked(MouseEvent e)
				{
					button.getTopLevelAncestor().setCursor(Cursor.getDefaultCursor());
					setCurrentPage(page);
				}
			});
			res = button;
		}
		else res = new JLabel(label);
		
		res.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		return res;
	}
}
