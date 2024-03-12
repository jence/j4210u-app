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
# Version: 1.1
#
# ABOUT THIS EXAMPLE:
# This single example covers all the methods offered by the library.
# It initially scans the serial port for any device and connects to the first
# such serial port then operates on that device.
# 
# The use may modify this program in their own code. For a graphycal demo,
# see another python code UhfApp.py
#

import j421xlib
import time

def Test():
    # load the library
    f = j421xlib.J4210()

    """
    # test the Hex2Bytes()
    hex = "feeddeaf"
    bb = f.Hex2Bytes(hex)
    hex2 = f.Bytes2Hex(bb)
    print('HEX: ', hex)
    print()
    """

    # object method calling
    ports = f.AvailablePorts()
    print("Available Serial Ports:")
    for i, port in enumerate(ports , start=1):
        print(f"{i}. {port}")
    portNum = input("Enter The number of port you want to connect: ")

    # connect to device
    ret = f.OpenPort(ports[int(portNum)-1], 57600)
    print('Last Error: ', f.LastError())
    assert ret != False

    # get driver version
    ver = f.LibVersion()
    print("Lib Version: ", ver)

    # get settings
    print("Loading Current Settings:")
    ri = f.LoadSettings()
    ri.echo()
    power = 26
    assert ri != None

    # change power
    print("Saving modified settings:")
    ri.Power = 20
    ret = f.SaveSettings(ri)
    assert ret != False

    # get settings to check if the value has changed
    print("Loading modified settings:")
    ri = f.LoadSettings()
    ri.echo()
    assert ri.Power == 20

    # return to original setting
    print("Returning to original settings:")
    ri.Power = power
    ri.echo()
    ret = f.SaveSettings(ri)
    assert ret != False

    # inventory
    # Single tag inventory. If only interested one tag, call InventoryOne
    n1 = f.InventoryOne()
    # n1 should be 1 if a tag found, 0 if not foud
    # list inventory
    print("One Tag List:")
    for i in range(n1):
        sr = f.GetResult(i)
        #sr.echo()
        sr.line()
    
    print("Performing Bulk Inventory:")
    q = 5
    ret = f.SetQ(q) # Q is 0 to 15
    assert ret == False
    print("Q = ",q)
    sess = 0
    ret = f.SetSession(sess)
    assert ret == False
    print("Session = ", sess) # session is 0 to 3
    n = f.Inventory(False) # inventory witout filtering
    print("Tags found: ", n)
    assert n>0, "No Tags found nearby. Aborting TEST!"

    # list inventory
    print("Tag List:")
    for i in range(n):
        sr = f.GetResult(i)
        #sr.echo()
        sr.line()

    # get TID
    if (n > 0):
        sr = f.GetResult(0) # get TID of first tag
        # check if the tag exist
        found = f.TagExists(sr.EPC)
        taginfo = None
        if (found):
            print("Tag FOUND!")
            time.sleep(1)
            tid = f.GetTID(sr.EPC)
            print("TID: ", f.Bytes2Hex(tid), " EPC: ", f.Bytes2Hex(sr.EPC))

            # get details of the Tag
            print("Getting Tag Info for this tag:")
            taginfo = f.GetTagInfo(tid)
            taginfo.echo()
        else:
            print("Tag with EPC ", f.Bytes2Hex(sr.EPC), " not found.")

        # we will set the password now
        # if you know the tags password, set it here
        # this is the default password (size 4 byte)
        password = b'\x00\x00\x00\x00' 
        ret = f.Auth(password)
        print(f.LastError())
        if(ret == True):
            print("Password has been set to default...")
            print("Stopping the example here so that EPC MEMORY doesn't change ")
            print("Uncomment the return if you want to run the full example")
            return
        else:
            print("Write failed due to signal strength")
            return

        # we will change the password
        # set a new password here. We used the default password
        # to keep the password unchange and showing you how to 
        # change the password, if you need to.
        # NOTE: You must first set the old password using the Auto
        # method. If you do not call it, the default password will
        # automatically be used.
        newpass =  b'\x00\x00\x00\x00'
        #ret = f.SetPassword(sr.EPC, newpass)
        #assert ret == True

        if (taginfo.userlen > 0):
            # write something to user memory
            data = b'\xFE\xED'
            print("Data to be written: ", f.Bytes2Hex(data))
            ret = f.WriteMemWord(sr.EPC, data, 0)
            assert ret == True

            # now read it back
            data2 = f.ReadMemWord(sr.EPC, 0)
            assert data2 != None
            print("Data Read: ",f.Bytes2Hex(data2))
            assert data == data2

        else:
            # this tag does not have user memory
            print("This tag does not have user memory")
        
        # change EPC word
        # EPC can be changed in two ways:
        # 1. Changing the entire EPC at once
        # 2. Changing EPC two bytes at a time
        # 
        # 1. EPC 1st method: Changing EPC with a single write
        # Use this method, if you are writing an entirely new EPC.
        print("Changing EPC:")
        print("Current EPC = ", f.Bytes2Hex(sr.EPC))
        # Example EPC (hex) = 5678 BEED DEAD 1234 CAFA FEED
        newepc = b'\x56\x78\xbe\xed\xde\xad\x12\x34\xca\44\xbe\xef'
        f.WriteEpc(sr.EPC, newepc)
        # Check if the old epc exist
        found = f.TagExists(sr.EPC)
        assert found == False   # old tag should not exist that means epc hasn't changed or trying to run the example twice with same tag

        found = f.TagExists(newepc)
        if (found) : # check the new tag
            print("[CHANGED] Tag FOUND with EPC ", f.Bytes2Hex(newepc))
            sr.EPC = newepc
        else :
            print("[FAILED TO CHANGE EPC]")
        return
        
        # 2. EPC 2nd method: change 16-bit (2 bytes) at a time.
        # EPC is usually 12 byte, so to change the EPC
        # you need to write 6 times where the index will
        # be supplied from 0 to 5
        print("Current EPC = ", f.Bytes2Hex(sr.EPC))
        newepc0 = b'\xba\xba'
        print("Changing the EPC first two bytes to ", f.Bytes2Hex(newepc0))

        # our first index is 0
        ret = f.WriteEpcWord(sr.EPC, newepc0, 0)
        assert ret == True
        # because our EPC has changed, we need to modify the EPC as well
        epc = newepc0 + sr.EPC[2:]
        print("The new EPC is now ", f.Bytes2Hex(epc))
        newepc1 = b'\xda\xda'
        print("Changing the EPC second two bytes to ", f.Bytes2Hex(newepc1))
        ret = f.WriteEpcWord(epc, newepc1, 1)
        assert ret == True
        # repeat this for index 2 through 5 to change the whole EPC
        epc = epc[0:2] + newepc1 + sr.EPC[4:]
        print("Thew new EPC is now ", f.Bytes2Hex(epc))

        # we will now use filter to find out how many tag starts with babadada
        adr = 0 # mask addres is in byte but on word (2 byte) boundry
        masklen = 4 # mask length is in byte
        mask = b'\xba\xba\xda\xda'
        ret = f.SetFilter(adr, masklen, mask)
        assert ret == True
        # now perform inventory using the filter
        n = f.Inventory(True) # passing True tells to use the filter
        assert n > 0, "Inventory count returned ZERO!"
        # list inventory
        print("Tag List:")
        for i in range(n):
            sr = f.GetResult(i)
            #sr.echo()
            sr.line()

    # Here GPIO tests are done. If you have the hardware with GPIO
    # you can run this test to turn on LEDs at the GPIO ports.
    print("Setting GPO-0 and GPO-1 to 1")
    f.SetGPO(3)
    print("GPI-0 = ", f.GetGPI(1))
    print("GPI-1 = ", f.GetGPI(2))


    # close connection
    f.ClosePort()

    print("DONE!")

Test()
