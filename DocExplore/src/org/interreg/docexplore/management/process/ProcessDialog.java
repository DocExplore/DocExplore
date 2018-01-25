/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
import org.interreg.docexplore.internationalization.Lang;
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
		super(JOptionPane.getRootFrame(), Lang.s("processTitleLabel"), true);
		
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
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("processPreviewLabel")) {public void actionPerformed(ActionEvent e)
			{viewPanel.updatePreview(filterPanel);}}));
		
		JButton processButton = new JButton(Lang.s("processProcessLabel"), 
			ImageUtils.getIcon("down-11x11.png"));
		processButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			JPopupMenu whichMenu = new JPopupMenu();
			
			JMenu allImages = new JMenu(Lang.s("processAllLabel"));
			allImages.add(new JMenuItem(new AbstractAction(Lang.s("processInFilesLabel")) {public void actionPerformed(ActionEvent arg0)
			{
				
			}}));
			allImages.add(new JMenuItem(new AbstractAction(Lang.s("processInMetadataLabel")) {public void actionPerformed(ActionEvent arg0)
				{try {MetaDataExport.exportAll(ProcessDialog.this);} catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}}}));
			
			JMenu currentImage = new JMenu(Lang.s("processCurrentLabel"));
			currentImage.add(new JMenuItem(new AbstractAction(Lang.s("processInFileLabel")) {public void actionPerformed(ActionEvent arg0)
			{
				
			}}));
			currentImage.add(new JMenuItem(new AbstractAction(Lang.s("processInMetadataLabel")) {public void actionPerformed(ActionEvent arg0)
				{try {MetaDataExport.export(ProcessDialog.this, currentPage);} catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}}}));
			
			whichMenu.add(allImages);
			whichMenu.add(currentImage);
			Component comp = (Component)e.getSource();
			whichMenu.show(comp, comp.getWidth()/2, comp.getHeight()/2);
		}});
		buttonPanel.add(processButton);
		
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("generalCloseLabel")) {public void actionPerformed(ActionEvent e)
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
