/**
 * 
 */
package jence.jni;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

/**
 * This is the main class that connects to the device driver and 
 * performs all operations. 
 * 
 * @author Ejaz Jamil
 *
 */
public class J4210U {
	/**
	 * List of all supported Chip type.
	 * 
	 * @author Ejaz Jamil
	 *
	 */
	public enum TagType {
        UNKNOWN,

        // ALIEN Family
        HIGGS_3,
        HIGGS_4,
        HIGGS_EC,
        HIGGS_9,

        // IMPINJ Family
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
        IMPINJ_M770,
        IMPINJ_M775,

        // NXP Family
        UCODE_7,
        UCODE_8,
        UCODE_8M,
        UCODE_DNA,
        UCODE_DNA_CITY,
        UCODE_DNA_TRACK,

        // EM Family
        EM4423,
        
        // Kiloway family
        KILOWAY_2005BR,
        KILOWAY_2005BL,
        KILOWAY_2005BT,
	};

	/**
	 * TagInfo class contains information about the tag. For example
	 * its EPC length, TID length, type of chip, Chip name, User memory
	 * size, etc.
	 * 
	 * @author Ejaz Jamil
	 *
	 */
	public static class TagInfo {
		/**
		 * Chip type.
		 */
		public TagType type;
		/**
		 * TID length in bytes.
		 */
		public int tidlen;
		/**
		 * TID value.
		 */
		public byte[] tid;
		/**
		 * Chip name.
		 */
		public String chip;
		/**
		 * EPC length in bytes.
		 */
		public int epclen;
		/**
		 * User memory size in bytes.
		 */
		public int userlen;
		/**
		 * Password length in bytes.
		 */
		public int pwdlen;

		/**
		 * Creates a TagInfo instance from provided data.
		 * The data is usually returned by the JNI library.
		 * This data can be directly fed into this parameter.
		 * 
		 * @param data data returned by the JNI library.
		 */
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

	/**
	 * ScanResult class is used to convert the byte array 
	 * returned by the JNI driver into the Antenna, RSSI,
	 * Count, Epc length and EPC value data.
	 * 
	 * @author Ejaz Jamil
	 *
	 */
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
			return "Ant="+Ant+", RSSI="+RSSI+", Count="+Count+", EPCLength="+EpcLength+", EPC="+J4210U.toHex(EPC)+", Timestamp="+getIsoTimestamp();
		}
		
		public String toJson() {
			OffsetDateTime now = OffsetDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
			return "{" +
					"\"Ant\":"+Ant+", " +
					"\"RSSI\":"+RSSI+", " +
					"\"Count\":"+Count+", " +
					"\"EPCLength\":"+EpcLength+", " +
					"\"EPC\":\""+J4210U.toHex(EPC)+"\"," +
					"\"Timestamp\":\""+getIsoTimestamp()+"\"" +
					"}";
			
		}
	}

	/**
	 * Gets current timestamp in the format yyyy-mm-dd hh:mm:ss.uuu +|-Z.
	 * Example: 2021-08-12 15:32:931 +4 indicate August 12th 2021 at 3:32PM 
	 * and 931 usec in a timezone 4 hours ahead of greenwitch mean time.
	 * 
	 * @return ISO timestamp.
	 */
	public static String getIsoTimestamp() {
		OffsetDateTime now = OffsetDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		return formatter.format(now);
	}

	/**
	 * Parses the reader information data from the device driver
	 * and assigns them to the public variables. After these variables
	 * are reassigned with new valuees, the class has {@link #toByteArray()}
	 * method to return the byte array that is needed to be passed
	 * to the driver for saving the settings.
	 * 
	 * @author Ejaz Jamil
	 *
	 */
	public static class ReaderInfo {
		/**
		 * The devices read only serial number. Assigning this variable does not have
		 * any effect.
		 */
		public int Serial = 0;
		/**
		 * The device firmware read only version number String. Assigning this variable does not have
		 * any effect.
		 */
		public byte[] VersionInfo = new byte[2];
		/**
		 * Number of antenna (read only) this device support. Assigning this variable does not have
		 * any effect.
		 */
		public byte Antenna = 0;
		
		/**
		 * Read only com address of the device. Assigning this variable does not have
		 * any effect.
		 */
		public byte ComAdr = 0;
		/**
		 * Read only reader type. Assigning this variable does not have
		 * any effect.
		 */
		public byte ReaderType = 0;
		/**
		 * Read only protocol number. Assigning this variable does not have
		 * any effect.
		 */
		public byte Protocol = 0;
		/**
		 * Frequency band to be used during scanning. This variable can be assigned 'U' (USA),
		 * 'K' (Korea), 'C' (China) and 'E' (EU).
		 */
		public byte Band = 0;
		/**
		 * Power to be used during scanning. This variable is in the range 0 - 26.
		 */
		public byte Power = 0;
		/**
		 * Scan time in 100 ms unit for scanning. The value can be 1-255 representing 100 ms
		 * - 25.5 s. A value of 10 means 1s (10 * 100 = 1000 ms)
		 */
		public int ScanTime = 0;
		/**
		 * Turn on/off beep. 0 = Off, 1 = On.
		 */
		public byte BeepOn = 0;

		/**
		 * Unused.
		 */
		public byte Reserved1 = 0;
		/**
		 * Unused.
		 */
		public byte Reserved2 = 0;
		
		/**
		 * Read only maximum frequency for the band. Assigning this variable does not have
		 * any effect.
		 */
		public int MaxFreq = 0;
		/**
		 * Read only minimum frequency for the band. Assigning this variable does not have
		 * any effect.
		 */
		public int MinFreq = 0;
		/**
		 * Baud rate to be used for next use. The allowed values are 9600, 19200, 38400,
		 * 57600 and 115200. Any other values will be rejected. After saving the setting,
		 * the effect is immediate. That means, the device is to be reconnected using the
		 * new baud rate.
		 */
		public int BaudRate = 0;

		/**
		 * Converts the setting into byte array.
		 * 
		 * @return byte array.
		 */
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

		/**
		 * Creates object from serialized data (byte array).
		 * 
		 * @param raw byte array containing valid values.
		 */
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

		/**
		 * Default constructor.
		 */
		public ReaderInfo(){}

		/**
		 * Converts to printable string.
		 */
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

	/**
	 * Loads driver library.
	 */
	static {
		System.loadLibrary("j4210u");
	}

	//--------------------------[JNI FUNCTIONS]---------------------------------
	// Declare an instance native method sayHello() which receives no parameter
	// and returns void
	private native short AvailablePorts(byte[] ports);
	private native byte OpenComPort(byte[] port, int baud);
	private native void CloseComPort();
	private native byte LoadSettings(byte[] buff);
	private native byte SaveSettings(byte[] buff);
	private native int  Inventory(byte filter);	
	private native byte GetResult(byte[] scanresult, int index);
	private native byte GetTID(byte[] epc, byte epclen, byte[] tid, byte[] tidlen);
	
	private native byte SetPassword(byte[] epc, byte epcLen, byte[] pass, byte size);
	private native byte SetKillPassword(byte[] epc, byte epcLen, byte[] pass, byte size);
	private native void LastError(byte[] buffer);
	
	private native byte Auth(byte[] password, byte size);
	private native byte WriteMemWord(byte[] epc, byte epclen, byte[] data, byte windex);
	private native byte ReadMemWord(byte[] epc, byte epclen, byte[] data, byte windex);
	private native byte SetFilter(int maskAdrInByte, int maskLenInByte, byte[] maskDataByte);
	
	private native byte TagExists(byte[] epc, byte epclen);
	private native byte WriteEpcWord(byte[] epc, byte epclen, byte[] data, byte windex);
	
	private native byte GetTagInfo(byte[] tid, byte[] info);
	
	private native byte GetGPI(byte gpino);
	private native byte SetGPO(byte gpono);
	private native void LibVersion(byte[] version);
	
	private native byte SetQ(byte q);
	private native byte SetSession(byte session);
	//^^^^^^^^^^^^^^^^^^^^^^^^^^[JNI FUNCTIONS]^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	
	private static byte[] LIB_VERSION = {0,0};
	
	// locals
	private static boolean log_ = false;
	private static FileOutputStream logstream_ = null;
	
	private void log(String text) {
		try {
			if (logstream_ == null) {
				return;
			}
			String s = text + "\n";
			logstream_.write(s.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Converts hex to byte array.
	 * 
	 * @param hex hex value as String.
	 * @param arraySize Size of the byte array to return.
	 * @return byte array generated from hex value.
	 */
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
	
	/**
	 * Gets version of the driver library.
	 * 
	 * @return version in the form of major.minor as String.
	 */
	public String getVersion() {
		byte[] v = {0,0};
		LibVersion(v);
		return v[0] + "." + v[1];
	}

	/**
	 * Extract the String from the NULL terminated String array.
	 * The byte array is synonymous to NULL terminated C String.
	 * 
	 * @param str a NULL terminated String as it is in C programming language.
	 * @return String represented by the byte array.
	 */
	private static String getNullTerminatedString(byte[] str) {
		StringBuffer b = new StringBuffer();
		for(int i=0;i<str.length;i++) {
			if (str[i] == 0)
				break;
			b.append((char)str[i]);
		}
		return b.toString();
	}

	/**
	 * Checks if there is a tag with the given EPC.
	 * 
	 * @param epc full EPC.
	 * @return <code>true</code>, if found.
	 * @throws Exception
	 */
	public boolean tagExists(byte[] epc) throws Exception {
		byte found = TagExists(epc, (byte)epc.length);
		log("{tagExists(EPC="+toHex(epc)+") : "+found + "}");
		return found == 1;
	}

	/**
	 * Gets GPI input from the specified GPI port.
	 * 
	 * @param gpNumber 1 -> GPI0, 2 -> GPI1.
	 * @return GPI input state.
	 * @throws Exception
	 */
	public boolean getGPInput(int gpNumber) throws Exception {
		byte b = GetGPI((byte)gpNumber);
		if (b < 0) {
			throw new Exception("Failed to obtain GP Input for "+gpNumber);
		}
		log("{getGPInput(gpNumber="+gpNumber+") : "+b + "}");
		return b == 1;
	}

	/**
	 * Gets GPO0 or GPO1 to desired value. 
	 * 
	 * @param gpNumber GPO0 is bit-0 and GPO1 is bit-1.
	 * @throws Exception
	 */
	public void setGPOutput(int gpNumber) throws Exception {
		byte b = SetGPO((byte)gpNumber);
		if (b < 0) {
			throw new Exception("Failed to obtain GP Input for "+gpNumber);
		}
		log("{setGPOutput(gpNumber="+gpNumber+") : "+b + "}");
	}

	/**
	 * Creates a filter for use during scanning.
	 * 
	 * @param offsetByte offset byte (word unit, multiple of 2)
	 * @param maskBytes mask bytes, can be any value. The array size must be multiple of 2.
	 * @throws Exception
	 */
	public void filter(int offsetByte, byte[] maskBytes) throws Exception {
		byte success = SetFilter(offsetByte, maskBytes.length, maskBytes);
		if (success == 0) {
			throw new Exception(error());
		}
		log("{filter("+offsetByte+" = "+offsetByte+", maskBytes = "+toHex(maskBytes)+") : "+success + "}");
	}

	/**
	 * Writes 2 byte word into the User memory pointed by word index (even) of the tag pointed by the EPC value.
	 * 
	 * @param epc a valid EPC value.
	 * @param word 2 byte word to write.
	 * @param windex word index (even)
	 * @throws Exception
	 */
	public void writeWord(byte[] epc, byte[] word, int windex) throws Exception {
		byte success = WriteMemWord(epc, (byte)epc.length, word, (byte)(windex & 0xFF));
		log("{writeWord(EPC = "+toHex(epc)+", word="+toHex(word)+", windex = "+windex+") : "+success+"}");		
		if (success == 0) {
			throw new Exception(error());
		}
	}

	/**
	 * Reads a word from User memory with word index for the tag with valid EPC value.
	 * 
	 * @param epc a valid EPC value.
	 * @param windex word index (must be even).
	 * @return 2 byte word of the content of memory.
	 * @throws Exception
	 */
	public byte[] readWord(byte[] epc, int windex) throws Exception {		
		byte[] word = {0,0};
		byte success = ReadMemWord(epc, (byte)epc.length, word, (byte)(windex & 0xFF));
		log("{readWord(EPC = "+toHex(epc)+", windex = "+windex+") : "+success+"}");
		if (success == 0) {
			throw new Exception(error());
		}
		return word;
	}

	/**
	 * Saves the {@link ReaderInfo} as current settings.
	 * 
	 * @param ri ReaderInfo object.
	 * @throws Exception
	 */
	public void saveSettings(ReaderInfo ri) throws Exception {
		byte success = SaveSettings(ri.toByteArray());
		if (success == 0)
			throw new Exception("Failed to save settings.");
	}

	/**
	 * Loads current settings into {@link ReaderInfo} object.
	 * 
	 * @return ReaderInfo object with current settings.
	 * @throws Exception
	 */
	public ReaderInfo loadSettings() throws Exception {
		ReaderInfo ri = new ReaderInfo();
		byte[] bytes = ri.toByteArray();
		byte success = LoadSettings(bytes);
		ri = new ReaderInfo(bytes);
		if (success == 0)
			throw new Exception("Failed to load settings. "+error());
		log("loadSettings: "+ri);
		return ri;
	}

	/**
	 * Gets the TID for the tag with the EPC
	 * 
	 * @param epc a valid EPC.
	 * @return TID as byte array.
	 * @throws Exception
	 */
	public byte[] getTID(byte[] epc) throws Exception {
		
		byte[] tid = new byte[epc.length];
		byte[] tidlen = {0};
		byte success = GetTID(epc, (byte)epc.length, tid, tidlen);
		if (success == 0) {
			throw new Exception("FAILED to acquire TID.");
		}
		//System.out.println(toHex(tid));
		log("{getTID(EPC = "+toHex(epc)+") : "+success+"}");
		return Arrays.copyOf(tid, tidlen[0]);
	}

	/**
	 * Checks if the tag pointed by the EPC exists nearby.
	 * 
	 * @param epc a valid EPC.
	 * @return <code>true</code>, if exists.
	 * @throws Exception
	 */
	public boolean exists(byte[] epc) throws Exception {
		byte success = TagExists(epc, (byte)epc.length);
		log("{exists(EPC = "+toHex(epc)+") : "+success+"}");
		return (success == 1) ? true : false;
	}

	/**
	 * Sets password for the tag with given EPC.
	 * 
	 * @param epc a valid EPC.
	 * @param password 4 byte password. Default password is all zeros.
	 * @throws Exception
	 */
	public void setPassword(byte[] epc, byte[] password) throws Exception {		
		if (password.length < 4) {
			throw new Exception("Password length must at least be 8 bytes.");
		}
		byte success = SetPassword(epc, (byte)epc.length, password, (byte)password.length);
		if (success == 0) {
			throw new Exception("FAILED to set password for the tag.");
		}
		log("{setPassword(EPC = "+toHex(epc)+", password = "+toHex(password)+") : "+success+"}");
	}
	
	public void setKillPassword(byte[] epc, byte[] password) throws Exception {		
		if (password.length < 4) {
			throw new Exception("Password length must at least be 8 bytes.");
		}
		byte success = SetKillPassword(epc, (byte)epc.length, password, (byte)password.length);
		if (success == 0) {
			throw new Exception("FAILED to set password KILL for the tag.");
		}
		log("{setPassword(EPC = "+toHex(epc)+", password = "+toHex(password)+") : "+success+"}");
	}

	/**
	 * The driver sends a NULL terminated string. This method finds the NULL and converts
	 * the String into byte array
	 * 
	 * @param z String as byte array.
	 * @return
	 */
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

	/**
	 * List the available serial ports.
	 * 
	 * @return array of port names.
	 * @throws Exception
	 */
	public String[] listPorts() throws Exception {
		
		byte[] ports = new byte[256*8];
		int n = AvailablePorts(ports);
		String[] s = new String[n];
		String z = this.getNullTerminatedString(ports);
		s = z.split("\\n");
		log("{listPorts() : "+s+"}");
		return s;
	}

	/**
	 * Gets tag detail information from the tag's TID.
	 * 
	 * @param tid a valid TID.
	 * @return {@link jence.jni.TagInfo} object.
	 * @throws Exception
	 */
	public TagInfo getTagInfo(byte[] tid) throws Exception {
		
		byte[] info = new byte[1024];
		byte success = GetTagInfo(tid, info);
		if (success == 0) {
			//throw new Exception("Failed to obtain Tag Info. "+error());
		}
		TagInfo ti = new TagInfo(info);
		log("{getTagInfo(tid = "+toHex(tid)+") : "+ti+"}");
		return ti;
	}

	/**
	 * [Not implemented]
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean format() throws Exception {
		
		return false; //uhf_.Format() == 1;
	}
	
	/**
	 * Open the reader at the Com Port. The COM port may be found by opening the Device Manager
	 * in Windows.
	 * 
	 * @param comPort com port string, for example "COM31", etc.
	 * @param baudrate TODO
	 * @throws Exception a general exception is thrown.
	 */
	public void open(String comPort, int baudrate) throws Exception {		
		comPort += "\0";
		byte[] port = comPort.getBytes("UTF-8");
		byte ok = OpenComPort(port, baudrate);
		if (ok == 0) 
			throw new Exception("Failed to connect to J4210U reader. " + error());
		log("{open(comPort = "+comPort+", baudrate = "+baudrate+") : "+ok+"}");
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
		log("{auth(password = "+toHex(password)+") : "+success+"}");
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
		log("{writeEpcWord(EPC = "+toHex(epc)+", word = "+toHex(word)+", windex = "+windex+")}");
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
		log("{close()}");
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

	/**
	 * Scans with or without using filter. 
	 * 
	 * @param filter if <code>true</code>, the filter specified by {@link #SetFilter(int, int, byte[])}
	 * will be used.
	 * 
	 * @return number of inventory items (tags) scanned.
	 * @throws Exception
	 */
	public int inventory(boolean filter) throws Exception {
		int count = Inventory((filter) ? (byte)1 : 0);
		log("{inventory(filter = "+filter+") : "+count + "}" );
		return count;
	}

	/**
	 * Returns the last error message.
	 * 
	 * @return last error message.
	 * @throws Exception
	 */
	public String error() throws Exception {
		byte[] buffer = new byte[128];
		LastError(buffer);
		String err = getNullTerminatedString(buffer);
		log("{error() : "+err+"}");
		return err;
	}

	/**
	 * Sets Q number for the scan. For a large number of tags, set this to above 5.
	 * 
	 * @param q
	 * @throws Exception
	 */
	public void setQ(int q) throws Exception {
		int b = SetQ((byte)q);
		if (b>0) {
			throw new Exception("Q value must be between 0 to 15.");
		}
		log("{setQ(q = "+q+")}");
	}

	/**
	 * Sets session number for the reader. If there are three other readers, then each should
	 * be assigned a different session number so the one reader does not affect the other
	 * reader's scan.
	 * 
	 * @param session Session number 0 - 3.
	 * @throws Exception
	 */
	public void setSession(int session) throws Exception {
		int b = SetSession((byte)session);
		if (b>0) {
			throw new Exception("Session value must be between 0 to 3.");
		}
		log("{setSession(session = "+session+")}");
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
		log("{getResult(index = "+index+") : "+sr+"}");
		return sr;
	}

	/**
	 * Enable/disable logging.
	 * 
	 * @param log <code>true</code>, enables logging.
	 */
	public void setLog(boolean log) {
		log_ = log;
		if (!log_) {
			if (logstream_ != null) {
				try {
					logstream_.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				logstream_ = null;
			}
		} else {
			try {
				logstream_ = new FileOutputStream(new File(J4210U.class.getName()+".log"));
				String s = "{LOG start @ "+new Date() + "}";
				logstream_.write(s.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * The code here is for development purposes only. This code may not run in your
	 * environment.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		J4210U uhf = new J4210U();
		try {
			String[] ports = uhf.listPorts();
			uhf.open("com4", 57600);
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
