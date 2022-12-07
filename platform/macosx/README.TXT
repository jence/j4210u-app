BUILD
=====
Open the project associated with this folder into Eclipse or your preferred Java IDE. The simply run the class Jence.swt.app.UhfApp. Make sure that all files in the lib/ folder is in build path.

To run from command line, type:

java -XstartOnFirstThread -jar j4210u.jar

or, run the j4210u.sh script.

A Mac OS X app name UhfApp may be found in this folder. Move this app to your Applications folder, if you want.


jJ4210u.xml
==========
Run this Ant script to build j4210u.jar file. This jar file includes include all files in lib/ folder, therefore, no need to put the lib/ folder on path. If you have changed Java code, then run this Ant task. This will copy the new jar into the UhfApp.app folder. 

libj4210u.dylib
===============
This is the native library, which must be on path when running Java program. This library is a Universal binary.

NOTE:
This project uses the shared src/ folder in the parent directory.

KNOWN ISSUES
============
1. If the libj4210u.dylib fails to load, do one of the following:
   - place it in the current directory.
   - reboot the PC
2. Screen going blank during scan. This can be resolved by rebooting PC.
