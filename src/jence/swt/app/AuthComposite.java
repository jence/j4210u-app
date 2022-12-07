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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * NDEF Write Dialog.
 * 
 * @author Ejaz Jamil
 * @version 1.0
 */
public class AuthComposite extends Composite {
	private Button btnWrite;
	private Label label_4;
	private Composite composite_;
	private int selection_;
	private Callback callback_;
	private Label lblKeyA;
	private Text asciiA_;
	private Label lblKeyB;
	private Text asciiB_;
	private Text hexA_;
	private Label lblHex;
	private Label lblAscii;
	private Text hexB_;
	private Label lblHex_1;
	private Label lblAscii_1;
	private Button btnSave;
	private Button btnLoad;
	private Button btnDefault_A;
	private Button btnDefault_B;
	private Map auth_ = new Properties();
	private Button btnDefaultKey;
	private Composite composite;
	private Label lblTrailerData;
	// private J4210U.KeyData key_ = new J4210U.KeyData(0);
	private Button btnUse;
	
	private static String PasswordCharacters = " ABCDEFGHIJKLMNOPQRSTUVWXYZ_-.@#";
	private Text passwordChars_;
	private byte[] epc_ = null;

	private String hex2ascii(String hex) {
		if (hex == null) {
			return null;
		}
		if (hex.length() % 2 != 0)
			hex = "0" + hex; // make the length even;
		long n = Long.parseLong(hex, 16);
		String z = "";
		int d = (int)(n & (long)0x03) + 3;
		// password length: 3->6, 2->5, 1->4, 0->3
//		if (d > 0)
//			d += 3;
		n >>= 2;
		for (int i = 0; i < d; i++) {
			long nn = (n >> (i*5));
			int cindex = (int)(nn & (long)0x1F);
			z += PasswordCharacters.charAt(cindex);
		}
		return z;
	}

	private String ascii2hex(String ascii) {
		long n = 0, d = 0;
		int N = ascii.length();
		if (ascii.length() > 6)
			N = 6;
		if (N < 3)
			d = 0;
		else
			d = N - 3;
		for (int i = 0; i < N; i++) {
			//short c = (byte) (ascii.charAt(i) & 0xFF);
			char c = ascii.charAt(i);
			int index = PasswordCharacters.indexOf(c);
			long nc = index << (5*i);
			//System.out.println("nc="+Long.toString(nc, 2));
			n |= nc;
		}

		n <<= 2;
		n |= d;
		String hex = String.format("%08X",n);
		//System.out.println(Long.toString(n, 2));
		//String ascii2 = hex2ascii(hex);
		//System.out.println(ascii2.trim());
		return hex;
	}

	private void saveFile() {
		FileDialog fd = new FileDialog(this.getShell(), SWT.SAVE);
		fd.setText("Save");
		fd.setFilterPath("");
		String[] filterExt = { "*.j4210u", "*.*" };
		fd.setFilterExtensions(filterExt);
		String filename = fd.open();
		// System.out.println(selected);
		String warning = " IMPORTANT: Keep this file in a safe "
				+ "place and do not\n distribute. The file contains sensitive password information.";
		try {
			auth_.clear();
			// auth_.put("type", UhfApp.driver_.type().name());
			auth_.put("password", hexA_.getText());
			auth_.put("killpass", hexB_.getText());
			((Properties) auth_).store(new FileOutputStream(filename), warning);
		} catch (Exception e) {
			e.printStackTrace();
			UhfApp.prompt(this.getShell(), e.getLocalizedMessage(),
					SWT.ICON_WARNING | SWT.OK);
		}
	}

	private Map loadFile() {
		FileDialog fd = new FileDialog(this.getShell(), SWT.OPEN);
		fd.setText("Open");
		fd.setFilterPath("");
		String[] filterExt = { "*.j4210u", "*.*" };
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();
		System.out.println(selected);
		Properties p = new Properties();
		try {
			p.clear();
			p.load(new FileInputStream(selected));
			String pass = p.getProperty("password");
			String killpass = p.getProperty("killpass");
			if (pass != null)
				asciiA_.setText(pass);
			if (killpass != null) 
				asciiB_.setText(killpass);
			return p;
		} catch (Exception e) {
			UhfApp.prompt(this.getShell(), e.getLocalizedMessage(),
					SWT.ICON_WARNING | SWT.OK);
		}
		return null;
	}

	public AuthComposite(Composite arg0, int arg1) {
		super(arg0, arg1);
		composite_ = this;
		composite_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		composite_.setLayout(new GridLayout(6, false));

		lblKeyA = new Label(this, SWT.NONE);
		lblKeyA.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblKeyA.setText("Password");

		lblAscii = new Label(this, SWT.NONE);
		lblAscii.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblAscii.setText("ASCII");

		asciiA_ = new Text(this, SWT.BORDER);
		asciiA_.setToolTipText("Type any ASCII character of length 6");
		asciiA_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		asciiA_.setTextLimit(6);
		UhfApp.specificKeyListener(asciiA_, PasswordCharacters);
		asciiA_.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				//String text = asciiA_.getText();
				//asciiA_.setText(text.toUpperCase());
				hexA_.setText(ascii2hex(asciiA_.getText()));
			}
		});

		lblHex = new Label(this, SWT.NONE);
		lblHex.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblHex.setText("Hex");

		hexA_ = new Text(this, SWT.BORDER);
		hexA_.setText("00000000");
		hexA_.setToolTipText("write an HEX character.");
		hexA_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		UhfApp.hexKeyListener(hexA_);
		hexA_.setTextLimit(8);
		hexA_.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
			}
		});

		btnDefault_A = new Button(this, SWT.NONE);
		btnDefault_A.setImage(SWTResourceManager.getImage(AuthComposite.class,
				"/jence/icon/default16.png"));
		btnDefault_A.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				hexA_.setText("00000000");
				asciiA_.setText("");
			}
		});
		btnDefault_A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));
		btnDefault_A.setText("Default");

		lblKeyB = new Label(this, SWT.NONE);
		lblKeyB.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblKeyB.setText("Kill Pass");

		lblAscii_1 = new Label(this, SWT.NONE);
		lblAscii_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblAscii_1.setText("ASCII");

		asciiB_ = new Text(this, SWT.BORDER);
		asciiB_.setToolTipText("Type any ASCII character of length 6");
		asciiB_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		asciiB_.setTextLimit(6);
		UhfApp.specificKeyListener(asciiB_, PasswordCharacters);
		asciiB_.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				//String text = asciiB_.getText();
				//asciiB_.setText(text.toUpperCase());
				hexB_.setText(ascii2hex(asciiB_.getText()));
			}
		});

		lblHex_1 = new Label(this, SWT.NONE);
		lblHex_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblHex_1.setText("Hex");

		hexB_ = new Text(this, SWT.BORDER);
		hexB_.setText("00000000");
		hexB_.setToolTipText("write an HEX character.");
		hexB_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		UhfApp.hexKeyListener(hexB_);
		hexB_.setTextLimit(8);
		hexB_.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
			}
		});

		btnDefault_B = new Button(this, SWT.NONE);
		btnDefault_B.setImage(SWTResourceManager.getImage(AuthComposite.class,
				"/jence/icon/default16.png"));
		btnDefault_B.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				hexB_.setText("00000000");
				asciiB_.setText("");
			}
		});
		btnDefault_B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 1));
		btnDefault_B.setText("Default");

		composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(5, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 6, 1));

		btnSave = new Button(composite, SWT.NONE);
		btnSave.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				saveFile();
			}
		});
		btnSave.setImage(SWTResourceManager.getImage(AuthComposite.class,
				"/jence/icon/save.png"));
		btnSave.setText("Save");

		btnLoad = new Button(composite, SWT.NONE);
		btnLoad.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Map map = loadFile();
				if (map != null) {
					auth_ = map;
					hexA_.setText(auth_.get("password").toString());
					hexB_.setText(auth_.get("killpass").toString());
				}
			}
		});
		btnLoad.setImage(SWTResourceManager.getImage(AuthComposite.class,
				"/jence/icon/load.png"));
		btnLoad.setText("Load");

		btnUse = new Button(composite, SWT.NONE);
		btnUse.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnUse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				useKey();
			}
		});
		btnUse.setToolTipText("Use these keys for all subsequent read/write operations.");
		btnUse.setImage(SWTResourceManager.getImage(AuthComposite.class,
				"/jence/icon/cardread.png"));
		btnUse.setText("Use");

		btnWrite = new Button(composite, SWT.NONE);
		btnWrite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnWrite.setEnabled(false);
		btnWrite.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				writeAuth(false);
			}
		});
		btnWrite.setImage(SWTResourceManager.getImage(AuthComposite.class,
				"/jence/icon/write.png"));
		btnWrite.setToolTipText("Write NDEF data.");
		btnWrite.setText("Write");

		btnDefaultKey = new Button(composite, SWT.NONE);
		btnDefaultKey.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnDefaultKey.setEnabled(false);
		btnDefaultKey
				.setToolTipText("This operation will remove the keys programed before and replace with the default key setting.");
		btnDefaultKey.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				writeAuth(true);
			}
		});
		btnDefaultKey.setImage(SWTResourceManager.getImage(AuthComposite.class,
				"/jence/icon/default.png"));
		btnDefaultKey.setText("Default");

		label_4 = new Label(this, SWT.BORDER | SWT.SEPARATOR | SWT.HORIZONTAL);
		label_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				6, 1));
		lblTrailerData = new Label(this, SWT.NONE);
		lblTrailerData.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblTrailerData.setText("Password Chars");
		
		passwordChars_ = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		passwordChars_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
		passwordChars_.setText(PasswordCharacters);

		this.getShell().pack();
	}

	private void updateAscii() {
		asciiA_.setText(hex2ascii(hexA_.getText()));
		asciiB_.setText(hex2ascii(hexB_.getText()));
	}

	public void callback(Callback runnable) {
		callback_ = runnable;
	}

	private void writeAuth(boolean defaultKwy) {
		if (epc_ == null) {
			UhfApp.prompt(this.getShell(), "Could not determine on which chip to operate. No EPC set.", SWT.OK | SWT.ICON_WARNING);
			return;
		}
		String warning = "You are about to change authentication by changing keys and access conditions.";
		warning += " This will apply to all the sectors of this tag.";
		warning += " Some access conditions may make the tag unreadable. Are you sure you want to perform this operation?";
		if (UhfApp.prompt(this.getShell(), warning, SWT.OK | SWT.ICON_WARNING
				| SWT.CANCEL) == SWT.CANCEL) {
			return;
		}

		int block;
		try {
			byte[] killpass = {0,0,0,0};
			byte[] pass = {0,0,0,0};
			if (defaultKwy) {
				UhfApp.driver_.setPassword(epc_, pass);
				UhfApp.driver_.setKillPassword(epc_, killpass);
			} else {
				pass = UhfApp.driver_.hex2bytes(hexA_.getText(), 4);
				UhfApp.driver_.setPassword(epc_, pass);
				killpass = UhfApp.driver_.hex2bytes(hexB_.getText(), 4);
				UhfApp.driver_.setKillPassword(epc_, killpass);
			}
			UhfApp.prompt(this.getShell(),
					"Successfully changed password.", SWT.OK);
		} catch (Exception e) {
			UhfApp.prompt(this.getShell(), e.getLocalizedMessage(), SWT.OK
					| SWT.ICON_WARNING);
		}
	}

	private void useKey() {
		if (UhfApp
				.prompt(this.getShell(),
						"Use have choosen to use this key for subsequent read and write. "
								+ "If your tag does not use this key, then you should write this key to the tag first before "
								+ "subsequent read or write. ", SWT.OK
								| SWT.CANCEL) == SWT.OK) {
			try {
				UhfAppComposite u = (UhfAppComposite)this.getShell().getParent().getChildren()[0];
				u.setPasswords(UhfApp.driver_.hex2bytes(hexA_.getText(), 4), UhfApp.driver_.hex2bytes(hexB_.getText(), 4));
				this.getShell().dispose();
			} catch (Exception e) {
				UhfApp.prompt(this.getShell(), e.getLocalizedMessage(), SWT.OK
						| SWT.ICON_WARNING);
			}
		}
	}
	
	public void setEPC(byte[] epc, byte[] password, byte[] killpass, boolean enableWrite) {
		epc_ = epc;
		String hexp = UhfApp.driver_.toHex(password);
		String hexk = UhfApp.driver_.toHex(killpass);
		asciiA_.setText(hex2ascii(hexp).trim());
		asciiB_.setText(hex2ascii(hexk).trim());
		btnWrite.setEnabled(enableWrite);
		btnDefaultKey.setEnabled(enableWrite);
	}
}
