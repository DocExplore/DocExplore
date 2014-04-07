package org.interreg.docexplore.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Calendar;

import org.interreg.docexplore.DocExploreTool;

public class ErrorHandler
{
	File errorLog;
	ErrorDialog dialog = new ErrorDialog();
	
	public ErrorHandler(File errorLog)
	{
		this.errorLog = errorLog;
	}
	public ErrorHandler() {this(new File(DocExploreTool.getHomeDir(), "error.log"));}
	
	public void submit(Throwable e) {submit(e, false);}
	public void submit(Throwable e, boolean silent)
	{
		e.printStackTrace();
		write(e);
		
		if (!silent)
		{
//			String msg = e.getMessage() == null || e.getMessage().trim().length() == 0 ? "Oops!" : e.getMessage();
//			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			dialog.show(e);
		}
	}
	
	public void write(Throwable e)
	{
		try
		{
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(errorLog, true));
			
			StringBuffer sb = new StringBuffer(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(Calendar.getInstance().getTime()));
			sb.append(" : ");
			
			Throwable t = e;
			while (t != null)
			{
				sb.append(t.getMessage()).append("\n");
				for (StackTraceElement elem : t.getStackTrace())
					sb.append("\t\t").append(elem.toString()).append("\n");
				
				t = t.getCause();
				if (t != null)
					sb.append("\tCaused by : ");
			}
			
			writer.write(sb.toString());
			writer.close();
		}
		catch (Exception ex) {ex.printStackTrace();}
	}
	
	public final static ErrorHandler defaultHandler = new ErrorHandler();
}
