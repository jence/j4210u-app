/**
 * 
 */
package jence.demo;

import java.util.Scanner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import jence.jni.J4210U;
import jence.swt.app.UhfApp;

/**
 * @author soalib
 *
 */
public class TagInventoryDemo {
	private static J4210U uhf = new J4210U();

	/**
	 * 
	 */
	public TagInventoryDemo() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String COM = null; //"com2";
		try {
			System.out.println("Usage: j4210n <comport>");
			System.out.println("OR, enter J4210N reader to an USB port then provide the COM port at which the reader is found.");
			Scanner scanner = new Scanner(System.in);
			if (args.length > 0) {
				COM = args[0];
			}
			if (COM == null) {
				System.out.println("Please enter COM port (e.g., COM4):");
				COM = scanner.nextLine();
				COM = COM.trim();
			}
			
			uhf.open(COM, 57600);
			System.out.println("Connect to the reader: SUCCESS");
			
			// get reader info.
			J4210U.ReaderInfo ri = uhf.loadSettings();
			System.out.println(ri);
			
			uhf.setQ(6);
			uhf.setSession(0);
			uhf.auth(new byte[]{0,0,0,0});
			ri.ScanTime = 200;
			//uhf.saveSettings(ri);
			
			// scan without filtering
			int n = uhf.inventory(false);
			for (int i = 0; i < n; i++) {
				final J4210U.ScanResult sr = UhfApp.driver_.getResult(i);
				// System.out.println(sr);
				System.out.println("EPC=" + uhf.toHex(sr.EPC) + " + EPC Len=" + sr.EpcLength + " + Ant=" +
						sr.Ant + "+ Count="+ sr.Count + ", RSSI="+ sr.RSSI + "" );
			}
			
			uhf.close();
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			//e.printStackTrace();
		}
		System.out.println("Program FINISHED.");
	}
}

