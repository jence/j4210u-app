package jence.swing.app;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jence.jni.J4210U;

public class UhfApp {
	public static final String VERSION = "3.0";

	public static String LAST_USE_SERIAL_PORT = "";

	public static J4210U driver_ = new J4210U();

	public static boolean prompt(String text, String heading,int type, int style) {
		// Create a JLabel with HTML formatting to enable text wrapping
		JLabel label = new JLabel("<html><body style='width: 350px;'>" + text + "</body></html>");

		// Determine the appropriate message type based on the style
		int messageType = style;
		if (type == 0) { //warning msg
			JOptionPane.showMessageDialog(null, label, heading, messageType);
			return true;
		}
		if (type == 1) { //error
			JOptionPane.showMessageDialog(null, label, heading, messageType);
			return true;

		}
		if (type == 2) { //yes no
			int confirmationResult = JOptionPane.showConfirmDialog(null, label, heading,
					JOptionPane.YES_NO_OPTION);
			return (confirmationResult == JOptionPane.YES_OPTION);

		}
		if (type == 3) { //information
			JOptionPane.showMessageDialog(null, label, heading, messageType);
			return true;
		}
		if (type == 4) { //abort
			JOptionPane.showMessageDialog(null, label, heading, messageType);
			return true;
		}
		return false;

		// Return a default value (can be modified as needed)
//        return JOptionPane.OK_OPTION;
	}

	public static void main(String[] args) {
		UhfAppFrame app_ = new UhfAppFrame();
	}

}
