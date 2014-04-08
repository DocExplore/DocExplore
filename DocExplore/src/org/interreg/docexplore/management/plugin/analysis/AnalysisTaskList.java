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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.StringUtils;

@SuppressWarnings("serial")
public class AnalysisTaskList extends JPanel
{
	public static class TaskInfo extends JPanel implements AnalysisPluginTask.Listener
	{
		AnalysisTaskList taskList;
		AnalysisPluginTask task;
		JLabel status, time;
		JButton results;
		PluginResultFrame resultFrame = null;
		
		public TaskInfo(final AnalysisTaskList taskList, final AnalysisPluginTask task)
		{
			super(new BorderLayout());
			
			this.taskList = taskList;
			this.task = task;
			setBorder(BorderFactory.createLineBorder(Color.black, 2));
			
			JPanel infoPanel = new JPanel(new LooseGridLayout(0, 2, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.LEFT, false, false));
			infoPanel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("pluginTaskPluginLabel")+"</b></html>"));
			infoPanel.add(new JLabel(task.plugin.getName()));
			infoPanel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("pluginTaskTaskLabel")+"</b></html>"));
			infoPanel.add(new JLabel(task.plugin.getTasks()[task.task]));
			infoPanel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("pluginTaskStatusLabel")+"</b></html>"));
			infoPanel.add(status = new JLabel());
			infoPanel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("pluginTaskTimeLabel")+"</b></html>"));
			infoPanel.add(time = new JLabel());
			
			JPanel resultPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			results = new JButton(new AbstractAction(XMLResourceBundle.getBundledString("pluginResultLabel"))
			{
				public void actionPerformed(ActionEvent e)
				{
					if (task.success)
					{
						if (resultFrame == null)
						{
							resultFrame = new PluginResultFrame(task.setup.win);
							resultFrame.addResults(task.results);
							GuiUtils.centerOnScreen(resultFrame);
						}
						resultFrame.setVisible(true);
					}
					else JOptionPane.showMessageDialog(taskList, task.error.getMessage());
				}
			});
			results.setEnabled(false);
			resultPanel.add(results);
			resultPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCloseLabel"))
			{
				public void actionPerformed(ActionEvent e)
				{
					synchronized (taskList.tasks) {taskList.tasks.remove(task);}
					taskList.remove(TaskInfo.this);
					taskList.changed();
				}
			}));
			
			add(infoPanel, BorderLayout.CENTER);
			add(resultPanel, BorderLayout.SOUTH);
		}
		
		public void update()
		{
			String customStatus = task.customStatus;
			status.setText(!task.started ? XMLResourceBundle.getBundledString("pluginTaskPendingLabel") : 
				!task.completed ? (customStatus != null ? customStatus : XMLResourceBundle.getBundledString("pluginTaskRunningLabel")) : 
				!task.success ? XMLResourceBundle.getBundledString("pluginTaskCanceledLabel") : 
				XMLResourceBundle.getBundledString("pluginTaskCompletedLabel"));
			long timeVal = !task.started ? -1 : !task.completed ? System.currentTimeMillis()-task.startTime : task.endTime-task.startTime;
			time.setText(timeVal < 0 ? "" : StringUtils.formatMillis(timeVal, true));
			results.setEnabled(task.completed);
			status.repaint();
			time.repaint();
			results.repaint();
		}

		public void taskStarted(AnalysisPluginTask task) {}
		public void taskCompleted(AnalysisPluginTask task) {}
	}
	
	AnalysisPluginSetup setup;
	Map<AnalysisPluginTask, TaskInfo> tasks = new HashMap<AnalysisPluginTask, AnalysisTaskList.TaskInfo>();
	
	public AnalysisTaskList(AnalysisPluginSetup setup)
	{
		super(new LooseGridLayout(0, 1, 5, 5, true, true, SwingConstants.CENTER, SwingConstants.CENTER, true, false));
		
		this.setup = setup;
		
		new Thread() {public void run()
		{
			while (true)
			{
				for (TaskInfo info : tasks.values())
					info.update();
				try {Thread.sleep(1000);}
				catch (Exception e) {}
			}
		}}.start();
		
		setPreferredSize(new Dimension(500, 10));
	}
	
	void addTask(AnalysisPluginTask task)
	{
		TaskInfo info = new TaskInfo(this, task);
		synchronized (tasks) {tasks.put(task, info);}
		
		add(info);
		invalidate();
		validate();
		repaint();
	}
	
	void changed()
	{
		invalidate();
		validate();
		repaint();
	}
}
