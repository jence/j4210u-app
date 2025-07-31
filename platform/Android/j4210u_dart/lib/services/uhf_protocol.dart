import 'dart:typed_data';

class UHFProtocol {
  // Protocol constants from your driver.cpp
  static const int PRESET_VALUE = 0xFFFF;
  static const int POLYNOMIAL = 0x8408;
  
  /// Generate CRC16 exactly like your driver.cpp gencrc function
  static int generateCRC(Uint8List data) {
    int crc = PRESET_VALUE;
    
    for (int i = 0; i < data.length; i++) {
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
  
  /// Verify CRC exactly like your driver.cpp verifycrc function
  static bool verifyCRC(Uint8List data) {
    if (data.isEmpty) return false;
    
    final len = data[0]; // first byte contains the length
    if (data.length < len + 1) return false;
    
    final crc = generateCRC(data.sublist(0, len - 1));
    final crcl = crc & 0xFF;
    final crch = (crc >> 8) & 0xFF;
    
    if (data.length >= len + 1) {
      return data[len - 1] == crcl && data[len] == crch;
    }
    return false;
  }
  
  /// Build command exactly like your driver.cpp transfer function
  static Uint8List buildCommand(int command, [List<int> data = const []]) {
    final List<int> packet = [];
    
    // Calculate command size like in your driver
    final cmdsize = 3 + data.length + 2; // length + address + command + data + 2 CRC bytes
    
    packet.add(cmdsize - 1); // command[0] = cmdsize - 1
    packet.add(0x00);        // address (always 0x00)
    packet.add(command);     // command
    packet.addAll(data);     // data
    
    // Calculate CRC like in your driver: gencrc(command, cmdsize - 2)
    final crc = generateCRC(Uint8List.fromList(packet));
    packet.add(crc & 0xFF);        // command[cmdsize-2] = crc & 0xFF
    packet.add((crc >> 8) & 0xFF); // command[cmdsize-1] = (crc >> 8)
    
    return Uint8List.fromList(packet);
  }
  
  /// Convert bytes to hex string
  static String bytesToHex(Uint8List bytes) {
    return bytes.map((b) => b.toRadixString(16).padLeft(2, '0')).join(' ').toUpperCase();
  }
  
  /// Convert hex string to bytes
  static Uint8List hexToBytes(String hex) {
    final result = <int>[];
    for (int i = 0; i < hex.length; i += 2) {
      final hexPair = hex.substring(i, i + 2);
      result.add(int.parse(hexPair, radix: 16));
    }
    return Uint8List.fromList(result);
  }
}