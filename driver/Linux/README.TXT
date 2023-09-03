MCP2200/MCP2221 - Linux Driver Instructions

In order to be able to use the MCP2200/MCP2221 with Linux, the kernel must have support for USB CDC class drivers.
The cdc_acm driver is used for the CDC interface of the device.
If the cdc_acm driver is compiled as a module, when plugging the MCP2200/MCP2221, this driver will pe loaded by the kernel.

To verify that, type:

lsmod | grep cdc

or you can verify it like this:

dmesg | grep ttyACM

After that, there will be an entry in /dev

in order to find the entry type:

ls /dev/ttyACM*

If there are any issues, there will be no entries. But if everything goes well we should see something like:
/dev/ttyACM0

In order to be able to use your favourite terminal program, we will create a link between /dev/ttyACMx and a serial port.
To do that just type:

ln -sf /dev/ttyACM0 /dev/ttyS3

In some other systems, there might be a different number after "ttyACM". You have to provide the number that 
appears in your system. As for the "ttyS3", I had chosen this one since it was an unused serial device node.
Now, let's make some settings on the serial port. Use "stty" to set serial port parameters. E.g. :

stty -F /dev/ttyS3 115200

You can start now whatever serial port utility you want (minicom, gtkterm, ...)