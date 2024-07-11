package jence.swing.app;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Map;
import java.util.Properties;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.eclipse.swt.SWT;

import javax.swing.JButton;

import jence.jni.J4210U;
//import jence.swt.app.Callback;
import jence.swing.app.UhfApp;
import jence.swing.app.UhfAppFrame;
import jence.swt.app.Callback;
import jence.swt.app.UhfAppComposite;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

public class AuthFrame extends JDialog {

//	private Callback callback_;
	private JTextField asciiA_;
	private JTextField hexA_;
	private JTextField asciiB_;
	private JTextField hexB_;
	private JTextField passwordChars_;
	private JButton btnWrite;
	private JButton btnDefaultKey;

	byte[] epc_;
	private Map auth_ = new Properties();
	private static String PasswordCharacters = " ABCDEFGHIJKLMNOPQRSTUVWXYZ_-.@#";

	private void useKey(UhfAppFrame parent) {
		if (UhfApp.prompt("Use have choosen to use this key for subsequent read and write. "
				+ "If your tag does not use this key, then you should write this key to the tag first before "
				+ "subsequent read or write. ", "Confirmation", 2, JOptionPane.YES_NO_OPTION) == true) {
			try {
				parent.setPasswords(UhfApp.driver_.hex2bytes(hexA_.getText(), 4),
						UhfApp.driver_.hex2bytes(hexB_.getText(), 4));
			} catch (Exception e) {
				UhfApp.prompt(e.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void saveFile() {
		JFileChooser fileChooser = new JFileChooser();
		Dimension size = new Dimension(600, 400);
		fileChooser.setPreferredSize(size);

		fileChooser.setDialogTitle("Save");
		fileChooser.setCurrentDirectory(new File("."));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("J4210U Files", "j4210u");
		fileChooser.setFileFilter(filter);
		int result = fileChooser.showSaveDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			String filename = fileChooser.getSelectedFile().getPath();
			if (!filename.toLowerCase().endsWith(".j4210u")) {
				filename += ".j4210u"; // Append extension if missing
			}
			String warning = " IMPORTANT: Keep this file in a safe "
					+ "place and do not\n distribute. The file contains sensitive password information.";
			try {
				Properties auth = new Properties();
				auth.put("password", "examplePassword"); // Replace with your actual password
				auth.put("killpass", "exampleKillPass"); // Replace with your actual kill pass

				// Write to file
				try (FileOutputStream fos = new FileOutputStream(filename)) {
					auth.store(fos, warning);
				}

				// Show success message
				JOptionPane.showMessageDialog(this, "File saved successfully", "Success",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void writeAuth(boolean defaultKwy) {
		if (epc_ == null) {
			UhfApp.prompt("Could not determine on which chip to operate. No EPC set.", "Error", 1,
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String warning = "You are about to change authentication by changing keys and access conditions.";
		warning += " This will apply to all the sectors of this tag.";
		warning += " Some access conditions may make the tag unreadable. Are you sure you want to perform this operation?";
		if (UhfApp.prompt(warning, "Confirmation", 2, JOptionPane.YES_NO_OPTION) == false) {
			return;
		}

		int block;
		try {
			byte[] killpass = { 0, 0, 0, 0 };
			byte[] pass = { 0, 0, 0, 0 };
			if (defaultKwy) {
				UhfApp.driver_.setPassword(epc_, pass);
				UhfApp.driver_.setKillPassword(epc_, killpass);
			} else {
				pass = UhfApp.driver_.hex2bytes(hexA_.getText(), 4);
				UhfApp.driver_.setPassword(epc_, pass);
				killpass = UhfApp.driver_.hex2bytes(hexB_.getText(), 4);
				UhfApp.driver_.setKillPassword(epc_, killpass);
			}
			UhfApp.prompt("Successfully changed password.", "Success", 3, JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			UhfApp.prompt(e.getLocalizedMessage(), "Error", 1, JOptionPane.ERROR_MESSAGE);
		}
	}

	private String hex2ascii(String hex) {
		if (hex == null) {
			return null;
		}
		if (hex.length() % 2 != 0)
			hex = "0" + hex; // make the length even;
		long n = Long.parseLong(hex, 16);
		String z = "";
		int d = (int) (n & (long) 0x03) + 3;
		// password length: 3->6, 2->5, 1->4, 0->3
//		if (d > 0)
//			d += 3;
		n >>= 2;
		for (int i = 0; i < d; i++) {
			long nn = (n >> (i * 5));
			int cindex = (int) (nn & (long) 0x1F);
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
			// short c = (byte) (ascii.charAt(i) & 0xFF);
			char c = ascii.charAt(i);
			int index = PasswordCharacters.indexOf(c);
			long nc = index << (5 * i);
			// System.out.println("nc="+Long.toString(nc, 2));
			n |= nc;
		}

		n <<= 2;
		n |= d;
		String hex = String.format("%08X", n);
		// System.out.println(Long.toString(n, 2));
		// String ascii2 = hex2ascii(hex);
		// System.out.println(ascii2.trim());
		return hex;
	}

	public AuthFrame(UhfAppFrame parent) {
		super(parent, "Auth", true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(550, 320);
		setLocationRelativeTo(parent);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 80, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 70, 0, 145, 8, 145, 0, 0 };
		gbl_panel.rowHeights = new int[] { 20, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblKeyA = new JLabel("Password");
		GridBagConstraints gbc_lblKeyA = new GridBagConstraints();
		gbc_lblKeyA.anchor = GridBagConstraints.EAST;
		gbc_lblKeyA.insets = new Insets(0, 0, 5, 5);
		gbc_lblKeyA.gridx = 0;
		gbc_lblKeyA.gridy = 0;
		panel.add(lblKeyA, gbc_lblKeyA);

		JLabel lblAscii = new JLabel("ASCII");
		GridBagConstraints gbc_lblAscii = new GridBagConstraints();
		gbc_lblAscii.fill = GridBagConstraints.BOTH;
		gbc_lblAscii.insets = new Insets(0, 0, 5, 5);
		gbc_lblAscii.gridx = 1;
		gbc_lblAscii.gridy = 0;
		panel.add(lblAscii, gbc_lblAscii);

		asciiA_ = new JTextField(6);
		asciiA_.setDocument(new JTextFieldLimit(6));

		asciiA_.setText("");
		asciiA_.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				// Text inserted
				SwingUtilities.invokeLater(() -> {
					System.out.println(asciiA_.getText());
					String hex = ascii2hex(asciiA_.getText());
					System.out.println(hex);
					hexA_.setText(hex);
				});

			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// Text removed
				SwingUtilities.invokeLater(() -> {
					hexA_.setText(ascii2hex(asciiA_.getText()));
				});

			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Style change (not applicable for plain text components)
			}
		});

		GridBagConstraints gbc_asciiA_ = new GridBagConstraints();
		gbc_asciiA_.insets = new Insets(0, 0, 5, 5);
		gbc_asciiA_.fill = GridBagConstraints.BOTH;
		gbc_asciiA_.gridx = 2;
		gbc_asciiA_.gridy = 0;
		panel.add(asciiA_, gbc_asciiA_);

		JLabel lblHex = new JLabel("HEX");
		GridBagConstraints gbc_lblHex = new GridBagConstraints();
		gbc_lblHex.anchor = GridBagConstraints.EAST;
		gbc_lblHex.insets = new Insets(0, 0, 5, 5);
		gbc_lblHex.gridx = 3;
		gbc_lblHex.gridy = 0;
		panel.add(lblHex, gbc_lblHex);

		hexA_ = new JTextField();
		hexA_.setDocument(new LimitedHexText(8));

		hexA_.setText(ascii2hex(asciiA_.getText()));

		GridBagConstraints gbc_hexA_ = new GridBagConstraints();
		gbc_hexA_.insets = new Insets(0, 0, 5, 5);
		gbc_hexA_.fill = GridBagConstraints.BOTH;
		gbc_hexA_.gridx = 4;
		gbc_hexA_.gridy = 0;
		panel.add(hexA_, gbc_hexA_);

		JButton btnDefault_A = new JButton("Default");
		btnDefault_A.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hexA_.setText("00000000");
				asciiA_.setText("");
			}
		});
		btnDefault_A.setIcon(new ImageIcon(AuthFrame.class.getResource("/jence/icon/default16.png")));
		GridBagConstraints gbc_btnDefault_A = new GridBagConstraints();
		gbc_btnDefault_A.insets = new Insets(0, 0, 5, 0);
		gbc_btnDefault_A.gridx = 5;
		gbc_btnDefault_A.gridy = 0;
		panel.add(btnDefault_A, gbc_btnDefault_A);

		JLabel lblKeyB = new JLabel("Kill Pass");
		GridBagConstraints gbc_lblKeyB = new GridBagConstraints();
		gbc_lblKeyB.anchor = GridBagConstraints.EAST;
		gbc_lblKeyB.insets = new Insets(0, 0, 0, 5);
		gbc_lblKeyB.gridx = 0;
		gbc_lblKeyB.gridy = 1;
		panel.add(lblKeyB, gbc_lblKeyB);

		JLabel lblAscii_1 = new JLabel("ASCII");
		GridBagConstraints gbc_lblAscii_1 = new GridBagConstraints();
		gbc_lblAscii_1.anchor = GridBagConstraints.WEST;
		gbc_lblAscii_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblAscii_1.gridx = 1;
		gbc_lblAscii_1.gridy = 1;
		panel.add(lblAscii_1, gbc_lblAscii_1);

		asciiB_ = new JTextField(6);
		asciiB_.setDocument(new JTextFieldLimit(6));

		asciiB_.setText("");
		asciiB_.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				// Text inserted
				SwingUtilities.invokeLater(() -> {
					String hex = ascii2hex(asciiB_.getText());
					hexB_.setText(hex);
				});

			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// Text removed
				SwingUtilities.invokeLater(() -> {
					String hex = ascii2hex(asciiB_.getText());
					hexB_.setText(hex);
				});

			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Style change (not applicable for plain text components)
			}
		});

		GridBagConstraints gbc_asciiB_ = new GridBagConstraints();
		gbc_asciiB_.insets = new Insets(0, 0, 0, 5);
		gbc_asciiB_.fill = GridBagConstraints.BOTH;
		gbc_asciiB_.gridx = 2;
		gbc_asciiB_.gridy = 1;
		panel.add(asciiB_, gbc_asciiB_);

		JLabel lblHex_1 = new JLabel("HEX");
		GridBagConstraints gbc_lblHex_1 = new GridBagConstraints();
		gbc_lblHex_1.anchor = GridBagConstraints.EAST;
		gbc_lblHex_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblHex_1.gridx = 3;
		gbc_lblHex_1.gridy = 1;
		panel.add(lblHex_1, gbc_lblHex_1);

		hexB_ = new JTextField(8);
		hexB_.setDocument(new LimitedHexText(8));

		hexB_.setText(ascii2hex(asciiB_.getText()));
		GridBagConstraints gbc_hexB_ = new GridBagConstraints();
		gbc_hexB_.insets = new Insets(0, 0, 0, 5);
		gbc_hexB_.fill = GridBagConstraints.BOTH;
		gbc_hexB_.gridx = 4;
		gbc_hexB_.gridy = 1;
		panel.add(hexB_, gbc_hexB_);

		JButton btnDefault_B = new JButton("Default");
		btnDefault_B.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hexB_.setText("00000000");
				asciiB_.setText("");

			}
		});
		btnDefault_B.setIcon(new ImageIcon(AuthFrame.class.getResource("/jence/icon/default16.png")));
		GridBagConstraints gbc_btnDefault_B = new GridBagConstraints();
		gbc_btnDefault_B.gridx = 5;
		gbc_btnDefault_B.gridy = 1;
		panel.add(btnDefault_B, gbc_btnDefault_B);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.anchor = GridBagConstraints.WEST;
		gbc_panel_1.insets = new Insets(5, 5, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.VERTICAL;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		getContentPane().add(panel_1, gbc_panel_1);

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});
		btnSave.setIcon(new ImageIcon(AuthFrame.class.getResource("/jence/icon/save.png")));
		panel_1.add(btnSave);

		JButton btnLoad = new JButton("Load");
		btnLoad.setIcon(new ImageIcon(AuthFrame.class.getResource("/jence/icon/load.png")));
		panel_1.add(btnLoad);

		JButton btnUse = new JButton("Use");
		btnUse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				useKey(parent);
			}
		});
		btnUse.setIcon(new ImageIcon(AuthFrame.class.getResource("/jence/icon/cardread.png")));
		panel_1.add(btnUse);

		btnWrite = new JButton("Write");
		btnWrite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				TODO: if fields are not empty
				writeAuth(true);
			}
		});
		btnWrite.setIcon(new ImageIcon(AuthFrame.class.getResource("/jence/icon/write.png")));
		panel_1.add(btnWrite);

		btnDefaultKey = new JButton("Default");
		btnDefaultKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeAuth(true);
			}
		});
		btnDefaultKey.setIcon(new ImageIcon(AuthFrame.class.getResource("/jence/icon/default.png")));
		panel_1.add(btnDefaultKey);

		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(5, 5, 5, 5);
		gbc_panel_2.ipady = 5;
		gbc_panel_2.ipadx = 5;
		gbc_panel_2.gridheight = 2;
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 2;
		getContentPane().add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JLabel label_4 = new JLabel("Password Chars");
		GridBagConstraints gbc_label_4 = new GridBagConstraints();
		gbc_label_4.anchor = GridBagConstraints.EAST;
		gbc_label_4.gridx = 0;
		gbc_label_4.gridy = 0;
		panel_2.add(label_4, gbc_label_4);

		passwordChars_ = new JTextField();
		passwordChars_.setText(" ABCDEFGHIJKLMNOPQRSTUVWXYZ_-.@#");
		passwordChars_.setEditable(false);
		GridBagConstraints gbc_passwordChars_ = new GridBagConstraints();
		gbc_passwordChars_.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordChars_.gridx = 1;
		gbc_passwordChars_.gridy = 0;
		panel_2.add(passwordChars_, gbc_passwordChars_);
		passwordChars_.setColumns(10);

//		// Assuming 'parent' is the parent component
//		Point parentLocationOnScreen = parent.getLocationOnScreen();
//		Dimension parentSize = parent.getSize();
//		Dimension dialogSize = this.getSize();
//
//		int x = parentLocationOnScreen.x + (parentSize.width - dialogSize.width) / 2;
//		int y = parentLocationOnScreen.y + (parentSize.height - dialogSize.height) / 2;
//
//		// Set the location of the dialog manually
//		this.setLocation(x, y);

	}

//	private void updateAscii() {
//		asciiA_.setText(hex2ascii(hexA_.getText()));
//		asciiB_.setText(hex2ascii(hexB_.getText()));
//	}
//
//	public void callback(Callback runnable) {
//		callback_ = runnable;
//	}
//
//	private void writeAuth(boolean defaultKwy) {
//		if (epc_ == null) {
//			UhfApp.prompt(this.getShell(), "Could not determine on which chip to operate. No EPC set.", SWT.OK | SWT.ICON_WARNING);
//			return;
//		}
//		String warning = "You are about to change authentication by changing keys and access conditions.";
//		warning += " This will apply to all the sectors of this tag.";
//		warning += " Some access conditions may make the tag unreadable. Are you sure you want to perform this operation?";
//		if (UhfApp.prompt(this.getShell(), warning, SWT.OK | SWT.ICON_WARNING
//				| SWT.CANCEL) == SWT.CANCEL) {
//			return;
//		}
//
//		int block;
//		try {
//			byte[] killpass = {0,0,0,0};
//			byte[] pass = {0,0,0,0};
//			if (defaultKwy) {
//				UhfApp.driver_.setPassword(epc_, pass);
//				UhfApp.driver_.setKillPassword(epc_, killpass);
//			} else {
//				pass = UhfApp.driver_.hex2bytes(hexA_.getText(), 4);
//				UhfApp.driver_.setPassword(epc_, pass);
//				killpass = UhfApp.driver_.hex2bytes(hexB_.getText(), 4);
//				UhfApp.driver_.setKillPassword(epc_, killpass);
//			}
//			UhfApp.prompt(this.getShell(),
//					"Successfully changed password.", SWT.OK);
//		} catch (Exception e) {
//			UhfApp.prompt(this.getShell(), e.getLocalizedMessage(), SWT.OK
//					| SWT.ICON_WARNING);
//		}
//	}
//
//	private void useKey() {
//		if (UhfApp
//				.prompt(this.getShell(),
//						"Use have choosen to use this key for subsequent read and write. "
//								+ "If your tag does not use this key, then you should write this key to the tag first before "
//								+ "subsequent read or write. ", SWT.OK
//								| SWT.CANCEL) == SWT.OK) {
//			try {
//				UhfAppComposite u = (UhfAppComposite)this.getShell().getParent().getChildren()[0];
//				u.setPasswords(UhfApp.driver_.hex2bytes(hexA_.getText(), 4), UhfApp.driver_.hex2bytes(hexB_.getText(), 4));
//				this.getShell().dispose();
//			} catch (Exception e) {
//				UhfApp.prompt(this.getShell(), e.getLocalizedMessage(), SWT.OK
//						| SWT.ICON_WARNING);
//			}
//		}
//	}
//	
	public void setEPC(byte[] epc, byte[] password, byte[] killpass, boolean enableWrite) {
		epc_ = epc;
		String hexp = J4210U.toHex(password);
		String hexk = J4210U.toHex(killpass);
		asciiA_.setText(hex2ascii(hexp).trim());
		asciiB_.setText(hex2ascii(hexk).trim());
		btnWrite.setEnabled(true);
		btnDefaultKey.setEnabled(true);
	}
}
