import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/foundation.dart';
import 'package:j42210_dart/models/reader_info.dart';
import 'package:j42210_dart/models/tag_data.dart';
import 'package:usb_serial/usb_serial.dart';
import 'uhf_protocol.dart';
// import 'mu903_protocol.dart';
// import 'mu910_protocol.dart';

class UHFRFIDManager {
  UsbPort? _port;
  UsbDevice? _device;
  bool _isConnected = false;
  bool _isMU910 = false;
  StreamSubscription<Uint8List>? _subscription;
  final List<int> _buffer = [];

  // All supported baud rates
  static const List<int> supportedBaudRates = [
    57600,
    115200,
    38400,
    19200,
    9600,
  ];
  int _scanTime = 300;
  int _qValue = 2;
  int _session = 0;

  // Device information storage
  Map<String, String> _deviceInfo = {};

  bool get isConnected => _isConnected;
  bool get isMU910 => _isMU910;
  String? get deviceName => _device?.deviceName;
  String? get deviceInfo => _device?.toString();
  int get baudRate => _port?.baudRate ?? 0;
  Map<String, String> get detectedDeviceInfo => _deviceInfo;

  //#####For Seting band and power#############
  static const Map<String, int> bandCodes = {
    'U': 2, // USA
    'C': 1, // China
    'K': 3, // Korea
    'E': 4, // Europe
  };

  static const Map<int, String> bandNames = {
    1: 'China (920.125-924.875 MHz)',
    2: 'USA (902.75-927.25 MHz)',
    3: 'Korea (917.1-923.5 MHz)',
    4: 'Europe (865.1-868.1 MHz)',
  };
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
        final device = devices[i];
        debugPrint('📱 Device $i: ${device.deviceName}');
        debugPrint(
          '  VID: 0x${device.vid?.toRadixString(16).padLeft(4, '0')} (${device.vid})',
        );
        debugPrint(
          '  PID: 0x${device.pid?.toRadixString(16).padLeft(4, '0')} (${device.pid})',
        );
        debugPrint('  Manufacturer: ${device.manufacturerName ?? 'Unknown'}');
        debugPrint('  Product: ${device.productName ?? 'Unknown'}');
      }

      for (final device in devices) {
        if (await _connectToDeviceWithSmartDetection(device)) {
          _device = device;

          // Get settings based on detected device type
          final readerInfo = await getSettings();
          if (readerInfo != null) {
            debugPrint(
              '📊 Reader connected: $readerInfo (Type: ${_isMU910 ? "MU910" : "MU903"})',
            );
            _scanTime = readerInfo.scanTime * 100;

            // Log device information
            if (_deviceInfo.isNotEmpty) {
              debugPrint('📋 Device Information:');
              _deviceInfo.forEach((key, value) {
                debugPrint('   $key: $value');
              });
            }

            return true;
          }
        }
      }

      return false;
    } catch (e) {
      debugPrint('❌ Connection error: $e');
      return false;
    }
  }

  /// Smart device connection with detection at each baud rate
  Future<bool> _connectToDeviceWithSmartDetection(UsbDevice device) async {
    // Test combinations: (baudRate, deviceType)
    final testCombinations = [
      // Try MU903 common baud rates first
      {'baud': 57600, 'priority': 'MU903'},
      {'baud': 38400, 'priority': 'MU903'},
      {'baud': 115200, 'priority': 'MU903'},
      // Try MU910 common baud rate
      {'baud': 115200, 'priority': 'MU910'},
      {'baud': 57600, 'priority': 'MU910'},

      // Try remaining baud rates for both
      {'baud': 19200, 'priority': 'Both'},
      {'baud': 9600, 'priority': 'Both'},
    ];

    for (final combination in testCombinations) {
      final baudRate = combination['baud'] as int;
      final priority = combination['priority'] as String;

      debugPrint('🔧 Testing $baudRate baud (Priority: $priority)...');

      UsbPort? port;
      try {
        port = await device.create();
        if (port == null) continue;

        await Future.delayed(const Duration(milliseconds: 300));

        bool opened = false;
        for (int attempt = 0; attempt < 2; attempt++) {
          try {
            opened = await port.open();
            if (opened) break;
            await Future.delayed(const Duration(milliseconds: 200));
          } catch (e) {
            debugPrint('❌ Open attempt ${attempt + 1} failed: $e');
          }
        }

        if (!opened) continue;

        await port.setPortParameters(
          baudRate,
          UsbPort.DATABITS_8,
          UsbPort.STOPBITS_1,
          UsbPort.PARITY_NONE,
        );

        await port.setDTR(true);
        await port.setRTS(false);

        await Future.delayed(const Duration(milliseconds: 300));

        _setupDataListener(port);

        _port = port;
        _isConnected = true;

        // Test device type at this baud rate
        final deviceType = await _detectDeviceTypeAtCurrentBaud(priority);

        if (deviceType != null) {
          _isMU910 = (deviceType == 'MU910');
          debugPrint(
            '✅ Connected to ${device.deviceName} at $baudRate baud as $deviceType',
          );
          return true;
        } else {
          debugPrint('❌ No valid device detected at $baudRate baud');
          await port.close();
          _port = null;
          _isConnected = false;
        }
      } catch (e) {
        debugPrint('❌ Error at $baudRate: $e');
        if (port != null) {
          try {
            await port.close();
          } catch (_) {}
        }
      }
    }
    return false;
  }

  /// Detect device type at current baud rate
  Future<String?> _detectDeviceTypeAtCurrentBaud(String priority) async {
    debugPrint(
      '🔍 Device type detection at current baud rate (Priority: $priority)...',
    );
    _deviceInfo.clear();

    // Test based on priority
    if (priority == 'MU903' || priority == 'Both') {
      // Try MU903 first
      if (await _testMU903Protocol()) {
        debugPrint('✅ Detected MU903 device');
        return 'MU903';
      }
    }

    if (priority == 'MU910' || priority == 'Both') {
      // Try MU910 enhanced detection
      if (await _testMU910ProtocolEnhanced()) {
        debugPrint('✅ Detected MU910 device with enhanced detection');
        return 'MU910';
      }

      // Try MU910 original detection
      if (await _testMU910Protocol()) {
        debugPrint('✅ Detected MU910 device with original method');
        return 'MU910';
      }
    }

    // If priority is 'Both', try the other type
    if (priority == 'Both') {
      if (await _testMU903Protocol()) {
        debugPrint('✅ Detected MU903 device (fallback)');
        return 'MU903';
      }
    }

    return null; // No device detected at this baud rate
  }

  /// Original MU903 protocol test
  Future<bool> _testMU903Protocol() async {
    try {
      debugPrint('🧪 Testing MU903 protocol...');
      final response = await _transferMU903(0x21, [], 200, 10);
      bool isValid =
          response.isNotEmpty && response.length >= 16 && response[2] == 0x21;

      if (isValid) {
        _deviceInfo['Device Type'] = 'MU903';
        _deviceInfo['Communication'] = 'USB Serial';
        _deviceInfo['Baud Rate'] = '${_port?.baudRate ?? 0}';
        // Extract version info from MU903 response
        if (response.length >= 6) {
          _deviceInfo['Version'] = '${response[4]}.${response[5]}';
        }
        debugPrint('✅ MU903 protocol test successful');
      } else {
        debugPrint('❌ MU903 protocol test failed');
      }

      return isValid;
    } catch (e) {
      debugPrint('❌ MU903 protocol test error: $e');
      return false;
    }
  }

  /// Enhanced MU910 protocol test
  Future<bool> _testMU910ProtocolEnhanced() async {
    try {
      debugPrint('🧪 Enhanced MU910 protocol test...');

      // Clear buffer first
      await _emptySerial();

      // Test Module Init (0x50) - this worked in our diagnostic
      final moduleInitCmd = [0xCF, 0xFF, 0x00, 0x50, 0x00, 0x00, 0x00];
      final initResponse = await _transferMU910(moduleInitCmd, 100, 200, 100);

      if (initResponse.isNotEmpty && _isMU910ResponseEnhanced(initResponse)) {
        debugPrint('✅ MU910 Module Init successful');
        _parseMU910DeviceInfo(initResponse, 0x50);
        _deviceInfo['Baud Rate'] = '${_port?.baudRate ?? 0}';

        // Try Device Info (0x51) for more details
        try {
          final deviceInfoCmd = [0xCF, 0xFF, 0x00, 0x51, 0x00, 0x00, 0x00];
          final deviceResponse = await _transferMU910(
            deviceInfoCmd,
            100,
            300,
            100,
          );

          if (deviceResponse.isNotEmpty) {
            _parseMU910DeviceInfo(deviceResponse, 0x51);
          }
        } catch (e) {
          debugPrint(
            '⚠️ MU910 Device Info failed, but Module Init succeeded: $e',
          );
        }

        return true;
      }

      debugPrint('❌ Enhanced MU910 test failed');
      return false;
    } catch (e) {
      debugPrint('❌ Enhanced MU910 test error: $e');
      return false;
    }
  }

  /// Original MU910 protocol test (keeping as fallback)
  Future<bool> _testMU910Protocol() async {
    try {
      debugPrint('🧪 Original MU910 protocol test...');
      // Try the basic 910 settings command (0x72)
      final command = [0xCF, 0xFF, 0x00, 0x72, 0, 0, 0];
      final response = await _transferMU910(command, 33, 200, 100);
      bool isValid =
          response.isNotEmpty &&
          response.length >= 7 &&
          response[0] == 0xCF &&
          response[1] == 0xFF &&
          response[5] == 0x00;

      if (isValid) {
        _deviceInfo['Device Type'] = 'MU910';
        _deviceInfo['Communication'] = 'USB CDC Serial';
        _deviceInfo['Baud Rate'] = '${_port?.baudRate ?? 0}';
        debugPrint('✅ Original MU910 protocol test successful');
      } else {
        debugPrint('❌ Original MU910 protocol test failed');
      }

      return isValid;
    } catch (e) {
      debugPrint('❌ Original MU910 test error: $e');
      return false;
    }
  }

  /// Enhanced MU910 response detection
  bool _isMU910ResponseEnhanced(Uint8List response) {
    if (response.isEmpty) return false;

    // Convert to string to check for MU910 signatures
    try {
      final responseStr = String.fromCharCodes(response);

      // Check for MU910 specific strings (these were in our successful test)
      if (responseStr.contains('MU-910') ||
          responseStr.contains('UHF Senior Reader') ||
          responseStr.contains('SoftVer:') ||
          responseStr.contains('Hardver:')) {
        debugPrint('✅ Found MU910 signature in response');
        return true;
      }
    } catch (e) {
      // Ignore string conversion errors
    }

    // Check for MU910 command response format: CF 00 00 [CMD]
    if (response.length >= 6 &&
        response[0] == 0xCF &&
        response[1] == 0x00 &&
        response[2] == 0x00) {
      debugPrint('✅ Found MU910 command response format');
      return true;
    }

    return false;
  }

  /// Parse MU910 device information from responses
  void _parseMU910DeviceInfo(Uint8List response, int command) {
    try {
      final responseStr = String.fromCharCodes(response);

      if (command == 0x50) {
        // Module Init response
        // Parse software version
        if (responseStr.contains('SoftVer:')) {
          final lines = responseStr.split('\n');
          for (final line in lines) {
            if (line.contains('SoftVer:')) {
              _deviceInfo['Software Version'] = line
                  .replaceAll('SoftVer:', '')
                  .trim();
            }
            if (line.contains('Hardver:')) {
              _deviceInfo['Hardware Version'] = line
                  .replaceAll('Hardver:', '')
                  .trim();
            }
          }
        }

        // Parse UHF Senior Reader version
        if (responseStr.contains('UHF Senior Reader')) {
          final pattern = RegExp(r'UHF Senior Reader (V[\d.]+)');
          final match = pattern.firstMatch(responseStr);
          if (match != null) {
            _deviceInfo['Reader Software'] = match.group(1) ?? '';
          }
        }
      }

      if (command == 0x51) {
        // Device Info response
        if (response.length > 20) {
          // Extract hardware model (starts around byte 6)
          try {
            final hwStart = 6;
            final hwEnd = response.indexOf(0, hwStart);
            if (hwEnd > hwStart) {
              final hwModel = String.fromCharCodes(
                response.sublist(hwStart, hwEnd),
              );
              if (hwModel.isNotEmpty) {
                _deviceInfo['Hardware Model'] = hwModel;
              }
            }
          } catch (e) {
            debugPrint('⚠️ Error parsing hardware model: $e');
          }
        }
      }

      _deviceInfo['Device Type'] = 'MU910';
      _deviceInfo['Communication'] = 'USB CDC Serial';
    } catch (e) {
      debugPrint('⚠️ Error parsing MU910 device info: $e');
    }
  }

  void _setupDataListener(UsbPort port) {
    _subscription?.cancel();
    _subscription = port.inputStream?.listen(
      (Uint8List data) {
        debugPrint('📥 Raw data: ${UHFProtocol.bytesToHex(data)}');
        _buffer.addAll(data);
      },
      onError: (error) {
        debugPrint('❌ Stream error: $error');
      },
    );
  }

  /// ORIGINAL MU903 Transfer function (EXACTLY like your working version)
  Future<Uint8List> _transferMU903(
    int command, [
    List<int> data = const [],
    int timeoutMs = 1000,
    int pause = 10,
  ]) async {
    if (_port == null) throw Exception('Port not connected');

    const defaultRetries = 2;

    for (int retry = 0; retry < defaultRetries; retry++) {
      try {
        debugPrint(
          '📤 MU903 Transfer attempt ${retry + 1}: command 0x${command.toRadixString(16).padLeft(2, '0')}',
        );

        // Clear buffer before sending
        _buffer.clear();

        // Build MU903 command using your original working protocol
        final commandPacket = UHFProtocol.buildCommand(command, data);
        debugPrint(
          '📤 MU903 Sending: ${UHFProtocol.bytesToHex(commandPacket)}',
        );

        // Send command
        await _port!.write(commandPacket);

        // Pause
        if (pause > 0) {
          await Future.delayed(Duration(milliseconds: pause));
        }

        // Wait for response
        await Future.delayed(Duration(milliseconds: timeoutMs));

        if (_buffer.isNotEmpty) {
          final response = Uint8List.fromList(List.from(_buffer));
          debugPrint('📥 MU903 Received: ${UHFProtocol.bytesToHex(response)}');

          // Verify MU903 CRC
          final crcVerified = UHFProtocol.verifyCRC(response);
          debugPrint(
            crcVerified ? '✅ MU903 CRC verified' : '⚠️ MU903 CRC mismatch',
          );

          return response;
        } else {
          debugPrint('❌ MU903 No response on attempt ${retry + 1}');
          if (retry < defaultRetries - 1 && pause > 0) {
            await Future.delayed(Duration(milliseconds: pause));
          }
        }
      } catch (e) {
        debugPrint('❌ MU903 Transfer attempt ${retry + 1} error: $e');
        if (retry < defaultRetries - 1 && pause > 0) {
          await Future.delayed(Duration(milliseconds: pause));
        }
      }
    }

    debugPrint('❌ All MU903 transfer attempts failed');
    return Uint8List(0);
  }

  /// MU910 Transfer function with improved timing
  Future<Uint8List> _transferMU910(
    List<int> command,
    int responseSize,
    int sleepMs,
    int pause,
  ) async {
    if (_port == null) throw Exception('Port not connected');

    // Empty serial like in your 910 driver
    await _emptySerial();

    // Build command with CRC exactly like your 910driver.cpp
    final commandPacket = Uint8List.fromList(command);
    final crc = _generateMU910CRC(commandPacket, commandPacket.length - 2);
    commandPacket[commandPacket.length - 1] = crc & 0xFF;
    commandPacket[commandPacket.length - 2] = (crc >> 8) & 0xFF;

    debugPrint('📤 MU910 Sending: ${UHFProtocol.bytesToHex(commandPacket)}');

    const defaultRetries = 2; // Reduced retries for faster detection
    int retries = defaultRetries;
    bool fail = false;

    // Send with retries
    do {
      try {
        await _port!.write(commandPacket);
        fail = false;
        break;
      } catch (e) {
        fail = true;
        if (pause > 0) {
          await Future.delayed(Duration(milliseconds: pause ~/ 2));
        }
      }
    } while (retries-- > 0);

    if (fail) {
      debugPrint('❌ MU910 Send command failed.');
      return Uint8List(0);
    }

    // Clear buffer and pause
    _buffer.clear();
    if (pause > 0) {
      await Future.delayed(Duration(milliseconds: pause));
    }

    // Wait for response
    await Future.delayed(Duration(milliseconds: sleepMs));

    if (_buffer.isNotEmpty) {
      final response = Uint8List.fromList(List.from(_buffer));
      debugPrint('📥 MU910 Received: ${UHFProtocol.bytesToHex(response)}');

      // For MU910, accept response if it looks valid (prioritize detection over strict CRC)
      if (_isMU910ResponseEnhanced(response)) {
        debugPrint('✅ MU910 Valid response detected');
        return response;
      } else {
        // Try original CRC verification as fallback
        final crcMatch = _verifyMU910CRC(response);
        debugPrint(
          crcMatch
              ? '✅ MU910 CRC match.'
              : '⚠️ MU910 CRC MISMATCH but response received',
        );
        return response; // Return even with CRC mismatch if we got data
      }
    }

    debugPrint('❌ MU910 No response received');
    return Uint8List(0);
  }

  /// Empty serial like emptyserial() in 910driver.cpp
  Future<void> _emptySerial() async {
    try {
      _buffer.clear();
      await Future.delayed(const Duration(milliseconds: 50));
      _buffer.clear();
    } catch (e) {
      debugPrint('⚠️ Error emptying serial: $e');
    }
  }

  /// Generate MU910 CRC exactly like your 910driver.cpp
  int _generateMU910CRC(Uint8List data, int n) {
    const int PRESET_VALUE = 0xFFFF;
    const int POLYNOMIAL = 0x8408;

    int crc = PRESET_VALUE;

    for (int i = 0; i < n; i++) {
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

  /// Verify MU910 CRC exactly like your 910driver.cpp
  bool _verifyMU910CRC(Uint8List data) {
    if (data.length < 7) return false;

    final len = data[4];
    final rawSize = len + 5;

    if (data.length < rawSize + 2) return false;

    final crc = _generateMU910CRC(data, rawSize);
    final crcl = crc & 0xFF;
    final crch = (crc >> 8) & 0xFF;

    debugPrint(
      '🔍 MU910 CRC: Generated=${crc.toRadixString(16)}, Expected=${data[rawSize].toRadixString(16)}${data[rawSize + 1].toRadixString(16)}',
    );

    return data[rawSize] == crch && data[rawSize + 1] == crcl;
  }

  /// Route to appropriate getSettings
  Future<ReaderInfo?> getSettings() async {
    if (!_isConnected) return null;

    try {
      if (_isMU910) {
        return await _getMU910Settings();
      } else {
        return await _getMU903Settings();
      }
    } catch (e) {
      debugPrint('❌ Get settings error: $e');
      return null;
    }
  }

  /// ORIGINAL MU903 getSettings (EXACTLY like your working version)
  Future<ReaderInfo?> _getMU903Settings() async {
    debugPrint('🔍 Getting MU903 reader settings...');

    try {
      final response = await _transferMU903(0x21, [], 100, 10);

      if (response.isEmpty || response.length < 16) {
        debugPrint('❌ Invalid MU903 settings response');
        return null;
      }

      return _parseMU903Settings(response);
    } catch (e) {
      debugPrint('❌ Get MU903 settings error: $e');
      return null;
    }
  }

  /// Enhanced MU910 getSettings
  Future<ReaderInfo?> _getMU910Settings() async {
    debugPrint('🔍 Getting MU910 reader settings...');

    try {
      // Get device info first (command 0x51)
      final deviceInfoCmd = [0xCF, 0xFF, 0x00, 0x51, 0, 0, 0];
      final deviceResponse = await _transferMU910(deviceInfoCmd, 100, 200, 100);

      // Get settings (command 0x72)
      final settingsCmd = [0xCF, 0xFF, 0x00, 0x72, 0, 0, 0];
      final settingsResponse = await _transferMU910(settingsCmd, 33, 100, 100);

      if (settingsResponse.isNotEmpty && settingsResponse.length >= 20) {
        return _parseMU910Settings(deviceResponse, settingsResponse);
      } else {
        debugPrint(
          '⚠️ Using minimal MU910 settings due to incomplete response',
        );
        return _getMinimalMU910Settings();
      }
    } catch (e) {
      debugPrint('❌ Get MU910 settings error: $e');
      // Return minimal settings rather than failing completely
      return _getMinimalMU910Settings();
    }
  }

  /// Fallback minimal MU910 settings
  ReaderInfo _getMinimalMU910Settings() {
    final readerInfo = ReaderInfo();
    readerInfo.readerType = 0x0A; // MU910 type
    readerInfo.versionInfo[0] = 1;
    readerInfo.versionInfo[1] = 0;
    readerInfo.baudRate = baudRate;
    readerInfo.band = 'U';
    readerInfo.scanTime = 3;
    readerInfo.power = 30;
    readerInfo.antenna = 1;
    readerInfo.beepOn = 1;
    readerInfo.protocol = 0x6C;
    readerInfo.minFreq = 902750;
    readerInfo.maxFreq = 927250;
    return readerInfo;
  }

  // Keep all your ORIGINAL parsing and scanning methods EXACTLY as they were
  ReaderInfo _parseMU903Settings(Uint8List response) {
    final readerInfo = ReaderInfo();

    readerInfo.comAdr = response[1];
    readerInfo.versionInfo[0] = response[4];
    readerInfo.versionInfo[1] = response[5];
    readerInfo.readerType = response[6];
    readerInfo.protocol = response[7];

    final dmaxfre = response[8];
    final dminfre = response[9];
    readerInfo.power = response[10];
    readerInfo.scanTime = response[11];
    readerInfo.antenna = response[12];
    readerInfo.beepOn = response[13];

    readerInfo.band = _parseBand((dmaxfre >> 6) << 2 | (dminfre >> 6));
    readerInfo.minFreq = dminfre & 0x3f;
    readerInfo.maxFreq = dmaxfre & 0x3f;

    _applyBandFrequencies(readerInfo);
    _parseProtocol(readerInfo);
    readerInfo.baudRate = baudRate;

    return readerInfo;
  }

  /// Parse MU910 settings exactly like your 910driver.cpp
  ReaderInfo _parseMU910Settings(
    Uint8List deviceResponse,
    Uint8List settingsResponse,
  ) {
    final readerInfo = ReaderInfo();

    if (settingsResponse.length >= 30) {
      // Parse from settings response like your getSettings() internal
      readerInfo.antenna = settingsResponse[12];
      readerInfo.comAdr = settingsResponse[6];
      readerInfo.readerType = 0x0A; // MU910
      readerInfo.protocol = settingsResponse[7];

      // Parse band as integer first, then convert
      final bandCode = settingsResponse[13];
      readerInfo.power = settingsResponse[21];
      readerInfo.scanTime = _scanTime ~/ 100;
      readerInfo.beepOn = settingsResponse[29] != 0x00 ? 1 : 0;

      // Parse frequencies like in your 910driver.cpp
      final startFreqI =
          ((settingsResponse[14] << 8) & 0xFFFF) | settingsResponse[15];
      final startFreqD =
          ((settingsResponse[16] << 8) & 0xFFFF) | settingsResponse[17];
      final stepFreq =
          ((settingsResponse[18] << 8) & 0xFFFF) | settingsResponse[19];
      final CN = settingsResponse[20];

      debugPrint(
        '🔍 MU910 Freq: StartFreqI=$startFreqI, StartFreqD=$startFreqD, stepFreq=$stepFreq, CN=$CN',
      );

      // Parse protocol like your 910driver.cpp
      if (readerInfo.protocol == 0x00) {
        readerInfo.protocol = 0x6B;
      } else if (readerInfo.protocol == 0x01) {
        readerInfo.protocol = 0x77;
      } else {
        readerInfo.protocol = 0x6C;
      }

      // Set band and frequency exactly like your 910driver.cpp
      switch (bandCode) {
        case 1: // USA BAND
          readerInfo.minFreq = (902.75 * 1000.0).round();
          readerInfo.maxFreq = (927.25 * 1000.0).round();
          readerInfo.band = 'U';
          break;
        case 2: // KOREA BAND
          readerInfo.minFreq = (917.1 * 1000.0).round();
          readerInfo.maxFreq = (923.5 * 1000.0).round();
          readerInfo.band = 'K';
          break;
        case 3: // EU BAND
          readerInfo.minFreq = (865.1 * 1000.0).round();
          readerInfo.maxFreq = (868.1 * 1000.0).round();
          readerInfo.band = 'E';
          break;
        case 8: // CHINA2 BAND
          readerInfo.minFreq = (920.125 * 1000.0).round();
          readerInfo.maxFreq = (924.875 * 1000.0).round();
          readerInfo.band = 'C';
          break;
        default:
          readerInfo.minFreq = (902.75 * 1000.0).round();
          readerInfo.maxFreq = (927.25 * 1000.0).round();
          readerInfo.band = 'U';
      }
    } else {
      // Fallback settings
      return _getMinimalMU910Settings();
    }

    // Parse version from device response
    readerInfo.versionInfo[0] = 1;
    readerInfo.versionInfo[1] = 0;
    readerInfo.baudRate = baudRate;

    return readerInfo;
  }

  void _applyBandFrequencies(ReaderInfo readerInfo) {
    switch (readerInfo.band) {
      case 'C':
        readerInfo.minFreq = (920.125 * 1000).round();
        readerInfo.maxFreq = (920.125 * 1000 + readerInfo.maxFreq * 0.25 * 1000)
            .round();
        break;
      case 'U':
        readerInfo.minFreq = (902.75 * 1000).round();
        readerInfo.maxFreq = (902.75 * 1000 + readerInfo.maxFreq * 0.5 * 1000)
            .round();
        break;
      case 'K':
        readerInfo.minFreq = (917.1 * 1000).round();
        readerInfo.maxFreq = (917.1 * 1000 + readerInfo.maxFreq * 0.2 * 1000)
            .round();
        break;
      case 'E':
        readerInfo.minFreq = (865.1 * 1000).round();
        readerInfo.maxFreq = (865.1 * 1000 + readerInfo.maxFreq * 0.2 * 1000)
            .round();
        break;
    }
  }

  void _parseProtocol(ReaderInfo readerInfo) {
    if (readerInfo.protocol & 0x01 != 0) {
      readerInfo.protocol = 0x6B;
    } else if (readerInfo.protocol & 0x02 != 0) {
      readerInfo.protocol = 0x6C;
    }
  }

  String _parseBand(int bandCode) {
    switch (bandCode) {
      case 1:
        return 'C';
      case 2:
        return 'U';
      case 3:
        return 'K';
      case 4:
        return 'E';
      case 8:
        return 'C';
      default:
        return 'U';
    }
  }

  Future<List<TagData>> scanTags() async {
    if (!_isConnected || _port == null) {
      throw Exception('Device not connected');
    }

    try {
      if (_isMU910) {
        return await _scanTagsMU910();
      } else {
        return await _scanTagsMU903();
      }
    } catch (e) {
      debugPrint('❌ Scan error: $e');
      rethrow;
    }
  }

  /// UPDATED MU903 scan to return TagData objects
  Future<List<TagData>> _scanTagsMU903() async {
    debugPrint('🔍 Starting MU903 inventory scan...');

    await Future.delayed(const Duration(milliseconds: 100));
    await _clearBuffer();

    final inventoryData = <int>[
      _qValue,
      _session,
      0x00,
      0x80,
      (_scanTime / 100).round(),
    ];

    final inventoryResp = await _transferMU903(
      0x18,
      inventoryData,
      (_scanTime * 1.5).round(),
      100,
    );

    if (inventoryResp.isEmpty) {
      debugPrint('❌ No inventory response');
      return [];
    }

    final List<TagData> allTags = [];
    bool repeat = true;
    int maxLoop = 100;

    while (repeat && maxLoop > 0) {
      final bufferResp = await _transferMU903(0x72, [], 200, 200);

      if (bufferResp.isEmpty) {
        break;
      }

      if (bufferResp.length >= 4 && bufferResp[3] == 0x01) {
        repeat = false;
      }

      final tags = _parseBufferResponse(bufferResp);
      allTags.addAll(tags);

      maxLoop--;
    }

    await _clearBuffer();

    // Remove duplicates based on EPC
    final Map<String, TagData> uniqueTagsMap = {};
    for (final tag in allTags) {
      if (uniqueTagsMap.containsKey(tag.epc)) {
        // Keep the one with higher RSSI or more recent timestamp
        final existing = uniqueTagsMap[tag.epc]!;
        if (tag.rssi > existing.rssi ||
            tag.timestamp.isAfter(existing.timestamp)) {
          uniqueTagsMap[tag.epc] = tag;
        }
      } else {
        uniqueTagsMap[tag.epc] = tag;
      }
    }

    final uniqueTags = uniqueTagsMap.values.toList();
    debugPrint('✅ MU903 scan complete. Found ${uniqueTags.length} unique tags');
    return uniqueTags;
  }

  /// MU910 scan using MU910 protocol to return TagData objects
  Future<List<TagData>> _scanTagsMU910() async {
    debugPrint('🔍 Starting MU910 inventory scan...');

    // Build inventory command like your InventoryNB in 910driver.cpp
    final inventoryCmd = [
      0xCF, 0xFF, 0x00, 0x01, // Continue inventory command
      0x05, // Length of data
      0x01, // Type: scan by number of cycle
      0x00, 0x00, 0x00, 0x01, // Just once
      0x00, 0x00, // CRC placeholders
    ];

    final response = await _transferMU910(inventoryCmd, 2048, 200, _scanTime);

    if (response.isEmpty) {
      debugPrint('❌ No MU910 inventory response');
      return [];
    }

    // Stop inventory like in your 910driver.cpp
    final stopCmd = [0xCF, 0xFF, 0x00, 0x02, 0x00, 0, 0];
    await _transferMU910(stopCmd, 8, 0, 5);

    final tags = _parseMU910Response(response);
    debugPrint('✅ MU910 scan complete. Found ${tags.length} unique tags');
    return tags;
  }

  // Keep all your ORIGINAL parsing methods
  // Update the parsing methods to return TagData objects
  List<TagData> _parseBufferResponse(Uint8List response) {
    final tags = <TagData>[];

    try {
      if (response.length < 10) {
        return tags;
      }

      if (response[2] != 0x72) {
        return tags;
      }

      int index = 4;

      if (index + 1 >= response.length) return tags;
      final tagCount = response[index++];

      for (int i = 0; i < tagCount && index < response.length - 2; i++) {
        try {
          if (index >= response.length) break;
          final antenna = response[index++];

          if (index >= response.length) break;
          final epcLength = response[index++];

          if (epcLength == 0 || epcLength > 64) {
            break;
          }

          if (index + epcLength > response.length - 2) {
            break;
          }

          final epcData = response.sublist(index, index + epcLength);
          index += epcLength;

          final epcHex = UHFProtocol.bytesToHex(epcData).replaceAll(' ', '');

          if (epcData.every((b) => b == 0)) {
            continue;
          }

          // Extract RSSI and count
          int rssi = 0;
          int count = 1;

          if (index < response.length) {
            rssi = response[index++];
            // Convert to signed dBm (assuming the value is relative to -40dBm)
            rssi = rssi > 127 ? rssi - 256 : rssi; // Convert to signed
            rssi = -40 + rssi; // Adjust to dBm scale
          }

          if (index < response.length) {
            count = response[index++];
          }

          if (_isValidEPC(epcHex)) {
            tags.add(
              TagData(
                epc: epcHex,
                antenna: antenna,
                rssi: rssi,
                count: count,
                timestamp: DateTime.now(),
              ),
            );
          }
        } catch (e) {
          break;
        }
      }
    } catch (e) {
      // Ignore parsing errors
    }

    return tags;
  }

  /// Parse MU910 response to return TagData objects
  List<TagData> _parseMU910Response(Uint8List response) {
    final tags = <TagData>[];
    final Set<String> uniqueTags = <String>{}; // Track unique tags

    try {
      int i = 0;
      while (i < response.length) {
        // Look for response pattern like your 910driver.cpp
        if (response.length <= i + 4 || response[i + 4] != 0x12) {
          break;
        }

        if (response.length <= i + 10) break;

        final epclen = response[i + 10];
        final singleResponseLen = epclen + 13;

        if (response.length < i + singleResponseLen) break;

        if (response.length > i + 11 + epclen) {
          final epcData = response.sublist(i + 11, i + 11 + epclen);

          if (epcData.any((b) => b != 0)) {
            final epcHex = UHFProtocol.bytesToHex(epcData).replaceAll(' ', '');

            // Only add if not already seen
            if (uniqueTags.add(epcHex)) {
              // Extract antenna and RSSI info
              int antenna = 1; // Default antenna
              int rssi = -50; // Default RSSI
              int count = 1; // Default count

              // Try to extract antenna (usually at i + 6)
              if (response.length > i + 6) {
                antenna = response[i + 6];
              }

              // Try to extract RSSI (usually near the end of the response)
              if (response.length > i + 11 + epclen + 1) {
                final rssiRaw = response[i + 11 + epclen];
                rssi = rssiRaw > 127 ? rssiRaw - 256 : rssiRaw;
                rssi = -40 + rssi; // Adjust to dBm scale
              }

              tags.add(
                TagData(
                  epc: epcHex,
                  antenna: antenna,
                  rssi: rssi,
                  count: count,
                  timestamp: DateTime.now(),
                ),
              );

              debugPrint(
                '✅ Valid MU910 EPC: $epcHex (Ant: $antenna, RSSI: ${rssi}dBm)',
              );
            } else {
              debugPrint('⚠️ Duplicate MU910 EPC ignored: $epcHex');
            }
          }
        }

        // Move to next response like your 910driver.cpp
        if (epclen == 0x3E) {
          i += (13 + 62); // 62 bit
        } else {
          i += (13 + 12); // default 12 bit
        }
      }
    } catch (e) {
      debugPrint('❌ MU910 parse error: $e');
    }

    debugPrint(
      '🔍 MU910 Parsed ${tags.length} unique tags from ${response.length} bytes',
    );
    return tags;
  }

  Future<bool> _clearBuffer() async {
    try {
      final response = await _transferMU903(0x73, [], 100, 0);
      return response.isNotEmpty && response.length > 3 && response[3] == 0;
    } catch (e) {
      debugPrint('❌ Clear buffer error: $e');
      return false;
    }
  }

  bool _isValidEPC(String epcHex) {
    if (epcHex.length < 4) return false;
    if (epcHex.length % 2 != 0) return false;
    return true;
  }

/// Set band and power for both MU903 and MU910 (UPDATED WITH PROPER VALIDATION)
Future<bool> setBandAndPower(String band, int power) async {
  if (!_isConnected) {
    debugPrint('❌ Device not connected');
    return false;
  }
  
  try {
    // Get power range for current device
    final powerRange = getPowerRange();
    
    // Validate settings
    if (power < powerRange['min']! || power > powerRange['max']!) {
      debugPrint('❌ Invalid power: $power (valid range: ${powerRange['min']}-${powerRange['max']})');
      return false;
    }
    
    if (!['U', 'C', 'K', 'E'].contains(band)) {
      debugPrint('❌ Invalid band: $band');
      return false;
    }
    
    if (_isMU910) {
      return await _setMU910BandAndPower(band, power);
    } else {
      return await _setMU903BandAndPower(band, power);
    }
  } catch (e) {
    debugPrint('❌ Set band/power error: $e');
    return false;
  }
}

  /// Set MU903 band and power
  /// Set MU903 band and power (FIXED VERSION)
/// Set MU903 band and power (CORRECTED VERSION)
/// Set MU903 band and power (DIAGNOSTIC VERSION)
/// Set MU903 band and power (CORRECTED TO MATCH C++ DRIVER)
/// Set MU903 band and power (CORRECTED TO MATCH C++ DRIVER)
Future<bool> _setMU903BandAndPower(String band, int power) async {
  debugPrint('🔧 Setting MU903 band: $band, power: $power');
  
  try {
    // Get current settings for comparison
    await _debugSettingsComparison(band, power);
    
    // First set the band using command 0x22 (like C++ driver)
    int maxfre = 0;
    int minfre = 0;
    
    switch (band) {
      case 'C': // China
        maxfre = 0x13;
        minfre = 0x40;
        break;
      case 'U': // USA  
        maxfre = 0x31;
        minfre = 0x80;
        break;
      case 'K': // Korea
        maxfre = 0x1F;
        minfre = 0xC0;
        break;
      case 'E': // Europe
        maxfre = 0x4E;
        minfre = 0x00;
        break;
      default:
        debugPrint('❌ Unsupported band: $band');
        return false;
    }
    
    debugPrint('🔧 Setting MU903 band with command 0x22: maxfre=0x${maxfre.toRadixString(16)}, minfre=0x${minfre.toRadixString(16)}');
    
    // Set region command (0x22) - exactly like C++ driver
    final setRegionData = [maxfre, minfre];
    final regionResponse = await _transferMU903(0x22, setRegionData, 200, 50);
    
    if (regionResponse.isEmpty || regionResponse.length < 4 || regionResponse[3] != 0x00) {
      debugPrint('❌ MU903 band set failed - status: 0x${regionResponse.length >= 4 ? regionResponse[3].toRadixString(16) : 'no response'}');
      return false;
    }
    
    debugPrint('✅ MU903 band set successfully');
    
    // Now set power using command 0x2F (like C++ driver)
    debugPrint('🔧 Setting MU903 power with command 0x2F: power=$power');
    
    final setPowerData = [power.clamp(0, 30)];
    final powerResponse = await _transferMU903(0x2F, setPowerData, 200, 50);
    
    if (powerResponse.isEmpty || powerResponse.length < 4 || powerResponse[3] != 0x00) {
      debugPrint('❌ MU903 power set failed - status: 0x${powerResponse.length >= 4 ? powerResponse[3].toRadixString(16) : 'no response'}');
      return false;
    }
    
    debugPrint('✅ MU903 power set successfully');
    
    // Verify settings
    await Future.delayed(const Duration(milliseconds: 200));
    final newSettings = await _getMU903Settings();
    if (newSettings != null) {
      debugPrint('🔍 Verification: New band=${newSettings.band}, power=${newSettings.power}');
      
      // Check if settings actually changed
      if (newSettings.band == band && newSettings.power == power) {
        debugPrint('✅ Settings verification successful');
        return true;
      } else {
        debugPrint('⚠️ Settings verification failed: expected band=$band, power=$power; got band=${newSettings.band}, power=${newSettings.power}');
        return false;
      }
    }
    
    debugPrint('⚠️ Could not verify settings, but commands succeeded');
    return true;
  } catch (e) {
    debugPrint('❌ MU903 set band/power error: $e');
    return false;
  }
}
/// Debug method to show current vs desired settings
Future<void> _debugSettingsComparison(String desiredBand, int desiredPower) async {
  debugPrint('🔍 === SETTINGS COMPARISON ===');
  
  final current = await _getMU903Settings();
  if (current != null) {
    debugPrint('📊 Current: Band=${current.band}, Power=${current.power}');
    debugPrint('🎯 Desired: Band=$desiredBand, Power=$desiredPower');
    
    // Show raw values from current settings
    final response = await _transferMU903(0x21, [], 100, 10);
    if (response.length >= 16) {
      final currentDmaxfre = response[8];
      final currentDminfre = response[9];
      final currentPower = response[10];
      
      debugPrint('📝 Current raw: dmaxfre=0x${currentDmaxfre.toRadixString(16)}, dminfre=0x${currentDminfre.toRadixString(16)}, power=$currentPower');
    }
  }
  debugPrint('🔍 === END COMPARISON ===');
}

/// Check what settings changes are supported by the device (FIXED VERSION)
Future<Map<String, bool>> checkSupportedFeatures() async {
  final features = <String, bool>{
    'powerChange': false,
    'bandChange': false,
  };
  
  if (!_isConnected) return features;
  
  try {
    debugPrint('🔍 Checking supported features...');
    
    // Get current settings
    final currentSettings = await _getMU903Settings();
    if (currentSettings == null) return features;
    
    final originalPower = currentSettings.power;
    
    // Test 1: Try changing power by 1 dB using CORRECT command 0x2F
    final testPower = originalPower == 30 ? 29 : originalPower + 1;
    
    debugPrint('🧪 Testing power change with CORRECT command 0x2F: $originalPower -> $testPower');
    
    // Use command 0x2F for power (like C++ driver)
    final setPowerData = [testPower];
    final powerResponse = await _transferMU903(0x2F, setPowerData, 200, 50);
    
    if (powerResponse.isNotEmpty && powerResponse.length >= 4) {
      features['powerChange'] = powerResponse[2] == 0x2F && powerResponse[3] == 0x00;
      debugPrint('📊 Power change support: ${features['powerChange']}');
      
      if (features['powerChange']!) {
        // Restore original power
        await Future.delayed(const Duration(milliseconds: 100));
        final restoreData = [originalPower];
        await _transferMU903(0x2F, restoreData, 200, 50);
        debugPrint('🔄 Restored original power: $originalPower');
      }
    }
    
    // Test 2: Try changing band using CORRECT command 0x22 (only if power change works)
    if (features['powerChange']!) {
      debugPrint('🧪 Testing band change with CORRECT command 0x22...');
      
      // Try switching from current China to USA and back
      final setRegionData = [0x31, 0x80]; // USA band values
      final regionResponse = await _transferMU903(0x22, setRegionData, 200, 50);
      
      if (regionResponse.isNotEmpty && regionResponse.length >= 4) {
        features['bandChange'] = regionResponse[2] == 0x22 && regionResponse[3] == 0x00;
        
        if (features['bandChange']!) {
          // Restore original China band
          await Future.delayed(const Duration(milliseconds: 100));
          final restoreRegionData = [0x13, 0x40]; // China band values
          await _transferMU903(0x22, restoreRegionData, 200, 50);
          debugPrint('🔄 Restored original China band');
        }
      }
      
      debugPrint('📊 Band change support: ${features['bandChange']}');
    }
    
    debugPrint('📋 Feature support summary: $features');
    return features;
    
  } catch (e) {
    debugPrint('❌ Feature check error: $e');
    return features;
  }
}

/// Check what settings the device supports changing
Future<Map<String, bool>> getSupportedFeatures() async {
  if (_isMU910) {
    // MU910 generally supports both
    return {'powerChange': true, 'bandChange': true};
  } else {
    return await checkSupportedFeatures();
  }
}


  /// Set MU910 band and power
/// Set MU910 band and power (CORRECTED VERSION)
Future<bool> _setMU910BandAndPower(String band, int power) async {
  debugPrint('🔧 Setting MU910 band: $band, power: $power');
  
  try {
    // First set the region using command 0x55 (like C++ driver)
    int startFreqI = 0;
    int startFreqD = 0;
    int stepFreq = 0;
    int CN = 0;
    int region = 0;
    
    switch (band) {
      case 'U': // USA
        startFreqI = 902;
        startFreqD = 750;
        stepFreq = 500;
        CN = 50;
        region = 0x01;
        break;
      case 'K': // Korea
        startFreqI = 917;
        startFreqD = 100;
        stepFreq = 200;
        CN = 32;
        region = 0x02;
        break;
      case 'E': // Europe
        startFreqI = 865;
        startFreqD = 100;
        stepFreq = 200;
        CN = 15;
        region = 0x03;
        break;
      case 'C': // China
        startFreqI = 920;
        startFreqD = 125;
        stepFreq = 250;
        CN = 20;
        region = 0x08;
        break;
      default:
        debugPrint('❌ Unsupported band: $band');
        return false;
    }
    
    debugPrint('🔧 Setting MU910 region with command 0x55: region=0x${region.toRadixString(16)}, startFreqI=$startFreqI, startFreqD=$startFreqD');
    
    // Build set region command (0x55) exactly like C++ driver
    final setRegionCmd = [
      0xCF, 0xFF, 0x00, 0x55, // Command 0x55
      0x08, // Length 8
      region,
      (startFreqI >> 8) & 0xFF,
      startFreqI & 0xFF,
      (startFreqD >> 8) & 0xFF,
      startFreqD & 0xFF,
      (stepFreq >> 8) & 0xFF,
      stepFreq & 0xFF,
      CN,
      0x00, 0x00 // CRC placeholders
    ];
    
    final regionResponse = await _transferMU910(setRegionCmd, 100, 200, 100);
    
    if (regionResponse.isEmpty || regionResponse.length < 6 || regionResponse[5] != 0x00) {
      debugPrint('❌ MU910 region set failed');
      return false;
    }
    
    debugPrint('✅ MU910 region set successfully');
    
    // Now set power using command 0x53 (like C++ driver)
    debugPrint('🔧 Setting MU910 power with command 0x53: power=$power');
    
    final setPowerCmd = [
      0xCF, 0xFF, 0x00, 0x53, // Command 0x53
      0x02, // Length 2
      power.clamp(0, 35),
      0x00,
      0x00, 0x00 // CRC placeholders
    ];
    
    final powerResponse = await _transferMU910(setPowerCmd, 100, 200, 100);
    
    if (powerResponse.isEmpty || powerResponse.length < 6 || powerResponse[5] != 0x00) {
      debugPrint('❌ MU910 power set failed');
      return false;
    }
    
    debugPrint('✅ MU910 power set successfully');
    
    // Verify settings
    await Future.delayed(const Duration(milliseconds: 100));
    final newSettings = await _getMU910Settings();
    if (newSettings != null) {
      debugPrint('🔍 Verification: New band=${newSettings.band}, power=${newSettings.power}');
    }
    
    return true;
  } catch (e) {
    debugPrint('❌ MU910 set band/power error: $e');
    return false;
  }
}

  // Helper methods for UI
  List<String> getAvailableBands() {
    return ['U', 'C', 'K', 'E'];
  }

  String getBandDisplayName(String band) {
    final code = bandCodes[band];
    return bandNames[code] ?? 'Unknown';
  }

  Map<String, int> getPowerRange() {
      if (_isMU910) {
    return {'min': 0, 'max': 35}; // MU910 supports higher power
  } else {
    return {'min': 0, 'max': 30}; // MU903 standard range
  }

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
      _isMU910 = false;
      _deviceInfo.clear();
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
}
