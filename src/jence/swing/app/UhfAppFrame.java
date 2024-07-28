package jence.swing.app;

import jence.jni.J4210U;
import jence.jni.J4210U.ScanResult;
import jence.jni.J4210U.TagType;

import jence.swing.app.UhfAppFrame;
import jence.swing.app.UhfApp;

//import jence.swing.app.Callback;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.border.LineBorder;

public class UhfAppFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	// IMPORTANT: any name change from WindowBuilder will make things disappear from
	// here and that will break the functionality of the code
	// adding something new won't cause problem but renaming existing element will
	// cause problem
	public int SCAN_SERVER_DELAY = 550;

	private JButton btnHelp_;
	private JButton btnRefresh_;
	private JButton btnConnect_;
	public JButton btnDisconnect_;
	public JButton btnScan_;
	public JToggleButton btnScanServer_;
	public JToggleButton btnScanOnTrigger_;

	private JTabbedPane tabFolder;
	private JPanel tabs;
	private JPanel panelInventory;
	private JPanel penelMemory;
	Set<Integer> uneditableRows = new HashSet<>();
	private byte[] lastEpc;

	private JPanel panelInfo;

	private JPanel panelGPIO;
	private JCheckBox btnGpo1;
	private JCheckBox btnGpo2;
	private JCheckBox btnGpi1;
	private JCheckBox btnGpi2;
	private JToggleButton btnMonitor_;

	private JPanel panelMessaging;

	public JToggleButton btnMerge_;
	private JCheckBox btnUseFilter;
	private JCheckBox btnSingleTag_;
	private JSpinner comboIterations_;
	private DefaultTableModel model;
	private Hashtable<String, J4210U.ScanResult> previousContent_ = new Hashtable<String, J4210U.ScanResult>();

	private JComboBox<String> comboPorts_;
	private JComboBox<String> comboBaudrate_;

	private AuthFrame authDialog;
	public byte[] password_ = { 0, 0, 0, 0 };
	public byte[] killpass_ = { 0, 0, 0, 0 };

	private HelpDialog helpDialog;
	private InfoPanel info_;

	private JTextField txtSupportedChips_;
	private JTextField offset_;
	private JTextField filter_;
	private JTable memory_;
	private Object[][] cellValues;

	private Timer gpioTimer_ = null;
	private Messenger messenger_ = null;

	// private byte[] epc = null;
	private byte[] password = { 0, 0, 0, 0 };
	private byte[] killpass = { 0, 0, 0, 0 };
	private boolean scanning = false;
//	private volatile boolean merge_ = false;
//    private Timer timer = null;

	public static final String DOWNLOAD_PAGE = "https://jence.com/web/index.php?route=product/product&path=69_25_225&product_id=792";
	public static final String LATEST_VERSION_PAGE = "https://jence.com/downloads/version.properties";
	public static final String[] AUTODETECTED_CHIPS = { TagType.HIGGS_3.toString(), TagType.HIGGS_4.name(),
			TagType.HIGGS_EC.name(), TagType.IMPINJ_M730.name(), TagType.IMPINJ_M750.name(), TagType.IMPINJ_M770.name(),
			TagType.IMPINJ_M775.name(), TagType.MONZA_4D.name(), TagType.MONZA_4E.name(), TagType.MONZA_4I.name(),
			TagType.MONZA_4QT.name(), TagType.MONZA_R6.name(), TagType.MONZA_R6P.name(), TagType.UCODE_8.name(),
			TagType.KILOWAY_2005BL.name() };
	private JTable inventory_;
	public static ArrayList<Tuple<Integer, Integer>> editedValue = new ArrayList<>();

	private JTextField chip_;
	private JTextField total_;
	private JTextField total2_;
	private JTextField pwdlen_;
	private JTextField pwdlen2_;
	private JTextField epclen_;
	private JTextField epclen2_;
	private JTextField tidlen_;
	private JTextField tidlen2_;
	private JTextField usrlen_;
	private JTextField usrlen2_;

	private JCheckBox btnActivateJsonMessaging_;
	private JCheckBox btnActivateHttpQuery_;
	private JCheckBox btnActivateWriteToFile_;
	private JTextField textIP_;
	private JTextField textPort_;
	private JTextField textHttpUrl_;
	private JTextField textDirectory_;
	private JTextField textFilename_;
	private JPanel panel_7;
	private JPanel panel_8;
	private JLabel lblStatus;
	private JLabel lblLibraryVersion;
	private JLabel lblLibVersion;
	private JLabel lblAppVersion;
	private JCheckBox btnDebug;

	private boolean merge_;

	public synchronized void setMerge(boolean state) {
		this.merge_ = state;
	}

	public synchronized boolean getMerge() {
		return this.merge_;
	}

	public void status(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (lblStatus != null) {
					lblStatus.setText(text);
					lblStatus.repaint();

				}
			}
		});
	}

	private boolean hasSelectedCell() {
		int selectionRow = inventory_.getSelectedRow();
		Object[] rowData = null;
		if (selectionRow >= 0) {

			DefaultTableModel model = (DefaultTableModel) inventory_.getModel();
			int columnCount = model.getColumnCount();
			rowData = new Object[columnCount];
			for (int i = 0; i < columnCount; i++) {
				rowData[i] = model.getValueAt(selectionRow, i);
			}
			ScanResult sr = convertToScanResult(rowData);
//			System.out.println(sr.toString());
			return true;

		} else {
			jence.swing.app.UhfApp.prompt("You must select a row in the inventory result to get memory detail.",
					"Warning", 0, JOptionPane.WARNING_MESSAGE);
			return false;
		}

	}

	private void openAuthDialog(byte[] epc, byte[] password, byte[] killpass, boolean enableWrite) {
		authDialog = new AuthFrame(UhfAppFrame.this);
		epc = lastEpc;
		authDialog.setEPC(epc, password, killpass, enableWrite);
		authDialog.setVisible(true);
	}

	private void openHelpDialog() {
		helpDialog = new HelpDialog(UhfAppFrame.this);

//		helpDialog.setSize(550, 320);
		helpDialog.setVisible(true);
	}

	public static <T extends JComponent> void setEnabled(boolean enabled, T... components) {
		for (T component : components) {
			component.setEnabled(enabled);
		}
	}

	private boolean portlist() {
		try {
			String[] ports = jence.swing.app.UhfApp.driver_.listPorts();
			comboPorts_.removeAllItems();

			int portCount = 0;
			for (String port : ports) {
				port = port.trim();
				if (!port.isEmpty()) {
					this.comboPorts_.addItem(port);
					portCount++;
				}
			}

			if (portCount == 0) {
				return false;
			} else {
				this.comboPorts_.setSelectedIndex(0);
				return true;
			}
		} catch (Exception e) {

			UhfApp.prompt(e.getLocalizedMessage(), "ERROR", 1, JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
			return false;
		}
	}

	private Properties getMessagingProperties() {
		Properties properties = new Properties();

		properties.setProperty("socket.messaging", btnActivateJsonMessaging_.isSelected() + "");
		properties.setProperty("socket.ip", textIP_.getText());
		properties.setProperty("socket.port", textPort_.getText());

		properties.setProperty("http.messaging", btnActivateHttpQuery_.isSelected() + "");
		properties.setProperty("http.url", textHttpUrl_.getText());

		properties.setProperty("file.messaging", btnActivateWriteToFile_.isSelected() + "");
		properties.setProperty("file.dir", textDirectory_.getText());
		properties.setProperty("file.name", textFilename_.getText());

		return properties;
	}

	private boolean startMessenger() {
		try {
			messenger_ = new Messenger(getMessagingProperties());
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
			UhfApp.prompt("You have incorrect Messenger Settings. " + e1.getLocalizedMessage() + "\n\nScan Aborted.",
					"Scan Aborted", 4, JOptionPane.ABORT);
			return false;
		}
	}

	private void saveMessagingSetting() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save");
		fileChooser.setCurrentDirectory(new File("./"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Properties Files", "properties");
		fileChooser.setFileFilter(filter);
		fileChooser.setPreferredSize(new Dimension(600, 400));
		int result = fileChooser.showSaveDialog(null);
		if (result != JFileChooser.APPROVE_OPTION) {
			UhfApp.prompt("Settings will not be saved. No filename provided", "Warning", 0, JOptionPane.ERROR_MESSAGE);
			return;
		}
		File selectedFile = fileChooser.getSelectedFile();
		try {
			if (!selectedFile.getName().toLowerCase().endsWith(".properties")) {
				selectedFile = new File(selectedFile.getAbsolutePath() + ".properties");
			}
			Properties properties = getMessagingProperties();
			try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
				properties.store(fos, "# UhfApp Messaging Setting created on " + new Date());
			}
		} catch (Exception e) {
			e.printStackTrace();
			UhfApp.prompt("Error " + e.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);
		}
	}

	private void loadMessagingSetting() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Open");
		fileChooser.setCurrentDirectory(new File("./"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Properties Files", "properties");
		fileChooser.setFileFilter(filter);

		fileChooser.setPreferredSize(new Dimension(600, 400));
		int result = fileChooser.showOpenDialog(null);
		if (result != JFileChooser.APPROVE_OPTION) {
			UhfApp.prompt("No settings to load. No filename provided", "Warning", 0, JOptionPane.ERROR_MESSAGE);
			return;
		}
		File selectedFile = fileChooser.getSelectedFile();
		try {
			FileReader fileReader = new FileReader(selectedFile);
			Properties properties = new Properties();
			properties.load(fileReader);

			String value = properties.getProperty("socket.messaging", "false");
			btnActivateJsonMessaging_.setSelected(Boolean.parseBoolean(value));
			textIP_.setText(properties.getProperty("socket.ip", ""));
			textPort_.setText(properties.getProperty("socket.port", ""));

			value = properties.getProperty("http.messaging", "false");
			btnActivateHttpQuery_.setSelected(Boolean.parseBoolean(value));
			textHttpUrl_.setText(properties.getProperty("http.url", ""));

			value = properties.getProperty("file.messaging", "false");
			btnActivateWriteToFile_.setSelected(Boolean.parseBoolean(value));
			textDirectory_.setText(properties.getProperty("file.dir", ""));
			textFilename_.setText(properties.getProperty("file.name", ""));

			if (messenger_ != null) {
				messenger_.close();
			}
			fileReader.close(); // Close the FileReader
		} catch (Exception e) {
			e.printStackTrace();
			UhfApp.prompt(e.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);
		}
	}

	boolean scan() {
		if (!startMessenger())
			return false;
		int iterations = Integer.parseInt(comboIterations_.getValue().toString());
		boolean ismerging = getMerge();
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
			} while (--iterations > 0);
		} catch (Throwable t) {
			UhfApp.prompt(t.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);
		} finally {
			merge_ = ismerging;
			if (messenger_ != null) {
				messenger_.close();
			}
		}
		return true;
	}

	private boolean scans() {
		try {
			ScanSWorker worker = new ScanSWorker(this);
			worker.execute();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private boolean scant() {
		try {
			ScanTWorker worker = new ScanTWorker(this);
			worker.execute();
		} catch (Exception e) {
			return false;
		}
		return true;

	}

	private boolean scanonce() {
		try {
			tabFolder.setSelectedIndex(0);
			boolean filter = false;
			if (btnUseFilter.isSelected()) {
				String off = offset_.getText().trim();
				String filt = filter_.getText().trim();
				if (off.length() > 0 && filt.length() > 0) {
					int offset = Integer.parseInt(off);
					byte[] hex = jence.swing.app.UhfApp.driver_.hex2bytes(filt, filt.length() / 2);
					UhfApp.driver_.filter(offset, hex);
					filter = true;
					status("Filter is used during scan.");
				} else {
					status("Ignored filter because one of the field is left blank.");
				}
			}
			if (!merge_) {
				previousContent_.clear();
				model.setRowCount(0);
			}
			int n = 0;
			if (btnSingleTag_.isSelected())
				n = jence.swing.app.UhfApp.driver_.inventoryOne(); // single tag scan
			else
				n = jence.swing.app.UhfApp.driver_.inventory(filter);

			if (n == 0) {
				status("No tags found.");
				return false;
			}
			status(n + " Tags detected. Fetching tag details...");

			HashSet unique = new HashSet();
			int nonunique = 0;
			// Get previous content
			for (int i = 0; i < n; i++) {
				final J4210U.ScanResult sr = jence.swing.app.UhfApp.driver_.getResult(i);
				String hex = jence.swing.app.UhfApp.driver_.toHex(sr.EPC);
				previousContent_.put(hex, sr);
				if (!unique.contains(hex))
					unique.add(hex); // Keep only the unique EPC
				else {
					nonunique++;
					// If only unique tags are needed, uncomment this line
//					continue;
				}

			}

			model.setRowCount(0);

			int count = 0;
			Enumeration<String> keys = previousContent_.keys();

			while (keys.hasMoreElements()) {
				// for (String key = previousContent.ge; i < previousContent.keys(); i++) {
				// final J4210U.ScanResult sr = UhfApp.driver_.getResult(i);
				// String hex = UhfApp.driver_.toHex(sr.EPC);
				String epc = keys.nextElement();
				J4210U.ScanResult sr = previousContent_.get(epc);

				count++;
				model.addRow(new Object[] { (count) + "", epc, sr.EpcLength + "", sr.Ant + "", sr.Count + "",
						sr.RSSI + "" });
//				item.setData(sr);
				// if messaging is enabled, send the message now in a thread.
				if (messenger_ != null) {
					try {
						final String json = sr.toJson();
						Thread.sleep(20);
						// TODO: make an option in gui that lets select the format of json is it an
						// arrya or single unit
						// and that'll allow to render the whole table at once and make ui faster
						messenger_.sendMessage(json);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				status("Fetched Tag Info: " + count + " of " + n + "...");
//				updateGuiTasks();
			}
			status("Scan Completed. Tags Found: " + n + ". Non-Unique: " + nonunique);
		} catch (Exception e) {
			UhfApp.prompt(e.getLocalizedMessage(), "Warning", 0, JOptionPane.WARNING_MESSAGE);

		}
		return true;
	}

	private boolean connect(boolean recurse) {
		try {
			String port = comboPorts_.getSelectedItem().toString();
			int baudrate = Integer.parseInt(comboBaudrate_.getSelectedItem().toString());
			int index = comboBaudrate_.getSelectedIndex();

			if (recurse) {
				if (index == 0) {
					UhfApp.driver_.open(port, 115200);
					UhfApp.prompt("Baudrate is 115200 so connected with 115200...", "Important Information", 3,
							JOptionPane.INFORMATION_MESSAGE);
					comboBaudrate_.setSelectedIndex(1);
					return true;
				} else if (index == 1) {
					UhfApp.driver_.open(port, 57600);
					UhfApp.prompt("Baudrate is 57600 so connected with 57600...", "Important Information", 3,
							JOptionPane.INFORMATION_MESSAGE);
					comboBaudrate_.setSelectedIndex(0);
					return true;
				}
			} else {
				System.out.println(baudrate);
				System.out.println(port);
				UhfApp.driver_.open(port, baudrate);
				tabFolder.setSelectedIndex(0);
				clearMessaging();
				status("Device is Successfully connected to " + port + " with " + baudrate + " baud speed.");
				return true;
			}
		} catch (Exception e) {
			boolean connected = false;
			if (!recurse) {
				connected = connect(true);
			}
			if (!recurse && !connected) {
				UhfApp.prompt(e.getMessage() + " Could not connect to this port.\nTry another port.", "Warning", 0,
						JOptionPane.WARNING_MESSAGE);
				return false;
			} else if (!recurse && connected) {
				return true;
			}
		}
		return false;
	}

	private boolean disconnect() {
		try {
			if (messenger_ != null) {
				messenger_.close();
				messenger_ = null;
			}
			jence.swing.app.UhfApp.driver_.close();
			return true;
		} catch (Exception e) {
			UhfApp.prompt(e.getLocalizedMessage(), "Warning", 0, JOptionPane.WARNING_MESSAGE);

		}
		return false;
	}

	private void settings() {
		status("Loading settings...");
		try {
			info_.refresh();
			status("Settings loaded.");
		} catch (Exception e) {
			status("Settings load failed.");
//			e.printStackTrace();
		}
	}

	public static void setPanelsEnabled(boolean isEnabled, JPanel... panels) {
		for (JPanel panel : panels) {
			setPanelEnabled(panel, isEnabled);
		}
	}

	static void setPanelEnabled(JPanel panel, boolean isEnabled) {
		panel.setEnabled(isEnabled);
		Component[] components = panel.getComponents();
		for (Component component : components) {
			if (component instanceof JPanel) {
				setPanelEnabled((JPanel) component, isEnabled);
			}
			component.setEnabled(isEnabled);
		}
	}

	public static void setClear(boolean clear, JComponent... components) {
		for (JComponent component : components) {
			if (component instanceof JTextField) {
				JTextField textField = (JTextField) component;
				textField.setText(!clear ? "" : textField.getText());
			} else if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setSelected(clear);
			}
		}
	}

	private void clearMessaging() {
		setClear(false, btnActivateJsonMessaging_, textIP_, textPort_, btnActivateHttpQuery_, textHttpUrl_,
				btnActivateWriteToFile_, textDirectory_, textFilename_);
	}

	private void gpo() throws Exception {
//		UhfApp.driver_.setGPOutput((byte)0x01);
//		UhfApp.driver_.setGPOutput((byte)0x02);

		boolean gpo1 = btnGpo1.isSelected();
		boolean gpo2 = btnGpo2.isSelected();
		byte gpoVal = (gpo1) ? (byte) 0x01 : (byte) 0x00;
		gpoVal |= (gpo2) ? (byte) 0x02 : (byte) 0x00;
		jence.swing.app.UhfApp.driver_.setGPOutput(gpoVal);
	}

	private void gpi() throws Exception {
//		System.out.println("ENTERED INTO GPI");
		boolean gpi1 = jence.swing.app.UhfApp.driver_.getGPInput(1);
		Thread.sleep(20);
		gpi1 = gpi1 && jence.swing.app.UhfApp.driver_.getGPInput(1);
		boolean gpi2 = jence.swing.app.UhfApp.driver_.getGPInput(2);
		Thread.sleep(20);
		gpi2 = gpi2 && jence.swing.app.UhfApp.driver_.getGPInput(2);

		btnGpi1.setSelected(gpi1);
		btnGpi2.setSelected(gpi2);
	}

	private void monitorStop() {
		if (gpioTimer_ != null) {
			monitor();
		}
	}

	private void monitor() {
		if (gpioTimer_ != null) {
			btnMonitor_.setText("Monitor Input");
			gpioTimer_.cancel();
			gpioTimer_ = null;
		} else {
			gpioTimer_ = new Timer();
			btnMonitor_.setText("Monitor [STOP]");
			gpioTimer_.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						final boolean gpi1 = jence.swing.app.UhfApp.driver_.getGPInput(1);
						final boolean gpi2 = jence.swing.app.UhfApp.driver_.getGPInput(2);

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								btnGpi1.setSelected(gpi1);
								btnGpi2.setSelected(gpi2);
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 0, 500);
		}
	}

	// Method to convert rowData into a ScanResult object
	private ScanResult convertToScanResult(Object[] rowData) {
		byte Ant;
		byte RSSI;
		int Count;
		byte EpcLength;
		byte[] EPC;

		try {
			String epcString = (String) rowData[1];
			EpcLength = Byte.parseByte((String) rowData[2]);
			Ant = Byte.parseByte((String) rowData[3]);
			Count = Integer.parseInt((String) rowData[4]);
			RSSI = Byte.parseByte((String) rowData[5]);

			EPC = J4210U.hex2bytes(epcString, EpcLength);
			// Create and return the ScanResult object
			return new ScanResult(Ant, RSSI, Count, EpcLength, EPC);
		} catch (Exception e) {
			// Handle any errors that may occur during conversion
			e.printStackTrace();
			return null; // Return null if conversion fails
		}
	}

	public Object[][] getTableData(JTable table) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int rowCount = model.getRowCount();
		int columnCount = model.getColumnCount();

		Object[][] data = new Object[rowCount][columnCount];

		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				data[row][column] = model.getValueAt(row, column);
			}
		}

		return data;
	}

	public ArrayList<ArrayList<Integer>> getChangedCellIndexes(Object[][] data, JTable table) {

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int rowCount = model.getRowCount();
		int columnCount = model.getColumnCount();

		// Check if dimensions match
		if (data.length != rowCount || data[0].length != columnCount) {
			return null;
		}

		ArrayList<ArrayList<Integer>> changedCellIndexes = new ArrayList<>();

		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				Object tableValue = model.getValueAt(row, column);
				Object dataValue = data[row][column];
				if (column != 0 && !Objects.equals(tableValue, dataValue)) {
//					changedCellIndexes.add(row*8 + (column-1));// for each 9 cell minus one
					ArrayList<Integer> changedCellIndex = new ArrayList<>();
					changedCellIndex.add(row);
					changedCellIndex.add(column);
					changedCellIndexes.add(changedCellIndex);
//					System.out.println((row-2)*8+(column-1));
//					System.out.println(column);
				}
			}
		}

		return changedCellIndexes;
	}

	private void loadMemory(ScanResult sr) {
		try {
			// delete all rows
			DefaultTableModel model = (DefaultTableModel) memory_.getModel();
			model.setRowCount(0); // This removes all rows from the table

			byte[] epc = sr.EPC;
			lastEpc = epc;
			int epclen = sr.EpcLength;
			if (epc.length != epclen) {
				epc = Arrays.copyOf(epc, epclen);
			}

			// Load EPC
			// Create an array to hold the data for the new row

			uneditableRows.add(1); // Makes the first row uneditable

			status("Reading EPC");
			int numOfBytes = sr.EPC.length / 2;
			Object[] epcArray = new Object[numOfBytes + 1];
			epcArray[0] = "EPC";

			for (int i = 0; i < sr.EPC.length; i += 2) {
				byte[] word = Arrays.copyOfRange(sr.EPC, i, i + 2);
				String hex = jence.swing.app.UhfApp.driver_.toHex(word);

				int index = i / 2;
				epcArray[index + 1] = hex;
			}

			status("Reading TID");
			byte[] tid = jence.swing.app.UhfApp.driver_.getTID(epc);
			if (tid == null) {
				status("Could not retrieve TID");
			}

			int numOfTIDBytes = tid.length / 2;
			Object[] tidArray = new Object[numOfTIDBytes + 1];
			tidArray[0] = "TID";

			for (int i = 0; i < tid.length; i += 2) {
				byte[] word = Arrays.copyOfRange(tid, i, i + 2);
				String hex = jence.swing.app.UhfApp.driver_.toHex(word);

				int index = i / 2;
				tidArray[index + 1] = hex;
			}

			model.addRow(epcArray);
			model.addRow(tidArray);
			BoldCellRenderer cellRenderer = new BoldCellRenderer();
			cellRenderer.setBoldCell(0, 0); // Row 0, Column 1
			cellRenderer.setBoldCell(1, 0);

			J4210U.TagInfo ti = jence.swing.app.UhfApp.driver_.getTagInfo(tid);
//			System.out.println(Math.ceil((ti.userlen / 2) / 8));

			int rowNum = (int) (Math.ceil((ti.userlen / 2) / 8) == 0 ? 1 : Math.ceil((ti.userlen / 2) / 8));

			for (int row = 0; row < rowNum; row++) {
				Object[] usrRowArray = new Object[8 + 1];
				usrRowArray[0] = String.format("User [%d...%d]", (row * 8), (row + 1) * 8 - 1);
				for (int col = 0; col <= 7; col++) {
					if (row * 8 + col < ti.userlen / 2) {
						byte[] word = new byte[2];
						word = jence.swing.app.UhfApp.driver_.readWord(epc, row * 8 + col);
						String hex = jence.swing.app.UhfApp.driver_.toHex(word);
						usrRowArray[col + 1] = hex;
						status("Reading User Memory address " + (row * 8 + col));

					} else {
						usrRowArray[col + 1] = null; // empty cells of the last row
					}
				}
				model.addRow(usrRowArray);
				cellRenderer.setBoldCell(row + 2, 0);

			}
			status("Memory Load completed.");

			// TODO: Create a default model for bold on change and bold heading
			for (int i = 0; i < memory_.getColumnCount(); i++) {
				memory_.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
			}

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

			tabFolder.setSelectedIndex(1); // selects MEMORY folder
			cellValues = getTableData(memory_);
			for (int i = 0; i < memory_.getColumnCount(); i++) {
				TableColumn column = memory_.getColumnModel().getColumn(i);
				column.setPreferredWidth(80);
				column.setMinWidth(80);
				column.setMaxWidth(135);
			}

		} catch (Exception e) {
			try {
				UhfApp.prompt(e.getLocalizedMessage() + UhfApp.driver_.error(), "Information", 3,
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void getMemoryDetail() {
		UhfAppFrame.editedValue.clear();
		int selectionRow = inventory_.getSelectedRow();
		Object[] rowData = null;
		if (selectionRow >= 0) {
			tabFolder.setSelectedIndex(1); // go to memory folder

			DefaultTableModel model = (DefaultTableModel) inventory_.getModel();
			int columnCount = model.getColumnCount();
			rowData = new Object[columnCount];
			for (int i = 0; i < columnCount; i++) {
				rowData[i] = model.getValueAt(selectionRow, i);
			}

		} else {
			jence.swing.app.UhfApp.prompt("You must select a row in the inventory result to get memory detail.",
					"Warning", 0, JOptionPane.WARNING_MESSAGE);
			return;
		}

		ScanResult sr = convertToScanResult(rowData);
//		System.out.println(sr.toString());

		loadMemory(sr);

	}

	private void rawWrite(byte[] epc) {
		status("Writing EPC...");
		ArrayList<ArrayList<Integer>> changedIndexes;
		changedIndexes = getChangedCellIndexes(cellValues, memory_);
		DefaultTableModel model = (DefaultTableModel) memory_.getModel();
		int rowCount = model.getRowCount();
		int columnCount = model.getColumnCount();

		for (ArrayList<Integer> indexes : changedIndexes) {
			int rowNum = indexes.get(0);
			int colNum = indexes.get(1);

			int memidx;
			byte[] word = new byte[2];

			// if user mem
			if (rowNum >= 2) {
				memidx = (rowNum - 2) * 8 + (colNum - 1);

				// Fetch the value from the changed columns
				// Set change request
				String data = model.getValueAt(rowNum, colNum).toString();
//				System.out.println(data);
				int d = Integer.parseInt(data, 16);
				word[1] = (byte) (d & 0xFF);
				word[0] = (byte) ((d >> 8) & 0xFF);

				// Write the word to the specified memory index
				try {
					jence.swing.app.UhfApp.driver_.writeWord(epc, word, memidx);
					cellValues = getTableData(memory_); // updating the comparing value
					status("Memory Write Operation is successfull.");
				} catch (Exception e) {
					// TODO: handle exception
					status("Write failed! please try again.");
				}
			}
			// if epc changed
			if (rowNum == 0) {
				byte[] changedWord = new byte[2];
				memidx = (colNum - 1);
				String changedWordString = model.getValueAt(rowNum, colNum).toString();
				System.out.println(changedWordString);
				System.out.println(memidx);
				int h = (memidx + 1) * 2 - 2; // sub 1 for array index 0 , 1 for being 1st byte of the word
				int l = (memidx + 1) * 2 - 1; // sub 1 for array
				int changedWordByte = Integer.parseInt(changedWordString, 16); // parsing to hex
				changedWord[1] = (byte) (changedWordByte & 0xFF);
				changedWord[0] = (byte) ((changedWordByte >> 8) & 0xFF);

				// Write the word to the specified memory index
				try {
//					System.out.println(J4210U.toHex(epc));

					final int index = memidx;
					jence.swing.app.UhfApp.driver_.writeEpcWord(epc, changedWord, memidx);

					epc[h] = changedWord[0];
					epc[l] = changedWord[1];
					System.out.println("");
					String epcString = "";
					for (int i = 1; i <= 6; i++) {
						epcString += model.getValueAt(0, i).toString();
					}
//					System.out.println(epcString);
					lastEpc = J4210U.hex2bytes(epcString, 12);
					cellValues = getTableData(memory_);
					status("EPC Write Operation is successfull.");
				} catch (Exception e) {
					// TODO: handle exception
					status("Write failed! please try again.");
				}

			}
		}
		editedValue.clear();
		memory_.repaint();

	}

	public UhfAppFrame() {
		setIconImage(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/UhfApp32.png")).getImage());
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 1040 };
		gridBagLayout.rowHeights = new int[] { 75, 45, 455, 25 };
		gridBagLayout.columnWeights = new double[] { 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
		getContentPane().setLayout(gridBagLayout);

		JPanel head = new JPanel();
		head.setBackground(new Color(240, 240, 240));
		head.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btnHelp_ = new JButton(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/help.png")));
		btnHelp_.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				openHelpDialog();
			}
		});
		btnHelp_.setMargin(new Insets(11, 5, 11, 5));
		btnHelp_.setText("Help");
		head.add(btnHelp_);

		JPanel usrPortBaudPanel = new JPanel();
		head.add(usrPortBaudPanel);
		GridBagLayout gbl_usrPortBaudPanel = new GridBagLayout();
		gbl_usrPortBaudPanel.columnWidths = new int[] { 130, 0 };
		gbl_usrPortBaudPanel.rowHeights = new int[] { 28, 25, 0 };
		gbl_usrPortBaudPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_usrPortBaudPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		usrPortBaudPanel.setLayout(gbl_usrPortBaudPanel);

		JPanel portPanel = new JPanel();
		GridBagConstraints gbc_portPanel = new GridBagConstraints();
		gbc_portPanel.fill = GridBagConstraints.BOTH;
		gbc_portPanel.insets = new Insets(0, 0, 5, 0);
		gbc_portPanel.gridx = 0;
		gbc_portPanel.gridy = 0;
		usrPortBaudPanel.add(portPanel, gbc_portPanel);
		GridBagLayout gbl_portPanel = new GridBagLayout();
		gbl_portPanel.columnWidths = new int[] { 60, 120, 0 };
		gbl_portPanel.rowHeights = new int[] { 25, 0 };
		gbl_portPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_portPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		portPanel.setLayout(gbl_portPanel);

		JLabel lblPort = new JLabel("Port");
		lblPort.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/usb16.png")));
		lblPort.setSize(new Dimension(10, 10));
		GridBagConstraints gbc_lblPort = new GridBagConstraints();
		gbc_lblPort.fill = GridBagConstraints.BOTH;
		gbc_lblPort.insets = new Insets(0, 0, 0, 5);
		gbc_lblPort.gridx = 0;
		gbc_lblPort.gridy = 0;
		portPanel.add(lblPort, gbc_lblPort);

		comboPorts_ = new JComboBox<String>();
		comboPorts_.setPreferredSize(new Dimension(120, 20));
		comboPorts_.setMinimumSize(new Dimension(111, 20));
		GridBagConstraints gbc_comboPorts_ = new GridBagConstraints();
		gbc_comboPorts_.fill = GridBagConstraints.BOTH;
		gbc_comboPorts_.gridx = 1;
		gbc_comboPorts_.gridy = 0;
		portPanel.add(comboPorts_, gbc_comboPorts_);

		JPanel baudPanel = new JPanel();
		GridBagConstraints gbc_baudPanel = new GridBagConstraints();
		gbc_baudPanel.fill = GridBagConstraints.BOTH;
		gbc_baudPanel.gridx = 0;
		gbc_baudPanel.gridy = 1;
		usrPortBaudPanel.add(baudPanel, gbc_baudPanel);
		GridBagLayout gbl_baudPanel = new GridBagLayout();
		gbl_baudPanel.columnWidths = new int[] { 60, 120, 0 };
		gbl_baudPanel.rowHeights = new int[] { 28, 0 };
		gbl_baudPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_baudPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		baudPanel.setLayout(gbl_baudPanel);

		JLabel lblBaud_1 = new JLabel("Baud");

		lblBaud_1.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/baud16.png")));

		GridBagConstraints gbc_lblBaud_1 = new GridBagConstraints();
		gbc_lblBaud_1.fill = GridBagConstraints.BOTH;
		gbc_lblBaud_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblBaud_1.gridx = 0;
		gbc_lblBaud_1.gridy = 0;
		baudPanel.add(lblBaud_1, gbc_lblBaud_1);

		comboBaudrate_ = new JComboBox(new String[] { "57600", "115200" });
		comboBaudrate_.setPreferredSize(new Dimension(120, 20));
		comboBaudrate_.setMinimumSize(new Dimension(111, 20));
		// this can be set by detecting device
		comboBaudrate_.setSelectedIndex(0);
		GridBagConstraints gbc_comboBaudrate_ = new GridBagConstraints();
		gbc_comboBaudrate_.fill = GridBagConstraints.BOTH;
		gbc_comboBaudrate_.gridx = 1;
		gbc_comboBaudrate_.gridy = 0;
		baudPanel.add(comboBaudrate_, gbc_comboBaudrate_);

		btnRefresh_ = new JButton(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/usb.png")));
		btnRefresh_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (portlist()) {
					btnConnect_.setEnabled(true);
					status("Completed listing available ports.");
				} else {

				}
			}
		});
		btnRefresh_.setMargin(new Insets(11, 5, 11, 5));
		btnRefresh_.setText("Refresh");
		btnRefresh_.setToolTipText("Refresh");
		head.add(btnRefresh_);

		btnConnect_ = new JButton(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/connect.png")));
		btnConnect_.setMultiClickThreshhold(1L);
		btnConnect_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (connect(false)) {
					System.out.println("Connected");
					setEnabled(true, btnDisconnect_, btnScan_, btnScanOnTrigger_, btnScanServer_, tabFolder, memory_,
							inventory_);
					setEnabled(false, btnConnect_, btnRefresh_);
					UhfApp.LAST_USE_SERIAL_PORT = comboPorts_.getSelectedItem().toString();
					setPanelsEnabled(true, panelInventory, penelMemory, panelInfo, panelGPIO, panelMessaging);
				}
			}
		});

		btnConnect_.setEnabled(false);
		btnConnect_.setMargin(new Insets(11, 5, 11, 5));
		btnConnect_.setText("Connect");
		head.add(btnConnect_);

		btnDisconnect_ = new JButton(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/disconnect.png")));
		btnDisconnect_.setMultiClickThreshhold(1L);
		btnDisconnect_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//					monitorStop();
				if (disconnect()) {
//						btnScanServer_.setSelection(false);
					setEnabled(true, btnRefresh_, btnConnect_);
					setEnabled(false, btnDisconnect_, btnScan_, btnScanServer_, btnScanOnTrigger_, tabFolder,
							inventory_, memory_);
					btnScanServer_.setSelected(false);
					btnScanOnTrigger_.setSelected(false);
					status("Disconnected.");
					setPanelsEnabled(false, panelInventory, penelMemory, panelInfo, panelGPIO, panelMessaging);
				}

			}
		});
		btnDisconnect_.setEnabled(false);
		btnDisconnect_.setMargin(new Insets(11, 5, 11, 5));
		btnDisconnect_.setText("Disconnect");
		head.add(btnDisconnect_);

		btnScan_ = new JButton(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/scan.png")));
		btnScan_.setMultiClickThreshhold(1L);
		btnScan_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(false, btnScanServer_, btnScanOnTrigger_);
				if (scan()) {
					// setEnabled(true,);
				}
				setEnabled(true, btnScanServer_, btnScanOnTrigger_);

			}
		});
		btnScan_.setEnabled(false);
		btnScan_.setMargin(new Insets(11, 5, 11, 5));
		btnScan_.setText("Scan");
		head.add(btnScan_);

		btnScanServer_ = new JToggleButton(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/scans.png")));
		btnScanServer_.setEnabled(false);
		btnScanServer_.setMargin(new Insets(11, 5, 11, 5));
		btnScanServer_.setText("Scan Server");
		btnScanServer_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				monitorStop();

				setEnabled(false, btnScan_);
				scans();
				setEnabled(true, btnScan_);

			}
		});
		head.add(btnScanServer_);

		btnScanOnTrigger_ = new JToggleButton(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/scant.png")));
		btnScanOnTrigger_.setEnabled(false);
		btnScanOnTrigger_.setMargin(new Insets(11, 5, 11, 5));
		btnScanOnTrigger_.setText("Scan On Trigger");
		btnScanOnTrigger_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				monitorStop();
				scant();
			}
		});
		head.add(btnScanOnTrigger_);
		GridBagConstraints gbc_head = new GridBagConstraints();
		gbc_head.ipady = 5;
		gbc_head.ipadx = 5;
		gbc_head.anchor = GridBagConstraints.NORTHWEST;
		gbc_head.gridheight = 4;
		gbc_head.weighty = 1.0;
		gbc_head.insets = new Insets(5, 5, 0, 0);
		gbc_head.gridx = 0;
		gbc_head.gridy = 0;
		getContentPane().add(head, gbc_head);

		txtSupportedChips_ = new JTextField();
		txtSupportedChips_.setDropMode(DropMode.INSERT);
		txtSupportedChips_.setEditable(false);

		txtSupportedChips_.isEditable();
		GridBagConstraints gbc_txtSupportedChips_ = new GridBagConstraints();
		gbc_txtSupportedChips_.ipadx = 10;
		gbc_txtSupportedChips_.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSupportedChips_.anchor = GridBagConstraints.NORTH;
		gbc_txtSupportedChips_.insets = new Insets(5, 5, 5, 0);
		gbc_txtSupportedChips_.gridx = 0;
		gbc_txtSupportedChips_.gridy = 1;
		getContentPane().add(txtSupportedChips_, gbc_txtSupportedChips_);
		txtSupportedChips_.setColumns(10);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < AUTODETECTED_CHIPS.length; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append(AUTODETECTED_CHIPS[i]);
		}
		txtSupportedChips_.setText(sb.toString());

		tabs = new JPanel();
		tabs.setBackground(new Color(240, 240, 240));
		GridBagConstraints gbc_tabs = new GridBagConstraints();
		gbc_tabs.fill = GridBagConstraints.BOTH;
		gbc_tabs.ipadx = 10;
		gbc_tabs.gridx = 0;
		gbc_tabs.gridy = 2;
		getContentPane().add(tabs, gbc_tabs);

		tabFolder = new JTabbedPane();
		tabFolder.setBackground(new Color(240, 240, 240));
		tabFolder.setBorder(new EmptyBorder(0, 10, 0, 10));
		tabFolder.setEnabled(false);

		tabFolder.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				status("");
				// Get the index of the selected tab
				int selectedIndex = tabFolder.getSelectedIndex();
				// Perform actions based on the selected tab index
				if (selectedIndex != 2) {
					return;
				}
				try {
					settings(); // Call settings() method
				} catch (Exception ex) {
					status("");
					UhfApp.prompt(ex.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);

				}
			}
		});

		tabFolder.setPreferredSize(new Dimension(1050, 400));
		tabFolder.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		panelInventory = new JPanel();
		panelInventory.setEnabled(false);
		panelInventory.setPreferredSize(new Dimension(1, 50));

		tabFolder.addTab("INVENTORY", null, panelInventory, null);
		GridBagLayout gbl_panelInventory = new GridBagLayout();
		gbl_panelInventory.columnWidths = new int[] { 900 };
		gbl_panelInventory.rowHeights = new int[] { 60, 300 };
		gbl_panelInventory.columnWeights = new double[] { 1.0 };
		gbl_panelInventory.rowWeights = new double[] { 0.0, 1.0 };
		panelInventory.setLayout(gbl_panelInventory);

		JPanel controlPanel = new JPanel();
		FlowLayout fl_controlPanel = (FlowLayout) controlPanel.getLayout();
		fl_controlPanel.setAlignment(FlowLayout.LEFT);
		controlPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		controlPanel.setBackground(new Color(240, 240, 240));
		GridBagConstraints gbc_controlPanel = new GridBagConstraints();
		gbc_controlPanel.insets = new Insets(0, 0, 5, 0);
		gbc_controlPanel.fill = GridBagConstraints.BOTH;
		gbc_controlPanel.gridx = 0;
		gbc_controlPanel.gridy = 0;
		panelInventory.add(controlPanel, gbc_controlPanel);

		JButton btnAuth_ = new JButton("Auth");
		btnAuth_.setMargin(new Insets(8, 5, 8, 5));
		btnAuth_.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/key.png")));

		btnAuth_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (hasSelectedCell() == true) {
					byte[] epc;
					if (lastEpc == null) {
						UhfApp.prompt("No Selected EPC", "Error", 1, JOptionPane.ERROR_MESSAGE);
					}
					try {
						epc = lastEpc;
						openAuthDialog(epc, password_, killpass_, true);
					} catch (Exception err) {
						UhfApp.prompt(err.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);
					}

				}

			}
		});
		controlPanel.add(btnAuth_);

		JButton btnClear_ = new JButton("Clear");
		btnClear_.setMargin(new Insets(8, 5, 8, 5));
		btnClear_.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/clean.png")));
		btnClear_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) inventory_.getModel();
				model.setRowCount(0); // This will remove all rows from the table
			}
		});
		controlPanel.add(btnClear_);

		btnMerge_ = new JToggleButton("Merge");
		btnMerge_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				merge_ = btnMerge_.isSelected();
//				setMerge(btnMerge_.isSelected());

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setMerge(btnMerge_.isSelected());
					}
				});

//				System.out.println(btnMerge_.isSelected());
			}
		});
		btnMerge_.setMargin(new Insets(8, 5, 8, 5));
		btnMerge_.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/merge.png")));
		controlPanel.add(btnMerge_);

		JToggleButton btnLog = new JToggleButton("Log");
		btnLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UhfApp.driver_.setLog(btnLog.isEnabled());
			}
		});
		btnLog.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/log.png")));
		btnLog.setMargin(new Insets(8, 5, 8, 5));
		controlPanel.add(btnLog);

		JPanel grpMode_ = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) grpMode_.getLayout();
		grpMode_.setPreferredSize(new Dimension(95, 50));
		grpMode_.setBorder(new TitledBorder(null, "Scan Mode", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanel.add(grpMode_);

		btnSingleTag_ = new JCheckBox("Single Tag");
		btnSingleTag_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btnSingleTag_.isSelected()) {
					UhfApp.prompt("This feature is experimental.\n\nWhen Scan button is pressed, "
							+ "only one tag is selected that the reader receives first. This does not mean that the tag is the "
							+ "closest. Dont use this option to find the nearest tag. Instead, you can use RSSI with Transmit "
							+ "Power to determine the nearest tag.", "Information", 3, JOptionPane.INFORMATION_MESSAGE);
				}

			}
		});
		grpMode_.add(btnSingleTag_);

		JPanel grpIterationsPerScan_ = new JPanel();
		grpIterationsPerScan_.setPreferredSize(new Dimension(120, 50));
		grpIterationsPerScan_.setBorder(
				new TitledBorder(null, "Iteration Per Scan", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanel.add(grpIterationsPerScan_);

		comboIterations_ = new JSpinner();
		comboIterations_.setPreferredSize(new Dimension(97, 20));
		comboIterations_.setModel(new SpinnerNumberModel(1, 1, 100, 1));
		grpIterationsPerScan_.add(comboIterations_);

		JPanel grpFilter_ = new JPanel();
		FlowLayout flowLayout = (FlowLayout) grpFilter_.getLayout();
		flowLayout.setHgap(10);
		flowLayout.setAlignment(FlowLayout.LEFT);
		grpFilter_.setBorder(new TitledBorder(null, "Filter", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanel.add(grpFilter_);

		btnUseFilter = new JCheckBox("Use Filter");
		grpFilter_.add(btnUseFilter);

		JLabel lblOffset_ = new JLabel("Offset");
		grpFilter_.add(lblOffset_);

		offset_ = new JTextField();
		grpFilter_.add(offset_);
		offset_.setColumns(10);

		JLabel lblData_ = new JLabel("Data");
		grpFilter_.add(lblData_);

		filter_ = new JTextField();
		grpFilter_.add(filter_);
		filter_.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBackground(new Color(240, 240, 240));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panelInventory.add(scrollPane, gbc_scrollPane);

		inventory_ = new JTable();
		inventory_.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		inventory_.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) { // Check for double-click
					getMemoryDetail();
				}
			}
		});
		inventory_.setBorder(new MatteBorder(0, 0, 0, 0, (Color) UIManager.getColor("Button.light")));
		inventory_.setAutoCreateRowSorter(true);
		model = new DefaultTableModel(new Object[][] {},
				new String[] { "Index", "EPC", "LEN", "ANT", "Times", "RSSI", "" }) {
			Class[] columnTypes = new Class[] { Object.class, Object.class, Object.class, Object.class, Object.class,
					Object.class, String.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			boolean[] columnEditables = new boolean[] { false, false, false, false, false, false, false };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		};

		inventory_.setModel(model);
		inventory_.getColumnModel().getColumn(5).setResizable(false);

		inventory_.getColumnModel().getColumn(0).setPreferredWidth(40);
		inventory_.getColumnModel().getColumn(1).setPreferredWidth(250);
		inventory_.getColumnModel().getColumn(2).setPreferredWidth(40);
		inventory_.getColumnModel().getColumn(3).setPreferredWidth(40);
		inventory_.getColumnModel().getColumn(4).setPreferredWidth(40);
		inventory_.getColumnModel().getColumn(5).setPreferredWidth(40);
		inventory_.getColumnModel().getColumn(6).setPreferredWidth(40);

		inventory_.getColumnModel().getColumn(0).setMaxWidth(100);
		inventory_.getColumnModel().getColumn(1).setMaxWidth(350);
		inventory_.getColumnModel().getColumn(2).setMaxWidth(150);
		inventory_.getColumnModel().getColumn(3).setMaxWidth(150);
		inventory_.getColumnModel().getColumn(4).setMaxWidth(150);
		inventory_.getColumnModel().getColumn(5).setMaxWidth(150);
		inventory_.getColumnModel().getColumn(6).setMaxWidth(850);
		// TODO: increase column width

		// Set the horizontal alignment to center
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		inventory_.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		inventory_.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		inventory_.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		inventory_.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		inventory_.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

		inventory_.setEnabled(false);
		inventory_.getTableHeader().setReorderingAllowed(false);
//        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
		inventory_.setRowSorter(null);

		scrollPane.setViewportView(inventory_);

		penelMemory = new JPanel();
		penelMemory.setEnabled(false);
		tabFolder.addTab("MEMORY", null, penelMemory, null);
		GridBagLayout gbl_penelMemory = new GridBagLayout();
		gbl_penelMemory.columnWidths = new int[] { 0 };
		gbl_penelMemory.rowHeights = new int[] { 90, 70, 240, 0 };
		gbl_penelMemory.columnWeights = new double[] { 1.0 };
		gbl_penelMemory.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		penelMemory.setLayout(gbl_penelMemory);

		JPanel operationPanel = new JPanel();
		operationPanel
				.setBorder(new TitledBorder(null, "Operations", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_operationPanel = new GridBagConstraints();
		gbc_operationPanel.insets = new Insets(0, 0, 5, 0);
		gbc_operationPanel.fill = GridBagConstraints.BOTH;
		gbc_operationPanel.gridx = 0;
		gbc_operationPanel.gridy = 0;
		penelMemory.add(operationPanel, gbc_operationPanel);
		operationPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JButton btnNdefRead_ = new JButton("Refresh");
		btnNdefRead_.setMargin(new Insets(0, 0, 0, 0));
		btnNdefRead_.setPreferredSize(new Dimension(120, 50));
		btnNdefRead_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getMemoryDetail();
			}
		});
		btnNdefRead_.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/read.png")));
		operationPanel.add(btnNdefRead_);

		JButton btnNdefWrite_ = new JButton("Write");
		btnNdefWrite_.setMargin(new Insets(0, 0, 0, 0));
		btnNdefWrite_.setPreferredSize(new Dimension(100, 50));
		btnNdefWrite_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rawWrite(lastEpc);

			}
		});
		btnNdefWrite_.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/write.png")));
		operationPanel.add(btnNdefWrite_);

		JButton btnNdefClean_ = new JButton("Clean");
		btnNdefClean_.setMargin(new Insets(0, 0, 0, 0));
		btnNdefClean_.setPreferredSize(new Dimension(100, 50));
		btnNdefClean_.setEnabled(false);
		btnNdefClean_.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/clean.png")));
		operationPanel.add(btnNdefClean_);

		JButton btnAuthWrite_ = new JButton("Auth");
		btnAuthWrite_.setMargin(new Insets(0, 0, 0, 0));
		btnAuthWrite_.setPreferredSize(new Dimension(100, 50));
		btnAuthWrite_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byte[] epc;
				// TODO: find selected sell and extract EPC
				if (lastEpc == null) {
					UhfApp.prompt("No Selected EPC", "Error", 1, JOptionPane.ERROR_MESSAGE);
				}
				try {
					epc = lastEpc;
					openAuthDialog(epc, password_, killpass_, false);
				} catch (Exception err) {
					UhfApp.prompt(err.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		btnAuthWrite_.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/key.png")));
		operationPanel.add(btnAuthWrite_);

		JButton btnExists_ = new JButton("Exists");
		btnExists_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				byte[] epc;
				try {
					epc = lastEpc;
					boolean found = UhfApp.driver_.exists(epc);
					if (found)
						UhfApp.prompt("Tag FOUND in the inventory", "TAG FOUND!", 3, JOptionPane.INFORMATION_MESSAGE);
					else
						UhfApp.prompt("Tag NOT FOUND near the reader", "TAG NOT FOUND!", 3,
								JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					UhfApp.prompt(ex.getLocalizedMessage(), "Error!", 1, JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		btnExists_.setMargin(new Insets(0, 0, 0, 0));
		btnExists_.setPreferredSize(new Dimension(100, 50));
		btnExists_.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/cardread.png")));
		operationPanel.add(btnExists_);

		JPanel tagInfo = new JPanel();
		GridBagConstraints gbc_tagInfo = new GridBagConstraints();
		gbc_tagInfo.insets = new Insets(0, 0, 5, 0);
		gbc_tagInfo.fill = GridBagConstraints.HORIZONTAL;
		gbc_tagInfo.gridx = 0;
		gbc_tagInfo.gridy = 1;
		penelMemory.add(tagInfo, gbc_tagInfo);
		GridBagLayout gbl_tagInfo = new GridBagLayout();
		gbl_tagInfo.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_tagInfo.rowHeights = new int[] { 30, 25, 0 };
		gbl_tagInfo.columnWeights = new double[] { 0.0, 1.0, 4.0, 0.0, 1.0, 4.0, 0.0, 1.0, 4.0, Double.MIN_VALUE };
		gbl_tagInfo.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		tagInfo.setLayout(gbl_tagInfo);

		JLabel lblNewLabel = new JLabel("Chip Type");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		tagInfo.add(lblNewLabel, gbc_lblNewLabel);

		chip_ = new JTextField();
		chip_.setBackground(new Color(255, 255, 255));
		chip_.setEnabled(false);
		chip_.setText("");
		chip_.setBounds(new Rectangle(5, 5, 0, 0));

		Border paddingBorder = new EmptyBorder(2, 2, 2, 2);
		Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
		chip_.setBorder(new CompoundBorder(blackBorder, paddingBorder));
//		chip_.setBorder(new LineBorder(new Color(0, 0, 0)));
		chip_.setEditable(false);
		GridBagConstraints gbc_chip_ = new GridBagConstraints();
		gbc_chip_.ipady = 2;
		gbc_chip_.ipadx = 2;
		gbc_chip_.fill = GridBagConstraints.BOTH;
		gbc_chip_.gridwidth = 2;
		gbc_chip_.insets = new Insets(0, 0, 5, 5);
		gbc_chip_.gridx = 1;
		gbc_chip_.gridy = 0;
		tagInfo.add(chip_, gbc_chip_);
		chip_.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("Total Memory");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridx = 3;
		gbc_lblNewLabel_1.gridy = 0;
		tagInfo.add(lblNewLabel_1, gbc_lblNewLabel_1);

		total_ = new JTextField();
		total_.setBackground(new Color(255, 255, 255));
		total_.setBorder(new CompoundBorder(blackBorder, paddingBorder));

		total_.setEditable(false);
		GridBagConstraints gbc_total_ = new GridBagConstraints();
		gbc_total_.insets = new Insets(0, 0, 5, 5);
		gbc_total_.fill = GridBagConstraints.BOTH;
		gbc_total_.gridx = 4;
		gbc_total_.gridy = 0;
		tagInfo.add(total_, gbc_total_);
		total_.setColumns(10);

		total2_ = new JTextField();
		total2_.setBorder(new CompoundBorder(blackBorder, paddingBorder));

		total2_.setBackground(new Color(255, 255, 0));
		total2_.setEditable(false);
		GridBagConstraints gbc_total2_ = new GridBagConstraints();
		gbc_total2_.insets = new Insets(0, 0, 5, 5);
		gbc_total2_.fill = GridBagConstraints.BOTH;
		gbc_total2_.gridx = 5;
		gbc_total2_.gridy = 0;
		tagInfo.add(total2_, gbc_total2_);
		total2_.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("PWD Size");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 6;
		gbc_lblNewLabel_2.gridy = 0;
		tagInfo.add(lblNewLabel_2, gbc_lblNewLabel_2);

		pwdlen_ = new JTextField();
		pwdlen_.setBackground(new Color(255, 255, 255));
		pwdlen_.setBorder(new CompoundBorder(blackBorder, paddingBorder));
		pwdlen_.setEditable(false);
		GridBagConstraints gbc_pwdlen_ = new GridBagConstraints();
		gbc_pwdlen_.insets = new Insets(0, 0, 5, 5);
		gbc_pwdlen_.fill = GridBagConstraints.BOTH;
		gbc_pwdlen_.gridx = 7;
		gbc_pwdlen_.gridy = 0;
		tagInfo.add(pwdlen_, gbc_pwdlen_);
		pwdlen_.setColumns(10);

		pwdlen2_ = new JTextField();
		pwdlen2_.setBorder(new CompoundBorder(blackBorder, paddingBorder));
		pwdlen2_.setBackground(new Color(255, 255, 0));
		pwdlen2_.setEditable(false);
		GridBagConstraints gbc_pwdlen2_ = new GridBagConstraints();
		gbc_pwdlen2_.insets = new Insets(0, 0, 5, 0);
		gbc_pwdlen2_.fill = GridBagConstraints.BOTH;
		gbc_pwdlen2_.gridx = 8;
		gbc_pwdlen2_.gridy = 0;
		tagInfo.add(pwdlen2_, gbc_pwdlen2_);
		pwdlen2_.setColumns(10);

		JLabel lblNewLabel_3 = new JLabel("EPC Size");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 1;
		tagInfo.add(lblNewLabel_3, gbc_lblNewLabel_3);

		epclen_ = new JTextField();
		epclen_.setBackground(new Color(255, 255, 255));
		epclen_.setBounds(new Rectangle(5, 5, 0, 0));
		epclen_.setBorder(new CompoundBorder(blackBorder, paddingBorder));
		epclen_.setEditable(false);
		GridBagConstraints gbc_epclen_ = new GridBagConstraints();
		gbc_epclen_.insets = new Insets(0, 0, 0, 5);
		gbc_epclen_.fill = GridBagConstraints.BOTH;
		gbc_epclen_.gridx = 1;
		gbc_epclen_.gridy = 1;
		tagInfo.add(epclen_, gbc_epclen_);
		epclen_.setColumns(10);

		epclen2_ = new JTextField();
		epclen2_.setBorder(new CompoundBorder(blackBorder, paddingBorder));
		epclen2_.setBackground(new Color(255, 255, 0));
		epclen2_.setEditable(false);
		GridBagConstraints gbc_epclen2_ = new GridBagConstraints();
		gbc_epclen2_.fill = GridBagConstraints.BOTH;
		gbc_epclen2_.insets = new Insets(0, 0, 0, 5);
		gbc_epclen2_.gridx = 2;
		gbc_epclen2_.gridy = 1;
		tagInfo.add(epclen2_, gbc_epclen2_);
		epclen2_.setColumns(10);

		JLabel lblNewLabel_4 = new JLabel("TID Size");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4.gridx = 3;
		gbc_lblNewLabel_4.gridy = 1;
		tagInfo.add(lblNewLabel_4, gbc_lblNewLabel_4);

		tidlen_ = new JTextField();
		tidlen_.setBackground(new Color(255, 255, 255));
		tidlen_.setBorder(new CompoundBorder(blackBorder, paddingBorder));
		tidlen_.setEditable(false);
		GridBagConstraints gbc_tidlen_ = new GridBagConstraints();
		gbc_tidlen_.insets = new Insets(0, 0, 0, 5);
		gbc_tidlen_.fill = GridBagConstraints.BOTH;
		gbc_tidlen_.gridx = 4;
		gbc_tidlen_.gridy = 1;
		tagInfo.add(tidlen_, gbc_tidlen_);
		tidlen_.setColumns(10);

		tidlen2_ = new JTextField();
		tidlen2_.setBorder(new CompoundBorder(blackBorder, paddingBorder));
		tidlen2_.setBackground(new Color(255, 255, 0));
		tidlen2_.setEditable(false);
		GridBagConstraints gbc_tidlen2_ = new GridBagConstraints();
		gbc_tidlen2_.insets = new Insets(0, 0, 0, 5);
		gbc_tidlen2_.fill = GridBagConstraints.BOTH;
		gbc_tidlen2_.gridx = 5;
		gbc_tidlen2_.gridy = 1;
		tagInfo.add(tidlen2_, gbc_tidlen2_);
		tidlen2_.setColumns(10);

		JLabel lblNewLabel_5 = new JLabel("USER Size");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_5.gridx = 6;
		gbc_lblNewLabel_5.gridy = 1;
		tagInfo.add(lblNewLabel_5, gbc_lblNewLabel_5);

		usrlen_ = new JTextField();
		usrlen_.setBackground(new Color(255, 255, 255));
		usrlen_.setBorder(new CompoundBorder(blackBorder, paddingBorder));
		usrlen_.setEditable(false);
		GridBagConstraints gbc_usrlen_ = new GridBagConstraints();
		gbc_usrlen_.insets = new Insets(0, 0, 0, 5);
		gbc_usrlen_.fill = GridBagConstraints.BOTH;
		gbc_usrlen_.gridx = 7;
		gbc_usrlen_.gridy = 1;
		tagInfo.add(usrlen_, gbc_usrlen_);
		usrlen_.setColumns(10);

		usrlen2_ = new JTextField();
		usrlen2_.setBorder(new CompoundBorder(blackBorder, paddingBorder));
		usrlen2_.setBackground(new Color(255, 255, 0));
		usrlen2_.setEditable(false);
		GridBagConstraints gbc_usrlen2_ = new GridBagConstraints();
		gbc_usrlen2_.fill = GridBagConstraints.BOTH;
		gbc_usrlen2_.gridx = 8;
		gbc_usrlen2_.gridy = 1;
		tagInfo.add(usrlen2_, gbc_usrlen2_);
		usrlen2_.setColumns(10);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 2;
		penelMemory.add(scrollPane_1, gbc_scrollPane_1);

		memory_ = new JTable();
		memory_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		memory_.setBackground(new Color(240, 240, 240));
		DefaultTableModel model = new DefaultTableModel(
				new Object[][] { { "EPC", null, null, null, null, null, null, null, null },
						{ "TID", null, null, null, null, null, null, null, null },
						{ null, null, null, null, null, null, null, null, null } },
				new String[] { "Memory", "Word 0", "Word 1", "Word 2", "Word 3", "Word 4", "Word 5", "Word 6",
						"Word 7" }) {
			boolean[] columnEditables = new boolean[] { true, true, true, true, true, true, true, true, true };

			@Override
			public boolean isCellEditable(int row, int column) {
				if (uneditableRows.contains(row) | column == 0) {
					return false; // Make the row uneditable if it's in the set
				}
				return true;
			}
		};

		// Create a custom cell editor to enforce maximum character limit

		// Set the custom cell editor to the editable columns

//		memory_.setDefaultRenderer(Object.class, new BoldCellRenderer());

		// Enable cell selection
		memory_.setCellSelectionEnabled(false);

		// Set custom selection background color for cells
		memory_.setSelectionBackground(Color.YELLOW);
		memory_.setSelectionForeground(Color.RED);
		memory_.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		memory_.setModel(model);

		for (int columnIndex = 0; columnIndex < memory_.getColumnCount(); columnIndex++) {
			// Set the LimitedCharacterCellEditor for each column
			memory_.getColumnModel().getColumn(columnIndex)
					.setCellEditor(new LimitedCharacterCellEditor(memory_, 4, "0123456789ABCDEFabcdef"));
		}

		for (int columnIndex = 0; columnIndex <= 8; columnIndex++) {
			// Set the LimitedCharacterCellEditor for each column
			memory_.getColumnModel().getColumn(columnIndex).setResizable(false);
		}

		memory_.getTableHeader().setReorderingAllowed(false);
		memory_.setEnabled(false);
		scrollPane_1.setViewportView(memory_);

		panelInfo = new JPanel();
		panelInfo.setEnabled(false);
		panelInfo.setLayout(new GridLayout(0, 1, 0, 0));
		info_ = new InfoPanel(this);
		info_.setBorder(new EmptyBorder(10, 5, 5, 5));

		GridBagLayout gbl_infoPanel = (GridBagLayout) info_.getLayout();
		gbl_infoPanel.columnWeights = new double[] { 1.0 };
		gbl_infoPanel.columnWidths = new int[] { 0 };
		panelInfo.add(info_);
		tabFolder.addTab("INFO", null, panelInfo, null);

		panelGPIO = new JPanel();
		panelGPIO.setEnabled(false);
		FlowLayout flowLayout_1 = (FlowLayout) panelGPIO.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		tabFolder.addTab("GPIO", null, panelGPIO, null);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "GP Output", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelGPIO.add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JButton btnSetGpOutput_ = new JButton("Set GP Output");
		btnSetGpOutput_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					gpo();
				} catch (Exception ex) {
					jence.swing.app.UhfApp.prompt(ex.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);

				}

			}
		});
		btnSetGpOutput_.setActionCommand("Set GP Output");
		GridBagConstraints gbc_btnSetGpOutput_ = new GridBagConstraints();
		gbc_btnSetGpOutput_.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSetGpOutput_.gridwidth = 2;
		gbc_btnSetGpOutput_.insets = new Insets(0, 0, 5, 0);
		gbc_btnSetGpOutput_.gridx = 0;
		gbc_btnSetGpOutput_.gridy = 0;
		panel.add(btnSetGpOutput_, gbc_btnSetGpOutput_);

		btnGpo2 = new JCheckBox("GPO2");
		btnGpo2.setActionCommand("");
		GridBagConstraints gbc_btnGpo2 = new GridBagConstraints();
		gbc_btnGpo2.insets = new Insets(0, 0, 0, 5);
		gbc_btnGpo2.gridx = 0;
		gbc_btnGpo2.gridy = 1;
		panel.add(btnGpo2, gbc_btnGpo2);

		btnGpo1 = new JCheckBox("GPO1");
		GridBagConstraints gbc_btnGpo1 = new GridBagConstraints();
		gbc_btnGpo1.gridx = 1;
		gbc_btnGpo1.gridy = 1;
		panel.add(btnGpo1, gbc_btnGpo1);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "GP Input", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelGPIO.add(panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JButton btnGetGpInput = new JButton("Get GP Input");
		btnGetGpInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					gpi();
				} catch (Exception err) {
					jence.swing.app.UhfApp.prompt(err.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR);

				}

			}

		});
		GridBagConstraints gbc_btnGetGpInput = new GridBagConstraints();
		gbc_btnGetGpInput.insets = new Insets(0, 0, 5, 5);
		gbc_btnGetGpInput.gridx = 0;
		gbc_btnGetGpInput.gridy = 0;
		panel_1.add(btnGetGpInput, gbc_btnGetGpInput);

		btnMonitor_ = new JToggleButton("Monitor Input");
		btnMonitor_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				monitor();
			}
		});
		GridBagConstraints gbc_btnMonitor_ = new GridBagConstraints();
		gbc_btnMonitor_.insets = new Insets(0, 0, 5, 0);
		gbc_btnMonitor_.gridx = 1;
		gbc_btnMonitor_.gridy = 0;
		panel_1.add(btnMonitor_, gbc_btnMonitor_);

		btnGpi2 = new JCheckBox("GPI2");
		GridBagConstraints gbc_btnGpi2 = new GridBagConstraints();
		gbc_btnGpi2.insets = new Insets(0, 0, 0, 5);
		gbc_btnGpi2.anchor = GridBagConstraints.WEST;
		gbc_btnGpi2.gridx = 0;
		gbc_btnGpi2.gridy = 1;
		panel_1.add(btnGpi2, gbc_btnGpi2);

		btnGpi1 = new JCheckBox("GPI1");
		GridBagConstraints gbc_btnGpi1 = new GridBagConstraints();
		gbc_btnGpi1.anchor = GridBagConstraints.WEST;
		gbc_btnGpi1.gridx = 1;
		gbc_btnGpi1.gridy = 1;
		panel_1.add(btnGpi1, gbc_btnGpi1);

		panelMessaging = new JPanel();
		panelMessaging.setEnabled(false);
		tabFolder.addTab("Messaging", null, panelMessaging, null);
		GridBagLayout gbl_panelMessaging = new GridBagLayout();
		gbl_panelMessaging.columnWidths = new int[] { 105, 0 };
		gbl_panelMessaging.rowHeights = new int[] { 0, 50, 25, 45, 0, 0 };
		gbl_panelMessaging.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMessaging.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelMessaging.setLayout(gbl_panelMessaging);

		JLabel lblNewLabel_8 = new JLabel(
				"When a tag is detected, its EPC and other relevent information can be sent to one of the following targets:");
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_8.gridx = 0;
		gbc_lblNewLabel_8.gridy = 0;
		panelMessaging.add(lblNewLabel_8, gbc_lblNewLabel_8);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Send JSON message to a remote socket with port.",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(5, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 1;
		panelMessaging.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 20, 25, 0 };
		gbl_panel_2.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		btnActivateJsonMessaging_ = new JCheckBox("Activate This Option");
		GridBagConstraints gbc_btnActivateJsonMessaging_ = new GridBagConstraints();
		gbc_btnActivateJsonMessaging_.anchor = GridBagConstraints.WEST;
		gbc_btnActivateJsonMessaging_.insets = new Insets(0, 0, 5, 0);
		gbc_btnActivateJsonMessaging_.gridx = 0;
		gbc_btnActivateJsonMessaging_.gridy = 0;
		panel_2.add(btnActivateJsonMessaging_, gbc_btnActivateJsonMessaging_);

		JPanel panel_6 = new JPanel();
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.anchor = GridBagConstraints.WEST;
		gbc_panel_6.fill = GridBagConstraints.VERTICAL;
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 1;
		panel_2.add(panel_6, gbc_panel_6);
		GridBagLayout gbl_panel_6 = new GridBagLayout();
		gbl_panel_6.columnWidths = new int[] { 85, 306, 60, 430, 0 };
		gbl_panel_6.rowHeights = new int[] { 20, 0 };
		gbl_panel_6.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel_6.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_6.setLayout(gbl_panel_6);

		JLabel lblIpOrDomain = new JLabel("IP or Domain");
		GridBagConstraints gbc_lblIpOrDomain = new GridBagConstraints();
		gbc_lblIpOrDomain.anchor = GridBagConstraints.WEST;
		gbc_lblIpOrDomain.insets = new Insets(0, 0, 0, 5);
		gbc_lblIpOrDomain.gridx = 0;
		gbc_lblIpOrDomain.gridy = 0;
		panel_6.add(lblIpOrDomain, gbc_lblIpOrDomain);

		textIP_ = new JTextField();
		GridBagConstraints gbc_textIP_ = new GridBagConstraints();
		gbc_textIP_.fill = GridBagConstraints.HORIZONTAL;
		gbc_textIP_.insets = new Insets(0, 0, 0, 5);
		gbc_textIP_.gridx = 1;
		gbc_textIP_.gridy = 0;
		panel_6.add(textIP_, gbc_textIP_);
		textIP_.setColumns(10);

		JLabel lblPort_1 = new JLabel("Port");
		GridBagConstraints gbc_lblPort_1 = new GridBagConstraints();
		gbc_lblPort_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblPort_1.gridx = 2;
		gbc_lblPort_1.gridy = 0;
		panel_6.add(lblPort_1, gbc_lblPort_1);

		textPort_ = new JTextField();
		textPort_.setColumns(10);
		GridBagConstraints gbc_textPort_ = new GridBagConstraints();
		gbc_textPort_.fill = GridBagConstraints.HORIZONTAL;
		gbc_textPort_.gridx = 3;
		gbc_textPort_.gridy = 0;
		panel_6.add(textPort_, gbc_textPort_);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Send to HTPP url as query string.", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(5, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 2;
		panelMessaging.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 85, 0, 0 };
		gbl_panel_3.rowHeights = new int[] { 20, 25, 0 };
		gbl_panel_3.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel_3.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_3.setLayout(gbl_panel_3);

		btnActivateHttpQuery_ = new JCheckBox("Activate This Option");
		GridBagConstraints gbc_btnActivateHttpQuery_ = new GridBagConstraints();
		gbc_btnActivateHttpQuery_.gridwidth = 2;
		gbc_btnActivateHttpQuery_.anchor = GridBagConstraints.WEST;
		gbc_btnActivateHttpQuery_.insets = new Insets(0, 0, 5, 0);
		gbc_btnActivateHttpQuery_.gridx = 0;
		gbc_btnActivateHttpQuery_.gridy = 0;
		panel_3.add(btnActivateHttpQuery_, gbc_btnActivateHttpQuery_);

		JLabel lblHttpsUrl = new JLabel("Http(s) URL:");
		GridBagConstraints gbc_lblHttpsUrl = new GridBagConstraints();
		gbc_lblHttpsUrl.anchor = GridBagConstraints.WEST;
		gbc_lblHttpsUrl.insets = new Insets(0, 0, 0, 5);
		gbc_lblHttpsUrl.gridx = 0;
		gbc_lblHttpsUrl.gridy = 1;
		panel_3.add(lblHttpsUrl, gbc_lblHttpsUrl);

		textHttpUrl_ = new JTextField();
		GridBagConstraints gbc_textHttpUrl_ = new GridBagConstraints();
		gbc_textHttpUrl_.fill = GridBagConstraints.HORIZONTAL;
		gbc_textHttpUrl_.gridx = 1;
		gbc_textHttpUrl_.gridy = 1;
		panel_3.add(textHttpUrl_, gbc_textHttpUrl_);
		textHttpUrl_.setColumns(10);

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(
				new TitledBorder(null, "Write to a File:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.insets = new Insets(5, 0, 5, 0);
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 3;
		panelMessaging.add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] { 85, 306, 0, 430, 0 };
		gbl_panel_4.rowHeights = new int[] { 20, 25, 0 };
		gbl_panel_4.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel_4.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_4.setLayout(gbl_panel_4);

		btnActivateWriteToFile_ = new JCheckBox("Activate This Option (file extension will be .json)");
		GridBagConstraints gbc_btnActivateWriteToFile_ = new GridBagConstraints();
		gbc_btnActivateWriteToFile_.gridwidth = 4;
		gbc_btnActivateWriteToFile_.anchor = GridBagConstraints.WEST;
		gbc_btnActivateWriteToFile_.insets = new Insets(0, 0, 5, 5);
		gbc_btnActivateWriteToFile_.gridx = 0;
		gbc_btnActivateWriteToFile_.gridy = 0;
		panel_4.add(btnActivateWriteToFile_, gbc_btnActivateWriteToFile_);

		JLabel lblDatabaseHost = new JLabel("Directory");
		GridBagConstraints gbc_lblDatabaseHost = new GridBagConstraints();
		gbc_lblDatabaseHost.anchor = GridBagConstraints.WEST;
		gbc_lblDatabaseHost.insets = new Insets(0, 0, 0, 5);
		gbc_lblDatabaseHost.gridx = 0;
		gbc_lblDatabaseHost.gridy = 1;
		panel_4.add(lblDatabaseHost, gbc_lblDatabaseHost);

		textDirectory_ = new JTextField();
		GridBagConstraints gbc_textDirectory_ = new GridBagConstraints();
		gbc_textDirectory_.insets = new Insets(0, 0, 0, 5);
		gbc_textDirectory_.fill = GridBagConstraints.HORIZONTAL;
		gbc_textDirectory_.gridx = 1;
		gbc_textDirectory_.gridy = 1;
		panel_4.add(textDirectory_, gbc_textDirectory_);
		textDirectory_.setColumns(10);

		JLabel lblDatabasePort = new JLabel("Filename");
		GridBagConstraints gbc_lblDatabasePort = new GridBagConstraints();
		gbc_lblDatabasePort.insets = new Insets(0, 0, 0, 5);
		gbc_lblDatabasePort.anchor = GridBagConstraints.EAST;
		gbc_lblDatabasePort.gridx = 2;
		gbc_lblDatabasePort.gridy = 1;
		panel_4.add(lblDatabasePort, gbc_lblDatabasePort);

		textFilename_ = new JTextField();
		GridBagConstraints gbc_textFilename_ = new GridBagConstraints();
		gbc_textFilename_.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFilename_.gridx = 3;
		gbc_textFilename_.gridy = 1;
		panel_4.add(textFilename_, gbc_textFilename_);
		textFilename_.setColumns(10);

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(null, "Save or Load your these settings for later", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.anchor = GridBagConstraints.NORTH;
		gbc_panel_5.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_5.insets = new Insets(5, 0, 0, 0);
		gbc_panel_5.gridx = 0;
		gbc_panel_5.gridy = 4;
		panelMessaging.add(panel_5, gbc_panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_5.rowHeights = new int[] { 0, 0 };
		gbl_panel_5.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_5.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panel_5.setLayout(gbl_panel_5);

		JButton btnClearSetting_ = new JButton("Clear");
		btnClearSetting_.setPreferredSize(new Dimension(80, 50));
		btnClearSetting_.setMargin(new Insets(0, 0, 0, 0));
		btnClearSetting_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearMessaging();
			}
		});
		btnClearSetting_.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/clean.png")));
		GridBagConstraints gbc_btnClearSetting_ = new GridBagConstraints();
		gbc_btnClearSetting_.insets = new Insets(0, 0, 0, 5);
		gbc_btnClearSetting_.gridx = 0;
		gbc_btnClearSetting_.gridy = 0;
		panel_5.add(btnClearSetting_, gbc_btnClearSetting_);

		JButton btnSave = new JButton("Save");
		btnSave.setPreferredSize(new Dimension(80, 50));
		btnSave.setMargin(new Insets(0, 0, 0, 0));
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveMessagingSetting();
			}
		});
		btnSave.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/save.png")));
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(0, 0, 0, 5);
		gbc_btnSave.gridx = 1;
		gbc_btnSave.gridy = 0;
		panel_5.add(btnSave, gbc_btnSave);

		JButton btnLoad = new JButton("Load");
		btnLoad.setPreferredSize(new Dimension(80, 50));
		btnLoad.setMargin(new Insets(0, 0, 0, 0));
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadMessagingSetting();
			}
		});
		GridBagLayout gbl_tabs = new GridBagLayout();
		gbl_tabs.rowHeights = new int[] { 300 };
		gbl_tabs.rowWeights = new double[] { 1.0 };
		gbl_tabs.columnWeights = new double[] { 1.0 };
		tabs.setLayout(gbl_tabs);
		btnLoad.setIcon(new ImageIcon(UhfAppFrame.class.getResource("/jence/icon/load.png")));
		GridBagConstraints gbc_btnLoad = new GridBagConstraints();
		gbc_btnLoad.gridx = 2;
		gbc_btnLoad.gridy = 0;
		panel_5.add(btnLoad, gbc_btnLoad);
		GridBagConstraints gbc_tabFolder = new GridBagConstraints();
		gbc_tabFolder.fill = GridBagConstraints.BOTH;
		gbc_tabFolder.gridy = 0;
		gbc_tabFolder.gridx = 0;
		gbc_tabFolder.anchor = GridBagConstraints.NORTH;
		tabs.add(tabFolder, gbc_tabFolder);
		setPanelsEnabled(false, panelInventory, penelMemory, panelInfo, panelGPIO, panelMessaging);

		JPanel softwareInfoPanel = new JPanel();
		softwareInfoPanel.setPreferredSize(new Dimension(10, 50));
		GridBagConstraints gbc_softwareInfoPanel = new GridBagConstraints();
		gbc_softwareInfoPanel.ipadx = 10;
		gbc_softwareInfoPanel.fill = GridBagConstraints.BOTH;
		gbc_softwareInfoPanel.gridx = 0;
		gbc_softwareInfoPanel.gridy = 3;

		getContentPane().add(softwareInfoPanel, gbc_softwareInfoPanel);
		GridBagLayout gbl_softwareInfoPanel = new GridBagLayout();
		gbl_softwareInfoPanel.columnWidths = new int[] { 262, 343, 0 };
		gbl_softwareInfoPanel.rowHeights = new int[] { 20 };
		gbl_softwareInfoPanel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_softwareInfoPanel.rowWeights = new double[] { 0.0 };
		softwareInfoPanel.setLayout(gbl_softwareInfoPanel);
		String v = UhfApp.driver_.getVersion();

		panel_7 = new JPanel();
		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
		gbc_panel_7.anchor = GridBagConstraints.WEST;
		gbc_panel_7.insets = new Insets(0, 0, 0, 5);
		gbc_panel_7.fill = GridBagConstraints.VERTICAL;
		gbc_panel_7.gridx = 0;
		gbc_panel_7.gridy = 0;
		softwareInfoPanel.add(panel_7, gbc_panel_7);

		lblStatus = new JLabel("Refresh to see the List of Available devices.");
		panel_7.add(lblStatus);

		panel_8 = new JPanel();
		GridBagConstraints gbc_panel_8 = new GridBagConstraints();
		gbc_panel_8.fill = GridBagConstraints.VERTICAL;
		gbc_panel_8.anchor = GridBagConstraints.EAST;
		gbc_panel_8.gridx = 1;
		gbc_panel_8.gridy = 0;
		softwareInfoPanel.add(panel_8, gbc_panel_8);

		btnDebug = new JCheckBox("Debug");
		btnDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					UhfApp.driver_.SetDebug(btnDebug.isSelected());
				} catch (Exception err) {
					// e.printStackTrace();
					UhfApp.prompt(err.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		panel_8.add(btnDebug);

		lblLibraryVersion = new JLabel("Library Version: " + v);
		panel_8.add(lblLibraryVersion);

		lblLibVersion = new JLabel();
		panel_8.add(lblLibVersion);

		lblAppVersion = new JLabel("| Application Version: " + UhfApp.VERSION);
		panel_8.add(lblAppVersion);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 1200 for mac
		// 1100 for linux and windows

		String osName = System.getProperty("os.name").toLowerCase();
		// Check if the operating system is macOS
		if (osName.contains("mac") || osName.contains("darwin")) {
			// Run macOS-specific code here
			this.setSize(1200, 660);
		} else if (osName.contains("linux")) {
			this.setSize(1200, 660);
		} else {
			this.setSize(1100, 660);
			this.pack();

		}
		setLocationRelativeTo(null);
		this.setVisible(true);

	}

	public void setPasswords(byte[] pass, byte[] killpass) {
		password_ = pass;
		killpass_ = killpass;
	}
}

class ScanSWorker extends SwingWorker<Void, Void> {
	private UhfAppFrame parent;

	public ScanSWorker(UhfAppFrame parent) {
		this.parent = parent;
	}

	@Override
	protected Void doInBackground() throws Exception {
		// Perform scanning task
		parent.btnScan_.setEnabled(false);
		parent.btnDisconnect_.setEnabled(false);
		parent.btnScanOnTrigger_.setEnabled(false);
		parent.btnMerge_.setEnabled(false);

		while (parent.btnScanServer_.isSelected()) {
			parent.scan();
			Thread.sleep(parent.SCAN_SERVER_DELAY); // Adjust the sleep duration as needed
		}
		parent.btnScan_.setEnabled(true);
		parent.btnDisconnect_.setEnabled(true);
		parent.btnScanOnTrigger_.setEnabled(true);
		parent.btnMerge_.setEnabled(true);

		return null;
	}
}

class ScanTWorker extends SwingWorker<Void, Void> {

	private UhfAppFrame parent;

	public ScanTWorker(UhfAppFrame parent) {
		this.parent = parent;
	}

	@Override
	protected Void doInBackground() throws Exception {
		boolean trigger = true;
		parent.btnScan_.setEnabled(false);
		parent.btnDisconnect_.setEnabled(false);
		parent.btnScanServer_.setEnabled(false);
		parent.btnMerge_.setEnabled(false);

		while (parent.btnScanOnTrigger_.isSelected()) {
			trigger = UhfApp.driver_.getGPInput(1);
			Thread.sleep(20);
			trigger = !(trigger || UhfApp.driver_.getGPInput(1)); // active high

			if (trigger) {
				parent.status("Trigger Pressed");
				parent.scan();
				Thread.sleep(5); // Adjust the sleep duration as needed
			} else {
				parent.status("Trigger Released");
			}
		}
		parent.btnScan_.setEnabled(true);
		parent.btnDisconnect_.setEnabled(true);
		parent.btnScanServer_.setEnabled(true);
		parent.btnMerge_.setEnabled(true);
		return null;

	}

}

class Tuple<A, B> {
	private final A row;
	private final B col;

	public Tuple(A row, B col) {
		this.row = row;
		this.col = col;
	}

	public A getRow() {
		return row;
	}

	public B getCol() {
		return col;
	}
}
