This directory contains the python 3 code.
To run the code under python, you must have python 3 installed.
We used Python 3.7 in our development.

j421xlib.py  : Contains the Python library for calling into the shared library.
j421xtest.py : Contains the test demo.
j421xgui.py  : Is incomplete program that will have the GUI tool.

To run the test code:

python j421xtest.py

Make sure that the shared library or DLL are in this directory.
The shared library or DLL must be in this directory for the platform
you are running. For example, to run in Windows, you must have the
following DLLs in this directory: 
j4210u.dll, libgcc_s_seh-1.dll, libstdc++-6.dll, libwinpthread-1.dll

For Mac OSx: libj4210u.dylib

For all other OS: libj4210u.so

Alternatively, if any of these shared objects or DLL are in your system path
or Python path, then there is no need to copy the files into this directory.

