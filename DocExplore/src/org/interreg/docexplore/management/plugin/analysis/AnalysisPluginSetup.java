/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.plugin.analysis;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.management.plugin.PluginManager;

@SuppressWarnings("serial")
public class AnalysisPluginSetup extends JFrame
{
	public MainWindow win = null;
	PluginManager manager;
	//ImageInputPanel inputPanel;
	public final AnalysisTaskList taskList;
	//PluginParameterPanel paramPanel;
	
	public AnalysisPluginSetup(final PluginManager manager)
	{
		super(XMLResourceBundle.getBundledString("pluginAnalysisLabel"));
		
		this.manager = manager;
		
		setLayout(new BorderLayout(10, 10));
//		JPanel setupPanel = new JPanel(new GridLayout(0, 1, 10, 10));
//		
//		this.inputPanel = new ImageInputPanel();
//		inputPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("pluginInputLabel")));
//		setupPanel.add(inputPanel);
//		
//		paramPanel = new PluginParameterPanel();
//		paramPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("pluginParametersLabel")));
//		setupPanel.add(paramPanel);
//		
//		JPanel pluginPanel = new JPanel(new BorderLayout(10, 10));
//		pluginPanel.setBorder(BorderFactory.createTitledBorder("Plugin"));
//		
//		JPanel pluginChoicePanel = new JPanel(new GridLayout(1, 0, 10, 10));
//		
//		JPanel pluginListPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, true, SwingConstants.LEFT, SwingConstants.CENTER, true, true));
//		pluginListPanel.add(new JLabel(XMLResourceBundle.getBundledString("pluginAnalysisToolMessage")));
//		final JList pluginList = new JList(manager.analysisPlugins);
//		pluginList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		pluginListPanel.add(new JScrollPane(pluginList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
//		pluginChoicePanel.add(pluginListPanel);
//		
//		JPanel taskListPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, true, SwingConstants.LEFT, SwingConstants.CENTER, true, true));
//		taskListPanel.add(new JLabel(XMLResourceBundle.getBundledString("pluginAnalysisTaskMessage")));
//		final JList ptaskList = new JList(new DefaultListModel());
//		ptaskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		taskListPanel.add(new JScrollPane(ptaskList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
//		pluginChoicePanel.add(taskListPanel);
//		
//		pluginList.setCellRenderer(new DefaultListCellRenderer() {
//			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
//			{
//				return super.getListCellRendererComponent(list, ((AnalysisPlugin)value).getName(), index, isSelected, cellHasFocus);
//			}});
//		pluginList.addListSelectionListener(new ListSelectionListener()
//		{
//			public void valueChanged(ListSelectionEvent e)
//			{
//				if (e.getValueIsAdjusting())
//					return;
//				AnalysisPlugin plugin = (AnalysisPlugin)pluginList.getSelectedValue();
//				DefaultListModel model = (DefaultListModel)ptaskList.getModel();
//				model.clear();
//				if (plugin != null)
//					for (int i=0;i<plugin.getTasks().length;i++)
//						model.addElement(new Pair<Integer, String>(i, plugin.getTasks()[i]) {public String toString() {return second;}});
//				ptaskList.repaint();
//			}
//		});
//		pluginPanel.add(pluginChoicePanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
//		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("processProcessLabel"))
//		{
//			@SuppressWarnings({ "rawtypes", "unchecked" })
//			public void actionPerformed(ActionEvent arg0)
//			{
//				AnalysisPlugin plugin = (AnalysisPlugin)pluginList.getSelectedValue();
//				if (plugin == null)
//					return;
//				
//				Pair<Integer, String> ptask = (Pair<Integer, String>)ptaskList.getSelectedValue();
//				if (ptask == null)
//					return;
//				
//				AnalysisPluginTask task = new AnalysisPluginTask(AnalysisPluginSetup.this, plugin, ptask.first);
//				task.setInput(inputPanel.getIncludedImages());
//				task.setParams((Map)paramPanel.getParameterMap());
//				addTask(task);
//			}
//		}));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCloseLabel"))
			{public void actionPerformed(ActionEvent arg0) {setVisible(false);}}));
//		pluginPanel.add(buttonPanel, BorderLayout.SOUTH);
//		
//		
//		setupPanel.add(pluginPanel);
//		add(setupPanel, BorderLayout.CENTER);
		
		taskList = new AnalysisTaskList(this);
		taskList.setPreferredSize(new Dimension(480, 640));
		JPanel taskPanel = new JPanel(new BorderLayout());
		taskPanel.add(new JScrollPane(taskList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		taskPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("pluginTasksLabel")));
		add(taskPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		pack();
		//setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
	
	public List<AnalysisPluginTask> getTasks(Class<? extends AnalysisPlugin> clazz)
	{
		LinkedList<AnalysisPluginTask> res = new LinkedList<AnalysisPluginTask>();
		synchronized (taskList.tasks)
		{
			for (AnalysisPluginTask task : taskList.tasks.keySet())
				if (task.plugin.getClass() == clazz)
					res.add(task);
		}
		return res;
	}
	
	public void addTask(AnalysisPluginTask task)
	{
		taskList.addTask(task);
		task.start();
	}
	
	public void addAnalysisInput(BufferedImage image)
	{
//		inputPanel.addInput(image);
//		inputPanel.validate();
//		if (inputPanel.autoOpen.isSelected())
//			setVisible(true);
	}
	
	public static interface Listener
	{
		public void taskStarted(AnalysisPluginTask task);
		public void taskCompleted(AnalysisPluginTask task);
	}
	List<Listener> listeners = new LinkedList<Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	void notifyTaskStarted(AnalysisPluginTask task) {for (Listener listener : listeners) listener.taskStarted(task);}
	void notifyTaskCompleted(AnalysisPluginTask task) {for (Listener listener : listeners) listener.taskCompleted(task);}
}
