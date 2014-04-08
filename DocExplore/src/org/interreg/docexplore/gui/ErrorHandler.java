/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
