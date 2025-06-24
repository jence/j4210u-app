import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/foundation.dart';
import 'package:usb_serial/usb_serial.dart';

class UHFDeviceDiagnostic {
  UsbPort? _port;
  UsbDevice? _device;
  bool _isConnected = false;
  StreamSubscription<Uint8List>? _subscription;
  final List<int> _buffer = [];
  
  // Detection results
  String _detectionLog = '';
  Map<String, dynamic> _testResults = {};
  String _deviceType = 'Unknown';
  Map<String, String> _deviceInfo = {};
  
  // Getters
  bool get isConnected => _isConnected;
  String get detectionLog => _detectionLog;
  Map<String, dynamic> get testResults => _testResults;
  String get deviceType => _deviceType;
  Map<String, String> get deviceInfo => _deviceInfo;

  void _log(String message) {
    debugPrint(message);
    _detectionLog += '$message\n';
  }

  void clearLog() {
    _detectionLog = '';
  }

  /// Connect to USB device
  Future<bool> connect() async {
    if (_isConnected) return true;

    _log('🔍 Starting comprehensive UHF device diagnostic...');
    _detectionLog = '';
    _testResults.clear();
    _deviceType = 'Unknown';
    _deviceInfo.clear();
    
    try {
      final devices = await UsbSerial.listDevices();
      
      if (devices.isEmpty) {
        _log('❌ No USB devices found');
        return false;
      }

      _log('📱 Found ${devices.length} USB devices');
      for (int i = 0; i < devices.length; i++) {
        final device = devices[i];
        _log('Device $i: ${device.deviceName}');
        _log('  VID: 0x${device.vid?.toRadixString(16).padLeft(4, '0')} (${device.vid})');
        _log('  PID: 0x${device.pid?.toRadixString(16).padLeft(4, '0')} (${device.pid})');
        _log('  Manufacturer: ${device.manufacturerName ?? 'Unknown'}');
        _log('  Product: ${device.productName ?? 'Unknown'}');
        _log('  Serial: ${device.serialNumber ?? 'Unknown'}');
      }

      // Try to connect to each device
      for (final device in devices) {
        if (await _connectToDevice(device)) {
          _device = device;
          _log('✅ Connected to ${device.deviceName}');
          return true;
        }
      }
      
      return false;
    } catch (e) {
      _log('❌ Connection error: $e');
      return false;
    }
  }

  /// Connect with comprehensive baud rate testing
  Future<bool> _connectToDevice(UsbDevice device) async {
    // Based on your log, 115200 works, but let's test all
    final baudRates = [115200, 57600, 38400, 19200, 9600];
    
    for (final baudRate in baudRates) {
      _log('🔧 Testing ${device.deviceName} at $baudRate baud...');
      
      UsbPort? port;
      try {
        port = await device.create();
        if (port == null) {
          _log('❌ Failed to create port');
          continue;
        }

        bool opened = await port.open();
        if (!opened) {
          _log('❌ Failed to open port');
          continue;
        }

        await port.setPortParameters(
          baudRate,
          UsbPort.DATABITS_8,
          UsbPort.STOPBITS_1,
          UsbPort.PARITY_NONE,
        );

        _setupDataListener(port);
        
        _port = port;
        _isConnected = true;
        _log('✅ Port opened at $baudRate baud');
        
        // Test communication at this baud rate
        if (await _testCommunicationAtBaudRate(baudRate)) {
          _log('🎉 Communication working at $baudRate baud');
          return true;
        } else {
          _log('❌ No communication at $baudRate baud');
          await port.close();
          _port = null;
          _isConnected = false;
        }
        
      } catch (e) {
        _log('❌ Error at $baudRate baud: $e');
        if (port != null) {
          try { await port.close(); } catch (_) {}
        }
      }
    }
    return false;
  }

  /// Test communication at specific baud rate
  Future<bool> _testCommunicationAtBaudRate(int baudRate) async {
    _log('📡 Testing communication at $baudRate baud...');
    
    // Test MU910 commands first (most likely based on your log)
    if (await _testMU910Commands()) {
      _deviceType = 'MU910';
      _log('✅ Device responds to MU910 commands');
      return true;
    }
    
    // Test MU903 commands
    if (await _testMU903Commands()) {
      _deviceType = 'MU903';
      _log('✅ Device responds to MU903 commands');
      return true;
    }
    
    // Test if any data is received
    if (await _testForAnyResponse()) {
      _deviceType = 'Generic UHF';
      _log('✅ Device sends data but unknown protocol');
      return true;
    }
    
    return false;
  }

  /// Test MU910 commands with proper response detection
  Future<bool> _testMU910Commands() async {
    _log('🧪 Testing MU910 commands...');
    
    final commands = [
      {'cmd': 0x50, 'name': 'Module Init'},
      {'cmd': 0x51, 'name': 'Device Info'},  
      {'cmd': 0x72, 'name': 'Get Parameters'},
    ];
    
    for (final cmdInfo in commands) {
      try {
        await _clearBuffer();
        
        final packet = _buildMU910Command(cmdInfo['cmd'] as int, []);
        _log('📤 MU910 ${cmdInfo['name']}: ${_bytesToHex(packet)}');
        
        await _port!.write(packet);
        await Future.delayed(const Duration(milliseconds: 300)); // Increased wait time
        
        if (_buffer.isNotEmpty) {
          final response = Uint8List.fromList(List.from(_buffer));
          _log('📥 MU910 Response: ${_bytesToHex(response)}');
          
          // Parse the response for device information
          _parseMU910Response(response, cmdInfo['cmd'] as int);
          
          if (_isMU910Response(response)) {
            _log('✅ Valid MU910 response for ${cmdInfo['name']}');
            return true;
          }
        }
      } catch (e) {
        _log('❌ MU910 ${cmdInfo['name']} error: $e');
      }
    }
    
    return false;
  }

  /// Check if response is from MU910 (more flexible detection)
  bool _isMU910Response(Uint8List response) {
    if (response.isEmpty) return false;
    
    // Look for MU910 signatures in the response
    final responseStr = String.fromCharCodes(response);
    
    // Check for MU910 specific strings
    if (responseStr.contains('MU-910') || 
        responseStr.contains('UHF Senior Reader') ||
        responseStr.contains('SoftVer:')) {
      return true;
    }
    
    // Check for MU910 command response format: CF 00 00 [CMD]
    if (response.length >= 4 && 
        response[0] == 0xCF && 
        response[1] == 0x00 && 
        response[2] == 0x00) {
      return true;
    }
    
    return false;
  }

  /// Parse MU910 response for device information
  void _parseMU910Response(Uint8List response, int command) {
    final responseStr = String.fromCharCodes(response);
    
    if (command == 0x50) { // Module Init response
      if (responseStr.contains('SoftVer:')) {
        final lines = responseStr.split('\n');
        for (final line in lines) {
          if (line.contains('SoftVer:')) {
            _deviceInfo['Software Version'] = line.replaceAll('SoftVer:', '').trim();
          }
          if (line.contains('Hardver:')) {
            _deviceInfo['Hardware Version'] = line.replaceAll('Hardver:', '').trim();
          }
        }
      }
    }
    
    if (command == 0x51) { // Device Info response
      if (response.length > 20) {
        // Extract hardware version (starts around byte 6)
        try {
          final hwStart = 6;
          final hwEnd = response.indexOf(0, hwStart);
          if (hwEnd > hwStart) {
            _deviceInfo['Hardware Model'] = String.fromCharCodes(response.sublist(hwStart, hwEnd));
          }
          
          // Extract software version (starts around byte 40)
          final swStart = 40;
          final swEnd = response.indexOf(0, swStart);
          if (swEnd > swStart && swEnd < response.length) {
            _deviceInfo['Software Version'] = String.fromCharCodes(response.sublist(swStart, swEnd));
          }
        } catch (e) {
          _log('⚠️ Error parsing device info: $e');
        }
      }
    }
  }

  /// Test MU903 commands  
  Future<bool> _testMU903Commands() async {
    _log('🧪 Testing MU903 commands...');
    
    final commands = [
      {'cmd': 0x21, 'name': 'Get Settings'},
      {'cmd': 0x4C, 'name': 'Get Serial'},
    ];
    
    for (final cmdInfo in commands) {
      try {
        await _clearBuffer();
        
        final packet = _buildMU903Command(cmdInfo['cmd'] as int, []);
        _log('📤 MU903 ${cmdInfo['name']}: ${_bytesToHex(packet)}');
        
        await _port!.write(packet);
        await Future.delayed(const Duration(milliseconds: 200));
        
        if (_buffer.isNotEmpty) {
          final response = Uint8List.fromList(List.from(_buffer));
          _log('📥 MU903 Response: ${_bytesToHex(response)}');
          
          if (_verifyMU903Response(response, cmdInfo['cmd'] as int)) {
            _log('✅ Valid MU903 response for ${cmdInfo['name']}');
            return true;
          }
        }
      } catch (e) {
        _log('❌ MU903 ${cmdInfo['name']} error: $e');
      }
    }
    
    return false;
  }

  /// Test for any response
  Future<bool> _testForAnyResponse() async {
    _log('🧪 Testing for any response...');
    
    await _clearBuffer();
    
    // Send a simple pattern
    final testData = Uint8List.fromList([0xFF, 0xFF, 0xFF]);
    _log('📤 Test pattern: ${_bytesToHex(testData)}');
    
    await _port!.write(testData);
    await Future.delayed(const Duration(milliseconds: 200));
    
    if (_buffer.isNotEmpty) {
      _log('📥 Some response: ${_bytesToHex(Uint8List.fromList(_buffer))}');
      return true;
    }
    
    return false;
  }

  /// Comprehensive device detection
  Future<bool> detectDevice() async {
    if (!_isConnected) {
      _log('❌ Not connected to any device');
      return false;
    }

    _log('🔍 Running comprehensive device detection...');
    _testResults.clear();

    bool deviceResponded = false;

    // The device is already working at this point, just need to confirm type
    if (await _testMU910Commands()) {
      _testResults['MU910 Detection'] = true;
      _deviceType = 'MU910';
      deviceResponded = true;
      
      _log('🎉 MU910 DEVICE CONFIRMED!');
      _log('📋 Device Information:');
      _deviceInfo.forEach((key, value) {
        _log('   $key: $value');
      });
      
    } else if (await _testMU903Commands()) {
      _testResults['MU903 Detection'] = true;
      _deviceType = 'MU903';
      deviceResponded = true;
      _log('🎉 MU903 DEVICE CONFIRMED!');
      
    } else {
      _testResults['Unknown Device'] = true;
      _deviceType = 'Unknown';
      _log('❌ Device type could not be determined');
    }

    return deviceResponded;
  }

  /// Build MU910 command
  Uint8List _buildMU910Command(int command, List<int> data) {
    final packet = Uint8List(7 + data.length);
    packet[0] = 0xCF;
    packet[1] = 0xFF;  // Note: Your device responds to 0xFF here
    packet[2] = 0x00;
    packet[3] = command;
    packet[4] = data.length;
    
    for (int i = 0; i < data.length; i++) {
      packet[5 + i] = data[i];
    }
    
    final crc = _calculateMU910CRC(packet, packet.length - 2);
    packet[packet.length - 2] = (crc >> 8) & 0xFF;
    packet[packet.length - 1] = crc & 0xFF;
    
    return packet;
  }

  /// Build MU903 command
  Uint8List _buildMU903Command(int command, List<int> data) {
    final packet = Uint8List(5 + data.length);
    packet[0] = 4 + data.length;
    packet[1] = 0x00;
    packet[2] = command;
    
    for (int i = 0; i < data.length; i++) {
      packet[3 + i] = data[i];
    }
    
    final crc = _calculateMU903CRC(packet, packet.length - 2);
    packet[packet.length - 2] = crc & 0xFF;
    packet[packet.length - 1] = (crc >> 8) & 0xFF;
    
    return packet;
  }

  /// Verify MU903 response
  bool _verifyMU903Response(Uint8List response, int expectedCommand) {
    if (response.length < 4) return false;
    return response[2] == expectedCommand;
  }

  /// Calculate CRC (same for both)
  int _calculateMU910CRC(Uint8List data, int length) {
    const int PRESET_VALUE = 0xFFFF;
    const int POLYNOMIAL = 0x8408;
    
    int crc = PRESET_VALUE;
    
    for (int i = 0; i < length; i++) {
      crc = (crc ^ (data[i] & 0xFF)) & 0xFFFF;
      for (int j = 0; j < 8; j++) {
        if (crc & 0x0001 != 0) {
          crc = ((crc >> 1) ^ POLYNOMIAL) & 0xFFFF;
        } else {
          crc = (crc >> 1) & 0xFFFF;
        }
      }
    }
    return crc;
  }

  int _calculateMU903CRC(Uint8List data, int length) {
    return _calculateMU910CRC(data, length); // Same algorithm
  }

  /// Setup data listener
  void _setupDataListener(UsbPort port) {
    _subscription?.cancel();
    _subscription = port.inputStream?.listen(
      (Uint8List data) {
        _log('📥 Raw data: ${_bytesToHex(data)}');
        _buffer.addAll(data);
      },
      onError: (error) {
        _log('❌ Stream error: $error');
      },
    );
  }

  /// Clear buffer
  Future<void> _clearBuffer() async {
    _buffer.clear();
    await Future.delayed(const Duration(milliseconds: 100));
    _buffer.clear();
  }

  /// Convert bytes to hex
  String _bytesToHex(Uint8List bytes) {
    return bytes.map((b) => b.toRadixString(16).padLeft(2, '0').toUpperCase()).join(' ');
  }

  /// Disconnect
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
      _deviceType = 'Unknown';
      _deviceInfo.clear();
      _buffer.clear();
      
      _log('🔌 Disconnected');
    } catch (e) {
      _log('❌ Disconnect error: $e');
    }
  }
}

extension on UsbDevice {
  get serialNumber => null;
}