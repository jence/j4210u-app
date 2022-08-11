/**
 * MIT LICENSE
 * 
 * Copyright � 2021 Jence, Ejaz Jamil.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
 * files (the �Software�), to deal in the Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED �AS IS�, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 * 
 */
package jence.swt.app;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import jence.jni.J4210U;
import jence.jni.J4210U.ScanResult;
import jence.jni.J4210U.TagType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Spinner;

/**
 * The application front end.
 * 
 * 
 * @author Ejaz Jamil
 * @version 1.0
 * 
 */
public class UhfAppComposite extends Composite {
	private Table inventory_;
	private Text chip_;
	private Text total_;
	private Combo comboPorts_;
	private Button btnConnect_;
	private Button btnDisconnect_;
	private Button btnRefresh_;
	private Button btnScan_;
	private Label lblStatus_;
	private Label lblBlocks;
	private Text tidlen_;
	private Label lblBlockSizebyte;
	private Text usrlen_;
	private Group grpNdef;
	private Button btnNdefRead_;
	private Button btnNdefWrite_;
	private Button btnNdefClean_;
	private Label lblNdef;
	private Text pwdlen_;
	private Label lblAuth;
	private Text epclen_;
	private Composite composite_1;
	private Button btnAuth_;
	private TabFolder tabFolder;
	private TabItem tbtmRawData;
	private TabItem tbtmRaw;
	private Composite composite_2;
	private Table memory_;
	private Composite composite_4;
	private TableColumn tblclmnId;
	private Label lblNewLabel;
	private TableColumn tblclmnType_;
	private Listener listener_;
	private TabItem tbtmEmulate;
	private InfoComposite info_;
	private Composite composite_6;
	private TableColumn tblclmnIndex_1;
	private TableColumn tblclmnCount;
	private TableColumn tblclmnLen;
	private TableColumn tblclmnAnt;
	private TableColumn tblclmnTimes;
	private TableColumn tblclmnRssi;
	private Group grpFilter;
	private Label lblOffset;
	private Text offset_;
	private Label lblNewLabel_2;
	private Text filter_;
	private Button btnUseFilter;
	private TableColumn tblclmnWord;
	private TableColumn tblclmnWord_1;
	private TableColumn tblclmnWord_2;
	private TableColumn tblclmnWord_3;
	private TableItem tableItem;
	private TableItem tableItem_1;
	private Button btnAuthWrite_;

//	private byte[] epc_ = null;
	private byte[] password_ = { 0, 0, 0, 0 };
	private byte[] killpass_ = { 0, 0, 0, 0 };
	private boolean scanning_ = false;
	private Timer timer_ = null;

	public static final String DOWNLOAD_PAGE = "https://jence.com/web/index.php?route=product/product&path=69_25_225&product_id=792";
	public static final String LATEST_VERSION_PAGE = "http://jence.com/downloads/version.properties";
	public static final String[] AUTODETECTED_CHIPS = {
			TagType.HIGGS_3.toString(), TagType.HIGGS_4.name(),
			TagType.HIGGS_EC.name(), TagType.IMPINJ_M730.name(),
			TagType.IMPINJ_M750.name(), TagType.MONZA_4D.name(),
			TagType.MONZA_4E.name(), TagType.MONZA_4I.name(),
			TagType.MONZA_4QT.name(), TagType.MONZA_R6.name(),
			TagType.MONZA_R6P.name(), TagType.UCODE_8.name() };
	private Text epclen2_;
	private Text pwdlen2_;
	private Text usrlen2_;
	private TabItem tbtmGpio;
	private Composite composite_5;
	private Button btnGetGpInput;
	private Text in1_;
	private Text in2_;
	private Button btnSetGpOutput;
	private Label lblOut;
	private Text out1_;
	private Label lblOut_1;
	private Text out2_;
	private Label lblIn;
	private Label lblNewLabel_3;
	private Button btnScanOnTrigger_;
	private Label lblSupportedChips_;
	private Text tidlen2_;
	private Text total2_;
	private Label lblLibraryVersion;
	private Label lblLibVersion_;
	private Button btnExists;
	private boolean merge_ = false;
	private Button btnMerge;
	private Button btnClear;
	private Spinner comboIterations_;
	private Group grpIterationsPerScan;
	private Composite composite_7;
	private Combo comboBaudrate_;
	private Label lblBaud;

	private int prompt(String text, int style) {
		return UhfApp.prompt(this.getShell(), text, style);
	}

	public void status(String text) {
		lblStatus_.setText(text);
	}

	private void syncstatus(final String text) {
		this.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				status(text);
			}
		});
	}

	private void setEnabled(boolean state, Control... w) {
		for (int i = 0; i < w.length; i++) {
			w[i].setEnabled(state);
		}
	}

	private void createEditableTable(final Table table) {
		final Color COLOR_ORANGE = Display.getDefault().getSystemColor(
				SWT.COLOR_GRAY);

		FontData fd = table.getFont().getFontData()[0];
		fd.setStyle(SWT.BOLD);
		final Font FONT_BOLD = new Font(Display.getDefault(), fd);
		
		final TableEditor editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		if (listener_ != null)
			table.removeListener(SWT.MouseDown, listener_);
		table.addListener(SWT.MouseDown, listener_ = new Listener() {
			public void handleEvent(Event event) {
				Rectangle clientArea = table.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = table.getTopIndex();
				while (index < table.getItemCount()) {
					boolean visible = false;
					final TableItem item = table.getItem(index);
					for (int i = 0; i < table.getColumnCount(); i++) {
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)) {
							final int column = i;
							if (column < 1)
								continue; // thse are read only type
							// do not allow to edit TID
							if (index == 1)
								continue;
							final Text text = new Text(table, SWT.CENTER);
							text.setBackground(COLOR_ORANGE);
							text.setTextLimit(4);
							UhfApp.hexKeyListener(text);
							// text.setFont(FONT_BOLD);
							Listener textListener = new Listener() {
								public void handleEvent(final Event e) {
									switch (e.type) {
									case SWT.FocusOut:
										String oldText = item.getText(column);
										String newText = text.getText();
										if (newText.length() == 0) {
											newText = oldText;
										}
										newText = "0000" + newText;
										newText = newText.substring(newText
												.length() - 4);
										item.setText(column, newText);
										if (!oldText.equalsIgnoreCase(newText)) {
											item.setData(column + "",
													oldText.toUpperCase());
											Font ft = item.getFont();
											item.setFont(column, FONT_BOLD);
										}
										text.dispose();
										break;
									case SWT.Traverse:
										switch (e.detail) {
										case SWT.TRAVERSE_RETURN:
											item.setText(column, text.getText());
											// FALL THROUGH
										case SWT.TRAVERSE_ESCAPE:
											text.dispose();
											e.doit = false;
										}
										break;
									}
								}
							};
							text.addListener(SWT.FocusOut, textListener);
							text.addListener(SWT.Traverse, textListener);
							editor.setEditor(text, item, i);
							text.setText(item.getText(i));
							text.selectAll();
							text.setFocus();
							return;
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible)
						return;
					index++;
				}
			}
		});
	}

	public UhfAppComposite(Composite arg0, int arg1) {
		super(arg0, arg1);
		Composite composite = this;
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		composite.setLayout(new GridLayout(3, false));
		getShell().addListener(SWT.Close, new Listener(){
			@Override
			public void handleEvent(Event e) {
				try {
					// close the port if the port is open
					UhfApp.driver_.close();
				} catch (Exception e1) {
				}
				e.doit = true;
			}});


		composite_1 = new Composite(this, SWT.NONE);
		composite_1.setLayout(new GridLayout(6, false));
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1);
		gd_composite_1.heightHint = 69;
		composite_1.setLayoutData(gd_composite_1);
		
		composite_7 = new Composite(composite_1, SWT.NONE);
		GridData gd_composite_7 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite_7.widthHint = 155;
		composite_7.setLayoutData(gd_composite_7);
		composite_7.setLayout(new GridLayout(2, false));
		
				Label lblPort = new Label(composite_7, SWT.NONE);
				lblPort.setImage(SWTResourceManager.getImage(UhfAppComposite.class,
						"/jence/icon/usb.png"));
				lblPort.setText("Port");

		comboPorts_ = new Combo(composite_7, SWT.READ_ONLY);
		comboPorts_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		lblBaud = new Label(composite_7, SWT.NONE);
		lblBaud.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBaud.setText("Baud");
		
		comboBaudrate_ = new Combo(composite_7, SWT.READ_ONLY);
		comboBaudrate_.setItems(new String[] {"9600", "19200", "38400", "57600", "115200"});
		comboBaudrate_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboBaudrate_.select(3);

		btnRefresh_ = new Button(composite_1, SWT.NONE);
		btnRefresh_.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		btnRefresh_.setImage(SWTResourceManager.getImage(UhfAppComposite.class,
				"/jence/icon/usb.png"));
		btnRefresh_.setToolTipText("Refresh available serial ports.");
		btnRefresh_.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (portlist()) {
					btnConnect_.setEnabled(true);
					status("Completed listing available ports.");
				}
			}
		});
		btnRefresh_.setText("Refresh");
	

		btnConnect_ = new Button(composite_1, SWT.NONE);
		btnConnect_.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnConnect_.setToolTipText("Connect to Device.");
		btnConnect_.setImage(SWTResourceManager.getImage(UhfAppComposite.class,
				"/jence/icon/connect.png"));
		btnConnect_.setEnabled(false);
		btnConnect_.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (connect()) {
					setEnabled(true, btnDisconnect_, btnScan_, btnScanOnTrigger_);
					setEnabled(false, btnRefresh_, btnConnect_);
					status("Connection was successful.");
				}
			}
		});
		btnConnect_.setText("Connect");

		btnDisconnect_ = new Button(composite_1, SWT.NONE);
		btnDisconnect_.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnDisconnect_.setToolTipText("Disconnect Device.");
		btnDisconnect_.setImage(SWTResourceManager.getImage(
				UhfAppComposite.class, "/jence/icon/disconnect.png"));
		btnDisconnect_.setEnabled(false);
		btnDisconnect_.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (disconnect()) {
					setEnabled(true, btnRefresh_, btnConnect_);
					setEnabled(false, btnDisconnect_, btnScan_, btnScanOnTrigger_);
					status("Disconnected.");
				}
			}
		});
		btnDisconnect_.setText("Disconnect");

		btnScan_ = new Button(composite_1, SWT.NONE);
		btnScan_.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnScan_.setToolTipText("Scan Tags.");
		btnScan_.setImage(SWTResourceManager.getImage(UhfAppComposite.class,
				"/jence/icon/scan.png"));
		btnScan_.setEnabled(false);
		btnScan_.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (scan()) {
					// setEnabled(true,);
					status("Scan completed.");
				}
			}
		});
		btnScan_.setText("Scan");

		btnScanOnTrigger_ = new Button(composite_1, SWT.TOGGLE);
		btnScanOnTrigger_.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnScanOnTrigger_.setEnabled(false);
		btnScanOnTrigger_.setToolTipText("Press the Trigger button on Handheld to activate scan.");
		btnScanOnTrigger_.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				scant();
			}
		});
		btnScanOnTrigger_.setForeground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		btnScanOnTrigger_.setImage(SWTResourceManager.getImage(
				UhfAppComposite.class, "/jence/icon/scant.png"));
		btnScanOnTrigger_.setText("Scan On Trigger");

		lblSupportedChips_ = new Label(this, SWT.WRAP);
		lblSupportedChips_.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 3, 1));
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < AUTODETECTED_CHIPS.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append(AUTODETECTED_CHIPS[i]);
		}
		lblSupportedChips_.setText(sb.toString());

		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setSelection(0);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3,
				1));

		tbtmRaw = new TabItem(tabFolder, SWT.NONE);
		tbtmRaw.setText("INVENTORY");

		Composite composite_3 = new Composite(tabFolder, SWT.NONE);
		tbtmRaw.setControl(composite_3);
		composite_3.setLayout(new GridLayout(3, true));

		composite_4 = new Composite(composite_3, SWT.NONE);
		composite_4.setLayout(new GridLayout(5, false));
		composite_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				3, 1));

		btnAuth_ = new Button(composite_4, SWT.NONE);
		btnAuth_.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnAuth_.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				byte[] epc;
				try {
					epc = getLastEPC();
					openAuthDialog(epc, password_, killpass_, false);
				} catch (Exception e) {
					UhfApp.prompt(UhfAppComposite.this.getShell(), e.getLocalizedMessage(), SWT.OK | SWT.ICON_WARNING);
				}
			}
		});
		btnAuth_.setToolTipText("Scan Tags with Specified Passwords.");
		btnAuth_.setImage(SWTResourceManager.getImage(UhfAppComposite.class,
				"/jence/icon/key.png"));
		btnAuth_.setText("Auth");
		
		btnClear = new Button(composite_4, SWT.NONE);
		btnClear.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				inventory_.removeAll();
			}
		});
		btnClear.setToolTipText("Clears the Inventory table.");
		btnClear.setImage(SWTResourceManager.getImage(UhfAppComposite.class, "/jence/icon/clean.png"));
		btnClear.setText("Clear");
		
		btnMerge = new Button(composite_4, SWT.TOGGLE);
		btnMerge.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnMerge.setToolTipText("Previous scan result will be merge with new scan result.");
		btnMerge.setImage(SWTResourceManager.getImage(UhfAppComposite.class, "/jence/icon/merge.png"));
		btnMerge.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				merge_ = btnMerge.getSelection();
			}
		});
		btnMerge.setText("Merge");
		
		grpIterationsPerScan = new Group(composite_4, SWT.NONE);
		grpIterationsPerScan.setText("Iterations per Scan");
		grpIterationsPerScan.setLayout(new GridLayout(1, false));
		
		comboIterations_ = new Spinner(grpIterationsPerScan, SWT.BORDER);
		comboIterations_.setToolTipText("Sets the number of internal scans done per scan button press. Default is 1.");
		comboIterations_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboIterations_.setMinimum(1);

		grpFilter = new Group(composite_4, SWT.NONE);
		grpFilter.setLayout(new GridLayout(5, false));
		grpFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		grpFilter.setText("Filter");

		btnUseFilter = new Button(grpFilter, SWT.CHECK);
		btnUseFilter.setToolTipText("Click this button if you would like to filter the EPC.");
		btnUseFilter.setText("Use Filter");

		lblOffset = new Label(grpFilter, SWT.NONE);
		lblOffset.setText("Offset");

		offset_ = new Text(grpFilter, SWT.BORDER);
		offset_.setTextLimit(3);
		UhfApp.numericKeyListener(offset_);

		lblNewLabel_2 = new Label(grpFilter, SWT.NONE);
		lblNewLabel_2.setText("Data");

		filter_ = new Text(grpFilter, SWT.BORDER);
		filter_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		UhfApp.hexKeyListener(filter_);

		inventory_ = new Table(composite_3, SWT.BORDER | SWT.FULL_SELECTION);
		inventory_.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		inventory_.setLinesVisible(true);
		inventory_.setHeaderVisible(true);
		inventory_.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				getMemoryDetail();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});

		tblclmnIndex_1 = new TableColumn(inventory_, SWT.NONE);
		tblclmnIndex_1.setWidth(50);
		tblclmnIndex_1.setText("Index");

		tblclmnCount = new TableColumn(inventory_, SWT.NONE);
		tblclmnCount.setWidth(300);
		tblclmnCount.setText("EPC");

		tblclmnLen = new TableColumn(inventory_, SWT.NONE);
		tblclmnLen.setWidth(40);
		tblclmnLen.setText("Len");

		tblclmnAnt = new TableColumn(inventory_, SWT.NONE);
		tblclmnAnt.setWidth(40);
		tblclmnAnt.setText("ANT");

		tblclmnTimes = new TableColumn(inventory_, SWT.NONE);
		tblclmnTimes.setWidth(50);
		tblclmnTimes.setText("Times");

		tblclmnRssi = new TableColumn(inventory_, SWT.NONE);
		tblclmnRssi.setWidth(60);
		tblclmnRssi.setText("RSSI");

		tbtmRawData = new TabItem(tabFolder, SWT.NONE);
		tbtmRawData.setText("MEMORY");

		composite_2 = new Composite(tabFolder, SWT.NONE);
		tbtmRawData.setControl(composite_2);
		composite_2.setLayout(new GridLayout(1, false));

		grpNdef = new Group(composite_2, SWT.NONE);
		GridData gd_grpNdef = new GridData(SWT.FILL, SWT.CENTER, false, false,
				1, 1);
		gd_grpNdef.heightHint = 73;
		grpNdef.setLayoutData(gd_grpNdef);
		grpNdef.setText("Operations");
		grpNdef.setLayout(new GridLayout(5, false));

		btnNdefRead_ = new Button(grpNdef, SWT.NONE);
		btnNdefRead_.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnNdefRead_.setImage(SWTResourceManager.getImage(
				UhfAppComposite.class, "/jence/icon/read.png"));
		btnNdefRead_.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				getMemoryDetail();
			}
		});
		btnNdefRead_.setToolTipText("Reads all NDEF records.");
		btnNdefRead_.setText("Refresh");

		btnNdefWrite_ = new Button(grpNdef, SWT.NONE);
		btnNdefWrite_.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnNdefWrite_.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				byte[] epc;
				try {
					epc = getLastEPC();
					rawWrite(epc);
				} catch (Exception e) {
					UhfApp.prompt(UhfAppComposite.this.getShell(), e.getLocalizedMessage(), SWT.OK | SWT.ICON_WARNING);
				}
			}
		});
		btnNdefWrite_.setImage(SWTResourceManager.getImage(
				UhfAppComposite.class, "/jence/icon/write.png"));
		btnNdefWrite_.setToolTipText("Writes an new NDEF record.");
		btnNdefWrite_.setText("Write");

		btnNdefClean_ = new Button(grpNdef, SWT.NONE);
		btnNdefClean_.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnNdefClean_.setEnabled(false);
		btnNdefClean_.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				clean();
			}
		});
		btnNdefClean_.setImage(SWTResourceManager.getImage(
				UhfAppComposite.class, "/jence/icon/clean.png"));
		btnNdefClean_
				.setToolTipText("This feature is not currently implemented.");
		btnNdefClean_.setText("Clean");

		btnAuthWrite_ = new Button(grpNdef, SWT.NONE);
		btnAuthWrite_.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnAuthWrite_.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				byte[] epc;
				try {
					epc = getLastEPC();
					openAuthDialog(epc, password_, killpass_, true);
				} catch (Exception e) {
					UhfApp.prompt(UhfAppComposite.this.getShell(), e.getLocalizedMessage(), SWT.OK | SWT.ICON_WARNING);
				}
			}
		});
		btnAuthWrite_.setToolTipText("Change Password and Kill Password");
		btnAuthWrite_.setImage(SWTResourceManager.getImage(
				UhfAppComposite.class, "/jence/icon/key.png"));
		btnAuthWrite_.setText("Auth");
		
		btnExists = new Button(grpNdef, SWT.NONE);
		btnExists.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		btnExists.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				byte[] epc;
				try {
					epc = getLastEPC();
					boolean found = UhfApp.driver_.exists(epc);
					if (found)
						UhfApp.prompt(UhfAppComposite.this.getShell(), "Tag FOUND in the inventory", SWT.OK);
					else
						UhfApp.prompt(UhfAppComposite.this.getShell(), "Tag NOT FOUND near the reader.", SWT.OK);
				} catch (Exception e) {
					UhfApp.prompt(UhfAppComposite.this.getShell(), e.getLocalizedMessage(), SWT.OK | SWT.ICON_WARNING);
				}
			}
		});
		btnExists.setToolTipText("Check if this tag is in the inventory.");
		btnExists.setImage(SWTResourceManager.getImage(UhfAppComposite.class, "/jence/icon/cardread.png"));
		btnExists.setText("Exists");

		Composite composite_10 = new Composite(composite_2, SWT.NONE);
		composite_10.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		composite_10.setLayout(new GridLayout(10, false));

		lblNewLabel = new Label(composite_10, SWT.NONE);
		lblNewLabel.setSize(54, 15);
		lblNewLabel.setText("Chip Type");

		chip_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		chip_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3,
				1));
		chip_.setSize(212, 21);
		chip_.setToolTipText("Type of detected tag.");

		Label lblUid = new Label(composite_10, SWT.NONE);
		lblUid.setText("Total Memory");

		total_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		total_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				1, 1));
		total_.setToolTipText("Total Memory in Byte");

		total2_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		total2_.setToolTipText("Total Memory in Bits");
		total2_.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		total2_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		lblNdef = new Label(composite_10, SWT.NONE);
		lblNdef.setText("PWD Size");

		pwdlen_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		pwdlen_.setToolTipText("Password Length in Byte");

		pwdlen2_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		pwdlen2_.setToolTipText("Password Length in Bits");
		pwdlen2_.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		pwdlen2_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		lblAuth = new Label(composite_10, SWT.NONE);
		lblAuth.setText("EPC Size");

		epclen_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		epclen_.setToolTipText("EPC Length in Byte");
		epclen_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				2, 1));

		epclen2_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		epclen2_.setToolTipText("EPC Length in Bits");
		epclen2_.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		epclen2_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		lblBlocks = new Label(composite_10, SWT.NONE);
		lblBlocks.setText("TID Size");

		tidlen_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		tidlen_.setToolTipText("TID Length in Byte");

		tidlen2_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		tidlen2_.setToolTipText("TID Length in Bits");
		tidlen2_.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		tidlen2_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		lblBlockSizebyte = new Label(composite_10, SWT.NONE);
		lblBlockSizebyte.setText("USER Size");

		usrlen_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		usrlen_.setToolTipText("User Memory Size in Byte");

		usrlen2_ = new Text(composite_10, SWT.BORDER | SWT.READ_ONLY);
		usrlen2_.setToolTipText("User Memory Size in Bits");
		usrlen2_.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		usrlen2_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		memory_ = new Table(composite_2, SWT.BORDER);
		memory_.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		memory_.setHeaderVisible(true);
		memory_.setLinesVisible(true);

		TableColumn tblclmnIndex = new TableColumn(memory_, SWT.NONE);
		tblclmnIndex.setWidth(100);
		tblclmnIndex.setText("Memory");

		tblclmnId = new TableColumn(memory_, SWT.NONE);
		tblclmnId.setWidth(60);
		tblclmnId.setText("Word 0");

		tblclmnType_ = new TableColumn(memory_, SWT.NONE);
		tblclmnType_.setWidth(60);
		tblclmnType_.setText("Word 1");

		TableColumn tblclmnEncoding = new TableColumn(memory_, SWT.NONE);
		tblclmnEncoding.setWidth(60);
		tblclmnEncoding.setText("Word 2");

		tblclmnWord = new TableColumn(memory_, SWT.NONE);
		tblclmnWord.setWidth(60);
		tblclmnWord.setText("Word 3");

		tblclmnWord_1 = new TableColumn(memory_, SWT.NONE);
		tblclmnWord_1.setWidth(60);
		tblclmnWord_1.setText("Word 4");

		tblclmnWord_2 = new TableColumn(memory_, SWT.NONE);
		tblclmnWord_2.setWidth(60);
		tblclmnWord_2.setText("Word 5");

		tblclmnWord_3 = new TableColumn(memory_, SWT.NONE);
		tblclmnWord_3.setWidth(60);
		tblclmnWord_3.setText("Word 6");

		TableColumn tblclmnData = new TableColumn(memory_, SWT.NONE);
		tblclmnData.setWidth(60);
		tblclmnData.setText("Word 7");

		tableItem = new TableItem(memory_, SWT.NONE);
		tableItem.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		tableItem.setText("EPC");

		tableItem_1 = new TableItem(memory_, SWT.NONE);
		tableItem_1.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		tableItem_1.setText("TID");

		tbtmEmulate = new TabItem(tabFolder, SWT.NONE);
		tbtmEmulate.setText("INFO");

		info_ = new InfoComposite(tabFolder, SWT.NONE);
		tbtmEmulate.setControl(info_);

		tbtmGpio = new TabItem(tabFolder, SWT.NONE);
		tbtmGpio.setText("GPIO");

		composite_5 = new Composite(tabFolder, SWT.NONE);
		tbtmGpio.setControl(composite_5);
		composite_5.setLayout(new GridLayout(5, false));

		btnGetGpInput = new Button(composite_5, SWT.NONE);
		btnGetGpInput.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnGetGpInput.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				gpi();
			}
		});
		btnGetGpInput.setText("Get GP Input");

		lblIn = new Label(composite_5, SWT.NONE);
		lblIn.setText("IN1");

		in1_ = new Text(composite_5, SWT.BORDER | SWT.READ_ONLY);
		in1_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		in1_.setTextLimit(1);

		lblNewLabel_3 = new Label(composite_5, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblNewLabel_3.setText("IN2");

		in2_ = new Text(composite_5, SWT.BORDER | SWT.READ_ONLY);
		in2_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		in2_.setTextLimit(1);

		btnSetGpOutput = new Button(composite_5, SWT.NONE);
		btnSetGpOutput.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnSetGpOutput.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				gpo();
			}
		});
		btnSetGpOutput.setText("Set GP Output");

		lblOut = new Label(composite_5, SWT.NONE);
		lblOut.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblOut.setText("OUT1");

		out1_ = new Text(composite_5, SWT.BORDER | SWT.READ_ONLY);
		out1_.setText("0");
		out1_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		out1_.setTextLimit(1);

		lblOut_1 = new Label(composite_5, SWT.NONE);
		lblOut_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblOut_1.setText("OUT2");

		out2_ = new Text(composite_5, SWT.BORDER | SWT.READ_ONLY);
		out2_.setText("0");
		out2_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		out2_.setTextLimit(1);
		tabFolder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					if (tabFolder.getSelectionIndex() != 2) {
						return;
					}
					settings();
				} catch (Exception e) {
					status("");
					UhfApp.prompt(UhfAppComposite.this.getShell(),
							e.getLocalizedMessage(), SWT.Close
									| SWT.ICON_WARNING);
				}
			}
		});

		composite_6 = new Composite(this, SWT.NONE);
		composite_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1));
		composite_6.setLayout(new GridLayout(5, false));

		lblStatus_ = new Label(composite_6, SWT.NONE);
		lblStatus_.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));
		lblStatus_.setSize(0, 15);
		
		lblLibraryVersion = new Label(composite_6, SWT.NONE);
		lblLibraryVersion.setText("Library Version");
		
		lblLibVersion_ = new Label(composite_6, SWT.NONE);
		String v = UhfApp.driver_.getVersion();
		lblLibVersion_.setText(v);
		

		Label lblNewLabel_1 = new Label(composite_6, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblNewLabel_1.setBounds(0, 0, 55, 15);
		lblNewLabel_1.setText("| Application Version: " + UhfApp.VERSION);

		// this.pack();
		checkVersion();
	}

	private void checkVersion() {
		try {
			URL url = new URL(LATEST_VERSION_PAGE);
			URLConnection con = url.openConnection();
			InputStream stream = con.getInputStream();
			Properties properties = new Properties();
			properties.load(stream);
			String version = properties.getProperty("J4210U");
			if (version.compareTo(UhfApp.VERSION) > 0) {
				if (UhfApp
						.prompt(getShell(),
								"New version "
										+ version
										+ " found. You can download the latest version by clicking the OK button.",
								SWT.OK | SWT.CANCEL) == SWT.OK) {
					java.awt.Desktop.getDesktop()
							.browse(new URI(DOWNLOAD_PAGE));
				}
			}
			System.out.println(properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean disconnect() {
		try {
			UhfApp.driver_.close();
			return true;
		} catch (Exception e) {
			prompt(e.getMessage(), SWT.ICON_WARNING);
		}
		return false;
	}

	private boolean connect() {
		try {
			int baudrate = Integer.parseInt(comboBaudrate_.getText());
			UhfApp.driver_.open(comboPorts_.getText(), baudrate);
			tabFolder.setSelection(0);
			return true;
		} catch (Exception e) {
			prompt(e.getMessage()
					+ " Could not connect to this port. Try another port.",
					SWT.ICON_WARNING);
		}
		return false;
	}
	
	private boolean scan() {
		int iterations = Integer.parseInt(comboIterations_.getText());
		boolean ismerging = merge_;
		if (iterations > 1)
			merge_ = true;
		try {
			UhfApp.driver_.setQ(6);
			UhfApp.driver_.setSession(0);
			do {
				boolean ok = scanonce();
				if (!ok) {
					return false;
				}
			} while(--iterations > 0);
		} catch(Throwable t) {
			prompt(t.getLocalizedMessage(), SWT.ICON_WARNING);
		} finally {
			merge_ = ismerging;
		}
		return true;
	}

	private boolean scanonce() {
		try {
			tabFolder.setSelection(0);
			boolean filter = false;
			if (btnUseFilter.getSelection()) {
				String off = offset_.getText().trim();
				String filt = filter_.getText().trim();
				if (off.length() > 0 && filt.length() > 0) {
					int offset = Integer.parseInt(off);
					byte[] hex = UhfApp.driver_.hex2bytes(filt,
							filt.length() / 2);
					UhfApp.driver_.filter(offset, hex);
					filter = true;
					status("Filter is used during scan.");
				} else {
					status("Ignored filter because one of the field is left blank.");
				}
			}
			int n = UhfApp.driver_.inventory(filter);
			if (n == 0) {
				status("No tags found.");
				return false;
			}
			Hashtable<String, J4210U.ScanResult> previousContent = new Hashtable<String, J4210U.ScanResult>();
			if (merge_) {
				for(int i=0;i<inventory_.getItemCount();i++) {
					TableItem item = inventory_.getItem(i);
					J4210U.ScanResult sr = (J4210U.ScanResult)item.getData();
					previousContent.put(UhfApp.driver_.toHex(sr.EPC), sr);
				}
			}
			inventory_.removeAll();
			for (int i = 0; i < n; i++) {
				J4210U.ScanResult sr = UhfApp.driver_.getResult(i);
				if (merge_) {
					
					String hex = UhfApp.driver_.toHex(sr.EPC);
					J4210U.ScanResult srold = previousContent.get(hex);
					if (srold != null) {
						sr.Count += srold.Count;
						previousContent.remove(hex);
					}
				}
				// System.out.println(sr);
				TableItem item = new TableItem(inventory_, SWT.None
						| SWT.FULL_SELECTION);
				item.setText(new String[] { (i + 1) + "",
						UhfApp.driver_.toHex(sr.EPC), sr.EpcLength + "",
						sr.Ant + "", sr.Count + "", sr.RSSI + "" });
				item.setData(sr);
			}
			if (merge_) {
				// if the previous content is not empty, the add them too
				int i = inventory_.getItemCount();
				for(Enumeration keys = previousContent.keys(); keys.hasMoreElements(); i++){
					J4210U.ScanResult sr = previousContent.get(keys.nextElement());
					TableItem item = new TableItem(inventory_, SWT.None
							| SWT.FULL_SELECTION);
					item.setText(new String[] { (i + 1) + "",
							UhfApp.driver_.toHex(sr.EPC), sr.EpcLength + "",
							sr.Ant + "", sr.Count + "", sr.RSSI + "" });
					item.setData(sr);
				}
			}
		} catch (Exception e) {
			prompt(e.getMessage(), SWT.ICON_WARNING | SWT.OK);
		}
		return true;
	}

	/**
	 * Scan on Trigger.
	 */
	private void scant() {
		if (!btnScanOnTrigger_.getSelection()) {
			scanning_ = false;
			if (timer_ != null) {
				timer_.cancel();
				timer_ = null;
			}
			btnScan_.setEnabled(true);
			btnScanOnTrigger_.setEnabled(true);
			return;
		}
		if (timer_ == null) {
			if (timer_ == null)
				timer_ = new Timer();
			btnScan_.setEnabled(false);
			btnScanOnTrigger_.setEnabled(false);
			timer_.schedule(new TimerTask() {
	
				@Override
				public void run() {
					try {
						if (scanning_)
							return;
						boolean trigger = UhfApp.driver_.getGPInput(1);
						if (trigger) {
							System.out.println("Trigger Pressed.");
							UhfAppComposite.this.getDisplay().syncExec(
									new Runnable() {
										@Override
										public void run() {
											if (!btnScanOnTrigger_.getSelection()) {
												return;
											}

											scanning_ = true;
											scan();
											scanning_ = false;
										}
									});
						}
					} catch (Exception e) {
						//e.printStackTrace();
						System.out.println("Could not detect press.");
					}
				}
			}, 0, 500);
		}
	}

	private boolean portlist() {
		try {
			String[] ports = UhfApp.driver_.listPorts();
			comboPorts_.removeAll();
			for (int i = 0; i < ports.length; i++) {
				comboPorts_.add(ports[i]);
			}
			if (ports.length > 0) {
				comboPorts_.select(0);
			}
			return true;
		} catch (Exception e) {
			prompt(e.getMessage()
					+ " Please check if the device is attached to an USB port.",
					SWT.ICON_WARNING);
		}
		return false;
	}

	private boolean clean() {
		try {
			if (prompt(
					"This operation will reset all the data in the card. Do you want to proceed?",
					SWT.ICON_WARNING | SWT.OK | SWT.CANCEL) == SWT.CANCEL) {
				return false;
			}
			UhfApp.driver_.format();
		} catch (Exception e) {
			prompt(e.getMessage(), SWT.ICON_WARNING);
		}
		return false;
	}

	private void openAuthDialog(byte[] epc, byte[] password, byte[] killpass,
			boolean enableWrite) {
		Shell dialog = new Shell(this.getShell(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		AuthComposite composite = new AuthComposite(dialog, SWT.NONE);
		composite.setEPC(epc, password, killpass, enableWrite);
		dialog.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dialog.setBounds(new Rectangle(0, 0, 600, 400));

		composite.callback(new Callback() {
			@Override
			public void callback(int selection, String text) throws Exception {
			}
		});

		UhfApp.center(dialog);
		dialog.open();
	}

	private void loadMemory(ScanResult sr) {
		int lastusrindex = 0;
		try {
			memory_.setData("epc",sr);
			// delete all rows
			for (int i = memory_.getItemCount() - 1; i <= 0; i--) {
				// TableItem item = memory_.getItem(i);
			}
			memory_.removeAll();
			byte[] epc = sr.EPC;
			int epclen = sr.EpcLength;
			if (epc.length != epclen) {
				epc = Arrays.copyOf(epc, epclen);
			}
			TableItem epcrow = new TableItem(memory_, SWT.FULL_SELECTION);
			epcrow.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
			epcrow.setText(0, "EPC");
			for (int i = 0; i < sr.EPC.length; i += 2) {
				byte[] word = Arrays.copyOfRange(sr.EPC, i, i + 2);
				String hex = UhfApp.driver_.toHex(word);
				epcrow.setText(1 + i / 2, hex);
			}
			TableItem tidrow = new TableItem(memory_, SWT.FULL_SELECTION);
			tidrow.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
			tidrow.setText(0, "TID");
			status("Reading TID");
			byte[] tid = UhfApp.driver_.getTID(epc);
			if (tid == null) {
				status("Could not retrieve TID");
			}
			J4210U.TagInfo ti = UhfApp.driver_.getTagInfo(tid);
			memory_.setData(ti);
			// if (usrlen_.getText().length() > 0) {
			// int ll = Integer.parseInt(usrlen_.getText());
			// if (ll > 0)
			// ti.userlen = ll;
			// }
			chip_.setText(ti.chip);
			epclen_.setText(ti.epclen + "");
			epclen2_.setText(ti.epclen * 8 + "");
			tidlen_.setText(ti.tidlen + "");
			tidlen2_.setText(ti.tidlen * 8 + "");
			usrlen_.setText(ti.userlen + "");
			usrlen2_.setText(ti.userlen * 8 + "");
			pwdlen_.setText(ti.pwdlen + "");
			pwdlen2_.setText(ti.pwdlen * 8 + "");
			int total = ti.epclen + ti.tidlen + ti.userlen + ti.pwdlen * 2 + 4;
			total_.setText(total + "");
			total2_.setText(total * 8 + "");
			// populate TID
			for (int i = 0; i < tid.length; i += 2) {
				byte[] word = Arrays.copyOfRange(tid, i, i + 2);
				String hex = UhfApp.driver_.toHex(word);
				tidrow.setText(1 + i / 2, hex);
			}
			// populate USER memory
			int col = 1;
			TableItem userrow = null;
			for (int i = 0; i < ti.userlen / 2; i++) {
				if (i % 8 == 0) {
					col = 1;
					userrow = new TableItem(memory_, SWT.FULL_SELECTION);
					final String label = "USER[" + i + ".." + (i + 8 - 1) + "]";
					userrow.setText(0, label);
					userrow.setBackground(SWTResourceManager
							.getColor(SWT.COLOR_CYAN));
				}
				byte[] word = new byte[2];
				status("Reading User Memory address " + i);
				word = UhfApp.driver_.readWord(epc, i);
				lastusrindex = i;
				final int cc = col;
				final String hex = UhfApp.driver_.toHex(word);
				final TableItem tablerow = userrow;
				getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						tablerow.setText(cc, hex);
					}
				});
				col++;
			}
			createEditableTable(memory_);
			status("Memory Load completed.");
			tabFolder.setSelection(1); // selects MEMORY folder
		} catch (Exception e) {
			try {
				UhfApp.prompt(this.getShell(), e.getLocalizedMessage()
						+ UhfApp.driver_.error(), SWT.Close
						| SWT.ICON_INFORMATION);
				usrlen_.setText(lastusrindex * 2 + "");
				usrlen2_.setText(lastusrindex * 16 + "");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void setPasswords(byte[] pass, byte[] killpass) {
		password_ = pass;
		killpass_ = killpass;
	}

	private void settings() {
		status("Loading settings...");
		try {
			info_.refresh();
			status("Settings loaded.");
		} catch (Exception e) {
			status("Settings load failed.");
			e.printStackTrace();
		}
	}

	private void rawWrite(byte[] epc) {
		try {
			int rows = memory_.getItemCount();
			int column = memory_.getColumnCount();
			byte[] data = new byte[2];

			// Write into User Memory
			for (int i = 2; i < rows; i++) {
				TableItem item = memory_.getItem(i);
				for (int j = 1; j < column; j++) {
					String oldData = (String) item.getData(j + "");
					if (oldData == null) {
						continue;
					}
					String newData = item.getText(j);
					int d = Integer.parseInt(newData, 16);
					data[1] = (byte) (d & 0xFF);
					data[0] = (byte) ((d >> 8) & 0xFF);

					int jj = (i - 2) * 8 + (j - 1);
					syncstatus("Writing User Memory address " + jj + " = "
							+ UhfApp.driver_.toHex(data));
					UhfApp.driver_.writeWord(epc, data, jj);
				}
			}

			epc = Arrays.copyOf(epc, epc.length);

			// change EPC
			TableItem epcitem = memory_.getItem(0);
			for (int j = 1; j < column; j++) {
				String oldData = (String) epcitem.getData(j + "");
				if (oldData == null) {
					continue;
				}
				String newData = epcitem.getText(j);
				int d = Integer.parseInt(newData, 16);
				data[0] = (byte) ((d >> 8) & 0xFF);
				data[1] = (byte) (d & 0xFF);

				int n = (j - 1) * 2;
				int jj = j - 1;
				status("Writing EPC Word " + jj + " = "
						+ UhfApp.driver_.toHex(data));
				UhfApp.driver_.writeEpcWord(epc, data, jj);
				epc[n] = data[0];
				epc[n + 1] = data[1];
				ScanResult sr = (ScanResult) memory_.getData("epc");
				sr.EPC = epc;
				// UhfApp.driver_.tagExists(epc);
			}

			// All writes were successful, so remove all the old data
			for (int i = 0; i < memory_.getItemCount(); i++) {
				TableItem item = memory_.getItem(i);
				for (int j = 0; j < memory_.getColumnCount(); j++) {
					String oldData = (String) item.getData(j + "");
					if (oldData != null) {
						item.setData(j + "", null); // clear stored data
						item.setFont(j, null);
					}
				}
			}
			status("Writing to Tag completed.");
		} catch (Exception e) {
			UhfApp.prompt(this.getShell(), e.getLocalizedMessage(), SWT.OK
					| SWT.ICON_WARNING);
			return;
		}
	}
	
	private void getMemoryDetail() {
		int selection = inventory_.getSelectionIndex();
		if (selection >= 0) {
			tabFolder.setSelection(1); // go to memory folder
		} else {
			UhfApp.prompt(
					getShell(),
					"You must select a row in the inventory result to get memory detail.",
					SWT.OK | SWT.ICON_WARNING);
			return;
		}
		TableItem item = inventory_.getItem(selection);
		ScanResult sr = (ScanResult)item.getData();

		loadMemory(sr);
	}
	
	private byte[] getLastEPC() throws Exception {
		ScanResult sr = (ScanResult)memory_.getData("epc");
		if (sr == null) {
			throw new Exception("No EPC found.");
		}
		return sr.EPC;
	}
	
	private void gpi() {
		in1_.setText(UhfApp.driver_.GetGPI((byte)1) + "");
		in2_.setText(UhfApp.driver_.GetGPI((byte)2) + "");
	}
	
	private void gpo() {
		UhfApp.driver_.SetGPO((byte)0x01);
		UhfApp.driver_.SetGPO((byte)0x02);
	}
}
