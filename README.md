# j4210u-app
J4210U Hardware Demo Application

BUILD
The demo application is built using Eclipse with SWT toolkit. To successfully 
compile this application under other Java IDE and with your own code, do as
follows:

1. Select the platform. Go to each platform and open the project file for that
platform. 
2. But when coding in Java, all cost reside in a platform shared /src folder.
3. The binary class files reside in the platform's /bin folder.

<h2>FRAMEWORK</h2>
You can modify this application and create your own application. Your application then automatically run on all supported platform if you do as follows:
1. Each platform has j4210u.xml. Run this Ant script independently for each platform to build the single executable j4210u.jar. NOTE: This jar is platform dependent.

<h3>Windows 64 bit</h3>
<p>Install Launch4j, a free Java executable from jar to create your Windows executable. Make sure that the following files are in the same directory as the executable: j4210u.dll, libgcc_s_seh-1.dll, libstdc++-6.dll, libwinpthread-1.dll and any other files that you load in the application.</p>
<h3>MAC OS X</h3>
<p>There is a Mac OS X application named UhfApp.app. You can directly run this application. To create your own application, make sure that simply run the Ant script j4210u.xml, the script will modify an application name UhfApp.app. You can change the name and the application icon by editing Info.plist inside UhfApp.app directory.</p>
<h3>Linux 64 bit</h3>
<p>In Linux, you simply run j4210u.sh script. To make this file executable, run the command:<br/>
  <code>chmod 755 j4210u.sh</code><br/>
Make sure libj4210u.so is in the same directory as the jar file.</p>

ECLIPSE PROJECT
===============
Choose the product. Open the project for appropriate platform under the 
directory /platform. 

Windows 64 -> /platform/win64 <br/>
Mac OSX -> /platform/macosx <br/>
Linux 64 -> /platform/linux64 <br/>

NOTE: You may get error after loading the product because the /src folder will not in the same directory where we developed. Simply point to the correct directory in your development environment.

<h4>NOTE</h4>
<p>The hardware driver for j4210u is compiled to run only in 64 bit platform. </p>
