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
# https://www.soalib.com

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
        UNKNOWN = 0

        # ALIEN Family
        HIGGS_3 =  1
        HIGGS_4 =  2
        HIGGS_EC = 3
        HIGGS_9 =  4

        # IMPINJ Family
        MONZA_4QT =   5
        MONZA_4E =    6
        MONZA_4D =    7
        MONZA_4I =    8
        MONZA_5 =     9
        MONZA_R6 =    10
        MONZA_R6P =   11
        MONZA_X2K =   12
        MONZA_X8K =   13
        IMPINJ_M730 = 14
        IMPINJ_M750 = 15

        # NXP Family
        UCODE_7 =         16
        UCODE_8 =         17
        UCODE_8M =        18
        UCODE_DNA =       19
        UCODE_DNA_CITY =  20
        UCODE_DNA_TRACK = 21

        # EM Family
        EM4423 = 22
		
class TagInfo():
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

    def echo(self):
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

    def __init__(self, bb = None):
        if (bb == None):
            self.Ant = 0
            self.RSSI = 0
            self.Count = 0
            self.EpcLength = 0
            self.EPC = bytes(12)
            return

        self.Ant = bb[0]
        self.RSSI = bb[1]
        self.Count = bb[2]
        self.EpcLength = bb[3]
        self.EPC = bb[4:16]
        return

    def echo(self):
        print("ScanResult{")
        print("\tAnt: ", bytes(self.Ant)[0])
        print("\tRSSI: ", bytes(self.RSSI)[0])
        print("\tCount: ", bytes(self.Count)[0])
        print("\tEpcLength: ", bytes(self.EpcLength)[0])
        h = binascii.hexlify(self.EPC).decode('utf-8')
        print("\tEPC: ", h)
        print("}")
        return

    def line(self):
        h = binascii.hexlify(self.EPC).decode('utf-8')
        print("Ant: ", bytes(self.Ant)[0],\
        " | RSSI: %02d" % (bytes(self.RSSI)[0]),\
        " | EpcLength: %02d" % bytes(self.EpcLength)[0],\
        " | Count: % 3d" % (bytes(self.Count)[0]),\
        " | EPC: ", h)

class ReaderInfo():

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
        print("ReaderInfo{")
        print('\tSerial:     ', self.Serial)
        print('\tVersion:    ', self.Version)
        print('\tAntenna:    ', self.Antenna)
        print('\tComAdr:     ', self.ComAdr)
        print('\tReaderType: ', self.ReaderType)
        print('\tProtocol:   ', self.Protocol)
        print('\tBand:       ', self.Band)
        print('\tPower:      ', self.Power)
        print('\tScanTime:   ', self.ScanTime)
        print('\tBeepOn:     ', bool(self.BeepOn))
        print('\tMaxFreq:    ', self.MaxFreq)
        print('\tMinFreq:    ', self.MinFreq)
        print('\tBaudRate:   ', self.BaudRate)
        print("}")
    
    def tobytes(self):
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
    
    # constructor
    def __init__(self):
        # load the library
        # os.chdir('D:\Work\workspace\j4210u-app\platform\win64')
        # REM: All the drivers should be in path or in currect dir
        self.lib = None
        if (platform.system() == 'Windows'):
            self.lib = cdll.LoadLibrary('j4210u.dll')
        elif (platform.system() == 'Linux'):
            self.lib = cdll.LoadLibrary('libj4210u.so')
        elif (platform.system() == 'Mac OS X'):
            self.lib = cdll.LoadLibrary('libj4210u.dylib')
        lib = self.lib
        return
    
    # short AvailablePorts(byte[] ports);
    def AvailablePorts(self):
        self.lib.AvailablePorts.argtypes = [c_char_p]
        self.lib.AvailablePorts.retypes = [c_uint]
        arg = create_string_buffer(2048)
        n = self.lib.AvailablePorts(arg)
        str = arg.value
        str = str.decode("utf-8")
        ret = str.split('\n',n)

        #print(ret)
        #print(n)
        del arg
        return ret
    
    # byte OpenComPort(byte[] port, int baud);
    def OpenPort(self, port, baud):
        self.lib.AvailablePorts.argtypes = [c_char_p, c_uint]
        self.lib.AvailablePorts.retypes = [c_char]
        arg = bytes(port,"ascii")
        print('arg=', arg)
        ret = self.lib.OpenComPort(arg, baud)
        del arg
        return bool(ret)

    # void CloseComPort();
    def ClosePort(self):
        self.lib.CloseComPort()
        return

    # byte LoadSettings(byte[] buff);
    def LoadSettings(self):
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
        self.lib.SaveSettings.argtypes = [c_char_p]
        self.lib.SaveSettings.retypes = [c_char]
        buf = ri.tobytes()
        ret = self.lib.SaveSettings(buf)
        return bool(ret)

    # int  Inventory(byte filter);	
    def Inventory(self, filter):
        self.lib.Inventory.argtypes = [c_char]
        self.lib.Inventory.retypes = [c_uint]
        ff = 0
        if (filter == True):
            ff = 1
        ret = self.lib.Inventory(ff)
        return ret

    # byte GetResult(byte[] scanresult, int index);
    def GetResult(self, index = 0):
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
        h = binascii.hexlify(bb).decode('utf-8')
        return h

    # byte GetTID(byte[] epc, byte epclen, byte[] tid, byte[] tidlen);
    def GetTID(self, epc):
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
        self.lib.SetPassword.argtypes = [c_char_p, c_char, c_char_p, c_char]
        self.lib.SetPassword.retypes = [c_char]
        ret = self.lib.SetPassword(epc,len(epc),password,len(password))
        return bool(ret)

    # byte SetKillPassword(byte[] epc, byte epcLen, byte[] pass, byte size);
    def SetKillPassword(self, epc, password):
        self.lib.SetKillPassword.argtypes = [c_char_p, c_char, c_char_p, c_char]
        self.lib.SetKillPassword.retypes = [c_char]
        ret = self.lib.SetKillPassword(epc,len(epc),password,len(password))
        return bool(ret)

    # void LastError(byte[] buffer);
    def LastError(self):
        self.lib.LastError.argtypes = [c_char_p]
        buf = create_string_buffer(128)
        self.lib.LastError(buf);
        str = buf.value.decode("utf-8")
        return str
	
    # byte Auth(byte[] password, byte size);
    def Auth(self, password):
        self.lib.Auth.argtypes = [c_char_p]
        self.lib.Auth.retypes = [c_char]
        ret = self.lib.Auth(password, len(password))
        return bool(ret)

    # byte WriteMemWord(byte[] epc, byte epclen, byte[] data, byte windex);
    def WriteMemWord(self, epc, data, windex):
        self.lib.WriteMemWord.argtypes = [c_char_p, c_char, c_char_p, c_char]
        self.lib.WriteMemWord.retypes = [c_char]
        ret = self.lib.WriteMemWord(epc, len(epc), data, windex)
        return bool(ret)

    # byte ReadMemWord(byte[] epc, byte epclen, byte[] data, byte windex);
    def ReadMemWord(self, epc, windex):
        self.lib.ReadMemWord.argtypes = [c_char_p, c_char, c_char_p, c_char]
        self.lib.ReadMemWord.retypes = [c_char]
        data = create_string_buffer(2)
        ret = self.lib.ReadMemWord(epc, len(epc), data, windex)
        if (ret == 0):
            return None
        return data[0:2]

    # byte SetFilter(int maskAdrInByte, int maskLenInByte, byte[] maskDataByte);
    def SetFilter(self, adr, len, mask):
        self.lib.SetFilter.argtypes = [c_int, c_int, c_char_p]
        self.lib.SetFilter.retypes = [c_char]
        ret = self.lib.SetFilter(adr, len, mask)
        return bool(ret)
	
    # byte TagExists(byte[] epc, byte epclen);
    def TagExists(self, epc):
        self.lib.TagExists.argtypes = [c_char_p]
        self.lib.TagExists.retypes = [c_char]
        ret = self.lib.TagExists(epc, len(epc))
        return bool(ret)

    # byte WriteEpcWord(byte[] epc, byte epclen, byte[] data, byte windex);
    def WriteEpcWord(self, epc, data, windex):
        self.lib.WriteEpcWord.argtypes = [c_char_p, c_char, c_char_p, c_char]
        self.lib.WriteEpcWord.retypes = [c_char]
        ret = self.lib.WriteEpcWord(epc, len(epc), data, windex)
        return bool(ret)
	
    # byte GetTagInfo(byte[] tid, byte[] info);
    def GetTagInfo(self, tid):
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
        self.lib.SetGPO.argtypes = [c_char]
        self.lib.SetGPO.retypes = [c_char]
        ret = self.lib.SetGPO(gpono)
        return ret

    # void LibVersion(byte[] version);
    def LibVersion(self):
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
        self.lib.SetQ.argtypes = [c_char]
        self.lib.SetQ.retypes = [c_char]
        ret = self.lib.SetQ(q)
        return bool(ret)

    def SetQ(self, q):
        self.lib.SetQ.argtypes = [c_char]
        self.lib.SetQ.retypes = [c_char]
        ret = self.lib.SetQ(q)
        return bool(ret)

    # byte SetSession(byte session);
    def SetSession(self, session):
        self.lib.SetSession.argtypes = [c_char]
        self.lib.SetSession.retypes = [c_char]
        ret = self.lib.SetSession(session)
        return bool(ret)


