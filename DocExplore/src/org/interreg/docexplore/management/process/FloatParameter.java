package org.interreg.docexplore.management.process;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FloatParameter implements FilterParameter
{
	String name;
	double lowerBound, upperBound, defaultValue;
	
	public FloatParameter(String name, double lowerBound, double upperBound, double defaultValue)
	{
		this.name = name;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.defaultValue = defaultValue;
	}
	public FloatParameter(String name, double lowerBound, double upperBound) {this(name, lowerBound, upperBound, (lowerBound+upperBound)/2);}
	
	final int mag = 1000;
	public JPanel createPanel()
	{
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new JLabel(getName()), 0);
		final JSlider slider = new JSlider(SwingConstants.HORIZONTAL, (int)(lowerBound*mag), (int)(upperBound*mag), (int)(defaultValue*mag));
		panel.add(slider, 1);
		final JTextField text = new JTextField(""+defaultValue, 8);
		text.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(text, 2);
		
		DocumentListener docListener = new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e) {updateSlider();}
			public void removeUpdate(DocumentEvent e) {updateSlider();}
			public void insertUpdate(DocumentEvent e) {updateSlider();}
			void updateSlider()
			{
				text.getDocument().removeDocumentListener(this);
				try {slider.setValue((int)(Double.parseDouble(text.getText())*mag));}
				catch (Exception e) {}
				text.getDocument().addDocumentListener(this);
			}
		};
		text.getDocument().addDocumentListener(docListener);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {text.setText(""+slider.getValue()*1./mag);}});
		return panel;
	}
	
	public Object getValue(JPanel panel)
	{
		try
		{
			double d =  Double.parseDouble(((JTextField)panel.getComponent(2)).getText());
			if (d > upperBound) d = upperBound;
			if (d < lowerBound) d = lowerBound;
			return d;
		}
		catch (Exception e) {}
		return ((JSlider)panel.getComponent(1)).getValue()*1./mag;
	}
	public double get(JPanel panel) {return (Double)getValue(panel);}
	
	public void setPanel(JPanel panel, Object value)
	{
		//((JSlider)panel.getComponent(1)).setValue((int)(mag*(Double)value));
		((JTextField)panel.getComponent(2)).setText(value.toString());
	}
	
	public String getName() {return name;}
}
