import 'dart:typed_data';
import 'services/uhf_protocol.dart';

class MU903Protocol {
  static const int PRESET_VALUE = 0xFFFF;
  static const int POLYNOMIAL = 0x8408;
  
  /// Generate CRC for MU903 (same as your existing UHFProtocol)
  static int generateCRC(Uint8List data) {
    return UHFProtocol.generateCRC(data);
  }
  
  /// Verify CRC for MU903
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
  
  /// Build command for MU903
  static Uint8List buildCommand(int command, [List<int> data = const []]) {
    final List<int> packet = [];
    final cmdsize = 3 + data.length + 2; // length + address + command + data + 2 CRC bytes
    
    packet.add(cmdsize - 1); // command[0] = cmdsize - 1
    packet.add(0x00);        // address (always 0x00)
    packet.add(command);     // command
    packet.addAll(data);     // data
    
    final crc = generateCRC(Uint8List.fromList(packet));
    packet.add(crc & 0xFF);        // command[cmdsize-2] = crc & 0xFF
    packet.add((crc >> 8) & 0xFF); // command[cmdsize-1] = (crc >> 8)
    
    return Uint8List.fromList(packet);
  }
}