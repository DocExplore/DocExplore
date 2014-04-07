package org.interreg.docexplore.reader.net;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.interreg.docexplore.util.ByteUtils;

public class ReaderServerMonitor extends JPanel
{
	private static final long serialVersionUID = -1771995428428618487L;
	
	static class FileNode implements TreeNode
	{
		FileNode parent;
		File file;
		Vector<FileNode> children;
		
		FileNode(FileNode parent, File file)
		{
			this.parent = parent;
			this.file = file;
			this.children = new Vector<FileNode>();
			if (file.isDirectory())
				for (File child : file.listFiles())
					children.add(new FileNode(parent, child));
		}
		
		public Enumeration<Object> children()
		{
			return new Enumeration<Object>()
			{
				int index = 0;
				public boolean hasMoreElements() {return index < children.size();}
				public Object nextElement() {return children.get(index++);}
			};
		}
		public boolean getAllowsChildren() {return file.isDirectory();}
		public TreeNode getChildAt(int childIndex) {return children.get(childIndex);}
		public int getChildCount() {return children.size();}
		public int getIndex(TreeNode node) {return children.indexOf(node);}
		public TreeNode getParent() {return null;}
		public boolean isLeaf() {return !file.isDirectory();}
		public String toString() {return file.getName();}
	}
	
	static class RequestNode implements TreeNode
	{
		ConnectionNode parent;
		Request request;
		
		RequestNode(ConnectionNode parent, Request request)
		{
			this.request = request;
			this.parent = parent;
		}
		
		void update(DefaultTreeModel model)
		{
			model.reload(this);
		}
		
		public Enumeration<Object> children()
		{
			return new Enumeration<Object>()
			{
				public boolean hasMoreElements() {return false;}
				public Object nextElement() {return null;}
			};
		}
		public boolean getAllowsChildren() {return false;}
		public TreeNode getChildAt(int childIndex) {return null;}
		public int getChildCount() {return 0;}
		public int getIndex(TreeNode node) {return -1;}
		public TreeNode getParent() {return parent;}
		public boolean isLeaf() {return true;}
		public String toString() {return request.toString();}
	}
	
	static class ConnectionNode implements TreeNode
	{
		ServerNode parent;
		ServerTask connection;
		Vector<RequestNode> children;
		
		ConnectionNode(ServerNode parent, ServerTask connection)
		{
			this.parent = parent;
			this.connection = connection;
			this.children = new Vector<RequestNode>();
			synchronized (connection.requests)
			{
				for (Request request : connection.requests)
					children.add(new RequestNode(this, request));
			}
		}
		
		void update(DefaultTreeModel model)
		{
			boolean changed = false;
			synchronized (connection.requests)
			{
				for (Iterator<RequestNode> it = children.iterator();it.hasNext();)
				{
					RequestNode child = it.next();
					if (!connection.requests.contains(child.request))
						{it.remove(); changed = true;}
				}
				for (Request request : connection.requests)
				{
					boolean found = false;
					for (RequestNode child : children)
						if (child.request == request)
							{found = true; break;}
					if (!found)
						{children.add(new RequestNode(this, request)); changed = true;}
				}
			}
			if (changed)
				model.reload(this);
			else for (RequestNode child : children)
				child.update(model);
		}
		
		public Enumeration<Object> children()
		{
			return new Enumeration<Object>()
			{
				int index = 0;
				public boolean hasMoreElements() {return index < children.size();}
				public Object nextElement() {return children.get(index++);}
			};
		}
		public boolean getAllowsChildren() {return true;}
		public TreeNode getChildAt(int childIndex) {return children.get(childIndex);}
		public int getChildCount() {return children.size();}
		public int getIndex(TreeNode node) {return children.indexOf(node);}
		public TreeNode getParent() {return parent;}
		public boolean isLeaf() {return false;}
		public String toString() {return connection.toString();}
	}
	
	static class ServerNode implements TreeNode
	{
		ReaderServer server;
		Vector<ConnectionNode> children;
		
		ServerNode(ReaderServer server)
		{
			this.server = server;
			this.children = new Vector<ConnectionNode>();
			for (ServerTask connection : server.tasks)
				children.add(new ConnectionNode(this, connection));
		}
		
		void update(DefaultTreeModel model)
		{
			boolean changed = false;
			for (Iterator<ConnectionNode> it = children.iterator();it.hasNext();)
			{
				ConnectionNode child = it.next();
				if (!server.tasks.contains(child.connection))
					{it.remove(); changed = true;}
			}
			for (ServerTask connection : server.tasks)
			{
				boolean found = false;
				for (ConnectionNode child : children)
					if (child.connection == connection)
						{found = true; break;}
				if (!found)
					{children.add(new ConnectionNode(this, connection)); changed = true;}
			}
			if (changed)
				model.reload(this);
			else for (ConnectionNode child : children)
				child.update(model);
		}
		
		public Enumeration<Object> children()
		{
			return new Enumeration<Object>()
			{
				int index = 0;
				public boolean hasMoreElements() {return index < children.size();}
				public Object nextElement() {return children.get(index++);}
			};
		}
		public boolean getAllowsChildren() {return true;}
		public TreeNode getChildAt(int childIndex) {return children.get(childIndex);}
		public int getChildCount() {return children.size();}
		public int getIndex(TreeNode node) {return children.indexOf(node);}
		public TreeNode getParent() {return null;}
		public boolean isLeaf() {return false;}
		public String toString() {return server.toString();}
	}

	ReaderServer server;
	JTree resourceTree;
	JTree connectionTree;
	
	@SuppressWarnings("serial")
	public ReaderServerMonitor(final ReaderServer server)
	{
		super(new FlowLayout());
		
		this.server = server;
		
		this.resourceTree = new JTree(new DefaultTreeModel(new FileNode(null, server.baseDir)))
		{
			Color col1 = new Color(.85f, .85f, 1f);
			Color col2 = new Color(.9f, .9f, 1f);
			public void paintComponent(Graphics g)
			{
				int row = 0;
				for (int y=0;y<getHeight();y+=getRowHeight())
				{
					g.setColor(row%2 == 0 ? col1 : col2);
					g.fillRect(0, y, getWidth(), getRowHeight());
					row++;
				}
				super.paintComponent(g);
			}
		};
		resourceTree.setOpaque(false);
		resourceTree.setRowHeight(20);
		resourceTree.setTransferHandler(new TransferHandler(null)
		{
			public boolean canImport(TransferSupport support)
			{
				return support.getDataFlavors()[0].getMimeType().startsWith("application/x-java-file-list");
			}

			@SuppressWarnings("unchecked")
			public boolean importData(TransferSupport support)
			{
				try
				{
					List<Object> input = (List<Object>)support.getTransferable().getTransferData(support.getDataFlavors()[0]);
					File root;
					TreePath path = resourceTree.getSelectionPath();
					if (path == null)
						root = server.baseDir;
					else
					{
						FileNode node = (FileNode)path.getLastPathComponent();
						if (node.file.isDirectory())
							root = node.file;
						else root = node.file.getParentFile();
					}
					
					for (Object object : input)
					{
						File from = new File(object.toString());
						try {ByteUtils.copyFileRecursive(from, root);}
						catch (Exception e) {e.printStackTrace();}
					}
					
					((DefaultTreeModel)resourceTree.getModel()).reload();
				}
				catch (Exception e) {e.printStackTrace();}
				return true;
			}
			
		});
		resourceTree.setDropMode(DropMode.USE_SELECTION);
		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.setBorder(BorderFactory.createTitledBorder("Resources"));
		treePanel.add(resourceTree);
		treePanel.setPreferredSize(new Dimension(320, 480));
		add(treePanel);
		
		this.connectionTree = new JTree(new DefaultTreeModel(new ServerNode(server)))
		{
			Color col1 = new Color(.85f, .85f, 1f);
			Color col2 = new Color(.9f, .9f, 1f);
			public void paintComponent(Graphics g)
			{
				int row = 0;
				for (int y=0;y<getHeight();y+=getRowHeight())
				{
					g.setColor(row%2 == 0 ? col1 : col2);
					g.fillRect(0, y, getWidth(), getRowHeight());
					row++;
				}
				super.paintComponent(g);
			}
		};
		connectionTree.setOpaque(false);
		connectionTree.setRowHeight(20);
		JPanel connectionPanel = new JPanel(new BorderLayout());
		connectionPanel.setBorder(BorderFactory.createTitledBorder("Connections"));
		connectionPanel.add(connectionTree);
		connectionPanel.setPreferredSize(new Dimension(320, 480));
		connectionTree.setCellRenderer(new TreeCellRenderer()
		{
			public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
				JLabel res = new JLabel(value.toString());
				if (selected)
					res.setForeground(Color.white);
				return res;
			}
		});
		add(connectionPanel);
		new Thread() {public void run()
		{
			final DefaultTreeModel model = (DefaultTreeModel)connectionTree.getModel();
			while (server.running)
			{
				SwingUtilities.invokeLater(new Runnable() {public void run()
				{
					synchronized (server)
					{
						//Enumeration<TreePath> paths = connectionTree.getExpandedDescendants(new TreePath(model.getRoot()));
						((ServerNode)model.getRoot()).update(model);
						//model.reload();
						//while (paths.hasMoreElements())
						//	connectionTree.expandPath(paths.nextElement());
						connectionTree.repaint();
					}
				}});
				
				try {Thread.sleep(1000);}
				catch (Exception e) {}
			}
		}}.start();
	}
}
