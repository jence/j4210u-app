// test_direct_usb.dart
import 'package:flutter/services.dart';
import 'package:usb_serial/usb_serial.dart';

class DirectUSBTest {
  static Future<void> testDirectUSBAccess() async {
    try {
      print('🔍 Testing direct USB access...');
      
      // Get the device
      final devices = await UsbSerial.listDevices();
      if (devices.isEmpty) {
        print('❌ No devices found');
        return;
      }
      
      final device = devices.first;
      print('📱 Testing device: ${device.deviceName}');
      print('   VID: 0x${device.vid?.toRadixString(16)}');
      print('   PID: 0x${device.pid?.toRadixString(16)}');
      print('   Product: ${device.productName}');
      print('   Manufacturer: ${device.manufacturerName}');
      print('   Interfaces: ${device.interfaceCount}');
      
      // Try to create port with different types
      final types = ['', 'cdc', 'ftdi', 'cp210x', 'ch34x', 'pl2303'];
      
      for (final type in types) {
        try {
          print('🔧 Trying USB type: ${type.isEmpty ? 'auto' : type}');
          
          final port = await UsbSerial.create(
            device.vid ?? 0, 
            device.pid ?? 0, 
            type
          );
          
          if (port != null) {
            print('✅ Created port with type: ${type.isEmpty ? 'auto' : type}');
            
            final opened = await port.open();
            if (opened) {
              print('✅ Opened port successfully');
              
              // Try basic communication
              await port.setPortParameters(57600, 8, 1, 0);
              await port.setDTR(true);
              
              // Send a simple byte and see what happens
              await port.write(Uint8List.fromList([0xFF]));
              await Future.delayed(Duration(milliseconds: 100));
              
              await port.close();
              print('✅ Basic test completed');
            } else {
              print('❌ Could not open port');
            }
          } else {
            print('❌ Could not create port with type: $type');
          }
        } catch (e) {
          print('❌ Error with type $type: $e');
        }
      }
      
    } catch (e) {
      print('❌ Direct USB test error: $e');
    }
  }
}