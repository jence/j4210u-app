package jence.swing.app;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HelpDialog extends JDialog {
	private JButton btnClose;
	private JTextPane textPane;

	private JEditorPane editorPane;

	public HelpDialog(JFrame parent) {
		super(parent, "Help Dialog", true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(parent);

		JPanel contentPane = new JPanel(new BorderLayout());

		editorPane = new JEditorPane();
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		String html = "<html><body>" + "<h1>Have you connected the device?</h1>"
				+ "<p>If you believe that you have connected the device to a USB port but the Refresh button does not "
				+ "show the USB port, please do the following.</p>"
				+ "<p>Remove the device then hit Refresh button. Note the name of the USB ports "
				+ "displayed and count them. Now, connect the device and refresh. Do you see the number "
				+ "of USB ports has increased from before? If Yes, then you have to select "
				+ "the new port that just appeared and Connect. if No, then it might be a driver " + "issue.</p>"
				+ "<h2>Driver Independence</h2><p>Most latest OS versions, driver is not required. "
				+ "Inserting the device is found as USB Serial device and a built-in driver will be used. "
				+ "You need a driver for an older version of OS.</p>" + "<h2>Where are the Drivers</h2>"
				+ "<p>J4210U uses two types of USB to UART chips:</p>" + "<p>CP210x chips: "
				+ "<a href=\"https://www.silabs.com/developers/usb-to-uart-bridge-vcp-drivers?tab=downloads\">Download CP210x Drivers from here.</a></p>"
				+ "<p>MCP2221 chips: <a href=\"https://www.microchip.com/en-us/product/MCP2221\">MCP2221 Drivers from here</a></p>"
				+ "</body></html>";
		editorPane.setText(html);

		// Add HyperlinkListener to open links in default web browser
		editorPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI(e.getURL().toString()));
						} catch (IOException | URISyntaxException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});

		setFocusable(true);
		requestFocusInWindow();

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}
		});

		JScrollPane scrollPane = new JScrollPane(editorPane);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		setContentPane(contentPane);
	}

}
