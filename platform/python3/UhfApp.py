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


import j421xlib
import tkinter as tk
import webbrowser
from tkinter import *
from tkinter import ttk 
from tkinter import messagebox
from tkinter.ttk import *

class UhfApp():
    VERSION = "1.0"

    # Constructor
    def __init__(self):
        # load the library
        self.lib = j421xlib.J4210()

    def showHelp(self):
        webbrowser.open("https://soalib.com", new = 0, autoraise = True)

    def printStatus(self, text):
        self.lblStatus['text'] = text
        return

    def connect(self):
        port = self.cmbPorts.get()
        if (port == ""):
            messagebox.showinfo("No Port","No port selected.")
            return
        baudrate = self.cmbBaud.get()
        ret = int(self.lib.OpenPort(port,baudrate))
        if (ret == False):
            messagebox.showinfo("ERROR","Failed to connect.")
            return
        self.btnConnect["state"] = "disabled"
        self.btnDisconnect["state"] = "normal"
        self.btnScan["state"] = "normal"
        self.loadSettings()
        self.printStatus("Connected to Device")
        self.lblVer['text'] = "Lib Version: " + self.lib.LibVersion() + " | App Version: " + UhfApp.VERSION + " "
        return
    
    def loadSettings(self):
        ri = self.lib.LoadSettings()
        ri.echo()
        self.lblSerialNo['text'] = ri.Serial
        ver = ri.Version + ', Reader Type: ' + str(ri.ReaderType)
        self.lblVersion['text'] = ver
        self.lblAntenna['text'] = ri.Antenna
        self.lblProtocol['text'] = ri.Protocol
        self.lblFrequencyMin['text'] = ri.MinFreq
        self.lblFrequencyMax['text'] = ri.MaxFreq
        bands = {"U":"USA", "C":"CHINA","E":"EU","K":"KOREA"}
        self.cmbBand.set(bands[chr(ri.Band)])
        self.cmbBaudRate.set(str(ri.BaudRate))

        self.varPower.set(ri.Power)
        self.varScantime.set(ri.ScanTime*100)

        self.varBeepOn = ri.BeepOn
        return

    def saveSettings(self):
        # everything must be in byte(s)
        ri = self.lib.LoadSettings()
        ri.Band = bytes(self.cmbBand.get(), "utf-8")[0]
        ri.Power = int(self.varPower.get())
        ri.ScanTime = round(int(self.varScantime.get())/100)
        ri.BeepOn = self.varBeepOn
        ri.BaudRate = int(self.cmbBaudRate.get())
        ri.echo()
        ret = self.lib.SaveSettings(ri)
        if (ret == False):
            messagebox.showinfo("Error!","Failed to save settings.");
        # refresh settings
        self.loadSettings()
        return

    def disconnect(self):
        self.lib.ClosePort()
        self.btnConnect["state"] = "normal"
        self.btnDisconnect["state"] = "disabled"
        self.btnScan["state"] = "disabled"
        return

    def scan(self):
        n = self.lib.Inventory(False)
        print("Tags found: ", n)

        # list inventory
        self.tblInventory.delete(*self.tblInventory.get_children())
        self.scanResult = []

        color = 'even'
        if (n>0):
            self.tabFolder.select(self.tabInventory)
            print("Tag List:")
            for i in range(n):
                sr = self.lib.GetResult(i)
                #sr.echo()
                if (i % 2 == 0):
                    color = 'even'
                else:
                    color = 'odd'
                self.tblInventory.insert(parent='',tags=(color,),index='end',iid=i,text=str(i),values=(sr.hexEPC(),sr.EpcLength,sr.Ant,sr.Count,sr.RSSI))
                self.scanResult.append(sr)
                sr.line()
        return

    def refresh(self):
        print("Refresh")
        ports = self.lib.AvailablePorts()
        print("Available Serial Ports:")
        print(ports)
        self.cmbPorts['values'] = ports
        if (len(ports) > 0):
            self.cmbPorts.set(ports[0])
        return

    def readMemory(self, event):
        column = self.tblInventory.identify_column(event.x)
        rowidx = self.tblInventory.identify_row(event.y)
        #print(rowidx)
        iid = self.tblInventory.focus()
        colidx = int(column[1:]) - 1
        values = self.tblInventory.item(iid)
        epc = values.get("values")[1]
        print("EPC : "+str(epc))
        # insert epc row
        sr = self.scanResult[int(rowidx)]
        sr.line()

        self.tabFolder.select(self.tabMemory)

        self.tblMemory.delete(*self.tblMemory.get_children())
        # split the epc bytes into separate words so we can display it
        epcwords = []
        for i in range(int(sr.EpcLength/2)):
            j = i * 2
            hex = self.lib.Bytes2Hex(sr.EPC[j:j+2]).upper()
            #print("EPC : "+hex)
            epcwords.append(hex)

        print(epcwords)
        self.tblMemory.insert(parent='',tags=('eid',),text="EPC",index='end',iid=0,values=epcwords)

        tid = self.lib.GetTID(sr.EPC)
        if (tid == None):
            messagebox.showinfo("ERROR!","Failed to read TID.")
            return

        print("TID : "+self.lib.Bytes2Hex(tid))

        # get tag info
        taginfo = self.lib.GetTagInfo(tid)
        taginfo.echo()
        totmem = taginfo.epclen+taginfo.pwdlen+taginfo.userlen+taginfo.tidlen+8
        self.varChipType.set(taginfo.getChipName())
        self.varMemTotalByte.set(str(totmem))
        self.varMemTotalBit.set(str(totmem * 8))
        self.varEPCLenByte.set(str(taginfo.epclen))
        self.varEPCLenBit.set(str(taginfo.epclen * 8))
        self.varPwdLenByte.set(str(taginfo.pwdlen))
        self.varPwdLenBit.set(str(taginfo.pwdlen * 8))
        self.varUserMemByte.set(str(taginfo.userlen))
        self.varUserMemBit.set(str(taginfo.userlen * 8))
        self.varTIDSizeByte.set(str(taginfo.tidlen))
        self.varTIDSizeBit.set(str(taginfo.tidlen * 8))

        # split the tid bytes into separate words so we can display it
        tidwords = []
        for i in range(int(len(tid)/2)):
            j = i * 2
            hex = self.lib.Bytes2Hex(tid[j:j+2]).upper()
            #print("TID : "+hex)
            tidwords.append(hex)
        # insert tid row
        self.tblMemory.insert(parent='',tags=('tid',),text="TID",index='end',iid=1,values=tidwords)

        print(tidwords)
        
        if (taginfo.userlen > 0):
            # This tag has User Memory, get and display memory content
            memwords = []
            for i in range(int(taginfo.userlen/2)):
                retries = 2
                memdat = ""
                for j in range(retries):
                    memdat = self.lib.ReadMemWord(sr.EPC, i)
                    if memdat != None:
                        break
                    print("Retry "+str(j+1)+": Failed to Read Memory Word at "+str(i))
                if (memdat == None):
                    print("Skiping! Fetching next location.")
                    continue
                hex = self.lib.Bytes2Hex(memdat).upper()
                print("Data Read: ",hex)
                memwords.append(hex)
                # put this data in memory table in batch of 8 words
                if (len(memwords) == 8):
                    rowtext = "USER["+str(i-7)+".."+str(i)+"]"
                    self.tblMemory.insert(parent='',tags=('user',),text=rowtext,index='end',iid=1+i,values=memwords)
                    memwords = []
            # write incomplete rows
            if (len(memwords) > 0):
                self.tblMemory.insert(parent='',tags=('user',),text="TID",index='end',iid=1+i,values=memwords)

    def editMemory(self, event):
        print("Double Clicked.")
        cell = self.identify_region(event.x, event.y)
        if (cell not in ("tree","cell")):
            return
        column = self.identify_column(event.x)
        print(column)
        row = self.identify_row(event.y)
        print(row)
        iid = self.focus()
        print(iid)
        return
      
        #value = self.item(iid)
        #print(value)
        #if (column == "#0")
        #    selected = iid("text")
        #else
        #    selected = iid("values")
        #print(selected)
        #box = self.bbox(iid, column)
        #edit = ttk.Entry(self.tblInventory, box[2])
        #edit.place(x=box[0],y=box[1],w=box[2],h=box[3])

    def show(self):
        # [Help] [Ports] [Refresh] [Baud] [Connect] [Disconnect] [Scan] [Scan Continuous] [Scan on Button] 
        shell = tk.Tk()
        shell.geometry("1024x600")
        s = ttk.Style()
        s.theme_use('clam')
        s.configure("Treeview", foreground="black", background="white", fieldbackground="white")
        s.map("Treeview", background=[('selected',"gray")])

        frmToolbar = Frame(shell);
        # Help button
        imgHelp = PhotoImage(file="icon/help.png")
        self.btnHelp = tk.Button(frmToolbar, text="Help", image=imgHelp, compound=LEFT, command=lambda: UhfApp.showHelp(self))
        self.btnHelp.pack(side=LEFT, padx=5)

        # Ports Combo box
        self.cmbPorts = ttk.Combobox(frmToolbar, state="readonly")
        self.cmbPorts.pack(side=LEFT, padx=5)

        # Connect button
        imgRefresh1 = PhotoImage(file="icon/usb.png")
        btnRefresh = tk.Button(frmToolbar, text="Refresh", image=imgRefresh1, compound=LEFT, command=lambda: UhfApp.refresh(self))
        btnRefresh.pack(side = LEFT, padx=5)

        # Baudrate Combo box
        baudRates = ["57600", "115200" ]
        self.cmbBaud = ttk.Combobox(frmToolbar, values=baudRates, state="readonly")
        self.cmbBaud.set(baudRates[0])
        self.cmbBaud.pack(side = LEFT, padx=5)

        # Connect button
        imgConnect = PhotoImage(file="icon/connect.png")
        self.btnConnect = tk.Button(frmToolbar, text="Connect", image=imgConnect, compound=LEFT, command=lambda: UhfApp.connect(self))
        self.btnConnect.pack(side = LEFT, padx=5)

        # Disconnect button
        imgDisconnect = PhotoImage(file="icon/disconnect.png")
        self.btnDisconnect = tk.Button(frmToolbar, text="Disconnect", image=imgDisconnect, compound=LEFT, command=lambda: UhfApp.disconnect(self), state="disabled")
        self.btnDisconnect.pack(side = LEFT, padx=5)

        # Scan button
        imgScan = PhotoImage(file="icon/scan.png")
        self.btnScan = tk.Button(frmToolbar, text="Scan", image=imgScan, compound=LEFT, command=lambda: UhfApp.scan(self), state="disabled")
        self.btnScan.pack(side = LEFT, padx=5)

        frmToolbar.pack(anchor="w", padx=5, pady=5)

        self.txtSupportedChips = Text(shell, height = 2)
        self.txtSupportedChips.pack(anchor = "w", fill=X)
        self.txtSupportedChips.insert(END, self.lib.getSupportedChips())
        self.txtSupportedChips.config(state=DISABLED)

        # Tab Folder with 4 tabs
        self.tabFolder = ttk.Notebook(shell)
        self.tabFolder.pack(fill=BOTH, expand=True, padx=5, pady=5)

        self.tabInventory = ttk.Frame(self.tabFolder)
        self.tabMemory = ttk.Frame(self.tabFolder)
        tabInfo = ttk.Frame(self.tabFolder)
        #tabMessaging = ttk.Frame(self.tabFolder)
        #tabGPIO = ttk.Frame(self.tabFolder)
        self.tabFolder.add(self.tabInventory, text="INVENTORY")
        self.tabFolder.add(self.tabMemory, text="MEMORY")
        self.tabFolder.add(tabInfo, text="INFO")
        #self.tabFolder.add(tabGPIO, text="GPIO")
        #self.tabFolder.add(tabMessaging, text="Messaging")

        # Create Inventory Tab content
        tblFrame = Frame(self.tabInventory)
        tblFrame.pack(fill=BOTH, expand=True)

        scrollbar = ttk.Scrollbar(tblFrame)
        scrollbar.pack(side=RIGHT, fill=Y)
        self.tblInventory = ttk.Treeview(tblFrame, yscrollcommand=scrollbar)
        scrollbar.config(command=self.tblInventory.yview)

        self.tblInventory.tag_configure("even",background="white")
        self.tblInventory.tag_configure('odd',background="lightgreen")
        self.tblInventory['columns'] = ('EPC', "Len", "Ant", "Times", "RSSI")

        self.tblInventory.column("#0",anchor="w", stretch=NO, width=50, minwidth=10)
        self.tblInventory.heading("#0", text="Index")
        self.tblInventory.column("EPC",anchor="w", stretch=NO, width=200)
        self.tblInventory.heading("EPC", text="EPC")
        self.tblInventory.column("Len",anchor="w", stretch=NO, width=50)
        self.tblInventory.heading("Len", text="Len")
        self.tblInventory.column("Ant",anchor="w", stretch=NO, width=50)
        self.tblInventory.heading("Ant", text="Ant")
        self.tblInventory.column("Times",anchor="w", stretch=NO, width=50)
        self.tblInventory.heading("Times", text="Times")
        self.tblInventory.column("RSSI",anchor="w", stretch=NO, width=80)
        self.tblInventory.heading("RSSI", text="RSSI")

        self.tblInventory.bind("<Double-1>",self.readMemory)
        self.tblInventory.pack(fill=BOTH, expand=True)

        # [Create Info Tab Content]-------------------------------------------
        frmInfo = Frame(tabInfo)
        frmInfo.pack()

        # 1. [Serial]     [] [Ver]      []
        # 2. [Ant]        [] [Protocol] []
        # 3. [Freq Min]   [] [Freq Max] []
        # 4. [Band]       [] [Baudrate] []
        # 5. [Power]      [] [Scantime] []
        # 6. BeepOn

        # Row 1
        lblSerialInfo = Label(frmInfo, text="Device Info:")
        lblSerialInfo.grid(row=1,column=1, padx=5, pady=5)

        self.lblSerialNo = Label(frmInfo)
        self.lblSerialNo.grid(row=1,column=2, padx=5, pady=5)

        lblVerInfo = Label(frmInfo, text="Version:")
        lblVerInfo.grid(row=1,column=3, padx=5, pady=5)

        self.lblVersion = Label(frmInfo)
        self.lblVersion.grid(row=1,column=4, padx=5, pady=5)

        # Row 2
        lblAntInfo = Label(frmInfo, text="Antenna:")
        lblAntInfo.grid(row=2,column=1, padx=5, pady=5)

        self.lblAntenna = Label(frmInfo)
        self.lblAntenna.grid(row=2,column=2, padx=5, pady=5)

        lblProtoInfo = Label(frmInfo, text="Protocol:")
        lblProtoInfo.grid(row=2,column=3, padx=5, pady=5)

        self.lblProtocol = Label(frmInfo)
        self.lblProtocol.grid(row=2,column=4, padx=5, pady=5)

        # Row 3
        lblMinFreqInfo = Label(frmInfo, text="Frequency (Min):")
        lblMinFreqInfo.grid(row=3,column=1, padx=5, pady=5)

        self.lblFrequencyMin = Label(frmInfo)
        self.lblFrequencyMin.grid(row=3,column=2, padx=5, pady=5)

        lblMaxFreqInfo = Label(frmInfo, text="Frequency (Max):")
        lblMaxFreqInfo.grid(row=3,column=3, padx=5, pady=5)

        self.lblFrequencyMax = Label(frmInfo)
        self.lblFrequencyMax.grid(row=3,column=4, padx=5, pady=5)

        # Row 4
        lblBand = Label(frmInfo, text="Band:")
        lblBand.grid(row=4,column=1, padx=5, pady=5)
        self.cmbBand = Combobox(frmInfo, state="readonly", values=("CHINA", "USA", "KOREA", "EU"))
        self.cmbBand.grid(row=4,column=2, padx=5, pady=5, sticky="ew")

        lblBaudRate = Label(frmInfo, text="Baudrate:")
        lblBaudRate.grid(row=4,column=3, padx=5, pady=5)
        self.cmbBaudRate = Combobox(frmInfo, state="readonly", values=("9600","19200","38400","57600","115200"))
        self.cmbBaudRate.grid(row=4, column=4, padx=5, pady=5)

        # Row 5
        self.varPower = tk.StringVar()
        lblPower = Label(frmInfo, text="Power(dB)")
        lblPower.grid(row=5, column=1, padx=5, pady=5)
        txtPower = Entry(frmInfo, textvariable=self.varPower)
        txtPower.grid(row=5, column=2, padx=5, pady=5, sticky="ew")

        self.varScantime = tk.StringVar()
        lblScantime = Label(frmInfo, text="Scan Time (ms):")
        lblScantime.grid(row=5, column=3, padx=5, pady=5)
        txtScantime = Entry(frmInfo, textvariable=self.varScantime)
        txtScantime.grid(row=5, column=4, padx=5, pady=5, sticky="ew")

        # Row 6
        self.varBeepOn = BooleanVar()
        self.cbBeep = Checkbutton(frmInfo, text="Beep On", variable=self.varBeepOn,offvalue=False,onvalue=True)
        self.cbBeep.grid(row=6, column=1, padx=5, pady=5)

        imgLoad = PhotoImage(file="icon/load.png")
        btnReloadSettings = Button(frmInfo, image=imgLoad, text="Load Settings", command=lambda:UhfApp.loadSettings(self), compound = LEFT)
        btnReloadSettings.grid(row=6, column=2, padx=5, pady=5, sticky="ew")

        imgSave = PhotoImage(file="icon/save.png")
        btnWrite = Button(frmInfo, image=imgSave, text="Save", command=lambda:UhfApp.saveSettings(self), compound = LEFT)
        btnWrite.grid(row=6, column=4, padx=5, pady=5, sticky="ew")
        # [Create Info Tab Content]^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

        # ---------------------------[Memory Tab]-----------------------------
        frmMemory = Frame(self.tabMemory)
        frmMemory.pack(fill=X)

        # Memory Toolbar
        imgRefresh = PhotoImage(file="icon/load.png")
        btnMemoryRefresh = Button(frmMemory, image=imgRefresh, text="Refresh", compound = LEFT)
        btnMemoryRefresh.pack(side=LEFT,fill=Y)

        imgWrite = PhotoImage(file="icon/write.png")
        btnWrite = Button(frmMemory, image=imgWrite, text="Write", compound = LEFT)
        btnWrite.pack(side=LEFT,fill=Y)

        imgClean = PhotoImage(file="icon/clean.png")
        btnClean = Button(frmMemory, image=imgClean, text="Clean", compound = LEFT)
        btnClean.pack(side=LEFT,fill=Y)

        imgAuth = PhotoImage(file="icon/key.png")
        btnAuth = Button(frmMemory, image=imgAuth, text="Auth", compound = LEFT)
        btnAuth.pack(side=LEFT,fill=Y)

        imgExist = PhotoImage(file="icon/cardread.png")
        btnExist = Button(frmMemory, image=imgExist, text="Clean", compound = LEFT)
        btnExist.pack(side=LEFT,fill=Y)

        # Chip Information
        frmTagInfo = Frame(self.tabMemory)

        self.varChipType = StringVar()
        self.varMemTotalByte = StringVar()
        self.varMemTotalBit = StringVar()
        Label(frmTagInfo, text="Chip Type:").grid(row=1,column=1,sticky="ew")
        lblChipName = Entry(frmTagInfo, text="", textvariable = self.varChipType).grid(row=1,column=2,columnspan=2,sticky="ew")
        Label(frmTagInfo, text="Total Memory:").grid(row=1,column=4,sticky="ew")
        lblMemTotalByte = Entry(frmTagInfo, text="", textvariable = self.varMemTotalByte)
        lblMemTotalByte.grid(row=1,column=5,sticky="ew")
        lblMemTotalBit = Entry(frmTagInfo, text="", textvariable = self.varMemTotalBit)
        lblMemTotalBit.grid(row=1,column=6,sticky="ew")
#        lblMemTotalBit.config({background:'yellow'})

        self.varPwdLenByte = StringVar()
        self.varPwdLenBit = StringVar()
        Label(frmTagInfo, text="PWD Size:").grid(row=1,column=7,sticky="ew")
        lblPwdLenByte = Entry(frmTagInfo, text="", textvariable = self.varPwdLenByte)
        lblPwdLenByte.grid(row=1,column=8,sticky="ew")
        lblPwdLenBit = Entry(frmTagInfo, text="", textvariable = self.varPwdLenBit)
        lblPwdLenBit.grid(row=1,column=9,sticky="ew")
#        lblPwdLenBit.config(bg='yellow')

        self.varEPCLenByte = StringVar()
        self.varEPCLenBit = StringVar()
        self.varTIDSizeByte = StringVar()
        self.varTIDSizeBit = StringVar()
        Label(frmTagInfo, text="EPC Size:").grid(row=2,column=1,sticky="ew")
        lblEPCLenByte = Entry(frmTagInfo, text="", textvariable = self.varEPCLenByte).grid(row=2,column=2,sticky="ew")
        lblEPCLenBit = Entry(frmTagInfo, text="", textvariable = self.varEPCLenBit).grid(row=2,column=3,sticky="ew")
#        lblEPCLenBit.config(bg='yellow')
        Label(frmTagInfo, text="TID Size:").grid(row=2,column=4,sticky="ew")
        lblTIDSizeByte = Entry(frmTagInfo, text="", textvariable = self.varTIDSizeByte).grid(row=2,column=5,sticky="ew")
        lblTIDSizeBit = Entry(frmTagInfo, text="", textvariable = self.varTIDSizeBit).grid(row=2,column=6,sticky="ew")
#        lblTIDSizeBit.config(bg='yellow')

        Label(frmTagInfo, text="USER Size:").grid(row=2,column=7,sticky="ew")
        self.varUserMemByte = StringVar()
        self.varUserMemBit = StringVar()
        lblUserMemByte = Entry(frmTagInfo, text="", textvariable = self.varUserMemByte).grid(row=2,column=8,sticky="ew")
        lblUserMemBit = Entry(frmTagInfo, text="", textvariable = self.varUserMemBit).grid(row=2,column=9,sticky="ew")
#        lblUserMemBit.config(bg='yellow')

        frmTagInfo.pack(fill=BOTH, padx=5, pady=5)
        # Draw the table
        frmMemTable = Frame(self.tabMemory)
        frmMemTable.pack(fill=BOTH, expand=True)
        mscrollbar = ttk.Scrollbar(frmMemTable)
        mscrollbar.pack(side=RIGHT, fill=Y)
        
        self.tblMemory = ttk.Treeview(frmMemTable, yscrollcommand=mscrollbar)
        mscrollbar.config(command=self.tblMemory.yview)
        
        cols = ("Word 0", "Word 1", "Word 2", "Word 3", "Word 4", "Word 5","Word 6","Word 7")
        self.tblMemory.tag_configure("eid",background="yellow")
        self.tblMemory.tag_configure('tid',background="orange")
        self.tblMemory.tag_configure('user',background="lightblue")
        self.tblMemory['columns'] = cols

        self.tblMemory.column("#0",anchor="w", stretch=NO, width=100, minwidth=20)
        self.tblMemory.heading("#0", text="Memory")
        for i in range(len(cols)):
            self.tblMemory.column(cols[i],anchor="w", stretch=NO, width=80, minwidth=20)
            self.tblMemory.heading(cols[i], text=cols[i])

        self.tblMemory.pack(fill=BOTH, expand=True)
        # ^^^^^^^^^^^^^^^^^^^^^^^^^[Memory Tab]^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

        # [Status bar]--------------------------------------------------------
        frmStatus = Frame(shell)
        frmStatus.pack(fill=X, anchor="s")

        self.lblVer = Label(frmStatus, text="Lib Version: ")
        self.lblVer.pack(side=RIGHT)

        self.lblStatus = Label(frmStatus, text="Status Line")
        self.lblStatus.pack(fill=X)
        # [Status bar]^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

        shell.mainloop()
        return

def Show():
    gui = UhfApp()
    gui.show()

# This line launches the GUI.
Show()

