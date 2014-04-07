package org.interreg.docexplore.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.interreg.docexplore.internationalization.XMLResourceBundle;

@SuppressWarnings("serial")
public class RecordingPanel extends JPanel
{
	public static interface RecordingListener
	{
		public void recordingEnded(byte [] data);
	}
	
	public static class SoundDevice
	{
		Mixer.Info minfo;
		Mixer mixer;
		
		public SoundDevice(Mixer.Info minfo, Mixer mixer)
		{
			this.minfo = minfo;
			this.mixer = mixer;
		}
		
		public String toString() {return minfo.getName();}
	}
	
	public static final AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
	
	DefaultComboBoxModel inputBoxModel, outputBoxModel;
	AtomicBoolean recording, playing, stopped;
	byte [] currentBuffer;
	
	JButton recordButton, playButton, stopButton;
	JSlider slider;
	JLabel status;
	
	List<RecordingListener> listeners;
	
	public RecordingPanel()
	{
		super(new BorderLayout());
		
		this.inputBoxModel = new DefaultComboBoxModel();
		this.outputBoxModel = new DefaultComboBoxModel();
		this.recording = new AtomicBoolean(false);
		this.playing = new AtomicBoolean(false);
		this.stopped = new AtomicBoolean(true);
		this.status = new JLabel(XMLResourceBundle.getBundledString("recordStoppedLabel"));
		this.slider = new JSlider(0, 100);
		this.listeners = new LinkedList<RecordingListener>();
		refreshDevices();
		
		JPanel boxPanel = new JPanel(new LooseGridLayout(0, 2, 5, 5, true, true, 
			SwingConstants.LEFT, SwingConstants.TOP, true, true));
		boxPanel.add(new JLabel("Input"));
		boxPanel.add(new JComboBox(inputBoxModel));
		boxPanel.add(new JLabel("Output"));
		boxPanel.add(new JComboBox(outputBoxModel));
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		recordButton = new IconButton("record-24x24.png");
		playButton = new IconButton("play-24x24.png");
		stopButton = new IconButton("stop-24x24.png");
		recordButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {record();}});
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {play();}});
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {stop();}});
		buttonPanel.add(recordButton);
		buttonPanel.add(playButton);
		buttonPanel.add(stopButton);
		
		slider.setEnabled(false);
		slider.setValue(0);
		
		status.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		status.setBackground(getBackground());
		
		add(boxPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.WEST);
		add(slider, BorderLayout.EAST);
		add(status, BorderLayout.SOUTH);
	}
	
	public void addRecordingListener(RecordingListener listener) {listeners.add(listener);}
	public void removeRecordingListener(RecordingListener listener) {listeners.remove(listener);}
	public void notifyListenersRecordingEnded(byte [] data)
	{
		for (RecordingListener listener : listeners)
			listener.recordingEnded(data);
	}
	
	public void refreshDevices()
	{
		inputBoxModel.removeAllElements();
		DataLine.Info dinfo = new DataLine.Info(TargetDataLine.class, format);
		for (Mixer.Info minfo : AudioSystem.getMixerInfo())
		{
			Mixer mixer = AudioSystem.getMixer(minfo);
			if (mixer.getTargetLineInfo(dinfo).length > 0)
				inputBoxModel.addElement(new SoundDevice(minfo, mixer));
		}
		
		outputBoxModel.removeAllElements();
		dinfo = new DataLine.Info(SourceDataLine.class, format);
		for (Mixer.Info minfo : AudioSystem.getMixerInfo())
		{
			Mixer mixer = AudioSystem.getMixer(minfo);
			if (mixer.getSourceLineInfo(dinfo).length > 0)
				outputBoxModel.addElement(new SoundDevice(minfo, mixer));
		}
	}
	
	public synchronized void stop(boolean wait)
	{
		stopped.set(true);
		if (wait) while (playing.get() || recording.get())
			try {Thread.sleep(200);}
			catch (InterruptedException e) {}
	}
	public void stop() {stop(false);}
	
	public synchronized void play()
	{
		if (!stopped.get() || recording.get() || playing.get())
			return;
		
		try
		{
			if (currentBuffer == null)
				return;
			SoundDevice device = (SoundDevice)outputBoxModel.getSelectedItem();
			if (device == null)
				return;
			AudioInputStream stream = new AudioInputStream(
				new ByteArrayInputStream(currentBuffer), 
				format, currentBuffer.length/(format.getSampleSizeInBits()/8));
			final Clip clip = AudioSystem.getClip(device.minfo);
			clip.open(stream);
			
			playing.set(true);
			stopped.set(false);
			status.setText("Playing...");
			new Thread()
			{
				public void run()
				{
					try
					{
						clip.start();
						while (!stopped.get() && clip.getFramePosition() < clip.getFrameLength())
						{
							try {Thread.sleep(200);}
							catch (InterruptedException e) {}
							slider.setValue(100*clip.getFramePosition()/clip.getFrameLength());
						}
					}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					
					clip.stop();
					clip.flush();
					playing.set(false);
					stopped.set(true);
					status.setText("Stopped");
					slider.setValue(0);
				}
			}.start();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), e.getClass().getName(), 
				JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public synchronized void record()
	{
		if (!stopped.get() || recording.get() || playing.get())
			return;
		try
		{
			SoundDevice device = (SoundDevice)inputBoxModel.getSelectedItem();
			if (device == null)
				return;
			final TargetDataLine line = AudioSystem.getTargetDataLine(format, device.minfo);
			line.open();
			
			final ByteArrayOutputStream out  = new ByteArrayOutputStream();
			final byte [] data = new byte[line.getBufferSize()];
			
			recording.set(true);
			stopped.set(false);
			currentBuffer = null;
			status.setText("Recording...");
			new Thread()
			{
				public void run()
				{
					try
					{
						line.start();
						while (!stopped.get())
						{
							int read = line.read(data, 0, data.length);
							out.write(data, 0, read);
						}
						
						currentBuffer = out.toByteArray();
					}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					
					line.stop();
					line.flush();
					recording.set(false);
					status.setText("Stopped");
					notifyListenersRecordingEnded(currentBuffer);
				}
			}.start();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), e.getClass().getName(), 
				JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if (!visible)
			stop();
	}
	
	public byte [] getBuffer() {return currentBuffer;}
	public synchronized void setBuffer(byte [] data)
	{
		if (!stopped.get() || recording.get() || playing.get())
			return;
		currentBuffer = data;
	}
}
