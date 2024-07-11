package jence.swing.app;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.border.EmptyBorder;
import javax.swing.JSeparator;
import javax.swing.border.MatteBorder;

import jence.jni.J4210U;
import jence.swing.app.UhfApp;

import java.awt.SystemColor;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InfoPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField serialno_;
	private JTextField version_;
	private JTextField maxf_;
	private JTextField antenna_;
	private JTextField readertype_;
	private JTextField minf_;
	private JTextField comadr_;
	private JTextField protocol_;
	private JComboBox<String> baudrate_;
	private JComboBox<String> band_;
	private JCheckBox beepon_;
	private JSpinner scantime_;
	private JSpinner power_;
	private UhfAppFrame app_;
	private J4210U.ReaderInfo ri_ = null;

	public InfoPanel(UhfAppFrame app_) {
		this.app_ = app_;
		setBorder(null);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JPanel infoFormPanel = new JPanel();
		GridBagConstraints gbc_infoFormPanel = new GridBagConstraints();
		gbc_infoFormPanel.fill = GridBagConstraints.BOTH;
		gbc_infoFormPanel.insets = new Insets(0, 0, 5, 0);
		gbc_infoFormPanel.gridx = 0;
		gbc_infoFormPanel.gridy = 0;
		add(infoFormPanel, gbc_infoFormPanel);
		GridBagLayout gbl_infoFormPanel = new GridBagLayout();
		gbl_infoFormPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_infoFormPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_infoFormPanel.columnWeights = new double[] { 1.0, 3.0, 1.0, 3.0, Double.MIN_VALUE };
		gbl_infoFormPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		infoFormPanel.setLayout(gbl_infoFormPanel);

		JLabel lblReaderType = new JLabel("DeviceID");
		GridBagConstraints gbc_lblReaderType = new GridBagConstraints();
		gbc_lblReaderType.anchor = GridBagConstraints.EAST;
		gbc_lblReaderType.insets = new Insets(0, 0, 5, 5);
		gbc_lblReaderType.gridx = 0;
		gbc_lblReaderType.gridy = 0;
		infoFormPanel.add(lblReaderType, gbc_lblReaderType);

		serialno_ = new JTextField();
		serialno_.setEditable(false);
		GridBagConstraints gbc_serialno_ = new GridBagConstraints();
		gbc_serialno_.fill = GridBagConstraints.HORIZONTAL;
		gbc_serialno_.insets = new Insets(0, 0, 5, 5);
		gbc_serialno_.gridx = 1;
		gbc_serialno_.gridy = 0;
		infoFormPanel.add(serialno_, gbc_serialno_);
		serialno_.setColumns(10);

		JLabel lblVersion = new JLabel("Version");
		GridBagConstraints gbc_lblVersion = new GridBagConstraints();
		gbc_lblVersion.anchor = GridBagConstraints.EAST;
		gbc_lblVersion.insets = new Insets(0, 0, 5, 5);
		gbc_lblVersion.gridx = 2;
		gbc_lblVersion.gridy = 0;
		infoFormPanel.add(lblVersion, gbc_lblVersion);

		version_ = new JTextField();
		version_.setEditable(false);
		GridBagConstraints gbc_version_ = new GridBagConstraints();
		gbc_version_.fill = GridBagConstraints.HORIZONTAL;
		gbc_version_.insets = new Insets(0, 0, 5, 0);
		gbc_version_.gridx = 3;
		gbc_version_.gridy = 0;
		infoFormPanel.add(version_, gbc_version_);
		version_.setColumns(10);

		JLabel lblMaxFreq = new JLabel("Max Freq");
		GridBagConstraints gbc_lblMaxFreq = new GridBagConstraints();
		gbc_lblMaxFreq.anchor = GridBagConstraints.EAST;
		gbc_lblMaxFreq.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxFreq.gridx = 0;
		gbc_lblMaxFreq.gridy = 1;
		infoFormPanel.add(lblMaxFreq, gbc_lblMaxFreq);

		maxf_ = new JTextField();
		maxf_.setEditable(false);
		GridBagConstraints gbc_maxf_ = new GridBagConstraints();
		gbc_maxf_.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxf_.insets = new Insets(0, 0, 5, 5);
		gbc_maxf_.gridx = 1;
		gbc_maxf_.gridy = 1;
		infoFormPanel.add(maxf_, gbc_maxf_);
		maxf_.setColumns(10);

		JLabel lblMinFreq = new JLabel("Min Freq");
		GridBagConstraints gbc_lblMinFreq = new GridBagConstraints();
		gbc_lblMinFreq.anchor = GridBagConstraints.EAST;
		gbc_lblMinFreq.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinFreq.gridx = 2;
		gbc_lblMinFreq.gridy = 1;
		infoFormPanel.add(lblMinFreq, gbc_lblMinFreq);

		minf_ = new JTextField();
		minf_.setEditable(false);
		GridBagConstraints gbc_minf_ = new GridBagConstraints();
		gbc_minf_.fill = GridBagConstraints.HORIZONTAL;
		gbc_minf_.insets = new Insets(0, 0, 5, 0);
		gbc_minf_.gridx = 3;
		gbc_minf_.gridy = 1;
		infoFormPanel.add(minf_, gbc_minf_);
		minf_.setColumns(10);

		JLabel lblAntenna = new JLabel("Antenna");
		GridBagConstraints gbc_lblAntenna = new GridBagConstraints();
		gbc_lblAntenna.anchor = GridBagConstraints.EAST;
		gbc_lblAntenna.insets = new Insets(0, 0, 5, 5);
		gbc_lblAntenna.gridx = 0;
		gbc_lblAntenna.gridy = 2;
		infoFormPanel.add(lblAntenna, gbc_lblAntenna);

		antenna_ = new JTextField();
		antenna_.setEditable(false);
		GridBagConstraints gbc_antenna_ = new GridBagConstraints();
		gbc_antenna_.fill = GridBagConstraints.HORIZONTAL;
		gbc_antenna_.insets = new Insets(0, 0, 5, 5);
		gbc_antenna_.gridx = 1;
		gbc_antenna_.gridy = 2;
		infoFormPanel.add(antenna_, gbc_antenna_);
		antenna_.setColumns(10);

		JLabel lblComadr = new JLabel("ComAdr");
		GridBagConstraints gbc_lblComadr = new GridBagConstraints();
		gbc_lblComadr.anchor = GridBagConstraints.EAST;
		gbc_lblComadr.insets = new Insets(0, 0, 5, 5);
		gbc_lblComadr.gridx = 2;
		gbc_lblComadr.gridy = 2;
		infoFormPanel.add(lblComadr, gbc_lblComadr);

		comadr_ = new JTextField();
		comadr_.setEditable(false);
		GridBagConstraints gbc_comadr_ = new GridBagConstraints();
		gbc_comadr_.fill = GridBagConstraints.HORIZONTAL;
		gbc_comadr_.insets = new Insets(0, 0, 5, 0);
		gbc_comadr_.gridx = 3;
		gbc_comadr_.gridy = 2;
		infoFormPanel.add(comadr_, gbc_comadr_);
		comadr_.setColumns(10);

		JLabel lblReaderType_ = new JLabel("Reader Type");
		GridBagConstraints gbc_lblReaderType_ = new GridBagConstraints();
		gbc_lblReaderType_.anchor = GridBagConstraints.EAST;
		gbc_lblReaderType_.insets = new Insets(0, 0, 5, 5);
		gbc_lblReaderType_.gridx = 0;
		gbc_lblReaderType_.gridy = 3;
		infoFormPanel.add(lblReaderType_, gbc_lblReaderType_);

		readertype_ = new JTextField();
		readertype_.setEditable(false);
		GridBagConstraints gbc_readertype_ = new GridBagConstraints();
		gbc_readertype_.fill = GridBagConstraints.HORIZONTAL;
		gbc_readertype_.insets = new Insets(0, 0, 5, 5);
		gbc_readertype_.gridx = 1;
		gbc_readertype_.gridy = 3;
		infoFormPanel.add(readertype_, gbc_readertype_);
		readertype_.setColumns(10);

		JLabel lblProtocol = new JLabel("Protocol");
		GridBagConstraints gbc_lblProtocol = new GridBagConstraints();
		gbc_lblProtocol.anchor = GridBagConstraints.EAST;
		gbc_lblProtocol.insets = new Insets(0, 0, 5, 5);
		gbc_lblProtocol.gridx = 2;
		gbc_lblProtocol.gridy = 3;
		infoFormPanel.add(lblProtocol, gbc_lblProtocol);

		protocol_ = new JTextField();
		protocol_.setEditable(false);
		GridBagConstraints gbc_protocol_ = new GridBagConstraints();
		gbc_protocol_.fill = GridBagConstraints.HORIZONTAL;
		gbc_protocol_.insets = new Insets(0, 0, 5, 0);
		gbc_protocol_.gridx = 3;
		gbc_protocol_.gridy = 3;
		infoFormPanel.add(protocol_, gbc_protocol_);
		protocol_.setColumns(10);

		JLabel lblBand = new JLabel("Band");
		GridBagConstraints gbc_lblBand = new GridBagConstraints();
		gbc_lblBand.anchor = GridBagConstraints.EAST;
		gbc_lblBand.insets = new Insets(0, 0, 5, 5);
		gbc_lblBand.gridx = 0;
		gbc_lblBand.gridy = 4;
		infoFormPanel.add(lblBand, gbc_lblBand);

		band_ = new JComboBox();
		band_.setModel(new DefaultComboBoxModel(new String[] { "Chinese", "USA", "Korean", "EU" }));
		GridBagConstraints gbc_band_ = new GridBagConstraints();
		gbc_band_.fill = GridBagConstraints.HORIZONTAL;
		gbc_band_.insets = new Insets(0, 0, 5, 5);
		gbc_band_.gridx = 1;
		gbc_band_.gridy = 4;
		infoFormPanel.add(band_, gbc_band_);

		JLabel lblBaudRate = new JLabel("Baud Rate");
		GridBagConstraints gbc_lblBaudRate = new GridBagConstraints();
		gbc_lblBaudRate.anchor = GridBagConstraints.EAST;
		gbc_lblBaudRate.insets = new Insets(0, 0, 5, 5);
		gbc_lblBaudRate.gridx = 2;
		gbc_lblBaudRate.gridy = 4;
		infoFormPanel.add(lblBaudRate, gbc_lblBaudRate);

		baudrate_ = new JComboBox<String>();
		baudrate_.setModel(new DefaultComboBoxModel(new String[] { "57600", "115200" }));
		GridBagConstraints gbc_baudrate_ = new GridBagConstraints();
		gbc_baudrate_.fill = GridBagConstraints.HORIZONTAL;
		gbc_baudrate_.insets = new Insets(0, 0, 5, 0);
		gbc_baudrate_.gridx = 3;
		gbc_baudrate_.gridy = 4;
		infoFormPanel.add(baudrate_, gbc_baudrate_);

		JLabel lblPower = new JLabel("Power (dBm)");
		GridBagConstraints gbc_lblPower = new GridBagConstraints();
		gbc_lblPower.anchor = GridBagConstraints.EAST;
		gbc_lblPower.insets = new Insets(0, 0, 5, 5);
		gbc_lblPower.gridx = 0;
		gbc_lblPower.gridy = 6;
		infoFormPanel.add(lblPower, gbc_lblPower);

		power_ = new JSpinner();

		power_.setModel(new SpinnerNumberModel(19, 0, 26, 1));
		GridBagConstraints gbc_power_ = new GridBagConstraints();
		gbc_power_.fill = GridBagConstraints.HORIZONTAL;
		gbc_power_.insets = new Insets(0, 0, 5, 5);
		gbc_power_.gridx = 1;
		gbc_power_.gridy = 6;
		infoFormPanel.add(power_, gbc_power_);

		JLabel lblScanTime = new JLabel("Scan Time (ms)");
		GridBagConstraints gbc_lblScanTime = new GridBagConstraints();
		gbc_lblScanTime.anchor = GridBagConstraints.EAST;
		gbc_lblScanTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblScanTime.gridx = 2;
		gbc_lblScanTime.gridy = 6;
		infoFormPanel.add(lblScanTime, gbc_lblScanTime);

		scantime_ = new JSpinner();
		scantime_.setModel(new SpinnerNumberModel(300, 300, 10000, 100));
		GridBagConstraints gbc_scantime_ = new GridBagConstraints();
		gbc_scantime_.fill = GridBagConstraints.HORIZONTAL;
		gbc_scantime_.insets = new Insets(0, 0, 5, 0);
		gbc_scantime_.gridx = 3;
		gbc_scantime_.gridy = 6;
		infoFormPanel.add(scantime_, gbc_scantime_);

		beepon_ = new JCheckBox("Beep ON");
		GridBagConstraints gbc_beepon_ = new GridBagConstraints();
		gbc_beepon_.anchor = GridBagConstraints.WEST;
		gbc_beepon_.insets = new Insets(0, 0, 0, 5);
		gbc_beepon_.gridx = 1;
		gbc_beepon_.gridy = 7;
		infoFormPanel.add(beepon_, gbc_beepon_);

		JSeparator separator_1 = new JSeparator(SwingConstants.HORIZONTAL);
		separator_1.setBackground(new Color(64, 0, 64));
		separator_1.setPreferredSize(new Dimension(550, 20));
		separator_1.setForeground(Color.BLACK);
		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator_1.insets = new Insets(2, 2, 2, 2);
		gbc_separator_1.gridx = 0;
		gbc_separator_1.gridy = 2;
		add(separator_1, gbc_separator_1);

		JButton btnWrite = new JButton("Write");
		btnWrite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				save();
			}
		});
		btnWrite.setIcon(new ImageIcon(InfoPanel.class.getResource("/jence/icon/write.png")));
		GridBagConstraints gbc_btnWrite = new GridBagConstraints();
		gbc_btnWrite.anchor = GridBagConstraints.WEST;
		gbc_btnWrite.gridx = 0;
		gbc_btnWrite.gridy = 3;
		add(btnWrite, gbc_btnWrite);

	}

	private void save() {
		app_.status("Saving Settings...");
		Boolean hasBaudChangedBoolean = false;

		ri_.ScanTime = (int) scantime_.getValue();
		ri_.Band = (byte) ((String) band_.getSelectedItem()).charAt(0);
		ri_.BeepOn = (beepon_.isSelected()) ? (byte) 1 : 0;
		ri_.Power = (byte) ((int) power_.getValue() & 0xFF);
		// combo box index to internal baudrate mapping
		int oldBaudrate = ri_.BaudRate;
		int selectedBaudrate = Integer.parseInt(baudrate_.getSelectedItem().toString());
		ri_.BaudRate = selectedBaudrate;

		try {
			if (oldBaudrate != selectedBaudrate) {
				Boolean userSelection = UhfApp.prompt(
						"Your old baudrate " + oldBaudrate + " will be immediately changed to new baudrate "
								+ selectedBaudrate
								+ ". So, you have to disconnect and then reconnect using the new baudrate.",
						"Warning", 2, JOptionPane.YES_NO_OPTION);
				if (userSelection == false) {
					return;
				}
				hasBaudChangedBoolean = true;
			}
			UhfApp.driver_.saveSettings(ri_);
			refresh();
			if (!hasBaudChangedBoolean) {
				app_.status("Settings Saved Successfully.");
			}

		} catch (Exception e) {
			UhfApp.prompt(e.getMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);
			if (hasBaudChangedBoolean) {
				app_.status("Saved Successfully. BaudRate Changed to " + String.valueOf(selectedBaudrate));
			}
		}
	}

	public void refresh() throws Exception {
		try {
			ri_ = jence.swing.app.UhfApp.driver_.loadSettings();
			antenna_.setText(ri_.Antenna + "");
			baudrate_.setSelectedItem(String.valueOf(ri_.BaudRate));
			comadr_.setText(ri_.ComAdr + "");
			maxf_.setText(ri_.MaxFreq + "");
			minf_.setText(ri_.MinFreq + "");
			readertype_.setText(ri_.ReaderType + "");
			serialno_.setText(ri_.Serial + "");
			version_.setText(ri_.VersionInfo[0] + "." + ri_.VersionInfo[1]);
			protocol_.setText(ri_.Protocol + "");
			power_.setValue((int) ri_.Power);
			scantime_.setValue(ri_.ScanTime);

			char c = (char) ri_.Band;
			band_.selectWithKeyChar(c);
			beepon_.setSelected(ri_.BeepOn == 1 ? true : false);
		} catch (Exception e) {
			jence.swing.app.UhfApp.prompt(e.getLocalizedMessage(), "Error", 1, JOptionPane.WARNING_MESSAGE);
			throw new Exception("Couldn't Load Settings");
		}

	}

}
