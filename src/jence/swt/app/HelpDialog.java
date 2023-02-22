/**
 * MIT LICENSE
 * 
 * Copyright © 2021 Jence, Ejaz Jamil.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
 * files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 * 
 */
package jence.swt.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;

/**
 * NDEF Write Dialog.
 * 
 * @author Ejaz Jamil
 * @version 1.0
 */
public class HelpDialog extends Composite {
	private Button btnClose;
	private Browser browser;

	public HelpDialog(Composite arg0, int arg1) {
		super(arg0, arg1);
		
		setLayout(new FillLayout());
		
		browser = new Browser(this, SWT.NONE);
		String html = "<style>" +
				"h1{font-family: Arial, Helvetica, sans-serif;font-size:200%;}" +
				"h2{font-family: Arial, Helvetica, sans-serif;font-size:150%;}" +
				"p{font-family: Arial, Helvetica, sans-serif;font-size:100%;}" +
				"</style>" +
				"<h1>Have you connected the device?</h2>" +
				"<p>If you believe that you " +
				"have connected the device to a USB port but the Refresh button does not " +
				"show the USB port, please do the following.</p>" +
				"<p>Remove the device then hit Refresh button. Note the name of the USB ports " +
				"displayed and count them. Now, connect the device and refresh. Do you see number " +
				"of USB ports has increased from before? If Yes, then you have to select " +
				"the new port that just appeared and Connect. if No, then it might be a driver " +
				"issue.</p>" +
				"<h2>Driver Independance</h2><p>Most latest OS versions, driver is not required. " +
				"Inserting the device is found as USB Serial device and a built-in driver will be used. " +
				"You need driver for older version of OS.</p>" +
				"<h2>Where are the Drivers</h2>" +
				"<p>J4210U uses two types of USB to UART chips:</p>" +
				"<p>CP210x chips: " +
				"<a href=\"https://www.silabs.com/developers/usb-to-uart-bridge-vcp-drivers?tab=downloads\">Download CP210x Drivers from here.</a></p>" +
				"<p>MCP2221 chips: <a href=\"https://www.microchip.com/en-us/product/MCP2221\">MCP2221 Drivers from here</a></p>";
//		browser.setUrl(html);
		browser.setText(html);
		
//		this.getShell().pack();
	}
	
	public static void show(Display display) {
		Shell shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		HelpDialog h = new HelpDialog(shell, SWT.NONE);
//		h.setLayout(layout);
//        h.setLayoutData(new GridData(GridData.FILL_BOTH));
        shell.setBounds(0, 0, 800, 600);
        UhfApp.center(shell);
        shell.open();
	}

}
