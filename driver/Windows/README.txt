
--------------------------------------------------------------------------------------------------------------------------------------
           README for the MCP2200/MCP2221 Driver INF File 
--------------------------------------------------------------------------------------------------------------------------------------

                Microchip Technology, Inc.




--------------------------------------------------------------------------------------------------------------------------------------
System Requirements
--------------------------------------------------------------------------------------------------------------------------------------
Operating System:   Windows XP SP3, Vista, 7, 8 - (ADMIN PRIVILEGES ARE REQUIRED FOR DRIVER INSTALLATION) 


--------------------------------------------------------------------------------------------------------------------------------------
Important Information   
--------------------------------------------------------------------------------------------------------------------------------------

If you run into any of the listed problems below, installing a Windows hotfix may resolve the issue.  See below for details:
- "Code 10" error when installing driver:
    Microsoft has identified an issue with the "usbser.sys" driver causing performance issues and have created a fix.
    Refer to Knowledge Base (KB) article "KB943198" at: http://support.microsoft.com
    The fix can be downloaded here: 
    http://www.microsoft.com/downloads/details.aspx?FamilyId=F2F0A7C2-3B44-4D2E-9640-E0D21818763E&displaylang=en

- Device Enumerates, but does not communicate on the CDC interface (Windows XP):
    Microsoft has identified an issue with the "usbser.sys" driver causing performance issues and have created a fix. 
    Refer to Knowledge Base (KB) article "KB935892" at: http://support.microsoft.com
    Note: The hotfix refers to issues with Windows XP Tablet PCs, however, this fix also corrects the CDC communication issue.
    The fix can be downloaded here: http://support.microsoft.com/kb/935892

- Driver loading issue in Windows XP
    Microsoft has identified an issue with the "usbser.sys" driver may not load when USB device uses IAD to define a function that has 
    multiple interfaces.  Refer to Knowledge Base (KB) article "KB918365" at: http://support.microsoft.com
    Note: The hotfix refers to issues with Windows XP Tablet PCs, however, this fix also corrects the CDC communication issue.
    The fix can be downloaded here: http://support.microsoft.com/kb/918365

  
--------------------------------------------------------------------------------------------------------------------------------------
Versioning History
--------------------------------------------------------------------------------------------------------------------------------------
Version 1.4 (Driver version: 5.1.2600.9):
 - Added MCP2221 VID/PID to INF file
 - Note: This driver is WHQL certified

Version 1.3 (Driver version: 5.1.2600.7):
 - INF file was renamed to "mchpcdc" since it applies to more Microchip products than just the MCP2200 but is otherwise identical to previous versions of MCP2200 driver.
 - Driver is now WHQL certified

Version 1.2 (Driver version: 5.1.2600.3):
 - Digitally signed by Microchip to allow for installation on the Windows 8 operating system.

Version 1.1 (Driver version: 5.1.2600.2):
 - Use this driver (or a newer one if available) if device manufacturing code is 1002nnn or later.

Version 1.00 (Driver Version: 5.1.2600.0):
 **** Use this driver if device manufacturing code is 1001nnn or earlier ****
 - Initial Release
 - NOTE: Devices that use this driver were early samples only, and are not common.
   

--------------------------------------------------------------------------------------------------------------------------------------
Contact Information
--------------------------------------------------------------------------------------------------------------------------------------
Main Website: 		http://www.microchip.com
Technical Support: 	http://support.microchip.com

