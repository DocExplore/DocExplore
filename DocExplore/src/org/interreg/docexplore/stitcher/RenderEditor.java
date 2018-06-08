package org.interreg.docexplore.stitcher;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.interreg.docexplore.DocExplore;
import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.app.editors.ConfigurationEditor;
import org.interreg.docexplore.stitcher.Renderer.RenderMetrics;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.MemoryImageSource;

@SuppressWarnings("serial")
public class RenderEditor extends JPanel implements Renderer.Listener, ConfigurationEditor
{
	public static interface Listener
	{
		public boolean allowFileExports();
		public File getCurFile();
		public DocExploreDataLink getLink();
		
		public void onCancelRequest();
	}
	
	public final Listener listener;
	public final FragmentView fragmentView;
	
	public final RenderView view;
	Renderer renderer = new Renderer();
	
	int maxTileSize = 2048;
	JTextField maxTileSizeField;
	double kppu;
	JSlider kppuSlider;
	JTextField summary;
	File exportDir = new File(new File("").getAbsolutePath());
	JTextField exportDirField;
	JButton chooseButton;
	Renderer.RenderMetrics metrics = new Renderer.RenderMetrics();
	
	boolean rendering = false;
	float [][] rendered = null;
	List<MetaData> parts = null;
	public final JPanel tools;
	
	public RenderEditor(Listener listener, FragmentView fragmentView)
	{
		super(new BorderLayout());
		renderer.addListener(this);
		
		this.listener = listener;
		this.fragmentView = fragmentView;
		this.view = new RenderView(this);
		view.setView(fragmentView);
		
		requestFocusInWindow();
		add(view, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel(new GridLayout(0, 1));
		this.tools = new JPanel(new FlowLayout());
		tools.setOpaque(false);
		tools.add(new JLabel(Lang.s("stitcherMaxTileSize")));
		tools.add(maxTileSizeField = new JTextField(""+maxTileSize, 6));
		maxTileSizeField.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {updateFields();}});
		maxTileSizeField.addFocusListener(new FocusAdapter() {@Override public void focusLost(FocusEvent e) {updateFields();}});
		tools.add(new JLabel("   "));
		tools.add(new JLabel(Lang.s("stitcherResolution")));
		tools.add(kppuSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100));
		kppuSlider.setOpaque(false);
		kppuSlider.addChangeListener(new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {updateFields();}});
		tools.add(new JLabel("   "));
		tools.add(summary = new JTextField(12));
		summary.setEditable(false);
		summary.setBorder(null);
		summary.setOpaque(false);
		
		if (listener.allowFileExports())
		{
			bottomPanel.add(tools);
			JPanel dirs = new JPanel(new WrapLayout());
			dirs.add(new JLabel("Export folder"));
			dirs.add(exportDirField = new JTextField(40));
			exportDirField.setText(exportDir.getAbsolutePath());
			exportDirField.setEditable(false);
			this.chooseButton = new JButton("Choose");
			chooseButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
			{
				File dir = DocExplore.getFileDialogs().openFolder("Choose export folder", exportDir);
				if (dir != null)
				{
					exportDir = dir;
					exportDirField.setText(exportDir.getAbsolutePath());
				}
			}});
			dirs.add(chooseButton);
			bottomPanel.add(dirs);
		}
		
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
	boolean inited = false;
	public void init()
	{
		if (inited)
			return;
		inited = true;
		if (listener.allowFileExports())
			exportDir = 
				listener.getCurFile() != null ? new File(listener.getCurFile().getParentFile(), "render") :
				DocExploreTool.getImagesCategory().current() != null ? new File(DocExploreTool.getImagesCategory().current(), "render") :
				exportDir;
		updateFields();
	}
	
	public void updateFields()
	{
		try {maxTileSize = Math.max(16, Integer.parseInt(maxTileSizeField.getText()));}
		catch (Exception e) {maxTileSizeField.setText(""+maxTileSize);}
		kppu = .05+.95*kppuSlider.getValue()*1./kppuSlider.getMaximum();
		metrics.set(fragmentView.set, view.minx, view.miny, view.maxx, view.maxy, maxTileSize, kppu);
		summary.setText(metrics.w+"x"+metrics.h);
		if (exportDirField != null)
			exportDirField.setText(exportDir.getAbsolutePath());
		
		view.repaint();
	}
	
	public void render()
	{
		if (rendering)
			return;
		new Thread() {public void run()
		{
			renderer.render(fragmentView.set, "render", metrics, new float [1]);
		}}.start();
	}

	@Override public void onRenderStarted(RenderMetrics metrics)
	{
		rendered = new float [metrics.nw][metrics.nh];
		rendering = true;
		maxTileSizeField.setEnabled(false);
		kppuSlider.setEnabled(false);
		repaint();
		
		if (!listener.allowFileExports())
			parts = new ArrayList<>();
		else chooseButton.setEnabled(false);
	}

	@Override public void onImageProgressed(int i, int j, float f)
	{
		rendered[i][j] = f;
		repaint();
	}
	
	@Override public void onImageRendered(int i, int j, BufferedImage image)
	{
		rendered[i][j] = 1;
		repaint();
		
		try
		{
			if (!listener.allowFileExports())
			{
				DocExploreDataLink link = listener.getLink();
				MetaData part = new MetaData(link, link.partKey, MetaData.imageType, new MemoryImageSource(image).getFile());
				part.setMetaDataString(link.partPosKey, i+","+j);
				part.setMetaDataString(link.dimKey, image.getWidth()+","+image.getHeight());
				DocExploreDataLink.getImageMini(part);
				parts.add(part);
			}
			else ImageUtils.write(image, "PNG", new File(exportDir, "render_"+i+"_"+j+".png"));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}

	@Override public void onRenderEnded()
	{
		rendering = false;
		if (!listener.allowFileExports())
		{
			fragmentView.listener.onRenderEnded(parts);
		}
		else
		{
			((JFrame)getTopLevelAncestor()).getJMenuBar().setEnabled(true);
			maxTileSizeField.setEnabled(true);
			kppuSlider.setEnabled(true);
			chooseButton.setEnabled(true);
			repaint();
		}
	}
	
	@Override public Component getComponent() {return this;}
	@Override public void onActionRequest(String action, Object param)
	{
		if (action.equals("cancel"))
		{
			fragmentView.setView(view);
			listener.onCancelRequest();
		}
		else if (action.equals("render"))
			render();
		else if (action.equals("fit"))
			view.fitView(.3);
		else if (action.equals("fit-bounds"))
			view.fitBounds();
	}
	
	@Override public void onCloseRequest() {}
	@Override public void refresh() {repaint();}
	@Override public void setReadOnly(boolean b) {}
	@Override public boolean allowGoto() {return false;}
}
