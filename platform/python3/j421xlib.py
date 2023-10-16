# Copyright, Jence (c) 2022, All Rights Reserved
#
# MIT LICENSE
# Permission is hereby granted, free of charge, to any person obtaining 
# a copy of this software and associated documentation files (the "Software"), 
# to deal in the Software without restriction, including without limitation 
# the rights to use, copy, modify, merge, publish, distribute, sublicense, 
# and/or sell copies of the Software, and to permit persons to whom the 
# Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included 
# in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
# OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
# THE SOFTWARE.

# Author: Ejaz Jamil
# Soalib Incorporated, Massachusetts, USA
# Version: 1.2
# https://www.soalib.com
#
# J4210 Class Library for Python version 3.x
# This is a Python class for use with J4210 series of readers.
# This is a class library only. This class should be called
# from your program.
# To use this library, all the driver libraries should be
# on Python path or on the same directory of this class file.
# If the hardware libraries are not in path, then move the
# libraries to the directory where this library is. All the 
# dependent libraries must also be on path on in the same
# directory.

# import the module
from ctypes import *
import os
import platform
import binascii
import enum

class TagType(enum.Enum):
    """
      TagType class holds the types of chip the driver can detect automatically.
    """
    UNKNOWN = 0

    # ALIEN Family
    HIGGS_3 =   1
    HIGGS_4 =   2
    HIGGS_EC =  3
    HIGGS_9 =   4
    HIGGS_10 =  5

    # IMPINJ Family
    MONZA_4QT =   6
    MONZA_4E =    7
    MONZA_4D =    8
    MONZA_4I =    9
    MONZA_5 =     10
    MONZA_R6 =    11
    MONZA_R6P =   12
    MONZA_X2K =   13
    MONZA_X8K =   14
    IMPINJ_M730 = 15
    IMPINJ_M750 = 16
    IMPINJ_M770 = 17
    IMPINJ_M775 = 18

    # NXP Family
    UCODE_7 =         19
    UCODE_8 =         20
    UCODE_8M =        21
    UCODE_DNA =       22
    UCODE_DNA_CITY =  23
    UCODE_DNA_TRACK = 24

    # EM Family
    EM4423 = 25

    # Kiloway family
    KILOWAY_2005BR = 26
    KILOWAY_2005BL = 27
    KILOWAY_2005BT = 28

class TagInfo():
    """
       TagInfo class stores details of the tags memory size information.
    """		
    # tagtype : 4, tidlen : 4, tid : 64, chip : 16, epclen : 4, userlen : 4, pwdlen : 4
    def __init__(self, bb = None):
        n0 = 0; n1 = 4
        self.TagType = int.from_bytes(bb[n0:n1],"little")
        n0 = n1; n1 += 4
        self.tidlen = int.from_bytes(bb[n0:n1],"little")
        n0 = n1; n1 += 64
        self.tid = bb[n0:n0+self.tidlen]
        n0 = n1; n1 += 16
        self.chip = bb[n0:n1]
        n0 = n1; n1 += 4
        self.epclen = int.from_bytes(bb[n0:n1],"little")
        n0 = n1; n1 += 4
        self.userlen = int.from_bytes(bb[n0:n1],"little")
        n0 = n1; n1 += 4
        self.pwdlen = int.from_bytes(bb[n0:n1],"little")
        return

    def getChipName(self):
        """
        Returns chip name as string

        :param None

        :return: chip name as string
        """
        return self.chip.decode("utf-8")

    def echo(self):
        """
        Echos the tag type on the console.

        :param None

        :return: None
        """
        print("TagInfo{")
        print("\tTagType =          ", self.TagType)
        print("\tTID Length =       ", self.tidlen)
        h = binascii.hexlify(self.tid).decode('utf-8')
        print("\tTID =              ", h)
        print("\tChip Used =        ", self.chip.decode("utf-8"))
        print("\tEPC Length =       ", self.epclen)
        print("\tUser Memory Size = ", self.userlen)
        print("\tPassword Size =    ", self.pwdlen)
        print("}")

class ScanResult():
    """
       Stores scanned result.
    """
    def __init__(self, bb = None):
        if (bb == None):
            self.Ant = 0
            self.RSSI = 0
            self.Count = 0
            self.EpcLength = 0
            self.EPC = bytes(12)
            return

        self.Ant = int.from_bytes(bb[0:1],"little") #bb[0]
        self.RSSI = int.from_bytes(bb[1:2],"little", signed="True") #bb[1]
        self.Count = int.from_bytes(bb[2:3],"little") #bb[2]
        self.EpcLength = int.from_bytes(bb[3:4],"little") #bb[3]
        self.EPC = bb[4:16]
        return

    def echo(self):
        """
           Echos a printout of the Scan Result to the console.

           :param None
           :result: None
        """
        print("ScanResult{")
        print("\tAnt: ", self.Ant)
        print("\tRSSI: ", self.RSSI)
        print("\tCount: ", self.Count)
        print("\tEpcLength: ", self.EpcLength)
        h = binascii.hexlify(self.EPC).decode('utf-8').upper()
        print("\tEPC: ", h)
        print("}")
        return
    
    def hexEPC(self):
        """
           Converts EPC to hex value (upper case).

           :param None
           :return: EPC in hex (capital)
        """
        return binascii.hexlify(self.EPC).decode('utf-8').upper()

    def line(self):
        """
           Oututs the Scan Result in a single line with all column aligned.

           :param None
           :return: None
        """
        h = binascii.hexlify(self.EPC).decode('utf-8').upper()
        print("Ant: ", self.Ant,\
        " | RSSI: %02d" % self.RSSI,\
        " | EpcLength: %02d" % self.EpcLength,\
        " | Count: % 3d" % self.Count,\
        " | EPC: ", h)
    
class ReaderInfo():
    """
       Stores the Reader Info.
    """

    def __init__(self, bb = None):
        if (bb == None):
            self.Serial = 0
            self.major = 0
            self.minor = 0
            self.Version = 0
            self.Antenna = 0
            self.ComAdr = 0
            self.ReaderType = 0
            self.Band = 0
            self.Power = 0
            self.ScanTime = 0
            self.BeepOn = 0

            self.MaxFreq = int(0)
            self.MinFreq = int(0)
            self.BaudRate = int(0)
            return
        
        #_fields_ = [('Serial', c_int), ('VersionMajor', c_char),('VersionMinor', c_char)]
        self.Serial = int.from_bytes(bb[0:4],"little")
        self.major = bb[4]
        self.minor = bb[5]
        major = str(bytes(bb[4])[0])
        minor = str(bytes(bb[5])[0])
        self.Version = str(major) + "." + str(minor)
        self.Antenna = bytes(bb[6])[0]
        self.ComAdr = bytes(bb[7])[0]
        self.ReaderType = bytes(bb[8])[0]
        self.Protocol = bytes(bb[9])[0]
        self.Band = bytes(bb[10])[0]
        self.Power = bytes(bb[11])[0]
        self.ScanTime = bytes(bb[12])[0]
        self.BeepOn = bytes(bb[13])[0]

        reserved1 = bytes(bb[14])[0]
        reserved2 = bytes(bb[15])[0]
        
        self.MaxFreq = int.from_bytes(bb[16:19],"little")
        self.MinFreq = int.from_bytes(bb[20:23],"little")
        self.BaudRate = int.from_bytes(bb[24:27],"little")

    def echo(self):
        """
           Echos the content of Reader Info to the console.

           :param None
           :return: None
        """
        print("ReaderInfo{")
        print('\tSerial:     ', self.Serial)
        print('\tVersion:    ', self.Version)
        print('\tAntenna:    ', self.Antenna)
        print('\tComAdr:     ', self.ComAdr)
        print('\tReaderType: ', self.ReaderType)
        print('\tProtocol:   ', self.Protocol.to_bytes(1,'little').hex().upper())
        print('\tBand:       ', chr(self.Band))
        print('\tPower:      ', self.Power)
        print('\tScanTime:   ', self.ScanTime)
        print('\tBeepOn:     ', bool(self.BeepOn))
        print('\tMaxFreq:    ', self.MaxFreq)
        print('\tMinFreq:    ', self.MinFreq)
        print('\tBaudRate:   ', self.BaudRate)
        print("}")
    
    def tobytes(self):
        """
           Converts the content to Reader Info int byte array
           for writing to the device.

           :param None
           :return: array of bytes
        """
        buf = []
        buf += self.Serial.to_bytes(4, 'little')
        buf += self.major
        buf += self.minor
        buf += self.Antenna.to_bytes(1,"little")
        buf += self.ComAdr.to_bytes(1,"little")
        buf += self.ReaderType.to_bytes(1,"little")
        buf += self.Protocol.to_bytes(1,"little")
        buf += self.Band.to_bytes(1,"little")
        buf += self.Power.to_bytes(1,"little")
        buf += self.ScanTime.to_bytes(1,"little")
        buf += self.BeepOn.to_bytes(1,"little")

        buf += b'\x00'
        buf += b'\x00'

        buf += self.MaxFreq.to_bytes(4,"little")
        buf += self.MinFreq.to_bytes(4,"little")
        buf += self.BaudRate.to_bytes(4,"little")

        # print(bytes(buf))
        return bytes(buf)

# create a Geek class
class J4210():
    """
       This the main class which calls the native functions.
    """
    
    # constructor
    def __init__(self):
        """
           The native libraries must be on path. For Windows, its all the dependent dll
           including the main driver j4210u.dll. For Mac OS X, its libj4210u.dylib. For
           all other OSes, its libj4210u.so.
        """
        # load the library
        # os.chdir('D:\Work\workspace\j4210u-app\platform\win64')
        # REM: All the drivers should be in path or in currect dir
        self.lib = None
        if (platform.system() == 'Windows'):
            dllpath = os.path.dirname(os.path.realpath(__file__))
            dllpath = dllpath + '\\' + 'j4210u.dll'
            print(dllpath)
            self.lib = cdll.LoadLibrary(dllpath)
        elif (platform.system() == 'Linux'):
            self.lib = cdll.LoadLibrary('libj4210u.so')
        elif (platform.system() == 'Mac OS X'):
            self.lib = cdll.LoadLibrary('libj4210u.dylib')
        lib = self.lib
        return

    def getSupportedChips(self):
        """
           Returns a comma separated string of supported chip types.
        """
        x = ""
        for a in (TagType):
            if (a.value == 0):
                continue
            #print(a.name)
            if (len(x) > 0):
                x += ","
            x += a.name
        #print(x)
        return x

    # short AvailablePorts(byte[] ports);
    def AvailablePorts(self):
        """
           Gets a list of available USB serial ports for the computer.

           :return: An array of string with USB serial device names.
        """
        self.lib.AvailablePorts.argtypes = [c_char_p]
        self.lib.AvailablePorts.retypes = [c_uint]
        arg = create_string_buffer(2048)
        n = self.lib.AvailablePorts(arg)
        str = arg.value
        str = str.decode("utf-8")
        ret = str.split('\n',n)
        ret.remove("")

        #print(ret)
        #print(n)
        del arg
        return ret
    
    # byte OpenComPort(byte[] port, int baud);
    def OpenPort(self, port, baud):
        """
           Opens the given port at the stated baud rate.

           :param port (string): Name of the port returned by the function
           AvailablePorts.
           :param baud (int): A valid baud rate. Must be one of 9600, 19200,
           38400, 57600 and 115200.
           :return: True, if connected. Otherwise False.
        """
        self.lib.AvailablePorts.argtypes = [c_char_p, c_uint]
        self.lib.AvailablePorts.retypes = [c_char]
        arg = bytes(port,"ascii")
        print('arg=', arg, ', baud=', baud)
        ret = self.lib.OpenComPort(arg, int(baud))
        del arg
        return bool(ret)

    # void CloseComPort();
    def ClosePort(self):
        """
           Closes the last open port.
        """
        self.lib.CloseComPort()
        return

    # byte LoadSettings(byte[] buff);
    def LoadSettings(self):
        """
           Loads current setting from the device hardware

           :return (ReaderInfo): Returns the ReaderInfo object with current settings.
        """
        self.lib.LoadSettings.argtypes = [c_char_p]
        self.lib.LoadSettings.retypes = [c_char]
        arg = create_string_buffer(32)
        ret = self.lib.LoadSettings(arg)
        info = ReaderInfo(arg)
        #print(ret, bytes(arg))
        del arg
        if ret == 0:
            return None
        #info.echo()
        #info.tobytes()
        return info

    # byte SaveSettings(byte[] buff);
    def SaveSettings(self, ri):
        """
           Saves the supplied setting.

           :param ri (ReaderInfo): Setting to replace the currnent settings.
           :param (True): if successful, otherwise False
        """
        self.lib.SaveSettings.argtypes = [c_char_p]
        self.lib.SaveSettings.retypes = [c_char]
        buf = ri.tobytes()
        ret = self.lib.SaveSettings(buf)
        return bool(ret)

    # int  Inventory(byte filter);	
    def Inventory(self, filter):
        """
           Performs Inventory scan with or without Filtering.

           :param filter(boolean): If True, applies the filter settings.
           :return (int): Number of tags detected or 0 if none.
        """
        self.lib.Inventory.argtypes = [c_char]
        self.lib.Inventory.retypes = [c_uint]
        ff = 0
        if (filter == True):
            ff = 1
        ret = self.lib.Inventory(ff)
        return ret

    # int  Inventory(byte filter);	
    def InventoryOne(self):
        """
           Performs single tag Inventory scan.

           :return (int): Single tags that is detected first or 0 if none.
        """
        self.lib.Inventory.retypes = [c_uint]
        ret = self.lib.InventoryOne()
        return ret

    # byte GetResult(byte[] scanresult, int index);
    def GetResult(self, index = 0):
        """
           Gets the result at the given index. Index must be less than the number
           of tags detected in previous inventory scan.

           :param index (int): zero based index, zero being the first tag.
           :return (ScanResult): Returned object contains all the scanned result data.
        """
        self.lib.GetResult.argtypes = [c_char_p, c_int]
        self.lib.GetResult.retypes = [c_char]
        buf = create_string_buffer(64)
        ret = self.lib.GetResult(buf, index)
        if (ret == 0):
            return None
        result = ScanResult(buf)
        #result.echo()
        return result

    def Bytes2Hex(self, bb):
        """
           Converts the given byte array into HEX (capital)

           :param bb (byte[]): byte array to convert.
           :return (string): HEX value of the array of bytes.
        """
        h = binascii.hexlify(bb).decode('utf-8').upper()
        return h

    def Hex2Bytes(self, hex):
        """
           Converts the given HEX string to byte array

           :param bb (string): HEX string to convert.
           :return (byte[]): byte array of the HEX value.
        """
        result = bytes.fromhex(hex)
        return result

    def Ascii2Bytes(self, s):
        """
           Converts the given ASCII string to byte array

           :param bb (string): ASCII string to convert.
           :return (byte[]): byte array of the HEX value.
        """
        return s.encode('utf8')

    def Bytes2Ascii(self, b):
        """
           Converts the given byte array into ASCII

           :param bb (byte[]): byte array to convert.
           :return (string): ASCII string of the array of bytes.
        """
        return b.decode('ascii')

    # byte GetTID(byte[] epc, byte epclen, byte[] tid, byte[] tidlen);
    def GetTID(self, epc):
        """
           Gets the TID value for the provided EPC in bytes.

           :param epc(byte[]): EPC bytes (must exist).
           :return (byte[]): The TID bytes for the tag.
        """
        self.lib.GetTID.argtypes = [c_char_p, c_char, c_char_p, c_char_p]
        self.lib.GetTID.retypes = [c_char]
        buf = create_string_buffer(64)
        tidsize = create_string_buffer(1)
        #print('EPC:', self.Bytes2Hex(epc))
        #print('EPC Len: ', len(epc))
        ret = self.lib.GetTID(epc, len(epc), buf, tidsize)

        if (ret == 0):
            return None
        tidlen = bytes(tidsize[0])[0]
        #print("TID Len: ", tidlen)
        buf = buf[:tidlen]
        #print(self.Bytes2Hex(buf))
        return buf
	
    # byte SetPassword(byte[] epc, byte epcLen, byte[] pass, byte size);
    def SetPassword(self, epc, password):
        """
           Sets password of the tag with the EPC to the given password.
           The Old password should be provided by Auth method first.

           :param epc (byte[]): EPC of the tag to change the password.
           :param password (byte[]): a 4 byte password.
           :return (boolean): True, if successful, otherwise False.
        """
        self.lib.SetPassword.argtypes = [c_char_p, c_char, c_char_p, c_char]
        self.lib.SetPassword.retypes = [c_char]
        ret = self.lib.SetPassword(epc,len(epc),password,len(password))
        return bool(ret)

    # byte SetKillPassword(byte[] epc, byte epcLen, byte[] pass, byte size);
    def SetKillPassword(self, epc, password):
        """
           Sets kill password of the tag with the EPC to the given password.
           The Old password should be provided by Auth method first.

           :param epc (byte[]): EPC of the tag to change the password.
           :param password (byte[]): a 4 byte kill password.
           :return (boolean): True, if successful, otherwise False.
        """
        self.lib.SetKillPassword.argtypes = [c_char_p, c_char, c_char_p, c_char]
        self.lib.SetKillPassword.retypes = [c_char]
        ret = self.lib.SetKillPassword(epc,len(epc),password,len(password))
        return bool(ret)

    # void LastError(byte[] buffer);
    def LastError(self):
        """
           Returns the last error message.

           :return (str): Last error message.
        """
        self.lib.LastError.argtypes = [c_char_p]
        buf = create_string_buffer(128)
        self.lib.LastError(buf);
        str = buf.value.decode("utf-8")
        return str
	
    # byte Auth(byte[] password, byte size);
    def Auth(self, password):
        """
           Sets the current password. This password is required to read memory content.

           :param (byte[]): 4 byte default password.
           :return (boolean): True, if successful, otherwise False.
        """
        self.lib.Auth.argtypes = [c_char_p]
        self.lib.Auth.retypes = [c_char]
        ret = self.lib.Auth(password, len(password))
        return bool(ret)

    # byte WriteMemWord(byte[] epc, byte epclen, byte[] data, byte windex);
    def WriteMemWord(self, epc, data, windex):
        """
           Writes 16 bit (2 byte) data simultaneously to a word address.

           :param epc (byte[]): A valid EPC of the tag.
           :param data (byte[]): 16 bit (2 byte) data word.
           :param windex (int): word memory index (zero based).

           :return (boolean): True, if successful, otherwise False.
        """
        self.lib.WriteMemWord.argtypes = [c_char_p, c_char, c_char_p, c_char]
        self.lib.WriteMemWord.retypes = [c_char]
        ret = self.lib.WriteMemWord(epc, len(epc), data, windex)
        return bool(ret)

    # byte ReadMemWord(byte[] epc, byte epclen, byte[] data, byte windex);
    def ReadMemWord(self, epc, windex):
        """
           Reads 16 bit (2 byte) data simultaneously from a word address.

           :param epc (byte[]): A valid EPC of the tag.
           :param windex (int): valid word memory index (zero based).

           :return (byte[]): 2 byte of memory data.
        """
        self.lib.ReadMemWord.argtypes = [c_char_p, c_char, c_char_p, c_char]
        self.lib.ReadMemWord.retypes = [c_char]
        data = create_string_buffer(2)
        ret = self.lib.ReadMemWord(epc, len(epc), data, windex)
        if (ret == 0):
            return None
        return data[0:2]

    # byte SetFilter(int maskAdrInByte, int maskLenInByte, byte[] maskDataByte);
    def SetFilter(self, adr, len, mask):
        """
           Creates a filter which can be used during inventory, if desired.

           :param adr (int): 16 bit (2 byte) word address.
           :param len (int): length of the mask (must be even), i.e., 2 bytes at a time.
           :param mask (byte[]): actual bytes used as mask.

           :return (boolean): True, if successful, otherwise False.
        """
        self.lib.SetFilter.argtypes = [c_int, c_int, c_char_p]
        self.lib.SetFilter.retypes = [c_char]
        ret = self.lib.SetFilter(adr, len, mask)
        return bool(ret)
	
    # byte TagExists(byte[] epc, byte epclen);
    def TagExists(self, epc):
        """
           Checks if a tag with the given EPC exist.

           :param epc (bytep[]): full EPC value.
           :return (boolean): True, if found, otherwise False.
        """
        self.lib.TagExists.argtypes = [c_char_p]
        self.lib.TagExists.retypes = [c_char]
        ret = self.lib.TagExists(epc, len(epc))
        return bool(ret)

    # byte WriteEpcWord(byte[] epc, byte epclen, byte[] data, byte windex);
    def WriteEpcWord(self, epc, data, windex):
        """
           Writes EPC Word at specified word index in a 16-bit memory.

           :param epc (bytep[]): full EPC value.
           :param data (byte[]): New EPC word (2 byte).
           :param windoex (int): Word memory index, must be even.

           :return (boolean): True, if successful, otherwise False.
        """
        self.lib.WriteEpcWord.argtypes = [c_char_p, c_char, c_char_p, c_char]
        self.lib.WriteEpcWord.retypes = [c_char]
        ret = self.lib.WriteEpcWord(epc, len(epc), data, windex)
        return bool(ret)
	
    # byte WriteEpc(byte[] epc, byte epclen, byte[] data);
    def WriteEpc(self, epc, newepc):
        """
           Writes EPC for the Tag.

           :param epc (bytep[]): full EPC value.
           :param newepc (byte[]): New EPC value should be 12 byte or less. If number of bytes
              is less than 12 bytes, zeros will be padded. EPC will be right justified. That means
              if you put 0xAB12 as EPC, 0x0000 0000 0000 0000 0000 AB12 will be written.

           :return (boolean): True, if successful, otherwise False.
        """
        self.lib.WriteEpc.argtypes = [c_char_p, c_char, c_char_p]
        self.lib.WriteEpc.retypes = [c_char]
        ret = self.lib.WriteEpc(epc, len(epc), newepc)
        return bool(ret)
	
    # byte GetTagInfo(byte[] tid, byte[] info);
    def GetTagInfo(self, tid):
        """
           Gets the detail information about the tag and returns the TagInfo
           object.

           :param tid (byte[]): The full TID of the tag.
           :return (TagInfo): The TagInfo object.
        """
        self.lib.GetTagInfo.argtypes = [c_char_p, c_char_p]
        self.lib.GetTagInfo.retypes = [c_char]
        buf = create_string_buffer(128)
        ret = self.lib.GetTagInfo(tid, buf)
        # print(buf)
        if (ret == 0):
            return None
        taginfo = TagInfo(buf)
        return taginfo
	
    # byte GetGPI(byte gpino);
    # For GPI-0 use bit-0, for GPI-1 use bit-1
    # Example: GetGPI(1), gets input from GPI-0
    # GetGPI(2), gets input from GPI-1
    # All other values are invalid.
    def GetGPI(self, gpino):
        """
           Gets the GPIO Input from GPI0 or GPI1 port. The argument should be either,
           1, to read GPI0 or 2 to read GPI1 or 3 to read both GPI0 and GPI1.

           :param gpino(int): 1 -> GPI0, 2 -> GPI1, 3 -> GPI0 & GPI1
           :return (int): The state of the GPI inputs relative to the bit position.
        """
        self.lib.GetGPI.argtypes = [c_char]
        self.lib.GetGPI.retypes = [c_char]
        ret = self.lib.GetGPI(gpino)
        return ret

    # byte SetGPO(byte gpono);
    # For GPO-0 use bit-0, for GPO-1 use bit-1
    # therefore, this function affects both the 
    # the output ports.
    # Example: SetGPO(0) sets both GPO-0 and GPO-1 to 0
    # SetGPO(0x01) sets GPO-0 to 1 and GPO-1 to 0
    # SetGPO(0x02) sets GPO-0 to 0 and GPO-1 to 1
    # SetGPO(0x03) sets both GPO-0 and GPO-1 to 1
    def SetGPO(self, gpono):
        """
           Sets the GPIO Output of GPO0 or GPO1 port. Bit 0 is GPO0, bit 1 is GPO1.

           :param gpono(int): The state of bit 0 will be written to GPO0 and state of
           bit 1 will be written to GPO1.
           :return (int): The state of the GPO outputs after writing relative to bit position.
        """
        self.lib.SetGPO.argtypes = [c_char]
        self.lib.SetGPO.retypes = [c_char]
        ret = self.lib.SetGPO(gpono)
        return ret

    # void LibVersion(byte[] version);
    def LibVersion(self):
        """
           Gets the library version as string.

           :return (string): Library Version as string in major.minor format.
        """
        self.lib.LibVersion.argtypes = [c_char_p]
        self.lib.LibVersion.retypes = [c_char]
        arg = create_string_buffer(2)
        ret = self.lib.LibVersion(arg)
        if (ret == 0):
            return ""
        major = str(bytes(arg[0])[0])
        minor = str(bytes(arg[1])[0])
        ver = major + "." + minor
        #print(ver)
        return ver
	
    # byte SetQ(byte q);
    def SetQ1(self, q):
        """
           Obsolete
        """
        self.lib.SetQ.argtypes = [c_char]
        self.lib.SetQ.retypes = [c_char]
        ret = self.lib.SetQ(q)
        return bool(ret)

    def SetQ(self, q):
        """
           Sets the Q value of the reader. 

           :param q (int): A value 0 - 15 as Q value. If there is only 1 tag, use Q = 1.
           If there are a large number of tags, use Q = 15. Usually Q should be in the range
           of 4 - 5 for optimal reading when the number of tags is less than 16.

           :return (boolean): True, if successful, False otherwise.
        """
        self.lib.SetQ.argtypes = [c_char]
        self.lib.SetQ.retypes = [c_char]
        ret = self.lib.SetQ(q)
        return bool(ret)

    # byte SetSession(byte session);
    def SetSession(self, session):
        """
           Sets reader session. In a multi-reader environment, set this value such that each
           reader has different session. This will prevent interference of other readers when 
           reading the same tag. Sessions values are from 0 to 3. So, there should be at most 
           4 readers that can read the same tag simultaneously. 

           :param session (int): Session value 0 to 3.

           :return (boolean): True, if successful, False otherwise.
        """
        self.lib.SetSession.argtypes = [c_char]
        self.lib.SetSession.retypes = [c_char]
        ret = self.lib.SetSession(session)
        return bool(ret)


