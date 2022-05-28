/**
 * 
 */
package jence.jni;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @author soalib
 *
 */
public class J4210U {
	public enum TagType {
        UNKNOWN,
        
        // ULTRALIGHT FAMILY
        HIGGS_3,
        HIGGS_4,
        HIGGS_EC,
        HIGGS_9,

        MONZA_4QT,
        MONZA_4E,
        MONZA_4D,
        MONZA_4I,
        MONZA_5,
        MONZA_R6,
        MONZA_R6P,
        MONZA_X2K,
        MONZA_X8K,
        IMPINJ_M730,
        IMPINJ_M750,

        UCODE_7,
        UCODE_8,
        UCODE_8M,
        UCODE_DNA,
        UCODE_DNA_CITY,
        UCODE_DNA_TRACK,

        EM4423,
	};
	
	public static class TagInfo {
		public TagType type;
		public int tidlen;
		public byte[] tid;
		public String chip;
		public int epclen;
		public int userlen;
		public int pwdlen;
		
		public TagInfo(byte[] data) {
			ByteBuffer bb = ByteBuffer.wrap(data);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			
			type = TagType.values()[bb.getInt()];
			tidlen = bb.getInt();
			tid = new byte[64];
			bb.get(tid);
			tid = Arrays.copyOf(tid, tidlen);
			byte[] c = new byte[16];
			bb.get(c); // chip
			chip = J4210U.getNullTerminatedString(c);
			epclen = bb.getInt();
			userlen = bb.getInt();
			pwdlen = bb.getInt();
		}
	}
	
	public static class ScanResult {
		public byte Ant;
		public byte RSSI;
		public int Count;
		public byte EpcLength;
		public byte[] EPC = new byte[12];
		
		public byte[] toByteArray() {
			ByteBuffer bb = ByteBuffer.allocate(128);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			
			bb.put(Ant);
			bb.put(RSSI);
			bb.put((byte)(Count & 0xFF));
			bb.put(EpcLength);
			bb.put(EPC);
			
			return bb.array();
		}
		
		public ScanResult(byte[] raw) {
			ByteBuffer bb = ByteBuffer.wrap(raw);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			
			Ant = bb.get();
			RSSI = bb.get();
			Count = (bb.get() & 0xFF);
			EpcLength = bb.get();
			bb.get(EPC);
		}
		
		public ScanResult(){}
		
		public static int size() {
			return 1+1+1+1+12;
		}
		
		public String toString() {
			return "Ant="+Ant+", RSSI="+RSSI+", Count="+Count+", EPCLength="+EpcLength+", EPC="+J4210U.toHex(EPC);
		}
	}
	
	public static class ReaderInfo {
		public int Serial = 0;
		public byte[] VersionInfo = new byte[2];
		public byte Antenna = 0;
		
		public byte ComAdr = 0;
		public byte ReaderType = 0;
		public byte Protocol = 0;
		public byte Band = 0;
		public byte Power = 0;
		public int ScanTime = 0;
		public byte BeepOn = 0;
		
		public byte Reserved1 = 0;
		public byte Reserved2 = 0;
		
		public int MaxFreq = 0;
		public int MinFreq = 0;
		public int BaudRate = 0;
		
		public byte[] toByteArray() {
			ByteBuffer bb = ByteBuffer.allocate(128);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			
			bb.putInt(Serial);
			bb.put(VersionInfo[0]);
			bb.put(VersionInfo[1]);
			bb.put(Antenna);
			bb.put(ComAdr);
			bb.put(ReaderType);
			bb.put(Protocol);
			bb.put(Band);
			bb.put(Power);
			bb.put((byte)((ScanTime/100) & 0xFF));
			bb.put(BeepOn);
			
			bb.put(Reserved1);
			bb.put(Reserved2);
			
			bb.putInt(MaxFreq);
			bb.putInt(MinFreq);
			bb.putInt(BaudRate);
			
			return bb.array();
		}
		
		public ReaderInfo(byte[] raw) {
			ByteBuffer bb = ByteBuffer.wrap(raw);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			
			Serial = bb.getInt();
			VersionInfo[0] = bb.get();
			VersionInfo[1] = bb.get();
			Antenna = bb.get();
			ComAdr = bb.get();
			ReaderType = bb.get();
			Protocol = bb.get();
			Band = bb.get();
			Power = bb.get();
			ScanTime = (bb.get() & 0xFF) * 100;
			BeepOn = bb.get();
			
			Reserved1 = bb.get();
			Reserved2 = bb.get();
			
			MaxFreq = bb.getInt();
			MinFreq = bb.getInt();
			BaudRate = bb.getInt();
		}
		
		public ReaderInfo(){}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Serial: "+Serial+"\n");
			sb.append("Version: "+VersionInfo[0]+"."+VersionInfo[1]+"\n");
			sb.append("Antenna: "+Antenna+"\n");
			sb.append("ComAdr: "+ComAdr+"\n");
			sb.append("ReaderType: "+ReaderType+"\n");
			sb.append("Protocol: "+Protocol+"\n");
			sb.append("Band: "+Band+"\n");
			sb.append("Power: "+Power+"\n");
			sb.append("ScanTime: "+ScanTime+"\n");
			sb.append("BeepOn: "+BeepOn+"\n");
			sb.append("MaxFreq: "+MaxFreq+"KHz"+"\n");
			sb.append("MinFreq: "+MinFreq+"KHz"+"\n");
			sb.append("BaudRate: "+BaudRate+"\n");
			return sb.toString();
		}
	}

	static {
		System.loadLibrary("j4210u");
	}

	// Declare an instance native method sayHello() which receives no parameter
	// and returns void
	public native short AvailablePorts(byte[] ports);
	public native byte OpenComPort(byte[] port, int baud);
	public native void CloseComPort();
	public native byte LoadSettings(byte[] buff);
	public native byte SaveSettings(byte[] buff);
	public native int  Inventory(byte filter);	
	public native byte GetResult(byte[] scanresult, int index);
	public native byte GetTID(byte[] epc, byte epclen, byte[] tid, byte[] tidlen);
	
	public native byte SetPassword(byte[] epc, byte epcLen, byte[] pass, byte size);
	public native byte SetKillPassword(byte[] epc, byte epcLen, byte[] pass, byte size);
	public native void LastError(byte[] buffer);
	
	public native byte Auth(byte[] password, byte size);
	public native byte WriteMemWord(byte[] epc, byte epclen, byte[] data, byte windex);
	public native byte ReadMemWord(byte[] epc, byte epclen, byte[] data, byte windex);
	public native byte SetFilter(int maskAdrInByte, int maskLenInByte, byte[] maskDataByte);
	
	public native byte TagExists(byte[] epc, byte epclen);
	public native byte WriteEpcWord(byte[] epc, byte epclen, byte[] data, byte windex);
	
	public native byte GetTagInfo(byte[] tid, byte[] info);
	
	public native byte GetGPI(byte gpino);
	public native byte SetGPO(byte gpono);
	public native void LibVersion(byte[] version);
	
	public native byte SetQ(byte q);
	public native byte SetSession(byte session);
	
	private static byte[] LIB_VERSION = {0,0};

	public static byte[] hex2bytes(String hex, int arraySize) {
		if (hex.length() % 2 != 0)
			hex = "0" + hex; // make the length even;
		byte[] bytes = new byte[arraySize];
		for(int i=0;i<hex.length();i+=2) {
			//if (i == bytes.length)
			//	break;
			String h = hex.substring(i, i+2);
			int n = Integer.parseInt(h, 16);
			bytes[i/2] = (byte)(n & 0xFF);
		}
		return bytes;
	}
	
	public String getVersion() {
		byte[] v = {0,0};
		LibVersion(v);
		return v[0] + "." + v[1];
	}
	
	private static String getNullTerminatedString(byte[] str) {
		StringBuffer b = new StringBuffer();
		for(int i=0;i<str.length;i++) {
			if (str[i] == 0)
				break;
			b.append((char)str[i]);
		}
		return b.toString();
	}
	
	public boolean tagExists(byte[] epc) throws Exception {
		byte found = TagExists(epc, (byte)epc.length);
		return found == 1;
	}
	
	public boolean getGPInput(int gpNumber) throws Exception {
		byte b = GetGPI((byte)gpNumber);
		if (b < 0) {
			throw new Exception("Failed to obtain GP Input for "+gpNumber);
		}
		return b == 1;
	}
	
	public void setGPOutput(int gpNumber) throws Exception {
		byte b = SetGPO((byte)gpNumber);
		if (b < 0) {
			throw new Exception("Failed to obtain GP Input for "+gpNumber);
		}
	}
	
	public void filter(int offsetByte, byte[] maskBytes) throws Exception {
		byte success = SetFilter(offsetByte, maskBytes.length, maskBytes);
		if (success == 0) {
			throw new Exception(error());
		}
	}
	
	public void writeWord(byte[] epc, byte[] word, int windex) throws Exception {
		byte success = WriteMemWord(epc, (byte)epc.length, word, (byte)(windex & 0xFF));
		if (success == 0) {
			throw new Exception(error());
		}
	}
	
	public byte[] readWord(byte[] epc, int windex) throws Exception {
		
		byte[] word = {0,0};
		byte success = ReadMemWord(epc, (byte)epc.length, word, (byte)(windex & 0xFF));
		if (success == 0) {
			throw new Exception(error());
		}
		return word;
	}
	
	public void saveSettings(ReaderInfo ri) throws Exception {
		byte success = SaveSettings(ri.toByteArray());
		if (success == 0)
			throw new Exception("Failed to save settings.");
	}
	
	public ReaderInfo loadSettings() throws Exception {
		ReaderInfo ri = new ReaderInfo();
		byte[] bytes = ri.toByteArray();
		byte success = LoadSettings(bytes);
		ri = new ReaderInfo(bytes);
		if (success == 0)
			throw new Exception("Failed to load settings. "+error());
		return ri;
	}
	
	public byte[] getTID(byte[] epc) throws Exception {
		
		byte[] tid = new byte[epc.length];
		byte[] tidlen = {0};
		byte success = GetTID(epc, (byte)epc.length, tid, tidlen);
		if (success == 0) {
			throw new Exception("FAILED to acquire TID.");
		}
		//System.out.println(toHex(tid));
		return Arrays.copyOf(tid, tidlen[0]);
	}
	
	public boolean exists(byte[] epc) throws Exception {
		
		byte success = TagExists(epc, (byte)epc.length);
		return (success == 1) ? true : false;
	}
	
	public void setPassword(byte[] epc, byte[] password) throws Exception {
		
		if (password.length < 4) {
			throw new Exception("Password length must at least be 8 bytes.");
		}
		byte success = SetPassword(epc, (byte)epc.length, password, (byte)password.length);
		if (success == 0) {
			throw new Exception("FAILED to set password for the tag.");
		}
	}
	
	public void setKillPassword(byte[] epc, byte[] password) throws Exception {
		
		if (password.length < 4) {
			throw new Exception("Password length must at least be 8 bytes.");
		}
		byte success = SetKillPassword(epc, (byte)epc.length, password, (byte)password.length);
		if (success == 0) {
			throw new Exception("FAILED to set password KILL for the tag.");
		}
	}
	
	private byte[] createNullTerminatedString(String z) {
		z += "\0";
		try {
		return z.getBytes("UTF-8");
		} catch(Exception e) {}
		return null;
	}
	
	/**
	 * 
	 */
	public J4210U() {
	}
	
	public String[] listPorts() throws Exception {
		
		byte[] ports = new byte[256*8];
		int n = AvailablePorts(ports);
		String[] s = new String[n];
		String z = this.getNullTerminatedString(ports);
		s = z.split("\\n");
		return s;
	}
	
	public TagInfo getTagInfo(byte[] tid) throws Exception {
		
		byte[] info = new byte[1024];
		byte success = GetTagInfo(tid, info);
		if (success == 0) {
			//throw new Exception("Failed to obtain Tag Info. "+error());
		}
		TagInfo ti = new TagInfo(info);
		return ti;
	}
	
	public boolean format() throws Exception {
		
		return false; //uhf_.Format() == 1;
	}
	
	/**
	 * Open the reader at the Com Port. The COM port may be found by opening the Device Manager
	 * in Windows.
	 * 
	 * @param comPort com port string, for example "COM31", etc.
	 * @throws Exception a general exception is thrown.
	 */
	public void open(String comPort) throws Exception {
		
		comPort += "\0";
		byte[] port = comPort.getBytes("UTF-8");
		byte ok = OpenComPort(port, 57600);
		if (ok == 0) 
			throw new Exception("Failed to connect to J4210U reader. " + error());
	}
	
	/**
	 * Writes an authentication password to be used during successive scans.
	 * 
	 * @param password a valid password of 8 byte.
	 * @throws Exception a general exception is thrown.
	 */
	public void auth(byte[] password) throws Exception {
		
		byte success = Auth(password, (byte)(password.length & 0xFF));
		if (success == 0) {
			throw new Exception(error());
		}
	}

	/**
	 * Writes EPC Word (16-bit) to the tag pointed by the epc value. The words will be written
	 * into EPC memory's windex (word index) position.
	 * 
	 * @param epc tag's EPC value.
	 * @param word a single word to write (16-bit or 2-bytes)
	 * @param windex a word index (not byte index). A word index is even byte indexes.
	 * @throws Exception a general exception is thrown.
	 */
	public void writeEpcWord(byte[] epc, byte[] word, int windex) throws Exception {
		
		byte success = WriteEpcWord(epc, (byte)epc.length, word, (byte)windex);
		if (success == 0)
			throw new Exception("Failed to write EPC word.");
	}

	/**
	 * Get TagInfo from TID.
	 * 
	 * @param tid TID of the tag.
	 * @return TagInfo object.
	 * @throws Exception a general exception is thrown.
	 */
	public TagInfo type(byte[] tid) throws Exception {
		
		byte[] data = new byte[256];
		int type = GetTagInfo(tid, data);
		TagInfo taginfo = new TagInfo(data);
		return taginfo;
	}

	/**
	 * Closes the com port, if opened.
	 * @throws Exception a general exception is thrown.
	 */
	public void close() throws Exception {
		
		CloseComPort();
	}

	/**
	 * Convert data into hex.
	 * 
	 * @param data data as byte array.
	 * @return hex string.
	 */
	public static String toHex(byte[] data) {
		if (data == null)
			return null;
		StringBuffer b = new StringBuffer();
		for(int i=0;i<data.length;i++) {
			String st = String.format("%02X", data[i]);
			b.append(st);
		}
		return b.toString().toUpperCase();
	}
	
	public int inventory(boolean filter) throws Exception {
		int count = Inventory((filter) ? (byte)1 : 0);
		return count;
	}
	
	public String error() throws Exception {
		byte[] buffer = new byte[128];
		LastError(buffer);
		return getNullTerminatedString(buffer);
	}
	
	public void setQ(int q) throws Exception {
		int b = SetQ((byte)q);
		if (b>0) {
			throw new Exception("Q value must be between 0 to 15.");
		}
	}

	public void setSession(int session) throws Exception {
		int b = SetSession((byte)session);
		if (b>3) {
			throw new Exception("Session value must be between 0 to 3.");
		}
	}

	/**
	 * Gets scan result at index.
	 * 
	 * @param index a valid index of scan results.
	 * @return ScanResult object.
	 * @throws Exception a general exception is thrown.
	 */
	public ScanResult getResult(int index) throws Exception {
		byte[] section = new byte[ScanResult.size()];
		byte success = GetResult(section, index);
		if (success != 1) {
			throw new Exception("No Result found at index: "+index + ". "+error());
		}
		ScanResult sr = new ScanResult(section);
		//System.out.println(sr.toString());
		return sr;
	}

	public static void main(String[] args) {
		J4210U uhf = new J4210U();
		try {
			String[] ports = uhf.listPorts();
			uhf.open("com4");
			int count = uhf.inventory(false);
			if (count == 0) {
				System.out.println("No Tag found.");
				return;
			}
			byte[] epc = null;
			for(int i=0;i<count;i++) {
				ScanResult sr = uhf.getResult(i);
				//byte[] tid = uhf.getTID(sr.EPC);
				//System.out.println("TID="+toHex(tid));
				epc = sr.EPC;
			}
			//System.out.println("Card Name: "+nfc.type());
			uhf.writeWord(epc, new byte[]{0x11, 0x22}, 0);
			byte[] data = uhf.readWord(epc, 0);
			System.out.println("DATA = "+toHex(data));
			uhf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
