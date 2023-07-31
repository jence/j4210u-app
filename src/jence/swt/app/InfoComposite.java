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

import jence.jni.J4210U;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * NDEF Write Dialog.
 * 
 * @author Ejaz Jamil
 * @version 1.0
 */
public class InfoComposite extends Composite {
	private Label lblStatus_;
	private Button btnWrite;
	private Label label_4;
	private Composite composite_;
	private Callback callback_ = null;
	private Text serialno_;
	private Text version_;
	private Text maxf_;
	private Text minf_;
	private Text antenna_;
	private Text comadr_;
	private Text readertype_;
	private Text protocol_;
	private Spinner power_;
	private Spinner scantime_;
	private Combo band_;
	private Button beepon_;
	private J4210U.ReaderInfo ri_ = null;
	private Combo baudrate_;
	
	private void status(String text) {
		UhfAppComposite composite = (UhfAppComposite)(getShell()).getChildren()[0];
		composite.status(text);
	}

	public InfoComposite(Composite arg0, int arg1) {
		super(arg0, arg1);
		composite_ = this;
		composite_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		composite_.setLayout(new GridLayout(4, false));
		
		Label lblSerialNo = new Label(this, SWT.NONE);
		lblSerialNo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSerialNo.setText("Serial No");
		
		serialno_ = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		serialno_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblVersion = new Label(this, SWT.NONE);
		lblVersion.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblVersion.setText("Version");
		
		version_ = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		version_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblMaxFreq = new Label(this, SWT.NONE);
		lblMaxFreq.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMaxFreq.setText("Max Freq");
		
		maxf_ = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		maxf_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblMinFreq = new Label(this, SWT.NONE);
		lblMinFreq.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMinFreq.setText("Min Freq");
		
		minf_ = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		minf_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblAntenna = new Label(this, SWT.NONE);
		lblAntenna.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAntenna.setText("Antenna");
		
		antenna_ = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		antenna_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblComadr = new Label(this, SWT.NONE);
		lblComadr.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblComadr.setText("ComAdr");
		
		comadr_ = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		comadr_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblReaderType = new Label(this, SWT.NONE);
		lblReaderType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblReaderType.setText("Reader Type");
		
		readertype_ = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		readertype_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblProtocol = new Label(this, SWT.NONE);
		lblProtocol.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblProtocol.setText("Protocol");
		
		protocol_ = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		protocol_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblBand = new Label(this, SWT.NONE);
		lblBand.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBand.setText("Band");
		
		band_ = new Combo(this, SWT.READ_ONLY);
		band_.setItems(new String[] {"Chinese", "USA", "Korean", "EU"});
		band_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		band_.select(1);
		
		Label lblBaudRate = new Label(this, SWT.NONE);
		lblBaudRate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBaudRate.setText("Baud Rate");
		
		baudrate_ = new Combo(this, SWT.READ_ONLY);
		baudrate_.setItems(new String[] {"57600", "115200"});
		baudrate_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		baudrate_.select(3);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		Label lblPower = new Label(this, SWT.NONE);
		lblPower.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPower.setText("Power (dB)");
		
		power_ = new Spinner(this, SWT.BORDER);
//		power_.setTextLimit(26);
		power_.setMaximum(26);
		power_.setSelection(26);
		power_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label lblScanTime = new Label(this, SWT.NONE);
		lblScanTime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblScanTime.setText("Scan Time (ms)");
		
		scantime_ = new Spinner(this, SWT.BORDER);
//		scantime_.setTextLimit(25500);
		scantime_.setIncrement(100);
		scantime_.setMaximum(25500);
		scantime_.setMinimum(300);
		scantime_.setSelection(300);
		scantime_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(this, SWT.NONE);
		
		beepon_ = new Button(this, SWT.CHECK);
		beepon_.setText("Beep ON");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		label_4 = new Label(this, SWT.BORDER | SWT.SEPARATOR | SWT.HORIZONTAL);
		label_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				4, 1));

		btnWrite = new Button(this, SWT.NONE);
		GridData gd_btnWrite = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
		gd_btnWrite.heightHint = 48;
		btnWrite.setLayoutData(gd_btnWrite);
		btnWrite.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				save();
			}
		});
		btnWrite.setImage(SWTResourceManager.getImage(InfoComposite.class,
				"/jence/icon/write.png"));
		btnWrite.setToolTipText("Write NDEF data.");
		btnWrite.setText("Write");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		lblStatus_ = new Label(this, SWT.NONE);
		lblStatus_.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 4, 1));

		//this.getShell().pack();
	}

	public void callback(Callback runnable) {
		callback_ = runnable;
	}

	private void save() {
		ri_.ScanTime = Integer.parseInt(scantime_.getText());
		ri_.Band = (byte)band_.getText().charAt(0);
		ri_.BeepOn = (beepon_.getSelection()) ? (byte)1 : 0;
		ri_.Power = (byte)(Integer.parseInt(power_.getText()) & 0xFF);
		// combo box index to internal baudrate mapping
		int oldBaudrate = ri_.BaudRate;
		int selectedBaudrate = Integer.parseInt(baudrate_.getText());
		ri_.BaudRate = selectedBaudrate;

		try {
			if (oldBaudrate != selectedBaudrate) {
				UhfApp.prompt(this.getShell(), "Your old baudrate "+oldBaudrate+" will be immediately changed to new baudrate "+
						selectedBaudrate+". So, you have to disconnect and then reconnect using the new baudrate.", SWT.ICON_WARNING);
			}
			UhfApp.driver_.saveSettings(ri_);
			refresh();
			status("Saved Successfully.");
		} catch (Exception e) {
			UhfApp.prompt(this.getShell(), e.getLocalizedMessage(), SWT.OK
					| SWT.ICON_WARNING);
		}
	}
	
	public void refresh() {
		try {
			ri_ = UhfApp.driver_.loadSettings();
			antenna_.setText(ri_.Antenna + "");
			baudrate_.setText(ri_.BaudRate + "");
			comadr_.setText(ri_.ComAdr + "");
			maxf_.setText(ri_.MaxFreq + "");
			minf_.setText(ri_.MinFreq + "");
			readertype_.setText(ri_.ReaderType + "");
			serialno_.setText(ri_.Serial + "");
			version_.setText(ri_.VersionInfo[0] + "." + ri_.VersionInfo[1]);
			protocol_.setText(ri_.Protocol + "");
			power_.setSelection(ri_.Power);
			scantime_.setSelection(ri_.ScanTime);
			char c = (char)ri_.Band;
			band_.setText(c + "");
			beepon_.setSelection(ri_.BeepOn==1 ? true : false);
		} catch (Exception e) {
			UhfApp.prompt(this.getShell(), e.getLocalizedMessage(), SWT.Close | SWT.ICON_WARNING);
		}
		
	}
}
