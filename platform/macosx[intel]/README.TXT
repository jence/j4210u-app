DRIVER
======
If you have Mac OS X 10.11 or earlier, you may have to download the VCP driver from the following link: https://community.silabs.com/s/article/legacy-os-software-and-driver-packages?language=en_US
If you have later version of Mac OS X, then download the driver from the following link: https://www.silabs.com/developers/usb-to-uart-bridge-vcp-drivers?tab=downloads

BUILD
=====
Open the project associated with this folder into Eclipse or your preferred Java IDE. The simply run the class Jence.swt.app.NfcApp. Make sure that all files in the lib/ folder is in build path.

To run from command line, type:

java -XstartOnFirstThread -jar j4209n.jar

or, run the j4209n.sh script.

To build a Mac OSX app, install4j third party application may be used.


j4209n.xml
==========
Run this Ant script to build j4209n.jar file. This jar file includes include all files in lib/ folder, therefore, no need to put the lib/ folder on path. If you have changed Java code, then run this Ant task. This will copy the new jar into the UhfApp.app folder. 

libj4209n.dylib
===============
This is the native library, which must be on path when running Java program. This library is a Universal binary.

NOTE:
This project uses the shared src/ folder in the parent directory.

KNOWN ISSUES
============
1. If the libj4209n.dylib fails to load, do one of the following:
   - place it in the current directory.
   - reboot the PC
2. Screen going blank during scan. This can be resolved by rebooting PC.

APP
===
The NfcApp application will work only on Intel based Mac. If you have Arm based M1 Mac, use the macosx/NfcApp which is located in that directory.

