// raw_usb_test.dart
import 'dart:typed_data';
import 'package:flutter/foundation.dart';
import 'package:usb_serial/usb_serial.dart';

class RawUSBTest {
  static Future<void> testRawUSBCommunication() async {
    try {
      debugPrint('🔍 Testing RAW USB communication...');
      
      final devices = await UsbSerial.listDevices();
      if (devices.isEmpty) {
        debugPrint('❌ No devices found');
        return;
      }
      
      final device = devices.first;
      debugPrint('📱 Device: ${device.deviceName}');
      
      // Create CDC port
      final port = await device.create();
      if (port == null) {
        debugPrint('❌ Could not create port');
        return;
      }
      
      // Open port
      if (!await port.open()) {
        debugPrint('❌ Could not open port');
        return;
      }
      
      debugPrint('✅ Port opened successfully');
      
      // Set up data listener first
      final receivedData = <int>[];
      final subscription = port.inputStream?.listen((data) {
        debugPrint('📥 RECEIVED: ${_bytesToHex(data)} (${data.length} bytes)');
        receivedData.addAll(data);
      });
      
      // Configure port with your exact settings
      await port.setPortParameters(57600, 8, 1, 0);
      
      // Try different DTR/RTS combinations that might wake up the device
      final wakeUpSequences = [
        // Standard CDC initialization
        () async {
          debugPrint('🔧 Sequence 1: Standard CDC');
          await port.setDTR(false);
          await port.setRTS(false);
          await Future.delayed(Duration(milliseconds: 100));
          await port.setDTR(true);
          await port.setRTS(false);
        },
        
        // Modem-style initialization
        () async {
          debugPrint('🔧 Sequence 2: Modem style');
          await port.setDTR(true);
          await port.setRTS(true);
          await Future.delayed(Duration(milliseconds: 100));
          await port.setDTR(false);
          await port.setRTS(false);
          await Future.delayed(Duration(milliseconds: 100));
          await port.setDTR(true);
          await port.setRTS(false);
        },
        
        // Reset-style sequence
        () async {
          debugPrint('🔧 Sequence 3: Reset style');
          await port.setDTR(false);
          await Future.delayed(Duration(milliseconds: 500));
          await port.setDTR(true);
          await Future.delayed(Duration(milliseconds: 500));
        },
      ];
      
      // Try each wake-up sequence
      for (int i = 0; i < wakeUpSequences.length; i++) {
        debugPrint('🚀 Trying wake-up sequence ${i + 1}...');
        
        receivedData.clear();
        await wakeUpSequences[i]();
        
        // Wait for any response to wake-up
        await Future.delayed(Duration(milliseconds: 500));
        
        if (receivedData.isNotEmpty) {
          debugPrint('🎉 Wake-up sequence ${i + 1} triggered response!');
          break;
        }
        
        // Try sending your GetSettings command after each wake-up
        debugPrint('📤 Sending GetSettings after wake-up ${i + 1}...');
        final getSettingsCmd = Uint8List.fromList([0x04, 0x00, 0x21, 0xD9, 0x6A]);
        await port.write(getSettingsCmd);
        
        // Wait for response
        await Future.delayed(Duration(milliseconds: 1000));
        
        if (receivedData.isNotEmpty) {
          debugPrint('🎉 GetSettings triggered response after wake-up ${i + 1}!');
          break;
        }
        
        debugPrint('❌ No response to wake-up sequence ${i + 1}');
      }
      
      // If still no response, try the nuclear option - send lots of different things
      if (receivedData.isEmpty) {
        debugPrint('🧨 Nuclear option: Trying everything...');
        
        final nuclearCommands = [
          // Your original commands
          [0x01, 0x00, 0x21, 0x64, 0x53], // Original GetSettings
          [0x04, 0x00, 0x21, 0xD9, 0x6A], // Corrected GetSettings
          
          // Potential device reset/init commands
          [0x00], // Null
          [0xFF], // High
          [0x55], // Alternating
          [0xAA], // Reverse alternating
          
          // AT commands (in case it's AT-command based)
          [0x41, 0x54, 0x0D], // "AT\r"
          [0x41, 0x54, 0x0D, 0x0A], // "AT\r\n"
          
          // Common reset sequences
          [0x1B], // ESC
          [0x03], // ETX
          [0x04], // EOT
          [0x06], // ACK
          [0x15], // NAK
          
          // Potential wake-up commands
          [0x00, 0x00, 0x00, 0x00, 0x00], // Long null
          [0xFF, 0xFF, 0xFF, 0xFF, 0xFF], // Long high
          
          // Binary patterns
          [0x10, 0x20, 0x30, 0x40], // Incrementing
          [0x01, 0x02, 0x04, 0x08], // Powers of 2
        ];
        
        for (int i = 0; i < nuclearCommands.length; i++) {
          final cmd = nuclearCommands[i];
          debugPrint('💣 Nuclear ${i + 1}: ${_bytesToHex(Uint8List.fromList(cmd))}');
          
          receivedData.clear();
          await port.write(Uint8List.fromList(cmd));
          await Future.delayed(Duration(milliseconds: 300));
          
          if (receivedData.isNotEmpty) {
            debugPrint('💥 NUCLEAR COMMAND ${i + 1} WORKED! Response: ${_bytesToHex(Uint8List.fromList(receivedData))}');
            
            // Try to send your GetSettings again now that device responded
            debugPrint('🔄 Device is awake, trying GetSettings again...');
            receivedData.clear();
            await port.write(Uint8List.fromList([0x04, 0x00, 0x21, 0xD9, 0x6A]));
            await Future.delayed(Duration(milliseconds: 500));
            
            if (receivedData.isNotEmpty) {
              debugPrint('🎉 GetSettings now works! Response: ${_bytesToHex(Uint8List.fromList(receivedData))}');
            }
            break;
          }
        }
      }
      
      // Final status
      if (receivedData.isEmpty) {
        debugPrint('😞 Complete communication failure - device is unresponsive');
        debugPrint('💡 Recommendation: Use platform channels with your C++ code');
      } else {
        debugPrint('🎉 Communication established!');
        debugPrint('📊 Total received: ${receivedData.length} bytes');
        debugPrint('📥 Final data: ${_bytesToHex(Uint8List.fromList(receivedData))}');
      }
      
      // Cleanup
      subscription?.cancel();
      await port.close();
      
    } catch (e) {
      debugPrint('❌ Raw USB test error: $e');
    }
  }
  
  static String _bytesToHex(Uint8List bytes) {
    return bytes.map((b) => b.toRadixString(16).padLeft(2, '0')).join(' ').toUpperCase();
  }
}