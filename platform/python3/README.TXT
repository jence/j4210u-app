This directory contains the python 3 code.
To run the code under python, you must have python 3 installed.
We used Python 3.7 in our development.

j421xlib.py  : Contains the Python library for calling into the shared library.
j421xtest.py : Contains the test demo.
UhfApp.py    : Is incomplete demo that has the GUI tool.
icon/        : You need this folder to display the icons used in the UhfApp.

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

To run the GUI tool:

python UhfApp.py

Make sure that the library files are in the current directory or on its path.

Documentation
=============
To build documentation, you need to install sphinx then type as follows:

sphinx-apidocs -o docs .

Then go to the docs folder and type:

make.bat html

This will generate html files in _build/html folder.
