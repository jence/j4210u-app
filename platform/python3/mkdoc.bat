set %PATH% = %PATH%;c:\Python37\Scripts\;
set cwd = cd
sphinx-apidoc.exe -o docs .
cd docs
make.bat html
cd cwd

