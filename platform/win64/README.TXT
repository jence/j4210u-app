BUILD
=====
Open the project associated with this folder into Eclipse or your preferred Java IDE. The simply run the class Jence.swt.app.UhfApp. Make sure that all files in the lib/ folder is in build path.

To run from command line, type:

java -jar j4210u.jar

or, run the j4210u.exe 

To build a Windows app, launch4j third party application may be used.


j4210u.xml
==========
Run this Ant script to build j4210u.jar file. This jar file includes include all files in lib/ folder, therefore, no need to put the lib/ folder on path. If you have changed Java code, then run this Ant task. This will copy the new jar into the UhfApp.app folder. 

j4210u.dll
===============
This is the native library, which must be on path when running Java program.

NOTE:
This project uses the shared src/ folder in the parent directory.


