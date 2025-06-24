import 'dart:typed_data';

class MU910Protocol {
  static const int PRESET_VALUE = 0xFFFF;
  static const int POLYNOMIAL = 0x8408;
  
  /// Generate CRC for MU910 (exactly matching your C code)
  static int generateCRC(Uint8List data, int n) {
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
  
  /// Verify CRC for MU910 (matching your C code verifycrc function)
  static bool verifyCRC(Uint8List data) {
    if (data.length < 7) return false; // Minimum response size
    
    final len = data[4];
    final rawSize = len + 5;
    
    if (data.length < rawSize + 2) return false;
    
    final crc = generateCRC(data, rawSize);
    final crcl = crc & 0xFF;
    final crch = (crc >> 8) & 0xFF;
    
    // In your C code: data[rawSize] == crch && data[rawSize + 1] == crcl
    return data[rawSize] == crch && data[rawSize + 1] == crcl;
  }
  
  /// Build command for MU910 (matching your C transfer function)
  static Uint8List buildCommand(List<int> commandData) {
    final List<int> packet = List.from(commandData);
    final cmdsize = packet.length;
    
    // Calculate CRC on everything except the last 2 bytes (CRC placeholders)
    final crc = generateCRC(Uint8List.fromList(packet), cmdsize - 2);
    
    // From your C code: command[cmdsize-2] = (crc >> 8); command[cmdsize-1] = crc & 0xFF;
    packet[cmdsize - 2] = (crc >> 8) & 0xFF; // CRC high byte
    packet[cmdsize - 1] = crc & 0xFF;        // CRC low byte
    
    return Uint8List.fromList(packet);
  }
  
  /// Empty serial buffer (like emptyserial in your C code)
  static Uint8List buildEmptyCommand() {
    // Just a dummy read command to clear buffer
    return buildCommand([0xCF, 0xFF, 0x00, 0x72, 0, 0, 0]);
  }
}