// ultimate_debug_rfid_manager.dart
import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/foundation.dart';
import 'package:usb_serial/usb_serial.dart';

class RFIDManager {
  UsbPort? _port;
  UsbDevice? _device;
  bool _isConnected = false;
  StreamSubscription<Uint8List>? _subscription;
  final List<int> _buffer = [];
  
  static const List<int> supportedBaudRates = [57600, 115200, 38400, 19200, 9600];
  static const int PRESET_VALUE = 0xFFFF;
  static const int POLYNOMIAL = 0x8408;
  
  bool get isConnected => _isConnected;
  String? get deviceName => _device?.deviceName;
  String? get deviceInfo => _device?.toString();
  int get baudRate => _port?.baudRate ?? 0;

  Future<bool> connect() async {
    if (_isConnected) return true;

    try {
      debugPrint('🔍 Searching for USB devices...');
      final devices = await UsbSerial.listDevices();
      
      if (devices.isEmpty) {
        debugPrint('❌ No USB devices found');
        return false;
      }

      for (int i = 0; i < devices.length; i++) {
        debugPrint('📱 Device $i: ${devices[i]}');
      }

      for (final device in devices) {
        if (await _connectToDevice(device)) {
          _device = device;
          return true;
        }
      }
      
      return false;
    } catch (e) {
      debugPrint('❌ Connection error: $e');
      return false;
    }
  }

  Future<bool> _connectToDevice(UsbDevice device) async {
    for (final baudRate in supportedBaudRates) {
      debugPrint('🔧 Trying baud rate: $baudRate');
      
      UsbPort? port;
      try {
        port = await device.create();
        if (port == null) continue;

        await Future.delayed(const Duration(milliseconds: 500));

        bool opened = false;
        for (int attempt = 0; attempt < 3; attempt++) {
          try {
            opened = await port.open();
            if (opened) break;
            await Future.delayed(const Duration(milliseconds: 300));
          } catch (e) {
            debugPrint('❌ Open attempt ${attempt + 1} failed: $e');
          }
        }

        if (!opened) continue;

        // Try different USB configurations
        await _configureUSBPort(port, baudRate);
        
        // Setup listener first
        _setupDataListener(port);
        
        _port = port;
        _isConnected = true;
        debugPrint('✅ Connected to ${device.deviceName} at $baudRate baud');
        return true;
        
      } catch (e) {
        debugPrint('❌ Error at $baudRate: $e');
        if (port != null) {
          try { await port.close(); } catch (_) {}
        }
      }
    }
    return false;
  }

  /// Try different USB port configurations
  Future<void> _configureUSBPort(UsbPort port, int baudRate) async {
    debugPrint('🔧 Configuring USB port...');
    
    // Set basic parameters
    await port.setPortParameters(
      baudRate,
      UsbPort.DATABITS_8,
      UsbPort.STOPBITS_1,
      UsbPort.PARITY_NONE,
    );

    // Try different DTR/RTS combinations
    final configs = [
      {'dtr': true, 'rts': false},   // Your current config
      {'dtr': false, 'rts': false},  // Both off
      {'dtr': true, 'rts': true},    // Both on
      {'dtr': false, 'rts': true},   // Reverse
    ];
    
    for (final config in configs) {
      debugPrint('🔧 Trying DTR=${config['dtr']}, RTS=${config['rts']}');
      
      await port.setDTR(config['dtr'] as bool);
      await port.setRTS(config['rts'] as bool);
      
      // Wait for device to respond to configuration
      await Future.delayed(const Duration(milliseconds: 500));
      
      // Try flow control
      try {
        await port.setFlowControl(UsbPort.FLOW_CONTROL_OFF);
        debugPrint('✅ Flow control set to OFF');
      } catch (e) {
        debugPrint('⚠️ Could not set flow control: $e');
      }
      
      // Give it more time
      await Future.delayed(const Duration(milliseconds: 1000));
      
      break; // For now, just try the first config
    }
  }

  void _setupDataListener(UsbPort port) {
    _subscription?.cancel();
    _subscription = port.inputStream?.listen(
      (Uint8List data) {
        debugPrint('📥 RAW DATA: ${_bytesToHex(data)} (${data.length} bytes)');
        _buffer.addAll(data);
      },
      onError: (error) {
        debugPrint('❌ Stream error: $error');
      },
    );
  }

  /// Ultimate device wake-up test
  Future<void> ultimateWakeUpTest() async {
    if (!_isConnected || _port == null) {
      debugPrint('❌ Not connected');
      return;
    }

    debugPrint('🚀 === ULTIMATE WAKE-UP TEST ===');
    
    // Clear buffer
    _buffer.clear();
    
    // 1. Try to wake up device with various methods
    debugPrint('📡 Trying various wake-up methods...');
    
    // Method 1: DTR/RTS cycling (sometimes needed for sleeping devices)
    debugPrint('🔄 Method 1: DTR/RTS cycling...');
    await _port!.setDTR(false);
    await _port!.setRTS(false);
    await Future.delayed(const Duration(milliseconds: 100));
    await _port!.setDTR(true);
    await Future.delayed(const Duration(milliseconds: 100));
    await _port!.setRTS(false);
    await Future.delayed(const Duration(milliseconds: 500));
    
    // Method 2: Send break signal equivalent (lots of zeros)
    debugPrint('🔄 Method 2: Break signal...');
    final breakSignal = Uint8List.fromList(List.filled(10, 0x00));
    await _port!.write(breakSignal);
    await Future.delayed(const Duration(milliseconds: 200));
    
    // Method 3: Send wake-up pattern
    debugPrint('🔄 Method 3: Wake-up patterns...');
    final wakePatterns = [
      [0xFF, 0xFF, 0xFF, 0xFF], // All high
      [0x00, 0x00, 0x00, 0x00], // All low  
      [0xAA, 0xAA, 0xAA, 0xAA], // Alternating
      [0x55, 0x55, 0x55, 0x55], // Reverse alternating
    ];
    
    for (final pattern in wakePatterns) {
      await _port!.write(Uint8List.fromList(pattern));
      await Future.delayed(const Duration(milliseconds: 100));
    }
    
    // Method 4: Try original command format (the one that worked before)
    debugPrint('🔄 Method 4: Original command format...');
    final originalCommand = Uint8List.fromList([0x01, 0x00, 0x21, 0x64, 0x53]);
    await _port!.write(originalCommand);
    await Future.delayed(const Duration(milliseconds: 500));
    
    // Method 5: Try even simpler commands
    debugPrint('🔄 Method 5: Simple commands...');
    final simpleCommands = [
      [0x01], // Just length
      [0x01, 0x00], // Length + address
      [0x02, 0x00, 0x00], // Minimal command
    ];
    
    for (final cmd in simpleCommands) {
      await _port!.write(Uint8List.fromList(cmd));
      await Future.delayed(const Duration(milliseconds: 200));
    }
    
    // Method 6: Continuous ping
    debugPrint('🔄 Method 6: Continuous ping...');
    for (int i = 0; i < 5; i++) {
      await _port!.write(Uint8List.fromList([0xFF]));
      await Future.delayed(const Duration(milliseconds: 100));
      if (_buffer.isNotEmpty) {
        debugPrint('📥 Response during continuous ping: ${_bytesToHex(Uint8List.fromList(_buffer))}');
        break;
      }
    }
    
    // Final check
    await Future.delayed(const Duration(milliseconds: 1000));
    if (_buffer.isNotEmpty) {
      debugPrint('🎉 DEVICE RESPONDED! Data: ${_bytesToHex(Uint8List.fromList(_buffer))}');
    } else {
      debugPrint('😞 No response to any wake-up method');
    }
    
    debugPrint('🚀 === WAKE-UP TEST COMPLETE ===');
  }

  /// Test if the device is echoing data back
  Future<void> testEcho() async {
    if (!_isConnected || _port == null) return;
    
    debugPrint('🔄 === ECHO TEST ===');
    _buffer.clear();
    
    // Send a unique pattern and see if it comes back
    final testPattern = [0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF0];
    debugPrint('📤 Sending test pattern: ${_bytesToHex(Uint8List.fromList(testPattern))}');
    
    await _port!.write(Uint8List.fromList(testPattern));
    await Future.delayed(const Duration(milliseconds: 1000));
    
    if (_buffer.isNotEmpty) {
      final received = Uint8List.fromList(_buffer);
      debugPrint('📥 Received: ${_bytesToHex(received)}');
      
      // Check if it's an echo
      bool isEcho = true;
      if (received.length >= testPattern.length) {
        for (int i = 0; i < testPattern.length; i++) {
          if (received[i] != testPattern[i]) {
            isEcho = false;
            break;
          }
        }
      } else {
        isEcho = false;
      }
      
      if (isEcho) {
        debugPrint('✅ Device is ECHOING data back!');
      } else {
        debugPrint('🤔 Device responded but not echoing');
      }
    } else {
      debugPrint('❌ No echo response');
    }
    
    debugPrint('🔄 === ECHO TEST COMPLETE ===');
  }

  /// Try to detect what the device actually expects
  Future<void> protocolDetection() async {
    if (!_isConnected || _port == null) return;
    
    debugPrint('🕵️ === PROTOCOL DETECTION ===');
    
    // Common RFID reader protocols to try
    final protocols = [
      // ISO18000-6C commands
      {'name': 'ISO18000-6C Query', 'data': [0x08, 0x00, 0x00, 0x00]},
      
      // Common UART protocols
      {'name': 'AT Command', 'data': [0x41, 0x54, 0x0D, 0x0A]}, // "AT\r\n"
      
      // Different framing formats
      {'name': 'STX/ETX Frame', 'data': [0x02, 0x21, 0x03]}, // STX + command + ETX
      {'name': 'Length Prefix', 'data': [0x03, 0x00, 0x21]}, // Length + command
      
      // Your original but with different lengths
      {'name': 'Your Format 1', 'data': [0x01, 0x00, 0x21, 0x64, 0x53]},
      {'name': 'Your Format 2', 'data': [0x04, 0x00, 0x21, 0xD9, 0x6A]}, // The corrected version
      
      // Binary patterns that might trigger response
      {'name': 'Null Command', 'data': [0x00, 0x00, 0x00, 0x00]},
      {'name': 'High Pattern', 'data': [0xFF, 0xFF, 0xFF, 0xFF]},
    ];
    
    for (final protocol in protocols) {
      debugPrint('🧪 Testing: ${protocol['name']}');
      _buffer.clear();
      
      final data = protocol['data'] as List<int>;
      await _port!.write(Uint8List.fromList(data));
      
      // Wait and check multiple times
      for (int wait in [50, 200, 500, 1000]) {
        await Future.delayed(Duration(milliseconds: wait));
        if (_buffer.isNotEmpty) {
          debugPrint('📥 ${protocol['name']} got response after ${wait}ms: ${_bytesToHex(Uint8List.fromList(_buffer))}');
          break;
        }
      }
      
      if (_buffer.isEmpty) {
        debugPrint('❌ ${protocol['name']}: No response');
      }
      
      // Small delay between tests
      await Future.delayed(const Duration(milliseconds: 100));
    }
    
    debugPrint('🕵️ === PROTOCOL DETECTION COMPLETE ===');
  }

  String _bytesToHex(Uint8List bytes) {
    return bytes.map((b) => b.toRadixString(16).padLeft(2, '0')).join(' ').toUpperCase();
  }

  Future<void> disconnect() async {
    try {
      await _subscription?.cancel();
      _subscription = null;
      
      if (_port != null) {
        await _port!.close();
        _port = null;
      }
      
      _device = null;
      _isConnected = false;
      _buffer.clear();
      
      debugPrint('🔌 Disconnected');
    } catch (e) {
      debugPrint('❌ Disconnect error: $e');
    }
  }

  static Future<List<String>> listDevices() async {
    try {
      final devices = await UsbSerial.listDevices();
      return devices.map((d) => d.toString()).toList();
    } catch (e) {
      debugPrint('❌ List devices error: $e');
      return [];
    }
  }

  // UI methods
  Future<String> getReaderSettings() async {
    await ultimateWakeUpTest();
    return 'Wake-up test completed - check logs';
  }

  Future<List<String>> scanTags() async {
    await testEcho();
    await protocolDetection();
    return [];
  }
}